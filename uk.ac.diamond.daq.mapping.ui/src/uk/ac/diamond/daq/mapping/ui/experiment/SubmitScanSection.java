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

import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

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

	private static final String[] FILE_FILTER_NAMES = new String[] { "Mapping Scan Files", "All Files (*.*)" };
	private static final String[] FILE_FILTER_EXTENSIONS = new String[] { "*.map", "*.*" };

	private Composite composite;
	private Button submitScanButton;

	private String description = "Mapping scan";

	private String buttonText = "Queue Scan";

	private RGB buttonColour = null;

	private ScanManagementController smController;

	@Override
	public boolean createSeparator() {
		return false;
	}

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
		final Composite submitComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().applyTo(submitComposite);
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
		final Composite mscanComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).applyTo(mscanComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(mscanComposite);

		// Button to copy a scan to the clipboard
		final Button copyScanCommandButton = new Button(mscanComposite, SWT.PUSH);
		copyScanCommandButton.setImage(getImage("icons/copy.png"));
		copyScanCommandButton.setToolTipText("Copy the scan command to the system clipboard");
		GridDataFactory.swtDefaults().applyTo(copyScanCommandButton);
		copyScanCommandButton.addSelectionListener(widgetSelectedAdapter(e -> smController.copyScanToClipboard()));

		// Button to load a scan from disk
		final Button loadButton = new Button(mscanComposite, SWT.PUSH);
		loadButton.setImage(getImage("icons/open.png"));
		loadButton.setToolTipText("Load a scan from the file system");
		GridDataFactory.swtDefaults().applyTo(loadButton);
		loadButton.addSelectionListener(widgetSelectedAdapter(e -> {
			final Optional<IMappingExperimentBean> bean = smController.loadScanMappingBean(chooseFileName(SWT.OPEN));
			if (bean.isPresent()) {
				getMappingView().setMappingBean(bean.get());
				smController.updateGridModelIndex();
				getMappingView().updateControls();
			}
		}));

		// Button to save a scan to disk
		final Button saveButton = new Button(mscanComposite, SWT.PUSH);
		saveButton.setImage(getImage("icons/save.png"));
		saveButton.setToolTipText("Save a scan to the file system");
		GridDataFactory.swtDefaults().applyTo(saveButton);
		saveButton.addSelectionListener(widgetSelectedAdapter(e -> smController.saveScan(chooseFileName(SWT.SAVE))));
	}

	protected void submitScan() {
		smController.submitScan();
	}

	private String chooseFileName(int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(FILE_FILTER_NAMES);
		dialog.setFilterExtensions(FILE_FILTER_EXTENSIONS);
		final String visitConfigDir = getService(IFilePathService.class).getVisitConfigDir();
		dialog.setFilterPath(visitConfigDir);
		dialog.setOverwrite(true);

		return dialog.open();
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
