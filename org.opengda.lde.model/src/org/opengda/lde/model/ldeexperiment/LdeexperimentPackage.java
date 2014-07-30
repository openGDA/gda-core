/**
 */
package org.opengda.lde.model.ldeexperiment;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.opengda.lde.model.ldeexperiment.LdeexperimentFactory
 * @model kind="package"
 * @generated
 */
public interface LdeexperimentPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "ldeexperiment";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.opengda.org/LDEExperiments";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "lde";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LdeexperimentPackage eINSTANCE = org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl <em>Experiment Definition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getExperimentDefinition()
	 * @generated
	 */
	int EXPERIMENT_DEFINITION = 0;

	/**
	 * The feature id for the '<em><b>Samplelist</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION__SAMPLELIST = 0;

	/**
	 * The number of structural features of the '<em>Experiment Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Experiment Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPERIMENT_DEFINITION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleListImpl <em>Sample List</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.SampleListImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSampleList()
	 * @generated
	 */
	int SAMPLE_LIST = 1;

	/**
	 * The feature id for the '<em><b>Filename</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST__FILENAME = 0;

	/**
	 * The feature id for the '<em><b>Samples</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST__SAMPLES = 1;

	/**
	 * The number of structural features of the '<em>Sample List</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST_FEATURE_COUNT = 2;

	/**
	 * The operation id for the '<em>Get Sample By Id</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST___GET_SAMPLE_BY_ID__STRING = 0;

	/**
	 * The operation id for the '<em>Get Sample By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST___GET_SAMPLE_BY_NAME__STRING = 1;

	/**
	 * The number of operations of the '<em>Sample List</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_LIST_OPERATION_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl <em>Sample</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.impl.SampleImpl
	 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSample()
	 * @generated
	 */
	int SAMPLE = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Sample ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_ID = 1;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__STATUS = 2;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__ACTIVE = 3;

	/**
	 * The feature id for the '<em><b>Cell ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CELL_ID = 4;

	/**
	 * The feature id for the '<em><b>Visit ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__VISIT_ID = 5;

	/**
	 * The feature id for the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__EMAIL = 6;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__COMMENT = 7;

	/**
	 * The feature id for the '<em><b>Mail Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__MAIL_COUNT = 8;

	/**
	 * The feature id for the '<em><b>Data File Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__DATA_FILE_COUNT = 9;

	/**
	 * The feature id for the '<em><b>Data File Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__DATA_FILE_PATH = 10;

	/**
	 * The feature id for the '<em><b>Calibrant</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CALIBRANT = 11;

	/**
	 * The feature id for the '<em><b>Calibrant x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CALIBRANT_X = 12;

	/**
	 * The feature id for the '<em><b>Calibrant y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CALIBRANT_Y = 13;

	/**
	 * The feature id for the '<em><b>Calibrant exposure</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__CALIBRANT_EXPOSURE = 14;

	/**
	 * The feature id for the '<em><b>Xstart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__XSTART = 15;

	/**
	 * The feature id for the '<em><b>Xstop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__XSTOP = 16;

	/**
	 * The feature id for the '<em><b>Xstep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__XSTEP = 17;

	/**
	 * The feature id for the '<em><b>Ystop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__YSTOP = 18;

	/**
	 * The feature id for the '<em><b>Ystep</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__YSTEP = 19;

	/**
	 * The feature id for the '<em><b>Sample exposure</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__SAMPLE_EXPOSURE = 20;

	/**
	 * The feature id for the '<em><b>Drive ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__DRIVE_ID = 21;

	/**
	 * The feature id for the '<em><b>Pixium x</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__PIXIUM_X = 22;

	/**
	 * The feature id for the '<em><b>Pixium y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__PIXIUM_Y = 23;

	/**
	 * The feature id for the '<em><b>Pixium z</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__PIXIUM_Z = 24;

	/**
	 * The feature id for the '<em><b>Ystart</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__YSTART = 25;

	/**
	 * The feature id for the '<em><b>Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__START_DATE = 26;

	/**
	 * The feature id for the '<em><b>End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__END_DATE = 27;

	/**
	 * The feature id for the '<em><b>Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE__COMMAND = 28;

	/**
	 * The number of structural features of the '<em>Sample</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_FEATURE_COUNT = 29;

	/**
	 * The number of operations of the '<em>Sample</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAMPLE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
	 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSTATUS()
	 * @generated
	 */
	int STATUS = 3;


	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition <em>Experiment Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Experiment Definition</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.ExperimentDefinition
	 * @generated
	 */
	EClass getExperimentDefinition();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getSamplelist <em>Samplelist</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Samplelist</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.ExperimentDefinition#getSamplelist()
	 * @see #getExperimentDefinition()
	 * @generated
	 */
	EReference getExperimentDefinition_Samplelist();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.SampleList <em>Sample List</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sample List</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.SampleList
	 * @generated
	 */
	EClass getSampleList();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.SampleList#getFilename <em>Filename</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Filename</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.SampleList#getFilename()
	 * @see #getSampleList()
	 * @generated
	 */
	EAttribute getSampleList_Filename();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.lde.model.ldeexperiment.SampleList#getSamples <em>Samples</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Samples</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.SampleList#getSamples()
	 * @see #getSampleList()
	 * @generated
	 */
	EReference getSampleList_Samples();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.SampleList#getSampleById(java.lang.String) <em>Get Sample By Id</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Sample By Id</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.SampleList#getSampleById(java.lang.String)
	 * @generated
	 */
	EOperation getSampleList__GetSampleById__String();

	/**
	 * Returns the meta object for the '{@link org.opengda.lde.model.ldeexperiment.SampleList#getSampleByName(java.lang.String) <em>Get Sample By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Sample By Name</em>' operation.
	 * @see org.opengda.lde.model.ldeexperiment.SampleList#getSampleByName(java.lang.String)
	 * @generated
	 */
	EOperation getSampleList__GetSampleByName__String();

	/**
	 * Returns the meta object for class '{@link org.opengda.lde.model.ldeexperiment.Sample <em>Sample</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sample</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample
	 * @generated
	 */
	EClass getSample();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getName()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSampleID <em>Sample ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSampleID()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_SampleID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getStatus()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#isActive()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Active();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCellID <em>Cell ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Cell ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCellID()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_CellID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getVisitID <em>Visit ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visit ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getVisitID()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_VisitID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getEmail <em>Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Email</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getEmail()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Email();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCommand()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Command();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getComment <em>Comment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comment</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getComment()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Comment();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getMailCount <em>Mail Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mail Count</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getMailCount()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_MailCount();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFileCount <em>Data File Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data File Count</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getDataFileCount()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_DataFileCount();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath <em>Data File Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data File Path</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getDataFilePath()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_DataFilePath();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant <em>Calibrant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCalibrant()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Calibrant();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_x <em>Calibrant x</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant x</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_x()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Calibrant_x();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_y <em>Calibrant y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant y</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_y()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Calibrant_y();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_exposure <em>Calibrant exposure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Calibrant exposure</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getCalibrant_exposure()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Calibrant_exposure();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_start <em>Xstart</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xstart</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getX_start()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_X_start();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_stop <em>Xstop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xstop</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getX_stop()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_X_stop();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getX_step <em>Xstep</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Xstep</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getX_step()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_X_step();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_stop <em>Ystop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ystop</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getY_stop()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Y_stop();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_step <em>Ystep</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ystep</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getY_step()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Y_step();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure <em>Sample exposure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sample exposure</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getSample_exposure()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Sample_exposure();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getDriveID <em>Drive ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Drive ID</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getDriveID()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_DriveID();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_x <em>Pixium x</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixium x</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getPixium_x()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Pixium_x();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_y <em>Pixium y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixium y</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getPixium_y()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Pixium_y();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getPixium_z <em>Pixium z</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pixium z</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getPixium_z()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Pixium_z();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getY_start <em>Ystart</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ystart</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getY_start()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_Y_start();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getStartDate <em>Start Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Date</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getStartDate()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_StartDate();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.lde.model.ldeexperiment.Sample#getEndDate <em>End Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>End Date</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.Sample#getEndDate()
	 * @see #getSample()
	 * @generated
	 */
	EAttribute getSample_EndDate();

	/**
	 * Returns the meta object for enum '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>STATUS</em>'.
	 * @see org.opengda.lde.model.ldeexperiment.STATUS
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
	LdeexperimentFactory getLdeexperimentFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl <em>Experiment Definition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getExperimentDefinition()
		 * @generated
		 */
		EClass EXPERIMENT_DEFINITION = eINSTANCE.getExperimentDefinition();

		/**
		 * The meta object literal for the '<em><b>Samplelist</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPERIMENT_DEFINITION__SAMPLELIST = eINSTANCE.getExperimentDefinition_Samplelist();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleListImpl <em>Sample List</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.SampleListImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSampleList()
		 * @generated
		 */
		EClass SAMPLE_LIST = eINSTANCE.getSampleList();

		/**
		 * The meta object literal for the '<em><b>Filename</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE_LIST__FILENAME = eINSTANCE.getSampleList_Filename();

		/**
		 * The meta object literal for the '<em><b>Samples</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SAMPLE_LIST__SAMPLES = eINSTANCE.getSampleList_Samples();

		/**
		 * The meta object literal for the '<em><b>Get Sample By Id</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SAMPLE_LIST___GET_SAMPLE_BY_ID__STRING = eINSTANCE.getSampleList__GetSampleById__String();

		/**
		 * The meta object literal for the '<em><b>Get Sample By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SAMPLE_LIST___GET_SAMPLE_BY_NAME__STRING = eINSTANCE.getSampleList__GetSampleByName__String();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl <em>Sample</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.impl.SampleImpl
		 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSample()
		 * @generated
		 */
		EClass SAMPLE = eINSTANCE.getSample();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__NAME = eINSTANCE.getSample_Name();

		/**
		 * The meta object literal for the '<em><b>Sample ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_ID = eINSTANCE.getSample_SampleID();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__STATUS = eINSTANCE.getSample_Status();

		/**
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__ACTIVE = eINSTANCE.getSample_Active();

		/**
		 * The meta object literal for the '<em><b>Cell ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CELL_ID = eINSTANCE.getSample_CellID();

		/**
		 * The meta object literal for the '<em><b>Visit ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__VISIT_ID = eINSTANCE.getSample_VisitID();

		/**
		 * The meta object literal for the '<em><b>Email</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__EMAIL = eINSTANCE.getSample_Email();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__COMMAND = eINSTANCE.getSample_Command();

		/**
		 * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__COMMENT = eINSTANCE.getSample_Comment();

		/**
		 * The meta object literal for the '<em><b>Mail Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__MAIL_COUNT = eINSTANCE.getSample_MailCount();

		/**
		 * The meta object literal for the '<em><b>Data File Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__DATA_FILE_COUNT = eINSTANCE.getSample_DataFileCount();

		/**
		 * The meta object literal for the '<em><b>Data File Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__DATA_FILE_PATH = eINSTANCE.getSample_DataFilePath();

		/**
		 * The meta object literal for the '<em><b>Calibrant</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CALIBRANT = eINSTANCE.getSample_Calibrant();

		/**
		 * The meta object literal for the '<em><b>Calibrant x</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CALIBRANT_X = eINSTANCE.getSample_Calibrant_x();

		/**
		 * The meta object literal for the '<em><b>Calibrant y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CALIBRANT_Y = eINSTANCE.getSample_Calibrant_y();

		/**
		 * The meta object literal for the '<em><b>Calibrant exposure</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__CALIBRANT_EXPOSURE = eINSTANCE.getSample_Calibrant_exposure();

		/**
		 * The meta object literal for the '<em><b>Xstart</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__XSTART = eINSTANCE.getSample_X_start();

		/**
		 * The meta object literal for the '<em><b>Xstop</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__XSTOP = eINSTANCE.getSample_X_stop();

		/**
		 * The meta object literal for the '<em><b>Xstep</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__XSTEP = eINSTANCE.getSample_X_step();

		/**
		 * The meta object literal for the '<em><b>Ystop</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__YSTOP = eINSTANCE.getSample_Y_stop();

		/**
		 * The meta object literal for the '<em><b>Ystep</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__YSTEP = eINSTANCE.getSample_Y_step();

		/**
		 * The meta object literal for the '<em><b>Sample exposure</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__SAMPLE_EXPOSURE = eINSTANCE.getSample_Sample_exposure();

		/**
		 * The meta object literal for the '<em><b>Drive ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__DRIVE_ID = eINSTANCE.getSample_DriveID();

		/**
		 * The meta object literal for the '<em><b>Pixium x</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__PIXIUM_X = eINSTANCE.getSample_Pixium_x();

		/**
		 * The meta object literal for the '<em><b>Pixium y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__PIXIUM_Y = eINSTANCE.getSample_Pixium_y();

		/**
		 * The meta object literal for the '<em><b>Pixium z</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__PIXIUM_Z = eINSTANCE.getSample_Pixium_z();

		/**
		 * The meta object literal for the '<em><b>Ystart</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__YSTART = eINSTANCE.getSample_Y_start();

		/**
		 * The meta object literal for the '<em><b>Start Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__START_DATE = eINSTANCE.getSample_StartDate();

		/**
		 * The meta object literal for the '<em><b>End Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAMPLE__END_DATE = eINSTANCE.getSample_EndDate();

		/**
		 * The meta object literal for the '{@link org.opengda.lde.model.ldeexperiment.STATUS <em>STATUS</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.lde.model.ldeexperiment.STATUS
		 * @see org.opengda.lde.model.ldeexperiment.impl.LdeexperimentPackageImpl#getSTATUS()
		 * @generated
		 */
		EEnum STATUS = eINSTANCE.getSTATUS();

	}

} //LdeexperimentPackage
