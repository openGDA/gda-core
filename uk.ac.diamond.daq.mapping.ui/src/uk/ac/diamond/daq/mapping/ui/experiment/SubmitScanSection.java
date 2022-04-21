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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
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
import uk.ac.diamond.daq.mapping.ui.MultiFunctionButton;
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

	private static final String[] MAP_FILE_FILTER_NAMES = new String[] { "Mapping Scan Files", "All Files (*.*)" };
	private static final String[] MAP_FILE_FILTER_EXTENSIONS = new String[] { "*.map", "*.*" };

	private static final String[] NX_FILE_FILTER_NAMES = new String[] { "NeXus Files", "All Files (*.*)" };
	private static final String[] NX_FILE_FILTER_EXTENSIONS = new String[] { "*.nxs", "*.*" };

	private Composite composite;
	private Button submitScanButton;

	private String description = "Mapping scan";

	private String buttonText = "Queue Scan";

	private RGB buttonColour = null;

	private ScanManagementController smController;

	@Override
	public void createControls(Composite parent) {
		smController = getService(ScanManagementController.class);
		smController.initialise();
		super.createControls(parent);

		createMainComposite(parent);
		createSubmitSection();
		createMscanSection();
	}

	private void createMainComposite(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
	}

	protected void createSubmitSection() {
		final Composite submitComposite = createComposite(composite, 1, false);
		createSubmitButton(submitComposite);
	}

	protected void createSubmitButton(Composite parent) {
		// Button to submit a scan to the queue
		submitScanButton = new Button(parent, SWT.PUSH);
		submitScanButton.setText(buttonText);
		if (buttonColour != null) {
			submitScanButton.setBackground(new Color(Display.getDefault(), buttonColour));
		}
		GridDataFactory.swtDefaults().applyTo(submitScanButton);
		submitScanButton.addSelectionListener(widgetSelectedAdapter(e -> submitScan()));
	}

	private void createMscanSection() {
		final Composite mscanComposite = createComposite(composite, 3, false);
		((GridData) mscanComposite.getLayoutData()).horizontalAlignment = SWT.TRAIL;

		// Button to copy a scan to the clipboard
		final Button copyScanCommandButton = new Button(mscanComposite, SWT.PUSH);
		copyScanCommandButton.setImage(getImage("icons/copy.png"));
		copyScanCommandButton.setToolTipText("Copy the scan command to the system clipboard");
		GridDataFactory.swtDefaults().applyTo(copyScanCommandButton);
		copyScanCommandButton.addSelectionListener(widgetSelectedAdapter(e -> smController.copyScanToClipboard()));

		List<AbstractSearchResultLabelProvider> labelProviders =
				Arrays.asList(
						new ComplexScanNameLabelProvider("Name", PersistableMappingExperimentBean.SCAN_NAME_TITLE, true, 0),
						new ComplexScanNameLabelProvider("Dimensions", PersistableMappingExperimentBean.SCAN_NAME_TITLE, false, 1));


		// Multi-functional button to load scan parameters from various places into the Mapping UI
		final MultiFunctionButton loadScanButton = new MultiFunctionButton();

		// Loads parameters from a .map file
		loadScanButton.addFunction(
				"Load a scan from a .map file",
				"Load a scan from a .map file",
				new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/open.png")),
				() -> smController
						.loadScanMappingBean(chooseMapFileName(SWT.OPEN))
						.ifPresent(this::loadNewMappingBean));

		// Derives parameters from a NeXus file if the correct entry is present
		loadScanButton.addFunction(
				"Load a scan from a NeXus file",
				"Load a scan from a compatible .nxs file",
				new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/nexus.png")),
				() -> smController
						.loadScanRequest(chooseNxFileName(SWT.OPEN))
						.ifPresent(this::updateMappingBean));

		// Loads parameters from the persistence service,
		// this function only appears if GDA is configured to use the
		// persistence service
		if (LocalProperties.isPersistenceServiceAvailable()) {
			loadScanButton.addFunction(
					"Load a scan from the database",
					"Load a scan from the GDA persistence database",
					new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/database--arrow.png")),
					() -> loadNewMappingBeanFromPersistenceService(labelProviders));
		}

		loadScanButton.draw(mscanComposite);

		// Button to save a scan to disk
		final MultiFunctionButton saveScanButton = new MultiFunctionButton();

		// Saves parameters to a .map file
		saveScanButton.addFunction(
						"Save a scan to a .map file",
						"Save a scan to a .map file",
						new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/save.png")),
						this::saveCurrentMappingBean);

		// Saves parameters to the persistence service,
		// this function only appears if GDA is configured to use the
		// persistence service
		if (LocalProperties.isPersistenceServiceAvailable()) {
			saveScanButton.addFunction(
					"Save a scan to the database",
					"Save a scan to the GDA persistence database",
					new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/database--plus.png")),
					() -> saveCurrentMappingBeanToPersistenceService(labelProviders));
		}

		saveScanButton.draw(mscanComposite);
	}

	private void updateMappingBean(final ScanRequest request) {
		// Merges a ScanRequest into the mapping bean and refreshes the UI
		getService(ScanRequestConverter.class).mergeIntoMappingBean(request, getBean());
		refreshMappingView();
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
		refreshMappingView();
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

	private void saveCurrentMappingBean() {
		smController.saveScan(chooseMapFileName(SWT.SAVE));
	}

	private void refreshMappingView() {
		smController.updateGridModelIndex();
		getView().updateControls();

	}
	protected void submitScan() {
		smController.submitScan();
	}

	private String chooseMapFileName(int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(MAP_FILE_FILTER_NAMES);
		dialog.setFilterExtensions(MAP_FILE_FILTER_EXTENSIONS);
		dialog.setFilterPath(getVisitConfigDir());
		dialog.setOverwrite(true);

		return dialog.open();
	}

	private String chooseNxFileName(int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(NX_FILE_FILTER_NAMES);
		dialog.setFilterExtensions(NX_FILE_FILTER_EXTENSIONS);
		dialog.setFilterPath(getVisitDir());
		dialog.setOverwrite(true);

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
	String getDescription() {
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

	/**
	 * Set the enabled state of the Submit button
	 *
	 * @param enabled
	 *            <code>true</code> to enable the button, <code>false</code> to disable it
	 */
	protected void setSubmitScanButtonEnabled(boolean enabled) {
		submitScanButton.setEnabled(enabled);
	}
}
