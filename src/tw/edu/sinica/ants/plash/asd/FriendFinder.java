package tw.edu.sinica.ants.plash.asd;

import system.ArActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

public class FriendFinder extends Activity {
	private WebView mWebView1;  
	
	//Login 資料
	private String id, password;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // ******************************************* 
        // fullscreen mode 
        // ******************************************* 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
     
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , 
                       WindowManager.LayoutParams.FLAG_FULLSCREEN );    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendfinder);
        
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
        mWebView1 = (WebView) findViewById(R.id.myWebView1);
        mWebView1.loadUrl("http://plash2.iis.sinica.edu.tw/ASD/android/index.php?uid="+id);
        
        // Map Explorer Button
        ImageButton MapExplorerBtn = (ImageButton) findViewById(R.id.MapExplorerBtn);
        MapExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new一個Intent物件，並指定要啟動的class
        	  Intent intent = new Intent();
        	  intent.setClass(FriendFinder.this, MapExplorer.class);
        	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  FriendFinder.this.finish();
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
        	  intent.setClass(FriendFinder.this, ArActivity.class);        	  
          	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  //MapExplorer.this.finish();
        	  
        	  ArActivity.startWithSetup(FriendFinder.this, new ARStuff(id));
        	  FriendFinder.this.finish();
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
        	  intent.setClass(FriendFinder.this, ASDGallery.class);
        	            	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  FriendFinder.this.finish();
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
        	  intent.setClass(FriendFinder.this, AsdGuidance.class);
          	  
        	  // 呼叫一個新的Activity 
        	  startActivity(intent);
        	  // 關閉原本的Activity 
        	  FriendFinder.this.finish();
          }
        });
        
    }
}