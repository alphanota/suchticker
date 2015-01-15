package com.parlanto.suchticker;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


public class WidgetProvider extends AppWidgetProvider {

	public final static String shareResults = "SHARE_RESULTS";	
    Context c_context;
    int appWidgetIds[];
    AppWidgetManager appWidgetManager;
    SharedPreferences sharedPrefs;
    double doge_usd_price = 0.00;
    double doge_btc_price = 0.00;
    String[] last_results;
   
   
    Handler mHandler = new Handler(Looper.getMainLooper() ) {
		
		
		@Override
		public void handleMessage(Message inputMessage){
			
			Bundle data = inputMessage.getData();
			String results[] = data.getStringArray("RESULT");
			updateView(results);
		}
		
		
	};
    
    
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
    	int[] appWidgetIds) {
	    c_context = context;
	    
	    //infodl = new InfoDownloader(mHandler);
	    this.appWidgetManager = appWidgetManager;
        // Get all ids
	    this.appWidgetIds = appWidgetIds;
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
    
        sharedPrefs = context.getSharedPreferences("update_wifi_only", Context.MODE_PRIVATE);
	
        
        boolean defaultValue = false; //update over wifi and mobile network by default
        boolean update_wifi_only = sharedPrefs.getBoolean(
        		context.getResources().getString(R.string.update_wifi_only), defaultValue);
	
        //checks if there is a stable internet connection ... aborts data download if there isn't one
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		//if there are no active connections
		if (networkInfo == null){
			Toast.makeText(c_context, "No internet connection!", Toast.LENGTH_SHORT);
			return;
		}
		
		boolean is_wifi_connected;
		if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) is_wifi_connected = true;
		else is_wifi_connected = false;
		 
		 /**/
		boolean is_pref_connection;
		 if(update_wifi_only == true && is_wifi_connected == false) {
			 is_pref_connection = false;
			Toast.makeText(context, R.string.wifi_fail, Toast.LENGTH_SHORT).show();
			return;
		 }
		 else is_pref_connection = true;
		
		 if (networkInfo != null && networkInfo.isConnected() && is_pref_connection) {
			 int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		     for (int widgetId : allWidgetIds) {
		    	 new InfoDownloader(mHandler).execute(widgetId);
		    	 // Register an onClickListener
		    	 Intent intent = new Intent(context, WidgetProvider.class);
		    	 intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		    	 intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		    	 PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	      			 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		    	 RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
	      	         R.layout.widget_layout);
		    	 remoteViews.setOnClickPendingIntent(R.id.doge_widget_text, pendingIntent);
		    	 Intent s_intent = new Intent(context,MainActivity.class);
		    	 s_intent.putExtra("RESULTS", last_results);
		    	 PendingIntent pintent = PendingIntent.getActivity(context, 0, s_intent, PendingIntent.FLAG_UPDATE_CURRENT);
		    	 remoteViews.setOnClickPendingIntent(R.id.dogecoin_logo,pintent);
		    	 appWidgetManager.updateAppWidget(widgetId, remoteViews);
		    }
		 } 
		 else {
			 // display error
		     Toast.makeText(context, R.string.internet_fail, Toast.LENGTH_SHORT).show();
		     return;
		 }

  }
  
  void startMainActivity(String[] results){
	  
	  Intent s_intent = new Intent("WIDGET"); 
	  s_intent.putExtra("RESULTS", results);
	  PendingIntent pintent = PendingIntent.getBroadcast(c_context, 0, s_intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    try {
			pintent.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
  }
  
  
  
  void updateView(String[] result){
	  
	  
	  if(result == null){
			 Toast.makeText(c_context, R.string.internet_fail, Toast.LENGTH_SHORT).show();
			 return;
		 }
	
	  
	      RemoteViews remoteViews = new RemoteViews(c_context.getPackageName(),
	          R.layout.widget_layout);
	      Log.d("suchticker", result[0]);
	      // Set the remoteviews
	      remoteViews.setTextViewText(R.id.update_dogecoin_usd,result[0]);
	      remoteViews.setTextViewText(R.id.update_dogecoin_btc, result[1]);
	      remoteViews.setTextViewText(R.id.update_dogecoin_time, "Last checked "+result[2]);

	      
	      int color_usd = Color.GREEN;
	      int color_btc = Color.GREEN;
	      
	      if (result[3].equals("FALSE")) color_usd = Color.RED;
	      if (result[4].equals("FALSE")) color_btc = Color.RED;
	      
	      remoteViews.setTextColor(R.id.update_dogecoin_usd, color_usd);
	      remoteViews.setTextColor(R.id.update_dogecoin_btc, color_btc);
	      
	      last_results = result;
	      int wids[] = this.appWidgetIds;
	      for(int wid:wids){
	      appWidgetManager.updateAppWidget(wid, remoteViews);
	      }// Register an onClickListener
	      //starMainAcitivty updates the mainActivity with the prices we just downloaded
	      startMainActivity(result);
 
	  
	  
	  
	  
  }
  
  
} 