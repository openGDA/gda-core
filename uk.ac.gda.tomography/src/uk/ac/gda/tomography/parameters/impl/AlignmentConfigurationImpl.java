/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import java.util.Collection;
import java.util.Date;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.MotorPosition;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.ScanMode;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Alignment Configuration</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getId <em>Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getEnergy <em>Energy</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getDetectorProperties <em>Detector Properties</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getScanMode <em>Scan Mode</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSampleExposureTime <em>Sample Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getFlatExposureTime <em>Flat Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getCreatedUserId <em>Created User Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getCreatedDateTime <em>Created Date Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSampleWeight <em>Sample Weight</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getProposalId <em>Proposal Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getStitchParameters <em>Stitch Parameters</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSelectedToRun <em>Selected To Run</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getMotorPositions <em>Motor Positions</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getInBeamPosition <em>In Beam Position</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getOutOfBeamPosition <em>Out Of Beam Position</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getTomoRotationAxis <em>Tomo Rotation Axis</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AlignmentConfigurationImpl extends EObjectImpl implements AlignmentConfiguration {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * This is true if the Id attribute has been set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean idESet;

	/**
	 * The default value of the '{@link #getEnergy() <em>Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double ENERGY_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getEnergy() <em>Energy</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected double energy = ENERGY_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getDetectorProperties() <em>Detector Properties</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getDetectorProperties()
	 * @generated
	 * @ordered
	 */
	protected DetectorProperties detectorProperties;

	/**
	 * This is true if the Detector Properties containment reference has been set.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean detectorPropertiesESet;

	/**
	 * The default value of the '{@link #getScanMode() <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getScanMode()
	 * @generated
	 * @ordered
	 */
	protected static final ScanMode SCAN_MODE_EDEFAULT = ScanMode.STEP;

	/**
	 * The cached value of the '{@link #getScanMode() <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getScanMode()
	 * @generated
	 * @ordered
	 */
	protected ScanMode scanMode = SCAN_MODE_EDEFAULT;

	/**
	 * This is true if the Scan Mode attribute has been set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean scanModeESet;

	/**
	 * The default value of the '{@link #getSampleExposureTime() <em>Sample Exposure Time</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSampleExposureTime()
	 * @generated
	 * @ordered
	 */
	protected static final double SAMPLE_EXPOSURE_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getSampleExposureTime() <em>Sample Exposure Time</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSampleExposureTime()
	 * @generated
	 * @ordered
	 */
	protected double sampleExposureTime = SAMPLE_EXPOSURE_TIME_EDEFAULT;

	/**
	 * This is true if the Sample Exposure Time attribute has been set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleExposureTimeESet;

	/**
	 * The default value of the '{@link #getFlatExposureTime() <em>Flat Exposure Time</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getFlatExposureTime()
	 * @generated
	 * @ordered
	 */
	protected static final double FLAT_EXPOSURE_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getFlatExposureTime() <em>Flat Exposure Time</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getFlatExposureTime()
	 * @generated
	 * @ordered
	 */
	protected double flatExposureTime = FLAT_EXPOSURE_TIME_EDEFAULT;

	/**
	 * This is true if the Flat Exposure Time attribute has been set.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean flatExposureTimeESet;

	/**
	 * The default value of the '{@link #getCreatedUserId() <em>Created User Id</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getCreatedUserId()
	 * @generated
	 * @ordered
	 */
	protected static final String CREATED_USER_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreatedUserId() <em>Created User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreatedUserId()
	 * @generated
	 * @ordered
	 */
	protected String createdUserId = CREATED_USER_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getCreatedDateTime() <em>Created Date Time</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getCreatedDateTime()
	 * @generated
	 * @ordered
	 */
	protected static final Date CREATED_DATE_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreatedDateTime() <em>Created Date Time</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getCreatedDateTime()
	 * @generated
	 * @ordered
	 */
	protected Date createdDateTime = CREATED_DATE_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSampleWeight() <em>Sample Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleWeight()
	 * @generated
	 * @ordered
	 */
	protected static final SampleWeight SAMPLE_WEIGHT_EDEFAULT = SampleWeight.LESS_THAN_1;

	/**
	 * The cached value of the '{@link #getSampleWeight() <em>Sample Weight</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleWeight()
	 * @generated
	 * @ordered
	 */
	protected SampleWeight sampleWeight = SAMPLE_WEIGHT_EDEFAULT;

	/**
	 * The default value of the '{@link #getProposalId() <em>Proposal Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getProposalId()
	 * @generated
	 * @ordered
	 */
	protected static final String PROPOSAL_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProposalId() <em>Proposal Id</em>}' attribute.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @see #getProposalId()
	 * @generated
	 * @ordered
	 */
	protected String proposalId = PROPOSAL_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getStitchParameters() <em>Stitch Parameters</em>}' containment reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getStitchParameters()
	 * @generated
	 * @ordered
	 */
	protected StitchParameters stitchParameters;

	/**
	 * This is true if the Stitch Parameters containment reference has been set.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean stitchParametersESet;

	/**
	 * The default value of the '{@link #getSelectedToRun() <em>Selected To Run</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getSelectedToRun()
	 * @generated
	 * @ordered
	 */
	protected static final Boolean SELECTED_TO_RUN_EDEFAULT = Boolean.FALSE;

	/**
	 * The cached value of the '{@link #getSelectedToRun() <em>Selected To Run</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSelectedToRun()
	 * @generated
	 * @ordered
	 */
	protected Boolean selectedToRun = SELECTED_TO_RUN_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMotorPositions() <em>Motor Positions</em>}' containment reference list. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getMotorPositions()
	 * @generated
	 * @ordered
	 */
	protected EList<MotorPosition> motorPositions;

	/**
	 * The default value of the '{@link #getInBeamPosition() <em>In Beam Position</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getInBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected static final double IN_BEAM_POSITION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getInBeamPosition() <em>In Beam Position</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getInBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected double inBeamPosition = IN_BEAM_POSITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutOfBeamPosition() <em>Out Of Beam Position</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getOutOfBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected static final double OUT_OF_BEAM_POSITION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getOutOfBeamPosition() <em>Out Of Beam Position</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getOutOfBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected double outOfBeamPosition = OUT_OF_BEAM_POSITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getTomoRotationAxis() <em>Tomo Rotation Axis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTomoRotationAxis()
	 * @generated
	 * @ordered
	 */
	protected static final Integer TOMO_ROTATION_AXIS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTomoRotationAxis() <em>Tomo Rotation Axis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTomoRotationAxis()
	 * @generated
	 * @ordered
	 */
	protected Integer tomoRotationAxis = TOMO_ROTATION_AXIS_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated not
	 */
	protected AlignmentConfigurationImpl() {
		super();
		setId(EcoreUtil.generateUUID());
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.ALIGNMENT_CONFIGURATION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		boolean oldIdESet = idESet;
		idESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID, oldId, id, !oldIdESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetId() {
		String oldId = id;
		boolean oldIdESet = idESet;
		id = ID_EDEFAULT;
		idESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID, oldId, ID_EDEFAULT, oldIdESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetId() {
		return idESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getEnergy() {
		return energy;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnergy(double newEnergy) {
		double oldEnergy = energy;
		energy = newEnergy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY, oldEnergy, energy));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorProperties getDetectorProperties() {
		return detectorProperties;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDetectorProperties(DetectorProperties newDetectorProperties, NotificationChain msgs) {
		DetectorProperties oldDetectorProperties = detectorProperties;
		detectorProperties = newDetectorProperties;
		boolean oldDetectorPropertiesESet = detectorPropertiesESet;
		detectorPropertiesESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, oldDetectorProperties, newDetectorProperties, !oldDetectorPropertiesESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDetectorProperties(DetectorProperties newDetectorProperties) {
		if (newDetectorProperties != detectorProperties) {
			NotificationChain msgs = null;
			if (detectorProperties != null)
				msgs = ((InternalEObject)detectorProperties).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, null, msgs);
			if (newDetectorProperties != null)
				msgs = ((InternalEObject)newDetectorProperties).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, null, msgs);
			msgs = basicSetDetectorProperties(newDetectorProperties, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorPropertiesESet = detectorPropertiesESet;
			detectorPropertiesESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, newDetectorProperties, newDetectorProperties, !oldDetectorPropertiesESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetDetectorProperties(NotificationChain msgs) {
		DetectorProperties oldDetectorProperties = detectorProperties;
		detectorProperties = null;
		boolean oldDetectorPropertiesESet = detectorPropertiesESet;
		detectorPropertiesESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, oldDetectorProperties, null, oldDetectorPropertiesESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetDetectorProperties() {
		if (detectorProperties != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)detectorProperties).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, null, msgs);
			msgs = basicUnsetDetectorProperties(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorPropertiesESet = detectorPropertiesESet;
			detectorPropertiesESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES, null, null, oldDetectorPropertiesESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDetectorProperties() {
		return detectorPropertiesESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ScanMode getScanMode() {
		return scanMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScanMode(ScanMode newScanMode) {
		ScanMode oldScanMode = scanMode;
		scanMode = newScanMode == null ? SCAN_MODE_EDEFAULT : newScanMode;
		boolean oldScanModeESet = scanModeESet;
		scanModeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE, oldScanMode, scanMode, !oldScanModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetScanMode() {
		ScanMode oldScanMode = scanMode;
		boolean oldScanModeESet = scanModeESet;
		scanMode = SCAN_MODE_EDEFAULT;
		scanModeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE, oldScanMode, SCAN_MODE_EDEFAULT, oldScanModeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetScanMode() {
		return scanModeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSampleExposureTime(double newSampleExposureTime) {
		double oldSampleExposureTime = sampleExposureTime;
		sampleExposureTime = newSampleExposureTime;
		boolean oldSampleExposureTimeESet = sampleExposureTimeESet;
		sampleExposureTimeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME, oldSampleExposureTime, sampleExposureTime, !oldSampleExposureTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetSampleExposureTime() {
		double oldSampleExposureTime = sampleExposureTime;
		boolean oldSampleExposureTimeESet = sampleExposureTimeESet;
		sampleExposureTime = SAMPLE_EXPOSURE_TIME_EDEFAULT;
		sampleExposureTimeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME, oldSampleExposureTime, SAMPLE_EXPOSURE_TIME_EDEFAULT, oldSampleExposureTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSampleExposureTime() {
		return sampleExposureTimeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFlatExposureTime(double newFlatExposureTime) {
		double oldFlatExposureTime = flatExposureTime;
		flatExposureTime = newFlatExposureTime;
		boolean oldFlatExposureTimeESet = flatExposureTimeESet;
		flatExposureTimeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME, oldFlatExposureTime, flatExposureTime, !oldFlatExposureTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetFlatExposureTime() {
		double oldFlatExposureTime = flatExposureTime;
		boolean oldFlatExposureTimeESet = flatExposureTimeESet;
		flatExposureTime = FLAT_EXPOSURE_TIME_EDEFAULT;
		flatExposureTimeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME, oldFlatExposureTime, FLAT_EXPOSURE_TIME_EDEFAULT, oldFlatExposureTimeESet));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetFlatExposureTime() {
		return flatExposureTimeESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreatedUserId() {
		return createdUserId;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCreatedUserId(String newCreatedUserId) {
		String oldCreatedUserId = createdUserId;
		createdUserId = newCreatedUserId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID, oldCreatedUserId, createdUserId));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCreatedDateTime(Date newCreatedDateTime) {
		Date oldCreatedDateTime = createdDateTime;
		createdDateTime = newCreatedDateTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME, oldCreatedDateTime, createdDateTime));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SampleWeight getSampleWeight() {
		return sampleWeight;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSampleWeight(SampleWeight newSampleWeight) {
		SampleWeight oldSampleWeight = sampleWeight;
		sampleWeight = newSampleWeight == null ? SAMPLE_WEIGHT_EDEFAULT : newSampleWeight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT, oldSampleWeight, sampleWeight));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getProposalId() {
		return proposalId;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProposalId(String newProposalId) {
		String oldProposalId = proposalId;
		proposalId = newProposalId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__PROPOSAL_ID, oldProposalId, proposalId));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public StitchParameters getStitchParameters() {
		return stitchParameters;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStitchParameters(StitchParameters newStitchParameters, NotificationChain msgs) {
		StitchParameters oldStitchParameters = stitchParameters;
		stitchParameters = newStitchParameters;
		boolean oldStitchParametersESet = stitchParametersESet;
		stitchParametersESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, oldStitchParameters, newStitchParameters, !oldStitchParametersESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStitchParameters(StitchParameters newStitchParameters) {
		if (newStitchParameters != stitchParameters) {
			NotificationChain msgs = null;
			if (stitchParameters != null)
				msgs = ((InternalEObject)stitchParameters).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, null, msgs);
			if (newStitchParameters != null)
				msgs = ((InternalEObject)newStitchParameters).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, null, msgs);
			msgs = basicSetStitchParameters(newStitchParameters, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldStitchParametersESet = stitchParametersESet;
			stitchParametersESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, newStitchParameters, newStitchParameters, !oldStitchParametersESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetStitchParameters(NotificationChain msgs) {
		StitchParameters oldStitchParameters = stitchParameters;
		stitchParameters = null;
		boolean oldStitchParametersESet = stitchParametersESet;
		stitchParametersESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, oldStitchParameters, null, oldStitchParametersESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetStitchParameters() {
		if (stitchParameters != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)stitchParameters).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, null, msgs);
			msgs = basicUnsetStitchParameters(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldStitchParametersESet = stitchParametersESet;
			stitchParametersESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS, null, null, oldStitchParametersESet));
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetStitchParameters() {
		return stitchParametersESet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Boolean getSelectedToRun() {
		return selectedToRun;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSelectedToRun(Boolean newSelectedToRun) {
		Boolean oldSelectedToRun = selectedToRun;
		selectedToRun = newSelectedToRun;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN, oldSelectedToRun, selectedToRun));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<MotorPosition> getMotorPositions() {
		if (motorPositions == null) {
			motorPositions = new EObjectContainmentEList<MotorPosition>(MotorPosition.class, this, TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS);
		}
		return motorPositions;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getInBeamPosition() {
		return inBeamPosition;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInBeamPosition(double newInBeamPosition) {
		double oldInBeamPosition = inBeamPosition;
		inBeamPosition = newInBeamPosition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION, oldInBeamPosition, inBeamPosition));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getOutOfBeamPosition() {
		return outOfBeamPosition;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOutOfBeamPosition(double newOutOfBeamPosition) {
		double oldOutOfBeamPosition = outOfBeamPosition;
		outOfBeamPosition = newOutOfBeamPosition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION, oldOutOfBeamPosition, outOfBeamPosition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Integer getTomoRotationAxis() {
		return tomoRotationAxis;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTomoRotationAxis(Integer newTomoRotationAxis) {
		Integer oldTomoRotationAxis = tomoRotationAxis;
		tomoRotationAxis = newTomoRotationAxis;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS, oldTomoRotationAxis, tomoRotationAxis));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public Double getMotorPosition(String motorName) {
		for (MotorPosition mp : motorPositions) {
			if (mp.getName().equals(motorName)) {
				return mp.getPosition();
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return basicUnsetDetectorProperties(msgs);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS:
				return basicUnsetStitchParameters(msgs);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS:
				return ((InternalEList<?>)getMotorPositions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				return getId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				return getEnergy();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				return getDescription();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return getDetectorProperties();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				return getScanMode();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				return getSampleExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				return getFlatExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				return getCreatedUserId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				return getCreatedDateTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT:
				return getSampleWeight();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__PROPOSAL_ID:
				return getProposalId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS:
				return getStitchParameters();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN:
				return getSelectedToRun();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS:
				return getMotorPositions();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION:
				return getInBeamPosition();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION:
				return getOutOfBeamPosition();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS:
				return getTomoRotationAxis();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				setId((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				setEnergy((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				setDetectorProperties((DetectorProperties)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				setScanMode((ScanMode)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				setSampleExposureTime((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				setFlatExposureTime((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				setCreatedUserId((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				setCreatedDateTime((Date)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT:
				setSampleWeight((SampleWeight)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__PROPOSAL_ID:
				setProposalId((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS:
				setStitchParameters((StitchParameters)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN:
				setSelectedToRun((Boolean)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS:
				getMotorPositions().clear();
				getMotorPositions().addAll((Collection<? extends MotorPosition>)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION:
				setInBeamPosition((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION:
				setOutOfBeamPosition((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS:
				setTomoRotationAxis((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				unsetId();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				setEnergy(ENERGY_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				unsetDetectorProperties();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				unsetScanMode();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				unsetSampleExposureTime();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				unsetFlatExposureTime();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				setCreatedUserId(CREATED_USER_ID_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				setCreatedDateTime(CREATED_DATE_TIME_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT:
				setSampleWeight(SAMPLE_WEIGHT_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__PROPOSAL_ID:
				setProposalId(PROPOSAL_ID_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS:
				unsetStitchParameters();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN:
				setSelectedToRun(SELECTED_TO_RUN_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS:
				getMotorPositions().clear();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION:
				setInBeamPosition(IN_BEAM_POSITION_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION:
				setOutOfBeamPosition(OUT_OF_BEAM_POSITION_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS:
				setTomoRotationAxis(TOMO_ROTATION_AXIS_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				return isSetId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				return energy != ENERGY_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return isSetDetectorProperties();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				return isSetScanMode();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				return isSetSampleExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				return isSetFlatExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				return CREATED_USER_ID_EDEFAULT == null ? createdUserId != null : !CREATED_USER_ID_EDEFAULT.equals(createdUserId);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				return CREATED_DATE_TIME_EDEFAULT == null ? createdDateTime != null : !CREATED_DATE_TIME_EDEFAULT.equals(createdDateTime);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_WEIGHT:
				return sampleWeight != SAMPLE_WEIGHT_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__PROPOSAL_ID:
				return PROPOSAL_ID_EDEFAULT == null ? proposalId != null : !PROPOSAL_ID_EDEFAULT.equals(proposalId);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCH_PARAMETERS:
				return isSetStitchParameters();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SELECTED_TO_RUN:
				return SELECTED_TO_RUN_EDEFAULT == null ? selectedToRun != null : !SELECTED_TO_RUN_EDEFAULT.equals(selectedToRun);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__MOTOR_POSITIONS:
				return motorPositions != null && !motorPositions.isEmpty();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__IN_BEAM_POSITION:
				return inBeamPosition != IN_BEAM_POSITION_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__OUT_OF_BEAM_POSITION:
				return outOfBeamPosition != OUT_OF_BEAM_POSITION_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__TOMO_ROTATION_AXIS:
				return TOMO_ROTATION_AXIS_EDEFAULT == null ? tomoRotationAxis != null : !TOMO_ROTATION_AXIS_EDEFAULT.equals(tomoRotationAxis);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (id: ");
		if (idESet) result.append(id); else result.append("<unset>");
		result.append(", energy: ");
		result.append(energy);
		result.append(", description: ");
		result.append(description);
		result.append(", scanMode: ");
		if (scanModeESet) result.append(scanMode); else result.append("<unset>");
		result.append(", sampleExposureTime: ");
		if (sampleExposureTimeESet) result.append(sampleExposureTime); else result.append("<unset>");
		result.append(", flatExposureTime: ");
		if (flatExposureTimeESet) result.append(flatExposureTime); else result.append("<unset>");
		result.append(", createdUserId: ");
		result.append(createdUserId);
		result.append(", createdDateTime: ");
		result.append(createdDateTime);
		result.append(", sampleWeight: ");
		result.append(sampleWeight);
		result.append(", proposalId: ");
		result.append(proposalId);
		result.append(", selectedToRun: ");
		result.append(selectedToRun);
		result.append(", inBeamPosition: ");
		result.append(inBeamPosition);
		result.append(", outOfBeamPosition: ");
		result.append(outOfBeamPosition);
		result.append(", tomoRotationAxis: ");
		result.append(tomoRotationAxis);
		result.append(')');
		return result.toString();
	}

} // AlignmentConfigurationImpl
