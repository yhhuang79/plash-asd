package tw.edu.sinica.ants.plash.asd;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import system.ArActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class ASDGallery extends Activity implements ViewFactory{
	
	  private ImageSwitcher mSwitcher;
	  private Gallery mGallery;  
	  private List<String> smallPhoto=new ArrayList<String>();
	  private List<String> bigPhoto=new ArrayList<String>();	  
		
	  //Login 資料
	  private String id, password;
		
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
        setContentView(R.layout.asdgallery);

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
	    String userId = "plash.ants";
	    String albumId = "5665636282116258881";
	    
	    // 呼叫getPhotoList()取得解析後的List
	    this.getPhotoList(userId,albumId);
	
	    // 設定Switcher
	    mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
	    mSwitcher.setFactory(this);	    
	    // 設定載入Switcher的模式
	    mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
	            android.R.anim.fade_in));
	    // 設定輸出Switcher的模式
	    mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
	            android.R.anim.fade_out));	    
	    mGallery = (Gallery) findViewById(R.id.gallery);
	    /* 設定Gallery的Adapter為自定義的PhotoAdapter 
	     * Gallery內顯示解析度為288的相片 */
	    mGallery.setAdapter(new PhotoAdapter(this,smallPhoto));  
	    // 設定Gallery的圖片選擇事件 	    	      
	    mGallery.setOnItemSelectedListener(
	        new Gallery.OnItemSelectedListener() {
	      @Override
	      public void onItemSelected(AdapterView<?> arg0, View arg1,
	                                 int arg2, long arg3) {
	    	URL url;  
	        try {
	            // Switcher內顯示解析度為288的相片
	            url = new URL(bigPhoto.get(arg2).toString());
	            URLConnection conn = url.openConnection(); 
	            conn.connect();
	            mSwitcher.setImageDrawable(
	                Drawable.createFromStream(conn.getInputStream(),
	                "PHOTO"));
	        } catch (Exception e) {
	          // 發生錯誤時回傳result回上一個activity 
	          Intent intent=new Intent();
	          Bundle bundle = new Bundle();
	          bundle.putString("error",""+e);
	          intent.putExtras(bundle);
	          // 錯誤的回傳值設定為99
	          //gallery.this.setResult(99, intent);
	          //gallery.this.finish();
	        } 
	      }

	      @Override
	      public void onNothingSelected(AdapterView<?> arg0) {
	      }     
	    });	    

        // Map Explorer Button
        ImageButton MapExplorerBtn = (ImageButton) findViewById(R.id.MapExplorerBtn);
        MapExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(ASDGallery.this, MapExplorer.class);
        	  
        	  Bundle bundle = new Bundle();
          	  bundle.putString("id", id);
          	  bundle.putString("password", password);
          	  intent.putExtras(bundle);
          	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  ASDGallery.this.finish();
          }
        });

        // AR Explorer
        ImageButton ARExplorerBtn = (ImageButton) findViewById(R.id.ARExplorerBtn);
        ARExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(ASDGallery.this, ArActivity.class);
          	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  //MapExplorer.this.finish();
        	  
        	  ArActivity.startWithSetup(ASDGallery.this, new ARStuff(id));
        	  ASDGallery.this.finish();
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
        	  intent.setClass(ASDGallery.this, FriendFinder.class);
        	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  ASDGallery.this.finish();
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
        	  intent.setClass(ASDGallery.this, AsdGuidance.class);
        	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  ASDGallery.this.finish();
          }
        });     	  
	  }  
	  
	  // 剖析XML取得相片資訊的method
	  private void getPhotoList(String userId,String albumId) {
	    URL url = null;
	    String path="http://picasaweb.google.com/data/feed/api/user/"
	                +userId.trim()+"/albumid/"+albumId.trim();
	    try {  
	      url = new URL(path);
	      // 以自訂的PhotoHandler作為解析XML的Handler
	      PhotoHandler handler = new PhotoHandler(); 
	      Xml.parse(url.openConnection().getInputStream(),
	                Xml.Encoding.UTF_8,handler);
	      
	      // 取得照片路徑(72 & 288)
	      Display display = getWindowManager().getDefaultDisplay(); 
	      if(display.getHeight()<=480) {
	    	  smallPhoto =handler.getSmallPhoto();
	    	  bigPhoto =handler.getBigPhoto();	    	  
	      } else {
	    	  smallPhoto =handler.getMidPhoto();
	    	  bigPhoto =handler.getBigPhoto();
	      }
	    } catch (Exception e) { 
	      // 發生錯誤時回傳result回上一個activity
	      //Intent intent=new Intent();
	      //Bundle bundle = new Bundle();
	      //bundle.putString("error",""+e);
	      //intent.putExtras(bundle);
	      /* 錯誤的回傳值設定為99 */
	      //gallery.this.setResult(99, intent);
	      //gallery.this.finish();
	    }
	  }

	@Override
	public View makeView() {
		// TODO Auto-generated method stub
	    ImageView i = new ImageView(this);
	    i.setBackgroundColor(0x0);
	    i.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    i.setLayoutParams(new ImageSwitcher.LayoutParams(
	        LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	    
	    return i;
	}

}