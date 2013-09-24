package massimocamaggi.download;

import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadTask extends AsyncTask<HttpURLConnection, Integer, String>
{
	static final int BUFFER_SIZE = 1024;

	HttpURLConnection connection;
	boolean connected = false;
	String file_path = null; // set by DownloadActivity
	int total_size = 0, downloaded_size = 0, num_of_bytes_read = 0;
	Timer timer;

	public String doInBackground(HttpURLConnection... params)
	{
		connection = params[0];
		timer = new Timer(true);
		try
		{
			connection.connect();
			connected = true;
			total_size = connection.getContentLength();

			BufferedInputStream input_stream = new BufferedInputStream(
				connection.getInputStream()
			);
			BufferedOutputStream output_stream = new BufferedOutputStream(
				new FileOutputStream(file_path)
			);
			byte[] buffer = new byte[BUFFER_SIZE];

			timer.schedule(
				new TimerTask() {
					public void run()
					{
						publishProgress(
							total_size,
							downloaded_size
						);
					}
				},
				0,
				1000
			); // every second

			do
			{
				num_of_bytes_read = input_stream.read(buffer, 0, BUFFER_SIZE);
				if(num_of_bytes_read >= 0)
				{
					output_stream.write(buffer, 0, num_of_bytes_read);
					downloaded_size += num_of_bytes_read;
				}
			}
			while(num_of_bytes_read >= 0);

			input_stream.close();
			output_stream.flush();
			output_stream.close();
			return null; // ok
		}
		catch(Exception e)
		{
			timer.cancel();
			return e.getMessage();
		}
	}

	public void onProgressUpdate(Integer... values) // redefined in DownloadActivity
	{
	}

	public void onPostExecute(String result) // redefined in DownloadActivity
	{
	}

	public void onCancelled()
	{
		if(connected)
		{
			timer.cancel();
			connection.disconnect();
		}
	}
}
