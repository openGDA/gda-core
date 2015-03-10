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

package gda.device.detector.maxipix2;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.TangoUtils;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.DetectorBase;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;
import gda.device.lima.LimaCCD;
import gda.device.lima.LimaCCD.AcqTriggerMode;
import gda.device.lima.LimaCCD.SavingMode;
import gda.device.lima.LimaCCD.SavingOverwritePolicy;
import gda.device.maxipix2.MaxiPix2;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * Can work in 2 modes.
 * 
 * A. 1 readout/getPositionCallable per acquisition - multiple frames. Used to taking multiple exposures
 *    with feedback to user after each acqusition. This is the default mode.
 *    
 *    
 * B. 1 readout/getPositionCallable per frame of an acquisition - with timing done by detector. Used to taking multiple exposures
 *    with feedback to user after each frame. This is configured by repscan
 *    
 *    det.setFrames(10)
 *    scan x - - - det 0.1
 *    
 *    This will results in 1 acquisition per value of x. Each acquisition takes 10 frames each 0.1 s apart. There will be
 *    1 scandatapoint per value of x
 *    
 *    repscan 10 det 0.1
 *    
 *    This will results in 1 acquisition consisting of 10 frames each 0.1 s apart. There will be 10 scandatapoints


 * 
 * In mode A:
 *  collectData - starts acquisition having set numberOfFrames in det
 *  getStatus - returns BUSY whilst acquisition continues
 *  getPositionCallable creates data for all frames
 *  
 *  In mode B:
 *   collectData - if first following atLineStart then initiate an acquisition  having set numberOfFrames in det
 *               - else fo nothing
 *   getStatus - returns IDLE
 *   getPositionCallable - returns a single frames worth of info. If last call of current acquisition then wait for
 *                         acqusition to end. 
 * 
 */
public class MaxiPix2MultiFrameDetector extends DetectorBase implements PositionCallableProvider<String>, HardwareTriggerableDetector  {
	private static final Logger logger = LoggerFactory.getLogger(MaxiPix2MultiFrameDetector.class);

	private MaxiPix2 maxiPix2;
	private LimaCCD limaCCD;
	HighestExistingFileMonitor highestExistingFileMonitor=null;

	public MaxiPix2 getMaxiPix2() {
		return maxiPix2;
	}

	public void setMaxiPix2(MaxiPix2 maxiPix2) {
		this.maxiPix2 = maxiPix2;
	}

	public LimaCCD getLimaCCD() {
		return limaCCD;
	}

	public void setLimaCCD(LimaCCD limaCCD) {
		this.limaCCD = limaCCD;
	}

	public HighestExistingFileMonitor getHighestExistingFileMonitor() {
		return highestExistingFileMonitor;
	}

	public void setHighestExistingFileMonitor(HighestExistingFileMonitor highestExistingFileMonitor) {
		this.highestExistingFileMonitor = highestExistingFileMonitor;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		setLocal(true);
		if (getMaxiPix2() == null)
			throw new IllegalStateException("maxiPix2 is not set");
		if (getLimaCCD() == null)
			throw new IllegalStateException("limaCCD is not set");
	}

	private PositionInputStream<Integer> lastImageNumberPositionInputStream = null;

	private PositionStreamIndexer<Integer> lastImageNumberStreamIndexer = null;


	private String fileTemplateForCurrentAcq;

	private int fastModeFramesStarted=0;

	private boolean startAcqPerFrame=true;

	private boolean firstFrameOfScan=true;

	@Override
	public void collectData() throws DeviceException {
		try {
			if( !isFastMode() || ( fastModeFramesStarted== 0)){
				if(!hardwareTriggering)
					prepareForAcquire();
				getLimaCCD().prepareAcq();
				
			}
			if( firstFrameOfScan || startAcqPerFrame){
				getLimaCCD().startAcq();
				checkNotInFault("collectData");
				firstFrameOfScan=false;
			}
			if( isFastMode()){
				fastModeFramesStarted++;
			}
		} catch (DevFailed e1) {
			throw new DeviceException(" collectData failed ", TangoUtils.createDeviceExceptionStack(e1));
		}
	}

