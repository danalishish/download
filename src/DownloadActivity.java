package massimocamaggi.download;

import android.os.Bundle;
import android.widget.ListView;
import android.view.View;
import android.content.Intent;
import java.net.URL;
import java.net.HttpURLConnection;
import android.text.format.Formatter;
import android.widget.Toast;
import android.webkit.MimeTypeMap;
import android.net.Uri;
import java.io.File;

public class DownloadActivity extends MyListActivity
{
	static final String INTENT_EXTRA_PATH = "path";

	DownloadTask download_task;
	int seconds = 0,
		previous_downloaded_size = 0; // for calculating speed
	boolean download_finished = false;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		download_task = new DownloadTask()
		{
			public void onPreExecute()
			{
				addListItem(getString(R.string.total_size), "");
				addListItem(getString(R.string.downloaded_size), "");
				addListItem(getString(R.string.downloaded_perc), "");
				addListItem(getString(R.string.speed), "");
				simple_adapter.notifyDataSetChanged();
			}

			public void onProgressUpdate(Integer... values)
			{
				updateValues(values);
				if(download_finished) downloadFinished();
			}

			public void onPostExecute(String result)
			{
				if(result == null)
					download_finished = true;
				else
				{
					addListItem(
						getString(R.string.error),
						result
					);
					simple_adapter.notifyDataSetChanged();
				}
			}
		};
		Intent intent = getIntent();
		download_task.file_path = intent.getStringExtra(INTENT_EXTRA_PATH);
		try
		{
			download_task.execute( (HttpURLConnection)
				(new URL( intent.getDataString() )
					.openConnection()
				)
			);
		}
		catch(Exception e)
		{
			addListItem(getString(R.string.error), e.getMessage());
			simple_adapter.notifyDataSetChanged();
		}
	}

	public void onPause()
	{
		super.onPause();
		if(isFinishing() && !download_finished)
		{
			download_task.cancel(true);
			Toast.makeText(
				this,
				R.string.download_cancelled,
				Toast.LENGTH_SHORT
			).show();
		}
	}

	public void updateValues(Integer... values)
	{
		if(seconds == 0)
		{
			if(values[0] < 0)
				list.get(0).put(TEXT2, getString(R.string.unknown));
			else
				list.get(0).put(TEXT2, Formatter.formatShortFileSize(this, values[0]));
		}

		list.get(1).put(TEXT2, Formatter.formatShortFileSize(this, values[1]));

		if(values[0] < 0)
		{
			if(seconds == 0)
				list.get(2).put(TEXT2, getString(R.string.unknown));
		}
		else if(values[0] > 0)
		{
			int percentage = values[1] * 100 / values[0];
			list.get(2).put(TEXT2, String.format("%d%%", percentage));
		}

		int bytes_per_second = values[1] - previous_downloaded_size;
		previous_downloaded_size = values[1];
		list.get(3).put(TEXT2, Formatter.formatShortFileSize(this, bytes_per_second) + "/s");

		simple_adapter.notifyDataSetChanged();
	}

	public void downloadFinished()
	{
		download_task.timer.cancel();
		addListItem(
			getString(R.string.download_finished),
			getString(R.string.touch_to_open)
		);
	}

	public void onListItemClick(ListView l, View v, int position, long id)
	{
		if(position == 4 && download_finished)
		{
			try
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File f = new File(download_task.file_path);
				Uri file_uri = Uri.fromFile(f);
				String mime_type = MimeTypeMap
					.getSingleton()
					.getMimeTypeFromExtension(
						MimeTypeMap.getFileExtensionFromUrl(f.toURL().toString())
					);
				if(mime_type != null)
					intent.setDataAndType(file_uri, mime_type);
				else
					intent.setData(file_uri);

				startActivity(intent);
			}
			catch(Exception e)
			{
				Toast.makeText(
					this,
					R.string.no_app_can_open,
					Toast.LENGTH_SHORT
				).show();
			}
		}
	}
}
