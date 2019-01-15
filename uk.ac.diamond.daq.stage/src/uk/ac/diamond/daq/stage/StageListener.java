package uk.ac.diamond.daq.stage;

import uk.ac.diamond.daq.stage.event.StageEvent;

public interface StageListener {
	void stageStatusChanged (StageEvent stageEvent);
}
