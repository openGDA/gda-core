package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.widgets.Composite;

/**
 * The minimal object to interact with the plotting system image. While the
 * {@link #getPlottingSystem()} returns the GUI graphical elements,
 * {@link #getImageTrace()} returns the image displayed in the plotting system
 * 
 * @author Maurizio Nagni
 */
public interface CameraPlotter {

	/**
	 * @return the GUI plotting system
	 */
	IPlottingSystem<Composite> getPlottingSystem();

	/**
	 * @return the image displayed in the plotting system
	 */
	IImageTrace getImageTrace();

}