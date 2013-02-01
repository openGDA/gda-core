/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RegiondefinitionFactoryImpl extends EFactoryImpl implements RegiondefinitionFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RegiondefinitionFactory init() {
		try {
			RegiondefinitionFactory theRegiondefinitionFactory = (RegiondefinitionFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.opengda.org/regiondefinition"); 
			if (theRegiondefinitionFactory != null) {
				return theRegiondefinitionFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new RegiondefinitionFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegiondefinitionFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case RegiondefinitionPackage.DOCUMENT_ROOT: return createDocumentRoot();
			case RegiondefinitionPackage.SEQUENCE: return createSequence();
			case RegiondefinitionPackage.REGION: return createRegion();
			case RegiondefinitionPackage.RUN_MODE: return createRunMode();
			case RegiondefinitionPackage.ENERGY: return createEnergy();
			case RegiondefinitionPackage.STEP: return createStep();
			case RegiondefinitionPackage.DETECTOR: return createDetector();
			case RegiondefinitionPackage.SPECTRUM: return createSpectrum();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case RegiondefinitionPackage.LENS_MODE:
				return createLENS_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.RUN_MODES:
				return createRUN_MODESFromString(eDataType, initialValue);
			case RegiondefinitionPackage.ACQUIAITION_MODE:
				return createACQUIAITION_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.ENERGY_MODE:
				return createENERGY_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.DETECTOR_MODE:
				return createDETECTOR_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.PASS_ENERGY:
				return createPASS_ENERGYFromString(eDataType, initialValue);
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
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case RegiondefinitionPackage.LENS_MODE:
				return convertLENS_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.RUN_MODES:
				return convertRUN_MODESToString(eDataType, instanceValue);
			case RegiondefinitionPackage.ACQUIAITION_MODE:
				return convertACQUIAITION_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.ENERGY_MODE:
				return convertENERGY_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.DETECTOR_MODE:
				return convertDETECTOR_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.PASS_ENERGY:
				return convertPASS_ENERGYToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DocumentRoot createDocumentRoot() {
		DocumentRootImpl documentRoot = new DocumentRootImpl();
		return documentRoot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Sequence createSequence() {
		SequenceImpl sequence = new SequenceImpl();
		return sequence;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Region createRegion() {
		RegionImpl region = new RegionImpl();
		return region;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RunMode createRunMode() {
		RunModeImpl runMode = new RunModeImpl();
		return runMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Energy createEnergy() {
		EnergyImpl energy = new EnergyImpl();
		return energy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Step createStep() {
		StepImpl step = new StepImpl();
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Detector createDetector() {
		DetectorImpl detector = new DetectorImpl();
		return detector;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Spectrum createSpectrum() {
		SpectrumImpl spectrum = new SpectrumImpl();
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LENS_MODE createLENS_MODEFromString(EDataType eDataType, String initialValue) {
		LENS_MODE result = LENS_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertLENS_MODEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RUN_MODES createRUN_MODESFromString(EDataType eDataType, String initialValue) {
		RUN_MODES result = RUN_MODES.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRUN_MODESToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ACQUIAITION_MODE createACQUIAITION_MODEFromString(EDataType eDataType, String initialValue) {
		ACQUIAITION_MODE result = ACQUIAITION_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertACQUIAITION_MODEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ENERGY_MODE createENERGY_MODEFromString(EDataType eDataType, String initialValue) {
		ENERGY_MODE result = ENERGY_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertENERGY_MODEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DETECTOR_MODE createDETECTOR_MODEFromString(EDataType eDataType, String initialValue) {
		DETECTOR_MODE result = DETECTOR_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertDETECTOR_MODEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PASS_ENERGY createPASS_ENERGYFromString(EDataType eDataType, String initialValue) {
		PASS_ENERGY result = PASS_ENERGY.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPASS_ENERGYToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegiondefinitionPackage getRegiondefinitionPackage() {
		return (RegiondefinitionPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static RegiondefinitionPackage getPackage() {
		return RegiondefinitionPackage.eINSTANCE;
	}

} //RegiondefinitionFactoryImpl