	public void prepareForAcquire() throws DevFailed, DeviceException {
		long maxTimeToWaitForImage_ms=0;
		long timeIntervalWhilstWaiting_ms=50;
		getLimaCCD().setAcqExpoTime(getCollectionTime()-.005);
		AcqTriggerMode acqTriggerMode = getLimaCCD().getAcqTriggerMode();
		if( acqTriggerMode.equals(LimaCCD.AcqTriggerMode.EXTERNAL_TRIGGER_MULTI) ||
				acqTriggerMode.equals(LimaCCD.AcqTriggerMode.EXTERNAL_GATE) ||	
				acqTriggerMode.equals(LimaCCD.AcqTriggerMode.EXTERNAL_START_STOP) ||	
				acqTriggerMode.equals(LimaCCD.AcqTriggerMode.EXTERNAL_TRIGGER )){
			startAcqPerFrame= fastMode ? false : true;
			
			maxTimeToWaitForImage_ms=-1; //wait for ever or until the scan is interruped
		} else {
			if(fastMode)
				getLimaCCD().setAcqTriggerMode(LimaCCD.AcqTriggerMode.INTERNAL_TRIGGER_MULTI);
			else
				getLimaCCD().setAcqTriggerMode(LimaCCD.AcqTriggerMode.INTERNAL_TRIGGER);
				
			maxTimeToWaitForImage_ms = Math.max(50,  (long)(1.5 * 1000. *	getCollectionTime()));
			timeIntervalWhilstWaiting_ms = maxTimeToWaitForImage_ms/2;
			startAcqPerFrame= true;
		}
		int acqNbFrames = isFastMode() ? fastModeFramesRequired:numberOfFrames;
		int savingFramePerFile = getLimaCCD().getSavingFramePerFile();
		getLimaCCD().setAcqNbFrames(acqNbFrames);
		if( (savingFramePerFile != 1) && (acqNbFrames != savingFramePerFile)){
			throw new DeviceException("(savingFramePerFile != 1) && (acqNbFrames != savingFramePerFile)");
		}
		//if using external trigger then we do not know the collectionTime so wait 
		savingNextNumberStartAcq = getLimaCCD().getSavingNextNumber(); 
		lastImageNumberPositionInputStream = new LastImagedPositionInputStreamImpl(getLimaCCD(), savingNextNumberStartAcq, isHardwareTriggering() ? acqNbFrames : numberOfFrames,
				maxTimeToWaitForImage_ms, timeIntervalWhilstWaiting_ms, getName());
		lastImageNumberStreamIndexer = new PositionStreamIndexer<Integer>(lastImageNumberPositionInputStream);
		fileTemplateForCurrentAcq = getFullSavingFileTemplate();
	}	
	
	void checkNotInFault(String methodName) throws DeviceException {
		if (getLimaStatus() == FAULT) {
			String possibleReason = "Unknown";
			// look for reason
			// 1 reason is that overwrite policy is ABORT if file exists and indeed file does exist
			// second is if saving_mode is not in Auto_frame
			try {
				if (getLimaCCD().getSavingOverwritePolicy() == SavingOverwritePolicy.ABORT) {
					File f = new File(String.format(getFullSavingFileTemplate(), getLimaCCD().getSavingNextNumber()));
					if (f.exists()) {
						possibleReason = "overwrite policy is ABORT and next file to be created already exists - ' "
								+ f.getAbsolutePath() + "'";
					}
				}
				if( getLimaCCD().getSavingMode() != SavingMode.AUTO_FRAME) {
					possibleReason = "lima saving_mode is not Auto_Frame";
				}
			} catch (DevFailed e) {
				logger.error("Error returned from getLimaCCD().getSavingOverwritePolicy() ",
						TangoUtils.createDeviceExceptionStack(e));
			}
			throw new DeviceException(getName() + " " + methodName
					+ " failed. Detector is in FAULT state.  Possible reason:" + possibleReason);
		}
	}

