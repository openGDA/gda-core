/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import gda.device.Scannable;
import gda.factory.Findable;

public class AnalyserPVProvider implements Findable {

	private String name;
	private String spectrumPV;
	private String imagePV;
	private String totalIterations;
	private String totalPointsIterationPV;
	private String currentPointIterationPV;
	private String totalPointsPV;
	private String currentChannelPV;
	private String slicesPV;
	private String yStartPV;
	private String yEndPV;
	private String yUnitsPV;
	// Needed for energy axis and conversion to binding energy
	private Scannable photonEnergy;
	private String lowEnergyPV;
	private String highEnergyPV;
	private double workFunction;
	private boolean separateIterationSaving;
	private boolean imageDataNeeded;
	private String acquisitionModePV;


	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSpectrumPV() {
		return spectrumPV;
	}

	public void setSpectrumPV(String spectrumPV) {
		this.spectrumPV = spectrumPV;
	}

	public String getImagePV() {
		return imagePV;
	}

	public void setImagePV(String imagePV) {
		this.imagePV = imagePV;
	}

	public String getTotalIterations() {
		return totalIterations;
	}

	public void setTotalIterations(String totalIterations) {
		this.totalIterations = totalIterations;
	}

	public String getTotalPointsIterationPV() {
		return totalPointsIterationPV;
	}

	public void setTotalPointsIterationPV(String totalPointsIterationPV) {
		this.totalPointsIterationPV = totalPointsIterationPV;
	}

	public String getSlicesPV() {
		return slicesPV;
	}

	public void setSlicesPV(String slicesPV) {
		this.slicesPV = slicesPV;
	}

	public String getCurrentPointIterationPV() {
		return currentPointIterationPV;
	}

	public void setCurrentPointIterationPV(String currentPointIterationPV) {
		this.currentPointIterationPV = currentPointIterationPV;
	}

	public String getTotalPointsPV() {
		return totalPointsPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

	public String getCurrentChannelPV() {
		return currentChannelPV;
	}

	public void setCurrentChannelPV(String currentChannelPV) {
		this.currentChannelPV = currentChannelPV;
	}

	public String getyStartPV() {
		return yStartPV;
	}

	public void setyStartPV(String yStartPV) {
		this.yStartPV = yStartPV;
	}

	public String getyEndPV() {
		return yEndPV;
	}

	public void setyEndPV(String yEndPV) {
		this.yEndPV = yEndPV;
	}

	public String getyUnitsPV() {
		return yUnitsPV;
	}

	public void setyUnitsPV(String yUnitsPV) {
		this.yUnitsPV = yUnitsPV;
	}

	public Scannable getPhotonEnergy() {
		return photonEnergy;
	}

	public void setPhotonEnergy(Scannable photonEnergy) {
		this.photonEnergy = photonEnergy;
	}

	public String getLowEnergyPV() {
		return lowEnergyPV;
	}

	public void setLowEnergyPV(String lowEnergyPV) {
		this.lowEnergyPV = lowEnergyPV;
	}

	public String getHighEnergyPV() {
		return highEnergyPV;
	}

	public void setHighEnergyPV(String highEnergyPV) {
		this.highEnergyPV = highEnergyPV;
	}

	public double getWorkFunction() {
		return workFunction;
	}

	public void setWorkFunction(double workFunction) {
		this.workFunction = workFunction;
	}

	public boolean isSeparateIterationSaving() {
		return separateIterationSaving;
	}

	public void setSeparateIterationSaving(boolean separateIterationSaving) {
		this.separateIterationSaving = separateIterationSaving;
	}

	public boolean isImageDataNeeded() {
		return imageDataNeeded;
	}

	public void setImageDataNeeded(boolean imageDataNeeded) {
		this.imageDataNeeded = imageDataNeeded;
	}

	public String getAcquisitionModePV() {
		return acquisitionModePV;
	}

	public void setAcquisitionModePV(String acquisitionModePV) {
		this.acquisitionModePV = acquisitionModePV;
	}

}
