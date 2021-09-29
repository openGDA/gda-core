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

package uk.ac.gda.ui.tool.processing;

import static uk.ac.gda.ui.tool.ClientMessages.ACTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.ADD;
import static uk.ac.gda.ui.tool.ClientMessages.PROCESS;
import static uk.ac.gda.ui.tool.ClientMessages.SELECTION;
import static uk.ac.gda.ui.tool.ClientMessages.SWITCHBACK_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKey;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Displays in a tabular way a set of {@link ProcessingRequest}s
 *
 * <p>
 * This composite centralises, per perspective, the configuration of the processing requests.
 * The user may add one row at time and only if the last added row has been completed or deleted.
 * Once completed each row is able to update the actual acquisition in the controller.
 * </p>
 *
 * @author Maurizio Nagni
 *
 * @see ProcessingRequestRow
 */
public class ProcessingRequestComposite implements CompositeFactory, Reloadable {

	/**
	 * The composite Table
	 */
	private Table table;

	/**
	 * The available processes for the user to select from
	 */
	private final List<ProcessingRequestContext<?>> processingRequestContexts;

	/**
	 * Creates a composite able to add rows based on a user collection
	 *
	 * @param processingRequestContexts
	 */
	public ProcessingRequestComposite(List<ProcessingRequestContext<?>> processingRequestContexts) {
		this.processingRequestContexts = processingRequestContexts;
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		var columnOneContainer = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		var addRow = createClientButton(columnOneContainer, SWT.PUSH, ADD,
				SWITCHBACK_SCAN_TOOLTIP, ClientImages.ADD);
		createClientGridDataFactory().grab(true, true).span(3, 1).applyTo(columnOneContainer);
		addRowSelectionListener(addRow);

		// Creates a table
		table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createClientGridDataFactory().grab(true, true).applyTo(table);
		createTableColumns(table);

		appendMandatoryContexts();
		return table;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reload() {
		table.removeAll();

		List<ProcessingRequestPair<?>> processes = getScanningAcquisition()
			.map(ScanningAcquisition::getAcquisitionConfiguration)
			.map(AcquisitionConfiguration::getProcessingRequest)
			.orElse(Collections.emptyList());

		if (processes.isEmpty()) {
			appendMandatoryContexts();
			return;
		}

		for (var process : processes) {
			ProcessingRequestKey<?> key = getProcessingRequestKeyFactory().getProcessingKey(process.getKey());
			ProcessingRequestContext<?> contextKey = processingRequestContexts.stream()
				.filter(context -> context.getKey().equals(key))
				.findFirst().orElseThrow();
			createRow().configureRow(new ProcessingRequestContext(contextKey, process.getValue()));
		}
	}

	private void appendMandatoryContexts() {
		processingRequestContexts.stream()
			.filter(ProcessingRequestContext::isMandatory)
			.filter(p ->
				p.getDefaultConfiguration() != null && !p.getDefaultConfiguration().isEmpty())
			.forEach(createRow()::configureRow);
	}

	private Function<ProcessingRequestKey<?>, Optional<ProcessingRequestContext<?>>> getProcessingRequestContext() {
		return this::getProcessingRequestContext;
	}

	private Optional<ProcessingRequestContext<?>> getProcessingRequestContext(ProcessingRequestKey<?> key) {
		return processingRequestContexts.stream()
		.filter(Predicate.not(ProcessingRequestContext::isMandatory))
		.filter(p -> p.getKey().equals(key))
		.findFirst();
	}

	private ProcessingRequestRow createRow() {
		var row = new ProcessingRequestRow(table, getProcessingRequestKeys(), getProcessingRequestContext());
		table.getShell().layout(true, true);
		return row;
	}

	private void addRowSelectionListener(Button addRow) {
		Listener selectionListener = e -> {
			// Does not add a new row if the previous is not complete and rows is not empty
			if (!rowsComplete()) {
				UIHelper.showWarning("Cannot add new row", "There is one incomplete row");
				return;
			}
			createRow();
		};
		addRow.addListener(SWT.Selection, selectionListener);
	}

	private List<ProcessingRequestKey<?>> getProcessingRequestKeys() {
		return processingRequestContexts.stream()
			.map(ProcessingRequestContext::getKey)
			.collect(Collectors.toList());
	}

	private boolean rowsComplete() {
		boolean isEmpty = table.getItems().length == 0;
		long incompletes = Arrays.stream(table.getItems())
				.filter(i -> i.getData() == null)
				.count();
		return isEmpty || incompletes == 0;
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { PROCESS, SELECTION, ACTIONS };
		IntStream.range(0, headers.length).forEach(c -> {
			var column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}

	private Optional<ScanningAcquisition> getScanningAcquisition() {
		return SpringApplicationContextFacade.getBean(ClientSpringContext.class).getAcquisitionController()
				.map(AcquisitionController::getAcquisition);
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return SpringApplicationContextFacade.getBean(ProcessingRequestKeyFactory.class);
	}
}
