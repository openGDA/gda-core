package uk.ac.diamond.daq.stage;

import uk.ac.diamond.daq.stage.event.StageGroupEvent;

public interface StageGroupListener {
	void stageGroupChanged (StageGroupEvent event);
}
