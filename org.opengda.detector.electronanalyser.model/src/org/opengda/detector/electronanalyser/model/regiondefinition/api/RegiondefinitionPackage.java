/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory
 * @model kind="package"
 * @generated
 */
public interface RegiondefinitionPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "regiondefinition";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.opengda.org/regiondefinition";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RegiondefinitionPackage eINSTANCE = org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.DocumentRootImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDocumentRoot()
	 * @generated
	 */
	int DOCUMENT_ROOT = 0;

	/**
	 * The feature id for the '<em><b>Sequence</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__SEQUENCE = 0;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl <em>Sequence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSequence()
	 * @generated
	 */
	int SEQUENCE = 1;

	/**
	 * The feature id for the '<em><b>Region</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__REGION = 0;

	/**
	 * The feature id for the '<em><b>Run Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__RUN_MODE = 1;

	/**
	 * The feature id for the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__NUM_ITERATIONS = 2;

	/**
	 * The feature id for the '<em><b>Repeat Unitil Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__REPEAT_UNITIL_STOPPED = 3;

	/**
	 * The feature id for the '<em><b>Spectrum</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__SPECTRUM = 4;

	/**
	 * The feature id for the '<em><b>Filename</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__FILENAME = 5;

	/**
	 * The number of structural features of the '<em>Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl <em>Region</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRegion()
	 * @generated
	 */
	int REGION = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__NAME = 0;

	/**
	 * The feature id for the '<em><b>Lensmode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LENSMODE = 1;

	/**
	 * The feature id for the '<em><b>Run Mode</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__RUN_MODE = 2;

	/**
	 * The feature id for the '<em><b>Acquisition Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ACQUISITION_MODE = 3;

	/**
	 * The feature id for the '<em><b>Energy Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ENERGY_MODE = 4;

	/**
	 * The feature id for the '<em><b>Energy</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ENERGY = 5;

	/**
	 * The feature id for the '<em><b>Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__STEP = 6;

	/**
	 * The feature id for the '<em><b>Detector</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__DETECTOR = 7;

	/**
	 * The feature id for the '<em><b>Pass Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__PASS_ENERGY = 8;

	/**
	 * The number of structural features of the '<em>Region</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION_FEATURE_COUNT = 9;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl <em>Run Mode</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRunMode()
	 * @generated
	 */
	int RUN_MODE = 3;

	/**
	 * The feature id for the '<em><b>Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__MODE = 0;

	/**
	 * The feature id for the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__NUM_ITERATIONS = 1;

	/**
	 * The feature id for the '<em><b>Repeat Unitil Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__REPEAT_UNITIL_STOPPED = 2;

	/**
	 * The number of structural features of the '<em>Run Mode</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.EnergyImpl <em>Energy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.EnergyImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getEnergy()
	 * @generated
	 */
	int ENERGY = 4;

	/**
	 * The feature id for the '<em><b>Low</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENERGY__LOW = 0;

	/**
	 * The feature id for the '<em><b>High</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENERGY__HIGH = 1;

	/**
	 * The feature id for the '<em><b>Center</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENERGY__CENTER = 2;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENERGY__WIDTH = 3;

	/**
	 * The number of structural features of the '<em>Energy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENERGY_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl <em>Step</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getStep()
	 * @generated
	 */
	int STEP = 5;

	/**
	 * The feature id for the '<em><b>Frames</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP__FRAMES = 0;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP__TIME = 1;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP__SIZE = 2;

	/**
	 * The feature id for the '<em><b>Total Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP__TOTAL_TIME = 3;

	/**
	 * The feature id for the '<em><b>Total Steps</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP__TOTAL_STEPS = 4;

	/**
	 * The number of structural features of the '<em>Step</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STEP_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl <em>Detector</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDetector()
	 * @generated
	 */
	int DETECTOR = 6;

	/**
	 * The feature id for the '<em><b>First XChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__FIRST_XCHANNEL = 0;

	/**
	 * The feature id for the '<em><b>Last XChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__LAST_XCHANNEL = 1;

	/**
	 * The feature id for the '<em><b>First YChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__FIRST_YCHANNEL = 2;

	/**
	 * The feature id for the '<em><b>Last YChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__LAST_YCHANNEL = 3;

	/**
	 * The feature id for the '<em><b>Slices</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__SLICES = 4;

	/**
	 * The feature id for the '<em><b>Detector Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR__DETECTOR_MODE = 5;

	/**
	 * The number of structural features of the '<em>Detector</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl <em>Spectrum</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSpectrum()
	 * @generated
	 */
	int SPECTRUM = 7;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__LOCATION = 0;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__USER = 1;

	/**
	 * The feature id for the '<em><b>Sample Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__SAMPLE_NAME = 2;

	/**
	 * The feature id for the '<em><b>Filename Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__FILENAME_PREFIX = 3;

	/**
	 * The feature id for the '<em><b>Base Directory</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__BASE_DIRECTORY = 4;

	/**
	 * The feature id for the '<em><b>Filename Formet</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__FILENAME_FORMET = 5;

	/**
	 * The feature id for the '<em><b>File Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__FILE_EXTENSION = 6;

	/**
	 * The feature id for the '<em><b>Number Of Comments</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__NUMBER_OF_COMMENTS = 7;

	/**
	 * The feature id for the '<em><b>Comments</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM__COMMENTS = 8;

	/**
	 * The number of structural features of the '<em>Spectrum</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SPECTRUM_FEATURE_COUNT = 9;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY <em>PASS ENERGY</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getPASS_ENERGY()
	 * @generated
	 */
	int PASS_ENERGY = 13;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE <em>LENS MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getLENS_MODE()
	 * @generated
	 */
	int LENS_MODE = 8;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES <em>RUN MODES</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRUN_MODES()
	 * @generated
	 */
	int RUN_MODES = 9;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE <em>ACQUIAITION MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getACQUIAITION_MODE()
	 * @generated
	 */
	int ACQUIAITION_MODE = 10;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE <em>ENERGY MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getENERGY_MODE()
	 * @generated
	 */
	int ENERGY_MODE = 11;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE <em>DETECTOR MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDETECTOR_MODE()
	 * @generated
	 */
	int DETECTOR_MODE = 12;


	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence <em>Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Sequence</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot#getSequence()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_Sequence();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence <em>Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sequence</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence
	 * @generated
	 */
	EClass getSequence();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRegion <em>Region</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Region</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRegion()
	 * @see #getSequence()
	 * @generated
	 */
	EReference getSequence_Region();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode <em>Run Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Run Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_RunMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations <em>Num Iterations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Num Iterations</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_NumIterations();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Repeat Unitil Stopped</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_RepeatUnitilStopped();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum <em>Spectrum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Spectrum</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum()
	 * @see #getSequence()
	 * @generated
	 */
	EReference getSequence_Spectrum();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename <em>Filename</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Filename</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_Filename();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region <em>Region</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Region</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region
	 * @generated
	 */
	EClass getRegion();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode <em>Lensmode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lensmode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Lensmode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pass Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_PassEnergy();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Run Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode()
	 * @see #getRegion()
	 * @generated
	 */
	EReference getRegion_RunMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Acquisition Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_AcquisitionMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Energy Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_EnergyMode();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergy <em>Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EReference getRegion_Energy();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStep <em>Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Step</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStep()
	 * @see #getRegion()
	 * @generated
	 */
	EReference getRegion_Step();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetector <em>Detector</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Detector</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetector()
	 * @see #getRegion()
	 * @generated
	 */
	EReference getRegion_Detector();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode <em>Run Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Run Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode
	 * @generated
	 */
	EClass getRunMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode <em>Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_Mode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations <em>Num Iterations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Num Iterations</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_NumIterations();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Repeat Unitil Stopped</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUnitilStopped()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_RepeatUnitilStopped();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy <em>Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy
	 * @generated
	 */
	EClass getEnergy();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getLow <em>Low</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Low</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getLow()
	 * @see #getEnergy()
	 * @generated
	 */
	EAttribute getEnergy_Low();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getHigh <em>High</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>High</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getHigh()
	 * @see #getEnergy()
	 * @generated
	 */
	EAttribute getEnergy_High();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getCenter <em>Center</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Center</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getCenter()
	 * @see #getEnergy()
	 * @generated
	 */
	EAttribute getEnergy_Center();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy#getWidth()
	 * @see #getEnergy()
	 * @generated
	 */
	EAttribute getEnergy_Width();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step <em>Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Step</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step
	 * @generated
	 */
	EClass getStep();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getFrames <em>Frames</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Frames</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getFrames()
	 * @see #getStep()
	 * @generated
	 */
	EAttribute getStep_Frames();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTime <em>Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTime()
	 * @see #getStep()
	 * @generated
	 */
	EAttribute getStep_Time();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getSize <em>Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Size</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getSize()
	 * @see #getStep()
	 * @generated
	 */
	EAttribute getStep_Size();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalTime <em>Total Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Time</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalTime()
	 * @see #getStep()
	 * @generated
	 */
	EAttribute getStep_TotalTime();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalSteps <em>Total Steps</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Steps</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalSteps()
	 * @see #getStep()
	 * @generated
	 */
	EAttribute getStep_TotalSteps();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector <em>Detector</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Detector</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector
	 * @generated
	 */
	EClass getDetector();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getFirstXChannel <em>First XChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>First XChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getFirstXChannel()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_FirstXChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getLastXChannel <em>Last XChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last XChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getLastXChannel()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_LastXChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getFirstYChannel <em>First YChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>First YChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getFirstYChannel()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_FirstYChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getLastYChannel <em>Last YChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last YChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getLastYChannel()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_LastYChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getSlices <em>Slices</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Slices</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getSlices()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_Slices();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getDetectorMode <em>Detector Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Detector Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector#getDetectorMode()
	 * @see #getDetector()
	 * @generated
	 */
	EAttribute getDetector_DetectorMode();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum <em>Spectrum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Spectrum</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum
	 * @generated
	 */
	EClass getSpectrum();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Location</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_Location();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser <em>User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_User();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName <em>Sample Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample Name</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_SampleName();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix <em>Filename Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Filename Prefix</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_FilenamePrefix();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory <em>Base Directory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Base Directory</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_BaseDirectory();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormet <em>Filename Formet</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Filename Formet</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormet()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_FilenameFormet();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension <em>File Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Extension</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_FileExtension();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments <em>Number Of Comments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number Of Comments</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_NumberOfComments();

	/**
	 * Returns the meta object for the attribute list '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getComments <em>Comments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Comments</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getComments()
	 * @see #getSpectrum()
	 * @generated
	 */
	EAttribute getSpectrum_Comments();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE <em>LENS MODE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>LENS MODE</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
	 * @generated
	 */
	EEnum getLENS_MODE();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES <em>RUN MODES</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>RUN MODES</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @generated
	 */
	EEnum getRUN_MODES();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE <em>ACQUIAITION MODE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>ACQUIAITION MODE</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
	 * @generated
	 */
	EEnum getACQUIAITION_MODE();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE <em>ENERGY MODE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>ENERGY MODE</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @generated
	 */
	EEnum getENERGY_MODE();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE <em>DETECTOR MODE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>DETECTOR MODE</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
	 * @generated
	 */
	EEnum getDETECTOR_MODE();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY <em>PASS ENERGY</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>PASS ENERGY</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY
	 * @generated
	 */
	EEnum getPASS_ENERGY();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RegiondefinitionFactory getRegiondefinitionFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.DocumentRootImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDocumentRoot()
		 * @generated
		 */
		EClass DOCUMENT_ROOT = eINSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Sequence</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__SEQUENCE = eINSTANCE.getDocumentRoot_Sequence();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl <em>Sequence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.SequenceImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSequence()
		 * @generated
		 */
		EClass SEQUENCE = eINSTANCE.getSequence();

		/**
		 * The meta object literal for the '<em><b>Region</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SEQUENCE__REGION = eINSTANCE.getSequence_Region();

		/**
		 * The meta object literal for the '<em><b>Run Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__RUN_MODE = eINSTANCE.getSequence_RunMode();

		/**
		 * The meta object literal for the '<em><b>Num Iterations</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__NUM_ITERATIONS = eINSTANCE.getSequence_NumIterations();

		/**
		 * The meta object literal for the '<em><b>Repeat Unitil Stopped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__REPEAT_UNITIL_STOPPED = eINSTANCE.getSequence_RepeatUnitilStopped();

		/**
		 * The meta object literal for the '<em><b>Spectrum</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SEQUENCE__SPECTRUM = eINSTANCE.getSequence_Spectrum();

		/**
		 * The meta object literal for the '<em><b>Filename</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__FILENAME = eINSTANCE.getSequence_Filename();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl <em>Region</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegionImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRegion()
		 * @generated
		 */
		EClass REGION = eINSTANCE.getRegion();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__NAME = eINSTANCE.getRegion_Name();

		/**
		 * The meta object literal for the '<em><b>Lensmode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LENSMODE = eINSTANCE.getRegion_Lensmode();

		/**
		 * The meta object literal for the '<em><b>Pass Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__PASS_ENERGY = eINSTANCE.getRegion_PassEnergy();

		/**
		 * The meta object literal for the '<em><b>Run Mode</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGION__RUN_MODE = eINSTANCE.getRegion_RunMode();

		/**
		 * The meta object literal for the '<em><b>Acquisition Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__ACQUISITION_MODE = eINSTANCE.getRegion_AcquisitionMode();

		/**
		 * The meta object literal for the '<em><b>Energy Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__ENERGY_MODE = eINSTANCE.getRegion_EnergyMode();

		/**
		 * The meta object literal for the '<em><b>Energy</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGION__ENERGY = eINSTANCE.getRegion_Energy();

		/**
		 * The meta object literal for the '<em><b>Step</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGION__STEP = eINSTANCE.getRegion_Step();

		/**
		 * The meta object literal for the '<em><b>Detector</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGION__DETECTOR = eINSTANCE.getRegion_Detector();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl <em>Run Mode</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RunModeImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRunMode()
		 * @generated
		 */
		EClass RUN_MODE = eINSTANCE.getRunMode();

		/**
		 * The meta object literal for the '<em><b>Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__MODE = eINSTANCE.getRunMode_Mode();

		/**
		 * The meta object literal for the '<em><b>Num Iterations</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__NUM_ITERATIONS = eINSTANCE.getRunMode_NumIterations();

		/**
		 * The meta object literal for the '<em><b>Repeat Unitil Stopped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__REPEAT_UNITIL_STOPPED = eINSTANCE.getRunMode_RepeatUnitilStopped();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.EnergyImpl <em>Energy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.EnergyImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getEnergy()
		 * @generated
		 */
		EClass ENERGY = eINSTANCE.getEnergy();

		/**
		 * The meta object literal for the '<em><b>Low</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENERGY__LOW = eINSTANCE.getEnergy_Low();

		/**
		 * The meta object literal for the '<em><b>High</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENERGY__HIGH = eINSTANCE.getEnergy_High();

		/**
		 * The meta object literal for the '<em><b>Center</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENERGY__CENTER = eINSTANCE.getEnergy_Center();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENERGY__WIDTH = eINSTANCE.getEnergy_Width();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl <em>Step</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.StepImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getStep()
		 * @generated
		 */
		EClass STEP = eINSTANCE.getStep();

		/**
		 * The meta object literal for the '<em><b>Frames</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STEP__FRAMES = eINSTANCE.getStep_Frames();

		/**
		 * The meta object literal for the '<em><b>Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STEP__TIME = eINSTANCE.getStep_Time();

		/**
		 * The meta object literal for the '<em><b>Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STEP__SIZE = eINSTANCE.getStep_Size();

		/**
		 * The meta object literal for the '<em><b>Total Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STEP__TOTAL_TIME = eINSTANCE.getStep_TotalTime();

		/**
		 * The meta object literal for the '<em><b>Total Steps</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STEP__TOTAL_STEPS = eINSTANCE.getStep_TotalSteps();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl <em>Detector</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.DetectorImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDetector()
		 * @generated
		 */
		EClass DETECTOR = eINSTANCE.getDetector();

		/**
		 * The meta object literal for the '<em><b>First XChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__FIRST_XCHANNEL = eINSTANCE.getDetector_FirstXChannel();

		/**
		 * The meta object literal for the '<em><b>Last XChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__LAST_XCHANNEL = eINSTANCE.getDetector_LastXChannel();

		/**
		 * The meta object literal for the '<em><b>First YChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__FIRST_YCHANNEL = eINSTANCE.getDetector_FirstYChannel();

		/**
		 * The meta object literal for the '<em><b>Last YChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__LAST_YCHANNEL = eINSTANCE.getDetector_LastYChannel();

		/**
		 * The meta object literal for the '<em><b>Slices</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__SLICES = eINSTANCE.getDetector_Slices();

		/**
		 * The meta object literal for the '<em><b>Detector Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR__DETECTOR_MODE = eINSTANCE.getDetector_DetectorMode();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl <em>Spectrum</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSpectrum()
		 * @generated
		 */
		EClass SPECTRUM = eINSTANCE.getSpectrum();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__LOCATION = eINSTANCE.getSpectrum_Location();

		/**
		 * The meta object literal for the '<em><b>User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__USER = eINSTANCE.getSpectrum_User();

		/**
		 * The meta object literal for the '<em><b>Sample Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__SAMPLE_NAME = eINSTANCE.getSpectrum_SampleName();

		/**
		 * The meta object literal for the '<em><b>Filename Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__FILENAME_PREFIX = eINSTANCE.getSpectrum_FilenamePrefix();

		/**
		 * The meta object literal for the '<em><b>Base Directory</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__BASE_DIRECTORY = eINSTANCE.getSpectrum_BaseDirectory();

		/**
		 * The meta object literal for the '<em><b>Filename Formet</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__FILENAME_FORMET = eINSTANCE.getSpectrum_FilenameFormet();

		/**
		 * The meta object literal for the '<em><b>File Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__FILE_EXTENSION = eINSTANCE.getSpectrum_FileExtension();

		/**
		 * The meta object literal for the '<em><b>Number Of Comments</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__NUMBER_OF_COMMENTS = eINSTANCE.getSpectrum_NumberOfComments();

		/**
		 * The meta object literal for the '<em><b>Comments</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SPECTRUM__COMMENTS = eINSTANCE.getSpectrum_Comments();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY <em>PASS ENERGY</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getPASS_ENERGY()
		 * @generated
		 */
		EEnum PASS_ENERGY = eINSTANCE.getPASS_ENERGY();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE <em>LENS MODE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getLENS_MODE()
		 * @generated
		 */
		EEnum LENS_MODE = eINSTANCE.getLENS_MODE();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES <em>RUN MODES</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRUN_MODES()
		 * @generated
		 */
		EEnum RUN_MODES = eINSTANCE.getRUN_MODES();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE <em>ACQUIAITION MODE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getACQUIAITION_MODE()
		 * @generated
		 */
		EEnum ACQUIAITION_MODE = eINSTANCE.getACQUIAITION_MODE();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE <em>ENERGY MODE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getENERGY_MODE()
		 * @generated
		 */
		EEnum ENERGY_MODE = eINSTANCE.getENERGY_MODE();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE <em>DETECTOR MODE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDETECTOR_MODE()
		 * @generated
		 */
		EEnum DETECTOR_MODE = eINSTANCE.getDETECTOR_MODE();

	}

} //RegiondefinitionPackage
