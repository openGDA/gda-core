/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.nfunk.jep.JEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.medipix.MedipixParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.beans.validation.WarningType;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.ElementEdgeEditor;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Abstract to hold generic XAS validations for beamlines using the server.exafs plugin
 */
public abstract class ExafsValidator extends AbstractValidator {

	private static final Logger logger = LoggerFactory.getLogger(ExafsValidator.class);

	private JEP jepParser;

	private static final List<String> EDGES = Arrays.asList("K", "L1", "L2", "L3");
	private static final List<Character> ILLEGAL_CHARACTERS = Arrays.asList('/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<',
			'>', '|', '\"', ':', '@', '!', '$', '#', '%', '&', '(', ')');

	private static final String ALPHA_NUMERIC_REGEX = "[a-zA-Z0-9_\\-]+";
	private static final String ALPHA_NUMERIC_WARNING = "Only alpha-numeric characters (a-z,A-Z,0-9,_-) are allowed.";


	protected ScanObject bean;

	@Override
	public void validate(IExperimentObject b) throws InvalidBeanException {
		this.bean = (ScanObject) b;

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);

		try {
			errors.addAll(validateIScanParameters(bean.getScanParameters(), bean.getDetectorParameters()));
			errors.addAll(validateISampleParameters(bean.getSampleParameters()));
			errors.addAll(validateIDetectorParameters(bean.getDetectorParameters()));
			errors.addAll(validateIOutputParameters(bean.getOutputParameters()));
		} catch (Exception e) {
			throw new InvalidBeanException("Exception retrieving parameters objects: " + e.getMessage());
		}

