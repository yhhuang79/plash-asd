package tw.edu.sinica.ants.plash.asd;

import geo.GeoObj;
import gl.Color;
import gl.CustomGLSurfaceView;
import gl.GLCamera;
import gl.GLFactory;
import gl.GLRenderer;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
import gui.InfoScreenSettings;

import java.io.IOException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import system.EventManager;
import system.Setup;
import util.Vec;
import worldData.Obj;
import worldData.SystemUpdater;
import worldData.UpdateTimer;
import worldData.World;
import actions.ActionCalcRelativePos;
import actions.ActionRotateCameraBuffered;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;

import commands.Command;
import commands.CommandGroup;
import commands.ui.CommandShowToast;
import components.ProximitySensor;

public class ARStuff extends Setup {
	
	private World world;
	private GLCamera camera;
	public static GLRenderer renderer;
	
	private ActionCalcRelativePos gpsAction;
	
	private CommandGroup gunShot;
	
	private String userid;
	private String password;
	private double zeroLat;
	private double zeroLon;
	private double userHeight;
	public static double userVirtualEyeLevel;
	
	private MediaPlayer mpGunShot;
	
	public ARStuff(String userid, String password, double zeroLat, double zeroLon, double userHeight){
		this.userid = userid; //for uploading item choice to server
		this.password = userid;
		this.zeroLat = zeroLat; //a fixed GPS location pass in from mapView
		this.zeroLon = zeroLon;
		this.userHeight = userHeight; //get user height to set camera height for better viewing experience
		ARStuff.userVirtualEyeLevel = (userHeight - 0.2) * GeoObj.MagicNumber;
	}

	public ARStuff(String userid){
		this(userid, "123456", -1.0, -1.0, 1.6);
	}
	
