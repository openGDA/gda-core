package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;

public interface PlotController {
	
	
	/**
	 * Clears all traces and annotations
	 */
	void clear();
	
	
	/**
	 * Creates a 2D plot
	 */
	void createPlot(Dataset x, Dataset y);
	
	
	/**
	 * Creates a 1D plot
	 */
	void createPlot(Dataset x);
	

	/**
	 * x and y could be single values or arrays
	 */
	void plotTriggerPoints(String triggerName, Object x, Object y);
	
	
	/**
	 * Places a marker on the plot indicating the end of the given segment
	 */
	void markSegmentEnd(String segmentName, double x);
	
	
	/**
	 * Flag up point (x, y)
	 */
	void flag(String elementName, String message, double x, double y);
	

	/**
	 * Flag up point (x, 0)
	 */
	void flag(String elementName, String message, double x);

}
