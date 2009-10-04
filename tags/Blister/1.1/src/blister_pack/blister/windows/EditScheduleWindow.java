package blister_pack.blister.windows;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import blister_pack.blister.R;

public class EditScheduleWindow extends TabActivity{
	
	String pillNameTitle;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        Log.v("eldar","EditScheduleWindow: onCreate");
        Intent intent = getIntent();
		pillNameTitle = intent.getStringExtra("pillName");
		
		
		super.onCreate(savedInstanceState);
        
        final TabHost tabHost = getTabHost();
 
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(pillNameTitle+"\n"+getString(R.string.timetable_text_low))
                .setContent(new Intent(this, WeekWindow.class)
                	.putExtra("pillName", pillNameTitle)
                )); 
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(pillNameTitle+"\n"+getString(R.string.info_text_low))
                .setContent(new Intent(this, ScheduleInfoWindow.class)
                	.putExtra("pillName", pillNameTitle)
                ));
        if (intent.getBooleanExtra("rebuildState", false)) {
        	tabHost.setCurrentTabByTag("tab2");
        }
        Log.v("eldar","EditScheduleWindow: created");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
