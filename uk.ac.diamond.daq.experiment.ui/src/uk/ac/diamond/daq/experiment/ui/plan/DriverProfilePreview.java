package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;

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
	
	public void plot(List<DriverProfileSection> profile) {
		
		plottingSystem.clear();
		
		// create dataset from model
		if (profile.isEmpty()) return;
		
		double[] x = new double[profile.size()+1];
		double[] y = new double[profile.size()+1];
		
		x[0] = 0;
		y[0] = profile.get(0).getStart();
		
		for (int i = 0; i < profile.size(); i++) {
			x[i+1] = profile.get(i).getDuration() + x[i];
			y[i+1] = profile.get(i).getStop();
		}
		
		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		
		xDataset.setName("Time (min)");
		
		plottingSystem.createPlot1D(xDataset, Arrays.asList(yDataset), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
	}
	

}
