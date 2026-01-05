/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroupTemplateConfiguration;
import uk.ac.gda.beans.exafs.ScanColourType;
import uk.ac.gda.beans.exafs.SpectrometerScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ui.DetectorConfigFileControls;
import uk.ac.gda.exafs.ui.XesScanParametersComposite;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class QxesScanView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(QxesScanView.class);

	public static final String ID = "uk.ac.gda.exafs.ui.xes.QxesScanView";

	private GridDataFactory gdFactory = GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT);

	private Text numberOfRepetitions;
	private Text pauseTimeBetweenRepetitions;
	private Button bidirectionalScan;
	private XesScanParametersComposite xesScanParametersComposite;

	// Path to qxes template file inside beamline config folder
	private String templateFileName = "var/templates/QXES_Parameters.xml";
	private String initialXmlSubFolderName = "Qxes";
	private String currentDirectory = "";

	private String saveOffsetsCommand = "save_tmp_offsets(%s)";
	private String stopScanCommand = "stop_scan()";
	private String runScanFunctionName = "prepare_run_qxes_scan";

	private List<String> detectorNames = List.of("medipix1", "medipix2");
	private List<DetectorConfigFileControls> medipixConfigControls;
	private DetectorGroupTemplateConfiguration templateConfiguration;

	public QxesScanView() {
		templateConfiguration = Finder.findSingleton(DetectorGroupTemplateConfiguration.class);
		currentDirectory = setupInitialDirectory(initialXmlSubFolderName);
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite mainComposite = new Composite(scrolledComposite, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));

		// Add XES scam GUI
		xesScanParametersComposite = new XesScanParametersComposite(mainComposite, SWT.NONE);
		try {
			xesScanParametersComposite.setBean(getBean());
		} catch (Exception e) {
			logger.error("Problem loading template from file", e);
		}
		xesScanParametersComposite.addControls();

		// hide some components not needed for QXes (probably)
		xesScanParametersComposite.setDiagramVisible(false);
		xesScanParametersComposite.setScanTypeComboVisible(false);
		xesScanParametersComposite.setOffsetsVisible(false);
		xesScanParametersComposite.setEnergyTransferVisible(false);

		xesScanParametersComposite.setupUiFromBean();

		// Add the QXes specific widgets

		Composite controls = new Composite(xesScanParametersComposite, SWT.NONE);
		controls.setLayout(new GridLayout(2, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		addQxesControls(controls);

		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// This needs to come last
		xesScanParametersComposite.linkUI();
	}

	/**
	 * Add QXes specific widgets to :
	 * <li> set the number of repetitions
	 * <li> set time between repetitions
	 * <li> Set the bi-directional scan flag
	 * <li> Run button for starting the scan
	 * @param parent
	 */
	private void addQxesControls(Composite controls) {
		Composite repetitionsGroup = new Composite(controls, SWT.NONE);
		repetitionsGroup.setLayout(new GridLayout(2, false));
		repetitionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label repetitionsLabel = new Label(repetitionsGroup, SWT.NONE);
		repetitionsLabel.setText("Number of repetitions");

		numberOfRepetitions = new Text(repetitionsGroup, SWT.NONE);
		numberOfRepetitions.addVerifyListener(ev -> ev.doit = verifyNumber(Integer.class, ev));

		Label pauseLabel = new Label(repetitionsGroup, SWT.NONE);
		pauseLabel.setText("Pause between repetitions (secs)");

		pauseTimeBetweenRepetitions = new Text(repetitionsGroup, SWT.NONE);
		pauseTimeBetweenRepetitions.addVerifyListener(ev -> ev.doit = verifyNumber(Double.class, ev));

		Label bidrectionalLabel = new Label(repetitionsGroup, SWT.NONE);
		bidrectionalLabel.setText("Bi-directional scan");

		bidirectionalScan = new Button(repetitionsGroup, SWT.CHECK);

		// labels
		gdFactory.applyTo(repetitionsLabel);
		gdFactory.applyTo(pauseLabel);
		gdFactory.applyTo(bidrectionalLabel);

		// control widgets
		int width = 75;

		gdFactory.hint(width, SWT.DEFAULT).applyTo(numberOfRepetitions);
		gdFactory.hint(width, SWT.DEFAULT).applyTo(pauseTimeBetweenRepetitions);
		gdFactory.hint(width, SWT.DEFAULT).applyTo(bidirectionalScan);

		Composite detControls = new Composite(controls, SWT.NONE);
		detControls.setLayout(new GridLayout(4, false));
		detControls.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		addDetectorConfigControls(detControls);

		Composite offsetButtons = new Composite(controls, SWT.NONE);
		offsetButtons.setLayout(new GridLayout(1, false));
		offsetButtons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		addSaveOffsetsButtons(offsetButtons);

		Composite buttonGroup = new Composite(controls, SWT.NONE);
		buttonGroup.setLayout(new GridLayout(2, false));
		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		addScanControlButtons(buttonGroup);

		// Set some default values
		numberOfRepetitions.setText("1");
		pauseTimeBetweenRepetitions.setText("0.0");
		bidirectionalScan.setSelection(true);
	}

	/**
	 * Load XesScanParameters bean from file in the template directory
	 * @return
	 * @throws Exception
	 */
	private XesScanParameters getBean() throws Exception {
		String configDir = LocalProperties.getConfigDir();
		XesScanParameters params = (XesScanParameters) XMLHelpers.getBean(Paths.get(configDir, templateFileName).toFile());
		params.setScanType(XesScanParameters.SCAN_XES_FIXED_MONO);
		return params;
	}

	/**
	 * Set make new sub-folder inside xml directory of current visit.
	 * A new folder will be created if it doens't exist already .
	 *
	 * @return full path to the directory.
	 */
	private String setupInitialDirectory(String subfolder) {
		var parentDirectory = ExperimentFactory.getExperimentEditorManager().getProjectFolder().toString();
		var qxesDirectory = Paths.get(parentDirectory, subfolder);
		if (!qxesDirectory.toFile().exists() ) {
			boolean ok = qxesDirectory.toFile().mkdirs();
			if (!ok) {
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), null, "Problem creating new directory for XML files ");
			}
		}
		return qxesDirectory.toString();
	}
	private void addDetectorConfigControls(Composite parent) {
		medipixConfigControls = detectorNames.stream()
				.map(name -> addDetectorConfigControl(parent, name))
				.toList();
	}

	private DetectorConfigFileControls addDetectorConfigControl(Composite parent, String detName) {
		DetectorConfig config = new DetectorConfig();
		config.setDetectorName(detName);
		config.setDescription(StringUtils.capitalize(detName));

		Label label = new Label(parent, SWT.NONE);
		label.setText(config.getDescription());
		DetectorConfigFileControls controls = new DetectorConfigFileControls();
		controls.setCurrentDirectory(currentDirectory);
		controls.setTemplateConfiguration(templateConfiguration);
		controls.addControls(parent, config);

		gdFactory.hint(300, SWT.DEFAULT).applyTo(controls.getFilenameTextbox());

		return controls;
	}

	private void addScanControlButtons(Composite parent) {
		Button runScanButton = new Button(parent, SWT.PUSH);
		runScanButton.setText("Run scan");
		runScanButton.addListener(SWT.Selection, l -> {
			String scanCommand = createScanCommand();
			InterfaceProvider.getTerminalPrinter().print("\nCalling function to run QXes scan : "+scanCommand);
			runJythonCommand(scanCommand);
		});

		Button stopScanButton = new Button(parent, SWT.PUSH);
		stopScanButton.setText("Stop scan");
		stopScanButton.addListener(SWT.Selection, l -> runJythonCommand(stopScanCommand));
	}

