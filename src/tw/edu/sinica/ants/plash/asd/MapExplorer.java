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
	
	//Login ���
	private String id, password;
		
	/*�إߤ@�ӥ��쪺���O�����ܼơA���O��ProgressDialog����*/
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
        
        // ���o Bundle ����
        //intent = this.getIntent();
        //bundle = intent.getExtras();
        //id = bundle.getString("id");
        //password = bundle.getString("password");
        
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
	    /// �إ�MapView����
	    mMapView = (MapView)findViewById(R.id.myMapView1); 
	    mMapController = mMapView.getController();

	    // �]�w�w�]����j�h�� 
	    zoomLevel = 17; 
	    mMapController.setZoom(zoomLevel); 
	    
        /*�}��Zoomlevel button*/
        mMapView.setBuiltInZoomControls(true);
        
	    /* Provider��l�� */
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
	    
	    /* ���oProvider�PLocation */
	    getLocationPrivider();
	    //while(mLocation==null)
	    //	mLocationManager.requestLocationUpdates("gps" ,1000, 1, mLocationListener);
	    if(mLocation!=null)
	    {
	      /* ���o�ثe���g�n�� */
	      gp1=getGeoByLocation(mLocation); 
	      gp2=gp1;
	      /* �NMapView�����I���ܥثe��m */
	      refreshMapView();
	      /* �]�w�ƥ�Listener */ 
	      mLocationManager.requestLocationUpdates("gps" ,1000, 1, mLocationListener);   // 1000, 1�N�令�ܼ�
	    }
	    else
	    {
	      new AlertDialog.Builder(MapExplorer.this).setTitle("�t�ΰT��")
	      .setMessage(getResources().getString(R.string.str_message))
	      .setNegativeButton("�T�w",new DialogInterface.OnClickListener()
	       {
	         public void onClick(DialogInterface dialog, int which)
	         {
	           MapExplorer.this.finish();
	         }
	       })
	       .show();
	    }

        /* ��sMapView */
        //refreshMapView();
        
        point[0] = new GeoPoint(25043830, 121613940);
        point[1] = new GeoPoint(25042960, 121613190);
        point[2] = new GeoPoint(25042770, 121616420);
        point[3] = new GeoPoint(25041040, 121613250);
        point[4] = new GeoPoint(25041170, 121614190);
        point[5] = new GeoPoint(25041320, 121614710);
        point[6] = new GeoPoint(25040660, 121616090);
        
        overlayitem[0] = new OverlayItem(point[0],"�ӭM�P����ͪ��Ǭ�s��", null);
        overlayitem[1] = new OverlayItem(point[1],"�ͪ��ƾǬ�s��", null);
        overlayitem[2] = new OverlayItem(point[2],"�ܺ��I", null);
        overlayitem[3] = new OverlayItem(point[3],"�ǳN���ʤ���", null);
        overlayitem[4] = new OverlayItem(point[4],"�a�y��Ǭ�s��", null);
        overlayitem[5] = new OverlayItem(point[5],"��T��Ǭ�s��", null);
        overlayitem[6] = new OverlayItem(point[6],"�J�A�����]", null);
        
        for (int i=0; i<7; i++)
        	if(_location[i] < 2) _location[i] = 0;
        
        setStartPoint();
        // �HfindViewById()���oButton����A�å[�JonClickListener
/*        final ImageButton Shutter = (ImageButton) findViewById(R.id.Shutter);
        Shutter.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
              gp2 = myLocOverlay.getMyLocation();
        	  gp1=gp2;
              // �M��Overlay
              resetOverlay();
              // ��sMapView
              refreshMapView();
              // ���]���ʶZ����0
              distance=0;
              // �Ұʵe���u������ 
              _run=!_run;
              
              if(_run)
              {
                // �e�_�I 
                setStartPoint();
                Toast.makeText(MapExplorer.this, "�y������}�l", Toast.LENGTH_LONG).show();
            	Shutter.setImageResource(R.drawable.ic_pwrbutton_green); 
              }
              else
              {
            	// �e���I  
            	setEndPoint();
                Toast.makeText(MapExplorer.this, "�y���������", Toast.LENGTH_LONG).show();
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
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, ArActivity.class);
          	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
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
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, FriendFinder.class);
        	            	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  MapExplorer.this.finish();
          }
        });        
        // ASD Gallery Button
        ImageButton ASDGalleryBtn = (ImageButton) findViewById(R.id.ASDGalleryBtn);
        ASDGalleryBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, ASDGallery.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  MapExplorer.this.finish();
          }
        });
        // ASD Guidance Button
        ImageButton ASDGuidanceBtn = (ImageButton) findViewById(R.id.ASDGuidanceBtn);
        ASDGuidanceBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(MapExplorer.this, AsdGuidance.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
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
    
	  /* MapView��Listener */
	  public final LocationListener mLocationListener = 
	    new LocationListener() 
	  { 
	    @Override 
	    public void onLocationChanged(Location location) 
	    { 	        	        
	      /* �p�G�O���i�椤�A�N�e���u�ç�s���ʶZ�� */
	      if(_run)
	      {
	        /* �O�U���ʫ᪺��m */
	        gp2=getGeoByLocation(location);
	        /* �e���u */
	        setRoute();
	        /* ��sMapView */
	        refreshMapView();
	        /* ���o���ʶZ�� */
	        distance+=GetDistance(gp1,gp2);
	        //mTextView.setText("���ʶZ���G"+format(distance)+"M  "+mLocationPrivider); 
	        
	        //Date d = new Date();
	        //tripID = getTripID(userID);
	        //updateLocation(tripID , userID, gp2.getLatitudeE6() + "",
	        //		gp2.getLongitudeE6() + "", d.getTime() + "", 3 ); // �W�Ǵ��ե�
	        
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

	  /* ���oGeoPoint��method */ 
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
	  
	  /* ���oLocationProvider */
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
	  
	  /* �e�X�a�Ϫ��� */
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
	  
	  /* �]�w�_�I��method */
	  private void setStartPoint() 
	  {  
	    int mode=1;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* �]�w���u��method */  
	  private void setRoute() 
	  {  
	    int mode=2;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* �]�w���I��method */
	  private void setEndPoint() 
	  {  
	    int mode=3;
	    MapOverLay mOverlay = new MapOverLay(gp1,gp2,mode); 
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.add(mOverlay);
	  }
	  /* ���]Overlay��method */
	  private void resetOverlay() 
	  {
	    List<Overlay> overlays = mMapView.getOverlays(); 
	    overlays.clear();
	    overlays.add(myLocOverlay);
	  } 
	  /* ��sMapView��method */
	  public void refreshMapView() 
	  { 
	    mMapView.displayZoomControls(true); 
	    MapController myMC = mMapView.getController(); 
	    myMC.animateTo(gp2); 
	    myMC.setZoom(zoomLevel); 
	    mMapView.setSatellite(false); 
	  } 
	  
	  /* ���o���I�����Z����method */
	  public double GetDistance(GeoPoint gp1,GeoPoint gp2)
	  {
	    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
	    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
	    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
	    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);
	    /* �a�y�b�|(KM) */
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
	  
	  /* format���ʶZ����method */
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

	  /* �мg onActivityResult()*/
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode,
	                                  Intent data)
	  {
	    switch (resultCode)
	    { 
	      case RESULT_OK:
	    	/* ���o��ơA����ܩ�e���W */  
	        Bundle bunde = data.getExtras();
	        _location = bunde.getIntArray("_location");
	        break;       
	      default: 
	        break; 
	     } 
	   } 
}