	public void setSavingDirectory(String savingDirectory) throws DeviceException {
		try {
			getLimaCCD().setSavingDirectory(savingDirectory);
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " setSavingDirectory failed ",
					TangoUtils.createDeviceExceptionStack(e));
		}
	}

	public void setSavingPrefix(String savingPrefix) throws DeviceException {
		try {
			getLimaCCD().setSavingPrefix(savingPrefix);
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " setSavingPrefix failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	public String getSavingDirectory() throws DeviceException {
		try {
			return getLimaCCD().getSavingDirectory();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " getSavingDirectory failed ",
					TangoUtils.createDeviceExceptionStack(e));
		}
	}

	public String getSavingPrefix() throws DeviceException {
		try {
			return getLimaCCD().getSavingPrefix();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " getSavingPrefix failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public Object readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "MaxiPix2Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "MaxiPix2Detector";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "MaxiPix2Detector";
	}

	private Callable<Integer> getLastImageNumberCallable() throws DeviceException {

		return lastImageNumberStreamIndexer.getPositionCallable();
	}

	Callable<FilePathNumber> getFilePathNumberCallable() throws DeviceException {
		return new FilePathNumberCallable(fileTemplateForCurrentAcq, getLastImageNumberCallable());
	}

	/*
	 * The callable simply returns an array of FilePathNumber. 1 item for each frame in current acquisition
	 * The acquisition has finished so we just use the lastSavedNumber - having check it is as expected
	 */
	FilePathNumber[] getFilePathNumberArray() throws DeviceException {
		int savingNextNumber;
		int savingFramePerFile;
		try {
			savingNextNumber = getLimaCCD().getSavingNextNumber();
			savingFramePerFile = getLimaCCD().getSavingFramePerFile();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " getSavingPrefix failed ", TangoUtils.createDeviceExceptionStack(e));
		}
		if( !startAcqPerFrame) {//hardware trigger{
			if( savingNextNumber < savingNextNumberStartAcq + fastModeFramesStarted){
				throw new DeviceException("savingNextNumber < savingNextNumberStartAcq + fastModeFramesStarted");
			}
			return createFilePathNumberArray(fileTemplateForCurrentAcq, savingNextNumberStartAcq+fastModeFramesStarted-1,1 );		
		} 
		if( isFastMode()){
			if( savingNextNumber < savingNextNumberStartAcq + fastModeFramesStarted){
				throw new DeviceException("savingNextNumber != savingNextNumberStartAcq + fastModeFramesStarted");
			}
			return createFilePathNumberArray(fileTemplateForCurrentAcq, savingNextNumberStartAcq+fastModeFramesStarted-1,1 );		
		} 
		if( savingNextNumber != savingNextNumberStartAcq + (numberOfFrames/savingFramePerFile)){
			//TODO we expect savingFramePerFile to be 1 or equal to numberOfFrames
			throw new DeviceException("savingNextNumber != savingNextNumberStartAcq + numberOfFrames/savingFramePerFile");
		}
		return createFilePathNumberArray(fileTemplateForCurrentAcq, savingNextNumberStartAcq, numberOfFrames);		
	}
	
	private FilePathNumber[] createFilePathNumberArray(String template, int savingNextNumberStartAcq, int numberOfFrames) {
		Vector<FilePathNumber> v = new Vector<FilePathNumber>();
		for( int i=0; i<numberOfFrames;i++){
			int imageNumber = savingNextNumberStartAcq+i;
			v.add(new FilePathNumber(Utils.getImagePath(template, imageNumber),imageNumber));
		}
		FilePathNumber[] a = new FilePathNumber[]{};
		return v.toArray(a);
	}
	
	@Override
	public Callable<String> getPositionCallable() throws DeviceException {
		return new FilePathCallable(fileTemplateForCurrentAcq, getLastImageNumberCallable());
	}

	private int numberOfFrames = 1;

	private int savingNextNumberStartAcq;

	private boolean fastMode=false;

	private int fastModeFramesRequired=0;

	private boolean useScanSpecificFolder=true;

	private HardwareTriggerProvider hardwareTriggerProvider;

	private int numberImagesToCollect;

	private boolean hardwareTriggering;

	public boolean isUseScanSpecificFolder() {
		return useScanSpecificFolder;
	}

	public void setUseScanSpecificFolder(boolean useScanSpecificFolder) {
		this.useScanSpecificFolder = useScanSpecificFolder;
	}

	public void setNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public boolean isFastMode() {
		return fastMode;
	}

	public void setFastMode(boolean fastMode) {
		this.fastMode = fastMode;
	}

	String getSavingFileTemplate() throws DeviceException {
		try {
			return getLimaCCD().getSavingPrefix() + "%04d" + getLimaCCD().getSavingSuffix();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " getSavingFileTemplate failed ",
					TangoUtils.createDeviceExceptionStack(e));
		}
	}

	String getFullSavingFileTemplate() throws DeviceException {
		return getSavingDirectory() + getSavingFileTemplate();
	}

	void reset() throws DevFailed{
		limaCCD.setAcqTriggerMode(LimaCCD.AcqTriggerMode.INTERNAL_TRIGGER);
		limaCCD.setAcqMode( LimaCCD.AcqMode.ACCUMULATION);
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		try {
			limaCCD.stopAcq();
			reset();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " stop failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		try {
			reset();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " atScanEnd failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		super.atCommandFailure();
		try {
			limaCCD.stopAcq();
			reset();
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " atCommandFailure failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		int limaStatus = getLimaStatus();
		if(limaStatus != BUSY || !isFastMode())
			return limaStatus;
		try {
			return limaCCD.getSavingNextNumber()<(savingNextNumberStartAcq+fastModeFramesStarted) ? BUSY : IDLE;
		} catch (DevFailed e1) {
			throw new DeviceException(" getStatus failed ", TangoUtils.createDeviceExceptionStack(e1));
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while(isBusy()){
			Thread.sleep(50);
		}
	}

	int getLimaStatus() throws DeviceException {
		try {
			if (limaCCD.getState() != DevState.ON)
				return FAULT;
			switch (limaCCD.getAcqStatus()) {
			case READY:
				return IDLE;
			case FAULT:
				return FAULT;
			case RUNNING:
				return BUSY;
			}
			return FAULT;
		} catch (DevFailed e) {
			throw new DeviceException(getName() + " getLimaStatus failed ", TangoUtils.createDeviceExceptionStack(e));
		}
	}

	public Scannable getThreshold() throws FactoryException {
		return getMaxiPix2().getThresholdScannable();
	}

	public Scannable getEnergyThreshold() throws FactoryException {
		return getMaxiPix2().getEnergyThresholdScannable();
	}

	String getImagePath(int imageNumber) throws DeviceException {
		return Utils.getImagePath(getFullSavingFileTemplate(), imageNumber);
	}

	FilePathNumber getLastFilePathNumber() throws DevFailed, DeviceException {
		checkNotInFault("readout");
		int imageNumber = getLimaCCD().getSavingNextNumber() - 1;
		return new FilePathNumber(getImagePath(imageNumber),imageNumber);
	}
	
	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		firstFrameOfScan=true;
		ScanInformation currentScanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		if( (currentScanInformation!=null) && useScanSpecificFolder  ){
			int scanNumber = currentScanInformation.getScanNumber();
			String dataDir = PathConstructor.createFromDefaultProperty();
			dataDir = dataDir + File.separator + Long.toString(scanNumber) + File.separator + "mpx" + File.separator ;
			File f = new File(dataDir);
			if (!f.exists()){
				if(!f.mkdirs())
					throw new DeviceException("Folder does not exist and cannot be made:" + dataDir);
			}			
			try {
				limaCCD.setSavingDirectory(dataDir);
				limaCCD.setSavingPrefix("");
				int startNumber=0;
				limaCCD.setSavingNextNumber(startNumber);
				if( highestExistingFileMonitor != null){
					HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = 
							new HighestExitingFileMonitorSettings(getSavingDirectory(), getSavingFileTemplate(), startNumber);
					highestExistingFileMonitor.setHighestExitingFileMonitorSettings(highestExitingFileMonitorSettings);
				}
				Thread.sleep(1000);
			} catch (DevFailed e1) {
				throw new DeviceException(" Error setting saving parameters", TangoUtils.createDeviceExceptionStack(e1));
			} catch (InterruptedException e) {
				throw new DeviceException("Interrupted exception ",e);
			}
		}
		if( fastMode ){
			if( currentScanInformation==null)
				throw new DeviceException("Error - no currentScanInformation");

			int[] dimensions = currentScanInformation.getDimensions();
			if( dimensions == null || dimensions.length==0 )
				throw new DeviceException("Error - scan dimensions are null");

			fastModeFramesStarted=0;
			fastModeFramesRequired = 1;
			for( int dim : dimensions){
				fastModeFramesRequired*=dim;
			}
			if( fastModeFramesRequired <1)
				throw new DeviceException("Error - scan dimensions are invalid");
			
			if( hardwareTriggering){
				try {
					//TODO reset on stop/failure
//			        mpx_limaCCD.setAcqTriggerMode(LimaCCD.AcqTriggerMode.INTERNAL_TRIGGER)
//			        mpx_limaCCD.setAcqMode( LimaCCD.AcqMode.ACCUMULATION)
					
					getLimaCCD().setAcqMode( LimaCCD.AcqMode.SINGLE);
					getLimaCCD().setAcqNbFrames(1); 
					getLimaCCD().setAcqTriggerMode(LimaCCD.AcqTriggerMode.EXTERNAL_TRIGGER_MULTI);
					prepareForAcquire();
				} catch (DevFailed e) {
					throw new DeviceException(" Error setting up maxipix", TangoUtils.createDeviceExceptionStack(e));
				}
				setNumberOfFrames(1);
			}
			if (numberOfFrames > 1)
				throw new DeviceException("fastMode and numberOfFrames per acquisition > 1 are incompatible at the moment");

		}
	}
	

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return hardwareTriggerProvider;
	}

	public void setHardwareTriggerProvider(HardwareTriggerProvider hardwareTriggerProvider) {
		this.hardwareTriggerProvider = hardwareTriggerProvider;
	}

	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		this.numberImagesToCollect = numberImagesToCollect;
		
	}

	@Override
	public boolean integratesBetweenPoints() {
		return true;
	}

	@Override
	public void setHardwareTriggering(boolean b) throws DeviceException {
		hardwareTriggering = b;
		fastMode = b;
	}

	@Override
	public boolean isHardwareTriggering() {
		return hardwareTriggering;
	}		
}

