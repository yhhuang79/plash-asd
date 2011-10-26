package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;



public class GetHostAddress {
	
	//Android version 
	public static String AndroidVersion = Build.VERSION.RELEASE;
	
	//Set it to true for debugging 
	public static boolean isDebug = false;
	
	public static String hostAddress (){
		//String host = "http://plash.iis.sinica.edu.tw/plash/";
		
		//String host = "http://140.109.18.34:8080/plash/";
		
		//Danny: 4/20/2011
		//Mule Structure 
		//Host address for Mule structure
		String host = "https://plash.iis.sinica.edu.tw:8080/";
		return host;		 
	 }
	
	public static String clientAddress() {
		String client = "http://plash.iis.sinica.edu.tw/plash/";
		return client;
	}
	
	public static String completeURL(String url) {
		url += "&appID=001";
		return url;
	}
	
	public static String connectServer(String host, String action, String parameters) {
		String appID = "&appID=001";
		String location = host + action + parameters;
		String inputLine = null;
		URL url = null;
		try {
			url = new URL(location);
			
		}catch(MalformedURLException  e){
		}
		
		if (url != null){
			try{
				
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				
				BufferedReader in = 
					new BufferedReader(
							new InputStreamReader(
									urlConn.getInputStream()));
				
				inputLine = in.readLine();
				in.close();
				urlConn.disconnect();
			}catch(IOException e){
			}	
		}else{
			inputLine = "url is not found!";
			//lblInformation.setText(results);
			//lblResult.setText(results);
		}
		return inputLine;
	}
	
	/*
	 * for general purpose
	 */
	public static int NONE = 0;
	public static int CAR = 1;
	public static int BUS = 2;
	public static int WALK = 3;
	public static int BIKE = 4;
	public static int MOTORCYCLE = 5;
	public static int LONG_BUS = 6;
	public static int TRAIN = 7;
	public static int SUBWAY = 8;
	public static int THSR = 9;
	
	public static String parseLabel(int label) {
		//1 = car, 2 = Bus, 4 = Bike, 3 = Walk, 0 means not mentioned
		String stringLabel = "";
		switch(label) {
		case 0:
			stringLabel = "no label";
			break;
		case 1:
			stringLabel = "by Car";
			break;
		case 2:
			stringLabel = "by Bus";
			break;
		case 3:
			stringLabel = "by Walk";
			break;
		case 4:
			stringLabel = "by Bike";
			break;
		case 5:
			stringLabel = "by Motorcycle";
			break;
		case 6:
			stringLabel = "by Long bus";
			break;
		case 7:
			stringLabel = "by Train";
			break;
		case 8:
			stringLabel = "by Subway";
			break;
		case 9:
			stringLabel = "by THSR";
			break;
		}
		return stringLabel;
	}
	
	public static String[] returnLabel() {
		String[] allLabels = {
				parseLabel(CAR),
				parseLabel(BUS),
				parseLabel(WALK),
				parseLabel(BIKE),
				parseLabel(MOTORCYCLE),
				parseLabel(LONG_BUS),
				parseLabel(TRAIN),
				parseLabel(SUBWAY),
				parseLabel(THSR),
		};
		
		return allLabels;
	}
	
}
