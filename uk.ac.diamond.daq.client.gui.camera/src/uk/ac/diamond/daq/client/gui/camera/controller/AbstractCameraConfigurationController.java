package uk.ac.diamond.daq.client.gui.camera.controller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;

public abstract class AbstractCameraConfigurationController implements IObserver {
	private static final Logger log = LoggerFactory.getLogger(AbstractCameraConfigurationController.class);
	
	protected List<CameraConfigurationListener> listeners = new ArrayList<>();
	private CameraControl cameraControl;
	private CameraConfigurationMode cameraConfigurationMode;
	private RectangularROI currentRoi;
	private RectangularROI proposedRoi;
	
	public AbstractCameraConfigurationController (String findableInstance) throws DeviceException {
		cameraControl = Finder.getInstance().find(findableInstance);
		cameraControl.addIObserver(this);
		
		int[] rawRoi= cameraControl.getRoi();
		currentRoi = new RectangularROI(rawRoi[0], rawRoi[1], rawRoi[2], rawRoi[3], 0.0);
		proposedRoi = new RectangularROI(0.0, 0.0, 0.0, 0.0, 0.0);
	}
	
	public void dispose () {
		cameraControl.deleteIObserver(this);
	}
	
	public void addListener (CameraConfigurationListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener (CameraConfigurationListener listener) {
		listeners.remove(listener);
	}
	
	public RectangularROI getROI () {
		return proposedRoi;
	}
	
	public RectangularROI getCurrentRoi() {
		return currentRoi;
	}
	
	public void setROI (RectangularROI roi)  {
		if (log.isTraceEnabled()) {
			String was = String.format("(%d, %d, %d, %d)", proposedRoi.getIntPoint()[0], proposedRoi.getIntPoint()[1],
					proposedRoi.getIntLengths()[0], proposedRoi.getIntLengths()[1]);
			String now = String.format("(%d, %d, %d, %d)", roi.getIntPoint()[0], roi.getIntPoint()[1],
					roi.getIntLengths()[0], roi.getIntLengths()[1]);
			log.trace("Updatinng ROI => Was: {}, Now: {}", was, now);
		}
		proposedRoi = roi;
		if (proposedRoi.getLength(0) == 0.0 && proposedRoi.getLength(1) == 0.0) {
			for (CameraConfigurationListener listener : listeners) {
				listener.clearRegionOfInterest();
			}
		} else {
			for (CameraConfigurationListener listener : listeners) {
				listener.setROI(proposedRoi);
			}
		}
	}
	
	public void deleteROI () throws DeviceException {
		proposedRoi.setPoint(0.0, 0.0);
		proposedRoi.setLengths(0.0, 0.0);
		cameraControl.clearRoi();
		currentRoi = proposedRoi.copy();
		for (CameraConfigurationListener listener : listeners) {
			listener.clearRegionOfInterest();
		}
	}
	
	public void applyROI () throws DeviceException {
		if (proposedRoi.getLength(0) == 0.0 && proposedRoi.getLength(1) == 0.0) {
			cameraControl.clearRoi();
		} else {
			cameraControl.setRoi(proposedRoi.getIntPoint()[0], proposedRoi.getIntPoint()[1], 
				proposedRoi.getIntLength(0), proposedRoi.getIntLength(1));
			currentRoi = proposedRoi.copy();
		}
		for (CameraConfigurationListener listener : listeners) {
			listener.clearRegionOfInterest();
		}
	}
	
	public RectangularROI getMaximumSizedROI () throws DeviceException {
		int[] frameSize = cameraControl.getFrameSize();
		RectangularROI max = new RectangularROI();
		max.setPoint(0,  0);
		max.setEndPoint(frameSize);
		return max;
	}
	
	public void calculateRatio (int highRegion, int lowRegion) {
		if (lowRegion == 0) {
			lowRegion = 1;
		}
		double ratio = (double)highRegion / lowRegion;
		for (CameraConfigurationListener listener : listeners) {
			listener.setRatio(highRegion, lowRegion, ratio);
		}
	}
	
	public CameraConfigurationMode getCameraDialogMode() {
		return cameraConfigurationMode;
	}
	
	public void setMode (CameraConfigurationMode cameraConfigurationMode) {
		this.cameraConfigurationMode = cameraConfigurationMode;
		for (CameraConfigurationListener listener : listeners) {
			listener.setCameraConfigurationMode(cameraConfigurationMode);
		}
	}
	
	public BinningFormat getBinning () throws DeviceException {
		return cameraControl.getBinningPixels();
	}
	
	public void setBinning (BinningFormat binningFormat) throws DeviceException {
		cameraControl.setBinningPixels(binningFormat);
	}
	
	public double getExposure () throws DeviceException {
		return cameraControl.getAcquireTime();
	}
	
	public void setExposure (double time) throws DeviceException {
		cameraControl.setAcquireTime(time);
	}
	
	public void refreshSnapshot () {
		for (CameraConfigurationListener listener : listeners) {
			listener.refreshSnapshot();
		}
	}

	@Override
	public void update(Object source, Object arg) {
		//nothing yet
	}
}