class Utils {
	static String getImagePath(String template, int imageNumber) {
		return String.format(template, imageNumber);
	}

}


class LastImagedPositionInputStreamImpl implements PositionInputStream<Integer> {
	private static final Logger logger = LoggerFactory.getLogger(LastImagedPositionInputStreamImpl.class);
	int lastSavingNextNumber;
	int currentSavingNextNumber;
	final int finalSavingNextNumber;
	private final LimaCCD limaCCD;
	final long maxTimeToWaitForImage_ms;
	private final String name;
	int image_saved = -1;
	private long timeIntervalWhilstWaiting_ms;

	@SuppressWarnings("unused")
	LastImagedPositionInputStreamImpl(LimaCCD limaCCD, int initialSavingNextNumber, int numberOfFrames, 
			long maxTimeToWaitForImage_ms, long timeIntervalWhilstWaiting_ms,
			String name)
			throws DevFailed {
		this.limaCCD = limaCCD;
		this.name = name;
		lastSavingNextNumber = initialSavingNextNumber;
		this.finalSavingNextNumber = lastSavingNextNumber + numberOfFrames;
		currentSavingNextNumber = lastSavingNextNumber;
		logger.info("PositionInputStreamImpl getSavingNextNumber returned " + Integer.valueOf(lastSavingNextNumber));
		this.maxTimeToWaitForImage_ms = maxTimeToWaitForImage_ms;
		this.timeIntervalWhilstWaiting_ms = timeIntervalWhilstWaiting_ms;
	}

