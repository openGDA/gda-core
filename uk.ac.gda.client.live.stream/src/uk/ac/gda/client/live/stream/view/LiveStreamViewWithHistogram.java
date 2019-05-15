/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of LiveStreamView, with a histogram.
 *
 * Can be further extended by implementing {@link #addCustomWidgets(Composite)}
 *
 */
public class LiveStreamViewWithHistogram extends LiveStreamView {

	public static final String ID = "uk.ac.gda.client.live.stream.view.LiveStreamViewWithHistogram";

	private Job updateJob;
	private AtomicReference<IPaletteTrace> activePaletteTrace = new AtomicReference<>(null);
	private HistogramViewer histogramViewer;

	private Text low;
	private Text high;

	private Text imageLow;
	private Text imageHigh;

	private Font vSmallFont;
	private ICustomWidget customWidget = null;

	private static final MathContext PRECISION = new MathContext(6, RoundingMode.HALF_UP);

	private ITraceListener traceListener = new TraceListener();

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewWithHistogram.class);

	/**
	 * Use this method to add custom UI to the stream view with histogram
	 *
	 * Parent has a grid layout
	 *
	 * @param parent
	 */
	protected void addCustomWidgets(Composite parent) {
		if (customWidget != null) {
			customWidget.createWidget(parent);
		} else {
			logger.info("No custom widgets are added.");
		}
	}

	@Override
	protected void createLivePlot(final Composite parent, final String secondaryId) {

		int boxWidthHint = 65;

		boxWidthHint = calculateBoxWidth(parent, boxWidthHint);

		VerifyListener v = buildDoubleVerifyListener();

		parent.setLayout(new GridLayout());

		addCustomWidgets(parent);

		buildImageRangeGroup(parent, boxWidthHint, v);

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		sash.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		sash.setLayout(new GridLayout());

		Composite plotComposite = new Composite(sash, SWT.None);

		plotComposite.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).create());
		plotComposite.setLayout(new FillLayout());

		super.createLivePlot(plotComposite, secondaryId);

		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
		if (plottingSystem == null) {
			logger.error("Plotting system could not be created!");
			return;
		}

		plottingSystem.setShowIntensity(false);
		plottingSystem.addTraceListener(traceListener);

		Composite after = new Composite(sash, SWT.NONE);
		sash.setWeights(new int[] { 70, 30 });
		after.setLayout(new FillLayout());
		after.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).create());

		try {
			histogramViewer = new HistogramViewer(after, "Test", null, (IActionBars) null, true);
		} catch (Exception e) {
			logger.error("Could not create histogram viewer", e);
			return;
		}

		IPlottingSystem<?> histoPlot = histogramViewer.getHistogramPlot();

		histogramViewer.setContentProvider(new ImageHistogramProvider());

		updateJob = new Job("Histo update") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPaletteTrace t = activePaletteTrace.get();
				if (t == null)
					return Status.OK_STATUS;
				histogramViewer.setInput(t);
				return Status.OK_STATUS;
			}
		};

		updateJob.setPriority(Job.INTERACTIVE);

		Collection<ITrace> traces = plottingSystem.getTraces(IPaletteTrace.class);
		if (!traces.isEmpty()) {

			IPaletteTrace next = (IPaletteTrace) traces.iterator().next();
			next.addPaletteListener(new IPaletteListener.Stub() {

				@Override
				public void minChanged(PaletteEvent evt) {
					imageLow.setText(doubleToStringWithPrecision(evt.getTrace().getMin().doubleValue()));
				}

				@Override
				public void maxChanged(PaletteEvent evt) {
					imageHigh.setText(doubleToStringWithPrecision(evt.getTrace().getMax().doubleValue()));
				}
			});

			histogramViewer.setInput(traces.iterator().next());
			histogramViewer.refresh();
			histoPlot.setShowLegend(false);
			histoPlot.autoscaleAxes();
		}

		IAxis xAxis = histoPlot.getSelectedXAxis();
		IAxis yAxis = histoPlot.getSelectedYAxis();

		double lower = xAxis.getLower();
		double upper = xAxis.getUpper();

		// think a title shouldn't be null, so just make it empty with a tiny font.
		// saves quite a bit of space.
		xAxis.setTitle("");
		yAxis.setTitle("");

		vSmallFont = new Font(parent.getDisplay(), new FontData("Arial", 1, SWT.NONE));

		xAxis.setTitleFont(vSmallFont);
		yAxis.setTitleFont(vSmallFont);

		buildHistoGroup(parent, boxWidthHint, v);

		xAxis.addAxisListener(new IAxisListener() {

			@Override
			public void revalidated(AxisEvent evt) {
				// do nothing
			}

			@Override
			public void rangeChanged(AxisEvent evt) {
				double newLower = evt.getNewLower();
				double newUpper = evt.getNewUpper();
				low.setText(doubleToStringWithPrecision(newLower));
				high.setText(doubleToStringWithPrecision(newUpper));
			}
		});

		low.setText(doubleToStringWithPrecision(lower));
		high.setText(doubleToStringWithPrecision(upper));

	}

	@Override
	protected void reopenViewWithSecondaryId(final String secondaryId) {
		final IWorkbenchPage page = getSite().getPage();
		//get the ID of this view which contains custom widgets injected using IViewFactory
		String id2 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePartReference().getId();
		page.hideView(this);
		try {
			page.showView(id2, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Error activatin view with secondary ID {}", secondaryId, e);
		}
	}

	private void buildHistoGroup(Composite parent, int boxWidthHint, VerifyListener v) {
		Group histoGroup = new Group(parent, SWT.BORDER);
		histoGroup.setLayout(new GridLayout(7, false));
		histoGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		histoGroup.setText("Histogram");
		Label l = new Label(histoGroup, SWT.NONE);
		l.setText("X Range:");

		low = makeTextForDouble(histoGroup, boxWidthHint, "Set x-axis low value", v);

		final IAxis xAxis = histogramViewer.getHistogramPlot().getSelectedXAxis();

		low.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				double u = xAxis.getUpper();
				double l = xAxis.getLower();

				try {
					u = Double.parseDouble(high.getText());
				} catch (Exception ex) {
					high.setText(doubleToStringWithPrecision(u));
				}
				xAxis.setRange(l, u);

			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing

			}
		});

		low.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				high.setFocus();
			}
		});

		l = new Label(histoGroup, SWT.NONE);
		l.setText("-");

		high = makeTextForDouble(histoGroup, boxWidthHint, "Set x-axis high value", v);

		high.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				double u = xAxis.getUpper();
				double l = xAxis.getLower();

				try {
					u = Double.parseDouble(high.getText());
				} catch (Exception ex) {
					high.setText(doubleToStringWithPrecision(u));
				}
				xAxis.setRange(l, u);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing

			}
		});

		high.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				low.setFocus();
			}
		});

		HistoDefaultRange[] defaults = new HistoDefaultRange[] { new HistoDefaultRange(0, 1600),
				new HistoDefaultRange(0, 16000) };

		ComboViewer defaultViewer = new ComboViewer(histoGroup);
		defaultViewer.setContentProvider(new ArrayContentProvider());
		defaultViewer.setLabelProvider(new LabelProvider());
		defaultViewer.setInput(defaults);
		defaultViewer.getCombo().select(0);
		defaultViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection && ((StructuredSelection) selection)
						.getFirstElement() instanceof HistoDefaultRange) {
					HistoDefaultRange firstElement = (HistoDefaultRange)((StructuredSelection)selection).getFirstElement();
					histogramViewer.getHistogramPlot().getSelectedXAxis().setRange(firstElement.getLower(), firstElement.getUpper());
				}
			}
		});

		Button log = new Button(histoGroup, SWT.CHECK);
		log.setText("Log Y Axis");
		log.setSelection(histogramViewer.getHistogramPlot().getSelectedYAxis().isLog10());
		log.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				histogramViewer.getHistogramPlot().getSelectedYAxis().setLog10(b.getSelection());
			}

		});

	}

	@Override
	public void dispose() {
		super.dispose();
		if (vSmallFont != null)
			vSmallFont.dispose();
		if (customWidget != null) {
			customWidget.disposeWidget();
		}
	}

	private void buildImageRangeGroup(Composite parent, int boxWidthHint, VerifyListener v) {
		Group imageRangeGroup = new Group(parent, SWT.BORDER);
		imageRangeGroup.setLayout(new GridLayout(5, false));
		imageRangeGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		imageRangeGroup.setText("Image Range");

		imageLow = makeTextForDouble(imageRangeGroup, boxWidthHint, "Set image colour low value", v);

		imageLow.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
				final IImageTrace iTrace = getITrace();
				if (plottingSystem != null && iTrace != null) {
					String text = imageLow.getText();
					try {
						final double min = Double.parseDouble(text);
						iTrace.getImageServiceBean().setMin(min);
						iTrace.setMin(min);
						iTrace.setPaletteData(iTrace.getPaletteData());
						plottingSystem.repaint();
					} catch (Exception ex) {
						if (imageLow != null)
							imageLow.setText(iTrace.getImageServiceBean().getMin().toString());
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing
			}
		});

		imageLow.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				imageHigh.setFocus();
			}
		});

		Label l1 = new Label(imageRangeGroup, SWT.NONE);
		l1.setText("-");

		imageHigh = makeTextForDouble(imageRangeGroup, boxWidthHint, "Set image colour high value", v);

		imageHigh.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
				final IImageTrace iTrace = getITrace();
				if (plottingSystem != null && iTrace != null) {
					String text = imageHigh.getText();
					try {
						final double max = Double.parseDouble(text);
						iTrace.getImageServiceBean().setMax(max);
						iTrace.setMax(max);
						iTrace.setPaletteData(iTrace.getPaletteData());
						plottingSystem.repaint();
					} catch (Exception ex) {
						imageHigh.setText(iTrace.getImageServiceBean().getMin().toString());
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing
			}
		});

		imageHigh.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				imageLow.setFocus();
			}
		});

		Button autoScale = new Button(imageRangeGroup, SWT.CHECK);
		autoScale.setText("Always Autoscale");

		autoScale.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Button b = (Button) e.getSource();
				final IImageTrace iTrace = getITrace();
				if (iTrace != null) {
					iTrace.setRescaleHistogram(b.getSelection());
					if (b.getSelection()) {
						iTrace.rehistogram();
					}
				}
			}

		});

		Button autoScaleOnce = new Button(imageRangeGroup, SWT.PUSH);
		autoScaleOnce.setText("Autoscale");

		autoScaleOnce.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IImageTrace iTrace = getITrace();
				if (iTrace != null) {
					iTrace.rehistogram();
				}
			}

		});
	}

	private VerifyListener buildDoubleVerifyListener() {
		return e -> {
			// Validation for keys like Backspace, left arrow key, right arrow key and del keys
			if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
					|| e.keyCode == SWT.DEL || e.character == '.') {
				e.doit = true;
				return;
			}

			if (e.character == '\0') {
				e.doit = true;
				return;
			}

			if (e.character == '-') {
				e.doit = true;
				return;
			}
			// for scientific notation
			if (e.character == 'e' || e.character == 'E') {
				e.doit = true;
				return;
			}

			if (!('0' <= e.character && e.character <= '9')) {
				e.doit = false;
				return;
			}
		};
	}

	private int calculateBoxWidth(Composite parent, int init) {
		GC g = null;

		try {
			g = new GC(parent);
			init = g.getFontMetrics().getAverageCharWidth() * 9;
		} finally {
			if (g != null) {
				g.dispose();
			}

		}
		return init;
	}

	private class TraceListener extends ITraceListener.Stub {

		@Override
		public void traceUpdated(TraceEvent evt) {
			if (!(evt.getSource() instanceof IPaletteTrace))
				return;
			IPaletteTrace it = (IPaletteTrace) evt.getSource();
			activePaletteTrace.set(it);
			updateHistogramUIElements(it);
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			IPaletteTrace p = (IPaletteTrace) evt.getSource();
			activePaletteTrace.set(p);
			updateHistogramUIElements(p);
		}

		private void updateHistogramUIElements(final IPaletteTrace it) {
			if (it == null && histogramViewer != null) {
				histogramViewer.clear();
			}

			if (histogramViewer != null && it != null && updateJob != null) {
				updateJob.schedule();
			}
		}

	}

	private String doubleToStringWithPrecision(double d) {
		BigDecimal bd = BigDecimal.valueOf(d).round(PRECISION).stripTrailingZeros();
		// stop 100 going to 1.0E2
		if (bd.precision() >= 1 && bd.precision() < PRECISION.getPrecision() && bd.scale() < 0
				&& bd.scale() > (-1 * PRECISION.getPrecision())) {
			bd = bd.setScale(0);
		}
		return bd.toString();
	}

	private Text makeTextForDouble(Composite parent, int widthHint, String toolTip, VerifyListener v) {
		Text t = new Text(parent, SWT.BORDER | SWT.RIGHT);
		GridData d = GridDataFactory.swtDefaults().create();
		d.widthHint = widthHint;
		t.setLayoutData(d);
		t.setToolTipText(toolTip);
		t.addVerifyListener(v);
		return t;
	}

	private class HistoDefaultRange {

		private double lower;
		private double upper;

		public HistoDefaultRange(double lower, double upper) {
			this.lower = lower;
			this.upper = upper;
		}

		@Override
		public String toString() {
			return BigDecimal.valueOf(lower).toString() + " - " + BigDecimal.valueOf(upper).toString();
		}

		public double getLower() {
			return lower;
		}

		public double getUpper() {
			return upper;
		}

	}

	public void setCustomWidget(ICustomWidget customWidget) {
		this.customWidget = customWidget;
	}

}
