package blister_pack.blister.database.triggers;

import blister_pack.blister.database.tables.CourseTable;
import blister_pack.blister.database.tables.OccuredNotificationTable;
import android.database.sqlite.SQLiteDatabase;

public class OccuredNotificationOnUpdateTrigger extends AbstractTrigger 
{
	public static final String TRIGGER_NAME = "fku_" + OccuredNotificationTable.TABLE_NAME;

	public static final String SQL_TRIGGER_ATTRIBUTES = "BEFORE UPDATE ON " + CourseTable.TABLE_NAME + " FOR EACH ROW BEGIN " 
			+ "UPDATE " + OccuredNotificationTable.TABLE_NAME + " SET " + OccuredNotificationTable.COURSE_NAME + "=NEW." 
			+ CourseTable.COURSE_NAME + " WHERE " + CourseTable.COURSE_NAME + "=OLD." + CourseTable.COURSE_NAME 
			+ ";END";

	public OccuredNotificationOnUpdateTrigger(SQLiteDatabase database) 
	{
		super(database);
	}

	@Override
	protected void setNameAndAttributes() 
	{
		triggerName = TRIGGER_NAME;
		triggerAttributes = SQL_TRIGGER_ATTRIBUTES;
	}

}
