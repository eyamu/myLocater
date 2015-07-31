package com.wpr.mylocator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MySQLiteHelper extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "locatorDB";

	// table name
	private static final String TABLE_JNY = "destn";

	// Table Columns names
	private static final String KEY_FROM = "position";
	private static final String KEY_TO = "destination";
	private static final String KEY_LONG = "longtudes";
	private static final String KEY_LAT = "latitudes";

	private static final String[] COLUMNS = { KEY_FROM, KEY_TO, KEY_LONG,
			KEY_LAT };
	
	Context cntx;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.cntx = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create table
		String CREATE_DESTN_TABLE = "CREATE TABLE destn ( " + "position TEXT, "
				+ "destination TEXT, " + "longtudes TEXT, " + "latitudes TEXT "
				+ ")";
		// create table
		db.execSQL(CREATE_DESTN_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
		db.execSQL("DROP TABLE IF EXISTS destn");
		this.onCreate(db);
	}

	// CRUDE FUNCTIONS

	public void addJourney(Journey journey) {
		// for logging
		deleteJourney();
		try{
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_FROM, journey.getFrom()); // get from
		values.put(KEY_TO, journey.getTo()); // get to
		values.put(KEY_LONG, journey.getLongtudes()); // get to
		values.put(KEY_LAT, journey.getLatitudes()); // get to

		// 3. insert
		db.insert(TABLE_JNY, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values
		// 4. close
		db.close();
		}catch(Exception e){
			Toast.makeText(this.cntx, e.toString(),
					Toast.LENGTH_SHORT).show();
		}
	}

	// return the journey
	public Journey getJourney() {
		Journey journey = new Journey();
		try{
		// 1. get reference to readable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. build query
		Cursor cursor = db.query(TABLE_JNY, // a. table
				COLUMNS, // b. column names
				null, // c. selections
				null, // d. selections args
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit

		// 3. if we got results get the first one
		if (cursor != null)
			cursor.moveToFirst();		
		journey.setFrom(cursor.getString(0));
		journey.setTo(cursor.getString(1));
		
		}catch(Exception ex){
			Toast.makeText(this.cntx, ex.toString(),
					Toast.LENGTH_SHORT).show();
		}
		return journey;
	}

	// delete the journey
	public void deleteJourney() {
		try{
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. delete
		db.delete(TABLE_JNY, // table name
				null, // selections
				null); // selections args

		// 3. close
		db.close();
		}catch(Exception e){
			Toast.makeText(this.cntx, e.toString(),
					Toast.LENGTH_SHORT).show();		}
	}

}
