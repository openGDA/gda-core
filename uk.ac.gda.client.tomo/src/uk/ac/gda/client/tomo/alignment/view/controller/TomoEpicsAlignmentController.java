/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.controller;

import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.TiffFileInfo;

/**
 * Class to control the EPICS Tomo alignment GUI. The EPICS interface can be navigated by going to launcher -> Beamlines
 * -> I12 -> Hardware Status -> Tomography alignment
 */
public class TomoEpicsAlignmentController implements InitializingBean {

	// Expect these to be with the camera and not with the Tomo Epics alignment controller
	// But on asking Ulrik - these come from an independent IOC which is attached to the Tomo epics alignment IOCs
	private static final String BL12I_EA_CAM_01_ST1_CENTROID_Y_RBV = "BL12I-EA-CAM-01:ST1:CentroidY_RBV";

	private static final String BL12I_EA_CAM_01_ST1_CENTROID_X_RBV = "BL12I-EA-CAM-01:ST1:CentroidX_RBV";

	private static final String BL12I_EA_CAM_01_ST1_CENTROID_THRESHOLD = "BL12I-EA-CAM-01:ST1:CentroidThreshold";

	private static final String BL12I_EA_CAM_01_ROI1_SIZE_Y = "BL12I-EA-CAM-01:ROI1:SizeY";

	private static final String BL12I_EA_CAM_01_ROI1_MIN_Y = "BL12I-EA-CAM-01:ROI1:MinY";

	// ------------------------------------------------------------------------
	private static final String FILE_STAT = "FileStat";

	private static final String FILE_NO = "FileNo";

	private static final String FILE_NAME = "FileName";

	private static final String FILE_PATH = "FilePath";

	private static final String SCAN_EXSC = "SCAN.EXSC";

	private static final String SCAN_D01DA = "SCAN.D01DA";

	private static final String SCAN_D02DA = "SCAN.D02DA";

	private static final String YRANGE = "YRANGE";

	private static final String XRANGE = "XRANGE";

	private static final String SCANPARM_GO = "SCANPARM.GO";

	private static final String SCAN_SMSG = "SCAN.SMSG";

	private static final String SCAN_FAZE = "SCAN.FAZE";

	private static final String SCAN_P1DV = "SCAN.P1DV";

	private static final String SCANPARM_NP = "SCANPARM.NP";

	private static final String SCANPARM_SP = "SCANPARM.SP";

	private static final String CONFALIGN = "CONFALIGN";

	private static final String CAPTFLAT = "CAPTFLAT";

	private static final String BGND_FILE_STAT = "BgndFileStat";

	private static final String INITIALISE = "INITIALISE";

	private static final String BACKGROUNDFILE = "BackgroundFile";

	private HashSet<IStatusListener> statusListeners;

	private HashSet<IUpdateListener> updateListeners;

	private static final Logger logger = LoggerFactory.getLogger(TomoEpicsAlignmentController.class);

	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	protected Map<String, Channel> channelMap;
	protected IPVProvider pvProvider;
	protected String basePVName;

	private P1DVMonitorListener p1dvMonitorListener;

	private CentroidXMonitorListener centroidXMonitorListener;

	private CentroidYMonitorListener centroidYMonitorListener;

	private FazeMonitorListener fazeMonitorListener;

	private ShortMsgMonitorListener shortMsgMonitorListener;

	public interface IStatusListener {
		public void updateError(FLAT_STATUS status);
	}

	public interface IUpdateListener {

		public void updateCentroidThreshold(double centroidThreshold);

		public void updateP1DV(double d);

		public void updateFaze(short s);

		public void updateCentroidX(double d);

		public void updateCentroidY(double d);

		public void updateShortMsg(String string);

		public void operationFailed(String msg);

		public void updateScanProcessed(IProgressMonitor monitor, double xRange, double yRange, int[] xPoints,
				int[] yPoints);
	}

	public enum FLAT_STATUS {
		UNCONFIGURED("unconfigure"), FILE_OK("file ok"), FILE_NOT_READABLE("file not readable");
		private final String msg;

		@Override
		public String toString() {
			return msg;
		}

		FLAT_STATUS(String msg) {
			this.msg = msg;

		}

		public static FLAT_STATUS getEnum(String bgndFileStat) {
			for (FLAT_STATUS status : values()) {
				if (status.equals(bgndFileStat)) {
					return status;
				}
			}
			return null;
		}
	}

