package tw.edu.sinica.ants.plash.asd;

import commands.ui.CommandShowToast;

import geo.GeoObj;
import gl.GLCamera;
import gl.GLRenderer;
import gl.scenegraph.MeshComponent;
import util.EfficientList;
import util.Vec;
import worldData.RenderableEntity;
import worldData.World;
import actions.GDXConnection;
import actions.ModelLoader;
import android.app.Activity;
import android.location.Location;
import android.util.Log;

/**
 * To handle the event of loading and deleting a model from an obj
 * Will be called by the source code, from DroidAR
 * This class only exist to avoid excessive editing of the source code
 * TODO Should design a better workflow to handle this special event
 * 
 * @author CSZU
 *
 */
public class ModelHandler {
	
	private World world;
	private GLCamera camera;
	private GLRenderer renderer;
	private Activity activity;
	
	private Double[] LAT;
	private Double[] LON;
	private Double[] rota;
	
	private statusList mSTATUS;
	
	private EfficientList<RenderableEntity> items;
	
	private final String genericPrefix = "asd_ar_";
	private final String nameModelSuffix = "_name.dae";
	private final String nameTextureSuffix = "_nametexture.png";
	private final String boardModelSuffix = "_board.dae";
	private final String boardTextureSuffix = "_boardtexture.png";
	
	public ModelHandler(World world, GLCamera camera, GLRenderer renderer, Activity activity, Double[] LAT, Double[] LON, Double[] rota){
		this.world = world;
		this.camera = camera;
		this.renderer = renderer;
		this.activity = activity;
		
		this.LAT = LAT;
		this.LON = LON;
		this.rota = rota;
		
		mSTATUS = new statusList(LAT.length);
		
		items = world.getAllItems();
		GDXConnection.init(activity, renderer);
	}
	
	public void deleteModel(){
		for(int i = 0; i < mSTATUS.length; i++){ //go through the list
			//if name is not in range, but exist previously, delete its model
			if(mSTATUS.nameExist[i] && !mSTATUS.nameInRange[i]){
				GeoObj o = (GeoObj) items.get(i);
				o.getGraphicsComponent().clearChildren();
			}
			//same treatment for boards
			if(mSTATUS.boardExist[i] && !mSTATUS.boardInRange[i]){
				GeoObj o = (GeoObj) items.get(i + mSTATUS.length);
				o.getGraphicsComponent().clearChildren();
			}
		}
	}
	
	public void loadModel(){
		for(int i = 0; i < mSTATUS.length; i++){
			String modelName = "";
			String textureName = "";
			//if name is not in range, but exist previously, delete its model
Log.e("booleans", "nameinrange:" + mSTATUS.nameInRange[i] + " ,exist:" + mSTATUS.nameExist[i]);
			if(mSTATUS.nameInRange[i] && !mSTATUS.nameExist[i]){
				
				final int j = i; //for referencing in the modelLoader method
				
				if(i < 8){ //generate the filenames for modelLoader
					modelName = genericPrefix+0+(i+2)+nameModelSuffix;
					textureName = genericPrefix+0+(i+2)+nameTextureSuffix;
				}else{
					modelName = genericPrefix+(i+2)+nameModelSuffix;
					textureName = genericPrefix+(i+2)+nameTextureSuffix;
				}
				
				try{
					new ModelLoader(renderer, modelName, textureName) {
						@Override
						public void modelLoaded(MeshComponent gdxMesh) {
							GeoObj o = (GeoObj) items.get(j);
							o.setComp(gdxMesh);
							o.setMyPosition(new Vec(LON[j].floatValue(),LAT[j].floatValue(),100f));
							o.getGraphicsComponent().scaleEqual(1.0f);
							o.getGraphicsComponent().setRotation(new Vec(90.0f, 0f, rota[j].floatValue()));
Log.e("load", "id:" + j);
						}
					};
				}catch(OutOfMemoryError e){
					e.printStackTrace();
					CommandShowToast.show(activity, activity.getResources().getString(R.string.outOfMemory));
				}
			}
			
Log.e("booleans", "boardinrange:" + mSTATUS.boardInRange[i] + " ,exist:" + mSTATUS.boardExist[i]);
			//same treatment for boards
			if(mSTATUS.boardInRange[i] && !mSTATUS.boardExist[i]){
				final int ii = i;
				final int j = i + mSTATUS.length; //for referencing in the modelLoader method
				
				if(i < 8){ //generate the filenames for modelLoader
					modelName = genericPrefix+0+(i+2)+nameModelSuffix;
					textureName = genericPrefix+0+(i+2)+nameTextureSuffix;
				}else{
					modelName = genericPrefix+(i+2)+nameModelSuffix;
					textureName = genericPrefix+(i+2)+nameTextureSuffix;
				}
				
				try{
					new ModelLoader(renderer, modelName, textureName) {
						@Override
						public void modelLoaded(MeshComponent gdxMesh) {
							GeoObj o = (GeoObj) items.get(j);
							o.setComp(gdxMesh);
							o.setMyPosition(new Vec(LON[ii].floatValue(),LAT[ii].floatValue(),0f));
							o.getGraphicsComponent().scaleEqual(1.0f);
							o.getGraphicsComponent().setRotation(new Vec(90.0f, 0f, rota[ii].floatValue()));
Log.e("load", "id:" + j);
						}
					};
				}catch(OutOfMemoryError e){
					e.printStackTrace();
					CommandShowToast.show(activity, activity.getResources().getString(R.string.outOfMemory));
				}
			}
		}
	}
	
	public void calcDistance(final Location location) {
		final double lat1 = location.getLatitude();
		final double lon1 = location.getLongitude();
		
		for(int i = 0; i < LAT.length; i++){
			//go through every point the calculate the distance
			mSTATUS.distance[i] = GeoObj.distFrom(lat1, lon1, LAT[i], LON[i]);
			
Log.e("mSTATUS.distance", "id:" + i + " dist:" + mSTATUS.distance[i]);

			//check the distance with name range limit and update the status list
			if(mSTATUS.distance[i] > mSTATUS.nameRange){
				mSTATUS.nameInRange[i] = false;
			}else{
				mSTATUS.nameInRange[i] = true;
			}
			//check the distance with board range limit and update the status list
			if(mSTATUS.distance[i] > mSTATUS.boardRange){
				mSTATUS.boardInRange[i] = false;
			}else{
				mSTATUS.boardInRange[i] = true;
			}
		}
		//now the checking is all done, delete or load models if status changed
		//delete models first, free memory before loading any more models
		deleteModel();
		loadModel();
	}
	
	/**
	 * to keep track whether a board or a name is in range and whether they already existed or not
	 * @author CSZU
	 *
	 */
	class statusList{
		final double nameRange = 300.0; //in meters
		final double boardRange = 50.0; //in meters
		
		final int length; //length of the list
		
		boolean[] nameExist;
		boolean[] boardExist;
		boolean[] nameInRange;
		boolean[] boardInRange;
		
		Double[] distance;
		
		public statusList(final int size){
			this.nameExist = new boolean[size];
			this.boardExist = new boolean[size];
			this.nameInRange = new boolean[size];
			this.boardInRange = new boolean[size];
			this.distance = new Double[size];
			this.length = size;
		}
	}
}