//	private void stopScan() {
//		try {
//			logger.info("Stopping scan");
//			var runnableDeviceService = ServiceProvider.getService(IRunnableDeviceService.class);
//			var activeScanner = runnableDeviceService.getActiveScanner();
//			// this is always null, even if scan is running!
//			if (activeScanner != null) {
//				activeScanner.abort();
//			} else {
//				logger.info("Cannot stop scan - no scan is currently running");
//			}
//		} catch (ScanningException | InterruptedException e) {
//			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Problem stopping scan",
//					"Problem stopping scan : "+e.getMessage()+
//					"\nSee logs for more details");
//			logger.error("Problem stopping scan", e);
//		}
//	}

	private void addSaveOffsetsButtons(Composite parent) {
		List<String> spectrometerScannableNames = xesScanParametersComposite.getBean().getSpectrometerScanParameters()
				.stream()
				.map(SpectrometerScanParameters::getScannableName)
				.toList();

		spectrometerScannableNames.forEach(name -> addSaveOffsetsButton(parent, name));
	}

	private void addSaveOffsetsButton(Composite parent, String name) {
		Button saveOffsetsButton = new Button(parent, SWT.PUSH);
		saveOffsetsButton.setText("Save offsets for "+name);
		saveOffsetsButton.addListener(SWT.Selection,
				l -> runJythonCommand(String.format(saveOffsetsCommand, name)));
	}


	/**
	 * Extract current text string from VerifyEvent object produced by Text widget
	 * and attempt to convert it to a number of the specified type (Double or Integer).
	 *
	 * Empty strings are treated as valid numbers.
	 *
	 * @param <T>
	 * @param type class type (Double or Integer)
	 * @param event VerifyEvent object
	 * @return true if number could be converted successfully, false otherwise.
	 */
	private <T> boolean verifyNumber(Class<T> type, VerifyEvent event) {
		String currentText = ((Text) event.widget).getText();
		String value = currentText.substring(0, event.start) + event.text	+ currentText.substring(event.end);

		if (value.equals("")) {
			return true;
		}
		try {
			if (type.equals(Integer.class)) {
				Integer.parseInt(value);
			} else if (type.equals(Double.class)) {
				Double.parseDouble(value);
			}
			return true;
		} catch(NumberFormatException nfe) {
			return false;
		}
	}

	/**
	 * Build text strings containing Jython function calls to run the scans
	 *
	 * @return
	 */
	private String createScanCommand() {

		XesScanParameters xesParams = xesScanParametersComposite.getBean();
		int numReps = Integer.parseInt(StringUtils.defaultIfBlank(numberOfRepetitions.getText(), "1"));

		StringBuilder commandBuilder = new StringBuilder();
		commandBuilder.append(runScanFunctionName);
		commandBuilder.append("(");

		ScanColourType colourType = xesParams.getScanColourType();
		if (colourType == ScanColourType.ONE_COLOUR_ROW1 ||
				colourType == ScanColourType.ONE_COLOUR_ROW2) {
			// single row
			Map<String, SpectrometerScanParameters> params = xesParams.getActiveSpectrometerParameters();
			Entry<String, SpectrometerScanParameters> p = params.entrySet().iterator().next();
			SpectrometerScanParameters specParams = p.getValue();

			commandBuilder.append(String.format("%s, %.4f, %.4f, %.4f, %.4f", p.getKey(),
					specParams.getInitialEnergy(), specParams.getFinalEnergy(),
					specParams.getStepSize(), specParams.getIntegrationTime()));

		} else {
			// both rows
			Collection<SpectrometerScanParameters> params = xesParams.getActiveSpectrometerParameters().values();
			var iter = params.iterator();
			SpectrometerScanParameters row1Params = iter.next();
			var row2Params = row1Params;

			if (colourType == ScanColourType.TWO_COLOUR) {
				row2Params = iter.next();
			}

			commandBuilder.append(String.format("%s, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f", "XESEnergyBoth",
					row1Params.getInitialEnergy(), row1Params.getFinalEnergy(), row1Params.getStepSize(),
					row1Params.getIntegrationTime(), row2Params.getInitialEnergy(), row2Params.getStepSize()));

		}
		boolean isBidrectional = bidirectionalScan.getSelection();
		String birectionalString = "is_alternating=" + (isBidrectional ? "True" : "False");
		commandBuilder.append(", "+birectionalString);
		commandBuilder.append(String.format(", num_reps=%d", numReps));
		commandBuilder.append(String.format(", mono_energy=%.4f", xesParams.getMonoEnergy()));

		// add the medipix ROI files
		if (colourType.useRow1()) {
			commandBuilder.append(getDetectorFileString(medipixConfigControls.get(0)));
		}
		if (colourType.useRow2()) {
			commandBuilder.append(getDetectorFileString(medipixConfigControls.get(1)));
		}

		commandBuilder.append(")");

		logger.info("Scan command : {}", commandBuilder);
		return commandBuilder.toString();
	}

	private String getDetectorFileString(DetectorConfigFileControls detControls) {
		if (StringUtils.isEmpty(detControls.getFilename())) {
			return "";
		}
		return String.format(", %s_xml_file=\"%s\"",
				detControls.getDetectorConfig().getDetectorName(),
				detControls.getFilename());
	}

	@Override
	public void setFocus() {
	}

	private void runJythonCommand(String command) {
		logger.info("Running Jython command : {}", command);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}

	public String getSaveOffsetsCommand() {
		return saveOffsetsCommand;
	}

	public void setSaveOffsetsCommand(String saveOffsetsCommand) {
		this.saveOffsetsCommand = saveOffsetsCommand;
	}

	public String getStopScanCommand() {
		return stopScanCommand;
	}

	public void setStopScanCommand(String stopScanCommand) {
		this.stopScanCommand = stopScanCommand;
	}

	public String getRunScanFunctionName() {
		return runScanFunctionName;
	}

	public void setRunScanFunctionName(String runScanFunctionName) {
		this.runScanFunctionName = runScanFunctionName;
	}

	public String getInitialXmlSubFolderName() {
		return initialXmlSubFolderName;
	}

	public void setInitialXmlSubFolderName(String initialXmlSubFolderName) {
		this.initialXmlSubFolderName = initialXmlSubFolderName;
	}
}
