package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import system.ArActivity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MapExplorer extends MapActivity {
	
	private MapView mMapView;
	private MapController mMapController; 
	private LocationManager mLocationManager;
	private Location mLocation;
	private String mLocationProvider="";
	private int zoomLevel=0;
	private GeoPoint gp1;
	private GeoPoint gp2;
	private boolean _run=true;
	private double distance=0;
	
	// Overlay 
	private int _location[] = new int[7];
	private GeoPoint point[] = new GeoPoint[7];
	private OverlayItem overlayitem[] = new OverlayItem[7];	
	private MyLocationOverlay myLocOverlay;
	
	//Login 資料
	private String id, password;
		
	/*建立一個全域的類別成員變數，型別為ProgressDialog物件*/
	public ProgressDialog myDialog = null;	
	  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ******************************************* 
        // fullscreen mode 
        // ******************************************* 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
     
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , 
                       WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.mapexplorer);
        
        // 取得 Bundle 物件
        //intent = this.getIntent();
        //bundle = intent.getExtras();
        //id = bundle.getString("id");
        //password = bundle.getString("password");
        
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
	    /// 建立MapView物件
	    mMapView = (MapView)findViewById(R.id.myMapView1); 
	    mMapController = mMapView.getController();

	    // 設定預設的放大層級 
	    zoomLevel = 17; 
	    mMapController.setZoom(zoomLevel); 
	    
        /*開啟Zoomlevel button*/
        mMapView.setBuiltInZoomControls(true);
        
	    /* Provider初始化 */
	    mLocationManager = (LocationManager)
	                       getSystemService(Context.LOCATION_SERVICE); 
	    
	    /* MyLocationOverlay */
	    List<Overlay> overlays = mMapView.getOverlays();

	    myLocOverlay = new MyLocationOverlay(this, mMapView);
	    myLocOverlay.runOnFirstFix(new Runnable() 
	    {
	    	public void run() 
	    	{
	    		mMapController.animateTo(myLocOverlay.getMyLocation());
	    	}
	    });
	    overlays.add(myLocOverlay);
	    
	    /* 取得Provider與Location */
	    getLocationPrivider();
	    //while(mLocation==null)
	    //	mLocationManager.requestLocationUpdates("gps" ,1000, 1, mLocationListener);
	    if(mLocation!=null)
	    {
	      /* 取得目前的經緯度 */
	      gp1=getGeoByLocation(mLocation); 
	      gp2=gp1;
	      /* 將MapView的中點移至目前位置 */
	      refreshMapView();
	      /* 設定事件的Listener */ 
	      mLocationManager.requestLocationUpdates("gps" ,1000, 1, mLocationListener);   // 1000, 1將改成變數
	    }
	    else
	    {
	      new AlertDialog.Builder(MapExplorer.this).setTitle("系統訊息")
	      .setMessage(getResources().getString(R.string.str_message))
	      .setNegativeButton("確定",new DialogInterface.OnClickListener()
	       {
	         public void onClick(DialogInterface dialog, int which)
	         {
	           MapExplorer.this.finish();
	         }
	       })
	       .show();
	    }

        /* 更新MapView */
        //refreshMapView();
        
        point[0] = new GeoPoint(25043830, 121613940);
        point[1] = new GeoPoint(25042960, 121613190);
        point[2] = new GeoPoint(25042770, 121616420);
        point[3] = new GeoPoint(25041040, 121613250);
        point[4] = new GeoPoint(25041170, 121614190);
        point[5] = new GeoPoint(25041320, 121614710);
        point[6] = new GeoPoint(25040660, 121616090);
        
        overlayitem[0] = new OverlayItem(point[0],"細胞與個體生物學研究所", null);
        overlayitem[1] = new OverlayItem(point[1],"生物化學研究所", null);
        overlayitem[2] = new OverlayItem(point[2],"萊爾富", null);
        overlayitem[3] = new OverlayItem(point[3],"學術活動中心", null);
        overlayitem[4] = new OverlayItem(point[4],"地球科學研究所", null);
        overlayitem[5] = new OverlayItem(point[5],"資訊科學研究所", null);
        overlayitem[6] = new OverlayItem(point[6],"胡適紀念館", null);
        
        for (int i=0; i<7; i++)
        	if(_location[i] < 2) _location[i] = 0;
        
        setStartPoint();
        // 以findViewById()取得Button物件，並加入onClickListener
