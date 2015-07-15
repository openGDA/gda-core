package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

public class ScanPointStartEvent implements Serializable {

	private int currentPointNumber;

	public int getCurrentPointNumber() {
		return currentPointNumber;
	}

	public ScanPointStartEvent(int i) {
		this.currentPointNumber=i;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

}
