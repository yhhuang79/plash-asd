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
					CommandShowToast.show(myTargetActivity, "�u!��𤣦n!�U���A�ո�! ");
					new Thread(new Runnable() {
						public void run() {
							Facebook.postMessage("�u!" + userid + "��𤣦n!�U���A�ո�!");
						}
					}).start();
				}else{
					/**
					 * upload result to server here
					 * prize = {0, 1, 2, 3, ..., 36}
					 */
					prize(userid, prize);
					CommandShowToast.show(myTargetActivity, "����! \"" + loot[prize] + "\"�w�s�W��A������ ");
//					GuiSetup.this.
					new Thread(new Runnable() {
						public void run() {
							Facebook.postMessage("����!" + userid + "�o��F" + loot[prize] + "!!!" );
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
	
	private final String loot[] = {	"���ھǬ�s�ҦN����",
									"�g�٬�s�ҦN����",
									"�Ŵ��~�Ϯ��]�N����",
									"���ά�Ǭ�s���ߦN����",
									"�J�A�����]�N����",
									"�x�W�ҥj�]�N����",
									"���v�媫���C�]�N����",
									"��N�v��s�ҦN����",
									"���v�y����s�ҦN����",
									"�ڬ���s�ҦN����",
									"��v���ɮ��]�N����",
									"���n���N�]�N����",
									"�������]�N����",
									"�a�y��Ǭ�s�ҦN����",
									"�έp��Ǭ�s�ҦN����",
									"��X��|�]�N����",
									"�ǳN���ʤ��ߦN����",
									"��������s�ҦN����",
									"��T��Ǭ�s�ҦN����",
									"�ͪ��ƾǬ�s�ҦN����",
									"�ūǦN����",
									"���l�ͪ���s�ҦN����",
									"�ͪ���Ǭ�Ǭ�s�ҦN����",
									"�ͪ��h�˩ʬ�s���ߦN����",
									"����|���]���X��N����",
									"��a����ʪ����ߦN����",
									"�H����|��Ǭ�s���ߦN����",
									"��T��޳зs��s���ߦN����",
									"�ƾǬ�s�ҦN����",
									"�x�W�v��s�ҦN����",
									"��]���s���ߦN����",
									"�Ӫ��[�L�ͪ��Ǭ�s�ҦN����",
									"��F�j�ӭp�⤤�ߦN����",
									"�l�����w�֧Q���N����",
									"���z��s�ҦN����",
									"�H����|����]�N����"};
	
	/**
	 * all the latitude, longitude pair, plus their corresponding .dae models and .png textures
	 * TODO load these setting from a web server or a local file...
	 */
	
//	private final Double[] rota = {170.0, //���ھǬ�s��
//								   -10.0, //�g�٬�s��
//								   -10.0, //�Ŵ��~�Ϯ��]
//								   190.0, //���ά�Ǭ�s����
//								   170.0, //�J�A�����]
//								   -10.0, //�x�W�ҥj�]
//								   170.0, //���v�媫���C�]
//								   -10.0, //��N�v��s��
//								   170.0, //���v�y����s��
//								   170.0, //�ڬ���s��
//								    80.0, //��v���ɮ��]
//								   -10.0, //���n���N�]
//								     0.0, //�������]
//								   170.0, //�a�y��Ǭ�s��
//								     0.0, //�έp��Ǭ�s��
//								   160.0, //��X��|�]
//								   100.0, //�ǳN���ʤ���
//								   100.0, //��������s��
//								   190.0, //��T��Ǭ�s��
//								    10.0, //�ͪ��ƾǬ�s��
//								   -70.0, //�ū�
//								    10.0, //���l�ͪ���s��
//								    10.0, //�ͪ���Ǭ�Ǭ�s��
//								    10.0, //�ͪ��h�˩ʬ�s����
//								     0.0, //����|���]���X��
//								     0.0, //��a����ʪ�����
//								   190.0, //�H����|��Ǭ�s����
//								    10.0, //��T��޳зs��s����
//								   190.0, //�ƾǬ�s��
//								    90.0, //�x�W�v��s��
//								   100.0, //��]���s����
//								   100.0, //�Ӫ��[�L�ͪ��Ǭ�s��
//								    10.0, //��F�j�ӭp�⤤��
//								   100.0, //�l�����w�֧Q��
//								   200.0, //���z��s��
//								    90.0}; //�H����|����]
	
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
	private final Double[] LON = {  121.616921,	//���ھǬ�s��
									121.617217,	//�g�٬�s��
									121.616698,	//�Ŵ��~�Ϯ��]
									121.617184,	//���ά�Ǭ�s����
									121.616289,	//�J�A�����]
									121.616252,	//�x�W�ҥj�]
									121.616208,	//���v�媫���C�]
									121.615664,	//��N�v��s��
									121.615799,	//���v�y����s��
									121.615150,	//�ڬ���s��
									121.615099,	//��v���ɮ��]
									121.615054,	//���n���N�]
									121.614102,	//�������]
									121.614057,	//�a�y��Ǭ�s��
									121.613634,	//�έp��Ǭ�s��
									121.613349,	//��X��|�]
									121.612619,	//�ǳN���ʤ���
									121.612200,	//��������s��
									121.614695,	//��T��Ǭ�s��
									121.613056,	//�ͪ��ƾǬ�s��
									121.615928,	//�ū�
									121.613836,	//���l�ͪ���s��
									121.614531,	//�ͪ���Ǭ�Ǭ�s��
									121.613827,	//�ͪ��h�˩ʬ�s����
									121.613603,	//����|���]���X��
									121.613121,	//��a����ʪ�����
									121.615175,	//�H����|��Ǭ�s����
									121.615460,	//��T��޳зs��s����
									121.615699,	//�ƾǬ�s��
									121.611249,	//�x�W�v��s��
									121.614303,	//��]���s����
									121.615186,	//�Ӫ��[�L�ͪ��Ǭ�s��
									121.616170,	//��F�j�ӭp�⤤��
									121.616514,	//�l�����w�֧Q��
									121.616707,	//���z��s��
									121.611445	//�H����|����]
								};
}
