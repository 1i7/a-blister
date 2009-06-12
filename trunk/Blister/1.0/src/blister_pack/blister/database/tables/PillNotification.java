package blister_pack.blister.database.tables;

import java.util.Date;

public class PillNotification
{
	public String courseName;
	public int dayOfWeek;
	public Date time;
	public int pillsToTake;

	public PillNotification()
	{
		return;
	}

	public PillNotification(Date time, int dayOfWeek, String courseName, int pillsToTake)
	{
		set(time, dayOfWeek, courseName, pillsToTake);
	}

	public PillNotification(PillNotification notification)
	{
		set(notification.time, notification.dayOfWeek, notification.courseName, notification.pillsToTake);
	}

	public void set(Date time, int dayOfWeek, String courseName, int pillsToTake)
	{
		this.time = time;
		this.dayOfWeek = dayOfWeek;
		this.courseName = courseName;
		this.pillsToTake = pillsToTake;
	}
}
