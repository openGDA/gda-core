/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nfunk.jep.JEP;

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

	protected ScanObject bean;

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

			String dataFormat = sp.getDataFormat();
			if (!dataFormat.startsWith("%")) {
				dataFormat = "%" + dataFormat;
			}
			checkRegExp("Data Format", dataFormat, "%\\'?\\-?(\\d)+\\.(\\d)+[cdxosqrfkpeg]", errors,
					"This must be a printf format but you can optionally remove the '%'.", "",
					"Usage: %['][-][number][.number]character");

			if (sp.getName() != null) {
				checkRegExp("Variable Name", sp.getName(), "[a-zA-Z0-9_]+", errors,
						"Only alpha-numeric characters (a-z,A-Z,0-9) and '_' are allowed.");
			}
			if (sp.getExpression() != null) {
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

		if (!(iDetectorParams instanceof DetectorParameters)) {
			if (iDetectorParams == null) {
				errors.add(new InvalidBeanMessage("Missing or Invalid Detector Paramters"));
			} else {
				errors.add(new InvalidBeanMessage("Unknown Detector Type " + iDetectorParams.getClass().getName()));
			}
		} 

		if (bean != null) {
			setFileName(errors, bean.getDetectorFileName());
		}
		return errors;
	}

	
	 // this section unused?
//	private List<InvalidBeanMessage> validateDetectorParameters(DetectorParameters d) {
//		if (!d.isShouldValidate()) {
//			return Collections.emptyList();
//		}
//
//		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
//		XasScanParameters xasScanParams;
//		try {
//			if (bean != null && bean.getScanParameters() instanceof XasScanParameters) {
//				xasScanParams = (XasScanParameters) bean.getScanParameters();
//			} else {
//				xasScanParams = null;
//			}
//		} catch (Exception e) {
//			logger.warn("Exception retrieving Scan Parameters" + e.getMessage());
//			xasScanParams = null;
//		}

		// final XasScanParameters xasScanParams = xasScanParams1;
//		final String exprType = d.getExperimentType();
//		if ("Transmission".equalsIgnoreCase(exprType)) {
//
//			final TransmissionParameters t = d.getTransmissionParameters();
//			final String message = "The transmission parameters are out of bounds.";
//			checkBounds("Working energy", t.getWorkingEnergy(), MINENERGY, MAXENERGY, errors, message);
//			if (xasScanParams != null) {
//				checkBounds("Working energy", t.getWorkingEnergy(), xasScanParams.getInitialEnergy(),
//						xasScanParams.getFinalEnergy(), errors, message);
//			}
//
//			checkIonChambers(t.getIonChamberParameters(), t.getWorkingEnergy(), errors);
//
//		} else if (("Fluorescence").equalsIgnoreCase(exprType)) {
//
//			final FluorescenceParameters f = d.getFluorescenceParameters();
//			final String message = "The fluorescence parameters are out of bounds.";
//			checkBounds("Working energy", f.getWorkingEnergy(), MINENERGY, MAXENERGY, errors, message);
//			if (xasScanParams != null) {
//				checkBounds("Working energy", f.getWorkingEnergy(), xasScanParams.getInitialEnergy(),
//						xasScanParams.getFinalEnergy(), errors, message);
//			}
//
//			checkIonChambers(f.getIonChamberParameters(), f.getWorkingEnergy(), errors);
//
//		}
//
//		return errors;
//	}

//	private void checkIonChambers(final List<IonChamberParameters> ionChamberParameters, final double workingEnergy,
//			final List<InvalidBeanMessage> errors) {
//
//		for (IonChamberParameters icp : ionChamberParameters) {
//			if (icp.isUseGasProperties()) {
//				checkValue("Gas Type", icp.getGasType(), new String[] { "He", "N", "Ar", "Kr" }, errors);
//				checkBounds("Percent Absorption", icp.getPercentAbsorption(), 0, 100, errors);
//				checkFindable("Device Name", icp.getDeviceName(), Detector.class, errors);
//				checkBounds("Channel", icp.getChannel(), 1, 32, errors);
//				checkFindable("Current Amplifier Name", icp.getCurrentAmplifierName(), CurrentAmplifier.class, errors);
//				checkBounds("Total Pressure", icp.getTotalPressure(), 0, MAXPRESSURE, errors);
//				checkBounds("Ion Chamber Length", icp.getIonChamberLength(), 1, 1000, errors);
//
//				try {
//					icp.setWorkingEnergy(workingEnergy);
//					if (icp.getAutoFillGas()){
//						final PressureBean bean = PressureCalculation.getPressure(icp);
//						if (bean != null) {
//							if (bean.getErrorMessage() != null) {
//								// ignore error messages about mucal code not working - these are logged anyway and
//								// should
//								// not prevent the experiment from proceeding
//								if (!bean.getErrorMessage().contains("operating system")) {
//									errors.add(new InvalidBeanMessage(bean.getErrorMessage(), bean.getErrorTooltip()));
//								}
//							}
//						}
//					}
//				} catch (Exception e) {
//					errors.add(new InvalidBeanMessage(e.getMessage()));
//				}
//			}
//		}
//	}



	public List<InvalidBeanMessage> validateXanesScanParameters(XanesScanParameters x) {

		if (x == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!x.isShouldValidate()) {
			return errors;
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

	public List<InvalidBeanMessage> validateXasScanParameters(XasScanParameters x, double beamlineMinEnergy, double beamlineMaxEnergy) {

		if (x == null) {
			return Collections.emptyList();
		}

		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!x.isShouldValidate()) {
			return errors;
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

}
