package tw.edu.sinica.ants.plash.asd;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Gallery;

public class album_view extends Activity {
	  private Gallery mGallery;  
	  private List<String> bigPhoto=new ArrayList<String>();
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState)
	  {
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
	    
	    mGallery = (Gallery) findViewById(R.id.gallery);
	    /* �]�wGallery��Adapter���۩w�q��PhotoAdapter 
	     * Gallery����ܸѪR�׬�288���ۤ� */
	    mGallery.setAdapter(new PhotoAdapter(this,bigPhoto));  
	    /* �]�wGallery���Ϥ���ܨƥ� */
	    mGallery.setOnItemSelectedListener(
	        new Gallery.OnItemSelectedListener()
	    {
	      @Override
	      public void onItemSelected(AdapterView<?> arg0, View arg1,
	                                 int arg2, long arg3)
	      {
	        try
	        {
	        } 
	        catch (Exception e)
	        {
	          /* �o�Ϳ��~�ɦ^��result�^�W�@��activity */
	          Intent intent=new Intent();
	          Bundle bundle = new Bundle();
	          bundle.putString("error",""+e);
	          intent.putExtras(bundle);
	          /* ���~���^�ǭȳ]�w��99 */
	          //gallery.this.setResult(99, intent);
	          //gallery.this.finish();
	        } 
	      }

	      @Override
	      public void onNothingSelected(AdapterView<?> arg0)
	      {
	      }     
	    });	    
	  }  
	  
	  /* ��RXML���o�ۤ���T��method */
	  private void getPhotoList(String userId,String albumId)
	  {
	    URL url = null;
	    String path="http://picasaweb.google.com/data/feed/api/user/"
	                +userId.trim()+"/albumid/"+albumId.trim();
	    try
	    {  
	      url = new URL(path);
	      /* �H�ۭq��PhotoHandler�@���ѪRXML��Handler */
	      PhotoHandler handler = new PhotoHandler(); 
	      Xml.parse(url.openConnection().getInputStream(),
	                Xml.Encoding.UTF_8,handler);
	      
	      /* ���o�Ӥ����|(288) */
	      bigPhoto =handler.getBigPhoto();
	    }
	    catch (Exception e)
	    { 
	      /* �o�Ϳ��~�ɦ^��result�^�W�@��activity */
	      Intent intent=new Intent();
	      Bundle bundle = new Bundle();
	      bundle.putString("error",""+e);
	      intent.putExtras(bundle);
	      /* ���~���^�ǭȳ]�w��99 */
	      //gallery.this.setResult(99, intent);
	      //gallery.this.finish();
	    }
	  }

}