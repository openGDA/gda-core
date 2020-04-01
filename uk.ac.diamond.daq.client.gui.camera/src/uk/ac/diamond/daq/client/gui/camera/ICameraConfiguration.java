package uk.ac.diamond.daq.client.gui.camera;

import java.util.Optional;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraMap;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.properties.CameraProperties;

/**
 * Provides information about a camera
 * 
 * @see CameraHelper
 * @author Maurizio Nagni
 */
public interface ICameraConfiguration {
	/**
	 * The index assigned by the configuration to the camera
	 * 
	 * @return the index assigned to this camera
	 */
	int getCameraIndex();

	/**
	 * Hold camera configuration related to streaming
	 * 
	 * @return
	 */
	Optional<CameraConfiguration> getCameraConfiguration();

	/**
	 * An interface allowing access to basic camera operations
	 * 
	 * @return
	 */
	Optional<CameraControl> getCameraControl();

	RectangularROI getMaximumSizedROI() throws GDAClientException;

	/**
	 * The client camera configuration. See
	 * <a href= "https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">
	 * 
	 * @return
	 */
	CameraProperties getCameraProperties();

	/**
	 * When the beam illuminating the sample is driven by X/Y motors, this method
	 * returns a class containing information about the transformation between the
	 * two spaces defined by the drivers and the camera
	 * 
	 * @return
	 */
	Optional<BeamCameraMap> getBeamCameraMap();
}
