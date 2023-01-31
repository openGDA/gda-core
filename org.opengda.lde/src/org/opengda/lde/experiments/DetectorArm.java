package org.opengda.lde.experiments;

import static java.lang.Math.abs;

import org.opengda.lde.model.ldeexperiment.Stage;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroupNamed;

public class DetectorArm extends ScannableGroupNamed {
	//TODO define detector safe position
	private double parkPosition = 2900.0;
	private double positionTolerance=0.001;

	public Scannable getXMotor() throws DeviceException {
		return this.getGroupMemberByName(getName() + "x");
	}

	public Scannable getYMotor() throws DeviceException {
		return this.getGroupMemberByName(getName() + "y");
	}

	public Scannable getZMotor() throws DeviceException {
		return this.getGroupMemberByName(getName() + "z");
	}

	public void parkDetector() throws DeviceException {
		getXMotor().asynchronousMoveTo(getParkPosition());
	}

	public boolean isParked() throws DeviceException {
		return abs((Double)(getZMotor().getPosition())-getParkPosition())<=getPositionTolerance();
	}

	public boolean isAtXPosition(Stage stage) throws DeviceException {
		return abs((Double)(getXMotor().getPosition())-stage.getDetector_x())<=getPositionTolerance();
	}
	public boolean isAtYPosition(Stage stage) throws DeviceException {
		return abs((Double)(getYMotor().getPosition())-stage.getDetector_y())<=getPositionTolerance();
	}
	public boolean isAtZPosition(Stage stage, double stageOffset) throws DeviceException {
		return abs((Double)(getZMotor().getPosition())-(stage.getDetector_z()+stageOffset))<=getPositionTolerance();
	}
	public boolean isAtPosition(Stage stage, double stageOffset) throws DeviceException {
		return isAtXPosition(stage) && isAtYPosition(stage) && isAtZPosition(stage, stageOffset);
	}
	public double getParkPosition() {
		return parkPosition;
	}

	public void setParkPosition(double parkPosition) {
		this.parkPosition = parkPosition;
	}

	public double getPositionTolerance() {
		return positionTolerance;
	}

	public void setPositionTolerance(double positionTolerance) {
		this.positionTolerance = positionTolerance;
	}
}
