package uk.ac.gda.devices.detector.xspress3;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;

/**
 * When using an Xspress3 system in a ContinuousScan.
 * 
 * @author rjw82
 *
 */
public class Xspress3BufferedDetector extends Xspress3Detector implements
		BufferedDetector {

	private ContinuousParameters parameters;
	private boolean isContinuousModeOn;
	private TRIGGER_MODE triggerModeWhenInContinuousScan = TRIGGER_MODE.TTl_Veto_Only;

	public Xspress3BufferedDetector(Xspress3Controller controller) {
		super(controller);
	}

	@Override
	public void clearMemory() throws DeviceException {
		controller.doErase();
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		this.isContinuousModeOn = on;
		this.controller.setNumFramesToAcquire(parameters.getNumberDataPoints());
		this.controller.setTriggerMode(triggerModeWhenInContinuousScan);
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousModeOn;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters)
			throws DeviceException {
				this.parameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters()
			throws DeviceException {
		return parameters;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		return controller.getTotalFramesAvailable();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame)
			throws DeviceException {
		return super.readoutFrames(startFrame, finalFrame);
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		return super.readoutFrames(0, controller.getNumFramesToAcquire());
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return controller.getNumFramesPerReadout();
	}

	public TRIGGER_MODE getTriggerModeWhenInContinuousScan() {
		return triggerModeWhenInContinuousScan;
	}

	public void setTriggerModeWhenInContinuousScan(
			TRIGGER_MODE triggerModeWhenInContinuousScan) {
		this.triggerModeWhenInContinuousScan = triggerModeWhenInContinuousScan;
	}

}
