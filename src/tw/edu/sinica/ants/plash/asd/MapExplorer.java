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
	private static int _location[] = new int[35];
	private GeoPoint point[] = new GeoPoint[35];
	private OverlayItem overlayitem[] = new OverlayItem[35];	
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
	    
        // 開啟Zoomlevel button
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
        point[0] = new GeoPoint(25039386, 121616921); 	//民族學研究所
        point[1] = new GeoPoint(25040102, 121617217); 	//經濟研究所
        point[2] = new GeoPoint(25040541, 121616698); 	//傅斯年圖書館
        point[3] = new GeoPoint(25040879, 121617184); 	//應用科學研究中心
        point[4] = new GeoPoint(25040576, 121616289); 	//胡適紀念館
        point[5] = new GeoPoint(25040118, 121616252); 	//台灣考古館
        point[6] = new GeoPoint(25039525, 121616208); 	//歷史文物陳列館
        point[7] = new GeoPoint(25040144, 121615664); 	//近代史研究所
        point[8] = new GeoPoint(25039286, 121615799); 	//歷史語言研究所
        point[9] = new GeoPoint(25039647, 121615150); 	//歐美研究所
        point[10] = new GeoPoint(25040697, 121615099); 	//近史所檔案館
        point[11] = new GeoPoint(25040311, 121615054); 	//嶺南美術館
        point[12] = new GeoPoint(25041804, 121614102); 	//蔡元培館
        point[13] = new GeoPoint(25040490, 121614057); 	//地球科學研究所
        point[14] = new GeoPoint(25041487, 121613634); 	//統計科學研究所
        point[15] = new GeoPoint(25040414, 121613349); 	//綜合體育館
        point[16] = new GeoPoint(25040921, 121612619); 	//學術活動中心
        point[17] = new GeoPoint(25041924, 121612200); 	//中國文哲研究所
        point[18] = new GeoPoint(25041078, 121614695); 	//資訊科學研究所
        point[19] = new GeoPoint(25043006, 121613056); 	//生物化學研究所
        point[20] = new GeoPoint(25043160, 121615928); 	//溫室
        point[21] = new GeoPoint(25043402, 121613836); 	//分子生物研究所
        point[22] = new GeoPoint(25043746, 121614531); 	//生物醫學科學研究所
        point[23] = new GeoPoint(25044391, 121613827); 	//生物多樣性研究中心
        point[24] = new GeoPoint(25045146, 121613603); 	//中研院附設幼稚園
        point[25] = new GeoPoint(25044426, 121613121); 	//國家實驗動物中心
        point[26] = new GeoPoint(25041202, 121615175); 	//人文社會科學研究中心
        point[27] = new GeoPoint(25041910, 121615460); 	//資訊科技創新研究中心
        point[28] = new GeoPoint(25041329, 121615699); 	//化學研究所
        point[29] = new GeoPoint(25041349, 121611249); 	//台灣史研究所
        point[30] = new GeoPoint(25042344, 121614303); 	//基因體研究中心
        point[31] = new GeoPoint(25042467, 121615186); 	//植物暨微生物學研究所
        point[32] = new GeoPoint(25042213, 121616170); 	//行政大樓計算中心
        point[33] = new GeoPoint(25042672, 121616514); 	//郵局車庫福利社
        point[34] = new GeoPoint(25041141, 121616707); 	//物理研究所
        point[35] = new GeoPoint(25041007, 121611445); 	//人文社會科學館
        
        overlayitem[0] = new OverlayItem(point[0],"民族學研究所", null);
        overlayitem[1] = new OverlayItem(point[1],"經濟研究所", null);
        overlayitem[2] = new OverlayItem(point[2],"傅斯年圖書館", null);
        overlayitem[3] = new OverlayItem(point[3],"應用科學研究中心", null);
        overlayitem[4] = new OverlayItem(point[4],"胡適紀念館", null);
        overlayitem[5] = new OverlayItem(point[5],"台灣考古館", null);
        overlayitem[6] = new OverlayItem(point[6],"歷史文物陳列館", null);
        overlayitem[7] = new OverlayItem(point[7],"近代史研究所", null);
        overlayitem[8] = new OverlayItem(point[8],"歷史語言研究所", null);
        overlayitem[9] = new OverlayItem(point[9],"歐美研究所", null);
        overlayitem[10] = new OverlayItem(point[10],"近史所檔案館", null);
        overlayitem[11] = new OverlayItem(point[11],"嶺南美術館", null);
        overlayitem[12] = new OverlayItem(point[12],"蔡元培館", null);
        overlayitem[13] = new OverlayItem(point[13],"地球科學研究所", null);
        overlayitem[14] = new OverlayItem(point[14],"統計科學研究所", null);
        overlayitem[15] = new OverlayItem(point[15],"綜合體育館", null);
        overlayitem[16] = new OverlayItem(point[16],"學術活動中心", null);
        overlayitem[17] = new OverlayItem(point[17], "中國文哲研究所", null);
        overlayitem[18] = new OverlayItem(point[18],"資訊科學研究所", null);
        overlayitem[19] = new OverlayItem(point[19],"生物化學研究所", null);
        overlayitem[20] = new OverlayItem(point[20],"溫室", null);
        overlayitem[21] = new OverlayItem(point[21],"分子生物研究所", null);
        overlayitem[22] = new OverlayItem(point[22],"生物醫學科學研究所", null);
        overlayitem[23] = new OverlayItem(point[23],"生物多樣性研究中心", null);
        overlayitem[24] = new OverlayItem(point[24],"中研院附設幼稚園", null);
        overlayitem[25] = new OverlayItem(point[25],"國家實驗動物中心", null);
        overlayitem[26] = new OverlayItem(point[26],"人文社會科學研究中心", null);
        overlayitem[27] = new OverlayItem(point[27],"資訊科技創新研究中心", null);
        overlayitem[28] = new OverlayItem(point[28],"化學研究所", null);
        overlayitem[29] = new OverlayItem(point[29],"台灣史研究所", null);
        overlayitem[30] = new OverlayItem(point[30],"基因體研究中心", null);
        overlayitem[31] = new OverlayItem(point[31],"植物暨微生物學研究所", null);
        overlayitem[32] = new OverlayItem(point[32],"行政大樓計算中心", null);
        overlayitem[33] = new OverlayItem(point[33],"郵局車庫福利社", null);
        overlayitem[34] = new OverlayItem(point[34],"物理研究所", null);
        overlayitem[35] = new OverlayItem(point[35],"人文社會科學館", null);
        
        for (int i=0; i<35; i++)
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