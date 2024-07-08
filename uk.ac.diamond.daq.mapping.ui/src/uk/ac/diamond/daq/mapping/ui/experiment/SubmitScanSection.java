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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_COPY;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_DATABASE_ARROW;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_DATABASE_PLUS;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_NEXUS;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_OPEN;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_SAVE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.application.persistence.data.SearchResult;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.client.gui.persistence.AbstractSearchResultLabelProvider;
import uk.ac.diamond.daq.client.gui.persistence.SearchResultViewDialog;
import uk.ac.diamond.daq.client.gui.persistence.SearchResultViewDialogMode;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.PersistableMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.Colour;
import uk.ac.diamond.daq.mapping.ui.MultiFunctionButton;
import uk.ac.diamond.daq.mapping.ui.experiment.StateReporter.State;
import uk.ac.diamond.daq.mapping.ui.experiment.StateReporter.StateReport;
import uk.ac.diamond.daq.persistence.manager.PersistenceServiceWrapper;

/**
 * A section containing:<ul>
 * <li>a section for submitting scans containing:</li>
 * <ul>
 * <li>a button to submit a scan to the queue</li>
 * </ul>
 * <li>a section to handle the corresponding mscan command containing:</li>
 * <ul>
 * <li>a button to copy a scan to the clipboard</li>
 * <li>a button to save a scan to disk</li>
 * <li>a button to load a scan from disk</li>
 * </ul>
 * </ul>
 */
public class SubmitScanSection extends AbstractMappingSection {
	private static final Logger log = LoggerFactory.getLogger(SubmitScanSection.class);

	private Composite composite;

	private Button submitScanButton;

	private String description = "Mapping scan";

	private String buttonText = "Queue Scan";

	private RGB buttonColour = null;

	private RGB badStateButtonColour;

	private ScanManagementController smController;

	private Optional<StateReporter> stateReporter = Optional.empty();

	private static final String LOAD_ACTION_MESSAGE = "Load a scan from a %s file";
	private static final String SAVE_ACTION_MESSAGE = "Save a scan to a %s file";

	public enum FileType{
		MAP(new String[] { "Mapping Scan Files", "All Files (*.*)" }, new String[] { "*.map", "*.*" }),
		JSON(new String[] { "Scan Request Files", "All Files (*.*"}, new String[] {"*.json", "*.*" }),
		NXS(new String[] { "NeXus Files", "All Files (*.*)" }, new String[]{ "*.nxs", "*.*" });

		private String[] filterNames;
		private String[] filterExtensions;

		private FileType(String[] filterNames, String[] filterExtensions) {
			this.filterNames = filterNames;
			this.filterExtensions = filterExtensions;
		}

		public String[] getFilterNames() {
			return filterNames;
		}

		public String[] getFilterExtensions() {
			return filterExtensions;
		}
	}

	@Override
	public void createControls(Composite parent) {
		smController = getService(ScanManagementController.class);
		smController.initialise();
		super.createControls(parent);

		createMainComposite(parent);
		createSubmitSection();
		createMscanSection();

		if (stateReporter.isPresent()) {
			stateReporter.get().initialize(stateConsumer);
			composite.addDisposeListener(e -> removeListeners());
		}
	}

	private void createMainComposite(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
	}

	protected void createSubmitSection() {
		final Composite submitComposite = createComposite(composite, 1, false);
		setButtonColour(Colour.WHITE.getRGB());
		createSubmitButton(submitComposite);
	}

	protected void createSubmitButton(Composite parent) {
		// Button to submit a scan to the queue
		submitScanButton = new Button(parent, SWT.PUSH);
		submitScanButton.setText(buttonText);
		if (buttonColour != null) {
			submitScanButton.setBackground(new Color(Display.getDefault(), buttonColour));
		}
		badStateButtonColour = Colour.RED.getRGB();
		GridDataFactory.swtDefaults().applyTo(submitScanButton);
		submitScanButton.addSelectionListener(widgetSelectedAdapter(e -> submitScan()));
	}

