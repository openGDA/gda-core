/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;

public class SpecsPhoibosCompletedRegionWithSeperateIterations extends SpecsPhoibosRegion {
	private double[] kineticEnergyScale;
	private String yAxisUnits;
	private double[] yAxisScale;
	private double[] summedSpectrum = null;
	private double[][] summedImage = null;

	private List<double[][]> images = new ArrayList<>();
	private List<double[]> spectra = new ArrayList<>();

	public double[] getKineticEnergyScale() {
		return kineticEnergyScale;
	}

	public void setKineticEnergyScale(double[] kineticEnergyScale) {
		this.kineticEnergyScale = kineticEnergyScale;
	}

	public String getYAxisUnits() {
		return yAxisUnits;
	}

	public void setYAxisUnits(String yAxisUnits) {
		this.yAxisUnits = yAxisUnits;
	}

	public double[] getYAxisScale() {
		return yAxisScale;
	}

	public void setyAxisScale(double[] yAxisScale) {
		this.yAxisScale = yAxisScale;
	}

	public Dataset getSummedSpectrum() {
		return DatasetFactory.createFromObject(summedSpectrum);
	}

	public Dataset getSummedImage() {
		return DatasetFactory.createFromObject(summedImage);
	}

	public double getSpectrumSum() {
		return Arrays.stream(summedSpectrum).sum();
	}

	public void addCompletedIteration(double[] spectrumData, double[][] imageData) {
		// Check both datasets are the correct sizes before doing anything because
		// we don't want to add any of the data if the sizes don't match
		checkDataSizesAndThrow(spectrumData, imageData);

		addSpectrum(spectrumData);
		addImage(imageData);
	}

	private void checkDataSizesAndThrow(double[] spectrumData, double[][] imageData) {
		if (summedSpectrum != null && summedSpectrum.length != spectrumData.length) {
			throw new IllegalArgumentException("Spectrum to add is not the same size as summed spectrum");
		}

		if (summedImage != null) {
			if (summedImage.length != imageData.length) {
				throw new IllegalArgumentException("Image to add is not the same size as summed image");
			}

			for (int i = 0; i < imageData.length; i++) {
				if (summedImage[i].length != imageData[i].length) {
					throw new IllegalArgumentException("Image to add is not the same size as summed image");
				}
			}
		}
	}

	private void addSpectrum(double[] spectrum) {
		spectra.add(spectrum);

		if (summedSpectrum == null) {
			// If it's the first dataset, just copy it
			summedSpectrum = spectrum.clone();
			return;
		}

		for (int i = 0; i < spectrum.length; i++) {
			summedSpectrum[i] += spectrum[i];
		}
	}

	private void addImage(double[][] image) {
		images.add(image);

		if (summedImage == null) {
			// If it's the first dataset, just copy it
			summedImage = Arrays.stream(image).map(double[]::clone).toArray(double[][]::new);
			return;
		}

		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				summedImage[i][j] += image[i][j];
			}
		}
	}

	public Dataset getImagesDataset () {
		return DatasetFactory.createFromObject(images.toArray());
	}

	public List<double[][]> getImages () {
		return images;
	}

	public Dataset getSpectraDataset () {
		return DatasetFactory.createFromObject(spectra.toArray());
	}

	public List<double[]> getSpectra () {
		return spectra;
	}
}
