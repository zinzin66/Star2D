package com.star4droid.star2d.Items;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import com.star4droid.star2d.Helpers.FileUtil;
import com.star4droid.star2d.Helpers.PropertySet;
import com.star4droid.star2d.evo.R;
import com.star4droid.star2d.Utils;

public class TextItem extends AppCompatTextView implements EditorItem {
	Editor editor;
	PropertySet<String, Object> propertySet;
	float sx = 0.0f;
	float sy = 0.0f;
	
	public TextItem(Context context) {
		super(context);
		this.setAutoSizeTextTypeUniformWithConfiguration(1,100000,1,AppCompatTextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
		setLayoutParams(new LayoutParams(50, 50));
		setTextColor(Color.WHITE);
		setPadding(0,0,0,0);
		setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
			}
		});
	}
	
	public void setProperties(PropertySet<String, Object> properties) {
		propertySet = properties;
		PropertySet<String,Object> temp = PropertySet.getDefualt(this,"text.json");
		for(String s:temp.keySet()){
			if(!propertySet.containsKey(s)){
				propertySet.put(s,temp.get(s));
			}
		}
		update();
		
		setTextColor(Color.parseColor(propertySet.getString("Text Color")));
	}
	
	public TextItem setDefault() {
		propertySet = PropertySet.getDefualt(this,"text.json");
		if(propertySet==null) Log.e(Utils.error_tag,"Null PropertySet");
		return this;
	}
	
	public PropertySet<String, Object> getPropertySet() {
		return this.propertySet;
	}
	
	public void update() {
		if (this.editor == null) {
			this.editor = (Editor) getParent();
		}
		
		if (propertySet != null) {
			int h = (int) propertySet.getFloat("height");
			int w = (int) this.propertySet.getFloat("width");
			float moveX = this.editor.getMoveX();
			float moveY = this.editor.getMoveY();
			float x = this.propertySet.getFloat("x");
			float y = this.propertySet.getFloat("y");
			setVisibility(propertySet.getString("Visible").equals("true")?VISIBLE:GONE);
			float editorScale = this.editor.getEditorScale();
			float measuredWidth = this.editor.getScreenView().getMeasuredWidth() / 2.0f;
			float measuredHeight = this.editor.getScreenView().getMeasuredHeight() / 2.0f;
			setX((((moveX + x) - measuredWidth) * editorScale) + measuredWidth);
			setY((((moveY + y) - measuredHeight) * editorScale) + measuredHeight);
			setZ(this.propertySet.getFloat("z"));
			getLayoutParams().height = Math.max((int)(h * editorScale), 1);
			getLayoutParams().width = Math.max((int)(w * editorScale), 1);
			setLayoutParams(getLayoutParams());
			setRotation(this.propertySet.getFloat("rotation"));
			setText(propertySet.getString("Text"));
		}
	}
	
	public boolean onTouchEvent(MotionEvent motionEvent) {
	    if(!isEnabled()) return false;
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			this.sx = motionEvent.getRawX();
			this.sy = motionEvent.getRawY();
			editor.disableTouchExcept(this);
		}
		if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL) {
			float X = this.sx - motionEvent.getRawX();
			float Y = this.sy - motionEvent.getRawY();
			double dist = Math.sqrt((X*X)+(Y*Y));
			if (dist<= 80) {
				editor.selectView(this);
			}
			editor.enableTouch();
		}
		//MotionEvent event = MotionEvent.obtain(android.os.SystemClock.uptimeMillis(),android.os.SystemClock.uptimeMillis(),motionEvent.getAction(),getX()+motionEvent.getX(),getY()+motionEvent.getY(),0);
		editor.onTouchEvent(motionEvent);
		//this.editor.dispatchTouchEvent(motionEvent);
		return true;
	}
	
	
	@Override
	public String getTypeName(){
		return "Text";
	}
}