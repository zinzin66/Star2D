package com.star4droid.star2d.evo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.star4droid.star2d.DebugActivity;
import com.star4droid.star2d.Helpers.EngineSettings;
import com.star4droid.star2d.Helpers.FileUtil;
import com.star4droid.star2d.Utils;

public class star2dApp extends Application {
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	private static Context mApplicationContext;
	public static Context getContext() {
		return mApplicationContext;
	}
	public void onCreate() {
		EngineSettings.init(this);
		Utils.setLanguage(this);
		new com.github.anrwatchdog.ANRWatchDog(7000).setANRListener(new ANRWatchDog.ANRListener() {
    		@Override
    		public void onAppNotResponding(ANRError error) {
        		error.fillInStackTrace();
				try {
					FileUtil.writeFile(getExternalFilesDir("logs")+"/not.respond.txt","");
					error.printStackTrace(new java.io.PrintStream(new java.io.File(getExternalFilesDir("logs")+"/not.respond.txt")));
				} catch(Exception e){}
				FileUtil.writeFile(getExternalFilesDir("logs")+"/error.txt",error.toString() + ",full :\n"+Utils.getStackTraceString(error));
        		//ExceptionHandler.saveException(error, new CrashManager());
    		}
		}).start();
		mApplicationContext = this.getApplicationContext();
		//com.star4droid.star2d.Helpers.CustomStrictMode.enableStrictMode();
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			
			public void uncaughtException(Thread thread, Throwable throwable) {
				final Intent intent = new Intent(mApplicationContext, DebugActivity.class);
				intent.putExtra("error", Log.getStackTraceString(throwable));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);
				int x=0;
				String str=FileUtil.getPackageDataDir(star2dApp.this)+"/logs/log"+"%1$s"+".txt";
				while(FileUtil.isExistFile(FileUtil.getPackageDataDir(star2dApp.this)+"/logs/log"+x+".txt")){
					x++;
				}
				String log=Log.getStackTraceString(throwable);
				if(!FileUtil.readFile(String.format(str,(x-1)+"")).equals(log))
				FileUtil.writeFile(String.format(str,x+""),log);
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
				Process.killProcess(Process.myPid());
				mApplicationContext.startActivity(intent);
				//System.exit(1);
				//Logger.initialize(this);
				star2dApp.this.uncaughtExceptionHandler.uncaughtException(thread, throwable);
			}
		});
        
           
		super.onCreate();
	}
}