package org.weather.weatherman.content;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseSupport extends SQLiteOpenHelper {

	public static final String DBNAME = "org.weather.weatherman.db";
	public static final int DBVERSION = 2;

	public static final String TABLE_NAME = "content";
	public static final String COL_ID = "id";
	public static final String COL_TYPE = "type";
	public static final String COL_VALUE = "value";
	public static final String COL_UPDATETIME = "ut";

	public DatabaseSupport(Context context) {
		super(context, DBNAME, null, DBVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		String sql = "create table if not exists " + TABLE_NAME + "( " + BaseColumns._ID + " integer primary key, "
				+ COL_ID + " integer, " + COL_TYPE + " integer, " + COL_VALUE + " text, " + COL_UPDATETIME
				+ " integer " + "); ";
		arg0.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("drop table is exists " + TABLE_NAME);
		onCreate(arg0);
	}

	public Cursor find(String where, Object[] args) {
		String sel = where;
		String[] selArgs = new String[args != null ? args.length : 0];
		for (int i = 0; i < selArgs.length; i++) {
			selArgs[i] = args[i].toString();
		}
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_NAME, null, sel, selArgs, null, null, null);
	}

	public long save(long rowId, ContentValues values) {
		values.put(COL_UPDATETIME, new Date().getTime() / 1000);
		SQLiteDatabase db = getWritableDatabase();
		if (rowId > 0) {
			db.update(TABLE_NAME, values, BaseColumns._ID + "=?", new String[] { String.valueOf(rowId) });
		} else {
			rowId = db.insert(TABLE_NAME, null, values);
		}
		return rowId;
	}

}
