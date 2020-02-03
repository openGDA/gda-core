package uk.ac.diamond.daq.client.gui.camera;

import java.util.Optional;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.properties.CameraProperties;

public interface ICameraConfiguration {
	int getCameraIndex();
	Optional<CameraConfiguration> getCameraConfiguration();
	Optional<CameraControl> getCameraControl();
	RectangularROI getMaximumSizedROI() throws GDAClientException;
	CameraProperties getCameraProperties(); 
}
