package uk.ac.diamond.daq.stage;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableStatus;
import gda.observable.IObserver;
import uk.ac.diamond.daq.stage.event.StageEvent;

public class Stage {
	private static final Logger log = LoggerFactory.getLogger(Stage.class);
	
	private class ScannableListener implements IObserver {
		@Override
		public void update(Object source, Object arg) {
			if (source instanceof ScannableMotor && arg instanceof ScannableStatus) {
				ScannableStatus status = (ScannableStatus)arg;
				if (status == ScannableStatus.IDLE || status == ScannableStatus.BUSY) {
					try {
						double position = (Double)scannable.getPosition();
						for (StageListener listener : listeners) {
							StageEvent stageEvent = new StageEvent(name, position, status == ScannableStatus.BUSY);
							listener.stageStatusChanged(stageEvent);
						}
					} catch (DeviceException e) {
						log.error("Unable to get motor position", e);
					}
				}
			}
		}
	}
	
	private String name;
	private Scannable scannable;
	private Object increment;
	private ScannableListener scannableListener;
	private List<StageListener> listeners = new ArrayList<>();

	public Stage() {
	}

	public Stage(String name, Scannable scannable, Object increment) {
		this.name = name;
		this.scannable = scannable;
		this.increment = increment;
		
		scannableListener = new ScannableListener();
	}

	public String getName() {
		return name;
	}

	public Object getPosition() throws DeviceException {
		return scannable.getPosition();
	}
	
	public Object getIncrement() {
		return increment;
	}

	public void setPosition(Object position) throws DeviceException {
		scannable.asynchronousMoveTo(position);
	}
	
	public void addStageListener (StageListener listener) {
		scannable.addIObserver(scannableListener);
		listeners.add(listener);
	}
	
	public void removeStageListener (StageListener listener) {
		scannable.deleteIObserver(scannableListener);
		listeners.remove(listener);
	}
}
