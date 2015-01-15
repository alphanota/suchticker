package com.parlanto.suchticker;




import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	
	SharedPreferences sharedPrefs;
	SharedPreferences.Editor editor;

	double doge_usd_price;
	double doge_btc_price;
	

    Handler mHandler = new Handler(Looper.getMainLooper() ) {
		
		
		@Override
		public void handleMessage(Message inputMessage){
			
			Bundle data = inputMessage.getData();
			String results[] = data.getStringArray("RESULT");
			updateView(results);
		}
		
		
	};
    
	
	
	
	
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sharedPrefs = getSharedPreferences("update_wifi_only", Context.MODE_PRIVATE);
		editor = sharedPrefs.edit();
		editor.commit();
		boolean t = sharedPrefs.getBoolean(getResources().getString(R.string.update_wifi_only), false);
		CheckBox check = (CheckBox)findViewById(R.id.main_update_wifi_only);
		check.setChecked(t);
		
		update();
		
	}
		
		
	
	@Override
	protected void onResume(){
     super.onResume();		
    // registerReceiver(receiver, new IntentFilter(Uploader.NOTIFICATION));
     registerReceiver(lreceiver, new IntentFilter("WIDGET"));
	}
	
	@Override 
	protected void onPause(){
		super.onPause();
		unregisterReceiver(lreceiver);
		
	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	
	public void update(){

		if (widgetExists()){
			
			//Toast.makeText(this, "active widgets", Toast.LENGTH_SHORT).show();
			
		Intent intent = new Intent(this,WidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
		// since it seems the onUpdate() is only fired on that:
		int[] ids = {R.layout.widget_info};
		
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
		sendBroadcast(intent);
		}
		
		else{
			updatePrice();
		}
		
		
		
		
		
	}
	
	
	
	public void updatePrice(View view){
		update();
	}
	
	public void savePreferences(View view){
		
		CheckBox check = (CheckBox)findViewById(R.id.main_update_wifi_only);
		boolean isChecked = check.isChecked();
		
		editor.putBoolean(getString(R.string.update_wifi_only), isChecked);
		editor.commit();
		
		Toast.makeText(this, "Such Preferences! Very safe! Wow!" , Toast.LENGTH_SHORT).show();
	}
	
	
	   private BroadcastReceiver lreceiver = new BroadcastReceiver() {

		     @Override
		     public void onReceive(Context context, Intent intent) {
					System.out.println("Loginreciever caught ");
		    	 
		       Bundle bundle = intent.getExtras();
		       if (bundle != null) {
		    	   	String[] results = bundle.getStringArray("RESULTS");
					if(results!= null){
						
						
					updateView(results);
						
						
					}
		       	}//bundle!=null
		     } //overr. onReceive
		   }; //new broadcast
	
		   
		   
		   
		   
		   
		   void updatePrice(){
			   
			   boolean defaultValue = false; //update over wifi and mobile network by default
			   boolean update_wifi_only = sharedPrefs.getBoolean(
						getResources().getString(R.string.update_wifi_only
						), defaultValue);
			   
			 //checks if there is a stable internet connection ... aborts data download if there isn't one
	      		 ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	      		 NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	      		 
	      		if (networkInfo == null){
	      			Toast.makeText(this, R.string.internet_fail, Toast.LENGTH_SHORT).show();
	      			return;
	      		}
	      		 boolean is_wifi_connected;
	      		 if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) is_wifi_connected = true;
	      		 else is_wifi_connected = false;
	      		 
	      		 
	      		 /**/
	      		 boolean is_pref_connection;
	      		 if(update_wifi_only == true && is_wifi_connected == false) {
	      			 is_pref_connection = false;
	      			Toast.makeText(this, "No wifi!", Toast.LENGTH_SHORT).show();
	      		 }
	      		 else is_pref_connection = true;
	      		 
	      		 
	      		    if (networkInfo != null && networkInfo.isConnected() && is_pref_connection) {
	      		        
	      		    	
	      		    	new InfoDownloader(mHandler).execute();
	      		    } else {
	      		        // display error
	      		    	Toast.makeText(this, R.string.internet_fail, Toast.LENGTH_SHORT).show();
	      		    }

			   
			   
			   
			   
		   }
		   
		   
		   
		   void updateView(String [] result){
		   if(result == null){
					 Toast.makeText(this, R.string.internet_fail, Toast.LENGTH_SHORT).show();
					 return;
				 }    
		        TextView main_usd = (TextView)findViewById(R.id.main_update_dogecoin_usd);
				TextView main_btc = (TextView)findViewById(R.id.main_update_dogecoin_btc);
				TextView main_time = (TextView)findViewById(R.id.main_update_dogecoin_time);
				
				main_usd.setText(result[0]);
				main_btc.setText(result[1]);
				main_time.setText("Last checked: " +result[2]);
				
		        int color_usd = Color.GREEN;
		        int color_btc = Color.GREEN;
		        
		      if (result[3].equals("FALSE")) color_usd = Color.RED;
		      if (result[4].equals("FALSE")) color_btc = Color.RED;
		      
		      main_usd.setTextColor(color_usd);
		      main_btc.setTextColor(color_btc);
		      
			  
			  

		   }
		   
		   
	
		   			  
		  
		  private boolean widgetExists() {
			    ComponentName myWidget = new ComponentName(this, WidgetProvider.class);
			    int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(myWidget);
			    return ids.length > 0;
			}
		   

}
