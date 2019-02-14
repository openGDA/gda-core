package uk.ac.diamond.daq.experiment.ui.driver;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public abstract class CustomProfileEditor implements ProfileEditor {
	
	private ListWithCustomEditor sections = new ListWithCustomEditor();
	private DriverProfileSectionEditor section = new DriverProfileSectionEditor();
	
	private PropertyChangeListener modelChanged = event -> updatePlot();
	
	private IPlottingSystem<Composite> plottingSystem;
	
	@Override
	public void createControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		Composite listEditorComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).applyTo(listEditorComposite);
		GridLayoutFactory.swtDefaults().applyTo(listEditorComposite);
		
		new Label(listEditorComposite, SWT.NONE).setText("Sections");
		
		sections.setMinimumElements(1);
		sections.setListHeight(150);
		sections.setTemplate(new DriverProfileSection());

		sections.setElementEditor(section);
		sections.create(listEditorComposite);
		section.setUnits(getQuantityUnits());
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		plottingSystem.createPlotPart(composite, "profile", null, PlotType.XY, null);
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		sections.addListListener(modelChanged);
		updatePlot();
	}
	
	private void updatePlot() {
		plottingSystem.clear();
		
		// create dataset from model
		List<DriverProfileSection> segments = getDriverProfile();
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
	
	public List<DriverProfileSection> getDriverProfile() {
		return sections.getList().stream().map(DriverProfileSection.class::cast).collect(Collectors.toList());
	}
	
	public void setDriverProfile(List<DriverProfileSection> profile) {
		sections.setList(profile.stream().map(EditableWithListWidget.class::cast).collect(Collectors.toList()));
	}
	
	abstract String getQuantityName();
	abstract String getQuantityUnits();

	
	@Override
	public List<DriverProfileSection> getProfile() {
		return getDriverProfile();
	}
}
