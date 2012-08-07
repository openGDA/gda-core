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

import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Detector Properties</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getDesired3DResolution <em>Desired3 DResolution</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getNumberOfFramerPerProjection <em>Number Of Framer Per Projection</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getAcquisitionTimeDivider <em>Acquisition Time Divider</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getDetectorRoi <em>Detector Roi</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getDetectorBin <em>Detector Bin</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorPropertiesImpl#getModuleParameters <em>Module Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DetectorPropertiesImpl extends EObjectImpl implements DetectorProperties {
	/**
	 * The default value of the '{@link #getDesired3DResolution() <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDesired3DResolution()
	 * @generated
	 * @ordered
	 */
	protected static final Resolution DESIRED3_DRESOLUTION_EDEFAULT = Resolution.FULL;

	/**
	 * The cached value of the '{@link #getDesired3DResolution() <em>Desired3 DResolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDesired3DResolution()
	 * @generated
	 * @ordered
	 */
	protected Resolution desired3DResolution = DESIRED3_DRESOLUTION_EDEFAULT;

	/**
	 * This is true if the Desired3 DResolution attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean desired3DResolutionESet;

	/**
	 * The default value of the '{@link #getNumberOfFramerPerProjection() <em>Number Of Framer Per Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfFramerPerProjection()
	 * @generated
	 * @ordered
	 */
	protected static final Integer NUMBER_OF_FRAMER_PER_PROJECTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNumberOfFramerPerProjection() <em>Number Of Framer Per Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfFramerPerProjection()
	 * @generated
	 * @ordered
	 */
	protected Integer numberOfFramerPerProjection = NUMBER_OF_FRAMER_PER_PROJECTION_EDEFAULT;

	/**
	 * This is true if the Number Of Framer Per Projection attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean numberOfFramerPerProjectionESet;

	/**
	 * The default value of the '{@link #getAcquisitionTimeDivider() <em>Acquisition Time Divider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAcquisitionTimeDivider()
	 * @generated
	 * @ordered
	 */
	protected static final Integer ACQUISITION_TIME_DIVIDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAcquisitionTimeDivider() <em>Acquisition Time Divider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAcquisitionTimeDivider()
	 * @generated
	 * @ordered
	 */
	protected Integer acquisitionTimeDivider = ACQUISITION_TIME_DIVIDER_EDEFAULT;

	/**
	 * This is true if the Acquisition Time Divider attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean acquisitionTimeDividerESet;

	/**
	 * The cached value of the '{@link #getDetectorRoi() <em>Detector Roi</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetectorRoi()
	 * @generated
	 * @ordered
	 */
	protected DetectorRoi detectorRoi;

	/**
	 * This is true if the Detector Roi containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean detectorRoiESet;

	/**
	 * The cached value of the '{@link #getDetectorBin() <em>Detector Bin</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetectorBin()
	 * @generated
	 * @ordered
	 */
	protected DetectorBin detectorBin;

	/**
	 * This is true if the Detector Bin containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean detectorBinESet;

	/**
	 * The cached value of the '{@link #getModuleParameters() <em>Module Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModuleParameters()
	 * @generated
	 * @ordered
	 */
	protected Module moduleParameters;

	/**
	 * This is true if the Module Parameters containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean moduleParametersESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DetectorPropertiesImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.DETECTOR_PROPERTIES;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resolution getDesired3DResolution() {
		return desired3DResolution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDesired3DResolution(Resolution newDesired3DResolution) {
		Resolution oldDesired3DResolution = desired3DResolution;
		desired3DResolution = newDesired3DResolution == null ? DESIRED3_DRESOLUTION_EDEFAULT : newDesired3DResolution;
		boolean oldDesired3DResolutionESet = desired3DResolutionESet;
		desired3DResolutionESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION, oldDesired3DResolution, desired3DResolution, !oldDesired3DResolutionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetDesired3DResolution() {
		Resolution oldDesired3DResolution = desired3DResolution;
		boolean oldDesired3DResolutionESet = desired3DResolutionESet;
		desired3DResolution = DESIRED3_DRESOLUTION_EDEFAULT;
		desired3DResolutionESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION, oldDesired3DResolution, DESIRED3_DRESOLUTION_EDEFAULT, oldDesired3DResolutionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDesired3DResolution() {
		return desired3DResolutionESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getNumberOfFramerPerProjection() {
		return numberOfFramerPerProjection;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNumberOfFramerPerProjection(Integer newNumberOfFramerPerProjection) {
		Integer oldNumberOfFramerPerProjection = numberOfFramerPerProjection;
		numberOfFramerPerProjection = newNumberOfFramerPerProjection;
		boolean oldNumberOfFramerPerProjectionESet = numberOfFramerPerProjectionESet;
		numberOfFramerPerProjectionESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION, oldNumberOfFramerPerProjection, numberOfFramerPerProjection, !oldNumberOfFramerPerProjectionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetNumberOfFramerPerProjection() {
		Integer oldNumberOfFramerPerProjection = numberOfFramerPerProjection;
		boolean oldNumberOfFramerPerProjectionESet = numberOfFramerPerProjectionESet;
		numberOfFramerPerProjection = NUMBER_OF_FRAMER_PER_PROJECTION_EDEFAULT;
		numberOfFramerPerProjectionESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION, oldNumberOfFramerPerProjection, NUMBER_OF_FRAMER_PER_PROJECTION_EDEFAULT, oldNumberOfFramerPerProjectionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetNumberOfFramerPerProjection() {
		return numberOfFramerPerProjectionESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getAcquisitionTimeDivider() {
		return acquisitionTimeDivider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAcquisitionTimeDivider(Integer newAcquisitionTimeDivider) {
		Integer oldAcquisitionTimeDivider = acquisitionTimeDivider;
		acquisitionTimeDivider = newAcquisitionTimeDivider;
		boolean oldAcquisitionTimeDividerESet = acquisitionTimeDividerESet;
		acquisitionTimeDividerESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER, oldAcquisitionTimeDivider, acquisitionTimeDivider, !oldAcquisitionTimeDividerESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetAcquisitionTimeDivider() {
		Integer oldAcquisitionTimeDivider = acquisitionTimeDivider;
		boolean oldAcquisitionTimeDividerESet = acquisitionTimeDividerESet;
		acquisitionTimeDivider = ACQUISITION_TIME_DIVIDER_EDEFAULT;
		acquisitionTimeDividerESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER, oldAcquisitionTimeDivider, ACQUISITION_TIME_DIVIDER_EDEFAULT, oldAcquisitionTimeDividerESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetAcquisitionTimeDivider() {
		return acquisitionTimeDividerESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorRoi getDetectorRoi() {
		return detectorRoi;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDetectorRoi(DetectorRoi newDetectorRoi, NotificationChain msgs) {
		DetectorRoi oldDetectorRoi = detectorRoi;
		detectorRoi = newDetectorRoi;
		boolean oldDetectorRoiESet = detectorRoiESet;
		detectorRoiESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, oldDetectorRoi, newDetectorRoi, !oldDetectorRoiESet);
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
	public void setDetectorRoi(DetectorRoi newDetectorRoi) {
		if (newDetectorRoi != detectorRoi) {
			NotificationChain msgs = null;
			if (detectorRoi != null)
				msgs = ((InternalEObject)detectorRoi).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, null, msgs);
			if (newDetectorRoi != null)
				msgs = ((InternalEObject)newDetectorRoi).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, null, msgs);
			msgs = basicSetDetectorRoi(newDetectorRoi, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorRoiESet = detectorRoiESet;
			detectorRoiESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, newDetectorRoi, newDetectorRoi, !oldDetectorRoiESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetDetectorRoi(NotificationChain msgs) {
		DetectorRoi oldDetectorRoi = detectorRoi;
		detectorRoi = null;
		boolean oldDetectorRoiESet = detectorRoiESet;
		detectorRoiESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, oldDetectorRoi, null, oldDetectorRoiESet);
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
	public void unsetDetectorRoi() {
		if (detectorRoi != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)detectorRoi).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, null, msgs);
			msgs = basicUnsetDetectorRoi(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorRoiESet = detectorRoiESet;
			detectorRoiESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI, null, null, oldDetectorRoiESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDetectorRoi() {
		return detectorRoiESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DetectorBin getDetectorBin() {
		return detectorBin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDetectorBin(DetectorBin newDetectorBin, NotificationChain msgs) {
		DetectorBin oldDetectorBin = detectorBin;
		detectorBin = newDetectorBin;
		boolean oldDetectorBinESet = detectorBinESet;
		detectorBinESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, oldDetectorBin, newDetectorBin, !oldDetectorBinESet);
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
	public void setDetectorBin(DetectorBin newDetectorBin) {
		if (newDetectorBin != detectorBin) {
			NotificationChain msgs = null;
			if (detectorBin != null)
				msgs = ((InternalEObject)detectorBin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, null, msgs);
			if (newDetectorBin != null)
				msgs = ((InternalEObject)newDetectorBin).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, null, msgs);
			msgs = basicSetDetectorBin(newDetectorBin, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorBinESet = detectorBinESet;
			detectorBinESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, newDetectorBin, newDetectorBin, !oldDetectorBinESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetDetectorBin(NotificationChain msgs) {
		DetectorBin oldDetectorBin = detectorBin;
		detectorBin = null;
		boolean oldDetectorBinESet = detectorBinESet;
		detectorBinESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, oldDetectorBin, null, oldDetectorBinESet);
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
	public void unsetDetectorBin() {
		if (detectorBin != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)detectorBin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, null, msgs);
			msgs = basicUnsetDetectorBin(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldDetectorBinESet = detectorBinESet;
			detectorBinESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN, null, null, oldDetectorBinESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetDetectorBin() {
		return detectorBinESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Module getModuleParameters() {
		return moduleParameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetModuleParameters(Module newModuleParameters, NotificationChain msgs) {
		Module oldModuleParameters = moduleParameters;
		moduleParameters = newModuleParameters;
		boolean oldModuleParametersESet = moduleParametersESet;
		moduleParametersESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, oldModuleParameters, newModuleParameters, !oldModuleParametersESet);
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
	public void setModuleParameters(Module newModuleParameters) {
		if (newModuleParameters != moduleParameters) {
			NotificationChain msgs = null;
			if (moduleParameters != null)
				msgs = ((InternalEObject)moduleParameters).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, null, msgs);
			if (newModuleParameters != null)
				msgs = ((InternalEObject)newModuleParameters).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, null, msgs);
			msgs = basicSetModuleParameters(newModuleParameters, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldModuleParametersESet = moduleParametersESet;
			moduleParametersESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, newModuleParameters, newModuleParameters, !oldModuleParametersESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetModuleParameters(NotificationChain msgs) {
		Module oldModuleParameters = moduleParameters;
		moduleParameters = null;
		boolean oldModuleParametersESet = moduleParametersESet;
		moduleParametersESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, oldModuleParameters, null, oldModuleParametersESet);
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
	public void unsetModuleParameters() {
		if (moduleParameters != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)moduleParameters).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, null, msgs);
			msgs = basicUnsetModuleParameters(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldModuleParametersESet = moduleParametersESet;
			moduleParametersESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS, null, null, oldModuleParametersESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetModuleParameters() {
		return moduleParametersESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI:
				return basicUnsetDetectorRoi(msgs);
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN:
				return basicUnsetDetectorBin(msgs);
			case TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS:
				return basicUnsetModuleParameters(msgs);
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
			case TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION:
				return getDesired3DResolution();
			case TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION:
				return getNumberOfFramerPerProjection();
			case TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER:
				return getAcquisitionTimeDivider();
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI:
				return getDetectorRoi();
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN:
				return getDetectorBin();
			case TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS:
				return getModuleParameters();
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
			case TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION:
				setDesired3DResolution((Resolution)newValue);
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION:
				setNumberOfFramerPerProjection((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER:
				setAcquisitionTimeDivider((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI:
				setDetectorRoi((DetectorRoi)newValue);
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN:
				setDetectorBin((DetectorBin)newValue);
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS:
				setModuleParameters((Module)newValue);
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
			case TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION:
				unsetDesired3DResolution();
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION:
				unsetNumberOfFramerPerProjection();
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER:
				unsetAcquisitionTimeDivider();
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI:
				unsetDetectorRoi();
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN:
				unsetDetectorBin();
				return;
			case TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS:
				unsetModuleParameters();
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
			case TomoParametersPackage.DETECTOR_PROPERTIES__DESIRED3_DRESOLUTION:
				return isSetDesired3DResolution();
			case TomoParametersPackage.DETECTOR_PROPERTIES__NUMBER_OF_FRAMER_PER_PROJECTION:
				return isSetNumberOfFramerPerProjection();
			case TomoParametersPackage.DETECTOR_PROPERTIES__ACQUISITION_TIME_DIVIDER:
				return isSetAcquisitionTimeDivider();
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_ROI:
				return isSetDetectorRoi();
			case TomoParametersPackage.DETECTOR_PROPERTIES__DETECTOR_BIN:
				return isSetDetectorBin();
			case TomoParametersPackage.DETECTOR_PROPERTIES__MODULE_PARAMETERS:
				return isSetModuleParameters();
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
		result.append(" (desired3DResolution: ");
		if (desired3DResolutionESet) result.append(desired3DResolution); else result.append("<unset>");
		result.append(", numberOfFramerPerProjection: ");
		if (numberOfFramerPerProjectionESet) result.append(numberOfFramerPerProjection); else result.append("<unset>");
		result.append(", acquisitionTimeDivider: ");
		if (acquisitionTimeDividerESet) result.append(acquisitionTimeDivider); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //DetectorPropertiesImpl
