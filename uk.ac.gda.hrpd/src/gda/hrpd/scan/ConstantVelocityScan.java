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

import static gda.jython.InterfaceProvider.getTerminalPrinter;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.multichannelscaler.EpicsMcsSis3820;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.detector.multichannelscaler.Mca;
import gda.device.enumpositioner.EpicsSimpleMbbinary;
import gda.device.monitor.IonChamberBeamMonitor;
import gda.factory.Finder;
import gda.hrpd.data.MacDataProcessing;
import gda.hrpd.data.MacDataWriter;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.scan.EpicsTrajectoryScanController;
import gda.scan.Scan;
import gda.scan.Trajectory;
import gda.scan.Scan.ScanStatus;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

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
public class ConstantVelocityScan extends CVScanBase implements Scan {

	private static final long serialVersionUID = 6245061265060159179L;
	private static final Logger logger = LoggerFactory.getLogger(ConstantVelocityScan.class);
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
	private Vector<Detector> detectors = new Vector<Detector>();
	// {			(EpicsMultiChannelScaler) finder.find("mcs1"), (EpicsMultiChannelScaler) finder.find("mcs2") };
	

	@Override
	public Vector<Detector> getDetectors() {
		return detectors;
	}

	public void setDetectors(Detector... detectors) {
		for (Detector each : detectors) {
			this.detectors.add(each);
		}
	}

	// default Epics Trajectory Scan Controller
	private EpicsTrajectoryScanController scanController = (EpicsTrajectoryScanController) finder
			.find("epicsTrajectoryScanController");
	

	public EpicsTrajectoryScanController getScanController() {
		return scanController;
	}

	public void setScanController(EpicsTrajectoryScanController scanController) {
		this.scanController = scanController;
	}

	private IonChamberBeamMonitor beamMonitor = (IonChamberBeamMonitor) finder.find("bm");
	

	public IonChamberBeamMonitor getBeamMonitor() {
		return beamMonitor;
	}

	public void setBeamMonitor(IonChamberBeamMonitor beamMonitor) {
		this.beamMonitor = beamMonitor;
	}
	private static final String SHUTTER_OPEN_POSITION = "OPEN";
	
	private static final String SHUTTER_CLOSED_POSITION = "CLOSE";

	private Scannable shutter = (EpicsSimpleMbbinary) finder.find("fastshutter");
	
	public void setShutterScannable(Scannable shutter) {
		this.shutter = shutter;
	}

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
	public static final double SCAN_RANGE = 35.0;

