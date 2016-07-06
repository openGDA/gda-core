/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

package gda.device.detector.mythen;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.NumTracker;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.pilatus.TangoLimaDetector;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * Mythen detectors interface.
 */
@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class TangoMythenDetector extends TangoLimaDetector implements Runnable { //implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(TangoMythenDetector.class);
	private Thread runner;
	private int state;

	private int lastFrame = 0;
	private int currentFrame = 0;
	private String plotPanelName;
	private TangoDeviceProxy tangoDeviceProxy = null;

//	private static int count = 0;
	private double collectionTime;

	public TangoMythenDetector() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			tangoDeviceProxy.isAvailable();
			width = getWidth();
			setConfigured(true);
			setExtraNames(new String[] { "lastImageNumber"});
			setOutputFormat(new String[]{"%d"});
			this.setInputNames(new String[0]);
//			setInputNames(null);
//			setExtraNames(null);
			runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
			runner.start();
		} catch (Exception e) {
			setConfigured(false);
			logger.error("TangoMythenDetector {} configure: {}", getName(), e.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("tango mythen device proxy needs to be set");
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
	}

	@Override
	public void atScanStart() throws DeviceException {
		// // scanPoint = 0;
		// // tangoDeviceProxy.setSavingNextNumber(getScanNumber());
		// String dataDir = PathConstructor.createFromDefaultProperty();
		// if (dataDir != null) {
		// // tangoDeviceProxy.setSavingDirectory(dataDir);
		// }
	}

	@Override
	public void atScanEnd() throws DeviceException {
//
//		@Override
//	    public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
//	            try {
//	                    String detectorType = detector.getDetectorType();
//	                    dataTree.addScanFileLink(getName(), "nxfile://" + detector.getAttribute("LastFileName") + "#entry_0000/measurement/" + detectorType + "/data");
//	            } catch (Exception e) {
//	                    throw new DeviceException("error getting HDF file name", e);
//	            }
//
//	            addMetadata(dataTree);
//	    }
	}

	@Override
	public void setCollectionTime(double time) {
		collectionTime = time;
		super.setCollectionTime(time);
		logger.debug("TangoMythenDetector: set collection time " + time);
	}

	/**
	 * Delay the start of acquisition if required. @see atPointStart()
	 * @throws DeviceException
	 */
	@Override
	public synchronized void collectData() throws DeviceException {
		logger.debug("TangoMythenDetector: Starting mythen acquisition");
		state = Detector.BUSY;
		notifyAll();
		// startAcq();
	}

	@Override
	public String readout() throws DeviceException {
		logger.debug("TangoMythenDetector: readout called");
		return getName();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	public void monitorLive(String panelName) {
		logger.debug("TangoMythenDetector: panel name is: " + panelName);
		plotPanelName = panelName;
		updateFrameClients(lastFrame, currentFrame);
	}

	public void monitorStop() {
		plotPanelName = null;
	}

	private int getScanNumber() {
		String beamline;
		int scanNumber = 0;
		try {
			Metadata metadata = GDAMetadataProvider.getInstance();
			beamline = metadata.getMetadataValue("instrument", "gda.instrument", null);
			// If the beamline name isn't set then default to 'base'.
			if (beamline == null) {
				// If the beamline name is not set then use 'base'
				beamline = "base";
			}
			NumTracker numTracker = new NumTracker(beamline);
			scanNumber = numTracker.getCurrentFileNumber();
		} catch (Exception ex) {
			logger.error("Error returning scan number");
		}
		return scanNumber;
	}

	private void updateFrameClients(int lastFrameUFC, int currentFrameUFC) {
		// logger.debug("TangoMythenDetector(): update frame client");
		try {
			double[] data = (double[])readLastImage();
			int[] dims = getDataDimensions();
			Dataset ds = DatasetFactory.createFromObject(data, dims);
			ds.setName("Mythen");
			plotData(plotPanelName, ds);
		} catch (Exception e) {
			logger.error("TangoMythenDetector: Exception preparing or sending live data: ", e.getMessage());
		}
		lastFrame = currentFrameUFC;
	}

	private void plotData(String panel, Dataset ds) {
		ds.squeeze();
		try {
			switch (ds.getShape().length) {
			case 1:
				SDAPlotter.plot(panel, ds);
				break;
			case 2:
				SDAPlotter.imagePlot(panel, ds);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("TangoMythenDetector: plotData() could not plot", e.getMessage());
		}
	}

	@Override
	public synchronized void run() {
		while (true) {
			try {
				logger.debug("TangoMythenDetector: runner() sleeping");
				wait();
				logger.debug("TangoMythenDetector: runner() awake");
				state = Detector.BUSY;
				for (int i=0; i<100; i++) {
					waitDouble(collectionTime * 10);
					updateFrameClients(lastFrame, currentFrame);
				}
			} catch (InterruptedException e) {
				logger.error("TangoMythenDetector: runner() exception " + e.getMessage());
			} catch (Exception e) {
				logger.error("TangoMythenDetector: runner() exception " + e.getMessage());
			} finally {
				state = Detector.IDLE;
			}
		}
	}

	private void waitDouble(double milliSeconds) throws InterruptedException {
		double mS = Math.floor(milliSeconds);
		double nS = (milliSeconds - mS) * 1.0E6;
		synchronized (this) {
			wait((int) mS, (int) nS);
		}
	}
}
