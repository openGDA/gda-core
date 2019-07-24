package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.driver.DriverModel;

public class DriverProfilePreview {
	
	private IPlottingSystem<Composite> plottingSystem;
	
	public DriverProfilePreview(Composite plotComposite) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComposite, "Preview", null, PlotType.XY, null);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(plottingSystem.getPlotComposite());
		} catch (Exception e) {
			new Label(plotComposite, SWT.NONE).setText("Preview cannot be displayed");
		}
	}
	
	public void plot(DriverModel profile) {
		
		plottingSystem.clear();
		
		List<Dataset> datasets = profile.getPlottableDatasets();
		
		plottingSystem.createPlot1D(datasets.get(0), Arrays.asList(datasets.get(1)), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
	}
	

}
