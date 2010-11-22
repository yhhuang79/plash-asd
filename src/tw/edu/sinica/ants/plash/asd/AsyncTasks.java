package tw.edu.sinica.ants.plash.asd;

import android.app.*;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.*;

/**
 * This class manages activity switching 
 * To use this class, instantiate an object by feeding in <br>
 * 1. the current context (activity) and 2. the type of job that you want to perform <br> 
 * Static fields are provided to indicate type of tasks. Please see field declaration for more info. 
 */
public class AsyncTasks extends AsyncTask<Object, Integer, Integer> {

	/**
	 * The caller. This should be Activity or class that inherit Activity
	 */
	private Context callerContext; 
	/**
	 * Type of task, see static public variable declaration
	 */
	private int taskType;
	/**
	 * Progress dialog that displays progress info
	 */
	private ProgressDialog progressDlg;
      
	  
	/**
	 * Supply this number to the constructor to indicate that you want to load album view activity 
	 */
	public static final int load_album = 0;
	/**
	 * Supply this number to the constructor to indicate that you want to load camera view activity 
	 */	
	public static final int load_camera = 1;
	/**
	 * Supply this number to the constructor to indicate that you want to load map view activity 
	 */	
	public static final int load_map = 2;
	/**
	 * Supply this number to the constructor to indicate that you want to load map & it is called from opening scene
	 */
	public static final int load_map_from_opening = 10;
	
	/**
	 * Constructor
	 * @param callerContext The activity that instantiates this asynchronous task.
	 * @param task Indicates which task to perform. Please see static fields for more information.
	 */
	public AsyncTasks(Context callerContext, int task) {
    	  this.callerContext = callerContext;
    	  this.taskType = task; //what job to perform?
	}//end constructor /*/
      
	  @Override
	  protected void onPreExecute() {

		  progressDlg = new ProgressDialog(callerContext);
		  progressDlg.setCancelable(false);
		  
		  switch (taskType) {
		  case load_map_from_opening:
			  progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  progressDlg.setMessage("Loading game...");
			  break;
		  	case load_camera:
		  	case load_album:
		  	case load_map:
			  progressDlg.setMessage("Preparing to load...");			  
			  progressDlg.setIndeterminate(true);
			  break;
		  }//end switch

		  progressDlg.show();		  			

	  }//end method
	  
	  @Override
	  protected void onProgressUpdate(Integer... progress) {		  
		  /*
		   * This if just tests progress bar, nothing useful
		   */
		  if (this.taskType == load_map_from_opening) { 
			  for (progress[0] = 0; progress[0]<=100 ; progress[0]++ ) {
				  progressDlg.setProgress(progress[0]);
				  try {
					  Thread.sleep(5); //more useful work can be done
				  } catch (InterruptedException e) {
					  return;
				  }//end try catch				  
			  }//end for */
		  	return;
		  }//end if
		  
		  switch (progress[0]) {
		  case 0:
			  progressDlg.setMessage("Preparing for network connection...");	
			  break;
		  case 1:
			  progressDlg.setMessage("Connecting...");		  
			  break;
		  case 2:
			  progressDlg.setMessage("Opening map...");
			  break;			  
		  case 10:
			  progressDlg.setMessage("Preparing for camera hardware...");
			  break;
		  case 11:
			  progressDlg.setMessage("Camera ready!");
			  break;

		  case 20:
			  progressDlg.setMessage("Loading completed!");

			  break;

			  
		  }//end switch */
		  

		  

		  
	  }//end method

	  @Override
	  protected void onPostExecute(Integer result) {
		  switch (taskType) {
		  	case load_camera:
		  		break;
		  	case load_map:		  		
		  	case load_album:
		  	case load_map_from_opening:
				((Activity)callerContext).finish();
		  		break;
		  	
		  }//end switch
		  progressDlg.dismiss();
	  }//end method
	  
	  @Override
	  protected Integer doInBackground(Object... params) {
		  Log.i("doInBG begins at", ": " + android.os.SystemClock.uptimeMillis() );
	      Intent intent = new Intent();
		  switch (taskType) {
		  	case load_map:
		  		publishProgress(2);
		  		intent.setClass(callerContext, map_view.class);	        	 
		  		callerContext.startActivity(intent);	
		  		publishProgress(10);
		  		break;
		  	case load_camera:
		  		publishProgress(10);
	        	intent.setClass(callerContext, camera_view.class);
	        	        	 
	        	intent.putExtras((Bundle)params[0]);	        	
	        	 ((Activity) callerContext).startActivityForResult(intent, 0);
	        	 publishProgress(11);  
		  		return 1;
		  	case load_album:
				publishProgress(0);
		      	try {
		      		Thread.sleep(1000);          		 
		        } catch (InterruptedException e) {         		 
		        	return 1;
		        }//end try catch			  
			 

				publishProgress(1);
		       
		      	intent.setClass(callerContext, album_view.class);
		      	callerContext.startActivity(intent);

		      	Log.i("doInBG at mid", ": " + android.os.SystemClock.uptimeMillis() );
		     	  
		      	publishProgress(20);
					try {
					Thread.sleep(1000);          		
				} catch (InterruptedException e) {				 
					return 1;
				}//end try catch 			  
		      	  
		      	Log.i("doInBG ends at", ": " + android.os.SystemClock.uptimeMillis() );
				return 1;//success	
		  	case load_map_from_opening:
				onProgressUpdate(0);		  
				intent.setClass(callerContext, map_view.class);      	  
				callerContext.startActivity(intent);
				return 1;//success	
		  }//end switch

		  return -1;//failed, not going through case-switch
	  }//end method

}//end class
