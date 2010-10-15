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
	  
	  /*�إߤ@�ӥ��쪺���O�����ܼơA���O��ProgressDialog����*/
	  public ProgressDialog myDialog = null;	  
	  
    /** Called when the activity is first created. */
	  @Override 
	  public void onCreate(Bundle savedInstanceState) 
	  { 
	    super.onCreate(savedInstanceState); 
	    setContentView(R.layout.map); 
	    
	    /* �إ�MapView���� */ 
	    mMapView = (MapView)findViewById(R.id.myMapView1); 
	    mMapController = mMapView.getController();

	    /* �]�w�w�]����j�h�� */
	    zoomLevel = 17; 
	    mMapController.setZoom(zoomLevel); 
	    
        /*�}��Zoomlevel button*/
        mMapView.setBuiltInZoomControls(true);

	    /* Provider��l�� */
	    mLocationManager = (LocationManager)
	                       getSystemService(Context.LOCATION_SERVICE); 
	    /* ���oProvider�PLocation */
	    getLocationPrivider();
	    if(mLocation!=null)
	    {
	      /* ���o�ثe���g�n�� */
	      gp1=getGeoByLocation(mLocation); 
	      gp2=gp1;
	      /* �NMapView�����I���ܥثe��m */
	      refreshMapView();
	      /* �]�w�ƥ�Listener */ 
	      mLocationManager.requestLocationUpdates("gps",
	          1000, 1, mLocationListener);   // 1000, 1�N�令�ܼ�
	    }
	    else
	    {
	      new AlertDialog.Builder(Map.this).setTitle("�t�ΰT��")
	      .setMessage(getResources().getString(R.string.str_message))
	      .setNegativeButton("�T�w",new DialogInterface.OnClickListener()
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

        	// ���Progress��ܤ��
        	myDialog = ProgressDialog.show
        			(
                     Map.this,    
                     "",
                     "GPS�w�줤,�еy�Ԥ���...", 
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
        			// �����ҫإߪ�myDialog����C
        			myDialog.dismiss(); 
        		}
        	}.start(); /* �}�l�������� */
        }        
        /* ��sMapView */
        refreshMapView();
        
        gp1=gp2;
        /* �M��Overlay */
        resetOverlay();
        /* �e�_�I */
        setStartPoint();
        /* ��sMapView */
        refreshMapView();
        /* ���]���ʶZ����0�A�ç�sTextView */
        distance=0;

        /* �Ұʵe���u������ */
        _run=true;
	    
	    
        /* �HfindViewById()���oButton����A�å[�JonClickListener */
        ImageButton b1_1 = (ImageButton) findViewById(R.id.CameraMode1);
        b1_1.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  setEndPoint();
            /* new�@��Intent����A�ë��w�n�Ұʪ�class */
            Intent intent = new Intent();
        	  intent.setClass(Map.this, AR.class);
        	  
        	  /* �I�s�@�ӷs��Activity */
        	  startActivity(intent);
        	  /* �����쥻��Activity */
        	  Map.this.finish();
          }
        });
        
        /* �HfindViewById()���oButton����A�å[�JonClickListener */
        ImageButton b1_3 = (ImageButton) findViewById(R.id.GalleryMode1);
        b1_3.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  setEndPoint();
            /* new�@��Intent����A�ë��w�n�Ұʪ�class */
            Intent intent = new Intent();
        	  intent.setClass(Map.this, gallery.class);
        	  
        	  /* �I�s�@�ӷs��Activity */
        	  startActivity(intent);
        	  /* �����쥻��Activity */
        	  Map.this.finish();
          }
        });        
	    	    

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
	        Date d = new Date();

	        updateLocation("0", "131", gp2.getLatitudeE6() + "",
	        		gp2.getLongitudeE6() + "", d.getTime() + "", 3 ); // �W�Ǵ��ե�
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
	  
	  /* �W�Ǧ�m��Server��method */
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
