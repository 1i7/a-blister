package blister_pack.blister.database.tables;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class AbstractTable
{
	public String tableName;
	public String tableAttributes;
	public String sqlCreateTable;
	public String sqlDropTable;

	protected SQLiteDatabase db;

	public AbstractTable(SQLiteDatabase database)
	{
		db = database;
		setNameAndAttributes();
		sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + tableAttributes + ")";
		sqlDropTable = "DROP TABLE IF EXISTS " + tableName;
	}

	protected abstract void setNameAndAttributes();

	public void create()
	{
		try
		{
			db.execSQL(sqlCreateTable);
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
			db.execSQL(sqlDropTable);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}

	}

	public void deleteWhere(String where)
	{
		try
		{
			db.delete(tableName, where, null);
		}
		catch (SQLException e)
		{
			Log.e("adalx", e.getMessage());
		}

	}
}
