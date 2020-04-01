package uk.ac.diamond.daq.client.gui.camera.beam;

import java.awt.geom.Point2D;

/**
 * Maps an array pixel against the beam drivers positions.  
 * 
 * @author Maurizio Nagni
 */
class BeamCameraPoint {
	private final Point2D arrayPosition;
	private final Point2D driverPosition;

	public BeamCameraPoint(Point2D arrayPosition, Point2D driverPosition) {
		super();
		this.arrayPosition = arrayPosition;
		this.driverPosition = driverPosition;
	}

	public Point2D getArrayPosition() {
		return arrayPosition;
	}

	public Point2D getDriverPosition() {
		return driverPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrayPosition == null) ? 0 : arrayPosition.hashCode());
		result = prime * result + ((driverPosition == null) ? 0 : driverPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeamCameraPoint other = (BeamCameraPoint) obj;
		if (arrayPosition == null) {
			if (other.arrayPosition != null)
				return false;
		} else if (!arrayPosition.equals(other.arrayPosition))
			return false;
		if (driverPosition == null) {
			if (other.driverPosition != null)
				return false;
		} else if (!driverPosition.equals(other.driverPosition))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "BeamCameraPoint [arrayPosition=" + arrayPosition + ", driverPosition=" + driverPosition + "]";
	}
}