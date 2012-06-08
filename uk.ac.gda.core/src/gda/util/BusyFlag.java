/*
 * Copyright (c) 1997-1999 Scott Oaks and Henry Wong. All Rights Reserved. Permission to use, copy, modify, and
 * distribute this software and its documentation for NON-COMMERCIAL purposes and without fee is hereby granted. This
 * sample source code is provided for example only, on an unsupported, as-is basis. AUTHOR MAKES NO REPRESENTATIONS OR
 * WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. AUTHOR SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS
 * REQUIRING FAIL-SAFE PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT NAVIGATION OR COMMUNICATION
 * SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE SOFTWARE
 * COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").
 * AUTHOR SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR HIGH RISK ACTIVITIES.
 */

/* BusyFlag class from chapter four of Java Threads by Oaks and Wong */
/* PCS 26/06/03 Imported and changed to our layout standard */
/* PCS 04/07/03 Changed protection level of some methods and variables */
/* and added comments (but see the book for full explanation) */

package gda.util;

/**
 * Class to allow several Threads to access the same object without interfering with one another and without using
 * sychronization. An example of its use can be found in AsynchronousReaderWriter.
 * 
 */
public class BusyFlag {
	private Thread busyflag = null;

	private int busycount = 0;

	/**
	 * Call this to become the BusyFlag owner. If some other thread is the BusyFlag owner this will wait() indefinitely.
	 */
	public synchronized void getBusyFlag() {
		while (tryGetBusyFlag() == false) {
			try {
				wait();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Checks whether it is possible for the current thread to become the busyflag owner and if it is makes it so. The
	 * busycount mechanism allows Threads already owning the flag to call getBusyFlag without becoming deadlocked
	 * (waiting for themselves to free the flag).
	 * 
	 * @return true if busy
	 */
	private synchronized boolean tryGetBusyFlag() {
		/* If noone is the busyflag owner this thread */
		/* can have it. */
		if (busyflag == null) {
			busyflag = Thread.currentThread();
			busycount = 1;
			return true;
		}
		/* If this thread is already the busyflag owner */
		/* then just increment the counter */
		if (busyflag == Thread.currentThread()) {
			busycount++;
			return true;
		}
		return false;
	}

	/**
	 * Call this to free the BusyFlag. Other threads waiting on this are notified.
	 */
	public synchronized void freeBusyFlag() {
		/* Only the owning thread can reduce the count */
		/* or (eventually) free the busyflag */
		if (getBusyFlagOwner() == Thread.currentThread()) {
			busycount--;
			if (busycount == 0) {
				busyflag = null;
				notify();
			}
		}
	}

	/**
	 * Find the Thread that currently owns the BusyFlag
	 * 
	 * @return busyFlag Thread that owns the BusyFlag
	 */
	private synchronized Thread getBusyFlagOwner() {
		return busyflag;
	}
}