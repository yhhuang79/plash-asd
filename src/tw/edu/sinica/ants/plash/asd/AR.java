package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class AR extends Activity {
	
	//相機
	private SurfaceView sv;
	private SurfaceHolder sh;

	private Camera camera;
	  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        //設定相機
        sv = (SurfaceView)findViewById(R.id.sv);
        sh = sv.getHolder();
        sh.addCallback(new MySHCallback());
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton b2_2 = (ImageButton) findViewById(R.id.MapMode2);
        b2_2.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
            /* new一個Intent物件，並指定要啟動的class */
            Intent intent = new Intent();
        	  intent.setClass(AR.this, Map.class);
        	  
        	  /* 呼叫一個新的Activity */
        	  startActivity(intent);
        	  /* 關閉原本的Activity */
        	  AR.this.finish();
          }
        });
        /* 以findViewById()取得Button物件，並加入onClickListener */
        ImageButton b2_3 = (ImageButton) findViewById(R.id.GalleryMode2);
        b2_3.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
            /* new一個Intent物件，並指定要啟動的class */
            Intent intent = new Intent();
        	  intent.setClass(AR.this, gallery.class);
        	  
        	  /* 呼叫一個新的Activity */
        	  startActivity(intent);
        	  /* 關閉原本的Activity */
        	  AR.this.finish();
          }
        });        
    }

    public boolean onKeyDown (int keyCode, KeyEvent event) 
    {
    	if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) 
    	{
    		return super.onKeyDown(keyCode, event);
    	}
    	if (camera != null) 
    	{
    		camera.takePicture(null, null, jpeg);
    	}
    	return true;
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
    			
    			uploadPicasa("http://203.72.144.85/~yuhsiang/upload2.php", 
    					path, filename);
    			
    			Toast.makeText(AR.this, "照片已存檔",
    					Toast.LENGTH_SHORT).show();
    			camera.startPreview();
    		}catch (Exception e) {
    			Toast.makeText(AR.this, e.toString(),
    					Toast.LENGTH_SHORT).show();
    		}
    	}
    };    
    
    class MySHCallback implements SurfaceHolder.Callback {
    	public void surfaceCreated(SurfaceHolder holder) {
    		camera = Camera.open();

    		if (camera == null) {
    			Toast.makeText(AR.this, "camera is null",
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
    			Toast.makeText(AR.this, e.toString(),
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
          Toast.makeText(AR.this, actionUrl+"   "+path+uploadFile,Toast.LENGTH_SHORT).show();

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
      new AlertDialog.Builder(AR.this).setTitle("Message")
       .setMessage(mess)
       .setNegativeButton("確定",new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int which)
         {          
         }
       })
       .show();
    }
}