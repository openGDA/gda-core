/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.SampleParams;
import uk.ac.gda.tomography.parameters.ScanMode;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Alignment Configuration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getId <em>Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getEnergy <em>Energy</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getNumberOfProjections <em>Number Of Projections</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getDetectorProperties <em>Detector Properties</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSampleDetectorDistance <em>Sample Detector Distance</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSampleParams <em>Sample Params</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getScanMode <em>Scan Mode</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getSampleExposureTime <em>Sample Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getFlatExposureTime <em>Flat Exposure Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getCreatedUserId <em>Created User Id</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getStitchingThetaAngle <em>Stitching Theta Angle</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.AlignmentConfigurationImpl#getCreatedDateTime <em>Created Date Time</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AlignmentConfigurationImpl extends EObjectImpl implements AlignmentConfiguration {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * This is true if the Id attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean idESet;

	/**
	 * The default value of the '{@link #getEnergy() <em>Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected static final double ENERGY_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getEnergy() <em>Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnergy()
	 * @generated
	 * @ordered
	 */
	protected double energy = ENERGY_EDEFAULT;

	/**
	 * The default value of the '{@link #getNumberOfProjections() <em>Number Of Projections</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfProjections()
	 * @generated
	 * @ordered
	 */
	protected static final Integer NUMBER_OF_PROJECTIONS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNumberOfProjections() <em>Number Of Projections</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfProjections()
	 * @generated
	 * @ordered
	 */
	protected Integer numberOfProjections = NUMBER_OF_PROJECTIONS_EDEFAULT;

	/**
	 * This is true if the Number Of Projections attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean numberOfProjectionsESet;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getDetectorProperties() <em>Detector Properties</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetectorProperties()
	 * @generated
	 * @ordered
	 */
	protected DetectorProperties detectorProperties;

	/**
	 * This is true if the Detector Properties containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean detectorPropertiesESet;

	/**
	 * The default value of the '{@link #getSampleDetectorDistance() <em>Sample Detector Distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleDetectorDistance()
	 * @generated
	 * @ordered
	 */
	protected static final double SAMPLE_DETECTOR_DISTANCE_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getSampleDetectorDistance() <em>Sample Detector Distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleDetectorDistance()
	 * @generated
	 * @ordered
	 */
	protected double sampleDetectorDistance = SAMPLE_DETECTOR_DISTANCE_EDEFAULT;

	/**
	 * This is true if the Sample Detector Distance attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleDetectorDistanceESet;

	/**
	 * The cached value of the '{@link #getSampleParams() <em>Sample Params</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleParams()
	 * @generated
	 * @ordered
	 */
	protected SampleParams sampleParams;

	/**
	 * This is true if the Sample Params containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleParamsESet;

	/**
	 * The default value of the '{@link #getScanMode() <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScanMode()
	 * @generated
	 * @ordered
	 */
	protected static final ScanMode SCAN_MODE_EDEFAULT = ScanMode.CONTINUOUS;

	/**
	 * The cached value of the '{@link #getScanMode() <em>Scan Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScanMode()
	 * @generated
	 * @ordered
	 */
	protected ScanMode scanMode = SCAN_MODE_EDEFAULT;

	/**
	 * This is true if the Scan Mode attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean scanModeESet;

	/**
	 * The default value of the '{@link #getSampleExposureTime() <em>Sample Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleExposureTime()
	 * @generated
	 * @ordered
	 */
	protected static final double SAMPLE_EXPOSURE_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getSampleExposureTime() <em>Sample Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleExposureTime()
	 * @generated
	 * @ordered
	 */
	protected double sampleExposureTime = SAMPLE_EXPOSURE_TIME_EDEFAULT;

	/**
	 * This is true if the Sample Exposure Time attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleExposureTimeESet;

	/**
	 * The default value of the '{@link #getFlatExposureTime() <em>Flat Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatExposureTime()
	 * @generated
	 * @ordered
	 */
	protected static final double FLAT_EXPOSURE_TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getFlatExposureTime() <em>Flat Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatExposureTime()
	 * @generated
	 * @ordered
	 */
	protected double flatExposureTime = FLAT_EXPOSURE_TIME_EDEFAULT;

	/**
	 * This is true if the Flat Exposure Time attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean flatExposureTimeESet;

	/**
	 * The default value of the '{@link #getCreatedUserId() <em>Created User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * The default value of the '{@link #getStitchingThetaAngle() <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStitchingThetaAngle()
	 * @generated
	 * @ordered
	 */
	protected static final double STITCHING_THETA_ANGLE_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getStitchingThetaAngle() <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStitchingThetaAngle()
	 * @generated
	 * @ordered
	 */
	protected double stitchingThetaAngle = STITCHING_THETA_ANGLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCreatedDateTime() <em>Created Date Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreatedDateTime()
	 * @generated
	 * @ordered
	 */
	protected static final String CREATED_DATE_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreatedDateTime() <em>Created Date Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreatedDateTime()
	 * @generated
	 * @ordered
	 */
	protected String createdDateTime = CREATED_DATE_TIME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AlignmentConfigurationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.ALIGNMENT_CONFIGURATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetId() {
		return idESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getEnergy() {
		return energy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getNumberOfProjections() {
		return numberOfProjections;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNumberOfProjections(Integer newNumberOfProjections) {
		Integer oldNumberOfProjections = numberOfProjections;
		numberOfProjections = newNumberOfProjections;
		boolean oldNumberOfProjectionsESet = numberOfProjectionsESet;
		numberOfProjectionsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS, oldNumberOfProjections, numberOfProjections, !oldNumberOfProjectionsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetNumberOfProjections() {
		Integer oldNumberOfProjections = numberOfProjections;
		boolean oldNumberOfProjectionsESet = numberOfProjectionsESet;
		numberOfProjections = NUMBER_OF_PROJECTIONS_EDEFAULT;
		numberOfProjectionsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS, oldNumberOfProjections, NUMBER_OF_PROJECTIONS_EDEFAULT, oldNumberOfProjectionsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetNumberOfProjections() {
		return numberOfProjectionsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorProperties getDetectorProperties() {
		return detectorProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDetectorProperties() {
		return detectorPropertiesESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getSampleDetectorDistance() {
		return sampleDetectorDistance;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSampleDetectorDistance(double newSampleDetectorDistance) {
		double oldSampleDetectorDistance = sampleDetectorDistance;
		sampleDetectorDistance = newSampleDetectorDistance;
		boolean oldSampleDetectorDistanceESet = sampleDetectorDistanceESet;
		sampleDetectorDistanceESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE, oldSampleDetectorDistance, sampleDetectorDistance, !oldSampleDetectorDistanceESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetSampleDetectorDistance() {
		double oldSampleDetectorDistance = sampleDetectorDistance;
		boolean oldSampleDetectorDistanceESet = sampleDetectorDistanceESet;
		sampleDetectorDistance = SAMPLE_DETECTOR_DISTANCE_EDEFAULT;
		sampleDetectorDistanceESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE, oldSampleDetectorDistance, SAMPLE_DETECTOR_DISTANCE_EDEFAULT, oldSampleDetectorDistanceESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSampleDetectorDistance() {
		return sampleDetectorDistanceESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SampleParams getSampleParams() {
		return sampleParams;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSampleParams(SampleParams newSampleParams, NotificationChain msgs) {
		SampleParams oldSampleParams = sampleParams;
		sampleParams = newSampleParams;
		boolean oldSampleParamsESet = sampleParamsESet;
		sampleParamsESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, oldSampleParams, newSampleParams, !oldSampleParamsESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSampleParams(SampleParams newSampleParams) {
		if (newSampleParams != sampleParams) {
			NotificationChain msgs = null;
			if (sampleParams != null)
				msgs = ((InternalEObject)sampleParams).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, null, msgs);
			if (newSampleParams != null)
				msgs = ((InternalEObject)newSampleParams).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, null, msgs);
			msgs = basicSetSampleParams(newSampleParams, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldSampleParamsESet = sampleParamsESet;
			sampleParamsESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, newSampleParams, newSampleParams, !oldSampleParamsESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetSampleParams(NotificationChain msgs) {
		SampleParams oldSampleParams = sampleParams;
		sampleParams = null;
		boolean oldSampleParamsESet = sampleParamsESet;
		sampleParamsESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, oldSampleParams, null, oldSampleParamsESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetSampleParams() {
		if (sampleParams != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)sampleParams).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, null, msgs);
			msgs = basicUnsetSampleParams(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldSampleParamsESet = sampleParamsESet;
			sampleParamsESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS, null, null, oldSampleParamsESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSampleParams() {
		return sampleParamsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ScanMode getScanMode() {
		return scanMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetScanMode() {
		return scanModeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSampleExposureTime() {
		return sampleExposureTimeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetFlatExposureTime() {
		return flatExposureTimeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCreatedUserId() {
		return createdUserId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCreatedUserId(String newCreatedUserId) {
		String oldCreatedUserId = createdUserId;
		createdUserId = newCreatedUserId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID, oldCreatedUserId, createdUserId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getStitchingThetaAngle() {
		return stitchingThetaAngle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStitchingThetaAngle(double newStitchingThetaAngle) {
		double oldStitchingThetaAngle = stitchingThetaAngle;
		stitchingThetaAngle = newStitchingThetaAngle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCHING_THETA_ANGLE, oldStitchingThetaAngle, stitchingThetaAngle));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCreatedDateTime() {
		return createdDateTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCreatedDateTime(String newCreatedDateTime) {
		String oldCreatedDateTime = createdDateTime;
		createdDateTime = newCreatedDateTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME, oldCreatedDateTime, createdDateTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return basicUnsetDetectorProperties(msgs);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS:
				return basicUnsetSampleParams(msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				return getId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				return getEnergy();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS:
				return getNumberOfProjections();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				return getDescription();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return getDetectorProperties();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE:
				return getSampleDetectorDistance();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS:
				return getSampleParams();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				return getScanMode();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				return getSampleExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				return getFlatExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				return getCreatedUserId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCHING_THETA_ANGLE:
				return getStitchingThetaAngle();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				return getCreatedDateTime();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				setId((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				setEnergy((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS:
				setNumberOfProjections((Integer)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				setDetectorProperties((DetectorProperties)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE:
				setSampleDetectorDistance((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS:
				setSampleParams((SampleParams)newValue);
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
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCHING_THETA_ANGLE:
				setStitchingThetaAngle((Double)newValue);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				setCreatedDateTime((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS:
				unsetNumberOfProjections();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				unsetDetectorProperties();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE:
				unsetSampleDetectorDistance();
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS:
				unsetSampleParams();
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
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCHING_THETA_ANGLE:
				setStitchingThetaAngle(STITCHING_THETA_ANGLE_EDEFAULT);
				return;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				setCreatedDateTime(CREATED_DATE_TIME_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ID:
				return isSetId();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__ENERGY:
				return energy != ENERGY_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__NUMBER_OF_PROJECTIONS:
				return isSetNumberOfProjections();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__DETECTOR_PROPERTIES:
				return isSetDetectorProperties();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_DETECTOR_DISTANCE:
				return isSetSampleDetectorDistance();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_PARAMS:
				return isSetSampleParams();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SCAN_MODE:
				return isSetScanMode();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__SAMPLE_EXPOSURE_TIME:
				return isSetSampleExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__FLAT_EXPOSURE_TIME:
				return isSetFlatExposureTime();
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_USER_ID:
				return CREATED_USER_ID_EDEFAULT == null ? createdUserId != null : !CREATED_USER_ID_EDEFAULT.equals(createdUserId);
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__STITCHING_THETA_ANGLE:
				return stitchingThetaAngle != STITCHING_THETA_ANGLE_EDEFAULT;
			case TomoParametersPackage.ALIGNMENT_CONFIGURATION__CREATED_DATE_TIME:
				return CREATED_DATE_TIME_EDEFAULT == null ? createdDateTime != null : !CREATED_DATE_TIME_EDEFAULT.equals(createdDateTime);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
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
		result.append(", numberOfProjections: ");
		if (numberOfProjectionsESet) result.append(numberOfProjections); else result.append("<unset>");
		result.append(", description: ");
		result.append(description);
		result.append(", sampleDetectorDistance: ");
		if (sampleDetectorDistanceESet) result.append(sampleDetectorDistance); else result.append("<unset>");
		result.append(", scanMode: ");
		if (scanModeESet) result.append(scanMode); else result.append("<unset>");
		result.append(", sampleExposureTime: ");
		if (sampleExposureTimeESet) result.append(sampleExposureTime); else result.append("<unset>");
		result.append(", flatExposureTime: ");
		if (flatExposureTimeESet) result.append(flatExposureTime); else result.append("<unset>");
		result.append(", createdUserId: ");
		result.append(createdUserId);
		result.append(", stitchingThetaAngle: ");
		result.append(stitchingThetaAngle);
		result.append(", createdDateTime: ");
		result.append(createdDateTime);
		result.append(')');
		return result.toString();
	}

} //AlignmentConfigurationImpl
