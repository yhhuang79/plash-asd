package tw.edu.sinica.ants.plash.asd;

/* import����class */
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.View;
import android.widget.AdapterView;
import android.util.Xml;
import android.widget.Gallery;
import android.widget.ViewSwitcher.ViewFactory;

public class gallery extends Activity implements ViewFactory
{
	  private Gallery mGallery;  
	  private List<String> bigPhoto=new ArrayList<String>();
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState)
	  {
	    super.onCreate(savedInstanceState);
	    /* �]�wlayout��photoshow.xml */
	    setContentView(R.layout.gallerymode);

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
	          gallery.this.setResult(99, intent);
	          gallery.this.finish();
	        } 
	      }

	      @Override
	      public void onNothingSelected(AdapterView<?> arg0)
	      {
	      }     
	    });
	    
	    /* �HfindViewById()���oButton����A�å[�JonClickListener */
	    ImageButton b3_1 = (ImageButton) findViewById(R.id.MapMode3);
	    b3_1.setOnClickListener(new Button.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	        /* new�@��Intent����A�ë��w�n�Ұʪ�class */
	        Intent intent = new Intent();
	    	  intent.setClass(gallery.this, Map.class);
	    	  
	    	  /* �I�s�@�ӷs��Activity */
	    	  startActivity(intent);
	    	  /* �����쥻��Activity */
	    	  gallery.this.finish();
	      }
	    });
	    
	    /* �HfindViewById()���oButton����A�å[�JonClickListener */
	    ImageButton b3_2 = (ImageButton) findViewById(R.id.CameraMode3);
	    b3_2.setOnClickListener(new Button.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	        /* new�@��Intent����A�ë��w�n�Ұʪ�class */
	        Intent intent = new Intent();
	    	  intent.setClass(gallery.this, AR.class);
	    	  
	    	  /* �I�s�@�ӷs��Activity */
	    	  startActivity(intent);
	    	  /* �����쥻��Activity */
	    	  gallery.this.finish();
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
	      gallery.this.setResult(99, intent);
	      gallery.this.finish();
	    }
	  }

	@Override
	public View makeView() {
		// TODO Auto-generated method stub
		return null;
	}
}