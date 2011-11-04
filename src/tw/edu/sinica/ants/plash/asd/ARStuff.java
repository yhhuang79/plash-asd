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
	
	private ModelHandler mh;
	
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
				final int prize = r.nextInt(180);
				if(prize > 35){
					CommandShowToast.show(myTargetActivity, "哎!手氣不好!下次再試試! ");
					new Thread(new Runnable() {
						public void run() {
							Facebook.postMessage("哎!" + userid + "手氣不好!下次再試試!");
						}
					}).start();
				}else{
					/**
					 * upload result to server here
					 * prize = {0, 1, 2, 3, ..., 36}
					 */
					prize(userid, prize);
					CommandShowToast.show(myTargetActivity, "恭喜! \"" + loot[prize] + "\"已新增到你的收藏 ");
//					GuiSetup.this.
					new Thread(new Runnable() {
						public void run() {
							Facebook.postMessage("恭喜!" + userid + "得到了" + loot[prize] + "!!!" );
						}
					}).start();
				}
				return false;
			}
		});
	}

	@Override
	public void _b_addWorldsToRenderer(GLRenderer renderer, GLFactory objectFactory, GeoObj currentPosition) {
		ARStuff.renderer = renderer;
		
		//try to create all GeoObjs before entering gpsAction
		//put all name obj first
		for(int i = 0; i < LAT.length; i++){
			GeoObj name = new GeoObj(LAT[i], LON[i]);
			name.setUpdateListener(null);
			world.add(name);
		}
		//put all board obj later
		for(int i=0; i < LAT.length; i++){
			GeoObj board = new GeoObj(LAT[i], LON[i]);
			board.setUpdateListener(null);
			world.add(board);
		}
		//by now the world should contain 72 empty Geo objects, 36 for name, in the front, 
		//36 for board, in the back
		
		renderer.addRenderElement(world);
		
		mh = new ModelHandler(world, camera, renderer, myTargetActivity, LAT, LON);
		
		gpsAction = new ActionCalcRelativePos(world, camera, mh); //need to construct with renderer, wait till renderer is available, instead of creating in step _a
		
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
	
	private final String loot[] = {	"民族學研究所吉祥物",
									"經濟研究所吉祥物",
									"傅斯年圖書館吉祥物",
									"應用科學研究中心吉祥物",
									"胡適紀念館吉祥物",
									"台灣考古館吉祥物",
									"歷史文物陳列館吉祥物",
									"近代史研究所吉祥物",
									"歷史語言研究所吉祥物",
									"歐美研究所吉祥物",
									"近史所檔案館吉祥物",
									"嶺南美術館吉祥物",
									"蔡元培館吉祥物",
									"地球科學研究所吉祥物",
									"統計科學研究所吉祥物",
									"綜合體育館吉祥物",
									"學術活動中心吉祥物",
									"中國文哲研究所吉祥物",
									"資訊科學研究所吉祥物",
									"生物化學研究所吉祥物",
									"溫室吉祥物",
									"分子生物研究所吉祥物",
									"生物醫學科學研究所吉祥物",
									"生物多樣性研究中心吉祥物",
									"中研院附設幼稚園吉祥物",
									"國家實驗動物中心吉祥物",
									"人文社會科學研究中心吉祥物",
									"資訊科技創新研究中心吉祥物",
									"化學研究所吉祥物",
									"台灣史研究所吉祥物",
									"基因體研究中心吉祥物",
									"植物暨微生物學研究所吉祥物",
									"行政大樓計算中心吉祥物",
									"郵局車庫福利社吉祥物",
									"物理研究所吉祥物",
									"人文社會科學館吉祥物"};
	
	/**
	 * all the latitude, longitude pair, plus their corresponding .dae models and .png textures
	 * TODO load these setting from a web server or a local file...
	 */
	
//	private final Double[] rota = {170.0, //民族學研究所
//								   -10.0, //經濟研究所
//								   -10.0, //傅斯年圖書館
//								   190.0, //應用科學研究中心
//								   170.0, //胡適紀念館
//								   -10.0, //台灣考古館
//								   170.0, //歷史文物陳列館
//								   -10.0, //近代史研究所
//								   170.0, //歷史語言研究所
//								   170.0, //歐美研究所
//								    80.0, //近史所檔案館
//								   -10.0, //嶺南美術館
//								     0.0, //蔡元培館
//								   170.0, //地球科學研究所
//								     0.0, //統計科學研究所
//								   160.0, //綜合體育館
//								   100.0, //學術活動中心
//								   100.0, //中國文哲研究所
//								   190.0, //資訊科學研究所
//								    10.0, //生物化學研究所
//								   -70.0, //溫室
//								    10.0, //分子生物研究所
//								    10.0, //生物醫學科學研究所
//								    10.0, //生物多樣性研究中心
//								     0.0, //中研院附設幼稚園
//								     0.0, //國家實驗動物中心
//								   190.0, //人文社會科學研究中心
//								    10.0, //資訊科技創新研究中心
//								   190.0, //化學研究所
//								    90.0, //台灣史研究所
//								   100.0, //基因體研究中心
//								   100.0, //植物暨微生物學研究所
//								    10.0, //行政大樓計算中心
//								   100.0, //郵局車庫福利社
//								   200.0, //物理研究所
//								    90.0}; //人文社會科學館
	
	private final Double[] LAT = {  25.039386, 
									25.040102,
									25.040541,
									25.040879,
									25.040576,
									25.040118,
									25.039525,
									25.040144,
									25.039286,
									25.039647,
									25.040697,
									25.040311,
									25.041804,
									25.040490,
									25.041487,
									25.040414,
									25.040921,
									25.041924,
									25.041078,
									25.043006,
									25.043160,
									25.043402,
									25.043746,
									25.044391,
									25.045146,
									25.044426,
									25.041202,
									25.041910,
									25.041329,
									25.041349,
									25.042344,
									25.042467,
									25.042213,
									25.042672,
									25.041141,
									25.041007,
								};
	private final Double[] LON = {  121.616921,	//民族學研究所
									121.617217,	//經濟研究所
									121.616698,	//傅斯年圖書館
									121.617184,	//應用科學研究中心
									121.616289,	//胡適紀念館
									121.616252,	//台灣考古館
									121.616208,	//歷史文物陳列館
									121.615664,	//近代史研究所
									121.615799,	//歷史語言研究所
									121.615150,	//歐美研究所
									121.615099,	//近史所檔案館
									121.615054,	//嶺南美術館
									121.614102,	//蔡元培館
									121.614057,	//地球科學研究所
									121.613634,	//統計科學研究所
									121.613349,	//綜合體育館
									121.612619,	//學術活動中心
									121.612200,	//中國文哲研究所
									121.614695,	//資訊科學研究所
									121.613056,	//生物化學研究所
									121.615928,	//溫室
									121.613836,	//分子生物研究所
									121.614531,	//生物醫學科學研究所
									121.613827,	//生物多樣性研究中心
									121.613603,	//中研院附設幼稚園
									121.613121,	//國家實驗動物中心
									121.615175,	//人文社會科學研究中心
									121.615460,	//資訊科技創新研究中心
									121.615699,	//化學研究所
									121.611249,	//台灣史研究所
									121.614303,	//基因體研究中心
									121.615186,	//植物暨微生物學研究所
									121.616170,	//行政大樓計算中心
									121.616514,	//郵局車庫福利社
									121.616707,	//物理研究所
									121.611445	//人文社會科學館
								};
}
