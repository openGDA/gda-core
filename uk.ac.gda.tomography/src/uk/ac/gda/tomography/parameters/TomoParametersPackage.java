/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see uk.ac.gda.tomography.parameters.TomoParametersFactory
 * @model kind="package"
 * @generated
 * 
 */
public interface TomoParametersPackage extends EPackage {
	/**
	 * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNAME = "parameters";

	/**
	 * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_URI = "http:///uk/ac/gda/client/tomo/tomoparameters.ecore";

	/**
	 * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	String eNS_PREFIX = "uk.ac.gda.client.tomo.parameters";

	/**
	 * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	TomoParametersPackage eINSTANCE = uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl
	 * <em>Tomo Experiment</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl
	 * @see TomoParametersPackageImpl#getTomoExperiment()
	 * @generated
	 */
	int TOMO_EXPERIMENT = 0;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__PARAMETERS = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * The feature id for the '<em><b>Version</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT__VERSION = 3;

	/**
	 * The number of structural features of the '<em>Tomo Experiment</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int TOMO_EXPERIMENT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl <em>Detector Bin</em>}'
	 * class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorBinImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorBin()
	 * @generated
	 */
	int DETECTOR_BIN = 1;

	/**
	 * The feature id for the '<em><b>Bin X</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_BIN__BIN_X = 0;

	/**
	 * The feature id for the '<em><b>Bin Y</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
	 * <em>Detector Properties</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorProperties()
	 * @generated
	 */
	int DETECTOR_PROPERTIES = 2;

	/**
	 * The feature id for the '<em><b>Module</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__MODULE = 0;

	/**
	 * The feature id for the '<em><b>Desired3 DResolution</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION = 1;

	/**
	 * The feature id for the '<em><b>Number Of Framer Per Projection</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION = 2;

	/**
	 * The feature id for the '<em><b>Acquisition Time Divider</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER = 3;

	/**
	 * The feature id for the '<em><b>Detector Roi</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DETECTOR_ROI = 4;

	/**
	 * The feature id for the '<em><b>Detector Bin</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES__DETECTOR_BIN = 5;

	/**
	 * The number of structural features of the '<em>Detector Properties</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_PROPERTIES_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl <em>Detector Roi</em>}'
	 * class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorRoi()
	 * @generated
	 */
	int DETECTOR_ROI = 3;

	/**
	 * The feature id for the '<em><b>Min X</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MIN_X = 0;

	/**
	 * The feature id for the '<em><b>Max X</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MAX_X = 1;

	/**
	 * The feature id for the '<em><b>Min Y</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int DETECTOR_ROI__MIN_Y = 2;

	/**
	 * The feature id for the '<em><b>Max Y</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
	 * <em>Alignment Configuration</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getAlignmentConfiguration()
	 * @generated
	 */
	int ALIGNMENT_CONFIGURATION = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__ID = 0;

	/**
	 * The feature id for the '<em><b>Energy</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__ENERGY = 1;

	/**
	 * The feature id for the '<em><b>Number Of Projections</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS = 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__DESCRIPTION = 3;

	/**
	 * The feature id for the '<em><b>Detector Properties</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES = 4;

	/**
	 * The feature id for the '<em><b>Sample Detector Distance</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE = 5;

	/**
	 * The feature id for the '<em><b>Sample Params</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS = 6;

	/**
	 * The feature id for the '<em><b>Scan Mode</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SCAN_MODE = 7;

	/**
	 * The feature id for the '<em><b>Sample Exposure Time</b></em>' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME = 8;

	/**
	 * The feature id for the '<em><b>Flat Exposure Time</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME = 9;

	/**
	 * The number of structural features of the '<em>Alignment Configuration</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int ALIGNMENT_CONFIGURATION_FEATURE_COUNT = 10;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.ParametersImpl <em>Parameters</em>}'
	 * class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.ParametersImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getParameters()
	 * @generated
	 */
	int PARAMETERS = 5;

	/**
	 * The feature id for the '<em><b>Configuration Set</b></em>' containment reference list. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
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
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.SampleParamsImpl <em>Sample Params</em>}
	 * ' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.SampleParamsImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSampleParams()
	 * @generated
	 */
	int SAMPLE_PARAMS = 6;

	/**
	 * The feature id for the '<em><b>Position</b></em>' containment reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_PARAMS__POSITION = 0;

	/**
	 * The feature id for the '<em><b>Weight</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_PARAMS__WEIGHT = 1;

	/**
	 * The number of structural features of the '<em>Sample Params</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_PARAMS_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl
	 * <em>Sample Position</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.impl.SamplePositionImpl
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSamplePosition()
	 * @generated
	 */
	int SAMPLE_POSITION = 7;

