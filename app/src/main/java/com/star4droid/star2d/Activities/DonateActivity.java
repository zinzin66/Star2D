package com.star4droid.star2d.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.ProductDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.star4droid.star2d.evo.R;

public class DonateActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final String TAG = "DonateActivity";

    private BillingClient billingClient;
    private TextView tvStatus;

    private final String ID_1 = "donation_1_usd";
    private final String ID_3 = "donation_3_usd";
    private final String ID_5 = "donation_5_usd";

    private final List<String> productIds = new ArrayList<>();
    private final HashMap<String, ProductDetails> productDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        tvStatus = findViewById(R.id.tvStatus);

        productIds.add(ID_1);
        productIds.add(ID_3);
        productIds.add(ID_5);

        Button b1 = findViewById(R.id.btnDonate1);
        Button b3 = findViewById(R.id.btnDonate3);
        Button b5 = findViewById(R.id.btnDonate5);

        b1.setOnClickListener(v -> purchaseProduct(ID_1));
        b3.setOnClickListener(v -> purchaseProduct(ID_3));
        b5.setOnClickListener(v -> purchaseProduct(ID_5));

        setupBillingClient();
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(com.android.billingclient.api.PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .setListener(this)
            .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    log("Billing setup OK");
                    queryProducts();
                } else {
                    log("Billing setup failed: " + billingResult.getDebugMessage());
                    tvStatus.setText("Payment service setup error: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                log("Billing service disconnected. Try reconnect.");
            }
        });
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        for (String id : productIds) {
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, queryProductDetailsResult) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
            if (productDetailsList != null) {
                for (ProductDetails pd : productDetailsList) {
                    productDetailsMap.put(pd.getProductId(), pd);
                }
                runOnUiThread(() -> tvStatus.setText("Products are ready for purchase"));
            }
        } else runOnUiThread(() -> tvStatus.setText("Failed to fetch product details"));
    });
    }

    private void purchaseProduct(String productId) {
        ProductDetails pd = productDetailsMap.get(productId);
        if (pd == null) {
            Toast.makeText(this, "Product details are not available now.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductDetails.OneTimePurchaseOfferDetails offer = pd.getOneTimePurchaseOfferDetails();
        if (offer == null) {
            Toast.makeText(this, "Purchase offer is not available for this product.", Toast.LENGTH_SHORT).show();
            return;
        }

        BillingFlowParams.ProductDetailsParams productDetailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(pd)
                        .setOfferToken(offer.getOfferToken())
                        .build();

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productDetailsParams))
                .build();

        BillingResult result = billingClient.launchBillingFlow(this, flowParams);
        log("launchBillingFlow result: " + result.getDebugMessage());
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Purchase cancelled", Toast.LENGTH_SHORT).show();
        } else {
            log("onPurchasesUpdated error: " + billingResult.getDebugMessage());
            Toast.makeText(this, "Purchase error: " + billingResult.getDebugMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        // 1) Verify the purchase status locally, then, preferably, verify it on the server using the Google Play Developer API.
        // 2) When the verification is valid, award the user the benefit or record the donation on the server.
        // 3) Consume the product.
        consumePurchase(purchase.getPurchaseToken());
    }

    private void consumePurchase(String purchaseToken) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build();

        ConsumeResponseListener listener = (billingResult, outToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                log("Consume OK");
                runOnUiThread(() -> tvStatus.setText("Thank you for your donation. You can donate again.."));
            } else {
                log("Consume failed: " + billingResult.getDebugMessage());
                runOnUiThread(() -> tvStatus.setText("Purchase consumption failure: " + billingResult.getDebugMessage()));
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    @Override
    protected void onDestroy() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
        super.onDestroy();
    }
}