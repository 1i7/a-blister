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

public class OccuredNotificationTable extends AbstractTable
{
	public static final String TABLE_NAME = "OccuredNotification";
	public static final String COURSE_NAME = CourseTable.COURSE_NAME;
	public static final String OCCUR_TIME = "occure_time";
	public static final String TABLE_ATTRIBUTES = COURSE_NAME + " INTEGER NOT NULL," +
			OCCUR_TIME + " INTEGER NOT NULL," +
			"PRIMARY KEY(" + COURSE_NAME + "," + OCCUR_TIME + ")," +
			"FOREIGN KEY(" + COURSE_NAME + ") REFERENCES " + CourseTable.TABLE_NAME + " ON DELETE CASCADE";

	public static final String TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm";

	private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);

	public OccuredNotificationTable(SQLiteDatabase database)
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
	public long insert(String courseName, Date occurTime)
	{
		ContentValues contentValues = new ContentValues();
		long result=0;
		// putting data into contentValues
		contentValues.put(COURSE_NAME, courseName);
		contentValues.put(OCCUR_TIME, timeFormat.format(occurTime));
		// putting data into database
		try
		{
			result=db.insert(TABLE_NAME, null, contentValues);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
		return result;
	}

	public void delete(String courseName, Date occurTime)
	{
		try
		{
			deleteWhere(OCCUR_TIME + "='" + timeFormat.format(
					occurTime) + "' and " + COURSE_NAME + "='" + courseName + "'");
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
	}
	
	public void delete(OccuredNotification occuredNotification) {
		delete(occuredNotification.courseName,occuredNotification.occurTime);
	}

	// String[] array representing all columns in PillNotificationTable database table
	public static final String[] ALL_COLUMNS = {COURSE_NAME, OCCUR_TIME};

	// returns all notifications from database
	public ArrayList<OccuredNotification> selectAll()
	{
		Cursor cursor = null;
		try
		{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, OCCUR_TIME);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
		ArrayList<OccuredNotification> result=convertIntoOccuredNotificationList(cursor);
		cursor.deactivate();
		return result;
	}

	// returns all notifications from database which belong to course: courseName
	public ArrayList<OccuredNotification> select(String courseName)
	{
		Cursor cursor;
		try
		{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, COURSE_NAME + "='" + courseName + "'", null, null, null, OCCUR_TIME);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
			cursor = null;
		}
		ArrayList<OccuredNotification> result=convertIntoOccuredNotificationList(cursor);
		cursor.deactivate();
		return result;
	}	

	//converts Cursor into ArrayList<OccuredNotification>
	private ArrayList<OccuredNotification> convertIntoOccuredNotificationList(Cursor cursor)
	{
		ArrayList<OccuredNotification> occuredNotifications = new ArrayList<OccuredNotification>();

		if (cursor == null)
			return occuredNotifications;

		int courseNameIndex = cursor.getColumnIndex(COURSE_NAME);
		int occurTimeIndex = cursor.getColumnIndex(OCCUR_TIME);
		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			OccuredNotification occuredNotification = new OccuredNotification();
			occuredNotification.courseName = cursor.getString(courseNameIndex);
			try
			{
				occuredNotification.occurTime = timeFormat.parse(cursor.getString(occurTimeIndex));
			}
			catch (ParseException e)
			{
				Log.v("adalx", "invalid time format: " + cursor.getString(occurTimeIndex));
			}
			occuredNotifications.add(occuredNotification);
			cursor.moveToNext();
		}
		return occuredNotifications;
	}
}
