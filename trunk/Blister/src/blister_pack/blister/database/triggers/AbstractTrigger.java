package blister_pack.blister.database.triggers;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class AbstractTrigger
{
	public String triggerName;
	public String triggerAttributes;
	public String sqlCreateTrigger;
	public String sqlDropTrigger;

	protected SQLiteDatabase db;

	public AbstractTrigger(SQLiteDatabase database)
	{
		db = database;
		setNameAndAttributes();
		sqlCreateTrigger = "CREATE TRIGGER IF NOT EXISTS " + triggerName + " " + triggerAttributes;
		sqlDropTrigger = "DROP TRIGGER IF EXISTS " + triggerName;
	}

	protected abstract void setNameAndAttributes();

	public void create()
	{
		try
		{
			db.execSQL(sqlCreateTrigger);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
	}

	public void drop()
	{
		try
		{
			db.execSQL(sqlDropTrigger);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}
	}
}
