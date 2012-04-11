package pro.dbro.bart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class RequestTask extends AsyncTask<String, String, String> {
	//allow data passing to calling activity
	String request;
	boolean updateUI;
	Activity caller;
	RequestTask(Activity caller, String request, boolean updateUI) {
		this.request = request;
        this.caller = caller;
        this.updateUI = updateUI;
    }
	@Override
	protected String doInBackground(String... uri) {
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
        	String url = uri[0];
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        	return "error";
        } catch (IOException e) {
        	Log.v("RequestTask", e.toString());
        	return "error";
            //TODO Handle problems..
        }
        return responseString;
	}
	
	@Override
    protected void onPostExecute(String result) {
		((TheActivity) caller).parseBart(result, request, updateUI);
        super.onPostExecute(result);
    }
}
