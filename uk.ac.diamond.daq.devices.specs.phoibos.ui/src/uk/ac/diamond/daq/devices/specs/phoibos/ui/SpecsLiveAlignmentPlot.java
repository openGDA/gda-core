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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSpectrumUpdate;

public class SpecsLiveAlignmentPlot extends SpecsLivePlot implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveAlignmentPlot.class);

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			plottingSystem.createPlotPart(parent, "Spectrum", actionBars, PlotType.XY, this);
			plottingSystem.setTitle("Alignment Spectrum");
			plottingSystem.setShowLegend(false);
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
			throw e;
		}
	}


	@Override
	public void updatePlot(SpecsPhoibosLiveUpdate update) {

		if (update instanceof SpecsPhoibosSpectrumUpdate) {
			SpecsPhoibosSpectrumUpdate spectrumUpdate = (SpecsPhoibosSpectrumUpdate) update;

			// Generate time axis
			int[] timeAxis = new int[spectrumUpdate.getDataLength()];
			for(int i=0; i < timeAxis.length; i++) {
				timeAxis[i] = i+1;
			}

			IDataset timeAxisDataset = DatasetFactory.createFromObject(timeAxis);

			// Get data
			IDataset spectrum = DatasetFactory.createFromObject(spectrumUpdate.getSpectrum());
			spectrum.setName("Alignment Spectrum");
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				// Thread safe so don't need to be in the UI thread
				plottingSystem.updatePlot1D(timeAxisDataset, Arrays.asList(spectrum), null);
				plottingSystem.getSelectedYAxis().setTitle("Intensity (counts)");
				plottingSystem.getSelectedXAxis().setTitle("Values");
				plottingSystem.getSelectedXAxis().setInverted(false);
				plottingSystem.repaint();
			});

			logger.trace("Updated plotting system");

		}
	}
}
