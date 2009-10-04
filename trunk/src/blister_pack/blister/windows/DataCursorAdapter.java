package blister_pack.blister.windows;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class DataCursorAdapter extends SimpleCursorAdapter {
	
	ListView list;
	
	public DataCursorAdapter (Context context, int layout, Cursor cursor, String[] from, int[] to)
	{
		super(context, layout, cursor, from, to);
		list = ((ListActivity)context).getListView();
	}
	
	public View getView (int position, View convertView, ViewGroup parent)
	{
		View v = super.getView(position, convertView, parent);	
		return v;
	}
}
