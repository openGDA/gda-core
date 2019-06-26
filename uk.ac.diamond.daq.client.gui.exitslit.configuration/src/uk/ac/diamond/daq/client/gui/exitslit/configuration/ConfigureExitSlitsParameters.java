/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.client.gui.exitslit.configuration;

import java.util.Objects;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import gda.device.IScannableMotor;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

/**
 * Parameters to configure the exit slit configuration dialogue
 * <p>
 * In order to make the dialogue as general as possible, the motors to be moved are configured in Spring, and other
 * parameters are set in a properties file (whose path is also set in Spring) which can be edited by beamline staff.
 */
public class ConfigureExitSlitsParameters extends FindableConfigurableBase {
	// These properties must be defined in file specified in propertyFilePath
	private static final String GDA_PITCH_TWEAK_AMOUNT = "gda.pitch.tweak.amount";
	private static final String GDA_SLIT_POSITION_TWEAK_AMOUNT = "gda.slit.position.tweak.amount";
	private static final String GDA_CROSSHAIR_Y_POSITION = "gda.crosshair.y.position";
	private static final String GDA_CROSSHAIR_X_POSITION = "gda.crosshair.x.position";
	private static final String GDA_APERTURE_ARRAY_IN_POSITION = "gda.aperture.array.in.position";
	private static final String GDA_APERTURE_ARRAY_OUT_POSITION = "gda.aperture.array.out.position";

	// To be configured in Spring
	private String propertyFilePath;
	private CameraConfiguration cameraConfig;
	private CameraControl cameraControl;
	private Scannable diagnosticPositioner;
	private Scannable exitSlitShutter;
	private IScannableMotor apertureArrayXMotor;
	private IScannableMotor apertureArrayYMotor;
	private IScannableMotor mirrorPitchMotor;

	// Set during execution
	private Double crosshairXPosition;
	private Double crosshairYPosition;
	private Double pitchTweakAmount;
	private Double slitPositionTweakAmount;
	private Double apertureArrayInPosition;
	private Double apertureArrayOutPosition;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			try {
				Objects.requireNonNull(propertyFilePath, "Property file path not set");
				Objects.requireNonNull(cameraConfig, "Camera configuration not set");
				Objects.requireNonNull(cameraControl, "Camera control not set");
				Objects.requireNonNull(diagnosticPositioner, "Diagnostic positioner not set");
				Objects.requireNonNull(exitSlitShutter, "Exit slit shutter not set");
				Objects.requireNonNull(apertureArrayXMotor, "Aperture array horizontal motor not set");
				Objects.requireNonNull(apertureArrayYMotor, "Aperture array vertical motor not set");
				Objects.requireNonNull(mirrorPitchMotor, "Aperture array motor not set");

				final Configuration configuration = new PropertiesConfiguration(propertyFilePath);
				crosshairXPosition = configuration.getDouble(GDA_CROSSHAIR_X_POSITION, null);
				crosshairYPosition = configuration.getDouble(GDA_CROSSHAIR_Y_POSITION, null);
				pitchTweakAmount = configuration.getDouble(GDA_PITCH_TWEAK_AMOUNT, null);
				slitPositionTweakAmount = configuration.getDouble(GDA_SLIT_POSITION_TWEAK_AMOUNT, null);
				apertureArrayInPosition = configuration.getDouble(GDA_APERTURE_ARRAY_IN_POSITION, null);
				apertureArrayOutPosition = configuration.getDouble(GDA_APERTURE_ARRAY_OUT_POSITION, null);

				Objects.requireNonNull(crosshairXPosition, "Crosshair x position not set");
				Objects.requireNonNull(crosshairYPosition, "Crosshair y position not set");
				Objects.requireNonNull(pitchTweakAmount, "Pitch tweak amount not set");
				Objects.requireNonNull(slitPositionTweakAmount, "Slit position tweak amount not set");
				Objects.requireNonNull(apertureArrayInPosition, "Aperture array 'in' position not set");
				Objects.requireNonNull(apertureArrayOutPosition, "Aperture array 'out' position not set");

				setConfigured(true);
			} catch (Exception e) {
				throw new FactoryException("Error creating wizard to configure exit slits", e);
			}
		}
	}

	public CameraConfiguration getCameraConfig() {
		return cameraConfig;
	}

	public void setCameraConfig(CameraConfiguration cameraConfig) {
		this.cameraConfig = cameraConfig;
	}

	public CameraControl getCameraControl() {
		return cameraControl;
	}

	public void setCameraControl(CameraControl cameraControl) {
		this.cameraControl = cameraControl;
	}

	public Scannable getDiagnosticPositioner() {
		return diagnosticPositioner;
	}

	public void setDiagnosticPositioner(Scannable diagnosticPositioner) {
		this.diagnosticPositioner = diagnosticPositioner;
	}

	public Scannable getExitSlitShutter() {
		return exitSlitShutter;
	}

	public IScannableMotor getApertureArrayXMotor() {
		return apertureArrayXMotor;
	}

	public void setApertureArrayXMotor(IScannableMotor apertureArrayXMotor) {
		this.apertureArrayXMotor = apertureArrayXMotor;
	}

	public void setExitSlitShutter(Scannable exitSlitShutter) {
		this.exitSlitShutter = exitSlitShutter;
	}

	public IScannableMotor getApertureArrayYMotor() {
		return apertureArrayYMotor;
	}

	public void setApertureArrayYMotor(IScannableMotor apertureArrayYMotor) {
		this.apertureArrayYMotor = apertureArrayYMotor;
	}

	public double getApertureArrayInPosition() {
		return apertureArrayInPosition;
	}

	public double getApertureArrayOutPosition() {
		return apertureArrayOutPosition;
	}

	public IScannableMotor getMirrorPitchMotor() {
		return mirrorPitchMotor;
	}

	public void setMirrorPitchMotor(IScannableMotor mirrorPitchMotor) {
		this.mirrorPitchMotor = mirrorPitchMotor;
	}

	public String getPropertyFilePath() {
		return propertyFilePath;
	}
	public void setPropertyFilePath(String propertyFilePath) {
		this.propertyFilePath = propertyFilePath;
	}

	public double getCrosshairXPosition() {
		return crosshairXPosition;
	}

	public double getCrosshairYPosition() {
		return crosshairYPosition;
	}

	public double getPitchTweakAmount() {
		return pitchTweakAmount;
	}

	public double getSlitPositionTweakAmount() {
		return slitPositionTweakAmount;
	}
}
