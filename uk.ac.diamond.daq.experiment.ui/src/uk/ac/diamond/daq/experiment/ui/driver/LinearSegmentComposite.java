package uk.ac.diamond.daq.experiment.ui.driver;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LinearSegmentComposite extends Composite {
	
	private static final GridDataFactory RIGHT_ALIGN = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
	private static final GridDataFactory STRETCH = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
	
	private ScaleBox start, stop, duration;
	private Label startUnits, stopUnits;
	
	public LinearSegmentComposite(Composite parent) {
		super(parent, SWT.NONE);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		
		Label lblStart = new Label(composite, SWT.NONE);
		lblStart.setText("Start");
		RIGHT_ALIGN.applyTo(lblStart);
		
		start = new ScaleBox(composite, SWT.BORDER);
		start.setMaximum(1000);
		start.setMinimum(-1000);
		STRETCH.applyTo(start);
		
		startUnits = new Label(composite, SWT.NONE);
		startUnits.setText("   ");
		
		Label lblStop = new Label(composite, SWT.NONE);
		lblStop.setText("Stop");
		RIGHT_ALIGN.applyTo(lblStop);
		
		stop = new ScaleBox(composite, SWT.BORDER);
		stop.setMaximum(1000);
		stop.setMinimum(-1000);
		STRETCH.applyTo(stop);
		
		stopUnits = new Label(composite, SWT.NONE);
		stopUnits.setText("   ");
		
		Label lblDuration = new Label(composite, SWT.NONE);
		lblDuration.setText("Duration");
		RIGHT_ALIGN.applyTo(lblDuration);
		
		duration = new ScaleBox(composite, SWT.BORDER);
		STRETCH.applyTo(duration);
		
		new Label(composite, SWT.NONE).setText("min");
	}
	
	public ScaleBox getStart() {
		return start;
	}
	
	public ScaleBox getStop() {
		return stop;
	}

	public ScaleBox getDuration() {
		return duration;
	}
	
	public void setUnits(String units) {
		startUnits.setText(units);
		stopUnits.setText(units);
	}
}
