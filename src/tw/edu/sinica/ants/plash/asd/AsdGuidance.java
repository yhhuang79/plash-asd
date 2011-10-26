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
import android.widget.Button;
import android.widget.ImageButton;

public class AsdGuidance extends Activity {
    /** Called when the activity is first created. */
	//Login ���
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
        setContentView(R.layout.asdguidance);

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());                
        
        id = sp.getString("id", "");
        password =sp.getString("password", "");
        
        // Map Explorer Button
        ImageButton MapExplorerBtn = (ImageButton) findViewById(R.id.MapExplorerBtn);
        MapExplorerBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(AsdGuidance.this, MapExplorer.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  AsdGuidance.this.finish();
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
        	  intent.setClass(AsdGuidance.this, ArActivity.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  //MapExplorer.this.finish();
        	  
        	  ArActivity.startWithSetup(AsdGuidance.this, new ARStuff(id));
        	  AsdGuidance.this.finish();
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
        	  intent.setClass(AsdGuidance.this, FriendFinder.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  AsdGuidance.this.finish();
          }
        });        
        // ASD Gallery Button
        ImageButton ASDGalleryBtn = (ImageButton) findViewById(R.id.ASDGalleryBtn);
        ASDGalleryBtn.setOnClickListener(new Button.OnClickListener()
        {
          public void onClick(View v)
          {
        	  // new�@��Intent����A�ë��w�n�Ұʪ�class
        	  Intent intent = new Intent();
        	  intent.setClass(AsdGuidance.this, ASDGallery.class);
        	  
        	  // �I�s�@�ӷs��Activity 
        	  startActivity(intent);
        	  // �����쥻��Activity 
        	  AsdGuidance.this.finish();
          }
        });
    }
}