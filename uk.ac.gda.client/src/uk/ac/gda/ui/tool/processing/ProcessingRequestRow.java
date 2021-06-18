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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.client.viewer.ThreeStateDisplay;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.context.handler.ProcessingRequestContextHandlerService;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKey;

/**
 * A single row into a {@link ProcessingRequestComposite} instance.
 *
 * <p>
 * Each row is composed by three columns:
 *<ul>
 * <li>
 * a combo to select the process type
 * </li>
 * <li>
 * a {@link ThreeStateDisplay} element to display the selected/not selected state
 * </li>
 * <li>
 * a {@link ToolBar} with one select (a file) button, and a delete row
 * </li>
 * </ul>
 * </p>
 * <p>
 * The row rules are:
 * <ul>
 * <li>
 * no selection in the combo disable the {@code ToolBar}. The {@code ThreeStateDisplay} is yellow (not set).
 * {@code #getProcessingPair()} returns {@code null}
 * </li>
 * <li>
 * selecting an item in the combo enable the {@code ToolBar}. The {@code ThreeStateDisplay} is yellow (not set)
 * {@code #getProcessingPair()} returns {@code null}
 * </li>
 * <li>
 * selecting a file using the select buttton in the {@code ToolBar} disables further selections from the combo.
 * The {@code ThreeStateDisplay} is green (set).  * {@code #getProcessingPair()} returns not {@code null}
 * </li>
 * </ul>
 * </p>
 *
 *
 * <p>
 * If {@link ProcessingRequestContextHandlerService} is available through Spring, the row can
 * add or remove elements from the active the {@link ScanningAcquisition}
 * </p>
 * @author Maurizio Nagni
 *
 * @see ProcessingRequestComposite
 */
