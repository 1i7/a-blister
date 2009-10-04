/**
 * 
 */
package blister_pack.blister;

import java.io.Serializable;
import java.util.Date;

import blister_pack.blister.database.tables.OccuredNotification;

public class MissedNotification extends OccuredNotification implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2422026978021908692L; // generated code for Serializable
	
	public MissedNotification() {
		super();
	}
	
	public MissedNotification(String courseName, Date time) {
		super(courseName, time);
	}
	
	public MissedNotification(OccuredNotification occuredNotification) {
		super(occuredNotification);
	}
}