/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
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

package gda.hrpd.scan;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.multichannelscaler.EpicsMcsSis3820;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.detector.multichannelscaler.Mca;
import gda.device.monitor.IonChamberBeamMonitor;
import gda.factory.Finder;
import gda.hrpd.data.MacDataProcessing;
import gda.hrpd.data.MacDataWriter;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.scan.EpicsTrajectoryScanController;
import gda.scan.Scan;
import gda.scan.Trajectory;
import gov.aps.jca.CAException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper class to the XPS controller based trajectory scan via EPICS interface. It implements the constant velocity
 * trajectory, supports up to 8 motor trajectories, and collects data from two MultiChannel scaler cards. The
 * implementation also supports beam monitor {@link gda.device.monitor.IonChamberBeamMonitor}: when beam is down, it
 * aborts the trajectory scan on XPS; when beam is back, it restarts the aborted scan if the original operation had not
 * be stopped manually. The default behaviours for each motors are:
 * <ol>
 * <li>Axis theta - depending on the number of parameters provided:
 * <ul>
 * <li>two input angle parameters - oscillation between the specified two positions;</li>
 * <li>one input angle - asymmetric or offset, synchronised theta-2theta scan, the input is the offset angle;</li>
 * <li>no input - symmetric or zero-offset, synchronised theta-2theta scan, where theta is always half or 2theta;</li>
 * </ul>
 * </li>
 * <li>Axis two-theta - the main constant velocity scan over 35 degree angle for a given start angle (0-13 deg) and
 * time;</li>
 * <li>the rest 6 axis - if present, constant velocity scan from start position to stop position;</li>
 * </ol>
 * The collected data is written to an ASCII file with metadata header using {@link gda.hrpd.data.MacDataWriter}.
 * 
 * @see gda.scan.Trajectory
 * @see gda.device.detector.multichannelscaler.EpicsMultiChannelScaler
 * @see gda.hrpd.data.MacDataWriter
 * @see gda.device.monitor.IonChamberBeamMonitor
 */
public class CVScan extends CVScanBase implements Scan {