	int getSavingNextNumber() throws DevFailed {
		int i = limaCCD.getSavingNextNumber();
		logger.info("getSavingNextNumber returned " + Integer.valueOf(i));
		return i;
	}

	@Override
	public Vector<Integer> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		try {
			// if lastSavingNextNumber is controlled by maxToRead then we can just handle the alreay
			// know about images
			if (lastSavingNextNumber == finalSavingNextNumber)
				throw new DeviceException("No more data expected. finalSavingNextNumber:" + finalSavingNextNumber);
			if (currentSavingNextNumber == lastSavingNextNumber) {
				currentSavingNextNumber = getSavingNextNumber();
				if (currentSavingNextNumber == lastSavingNextNumber) {
					long startTime = System.nanoTime();
					while( maxTimeToWaitForImage_ms < 0 || (System.nanoTime()-startTime) < maxTimeToWaitForImage_ms*1000000 ){
						Thread.sleep(timeIntervalWhilstWaiting_ms);
						currentSavingNextNumber = getSavingNextNumber();
						if (currentSavingNextNumber != lastSavingNextNumber)
							break;
					}
					currentSavingNextNumber = getSavingNextNumber();
					if (currentSavingNextNumber == lastSavingNextNumber)
						throw new DeviceException(name + " detector does not seem to be taking images");
				}
			}
			Vector<Integer> data = new Vector<Integer>();
			int numToRead = Math.min(currentSavingNextNumber - lastSavingNextNumber, maxToRead);
			for (int i = 0; i < numToRead; i++) {
				image_saved++;
				data.add(i + lastSavingNextNumber);
				logger.info("Adding value " + (i + lastSavingNextNumber));
			}
			lastSavingNextNumber += numToRead;
			return data;
		} catch (DevFailed e1) {
			throw new DeviceException(" getSavingNextNumber failed ", TangoUtils.createDeviceExceptionStack(e1));
		}
	}
	

}

class FilePathCallable implements Callable<String> {

	private final String template;
	private final Callable<Integer> positionCallable;

	public FilePathCallable(String template, Callable<Integer> positionCallable) {
		this.template = template;
		this.positionCallable = positionCallable;
	}

	@Override
	public String call() throws Exception {
		return Utils.getImagePath(template, positionCallable.call());
	}

}

class FilePathNumberCallable implements Callable<FilePathNumber> {
	private final String template;
	private final Callable<Integer> positionCallable;

	public FilePathNumberCallable(String template, Callable<Integer> positionCallable) {
		this.template = template;
		this.positionCallable = positionCallable;
	}

	@Override
	public FilePathNumber call() throws Exception {
		Integer imageNumber = positionCallable.call();
		return new FilePathNumber( Utils.getImagePath(template, imageNumber),imageNumber);
	}

}

class FilePathNumber {
	String path;
	Integer imageNumber;
	public FilePathNumber(String path, Integer imageNumber) {
		super();
		this.path = path;
		this.imageNumber = imageNumber;
	}
	
}
