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
					CommandShowToast.show(myTargetActivity, "�u!��𤣦n!�U���A�ո�! ");
					Facebook.postMessage("�u!" + userid + "��𤣦n!�U���A�ո�!");
				}else{
					/**
					 * upload result to server here
					 * prize = {0, 1, 2, 3, 4, 5}
					 */
					prize(userid, prize);
					CommandShowToast.show(myTargetActivity, "����! \"" + loot[prize] + "\"�w�s�W��A������ ");
					Facebook.postMessage("����!" + userid + "�o��F" + loot[prize] + "!!!" );
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
		}, "�ӱi��!");
		
		//return to the map activity
		guiSetup.addButtonToTopView(new Command() {
			@Override
			public boolean execute() {
				Intent i = new Intent(activity.getApplicationContext(), MapExplorer.class);
				activity.startActivity(i);
				return false;
			}
		}, "�^��a��");
		
		//add a button to trigger the commandGroup
		guiSetup.addButtonToRightView(gunShot, "���Q\n!!!!!!");
		
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
    	// �ŧi���}�r��
    	String uriAPI = "http://plash2.iis.sinica.edu.tw/prize.php?username="+uid+"&prize="+p; 
    	//�إ�HTTP Get�s�u
    	HttpGet httpRequest = new HttpGet(uriAPI); 
    	try{ 
    		//�o�XHTTP request
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
	
	private final String loot[] = {"��T��ǩҴ��w",
			   "�Ѥ媫�z�Ҵ��w",
			   "�ͪ���ǩҴ��w",
			   "�A�~��ީҴ��w",
			   "���v�y���Ҵ��w",
			   "���|��ǩҴ��w"};
	
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
	
	private final Double[] rota = {170.0, //���ھǬ�s��
								   -10.0, //�g�٬�s��
								   -10.0, //�Ŵ��~�Ϯ��]
								   190.0, //���ά�Ǭ�s����
								   170.0, //�J�A�����]
								   -10.0, //�x�W�ҥj�]
								   170.0, //���v�媫���C�]
								   -10.0, //��N�v��s��
								   170.0, //���v�y����s��
								   170.0, //�ڬ���s��
								    80.0, //��v���ɮ��]
								   -10.0, //���n���N�]
								     0.0, //�������]
								   170.0, //�a�y��Ǭ�s��
								     0.0, //�έp��Ǭ�s��
								   160.0, //��X��|�]
								   100.0, //�ǳN���ʤ���
								   100.0, //��������s��
								   190.0, //��T��Ǭ�s��
								    10.0, //�ͪ��ƾǬ�s��
								   -70.0, //�ū�
								    10.0, //���l�ͪ���s��
								    10.0, //�ͪ���Ǭ�Ǭ�s��
								    10.0, //�ͪ��h�˩ʬ�s����
								     0.0, //����|���]���X��
								     0.0, //��a����ʪ�����
								   190.0, //�H����|��Ǭ�s����
								    10.0, //��T��޳зs��s����
								   190.0, //�ƾǬ�s��
								    90.0, //�x�W�v��s��
								   100.0, //��]���s����
								   100.0, //�Ӫ��[�L�ͪ��Ǭ�s��
								    10.0, //��F�j�ӭp�⤤��
								   100.0, //�l�����w�֧Q��
								   200.0, //���z��s��
								    90.0}; //�H����|����]
	
	private final Double[] nameLat = {25.039734, //���ھǬ�s��
									  25.039767, //�g�٬�s��
									  25.039818, //�Ŵ��~�Ϯ��]
									  25.041201, //���ά�Ǭ�s����
									  25.040761, //�J�A�����]
									  25.039834, //�x�W�ҥj�]
									  25.039828, //���v�媫���C�]
									  25.039885, //��N�v��s��
									  25.039798, //���v�y����s��
									  25.039898, //�ڬ���s��
									  25.040544, //��v���ɮ��]
									  25.039947, //���n���N�]
									  25.041770, //�������]
									  25.040949, //�a�y��Ǭ�s��
									  25.041280, //�έp��Ǭ�s��
									  25.040619, //��X��|�]
									  25.040940, //�ǳN���ʤ���
									  25.041723, //��������s��
									  25.041487, //��T��Ǭ�s��
									  25.042744, //�ͪ��ƾǬ�s��
									  25.042877, //�ū�
									  25.043043, //���l�ͪ���s��
									  25.043295, //�ͪ���Ǭ�Ǭ�s��
									  25.042943, //�ͪ��h�˩ʬ�s����
									  25.045405, //����|���]���X��
									  25.044249, //��a����ʪ�����
									  25.041564, //�H����|��Ǭ�s����
									  25.041767, //��T��޳зs��s����
									  25.041706, //�ƾǬ�s��
									  25.042058, //�x�W�v��s��
									  25.042885, //��]���s����
									  25.042730, //�Ӫ��[�L�ͪ��Ǭ�s��
									  25.041870, //��F�j�ӭp�⤤��
									  25.042447, //�l�����w�֧Q��
									  25.041231, //���z��s��
									  25.041207}; //�H����|����]
	
	private final Double[] nameLon = {121.616755, //���ھǬ�s��
									  121.617109, //�g�٬�s��
									  121.616603, //�Ŵ��~�Ϯ��]
									  121.616237, //���ά�Ǭ�s����
									  121.616028, //�J�A�����]
									  121.616302, //�x�W�ҥj�]
									  121.616083, //���v�媫���C�]
									  121.615629, //��N�v��s��
									  121.615848, //���v�y����s��
									  121.615355, //�ڬ���s��
									  121.615394, //��v���ɮ��]
									  121.615021, //���n���N�]
									  121.614905, //�������]
									  121.614001, //�a�y��Ǭ�s��
									  121.614033, //�έp��Ǭ�s��
									  121.613784, //��X��|�]
									  121.613134, //�ǳN���ʤ���
									  121.612771, //��������s��
									  121.614813, //��T��Ǭ�s��
									  121.613131, //�ͪ��ƾǬ�s��
									  121.613515, //�ū�
									  121.614019, //���l�ͪ���s��
									  121.614785, //�ͪ���Ǭ�Ǭ�s��
									  121.615656, //�ͪ��h�˩ʬ�s����
									  121.613382, //����|���]���X��
									  121.612796, //��a����ʪ�����
									  121.615063, //�H����|��Ǭ�s����
									  121.615759, //��T��޳зs��s����
									  121.615552, //�ƾǬ�s��
									  121.611643, //�x�W�v��s��
									  121.614607, //��]���s����
									  121.615409, //�Ӫ��[�L�ͪ��Ǭ�s��
									  121.616133, //��F�j�ӭp�⤤��
									  121.616429, //�l�����w�֧Q��
									  121.616126, //���z��s��
									  121.612017}; //�H����|����]
	
	private final Double[] boardLat = {25.039624, //���ھǬ�s��
								 	   25.039937, //�g�٬�s��
								 	   25.040214, //�Ŵ��~�Ϯ��]
								 	   25.041112, //���ά�Ǭ�s����
								 	   25.040632, //�J�A�����]
								 	   25.039995, //�x�W�ҥj�]
								 	   25.039746, //���v�媫���C�]
								 	   25.040041, //��N�v��s��
								 	   25.039538, //���v�y����s��
								 	   25.039844, //�ڬ���s��
								 	   25.040690, //��v���ɮ��]
								 	   25.040310, //���n���N�]
								 	   25.041686, //�������]
								 	   25.040813, //�a�y��Ǭ�s��
								 	   25.041336, //�έp��Ǭ�s��
								 	   25.040695, //��X��|�]
								 	   25.040956, //�ǳN���ʤ���
								 	   25.041961, //��������s��
								 	   25.041271, //��T��Ǭ�s��
								 	   25.042814, //�ͪ��ƾǬ�s��
								 	   25.042963, //�ū�
								 	   25.043162, //���l�ͪ���s��
								 	   25.043381, //�ͪ���Ǭ�Ǭ�s��
								 	   25.042725, //�ͪ��h�˩ʬ�s����
								 	   25.045157, //����|���]���X��
								 	   25.043936, //��a����ʪ�����
								 	   25.041436, //�H����|��Ǭ�s����
								 	   25.041843, //��T��޳зs��s����
								 	   25.041586, //�ƾǬ�s��
								 	   25.041503, //�x�W�v��s��
								 	   25.042447, //��]���s����
								 	   25.042507, //�Ӫ��[�L�ͪ��Ǭ�s��
								 	   25.042014, //��F�j�ӭp�⤤��
								 	   25.042618, //�l�����w�֧Q��
								 	   25.041302, //���z��s��
								 	   25.041121}; //�H����|����]

	private final Double[] boardLon = {121.617024, //���ھǬ�s��
									   121.617194, //�g�٬�s��
									   121.616650, //�Ŵ��~�Ϯ��]
									   121.616429, //���ά�Ǭ�s����
									   121.616306, //�J�A�����]
									   121.616225, //�x�W�ҥj�]
									   121.616223, //���v�媫���C�]
									   121.615635, //��N�v��s��
									   121.615820, //���v�y����s��
									   121.615239, //�ڬ���s��
									   121.615393, //��v���ɮ��]
									   121.615056, //���n���N�]
									   121.614354, //�������]
									   121.614079, //�a�y��Ǭ�s��
									   121.613871, //�έp��Ǭ�s��
									   121.613456, //��X��|�]
									   121.612914, //�ǳN���ʤ���
									   121.612358, //��������s��
									   121.614726, //��T��Ǭ�s��
									   121.612898, //�ͪ��ƾǬ�s��
									   121.613471, //�ū�
									   121.613929, //���l�ͪ���s��
									   121.614527, //�ͪ���Ǭ�Ǭ�s��
									   121.615871, //�ͪ��h�˩ʬ�s����
									   121.613552, //����|���]���X��
									   121.613132, //��a����ʪ�����
									   121.615097, //�H����|��Ǭ�s����
									   121.615622, //��T��޳зs��s����
									   121.615604, //�ƾǬ�s��
									   121.611570, //�x�W�v��s��
									   121.614733, //��]���s����
									   121.615345, //�Ӫ��[�L�ͪ��Ǭ�s��
									   121.616235, //��F�j�ӭp�⤤��
									   121.616466, //�l�����w�֧Q��
									   121.616520, //���z��s��
									   121.611740}; //�H����|����]
}
