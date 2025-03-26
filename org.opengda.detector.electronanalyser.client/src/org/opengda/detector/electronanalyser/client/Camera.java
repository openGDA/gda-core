package org.opengda.detector.electronanalyser.client;

public class Camera {
	private int frameRate=70;
	private double energyResolution_eV=0.0877/1000;
	private int cameraXSize=1024;
	private int cameraYSize=1024;

	public Camera() {
		//no-op
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int framerate) {
		this.frameRate = framerate;
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