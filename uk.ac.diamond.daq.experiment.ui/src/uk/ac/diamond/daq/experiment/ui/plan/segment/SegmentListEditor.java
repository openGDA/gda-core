package uk.ac.diamond.daq.experiment.ui.plan.segment;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class SegmentListEditor {
	
	private ListWithCustomEditor listEditor;
	private SegmentEditor segmentEditor;
	
	private final ExperimentPlanBean planBean;
	
	public SegmentListEditor(String experimentId, ExperimentPlanBean planBean) {
		segmentEditor = new SegmentEditor(experimentId);
		this.planBean = planBean;
	}
	
	public Composite createEditorPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Segments");
		
		listEditor = new ListWithCustomEditor();
		listEditor.setMinimumElements(1);
		listEditor.setListHeight(150);
		listEditor.setTemplate(new SegmentDescriptor());
		
		listEditor.setElementEditor(segmentEditor);
		
		if (planBean.getSegments() != null) {
			listEditor.setList(planBean.getSegments().stream().map(EditableWithListWidget.class::cast).collect(Collectors.toList()));
		}
		
		listEditor.addListListener(e -> planBean.setSegments(getSegments()));
		
		listEditor.create(composite);
		
		PropertyChangeListener driverChangeListener = change -> {
			if (change.getPropertyName().equals(ExperimentPlanBean.DRIVER_PROPERTY)) {
				DriverBean driverBean = (DriverBean) change.getNewValue();
				updateReadouts(driverBean != null ? driverBean.getDriver() : null);
			}
		};
		
		planBean.addPropertyChangeListener(driverChangeListener);
		composite.addDisposeListener(dispose -> planBean.removePropertyChangeListener(driverChangeListener));
		
		if (planBean.isDriverUsed()) {
			updateReadouts(planBean.getDriverBean().getDriver());
		}
		
		return composite;
	}
	
	private void updateReadouts(String driverName) {
		if (driverName != null) {
			IExperimentDriver<?> driver = Finder.getInstance().find(driverName);
			segmentEditor.setReadouts(driver.getReadoutNames());
		} else {
			segmentEditor.setReadouts(Collections.emptySet());
		}
	}
	
	public List<SegmentDescriptor> getSegments() {
		return listEditor.getList().stream().map(SegmentDescriptor.class::cast).collect(Collectors.toList());
	}
	
	
}
