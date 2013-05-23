package org.opengda.detector.electronanalyser.client;

public class Camera {
	private int frameRate=70;
	private double energyResolution=0.0877;
	private int cameraXSize=1024;
	private int cameraYSize=1024;
	
	public Camera() {
		//no-op
	}

	public Camera(int frameRate, double energyresolution, int cameraXSize,
			int cameraYSize) {
		this.frameRate = frameRate;
		this.energyResolution = energyresolution;
		this.cameraXSize = cameraXSize;
		this.cameraYSize = cameraYSize;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int framerate) {
		this.frameRate = framerate;
	}

	public double getEnergyResolution() {
		return energyResolution;
	}

	public void setEnergyResolution(double energyresolution) {
		this.energyResolution = energyresolution;
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