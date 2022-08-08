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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.rcp.ncd.calibration.CalibrationSet;
import gda.rcp.ncd.calibration.views.BraggCalibrationModel.CalibrationListener;
import uk.ac.gda.server.ncd.calibration.CalibrationUpdate.Update;
import uk.ac.gda.server.ncd.calibration.ScanProgress;

public class BraggEdgeSelection extends ViewPart implements IObserver, CalibrationListener {
	private static final Logger logger = LoggerFactory.getLogger(BraggEdgeSelection.class);
	public static final String ID = "gda.rcp.ncd.calibration.views.edgeselection";

	private CheckboxTableViewer beamlineEdgeTable;

	private BraggCalibrationModel service;
	private EdgeTableListener ed = new EdgeTableListener();
	private Button runBeamlineScans;

	@PostConstruct
	public void init() {
		service = getSite().getService(BraggCalibrationModel.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		service.addListener(this);
		parent.setLayout(new GridLayout(1, false));
		beamlineEdgeTable = createEdgeTable(parent, service.availableEdges());
		ed.addTable(beamlineEdgeTable);
		createBeamlineRunControls(parent);
		addSeparator(parent);
	}

	private Composite createComposite(Composite parent, int columns, boolean fillVertically) {
		Composite tmpComp = new Composite(parent, SWT.NONE);
		tmpComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, fillVertically));
		tmpComp.setLayout(new GridLayout(columns, false));
		return tmpComp;
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createBeamlineRunControls(Composite parent) {
		Composite controlComp = createComposite(parent, 2, false);
		runBeamlineScans = new Button(controlComp, SWT.PUSH);
		runBeamlineScans.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 2, 1));
		runBeamlineScans.setText("Run Selected Scans");
		runBeamlineScans.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Running scan: {}", e);
				@SuppressWarnings("unchecked")
				Spliterator<CalibrationSet> edges = ((IStructuredSelection)beamlineEdgeTable.getSelection())
						.spliterator();
				service.scanAll(stream(edges, false)
						.map(CalibrationSet::getEdge)
						.collect(toList()));
			}
		});
	}

	private CheckboxTableViewer createEdgeTable(Composite parent, Collection<CalibrationSet> inputList) {
		Composite tableComp = createComposite(parent, 1, true);
		final CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(tableComp,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK | SWT.MULTI);

		createColumns(tableViewer);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(inputList);

		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableViewer.getControl().setLayoutData(gridData);

		return tableViewer;
	}

	@Override
	public void setFocus() {
	}

	private void createColumns(TableViewer table) {
		createColumn(table, "Edge", new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				var features = (CalibrationSet) element;
				return String.format("%s (%.3f keV)", features.getName(), features.getEdge().getEdgeEnergy());
			}
		});
		createColumn(table, "Scan File", new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				var features = (CalibrationSet) element;
				return features.getDataFile()
						.map(Path::getFileName)
						.map(Path::toString)
						.orElse("");
			}
		});
	}

	private void createColumn(TableViewer table, String name, ColumnLabelProvider clp) {
		TableViewerColumn tableColumn = new TableViewerColumn(table, SWT.NONE);
		final TableColumn col = tableColumn.getColumn();
		col.setText(name);
		col.setWidth(150);
		col.setResizable(true);
		col.setMoveable(true);
		tableColumn.setLabelProvider(clp);
	}

	public void enableRun(final boolean enable) {
		Display.getDefault().syncExec(() -> {
			if (!runBeamlineScans.isDisposed()) {
				runBeamlineScans.setEnabled(enable);
			}
		});
	}

	@Override
	public void dispose() {
		service.removeListener(this);
		super.dispose();
	}

	private class EdgeTableListener implements ICheckStateListener, IDoubleClickListener, ISelectionChangedListener {
		private List<CheckboxTableViewer> tables = new ArrayList<>();

		public void addTable(CheckboxTableViewer table) {
			table.addCheckStateListener(this);
			table.addDoubleClickListener(this);
			table.addSelectionChangedListener(this);
			tables.add(table);
		}

		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			logger.debug("Check state changed: {}", event);
			service.setActiveEdges(stream(beamlineEdgeTable.getCheckedElements()).map(CalibrationSet.class::cast).collect(toList()));
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			ISelection sel = event.getSelection();
			if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
				var iss = (IStructuredSelection) sel;
				var edge = (CalibrationSet) iss.getFirstElement();
				FileDialog fd = new FileDialog(getSite().getShell());
				fd.setText(String.format("Choose scan file for %s", edge.getName()));
				String initial = edge.getDataFile()
						.map(Path::getParent)
						.map(Path::toString)
						.orElse(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
				fd.setFilterPath(initial);
				fd.setFilterExtensions(new String[] { "*.nxs" });

				String scanPath = fd.open();
				if (scanPath != null) {
					edge.setDataFile(scanPath);
				}
				refresh();
				service.newScanData(edge);
			}
		}

		private void refresh() {
			for (TableViewer tv : tables) {
				tv.refresh();
			}
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			var edge = (CalibrationSet)event.getStructuredSelection().getFirstElement();
			if (!edge.equals(service.getSelectedEdge())) {
				service.setSelectedEdge(edge);
			}
		}
	}

	@Override
	public void selectedEdgeChanged(CalibrationSet selected) {
		beamlineEdgeTable.setSelection(new StructuredSelection(selected));
	}

	@Override
	public void newScanData(CalibrationSet calibration) {
		logger.debug("New scan data for {}", calibration);
		Display.getDefault().asyncExec(() -> ed.refresh());
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScanProgress) {
			logger.debug("ScanProgress update");
			var progress = (ScanProgress) arg;
			switch (progress.getType()) {
			case STARTED:
//				scanFiles.remove(progress.getEdgeId());
//				Display.getDefault().asyncExec(() -> beamlineEdgeTable.refresh());
				break;
			case FINISHED:
//				scanFiles.put(progress.getEdgeId(), Paths.get(progress.getScanFile()));
//				Display.getDefault().asyncExec(() -> beamlineEdgeTable.refresh());
				break;
			default:
				break;
			}
		} else if (arg instanceof Update) {
			logger.debug("Update update");
			var update = (Update) arg;
			switch (update) {
			case FAILED:
			case FINISHED:
				Display.getDefault().asyncExec(() -> runBeamlineScans.setEnabled(true));
				break;
			case STARTED:
				Display.getDefault().asyncExec(() -> runBeamlineScans.setEnabled(false));
				break;
			default:
				break;
			}
		}
	}
}