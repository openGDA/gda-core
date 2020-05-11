package org.eclipse.scanning.sequencer;

import java.util.Iterator;

import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 *
 * This class manages the location of
 * various parts of the scan as the scan runs.
 *
 * It maintains a count of levels and creates a
 *
 * @author Matthew Gerring
 *
 */
public final class LocationManager {


	/**
	 * Variables used to monitor progress of inner scans
	 */
	private int outerSize  = 0;
	private int outerCount = 0;
	private int innerSize  = 0;
	private int totalSize  = 0;
	private int stepNumber = -1;

	// External data
	private final ScanBean bean;
	private final ScanModel model;
	private final AnnotationManager manager;

	public LocationManager(ScanBean bean, ScanModel model, AnnotationManager manager) {
		this.bean    = bean;
		this.model   = model;
		this.manager = manager;
		manager.addDevices(this);
	}

	public int getOuterSize() {
		return outerSize;
	}
	public void setOuterSize(int outerSize) {
		this.outerSize = outerSize;
	}
	public int getOuterCount() {
		return outerCount;
	}
	public void setOuterCount(int outerCount) {
		this.outerCount = outerCount;
	}
	public int getInnerSize() {
		return innerSize;
	}
	public void setInnerSize(int innerSize) {
		this.innerSize = innerSize;
	}
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
	public int getStepNumber() {
		return stepNumber;
	}
	public void setStepNumber(int stepNumber) {
		this.stepNumber = stepNumber;
	}

	/**
	 * Called during Software scans to increment counts.
	 * outerCount only relevant in Malcolm scans, changed with setter/getters
	 * stepNumber for Malcolm scans changed with setter/getters 
	 */
	@PointEnd
	public void increment() {
		if (innerSize == 0) stepNumber++;
	}

	/**
	 * Method used to generate an iterator for the scan.
	 * It sets counts which are incremented during the scan.
	 *
	 * @return
	 * @throws ScanningException
	 */
	public Iterator<IPosition> createPositionIterator() throws ScanningException {
		SubscanModerator moderator = new SubscanModerator(model);
		manager.addContext(moderator);

		stepNumber = 0;
		outerSize  = moderator.getOuterScanSize();
		innerSize  = moderator.getInnerScanSize();
		totalSize  = moderator.getTotalScanSize();

		return moderator.getOuterPointGenerator().iterator();
	}

	/**
	 * Seek within the iterator for the given location.
	 * @param location
	 * @param iterator
	 * @return null if position not found.
	 */
	public IPosition seek(int location, Iterator<IPosition> iterator) {

		stepNumber=0;
		/*
		 * IMPORTANT We do not keep the positions in memory because there can be millions.
		 * Running over them is fast however.
		 */
		while(iterator.hasNext()) {
			IPosition pos = iterator.next();
			pos.setStepIndex(stepNumber);
			if (stepNumber == location) return pos;
			stepNumber+=Math.max(innerSize, 1);
		}

		return null;
	}

	public boolean isInnerScan() {
		return outerSize > 0 && innerSize > 0;
	}

	public int getOverallCount() {
		return getStepNumber();
	}

	/**
	 * Returns the total percent complete for the scan(s)
	 * @return
	 */
	public double getOuterPercent() {
		return ((double) (getOverallCount()) / getTotalSize()) * 100;
	}

}