	private void createMscanSection() {
		final Composite mscanComposite = createComposite(composite, 3, false);
		((GridData) mscanComposite.getLayoutData()).horizontalAlignment = SWT.TRAIL;

		// Button to copy a scan to the clipboard
		final Button copyScanCommandButton = new Button(mscanComposite, SWT.PUSH);
		copyScanCommandButton.setImage(getImage(IMG_COPY));
		copyScanCommandButton.setToolTipText("Copy the scan command to the system clipboard");
		GridDataFactory.swtDefaults().applyTo(copyScanCommandButton);
		copyScanCommandButton.addSelectionListener(widgetSelectedAdapter(e -> smController.copyScanToClipboard()));

		List<AbstractSearchResultLabelProvider> labelProviders =
				Arrays.asList(
						new ComplexScanNameLabelProvider("Name", PersistableMappingExperimentBean.SCAN_NAME_TITLE, true, 0),
						new ComplexScanNameLabelProvider("Dimensions", PersistableMappingExperimentBean.SCAN_NAME_TITLE, false, 1));


		// Multi-functional button to load scan parameters from various places into the Mapping UI
		final MultiFunctionButton loadScanButton = new MultiFunctionButton();

		addFunction(loadScanButton, String.format(LOAD_ACTION_MESSAGE, ".map"), IMG_OPEN,
				() -> smController
				.loadScanMappingBean(chooseFileName(FileType.MAP, SWT.OPEN))
				.ifPresent(this::loadNewMappingBean));

		// Derives parameters from a NeXus file if the correct entry is present
		addFunction(loadScanButton, String.format(LOAD_ACTION_MESSAGE, "Nexus"), IMG_NEXUS,
				() -> smController
				.loadScanRequest(chooseFileName(FileType.NXS, SWT.OPEN))
				.ifPresent(this::updateMappingBean));


		if (LocalProperties.isPersistenceServiceAvailable()) {
			addFunction(loadScanButton, "Load a scan from the database", IMG_DATABASE_ARROW,
					() -> loadNewMappingBeanFromPersistenceService(labelProviders));
		}

		loadScanButton.draw(mscanComposite);

		// Button to save a scan to disk
		final MultiFunctionButton saveScanButton = new MultiFunctionButton();

		addFunction(saveScanButton, String.format(SAVE_ACTION_MESSAGE, ".map"), IMG_SAVE, this::saveMappingBean);
		addFunction(saveScanButton, String.format(SAVE_ACTION_MESSAGE, ".json"), IMG_SAVE, this::saveScanRequest);

		if (LocalProperties.isPersistenceServiceAvailable()) {
			addFunction(saveScanButton, "Save a scan to the database", IMG_DATABASE_PLUS,
					() -> saveCurrentMappingBeanToPersistenceService(labelProviders));
		}

		saveScanButton.draw(mscanComposite);
	}

	private void addFunction(MultiFunctionButton button, String title, String iconFilePath, Runnable runnable) {
		button.addFunction(title, title, Activator.getImage(iconFilePath), runnable);
	}

	private void updateMappingBean(final ScanRequest request) {
		// Merges a ScanRequest into the mapping bean and refreshes the UI
		getService(ScanRequestConverter.class).mergeIntoMappingBean(request, getBean());
		getView().updateControls();
	}

	private void loadNewMappingBeanFromPersistenceService(
			final List<AbstractSearchResultLabelProvider> labelProviders) {
		PersistenceServiceWrapper persistenceService = getService(PersistenceServiceWrapper.class);
		SearchResult searchResult;
		try {
			searchResult = persistenceService.get(PersistableMappingExperimentBean.class);
		} catch (PersistenceException e1) {
			log.error("Unable to find existing Mapping Beans", e1);
			return;
		}
		SearchResultViewDialog searchDialog = new SearchResultViewDialog(getShell(), searchResult,
				"Load Scan Definition", true, false, PersistableMappingExperimentBean.SCAN_NAME_TITLE,
				SearchResultViewDialogMode.load, labelProviders);

		searchDialog.open();
		if (searchDialog.getReturnCode() == Window.OK) {
			smController
				.loadScanMappingBean(searchDialog.getItemId())
				.ifPresent(this::loadNewMappingBean);
		}
	}

	private void loadNewMappingBean(final IMappingExperimentBean bean) {
		// Replaces the mapping bean and refreshes the UI
		getView().setMappingBean(bean);
		getView().updateControls();
	}

	private void saveCurrentMappingBeanToPersistenceService(
			final List<AbstractSearchResultLabelProvider> labelProviders) {
		PersistenceServiceWrapper persistenceService = getService(PersistenceServiceWrapper.class);
		SearchResult searchResult;
		try {
			searchResult = persistenceService.get(PersistableMappingExperimentBean.class);
		} catch (PersistenceException e1) {
			log.error("Unable to find existing Mapping Beans", e1);
			return;
		}
		SearchResultViewDialog searchDialog = new SearchResultViewDialog(getShell(), searchResult,
				"Save Scan Definition", true, true, PersistableMappingExperimentBean.SCAN_NAME_TITLE,
				SearchResultViewDialogMode.save, labelProviders);
		searchDialog.open();
		if (searchDialog.getReturnCode() == Window.OK) {
			IMappingExperimentBean mappingBean = getBean();
			mappingBean.setDisplayName(searchDialog.getNewName());
			mappingBean.setId(searchDialog.getItemId());
			smController.saveScanAs(mappingBean.getId());
		}
	}

