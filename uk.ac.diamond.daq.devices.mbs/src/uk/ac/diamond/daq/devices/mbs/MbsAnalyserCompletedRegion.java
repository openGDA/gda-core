/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

public class MbsAnalyserCompletedRegion {

	private double collectionTime;
	private double acquireTime;
	private double acquirePeriod;
	private int iterations;
	private int passEnergy;
	private String lensMode;
	private String acquisitionMode;
	private double startEnergy;
	private double endEnergy;
	private double centreEnergy;
	private double energyWidth;
	private double deflectorX;
	private double deflectorY;
	private int numberOfSlices;
	private int numberfSteps;
	private int numberOfDitherSteps;
	private double spinOffset;
	private double  stepSize;
	private double[][] image;
	private int regionStartX;
	private int regionStartY;
	private int regionSizeX;
	private int regionSizeY;
	private int sensorSizeX;
	private int sensorSizeY;
	private double[] energyAxis;
	private double[] lensAxis;
	private double countPerSecond;
	private int[] cpsRegionOrigin;
	private int[] cpsRegionSize;
	private String psuMode;


	public double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}

	public double getAcquirePeriod() {
		return acquirePeriod;
	}

	public void setAcquirePeriod(double acquirePeriod) {
		this.acquirePeriod = acquirePeriod;
	}
	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int getPassEnergy() {
		return passEnergy;
	}

	public void setPassEnergy(int passEnergy) {
		this.passEnergy = passEnergy;
	}

	public String getLensMode() {
		return lensMode;
	}

	public void setLensMode(String lensMode) {
		this.lensMode = lensMode;
	}

	public String getAcquisitionMode() {
		return acquisitionMode;
	}

	public void setAcquisitionMode(String acquisitionMode) {
		this.acquisitionMode = acquisitionMode;
	}

	public double getStartEnergy() {
		return startEnergy;
	}

	public void setStartEnergy(double startEnergy) {
		this.startEnergy = startEnergy;
	}

	public double getEndEnergy() {
		return endEnergy;
	}

	public void setEndEnergy(double endEnergy) {
		this.endEnergy = endEnergy;
	}
	public double getCentreEnergy() {
		return centreEnergy;
	}

	public void setCentreEnergy(double centreEnergy) {
		this.centreEnergy = centreEnergy;
	}

	public double getEnergyWidth() {
		return energyWidth;
	}

	public void setEnergyWidth(double energyWidth) {
		this.energyWidth = energyWidth;
	}

	public double getDeflectorX() {
		return deflectorX;
	}

	public void setDeflectorX(double deflectorX) {
		this.deflectorX = deflectorX;
	}

	public double getDeflectorY() {
		return deflectorY;
	}

	public void setDeflectorY(double deflectorY) {
		this.deflectorY = deflectorY;
	}

	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	public int getNumberfSteps() {
		return numberfSteps;
	}

	public void setNumberfSteps(int numberfSteps) {
		this.numberfSteps = numberfSteps;
	}

	public int getNumberOfDitherSteps() {
		return numberOfDitherSteps;
	}

	public void setNumberOfDitherSteps(int numberOfDitherSteps) {
		this.numberOfDitherSteps = numberOfDitherSteps;
	}

	public double getSpinOffset() {
		return spinOffset;
	}

	public void setSpinOffset(double spinOffset) {
		this.spinOffset = spinOffset;
	}

	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	public double[][] getImage() {
		return image;
	}

	public void setImage(double[][] image) {
		this.image = image;
	}

	public int getRegionStartX() {
		return regionStartX;
	}

	public void setRegionStartX(int regionStartX) {
		this.regionStartX = regionStartX;
	}

	public int getRegionStartY() {
		return regionStartY;
	}

	public void setRegionStartY(int regionStartY) {
		this.regionStartY = regionStartY;
	}

	public int getRegionSizeX() {
		return regionSizeX;
	}

	public void setRegionSizeX(int regionSizeX) {
		this.regionSizeX = regionSizeX;
	}

	public int getRegionSizeY() {
		return regionSizeY;
	}

	public void setRegionSizeY(int regionSizeY) {
		this.regionSizeY = regionSizeY;
	}

	public int getSensorSizeX() {
		return sensorSizeX;
	}

	public void setSensorSizeX(int sensorSizeX) {
		this.sensorSizeX = sensorSizeX;
	}

	public int getSensorSizeY() {
		return sensorSizeY;
	}

	public void setSensorSizeY(int sensorSizeY) {
		this.sensorSizeY = sensorSizeY;
	}

	public double[] getEnergyAxis() {
		return energyAxis;
	}

	public void setEnergyAxis(double[] energyAxis) {
		this.energyAxis = energyAxis;
	}

	public double[] getLensAxis() {
		return lensAxis;
	}

	public void setLensAxis(double[] lensAxis) {
		this.lensAxis = lensAxis;
	}

	public boolean isTransmissionLensMode() {
		return getLensMode().toLowerCase().contains("spat");
	}

	public double getCountPerSecond() {
		return countPerSecond;
	}

	public void setCountPerSecond(double countPerSecond) {
		this.countPerSecond = countPerSecond;
	}

	public int[] getCpsRegionOrigin() {
		return cpsRegionOrigin;
	}

	public void setCpsRegionOrigin(int[] cpsRegionOrigin) {
		this.cpsRegionOrigin = cpsRegionOrigin;
	}

	public int[] getCpsRegionSize() {
		return cpsRegionSize;
	}

	public void setCpsRegionSize(int[] cpsRegionSize) {
		this.cpsRegionSize = cpsRegionSize;
	}

	public double getAcquireTime() {
		return acquireTime;
	}

	public void setAcquireTime(double acquireTime) {
		this.acquireTime = acquireTime;
	}

	public String getPsuMode() {
		return psuMode;
	}

	public void setPsuMode(String psuMode) {
		this.psuMode = psuMode;
	}
}
