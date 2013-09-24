package massimocamaggi.download;

import android.os.Bundle;
import android.net.Uri;
import java.net.URL;
import java.net.HttpURLConnection;

public class VerifyActivity extends MyListActivity
{
	VerifyTask verify_task;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		verify_task = new VerifyTask()
		{
			public void onPostExecute(String result)
			{
				if(result != null)
					addListItem(
						getString(R.string.error),
						result
					);
				else
				{
					try
					{
						addListItem(
							String.valueOf(connection.getResponseCode()),
							String.valueOf(connection.getResponseMessage())
						);
					}
					catch(Exception e)
					{
					}
					for(String k : connection.getHeaderFields().keySet())
					{
						addListItem(
							k,
							String.valueOf(connection.getHeaderField(k))
						);
					}
				}
				if(connected) connection.disconnect();
				simple_adapter.notifyDataSetChanged();
			}
		};
		try
		{
			verify_task.execute( (HttpURLConnection)
				(new URL( getIntent().getDataString() )
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
		if(isFinishing()) verify_task.cancel(true);
	}
}
