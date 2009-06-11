package blister_pack.blister.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PillNotificationTable extends AbstractTable
{
	public static final String TABLE_NAME = "PillNotification";

	public static final String COURSE_NAME = CourseTable.COURSE_NAME;
	public static final String DAY_OF_WEEK = "day_of_week";
	public static final String TIME = "time";
	public static final String PILLS_TO_TAKE = "pills_to_take";
	public static final int PILLS_TO_TAKE_DEFAULT = 1;

	public static final String TABLE_ATTRIBUTES = TIME + " TIME NOT NULL," +
			COURSE_NAME + " INTEGER NOT NULL," +
			DAY_OF_WEEK + " INTEGER NOT NULL," +
			PILLS_TO_TAKE + " INTEGER NOT NULL DEFAULT " + PILLS_TO_TAKE_DEFAULT + "," +
			"PRIMARY KEY(" + TIME + "," + COURSE_NAME + "," + DAY_OF_WEEK + ")," +
			"FOREIGN KEY(" + COURSE_NAME + ") REFERENCES " + CourseTable.TABLE_NAME + " ON DELETE CASCADE";

	public static final String TIME_FORMAT_STRING = "HH:mm";

	private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);

	public PillNotificationTable(SQLiteDatabase database)
	{
		super(database);
	}

	protected void setNameAndAttributes()
	{
		tableName = TABLE_NAME;
		tableAttributes = TABLE_ATTRIBUTES;
	}

	// returns SimpleDateFormat used in table to convert time from Date to String and vice-versa
	public SimpleDateFormat getTimeFormat()
	{
		return timeFormat;
	}

	// inserts data into database
	public long insert(String courseName, int dayOfWeek, Date time)
	{
		if (selectOne(courseName,dayOfWeek,time)!=null)
			return -1;
		ContentValues contentValues = new ContentValues();
		// putting data into contentValues
		contentValues.put(COURSE_NAME, courseName);
		contentValues.put(DAY_OF_WEEK, dayOfWeek);
		contentValues.put(TIME, timeFormat.format(time));
		// putting data into database
		try
		{
			long result=db.insert(TABLE_NAME, null, contentValues);
			return result;
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
			return -1;
		}
	}

	// inserts data into database
	public long insert(String courseName, int dayOfWeek, Date time, int pillsToTake) {
		if (selectOne(courseName,dayOfWeek,time)!=null)
			return -1;
		ContentValues contentValues = new ContentValues();
		long result=-1;
		// putting data into contentValues
		contentValues.put(COURSE_NAME, courseName);
		contentValues.put(DAY_OF_WEEK, dayOfWeek);
		contentValues.put(TIME, timeFormat.format(time));
		contentValues.put(PILLS_TO_TAKE, pillsToTake);
		// putting data into database
		try {
			result=db.insert(TABLE_NAME, null, contentValues);
		} catch (SQLException e) {
			Log.e("adalx", e.getMessage());
		}
		return result;

	}
	
	public int updatePills(String courseName, int dayOfWeek, Date time, int newPillsToTake) {
		int result;
		ContentValues contentValues = new ContentValues();
		contentValues.put(PILLS_TO_TAKE, newPillsToTake);
		try {
			result = db.update(TABLE_NAME, contentValues, COURSE_NAME + "=" + "'" + courseName + "'" 
					+ " AND " + DAY_OF_WEEK + "=" + dayOfWeek 
					+ " AND " + TIME + "=" + "'" + timeFormat.format(time) + "'", null);
		} catch (SQLException e) {
			Log.e("adalx", e.getMessage());
			return 0;
		}
		return result;
	}
	
	public void delete(String courseName, int dayOfWeek){
		try{
			deleteWhere(DAY_OF_WEEK + "="
					+ dayOfWeek + " and " + COURSE_NAME + "='" + courseName + "'");
		} catch (SQLException e) {
			Log.e("adalx", e.getMessage());
		}
	}

	public void delete(String courseName, int dayOfWeek, Date time)
	{
		try
		{
			deleteWhere(TIME + "='" + timeFormat.format(time) + "' and " + DAY_OF_WEEK + "="
					+ dayOfWeek + " and " + COURSE_NAME + "='" + courseName + "'");
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
	}

	public void delete(PillNotification notification)
	{
		delete(notification.courseName, notification.dayOfWeek, notification.time);
	}

	// String[] array representing all columns in PillNotificationTable database table
	public static final String[] ALL_COLUMNS = {COURSE_NAME, DAY_OF_WEEK, TIME,
			PILLS_TO_TAKE};

	// returns all notifications from database
	public ArrayList<PillNotification> selectAll()
	{
		Cursor cursor = null;
		try
		{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, TIME);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
		ArrayList<PillNotification> result=convertIntoPillNotificationList(cursor);
		cursor.deactivate();
		return result;
	}

	// returns all notifications from database which belong to course: courseName
	public ArrayList<PillNotification> select(String courseName)
	{
		Cursor cursor;
		try
		{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, COURSE_NAME + "='" + courseName+"'", null, null, null, TIME);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
			cursor = null;
		}
		ArrayList<PillNotification> result=convertIntoPillNotificationList(cursor);
		cursor.deactivate();
		return result;
	}
	
	// returns all notifications from database which belong to course: courseName, and day of week: dayOfWeek
	public ArrayList<PillNotification> select(String courseName, int dayOfWeek)
	{
		Cursor cursor;
		try
		{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, COURSE_NAME + "='" + courseName+"' AND " 
					+ DAY_OF_WEEK + "=" + dayOfWeek, null, null, null, TIME);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
			cursor = null;
		}
		ArrayList<PillNotification> result=convertIntoPillNotificationList(cursor);
		cursor.deactivate();
		return result;
	}

	// selects specific notification from database
	public PillNotification selectOne(String courseName, int dayOfWeek, Date time)
	{
		Cursor cursor = null;
		try
		{
			String where = TIME + "='" + timeFormat.format(time) + "' and " + DAY_OF_WEEK + "=" + dayOfWeek + " and " +
					COURSE_NAME + "='" + courseName + "'";
			Log.v("adalx","PillNotificationTable: selectOne: selection arguments: "+where);
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, where, null, null, null, TIME);
			cursor.moveToFirst();
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
		PillNotification result=convertOneIntoPillNotification(cursor);
		cursor.deactivate();
		return result;
	}

	// converts one row from cursor into Course
	private PillNotification convertOneIntoPillNotification(Cursor cursor)
	{
		if (cursor == null)
			return null;
		if (cursor.isAfterLast() || cursor.isBeforeFirst())
		{
			return null;
		}
		int courseNameIndex = cursor.getColumnIndex(COURSE_NAME);
		int dayOfWeekIndex = cursor.getColumnIndex(DAY_OF_WEEK);
		int timeIndex = cursor.getColumnIndex(TIME);
		int pillsToTakeIndex = cursor.getColumnIndex(PILLS_TO_TAKE);

		PillNotification notification = new PillNotification();
		notification.courseName = cursor.getString(courseNameIndex);
		notification.dayOfWeek = cursor.getInt(dayOfWeekIndex);
		try
		{
			notification.time = timeFormat.parse(cursor.getString(timeIndex));
		}
		catch (ParseException e)
		{
			Log.e("adalx", "invalid time format: " + cursor.getString(timeIndex));
			return null;
		}
		notification.pillsToTake = cursor.getInt(pillsToTakeIndex);

		return notification;
	}

	// converts Cursor into ArrayList<PillNotification>
	private ArrayList<PillNotification> convertIntoPillNotificationList(Cursor cursor)
	{
		ArrayList<PillNotification> pillNotifications = new ArrayList<PillNotification>();

		if (cursor == null)
			return pillNotifications;

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{

			pillNotifications.add(convertOneIntoPillNotification(cursor));
			cursor.moveToNext();
		}
		return pillNotifications;
	}
}
