package tw.edu.sinica.ants.plash.asd;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.*;
import android.util.Xml;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;

public class album_view extends Activity {
	  private Gallery mGallery;  
	  private List<String> bigPhoto=new ArrayList<String>();
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState)  {

		  
	    super.onCreate(savedInstanceState);

        // ******************************************* 
        // fullscreen mode 
        // ******************************************* 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
     
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , 
                       WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.album_view);
	    /* �]�wlayout��photoshow.xml */
	    //setContentView(R.layout.gallery);

	    /* ���oBundle�����ܼ� */
	    String userId = "plash.ants";
	    String albumId = "5525227127342865393";
	    
	    /* �I�sgetPhotoList()���o�ѪR�᪺List */
	    this.getPhotoList(userId,albumId);
	    //(new ConnectTask()).doInBackground(userId,albumId);
	    
	    
	    mGallery = (Gallery) findViewById(R.id.gallery);
	    /* �]�wGallery��Adapter���۩w�q��PhotoAdapter 
	     * Gallery����ܸѪR�׬�288���ۤ� */
	    mGallery.setAdapter(new PhotoAdapter(this,bigPhoto));  
	    /* �]�wGallery���Ϥ���ܨƥ� */
	    mGallery.setOnItemSelectedListener(
	        new Gallery.OnItemSelectedListener()  {
	      @Override
	      public void onItemSelected(AdapterView<?> arg0, View arg1,
	                                 int arg2, long arg3)   {
			        try{
			        } catch (Exception e) {
			          /* �o�Ϳ��~�ɦ^��result�^�W�@��activity */
			          Intent intent=new Intent();
			          Bundle bundle = new Bundle();
			          bundle.putString("error",""+e);
			          intent.putExtras(bundle);
			          /* ���~���^�ǭȳ]�w��99 */
			          //gallery.this.setResult(99, intent);
			          //gallery.this.finish();
			        }//end try catch
			      }//end method
		
			      @Override
			      public void onNothingSelected(AdapterView<?> arg0) {
			      }//end method     
	    });	    

        /* �HfindViewById()���oButton����A�å[�JonClickListener */
        ImageButton MapButton = (ImageButton) findViewById(R.id.MapButton01);
        MapButton.setOnClickListener(new Button.OnClickListener()  {
          public void onClick(View v)   {
        	  (new AsyncTasks(album_view.this, AsyncTasks.load_map)).execute(null);
          }//end method
        });
        
        /* �HfindViewById()���oButton����A�å[�JonClickListener */
        ImageButton CameraButton = (ImageButton) findViewById(R.id.CameraButton02);
        CameraButton.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
            /* new�@��Intent����A�ë��w�n�Ұʪ�class */
            Intent intent = new Intent();
        	  intent.setClass(album_view.this, camera_view.class);
        	  
        	  /* �I�s�@�ӷs��Activity */
        	  startActivity(intent);
        	  /* �����쥻��Activity */
        	  album_view.this.finish();
          }
        });
	  }//end method  


	  
	  /* 
	   * ��RXML���o�ۤ���T��method 
	   * 
	   */  
	  private void getPhotoList(String userId,String albumId)
	  {
	    URL url = null;
	    String path="http://picasaweb.google.com/data/feed/api/user/"
	                +userId.trim()+"/albumid/"+albumId.trim();
	    try    {  
	      url = new URL(path);
	      /* �H�ۭq��PhotoHandler�@���ѪRXML��Handler */
	      PhotoHandler handler = new PhotoHandler(); 
	      Xml.parse(url.openConnection().getInputStream(),
	                Xml.Encoding.UTF_8,handler);
	      
	      /* ���o�Ӥ����|(288) */
	      bigPhoto =handler.getBigPhoto();
	    }   catch (Exception e)   { 
	      /* �o�Ϳ��~�ɦ^��result�^�W�@��activity */
	      Intent intent=new Intent();
	      Bundle bundle = new Bundle();
	      bundle.putString("error",""+e);
	      intent.putExtras(bundle);
	      /* ���~���^�ǭȳ]�w��99 */
	      //gallery.this.setResult(99, intent);
	      //gallery.this.finish();
	    }
	  }//end method
	  
	  private class ConnectTask extends AsyncTask<String, Integer, Integer> {
          ProgressDialog progressDlg;
		  @Override
		  protected void onPreExecute() {
			  progressDlg = new ProgressDialog(album_view.this);
			  progressDlg.setMessage("Preparing to load...");			  
			  progressDlg.setIndeterminate(true);
			  progressDlg.setCancelable(false);
			  progressDlg.show();
		  }//end method
		  
		  @Override
		  protected void onProgressUpdate(Integer... progress) {
			  switch (progress[0]) {
			  
			  }//end switch


		  }//end method

		  @Override
		  protected void onPostExecute(Integer result) {			  
			  progressDlg.dismiss();
		  }//end method
		  @Override
		  protected Integer doInBackground(String... params) {
			  publishProgress(0);
			  if (params.length != 2) {
				  return -1; //invalid # of arguments
			  }//end if

			  /* ��RXML���o�ۤ���T��method */
			  String userId = params[0];
			  String albumId = params[1];

			    URL url = null;
			    String path="http://picasaweb.google.com/data/feed/api/user/"
			                +userId.trim()+"/albumid/"+albumId.trim();
			    try  {  
			      url = new URL(path);
			      /* �H�ۭq��PhotoHandler�@���ѪRXML��Handler */
			      PhotoHandler handler = new PhotoHandler(); 
			      Xml.parse(url.openConnection().getInputStream(),
			                Xml.Encoding.UTF_8,handler);
			      
			      /* ���o�Ӥ����|(288) */
			      bigPhoto =handler.getBigPhoto();
				}  catch (Exception e)  { 
					/* �o�Ϳ��~�ɦ^��result�^�W�@��activity */
					Intent intent=new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("error",""+e);
					intent.putExtras(bundle);
					/* ���~���^�ǭȳ]�w��99 */
					//gallery.this.setResult(99, intent);
					//gallery.this.finish();
					return 0;//error
				}//end try catch
				  publishProgress(1);
				  return 1;//success
		  }//end method
	  }//end private class

}//end class