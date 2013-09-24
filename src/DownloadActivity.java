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
	int seconds = 0;
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
		if(position == 3 && download_finished)
		{
			try
			{
				int dot_pos = download_task.file_path.lastIndexOf('.');
				String file_extension = null;
				if(dot_pos != -1 && !download_task.file_path.endsWith("."))
					file_extension = download_task.file_path.substring(dot_pos + 1);
				String mime_type = MimeTypeMap
					.getSingleton()
					.getMimeTypeFromExtension(file_extension);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri file_uri = Uri.fromFile(new File(download_task.file_path));
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