	/**
	 * The miminum scan range for two-theta. No rebin would be possible if 2theta scan less than this value.
	 */
	public static final double MIN_SCAN_RANGE = 30.0;
	/**
	 * MAC Offset - detector is at -8 deg
	 */
	public double tth_offset = 8.1; // 1st MAC stage's 1
	/**
	 * theta offset that depends on 2theta
	 */
	public double theta_offset = tth_offset / 2;
	/**
	 * thread to read the actual trajectory from XPS to EPICS
	 */
	private Thread reader = new Thread(new TrajectoryReader(), "readTrajectoryThread");

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
	public ConstantVelocityScan(Scannable motor, double start, double end, double time, Detector detector,
			DataWriter datahandler) {
		setDetectors((EpicsMultiChannelScaler) detector);
		totalChannelNumber = ((EpicsMultiChannelScaler)this.detectors.get(0)).getMcaList().size();
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
	public ConstantVelocityScan(Scannable motor, double start, double end, double time, String[] detectors,
			DataWriter datahandler) {
		for (int i = 0; i < detectors.length; i++) {
			this.detectors.add((EpicsMultiChannelScaler) finder.find(detectors[i]));
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
	public ConstantVelocityScan(String motor, double start, double end, double time, String[] detectors,
			DataWriter datahandler) {
		for (int i = 0; i < detectors.length; i++) {
			this.detectors.add((EpicsMultiChannelScaler) finder.find(detectors[i]));
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
	public ConstantVelocityScan(Scannable motor, double start, double time) {
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
	public ConstantVelocityScan(Scannable motor, double start, double end, double time, Detector[] detectors,
			DataWriter datahandler) {
		for (int i = 0; i < detectors.length; i++) {
			this.detectors.add(detectors[i]);
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
	public ConstantVelocityScan(Scannable motor, double start, double end, double time, Detector... detectors) {
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
	public ConstantVelocityScan(Scannable motor, double time) {
		this(motor, 0.0, SCAN_RANGE, time);
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
	public ConstantVelocityScan(Scannable motor1, double start1, double time, Scannable motor2, double start2, double end2) {
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
	public ConstantVelocityScan(Scannable motor1, double start1, double time, Scannable motor2, double offset) {
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
	public ConstantVelocityScan(Scannable motor1, double start1, double time, Scannable motor2) {
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
	public ConstantVelocityScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
			double end2, Detector[] detectors, DataWriter datahandler) {
		for (int i = 0; i < detectors.length; i++) {
			this.detectors.add((EpicsMultiChannelScaler) finder.find(detectors[i].getName()));
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
	public ConstantVelocityScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
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
	public ConstantVelocityScan(Scannable motor1, double start1, double end1, double time, Scannable motor2, double start2,
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
	public ConstantVelocityScan(Scannable[] motors, double[] starts, double[] ends, double time, Detector[] detectors,
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

		Finder finder = Finder.getInstance();
		for (int i = 0; i < detectors.length; i++) {
			this.detectors.add((EpicsMultiChannelScaler) finder.find(detectors[i].getName()));
			if (detectors[i] != null) {
				allScannables.add(detectors[i]);
			}
			totalChannelNumber += ((EpicsMultiChannelScaler)this.detectors.get(i)).getMcaList().size();
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
				((MacDataWriter)getDataWriter()).configure();
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
	public ConstantVelocityScan(Scannable[] motors, double[] starts, double[] ends, double time, Detector[] detectors) {
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
		for (Detector detector : this.detectors) {
			allScannables.add(detector);
			totalChannelNumber +=((EpicsMultiChannelScaler)detector).getMcaList().size();
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
		if (getDataWriter() != null) {
			if (getDataWriter() instanceof MacDataWriter)
				((MacDataWriter) getDataWriter()).configure();
		}
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
			if (start > 13.0) {
				start = 13.0;
				logger.warn("{} - start angle too big, set to maximum 13 degree.", motor.getName());
				getTerminalPrinter().print(
						"START ANGLE TOO HIGH, SCAN SET TO START FROM THE MAXIMUM ANGLE ALLOWED AT 13 DEGREE.");
			} else if (start < 0.0) {
				start = 0.0;
				logger.warn("{} - start angle too small, set to minimum 0 degree", motor.getName());
				getTerminalPrinter().print(
						"START ANGLE TOO LOW, SCAN SET TO START FROM THE MINIMUM ANGLE ALLOWED AT 0 DEGREE.");
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
			if (start > 13.0) {
				logger.error("maximum start angle for '{}' is 13 degree.", motor.getName());
				throw new IllegalArgumentException("START ANGLE TOO HIGH, MAXIMUM 13 DEGREE.");
			} else if (start < 0.0) {
				logger.error("minimum start angle for '{}' is 0 degree", motor.getName());
				throw new IllegalArgumentException("START ANGLE TOO LOW, MINIMUM 0 DEGREE.");
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
		
		collectData();
		// if the trajectory is already running, stop it before restarting is required for detector and XPS
		// synchronisation.
		// as we need to re-sync the data collection trigger pulses from scaler card to XPS
		if (scanController.getExecute() == 1) {
			scanController.stop();
		}
		Thread.sleep(1000);
		scanController.execute();
		Thread.sleep(1000);
		while (scanController.getExecute() != 0) {
			if (aborted) {
				break;
			}
			// we do not want to wait here if this cvscan is aborted by beam drop
			checkForInterruption(false);
			if (!aborted) {
				Thread.sleep(5000);
				String msg = scanController.getExecuteMessage();
				if (msg.equalsIgnoreCase("")) {
					getTerminalPrinter().print("Move to start point, please wait...");
				} else {
					getTerminalPrinter().print(msg);
				}
			}
		}
		if (!aborted) {
			Thread.sleep(1000);
			if (scanController.getExecuteStatusFromEpics() != 1) {
				throw new IllegalStateException("Execution NOT succeed; Status : "
						+ scanController.getExecuteStatus().toString() + "; Message : " + scanController.getExecuteMessage());
			}
			// move all motors to start points ready for next scan
			moveMotorsToStartPoints();
		}

		if (!aborted) {
			// terminate beam monitor thread
			runScanControlThread = false;
			checkForInterruptionIgnorBeamDrop();

			// read the actual trajectory path
			reader.start();
			checkForInterruptionIgnorBeamDrop();

			if (isChild() && !Thread.interrupted()) {
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
			getTerminalPrinter().print("Restart the last CVScan.");
			logger.info("Restart the last CVScan.");
			doCollection(); // beam recovered then restart the last aborted
			// cvscan.
		}
	}
	
	private void moveMotorsToStartPoints() {
		for (ScanObject so : allScanObjects) {
			try {
				so.getScannable().asynchronousMoveTo(so.getStart());
			} catch (DeviceException e) {
				logger.error("failed to move {} to starting point {}", so.getScannable().getName(), so.getStart());
			}
		}
	}

	private class TrajectoryReader implements Runnable {
		private boolean readCompleted = true;

		public boolean isReadCompleted() {
			return readCompleted;
		}

		public void setReadCompleted(boolean readCompleted) {
			this.readCompleted = readCompleted;
		}

		@Override
		public void run() {

			try {
				readCompleted = false;
				scanController.read();
				Thread.sleep(1000);
				while (scanController.getRead() != 0) {
					checkForInterruptionIgnorBeamDrop();
					Thread.sleep(1000);
					getTerminalPrinter().print(
							"Reading the acutal trajectory from XPS to EPICS, please wait...");
				}
				readCompleted = true;
				if (scanController.getReadStatusFromEpics() != 1) {
					throw new IllegalStateException("Trajectory execution NOT succeed."
							+ scanController.getReadStatus().toString() + " Message : " + scanController.getReadMessage());
				}

			} catch (Exception e) {
				logger.error("Error in TrajectoryReader",e);
			}

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
			if (getStatus() == ScanStatus.PAUSED & !Thread.interrupted()) {
				if (!aborted) {
					getTerminalPrinter().print(
							"Current constant velocity scan will continue on XPS server until it completes.");
					getTerminalPrinter().print("To stop CVScan, press Halt or StopAll");
				} else {
					getTerminalPrinter().print(
							"Current constant velocity scan is aborted and will restart once beam recovered.");
					logger.info("Current constant velocity scan is aborted and will restart once beam recovered.");
					getTerminalPrinter().print("To stop CVScan, press Halt or StopAll");

				}
				if (wait) {
					while (getStatus() == ScanStatus.PAUSED || aborted) {
						Thread.sleep(1000);
					}
				}
			}
		} catch (InterruptedException ex) {
			// terminate Beam monitor thread on interrupt
			runScanControlThread = false;
			// reset the abort scan flag on interrupt
			aborted = false;
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
			waitIfPaused();
		} catch (InterruptedException e) {
			// terminate cvscan Beam monitor thread on interrupt
			runScanControlThread = false;
			// reset the abort scan flag on interrupt
			aborted = false;

			throw e;
		}
	}

	@Override
	public void prepareForCollection() throws Exception {
		// display the filename that data will be saved to when completed
		getTerminalPrinter().print(
				"Raw data will be saved to file: " + getNextFileNameUrl()
						+ " When constant velocity scan completed successfully.");
		getTerminalPrinter().print("setting up detectors ......");
		if (!isChild()) {
			super.prepareForCollection();
		}
		prepareDevicesForCollection();
		if (beamMonitor.isMonitorOn()) {
			// if beam monitor is not on, cvscan control thread will not be started.
			// cvscan will finish its trajectory on XPS as before.
			runScanControlThread = true;
			Thread bmt = uk.ac.gda.util.ThreadManager.getThread(new ScanControl(), "cvscanBeamMonitor");
			bmt.start();
		} else {
			aborted = false;
		}
	}

	@Override
	protected void prepareDevicesForCollection() throws Exception {
		if (!isChild()) {
			super.prepareDevicesForCollection();
		}
		getTerminalPrinter().print("setting up motion axises and trajectory......");
		// make sure all move axis are de-activated first
		scanController.setM1Move(false);
		scanController.setM2Move(false);
		scanController.setM3Move(false);
		scanController.setM4Move(false);
		scanController.setM5Move(false);
		scanController.setM6Move(false);
		scanController.setM7Move(false);
		scanController.setM8Move(false);
		checkForInterruptionIgnorBeamDrop();
		// activate the participating axis now
		for (ScanObject so : allScanObjects) {
			if (so.getScannable().getName().equalsIgnoreCase(scanController.getM1axis())) {
				scanController.setM1Move(true);
				if (synchronous) {
					scanController.setM1Traj(traj1.defineCVPath(so.getStart() - theta_offset, so.getStop() - theta_offset,
							so.getTotaltime()));
				} else {
					scanController.setM1Traj(traj1.defineOscillationPath(so.getStart(), so.getStop(), so.getTotaltime()));
				}
				m1move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM2axis())) {
				scanController.setM2Move(true);
				double[] tthpath = traj2.defineCVPath(so.getStart() - tth_offset, so.getStop() - tth_offset, so
						.getTotaltime());
				getTerminalPrinter().print(String.valueOf(tthpath.length));
				scanController.setM2Traj(tthpath);
				m2move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM3axis())) {
				scanController.setM3Move(true);
				scanController.setM3Traj(traj3.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m3move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM4axis())) {
				scanController.setM4Move(true);
				scanController.setM4Traj(traj4.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m4move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM5axis())) {
				scanController.setM5Move(true);
				scanController.setM5Traj(traj5.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m5move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM6axis())) {
				scanController.setM6Move(true);
				scanController.setM6Traj(traj6.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m6move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM7axis())) {
				scanController.setM7Move(true);
				scanController.setM7Traj(traj7.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m7move = true;
			} else if (so.getScannable().getName().equalsIgnoreCase(scanController.getM8axis())) {
				scanController.setM8Move(true);
				scanController.setM8Traj(traj8.defineCVPath(so.getStart(), so.getStop(), so.getTotaltime()));
				m8move = true;
			} else {
				logger.warn("EPICS trajectory scan is not setup for scannable {}", so.getScannable().getName());
				throw new IllegalArgumentException("cvscan is not configured for this scannable "
						+ so.getScannable().getName());
			}
			checkForInterruptionIgnorBeamDrop();
		}

		scanController.setNumberOfElements((int) traj2.getElementNumbers());
		scanController.setNumberOfPulses((int) traj2.getPulseNumbers());
		scanController.setStartPulseElement((int) traj2.getStartPulseElement());
		scanController.setStopPulseElement((int) traj2.getStopPulseElement());
		Thread.sleep(100);
		if (scanController.getStopPulseElement() != (int) traj2.getStopPulseElement()) {
			scanController.setStopPulseElement((int) traj2.getStopPulseElement());
		}
		scanController.setTime(traj2.getTotalTime());
		boolean isFirst = true;
		scanController.build();
		Thread.sleep(1000);
		while (scanController.getBuild() != 0) {
			if (isFirst) {
				getTerminalPrinter().print("building trajectory ......");
				isFirst = false;
			}
			checkForInterruptionIgnorBeamDrop();
		}
		Thread.sleep(2000);
		checkForInterruptionIgnorBeamDrop();
		if (scanController.getBuildStatusFromEpics() != 1) {
			throw new IllegalStateException("Trajectory build NOT succeed; Status : " + scanController.getBuildStatus()
					+ "; Message : " + scanController.getBuildMessage());
		}
	}

	private class ScanControl implements Runnable {

		@Override
		public void run() {

			while (beamMonitor.isMonitorOn() && runScanControlThread) {
				if (!beamMonitor.isBeamOn() && !aborted) {
					try {
						aborted = true;
						getTerminalPrinter().print("BEAM OFF - abort current scan.");
						// abort EPICS trajectory scan on XPS controller
						scanController.stop();
						// ScanBase.paused = true;

						for (Detector detector : allDetectors) {
							detector.stop();
						}
						logger.warn("Beam OFF - Current CVScan is aborted");
					}  catch (Exception e) {
						logger.error("Failed to abort or stop current XPS trajectory scan or to stop detectors", e);
					}
				} else if (beamMonitor.isBeamOn() && aborted) {
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
		if (getStatus().isAborting()) {
			try {
				// stop external trajectory scan on XPS controller first
				scanController.stop();
			} catch (Exception e) {
				throw new DeviceException("Failed to stop XPS trajectory scan.", e);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// no op
			}

			// stop all scannables
			for (Scannable scannable : allScannables) {
				scannable.stop();
			}
			for (Detector detector : allDetectors) {
				detector.stop();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// no op
			}

			// disengage with the data handler, in case this scan is
			// restarted
			if (getDataWriter() != null) {
				try {
					getDataWriter().completeCollection();
				} catch (Exception e) {
					throw new DeviceException("problem calling DataWriter.completeCollection()", e);
				}
			}

		} else {
			for (Scannable scannable : this.allScannables) {
				scannable.atScanLineEnd();
			}

			if (!isChild()) {
				for (Scannable scannable : this.allScannables) {
					scannable.atScanEnd();
				}

				// tell detectors that collection is over
				for (Detector detector : allDetectors) {
					try {
						detector.endCollection();
					} catch (DeviceException ex) {
						logger.error("endScan(): Device Exception: {} ", ex.getMessage());
						throw ex;
					}
				}

				// tell the data handler that collection is complete
				if (getDataWriter() != null) {
					try {
						getDataWriter().completeCollection();
					} catch (Exception e) {
						throw new DeviceException("problem calling DataWriter.completeCollection()", e);
					}
				}
				getTerminalPrinter().print("Scan complete.");
			} else {
				getTerminalPrinter().print("Inner scan complete.");
			}
		}
		// reset the interrupt variables
		if (!isChild() && !getStatus().isAborting()) {
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
			actualpulses = scanController.getActualPulses();
			getTerminalPrinter().print("acutal pulses is : " + actualpulses);
			// retrieve the actual trajectory
			if (m1move) {
				m1actual = scanController.getM1Actual();
			} else {
				m1actual = null;
			}
			if (m2move) {
				m2actual = scanController.getM2Actual();
				m2error = scanController.getM2Error();
			} else {
				m2actual = null;
			}

			if (m3move) {
				m3actual = scanController.getM3Actual();
			} else {
				m3actual = null;
			}

			if (m4move) {
				m4actual = scanController.getM4Actual();
			} else {
				m4actual = null;
			}

			if (m5move) {
				m5actual = scanController.getM5Actual();
			} else {
				m5actual = null;
			}

			if (m6move) {
				m6actual = scanController.getM6Actual();
			} else {
				m6actual = null;
			}

			if (m7move) {
				m7actual = scanController.getM7Actual();
			} else {
				m7actual = null;
			}

			if (m8move) {
				m8actual = scanController.getM8Actual();
			} else {
				m8actual = null;
			}

		} catch (Throwable e) {
			logger.error("can not get trajector or actual pulases from " + scanController.getName(), e);
		}
		// retrieve the count data from detectors
		for (Detector detector : this.detectors) {
			for (Map.Entry<Integer, Mca> e : ((EpicsMultiChannelScaler)detector).getMcaList().entrySet()) {
				try {
					data[e.getKey().intValue() - 1] = ((EpicsMultiChannelScaler)detector).getData(e.getValue().getScalerChannel());
				} catch (Throwable ex) {
					logger.error("can not get detector data from " + detector.getName() + " " + e.getValue().getName(),
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
