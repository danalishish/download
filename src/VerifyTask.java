package massimocamaggi.download;

import android.os.AsyncTask;
import java.net.HttpURLConnection;

public class VerifyTask extends AsyncTask<HttpURLConnection, Integer, String>
{
	HttpURLConnection connection;
	boolean connected = false;

	public String doInBackground(HttpURLConnection... params)
	{
		connection = params[0];
		try
		{
			connection.setRequestMethod("HEAD");
			connection.connect();
			connected = true;
			return null; // ok
		}
		catch(Exception e)
		{
			return e.getMessage();
		}
	}

	public void onPostExecute(String result) // redefined in VerifyActivity
	{
	}

	public void onCancelled()
	{
		if(connected) connection.disconnect();
	}
}
