package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class TriggerDetailControl {
	
	private static final GridDataFactory STRETCH = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);
	
	private static final double TIME_TOLERANCE = 0.1;
	
	private Composite composite;
	
	private List<String> sevs;
	
	private Text target, tolerance, interval;
	private Combo sevCombo;
	
	public void setSevNames(List<String> sevs) {
		this.sevs = sevs;
	}
	
	public Composite update(Composite parent, SignalSource source, ExecutionPolicy mode, TriggerDescriptor model) {
		
		prepareComposite(parent);
		
		if (source == SignalSource.POSITION) {
			if (mode == ExecutionPolicy.SINGLE) {
				return sevSingle(model);
			} else if (mode == ExecutionPolicy.REPEATING) {
				return sevPeriodic(model);
			}
		} else if (source == SignalSource.TIME) {
			if (mode == ExecutionPolicy.SINGLE) {
				return timeSingle(model);
			} else if (mode == ExecutionPolicy.REPEATING) {
				return timePeriodic(model);
			}
		}
		
		throw new IllegalArgumentException("Invalid source / mode combination");
	}
	
	private Composite sevSingle(TriggerDescriptor model) {
		
		createSevCombo(model);
		
		new Label(composite, SWT.NONE).setText("Target");
		target = new Text(composite, SWT.BORDER);
		target.addListener(SWT.Modify, e -> {
			if (model != null) model.setTarget(Double.parseDouble(target.getText()));
		});
		STRETCH.applyTo(target);
		
		new Label(composite, SWT.NONE).setText("Tolerance");
		tolerance = new Text(composite, SWT.BORDER);
		tolerance.addListener(SWT.Modify, e -> {
			if (model != null) model.setTolerance(Double.parseDouble(tolerance.getText()));
		});
		STRETCH.applyTo(tolerance);
		
		if (model != null) {
			target.setText(String.valueOf(model.getTarget()));
			tolerance.setText(String.valueOf(model.getTolerance()));
		}
		
		return composite;
	}
	
	private Composite sevPeriodic(TriggerDescriptor model) {
		
		createSevCombo(model);
		
		new Label(composite, SWT.NONE).setText("Interval");
		interval = new Text(composite, SWT.BORDER);
		interval.addListener(SWT.Selection, e -> {
			if (model != null) model.setInterval(Double.parseDouble(interval.getText()));
		});
		if (model != null) {
			interval.setText(String.valueOf(model.getInterval()));
		}
		STRETCH.applyTo(interval);
		
		return composite;
	}
	
	private Composite timeSingle(TriggerDescriptor model) {
		
		new Label(composite, SWT.NONE).setText("Target (s)");
		target = new Text(composite, SWT.BORDER);
		target.addListener(SWT.Modify, e -> {
			if (model != null) model.setTarget(Double.parseDouble(target.getText()));
		});
		if (model != null) {
			target.setText(String.valueOf(model.getTarget()));
			model.setTolerance(TIME_TOLERANCE);
		}
		STRETCH.applyTo(target);
		
		return composite;
	}
	
	private Composite timePeriodic(TriggerDescriptor model) {
		
		new Label(composite, SWT.NONE).setText("Period");
		interval = new Text(composite, SWT.BORDER);
		interval.addListener(SWT.Selection, e -> {
			if (model != null) model.setInterval(Double.parseDouble(interval.getText()));
		});
		if (model != null) {
			interval.setText(String.valueOf(model.getInterval()));
		}
		STRETCH.applyTo(interval);
		
		return composite;
	}
	
	private Composite prepareComposite(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		STRETCH.applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
		return composite;
	}
	
	private void createSevCombo(TriggerDescriptor model) {
		new Label(composite, SWT.NONE).setText("Environment variable");
		sevCombo = new Combo(composite, SWT.READ_ONLY);
		if (sevs != null) {
			sevCombo.setItems(sevs.toArray(new String[0]));
			if (model != null) {
				sevCombo.select(sevs.indexOf(model.getSampleEnvironmentVariableName()));
			} else {
				sevCombo.select(0);
			}
		}

		sevCombo.addListener(SWT.Selection, e -> {
			if (model != null) model.setSampleEnvironmentVariableName(sevCombo.getText());
		});
		
		STRETCH.applyTo(sevCombo);
	}
	
	protected Text getTarget() {
		return target;
	}
	
	protected Text getTolerance() {
		return tolerance;
	}
	
	protected Text getInterval() {
		return interval;
	}
	
	protected Combo getSevs() {
		return sevCombo;
	}

}
