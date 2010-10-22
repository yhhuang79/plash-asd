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
     
  /* �غc�l�A�ǤJ�_�I�P���I��GeoPoint�Pmode */ 
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
      /* �]�w���� */ 
      Paint paint = new Paint(); 
      paint.setAntiAlias(true); 
      paint.setColor(Color.BLACK);
      
      Point point = new Point(); 
      projection.toPixels(gp1, point);
      /* mode=1�G�إ߰_�I */
      if(mode==1)
      {
        /* �w�qRectF���� */
        RectF oval=new RectF(point.x - mRadius, point.y - mRadius,  
                             point.x + mRadius, point.y + mRadius); 
        /* ø�s�_�I����� */ 
        canvas.drawOval(oval, paint); 
      }
      /* mode=2�G�e���u */
      else if(mode==2)
      {
        Point point2 = new Point(); 
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
        /* �e�u */ 
        canvas.drawLine(point.x, point.y, point2.x,point2.y, paint);
      }
      /* mode=3�G�إ߲��I */
      else if(mode==3)
      {
        /* �קK�~�t�A���e�̫�@�q�����u */
        Point point2 = new Point(); 
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
        canvas.drawLine(point.x, point.y, point2.x,point2.y, paint);
        
        /* �w�qRectF���� */ 
        RectF oval=new RectF(point2.x - mRadius,point2.y - mRadius,  
                             point2.x + mRadius,point2.y + mRadius); 
        /* ø�s���I����� */
        paint.setAlpha(255);
        canvas.drawOval(oval, paint);
      }
    } 
    return super.draw(canvas, mapView, shadow, when); 
  } 
}