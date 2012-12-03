/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see uk.ac.gda.tomography.parameters.TomoParametersFactory
 * @model kind="package"
 * @generated
 */
public interface TomoParametersPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "parameters";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///uk/ac/gda/client/tomo/tomoparameters.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	TomoParametersPackage eINSTANCE = uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl <em>Tomo Experiment</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getTomoExperiment()
	 * @generated
	 */
	int TOMO_EXPERIMENT = 8;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl <em>Detector Bin</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorBinImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorBin()
	 * @generated
	 */
	int DETECTOR_BIN = 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl <em>Detector Properties</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorProperties()
	 * @generated
	 */
	int DETECTOR_PROPERTIES = 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl <em>Detector Roi</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorRoi()
	 * @generated
	 */
	int DETECTOR_ROI = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl <em>Alignment Configuration</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getAlignmentConfiguration()
	 * @generated
	 */
	int ALIGNMENT_CONFIGURATION = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__ID = 0;

	/**
	 * The feature id for the '<em><b>Energy</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__ENERGY = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Detector Properties</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES = 3;

	/**
	 * The feature id for the '<em><b>Scan Mode</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SCAN_MODE = 4;

	/**
	 * The feature id for the '<em><b>Sample Exposure Time</b></em>' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME = 5;

	/**
	 * The feature id for the '<em><b>Flat Exposure Time</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME = 6;

	/**
	 * The feature id for the '<em><b>Created User Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__CREATED_USER_ID = 7;

	/**
	 * The feature id for the '<em><b>Created Date Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME = 8;

	/**
	 * The feature id for the '<em><b>Sample Weight</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT = 9;

	/**
	 * The feature id for the '<em><b>Proposal Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__PROPOSAL_ID = 10;

	/**
	 * The feature id for the '<em><b>Stitch Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS = 11;

	/**
	 * The feature id for the '<em><b>Selected To Run</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN = 12;

	/**
	 * The feature id for the '<em><b>Motor Positions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS = 13;

	/**
	 * The feature id for the '<em><b>In Beam Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION = 14;

	/**
	 * The feature id for the '<em><b>Out Of Beam Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION = 15;

	/**
	 * The feature id for the '<em><b>Tomo Rotation Axis</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS = 16;

	/**
	 * The number of structural features of the '<em>Alignment Configuration</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION_FEATURE_COUNT = 17;

	/**
	 * The feature id for the '<em><b>Bin X</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_BIN__BIN_X = 0;

	/**
	 * The feature id for the '<em><b>Bin Y</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_BIN__BIN_Y = 1;

	/**
	 * The number of structural features of the '<em>Detector Bin</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_BIN_FEATURE_COUNT = 2;

	/**
	 * The feature id for the '<em><b>Desired3 DResolution</b></em>' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION = 0;

	/**
	 * The feature id for the '<em><b>Number Of Framer Per Projection</b></em>' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION = 1;

	/**
	 * The feature id for the '<em><b>Acquisition Time Divider</b></em>' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER = 2;

	/**
	 * The feature id for the '<em><b>Detector Roi</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DETECTOR_ROI = 3;

	/**
	 * The feature id for the '<em><b>Detector Bin</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DETECTOR_BIN = 4;

	/**
	 * The feature id for the '<em><b>Module Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__MODULE_PARAMETERS = 5;

	/**
	 * The number of structural features of the '<em>Detector Properties</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES_FEATURE_COUNT = 6;

	/**
	 * The feature id for the '<em><b>Min X</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MIN_X = 0;

	/**
	 * The feature id for the '<em><b>Max X</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MAX_X = 1;

	/**
	 * The feature id for the '<em><b>Min Y</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MIN_Y = 2;

	/**
	 * The feature id for the '<em><b>Max Y</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MAX_Y = 3;

	/**
	 * The number of structural features of the '<em>Detector Roi</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.ParametersImpl <em>Parameters</em>}' class.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.ParametersImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getParameters()
	 * @generated
	 */
	int PARAMETERS = 6;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl <em>Module</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.ModuleImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getModule()
	 * @generated
	 */
	int MODULE = 4;

	/**
	 * The feature id for the '<em><b>Module Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODULE__MODULE_NUMBER = 0;

	/**
	 * The feature id for the '<em><b>Camera Magnification</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODULE__CAMERA_MAGNIFICATION = 1;

	/**
	 * The number of structural features of the '<em>Module</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODULE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.MotorPositionImpl <em>Motor Position</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.MotorPositionImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getMotorPosition()
	 * @generated
	 */
	int MOTOR_POSITION = 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOTOR_POSITION__NAME = 0;

	/**
	 * The feature id for the '<em><b>Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOTOR_POSITION__POSITION = 1;

	/**
	 * The number of structural features of the '<em>Motor Position</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOTOR_POSITION_FEATURE_COUNT = 2;

	/**
	 * The feature id for the '<em><b>Configuration Set</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__CONFIGURATION_SET = 0;

	/**
	 * The number of structural features of the '<em>Parameters</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int PARAMETERS_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.StitchParametersImpl <em>Stitch Parameters</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.impl.StitchParametersImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getStitchParameters()
	 * @generated
	 */
	int STITCH_PARAMETERS = 7;

	/**
	 * The feature id for the '<em><b>Stitching Theta Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STITCH_PARAMETERS__STITCHING_THETA_ANGLE = 0;

	/**
	 * The feature id for the '<em><b>Image At Theta</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STITCH_PARAMETERS__IMAGE_AT_THETA = 1;

	/**
	 * The feature id for the '<em><b>Image At Theta Plus90</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90 = 2;

	/**
	 * The number of structural features of the '<em>Stitch Parameters</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STITCH_PARAMETERS_FEATURE_COUNT = 3;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__PARAMETERS = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Total Time To Run</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__TOTAL_TIME_TO_RUN = 2;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__VERSION = 3;

	/**
	 * The number of structural features of the '<em>Tomo Experiment</em>' class.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.ScanMode <em>Scan Mode</em>}' enum. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.ScanMode
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getScanMode()
	 * @generated
	 */
	int SCAN_MODE = 9;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.Resolution <em>Resolution</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.Resolution
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getResolution()
	 * @generated
	 */
	int RESOLUTION = 10;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.SampleWeight <em>Sample Weight</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.parameters.SampleWeight
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSampleWeight()
	 * @generated
	 */
	int SAMPLE_WEIGHT = 11;

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.TomoExperiment <em>Tomo Experiment</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tomo Experiment</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment
	 * @generated
	 */
	EClass getTomoExperiment();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getParameters()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EReference getTomoExperiment_Parameters();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getDescription()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EAttribute getTomoExperiment_Description();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.TomoExperiment#getTotalTimeToRun <em>Total Time To Run</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Total Time To Run</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getTotalTimeToRun()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EAttribute getTomoExperiment_TotalTimeToRun();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getVersion()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EAttribute getTomoExperiment_Version();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.Module <em>Module</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Module</em>'.
	 * @see uk.ac.gda.tomography.parameters.Module
	 * @generated
	 */
	EClass getModule();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.Module#getModuleNumber <em>Module Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Module Number</em>'.
	 * @see uk.ac.gda.tomography.parameters.Module#getModuleNumber()
	 * @see #getModule()
	 * @generated
	 */
	EAttribute getModule_ModuleNumber();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.Module#getCameraMagnification <em>Camera Magnification</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Camera Magnification</em>'.
	 * @see uk.ac.gda.tomography.parameters.Module#getCameraMagnification()
	 * @see #getModule()
	 * @generated
	 */
	EAttribute getModule_CameraMagnification();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.MotorPosition <em>Motor Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Motor Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.MotorPosition
	 * @generated
	 */
	EClass getMotorPosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.MotorPosition#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see uk.ac.gda.tomography.parameters.MotorPosition#getName()
	 * @see #getMotorPosition()
	 * @generated
	 */
	EAttribute getMotorPosition_Name();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.MotorPosition#getPosition <em>Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.MotorPosition#getPosition()
	 * @see #getMotorPosition()
	 * @generated
	 */
	EAttribute getMotorPosition_Position();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorBin <em>Detector Bin</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Detector Bin</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin
	 * @generated
	 */
	EClass getDetectorBin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX <em>Bin X</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bin X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin#getBinX()
	 * @see #getDetectorBin()
	 * @generated
	 */
	EAttribute getDetectorBin_BinX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY <em>Bin Y</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bin Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin#getBinY()
	 * @see #getDetectorBin()
	 * @generated
	 */
	EAttribute getDetectorBin_BinY();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorProperties <em>Detector Properties</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Detector Properties</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties
	 * @generated
	 */
	EClass getDetectorProperties();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Desired3 DResolution</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_Desired3DResolution();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number Of Framer Per Projection</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_NumberOfFramerPerProjection();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Acquisition Time Divider</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_AcquisitionTimeDivider();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi <em>Detector Roi</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Detector Roi</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorRoi()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EReference getDetectorProperties_DetectorRoi();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin <em>Detector Bin</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Detector Bin</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getDetectorBin()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EReference getDetectorProperties_DetectorBin();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters <em>Module Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Module Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getModuleParameters()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EReference getDetectorProperties_ModuleParameters();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorRoi <em>Detector Roi</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Detector Roi</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi
	 * @generated
	 */
	EClass getDetectorRoi();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMinX <em>Min X</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMinX()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MinX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMaxX <em>Max X</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMaxX()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MaxX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMinY <em>Min Y</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMinY()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MinY();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMaxY <em>Max Y</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMaxY()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MaxY();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration <em>Alignment Configuration</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for class '<em>Alignment Configuration</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration
	 * @generated
	 */
	EClass getAlignmentConfiguration();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId <em>Id</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_Id();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy <em>Energy</em>}'.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Energy</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_Energy();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDescription <em>Description</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDescription()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_Description();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties <em>Detector Properties</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Detector Properties</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EReference getAlignmentConfiguration_DetectorProperties();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode <em>Scan Mode</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Scan Mode</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getScanMode()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_ScanMode();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime <em>Sample Exposure Time</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample Exposure Time</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_SampleExposureTime();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Flat Exposure Time</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_FlatExposureTime();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedUserId <em>Created User Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Created User Id</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedUserId()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_CreatedUserId();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedDateTime <em>Created Date Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Created Date Time</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getCreatedDateTime()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_CreatedDateTime();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleWeight <em>Sample Weight</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample Weight</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleWeight()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_SampleWeight();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getProposalId <em>Proposal Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Proposal Id</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getProposalId()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_ProposalId();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters <em>Stitch Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Stitch Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getStitchParameters()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EReference getAlignmentConfiguration_StitchParameters();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSelectedToRun <em>Selected To Run</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selected To Run</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSelectedToRun()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_SelectedToRun();

	/**
	 * Returns the meta object for the containment reference list '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getMotorPositions <em>Motor Positions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Motor Positions</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getMotorPositions()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EReference getAlignmentConfiguration_MotorPositions();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getInBeamPosition <em>In Beam Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>In Beam Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getInBeamPosition()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_InBeamPosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getOutOfBeamPosition <em>Out Of Beam Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Out Of Beam Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getOutOfBeamPosition()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_OutOfBeamPosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getTomoRotationAxis <em>Tomo Rotation Axis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tomo Rotation Axis</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getTomoRotationAxis()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_TomoRotationAxis();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.Parameters <em>Parameters</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.Parameters
	 * @generated
	 */
	EClass getParameters();

	/**
	 * Returns the meta object for the containment reference list '
	 * {@link uk.ac.gda.tomography.parameters.Parameters#getConfigurationSet <em>Configuration Set</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference list '<em>Configuration Set</em>'.
	 * @see uk.ac.gda.tomography.parameters.Parameters#getConfigurationSet()
	 * @see #getParameters()
	 * @generated
	 */
	EReference getParameters_ConfigurationSet();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.StitchParameters <em>Stitch Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stitch Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.StitchParameters
	 * @generated
	 */
	EClass getStitchParameters();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle <em>Stitching Theta Angle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stitching Theta Angle</em>'.
	 * @see uk.ac.gda.tomography.parameters.StitchParameters#getStitchingThetaAngle()
	 * @see #getStitchParameters()
	 * @generated
	 */
	EAttribute getStitchParameters_StitchingThetaAngle();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtTheta <em>Image At Theta</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Image At Theta</em>'.
	 * @see uk.ac.gda.tomography.parameters.StitchParameters#getImageAtTheta()
	 * @see #getStitchParameters()
	 * @generated
	 */
	EAttribute getStitchParameters_ImageAtTheta();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.StitchParameters#getImageAtThetaPlus90 <em>Image At Theta Plus90</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Image At Theta Plus90</em>'.
	 * @see uk.ac.gda.tomography.parameters.StitchParameters#getImageAtThetaPlus90()
	 * @see #getStitchParameters()
	 * @generated
	 */
	EAttribute getStitchParameters_ImageAtThetaPlus90();

	/**
	 * Returns the meta object for enum '{@link uk.ac.gda.tomography.parameters.ScanMode <em>Scan Mode</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for enum '<em>Scan Mode</em>'.
	 * @see uk.ac.gda.tomography.parameters.ScanMode
	 * @generated
	 */
	EEnum getScanMode();

	/**
	 * Returns the meta object for enum '{@link uk.ac.gda.tomography.parameters.Resolution <em>Resolution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Resolution</em>'.
	 * @see uk.ac.gda.tomography.parameters.Resolution
	 * @generated
	 */
	EEnum getResolution();

	/**
	 * Returns the meta object for enum '{@link uk.ac.gda.tomography.parameters.SampleWeight <em>Sample Weight</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Sample Weight</em>'.
	 * @see uk.ac.gda.tomography.parameters.SampleWeight
	 * @generated
	 */
	EEnum getSampleWeight();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	TomoParametersFactory getTomoParametersFactory();

	/**
	 * <!-- begin-user-doc --> Defines literals for the meta objects that represent
	 * <ul>
	 * <li>each class,</li>
	 * <li>each feature of each class,</li>
	 * <li>each enum,</li>
	 * <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("hiding")
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl <em>Tomo Experiment</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getTomoExperiment()
		 * @generated
		 */
		EClass TOMO_EXPERIMENT = eINSTANCE.getTomoExperiment();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		
		EReference TOMO_EXPERIMENT__PARAMETERS = eINSTANCE.getTomoExperiment_Parameters();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__DESCRIPTION = eINSTANCE.getTomoExperiment_Description();

		/**
		 * The meta object literal for the '<em><b>Total Time To Run</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__TOTAL_TIME_TO_RUN = eINSTANCE.getTomoExperiment_TotalTimeToRun();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__VERSION = eINSTANCE.getTomoExperiment_Version();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.ModuleImpl <em>Module</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.ModuleImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getModule()
		 * @generated
		 */
		EClass MODULE = eINSTANCE.getModule();

		/**
		 * The meta object literal for the '<em><b>Module Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODULE__MODULE_NUMBER = eINSTANCE.getModule_ModuleNumber();

		/**
		 * The meta object literal for the '<em><b>Camera Magnification</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODULE__CAMERA_MAGNIFICATION = eINSTANCE.getModule_CameraMagnification();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.MotorPositionImpl <em>Motor Position</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.MotorPositionImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getMotorPosition()
		 * @generated
		 */
		EClass MOTOR_POSITION = eINSTANCE.getMotorPosition();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MOTOR_POSITION__NAME = eINSTANCE.getMotorPosition_Name();

		/**
		 * The meta object literal for the '<em><b>Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MOTOR_POSITION__POSITION = eINSTANCE.getMotorPosition_Position();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl <em>Detector Bin</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorBinImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorBin()
		 * @generated
		 */
		EClass DETECTOR_BIN = eINSTANCE.getDetectorBin();

		/**
		 * The meta object literal for the '<em><b>Bin X</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_BIN__BIN_X = eINSTANCE.getDetectorBin_BinX();

		/**
		 * The meta object literal for the '<em><b>Bin Y</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_BIN__BIN_Y = eINSTANCE.getDetectorBin_BinY();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl <em>Detector Properties</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorProperties()
		 * @generated
		 */
		EClass DETECTOR_PROPERTIES = eINSTANCE.getDetectorProperties();

		/**
		 * The meta object literal for the '<em><b>Desired3 DResolution</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION = eINSTANCE.getDetectorProperties_Desired3DResolution();

		/**
		 * The meta object literal for the '<em><b>Number Of Framer Per Projection</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION = eINSTANCE.getDetectorProperties_NumberOfFramerPerProjection();

		/**
		 * The meta object literal for the '<em><b>Acquisition Time Divider</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER = eINSTANCE.getDetectorProperties_AcquisitionTimeDivider();

		/**
		 * The meta object literal for the '<em><b>Detector Roi</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference DETECTOR_PROPERTIES__DETECTOR_ROI = eINSTANCE.getDetectorProperties_DetectorRoi();

		/**
		 * The meta object literal for the '<em><b>Detector Bin</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference DETECTOR_PROPERTIES__DETECTOR_BIN = eINSTANCE.getDetectorProperties_DetectorBin();

		/**
		 * The meta object literal for the '<em><b>Module Parameters</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DETECTOR_PROPERTIES__MODULE_PARAMETERS = eINSTANCE.getDetectorProperties_ModuleParameters();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl <em>Detector Roi</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorRoi()
		 * @generated
		 */
		EClass DETECTOR_ROI = eINSTANCE.getDetectorRoi();

		/**
		 * The meta object literal for the '<em><b>Min X</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MIN_X = eINSTANCE.getDetectorRoi_MinX();

		/**
		 * The meta object literal for the '<em><b>Max X</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MAX_X = eINSTANCE.getDetectorRoi_MaxX();

		/**
		 * The meta object literal for the '<em><b>Min Y</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MIN_Y = eINSTANCE.getDetectorRoi_MinY();

		/**
		 * The meta object literal for the '<em><b>Max Y</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MAX_Y = eINSTANCE.getDetectorRoi_MaxY();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl <em>Alignment Configuration</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getAlignmentConfiguration()
		 * @generated
		 */
		EClass ALIGNMENT_CONFIGURATION = eINSTANCE.getAlignmentConfiguration();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__ID = eINSTANCE.getAlignmentConfiguration_Id();

		/**
		 * The meta object literal for the '<em><b>Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__ENERGY = eINSTANCE.getAlignmentConfiguration_Energy();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__DESCRIPTION = eINSTANCE.getAlignmentConfiguration_Description();

		/**
		 * The meta object literal for the '<em><b>Detector Properties</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES = eINSTANCE.getAlignmentConfiguration_DetectorProperties();

		/**
		 * The meta object literal for the '<em><b>Scan Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SCAN_MODE = eINSTANCE.getAlignmentConfiguration_ScanMode();

		/**
		 * The meta object literal for the '<em><b>Sample Exposure Time</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME = eINSTANCE.getAlignmentConfiguration_SampleExposureTime();

		/**
		 * The meta object literal for the '<em><b>Flat Exposure Time</b></em>' attribute feature.
		 * <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME = eINSTANCE.getAlignmentConfiguration_FlatExposureTime();

		/**
		 * The meta object literal for the '<em><b>Created User Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__CREATED_USER_ID = eINSTANCE.getAlignmentConfiguration_CreatedUserId();

		/**
		 * The meta object literal for the '<em><b>Created Date Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME = eINSTANCE.getAlignmentConfiguration_CreatedDateTime();

		/**
		 * The meta object literal for the '<em><b>Sample Weight</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT = eINSTANCE.getAlignmentConfiguration_SampleWeight();

		/**
		 * The meta object literal for the '<em><b>Proposal Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__PROPOSAL_ID = eINSTANCE.getAlignmentConfiguration_ProposalId();

		/**
		 * The meta object literal for the '<em><b>Stitch Parameters</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS = eINSTANCE.getAlignmentConfiguration_StitchParameters();

		/**
		 * The meta object literal for the '<em><b>Selected To Run</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN = eINSTANCE.getAlignmentConfiguration_SelectedToRun();

		/**
		 * The meta object literal for the '<em><b>Motor Positions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS = eINSTANCE.getAlignmentConfiguration_MotorPositions();

		/**
		 * The meta object literal for the '<em><b>In Beam Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION = eINSTANCE.getAlignmentConfiguration_InBeamPosition();

		/**
		 * The meta object literal for the '<em><b>Out Of Beam Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION = eINSTANCE.getAlignmentConfiguration_OutOfBeamPosition();

		/**
		 * The meta object literal for the '<em><b>Tomo Rotation Axis</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS = eINSTANCE.getAlignmentConfiguration_TomoRotationAxis();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.ParametersImpl <em>Parameters</em>}' class.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.ParametersImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getParameters()
		 * @generated
		 */
		EClass PARAMETERS = eINSTANCE.getParameters();

		/**
		 * The meta object literal for the '<em><b>Configuration Set</b></em>' containment reference list feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference PARAMETERS__CONFIGURATION_SET = eINSTANCE.getParameters_ConfigurationSet();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.StitchParametersImpl <em>Stitch Parameters</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.impl.StitchParametersImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getStitchParameters()
		 * @generated
		 */
		EClass STITCH_PARAMETERS = eINSTANCE.getStitchParameters();

		/**
		 * The meta object literal for the '<em><b>Stitching Theta Angle</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STITCH_PARAMETERS__STITCHING_THETA_ANGLE = eINSTANCE.getStitchParameters_StitchingThetaAngle();

		/**
		 * The meta object literal for the '<em><b>Image At Theta</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STITCH_PARAMETERS__IMAGE_AT_THETA = eINSTANCE.getStitchParameters_ImageAtTheta();

		/**
		 * The meta object literal for the '<em><b>Image At Theta Plus90</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90 = eINSTANCE.getStitchParameters_ImageAtThetaPlus90();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.ScanMode <em>Scan Mode</em>}' enum.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.ScanMode
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getScanMode()
		 * @generated
		 */
		EEnum SCAN_MODE = eINSTANCE.getScanMode();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.Resolution <em>Resolution</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.Resolution
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getResolution()
		 * @generated
		 */
		EEnum RESOLUTION = eINSTANCE.getResolution();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.SampleWeight <em>Sample Weight</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.parameters.SampleWeight
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSampleWeight()
		 * @generated
		 */
		EEnum SAMPLE_WEIGHT = eINSTANCE.getSampleWeight();

	}

} // TomoParametersPackage
