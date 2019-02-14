package uk.ac.diamond.daq.stage;

import java.util.List;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface MultipleStagePositioningService extends Findable, IObservable {
	List<Stage> getCurrentStages () throws DeviceException;
	
	String getStageGroup ();
}
