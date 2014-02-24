package org.weather.weatherman.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseSupport extends SQLiteOpenHelper {

	public static final String DBNAME = "org.weather.weatherman.db";
	public static final int DBVERSION = 183;

	public static final String AUTHORITY = "org.weather.weatherman.provider";
	public static final String PATH_CONTENT = "content", PATH_CITY = "city";

	/**
	 * 城市表，记录全国三级城市信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-24 下午5:23:06
	 */
	public static final class City {

		private City() {
		}

		public static final int TYPE = 10;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_CITY);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_CITY;

		public static final String TABLE_NAME = "city";

		public static final String ID = BaseColumns._ID;
		public static final String CODE = "code";
		public static final String NAME = "name";
		public static final String PARENT = "p";

	}

	/**
	 * 内容表，记录实况、预报、指数等天气信息以及城市设置信息
	 * 
	 * @author gengmaozhang01
	 * @since 2014-2-24 下午5:22:56
	 */
	public static final class Content {

		private Content() {
		}

		public static final int TYPE = 20;
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_CONTENT);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_CONTENT;

		public static final String TABLE_NAME = "content";

		public static final String COL_ID = BaseColumns._ID;
		public static final String COL_CODE = "code";
		public static final String COL_TYPE = "type";
		public static final String COL_VALUE = "value";
		public static final String COL_UPDATETIME = "ut";

	}

	private Context context;

	public DatabaseSupport(Context context) {
		super(context, DBNAME, null, DBVERSION);
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// 城市表，记录全国三级城市信息
		String sql = "create table if not exists " + City.TABLE_NAME + "( " + City.ID + " integer primary key, "
				+ City.CODE + " text, " + City.NAME + " text, " + City.PARENT + " text " + "); ";
		arg0.execSQL(sql);
		// 内容表，记录实况、预报、指数等天气信息以及城市设置信息
		sql = "create table if not exists " + Content.TABLE_NAME + "( " + Content.COL_ID + " integer primary key, "
				+ Content.COL_CODE + " text, " + Content.COL_TYPE + " integer, " + Content.COL_VALUE + " text, "
				+ Content.COL_UPDATETIME + " integer " + "); ";
		arg0.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("drop table if exists " + City.TABLE_NAME);
		arg0.execSQL("drop table if exists " + Content.TABLE_NAME);
		onCreate(arg0);
	}

}
