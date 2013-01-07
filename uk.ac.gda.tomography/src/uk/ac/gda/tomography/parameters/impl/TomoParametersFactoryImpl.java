/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import uk.ac.gda.tomography.parameters.*;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.MotorPosition;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.ScanMode;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * @generated
 */
public class TomoParametersFactoryImpl extends EFactoryImpl implements TomoParametersFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public static TomoParametersFactory init() {
		try {
			TomoParametersFactory theTomoParametersFactory = (TomoParametersFactory)EPackage.Registry.INSTANCE.getEFactory("http:///uk/ac/gda/client/tomo/tomoparameters.ecore"); 
			if (theTomoParametersFactory != null) {
				return theTomoParametersFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TomoParametersFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TomoParametersFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION: return createAlignmentConfiguration();
			case TomoParametersPackage.DETECTOR_BIN: return createDetectorBin();
			case TomoParametersPackage.DETECTOR_PROPERTIES: return createDetectorProperties();
			case TomoParametersPackage.DETECTOR_ROI: return createDetectorRoi();
			case TomoParametersPackage.MODULE: return createModule();
			case TomoParametersPackage.MOTOR_POSITION: return createMotorPosition();
			case TomoParametersPackage.PARAMETERS: return createParameters();
			case TomoParametersPackage.STITCH_PARAMETERS: return createStitchParameters();
			case TomoParametersPackage.TOMO_EXPERIMENT: return createTomoExperiment();
			case TomoParametersPackage.SCAN_COLLECTED: return createScanCollected();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case TomoParametersPackage.SCAN_MODE:
				return createScanModeFromString(eDataType, initialValue);
			case TomoParametersPackage.RESOLUTION:
				return createResolutionFromString(eDataType, initialValue);
			case TomoParametersPackage.SAMPLE_WEIGHT:
				return createSampleWeightFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case TomoParametersPackage.SCAN_MODE:
				return convertScanModeToString(eDataType, instanceValue);
			case TomoParametersPackage.RESOLUTION:
				return convertResolutionToString(eDataType, instanceValue);
			case TomoParametersPackage.SAMPLE_WEIGHT:
				return convertSampleWeightToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TomoExperiment createTomoExperiment() {
		TomoExperimentImpl tomoExperiment = new TomoExperimentImpl();
		return tomoExperiment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ScanCollected createScanCollected() {
		ScanCollectedImpl scanCollected = new ScanCollectedImpl();
		return scanCollected;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Module createModule() {
		ModuleImpl module = new ModuleImpl();
		return module;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MotorPosition createMotorPosition() {
		MotorPositionImpl motorPosition = new MotorPositionImpl();
		return motorPosition;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorBin createDetectorBin() {
		DetectorBinImpl detectorBin = new DetectorBinImpl();
		return detectorBin;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorProperties createDetectorProperties() {
		DetectorPropertiesImpl detectorProperties = new DetectorPropertiesImpl();
		return detectorProperties;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorRoi createDetectorRoi() {
		DetectorRoiImpl detectorRoi = new DetectorRoiImpl();
		return detectorRoi;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AlignmentConfiguration createAlignmentConfiguration() {
		AlignmentConfigurationImpl alignmentConfiguration = new AlignmentConfigurationImpl();
		return alignmentConfiguration;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Parameters createParameters() {
		ParametersImpl parameters = new ParametersImpl();
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public StitchParameters createStitchParameters() {
		StitchParametersImpl stitchParameters = new StitchParametersImpl();
		return stitchParameters;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ScanMode createScanModeFromString(EDataType eDataType, String initialValue) {
		ScanMode result = ScanMode.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String convertScanModeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Resolution createResolutionFromString(EDataType eDataType, String initialValue) {
		Resolution result = Resolution.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String convertResolutionToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SampleWeight createSampleWeightFromString(EDataType eDataType, String initialValue) {
		SampleWeight result = SampleWeight.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unused")
	public String convertSampleWeightToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TomoParametersPackage getTomoParametersPackage() {
		return (TomoParametersPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static TomoParametersPackage getPackage() {
		return TomoParametersPackage.eINSTANCE;
	}

} // TomoParametersFactoryImpl
