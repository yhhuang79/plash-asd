package tw.edu.sinica.ants.plash.asd;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Facebook {

    public static void postMessage(String message){
    	// 宣告網址字串
    	String uriAPI = "http://plash2.iis.sinica.edu.tw/post.php?message=" + message; 
    	//建立HTTP Get連線
    	HttpGet httpRequest = new HttpGet(uriAPI); 
    	try{ 
    		//發出HTTP request
    		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); 
        }catch (ClientProtocolException e){  
           e.printStackTrace(); 
        }catch (IOException e){  
          e.printStackTrace(); 
        }catch (Exception e){  
           e.printStackTrace();  
        }
    }	
	
    public static void uploadPicture(String path, String uploadFile)
    {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      try
      {
    	URL url =new URL("http://plash2.iis.sinica.edu.tw/upload.php");
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        // 允許Input、Output，不使用Cache
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        // 設定傳送的method=POST
        con.setRequestMethod("POST");
        // setRequestProperty
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        // 設定DataOutputStream
        DataOutputStream ds = 
          new DataOutputStream(con.getOutputStream());
        
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"submit\";value=\"save\"" + end);
        ds.writeBytes(end); 

        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"title\"   title" + end);
        ds.writeBytes(end);        
        
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"photofile\";filename=\"" +
                      path + uploadFile +"\"" + end);
        ds.writeBytes(end); 

        // 取得檔案的FileInputStream
        FileInputStream fStream = new FileInputStream(path+uploadFile);
        // 設定每次寫入1024bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        // 從檔案讀取資料至緩衝區 
        while((length = fStream.read(buffer)) != -1)
        {
          // 將資料寫入DataOutputStream中
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        // close streams
        fStream.close();
        ds.flush();
        
        // 取得Response內容 
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        // 關閉DataOutputStream
        ds.close();
      }
      catch(Exception e)
      {
      }
    }
}
