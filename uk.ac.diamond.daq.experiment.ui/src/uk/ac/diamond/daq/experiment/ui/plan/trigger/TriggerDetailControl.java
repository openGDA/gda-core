package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerDescriptor.Mode;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerDescriptor.Source;

public class TriggerDetailControl {
	
	private static final GridDataFactory STRETCH = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);
	
	private Composite composite;
	
	private List<String> sevs;
	
	private Text target, tolerance, interval;
	private Combo sevCombo;
	
	public void setSevNames(List<String> sevs) {
		this.sevs = sevs;
	}
	
	public Composite update(Composite parent, Source source, Mode mode, TriggerDescriptor model) {
		
		prepareComposite(parent);
		
		if (source == Source.SEV) {
			if (mode == Mode.SINGLE) {
				return sevSingle(model);
			} else if (mode == Mode.PERIODIC) {
				return sevPeriodic(model);
			}
		} else if (source == Source.TIME) {
			if (mode == Mode.SINGLE) {
				return timeSingle(model);
			} else if (mode == Mode.PERIODIC) {
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
		if (model != null) target.setText(String.valueOf(model.getTarget()));
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
				sevCombo.select(sevs.indexOf(model.getSevName()));
			} else {
				sevCombo.select(0);
			}
		}

		sevCombo.addListener(SWT.Selection, e -> {
			if (model != null) model.setSevName(sevCombo.getText());
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
