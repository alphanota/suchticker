package com.parlanto.suchticker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

public class InfoDownloader extends AsyncTask <Integer, Integer, String[]> {
		
	Handler mHandler;
    
    InfoDownloader(Handler h){
    	mHandler = h;
    }
    
    
	@Override
	/**
	 *downloads network price info, formats downloaded data to be displayed in UI 
	 */
	protected String[] doInBackground(Integer... params) {
	
	
	URL url = null;
	try {
		url = new URL("http://www.coinmarketcap.com");
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	String html = "";
	
	
	/* store for reference
	Calendar cal = Calendar.getInstance();
	int hours = cal.get(Calendar.HOUR_OF_DAY);
	int minutes = cal.get(Calendar.MINUTE);
	int seconds = cal.get(Calendar.SECOND);
	*/
	
	//webpage is being downloaded V V V z
	HttpURLConnection urlConnection = null;
	try {
		urlConnection = (HttpURLConnection) url.openConnection();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
		return null;
	} 
	InputStream in = null;
	BufferedReader reader = null;
	String line = "";
		try {
			in = new BufferedInputStream(urlConnection.getInputStream());
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder build_html = new StringBuilder();
			
			while( (line = reader.readLine() ) != null){
				build_html.append(line);
			}
			
			html = build_html.toString();
		}
		
	 catch(IOException e){
		 e.printStackTrace();
		 return null;
	 }
		finally{

				try {
					if(in != null) in.close();
					if(reader!= null)reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
		
		}
		
	
	//webpage downloaded at this point into string html
	//now the string data with html tags will be parsed
		
		
		try{
			Document doc = null;
			//if there were issues downloading webpage html string variable will be null
			doc = Jsoup.parse(html);
			Element content = doc.getElementById("id-dogecoin");
			Elements links= content.getElementsByClass("price");
			
			String doge_usd = "";
			String doge_btc = "";
			
			for (Element link : links) {
			  doge_usd = link.attr("data-usd");
			  doge_btc = link.attr("data-btc");
			  System.out.println(doge_usd);
			  System.out.println(doge_btc);
			  }	
			
			Time time = new Time();
			time.setToNow();
			
			
			String expanded_doge_usd = expandDoubleString(doge_usd,8);
			String expanded_doge_btc = expandDoubleString(doge_btc,8);
			
			double double_doge_usd = getDouble(doge_usd,8);
			double double_doge_btc = getDouble(doge_btc,8);
			
			String usd_up = "FALSE";
			String btc_up = "FALSE";
			
			//man strftime for format specifications 
			//format("%I:%M:%S %p") wile print time as 07:31:24 PM
			//H instead of I will print as 24 hour format
			String formatted_time = time.format("%I:%M:%S %p");
			
			return new String[]{"USD: "+expanded_doge_usd,"BTC: "+expanded_doge_btc, formatted_time, "TRUE","TRUE"};
			
		} catch (NullPointerException e){
			return null;
		}
		
} // end doInBackground()

	
//We are back on the main UI Thread. We update the UI Views if the download was successful
@Override
	protected void onPostExecute(String[] result) {
	Bundle bundle = new Bundle();
	bundle.putStringArray("RESULT", result);
	Message updateMessage = mHandler.obtainMessage();
	updateMessage.setData(bundle);
    updateMessage.sendToTarget();
}

//converts double string in a number 
public String expandDoubleString(String number, int decimals ){
	double d = Double.parseDouble(number);
    DecimalFormat df = new DecimalFormat("#");
    df.setMaximumFractionDigits(decimals);
    df.setMinimumFractionDigits(decimals);
    String s = (df.format(d));
    return "0"+s;
}


public double getDouble(String number, int decimals){
	return Double.parseDouble(number);
}
  




}
