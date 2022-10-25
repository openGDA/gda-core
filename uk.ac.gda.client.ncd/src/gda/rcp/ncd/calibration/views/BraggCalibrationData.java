/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import static java.util.stream.Collectors.toList;
import static org.eclipse.january.dataset.DatasetFactory.createFromList;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.client.UIHelper.showConfirm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.dawnsci.plotting.tools.fitting.FittedFunction;
import org.dawnsci.plotting.tools.fitting.FittedFunctions;
import org.dawnsci.plotting.tools.fitting.FittedPeaksInfo;
import org.dawnsci.plotting.tools.fitting.FittingUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.ncd.calibration.CalibrationSet;
import gda.rcp.ncd.calibration.ObservedFeature;
import gda.rcp.ncd.calibration.views.BraggCalibrationModel.CalibrationListener;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;

public class BraggCalibrationData extends ViewPart implements CalibrationListener {
	private static final Logger logger = LoggerFactory.getLogger(BraggCalibrationData.class);
	public static final String ID = "gda.rcp.ncd.views.CalibrationData";

	private static final String FIT_TRACE = "fitTrace";

	private static final Color FIT_COLOUR = new Color(Display.getDefault(), 0,125,0);

	private static final Color[] ROW_COLOURS = new Color[] {
			new Color(Display.getDefault(), 190,209,194),//DkGreen
			new Color(Display.getDefault(), 218,227,220),//LtGreen
			new Color(Display.getDefault(), 190,209,209),//DkBlue
			new Color(Display.getDefault(), 221,229,230)};//LtBlue
	private static final Color INVALID_INTERCEPT_COLOR = new Color(Display.getDefault(), 255, 180, 180);
	private IPlottingSystem<Composite> calibrationPlot;
	private List<CalibrationSet> edges;
	private CheckboxTableViewer featureTable;
	private Label equation;
	private Composite featureComp;

	private BraggCalibrationModel service;
	private Button calibrate;

	private double calculatedOffset;

	@PostConstruct
	public void init() {
		service = getSite().getService(BraggCalibrationModel.class);
	}

	class RowColour extends ColumnLabelProvider {
		@Override
		public Color getBackground(Object element) {
			var f = (ObservedFeature)element;
			int i;
			for (i = 0; i < edges.size(); i++) {
				var fs = edges.get(i).getFeatures();
				var idx = fs.indexOf(f);
				if (idx != -1) {
					return ROW_COLOURS[2*(i%2) + idx %2];
				}
			}
			return ROW_COLOURS[0]; // should never be reached
		}
	}


	public BraggCalibrationData() {
		try {
			calibrationPlot = PlottingFactory.createPlottingSystem();
			edges = new ArrayList<>();
		} catch (Exception e) {
			logger .error("Could not create calibration plot", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		featureComp = new Composite(parent, SWT.NONE);
		featureComp.setLayoutData(new GridData());
		featureComp.setLayout(new GridLayout(2, false));

		featureTable = CheckboxTableViewer.newCheckList(featureComp, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		featureTable.setContentProvider((IStructuredContentProvider) inputElement -> {
			@SuppressWarnings("unchecked")
			var input = (ArrayList<CalibrationSet>)inputElement;
			return input.stream().flatMap(e -> e.getFeatures().stream()).toArray();
		});
		createColumns(featureTable);
		final Table table = featureTable.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		featureTable.setInput(edges);
		featureTable.addCheckStateListener(event -> {
			var src = (CheckboxTableViewer)event.getSource();
			var f = (ObservedFeature)src.getStructuredSelection().getFirstElement();
			if (f != null) {
				f.setActive(event.getChecked());
			}
			service.featureChanged();
		});
		featureTable.addSelectionChangedListener(event -> {
			for (var e: edges) {
				if (e.getFeatures().contains(event.getStructuredSelection().getFirstElement())) {
					service.setSelectedEdge(e);
					break;
				}
			}
		});
		featureTable.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			@Override
			public boolean isChecked(Object element) {
				return ((ObservedFeature)element).isActive();
			}
		});

		equation = new Label(featureComp, SWT.CENTER | SWT.SINGLE );
		equation.setBackground(new Color(Display.getDefault(), 255,255,255));
		equation.setText("");
		equation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,1));
		calibrate = new Button(featureComp, SWT.PUSH);
		calibrate.setText("Recalibrate");
		calibrate.addSelectionListener(widgetSelectedAdapter(e -> {
				logger.debug("Recalibrating");
				if (showConfirm("Recalibrate bragg offset using intercept of " + calculatedOffset)) {
					service.setNewInterceptValue(calculatedOffset);
				}
		}));

		createCalibrationPlot(parent);