	public TomoEpicsAlignmentController() {
		channelMap = new HashMap<String, Channel>();
		statusListeners = new HashSet<TomoEpicsAlignmentController.IStatusListener>();
		p1dvMonitorListener = new P1DVMonitorListener();
		centroidXMonitorListener = new CentroidXMonitorListener();
		centroidYMonitorListener = new CentroidYMonitorListener();
		fazeMonitorListener = new FazeMonitorListener();
		shortMsgMonitorListener = new ShortMsgMonitorListener();

	}

	public boolean addStatusListener(IStatusListener listener) {
		return this.statusListeners.add(listener);
	}

	public boolean removeStatusListener(IStatusListener listener) {
		return this.statusListeners.remove(listener);
	}

	public boolean addUpdateListener(IUpdateListener listener) {
		return this.updateListeners.add(listener);
	}

	public boolean removeUpdateListener(IUpdateListener listener) {
		return this.updateListeners.remove(listener);
	}

	@SuppressWarnings("unused")
	public void setInitialise(IProgressMonitor monitor, int initialise) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(INITIALISE, null), initialise);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private synchronized void setStatus(FLAT_STATUS status) {
		if (!FLAT_STATUS.FILE_OK.equals(status)) {
			for (IStatusListener lis : statusListeners) {
				lis.updateError(status);
			}
		}
	}

	protected Channel getChannel(String pvElementName, MonitorListener ml, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName;
			if (pvProvider != null) {
				fullPvName = pvProvider.getPV(pvElementName);
			} else {
				fullPvName = basePVName + pvPostFix;
			}
			return createChannel(fullPvName, ml);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName, MonitorListener ml) throws CAException, TimeoutException,
			InterruptedException {
		logger.info(String.format("creating channel for :%1$s", fullPvName));
		Channel channel;
		synchronized (channelMap) {
			channel = channelMap.get(fullPvName);
			if (channel == null) {
				try {
					channel = EPICS_CONTROLLER.createChannel(fullPvName);
					if (ml != null) {
						EPICS_CONTROLLER.setMonitor(channel, ml);
					}
					int i = 0;
					while (Channel.CONNECTED != channel.getConnectionState()) {
						Thread.sleep(50);
						if (i > 10) {
							break;
						}
						i++;
					}
				} catch (CAException cae) {
					logger.warn("Problem creating channel", cae);
					throw cae;
				}
				if (Channel.CONNECTED == channel.getConnectionState()) {
					channelMap.put(fullPvName, channel);
				}
			}
		}
		return channel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public void setBackgroundFileName(String flatImageFullFileName) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(BACKGROUNDFILE, null), (flatImageFullFileName + '\0').getBytes());
	}

	// on callback, must check the file state field BL12I-EA-CAM-01:TOMO:BgndFileStat. Possible values for this PV are
	// "unconfigured", "file ok", "file not readable". Only proceed if and only if it returns "file ok" state, otherwise
	// throws error to users;
	public void setCapFlat(int value) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(CAPTFLAT, null), value, new PutListener() {

			@Override
			public void putCompleted(PutEvent putEvent) {
				try {
					String bgndFileStat = EPICS_CONTROLLER.caget(getChannel(BGND_FILE_STAT, null));
					if (!FLAT_STATUS.FILE_OK.equals(bgndFileStat)) {
						setStatus(FLAT_STATUS.getEnum(bgndFileStat));
					}
				} catch (TimeoutException e) {
					logger.error("Error getting BGND_FILE_STAT", e);
				} catch (CAException e) {
					logger.error("Error getting BGND_FILE_STAT", e);
				} catch (InterruptedException e) {
					logger.error("Error getting BGND_FILE_STAT", e);
				} catch (Exception e) {
					logger.error("Error getting BGND_FILE_STAT", e);
				}
			}
		});
	}

	/**
	 * Tilt 22
	 * 
	 * @param minY
	 * @param maxY
	 * @throws CAException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public void setYLimits(int minY, int maxY) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(createChannel(BL12I_EA_CAM_01_ROI1_MIN_Y, null), minY);
		EPICS_CONTROLLER.caput(createChannel(BL12I_EA_CAM_01_ROI1_SIZE_Y, null), (maxY - minY));

		EPICS_CONTROLLER.caput(getChannel(CONFALIGN, null), minY, new PutListener() {

			@Override
			public void putCompleted(PutEvent arg0) {
				if (arg0.getStatus() == CAStatus.PUTFAIL) {
					operationFailed("Tilt Configure Processing");
				} else {
					updateCentroidThreshold();
				}

			}
		});

	}

	protected void updateCentroidThreshold() {
		for (IUpdateListener listener : updateListeners) {
			try {
				double centroidThreshold = EPICS_CONTROLLER.cagetDouble(createChannel(
						BL12I_EA_CAM_01_ST1_CENTROID_THRESHOLD, null));
				listener.updateCentroidThreshold(centroidThreshold);
			} catch (Exception e) {
				logger.error("Exception white getting threshold centroid value", e);
			}
		}
	}

	/**
	 * 18 - prepare input files for alignment processing
	 * 
	 * @param tiffFileInfo
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws CAException
	 */
	public void setInputFiles(TiffFileInfo tiffFileInfo) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(FILE_PATH, null), tiffFileInfo.getFilePath());
		EPICS_CONTROLLER.caput(getChannel(FILE_NAME, null), tiffFileInfo.getFileName());
		// Ensured that before scan start the number is always set to 0
		EPICS_CONTROLLER.caput(getChannel(FILE_NO, null), 0);

		String fileStat = EPICS_CONTROLLER.caget(getChannel(FILE_STAT, null));
		if (FLAT_STATUS.FILE_OK.equals(fileStat)) {
			throw new DeviceException("File stat - NOT OK");
		}

	}

	/**
	 * Tilt - 21
	 * 
	 * @param i
	 * @param j
	 * @throws CAException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public void calculatePositionOfSphereAndStartProcessing(IProgressMonitor monitor, int i, int j, final int numPos)
			throws CAException, InterruptedException, Exception {
		final SubMonitor progress = SubMonitor.convert(monitor);
		EPICS_CONTROLLER.caput(getChannel(SCANPARM_SP, null), i);
		EPICS_CONTROLLER.caput(getChannel(SCANPARM_NP, null), j);

		// just to add monitors to the below PVs
		EPICS_CONTROLLER.caget(getChannel(SCAN_P1DV, p1dvMonitorListener));
		EPICS_CONTROLLER.caget(getChannel(SCAN_FAZE, fazeMonitorListener));
		EPICS_CONTROLLER.caget(createChannel(BL12I_EA_CAM_01_ST1_CENTROID_X_RBV, centroidXMonitorListener));
		EPICS_CONTROLLER.caget(createChannel(BL12I_EA_CAM_01_ST1_CENTROID_Y_RBV, centroidYMonitorListener));
		EPICS_CONTROLLER.caget(getChannel(SCAN_SMSG, shortMsgMonitorListener));

		// Start processing - assuming put complete will return after the operation has completed -
		EPICS_CONTROLLER.caput(getChannel(SCANPARM_GO, null), 1, new PutListener() {

			@Override
			public void putCompleted(PutEvent arg0) {
				try {
					double xRange = EPICS_CONTROLLER.cagetDouble(getChannel(XRANGE, null));
					double yRange = EPICS_CONTROLLER.cagetDouble(getChannel(YRANGE, null));
					int[] xPoints = EPICS_CONTROLLER.cagetIntArray(getChannel(SCAN_D01DA, null), numPos);
					int[] yPoints = EPICS_CONTROLLER.cagetIntArray(getChannel(SCAN_D02DA, null), numPos);
					for (IUpdateListener listener : updateListeners) {
						listener.updateScanProcessed(progress.newChild(1), xRange, yRange, xPoints, yPoints);
					}
				} catch (TimeoutException e) {
					logger.error("Error getting tomo processed values", e);
				} catch (CAException e) {
					logger.error("Error getting tomo processed values", e);
				} catch (InterruptedException e) {
					logger.error("Error getting tomo processed values", e);
				} catch (Exception e) {
					logger.error("Error getting tomo processed values", e);
				}

			}
		});

	}

	public void abortOperation() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(SCAN_EXSC, null), 1, new PutListener() {
			@Override
			public void putCompleted(PutEvent arg0) {
				if (arg0.getStatus().equals(CAStatus.PUTFAIL)) {
					operationFailed("Abort Operation");
				}
			}
		});
	}

	protected void operationFailed(String msg) {
		for (IUpdateListener listener : updateListeners) {
			listener.operationFailed(msg);
		}
	}

	private class P1DVMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (IUpdateListener listener : updateListeners) {
					listener.updateP1DV(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class FazeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (IUpdateListener listener : updateListeners) {
					listener.updateFaze(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	private class CentroidXMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (IUpdateListener listener : updateListeners) {
					listener.updateCentroidX(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class CentroidYMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (IUpdateListener listener : updateListeners) {
					listener.updateCentroidY(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class ShortMsgMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isSTRING()) {
				for (IUpdateListener listener : updateListeners) {
					listener.updateShortMsg(((DBR_String) dbr).getStringValue()[0]);
				}
			}
		}
	}
}