/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;

public class SpecsLiveDataImagePlotView implements IObserver {

	@Inject
	private IPlottingService plottingService;

	private IPlottingSystem<Composite> plottingSystem;

	private ISpecsPhoibosAnalyser analyser;

	public SpecsLiveDataImagePlotView() {
		System.out.println("Analyser Live Spectrum Plotter Started");
	}

	@PostConstruct
	void createView(Composite composite) {

		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);

		analyser.addIObserver(this);

		try {
			plottingSystem = plottingService.createPlottingSystem();
			plottingSystem.createPlotPart(composite, "Spectrum", null, PlotType.IMAGE, null);
			plottingSystem.setTitle("Image Plot");
			plottingSystem.getSelectedYAxis().setTitle("Y Pixel");
			plottingSystem.getSelectedXAxis().setTitle("Kinetic Energy (eV)");
			plottingSystem.setKeepAspect(false);
		} catch (Exception e) {

		}
	}

	@Override
	public void update(Object source, Object arg) {

		// Get the image data
		IDataset image = DatasetFactory.createFromObject(analyser.getImage());
		// Transpose the data
		// image = image.getTransposedView(1,0);

		// Get the axis
		IDataset energyAxis = DatasetFactory.createFromObject(analyser.getEnergyAxis());
		IDataset yAxis = DatasetFactory.createFromObject(analyser.getYAxis());
		List<IDataset> axis = Arrays.asList(energyAxis, yAxis);

		plottingSystem.createPlot2D(image, axis, null);
	}

	@PreDestroy
	void dispose() {
		analyser.deleteIObserver(this);
		plottingSystem.dispose();
	}

}
