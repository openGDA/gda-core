package uk.ac.diamond.daq.client.gui.camera.liveview;

import java.util.Collection;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class HistogramComposite extends Composite {

	private HistogramViewer histogram;

	public HistogramComposite(Composite parent, @SuppressWarnings("rawtypes") IPlottingSystem plottingSystem, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		try {

			histogram = new HistogramViewer(this);
			Collection<ITrace> traces = plottingSystem.getTraces(IPaletteTrace.class);
			histogram.setContentProvider(new ImageHistogramProvider());
			histogram.setInput(traces.iterator().next());
			histogram.refresh();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(histogram.getComposite());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Composite statisticsPanel = new Composite(this, SWT.None);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(statisticsPanel);

		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(statisticsPanel);

		Label label;

		label = new Label(statisticsPanel, SWT.LEFT);
		label.setText("Max:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label maxLabel = new Label(statisticsPanel, SWT.LEFT);
		maxLabel.setText("---");
		GridDataFactory.swtDefaults().applyTo(maxLabel);

		label = new Label(statisticsPanel, SWT.LEFT);
		GridDataFactory.swtDefaults().span(2, 0).applyTo(label);

		label = new Label(statisticsPanel, SWT.LEFT);
		label.setText("Min:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label minLabel = new Label(statisticsPanel, SWT.LEFT);
		minLabel.setText("---");
		GridDataFactory.swtDefaults().applyTo(minLabel);

		label = new Label(statisticsPanel, SWT.LEFT);
		GridDataFactory.swtDefaults().span(2, 0).applyTo(label);

		label = new Label(statisticsPanel, SWT.LEFT);
		label.setText("Mean:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label meanLabel = new Label(statisticsPanel, SWT.LEFT);
		meanLabel.setText("---");
		GridDataFactory.swtDefaults().applyTo(meanLabel);

		label = new Label(statisticsPanel, SWT.LEFT);
		label.setText("Std Dev:");

		GridDataFactory.swtDefaults().applyTo(label);
		Label stdDevLabel = new Label(statisticsPanel, SWT.LEFT);
		stdDevLabel.setText("--.---");
		GridDataFactory.swtDefaults().applyTo(stdDevLabel);
	}

}
