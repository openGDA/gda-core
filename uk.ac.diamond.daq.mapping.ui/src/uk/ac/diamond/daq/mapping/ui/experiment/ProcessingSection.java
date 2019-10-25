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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * Section to configure cluster processing for a mapping scan.
 */
public class ProcessingSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSection.class);

	private static final File[] NO_TEMPLATE_FILES = new File[0];


	private Composite processingChainsComposite;

	private TableViewer viewer;

	private static final Image ticked = MappingExperimentUtils.getImage("icons/ticked.png");
	private static final Image unticked = MappingExperimentUtils.getImage("icons/unticked.gif");

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		Composite processingComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(processingComposite);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(processingComposite);

		createTitleAndAddProcessingRow(processingComposite);
		createProcessingTable(processingComposite);
	}

	@Override
	public boolean shouldShow() {
		return getTemplateFiles().length > 0;
	}

	private void createTitleAndAddProcessingRow(Composite parent) {
		Composite rowComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(rowComposite);
		GridDataFactory grabHorizontalGridData = GridDataFactory.fillDefaults().grab(true, false);
		grabHorizontalGridData.applyTo(rowComposite);

		Label processingLabel = new Label(rowComposite, SWT.NONE);
		processingLabel.setText("Processing");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(processingLabel);

		// Button to add a processing model
		Button addProcessingModelButton = new Button(rowComposite, SWT.PUSH);
		addProcessingModelButton.setText("Add Processing...");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(addProcessingModelButton);

		addProcessingModelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addProcessingModel();
			}
		});
	}

	private void createProcessingTable(Composite parent) {
		processingChainsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(processingChainsComposite);
		GridLayoutFactory.fillDefaults().applyTo(processingChainsComposite);

		viewer = new TableViewer(processingChainsComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
		viewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn check   = new TableViewerColumn(viewer, SWT.CENTER, 0);
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

		TableViewerColumn app = new TableViewerColumn(viewer, SWT.LEFT);
		app.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((ConfigWrapper)element).getAppName();
			}
		});

		app.getColumn().setText("App");
		app.getColumn().setWidth(100);

		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
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

		MenuManager menuMgr = new MenuManager();

		menuMgr.add(new Action("Remove") {
			@Override
			public void run() {
				ISelection s = viewer.getSelection();
				if (s instanceof StructuredSelection) {
					List<ConfigWrapper> w = new ArrayList<>();
					@SuppressWarnings("rawtypes")
					Iterator iterator = ((StructuredSelection)s).iterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof ConfigWrapper) {
							w.add((ConfigWrapper)next);
						}
					}
					if (!w.isEmpty()) {
						getMappingBean().getProcessingConfigs().removeAll(w);
					}

					viewer.setInput(getMappingBean().getProcessingConfigs());
				}
			}
		});

		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getTable().setMenu(menu);

		List<ConfigWrapper> configs = getMappingBean().getProcessingConfigs();
		viewer.setInput(configs.toArray());

	}

	private ConfigWrapper configureProcessingModel() {
		final List<IOperationSetupWizardPage> startPages = new ArrayList<>(2);

		final AcquireDataWizardPage acquirePage = new AcquireDataWizardPage(getEclipseContext());


		DawnConfigBean	processingConfig = new DawnConfigBean();
		ConfigWrapper w = new ConfigWrapper();

		final ProcessingSelectionWizardPage selectionPage = new ProcessingSelectionWizardPage(getEclipseContext(),
				processingConfig,w, getMappingBean().getDetectorParameters());
		startPages.add(selectionPage);

		final Supplier<ProcessingSelectionWizardPage.ProcessingMode> selectedMode;

		selectedMode = selectionPage::selectedMode;

		startPages.add(acquirePage);

		try {
			IOperationModelWizard wizard = getEclipseContext().get(IOperationUIService.class).getWizard(null,
					startPages, (String) null, null);

			OperationModelWizardDialog dialog = new OperationModelWizardDialog(getShell(), wizard);
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

				if (!ProcessingSelectionWizardPage.ProcessingMode.OTHER.equals(selectedMode.get())) {
					IMarshallerService ms = getEclipseContext().get(IMarshallerService.class);

					String json = ms.marshal(processingConfig, false);

					try (BufferedWriter wr = new BufferedWriter(new FileWriter(w.getPathToConfig()))) {
						wr.write(json);
					} catch (Exception e) {
						// TODO: dialog!
					}

				}

				return w;
			}
		} catch (Exception e) {
			logger.error("Could not open operation wizard", e);
		}
		return null;
	}

	private void addProcessingModel() {

		ConfigWrapper config = configureProcessingModel();
		getMappingBean().addProcessingRequest(config);

		viewer.setInput(getMappingBean().getProcessingConfigs().toArray());
		viewer.refresh();
		getMappingView().updateControls();
	}

	private File[] getTemplateFiles() {
		// TODO: consider moving this method to a service
		File templatesDir = new File(getService(IFilePathService.class).getProcessingTemplatesDir());
		String[] names = templatesDir.list((dir, name) -> name.endsWith("." + MappingUIConstants.NEXUS_FILE_EXTENSION));
		if (names == null) {
			return NO_TEMPLATE_FILES;
		}

		return Arrays.stream(names).map(name -> new File(templatesDir, name)).toArray(File[]::new);
	}

	@Override
	public void updateControls() {

		List<ConfigWrapper> configs = getMappingBean().getProcessingConfigs();
		if (configs != null) {
			viewer.setInput(configs);
			viewer.refresh();
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
			if (element instanceof ConfigWrapper) return ((ConfigWrapper)element).isActive();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof ConfigWrapper && value instanceof Boolean){
				((ConfigWrapper)element).setActive((Boolean)value);
			}

			getViewer().refresh();
		}

	}

}
