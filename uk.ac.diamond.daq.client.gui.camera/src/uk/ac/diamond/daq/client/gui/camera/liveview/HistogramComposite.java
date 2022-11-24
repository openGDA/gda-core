/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.client.gui.camera.liveview;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientSWTElements.getImage;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyDoubleText;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Optional;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.tool.images.ClientImages;

public class HistogramComposite  {

	private static final Logger logger = LoggerFactory.getLogger(HistogramComposite.class);

	private final IPlottingSystem<Composite> plot;
	private final GridDataFactory stretch = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
	private final DecimalFormat numeric = new DecimalFormat("#.##");

	private Text regionMin;
	private Text regionMax;

	private Button autoRehistogram;

	private HistogramViewer histogram;

	public HistogramComposite(IPlottingSystem<Composite> plot) {
		this.plot = plot;
	}

	public void createComposite(Composite parent) {
		var composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		createHistogramControls(composite);
		createHistogramViewer(composite);
		createAxisControls(composite);
	}

	/**
	 * Creates min/max text boxes,
	 * and rehistogram buttons (one-off and automatic)
	 */
	private void createHistogramControls(Composite parent) {
		var regionGroup = new Group(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(5).applyTo(regionGroup);
		stretch.applyTo(regionGroup);

		regionGroup.setText("Histogram range");

		regionMin = new Text(regionGroup, SWT.BORDER);
		stretch.applyTo(regionMin);

		regionMin.addVerifyListener(verifyOnlyDoubleText);
		regionMin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				getImageTrace().ifPresent(trace -> {
					double min = Double.parseDouble(regionMin.getText());
					trace.getImageServiceBean().setMin(min);
				});
			}
		});
		regionMin.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				regionMax.setFocus();
			}
		});

		new Label(regionGroup, SWT.NONE).setText(":");

		regionMax = new Text(regionGroup, SWT.BORDER);
		stretch.applyTo(regionMax);

		regionMax.addVerifyListener(verifyOnlyDoubleText);
		regionMax.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				getImageTrace().ifPresent(trace -> {
					double max = Double.parseDouble(regionMax.getText());
					trace.getImageServiceBean().setMax(max);
				});
			}
		});
		regionMax.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				regionMin.setFocus();
			}
		});

		var rehistogram = new Button(regionGroup, SWT.PUSH);
		rehistogram.setToolTipText("Rehistogram");
		rehistogram.setImage(getImage(ClientImages.EXPAND));
		GridDataFactory.swtDefaults().applyTo(rehistogram);

		rehistogram.addSelectionListener(widgetSelectedAdapter(selection -> getImageTrace().ifPresent(IImageTrace::rehistogram)));

		autoRehistogram = new Button(regionGroup, SWT.TOGGLE);
		autoRehistogram.setText("Auto rehistogram");
		autoRehistogram.setToolTipText("Automatic histogram range");

		// we just disable/enable other widgets here:
		// auto rehistogramming logic in HistogramUpdater
		autoRehistogram.addSelectionListener(widgetSelectedAdapter(event -> {
			var selected = autoRehistogram.getSelection();
			regionMin.setEnabled(!selected);
			regionMax.setEnabled(!selected);
			rehistogram.setEnabled(!selected);
			var image = selected ? ClientImages.UNLOCK : ClientImages.LOCK;
			autoRehistogram.setImage(getImage(image));
		}));

		autoRehistogram.setSelection(true);
		autoRehistogram.notifyListeners(SWT.Selection, new Event());

		GridDataFactory.swtDefaults().applyTo(autoRehistogram);
	}

	private Optional<IImageTrace> getImageTrace() {
		Collection<ITrace> traces = plot.getTraces(IImageTrace.class);
		if (traces.isEmpty()) return Optional.empty();
		return Optional.of((IImageTrace) traces.iterator().next());
	}

	private void createHistogramViewer(Composite parent) {
		try {
			// create histogram only: we take care of the remaining controls
			histogram = new HistogramViewer(parent, null, null, null, true);
		} catch (Exception e) {
			var errorMessage = "Failed to create histogram viewer";
			logger.error(errorMessage, e);
			new Label(parent, SWT.NONE).setText(errorMessage);
		}

		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(histogram.getComposite());
		histogram.setContentProvider(new ImageHistogramProvider());
		histogram.getHistogramPlot().setShowLegend(false);

		var histogramUpdater = new HistogramUpdater();
		plot.addTraceListener(histogramUpdater);
		parent.addDisposeListener(dispose -> plot.removeTraceListener(histogramUpdater));
	}

	/**
	 * Creates buttons to toggle between log/linear y axis,
	 * and a button to auto scale axes
	 */
	private void createAxisControls(Composite parent) {
		var group = new Group(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(group);
		stretch.applyTo(group);

		new Label(group, SWT.NONE).setText("Y axis");

		var logarithmic = new Button(group, SWT.RADIO);
		logarithmic.setText("Logarithmic");

		var linear = new Button(group, SWT.RADIO);
		linear.setText("Linear");

		var autoScale = new Button(group, SWT.PUSH);
		autoScale.setImage(getImage(ClientImages.AUTOSCALE));
		autoScale.setToolTipText("Autoscale axes");
		autoScale.addSelectionListener(widgetSelectedAdapter(selection -> autoScaleAxes()));

		IAxis x = histogram.getHistogramPlot().getSelectedXAxis();
		IAxis y = histogram.getHistogramPlot().getSelectedYAxis();
		x.setTitle("");
		y.setTitle("");

		// we do not want negative quadrants
		x.setMaximumRange(0, 65536);
		y.setMaximumRange(0, Double.MAX_VALUE);

		if (y.isLog10()) {
			logarithmic.setSelection(true);
		} else {
			linear.setSelection(true);
		}

		logarithmic.addSelectionListener(widgetSelectedAdapter(selection -> y.setLog10(true)));
		linear.addSelectionListener(widgetSelectedAdapter(selection -> y.setLog10(false)));
	}

	/**
	 * Autoscale ensuring positive axes
	 */
	private void autoScaleAxes() {
		histogram.getHistogramPlot().autoscaleAxes();
		IAxis x = histogram.getHistogramPlot().getSelectedXAxis();
		IAxis y = histogram.getHistogramPlot().getSelectedYAxis();

		x.setRange(x.getLower() < 0 ? 0 : x.getLower(), x.getUpper());
		y.setRange(y.getLower() < 0 ? 0 : y.getLower(), y.getUpper());

		histogram.refresh();
	}


	private class HistogramUpdater extends ITraceListener.Stub {

		private boolean configured = false;
		private final IPaletteListener paletteListener;

		public HistogramUpdater() {
			paletteListener = new SimplePaletteListener();
		}

		@Override
		protected void update(TraceEvent evt) {
			getImageTrace().ifPresent(trace -> {
				trace.addPaletteListener(paletteListener);

				if (autoRehistogram.getSelection()) {
					trace.rehistogram();
				}

				histogram.setInput(trace);
				histogram.refresh();

				if (!configured) { // force an auto scale once
					autoScaleAxes();
					configured = true;
				}
			});
		}

		private class SimplePaletteListener extends IPaletteListener.Stub {

			@Override
			public void minChanged(PaletteEvent event) {
				setMin(event.getTrace().getMin().doubleValue());
			}

			@Override
			public void maxChanged(PaletteEvent event) {
				setMax(event.getTrace().getMax().doubleValue());
			}

			private void setMin(double min) {
				regionMin.setText(numeric.format(min));
			}

			private void setMax(double max) {
				regionMax.setText(numeric.format(max));
			}
		}

	}
}