class ProcessingRequestRow {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingRequestRow.class);

	private final Table table;
	private final TableItem tableItem;

	private CCombo comboKeys;
	private ThreeStateDisplay state;
	private Composite columnTwoContainer;
	private ToolItem selectItem;
	private ToolBar toolBar;
	/**
	 * Contains the elements added to the table item. This is a workaround as in the delete row action
	 * as the call table.remove(TableItem) does not remove the tableItems from the table itself.
	 */
	private List<Control> controls = new ArrayList<>();

	private final List<ProcessingRequestKey<?>> processingRequestKeys;
	private final Function<ProcessingRequestKey<?>, Optional<ProcessingRequestContext<?>>> contextFinder;

	/**
	 * The final processing request instance to inject into the {@link ScanningConfiguration#getProcessingRequest()}
	 */
	private ProcessingRequestPair<?> processingPair;
	private ProcessingRequestContext<?> selectedContext;

	/**
	 * @param table where append the row
	 * @param processingRequestKeys the keys used to populate the processes combo box
	 * @param contextFinder the function to retrieve, from the composite, the context associated with the selected key
	 */
	public ProcessingRequestRow(Table table, List<ProcessingRequestKey<?>> processingRequestKeys, Function<ProcessingRequestKey<?>,
			Optional<ProcessingRequestContext<?>>> contextFinder) {
		this.table = table;
		table.getChildren();
		this.tableItem = new TableItem(table, SWT.NONE);

		// here we use the controls property
		tableItem.addDisposeListener(e -> controls.forEach(Control::dispose) );

		this.processingRequestKeys = processingRequestKeys;
		this.contextFinder = contextFinder;
		createRow();
	}

	private void populateCombo() {
		processingRequestKeys.stream()
		.forEach(key -> {
			logger.debug("Populate ProcessingRequestRow");
			String label = ClientMessagesUtility.getMessage(key.getLabel());
			comboKeys.add(label);
			comboKeys.setData(label, key);
		});
	}

	private void createRow() {

		//---------- Combo column --------------
		var columnZero = new TableEditor(table);

		comboKeys = new CCombo (table, SWT.NONE);
		comboKeys.setEditable(false);
		populateCombo();
		columnZero.grabHorizontal = true;
		columnZero.setEditor(comboKeys, tableItem, 0);
		controls.add(comboKeys);

		//---------- ThreeStateDisplay column --------------
		var columnOne = new TableEditor(table);
		columnOne.grabHorizontal = true;
		var columnOneContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 1);

		columnOne.setEditor(columnOneContainer, tableItem, 1);
		controls.add(columnOneContainer);
		state = new ThreeStateDisplay(columnOneContainer, "Set", "Not set", "");
		state.setYellow();

		//---------- ToolBar column --------------
		columnTwoContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 2);
		toolBar = new ToolBar (columnTwoContainer, SWT.BORDER);
		toolBar.setEnabled(false);
		addSelectionListener(toolBar);
		var columnTwo = new TableEditor(table);
		columnTwo.grabHorizontal = true;
		columnTwo.setEditor(columnTwoContainer, tableItem, 2);
		controls.add(columnTwoContainer);

		setSelectItem(new ToolItem(toolBar, SWT.PUSH));
		getSelectItem().setImage(ClientSWTElements.getImage(ClientImages.SELECT_DOCUMENT));
		getSelectItem().setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.SELECT_PROCESSING_FILE_TP));
		getSelectItem().addSelectionListener(widgetSelectedAdapter(this::selectProcessingDocument));

		var deleteItem = new ToolItem (toolBar, SWT.PUSH);
		deleteItem.setImage(ClientSWTElements.getImage(ClientImages.DELETE));
		deleteItem.setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.REMOVE_SELECTION_TP));
		addDeleteListener(deleteItem);
	}

	/**
	 * when a combo item is selected...
	 * @param toolBar
	 */
	private void addSelectionListener(ToolBar toolBar) {
		Listener selectionListener = e -> {
			// Set the combo tooltip (process dependent)
			comboKeys.setToolTipText(ClientMessagesUtility.getMessage(getSelectedProcessingRequestKey().getTooltip()));

			contextFinder.apply(getSelectedProcessingRequestKey())
				.ifPresent(this::setSelectedContext);

			if (getSelectedContext() != null) {
				// Enable the tool bar
				toolBar.setEnabled(true);
			}
		};
		comboKeys.addListener(SWT.Selection, selectionListener);
	}

	private String getSelectedComboKey() {
		return comboKeys.getItems()[comboKeys.getSelectionIndex()];
	}

	private ProcessingRequestKey<?> getSelectedProcessingRequestKey() {
		return WidgetUtilities.getDataObject(comboKeys, ProcessingRequestKey.class, getSelectedComboKey());
	}

	private void selectProcessingDocument(SelectionEvent event) {
		// Delegate the ProcessingRequestRowHelper to find the appropriate class to create the appropriate dialog to create a processingRequestPair
		Optional.ofNullable(getProcessingRequestContextHandlerService()
				.handle(getSelectItem().getParent().getShell(), getSelectedContext()))
		.ifPresent(this::setProcessingPair);
	}

	private void setProcessingPair(ProcessingRequestPair<?> processingPair) {
		this.processingPair = processingPair;
		// This setData is a trick to inform the ProcessingRequestComposite that the row is complete
		// see ProcessingRequestComposite#rowsComplete()
		tableItem.setData(processingPair);

		state.setToolTipText(getProcessingRequestContextHandlerService().assembleTooltip(processingPair));

		state.setGreen();
		toolBar.setEnabled(true);
		comboKeys.setEnabled(false);
	}

	/**
	 * When a row is deleted...
	 * @param deleteItem
	 */
	private void addDeleteListener(ToolItem deleteItem) {
		Listener selectionListener = e -> {
			TableItem[] items = table.getItems();
			for (int index = 0 ; index < items.length ; index ++) {
				if (this.tableItem.equals(items[index])) {
					table.remove(index);
					table.getShell().layout(true, true);
					getProcessingRequestContextHandlerService().removeProcessingRequest(processingPair);
					break;
				}
			}
		};
		deleteItem.addListener(SWT.Selection, selectionListener);
	}

	/**
	 * This method is used exclusively by the {@link ProcessingRequestComposite} to inject a mandatory {@link ProcessingRequestContext}
	 * @param processingContext
	 */
	<T> void configureRow(ProcessingRequestContext<T> processingContext) {
		setSelectedContext(processingContext);
		List<T> defaultConfiguration = processingContext.getDefaultConfiguration();
		if (defaultConfiguration == null || defaultConfiguration.isEmpty())
			return;

		String label = ClientMessagesUtility.getMessage(processingContext.getKey().getLabel());
		int index = ArrayUtils.indexOf(comboKeys.getItems(), label);
		comboKeys.select(index);
		ProcessingRequestKey<?> processingKey = processingContext.getKey();
		comboKeys.setToolTipText(ClientMessagesUtility.getMessage(processingKey.getTooltip()));

		Optional.ofNullable(getProcessingRequestContextHandlerService().handle(processingContext))
			.ifPresent(this::setProcessingPair);

		toolBar.setEnabled(!processingContext.isMandatory());
		if (processingContext.isMandatory())
			ClientSWTElements.createClientLabel(columnTwoContainer, SWT.NONE, "Madatory Process");
	}

	public ProcessingRequestPair<?> getProcessingPair() {
		return processingPair;
	}

	private ToolItem getSelectItem() {
		return selectItem;
	}

	private void setSelectItem(ToolItem selectItem) {
		this.selectItem = selectItem;
	}

	private ProcessingRequestContext<?> getSelectedContext() {
		return selectedContext;
	}

	private void setSelectedContext(ProcessingRequestContext<?> selectedContext) {
		this.selectedContext = selectedContext;
	}

	private ProcessingRequestContextHandlerService getProcessingRequestContextHandlerService() {
		return SpringApplicationContextFacade.getBean(ProcessingRequestContextHandlerService.class);
	}
}