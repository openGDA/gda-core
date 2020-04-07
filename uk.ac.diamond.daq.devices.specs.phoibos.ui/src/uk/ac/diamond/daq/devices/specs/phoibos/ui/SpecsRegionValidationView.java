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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;

public class SpecsRegionValidationView {
	private static final Logger logger = LoggerFactory.getLogger(SpecsRegionValidationView.class);

	private final String PART_ID  = "uk.ac.diamond.daq.devices.specs.phoibos.ui.part.regionvalidation";
	private final String DEFAULT_LABEL = "Region Validation";

	private TableViewer regionValidationViewer;
	private Table table;

	private SpecsPhoibosSequenceValidation validationResult;

	@Inject
	private EPartService partService;

	@PostConstruct
	void createView(Composite parent) {
		// Region validation table
		regionValidationViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
	            | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table = regionValidationViewer.getTable();
		createColumns(regionValidationViewer);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		regionValidationViewer.setContentProvider(new ArrayContentProvider());
	}

	private void createColumns(final TableViewer tableViewer) {
		TableViewerColumn otherErrorsCol = createTableViewerColumn(tableViewer, "Validation Messages", 200);
		otherErrorsCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Optional
	@Inject
	private void cacheValidationResults(
		@UIEventTopic(SpecsUiConstants.REGION_VALIDATION_EVENT) SpecsPhoibosSequenceValidation validationResult) {
		this.validationResult = validationResult;
	}

	@Optional
	@Inject
	private void selectedRegionChanged(
			@UIEventTopic(SpecsUiConstants.REGION_SELECTED_EVENT) SpecsPhoibosRegion region) {
		if (validationResult != null) {
			// Delete old table data every time a region is selected
			table.removeAll();
			// Update view with region name
			partService.findPart(PART_ID).setLabel(DEFAULT_LABEL + " - " + region.getName());

			if (validationResult.getRegionsWithErrors().contains(region)) {
				// Input data in table
				regionValidationViewer.setInput(validationResult.getErrorMessagesforRegion(region));
			}
		}
	}

}

