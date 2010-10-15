package tw.edu.sinica.ants.plash.asd;

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

import android.content.Intent;
import android.view.MotionEvent;

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
            /* new�@��Intent����A�ë��w�n�Ұʪ�class */
            Intent intent = new Intent();
        	  intent.setClass(opening.this, AR.class);
        	  
        	  /* �I�s�@�ӷs��Activity */
        	  startActivity(intent);
        	  /* �����쥻��Activity */
        	  opening.this.finish();
		}
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}