	@Override
	public boolean onKeyDown(Activity a, int keyCode, KeyEvent event) {
		//intercept everything except home and power
		return true;
	}
	
	
	@Override
	public void _a_initFieldsIfNecessary() {
		Log.e("received id and password=", "id= " + userid + ", password= " + password);
		
		camera = new GLCamera(new Vec(0f, 0f, (float)userVirtualEyeLevel));
		world = new World(camera);
		gpsAction = new ActionCalcRelativePos(world, camera);
		
		if(zeroLat > 0.0 && zeroLon > 0.0){
			Location l = new Location(LocationManager.GPS_PROVIDER);
			l.setLatitude(zeroLat);
			l.setLongitude(zeroLon);
			gpsAction.resetWorldZeroPositions(l);
		}
		
		mpGunShot = MediaPlayer.create(myTargetActivity, R.raw.sound_gun1);
		try {
			mpGunShot.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gunShot = new CommandGroup();
		gunShot.add(new Command() { //sound effect
			@Override
			public boolean execute() {
				if(mpGunShot.isPlaying()){ //to avoid creating too many instance of mediaplayer from CommandPlaySound
					mpGunShot.seekTo(0);   //which fails after 5 or 6 creation. There's only 1 instance in this class,
				}else{					   //and it will seek to the beginning if user repeatedly press "FIRE" button
					mpGunShot.start();
				}
				return false;
			}
		});
		gunShot.add(new Command() {
			@Override
			public boolean execute() {
				//generate random number and upload the treasure choice to server...
				Random r = new Random(System.nanoTime());
				int prize = r.nextInt(30);
				if(prize > 5){
					CommandShowToast.show(myTargetActivity, "哎!手氣不好!下次再試試! ");
					Facebook.postMessage("哎!" + userid + "手氣不好!下次再試試!");
				}else{
					/**
					 * upload result to server here
					 * prize = {0, 1, 2, 3, 4, 5}
					 */
					prize(userid, prize);
					CommandShowToast.show(myTargetActivity, "恭喜! \"" + loot[prize] + "\"已新增到你的收藏 ");
					Facebook.postMessage("恭喜!" + userid + "得到了" + loot[prize] + "!!!" );
				}
				return false;
			}
		});
	}

	@Override
	public void _b_addWorldsToRenderer(GLRenderer renderer, GLFactory objectFactory, GeoObj currentPosition) {
		ARStuff.renderer = renderer;
		
		GDXConnection.init(myTargetActivity, renderer);
		
		/*new ModelLoader(renderer, sloganModel, sloganTexture) {
			@Override
			public void modelLoaded(MeshComponent gdxMesh) {
				Obj slogan = new Obj();
				slogan.setPosition(new Vec(0, 0, -39.5f));
				slogan.setComp(gdxMesh);
				slogan.getRenderComp().setScale(new Vec(1.0f, 1.0f, 1.0f));
				world.add(slogan);
			}
		};*/
		
		for(int i = 0; i < 36; i++){ //load all names and boards
			//cannot infer to non-final variables in the new modelLoader
			final int fi = i;
			//load a model from file
			String modelName, textureName;
			if(i<8){
				modelName = genericPrefix+0+(i+2)+nameModelSuffix;
				textureName = genericPrefix+0+(i+2)+nameTextureSuffix;
			}else{
				modelName = genericPrefix+(i+2)+nameModelSuffix;
				textureName = genericPrefix+(i+2)+nameTextureSuffix;
			}
			
			new ModelLoader(renderer, modelName, textureName) {
				@Override
				public void modelLoaded(MeshComponent gdxMesh) {
					GeoObj o = new GeoObj(nameLat[fi], nameLon[fi]);
					o.setUpdateListener(null);
					o.setComp(gdxMesh);
					o.getRenderComp().setScale(new Vec(1.0f, 1.0f, 1.0f));
					o.getRenderComp().setRotation(new Vec(90.0f, 0.0f, rota[fi].floatValue()));
					world.add(o);
				}
			};
			
			//if(i<8){
			String modelBoard, textureBoard;
			if(i<8){
				modelBoard = genericPrefix+0+(i+2)+boardModelSuffix;
				textureBoard = genericPrefix+0+(i+2)+boardTextureSuffix;
			}else{
				modelBoard = genericPrefix+(i+2)+boardModelSuffix;
				textureBoard = genericPrefix+(i+2)+boardTextureSuffix;
			}
			new ModelLoader(renderer, modelBoard, textureBoard) {
				@Override
				public void modelLoaded(MeshComponent gdxMesh) {
					GeoObj o = new GeoObj(boardLat[fi], boardLon[fi]);
					o.setUpdateListener(null);
					o.setComp(gdxMesh);
					o.getRenderComp().setScale(new Vec(1.0f, 1.0f, 1.0f));
					o.getRenderComp().setRotation(new Vec(90.0f, 0.0f, rota[fi].floatValue()));
					world.add(o);
				}
			};
			//}
		}
		renderer.addRenderElement(world);
	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager, CustomGLSurfaceView arView) {
		//rotate the camera when orientation changed
		eventManager.addOnOrientationChangedAction(new ActionRotateCameraBuffered(camera));
		//calculate the relative distance between world zero point and camera when phone location changed
		eventManager.addOnLocationChangedAction(gpsAction);
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		//update the world on every cycle
		updater.addObjectToUpdateCycle(world);
	}
	
	@Override
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, final Activity activity) {
		//add a button and make it take picture
		guiSetup.addButtonToBottomView(new Command() {
			@Override
			public boolean execute() {
				myCameraView.takeAPicture(renderer);
				new Thread(new Runnable() {
					public void run() {
						Facebook.uploadPicture("/sdcard/", myCameraView.pFile);
					}
				}).start();
				return false;
			}
		}, "照張像!");
		
		//return to the map activity
		guiSetup.addButtonToTopView(new Command() {
			@Override
			public boolean execute() {
				Intent i = new Intent(activity.getApplicationContext(), MapExplorer.class);
				activity.startActivity(i);
				return false;
			}
		}, "回到地圖");
		
		//add a button to trigger the commandGroup
		guiSetup.addButtonToRightView(gunShot, "拉霸\n!!!!!!");
		
//		/**
//		 * DEBUG ONLY
//		 */
//		guiSetup.addButtonToBottomView(new Command() {
//			@Override
//			public boolean execute() {
////				for(int i=0; i<world.getAllItems().myLength; i++){
////					Obj o = (Obj)world.getAllItems().get(i);
////					CommandShowToast.show(myTargetActivity, "= " + o.getPosition().x + ", " + o.getPosition().y + ", " + o.getPosition().z);
////				}
//				CommandShowToast.show(myTargetActivity, "= " + camera.getPosition().x + ", " + camera.getPosition().y + ", " + camera.getPosition().z);
//				CommandShowToast.show(myTargetActivity, "= " + camera.getGPSPositionVec().x + ", " + camera.getGPSPositionVec().y);
//				//CommandShowToast.show(myTargetActivity, "= " + camera.getGPSLocation().getAccuracy());
//				return false;
//			}
//		}, "show geoobj location");
		
	}
/*	
	@Override
	public void _f_addInfoScreen(InfoScreenSettings infoScreenData) {
		infoScreenData.addText("something");
	}
*/	
    public String prize(String uid, int p){
    	// 宣告網址字串
    	String uriAPI = "http://plash2.iis.sinica.edu.tw/prize.php?username="+uid+"&prize="+p; 
    	//建立HTTP Get連線
    	HttpGet httpRequest = new HttpGet(uriAPI); 
    	try{ 
    		//發出HTTP request
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); 
        }catch (ClientProtocolException e){  
          //mTextView1.setText(e.getMessage().toString()); 
          e.printStackTrace(); 
        }catch (IOException e){  
          //mTextView1.setText(e.getMessage().toString()); 
          e.printStackTrace(); 
        }catch (Exception e){  
          //mTextView1.setText(e.getMessage().toString()); 
          e.printStackTrace();  
        }
		return null;  	
    }
	
	private final String loot[] = {"資訊科學所智庫",
			   "天文物理所智庫",
			   "生物醫學所智庫",
			   "農業科技所智庫",
			   "歷史語言所智庫",
			   "社會科學所智庫"};
	
	/**
	 * all the latitude, longitude pair, plus their corresponding .dae models and .png textures
	 * TODO load these setting from a web server or a local file...
	 */
	
	private final String sloganModel = "asd_ar_01_slogan.dae";
	private final String sloganTexture = "asd_ar_01_slogantexture.png";
	private final String genericPrefix = "asd_ar_";
	private final String nameModelSuffix = "_name.dae";
	private final String nameTextureSuffix = "_nametexture.png";
	private final String boardModelSuffix = "_board.dae";
	private final String boardTextureSuffix = "_boardtexture.png";
	
	private final Double[] rota = {170.0, //民族學研究所
								   -10.0, //經濟研究所
								   -10.0, //傅斯年圖書館
								   190.0, //應用科學研究中心
								   170.0, //胡適紀念館
								   -10.0, //台灣考古館
								   170.0, //歷史文物陳列館
								   -10.0, //近代史研究所
								   170.0, //歷史語言研究所
								   170.0, //歐美研究所
								    80.0, //近史所檔案館
								   -10.0, //嶺南美術館
								     0.0, //蔡元培館
								   170.0, //地球科學研究所
								     0.0, //統計科學研究所
								   160.0, //綜合體育館
								   100.0, //學術活動中心
								   100.0, //中國文哲研究所
								   190.0, //資訊科學研究所
								    10.0, //生物化學研究所
								   -70.0, //溫室
								    10.0, //分子生物研究所
								    10.0, //生物醫學科學研究所
								    10.0, //生物多樣性研究中心
								     0.0, //中研院附設幼稚園
								     0.0, //國家實驗動物中心
								   190.0, //人文社會科學研究中心
								    10.0, //資訊科技創新研究中心
								   190.0, //化學研究所
								    90.0, //台灣史研究所
								   100.0, //基因體研究中心
								   100.0, //植物暨微生物學研究所
								    10.0, //行政大樓計算中心
								   100.0, //郵局車庫福利社
								   200.0, //物理研究所
								    90.0}; //人文社會科學館
	
	private final Double[] nameLat = {25.039734, //民族學研究所
									  25.039767, //經濟研究所
									  25.039818, //傅斯年圖書館
									  25.041201, //應用科學研究中心
									  25.040761, //胡適紀念館
									  25.039834, //台灣考古館
									  25.039828, //歷史文物陳列館
									  25.039885, //近代史研究所
									  25.039798, //歷史語言研究所
									  25.039898, //歐美研究所
									  25.040544, //近史所檔案館
									  25.039947, //嶺南美術館
									  25.041770, //蔡元培館
									  25.040949, //地球科學研究所
									  25.041280, //統計科學研究所
									  25.040619, //綜合體育館
									  25.040940, //學術活動中心
									  25.041723, //中國文哲研究所
									  25.041487, //資訊科學研究所
									  25.042744, //生物化學研究所
									  25.042877, //溫室
									  25.043043, //分子生物研究所
									  25.043295, //生物醫學科學研究所
									  25.042943, //生物多樣性研究中心
									  25.045405, //中研院附設幼稚園
									  25.044249, //國家實驗動物中心
									  25.041564, //人文社會科學研究中心
									  25.041767, //資訊科技創新研究中心
									  25.041706, //化學研究所
									  25.042058, //台灣史研究所
									  25.042885, //基因體研究中心
									  25.042730, //植物暨微生物學研究所
									  25.041870, //行政大樓計算中心
									  25.042447, //郵局車庫福利社
									  25.041231, //物理研究所
									  25.041207}; //人文社會科學館
	
	private final Double[] nameLon = {121.616755, //民族學研究所
									  121.617109, //經濟研究所
									  121.616603, //傅斯年圖書館
									  121.616237, //應用科學研究中心
									  121.616028, //胡適紀念館
									  121.616302, //台灣考古館
									  121.616083, //歷史文物陳列館
									  121.615629, //近代史研究所
									  121.615848, //歷史語言研究所
									  121.615355, //歐美研究所
									  121.615394, //近史所檔案館
									  121.615021, //嶺南美術館
									  121.614905, //蔡元培館
									  121.614001, //地球科學研究所
									  121.614033, //統計科學研究所
									  121.613784, //綜合體育館
									  121.613134, //學術活動中心
									  121.612771, //中國文哲研究所
									  121.614813, //資訊科學研究所
									  121.613131, //生物化學研究所
									  121.613515, //溫室
									  121.614019, //分子生物研究所
									  121.614785, //生物醫學科學研究所
									  121.615656, //生物多樣性研究中心
									  121.613382, //中研院附設幼稚園
									  121.612796, //國家實驗動物中心
									  121.615063, //人文社會科學研究中心
									  121.615759, //資訊科技創新研究中心
									  121.615552, //化學研究所
									  121.611643, //台灣史研究所
									  121.614607, //基因體研究中心
									  121.615409, //植物暨微生物學研究所
									  121.616133, //行政大樓計算中心
									  121.616429, //郵局車庫福利社
									  121.616126, //物理研究所
									  121.612017}; //人文社會科學館
	
	private final Double[] boardLat = {25.039624, //民族學研究所
								 	   25.039937, //經濟研究所
								 	   25.040214, //傅斯年圖書館
								 	   25.041112, //應用科學研究中心
								 	   25.040632, //胡適紀念館
								 	   25.039995, //台灣考古館
								 	   25.039746, //歷史文物陳列館
								 	   25.040041, //近代史研究所
								 	   25.039538, //歷史語言研究所
								 	   25.039844, //歐美研究所
								 	   25.040690, //近史所檔案館
								 	   25.040310, //嶺南美術館
								 	   25.041686, //蔡元培館
								 	   25.040813, //地球科學研究所
								 	   25.041336, //統計科學研究所
								 	   25.040695, //綜合體育館
								 	   25.040956, //學術活動中心
								 	   25.041961, //中國文哲研究所
								 	   25.041271, //資訊科學研究所
								 	   25.042814, //生物化學研究所
								 	   25.042963, //溫室
								 	   25.043162, //分子生物研究所
								 	   25.043381, //生物醫學科學研究所
								 	   25.042725, //生物多樣性研究中心
								 	   25.045157, //中研院附設幼稚園
								 	   25.043936, //國家實驗動物中心
								 	   25.041436, //人文社會科學研究中心
								 	   25.041843, //資訊科技創新研究中心
								 	   25.041586, //化學研究所
								 	   25.041503, //台灣史研究所
								 	   25.042447, //基因體研究中心
								 	   25.042507, //植物暨微生物學研究所
								 	   25.042014, //行政大樓計算中心
								 	   25.042618, //郵局車庫福利社
								 	   25.041302, //物理研究所
								 	   25.041121}; //人文社會科學館

	private final Double[] boardLon = {121.617024, //民族學研究所
									   121.617194, //經濟研究所
									   121.616650, //傅斯年圖書館
									   121.616429, //應用科學研究中心
									   121.616306, //胡適紀念館
									   121.616225, //台灣考古館
									   121.616223, //歷史文物陳列館
									   121.615635, //近代史研究所
									   121.615820, //歷史語言研究所
									   121.615239, //歐美研究所
									   121.615393, //近史所檔案館
									   121.615056, //嶺南美術館
									   121.614354, //蔡元培館
									   121.614079, //地球科學研究所
									   121.613871, //統計科學研究所
									   121.613456, //綜合體育館
									   121.612914, //學術活動中心
									   121.612358, //中國文哲研究所
									   121.614726, //資訊科學研究所
									   121.612898, //生物化學研究所
									   121.613471, //溫室
									   121.613929, //分子生物研究所
									   121.614527, //生物醫學科學研究所
									   121.615871, //生物多樣性研究中心
									   121.613552, //中研院附設幼稚園
									   121.613132, //國家實驗動物中心
									   121.615097, //人文社會科學研究中心
									   121.615622, //資訊科技創新研究中心
									   121.615604, //化學研究所
									   121.611570, //台灣史研究所
									   121.614733, //基因體研究中心
									   121.615345, //植物暨微生物學研究所
									   121.616235, //行政大樓計算中心
									   121.616466, //郵局車庫福利社
									   121.616520, //物理研究所
									   121.611740}; //人文社會科學館
}
