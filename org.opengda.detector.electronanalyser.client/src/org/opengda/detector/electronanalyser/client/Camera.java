package org.opengda.detector.electronanalyser.client;

import org.opengda.detector.electronanalyser.client.views.SequenceViewLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;

public class Camera {

	private static final Logger logger = LoggerFactory.getLogger(SequenceViewLive.class);

	private double energyResolution_eV=0.0877/1000;
	private int cameraXSize=1024;
	private int cameraYSize=1024;

	private Scannable frameRateScannable;
	private double defaultFramerate = 17;
	private boolean isConnected = true;

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public void setFrameRateScannable(Scannable frameRateScannable) {
		this.frameRateScannable = frameRateScannable;
	}

	public double getFrameRate() {
		//Check if connected, otherwise when polled it will spam with errors if disconnected.
		if (isConnected) {
			try {
				final double frameRate = (double) frameRateScannable.getPosition();
				//Update default framerate to be what it is now if it ever disconnects.
				defaultFramerate = frameRate;
			} catch (Exception e) {
				logger.warn("Unable to get frame rate, using default value of {}", defaultFramerate, e);
				isConnected = false;
			}
		}
		return defaultFramerate;
	}

	public double getDefaultFramerate() {
		return defaultFramerate;
	}

	public void setDefaultFramerate(double defaultFramerate) {
		this.defaultFramerate = defaultFramerate;
	}

	public double getEnergyResolution_meV() {
		return energyResolution_eV*1000;
	}

	public void setEnergyResolution_meV(double energyResolution_meV) {
		this.energyResolution_eV = energyResolution_meV / 1000.;
	}

	public double getEnergyResolution_eV() {
		return this.energyResolution_eV;
	}

	public void setEnergyResolution_eV(double energyResolution_eV) {
		this.energyResolution_eV = energyResolution_eV;
	}

	public int getCameraXSize() {
		return cameraXSize;
	}

	public void setCameraXSize(int cameraXSize) {
		this.cameraXSize = cameraXSize;
	}

	public int getCameraYSize() {
		return cameraYSize;
	}

	public void setCameraYSize(int cameraYSize) {
		this.cameraYSize = cameraYSize;
	}
}