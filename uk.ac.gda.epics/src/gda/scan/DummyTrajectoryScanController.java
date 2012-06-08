/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides support for EPICS EpicsTrajectoryScanController. A trajectory scan allows fine control of
 * movement using an XPS motor controller. It allows a given number of pulses to be output from the controller, spaced
 * evenly over a set time frame. These pulses are used to control MCA data acquisition module. Operations in trajectory
 * scan involves four steps:
 * <ol>
 * <li>Setup or configure</li>
 * <li>Build</li>
 * <li>Execute</li>
 * <li>Read</li>
 * </ol>
 * Detector data collection will be handled by MCA object.
 */
public class DummyTrajectoryScanController extends DeviceBase implements TrajectoryScanController, Runnable, Configurable, Findable {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyTrajectoryScanController.class);

	private String name = null;

	// GDA names map to EPICS trajectory move axis for CASTOR config
	private String[] maxis = new String[MAX_TRAJECTORY];
	
	private ScannableMotor motorToMove;
	/**
	 * the acceleration time for the motor
	 */
	private double accelerationTime = 1.0;

	/**
	 * /** Maximum array size of the defined trajectory path
	 */
	public static  int MAXIMUM_ELEMENT_NUMBER = 3500;

	
	public static int getMAXIMUM_ELEMENT_NUMBER() {
		return MAXIMUM_ELEMENT_NUMBER;
	}

	public static void setMAXIMUM_ELEMENT_NUMBER(int mAXIMUMELEMENTNUMBER) {
		MAXIMUM_ELEMENT_NUMBER = mAXIMUMELEMENTNUMBER;
	}

	/**
	 * Maximum array size of the output pulses or the data points collected during the trajectory scan.
	 */
	public static int MAXIMUM_PULSE_NUMBER = 60000;

	public static int getMAXIMUM_PULSE_NUMBER() {
		return MAXIMUM_PULSE_NUMBER;
	}

	public static void setMAXIMUM_PULSE_NUMBER(int mAXIMUMPULSENUMBER) {
		MAXIMUM_PULSE_NUMBER = mAXIMUMPULSENUMBER;
	}

	/**
	 * Maximum number of motor permitted to participate in this trajectory scan
	 */
	public static final int MAXIMUM_MOTOR_NUMBER = 8;

	private int nelm; // Number of element in trajectory pv

	private int npulses ; // number of output pulses pv

	private int spulses; // element number to start pulse pv

	private int epulses ; // element number to end pulses pv

	private int apulses ; // actual number of output pulses pv

	private double time ; // trajectory time mbbinary

	private boolean mmove[] = new boolean[MAX_TRAJECTORY];
	
	private double[][] mtraj = new double[MAX_TRAJECTORY][];
	
	private BuildStatus build; // build and check trajectory PV

	
	private String bmess = null; // trajectory build message mbbinary
	private String[] mname = new String[MAX_TRAJECTORY];
	private double[] m2error = null; // M2 actual positions array

	/**
	 * GDA device Name
	 */
	private String deviceName = null;

	
	
	/**
	 * EPICS Put call back handler
	 */
	
	protected boolean buildDone = true;

	
	protected boolean executeDone = true;
	

	protected boolean readDone = true;

	@SuppressWarnings("unused")
	private BuildStatus buildStatus = BuildStatus.UNDEFINED;

	private String buildState = null;

	private String buildMessage = "not set in EPICS";

	

	private ExecuteStatus executeStatus = ExecuteStatus.UNDEFINED;

	private String executeState = null;

	private String executeMessage = "not set in EPICS";

	
	private ReadStatus readStatus = ReadStatus.UNDEFINED;

	private String readState = null;

	private String readMessage = "not set in EPICS";
	private double[][] maccl = new double[MAX_TRAJECTORY][];

	private boolean buildRequired;

	private boolean executeRequired;

	private boolean readRequired;

	private double[] actualTraj;

	private Thread runner;

	@SuppressWarnings("unused")
	private ExecuteStatus execute;

	@SuppressWarnings("unused")
	private boolean abort;

	@SuppressWarnings("unused")
	private boolean waiting;

	
	/*
	 * private static EpicsTrajectoryScanController instance = null; public static EpicsTrajectoryScanController
	 * getInstance() { if(instance == null) { instance = new EpicsTrajectoryScanController(); } return instance; }
	 */

	/**
	 * default constructor
	 */
	public DummyTrajectoryScanController() {
		
	}

	/**
	 * Initialise the trajectory scan object.
	 * 
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
						configured = true;
		}// end of if (!configured)
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();
		runner.setName(getClass().getName() + " " + getName());
	}

	/**
	 * sets trajectory path for motor 1.
	 * 
	 
	 */
	@Override
	public void setMTraj(int motorIndex, double[] path) throws DeviceException, InterruptedException {
	
		this.mtraj[motorIndex -1] =path;
		actualTraj = this.mtraj[motorIndex -1];
	}
	
	
	/**
	 * gets the specified trajectory path for motor 1.
	 * 
	 * @return double value array
	 
	 */
	public double[] getMTraj(int motorIndex) {
		return mtraj[motorIndex - 1];
	}

	
	@Override
	public void setMMove(int motorIndex, boolean b) throws DeviceException, InterruptedException {
		mmove[motorIndex - 1] = b;
		
	}
	/**
	 * query if motor  moves to its trajectory or no, return Yes or No.
	 * 
	 * @return true or false
	 */
	public boolean isMMove(int motorIndex)  {
		return mmove[motorIndex -1];
	}

	
	/**
	 * sets the number of elements in the trajectory to look at for all motors (maximum is 2000).
	 * 
	 * @param value
	 */
	@Override
	public void setNumberOfElements(int value) {
		
		nelm =  value;
	}

	/**
	 * gets number of elements in the defined trajectory from EPICS
	 * 
	 * @return total element number
	 */
	public int getNumberOfElements() {
		return nelm;
	}

	/**
	 * sets the number of output pulses for triggering the detector.
	 * 
	 * @param value
	 */
	@Override
	public void setNumberOfPulses(int value) {
		npulses = value;
	}

	/**
	 * gets the number of pulses in the trajectory
	 * 
	 * @return number of pulses
	 */
	public int getNumberOfPulses() {
		return npulses;
	}

	/**
	 * sets the element number at which output pulses starts, i.e. the element that starts the triggering pulses.
	 * 
	 * @param value
	 */
	@Override
	public void setStartPulseElement(int value)  {
		spulses= value;
	}

	/**
	 * gets the elements number that starts the output pulses.
	 * 
	 * @return pulse-starting element
	 */
	public int getStartPulseElement()  {
		return spulses;
	}

	/**
	 * sets the element number at which output pulses stops, i.e. the element that stops the triggering pulses.
	 * 
	 * @param value
	 */
	@Override
	public void setStopPulseElement(int value) {
		epulses = value;
	}

	/**
	 * gets the element number that stops the ouptput pulses.
	 * 
	 * @return element where pulse stops
	 */
	@Override
	public int getStopPulseElement() {
		return epulses;
	}

	/**
	 * sets the trajectory time, i.e. the time to execute the trajectory.
	 * 
	 * @param value
	 */
	@Override
	public void setTime(double value)  {
		time =value;
	}

	/**
	 * gets the total time for the trajectory.
	 * 
	 * @return total trajectory time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * starts the build process in EPICS.
	 */
	@Override
	public void build() {
		buildDone = false;
		build = BuildStatus.SUCCESS;
		buildRequired = true;
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * @return short
	 */
	public short getBuild()  {
		return 0;
	}

	/**
	 * starts the execute process in EPICS.
	 */
	@Override
	public void execute() {
		/*
		 * if (!buildDone || getBuildStatus() != BuildStatus.SUCCESS) { throw new IllegalStateException( "Success build
		 * is required before execution."); }
		 */
		executeDone = false;
		execute = ExecuteStatus.SUCCESS;
		
		synchronized (this) {
			executeRequired = true;
			notifyAll();
		}
	}

	/**
	 * @return short
	 */
	public short getExecute(){
		return 0;
	}

	/**
	 * starts the read process in EPICS.
	 */
	@Override
	public void read() {
		/*
		 * if (!buildDone || getBuildStatus() != BuildStatus.SUCCESS) { throw new IllegalStateException( "Success build
		 * is required before execution."); } if (!executeDone || getExecuteStatus() != ExecuteStatus.SUCCESS) { throw
		 * new IllegalStateException( "Success execution is required before read data back."); }
		 */
		readDone = false;
		readStatus =ReadStatus.SUCCESS;
		readDone = true;
	
		/*synchronized (this) {
			readRequired = true;
			notifyAll();
		}*/
	}

	/**
	 * @return short
	 */
	public short getRead() {
		return 0;
	}

	/**
	 * gets the build message
	 * 
	 * @return String the message
	 */
	public String getBuildMessage() {
		return buildMessage;
	}

	/**
	 * @return build message
	 */
	public String getBuildMessageFromEpics()  {
		return bmess;
	}

	/**
	 * returns the build status
	 * 
	 * @return build status
	 */
	@Override
	public BuildStatus getBuildStatus() {
		return build;
	}

	
	/**
	 * gets the build state
	 * 
	 * @return build state
	 */
	public String getBuildState() {
		return buildState;
	}

	

	/**
	 * gets the execute message
	 * 
	 * @return String the message
	 */
	public String getExecuteMessage() {
		return executeMessage;
	}

	

	/**
	 * returns the Execute status
	 * 
	 * @return Execute status
	 */
	public ExecuteStatus getExecuteStatus() {
		return executeStatus;
	}


	/**
	 * gets the Execute state
	 * 
	 * @return Execute state
	 */
	public String getExecuteState() {
		return executeState;
	}

	

	/**
	 * gets the Read message
	 * 
	 * @return String the message
	 */
	public String getReadMessage() {
		return readMessage;
	}

	

	/**
	 * returns the Read status
	 * 
	 * @return Read status
	 */
	@Override
	public ReadStatus getReadStatus() {
		return readStatus;
	}
	

	/**
	 * gets the Read state
	 * 
	 * @return Read state
	 */
	public String getReadState() {
		return readState;
	}

	

	/**
	 * gets the actual output pulses
	 * 
	 * @return number of actual pulses
	 */
	@Override
	public int getActualPulses()  {
		return apulses;
	}

	
	/**
	 * gets the Actual trajectory path for motor 2.
	 * 
	 * @return double value array
	 */
	public double[] getM2Error()  {
		return m2error;
	}
	

	@Override
	public double[] getMActual(int motorIndex) throws DeviceException, TimeoutException, InterruptedException {
		return mtraj[motorIndex -1];
	}

	
	
	/**
	 * gets the motor name for motor.
	 * 
	 * @return name
	 */
	public String getMName(int motorIndex)  {
		return mname[motorIndex -1];
	}
	
	/**
	 * stop or abort the trajectory scan.
	 * 
	 */
	@Override
	public void stop()  {
		abort = true;
	}	
	
	/**
	 * sets acceleration for motor .
	 * 
	 * @param value
	 */
	public void setM1Acceleration(int motorIndex, double[] value)  {
		maccl[motorIndex -1] = value;
	}


	/**
	 * gets the acceleration of motor 1.
	 * 
	 * @return acceleration
	 */
	public double getMAcceleration(int motorIndex){
		return maccl[motorIndex -1][0] ;
	}	
	/**
	 * returns the device name
	 * 
	 * @return deviceName
	 */
	public String getDeviceName() {

		return deviceName;
	}

	/**
	 * sets device name
	 * 
	 * @param name
	 */
	public void setDeviceName(String name) {

		this.deviceName = name;
	}

	

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	public String getMaxis(int motorIndex)
	{
		return maxis[motorIndex -1];
	}
	
	/**
	 * @param maxis
	 */
	public void setMaxis(int motorIndex, String maxis) {
		this.maxis[motorIndex -1] = maxis;
	}
	/**
	 * @return m1axis
	 */
	public String getM1axis() {
		return maxis[0];
	}

	/**
	 * @param m1axis
	 */
	public void setM1axis(String m1axis) {
		this.maxis[0] = m1axis;
	}

	/**
	 * @return m2axis
	 */
	public String getM2axis() {
		return maxis[1];
	}

	/**
	 * @param m2axis
	 */
	public void setM2axis(String m2axis) {
		this.maxis[1]= m2axis;
	}

	/**
	 * @return m3axis
	 */
	public String getM3axis() {
		return maxis[2];
	}

	/**
	 * @param m3axis
	 */
	public void setM3axis(String m3axis) {
		this.maxis[2] = m3axis;
	}

	/**
	 * @return m4axis
	 */
	public String getM4axis() {
		return maxis[3];
	}

	/**
	 * @param m4axis
	 */
	public void setM4axis(String m4axis) {
		this.maxis[3] = m4axis;
	}

	/**
	 * @return m5axis
	 */
	public String getM5axis() {
		return maxis[4];
	}

	/**
	 * @param m5axis
	 */
	public void setM5axis(String m5axis) {
		this.maxis[4] = m5axis;
	}

	/**
	 * @return m6axis
	 */
	public String getM6axis() {
		return maxis[5];
	}

	/**
	 * @param m6axis
	 */
	public void setM6axis(String m6axis) {
		this.maxis[5] = m6axis;
	}

	/**
	 * @return m7axis
	 */
	public String getM7axis() {
		return maxis[6];
	}

	/**
	 * @param m7axis
	 */
	public void setM7axis(String m7axis) {
		this.maxis[6] = m7axis;
	}

	/**
	 * @return m8axis
	 */
	public String getM8axis() {
		return maxis[7];
	}

	/**
	 * @param m8axis
	 */
	public void setM8axis(String m8axis) {
		this.maxis[7] = m8axis;
	}
	/**
	 * @return accelerationTime
	 */
	@Override
	public double getAccelerationTime() {
		return accelerationTime;
	}

	/**
	 * @param accelerationTime
	 */
	@Override
	public void setAccelerationTime(double accelerationTime) {
		this.accelerationTime = accelerationTime;
	}

	@Override
	public synchronized void run() {
		while (true) {
			// Wait until the simulatedMoveRequired flag is set to true
			try {
				waiting = true;
				do {
					wait();
				} while (!buildRequired && !executeRequired && !readRequired);
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.debug(ex.getMessage());
			}
			if(buildRequired)
			{
				buildRequired = false;
				try {
					wait(5);
				} catch (InterruptedException e) {
					logger.error("build interrupted");
					buildStatus = BuildStatus.FAILURE;
					continue;
				}
				buildStatus = BuildStatus.SUCCESS;
				buildDone = true;
			}
			else if(executeRequired)
			{
				executeRequired = false;
				double timePerPoint = time / nelm;
				for(int i =0; i < nelm ; i++)
				{
					try {
						motorToMove.moveTo(actualTraj[i]);
						Thread.sleep((long) timePerPoint);
					} catch (DeviceException e) {
						logger.error("error moving the motor");
						executeStatus = ExecuteStatus.FAILURE;
						//notifyIObservers(TrajectoryScanProperty.EXECUTE,executeStatus );
						break;
					} catch (InterruptedException e) {
						logger.error("error during execute");
						executeStatus = ExecuteStatus.FAILURE;
						//notifyIObservers(TrajectoryScanProperty.EXECUTE,executeStatus );
						break;
					}
					executeStatus = ExecuteStatus.SUCCESS;
					
				}
				executeDone = true;
				notifyIObservers(TrajectoryScanProperty.EXECUTE,executeStatus );
				
			}
			else if(readRequired)
			{
				readRequired = false;
				try {
					wait(5);
				} catch (InterruptedException e) {
					logger.error("build interrupted");
					readStatus = ReadStatus.FAILURE;
					continue;
				}
				readStatus = ReadStatus.SUCCESS;
				readDone = true;
				
			}
		}//end of while loop
		
	}

	public void setMotorToMove(ScannableMotor motorToMove) {
		this.motorToMove = motorToMove;
	}

	public ScannableMotor getMotorToMove() {
		return motorToMove;
	}

	@Override
	public boolean isBusy()
	{
		if(buildDone && executeDone )
			return false;
		return true;
	}

	@Override
	public boolean isReading() {
		
		return false;
	}

	@Override
	public boolean isBuilding() {
		// TODO Auto-generated method stub
		return false;
	}

	public double[] getM3Actual()  {
		return mtraj[2];
	}

	
	
}
