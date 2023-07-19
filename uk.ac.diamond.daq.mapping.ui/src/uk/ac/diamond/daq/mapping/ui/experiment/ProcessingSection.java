/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static uk.ac.gda.ui.tool.ClientMessages.PROCESSING_MODEL_CONFIG;
import static uk.ac.gda.ui.tool.ClientMessages.PROCESSING_MODEL_CONFIG_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.dawnsci.processing.ui.model.OperationModelWizardDialog;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * Section to configure cluster processing for a mapping scan.
 */
public class ProcessingSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSection.class);

	private Composite processingChainsComposite;

	private TableViewer viewer;

	private static final Image ticked = Activator.getImage("icons/ticked.png");
	private static final Image unticked = Activator.getImage("icons/unticked.gif");

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		final Composite processingComposite = createComposite(parent, 1, true, true);
		createTitleAndAddProcessingRow(processingComposite);
		createProcessingTable(processingComposite);
	}

	private void createTitleAndAddProcessingRow(Composite parent) {
		final Composite rowComposite = createComposite(parent, 2, false);

		final Label processingLabel = new Label(rowComposite, SWT.NONE);
		processingLabel.setText("Processing");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(processingLabel);

		// Button to add a processing model
		final Button addProcessingModelButton = new Button(rowComposite, SWT.PUSH);
		addProcessingModelButton.setText(getMessage(PROCESSING_MODEL_CONFIG));
		addProcessingModelButton.setToolTipText(getMessage(PROCESSING_MODEL_CONFIG_TP));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(addProcessingModelButton);

		addProcessingModelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addProcessingModel();
			}
		});
	}

	private void createProcessingTable(Composite parent) {
		processingChainsComposite = createComposite(parent, 1, false, true);

		viewer = new TableViewer(processingChainsComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
		viewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.setContentProvider(new ArrayContentProvider());

		final TableViewerColumn check   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(viewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return ((ConfigWrapper)element).isActive() ? ticked : unticked;
			}

		});

		check.getColumn().setWidth(28);

		final TableViewerColumn app = new TableViewerColumn(viewer, SWT.LEFT);
		app.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((ConfigWrapper)element).getAppName();
			}
		});

		app.getColumn().setText("App");
		app.getColumn().setWidth(100);

		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((ConfigWrapper)element).getName();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((ConfigWrapper) element).getPathToConfig();
			}
		});

		name.getColumn().setText("Name");
		name.getColumn().setWidth(200);

		final MenuManager menuMgr = new MenuManager();

		menuMgr.add(new Action("Remove") {
			@Override
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				getBean().getProcessingConfigs().removeAll(selection.toList());
				getView().updateControls();
			}
		});

		final Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getTable().setMenu(menu);

		final List<ConfigWrapper> configs = getBean().getProcessingConfigs();
		viewer.setInput(configs.toArray());
		setTableSize();
	}

	private ConfigWrapper configureProcessingModel() {
		final List<IOperationSetupWizardPage> startPages = new ArrayList<>(2);

		final AcquireDataWizardPage acquirePage = new AcquireDataWizardPage(getEclipseContext());

		final DawnConfigBean processingConfig = new DawnConfigBean();
		final ConfigWrapper wrapper = new ConfigWrapper();

		final ProcessingSelectionWizardPage selectionPage = new ProcessingSelectionWizardPage(getEclipseContext(),
				processingConfig, wrapper, getBean().getDetectorParameters());

		startPages.add(selectionPage);
		startPages.add(acquirePage);

		final Supplier<ProcessingSelectionWizardPage.ProcessingMode> selectedMode = selectionPage::selectedMode;

		try {
			final IOperationModelWizard wizard = getService(IOperationUIService.class).getWizard(null,
					startPages, (String) null, null);

			final OperationModelWizardDialog dialog = new OperationModelWizardDialog(getShell(), wizard);
			dialog.setTitle("Setup Processing");

			if (dialog.open() == Window.OK) {
				if (ProcessingSelectionWizardPage.ProcessingMode.NEW_DAWN.equals(selectedMode.get())) {
					try {
						final Path processingFilePath = Paths.get(processingConfig.getProcessingFile());
						Files.createDirectories(processingFilePath.getParent());
						wizard.saveOutputFile(processingFilePath.toString());
					} catch (Exception e) {
						logger.error("Could not save template file!", e);
					}
				}

				if (!ProcessingSelectionWizardPage.ProcessingMode.OTHER.equals(selectedMode.get()) &&
						!ProcessingSelectionWizardPage.ProcessingMode.RESTAPI.equals(selectedMode.get())) {
					final String json = getService(IMarshallerService.class).marshal(processingConfig, false);
					try (BufferedWriter wr = new BufferedWriter(new FileWriter(wrapper.getConfigString()))) {
						wr.write(json);
					} catch (Exception e) {
						logger.error("Could not write config file!", e);
					}
				}

				return wrapper;
			}
		} catch (Exception e) {
			logger.error("Could not open operation wizard", e);
		}
		return null;
	}

	private void addProcessingModel() {
		final ConfigWrapper config = configureProcessingModel();
		if (config != null) {
			// Ensure file is selected
			config.setActive(true);
			getBean().addProcessingRequest(config);

			getView().updateControls();
			getView().showControl(processingChainsComposite.getParent());
		}
	}
	@Override
	public void updateControls() {
		List<ConfigWrapper> configs = getBean().getProcessingConfigs();
		if (configs == null) {
			configs = Collections.emptyList();
		}

		viewer.setInput(configs);
		viewer.refresh();

		setTableSize();
		processingChainsComposite.getParent().layout(true, true);
		relayoutView();
	}

	/**
	 * Resize the table to fit the data, subject to a maximum size: table will scroll to view all items
	 */
	private void setTableSize() {
		final int maxItems = 5;
		if (processingChainsComposite.getLayoutData() instanceof GridData gridData) {
			final int itemCount = Math.min(viewer.getTable().getItemCount(), maxItems);
			final int itemHeight = viewer.getTable().getItemHeight();
			final int headerHeight = viewer.getTable().getHeaderHeight();

			final int h = (1 + itemCount) * itemHeight + headerHeight;
			gridData.minimumHeight = h;
			gridData.heightHint = h;
		}
	}

	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(viewer.getTable());
			edit.setValue(((ConfigWrapper)element).isActive());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((ConfigWrapper)element).isActive();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((ConfigWrapper)element).setActive((Boolean)value);
			getViewer().refresh();
		}

	}

}
