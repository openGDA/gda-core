/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;

public class SpecsLiveImagePlot extends SpecsLivePlot {
	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveImagePlot.class);

	private Channel imageChannel;
	private Channel totalPointsIterationChannel;
	private Channel slicesChannel;


	public SpecsLiveImagePlot() {
		try {
			totalPointsIterationChannel = epicsController.createChannel(pvProvider.getTotalPointsIterationPV());
			imageChannel = epicsController.createChannel(pvProvider.getImagePV());
			slicesChannel = epicsController.createChannel(pvProvider.getSlicesPV());
		} catch (CAException | TimeoutException e) {
			logger.error("Could not create channels for image", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			actionBars.getToolBarManager().add(new KeBeSwich());
			plottingSystem.createPlotPart(parent, "Image", actionBars, PlotType.IMAGE, this);
			plottingSystem.setTitle("Analyser Image");
			plottingSystem.setShowLegend(false);
			plottingSystem.getSelectedYAxis().setInverted(true);
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
			throw e;
		}

	}


	@Override
	void updatePlot(SpecsPhoibosLiveUpdate update) {

		if (update instanceof SpecsPhoibosLiveDataUpdate) {
			SpecsPhoibosLiveDataUpdate dataUpdate = (SpecsPhoibosLiveDataUpdate) update;

			// Cache the update in case we want to switch KE and BE
			lastUpdate = dataUpdate;

			// Energy axis
			final double[] energyAxis;
			if (displayInBindingEnergy) {
				energyAxis = dataUpdate.getBeEnergyAxis();
			}
			else {
				energyAxis = dataUpdate.getKeEnergyAxis();
			}
			final IDataset energyAxisDataset = DatasetFactory.createFromObject(energyAxis);
			if (displayInBindingEnergy) {
				energyAxisDataset.setName("Binding Energy (eV)");
			} else {
				energyAxisDataset.setName("Kinetic Energy (eV)");
			}

			// Y axis
			final IDataset yAxis = DatasetFactory.createFromObject(dataUpdate.getyAxis());
			String units = dataUpdate.getYAxisUnits();
			yAxis.setName("Y scale (" + units + ")");
			List<IDataset> axis = Arrays.asList(energyAxisDataset, yAxis);

			// Get the image data
			IDataset data = DatasetFactory.createFromObject(constructImage());

			// Thread safe so don't need to be in the UI thread
			plottingSystem.updatePlot2D(data, axis, null);
			plottingSystem.repaint();

			logger.trace("Updated plotting system");
		}

	}

	private double[][] constructImage(){
		try {
			// Get the expected image size
			final int energyChannels = epicsController.cagetInt(totalPointsIterationChannel);
			final int yChannels = epicsController.cagetInt(slicesChannel);

			// Get the image data from the IOC
			final double[] image1DArray = epicsController.cagetDoubleArray(imageChannel, energyChannels*yChannels);

			// Reshape the data
			final double[][] image2DArray = new double[yChannels][energyChannels];
			for (int i = 0; i < yChannels; i++) {
				System.arraycopy(image1DArray, (i * energyChannels), image2DArray[i], 0, energyChannels);
			}

			return image2DArray;

		} catch (TimeoutException | CAException | InterruptedException e) {
			final String msg = "Could not create image from channels";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}

	}

}
