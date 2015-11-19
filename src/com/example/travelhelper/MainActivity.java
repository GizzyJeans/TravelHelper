package com.example.travelhelper;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.travelhelper.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, LocationListener {

	public static final String CUSTOM_INTENT = "com.example.googletraval.custom.intent.action.FINDPLACE";

	public class PlaceInfo {
		private String strPlace, strAddress, strLatitude, strLongitude;

		public PlaceInfo(String place, String address, String lat, String lng) {
			strPlace = place;
			strAddress = address;
			strLatitude = lat;
			strLongitude = lng;
		}
	}

	private LocationManager locationMgr;
	private boolean IsLocationOn = true;

	private ListView list;
	private MyAdapter adapter = null;

	private String m_srclat;
	private String m_srclng;

	private static String mCurrentPlanId;

	PlaceListDbHelper m_dbhelper = null;
	SQLiteDatabase m_db = null;

	private static ArrayList<PlaceInfo> mPlacesInfo = new ArrayList<PlaceInfo>();

	private ListView.OnItemClickListener mListClickListener = new ListView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> placelist, View arg1, int index,
				long id) {
			// TODO Auto-generated method stub;
			PlaceInfo selected = (PlaceInfo) placelist.getItemAtPosition(index);
			ShowMap(selected.strLatitude, selected.strLongitude);

		}

	};

	private ListView.OnItemLongClickListener mListLongClickListener = new ListView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int index, long id) {
			// TODO Auto-generated method stub
			final int pos = index;
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.delete_place)
					.setMessage(R.string.are_you_sure)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									RemovePlaceFromDb(mPlacesInfo.get(pos));
									mPlacesInfo.remove(pos);
									list.setAdapter(adapter);
								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).show();

			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// SupportMapFragment fragment = (SupportMapFragment)
		// getSupportFragmentManager().findFragmentById(R.id.map);
		// mGoogleMap =
		// ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		findViews();
		adapter = new MyAdapter(this);
		list.setAdapter(adapter);

		m_dbhelper = new PlaceListDbHelper(MainActivity.this);

		this.locationMgr = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		handleIntent(getIntent());

	}

	@Override
	protected void onPause() {
		super.onPause();
		this.locationMgr.removeUpdates(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (true == IsLocationOn) {
			String provider = this.locationMgr.getBestProvider(new Criteria(),
					true);
			if (null != provider) {
				this.locationMgr
						.requestLocationUpdates(provider, 1000, 0, this);
				btn_gps.setText(R.string.gpsoff);
				IsLocationOn = true;
			}
		}

	}

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater myInflater;

		public MyAdapter(Context c) {
			myInflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPlacesInfo.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mPlacesInfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = myInflater.inflate(R.layout.place_list, null);

			// ImageView logo = (ImageView)
			// convertView.findViewById(R.id.imglogo);
			TextView place = (TextView) convertView
					.findViewById(R.id.textView1);
			TextView address = (TextView) convertView
					.findViewById(R.id.textView2);

			// logo.setImageResource(logos[position]);
			place.setText(mPlacesInfo.get(position).strPlace);
			address.setText(mPlacesInfo.get(position).strAddress);

			return convertView;
		}

	}

	private TextView view_lat;
	private TextView view_lng;
	private TextView view_date;
	private TextView view_name;
	private Button btn_gps;
	private Button btn_add;

	private void findViews() {
		// button_calc = (Button) findViewById(R.id.submit);

		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(mListClickListener);
		list.setOnItemLongClickListener(mListLongClickListener);
		view_lat = (TextView) findViewById(R.id.lat);
		view_lng = (TextView) findViewById(R.id.lng);
		view_date = (TextView) findViewById(R.id.plandate);
		view_name = (TextView) findViewById(R.id.planname);
		btn_gps = (Button) findViewById(R.id.btn_gps);
		btn_gps.setText(R.string.gpson);

		btn_add = (Button) findViewById(R.id.btn_add);
		if (mCurrentPlanId == null)
			btn_add.setEnabled(false);

	}

	private void handleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			doSearch(intent.getStringExtra(SearchManager.QUERY));
		} else if (intent.getAction().equals(CUSTOM_INTENT)) {
			getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY),
					intent.getStringExtra(SearchManager.QUERY));

		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void doSearch(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getSupportLoaderManager().restartLoader(0, data, this);
	}

	private void getPlace(String query, String place) {
		Bundle data = new Bundle();
		data.putString("query", query);
		data.putString("place", place);
		getSupportLoaderManager().restartLoader(1, data, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.action_loaddb:
			SelectPlan();
			break;

		case R.id.action_newplan:
			CreateNewPlan();
			break;

		case R.id.action_delplan:
			RemovePlanFromDb();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
		CursorLoader cLoader = null;
		if (arg0 == 0)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.SEARCH_URI, null, null,
					new String[] { query.getString("query") }, null);
		else if (arg0 == 1)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.DETAILS_URI, null, null,
					new String[] { query.getString("query"),
							query.getString("place") }, null);
		return cLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		if (c.moveToFirst()) {
			do {

				String address = c.getString(c.getColumnIndex("description"));
				String place = c.getString(c.getColumnIndex("place"));
				String lat = c.getString(c.getColumnIndex("lat"));
				String lng = c.getString(c.getColumnIndex("lng"));

				// m_detlat = lat;
				// m_detlng = lng;

				PlaceInfo placeinfo = new PlaceInfo(place, address, lat, lng);
				mPlacesInfo.add(placeinfo);
				AddPlaceToDb(placeinfo);
				// do what ever you want here
			} while (c.moveToNext());
		}

		list.setAdapter(adapter);

		// showLocations(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}

	public void TurnGpsStatus(View v) {
		if (true == IsLocationOn) {
			this.locationMgr.removeUpdates(this);
			btn_gps.setText(R.string.gpson);
			IsLocationOn = false;
		} else {
			String provider = this.locationMgr.getBestProvider(new Criteria(),
					true);
			if (null != provider) {
				this.locationMgr
						.requestLocationUpdates(provider, 1000, 0, this);
				btn_gps.setText(R.string.gpsoff);
				IsLocationOn = true;
			}

		}
	}

	public void AddPlace(View v) {
		onSearchRequested();
		// mPlaces.add("test");
		// list.setAdapter(adapter);
	}

	public void ShowMap(View v) {
		// TODO
	}

	static final int PICK_PLAN_REQUEST = 1;

	private void SelectPlan() {
		m_db = m_dbhelper.getReadableDatabase();

		try {

			String columns[] = { "_id", "date", "name" };

			ArrayList<String[]> list = new ArrayList<String[]>();

			// read data
			Cursor cursor = m_db.query("plan", columns, null, null, null, null,
					null);

			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex("_id"));
				String planinfo[] = { "_" + String.valueOf(id),
						cursor.getString(1), cursor.getString(2) };
				list.add(planinfo);

			}

			Intent intent = new Intent(this, PlanListActivity.class);
			intent.putExtra(PlanListActivity.EXTRA_LIST, list);
			startActivityForResult(intent, PICK_PLAN_REQUEST);

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == PICK_PLAN_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {

				String planinfo[] = (String[]) data
						.getSerializableExtra(PlanListActivity.EXTRA_LIST);
				mCurrentPlanId = planinfo[0];
				view_date.setText(planinfo[1]);
				view_name.setText(planinfo[2]);

				LoadDb(mCurrentPlanId);

			}
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
			case Activity.RESULT_OK:
				Toast.makeText(MainActivity.this, "Result:OK",
						Toast.LENGTH_LONG).show();
				break;
			case Activity.RESULT_CANCELED:
				Toast.makeText(MainActivity.this, "Result:Cancel",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}

		}
	};

	private void CreateNewPlan() {
		// TODO Auto-generated method stub
		Calendar calendar = Calendar.getInstance();

		final DatePicker datePicker = new DatePicker(this);
		datePicker.init(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), null);

		new AlertDialog.Builder(MainActivity.this)
				.setTitle("選擇日期")
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("Picker", datePicker.getYear() + " "
										+ (datePicker.getMonth() + 1) + " "
										+ datePicker.getDayOfMonth());

								final String plandate = datePicker.getYear()
										+ "/" + (datePicker.getMonth() + 1)
										+ "/" + datePicker.getDayOfMonth();

								AlertDialog.Builder nameDialog = new AlertDialog.Builder(
										MainActivity.this);
								nameDialog.setTitle("請輸入行程名稱");

								final EditText nameText = new EditText(
										MainActivity.this);
								// editText.setText(textOut.getText());
								nameDialog.setView(nameText);

								nameDialog.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											// do something when the button is
											// clicked
											public void onClick(
													DialogInterface arg0,
													int arg1) {

												mPlacesInfo.clear();
												final String planname = nameText
														.getText().toString();
												view_date.setText(plandate);
												view_name.setText(planname);
												// Add new plan to plan database
												AddPlanToDb(plandate, planname);

												btn_add.setEnabled(true);

											}
										});
								nameDialog.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											// do something when the button is
											// clicked
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												// ...
											}
										});
								nameDialog.show();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("Picker", "Cancelled!");
							}
						}).setView(datePicker).show();

	}

	private long AddPlanToDb(String plandate, String planname) {
		m_db = m_dbhelper.getWritableDatabase();
		long ret = 0;
		// create table
		try {
			// define SQL instruction
			String sql = "create table plan(_id integer primary key autoincrement, "
					+ "date text not null, " + "name text not null)";

			m_db.execSQL(sql);
		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		// add new plan data
		try {
			// start transaction
			m_db.beginTransaction();

			// set data
			ContentValues val = new ContentValues();
			val.put("date", plandate);
			val.put("name", planname);

			// write data
			ret = m_db.insert("plan", null, val);

			mCurrentPlanId = "_" + String.valueOf(ret);

			// transaction commit
			m_db.setTransactionSuccessful();

			// transaction over
			m_db.endTransaction();

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		// create place table
		try {
			// define SQL instruction
			String sql = "create table " + mCurrentPlanId
					+ "(_id integer primary key autoincrement, "
					+ "name text not null, " + "address text not null, "
					+ "latitude text not null, " + "longitude text not null)";

			m_db.execSQL(sql);
		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();

		return ret;
	}

	private void RemovePlanFromDb() {
		m_db = m_dbhelper.getWritableDatabase();

		try {
			// set condition
			String condition_plan = null;
			if (mCurrentPlanId != null) {
				condition_plan = "date = '" + view_date.getText().toString()
						+ "' AND " + "name = '"
						+ view_name.getText().toString() + "'";
			}

			if (null != condition_plan) {
				// start transaction
				m_db.beginTransaction();

				// delete plan from plan list table
				m_db.delete("plan", condition_plan, null);

				// delete current plan table
				m_db.execSQL("DROP TABLE IF EXISTS " + mCurrentPlanId);

				// transaction commit
				m_db.setTransactionSuccessful();

				// transaction over
				m_db.endTransaction();

				mPlacesInfo.clear();
				list.setAdapter(adapter);

				view_date.setText(null);
				view_name.setText(null);

				mCurrentPlanId = null;
				btn_add.setEnabled(false);
			}

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();
	}

	private void LoadDb(String tableid) {
		m_db = m_dbhelper.getReadableDatabase();

		try {

			String columns[] = { "name", "address", "latitude", "longitude" };

			// read data
			Cursor cursor = m_db.query(tableid, columns, null, null, null,
					null, null);

			mPlacesInfo.clear();

			while (cursor.moveToNext()) {
				PlaceInfo placeInfo = new PlaceInfo(cursor.getString(0),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3));
				mPlacesInfo.add(placeInfo);
			}

			list.setAdapter(adapter);
			btn_add.setEnabled(true);

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();

	}

	private void RemovePlaceFromDb(PlaceInfo placeinfo) {
		m_db = m_dbhelper.getWritableDatabase();

		try {
			// set condition
			String condition = null;
			if (placeinfo.strPlace != null && !placeinfo.strPlace.equals("")) {
				condition = "name = '" + placeinfo.strPlace + "'";
			}

			// start transaction
			m_db.beginTransaction();

			// delete data
			m_db.delete(mCurrentPlanId, condition, null);

			// transaction commit
			m_db.setTransactionSuccessful();

			// transaction over
			m_db.endTransaction();

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();
	}

	private void AddPlaceToDb(PlaceInfo placeinfo) {
		m_db = m_dbhelper.getWritableDatabase();

		// create table
		try {
			// define SQL instruction
			String sql = "create table " + mCurrentPlanId
					+ "(_id integer primary key autoincrement, "
					+ "name text not null, " + "address text not null, "
					+ "latitude text not null, " + "longitude text not null)";

			m_db.execSQL(sql);
		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		// add new place data
		try {
			// start transaction
			m_db.beginTransaction();

			// set data
			ContentValues val = new ContentValues();
			val.put("name", placeinfo.strPlace);
			val.put("address", placeinfo.strAddress);
			val.put("latitude", placeinfo.strLatitude);
			val.put("longitude", placeinfo.strLongitude);

			// write data
			m_db.insert(mCurrentPlanId, null, val);

			// transaction commit
			m_db.setTransactionSuccessful();

			// transaction over
			m_db.endTransaction();

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
		}

		m_db.close();

	}

	private void ShowMap(final String lat, final String lng) {
		final CharSequence[] items = { "乘車", "步行", "駕車" };

		AlertDialog.Builder builder = new AlertDialog.Builder(

		MainActivity.this);

		builder.setTitle("選擇交通方式");

		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {

				StringBuilder params = new StringBuilder().append("&dirflg=");

				switch (item) {

				case 0:
					params.append("r");
					break;

				case 1:
					params.append("w");
					break;

				case 2:
					params.append("d");
					break;

				default:
					break;
				}
				getMap(params.toString(), lat, lng);
			}

		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void getMap(String params, String lat, String lng) {

		Intent i = new Intent(

		Intent.ACTION_VIEW,

		Uri.parse("https://maps.google.com.tw/maps?f=d" + "&saddr=" + m_srclat
				+ ", " + m_srclng + "&daddr=" + lat + ", " + lng + "&hl=tw"
				+ params));

		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				& Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

		i.setClassName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity");

		startActivity(i);

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		m_srclat = String.valueOf(arg0.getLatitude());
		m_srclng = String.valueOf(arg0.getLongitude());

		view_lat.setText(m_srclat);
		view_lng.setText(m_srclng);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
