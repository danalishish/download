package massimocamaggi.download;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.view.View;
import android.widget.SimpleAdapter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class MyListActivity extends ListActivity
{
	static final String TEXT1 = "text1", TEXT2 = "text2";

	List<Map<String,String>> list;
	SimpleAdapter simple_adapter;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mylistactivity);
		list = new ArrayList<Map<String,String>>();
		simple_adapter = new SimpleAdapter(
			this,
			list,
			R.layout.listitem,
			new String[] { TEXT1, TEXT2 },
			new int[] { android.R.id.text1, android.R.id.text2 }
		);
		setListAdapter(simple_adapter);
	}

	public void addListItem(String text1, String text2)
	{
		Map<String,String> map = new HashMap<String,String>();
		map.put(TEXT1, text1);
		map.put(TEXT2, text2);
		list.add(map);
	}
}
