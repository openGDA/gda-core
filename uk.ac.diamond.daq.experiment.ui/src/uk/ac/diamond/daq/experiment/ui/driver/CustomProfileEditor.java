package uk.ac.diamond.daq.experiment.ui.driver;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.device.ui.model.TypeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.driver.LinearSegment;
import uk.ac.diamond.daq.experiment.api.driver.MultiSegmentModel;

public abstract class CustomProfileEditor implements ProfileEditor, IModelProvider<MultiSegmentModel> {
	
	private MultiSegmentModel model;
	private TypeEditor<MultiSegmentModel> editor;
	private PropertyChangeListener modelChanged = event -> updatePlot();
	
	private IPlottingSystem<Composite> plottingSystem;
	
	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		
		editor = new TypeEditor<>(this, composite, SWT.NONE);
		
		try {
			editor.setModel(getModel());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		((MultiSegmentComposite) getUI()).setUnits(getQuantityUnits());
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		plottingSystem.createPlotPart(composite, "profile", null, PlotType.XY, null);
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	@Override
	public MultiSegmentModel getModel() throws Exception {
		if (model == null) {
			model = new MultiSegmentModel();
			model.addPropertyChangeListener(modelChanged);
		}
		return model;
	}
	
	@Override
	public void updateModel(MultiSegmentModel model) throws Exception {
		// Don't throw
	}
	
	Object getUI() {
		return editor.getUI();
	}
	
	private void updatePlot() {
		plottingSystem.clear();
		
		// create dataset from model
		List<LinearSegment> segments = model.getSegments();
		if (segments.isEmpty()) return;
		
		double[] x = new double[segments.size()+1];
		double[] y = new double[segments.size()+1];
		
		x[0] = 0;
		y[0] = segments.get(0).getStart();
		
		for (int i = 0; i < segments.size(); i++) {
			x[i+1] = segments.get(i).getDuration() + x[i];
			y[i+1] = segments.get(i).getStop();
		}
		
		final Dataset xDataset = DatasetFactory.createFromObject(x);
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		
		xDataset.setName("Time (min)");
		yDataset.setName(getQuantityName() + " (" + getQuantityUnits() + ")");
		
		plottingSystem.createPlot1D(xDataset, Arrays.asList(yDataset), null);
		plottingSystem.clearAnnotations();
		plottingSystem.setTitle("");
		plottingSystem.setShowLegend(false);
	}
	
	abstract String getQuantityName();
	abstract String getQuantityUnits();

}
