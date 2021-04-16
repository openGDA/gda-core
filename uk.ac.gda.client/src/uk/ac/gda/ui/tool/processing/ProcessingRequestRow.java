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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
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
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.client.viewer.ThreeStateDisplay;
import uk.ac.gda.client.widgets.SelectFileToolItem;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

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
 * If {@link AcquisitionController} is available through Spring, the row can automatically update the {@link ScanningAcquisition}
 * with the selected ProcessingRequestPair
 * </p>
 * @author Maurizio Nagni
 *
 * @see ProcessingRequestComposite
 */
class ProcessingRequestRow {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingRequestRow.class);

	private final Table table;
	private final TableItem tableItem;

	private CCombo combo;
	private ThreeStateDisplay state;
	private Composite columnTwoContainer;
	private SelectFileToolItem selectToolItem;
	private ToolBar toolBar;
	/**
	 * Contains the elements added to the table item. This is a workaround as in the delete row action
	 * as the call table.remove(TableItem) does not remove the tableItems from the table itself.
	 */
	private List<Control> controls = new ArrayList<>();

	private final Supplier<List<ProcessingRequestContext>> processingRequestContexts;

	/**
	 * The final processing request instance to inject into the {@link ScanningConfiguration#getProcessingRequest()}
	 */
	private ProcessingRequestPair<URL> processingPair;

	/**
	 * @param table
	 *            the {@link Table} where attach the {@link TableItem}
	 * @param processingRequestContexts
	 *            the available processing items
	 */
	public ProcessingRequestRow(Table table, Supplier<List<ProcessingRequestContext>> processingRequestContexts) {
		this.table = table;
		table.getChildren();
		this.tableItem = new TableItem(table, SWT.NONE);

		// here we use the controls property
		tableItem.addDisposeListener(e -> controls.forEach(Control::dispose) );

		this.processingRequestContexts = processingRequestContexts;
		addColumns();
	}

	public ProcessingRequestPair<URL> getProcessingPair() {
		return processingPair;
	}

	/**
	 * This method is used exclusively by the {@link ProcessingRequestComposite} to inject a mandatory {@link ProcessingRequestContext}
	 * @param context
	 */
	void configureRow(ProcessingRequestContext context) {
		List<URL> defaultConfiguration = context.getDefaultConfiguration();
		if (defaultConfiguration == null || defaultConfiguration.isEmpty())
			return;

		String label = ClientMessagesUtility.getMessage(context.getKey().getLabel());
		int index = ArrayUtils.indexOf(combo.getItems(), label);
		combo.select(index);
		ProcessingRequestKey processingKey = getSelectedProcessingRequestContext(label).getKey();
		combo.setToolTipText(ClientMessagesUtility.getMessage(processingKey.getTooltip()));
		updateSelectedURL(defaultConfiguration.get(0));
		toolBar.setEnabled(!context.isMandatory());
		if (context.isMandatory())
			ClientSWTElements.createClientLabel(columnTwoContainer, SWT.NONE, "Madatory Process");
	}

	private void populateCombo() {
		processingRequestContexts.get().stream()
		.forEach(context -> {
			logger.debug("Populate ProcessingRequestRow");
			ProcessingRequestKey key = context.getKey();
			String label = ClientMessagesUtility.getMessage(key.getLabel());
			combo.add(label);
			combo.setData(label, context);
		});
	}

	private void addColumns() {

		//---------- Combo column --------------
		TableEditor columnZero = new TableEditor(table);

		combo = new CCombo (table, SWT.NONE);
		combo.setEditable(false);
		populateCombo();
		columnZero.grabHorizontal = true;
		columnZero.setEditor(combo, tableItem, 0);
		controls.add(combo);

		//---------- ThreeStateDisplay column --------------
		TableEditor columnOne = new TableEditor(table);
		columnOne.grabHorizontal = true;
		Composite columnOneContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 1);

		columnOne.setEditor(columnOneContainer, tableItem, 1);
		controls.add(columnOneContainer);
		state = new ThreeStateDisplay(columnOneContainer, "Set", "Not set", "");
		state.setYellow();

		//---------- ToolBar column --------------
		columnTwoContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 2);
		toolBar = new ToolBar (columnTwoContainer, SWT.BORDER);
		toolBar.setEnabled(false);
		addSelectionListener(toolBar);
		TableEditor columnTwo = new TableEditor(table);
		columnTwo.grabHorizontal = true;
		columnTwo.setEditor(columnTwoContainer, tableItem, 2);
		controls.add(columnTwoContainer);
		selectToolItem = new SelectFileToolItem(toolBar, ClientMessages.SELECT_PROCESSING_FILE_TP, null, this::getDirPath, this::updateSelectedURL);
		ToolItem deleteItem = new ToolItem (toolBar, SWT.PUSH);
		deleteItem.setImage(ClientSWTElements.getImage(ClientImages.DELETE));
		deleteItem.setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.REMOVE_SELECTION_TP));
		addDeleteListener(deleteItem);
	}

	private void updateSelectedURL(URL selectedURL) {
		List<URL> selections = new ArrayList<>();
		selections.add(selectedURL);
		state.setToolTipText(selections.stream()
				.map(URL::getPath)
				.collect(Collectors.joining(", ")));
		state.setGreen();
		toolBar.setEnabled(true);
		combo.setEnabled(false);
		processingPair = createProcessingRequestPair(selections);
		addProcessingRequest(processingPair);
		// This setData is a trick to inform the ProcessingRequestComposite that the row is complete
		// see ProcessingRequestComposite#rowsComplete()
		tableItem.setData(processingPair);
	}

	private final ProcessingRequestPair<URL> createProcessingRequestPair(List<URL> selections) {
		String key = combo.getItems()[combo.getSelectionIndex()];
		try {
			return getSelectedProcessingRequestContext(key).getKey()
				.getBuilder()
				.getConstructor()
				.newInstance()
				.withValue(selections)
				.build();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			logger.error("Error creating ProcessingRequestBuilder for key: {}", key, e);
		}
		return null;
	}

	/**
	 * when a combo item is selected...
	 * @param toolBar
	 */
	private void addSelectionListener(ToolBar toolBar) {
		Listener selectionListener = e -> {
			// Extract the selected item label from the combo
			String key = combo.getItems()[combo.getSelectionIndex()];
			// Set the combo tooltip (process dependent)
			combo.setToolTipText(ClientMessagesUtility.getMessage(getSelectedProcessingRequestContext(key).getKey().getTooltip()));
			// Enable the tool bar
			toolBar.setEnabled(true);
		};
		combo.addListener(SWT.Selection, selectionListener);
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
					removeProcessingRequest(processingPair);
					break;
				}
			}
		};
		deleteItem.addListener(SWT.Selection, selectionListener);
	}

	private ProcessingRequestContext getSelectedProcessingRequestContext(String key) {
		return WidgetUtilities.getDataObject(combo, ProcessingRequestContext.class, key);
	}

	private void addProcessingRequest(ProcessingRequestPair<?> processingPair) {
		getScanningAcquisition().ifPresent(acquisition -> {
			if (acquisition.getAcquisitionConfiguration().getProcessingRequest() == null) {
				acquisition.getAcquisitionConfiguration().setProcessingRequest(new ArrayList<>());
			}

			boolean exists = acquisition.getAcquisitionConfiguration().getProcessingRequest().stream()
				.filter(p ->
					 p.getKey().equals(processingPair.getKey())
							&& p.getValue().containsAll(processingPair.getValue()))
				.count() > 0;

			if (!exists)
				acquisition.getAcquisitionConfiguration().getProcessingRequest().add(processingPair);
		});
	}

	private void removeProcessingRequest(ProcessingRequestPair<?> processingPair) {
		getScanningAcquisition().ifPresent(acquisition -> {
			if (acquisition.getAcquisitionConfiguration().getProcessingRequest() == null) {
				acquisition.getAcquisitionConfiguration().setProcessingRequest(new ArrayList<>());
			}
			acquisition.getAcquisitionConfiguration().getProcessingRequest().remove(processingPair);
		});
	}

	private Optional<ScanningAcquisition> getScanningAcquisition() {
		return SpringApplicationContextFacade.getBean(ClientSpringContext.class).getAcquisitionController()
				.map(AcquisitionController::getAcquisition);
	}

//	private Supplier<URL> getDirPathSupplier(String label) {
//		return () -> getSelectedProcessingRequestContext(label).getConfigurationSource();
//	}

	private URL getDirPath() {
		String key = combo.getItems()[combo.getSelectionIndex()];
		return getSelectedProcessingRequestContext(key).getConfigurationSource();
	}
}