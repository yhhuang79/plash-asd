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
		
	  //Login ���
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
	    
	    // �I�sgetPhotoList()���o�ѪR�᪺List
	    this.getPhotoList(userId,albumId);
	
	    // �]�wSwitcher
	    mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
	    mSwitcher.setFactory(this);	    
	    // �]�w���JSwitcher���Ҧ�
	    mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
	            android.R.anim.fade_in));
	    // �]�w��XSwitcher���Ҧ�
	    mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
	            android.R.anim.fade_out));	    
	    mGallery = (Gallery) findViewById(R.id.gallery);
	    /* �]�wGallery��Adapter���۩w�q��PhotoAdapter 
	     * Gallery����ܸѪR�׬�288���ۤ� */
	    mGallery.setAdapter(new PhotoAdapter(this,smallPhoto));  
	    // �]�wGallery���Ϥ���ܨƥ� 	    	      
	    mGallery.setOnItemSelectedListener(
	        new Gallery.OnItemSelectedListener() {
	      @Override
	      public void onItemSelected(AdapterView<?> arg0, View arg1,
	                                 int arg2, long arg3) {
	    	URL url;  
	        try {
	            // Switcher����ܸѪR�׬�288���ۤ�
	            url = new URL(bigPhoto.get(arg2).toString());
	            URLConnection conn = url.openConnection(); 
	            conn.connect();
	            mSwitcher.setImageDrawable(
	                Drawable.createFromStream(conn.getInputStream(),
	                "PHOTO"));
	        } catch (Exception e) {
	          // �o�Ϳ��~�ɦ^��result�^�W�@��activity 
	          Intent intent=new Intent();
	          Bundle bundle = new Bundle();
	          bundle.putString("error",""+e);
	          intent.putExtras(bundle);
	          // ���~���^�ǭȳ]�w��99
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
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(ASDGallery.this, MapExplorer.class);
        	  
        	  Bundle bundle = new Bundle();
          	  bundle.putString("id", id);
          	  bundle.putString("password", password);
          	  intent.putExtras(bundle);
          	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  ASDGallery.this.finish();
          }
        });

        // AR Explorer
        ImageButton ARExplorerBtn = (ImageButton) findViewById(R.id.ARExplorerBtn);
        ARExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(ASDGallery.this, ArActivity.class);
          	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
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
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(ASDGallery.this, FriendFinder.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  ASDGallery.this.finish();
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
        	  intent.setClass(ASDGallery.this, AsdGuidance.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  ASDGallery.this.finish();
          }
        });     	  
	  }  
	  
	  // ��RXML���o�ۤ���T��method
	  private void getPhotoList(String userId,String albumId) {
	    URL url = null;
	    String path="http://picasaweb.google.com/data/feed/api/user/"
	                +userId.trim()+"/albumid/"+albumId.trim();
	    try {  
	      url = new URL(path);
	      // �H�ۭq��PhotoHandler�@���ѪRXML��Handler
	      PhotoHandler handler = new PhotoHandler(); 
	      Xml.parse(url.openConnection().getInputStream(),
	                Xml.Encoding.UTF_8,handler);
	      
	      // ���o�Ӥ����|(72 & 288)
	      Display display = getWindowManager().getDefaultDisplay(); 
	      if(display.getHeight()<=480) {
	    	  smallPhoto =handler.getSmallPhoto();
	    	  bigPhoto =handler.getBigPhoto();	    	  
	      } else {
	    	  smallPhoto =handler.getMidPhoto();
	    	  bigPhoto =handler.getBigPhoto();
	      }
	    } catch (Exception e) { 
	      // �o�Ϳ��~�ɦ^��result�^�W�@��activity
	      //Intent intent=new Intent();
	      //Bundle bundle = new Bundle();
	      //bundle.putString("error",""+e);
	      //intent.putExtras(bundle);
	      /* ���~���^�ǭȳ]�w��99 */
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