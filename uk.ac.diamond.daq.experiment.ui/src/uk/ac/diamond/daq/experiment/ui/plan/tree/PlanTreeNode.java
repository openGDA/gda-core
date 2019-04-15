package uk.ac.diamond.daq.experiment.ui.plan.tree;

import org.eclipse.scanning.api.event.status.Status;

public interface PlanTreeNode {
	
	String getName();
	long getTime();
	String getSevName();
	double getSignificantSignal();
	long getRelativeStart();
	Status getStatus();
}
