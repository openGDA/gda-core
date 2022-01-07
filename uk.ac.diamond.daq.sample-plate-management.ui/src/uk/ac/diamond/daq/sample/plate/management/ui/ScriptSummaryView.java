/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.sample.plate.management.ui.edit.ScanNameEditingSupport;
import uk.ac.diamond.daq.sample.plate.management.ui.models.ScanModel;
import uk.ac.diamond.daq.sample.plate.management.ui.models.ScanModelProvider;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.ShapeComposite;

public class ScriptSummaryView {
	private static final Logger logger = LoggerFactory.getLogger(ScriptSummaryView.class);

	protected static final String ID = "uk.ac.diamond.daq.sample-plate-management.ui.part.scriptsummary";

	@Inject
	private IEventBroker eventBroker;

	private TableViewer summaryViewer;

	private Table summaryTable;

	private EventHandler generateSummaryHandler = event -> {
		ArrayList<?> scanModels = (ArrayList<?>) event.getProperty(IEventBroker.DATA);
		ScanModelProvider existingScans = ScanModelProvider.INSTANCE;
		for (Object scanElement: scanModels) {
			try {
				ScanModel scanModel = (ScanModel) scanElement;
				boolean exists = false;
				for (ScanModel existingItem: existingScans.getScanModels()) {
					if (scanModel.isLike(existingItem)) {
						exists = true;
						break;
					}
				}
				if (exists) {
					continue;
				}
				existingScans.getScanModels().add(scanModel);
			} catch (ClassCastException e) {
				logger.warn("Invalid element in list of scans sent to summary");
				return;
			}
		}

		List<ScanModel> scansToRemove = new ArrayList<>();
		for (ScanModel existingItem: existingScans.getScanModels()) {
			boolean exists = false;
			for (Object scanElement: scanModels) {
				try {
					ScanModel scanModel = (ScanModel) scanElement;
					if (existingItem.isLike(scanModel)) {
						exists = true;
						break;
					}
				} catch (ClassCastException e) {
					logger.warn("Invalid element in list of scans sent to summary");
					return;
				}
			}
			if (exists) {
				continue;
			}
			scansToRemove.add(existingItem);
		}

		existingScans.getScanModels().removeAll(scansToRemove);
		summaryViewer.refresh();
	};

	@Inject
	public ScriptSummaryView() {
		logger.trace("Constructor called");
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(child);

		summaryViewer = new TableViewer(child, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(summaryViewer.getControl());
		createTableViewerColumn("ID").setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((ScanModel) element).getId());
			}
		});

		TableViewerColumn nameCol = createTableViewerColumn("Scan Name");
		nameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ScanModel) element).getName();
			}
		});

		ScanNameEditingSupport scanNameEditingSupport = new ScanNameEditingSupport(summaryViewer);
		nameCol.setEditingSupport(scanNameEditingSupport);

		createTableViewerColumn("Sequence File").setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((ScanModel) element).getAnalyser());
			}
		});

		createTableViewerColumn("Sample").setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ShapeComposite shapeComposite = (ShapeComposite) ((ScanModel) element).getShapeTabItem().getControl();
				return shapeComposite.getSampleCombo().getText();
			}
		});

		createTableViewerColumn("Shape").setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ShapeComposite shapeComposite = (ShapeComposite) ((ScanModel) element).getShapeTabItem().getControl();
				return shapeComposite.getShapeCombo().getText();
			}
		});

		summaryTable = summaryViewer.getTable();
		summaryTable.setHeaderVisible(true);
		summaryViewer.setContentProvider(new ArrayContentProvider());
		summaryViewer.setInput(ScanModelProvider.INSTANCE.getScanModels());

        ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(summaryViewer) {
            @Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                // Enable editor only with mouse double click
                if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
                    EventObject source = event.sourceEvent;
                    return !(source instanceof MouseEvent && ((MouseEvent)source).button == 3);
                }
                return false;
            }
        };

        TableViewerEditor.create(summaryViewer, null, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL |
            ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR |
            ColumnViewerEditor.TABBING_VERTICAL |
            ColumnViewerEditor.KEYBOARD_ACTIVATION);

        summaryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("cast")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = summaryViewer.getStructuredSelection();
				eventBroker.post(PathscanConfigConstants.TOPIC_SELECT_SCAN, (ScanModel) selection.getFirstElement());
			}
		});

		eventBroker.subscribe(PathscanConfigConstants.TOPIC_GENERATE_SUMMARY, generateSummaryHandler);

		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);

		// Expand both horizontally and vertically
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		logger.trace("Finished building composite");
	}

	private TableViewerColumn createTableViewerColumn(String title) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(summaryViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();

		column.setText(title);
		column.setResizable(true);
		column.setWidth(100);

		return viewerColumn;
	}
}