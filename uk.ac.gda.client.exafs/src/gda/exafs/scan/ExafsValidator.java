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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nfunk.jep.JEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.Element;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.data.ScanObject;

/**
 * Abstract to hold generic XAS validations for beamlines using the server.exafs plugin
 */
public abstract class ExafsValidator extends AbstractValidator {

	protected JEP jepParser;

	protected static final List<String> EDGES = Arrays.asList(new String[] { "K", "L1", "L2", "L3" });
	protected static boolean isCheckingFinables = true;
	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<',
			'>', '|', '\"', ':', '@', '!', '$', '#', '%', '&', '(', ')' };

	protected ScanObject bean;

	private static final Logger logger = LoggerFactory.getLogger(ExafsValidator.class);

	public List<InvalidBeanMessage> validateIOutputParameters(IOutputParameters iOutputParams) {

		if (iOutputParams == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);

		if (!(iOutputParams instanceof OutputParameters)) {
			errors.add(new InvalidBeanMessage("Unknown Output Type " + iOutputParams.getClass().getName()));
		} else {
			errors.addAll(validateOutputParameters((OutputParameters) iOutputParams));
		}

		if (bean != null) {
			setFileName(errors, bean.getOutputFileName());
		}
		return errors;
	}

	public List<InvalidBeanMessage> validateOutputParameters(OutputParameters o) {
		if (!o.isShouldValidate()) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);

		checkRegExp("Ascii Name", o.getAsciiFileName(), "[a-zA-Z0-9_\\-]+", errors,
				"Only alpha-numeric characters (a-z,A-Z,0-9,_-) are allowed.");
		checkRegExp("Ascii Folder", o.getAsciiDirectory(), "[a-zA-Z0-9_\\-]+", errors,
				"Only alpha-numeric characters (a-z,A-Z,0-9,_-) are allowed.");
		checkRegExp("Nexus Folder", o.getNexusDirectory(), "[a-zA-Z0-9_\\-]+", errors,
				"Only alpha-numeric characters (a-z,A-Z,0-9,_-) are allowed.");

		final List<SignalParameters> sig = o.getCheckedSignalList();
		for (SignalParameters sp : sig) {

			checkRegExp("Label", sp.getLabel(), "[a-zA-Z0-9_\\-]+", errors,
					"Only alpha-numeric characters (a-z,A-Z,0-9) and '_' and '-' are allowed.",
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

	protected InvalidBeanMessage checkExpressionSyntax(final String label, final String value,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (value == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " has no value and this is not allowed.",
					messages);
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
					messages);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}

		return null;
	}

	public List<InvalidBeanMessage> validateIDetectorParameters(IDetectorParameters iDetectorParams) {

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);

		if (iDetectorParams == null) {
			errors.add(new InvalidBeanMessage("Missing or Invalid Detector Paramters"));
			return errors;
		} else if (!(iDetectorParams instanceof DetectorParameters)) {
			errors.add(new InvalidBeanMessage("Unknown Detector Type " + iDetectorParams.getClass().getName()));
			return errors;
		}

		String fileName = "";
		if (iDetectorParams.getExperimentType().equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE))
			fileName = iDetectorParams.getFluorescenceParameters().getConfigFileName();
		else if (iDetectorParams.getExperimentType().equalsIgnoreCase(DetectorParameters.XES_TYPE))
			fileName = iDetectorParams.getXesParameters().getConfigFileName();

		if (fileName == null || fileName.isEmpty())
				errors.add(new InvalidBeanMessage("Fluorescence detector XML configuration file not specified!"));

		if (bean != null) {
			setFileName(errors, bean.getDetectorFileName());
		}
		return errors;
	}

	public List<InvalidBeanMessage> validateXanesScanParameters(XanesScanParameters x) {

		if (x == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!x.isShouldValidate()) {
			return errors;
		}

		if (!isARealScannable(x.getScannableName())) {
			errors.add(new InvalidBeanMessage("The scannable " + x.getScannableName() + " cannot be found!"));
		}

		try {
			x.checkRegions();
		} catch (Exception e) {
			errors.add(new InvalidBeanMessage(e.getMessage()));
		}

		String minElement = LocalProperties.get("gda.exafs.element.min", "P");
		String maxElement = LocalProperties.get("gda.exafs.element.max", "U");
		if (!Arrays.asList(Element.getSortedEdgeSymbols(minElement, maxElement)).contains(x.getElement())) {
			errors.add(new InvalidBeanMessage("The element '" + x.getElement()
					+ "' is not currently allowed to be scanned."));
		}

		if (!EDGES.contains(x.getEdge())) {
			errors.add(new InvalidBeanMessage("The edge '" + x.getEdge() + "' is not currently allowed to be scanned."));
		}

		return errors;
	}

	public List<InvalidBeanMessage> validateXasScanParameters(XasScanParameters x, double beamlineMinEnergy,
			double beamlineMaxEnergy) {

		if (x == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!x.isShouldValidate()) {
			return errors;
		}

		if (!isARealScannable(x.getScannableName())) {
			errors.add(new InvalidBeanMessage("The scannable " + x.getScannableName() + " cannot be found!"));
		}

		String minElement = LocalProperties.get("gda.exafs.element.min", "P");
		String maxElement = LocalProperties.get("gda.exafs.element.max", "U");
		if (!Arrays.asList(Element.getSortedEdgeSymbols(minElement, maxElement)).contains(x.getElement())) {
			errors.add(new InvalidBeanMessage("The element '" + x.getElement()
					+ "' is not currently allowed to be scanned."));
		}

		if (!EDGES.contains(x.getEdge())) {
			errors.add(new InvalidBeanMessage("The edge '" + x.getEdge() + "' is not currently allowed to be scanned."));
		}

		final double initialE = x.getInitialEnergy();
		final double finalE = x.getFinalEnergy();
		if (initialE >= finalE) {
			errors.add(new InvalidBeanMessage("The initial energy is greater than or equal to the final energy."));
		}

		checkBounds("Initial Energy", initialE, beamlineMinEnergy, finalE, errors);
		checkBounds("Final Energy", finalE, initialE, beamlineMaxEnergy, errors);

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
		Findable obj = Finder.getInstance().find(scannableName);
		if (obj != null) {
			return obj instanceof Scannable;
		}
		return false;
	}

	/**
	 * Used in testing mode to switch off checking of findables which are not there.
	 *
	 * @param isChecking
	 */
	public static final void _setCheckingFinables(boolean isChecking) {
		isCheckingFinables = isChecking;
	}

	protected void checkFindable(final String label, final String deviceName, final Class<? extends Findable> clazz,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (!isCheckingFinables) {
			return;
		}

		if (deviceName == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " has no value and this is not allowed.",
					messages);
			msg.setLabel(label);
			errors.add(msg);
			return;
		}

		try {
			final Findable findable = Finder.getInstance().findNoWarn(deviceName);
			if (findable == null) {
				InvalidBeanMessage msg = new InvalidBeanMessage("Cannot find '" + deviceName + "' for input '" + label
						+ "'.", messages);
				msg.setLabel(label);
				errors.add(msg);
				return;
			}

			if (!clazz.isInstance(findable)) {
				InvalidBeanMessage msg = new InvalidBeanMessage("'" + deviceName + "' should be a '" + clazz.getName()
						+ "' but is a '" + findable.getClass() + "'.", messages);
				msg.setLabel(label);
				errors.add(msg);
			}

		} catch (Exception ne) {
			InvalidBeanMessage msg = new InvalidBeanMessage("Cannot find '" + deviceName + "' for input '" + label
					+ "'.", messages);
			msg.setLabel(label);
			errors.add(msg);
		}

	}

	protected boolean stringCouldBeConvertedToValidUnixFilename(String sampleName) {
		// ignore spaces as these will have underscores automatically substituted
		if (sampleName.startsWith("-")) {
			return false;
		}
		for (char thischar : ILLEGAL_CHARACTERS) {
			if (sampleName.indexOf(thischar) > 0) {
				return false;
			}
		}
		return true;
	}

}
