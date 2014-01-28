package edu.ucla.nesl.rulemanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "rules.db";
	private static final int DATABASE_VERSION = 9;
	
	public static final String COL_SERVER_ID = "server_id";
	public static final String COL_UPLOAD_COUNT = "upload_counter";
	
	public static final String TABLE_LOCATION_LABELS = "location_labels";
	public static final String COL_LABEL_NAME = "label_name";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_RADIUS = "radius";
	
	public static final String TABLE_TIME_LABELS = "time_labels";
	public static final String COL_FROM_DATE = "from_date";
	public static final String COL_FROM_TIME = "from_time";
	public static final String COL_TO_DATE = "to_date";
	public static final String COL_TO_TIME = "to_time";
	public static final String COL_IS_ALL_DAY = "is_all_day";
	public static final String COL_IS_REPEAT = "is_repeat";
	public static final String COL_REPEAT_TYPE = "repeat_type";
	public static final String COL_REPEAT_DAY = "repeat_day";
	public static final String COL_IS_REPEAT_MON = "is_repeat_mon";
	public static final String COL_IS_REPEAT_TUE = "is_repeat_tue";
	public static final String COL_IS_REPEAT_WED = "is_repeat_wed";
	public static final String COL_IS_REPEAT_THU = "is_repeat_thu";
	public static final String COL_IS_REPEAT_FRI = "is_repeat_fri";
	public static final String COL_IS_REPEAT_SAT = "is_repeat_sat";
	public static final String COL_IS_REPEAT_SUN = "is_repeat_sun";
	
	public static final String TABLE_RULES = "rules";
	public static final String COL_ID = "id";
	public static final String COL_ACTION = "action";
	public static final String COL_DATA = "data";
	public static final String COL_CONSUMER = "consumer";
	public static final String COL_TIME_LABEL = "time_label";
	public static final String COL_LOCATION_LABEL = "location_label";
	
	private static final String CREATE_TABLE_LOCATION_LABELS = "CREATE TABLE " + TABLE_LOCATION_LABELS
			+ "( " + COL_LABEL_NAME + " TEXT UNIQUE NOT NULL, "
			+ COL_LATITUDE + " REAL NOT NULL, "
			+ COL_LONGITUDE + " REAL NOT NULL, "
			+ COL_RADIUS + " REAL NOT NULL, "
			+ COL_SERVER_ID + " INTEGER NOT NULL DEFAULT -1, "
			+ COL_UPLOAD_COUNT + " INTEGER NOT NULL);";
	
	private static final String CREATE_TABLE_TIME_LABELS = "CREATE TABLE " + TABLE_TIME_LABELS 
			+ "( " + COL_LABEL_NAME + " TEXT UNIQUE NOT NULL, "
			+ COL_FROM_DATE + " TEXT, "
			+ COL_FROM_TIME + " TEXT, "
			+ COL_TO_DATE + " TEXT, "
			+ COL_TO_TIME + " TEXT, "
			+ COL_IS_ALL_DAY + " INTEGER, "
			+ COL_IS_REPEAT + " INTEGER, "
			+ COL_REPEAT_TYPE + " TEXT, "
			+ COL_REPEAT_DAY + " INTEGER, "
			+ COL_IS_REPEAT_MON + " INTEGER, "
			+ COL_IS_REPEAT_TUE + " INTEGER, "
			+ COL_IS_REPEAT_WED + " INTEGER, "
			+ COL_IS_REPEAT_THU + " INTEGER, "
			+ COL_IS_REPEAT_FRI + " INTEGER, "
			+ COL_IS_REPEAT_SAT + " INTEGER, "
			+ COL_IS_REPEAT_SUN + " INTEGER, "
			+ COL_SERVER_ID + " INTEGER NOT NULL DEFAULT -1, "
			+ COL_UPLOAD_COUNT + " INTEGER NOT NULL);";
	
	private static final String CREATE_TABLE_RULES = "CREATE TABLE " + TABLE_RULES
			+ "( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_ACTION + " TEXT NOT NULL, "
			+ COL_DATA + " TEXT NOT NULL, "
			+ COL_CONSUMER + " TEXT NOT NULL, "
			+ COL_TIME_LABEL + " TEXT NOT NULL DEFAULT \"\", "
			+ COL_LOCATION_LABEL + " TEXT NOT NULL DEFAULT \"\", "
			+ COL_SERVER_ID + " INTEGER NOT NULL DEFAULT -1, "
			+ COL_UPLOAD_COUNT + " INTEGER NOT NULL, "
			+ "UNIQUE (" + COL_ACTION + "," + COL_DATA + "," + COL_CONSUMER + "," + COL_TIME_LABEL + "," + COL_LOCATION_LABEL + ") );";
	
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_LOCATION_LABELS);
		db.execSQL(CREATE_TABLE_TIME_LABELS);
		db.execSQL(CREATE_TABLE_RULES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_LABELS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME_LABELS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RULES);
		onCreate(db);
	}
}
