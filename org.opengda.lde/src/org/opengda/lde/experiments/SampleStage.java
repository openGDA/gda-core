package org.opengda.lde.experiments;

import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.device.scannable.scannablegroup.corba.impl.ScannablegroupAdapter;
import gda.device.scannable.scannablegroup.corba.impl.ScannablegroupImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
@CorbaAdapterClass(ScannablegroupAdapter.class)
@CorbaImplClass(ScannablegroupImpl.class)
public class SampleStage extends ScannableGroup implements InitializingBean {
	private double parkPosition = -400.0;
	private double engagePosition = 0.0;
	private double positionTolerance=0.001;
	//the fixed offset of sample stage in Z direction reference to the Z-zero position of the detector 
	private double zPosition;
	private boolean samplesProcessed=false;
	private boolean detectorCalibrated;

	public Scannable getXMotor() {
		return this.getGroupMember(getName() + "x");
	}

	public Scannable getYMotor() {
		return this.getGroupMember(getName() + "y");
	}

	public Scannable getRotationMotor() {
		Scannable groupMember = this.getGroupMember(getName() + "rot");
		if (groupMember==null) {
			throw new IllegalArgumentException("No rotation motor in the group '"+getName()+"'.");
		}
		return groupMember;
	}

	public void parkStage() throws DeviceException {
		getXMotor().asynchronousMoveTo(getParkPosition());
	}

	public void engageStage() throws DeviceException {
		getXMotor().asynchronousMoveTo(getEngagePosition());
	}
	public boolean isAtXPosition(double demandPosition) throws DeviceException {
		return ((Double)(getXMotor().getPosition())-demandPosition)<=getPositionTolerance();
	}
	public boolean isAtYPosition(double demandPosition) throws DeviceException {
		return ((Double)(getYMotor().getPosition())-demandPosition)<=getPositionTolerance();
	}
	public boolean isAtCalibrantPosition(Cell cell) throws DeviceException {
		return isAtXPosition(cell.getCalibrant_x()) && isAtYPosition(cell.getCalibrant_y());
	}
	public boolean isAtSamplePosition(Sample sample) throws DeviceException {
		if (sample.getSample_x_start()!=Double.NaN && sample.getSample_y_start()!=Double.NaN)
			return isAtXPosition(sample.getSample_x_start()) && isAtYPosition(sample.getSample_y_start());
		if (sample.getSample_x_start()!=Double.NaN && sample.getSample_y_start() == Double.NaN)
			return isAtXPosition(sample.getSample_x_start());
		
		return true;
	}
	public boolean isParked() throws DeviceException {
		return ((Double)(getXMotor().getPosition())-getParkPosition())<=getPositionTolerance();
	}

	public boolean isEngaged() throws DeviceException {
		return ((Double)(getXMotor().getPosition())-getEngagePosition())<=getPositionTolerance();
	}

	public double getParkPosition() {
		return parkPosition;
	}

	public void setParkPosition(double parkPosition) {
		this.parkPosition = parkPosition;
	}

	public double getEngagePosition() {
		return engagePosition;
	}

	public void setEngagePosition(double engagePosition) {
		this.engagePosition = engagePosition;
	}

	public double getPositionTolerance() {
		return positionTolerance;
	}

	public void setPositionTolerance(double positionTolerance) {
		this.positionTolerance = positionTolerance;
	}

	public boolean isSamplesProcessed() {
		return samplesProcessed;
	}

	public void setSamplesProcessed(boolean samplesProcessed) {
		this.samplesProcessed = samplesProcessed;
	}

	public double getzPosition() throws DeviceException {
		Scannable groupMember = this.getGroupMember(getName() + "ztop");
		if (groupMember!=null) {
			return zPosition+Double.valueOf(groupMember.getPosition().toString());
		}
		return zPosition;
	}

	public void setzPosition(double zPosition) {
		this.zPosition = zPosition;
	}

	public void setDetectorCalibrated(boolean b) {
		this.detectorCalibrated=b;		
	}
	public boolean isDetectorCalibrated() {
		return detectorCalibrated;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zPosition==-1000.0) {
			throw new IllegalStateException("Stage must have Z position set.");
		}
	}

}