	private static final long serialVersionUID = 6245061265060159179L;
	private static final Logger logger = LoggerFactory.getLogger(CVScan.class);
	private Trajectory traj1 = new Trajectory();
	private Trajectory traj2 = new Trajectory();
	private Trajectory traj3 = new Trajectory();
	private Trajectory traj4 = new Trajectory();
	private Trajectory traj5 = new Trajectory();
	private Trajectory traj6 = new Trajectory();
	private Trajectory traj7 = new Trajectory();
	private Trajectory traj8 = new Trajectory();
	Vector<ScanObject> allScanObjects = new Vector<ScanObject>();
	private double totaltime;
	// the actual tracjectory path data
	private double[] m1actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m2actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m3actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m4actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m5actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m6actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m7actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m8actual = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private double[] m2error = new double[EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	// flag to mark if the motor participate in constant velocity scan or not
	private boolean m1move = false;
	private boolean m2move = false;
	private boolean m3move = false;
	private boolean m4move = false;
	private boolean m5move = false;
	private boolean m6move = false;
	private boolean m7move = false;
	private boolean m8move = false;
	/**
	 * actual pulse number i.e. number of detector data points
	 */
	private int actualpulses;
	/**
	 * detector data store
	 */
	private int[][] data = new int[2 * EpicsMcsSis3820.MAX_NUMBER_MCA][EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER];
	private int totalChannelNumber;
	private MacDataProcessing mdp = MacDataProcessing.getInstance();

	static Finder finder = Finder.getInstance();
	// i11 default MAC detectors
	private EpicsMultiChannelScaler[] mcs = new EpicsMultiChannelScaler[] {
			(EpicsMultiChannelScaler) finder.find("mcs1"), (EpicsMultiChannelScaler) finder.find("mcs2") };
	// default Epics Trajectory Scan Controller
	private EpicsTrajectoryScanController controller = (EpicsTrajectoryScanController) finder
			.find("epicsTrajectoryScanController");
	private IonChamberBeamMonitor bm = (IonChamberBeamMonitor) finder.find("bm");
	/**
	 * External trajectory scan status flag
	 */
	static public volatile boolean aborted = false;
	private volatile boolean runScanControlThread = false;
	private Vector<Scannable> parentScannables = new Vector<Scannable>();
	// private double start;
	private boolean synchronous = false;

	/**
	 * the fix scan range for 2theta. One need not to scan more than this angle range to have a full 140 degree profile.
	 */
	public static final double SCAN_RANGE = 46.667;

	/**
	 * The miminum scan range for two-theta. No rebin would be possible if 2theta scan less than this value.
	 */
	public static final double MIN_SCAN_RANGE = 30.0;
	/**
	 * MAC Offset - detector is at -8 deg
	 */
	public double tth_offset = 0.0; // 1st MAC stage's 1
	/**
	 * theta offset that depends on 2theta
	 */
	public double theta_offset = tth_offset / 2;

	/**
	 * Constructor for single motor and single detector trajectory scan,using specified data handler.
	 * 
	 * @param motor
	 * @param start
	 * @param end
	 * @param time
	 * @param detector
	 * @param datahandler
	 */
	public CVScan(Scannable motor, double start, double end, double time, Detector detector, DataWriter datahandler) {
		mcs[0] = (EpicsMultiChannelScaler) detector;
		totalChannelNumber = mcs[0].getMcaList().size();
		setDataWriter(datahandler);
		check2ThetaVaules(motor, start, end);
		setUp(motor, start, end, time, null, 0, 0);
	}

	/**
	 * constructor supports for single motor and multiple detector trajectory scan, using specified data handler
	 * 
	 * @param motor
	 * @param start
	 * @param end
	 * @param time
	 * @param detectors
	 * @param datahandler
	 */
	public CVScan(Scannable motor, double start, double end, double time, String[] detectors, DataWriter datahandler) {
		mcs = new EpicsMultiChannelScaler[detectors.length];
		for (int i = 0; i < detectors.length; i++) {
			mcs[i] = (EpicsMultiChannelScaler) finder.find(detectors[i]);
		}
		setDataWriter(datahandler);
		check2ThetaVaules(motor, start, end);
		setUp(motor, start, end, time, null, 0, 0);
	}

	/**
	 * constructor supports for single motor and multiple detector trajectory scan, using specified data handler
	 * 
	 * @param motor
	 * @param start
	 * @param end
	 * @param time
	 * @param detectors
	 * @param datahandler
	 */
	public CVScan(String motor, double start, double end, double time, String[] detectors, DataWriter datahandler) {
		mcs = new EpicsMultiChannelScaler[detectors.length];
		for (int i = 0; i < detectors.length; i++) {
			mcs[i] = (EpicsMultiChannelScaler) finder.find(detectors[i]);
		}
		setDataWriter(datahandler);
		Scannable motor1;
		if ((motor1 = (Scannable) finder.find(motor)) != null) {
			check2ThetaVaules(motor1, start, end);
		}
		setUp(motor1, start, end, time, null, 0, 0);

	}

	/**
	 * constructor supports for single motor with default detectors for I11 only.
	 * 
	 * @param motor
	 * @param start
	 * @param time
	 */
	public CVScan(Scannable motor, double start, double time) {
		double tthStart = check2Theta(motor, start);
		double tthStop = (start + SCAN_RANGE);
		setUp(motor, tthStart, tthStop, time, null, 0, 0);
	}

	/**
	 * constructor supports for single motor and multiple detector trajectory scan, using specified data handler
	 * 
	 * @param motor
	 * @param start
	 * @param end
	 * @param time
	 * @param detectors
	 * @param datahandler
	 */
	public CVScan(Scannable motor, double start, double end, double time, Detector[] detectors, DataWriter datahandler) {
		mcs = new EpicsMultiChannelScaler[detectors.length];
		for (int i = 0; i < detectors.length; i++) {
			mcs[i] = (EpicsMultiChannelScaler) detectors[i];
		}
		setDataWriter(datahandler);
		check2ThetaVaules(motor, start, end);
		setUp(motor, start, end, time, null, 0, 0);
	}

	/**
	 * constructor supports for single motor and multiple detector trajectory scan, using default data handler
	 * 
	 * @param motor
	 * @param start
	 * @param end
	 * @param time
	 * @param detectors
	 */
	public CVScan(Scannable motor, double start, double end, double time, Detector... detectors) {
		this(motor, start, end, time, detectors, null);
	}

	/**
	 * convenient constructor which only needs to specify motor and total time for constant velocity scan. The motor
	 * angle ranges from 0 to 35 degree. Default names for detector must be "mcs1" and/or "mcs2". Otherwise detectors
	 * will be null.
	 * 
	 * @param motor
	 * @param time
	 */
	public CVScan(Scannable motor, double time) {
		this(motor, -7.0, time);
	}

	/**
	 * constructor supports for single motor with default detectors for I11 only.
	 * 
	 * @param motor1
	 * @param start1
	 * @param time
	 * @param motor2
	 * @param start2
	 * @param end2
	 */
	public CVScan(Scannable motor1, double start1, double time, Scannable motor2, double start2, double end2) {
		double tthStart = check2Theta(motor1, start1);
		double tthStop = (tthStart + SCAN_RANGE);

		checkMotor2(motor2, start2, end2);
		setUp(motor1, tthStart, tthStop, time, motor2, start2, end2);
	}

	/**
	 * constructor for synchronous theta-2theta constant velocity scan with theta offset.
	 * 
	 * @param motor1
	 * @param start1
	 * @param time
	 * @param motor2
	 * @param offset
	 */
	public CVScan(Scannable motor1, double start1, double time, Scannable motor2, double offset) {
		double tthStart = check2Theta(motor1, start1);
		double tthStop = (tthStart + SCAN_RANGE);
		double thetaStart = 0;
		double thetaStop = 0;
		checkMotor2(motor2, offset);
		if (motor2.getName().equalsIgnoreCase("theta")) {
			thetaStart = tthStart / 2 + offset;
			thetaStop = tthStop / 2 + offset;
		}
		setUp(motor1, tthStart, tthStop, time, motor2, thetaStart, thetaStop);
	}

	/**
	 * constructor for synchronous theta-2theta constant velocity scan.
	 * 
	 * @param motor1
	 * @param start1
	 * @param time
	 * @param motor2
	 */
	public CVScan(Scannable motor1, double start1, double time, Scannable motor2) {
		double tthStart = check2Theta(motor1, start1);
		double tthStop = (tthStart + SCAN_RANGE);
		double thetaStart = 0;
		double thetaStop = 0;
		checkMotor2(motor2);
		if (motor2.getName().equalsIgnoreCase("theta")) {
			thetaStart = tthStart / 2;
			thetaStop = tthStop / 2;
		}
		setUp(motor1, tthStart, tthStop, time, motor2, thetaStart, thetaStop);
	}

	/**
	 * constructor supports for 2 motors and multiple detectors trajectory scan, using specified data handler
	 * 
	 * @param motor1
	 * @param start1
	 * @param end1
	 * @param time
	 * @param motor2
	 * @param start2
	 * @param end2
	 * @param detectors
	 * @param datahandler
	 */
	public CVScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
			double end2, Detector[] detectors, DataWriter datahandler) {
		mcs = new EpicsMultiChannelScaler[detectors.length];
		for (int i = 0; i < detectors.length; i++) {
			mcs[i] = (EpicsMultiChannelScaler) finder.find(detectors[i].getName());
		}
		setDataWriter(datahandler);
		check2ThetaVaules(motor1, start1, end1);
		checkMotor2(motor2, start2, end2);
		setUp(motor1, start1, end1, time, motor2, start2, end2);
	}

