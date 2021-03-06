package tw.edu.sinica.ants.plash.asd; 

import android.graphics.Canvas; 
import android.graphics.Color;
import android.graphics.Paint; 
import android.graphics.Point; 
import android.graphics.RectF; 
import com.google.android.maps.GeoPoint; 
import com.google.android.maps.MapView; 
import com.google.android.maps.Overlay; 
import com.google.android.maps.Projection; 

public class MapOverLay extends Overlay 
{ 
  private GeoPoint gp1;
  private GeoPoint gp2;
  private int mRadius=6;
  private int mode=0;
     
  /* 建構子，傳入起點與終點的GeoPoint與mode */ 
  public MapOverLay(GeoPoint gp1,GeoPoint gp2,int mode) 
  { 
    this.gp1 = gp1; 
    this.gp2 = gp2;
    this.mode = mode;
  } 
  
  @Override 
  public boolean draw 
  (Canvas canvas, MapView mapView, boolean shadow, long when) 
  { 
    Projection projection = mapView.getProjection(); 
    if (shadow == false) 
    {      
      /* 設定筆刷 */ 
      Paint paint = new Paint(); 
      paint.setAntiAlias(true); 
      paint.setColor(Color.BLACK);
      
      Point point = new Point(); 
      projection.toPixels(gp1, point);
      /* mode=1：建立起點 */
      if(mode==1)
      {
        /* 定義RectF物件 */
        RectF oval=new RectF(point.x - mRadius, point.y - mRadius,  
                             point.x + mRadius, point.y + mRadius); 
        /* 繪製起點的圓形 */ 
        canvas.drawOval(oval, paint); 
      }
      /* mode=2：畫路線 */
      else if(mode==2)
      {
        Point point2 = new Point(); 
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
        /* 畫線 */ 
        canvas.drawLine(point.x, point.y, point2.x,point2.y, paint);
      }
      /* mode=3：建立終點 */
      else if(mode==3)
      {
        /* 避免誤差，先畫最後一段的路線 */
        Point point2 = new Point(); 
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
        canvas.drawLine(point.x, point.y, point2.x,point2.y, paint);
        
        /* 定義RectF物件 */ 
        RectF oval=new RectF(point2.x - mRadius,point2.y - mRadius,  
                             point2.x + mRadius,point2.y + mRadius); 
        /* 繪製終點的圓形 */
        paint.setAlpha(255);
        canvas.drawOval(oval, paint);
      }
    } 
    return super.draw(canvas, mapView, shadow, when); 
  } 
}