		if (!errors.isEmpty()) {
			for (InvalidBeanMessage invalidBeanMessage : errors) {
				invalidBeanMessage.setFolderName(bean.getFolder().getName());
			}
			throw new InvalidBeanException(errors);
		}

	}

	// Generic implementation of validateIScanParameters: beamlines may want to override
	protected List<InvalidBeanMessage> validateIScanParameters(IScanParameters scanParams, IDetectorParameters detParams) {
		final List<InvalidBeanMessage> errors = new ArrayList<>();

		if (scanParams instanceof XasScanParameters xasParams) {
			errors.addAll(validateXasScanParameters(xasParams, getMinEnergy(), getMaxEnergy()));
		} else if (scanParams instanceof XanesScanParameters xanesParams) {
			errors.addAll(validateXanesScanParameters(xanesParams));
		} else if (scanParams instanceof XesScanParameters xesParams) {
			errors.addAll(validateXesScanParameters(xesParams, detParams));
		} else if (scanParams instanceof MicroFocusScanParameters microFocusParams) {
			errors.addAll(validateMicroFocusParameters(microFocusParams));
		} else if (scanParams instanceof QEXAFSParameters qexafsParams) {
			errors.addAll(validateQEXAFSParameters(qexafsParams));
		} else if (scanParams == null) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "Missing or Invalid Scan Parameters"));
		} else {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "Unknown Scan Type " + scanParams.getClass().getName()));
		}
		if (bean != null) {
			setFileName(errors, bean.getScanFileName());
		}
		return errors;
	}

	protected List<InvalidBeanMessage> validateGenericISampleParameters(ISampleParameters sampleParameters) {
		InvalidBeanMessage invalidBeanMessage;
		if (sampleParameters == null) {
			try {
				if (bean != null && bean.isMicroFocus()) {
					// do not have a sample file for microfocus scans
					return Collections.emptyList();
				}
				// else its missing
				invalidBeanMessage = new InvalidBeanMessage(WarningType.HIGH, "Missing or Invalid Sample Parameters");
			} catch (Exception e) {
				invalidBeanMessage = new InvalidBeanMessage(WarningType.HIGH,
						"Error testing if bean is a microfocus scan when testing Scan parameters from bean");
			}
		} else {
			invalidBeanMessage = new InvalidBeanMessage(WarningType.HIGH, "Unknown Sample Type " + sampleParameters.getClass().getName());
		}
		if (bean != null) {
			invalidBeanMessage.setFileName(bean.getSampleFileName());
		}
		final ArrayList<InvalidBeanMessage> errors = new ArrayList<>();
		errors.add(invalidBeanMessage);
		return errors;
	}

	protected List<InvalidBeanMessage> validateIOutputParameters(IOutputParameters iOutputParams) {

		if (iOutputParams == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<>();

		if (iOutputParams instanceof OutputParameters outputParams) {
			errors.addAll(validateOutputParameters(outputParams));
		} else {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "Unknown Output Type " + iOutputParams.getClass().getName()));
		}

		if (bean != null) {
			setFileName(errors, bean.getOutputFileName());
		}
		return errors;
	}

	private List<InvalidBeanMessage> validateOutputParameters(OutputParameters o) {
		if (!o.isShouldValidate()) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<>();

		checkRegExp("Ascii Name", o.getAsciiFileName(), ALPHA_NUMERIC_REGEX, errors, ALPHA_NUMERIC_WARNING);
		checkRegExp("Ascii Folder", o.getAsciiDirectory(), ALPHA_NUMERIC_REGEX, errors, ALPHA_NUMERIC_WARNING);
		checkRegExp("Nexus Folder", o.getNexusDirectory(), ALPHA_NUMERIC_REGEX, errors, ALPHA_NUMERIC_WARNING);

		final List<SignalParameters> sig = o.getCheckedSignalList();
		for (SignalParameters sp : sig) {

			checkRegExp("Label", sp.getLabel(), ALPHA_NUMERIC_REGEX, errors, ALPHA_NUMERIC_WARNING,
					"The label is used as a scannable name and recorded in the nexus file.",
					"This gives limitations as to what the signal parameter label may be.");

			checkFindable("Scannable Name", sp.getScannableName(), Scannable.class, errors);

			if (sp.getName() != null && !sp.getName().isEmpty()) {
				checkRegExp("Variable Name", sp.getName(), "[a-zA-Z0-9_]+", errors,
						"Only alpha-numeric characters (a-z,A-Z,0-9) and '_' are allowed.");
			}
			if (sp.getExpression() != null && !sp.getExpression().isEmpty()) {
				checkExpressionSyntax("Expression", sp.getExpression(), errors);
			}
		}
		return errors;
	}

	/**
	 * Implementation is for I20 only - in I20Validator
	 * @param x
	 * @param detParams
	 * @return empty list
	 */
	protected List<InvalidBeanMessage> validateXesScanParameters(XesScanParameters x, IDetectorParameters detParams) {
		return Collections.emptyList();
	}

	protected List<InvalidBeanMessage> validateMicroFocusParameters(MicroFocusScanParameters x) {
		// TODO add validation for MicroFocus
		return Collections.emptyList();
	}

	protected List<InvalidBeanMessage> validateQEXAFSParameters(QEXAFSParameters x) {
		if (x == null || !x.isShouldValidate()) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<>();
		checkEnergyRange("QExafs", x.getInitialEnergy(), x.getFinalEnergy(), getMinEnergy(), getMaxEnergy(), errors);
		return errors;
	}

	private InvalidBeanMessage checkExpressionSyntax(final String label, final String value,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (value == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " has no value and this is not allowed.",
					messages, WarningType.HIGH);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}

		if (jepParser == null) {
			jepParser = new JEP();
			jepParser.addStandardFunctions();
			jepParser.addStandardConstants();
			jepParser.setAllowUndeclared(true);
			jepParser.setImplicitMul(true);
		}
		try {
			jepParser.parse(value);
		} catch (Throwable ne) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " of '" + value + "' is not allowed.",
					messages, WarningType.HIGH);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}

		return null;
	}

	/**
	 * Examine the detector xml file and check that the elements it has settings for matches the number of
	 * elements actually on the detector. Add to error message if the number of elements do not match.
	 * @param errors
	 * @param iDetectorParams
	 * @since 26/9/2017
	 */
	public void checkDetectorElements(List<InvalidBeanMessage> errors, IDetectorParameters iDetectorParams) {

		if (!iDetectorParams.getExperimentType().equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE)) return;

		FluorescenceParameters params = iDetectorParams.getFluorescenceParameters();

		// Get full path to detector config XML file
		String configFileName = params.getConfigFileName();
		String fullPathToConfig = bean.getFolder().getFile(configFileName).getLocation().toString();

		checkDetectorXmlFile(fullPathToConfig).ifPresent(msg -> errors.add(new InvalidBeanMessage(WarningType.HIGH, msg)));
	}

	private Optional<String> checkDetectorXmlFile(String fullPathToConfig) {
		// Load from XML file and make bean object
		XMLRichBean detectorSettingsBean;

		try {
			detectorSettingsBean = XMLHelpers.getBean(new File(fullPathToConfig));
		} catch(Exception e) {
			return Optional.of("Problem reading XML file "+fullPathToConfig);
		}

		if (detectorSettingsBean instanceof FluorescenceDetectorParameters fluoParams) {
			return checkFluoDetectorParameters(fullPathToConfig, fluoParams);
		} else if (detectorSettingsBean instanceof MedipixParameters medipixParams) {
			return checkMedipixParameters(fullPathToConfig, medipixParams);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Check the fluorescence detector parameters :
	 * <li> Check that the named detector exists on the server and is a FluorescenceDetector
	 * <li> Check that the number of elements in the configuration matches the number of elements on the detector.
	 * @param fullPathToConfig
	 * @param parameters
	 * @return
	 */
	private Optional<String> checkFluoDetectorParameters(String fullPathToConfig, FluorescenceDetectorParameters parameters) {
		String configFileName = FilenameUtils.getName(fullPathToConfig);

		// final modifier unfortunately required by Optional of FluorescenceDetector
		final String detectorName = parameters.getDetectorName();
		final int numElementsInXml = parameters.getDetectorList().size();

		String errorMsg = null;
		// Try to get detector object (should have been exported from server to client using FluorescenceDetector interface)
		Optional<FluorescenceDetector> detector = Finder.findOptionalOfType(detectorName, FluorescenceDetector.class);

		if (detector.isPresent()) {
			int numElementsOnDetector = detector.get().getNumberOfElements();
			if (numElementsInXml != numElementsOnDetector) {
				String messageFormat = "Number of detector elements specified in XML file %s does not match number of elements on detector '%s'.\n" +
						"Expected %d elements but XML has %d.";
				errorMsg = String.format(messageFormat, configFileName, detectorName, numElementsOnDetector, numElementsInXml);
			}
		} else {
			String messageFormat = "Cannot find detector '%s'.\\nIs name of detector in XML file %s correct?";
			errorMsg = String.format(messageFormat, detectorName, configFileName);
		}
		return Optional.ofNullable(errorMsg);
	}

	/**
	 * Check that medipix parameters have at least 1 ROI set.
	 *
	 * @param fullPathToConfig
	 * @param parameters
	 * @return
	 */
	private Optional<String> checkMedipixParameters(String fullPathToConfig, MedipixParameters parameters) {
		if (parameters.getRegionList() == null || parameters.getRegionList().isEmpty()) {
			return Optional.of("No ROIs have been set in Medipix XML file "+fullPathToConfig);
		}
		return Optional.empty();
	}

	public List<InvalidBeanMessage> validateIDetectorParameters(IDetectorParameters iDetectorParams) {

		final List<InvalidBeanMessage> errors = new ArrayList<>();

		if (iDetectorParams == null) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "Missing or Invalid Detector Paramters"));
			return errors;
		} else if (!(iDetectorParams instanceof DetectorParameters)) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "Unknown Detector Type " + iDetectorParams.getClass().getName()));
			return errors;
		}

		String experimentType = iDetectorParams.getExperimentType();
		// Check the detectors in DetectorConfig list
		List<DetectorConfig> detectorConfigs = iDetectorParams.getDetectorConfigurations();
		if (experimentType==null) {
			if (detectorConfigs==null || detectorConfigs.isEmpty()) {
				errors.add(new InvalidBeanMessage(WarningType.HIGH, "No detector configuration parameters are present in the file"));
				return errors;
			}
			return validateDetectorConfigList(iDetectorParams.getDetectorConfigurations());
		}

		String fileName = "";
		if (experimentType.equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE))
			fileName = iDetectorParams.getFluorescenceParameters().getConfigFileName();
		else if (experimentType.equalsIgnoreCase(DetectorParameters.XES_TYPE))
			fileName = iDetectorParams.getXesParameters().getConfigFileName();

		if (fileName == null || fileName.isEmpty() && !experimentType.equalsIgnoreCase(DetectorParameters.TRANSMISSION_TYPE))
				errors.add(new InvalidBeanMessage(WarningType.HIGH, "Fluorescence detector XML configuration file not specified!"));

		if (bean != null) {
			setFileName(errors, bean.getDetectorFileName());
		}

		try {
			checkDetectorElements(errors, iDetectorParams);
		} catch (Exception e) {
			logger.warn("Problem comparing number of detector elements with actual number of elements available on detector", e);
		}

		return errors;
	}


	private List<InvalidBeanMessage> validateDetectorConfigList(List<DetectorConfig> detectorConfigs) {
		final List<InvalidBeanMessage> errors = new ArrayList<>();
		if (detectorConfigs==null || detectorConfigs.isEmpty()) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "No detector configuration parameters are present in the file"));
			return errors;
		}
		for(DetectorConfig config : detectorConfigs) {
			if (!config.isUseDetectorInScan()) {
				continue;
			}

			// Check all the detector objects exist
			for(String detName : config.getAllDetectorNames()) {
				if (Finder.findOptionalOfType(detName, Scannable.class).isEmpty()) {
					String message ="Could not find detector object "+detName+" needed for selected detector '"+config.getDescription()+"'";
					errors.add(new InvalidBeanMessage(WarningType.HIGH, message));
				}
			}
			// If using one, check that the config file exists and is valid
			if (Boolean.TRUE.equals(config.isUseConfigFile())) {
				String filename = config.getConfigFileName();
				IPath fullPathToConfig = bean.getFolder().getFile(filename).getLocation();

				if (fullPathToConfig == null) {
					errors.add(new InvalidBeanMessage(WarningType.HIGH, "Could not find config file "+filename+" for "+config.getDescription()));
				} else {
					checkDetectorXmlFile(fullPathToConfig.toString()).ifPresent(msg ->
						errors.add(new InvalidBeanMessage(WarningType.HIGH, msg)) );
				}
			}
		}
		return errors;
	}

	protected List<InvalidBeanMessage> validateXanesScanParameters(XanesScanParameters xanesParams) {

		if (xanesParams == null || !xanesParams.isShouldValidate()) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<>();
		if (!isARealScannable(xanesParams.getScannableName())) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "The scannable " + xanesParams.getScannableName() + " cannot be found!"));
		}

		try {
			xanesParams.checkRegions();
		} catch (Exception e) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, e.getMessage()));
		}


		if (StringUtils.isNotEmpty(xanesParams.getElement())) {
			checkElementEdge(xanesParams.getElement(), xanesParams.getEdge(), errors);
		}
		return errors;
	}

	protected List<InvalidBeanMessage> validateXasScanParameters(XasScanParameters x, double beamlineMinEnergy,
			double beamlineMaxEnergy) {

		if (x == null || !x.isShouldValidate()) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<>();
		if (!isARealScannable(x.getScannableName())) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "The scannable " + x.getScannableName() + " cannot be found!"));
		}

		checkElementEdge(x.getElement(), x.getEdge(), errors);

		checkEnergyRange("XAS", x.getInitialEnergy(), x.getFinalEnergy(), beamlineMinEnergy, beamlineMaxEnergy, errors);
		checkBounds("Gaf1", x.getGaf1(), 0d, 100d, errors);
		checkBounds("Gaf2", x.getGaf2(), 0d, x.getGaf1(), errors);
		checkBounds("A", x.getA(), x.getInitialEnergy(), x.getB(), errors);

		final Element element = Element.getElement(x.getElement());
		if (element != null) {
			final double edgeEn = element.getEdgeEnergy(x.getEdge());
			checkBounds("B", x.getB(), x.getA(), edgeEn, errors);
		}

		checkBounds("Pre Edge Step", x.getPreEdgeStep(), 0d, 20d, errors);
		checkBounds("Pre Edge Time", x.getPreEdgeTime(), 0d, 20d, errors);
		checkBounds("Edge Step", x.getEdgeStep(), 0d, 20d, errors);
		checkBounds("Edge Time", x.getEdgeTime(), 0d, 20d, errors);

		checkBounds("Exafs Step Energy", x.getExafsStep(), 0d, 100d, errors);
		checkBounds("Exafs Step Time", x.getExafsTime(), 0d, 20d, errors);
		checkBounds("Exafs From Time", x.getExafsFromTime(), 0d, x.getExafsToTime(), errors);
		checkBounds("Exafs To Time", x.getExafsToTime(), x.getExafsFromTime(), 20d, errors);

		return errors;

	}

	private boolean isARealScannable(String scannableName) {
		Findable obj = Finder.find(scannableName);
		if (obj != null) {
			return obj instanceof Scannable;
		}
		return false;
	}

	protected void checkFindable(String label, String deviceName, Class<? extends Findable> clazz,
			List<InvalidBeanMessage> errors, String... messages) {

		if (deviceName == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " has no value and this is not allowed.",
					messages, WarningType.HIGH);
			msg.setLabel(label);
			errors.add(msg);
			return;
		}

		try {
			if (Finder.findOptionalOfType(deviceName,clazz).isEmpty()) {
				String primaryMessageFormat = "Cannot find '{0}' (of type {1}) for input '{2}'.";
				String primaryMessage = String.format(primaryMessageFormat, deviceName, clazz.getName(), label);
				InvalidBeanMessage msg = new InvalidBeanMessage(primaryMessage, messages, WarningType.HIGH);
				msg.setLabel(label);
				errors.add(msg);
			}
		} catch (Exception ne) {
			InvalidBeanMessage msg = new InvalidBeanMessage("Cannot find '" + deviceName + "' for input '" + label
					+ "'.", messages, WarningType.HIGH);
			msg.setLabel(label);
			errors.add(msg);
		}

	}

	protected void checkEnergyRange(String namePrefix, double initialEnergy, double finalEnergy, List<InvalidBeanMessage> errors) {
		checkEnergyRange(namePrefix, initialEnergy, finalEnergy, 0.0, 35000.0, errors);
	}

	private void checkEnergyRange(String namePrefix, double initialEnergy, double finalEnergy, double minEnergy, double maxEnergy, List<InvalidBeanMessage> errors) {
		if (initialEnergy >= finalEnergy) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "The "+namePrefix+" initial energy is greater than or equal to the final energy."));
		}
		checkBounds(namePrefix+" Initial Energy", initialEnergy, minEnergy, finalEnergy, errors);
		checkBounds(namePrefix+" Final Energy", finalEnergy, initialEnergy, maxEnergy, errors);
	}

	private void checkElementEdge(String elementSymbol, String edgeSymbol, List<InvalidBeanMessage> errors) {
		String minElement = LocalProperties.get(ElementEdgeEditor.EXAFS_MIN_ELEMENT_PROP, "P");
		String maxElement = LocalProperties.get(ElementEdgeEditor.EXAFS_MAX_ELEMENT_PROP, "U");
		if (!Arrays.asList(Element.getSortedEdgeSymbols(minElement, maxElement)).contains(elementSymbol)) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "The element '" + elementSymbol
				+ "' is not currently allowed to be scanned."));
		}

		if (!EDGES.contains(edgeSymbol)) {
			errors.add(new InvalidBeanMessage(WarningType.HIGH, "The edge '" + edgeSymbol + "' is not currently allowed to be scanned."));
		}
	}

	protected boolean stringCouldBeConvertedToValidUnixFilename(String sampleName) {
		// ignore spaces as these will have underscores automatically substituted
		if (sampleName.startsWith("-")) {
			return false;
		}
		for (char thischar : ILLEGAL_CHARACTERS) {
			if (sampleName.indexOf(thischar) != -1) {
				return false;
			}
		}
		return true;
	}

	protected double getMinEnergy() {
		return Double.MIN_VALUE;
	}

	protected double getMaxEnergy() {
		return Double.MAX_VALUE;
	}

	protected abstract List<InvalidBeanMessage> validateISampleParameters(ISampleParameters sampleParameters);
}
