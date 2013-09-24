package massimocamaggi.download;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.content.SharedPreferences;
import java.net.URL;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends Activity
implements DialogInterface.OnClickListener
{
	static final String
		PREF_URL = "url",
		PREF_PATH = "path";

	EditText edittext_url, edittext_file;
	SharedPreferences preferences;
	boolean file_already_exists; // set by checkPath()
	Dialog overwrite_dialog;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mainactivity);
		edittext_url = (EditText)findViewById(R.id.mainactivity_url);
		edittext_file = (EditText)findViewById(R.id.mainactivity_file);

		preferences = getPreferences(MODE_PRIVATE);
		edittext_url.setText(preferences.getString(PREF_URL, null));
		edittext_file.setText(preferences.getString(PREF_PATH, null));
	}

	public void onPause()
	{
		super.onPause();
		if(isFinishing())
		{
			preferences
			.edit()
			.putString(PREF_URL, getEditTextString(edittext_url))
			.putString(PREF_PATH, getEditTextString(edittext_file))
			.commit();
		}
	}

	public String getEditTextString(EditText edittext)
	{
		return edittext.getText().toString().trim();
	}

	public boolean isStringEmpty(String s)
	{
		return (s == null) || (s.length() == 0);
	}

	public void buttonOnClick(View v)
	{
		String url_string;

		switch(v.getId())
		{
			case R.id.mainactivity_verify:
				url_string = checkUrl();
				if(url_string != null)
					startActivity(
						new Intent(
							null,
							Uri.parse(url_string),
							this,
							VerifyActivity.class
						)
					);
				break;
			case R.id.mainactivity_download:
				url_string = checkUrl();
				String file_path = checkPath();
				if(url_string != null && file_path != null)
				{
					if(file_already_exists)
						showDialog(R.string.file_already_exists);
					else
						startDownloadActivity(url_string, file_path);
				}
				break;
		}
	}

	public String showUrlError(int string_id)
	{
		edittext_url.setError(getString(string_id));
		return null;
	}

	public String checkUrl()
	{
		String url_string = getEditTextString(edittext_url);

		if(isStringEmpty(url_string))
			return showUrlError(R.string.error_missing_url);
		try
		{
			URL url = new URL(url_string);
			String url_protocol = url.getProtocol();
			if(url_protocol.equals("http") || url_protocol.equals("https"))
				return url.toString();
			else
				return showUrlError(R.string.error_bad_protocol);
		}
		catch(Exception e)
		{
			return showUrlError(R.string.error_malformed_url);
		}
	}

	public String showPathError(int string_id)
	{
		edittext_file.setError(getString(string_id));
		return null;
	}

	public String checkPath()
	{
		String file_path = getEditTextString(edittext_file);
		if(isStringEmpty(file_path)) return showPathError(R.string.error_missing_path);
		try
		{
			File f = new File(file_path);
			file_already_exists = f.exists();
			if(file_already_exists)
			{
				if(f.isFile())
				{
					if(f.canWrite()) return f.getAbsolutePath(); // ok
					else return showPathError(R.string.error_readonly_file);
				}
				else return showPathError(R.string.error_not_a_file);
			}
			else
			{
				if(f.createNewFile()) return f.getAbsolutePath(); // ok
				else return showPathError(R.string.error_cannot_create_file);
			}
		}
		catch(Exception e)
		{
			return showPathError(R.string.error_bad_path);
		}
	}

	public void startDownloadActivity(String url_string, String file_path)
	{
		startActivity(
			new Intent(
				null,
				Uri.parse(url_string),
				this,
				DownloadActivity.class
			)
			.putExtra(
				DownloadActivity.INTENT_EXTRA_PATH,
				file_path
			)
		);
	}

	public Dialog onCreateDialog(int id, Bundle args)
	{
		AlertDialog.Builder dialog_builder;

		switch(id)
		{
			case R.string.file_already_exists:
				dialog_builder = new AlertDialog.Builder(this);
				dialog_builder.setTitle(id);
				dialog_builder.setMessage(R.string.replace_its_content);
				dialog_builder.setPositiveButton(android.R.string.yes, this);
				dialog_builder.setNegativeButton(android.R.string.no, this);
				return dialog_builder.create();
			default:
				return null;
		}
	}

	public void onPrepareDialog(int id, Dialog dialog, Bundle args)
	{
		switch(id)
		{
			case R.string.file_already_exists:
				overwrite_dialog = dialog;
		}
	}

	public void onClick(DialogInterface dialog, int which)
	{
		if(dialog == overwrite_dialog)
		{
			if(which == DialogInterface.BUTTON_POSITIVE)
			{
				startDownloadActivity(
					getEditTextString(edittext_url),
					getEditTextString(edittext_file)
				);
			}
		}
	}
}
