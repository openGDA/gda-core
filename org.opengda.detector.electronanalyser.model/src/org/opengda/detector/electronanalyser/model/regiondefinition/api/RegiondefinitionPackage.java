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
	 * The feature id for the '<em><b>Filename</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__FILENAME = 0;

	/**
	 * The feature id for the '<em><b>Region</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__REGION = 1;

	/**
	 * The feature id for the '<em><b>Run Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__RUN_MODE = 2;

	/**
	 * The feature id for the '<em><b>Run Mode Index</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__RUN_MODE_INDEX = 3;

	/**
	 * The feature id for the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__NUM_ITERATIONS = 4;

	/**
	 * The feature id for the '<em><b>Repeat Until Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__REPEAT_UNTIL_STOPPED = 5;

	/**
	 * The feature id for the '<em><b>Confirm After Each Iteration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__CONFIRM_AFTER_EACH_ITERATION = 6;

	/**
	 * The feature id for the '<em><b>Spectrum</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__SPECTRUM = 7;

	/**
	 * The number of structural features of the '<em>Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE_FEATURE_COUNT = 8;

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
	 * The feature id for the '<em><b>Region Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__REGION_ID = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__STATUS = 1;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ENABLED = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__NAME = 3;

	/**
	 * The feature id for the '<em><b>Lens Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LENS_MODE = 4;

	/**
	 * The feature id for the '<em><b>Pass Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__PASS_ENERGY = 5;

	/**
	 * The feature id for the '<em><b>Run Mode</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__RUN_MODE = 6;

	/**
	 * The feature id for the '<em><b>Excitation Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__EXCITATION_ENERGY = 7;

	/**
	 * The feature id for the '<em><b>Acquisition Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ACQUISITION_MODE = 8;

	/**
	 * The feature id for the '<em><b>Energy Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ENERGY_MODE = 9;

	/**
	 * The feature id for the '<em><b>Fix Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__FIX_ENERGY = 10;

	/**
	 * The feature id for the '<em><b>Low Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LOW_ENERGY = 11;

	/**
	 * The feature id for the '<em><b>High Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__HIGH_ENERGY = 12;

	/**
	 * The feature id for the '<em><b>Energy Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ENERGY_STEP = 13;

	/**
	 * The feature id for the '<em><b>Step Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__STEP_TIME = 14;

	/**
	 * The feature id for the '<em><b>First XChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__FIRST_XCHANNEL = 15;

	/**
	 * The feature id for the '<em><b>Last XChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LAST_XCHANNEL = 16;

	/**
	 * The feature id for the '<em><b>First YChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__FIRST_YCHANNEL = 17;

	/**
	 * The feature id for the '<em><b>Last YChannel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LAST_YCHANNEL = 18;

	/**
	 * The feature id for the '<em><b>Slices</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__SLICES = 19;

	/**
	 * The feature id for the '<em><b>Detector Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__DETECTOR_MODE = 20;

	/**
	 * The feature id for the '<em><b>ADC Mask</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__ADC_MASK = 21;

	/**
	 * The feature id for the '<em><b>Discriminator Level</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__DISCRIMINATOR_LEVEL = 22;

	/**
	 * The feature id for the '<em><b>Total Steps</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__TOTAL_STEPS = 23;

	/**
	 * The feature id for the '<em><b>Total Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__TOTAL_TIME = 24;

	/**
	 * The number of structural features of the '<em>Region</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION_FEATURE_COUNT = 25;

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
	 * The feature id for the '<em><b>Run Mode Index</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__RUN_MODE_INDEX = 1;

	/**
	 * The feature id for the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__NUM_ITERATIONS = 2;

	/**
	 * The feature id for the '<em><b>Repeat Until Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__REPEAT_UNTIL_STOPPED = 3;

	/**
	 * The feature id for the '<em><b>Confirm After Each Interation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__CONFIRM_AFTER_EACH_INTERATION = 4;

	/**
	 * The feature id for the '<em><b>Num Iteration Option</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__NUM_ITERATION_OPTION = 5;

	/**
	 * The number of structural features of the '<em>Run Mode</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl <em>Spectrum</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSpectrum()
	 * @generated
	 */
	int SPECTRUM = 4;

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
	int PASS_ENERGY = 10;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS <em>STATUS</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSTATUS()
	 * @generated
	 */
	int STATUS = 11;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE <em>LENS MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getLENS_MODE()
	 * @generated
	 */
	int LENS_MODE = 5;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES <em>RUN MODES</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRUN_MODES()
	 * @generated
	 */
	int RUN_MODES = 6;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE <em>ACQUIAITION MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getACQUIAITION_MODE()
	 * @generated
	 */
	int ACQUIAITION_MODE = 7;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE <em>ENERGY MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getENERGY_MODE()
	 * @generated
	 */
	int ENERGY_MODE = 8;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE <em>DETECTOR MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDETECTOR_MODE()
	 * @generated
	 */
	int DETECTOR_MODE = 9;


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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunModeIndex <em>Run Mode Index</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Run Mode Index</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunModeIndex()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_RunModeIndex();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUntilStopped <em>Repeat Until Stopped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Repeat Until Stopped</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUntilStopped()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_RepeatUntilStopped();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isConfirmAfterEachIteration <em>Confirm After Each Iteration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Confirm After Each Iteration</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isConfirmAfterEachIteration()
	 * @see #getSequence()
	 * @generated
	 */
	EAttribute getSequence_ConfirmAfterEachIteration();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRegionId <em>Region Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Region Id</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRegionId()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_RegionId();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode <em>Lens Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lens Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_LensMode();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy <em>Excitation Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Excitation Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_ExcitationEnergy();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy <em>Fix Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fix Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_FixEnergy();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy <em>Low Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Low Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_LowEnergy();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy <em>High Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>High Energy</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_HighEnergy();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep <em>Energy Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Energy Step</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_EnergyStep();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime <em>Step Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Step Time</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_StepTime();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel <em>First XChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>First XChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_FirstXChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel <em>Last XChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last XChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_LastXChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel <em>First YChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>First YChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_FirstYChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel <em>Last YChannel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last YChannel</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_LastYChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices <em>Slices</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Slices</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Slices();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode <em>Detector Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Detector Mode</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_DetectorMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask <em>ADC Mask</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>ADC Mask</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_ADCMask();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel <em>Discriminator Level</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Discriminator Level</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_DiscriminatorLevel();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalSteps <em>Total Steps</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Steps</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalSteps()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_TotalSteps();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalTime <em>Total Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Time</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalTime()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_TotalTime();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStatus()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Enabled();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex <em>Run Mode Index</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Run Mode Index</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_RunModeIndex();

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
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped <em>Repeat Until Stopped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Repeat Until Stopped</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_RepeatUntilStopped();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isConfirmAfterEachInteration <em>Confirm After Each Interation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Confirm After Each Interation</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isConfirmAfterEachInteration()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_ConfirmAfterEachInteration();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isNumIterationOption <em>Num Iteration Option</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Num Iteration Option</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isNumIterationOption()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_NumIterationOption();

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
	 * Returns the meta object for enum '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS <em>STATUS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>STATUS</em>'.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS
	 * @generated
	 */
	EEnum getSTATUS();

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
		 * The meta object literal for the '<em><b>Run Mode Index</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__RUN_MODE_INDEX = eINSTANCE.getSequence_RunModeIndex();

		/**
		 * The meta object literal for the '<em><b>Num Iterations</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__NUM_ITERATIONS = eINSTANCE.getSequence_NumIterations();

		/**
		 * The meta object literal for the '<em><b>Repeat Until Stopped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__REPEAT_UNTIL_STOPPED = eINSTANCE.getSequence_RepeatUntilStopped();

		/**
		 * The meta object literal for the '<em><b>Confirm After Each Iteration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SEQUENCE__CONFIRM_AFTER_EACH_ITERATION = eINSTANCE.getSequence_ConfirmAfterEachIteration();

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
		 * The meta object literal for the '<em><b>Region Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__REGION_ID = eINSTANCE.getRegion_RegionId();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__NAME = eINSTANCE.getRegion_Name();

		/**
		 * The meta object literal for the '<em><b>Lens Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LENS_MODE = eINSTANCE.getRegion_LensMode();

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
		 * The meta object literal for the '<em><b>Excitation Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__EXCITATION_ENERGY = eINSTANCE.getRegion_ExcitationEnergy();

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
		 * The meta object literal for the '<em><b>Fix Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__FIX_ENERGY = eINSTANCE.getRegion_FixEnergy();

		/**
		 * The meta object literal for the '<em><b>Low Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LOW_ENERGY = eINSTANCE.getRegion_LowEnergy();

		/**
		 * The meta object literal for the '<em><b>High Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__HIGH_ENERGY = eINSTANCE.getRegion_HighEnergy();

		/**
		 * The meta object literal for the '<em><b>Energy Step</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__ENERGY_STEP = eINSTANCE.getRegion_EnergyStep();

		/**
		 * The meta object literal for the '<em><b>Step Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__STEP_TIME = eINSTANCE.getRegion_StepTime();

		/**
		 * The meta object literal for the '<em><b>First XChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__FIRST_XCHANNEL = eINSTANCE.getRegion_FirstXChannel();

		/**
		 * The meta object literal for the '<em><b>Last XChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LAST_XCHANNEL = eINSTANCE.getRegion_LastXChannel();

		/**
		 * The meta object literal for the '<em><b>First YChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__FIRST_YCHANNEL = eINSTANCE.getRegion_FirstYChannel();

		/**
		 * The meta object literal for the '<em><b>Last YChannel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LAST_YCHANNEL = eINSTANCE.getRegion_LastYChannel();

		/**
		 * The meta object literal for the '<em><b>Slices</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__SLICES = eINSTANCE.getRegion_Slices();

		/**
		 * The meta object literal for the '<em><b>Detector Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__DETECTOR_MODE = eINSTANCE.getRegion_DetectorMode();

		/**
		 * The meta object literal for the '<em><b>ADC Mask</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__ADC_MASK = eINSTANCE.getRegion_ADCMask();

		/**
		 * The meta object literal for the '<em><b>Discriminator Level</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__DISCRIMINATOR_LEVEL = eINSTANCE.getRegion_DiscriminatorLevel();

		/**
		 * The meta object literal for the '<em><b>Total Steps</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__TOTAL_STEPS = eINSTANCE.getRegion_TotalSteps();

		/**
		 * The meta object literal for the '<em><b>Total Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__TOTAL_TIME = eINSTANCE.getRegion_TotalTime();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__STATUS = eINSTANCE.getRegion_Status();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__ENABLED = eINSTANCE.getRegion_Enabled();

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
		 * The meta object literal for the '<em><b>Run Mode Index</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__RUN_MODE_INDEX = eINSTANCE.getRunMode_RunModeIndex();

		/**
		 * The meta object literal for the '<em><b>Num Iterations</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__NUM_ITERATIONS = eINSTANCE.getRunMode_NumIterations();

		/**
		 * The meta object literal for the '<em><b>Repeat Until Stopped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__REPEAT_UNTIL_STOPPED = eINSTANCE.getRunMode_RepeatUntilStopped();

		/**
		 * The meta object literal for the '<em><b>Confirm After Each Interation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__CONFIRM_AFTER_EACH_INTERATION = eINSTANCE.getRunMode_ConfirmAfterEachInteration();

		/**
		 * The meta object literal for the '<em><b>Num Iteration Option</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__NUM_ITERATION_OPTION = eINSTANCE.getRunMode_NumIterationOption();

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
		 * The meta object literal for the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS <em>STATUS</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS
		 * @see org.opengda.detector.electronanalyser.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSTATUS()
		 * @generated
		 */
		EEnum STATUS = eINSTANCE.getSTATUS();

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
