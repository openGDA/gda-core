/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.scan;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
import java.util.Vector;

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.jython.JythonStatus;

/**
 * Interface for all scans
 */
public interface Scan extends Serializable {

	public enum ScanStatus {

		// Before runScan()
		NOTSTARTED ("Not started"){
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(RUNNING);
			}
		},

		// Entered from runScan
		RUNNING ("Running"){
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(PAUSED, FINISHING_EARLY, TIDYING_UP_AFTER_STOP, TIDYING_UP_AFTER_FAILURE, COMPLETED_OKAY);
			}
		},

		// When the scan code has noticed the request from a static somewhere
		PAUSED ("Paused") {
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(RUNNING,FINISHING_EARLY, TIDYING_UP_AFTER_STOP);
			}
		},
		FINISHING_EARLY ("Finishing") {
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(COMPLETED_EARLY);
			}
		},  // When a bit of the code has noticed the request from a static somewhere

		TIDYING_UP_AFTER_STOP ("Stopping") {
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(TIDYING_UP_AFTER_STOP, COMPLETED_AFTER_STOP);
			}
		},
		TIDYING_UP_AFTER_FAILURE ("Failing") {
			@Override
			public Set<ScanStatus> possibleFollowUps() {
				return EnumSet.of(TIDYING_UP_AFTER_FAILURE, COMPLETED_AFTER_FAILURE);
			}
		},

		COMPLETED_OKAY ("Complete"),

		COMPLETED_EARLY ("Completed early"),

		COMPLETED_AFTER_STOP ("Aborted"),

		COMPLETED_AFTER_FAILURE ("Failed");

		public Set<ScanStatus> possibleFollowUps() {
			return EnumSet.noneOf(ScanStatus.class);
		}

		public boolean isComplete() {
			return (EnumSet.of(COMPLETED_OKAY, COMPLETED_EARLY, COMPLETED_AFTER_STOP, COMPLETED_AFTER_FAILURE).contains(this));
		}

		public boolean isRunning() {
			return EnumSet.of(RUNNING, FINISHING_EARLY, TIDYING_UP_AFTER_STOP, TIDYING_UP_AFTER_FAILURE ).contains(this);
		}

		public boolean isAborting() {
			return EnumSet.of(TIDYING_UP_AFTER_STOP, TIDYING_UP_AFTER_FAILURE).contains(this);
		}

		public JythonStatus asJython() {
			if (this == PAUSED) {
				return JythonStatus.PAUSED;
			} else if (isComplete() || EnumSet.of(NOTSTARTED).contains(this)) {
				return JythonStatus.IDLE;
			} else if (isRunning()) {
				return JythonStatus.RUNNING;
			} else {
				throw new AssertionError("No mapping from " + this.name());
			}
		}

		private final String string;

		private ScanStatus(final String text) {
			this.string = text;
		}

		@Override
		public String toString() {
			return string;
		}

	}

	/**
	 * pause the scans progress
	 */
	public void pause();

	/**
	 * resume the scans progress after a pause has been called
	 */
	public void resume();

	/**
	 * allows the scan in its own thread. This should NOT be called directly otherwise this may cause thread handling
	 * issues and instability in the command server. runScan() should be called instead.
	 */
	public void run() throws Exception;

	/**
	 * Does the work of creating a new thread and calling the run() method. Inheriting classes may also declare a
	 * runScan method with arguments identical to their constructor. The convention would be to create a new scan
	 * object, and then call this runScan method.
	 *
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public void runScan() throws InterruptedException, Exception;

	/**
	 * The method in which the work of the scan is performed. This method assumes that the data handler has already been
	 * created and the baton claimed.
	 *
	 * @throws Exception
	 */
	public void doCollection() throws Exception;

	/**
	 * Creates a dataHandler, waits until the baton is free and then claims it. This should be performed once in a
	 * collection of scans, before any calls to doCollection().
	 *
	 * @throws Exception
	 */
	public void prepareForCollection() throws Exception;

	/**
	 * Returns the list of all the Scannable objects which are part of this scan
	 *
	 * @return Vector of Scannables
	 */
	public Vector<Scannable> getScannables();

	/**
	 * Sets the list of all the Scannable objects. This should only be used for a parent scan giving its list to a child
	 * scan and not for setting up a scan (that work is done by ScanBase.setUp).
	 *
	 * @param allScannables
	 */
	public void setScannables(Vector<Scannable> allScannables);

	/**
	 * Returns the list of Detector objects which form part of the scan.
	 *
	 * @return Vector of Detectors
	 */
	public Vector<Detector> getDetectors();

	/**
	 * Sets the list of Detectors for this scan.
	 *
	 * @see Scan#setScannables(Vector)
	 * @param allDetectors
	 */
	public void setDetectors(Vector<Detector> allDetectors);

	/**
	 * Returns true if this scan is nested inside another scan.
	 *
	 * @return if this scan is a child
	 */
	public boolean isChild();

	/**
	 * Tells the scan if it is a child.
	 *
	 * @param child
	 */
	public void setIsChild(boolean child);

	/**
	 * Returns the reference to the DataWriter that this scan is using.
	 *
	 * @return the DataWriter
	 */
	public DataWriter getDataWriter();

	/**
	 * Gives the scan a reference to the DataWriter it should use to record data. This will
	 * create a new ScanDataPointPipeline appropriate for the Scannables to be scanned.
	 *
	 * @param dh
	 */
	public void setDataWriter(DataWriter dh);


	/**
	 * Sets the scan data point pipeline used to populate, write and broadcast ScanDataPoints. Should
	 * not normally be set directly except on a child (or sub) scan.
	 * @param scanDataPointPipeline
	 */
	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline);

	/**
	 * Returns the ScanDataPoint pipeline.
	 */
	public ScanDataPointPipeline getScanDataPointPipeline();


	/**
	 * Return a unique identifier for this scan. This is useful for plotting etc. The same id will be included in every
	 * ScanDataPoint sent out by the scan and will be the same for all the scans in a multiregion or mulit-dimensional
	 * scan.
	 *
	 * @return String
	 */
	public String getName();

	/**
	 * @return The child scan if this scan is nested
	 */
	Scan getChild();

	/**
	 * @param child
	 *            The child of this scan if this scan is nested
	 */
	void setChild(Scan child);

	/**
	 * @return The identifier of the current step. This is scan dependent and maybe null
	 */
	IScanStepId getStepId();

	/**
	 * @param IScanStepId
	 *            The identifier of the current step. This is scan dependent and maybe null
	 */
	void setStepId(IScanStepId IScanStepId);


	void setScanPlotSettings(ScanPlotSettings scanPlotSettings);

	/**
	 * @return Settings for plotting
	 */
	ScanPlotSettings getScanPlotSettings();

	/**
	 * @return the number of points of this scan object - the whole scan execution can be a hierarchy of parent scan
	 *         objects and layers of child scan objects
	 */
	int getDimension();

	/**
	 * @return the total number of nodes at which data will be collected in this scan. This includes all the dimensions
	 *         in a multi-dimensional scan.
	 */
	public int getTotalNumberOfPoints();

	/**
	 * @return The unique id of the scan. Null if not set
	 */
	public int getScanNumber();

	/**
	 * Set this scan to complete current scan data point and complete normally without doing any further data points.
	 */
	public void requestFinishEarly();

	public boolean isFinishEarlyRequested();

	/**
	 *
	 * @return The {@link ScanStatus}
	 */
	public ScanStatus getStatus();

	public ScanInformation getScanInformation();
}
