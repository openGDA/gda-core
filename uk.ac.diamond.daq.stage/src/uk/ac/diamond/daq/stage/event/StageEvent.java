package uk.ac.diamond.daq.stage.event;

import java.io.Serializable;

public class StageEvent implements Serializable {
	private static final long serialVersionUID = 3980010271361648569L;

	private String stageName;
	private double position;
	private boolean moving;

	public StageEvent(String stageName, double position, boolean moving) {
		this.stageName = stageName;
		this.position = position;
		this.moving = moving;
	}
	
	public String getStageName() {
		return stageName;
	}
	
	public double getPosition() {
		return position;
	}
	
	public boolean isMoving() {
		return moving;
	}
}
