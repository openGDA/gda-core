package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

public class ScanStartEvent implements Serializable {

	private int scanNumber;
	private String scanFilename;
	private int numberOfPoints;

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public ScanStartEvent(int scannumber, int numberOfPoints,String scanFilename) {
		this.scanNumber=scannumber;
		this.scanFilename=scanFilename;
		this.numberOfPoints=numberOfPoints;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	public String getScanFilename() {
		return scanFilename;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 640299152011958530L;

}
