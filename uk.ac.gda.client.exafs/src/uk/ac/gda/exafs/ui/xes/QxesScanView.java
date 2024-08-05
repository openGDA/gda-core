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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.ScanColourType;
import uk.ac.gda.beans.exafs.SpectrometerScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.exafs.ui.XesScanParametersComposite;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class QxesScanView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(QxesScanView.class);

	public static final String ID = "uk.ac.gda.exafs.ui.xes.QxesScanView";

	private GridDataFactory gdFactory = GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT);

	private Text numberOfRepetitions;
	private Text pauseTimeBetweenRepetitions;
	private Button bidirectionalScan;
	private XesScanParametersComposite xesScanParametersComposite;

	// Path to qxes template file inside beamline config folder
	private String templateFile = "var/templates/QXES_Parameters.xml";

	private String jythonFunctionName = "prepare_run_qxes_scan";

	private XesScanParameters getBean() throws Exception {
		String configDir = LocalProperties.getConfigDir();
		XesScanParameters params = (XesScanParameters) XMLHelpers.getBean(Paths.get(configDir, templateFile).toFile());
		params.setScanType(XesScanParameters.SCAN_XES_FIXED_MONO);
		return params;
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

		xesScanParametersComposite.setupUiFromBean();

		// Add the QXes specific widgets
		createQxesControls(xesScanParametersComposite);

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
	private void createQxesControls(Composite parent) {

		Composite controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new GridLayout(2, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label repetitionsLabel = new Label(controls, SWT.NONE);
		repetitionsLabel.setText("Number of repetitions");

		numberOfRepetitions = new Text(controls, SWT.NONE);
		numberOfRepetitions.addVerifyListener(ev -> ev.doit = verifyNumber(Integer.class, ev));

		Label pauseLabel = new Label(controls, SWT.NONE);
		pauseLabel.setText("Pause between repetitions (secs)");

		pauseTimeBetweenRepetitions = new Text(controls, SWT.NONE);
		pauseTimeBetweenRepetitions.addVerifyListener(ev -> ev.doit = verifyNumber(Double.class, ev));

		Label bidrectionalLabel = new Label(controls, SWT.NONE);
		bidrectionalLabel.setText("Bi-directional scan");

		bidirectionalScan = new Button(controls, SWT.CHECK);

		// labels
		gdFactory.applyTo(repetitionsLabel);
		gdFactory.applyTo(pauseLabel);
		gdFactory.applyTo(bidrectionalLabel);

		// control widgets
		int width = 75;

		gdFactory.hint(width, SWT.DEFAULT).applyTo(numberOfRepetitions);
		gdFactory.hint(width, SWT.DEFAULT).applyTo(pauseTimeBetweenRepetitions);
		gdFactory.hint(width, SWT.DEFAULT).applyTo(bidirectionalScan);

		// Set some default values
		numberOfRepetitions.setText("1");
		pauseTimeBetweenRepetitions.setText("0.0");
		bidirectionalScan.setSelection(true);

		Button runScanButton = new Button(controls, SWT.PUSH);
		runScanButton.addListener(SWT.Selection, l -> {
			String scanCommand = createScanCommand();
			InterfaceProvider.getTerminalPrinter().print("Calling function to run QXes scan : "+scanCommand);
			InterfaceProvider.getCommandRunner().runCommand(scanCommand);
		});
		runScanButton.setText("Run scan");
	}

	private String extractNewString(VerifyEvent ev) {
		String currentText = ((Text) ev.widget).getText();
		return currentText.substring(0, ev.start) + ev.text	+ currentText.substring(ev.end);
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
		String value = extractNewString(event);
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
		commandBuilder.append(jythonFunctionName);
		commandBuilder.append("(");

		if (xesParams.getScanColourType() == ScanColourType.ONE_COLOUR_ROW1 ||
				xesParams.getScanColourType() == ScanColourType.ONE_COLOUR_ROW2) {
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

			if (xesParams.getScanColourType() == ScanColourType.TWO_COLOUR) {
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
		commandBuilder.append(")");

		logger.info("Scan command : {}", commandBuilder);
		return commandBuilder.toString();
	}

	@Override
	public void setFocus() {

	}

}
