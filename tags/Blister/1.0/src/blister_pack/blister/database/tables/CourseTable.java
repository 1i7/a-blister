package blister_pack.blister.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CourseTable extends AbstractTable
{
	public static final String TABLE_NAME = "Course";
	public static final String COURSE_NAME = "course_name";
	public static final String START_DATE = "start_date";
	public static final String DURATION = "duration";
	public static final String PILLS_REMAINED = "pills_remained";
	public static final String TABLE_ATTRIBUTES = COURSE_NAME + " TEXT PRIMARY KEY," +
			START_DATE + " DATE NOT NULL," +
			DURATION + " INTEGER NOT NULL," +
			PILLS_REMAINED + " INTEGER NOT NULL";
	public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";

	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);


	public CourseTable(SQLiteDatabase database)
	{
		super(database);
	}

	// returns SimpleDateFormat used in table to convert date from Date to String and vice-versa
	public SimpleDateFormat getDateFormat()
	{
		return dateFormat;
	}

	protected void setNameAndAttributes(){
		tableName = TABLE_NAME;
		tableAttributes = TABLE_ATTRIBUTES;
	}

	// inserts data into database
	public long insert(String courseName, Date startDate, int duration, int pillsRemained){
		
		ContentValues contentValues = new ContentValues();
		if (select(courseName)!=null)
			return -1;
		long result=-1;
		// putting data into contentValues
		contentValues.put(COURSE_NAME, courseName);
		contentValues.put(START_DATE, dateFormat.format(startDate));
		contentValues.put(DURATION, duration);
		contentValues.put(PILLS_REMAINED, pillsRemained);
		// putting data into database
		try{
			result=db.insert(TABLE_NAME, null, contentValues);
		} catch (SQLException e) {
		}
		return result;
	}

	public long insert(Course course){
		return insert(course.courseName, course.startDate, course.duration, course.pillsRemained);
	}

	// updates the number of pills remained in course: courseName
	public int updatePills(String courseName, int pillsRemained){
		ContentValues contentValues = new ContentValues();
		int result;
		// putting data into contentValues
		contentValues.put(PILLS_REMAINED, pillsRemained);
		// putting data into database
		try {
			result=db.update(TABLE_NAME, contentValues, COURSE_NAME + "=" + "'" + courseName + "'", null);
		} catch (SQLException e) {
			return 0;
		}
		return result;
	}

	// updates all information about course: courseName
	public int update(String courseName, String newName, Date startDate, int duration, int pillsRemained){
		ContentValues contentValues = new ContentValues();
		int result=0;
		// putting data into contentValues
		contentValues.put(COURSE_NAME, newName);
		contentValues.put(START_DATE, dateFormat.format(startDate));
		contentValues.put(DURATION, duration);
		contentValues.put(PILLS_REMAINED, pillsRemained);
		// putting data into database
		try{
			result=db.update(TABLE_NAME, contentValues, COURSE_NAME + "=" + "'" + courseName + "'", null);
		} catch (SQLException e) {
		}
		return result;
	}

	public void delete(String courseName){
		try{
			deleteWhere(COURSE_NAME + "='" + courseName + "'");
		} catch (SQLException e) {
		}
	}

	public void delete(Course course){
		delete(course.courseName);
	}

	// String[] array representing all columns in CourseTable database table
	public static final String[] ALL_COLUMNS = {COURSE_NAME, START_DATE, DURATION,
			PILLS_REMAINED};

	public Course select(String courseName){
		Cursor cursor;
		try{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, COURSE_NAME + "='" + courseName + "'", null, null, null, null);
			cursor.moveToNext();
		} catch (SQLException e) {
			cursor = null;
		}
		Course result=convertOneIntoCourse(cursor);
		cursor.deactivate();
		return result;
	}

	// returns all courses from database
	public ArrayList<Course> selectAll(){
		Cursor cursor;
		try{
			cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, COURSE_NAME);
		} catch (SQLException e) {
			cursor = null;
		}
		ArrayList<Course> result=convertIntoCourseList(cursor);
		cursor.deactivate();
		return result;
	}

	// converts one row from cursor into Course
	private Course convertOneIntoCourse(Cursor cursor)
	{
		if (cursor == null)
			return null;
		if (cursor.isAfterLast() || cursor.isBeforeFirst())
			return null;

		int courseNameIndex = cursor.getColumnIndex(COURSE_NAME);
		int startDateIndex = cursor.getColumnIndex(START_DATE);
		int durationIndex = cursor.getColumnIndex(DURATION);
		int pillsRemainedIndex = cursor.getColumnIndex(PILLS_REMAINED);

		Course course = new Course();
		course.courseName = cursor.getString(courseNameIndex);
		try
		{
			course.startDate = dateFormat.parse(cursor.getString(startDateIndex));
		}
		catch (ParseException e)
		{
			return null;
		}
		course.duration = cursor.getInt(durationIndex);
		course.pillsRemained = cursor.getInt(pillsRemainedIndex);

		return course;
	}

	// converts Cursor into ArrayList<Course>
	private ArrayList<Course> convertIntoCourseList(Cursor cursor)
	{
		ArrayList<Course> courseList = new ArrayList<Course>();

		if (cursor == null)
			return courseList;

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			courseList.add(convertOneIntoCourse(cursor));
			cursor.moveToNext();
		}
		return courseList;
	}
}