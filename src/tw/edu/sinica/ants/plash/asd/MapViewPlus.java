package tw.edu.sinica.ants.plash.asd;

import android.graphics.*;

import com.google.android.maps.MapView;

public class MapViewPlus extends MapView {
    
	private Paint txtPaint = new Paint();
	private Matrix modelView = new Matrix(), rotM = new Matrix();
	
	public MapViewPlus(android.content.Context context, android.util.AttributeSet attrs) {
		super(context, attrs);
	}//end constructor
     
    public MapViewPlus(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }//end constructor

    public MapViewPlus(android.content.Context context, java.lang.String apiKey) {
    	super(context, apiKey);
    }//end constructor
    

	@Override
	protected void dispatchDraw(Canvas canvas) {
		/*
		modelView.postRotate(-1,300,130);
		canvas.concat(modelView);
		canvas.drawText("GPS :" + "aiaiai", 20, 225, txtPaint);
		canvas.drawRect(new RectF(0,0,300,200), txtPaint);		//*/
		
		super.dispatchDraw(canvas);
	}//end method
}//end class
