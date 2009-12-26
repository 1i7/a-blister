package blister_pack.blister;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver
{
	public void onReceive(Context context, Intent intent)
	{
		context.startService(new Intent(context, ScheduleAlarmsService.class));
	}
}
