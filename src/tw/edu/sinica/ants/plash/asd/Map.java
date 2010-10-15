package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

public class Map extends MapActivity {
	
	  private MapView mMapView;
	  private MapController mMapController; 
	  private LocationManager mLocationManager;
	  private Location mLocation;
	  private String mLocationPrivider="";
	  private int zoomLevel=0;
	  private GeoPoint gp1;
	  private GeoPoint gp2;
	  private boolean _run=false;
	  private double distance=0;
	  
	  /*建立一個全域的類別成員變數，型別為ProgressDialog物件*/
	  public ProgressDialog myDialog = null;	  
	  
    /** Called when the activity is first created. */
	  @Override 
	  public void onCreate(Bundle savedInstanceState) 
	  { 
	    super.onCreate(savedInstanceState); 
	    setContentView(R.layout.map); 
	    
	    /* 建立MapView物件 */ 
	    mMapView = (MapView)findViewById(R.id.myMapView1); 
	    mMapController = mMapView.getController();

	    /* 設定預設的放大層級 */
	    zoomLevel = 17; 
	    mMapController.setZoom(zoomLevel); 
	    
        /*開啟Zoomlevel button*/
        mMapView.setBuiltInZoomControls(true);

	    /* Provider初始化 */
	    mLocationManager = (LocationManager)
	                       getSystemService(Context.LOCATION_SERVICE); 
	    /* 取得Provider與Location */
	    getLocationPrivider();
	    if(mLocation!=null)
	    {
	      /* 取得目前的經緯度 */
	      gp1=getGeoByLocation(mLocation); 
	      gp2=gp1;
	      /* 將MapView的中點移至目前位置 */
	      refreshMapView();
	      /* 設定事件的Listener */ 
	      mLocationManager.requestLocationUpdates("gps",
	          1000, 1, mLocationListener);   // 1000, 1將改成變數
	    }
	    else
	    {
	      new AlertDialog.Builder(Map.this).setTitle("系統訊息")
	      .setMessage(getResources().getString(R.string.str_message))
	      .setNegativeButton("確定",new DialogInterface.OnClickListener()
	       {
	         public void onClick(DialogInterface dialog, int which)
	         {
	           Map.this.finish();
	         }
	       })
	       .show();
	    }
	    
        if(!_run)
        {	

        	// 顯示Progress對話方塊
        	myDialog = ProgressDialog.show
        			(
                     Map.this,    
                     "",
                     "GPS定位中,請稍候片刻...", 
                     true
        			); 
        	new Thread()
        	{ 
        		public void run()
        		{ 
        			try
        			{            	
        				sleep(5000);
        			}
        			catch (Exception e)
        			{
        			} 
        			// 卸載所建立的myDialog物件。
        			myDialog.dismiss(); 
        		}
        	}.start(); /* 開始執行執行緒 */
        }        
        /* 更新MapView */
        refreshMapView();
        
        gp1=gp2;
        /* 清除Overlay */
        resetOverlay();
        /* 畫起點 */
        setStartPoint();
        /* 更新MapView */
        refreshMapView();
        /* 重設移動距離為0，並更新TextView */
        distance=0;

        /* 啟動畫路線的機制 */
        _run=true;
	    
	    
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton b1_1 = (ImageButton) findViewById(R.id.CameraMode1);
        b1_1.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  setEndPoint();
            /* new一個Intent物件，並指定要啟動的class */
            Intent intent = new Intent();
        	  intent.setClass(Map.this, AR.class);
        	  
        	  /* 呼叫一個新的Activity */
        	  startActivity(intent);
        	  /* 關閉原本的Activity */
        	  Map.this.finish();
          }
        });
        
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton b1_3 = (ImageButton) findViewById(R.id.GalleryMode1);
        b1_3.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  setEndPoint();
            /* new一個Intent物件，並指定要啟動的class */
            Intent intent = new Intent();
        	  intent.setClass(Map.this, gallery.class);
        	  
        	  /* 呼叫一個新的Activity */
        	  startActivity(intent);
        	  /* 關閉原本的Activity */
        	  Map.this.finish();
          }
        });        
	    	    

	  }
	  
	  /* MapView的Listener */
	  public final LocationListener mLocationListener = 
	    new LocationListener() 
	  { 
	    @Override 
	    public void onLocationChanged(Location location) 
	    { 
	      /* 如果記錄進行中，就畫路線並更新移動距離 */
	      if(_run)
	      {
	        /* 記下移動後的位置 */
	        gp2=getGeoByLocation(location);
	        /* 畫路線 */
	        setRoute();
	        /* 更新MapView */
	        refreshMapView();
	        /* 取得移動距離 */
	        distance+=GetDistance(gp1,gp2);
	        //mTextView.setText("移動距離："+format(distance)+"M  "+mLocationPrivider); 
	        Date d = new Date();

	        updateLocation("0", "131", gp2.getLatitudeE6() + "",
	        		gp2.getLongitudeE6() + "", d.getTime() + "", 3 ); // 上傳測試用
	        gp1=gp2;
	      }  
	    } 
	     
	    @Override 
	    public void onProviderDisabled(String provider) 
	    { 
	    } 
	    @Override 
	    public void onProviderEnabled(String provider) 
	    { 
	    } 
	    @Override 
	    public void onStatusChanged(String provider,int status,
	                                Bundle extras) 
	    { 
	    } 
	  }; 

	  /* 取得GeoPoint的method */ 
	  private GeoPoint getGeoByLocation(Location location) 
	  { 
	    GeoPoint gp = null; 
	    try 
	    { 
	      if (location != null) 
	      { 
	        double geoLatitude = location.getLatitude()*1E6; 
	        double geoLongitude = location.getLongitude()*1E6; 
	        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude);
	      } 
	    } 
	    catch(Exception e) 
	    { 
	      e.printStackTrace(); 
	    }
	    return gp;
	  } 
	  
	  /* 取得LocationProvider */
	  public void getLocationPrivider() 
	  { 
	    Criteria mCriteria01 = new Criteria();
	    mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
	    mCriteria01.setAltitudeRequired(false); 
	    mCriteria01.setBearingRequired(false); 
	    mCriteria01.setCostAllowed(true); 
	    mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
	    
	    mLocationPrivider = mLocationManager
	                        .getBestProvider(mCriteria01, true); 
	    //mLocationPrivider = "gps";
	    
	    mLocation = mLocationManager
	                .getLastKnownLocation(mLocationPrivider); 
	  }
	  
	  /* 設定起點的method */
	  private void setStartPoint() 
	  {  
	    int mode=1;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* 設定路線的method */  
	  private void setRoute() 
	  {  
	    int mode=2;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* 設定終點的method */
	  private void setEndPoint() 
	  {  
	    int mode=3;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* 重設Overlay的method */
	  private void resetOverlay() 
	  {
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.clear();
	  } 
	  /* 更新MapView的method */
	  public void refreshMapView() 
	  { 
	    mMapView.displayZoomControls(true); 
	    MapController myMC = mMapView.getController(); 
	    myMC.animateTo(gp2); 
	    myMC.setZoom(zoomLevel); 
	    mMapView.setSatellite(false); 
	  } 
	  
	  /* 取得兩點間的距離的method */
	  public double GetDistance(GeoPoint gp1,GeoPoint gp2)
	  {
	    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
	    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
	    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
	    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);
	    /* 地球半徑(KM) */
	    double R = 6371;
	    double d = Math.acos(Math.sin(Lat1r)*Math.sin(Lat2r)+
	               Math.cos(Lat1r)*Math.cos(Lat2r)*
	               Math.cos(Long2r-Long1r))*R;
	    return d*1000;
	  }

	  private double ConvertDegreeToRadians(double degrees)
	  {
	    return (Math.PI/180)*degrees;
	  }
	  
	  /* format移動距離的method */
	  public String format(double num)
	  {
	    NumberFormat formatter = new DecimalFormat("###");
	    String s=formatter.format(num);
	    return s;
	  }
	  
	  @Override
	  protected boolean isRouteDisplayed()
	  {
	    return false;
	  }
	  
	  /* 上傳位置到Server的method */
	  public static String updateLocation(String tripid, String userid,
				String lat, String lng, String timestamp, int defaultLabel) {
			String result = "";
			try {
				String address = "http://plash.iis.sinica.edu.tw/plash/connPost.action?userid="
						+ userid
						+ "&lat="
						+ lat
						+ "&lng="
						+ lng
						+ "&label="
						+ defaultLabel
						+ "&timestamp="
						+ timestamp + "&type=input&tripid=" + tripid;
				URL url = new URL(address);
				BufferedReader br = new BufferedReader(new InputStreamReader(url
						.openStream()));
				String line;
				while ((line = br.readLine()) != null) {
					result += line;
				}
				br.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;

		}

	} 
