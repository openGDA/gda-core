package uk.ac.gda.client.live.stream.controls.handlers;

import com.opencsv.bean.CsvBindByName;

public class CrosshairBean {
	
	@CsvBindByName
	private String cameraName;
	
	@CsvBindByName
	private double xPosition;
	
	@CsvBindByName
	private double yPosition;

	public String getCameraName() {
		return cameraName;
	}

	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}

	public double getxPosition() {
		return xPosition;
	}

	public void setxPosition(double xPosition) {
		this.xPosition = xPosition;
	}

	public double getyPosition() {
		return yPosition;
	}

	public void setyPosition(double yPosition) {
		this.yPosition = yPosition;
	}

}
