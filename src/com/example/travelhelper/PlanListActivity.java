package com.example.travelhelper;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.travelhelper.R;
import com.example.travelhelper.MainActivity.PlaceInfo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PlanListActivity extends ListActivity {

	public static final String EXTRA_LIST = "com.example.travelhelper.PlanListActivity.EXTRA_LIST";

	/** Called when the activity is first created. */
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	private SimpleAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		ArrayList<String[]> planlist = (ArrayList<String[]>) intent
				.getSerializableExtra(EXTRA_LIST);

		// 把資料加入ArrayList中
		for (int i = 0; i < planlist.size(); i++) {
			HashMap<String, String> item = new HashMap<String, String>();
			String plan[] = planlist.get(i);
			item.put("id", plan[0]);
			item.put("date", plan[1]);
			item.put("name", plan[2]);
			list.add(item);
		}

		// 新增SimpleAdapter
		adapter = new SimpleAdapter(this, list, R.layout.plan_list,
				new String[] { "date", "name" }, new int[] { R.id.textDate,
						R.id.textName });

		// ListActivity設定adapter
		setListAdapter(adapter);

		// 啟用按鍵過濾功能，這兩行資料都會進行過濾
		getListView().setTextFilterEnabled(true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Toast.makeText(this, "Clicked row " + position,
		// Toast.LENGTH_SHORT).show();
		String selected[] = {list.get(position).get("id"),
				list.get(position).get("date"),
				list.get(position).get("name")};
		Intent intent = new Intent();
		intent.putExtra(EXTRA_LIST, selected);
		setResult(RESULT_OK, intent);
		finish();
	}

}