	protected String saveScanRequest() {
		var fileName = chooseFileName(FileType.JSON, SWT.SAVE);
		smController.saveScanRequest(fileName);
		return fileName;
	}

	private void saveMappingBean() {
		smController.saveMappingBean(chooseFileName(FileType.MAP, SWT.SAVE));
	}

	protected void submitScan() {
		smController.submitScan();
	}

	/**
	 * Sets the filter name, extensions and path depending on the type of file
	 * @param fileType
	 * @param fileDialogStyle
	 * @return a string describing the absolute path
	 */
	private String chooseFileName(FileType fileType, int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(fileType.getFilterNames());
		dialog.setFilterExtensions(fileType.getFilterExtensions());
		dialog.setOverwrite(true);
		if (fileType.equals(FileType.NXS)) {
			dialog.setFilterPath(getVisitDir());
		} else {
			dialog.setFilterPath(getVisitConfigDir());
		}
		return dialog.open();
	}

	private String getVisitDir() {
		return getService(IFilePathService.class).getVisitDir();
	}

	/**
	 * Called when this section is shown
	 * <p>
	 * This can be used for example to show controls allowing the user to define parameters specific to this submit
	 * section.
	 */
	protected void onShow() {
		// Nothing to do in this class: subclasses may override
	}

	/**
	 * Called when this section is no longer visible
	 * <p>
	 * This can be used for example to hide the controls made visible by {@link #onShow()}
	 */
	protected void onHide() {
		// Nothing to do in this class: subclasses may override
	}

	/**
	 * Return the composite created by this section
	 *
	 * @return the section composite
	 */
	protected Composite getComposite() {
		return composite;
	}

	/**
	 * Set the text to be shown on the Submit button<br>
	 * Typically set in Spring configuration
	 *
	 * @param buttonText
	 *            Text to be shown on the button
	 */
	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	/**
	 * Gets a user-friendly name for the section
	 *
	 * @return a description of the section
	 */
	protected String getDescription() {
		return description;
	}

	/**
	 * Set a user-friendly description for the section
	 * <p>
	 * Typically set in Spring and can be used for example in a list to give the user a choice of different Submit
	 * sections to give the user a choice of
	 *
	 * @param description
	 *            a description of this section e.g. "Mapping scan"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the colour of the submit button
	 * <p>
	 * This can for example be used to make it more obvious to the user which type of scan they are about to submit.
	 *
	 * @param buttonColour
	 *            RGB value of the required colour
	 */
	protected void setButtonColour(RGB buttonColour) {
		this.buttonColour = buttonColour;
	}

	protected RGB getButtonColour() {
		return buttonColour;
	}

	/**
	 * Set the enabled state of the Submit button
	 *
	 * @param enabled
	 *            <code>true</code> to enable the button, <code>false</code> to disable it
	 */
	protected void setSubmitScanButtonEnabled(boolean enabled) {
		submitScanButton.setEnabled(enabled);
	}

	public void setStateReporter(StateReporter stateReporter) {
		this.stateReporter = Optional.of(stateReporter);
	}

	private Consumer<StateReport> stateConsumer = stateReport -> {
		if (stateReport.getState().equals(State.GOOD)) {
			updateSubmitButton(getButtonColour(), stateReport.getMessage());
		} else {
			updateSubmitButton(badStateButtonColour, stateReport.getMessage());
		}
	};

	/**
	 * Sets the colour and tooltip message of the submit button
	 * <p>
	 * This is used to reflect a change of state on the beamline
	 * It can be used to warn users about something that requires their attention before queueing the scan
	 *
	 * @param buttonColour RGB value of the required colour
	 * @param message message to display as button tooltip
	 */
	private void updateSubmitButton(RGB buttonColour, String message) {
		Display.getDefault().asyncExec(() -> {
			submitScanButton.setBackground(new Color(Display.getDefault(), buttonColour));
			submitScanButton.setToolTipText(message);
		});
	}

	private void removeListeners() {
		stateReporter.get().dispose();
	}
}