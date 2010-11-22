package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class camera_view extends Activity {
	
	//相機
	private SurfaceView sv;
	private SurfaceHolder sh;
	private Camera camera;
	
	//Location 資料
	private GeoPoint point[] = new GeoPoint[7];
	private static int _location[] = new int[7];
	private GeoPoint gp2;
	
	// Intent Activity傳遞資料用
	Bundle bundle;
	Intent intent;
	
	/*建立一個全域的類別成員變數，型別為ProgressDialog物件*/
	public ProgressDialog myDialog = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fullscreen mode 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
     
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , 
                       WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.camera_view);
        
        // 取得 Bundle 物件
        intent = this.getIntent();
        bundle = intent.getExtras();
        _location = bundle.getIntArray("_location");
        int _lat = bundle.getInt("_lat");
        int _lng = bundle.getInt("_lng");
        gp2 = new GeoPoint(_lat,_lng);
        
        //設定目標座標
        point[0] = new GeoPoint(25043830, 121613940);
        point[1] = new GeoPoint(25042960, 121613190);
        point[2] = new GeoPoint(25042770, 121616420);
        point[3] = new GeoPoint(25041040, 121613250);
        point[4] = new GeoPoint(25041170, 121614190);
        point[5] = new GeoPoint(25041320, 121614710);
        point[6] = new GeoPoint(25040660, 121616090);

        //設定相機
        sv = (SurfaceView)findViewById(R.id.sv);
        sh = sv.getHolder();
        sh.addCallback(new MySHCallback());
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            	
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton Shutter = (ImageButton) findViewById(R.id.Shutter);
        Shutter.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
          	if (camera != null) 
        	{	
          		int j=0;
          		for(int i=1; i<7; i++)
          			if(GetDistance(gp2,point[j]) > GetDistance(gp2,point[i])) j = i;
          		_location[j]=2;
          		
          		int total_point=0;
          		for(int i=0; i<7; i++)
          			if(_location[i]==1) total_point++;
          		camera.takePicture(null, null, jpeg);
          		if(total_point==7)
          		{
          	        new AlertDialog.Builder(camera_view.this)
          	        .setTitle("恭喜闖關成功")
          	        .setMessage("請到資訊所Plash攤位領取闖關紀念品")
          	        .setPositiveButton("確認",
          	           new DialogInterface.OnClickListener()
          	            {
          	        		public void onClick(DialogInterface dialoginterface, int i)
          	        		{
          	        			finish();
          	        		}
          	            }
          	          )
          	        .show();
          			
          		}
        	}
          }
        });
        
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton MapButton = (ImageButton) findViewById(R.id.MapButton01);
        MapButton.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
              /* new一個Intent物件，並指定要啟動的class */
              //Intent intent = new Intent();
        	  //intent.setClass(camera_view.this, map_view.class);
        	  
        	  /* New 一個 Bundle 物件 */
        	  //Bundle bundle = new Bundle();
        	  //bundle.putIntArray("_location", _location);
        	  //intent.putExtras(bundle);
        	  
        	  /* 呼叫一個新的Activity */
        	  //startActivity(intent);
        	  camera_view.this.setResult(RESULT_OK, intent);
        	  /* 關閉原本的Activity */
        	  camera_view.this.finish();
          }
        });
        
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton AlbumButton = (ImageButton) findViewById(R.id.AlbumButton03);
        AlbumButton.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  (new AsyncTasks(camera_view.this, AsyncTasks.load_album)).execute(null);
          }
        });      
    }
    
    private PictureCallback jpeg = new PictureCallback() 
    {
    	public void onPictureTaken(byte[] data, Camera camera) 
    	{
    		Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

    		FileOutputStream fos = null;
    		try {
    			String path,filename;
    			Date date = new Date();
    			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss-SSS");
    			path = "/sdcard/ASD/";
    			filename = "ASD_"+dateFormat.format(date)+".jpg";
    			File file = new File(path + filename);
    			fos = new FileOutputStream(file);
    			BufferedOutputStream bos = new
    			BufferedOutputStream(fos);
    			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
    			bos.flush();
    			bos.close();

            	// 上傳照片至Picasa
    			uploadPicasa("http://203.72.144.85/~yuhsiang/upload2.php", 
    					path, filename);
    			
    			Toast.makeText(camera_view.this, "照片已存檔",
    					Toast.LENGTH_SHORT).show();
    			camera.startPreview();
    		}catch (Exception e) {
    			Toast.makeText(camera_view.this, e.toString(),
    					Toast.LENGTH_SHORT).show();
    		}
    	}
    };    
    
    class MySHCallback implements SurfaceHolder.Callback {
    	public void surfaceCreated(SurfaceHolder holder) {
    		camera = Camera.open();

    		if (camera == null) {
    			Toast.makeText(camera_view.this, "camera is null",
    					Toast.LENGTH_SHORT).show();
    			finish();
    			}

    			Camera.Parameters params = camera.getParameters();
    			params.setPictureFormat(PixelFormat.JPEG);
    			params.setPictureSize(1024, 768);
    			camera.setParameters(params);    		
    		
    		try {
    			camera.setPreviewDisplay(sh);
    		} catch (Exception e) {
    			Toast.makeText(camera_view.this, e.toString(),
    					Toast.LENGTH_SHORT).show();
    			finish();
    		}
    	}
    	
    	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    		camera.stopPreview();
    		camera.release();
    	}
    	
    	public void surfaceChanged(SurfaceHolder surfaceholder,int format , int w, int h) {
    		camera.startPreview();
    	}
    }



    private void uploadPicasa(String actionUrl, String path, String uploadFile )
    {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      try
      {
          Toast.makeText(camera_view.this, actionUrl+"   "+path+uploadFile,Toast.LENGTH_SHORT).show();

    	  URL url =new URL(actionUrl);
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        /* 允許Input、Output，不使用Cache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* 設定傳送的method=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        /* 設定DataOutputStream */
        DataOutputStream ds = 
          new DataOutputStream(con.getOutputStream());
        
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"submit\";value=\"save\"" + end);
        ds.writeBytes(end); 

        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"title\"   title" + end);
        ds.writeBytes(end);        
        
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"photofile\";filename=\"" +
                      path + uploadFile +"\"" + end);
        ds.writeBytes(end);   

        /* 取得檔案的FileInputStream */
        FileInputStream fStream = new FileInputStream(path+uploadFile);
        /* 設定每次寫入1024bytes */
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        /* 從檔案讀取資料至緩衝區 */
        while((length = fStream.read(buffer)) != -1)
        {
          /* 將資料寫入DataOutputStream中 */
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        /* close streams */
        fStream.close();
        ds.flush();
        
        /* 取得Response內容 */
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        /* 將Response顯示於Dialog */
        //showDialog(b.toString().trim());
        /* 關閉DataOutputStream */
        ds.close();
      }
      catch(Exception e)
      {
        showDialog(""+e);
      }
    }
    /* 顯示Dialog的method */
    private void showDialog(String mess)
    {
      new AlertDialog.Builder(camera_view.this).setTitle("Message")
       .setMessage(mess)
       .setNegativeButton("確定",new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int which)
         {          
         }
       })
       .show();
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
	  
}