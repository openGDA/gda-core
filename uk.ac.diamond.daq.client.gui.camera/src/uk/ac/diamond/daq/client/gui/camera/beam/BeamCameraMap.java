package uk.ac.diamond.daq.client.gui.camera.beam;

import org.apache.commons.math3.linear.RealMatrix;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;

/**
 * Defines the mapping from a camera array space to the beam drivers one. When
 * the beam illuminating a sample is driven by motors, the
 * {@link #getAffineTransformation()} represents the mapping from the camera
 * pixels positions to the motors positions. For instance, given the camera
 * pixels [x, y], the motors positions is given by
 * 
 * <pre>
 *  <code> 
 * double[] motorXY = beamCameraMap.getAffineTransformation().operate(new double[]{x, y});
 * </code>
 * </pre>
 *
 * @see CameraHelper
 * @author Maurizio Nagni
 *
 */
public class BeamCameraMap {
	private final RealMatrix affineTransformation;
	private final ICameraConfiguration cameraConfiguration;
	private final String driverX;
	private final String driverY;

	public BeamCameraMap(RealMatrix affineTransformation, ICameraConfiguration cameraConfiguration, String driverX,
			String driverY) {
		super();
		this.affineTransformation = affineTransformation;
		this.cameraConfiguration = cameraConfiguration;
		this.driverX = driverX;
		this.driverY = driverY;
	}

	public RealMatrix getAffineTransformation() {
		return affineTransformation;
	}

	public ICameraConfiguration getCameraConfiguration() {
		return cameraConfiguration;
	}

	public String getDriverX() {
		return driverX;
	}

	public String getDriverY() {
		return driverY;
	}
}
