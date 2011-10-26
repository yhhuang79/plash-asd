package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * PLASHConnectionManager is the helper used to facilitate the connection process from the client to the Mule ESB based PLASH server. <br>
 * This class is specifically implemented for Mule ESB based PLASH server.  
 * Example usage:
 * 
 * @author Yu-Hsiang, Danny, Yi-Chun Teng
 *
 */
public class PLASHConnectionManager {
	

	/**
	 * Used by MULE ESB server
	 * Do NOT modify this
	 */
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
	        public boolean verify(String hostname, SSLSession session) {
	                return true;
	        }
	};
	
	//Danny 4/23/2011
    //Mule Structure
	//DO NOT MODIFY THIS
    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }
        } };
        try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection
                                .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
                e.printStackTrace();
        }

    }
	
    //Danny: still working on chekc network available function 7/1
//    public static boolean isNetworkAvailable(final Handler handler, final int timeout) {
//
//        // ask for message '0' (not connected) or '1' (connected) on 'handler'
//        // the answer must be send before before within the 'timeout' (in milliseconds)
//
//        new Thread() {
//
//            private boolean responded = false;
//
//            @Override
//            public void run() {
//
//                // set 'responded' to TRUE if is able to connect with google mobile (responds fast)
//
//                new Thread() {
//
//                    @Override
//                    public void run() {
//                        HttpGet requestForTest = new HttpGet("http://m.google.com");
//                        try {
//                            new DefaultHttpClient().execute(requestForTest); // can last...
//                            responded = true;
//                        } catch (Exception e) {}
//                    }
//
//                }.start();
//
//                try {
//                    int waited = 0;
//                    while(!responded && (waited < timeout)) {
//                        sleep(100);
//                        if(!responded ) { 
//                            waited += 100;
//                        }
//                    }
//                } 
//                catch(InterruptedException e) {} // do nothing 
//                finally { 
//                    if (!responded) { handler.sendEmptyMessage(0); } 
//                    else { handler.sendEmptyMessage(1); }
//                }
//
//            }
//
//        }.start();
//        return responded;
//        
//    }
    
    public static boolean isNetworkAvailable() {
    	boolean responded = false;
    	Log.d("Danny: respond @SetConnection.isNetworkAvailable calling......", String.valueOf(responded));
    	HttpGet requestForTest = new HttpGet("http://m.google.com");
    	//## 07/20/2011 chris - try port 80 on our server 
    	//HttpGet requestForTest = new HttpGet("http://plash.iis.sinica.edu.tw/");
    	try {
    		new DefaultHttpClient().execute(requestForTest); // can last...
            responded = true;
            Log.d("Danny: respond @SetConnection.isNetworkAvailable = ", String.valueOf(responded));
        } catch (Exception e) {}
     	return responded;
    }
    
    Handler h = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what != 1) { // code if not connected

            } else { // code if connected

            }

        }
    };


    
    //------------------------------------------------------------//
    //Danny:	Use Yu-Hsiang's HttpClient method
    //Date:		2011/06/23
    public static HttpClient getHttpClient() { 
		try { 
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
			trustStore.load(null, null); 
			SSLSocketFactory sf = new AndroidSSLSocketFactory(trustStore); 
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
			HttpParams params = new BasicHttpParams();
			
			//set connection timeout
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
			//set socket timeout
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(params, timeoutSocket);
			
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 
			SchemeRegistry registry = new SchemeRegistry(); 
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
			registry.register(new Scheme("https", sf, 443)); 
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 
			return new DefaultHttpClient(ccm, params); 
		} catch (Exception e) { 
			return new DefaultHttpClient(); 
		} 
	} 
    //------------------------------------------------------------//
    
    
    
    /**
     * This method helps client obtains data by using secure HTTP get mechanism. <br>
     * If the query result is null, this method will retry the query. <br>
     * The maximum number of retries is 5.	 <br>
     * @author Yu-Hsiang, Yi-Chun Teng
     * @param url The PLASH mule server query string. The format is as follows: <br>
     * @throws NullDataException The server returns empty query result after 5 tries
     * @throws IllegalArgumentException The supplied URL string is null
     * 			 
     */
    public static JSONObject httpsGet(String url)throws NullDataException, IllegalArgumentException{
    	
   
    	JSONObject j = null;
   
    	
    	if (url == null){
    		throw new IllegalArgumentException("URL string cannot be absent");
       	}//end if
    	
    	for (int retry = 0; (retry < 5) && (j == null) ; retry ++ ) {
			try {
				HttpClient client = getHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(url));
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
    			
    			String readInputLine = null;
    			readInputLine = in.readLine();
    			
    			
    			
    			Log.d("url@SetConnection = ", url.toString());


    			j = new JSONObject(new JSONTokener(readInputLine));
    			
    			Log.d("end@SetConnection = ", "");
    			in.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//try catch
    	}//rof
    	if (j == null) {
    		throw new NullDataException("Null query returned from server");
    	} else {
    		return j;
    	} //fi
    }//end method

    public static void batchUpload(String filepath){

			try {
				String url = "https://plash.iis.sinica.edu.tw:12348/BatchUploadTrajectory";
				
				HttpClient client = getHttpClient();
				HttpPost request = new HttpPost(url);
				BasicHttpEntity entity = new BasicHttpEntity();
				entity.setContent(new FileInputStream(filepath));
				request.setEntity(entity);
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			    String line;
			    while ((line = in.readLine()) != null) {
			    	Log.e("IT RETURNS:",line);
			    }
			    in.close();
			    client.getConnectionManager().shutdown();
    		} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
    	//}//end if
    }
}
