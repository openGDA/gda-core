/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RegiondefinitionFactoryImpl extends EFactoryImpl implements RegiondefinitionFactory {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(RegiondefinitionFactoryImpl.class);

	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RegiondefinitionFactory init() {
		try {
			RegiondefinitionFactory theRegiondefinitionFactory = (RegiondefinitionFactory)EPackage.Registry.INSTANCE.getEFactory(RegiondefinitionPackage.eNS_URI);
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
			case RegiondefinitionPackage.SPECTRUM: return createSpectrum();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
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
			case RegiondefinitionPackage.RUN_MODES:
				return createRUN_MODESFromString(eDataType, initialValue);
			case RegiondefinitionPackage.ACQUISITION_MODE:
				return createACQUISITION_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.ENERGY_MODE:
				return createENERGY_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.DETECTOR_MODE:
				return createDETECTOR_MODEFromString(eDataType, initialValue);
			case RegiondefinitionPackage.STATUS:
				return createSTATUSFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
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
			case RegiondefinitionPackage.RUN_MODES:
				return convertRUN_MODESToString(eDataType, instanceValue);
			case RegiondefinitionPackage.ACQUISITION_MODE:
				return convertACQUISITION_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.ENERGY_MODE:
				return convertENERGY_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.DETECTOR_MODE:
				return convertDETECTOR_MODEToString(eDataType, instanceValue);
			case RegiondefinitionPackage.STATUS:
				return convertSTATUSToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DocumentRoot createDocumentRoot() {
		DocumentRootImpl documentRoot = new DocumentRootImpl();
		return documentRoot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Sequence createSequence() {
		SequenceImpl sequence = new SequenceImpl();
		return sequence;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Region createRegion() {
		RegionImpl region = new RegionImpl();
		return region;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RunMode createRunMode() {
		RunModeImpl runMode = new RunModeImpl();
		return runMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Spectrum createSpectrum() {
		SpectrumImpl spectrum = new SpectrumImpl();
		return spectrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RUN_MODES createRUN_MODESFromString(EDataType eDataType, String initialValue) {
		RUN_MODES result = RUN_MODES.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	public ACQUISITION_MODE createACQUISITION_MODEFromString(EDataType eDataType, String initialValue) {
		ACQUISITION_MODE result = ACQUISITION_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertACQUISITION_MODEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ENERGY_MODE createENERGY_MODEFromString(EDataType eDataType, String initialValue) {
		ENERGY_MODE result = ENERGY_MODE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	public STATUS createSTATUSFromString(EDataType eDataType, String initialValue) {
		STATUS result = STATUS.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSTATUSToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RegiondefinitionPackage getRegiondefinitionPackage() {
		return (RegiondefinitionPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated(since="GDA 8.30")
	public static RegiondefinitionPackage getPackage() {
		logger.deprecatedMethod("getPackage()");
		return RegiondefinitionPackage.eINSTANCE;
	}

} //RegiondefinitionFactoryImpl
