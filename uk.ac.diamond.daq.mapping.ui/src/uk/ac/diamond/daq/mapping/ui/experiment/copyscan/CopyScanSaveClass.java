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

package uk.ac.diamond.daq.mapping.ui.experiment.copyscan;

import static gda.configuration.properties.LocalProperties.GDA_BEAMLINE_NAME;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIRM_FILE_OVERWRITE;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIRM_FILE_OVERWRITE_TITLE;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_GENERATE_CLASS_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_SAVE_CLASS_DESCRIPTION;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_SAVE_CLASS_ERROR_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_SAVE_CLASS_ERROR_TITLE;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_SAVE_CLASS_TITLE;
import static uk.ac.gda.ui.tool.ClientMessages.SAVE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Generate a class from the current scan definition, copy it to the clipboard, display it to the user and allow them to
 * save it to a file.
 */
class CopyScanSaveClass extends WizardPage {
	private static final Logger logger = LoggerFactory.getLogger(CopyScanSaveClass.class);

	private static final int TEXT_AREA_WIDTH = 800;
	private static final int TEXT_AREA_HEIGHT = 500;

	private static final String SAVE_ICON_PATH = "icons/save.png";
	private static final String[] FILTER_NAMES = new String[] { "Python scripts", "All files" };
	private static final String[] FILTER_EXTENSIONS = new String[] { "*.py", "*.*" };
	private static final String DEFAULT_SAVE_DIRECTORY = String.format("/dls_sw/%s/scripts", LocalProperties.get(GDA_BEAMLINE_NAME));

	private static final String TEMPLATE_FILE_BUNDLE = "uk.ac.diamond.daq.mapping.ui";
	private static final String TEMPLATE_FILE_PATH = "files/save_scan/jython_class_template.txt";

	private final ScanManagementController controller;
	private final CopyScanConfig config;

	/**
	 * Multi-line text box to display the class that is generated.<br>
	 * This can be selected & copied - though it has already been copied to the clipboard anyway.
	 */
	private Text classText;

	protected CopyScanSaveClass(ScanManagementController controller, CopyScanConfig config) {
		super(CopyScanSaveClass.class.getSimpleName());
		setTitle(ClientMessagesUtility.getMessage(COPY_SCAN_SAVE_CLASS_TITLE));
		setDescription(ClientMessagesUtility.getMessage(COPY_SCAN_SAVE_CLASS_DESCRIPTION));
		this.controller = controller;
		this.config = config;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(mainComposite);

		classText = new Text(mainComposite, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().hint(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT).grab(true, true).applyTo(classText);
		classText.setFont(config.getMonospacedFont());
		classText.setTabs(4);

		final Button saveButton = createClientButton(mainComposite, SWT.PUSH, SAVE, SAVE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(saveButton);
		saveButton.setImage(Activator.getImage(SAVE_ICON_PATH));
		saveButton.setFont(config.getDefaultFont());

		final Listener saveButtonListener = e -> saveClass();
		addWidgetDisposableListener(saveButton, SWT.Selection, saveButtonListener);
		addWidgetDisposableListener(saveButton, SWT.DefaultSelection, saveButtonListener);

		setControl(mainComposite);
		setPageComplete(true);
	}

	/**
	 * Generate the Jython class from the scan definition and copy to the clipboard and the text box.
	 * <p>
	 * The wizard must call this function before displaying this page.
	 */
	public void onEnterPage() {
		try {
			final String scanCommand = createJythonClass();
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { scanCommand }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();

			classText.setText(scanCommand);
		} catch (Exception e) {
			logger.error("Copy to clipboard failed.", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Copying Scan Command",
					"The scan command could not be copied to the clipboard. See the error log for more details.");
		}
	}

	private String createJythonClass() {
		final ScanBean scanBean = controller.createScanBean();
		try {
			final IMarshallerService marshallerService = PlatformUI.getWorkbench().getService(IMarshallerService.class);
			final String scanRequestJson = marshallerService.marshal(scanBean.getScanRequest());
			final String classTemplate = readClassTemplate();
			return String.format(classTemplate, config.getClassName(), scanRequestJson);
		} catch (Exception e) {
			final String message = ClientMessagesUtility.getMessage(COPY_SCAN_GENERATE_CLASS_ERROR);
			logger.error(message, e);
			return String.format("%s%n%s", message, e.getMessage());
		}
	}

	private String readClassTemplate() throws IOException {
		final URL fileUrl = Platform.getBundle(TEMPLATE_FILE_BUNDLE).getEntry(TEMPLATE_FILE_PATH);
		if (fileUrl == null) {
			throw new IOException("Template file '" + TEMPLATE_FILE_PATH + "' not found in bundle " + TEMPLATE_FILE_BUNDLE);
		}
		final InputStream inputStream = fileUrl.openStream();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
			return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}

	/**
	 * Save the generated class to a file
	 */
	private void saveClass() {
		final FileDialog fileSave = new FileDialog(getShell(), SWT.SAVE);
		fileSave.setFileName(config.getClassName() + ".py");
		fileSave.setFilterExtensions(FILTER_EXTENSIONS);
		fileSave.setFilterNames(FILTER_NAMES);
		fileSave.setFilterPath(getInitialFileSaveLocation().getAbsolutePath());

		final String filePath = fileSave.open();
		if (filePath == null) {
			return;
		}

		final File file = new File(filePath);
		if (file.exists()) {
			final MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText(ClientMessagesUtility.getMessage(CONFIRM_FILE_OVERWRITE_TITLE));
			messageBox.setMessage(String.format(ClientMessagesUtility.getMessage(CONFIRM_FILE_OVERWRITE), file.getName()));
			final int reply = messageBox.open();
			if (reply != SWT.YES) {
				return;
			}
		}
		try (BufferedWriter outFile = new BufferedWriter(new FileWriter(file))) {
			outFile.write(classText.getText());
			config.setLastSaveLocation(file.getParentFile());
		} catch (IOException e) {
			MessageDialog.openError(getShell(),
					ClientMessagesUtility.getMessage(COPY_SCAN_SAVE_CLASS_ERROR_TITLE),
					ClientMessagesUtility.getMessage(COPY_SCAN_SAVE_CLASS_ERROR_MESSAGE) + "\n" + e.getMessage());
			logger.error(ClientMessagesUtility.getMessage(COPY_SCAN_SAVE_CLASS_ERROR_MESSAGE), e);
		}
	}

	/**
	 * Get an initial location for the "save to file" dialog to display<br>
	 * If the user has already saved a file, go to the corresponding directory, otherwise go to the user scripts
	 * directory.
	 */
	private File getInitialFileSaveLocation() {
		final File lastSaveLocation = config.getLastSaveLocation();
		if (lastSaveLocation != null && lastSaveLocation.length() > 0) {
			return lastSaveLocation;
		}
		return new File(DEFAULT_SAVE_DIRECTORY);
	}
}