	/**
	 * constructor supports for two motors and multiple detectors trajectory scan, using default data handler
	 * 
	 * @param motor1
	 * @param start1
	 * @param end1
	 * @param time
	 * @param motor2
	 * @param start2
	 * @param end2
	 * @param detectors
	 */
	public CVScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
			double end2, Detector... detectors) {
		this(motor1, start1, end1, time, motor2, start2, end2, detectors, null);
	}

	/**
	 * constructor supports for two motors and multiple detectors trajectory scan, using specified data handler
	 * 
	 * @param motor1
	 * @param start1
	 * @param end1
	 * @param time
	 * @param motor2
	 * @param start2
	 * @param end2
	 * @param detectors
	 */
	public CVScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
			double end2, String[] detectors) {
		this(motor1, start1, end1, time, motor2, start2, end2, (Detector) finder.find(detectors[0]), (Detector) finder
				.find(detectors[1]));
	}

	// ============up to 8 motors ===========================
	/**
	 * constructor supports for up to 8 motors and multiple Epics multi-channel Scaler detectors trajectory scan, using
	 * specified data handler
	 * 
	 * @param motors
	 * @param starts
	 * @param ends
	 * @param time
	 * @param detectors
	 * @param datahandler
	 */
	public CVScan(Scannable[] motors, double[] starts, double[] ends, double time, Detector[] detectors,
			DataWriter datahandler) {
		if (motors.length != starts.length || motors.length != ends.length || starts.length != ends.length) {
			throw new IllegalArgumentException(
					"Input error: Number of motors must be the same as number of start points or stop points.");
		}
		if (motors.length > 8) {
			throw new IllegalArgumentException(
					"Input error: EPICS Trajectory scan only support up to 8 motor on XPS controller.");
		}

		for (int i = 0; i < motors.length; i++) {
			if (motors[i] != null) {
				allScannables.add(motors[i]);
			}
		}
		mcs = new EpicsMultiChannelScaler[detectors.length];
		Finder finder = Finder.getInstance();
		for (int i = 0; i < detectors.length; i++) {
			mcs[i] = (EpicsMultiChannelScaler) finder.find(detectors[i].getName());
			if (mcs[i] != null) {
				allScannables.add(mcs[i]);
			}
			totalChannelNumber += mcs[i].getMcaList().size();
		}
		for (int i = 0; i < motors.length; i++) {
			if (motors[i] != null) {
				allScannables.add(motors[i]);
				allScanObjects.add(new ScanObject(motors[i], starts[i], ends[i], time));
			}
		}
		if (datahandler != null) {
			setDataWriter(datahandler);
			if (getDataWriter() instanceof MacDataWriter)
				((MacDataWriter) getDataWriter()).configure();
		} else {
			if (getDataWriter() != null) {
				if (getDataWriter() instanceof MacDataWriter)
					((MacDataWriter) getDataWriter()).configure();
			}
		}
		totaltime = time;

		super.setUp();
	}

	/**
	 * convenient constructor supports for up to 8 motors and multiple EPICS multi-channel scaler detectors trajectory
	 * scan, using default data handler
	 * 
	 * @param motors
	 * @param starts
	 * @param ends
	 * @param time
	 * @param detectors
	 */
	public CVScan(Scannable[] motors, double[] starts, double[] ends, double time, Detector[] detectors) {
		this(motors, starts, ends, time, detectors, null);
	}

	/**
	 * setup and register detectors, motors, and data handler for 2 motor constant velocity scan.
	 * 
	 * @param motor1
	 * @param start1
	 * @param time
	 * @param motor2
	 * @param start2
	 * @param end2
	 * @param stop1
	 */
	private void setUp(Scannable motor1, double start1, double stop1, double time, Scannable motor2, double start2,
			double end2) {
		for (int i = 0; i < mcs.length; i++) {
			if (mcs[i] != null) {
				allScannables.add(mcs[i]);
			}
			totalChannelNumber += mcs[i].getMcaList().size();
		}
		if (motor1 != null) {
			allScannables.add(motor1);
			allScanObjects.add(new ScanObject(motor1, start1, stop1, time));
		}
		if (motor2 != null) {
			allScannables.add(motor2);
			allScanObjects.add(new ScanObject(motor2, start2, end2, time));
		}
		totaltime = time;
		super.setUp();
	}

	/**
	 * @param motor2
	 * @throws IllegalArgumentException
	 */
	private void checkMotor2(Scannable motor2, double... args) throws IllegalArgumentException {
		if (motor2.getName().equalsIgnoreCase("theta")) {
			switch (args.length) {
			case 0:
				this.synchronous = true;
				logger.info("symmetric synchronised theta-2theta scan.");
				break;
			case 1:
				this.synchronous = true;
				logger.info("asymmetric synchronised theta-2theta scan with offset {}.", args[0]);
				break;
			case 2:
				this.synchronous = false;
				logger.info("rocking/oscilating theta position continuously during 2theta cvscan between {} and {}.",
						args[0], args[1]);
				break;
			default:
				logger.error("The 'theta' can not take more than 2 inputs in cvscan.");
				throw new IllegalArgumentException(
						"Maximum 2 arguments are allowed after 'theta': start position and stop position.");
			}
		} else if (motor2.getName().equalsIgnoreCase("delta")) {
			logger.error("cvscan 'tth' with 'delta' is not supported.");
			throw new IllegalArgumentException("cvscan 'tth' with 'delta' is not supported.");
		} else if (motor2.getName().equalsIgnoreCase("spos")) {
			if (args.length == 2) {
				logger.info("moving sample position synctronously during 2theta cvscan.");
			} else {
				logger.error("Two arguments are required after 'spos': start position and stop position.");
				throw new IllegalArgumentException(
						"Two arguments are required after 'spos': start position and stop position.");
			}
		} else {
			if (args.length > 0) {
				logger.error("cvscan does not support '{}' device", motor2.getName());
				throw new IllegalArgumentException("cvscan does not support '" + motor2.getName() + "' device");
			}
			logger.info("Collect metadata from device '{}'", motor2.getName());
			// #TODO implement metadata collection items as header
		}
	}

	/**
	 * checks if the start angle for 2-theta is within limits, if not, replace them with limit values
	 * 
	 * @param motor
	 * @param start
	 * @return start position
	 */
	private double check2Theta(Scannable motor, double start) {
		if (motor.getName().equalsIgnoreCase("tth")) {
			if (start > -6.0) {
				start = -6.0;
				logger.warn("{} - start angle too big, set to maximum -6.0 degree.", motor.getName());
				JythonServerFacade.getInstance().print(
						"START ANGLE TOO HIGH, SCAN SET TO START FROM THE MAXIMUM ANGLE ALLOWED AT -6.0 DEGREE.");
			} else if (start < -8.0) {
				start = -8.0;
				logger.warn("{} - start angle too small, set to minimum -8.0 degree", motor.getName());
				JythonServerFacade.getInstance().print(
						"START ANGLE TOO LOW, SCAN SET TO START FROM THE MINIMUM ANGLE ALLOWED AT -8.0 DEGREE.");
			}
		} else {
			throw new IllegalArgumentException(
					"The first scannable argument for 'cvscan' must be 'tth' for two-theta motor.");
		}
		return start;
	}

	/**
	 * checks if the start and end angles for 2-theta is within limits, if not, send message.
	 * 
	 * @param motor
	 * @param start
	 */
	private void check2ThetaVaules(Scannable motor, double start, double end) {
		if (motor.getName().equalsIgnoreCase("tth")) {
			if (start > -6.0) {
				logger.error("maximum start angle for '{}' is -6.0 degree.", motor.getName());
				throw new IllegalArgumentException("START ANGLE TOO HIGH, MAXIMUM -6.0 DEGREE.");
			} else if (start < -8.0) {
				logger.error("minimum start angle for '{}' is -8.0 degree", motor.getName());
				throw new IllegalArgumentException("START ANGLE TOO LOW, MINIMUM -8.0 DEGREE.");
			}
			if ((end - start) > SCAN_RANGE) {
				logger.error("maximum stop angle for '{}' is {} degree.", motor.getName(), start + SCAN_RANGE);
				throw new IllegalArgumentException("STOP ANGLE TOO HIGH, MAXIMUM " + start + SCAN_RANGE + " DEGREE.");
			} else if ((end - start) < MIN_SCAN_RANGE) {
				logger.error("minimum stop angle for '{}' is {} degree.", motor.getName(), start + MIN_SCAN_RANGE);
				throw new IllegalArgumentException("STOP ANGLE TOO LOW, MINIMUM " + start + MIN_SCAN_RANGE + " DEGREE.");
			}
		} else {
			throw new IllegalArgumentException(
					"The first scannable argument for 'cvscan' must be 'tth' for two-theta motor.");
		}
	}

	/**
	 * used to pass outer or parent scannables on to data writer when cvscan is used as nested or child scan.
	 * 
	 * @param scannableList
	 */
	public void addParentScannables(Vector<Scannable> scannableList) {
		for (Scannable s : scannableList) {
			parentScannables.add(s);
		}
	}

	@Override
	public void doCollection() throws Exception {
		logger.info("Starting constant velocity scan.");

		if (getChild() == null) {
			// call the atPointStart method of all the scannables
			for (Scannable scannable : this.allScannables) {
				scannable.atPointStart();
			}
			for (Scannable scannable : this.allDetectors) {
				scannable.atPointStart();
			}
		}
		collectData();
		// if the trajectory is already running, stop it before restarting is required for detector and XPS synchronisation.
		// as we need to re-sync the data collection trigger pulses from scaler card to XPS
		if (controller.getExecute() == 1) {
			controller.stop();
		}
		Thread.sleep(1000);
		controller.execute();
		Thread.sleep(1000);
		while (controller.getExecute() != 0) {
			if (aborted) {
				break;
			}
			// we do not want to wait here if this cvscan is aborted by beam drop
			checkForInterruption(false);
			if (!aborted) {
				Thread.sleep(5000);
				String msg = controller.getExecuteMessage();
				if (msg.equalsIgnoreCase("")) {
					JythonServerFacade.getInstance().print("Move to start point, please wait...");
				} else {
					JythonServerFacade.getInstance().print(msg);
				}
			}
		}
		if (!aborted) {
			Thread.sleep(1000);
			if (controller.getExecuteStatusFromEpics() != 1) {
				throw new IllegalStateException("Execution NOT succeed; Status : "
						+ controller.getExecuteStatus().toString() + "; Message : " + controller.getExecuteMessage());
			}
		}
		if (getChild() == null) {
			// call the atPointEnd method of all the scannables
			for (Scannable scannable : this.allScannables) {
				scannable.atPointEnd();
			}
			for (Scannable scannable : this.allDetectors) {
				scannable.atPointEnd();
			}
		}
		if (!aborted) {
			// terminate beam monitor thread
			runScanControlThread = false;
			checkForInterruptionIgnorBeamDrop();

			// read the actual trajectory path
			controller.read();
			Thread.sleep(1000);
			while (controller.getRead() != 0) {
				checkForInterruptionIgnorBeamDrop();
				Thread.sleep(1000);
				JythonServerFacade.getInstance().print(
						"Reading the acutal trajectory from XPS to EPICS, please wait...");
			}

			if (controller.getReadStatusFromEpics() != 1) {
				throw new IllegalStateException("Trajectory execution NOT succeed."
						+ controller.getReadStatus().toString() + " Message : " + controller.getReadMessage());
			}
			checkForInterruptionIgnorBeamDrop();

			if (isChild() && !interrupted) {
				String filename = saveRawData();
				if (mdp.isEnabled()) {
					String rebinnedDatafile = mdp.rebinning(filename);
					if (mdp.isRebinCompleted()) {
						mdp.plotData(rebinnedDatafile);
					}
				}
			}
		} else {
			// if this cvscan is ABORTED
			checkForInterruption(true); // will wait here for beam
			JythonServerFacade.getInstance().print("Restart the last CVScan.");
			logger.info("Restart the last CVScan.");
			doCollection(); // beam recovered then restart the last aborted
			// cvscan.
		}
	}

	/**
	 * Using the GDA rule to get the next file name, without incrementing file number
	 * 
	 * @return next file name
	 */
	public String getNextFileNameUrl() {
		NumTracker runs = null;

		try {
			runs = new NumTracker("tmp");
		} catch (IOException e) {
			logger.error("Could not instantiate NumTracker. {}",e.getMessage(), e);
			return ("ERROR: Could not instantiate NumTracker." + e.toString());
		}

		long nextNum = runs.getCurrentFileNumber() + 1;

		// Get the locatation of the GDA users directory and compose a data file
		// full path
		String path = PathConstructor.createFromDefaultProperty();

		String fileName = path + System.getProperty("file.separator") + nextNum + ".dat";
		return fileName;
	}

	/**
	 * This method should be called before every task in the doCollection method of a concrete scan class which takes a
	 * long period of time (e.g. collecting data, moving a motor).
	 * <P>
	 * If, since the last time this method was called, interrupted was set to true, an interrupted exception is thrown
	 * which should be used by the scan to end its run method.
	 * <P>
	 * If pause was set to true, then this method will loop endlessly and wait until paused has been set to false if you
	 * set wait argument to true. Otherwise it will simply return.
	 * <P>
	 * 
	 * @param wait
	 * @throws InterruptedException
	 */
	public void checkForInterruption(boolean wait) throws InterruptedException {
		try {
			if (paused & !interrupted) {
				JythonServerFacade.getInstance().setScanStatus(Jython.PAUSED);
				if (!aborted) {
					JythonServerFacade.getInstance().print(
							"Current constant velocity scan will continue on XPS server until it completes.");
					JythonServerFacade.getInstance().print("To stop CVScan, press Halt or StopAll");
				} else {
					JythonServerFacade.getInstance().print(
							"Current constant velocity scan is aborted and will restart once beam recovered.");
					logger.info("Current constant velocity scan is aborted and will restart once beam recovered.");
					JythonServerFacade.getInstance().print("To stop CVScan, press Halt or StopAll");

				}
				if (wait) {
					while (paused || aborted) {
						Thread.sleep(1000);
					}
				}
				JythonServerFacade.getInstance().setScanStatus(Jython.RUNNING);
			}
		} catch (InterruptedException ex) {
			interrupted = true;
		}

		if (interrupted) {

			// terminate Beam monitor thread on interrupt
			runScanControlThread = false;
			// reset the abort scan flag on interrupt
			aborted = false;

			JythonServerFacade.getInstance().setScanStatus(Jython.IDLE);
			throw new InterruptedException();
		}
	}

	/**
	 * only concern with manual pause and interruption.
	 * 
	 * @throws InterruptedException
	 */
	public void checkForInterruptionIgnorBeamDrop() throws InterruptedException {
		try {
			if (paused & !interrupted) {
				JythonServerFacade.getInstance().setScanStatus(Jython.PAUSED);
				while (paused) {
					Thread.sleep(1000);
				}
				JythonServerFacade.getInstance().setScanStatus(Jython.RUNNING);
			}
		} catch (InterruptedException ex) {
			interrupted = true;
		}

		if (interrupted) {

			// terminate cvscan Beam monitor thread on interrupt
			runScanControlThread = false;
			// reset the abort scan flag on interrupt
			aborted = false;

			JythonServerFacade.getInstance().setScanStatus(Jython.IDLE);
			throw new InterruptedException();
		}
	}

	@Override
	public void prepareForCollection() throws Exception {
		// display the filename or file number that data will be saved to when
		// completed
		JythonServerFacade.getInstance().print(
				"Raw data will be saved to file: " + getNextFileNameUrl()
						+ " When constant velocity scan completed successfully.");
		JythonServerFacade.getInstance().print("setting up detectors ......");
		if (!isChild()) {
			super.prepareForCollection();
		} else {
			super.createScanDataPointPipeline();
		}
		JythonServerFacade.getInstance().print("setting up motion axises and trajectory......");
		// make sure all move axis are de-activated first
		controller.setM1Move(false);
		controller.setM2Move(false);
		controller.setM3Move(false);
		controller.setM4Move(false);
		controller.setM5Move(false);
		controller.setM6Move(false);
		controller.setM7Move(false);
		controller.setM8Move(false);
		checkForInterruptionIgnorBeamDrop();
		// activate the participating axis now
		for (ScanObject so : allScanObjects) {
			if (so.getScannable().getName().equalsIgnoreCase(controller.getM1axis())) {
				controller.setM1Move(true);
				if (synchronous) {
					controller.setM1Traj(traj1.defineCVPath(so.getStart() - theta_offset, so.getStop() - theta_offset,
							so.getTotaltime()));
				} else {
					controller.setM1Traj(traj1.defineOscillationPath(so.getStart(), so.getStop(), so.getTotaltime()));
				}
				m1move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM2axis())) {
				controller.setM2Move(true);
				double[] tthpath = traj2.defineCVPath(so.getStart() - tth_offset, so.getStop() - tth_offset, so
						.getTotaltime());
				JythonServerFacade.getInstance().print(String.valueOf(tthpath.length));
				controller.setM2Traj(tthpath);
				m2move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM3axis())) {
				controller.setM3Move(true);
				controller.setM3Traj(traj3.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m3move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM4axis())) {
				controller.setM4Move(true);
				controller.setM4Traj(traj4.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m4move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM5axis())) {
				controller.setM5Move(true);
				controller.setM5Traj(traj5.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m5move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM6axis())) {
				controller.setM6Move(true);
				controller.setM6Traj(traj6.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m6move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM7axis())) {
				controller.setM7Move(true);
				controller.setM7Traj(traj7.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m7move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(controller.getM8axis())) {
				controller.setM8Move(true);
				controller.setM8Traj(traj8.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m8move = true;
			} else {
				logger.warn("EPICS trajectory scan is not setup for scannable {}", so.getScannable().getName());
				throw new IllegalArgumentException("cvscan is not configured for this scannable "
						+ so.getScannable().getName());
			}
			checkForInterruptionIgnorBeamDrop();
		}

		controller.setNumberOfElements((int) traj2.getElementNumbers());
		controller.setNumberOfPulses((int) traj2.getPulseNumbers());
		controller.setStartPulseElement((int) traj2.getStartPulseElement());
		controller.setStopPulseElement((int) traj2.getStopPulseElement());
		Thread.sleep(100);
		if (controller.getStopPulseElement() != (int) traj2.getStopPulseElement()) {
			controller.setStopPulseElement((int) traj2.getStopPulseElement());
		}
		controller.setTime(traj2.getTotalTime());
		boolean isFirst = true;
		controller.build();
		Thread.sleep(1000);
		while (controller.getBuild() != 0) {
			if (isFirst) {
				JythonServerFacade.getInstance().print("building trajectory ......");
				isFirst = false;
			}
			checkForInterruptionIgnorBeamDrop();
		}
		Thread.sleep(2000);
		checkForInterruptionIgnorBeamDrop();
		if (controller.getBuildStatusFromEpics() != 1) {
			throw new IllegalStateException("Trajectory build NOT succeed; Status : " + controller.getBuildStatus()
					+ "; Message : " + controller.getBuildMessage());
		}
		if (bm.isMonitorOn()) {
			// if beam monitor is not on, cvscan control thread will not be started.
			// cvscan will finish its trajectory on XPS as before.
			runScanControlThread = true;
			Thread bmt = uk.ac.gda.util.ThreadManager.getThread(new ScanControl(), "cvscanBeamMonitor");
			bmt.start();
		} else {
			aborted = false;
		}
	}

	private class ScanControl implements Runnable {

		@Override
		public void run() {

			while (bm.isMonitorOn() && runScanControlThread) {
				if (!bm.isBeamOn() && !aborted) {
					try {
						aborted = true;
						JythonServerFacade.getInstance().print("BEAM OFF - abort current scan.");
						// abort EPICS trajectory scan on XPS controller
						controller.stop();
						// ScanBase.paused = true;

						for (Detector detector : allDetectors) {
							detector.stop();
						}
						logger.warn("Beam OFF - Current CVScan is aborted");
					} catch (Exception e) {
						logger.error("Failed to abort or stop current XPS trajectory scan or to stop detectors", e);
					}
				} else if (bm.isBeamOn() && aborted) {
					aborted = false;
					// ScanBase.paused = false;
					logger.info("Beam ON - RESTART THE LAST ABORTED CVSACN.");
				}
			}
		}
	}

	@Override
	protected void collectData() throws DeviceException, InterruptedException {
		try {
			// start MCS
			for (Detector detector : allDetectors) {
				checkForInterruption(true);
				detector.collectData();
			}
			checkForInterruption(true);

		} catch (DeviceException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	protected void endScan() throws DeviceException {

		// if the interrupt was set
		if (interrupted) {
			try {
				// stop external trajectory scan on XPS controller first
				controller.stop();
			} catch (Exception e) {
				throw new DeviceException("Failed to stop XPS trajectory scan.", e);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// no op
			}
		}
		super.endScan();
		if (!isChild() && !interrupted) {
			String filename = saveRawData();
			if (mdp.isEnabled()) {
				String rebinnedDatafile = mdp.rebinning(filename);
				if (mdp.isRebinCompleted()) {
					mdp.plotData(rebinnedDatafile);
				}
			}
		}
	}

	private String saveRawData() {
		try {
			actualpulses = controller.getActualPulses();
			JythonServerFacade.getInstance().print("acutal pulses is : " + actualpulses);
			// retrieve the actual trajectory
			if (m1move) {
				m1actual = controller.getM1Actual();
			} else {
				m1actual = null;
			}
			if (m2move) {
				m2actual = controller.getM2Actual();
				m2error = controller.getM2Error();
			} else {
				m2actual = null;
			}

			if (m3move) {
				m3actual = controller.getM3Actual();
			} else {
				m3actual = null;
			}

			if (m4move) {
				m4actual = controller.getM4Actual();
			} else {
				m4actual = null;
			}

			if (m5move) {
				m5actual = controller.getM5Actual();
			} else {
				m5actual = null;
			}

			if (m6move) {
				m6actual = controller.getM6Actual();
			} else {
				m6actual = null;
			}

			if (m7move) {
				m7actual = controller.getM7Actual();
			} else {
				m7actual = null;
			}

			if (m8move) {
				m8actual = controller.getM8Actual();
			} else {
				m8actual = null;
			}

		} catch (Throwable e) {
			logger.error("can not get trajector or actual pulases from " + controller.getName(), e);
		}
		// retrieve the count data from detectors
		for (int i = 0; i < mcs.length; i++) {
			for (Map.Entry<Integer, Mca> e : mcs[i].getMcaList().entrySet()) {
				try {
					data[e.getKey().intValue() - 1] = mcs[i].getData(e.getValue().getScalerChannel());
				} catch (Throwable ex) {
					logger.error("can not get detector data from " + mcs[i].getName() + " " + e.getValue().getName(),
							ex);
				}
			}
		}

		String fileName = ((MacDataWriter) getDataWriter()).addData(actualpulses, parentScannables, allScannables,
				allDetectors, m2actual, m1actual, m3actual, m4actual, m5actual, m6actual, m7actual, m8actual, data,
				m2error, totaltime);

		return fileName;
	}

	/**
	 * @param is
	 * @return String
	 */
	public String convertStreamToString(InputStream is) {
		/*
		 * To conver the InputStream to String we use the BufferedReader.readLine() method. We iterate until the
		 * BufferedReader return null which means there's no more data to read. Each line will appended to a
		 * StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 *
	 */
	public class ScanObject {
		private Scannable scannable;
		private double start;
		private double stop;
		private double stepSize;
		private double stepTime;
		private double totalTime;

		/**
		 * @param scannable
		 * @param start
		 * @param stop
		 * @param stepSize
		 * @param stepTime
		 */
		public ScanObject(Scannable scannable, double start, double stop, double stepSize, double stepTime) {
			this.scannable = scannable;
			this.start = start;
			this.stop = stop;
			this.stepSize = stepSize;
			this.stepTime = stepTime;
			this.totalTime = stepTime * (stop - start) / stepSize;
		}

		/**
		 * @param scannable
		 * @param start
		 * @param stop
		 * @param time
		 */
		public ScanObject(Scannable scannable, double start, double stop, double time) {
			this.scannable = scannable;
			this.start = start;
			this.stop = stop;
			this.stepSize = 0.001; // 1mdeg
			this.stepTime = time / ((stop - start) / stepSize);
			this.totalTime = time;
		}

		/**
		 * @return start
		 */
		public double getStart() {
			return start;
		}

		/**
		 * @param start
		 */
		public void setStart(double start) {
			this.start = start;
		}

		/**
		 * @return stop
		 */
		public double getStop() {
			return stop;
		}

		/**
		 * @param stop
		 */
		public void setStop(double stop) {
			this.stop = stop;
		}

		/**
		 * @return step size
		 */
		public double getStepSize() {
			return stepSize;
		}

		/**
		 * @param stepSize
		 */
		public void setStepSize(double stepSize) {
			this.stepSize = stepSize;
		}

		/**
		 * @return step time
		 */
		public double getStepTime() {
			return stepTime;
		}

		/**
		 * @param stepTime
		 */
		public void setStepTime(double stepTime) {
			this.stepTime = stepTime;
		}

		/**
		 * @return scannable
		 */
		public Scannable getScannable() {
			return scannable;
		}

		/**
		 * @param scannable
		 */
		public void setScannable(Scannable scannable) {
			this.scannable = scannable;
		}

		/**
		 * @return total time
		 */
		public double getTotaltime() {
			return totalTime;
		}

		/**
		 * @param totalTime
		 */
		public void setTotaltime(double totalTime) {
			this.totalTime = totalTime;
		}

	}

	/*
	 * @Override public void update(Object theObserved, Object changeCode) { if ((TrajectoryScanProperty) theObserved ==
	 * TrajectoryScanProperty.BUILD) { buildFinished = ((Boolean) changeCode).booleanValue(); } else if
	 * ((TrajectoryScanProperty) theObserved == TrajectoryScanProperty.EXECUTE) { executionFinished = ((Boolean)
	 * changeCode).booleanValue(); } else if ((TrajectoryScanProperty) theObserved == TrajectoryScanProperty.READ) {
	 * readFinished = ((Boolean) changeCode).booleanValue(); } }
	 */
}
