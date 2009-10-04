package blister_pack.blister.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import blister_pack.blister.database.tables.CourseTable;
import blister_pack.blister.database.tables.OccuredNotificationTable;
import blister_pack.blister.database.tables.PillNotificationTable;
import blister_pack.blister.database.triggers.OccuredNotificationOnDeleteTrigger;
import blister_pack.blister.database.triggers.OccuredNotificationOnUpdateTrigger;
import blister_pack.blister.database.triggers.PillNotificationOnDeleteTrigger;
import blister_pack.blister.database.triggers.PillNotificationOnUpdateTrigger;

public class BlisterDatabase
{
	public static final String DATABASE_NAME = "blister_db";
	public static int DATABASE_VERSION = 1;

	private static SQLiteDatabase db;
	private static CourseTable courseTable;
	private static PillNotificationTable pillNotificationTable;
	private static OccuredNotificationTable occuredNotificationTable;
	private static PillNotificationOnDeleteTrigger pillNotificationOnDeleteTrigger;
	private static OccuredNotificationOnDeleteTrigger occuredNotificationOnDeleteTrigger;
	private static PillNotificationOnUpdateTrigger pillNotificationOnUpdateTrigger;
	private static OccuredNotificationOnUpdateTrigger occuredNotificationOnUpdateTrigger;
	private static Context openerContext;
	private static BlisterDatabase blisterDatabase;

	public static BlisterDatabase openDatabase(Context context) {
		if (blisterDatabase==null)
			blisterDatabase=new BlisterDatabase(context);
		return blisterDatabase;
	}

	private BlisterDatabase(Context context) {
		openerContext = context;
		create();
	}

	// opens existing database (or creates database file if it doesn't exist): doesn't create any tables
	protected void open() {
		db = openerContext.openOrCreateDatabase(DATABASE_NAME, 0, null);
		courseTable = new CourseTable(db);
		pillNotificationTable = new PillNotificationTable(db);
		occuredNotificationTable = new OccuredNotificationTable(db);
		pillNotificationOnDeleteTrigger = new PillNotificationOnDeleteTrigger(db);
		occuredNotificationOnDeleteTrigger = new OccuredNotificationOnDeleteTrigger(db);
		pillNotificationOnUpdateTrigger = new PillNotificationOnUpdateTrigger(db);
		occuredNotificationOnUpdateTrigger = new OccuredNotificationOnUpdateTrigger(db);
	}

	// creates database
	protected void create() {
		open();
		createTables();
		createTriggers();
	}

	// creates tables
	protected void createTables() {
		courseTable.create();
		pillNotificationTable.create();
		occuredNotificationTable.create();
	}

	// creates triggers
	protected void createTriggers() {
		occuredNotificationOnDeleteTrigger.create();
		pillNotificationOnDeleteTrigger.create();
		occuredNotificationOnUpdateTrigger.create();
		pillNotificationOnUpdateTrigger.create();
	}

	// drops tables
	protected void dropTables() {
		pillNotificationTable.drop();
		courseTable.drop();
		occuredNotificationTable.drop();
	}

	// drops triggers
	protected void dropTriggers() {
		pillNotificationOnDeleteTrigger.drop();
		occuredNotificationOnDeleteTrigger.drop();
		pillNotificationOnUpdateTrigger.drop();
		occuredNotificationOnUpdateTrigger.drop();
	}

	// recreates database
	public void recreate() {
		delete();
		create();
	}

	public void delete() {
		openerContext.deleteDatabase(DATABASE_NAME);
	}

	public CourseTable getCourseTable() {
		return courseTable;
	}

	public PillNotificationTable getPillNotificationTable() {
		return pillNotificationTable;
	}

	public OccuredNotificationTable getOccuredNotificationTable() {
		return occuredNotificationTable;
	}

}
