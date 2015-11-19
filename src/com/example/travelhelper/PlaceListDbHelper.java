package com.example.travelhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaceListDbHelper extends SQLiteOpenHelper{
	
	public PlaceListDbHelper(Context con)
	{
		super(con, "PlaceDb", null, 1);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
		// TODO Auto-generated method stub
		
	}

}
