package blister_pack.blister.database.tables;

import java.util.Date;

public class OccuredNotification {

	public String courseName;
	public Date occurTime;

	public OccuredNotification() {
		super();
	}

	public OccuredNotification(String courseName, Date occurTime) {
		set(courseName, occurTime);
	}

	public OccuredNotification(OccuredNotification occuredNotification) {
		set(occuredNotification.courseName, occuredNotification.occurTime);
	}

	public void set(String courseName, Date occurTime) {
		this.courseName = courseName;
		this.occurTime = occurTime;
	}
}