/*        final ImageButton Shutter = (ImageButton) findViewById(R.id.Shutter);
        Shutter.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
              gp2 = myLocOverlay.getMyLocation();
        	  gp1=gp2;
              // 清除Overlay
              resetOverlay();
              // 更新MapView
              refreshMapView();
              // 重設移動距離為0
              distance=0;
              // 啟動畫路線的機制 
              _run=!_run;
              
              if(_run)
              {
                // 畫起點 
                setStartPoint();
                Toast.makeText(MapExplorer.this, "軌跡紀錄開始", Toast.LENGTH_LONG).show();
            	Shutter.setImageResource(R.drawable.ic_pwrbutton_green); 
              }
              else
              {
            	// 畫終點  
            	setEndPoint();
                Toast.makeText(MapExplorer.this, "軌跡紀錄結束", Toast.LENGTH_LONG).show();
            	Shutter.setImageResource(R.drawable.ic_pwrbutton_white); 
              }
          }
        });*/
        

        // AR Explorer
        ImageButton ARExplorerBtn = (ImageButton) findViewById(R.id.ARExplorerBtn);
        ARExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, ArActivity.class);
          	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  //MapExplorer.this.finish();
        	  
        	  ArActivity.startWithSetup(MapExplorer.this, new ARStuff(id, password, 25.041384,121.61458, 1.6));
        	  MapExplorer.this.finish();
          }
        });
        
        // Friend Finder Button
        ImageButton FriendFinderBtn = (ImageButton) findViewById(R.id.FriendFinderBtn);
        FriendFinderBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, FriendFinder.class);
        	            	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  MapExplorer.this.finish();
          }
        });        
        // ASD Gallery Button
        ImageButton ASDGalleryBtn = (ImageButton) findViewById(R.id.ASDGalleryBtn);
        ASDGalleryBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, ASDGallery.class);
        	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  MapExplorer.this.finish();
          }
        });
        // ASD Guidance Button
        ImageButton ASDGuidanceBtn = (ImageButton) findViewById(R.id.ASDGuidanceBtn);
        ASDGuidanceBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, AsdGuidance.class);
        	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  MapExplorer.this.finish();
          }
        });
    }

    @Override
    protected void onResume() 
    {
    	super.onResume();
    	myLocOverlay.enableCompass();
    	myLocOverlay.enableMyLocation();
    	PaintItemPoint();
    }
    @Override
    protected void onStop() 
    {
    	myLocOverlay.disableMyLocation();
    	myLocOverlay.disableCompass();
    	super.onStop();
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
	        
	        //Date d = new Date();
	        //tripID = getTripID(userID);
	        //updateLocation(tripID , userID, gp2.getLatitudeE6() + "",
	        //		gp2.getLongitudeE6() + "", d.getTime() + "", 3 ); // 上傳測試用
	        
	        for(int i=0; i<7; i++)
	        	if(GetDistance(gp2,overlayitem[i].getPoint()) < 10)
	        		if(_location[i] < 2)_location[i] = 1;
			PaintItemPoint();
	        gp1=gp2;
	      }  
	      
	    } 
	     
	    @Override 
	    public void onProviderDisabled(String provider) 
	    { 
			/* bring up the GPS settings */
			Intent intent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);	    	
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
	    mCriteria01.setAccuracy(Criteria.ACCURACY_COARSE); 
	    //mCriteria01.setAltitudeRequired(true); 
	    //mCriteria01.setBearingRequired(true); 
	    //mCriteria01.setCostAllowed(true); 
	    mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
	    
	    mLocationProvider = mLocationManager
	                        .getBestProvider(mCriteria01, true); 
	    
	    mLocation = mLocationManager
	                .getLastKnownLocation(mLocationProvider);
	  }
	  
	  /* 畫出地圖物件 */
	  private void PaintItemPoint() 
	  {  
		  List<Overlay> overlays = mMapView.getOverlays();
		  Drawable red_flag = this.getResources().getDrawable(R.drawable.ic_red_flag);
		  Drawable green_flag = this.getResources().getDrawable(R.drawable.ic_green_flag);
		  MapItemizedOverlay redflag_overlay = new MapItemizedOverlay(red_flag);	
		  MapItemizedOverlay greenflag_overlay = new MapItemizedOverlay(green_flag);	
    
		  int red_noItem = 0, green_noItem = 0;
		  for(int i=0; i<7; i++)
		  {		  
			  if(_location[i] == 1)
			  {
				  redflag_overlay.addOverlay(overlayitem[i]);
				  red_noItem++;
			  }
			  if(_location[i] == 2)
			  {
				  greenflag_overlay.addOverlay(overlayitem[i]);
				  green_noItem++;
			  }
		  }
		  if(red_noItem>0)
			  overlays.add(redflag_overlay);
		  if(green_noItem>0)
			  overlays.add(greenflag_overlay);		  
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
	    overlays.add(myLocOverlay);
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

	  /* 覆寫 onActivityResult()*/
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode,
	                                  Intent data)
	  {
	    switch (resultCode)
	    { 
	      case RESULT_OK:
	    	/* 取得資料，並顯示於畫面上 */  
	        Bundle bunde = data.getExtras();
	        _location = bunde.getIntArray("_location");
	        break;       
	      default: 
	        break; 
	     } 
	   } 
}