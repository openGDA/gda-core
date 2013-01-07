package uk.ac.gda.devices.detector.xspress3;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DAServer;

/**
 * For a continuous (fly) type scan where the logic pulse from the motor is not
 * sent directly to the xspress3, but to a TFG unit, which in turn sends a
 * signal to the Xspress3.
 * <p>
 * This is useful for situations where the motor pulse is not a veto pulse but
 * is of a short duration, so an external TFG is required to create the veto.
 * This is because Xspress3 has no internal timing.
 * <p>
 * This class will derive the TFG time frames from the supplied
 * ContinuousParameters object.
 * 
 * @author rjw82
 * 
 */
public class Xspress3SystemBufferedDetector extends Xspress3System implements
		BufferedDetector {

	private ContinuousParameters parameters;
	private boolean isContinuousModeOn;
	private TRIGGER_MODE triggerModeWhenInContinuousScan = TRIGGER_MODE.TTl_Veto_Only;
	private int triggerSwitch = 0;
	private DAServer daServer;

	public Xspress3SystemBufferedDetector(Xspress3Controller controller,
			Timer tfg, DAServer daServer, int triggerSwitch ) {
		super(controller, tfg);
		this.daServer = daServer;
		this.triggerSwitch = triggerSwitch;
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

		if (on) {
			setTimeFrames();
		} else {
			switchOffExtTrigger();
		}

	}

	private void switchOffExtTrigger() throws DeviceException {
		getDaServer().sendCommand("tfg setup-trig start"); // disables external
															// triggering
	}

	private void setTimeFrames() throws DeviceException {
		switchOnExtTrigger();
		getDaServer().sendCommand("tfg setup-groups ext-start cycles 1");
		getDaServer().sendCommand(
				parameters.getNumberDataPoints()
						+ " 0.000001 0.00000001 0 0 0 8");
		getDaServer().sendCommand("-1 0 0 0 0 0 0");
		getDaServer().sendCommand("tfg arm");
	}

	private void switchOnExtTrigger() throws DeviceException {
		// enables external triggering through the given LEMO socket
		getDaServer().sendCommand("tfg setup-trig start ttl" + triggerSwitch);
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

	public int getTriggerSwitch() {
		return triggerSwitch;
	}

	public void setTriggerSwitch(int triggerSwitch) {
		this.triggerSwitch = triggerSwitch;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

}
