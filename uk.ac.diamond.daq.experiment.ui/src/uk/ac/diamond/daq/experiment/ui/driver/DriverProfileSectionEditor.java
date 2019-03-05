package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

public class DriverProfileSectionEditor implements ElementEditor {
	
	private Text start;
	private Text stop;
	private Text duration;
	private Label startUnits;
	private Label stopUnits;
	
	private DriverProfileSection section;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Start");
		start = new Text(composite, SWT.BORDER);
		start.addListener(SWT.FocusOut, e -> {
			if (section != null) {
				section.setStart(Double.parseDouble(start.getText()));
			}
		});
		STRETCH.applyTo(start);
		startUnits = new Label(composite, SWT.NONE);
		startUnits.setText("   ");
		
		new Label(composite, SWT.NONE).setText("Stop");
		stop = new Text(composite, SWT.BORDER);
		stop.addListener(SWT.FocusOut, e -> {
			if (section != null) {
				section.setStop(Double.parseDouble(stop.getText()));
			}
		});
		STRETCH.applyTo(stop);
		stopUnits = new Label(composite, SWT.NONE);
		stopUnits.setText("   ");
		
		new Label(composite, SWT.NONE).setText("Duration");
		duration = new Text(composite, SWT.BORDER);
		duration.addListener(SWT.FocusOut, e -> {
			if (section != null) {
				section.setDuration(Double.parseDouble(duration.getText()));
			}
		});
		STRETCH.applyTo(duration);
		new Label(composite, SWT.NONE).setText("min");
	}

	@Override
	public void load(EditableWithListWidget element) {
		section = (DriverProfileSection) element;
		
		start.setText(String.valueOf(section.getStart()));
		stop.setText(String.valueOf(section.getStop()));
		duration.setText(String.valueOf(section.getDuration()));
	}

	@Override
	public void clear() {
		section = null;
		
		start.setText("");
		stop.setText("");
		duration.setText("");
	}
	
	public void setUnits(String units) {
		startUnits.setText(units);
		stopUnits.setText(units);
	}

}
