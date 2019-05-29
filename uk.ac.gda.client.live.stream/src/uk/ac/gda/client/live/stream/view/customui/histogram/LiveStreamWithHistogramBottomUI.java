/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui.histogram;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.customui.AbstractLiveStreamViewCustomUi;

public class LiveStreamWithHistogramBottomUI extends AbstractLiveStreamViewCustomUi {

	private LiveStreamWithHistogramTopUI topUI;
	private Text xRangeLow;
	private Text xRangeHigh;
	private DoubleToStringConverter doubleToStringConverter = new DoubleToStringConverter();

	private HistogramViewer histogramViewer;
	private IAxis xAxis;
	private IAxis yAxis;

	private Job histogramUpdateJob;
	private AtomicReference<IPaletteTrace> activePaletteTrace = new AtomicReference<>(null);

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamWithHistogramBottomUI.class);

	public void setTopUI(LiveStreamWithHistogramTopUI topUI) {
		this.topUI = topUI;
	}

	public LiveStreamWithHistogramTopUI getTopUI() {
		return topUI;
	}

	@Override
	public void createUi(Composite composite) {
		composite.setLayout(new GridLayout());
		createHistogram(composite);
		createHistogramGroup(composite);
	}

	private void createHistogram(Composite parent) {
		try {
			createHistogramViewer(parent);
		} catch (Exception exception) {
			logger.error("Could not create histogram viewer", exception);
			return;
		}

		createHistogramUpdateJob();
		configureHistogramPlot(parent);
		configurePlottingSystem();
	}

	private void createHistogramViewer(Composite parent) throws Exception {
		histogramViewer = new HistogramViewer(parent, "Test", null, null, true);
		histogramViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true,  true).minSize(1, 150).create());
		histogramViewer.setContentProvider(new ImageHistogramProvider());
	}

	private void createHistogramUpdateJob() {
		histogramUpdateJob = new Job("Histogram update") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPaletteTrace trace = activePaletteTrace.get();
				if (trace != null) {
					histogramViewer.setInput(trace);
				}
				return Status.OK_STATUS;
			}
		};
		histogramUpdateJob.setPriority(Job.INTERACTIVE);
	}

	private void configureHistogramPlot(Composite parent) {
		IPlottingSystem<Composite> histoPlot = histogramViewer.getHistogramPlot();
		Collection<ITrace> traces = getPlottingSystem().getTraces(IPaletteTrace.class);

		if (!traces.isEmpty()) {
			IPaletteTrace trace = (IPaletteTrace)traces.iterator().next();

			trace.addPaletteListener(new IPaletteListener.Stub() {

				@Override
				public void minChanged(PaletteEvent event) {
					if (topUI != null) {
						topUI.setImageLow(event.getTrace().getMin().doubleValue());
					}
				}

				@Override
				public void maxChanged(PaletteEvent event) {
					if (topUI != null) {
						topUI.setImageHigh(event.getTrace().getMax().doubleValue());
					}
				}
			});

			histogramViewer.setInput(trace);
			histogramViewer.refresh();
			histoPlot.setShowLegend(false);
			histoPlot.autoscaleAxes();
		}

		xAxis = histoPlot.getSelectedXAxis();
		yAxis = histoPlot.getSelectedYAxis();
		xAxis.setTitle("");
		yAxis.setTitle("");

		Font vSmallFont = new Font(parent.getDisplay(), new FontData("Arial", 1, SWT.NONE));
		xAxis.setTitleFont(vSmallFont);
		yAxis.setTitleFont(vSmallFont);

		xAxis.addAxisListener(new IAxisListener() {

			@Override
			public void revalidated(AxisEvent event) {
				// do nothing
			}

			@Override
			public void rangeChanged(AxisEvent event) {
				setXRangeLow(event.getNewLower());
				setXRangeHigh(event.getNewUpper());
			}
		});
	}

	private void configurePlottingSystem() {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem == null) {
			logger.error("Plotting system could not be created!");
			return;
		}

		plottingSystem.setShowIntensity(false);
		plottingSystem.addTraceListener(new TraceListener());
	}

	private void createHistogramGroup(Composite parent) {
		Group histogramGroup = new Group(parent, SWT.BORDER);
		histogramGroup.setLayout(new GridLayout(7, false));
		histogramGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		histogramGroup.setText("Histogram");

		int boxWidthHint = new BoxWidthHintCalculator().calculate(parent);
		addXRangeLabel(histogramGroup);
		addXRangeLowTextBox(histogramGroup, boxWidthHint);
		addHyphenLabel(histogramGroup);
		addXRangeHighTextBox(histogramGroup, boxWidthHint);
		addDefaultXRangeButton(histogramGroup);
		addLogYAxisButton(histogramGroup);
	}

	private void addXRangeLabel(Group group) {
		Label xRangeLabel = new Label(group, SWT.NONE);
		xRangeLabel.setText("X Range:");
	}

	private void addXRangeLowTextBox(Group group, int boxWidthHint) {
		xRangeLow = new Text(group, SWT.BORDER | SWT.RIGHT);
		xRangeLow.setLayoutData(GridDataFactory.swtDefaults().hint(boxWidthHint, SWT.DEFAULT).create());
		xRangeLow.setToolTipText("Set x-axis low value");
		xRangeLow.addVerifyListener(new DoubleVerifyListener());

		xRangeLow.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				double upper = xAxis.getUpper();
				double lower = yAxis.getLower();

				try {
					upper = Double.parseDouble(xRangeHigh.getText());
				} catch (Exception ex) {
					setXRangeHigh(upper);
				}
				xAxis.setRange(lower, upper);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing

			}
		});

		xRangeLow.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				xRangeHigh.setFocus();
			}
		});

		setXRangeLow(xAxis.getLower());
	}

	private void addHyphenLabel(Group group) {
		Label hyphen = new Label(group, SWT.NONE);
		hyphen.setText("-");
	}

	private void addXRangeHighTextBox(Group group, int boxWidthHint) {
		xRangeHigh = new Text(group, SWT.BORDER | SWT.RIGHT);
		xRangeHigh.setLayoutData(GridDataFactory.swtDefaults().hint(boxWidthHint, SWT.DEFAULT).create());
		xRangeHigh.setToolTipText("Set x-axis high value");
		xRangeHigh.addVerifyListener(new DoubleVerifyListener());

		xRangeHigh.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				double upper = xAxis.getUpper();
				double lower = xAxis.getLower();

				try {
					upper = Double.parseDouble(xRangeHigh.getText());
				} catch (Exception ex) {
					setXRangeHigh(upper);
				}
				xAxis.setRange(lower, upper);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing

			}
		});

		xRangeHigh.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				xRangeLow.setFocus();
			}
		});

		setXRangeHigh(xAxis.getUpper());
	}

	private void addDefaultXRangeButton(Group group) {
		Button useDefaultXRangeButton = new Button(group, SWT.PUSH);
		useDefaultXRangeButton.setLayoutData(GridDataFactory.swtDefaults().create());
		useDefaultXRangeButton.setText("Use Default");

		HistogramDefaultRange[] defaults = new HistogramDefaultRange[] { new HistogramDefaultRange(0, 1600),
				new HistogramDefaultRange(0, 16000) };

		ComboViewer defaultViewer = new ComboViewer(group);
		defaultViewer.setContentProvider(new ArrayContentProvider());
		defaultViewer.setLabelProvider(new LabelProvider());
		defaultViewer.setInput(defaults);
		defaultViewer.getCombo().select(0);

		useDefaultXRangeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)defaultViewer.getSelection();
				HistogramDefaultRange selectedRange = (HistogramDefaultRange)selection.getFirstElement();
				xAxis.setRange(selectedRange.getLower(), selectedRange.getUpper());
			}
		});
	}

	private void addLogYAxisButton(Group group) {
		Button logYAxisButton = new Button(group, SWT.CHECK);
		logYAxisButton.setText("Log Y Axis");
		logYAxisButton.setSelection(yAxis.isLog10());
		logYAxisButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				yAxis.setLog10(logYAxisButton.getSelection());
			}

		});
	}

	private void setXRangeLow(double xRangeLowValue) {
		xRangeLow.setText(doubleToStringConverter.convert(xRangeLowValue));
	}

	private void setXRangeHigh(double xRangeHighValue) {
		xRangeHigh.setText(doubleToStringConverter.convert(xRangeHighValue));
	}

	private class TraceListener extends ITraceListener.Stub {

		@Override
		public void traceUpdated(TraceEvent event) {
			if (!(event.getSource() instanceof IPaletteTrace)) {
				return;
			}

			IPaletteTrace trace = (IPaletteTrace) event.getSource();
			activePaletteTrace.set(trace);
			updateHistogramUIElements(trace);
		}

		@Override
		public void traceAdded(TraceEvent event) {
			IPaletteTrace trace = (IPaletteTrace) event.getSource();
			activePaletteTrace.set(trace);
			updateHistogramUIElements(trace);
		}

		private void updateHistogramUIElements(final IPaletteTrace trace) {
			if (trace == null && histogramViewer != null) {
				histogramViewer.clear();
			}

			if (histogramViewer != null && trace != null && histogramUpdateJob != null) {
				histogramUpdateJob.schedule();
			}
		}
	}
}
