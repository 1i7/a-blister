package blister_pack.blister.windows;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import blister_pack.blister.R;

public abstract class ListWindow extends ListActivity{
	
	Button okButton;			// OkButton in the set_time_window layout
	
	ListView dataList;			// ListView used to image the data
	
	Cursor dataCursor;			// Cursor used to get data from
	
	Menu listMenu;
	
	protected static String pillNameTitle;		// Used when moving between Activities to 
										// store name of current pill
	protected String selectedItemTitle;
	protected int selectedItemPosition;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		/* Now we set the custom view of the ListActivity					*/
		setContentView(R.layout.list_window);
		
		dataList= (ListView) findViewById(android.R.id.list);
		dataList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		LinearLayout BottomButtonPanel = (LinearLayout)findViewById(R.id.ListWindowBottomButtonPanel);
		BottomButtonPanel.setVisibility(View.GONE);
		
        /* Now we create new buttons
         * by finding their id in set_time_window layout */
		okButton = (Button)findViewById(R.id.ListWindowOKButton);
	}
	
	@Override
	protected void onResume() {
		Log.v("eldar",this.getClass().getName()+": onResume");
		super.onResume();
		refreshListActivity();
		Log.v("eldar",this.getClass().getName()+": resumed");
	}
	
	protected void refreshListActivity() {
		/* Now we get the Cursor using abstract getCursor() method			*/
		dataCursor = getCursor();
		
		/* Now we make new ListAdapter to set the connection between
		 * ListView and Cursor.
		 * Then we apply this adapter to current ListView					*/
		ListAdapter adapter = new DataCursorAdapter(this,
				getListItemID(), dataCursor,
                        new String[] { "title", "info" }, 
                        new int[] { getItemTitleID(), getItemStatID() });
        setListAdapter(adapter);

		Log.v("eldar",this.getClass().getName()+": refreshListActivity");
	}
	
	abstract int getListItemID();
	abstract int getItemTitleID();
	abstract int getItemStatID();
	
	/* Override this method to return the cursor 
	 * used when filling the ListView.
	 * Cursor MUST contain columns "_id", "title", "info". */
	abstract Cursor getCursor();
	
}
