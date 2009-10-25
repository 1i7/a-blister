package blister_pack.blister.database.triggers;

import android.database.sqlite.SQLiteDatabase;
import blister_pack.blister.database.tables.CourseTable;
import blister_pack.blister.database.tables.OccuredNotificationTable;

public class OccuredNotificationOnDeleteTrigger extends AbstractTrigger
{
	public static final String TRIGGER_NAME = "fkd_" + OccuredNotificationTable.TABLE_NAME;

	public static final String SQL_TRIGGER_ATTRIBUTES = "BEFORE DELETE ON " + CourseTable.TABLE_NAME + " FOR EACH ROW BEGIN " +
			"DELETE FROM " + OccuredNotificationTable.TABLE_NAME + " WHERE " + CourseTable.COURSE_NAME + "=OLD." + CourseTable.COURSE_NAME +
			";END";

	public OccuredNotificationOnDeleteTrigger(SQLiteDatabase database)
	{
		super(database);
	}

	protected void setNameAndAttributes()
	{
		triggerName = TRIGGER_NAME;
		triggerAttributes = SQL_TRIGGER_ATTRIBUTES;
	}
}
