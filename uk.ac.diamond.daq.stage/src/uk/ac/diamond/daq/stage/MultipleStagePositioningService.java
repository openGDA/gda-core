package uk.ac.diamond.daq.stage;

import java.util.List;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface MultipleStagePositioningService extends Findable, IObservable {
	boolean isStageEnabled (String stageName);
	
	List<StageConfiguration> getCurrentPositions () throws DeviceException;
	
	Object getPosition (String stageName) throws DeviceException, StageException;
	
	void setPosition (String stageName, Object position) throws DeviceException, StageException;
	
	String getStageGroup ();
}
