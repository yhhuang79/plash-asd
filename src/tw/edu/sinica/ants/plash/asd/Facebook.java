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
    	// �ŧi���}�r��
    	String uriAPI = "http://plash2.iis.sinica.edu.tw/post.php?message=" + message; 
    	//�إ�HTTP Get�s�u
    	HttpGet httpRequest = new HttpGet(uriAPI); 
    	try{ 
    		//�o�XHTTP request
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
        // ���\Input�BOutput�A���ϥ�Cache
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        // �]�w�ǰe��method=POST
        con.setRequestMethod("POST");
        // setRequestProperty
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        // �]�wDataOutputStream
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

        // ���o�ɮת�FileInputStream
        FileInputStream fStream = new FileInputStream(path+uploadFile);
        // �]�w�C���g�J1024bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        // �q�ɮ�Ū����Ʀܽw�İ� 
        while((length = fStream.read(buffer)) != -1)
        {
          // �N��Ƽg�JDataOutputStream��
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        // close streams
        fStream.close();
        ds.flush();
        
        // ���oResponse���e 
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        // ����DataOutputStream
        ds.close();
      }
      catch(Exception e)
      {
      }
    }
}
