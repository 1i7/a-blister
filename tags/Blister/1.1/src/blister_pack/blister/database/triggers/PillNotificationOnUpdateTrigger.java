package blister_pack.blister.database.triggers;

import blister_pack.blister.database.tables.CourseTable;
import blister_pack.blister.database.tables.PillNotificationTable;
import android.database.sqlite.SQLiteDatabase;

public class PillNotificationOnUpdateTrigger extends AbstractTrigger 
{
	public static final String TRIGGER_NAME = "fku_" + PillNotificationTable.TABLE_NAME;

	public static final String SQL_TRIGGER_ATTRIBUTES = "BEFORE UPDATE ON " + CourseTable.TABLE_NAME + " FOR EACH ROW BEGIN " 
			+ "UPDATE " + PillNotificationTable.TABLE_NAME + " SET " + PillNotificationTable.COURSE_NAME + "=NEW." 
			+ CourseTable.COURSE_NAME + " WHERE " + CourseTable.COURSE_NAME + "=OLD." + CourseTable.COURSE_NAME 
			+ ";END";

	public PillNotificationOnUpdateTrigger(SQLiteDatabase database) 
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
