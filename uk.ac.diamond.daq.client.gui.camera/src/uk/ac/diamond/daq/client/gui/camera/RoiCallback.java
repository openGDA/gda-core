package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;

import gda.device.DeviceException;

public interface RoiCallback {
	IRectangularROI getROI() throws DeviceException;
}