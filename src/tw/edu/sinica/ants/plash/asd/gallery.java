package tw.edu.sinica.ants.plash.asd;

/* import相關class */
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
	    /* 設定layout為photoshow.xml */
	    setContentView(R.layout.gallerymode);

	    /* 取得Bundle中的變數 */
	    String userId = "plash.ants";
	    String albumId = "5525227127342865393";
	    
	    /* 呼叫getPhotoList()取得解析後的List */
	    this.getPhotoList(userId,albumId);
	    
	    mGallery = (Gallery) findViewById(R.id.gallery);
	    /* 設定Gallery的Adapter為自定義的PhotoAdapter 
	     * Gallery內顯示解析度為288的相片 */
	    mGallery.setAdapter(new PhotoAdapter(this,bigPhoto));  
	    /* 設定Gallery的圖片選擇事件 */
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
	          /* 發生錯誤時回傳result回上一個activity */
	          Intent intent=new Intent();
	          Bundle bundle = new Bundle();
	          bundle.putString("error",""+e);
	          intent.putExtras(bundle);
	          /* 錯誤的回傳值設定為99 */
	          gallery.this.setResult(99, intent);
	          gallery.this.finish();
	        } 
	      }

	      @Override
	      public void onNothingSelected(AdapterView<?> arg0)
	      {
	      }     
	    });
	    
	    /* 以findViewById()取得Button物件，並加入onClickListener */
	    ImageButton b3_1 = (ImageButton) findViewById(R.id.MapMode3);
	    b3_1.setOnClickListener(new Button.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	        /* new一個Intent物件，並指定要啟動的class */
	        Intent intent = new Intent();
	    	  intent.setClass(gallery.this, Map.class);
	    	  
	    	  /* 呼叫一個新的Activity */
	    	  startActivity(intent);
	    	  /* 關閉原本的Activity */
	    	  gallery.this.finish();
	      }
	    });
	    
	    /* 以findViewById()取得Button物件，並加入onClickListener */
	    ImageButton b3_2 = (ImageButton) findViewById(R.id.CameraMode3);
	    b3_2.setOnClickListener(new Button.OnClickListener()
	    {
	      public void onClick(View v)
	      {
	        /* new一個Intent物件，並指定要啟動的class */
	        Intent intent = new Intent();
	    	  intent.setClass(gallery.this, AR.class);
	    	  
	    	  /* 呼叫一個新的Activity */
	    	  startActivity(intent);
	    	  /* 關閉原本的Activity */
	    	  gallery.this.finish();
	      }
	    });	    
	  }  
	  
	  /* 剖析XML取得相片資訊的method */
	  private void getPhotoList(String userId,String albumId)
	  {
	    URL url = null;
	    String path="http://picasaweb.google.com/data/feed/api/user/"
	                +userId.trim()+"/albumid/"+albumId.trim();
	    try
	    {  
	      url = new URL(path);
	      /* 以自訂的PhotoHandler作為解析XML的Handler */
	      PhotoHandler handler = new PhotoHandler(); 
	      Xml.parse(url.openConnection().getInputStream(),
	                Xml.Encoding.UTF_8,handler);
	      
	      /* 取得照片路徑(288) */
	      bigPhoto =handler.getBigPhoto();
	    }
	    catch (Exception e)
	    { 
	      /* 發生錯誤時回傳result回上一個activity */
	      Intent intent=new Intent();
	      Bundle bundle = new Bundle();
	      bundle.putString("error",""+e);
	      intent.putExtras(bundle);
	      /* 錯誤的回傳值設定為99 */
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