	/**
	 * The feature id for the '<em><b>Vertical</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION__VERTICAL = 0;

	/**
	 * The feature id for the '<em><b>Center X</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION__CENTER_X = 1;

	/**
	 * The feature id for the '<em><b>Center Z</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION__CENTER_Z = 2;

	/**
	 * The feature id for the '<em><b>Tilt X</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION__TILT_X = 3;

	/**
	 * The feature id for the '<em><b>Tilt Z</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION__TILT_Z = 4;

	/**
	 * The number of structural features of the '<em>Sample Position</em>' class. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 * @ordered
	 */
	int SAMPLE_POSITION_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.parameters.ScanMode <em>Scan Mode</em>}' enum. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see uk.ac.gda.tomography.parameters.ScanMode
	 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getScanMode()
	 * @generated
	 */
	int SCAN_MODE = 8;

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.TomoExperiment
	 * <em>Tomo Experiment</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Tomo Experiment</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment
	 * @generated
	 */
	EClass getTomoExperiment();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.TomoExperiment#getParameters <em>Parameters</em>}'. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Parameters</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getParameters()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EReference getTomoExperiment_Parameters();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription
	 * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion
	 * <em>Version</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see uk.ac.gda.tomography.parameters.TomoExperiment#getVersion()
	 * @see #getTomoExperiment()
	 * @generated
	 */
	EAttribute getTomoExperiment_Version();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorBin <em>Detector Bin</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Detector Bin</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin
	 * @generated
	 */
	EClass getDetectorBin();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinX
	 * <em>Bin X</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Bin X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin#getBinX()
	 * @see #getDetectorBin()
	 * @generated
	 */
	EAttribute getDetectorBin_BinX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorBin#getBinY
	 * <em>Bin Y</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Bin Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorBin#getBinY()
	 * @see #getDetectorBin()
	 * @generated
	 */
	EAttribute getDetectorBin_BinY();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorProperties
	 * <em>Detector Properties</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Detector Properties</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties
	 * @generated
	 */
	EClass getDetectorProperties();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorProperties#getModule
	 * <em>Module</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Module</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getModule()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_Module();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution <em>Desired3 DResolution</em>}
	 * '. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Desired3 DResolution</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getDesired3DResolution()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_Desired3DResolution();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection
	 * <em>Number Of Framer Per Projection</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Number Of Framer Per Projection</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorProperties#getNumberOfFramerPerProjection()
	 * @see #getDetectorProperties()
	 * @generated
	 */
	EAttribute getDetectorProperties_NumberOfFramerPerProjection();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.DetectorProperties#getAcquisitionTimeDivider
	 * <em>Acquisition Time Divider</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.DetectorRoi <em>Detector Roi</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Detector Roi</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi
	 * @generated
	 */
	EClass getDetectorRoi();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMinX
	 * <em>Min X</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Min X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMinX()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MinX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMaxX
	 * <em>Max X</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Max X</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMaxX()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MaxX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMinY
	 * <em>Min Y</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Min Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMinY()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MinY();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.DetectorRoi#getMaxY
	 * <em>Max Y</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Max Y</em>'.
	 * @see uk.ac.gda.tomography.parameters.DetectorRoi#getMaxY()
	 * @see #getDetectorRoi()
	 * @generated
	 */
	EAttribute getDetectorRoi_MaxY();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration
	 * <em>Alignment Configuration</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Alignment Configuration</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration
	 * @generated
	 */
	EClass getAlignmentConfiguration();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId
	 * <em>Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getId()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_Id();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy <em>Energy</em>}'. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Energy</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getEnergy()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_Energy();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections
	 * <em>Number Of Projections</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Number Of Projections</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getNumberOfProjections()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_NumberOfProjections();

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
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties
	 * <em>Detector Properties</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Detector Properties</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getDetectorProperties()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EReference getAlignmentConfiguration_DetectorProperties();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance
	 * <em>Sample Detector Distance</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Sample Detector Distance</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleDetectorDistance()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_SampleDetectorDistance();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams <em>Sample Params</em>}'. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Sample Params</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleParams()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EReference getAlignmentConfiguration_SampleParams();

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
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime
	 * <em>Sample Exposure Time</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Sample Exposure Time</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getSampleExposureTime()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_SampleExposureTime();

	/**
	 * Returns the meta object for the attribute '
	 * {@link uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime <em>Flat Exposure Time</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Flat Exposure Time</em>'.
	 * @see uk.ac.gda.tomography.parameters.AlignmentConfiguration#getFlatExposureTime()
	 * @see #getAlignmentConfiguration()
	 * @generated
	 */
	EAttribute getAlignmentConfiguration_FlatExposureTime();

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
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.SampleParams <em>Sample Params</em>}'.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Sample Params</em>'.
	 * @see uk.ac.gda.tomography.parameters.SampleParams
	 * @generated
	 */
	EClass getSampleParams();

	/**
	 * Returns the meta object for the containment reference '
	 * {@link uk.ac.gda.tomography.parameters.SampleParams#getPosition <em>Position</em>}'. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @return the meta object for the containment reference '<em>Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.SampleParams#getPosition()
	 * @see #getSampleParams()
	 * @generated
	 */
	EReference getSampleParams_Position();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SampleParams#getWeight
	 * <em>Weight</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Weight</em>'.
	 * @see uk.ac.gda.tomography.parameters.SampleParams#getWeight()
	 * @see #getSampleParams()
	 * @generated
	 */
	EAttribute getSampleParams_Weight();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.parameters.SamplePosition
	 * <em>Sample Position</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for class '<em>Sample Position</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition
	 * @generated
	 */
	EClass getSamplePosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SamplePosition#getVertical
	 * <em>Vertical</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Vertical</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition#getVertical()
	 * @see #getSamplePosition()
	 * @generated
	 */
	EAttribute getSamplePosition_Vertical();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterX
	 * <em>Center X</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Center X</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition#getCenterX()
	 * @see #getSamplePosition()
	 * @generated
	 */
	EAttribute getSamplePosition_CenterX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ
	 * <em>Center Z</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Center Z</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition#getCenterZ()
	 * @see #getSamplePosition()
	 * @generated
	 */
	EAttribute getSamplePosition_CenterZ();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltX
	 * <em>Tilt X</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Tilt X</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition#getTiltX()
	 * @see #getSamplePosition()
	 * @generated
	 */
	EAttribute getSamplePosition_TiltX();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ
	 * <em>Tilt Z</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the meta object for the attribute '<em>Tilt Z</em>'.
	 * @see uk.ac.gda.tomography.parameters.SamplePosition#getTiltZ()
	 * @see #getSamplePosition()
	 * @generated
	 */
	EAttribute getSamplePosition_TiltZ();

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
	 * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
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
	 * 
	 * @generated
	 */
	@SuppressWarnings("hiding")
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.TomoExperimentImpl
		 * <em>Tomo Experiment</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
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
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__DESCRIPTION = eINSTANCE.getTomoExperiment_Description();

		/**
		 * The meta object literal for the '<em><b>Total Time To Run</b></em>' attribute feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__TOTAL_TIME_TO_RUN = eINSTANCE.getTomoExperiment_TotalTimeToRun();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute TOMO_EXPERIMENT__VERSION = eINSTANCE.getTomoExperiment_Version();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl
		 * <em>Detector Bin</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorBinImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorBin()
		 * @generated
		 */
		EClass DETECTOR_BIN = eINSTANCE.getDetectorBin();

		/**
		 * The meta object literal for the '<em><b>Bin X</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_BIN__BIN_X = eINSTANCE.getDetectorBin_BinX();

		/**
		 * The meta object literal for the '<em><b>Bin Y</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_BIN__BIN_Y = eINSTANCE.getDetectorBin_BinY();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
		 * <em>Detector Properties</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorProperties()
		 * @generated
		 */
		EClass DETECTOR_PROPERTIES = eINSTANCE.getDetectorProperties();

		/**
		 * The meta object literal for the '<em><b>Module</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__MODULE = eINSTANCE.getDetectorProperties_Module();

		/**
		 * The meta object literal for the '<em><b>Desired3 DResolution</b></em>' attribute feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION = eINSTANCE.getDetectorProperties_Desired3DResolution();

		/**
		 * The meta object literal for the '<em><b>Number Of Framer Per Projection</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION = eINSTANCE
				.getDetectorProperties_NumberOfFramerPerProjection();

		/**
		 * The meta object literal for the '<em><b>Acquisition Time Divider</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER = eINSTANCE
				.getDetectorProperties_AcquisitionTimeDivider();

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
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl
		 * <em>Detector Roi</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getDetectorRoi()
		 * @generated
		 */
		EClass DETECTOR_ROI = eINSTANCE.getDetectorRoi();

		/**
		 * The meta object literal for the '<em><b>Min X</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MIN_X = eINSTANCE.getDetectorRoi_MinX();

		/**
		 * The meta object literal for the '<em><b>Max X</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MAX_X = eINSTANCE.getDetectorRoi_MaxX();

		/**
		 * The meta object literal for the '<em><b>Min Y</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MIN_Y = eINSTANCE.getDetectorRoi_MinY();

		/**
		 * The meta object literal for the '<em><b>Max Y</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute DETECTOR_ROI__MAX_Y = eINSTANCE.getDetectorRoi_MaxY();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
		 * <em>Alignment Configuration</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getAlignmentConfiguration()
		 * @generated
		 */
		EClass ALIGNMENT_CONFIGURATION = eINSTANCE.getAlignmentConfiguration();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__ID = eINSTANCE.getAlignmentConfiguration_Id();

		/**
		 * The meta object literal for the '<em><b>Energy</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__ENERGY = eINSTANCE.getAlignmentConfiguration_Energy();

		/**
		 * The meta object literal for the '<em><b>Number Of Projections</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS = eINSTANCE
				.getAlignmentConfiguration_NumberOfProjections();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__DESCRIPTION = eINSTANCE.getAlignmentConfiguration_Description();

		/**
		 * The meta object literal for the '<em><b>Detector Properties</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES = eINSTANCE
				.getAlignmentConfiguration_DetectorProperties();

		/**
		 * The meta object literal for the '<em><b>Sample Detector Distance</b></em>' attribute feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE = eINSTANCE
				.getAlignmentConfiguration_SampleDetectorDistance();

		/**
		 * The meta object literal for the '<em><b>Sample Params</b></em>' containment reference feature. <!--
		 * begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS = eINSTANCE.getAlignmentConfiguration_SampleParams();

		/**
		 * The meta object literal for the '<em><b>Scan Mode</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SCAN_MODE = eINSTANCE.getAlignmentConfiguration_ScanMode();

		/**
		 * The meta object literal for the '<em><b>Sample Exposure Time</b></em>' attribute feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME = eINSTANCE
				.getAlignmentConfiguration_SampleExposureTime();

		/**
		 * The meta object literal for the '<em><b>Flat Exposure Time</b></em>' attribute feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME = eINSTANCE.getAlignmentConfiguration_FlatExposureTime();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.ParametersImpl
		 * <em>Parameters</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
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
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.SampleParamsImpl
		 * <em>Sample Params</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.SampleParamsImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSampleParams()
		 * @generated
		 */
		EClass SAMPLE_PARAMS = eINSTANCE.getSampleParams();

		/**
		 * The meta object literal for the '<em><b>Position</b></em>' containment reference feature. <!-- begin-user-doc
		 * --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		EReference SAMPLE_PARAMS__POSITION = eINSTANCE.getSampleParams_Position();

		/**
		 * The meta object literal for the '<em><b>Weight</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_PARAMS__WEIGHT = eINSTANCE.getSampleParams_Weight();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl
		 * <em>Sample Position</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.impl.SamplePositionImpl
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getSamplePosition()
		 * @generated
		 */
		EClass SAMPLE_POSITION = eINSTANCE.getSamplePosition();

		/**
		 * The meta object literal for the '<em><b>Vertical</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_POSITION__VERTICAL = eINSTANCE.getSamplePosition_Vertical();

		/**
		 * The meta object literal for the '<em><b>Center X</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_POSITION__CENTER_X = eINSTANCE.getSamplePosition_CenterX();

		/**
		 * The meta object literal for the '<em><b>Center Z</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_POSITION__CENTER_Z = eINSTANCE.getSamplePosition_CenterZ();

		/**
		 * The meta object literal for the '<em><b>Tilt X</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_POSITION__TILT_X = eINSTANCE.getSamplePosition_TiltX();

		/**
		 * The meta object literal for the '<em><b>Tilt Z</b></em>' attribute feature. <!-- begin-user-doc --> <!--
		 * end-user-doc -->
		 * 
		 * @generated
		 */
		EAttribute SAMPLE_POSITION__TILT_Z = eINSTANCE.getSamplePosition_TiltZ();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.tomography.parameters.ScanMode <em>Scan Mode</em>}' enum.
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @see uk.ac.gda.tomography.parameters.ScanMode
		 * @see uk.ac.gda.tomography.parameters.impl.TomoParametersPackageImpl#getScanMode()
		 * @generated
		 */
		EEnum SCAN_MODE = eINSTANCE.getScanMode();

	}

} // TomoParametersPackage
