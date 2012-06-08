/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device;

/**
 * Parameters which define the movement of a ContinuousScannable object during a continuous scan.
 */
public class ContinuousParameters {
	
	private double totalTime;
	
	private int numberDataPoints;
	
	private double startPosition;
	
	private double endPosition;
	
	private String ContinuouslyScannableName;

	/**
	 * @return Returns the total time in s
	 */
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * @param time The total time in s of synchronised data collection and movement.
	 */
	public void setTotalTime(double time) {
		this.totalTime = time;
	}

	/**
	 * @return Returns the numberDataPoints.
	 */
	public int getNumberDataPoints() {
		return numberDataPoints;
	}

	/**
	 * @param numberDataPoints The numberDataPoints to set.
	 */
	public void setNumberDataPoints(int numberDataPoints) {
		this.numberDataPoints = numberDataPoints;
	}

	/**
	 * @return Returns the startPositon.
	 */
	public double getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPositon The startPositon to set.
	 */
	public void setStartPosition(double startPositon) {
		this.startPosition = startPositon;
	}

	/**
	 * @return Returns the endPosition.
	 */
	public double getEndPosition() {
		return endPosition;
	}

	/**
	 * @param endPosition The endPosition to set.
	 */
	public void setEndPosition(double endPosition) {
		this.endPosition = endPosition;
	}

	public void setContinuouslyScannableName(String continuouslyScannableName) {
		ContinuouslyScannableName = continuouslyScannableName;
	}

	public String getContinuouslyScannableName() {
		return ContinuouslyScannableName;
	}

}
