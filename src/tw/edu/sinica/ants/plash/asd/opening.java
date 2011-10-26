package tw.edu.sinica.ants.plash.asd;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class opening extends BaseGameActivity implements IOnSceneTouchListener {	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 720;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;

	private Texture mTexture;
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mEnemyTextureRegion;

	private Texture mAutoParallaxBackgroundTexture;

	private TextureRegion mParallaxLayerBack;
	private TextureRegion mParallaxLayerMid;
	private TextureRegion mParallaxLayerFront;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT , new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}

	@Override
	public void onLoadResources() {
		this.mTexture = new Texture(256, 128, TextureOptions.BILINEAR);
		this.mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexture, this, "gfx/player.png", 0, 0, 3, 4);
		this.mEnemyTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexture, this, "gfx/enemy.png", 73, 0, 3, 4);

		this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024, TextureOptions.DEFAULT);
		this.mParallaxLayerFront = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "gfx/parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerBack = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "gfx/iis_background_layer_back2.png", 0, 188);
		this.mParallaxLayerMid = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "gfx/parallax_background_layer_mid.png", 0, 909);

		this.mEngine.getTextureManager().loadTextures(this.mTexture, this.mAutoParallaxBackgroundTexture);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene(1);
		scene.setOnSceneTouchListener(this);
		
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.addParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack)));
		autoParallaxBackground.addParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 500, this.mParallaxLayerMid)));
		autoParallaxBackground.addParallaxEntity(new ParallaxEntity(-20.0f, new Sprite(0, 80, this.mParallaxLayerMid)));		
		autoParallaxBackground.addParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront)));		
		scene.setBackground(autoParallaxBackground);

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final int playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getTileWidth()) / 2;
		final int playerY = CAMERA_HEIGHT - this.mPlayerTextureRegion.getTileHeight() - 5;

		/* Create two sprits and add it to the scene. */
		final AnimatedSprite player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion);
		player.setScaleCenterY(this.mPlayerTextureRegion.getTileHeight());
		player.setScale(2);
		player.animate(new long[]{200, 200, 200}, 3, 5, true);

		final AnimatedSprite enemy = new AnimatedSprite(playerX - 80, playerY, this.mEnemyTextureRegion);
		enemy.setScaleCenterY(this.mEnemyTextureRegion.getTileHeight());
		enemy.setScale(2);
		enemy.animate(new long[]{200, 200, 200}, 3, 5, true);

		scene.getTopLayer().addEntity(player);
		scene.getTopLayer().addEntity(enemy);

		return scene;
	}

	@Override
	public void onLoadComplete() {

	}
	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {

            LayoutInflater inflater = LayoutInflater.from(opening.this);
            View login = inflater.inflate(R.layout.login,null);
            final EditText passwordbox=(EditText)login.findViewById(R.id.passwordbox);
            final EditText idbox=(EditText)login.findViewById(R.id.id);
            
	        new AlertDialog.Builder(this)
	        .setTitle("登入ASD 2011")
	        .setMessage("請輸入帳號密碼")
	        .setPositiveButton("OK", new OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {							

	                String id = idbox.getText().toString();
	                String password = passwordbox.getText().toString();
	                int sid = login(id,password);
	                if(sid != -1){
	                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	                    SharedPreferences.Editor editor = preferences.edit();
	                    editor.putString("id", id);
	                    editor.putInt("sid", sid);
	                    editor.putString("password", password);
	                    
	                    
	                    editor.commit();    

	                    Intent ServiceIntent = new Intent(opening.this, tw.edu.sinica.ants.plash.asd.LocationService.class);
	                    opening.this.startService(ServiceIntent);
	                	// new一個Intent物件，並指定要啟動的class
	                	Intent intent = new Intent();
	                	intent.setClass(opening.this, MapExplorer.class); 
	            	
	          	  		//Bundle bundle = new Bundle();
	          	  		//bundle.putString("id", id);
	          	  		//bundle.putString("password", password);
	        	  
	          	  		//intent.putExtras(bundle);
	                	
	          	  		// 呼叫一個新的Activity
	          	  		startActivity(intent);
	          	  		// 關閉原本的Activity
	          	  		opening.this.finish();
	                }	                
	            }
	        })
	        // 設定EditText
	        .setView(login)
	        .create()
	        .show();			
		}
		return false;
	}
	
    public int login(String username, String password){
		//set host name and action type
		String deviceType = "phone";
		String location = null;
		
		String host = GetHostAddress.hostAddress();
		
		if (username.equalsIgnoreCase("")||password.equalsIgnoreCase("")){
			return -1;
		}
		
		//Danny: 4/20/2011
		//Mule Structure 
		String action = "Login?";
		String temp1 = "username="+username;
		String temp2 = "&password="+password;
		//Device type = phone or browser
		String temp3 = "&deviceType="+deviceType;
		String parameter = temp1+temp2+temp3;
		
		location = host+action+parameter;
		
		System.out.println(location);
		
		String results = null;
		
//		URL url = null;
//		
//		try {
//			url = new URL(location);						
//		}catch(MalformedURLException  e){  
//		
//		}//end try
		
		if (location != null){
			try{
				
				if (!PLASHConnectionManager.isNetworkAvailable()){
					Log.d("Danny: enter here @LoginAndroid -----------------> ", location);					
				}
				else {
				
					//----------------------------------------------//
					//Danny: 4/25/2011
					JSONObject j = PLASHConnectionManager.httpsGet(location);
					
					if(j == null){ //DO NOT PROCEED IF CONNECTION FAILED
						return -1;
					}//fi
					String inputLine = j.getString("message");
					//----------------------------------------------//
					
					results+="\n" + inputLine;
					//lblInformation.setText(results);
					
					//"Account is not activated!"
					if (inputLine.equals("Inactivate")){
					} else if (inputLine.equals("Login Fail")){ //"Username and Password do not match!"
					}else if (inputLine.contains("Successful Login:")){	//Successful Login		
						
						//Danny: 4/20/2011
						String userID = j.getString("sid");

						return Integer.parseInt(userID.toString());
						
						
						}//fi
				}
			}catch (JSONException e) {
				return -1;
			} catch (NullDataException e) {//DO NOT PROCEED IF CONNECTION FAILED
				return -1;
								
			}//end try catch
		}else{
		
		}//end if
		return -1;
    }
}