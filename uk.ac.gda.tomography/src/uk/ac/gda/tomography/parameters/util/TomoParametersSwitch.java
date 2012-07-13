/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

import uk.ac.gda.tomography.parameters.*;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.SampleParams;
import uk.ac.gda.tomography.parameters.SamplePosition;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage
 * @generated
 */
public class TomoParametersSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static TomoParametersPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TomoParametersSwitch() {
		if (modelPackage == null) {
			modelPackage = TomoParametersPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION: {
				AlignmentConfiguration alignmentConfiguration = (AlignmentConfiguration)theEObject;
				T result = caseAlignmentConfiguration(alignmentConfiguration);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.DETECTOR_BIN: {
				DetectorBin detectorBin = (DetectorBin)theEObject;
				T result = caseDetectorBin(detectorBin);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.DETECTOR_PROPERTIES: {
				DetectorProperties detectorProperties = (DetectorProperties)theEObject;
				T result = caseDetectorProperties(detectorProperties);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.DETECTOR_ROI: {
				DetectorRoi detectorRoi = (DetectorRoi)theEObject;
				T result = caseDetectorRoi(detectorRoi);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.PARAMETERS: {
				Parameters parameters = (Parameters)theEObject;
				T result = caseParameters(parameters);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.SAMPLE_PARAMS: {
				SampleParams sampleParams = (SampleParams)theEObject;
				T result = caseSampleParams(sampleParams);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.SAMPLE_POSITION: {
				SamplePosition samplePosition = (SamplePosition)theEObject;
				T result = caseSamplePosition(samplePosition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TomoParametersPackage.TOMO_EXPERIMENT: {
				TomoExperiment tomoExperiment = (TomoExperiment)theEObject;
				T result = caseTomoExperiment(tomoExperiment);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tomo Experiment</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tomo Experiment</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseTomoExperiment(TomoExperiment object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Detector Bin</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Detector Bin</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseDetectorBin(DetectorBin object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Detector Properties</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Detector Properties</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseDetectorProperties(DetectorProperties object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Detector Roi</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Detector Roi</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseDetectorRoi(DetectorRoi object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Alignment Configuration</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Alignment Configuration</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseAlignmentConfiguration(AlignmentConfiguration object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Parameters</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Parameters</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseParameters(Parameters object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Sample Params</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Sample Params</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseSampleParams(SampleParams object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Sample Position</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Sample Position</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	@SuppressWarnings("unused")
	public T caseSamplePosition(SamplePosition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //TomoParametersSwitch