		service.addListener(this);
	}

	private void createCalibrationPlot(Composite parent) {
		calibrationPlot.createPlotPart(parent, "Calibration", null, PlotType.XY, this);
		calibrationPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		calibrationPlot.setTitle("Expected vs Observed Bragg");
		calibrationPlot.setShowLegend(false);

		List<IAxis> axes = calibrationPlot.getAxes();
		IAxis xAxis = axes.get(0);
		IAxis yAxis = axes.get(1);
		xAxis.setTitle("Expected Bragg");
		yAxis.setTitle("Observed Bragg");
	}

	private void createColumns(CheckboxTableViewer table) {
		TableViewerColumn expectedTableColumn = new TableViewerColumn(table, SWT.NONE);
		final TableColumn expectedColumn = expectedTableColumn.getColumn();
		expectedColumn.setText("Expected");
		expectedColumn.setWidth(200);
		expectedColumn.setResizable(true);
		expectedColumn.setMoveable(false);
		expectedTableColumn.setLabelProvider(new RowColour() {
			@Override
			public String getText(Object element) {
				return Double.toString(((ObservedFeature)element).getExpected());
			}
		});

		TableViewerColumn observedTableColumn = new TableViewerColumn(table, SWT.NONE);
		final TableColumn observedColumn = observedTableColumn.getColumn();
		observedColumn.setText("Observed");
		observedColumn.setWidth(200);
		observedColumn.setResizable(true);
		observedColumn.setMoveable(false);
		observedTableColumn.setLabelProvider(new RowColour() {
			@Override
			public String getText(Object element) {
				return Double.toString(((ObservedFeature)element).getObservation());
			}
		});
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void activeEdgeChanged(Collection<CalibrationSet> activeEdges) {
		edges.clear();
		edges.addAll(activeEdges);
		refresh();
		selectedEdgeChanged(service.getSelectedEdge());
	}

	@Override
	public void selectedEdgeChanged(CalibrationSet selected) {
		if (selected != null) {
			var features = selected.getFeatures();
			var current = featureTable.getStructuredSelection().getFirstElement();
			if (!features.isEmpty()) {
				if (current == null || !features.contains(current)) {
					featureTable.setSelection(new StructuredSelection(features.get(0)));
				}
			} else {
				featureTable.setSelection(new StructuredSelection());
			}
		}
	}

	@Override
	public void featureChanged() {
		refresh();
	}

	@Override
	public void newScanData(CalibrationSet calibration) {
		Display.getDefault().asyncExec(this::refresh);
	}

	private void refresh() {
		featureTable.refresh();
		updatePlot();
	}

	private void updatePlot() {
		resetPlot();
		for (var edge: edges) {
			var included = edge.getFeatures().stream().filter(ObservedFeature::isActive).collect(toList());
			if (included.size() > 1) {
				var trace = (ILineTrace)calibrationPlot.createTrace(edge.getName());
				trace.setData(createFromList(included.stream().map(ObservedFeature::getExpected).collect(toList())),
						createFromList(included.stream().map(ObservedFeature::getObservation).collect(toList())));
				trace.setPointStyle(PointStyle.CROSS);
				trace.setTraceType(TraceType.POINT);
				trace.setPointSize(10);
				calibrationPlot.addTrace(trace);
			}
		}
		calibrationPlot.setShowLegend(true);
		calibrationPlot.repaint();
		addFitLine();
		calibrationPlot.repaint();
	}

	private void resetPlot() {
		calibrationPlot.clearTraces();
	}

	@Override
	public void dispose() {
		super.dispose();
		featureTable.getTable().dispose();
		service.removeListener(this);
	}

	private void addFitLine() {
		try {
			var features = edges.stream().flatMap(e -> e.getFeatures().stream()).filter(ObservedFeature::isActive).collect(toList());
			if (features.size() < 2) {
				clearEquation();
				return;
			}

			FittedFunctions bean = null;
			var x = createFromList(features.stream().map(ObservedFeature::getExpected).collect(toList()));
			var y = createFromList(features.stream().map(ObservedFeature::getObservation).collect(toList()));
			bean = FittingUtils.getFittedPolynomial(new FittedPeaksInfo(x, y, new ProgressMonitorWrapper(null), calibrationPlot, null));
			for (FittedFunction fp : bean.getFunctionList()) {
				final Dataset[] pair = fp.getPeakFunctions();
				final ILineTrace trace = TraceUtils.replaceCreateLineTrace(calibrationPlot, FIT_TRACE);

				trace.setData(pair[0], pair[1]);
				trace.setTraceColor(FIT_COLOUR);
				trace.setTraceType(TraceType.DASH_LINE);
				calibrationPlot.addTrace(trace);

				// to work around fit line stopping early
				int points = pair[0].getSize();
				double finalx = 2 * pair[0].getDouble(points - 1) - pair[0].getDouble(points - 2);
				double finaly = 2 * pair[1].getDouble(points - 1) - pair[1].getDouble(points - 2);
				calibrationPlot.append(FIT_TRACE, finalx, finaly, null);

				CompositeFunction func = fp.getFunction();
				double m = func.getParameter(0).getValue();
				double c = func.getParameter(1).getValue();
				logger.debug("eqn: obs = {} * exp + {}", m, c);
				updateEquation(m, c);
				calculatedOffset = c;
			}
		} catch (Exception e) {
			logger.error("Couldn't calculate fit", e);
		}
	}

	private void clearEquation() {
		equation.setText("");
	}

	private void updateEquation(double m, double c) {
		equation.setText(String.format("Intercept = %.5f, gradient = %.5f", c, m));
		if (service.checkInterceptValue(c)) {
			featureComp.setBackground(null);
			calibrate.setEnabled(true);
		} else {
			featureComp.setBackground(INVALID_INTERCEPT_COLOR);
			calibrate.setEnabled(false);
		}
	}
}
