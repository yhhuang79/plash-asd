package tw.edu.sinica.ants.plash.asd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SetConnection {
	
	//Danny 4/23/2011
    //Mule Structure
	//DO NOT MODIFY THIS
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

//    private static boolean isNetworkAvailable() {
//    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//    	NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//    	return activeNetworkInfo != null;
//    }
//    
//    public static boolean hasActiveInternetConnection(Context context) {
//        if (isNetworkAvailable()) {
//            try {
//                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
//                urlc.setRequestProperty("User-Agent", "Test");
//                urlc.setRequestProperty("Connection", "close");
//                urlc.setConnectTimeout(1500); 
//                urlc.connect();
//                return (urlc.getResponseCode() == 200);
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error checking internet connection", e);
//            }
//        } else {
//            Log.d(LOG_TAG, "No network available!");
//        }
//        return false;
//    }
    
//    //------------------------------------------------------------//
//    //Danny:	Old version
//    //Date:		2011/06/23
//    public static JSONObject setHttps(URL url){
//    	
//    	//URL url = null;
//    	JSONObject j = null;
//    	
////    	try {
////    		url = new URL(location);
////    	}catch(MalformedURLException  e){ 
////        
////    	}
//    	
//    	if (url != null){
//    		//try{
//    		//----------------------------------------------//
//    		//Danny: 4/23/2011
//    		trustAllHosts();
//    			
//    		HttpsURLConnection urlConn;
//			try {
//				urlConn = (HttpsURLConnection) url.openConnection();
//
//    			urlConn.setHostnameVerifier(DO_NOT_VERIFY);
//    			
//    			//Get the connection into reader
//    			BufferedReader in = new BufferedReader(	new InputStreamReader(urlConn.getInputStream()));
//    			
//    			String readInputLine = null;
//    			readInputLine = in.readLine();
//    			
//    			Log.d("url@SetConnection = ", url.toString());
//    			//Make the reader be the JSONObject
//    			j = new JSONObject(new JSONTokener(readInputLine));
//    			
//    			Log.d("end@SetConnection = ", "");
//    			in.close();
//    			urlConn.disconnect();
//    			
////    			System.out.println(j.get("message"));
////    			String inputLine = null;
////    			inputLine = j.getString("message");
////    			
//    		} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}//end if
//
//    	return j;
//    }
//    
//    //------------------------------------------------------------//
    
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
    
    
    //------------------------------------------------------------//
    //Danny:	Use Yu-Hsiang's HttpClient method
    //Date:		2011/06/23
    public static JSONObject setHttps(String url){
    	
    	//URL url = null;
    	JSONObject j = null;
    	//BufferedReader in = null;
    	
    	//String input_uri = String.valueOf(url);
    	
    	
    	
    	if (url != null){
    		//try{
    		//----------------------------------------------//
    		//Danny: 4/23/2011
    		
    		//trustAllHosts();
    		
    		HttpsURLConnection urlConn;
			try {
				HttpClient client = getHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(url));
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				//urlConn = (HttpsURLConnection) url.openConnection();

    			//urlConn.setHostnameVerifier(DO_NOT_VERIFY);
    			
    			//Get the connection into reader
    			//BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
    			
    			String readInputLine = null;
    			readInputLine = in.readLine();
    			
    			Log.d("url@SetConnection = ", url.toString());
    			//Make the reader be the JSONObject
    			j = new JSONObject(new JSONTokener(readInputLine));
    			
    			Log.d("end@SetConnection = ", "");
    			in.close();
    			//urlConn.disconnect();
    			
//    			System.out.println(j.get("message"));
//    			String inputLine = null;
//    			inputLine = j.getString("message");
//    			
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}//end if

    	return j;
    }
    
  	//------------------------------------------------------------//
    

  //------------------------------------------------------------//
}
