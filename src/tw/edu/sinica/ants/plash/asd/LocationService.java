package tw.edu.sinica.ants.plash.asd;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationService extends Service implements LocationListener{

	CommandReceiver cmdReceiver;
	boolean flag;

	private LocationManager mManager;
	
	//Login 資料
	private String id, password, tripID;
	private int sid;
	
	@Override
	public void onCreate() {//覆寫onCreate方法
		flag = true;
		cmdReceiver = new CommandReceiver();
		mManager =(LocationManager)getSystemService(LOCATION_SERVICE);
		
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction("tw.edu.sinica.ants.plash.asd.LocationService");
		registerReceiver(cmdReceiver, filter);
        String provider;
        provider = mManager.getBestProvider(new Criteria(), true);
        if (provider == null) {
            // 沒有可供取得位置資訊的提供者
            // 例如沒有Wifi也沒連3G等等
        }
        mManager.requestLocationUpdates(provider, 5000, 5, this);
        // 取得最後收到的位置資訊
        Location location = mManager.getLastKnownLocation(provider);
        if (location != null) onLocationChanged(location);
		//doJob();
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        sid = sp.getInt("sid", 0);
        password =sp.getString("password", "");
        
		String location1 = null;
		String host = GetHostAddress.hostAddress();
		String action = "GetNewTripId?";
		String temp1 = "userid="+sid;
		String parameter = temp1;
		location1 = host+action+parameter;
		try {
			//URL url = new URL(location);
			JSONObject j = SetConnection.setHttps(location1);
			tripID = j.getString("newTripId");
		
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end try catch
		return super.onStartCommand(intent, flags, startId);
	}	
	
	@Override
	public void onDestroy() {
		mManager.removeUpdates(this);
		this.unregisterReceiver(cmdReceiver);
		super.onDestroy();
	}	
	
	private class CommandReceiver extends BroadcastReceiver{//繼承自BroadcastReceiver的子類別
		@Override
		public void onReceive(Context context, Intent intent) {//覆寫onReceive方法
			int cmd = intent.getIntExtra("cmd", -1);//獲取Extra資訊
			if(cmd == 0){//如果發來的訊息是停止服務		
				flag = false;//停止執行緒
				stopSelf();//停止服務
			}
		}		
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		final String userID = Integer.toString(sid);
        final double lat = arg0.getLatitude()*1e6; 
        final double lng = arg0.getLongitude()*1e6; 
        final Date d = new Date();
		new Thread(new Runnable() {
			public void run() {
				updateLocation(tripID, userID, lat + "",
						lng + "", new Timestamp(d.getTime()) + "", 1, 1);
			}
		}).start();
		
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public static String updateLocation(String tripid, String userid,
			String lat, String lng, String timestamp, int defaultLabel, int counter) {
		String result = "";
		Log.d("Danny@MapRecorder - updateLocation", "Start!!");
		Date date = new Date();
		Log.d("Danny@MapRecorder - updateLocation", String.valueOf(date.getTime()));
		try {
			
			//----------------------------------------------//
			//Danny 5/4/2011 Mule server connection
			
			//Danny need to convert lat and lng number format 
			String lat_tmp = new BigDecimal(Double.parseDouble(lat) / 1000000).toPlainString();
			String lng_tmp = new BigDecimal(Double.parseDouble(lng) / 1000000).toPlainString();
			
			String location = null;
			String host = GetHostAddress.hostAddress();
			String action = "Input?";
			String temp1 = "userid="+userid;
			String temp2 = "&lat="+lat_tmp;
			String temp3 = "&lng="+lng_tmp;
			String temp4 = "&label="+defaultLabel;
			String temp5 = "&timestamp="+timestamp;
			String temp6 = "&trip_id="+tripid;
			String temp7 = "&packageno="+counter;
			String parameter = temp1+temp2+temp3+temp4+temp5+temp6+temp7;
			location = host+action+parameter;
			//needs to change the space " " to "%20" for url request
			location = location.replaceAll(" ", "%20");
			Log.d("Danny: location@MapRecorder = ", location);
			
//			URL url = null;
//			url = new URL(location);
			JSONObject j = SetConnection.setHttps(location);
			
			Log.d("Danny: tripID@MapRecorder = ", "Enter here?");
			//TODO: (Danny) Runtime crash!! 
			
			if (j == null){
				Log.d("Danny: if j == null--location@MapRecorder = ", location);
			}
			else if (j.getString("message")==null){
				Log.d("Danny: if j.message == null--location@MapRecorder = ", location);
			}
			else {
				result = j.getString("message");
				Log.d("Danny: tripID@MapRecorder = ", result);
			}
			
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end try catch
 

		return result;

	}//end method
}
