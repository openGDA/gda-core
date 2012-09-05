/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DefaultXmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>FBP Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getDefaultXml <em>Default Xml</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getGPUDeviceNumber <em>GPU Device Number</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getBeamlineUser <em>Beamline User</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getLogFile <em>Log File</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getInputData <em>Input Data</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getFlatDarkFields <em>Flat Dark Fields</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getPreprocessing <em>Preprocessing</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getTransform <em>Transform</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getBackprojection <em>Backprojection</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FBPTypeImpl#getOutputData <em>Output Data</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FBPTypeImpl extends EObjectImpl implements FBPType {
	/**
	 * The cached value of the '{@link #getDefaultXml() <em>Default Xml</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefaultXml()
	 * @generated
	 * @ordered
	 */
	protected DefaultXmlType defaultXml;

	/**
	 * The default value of the '{@link #getGPUDeviceNumber() <em>GPU Device Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGPUDeviceNumber()
	 * @generated
	 * @ordered
	 */
	protected static final int GPU_DEVICE_NUMBER_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGPUDeviceNumber() <em>GPU Device Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGPUDeviceNumber()
	 * @generated
	 * @ordered
	 */
	protected int gPUDeviceNumber = GPU_DEVICE_NUMBER_EDEFAULT;

	/**
	 * This is true if the GPU Device Number attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean gPUDeviceNumberESet;

	/**
	 * The cached value of the '{@link #getBeamlineUser() <em>Beamline User</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBeamlineUser()
	 * @generated
	 * @ordered
	 */
	protected BeamlineUserType beamlineUser;

	/**
	 * The default value of the '{@link #getLogFile() <em>Log File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLogFile()
	 * @generated
	 * @ordered
	 */
	protected static final String LOG_FILE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLogFile() <em>Log File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLogFile()
	 * @generated
	 * @ordered
	 */
	protected String logFile = LOG_FILE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getInputData() <em>Input Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputData()
	 * @generated
	 * @ordered
	 */
	protected InputDataType inputData;

	/**
	 * The cached value of the '{@link #getFlatDarkFields() <em>Flat Dark Fields</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatDarkFields()
	 * @generated
	 * @ordered
	 */
	protected FlatDarkFieldsType flatDarkFields;

	/**
	 * The cached value of the '{@link #getPreprocessing() <em>Preprocessing</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPreprocessing()
	 * @generated
	 * @ordered
	 */
	protected PreprocessingType preprocessing;

	/**
	 * The cached value of the '{@link #getTransform() <em>Transform</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransform()
	 * @generated
	 * @ordered
	 */
	protected TransformType transform;

	/**
	 * The cached value of the '{@link #getBackprojection() <em>Backprojection</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBackprojection()
	 * @generated
	 * @ordered
	 */
	protected BackprojectionType backprojection;

	/**
	 * The cached value of the '{@link #getOutputData() <em>Output Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputData()
	 * @generated
	 * @ordered
	 */
	protected OutputDataType outputData;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FBPTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.FBP_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DefaultXmlType getDefaultXml() {
		return defaultXml;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDefaultXml(DefaultXmlType newDefaultXml, NotificationChain msgs) {
		DefaultXmlType oldDefaultXml = defaultXml;
		defaultXml = newDefaultXml;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__DEFAULT_XML, oldDefaultXml, newDefaultXml);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefaultXml(DefaultXmlType newDefaultXml) {
		if (newDefaultXml != defaultXml) {
			NotificationChain msgs = null;
			if (defaultXml != null)
				msgs = ((InternalEObject)defaultXml).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__DEFAULT_XML, null, msgs);
			if (newDefaultXml != null)
				msgs = ((InternalEObject)newDefaultXml).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__DEFAULT_XML, null, msgs);
			msgs = basicSetDefaultXml(newDefaultXml, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__DEFAULT_XML, newDefaultXml, newDefaultXml));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGPUDeviceNumber() {
		return gPUDeviceNumber;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGPUDeviceNumber(int newGPUDeviceNumber) {
		int oldGPUDeviceNumber = gPUDeviceNumber;
		gPUDeviceNumber = newGPUDeviceNumber;
		boolean oldGPUDeviceNumberESet = gPUDeviceNumberESet;
		gPUDeviceNumberESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER, oldGPUDeviceNumber, gPUDeviceNumber, !oldGPUDeviceNumberESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetGPUDeviceNumber() {
		int oldGPUDeviceNumber = gPUDeviceNumber;
		boolean oldGPUDeviceNumberESet = gPUDeviceNumberESet;
		gPUDeviceNumber = GPU_DEVICE_NUMBER_EDEFAULT;
		gPUDeviceNumberESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER, oldGPUDeviceNumber, GPU_DEVICE_NUMBER_EDEFAULT, oldGPUDeviceNumberESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetGPUDeviceNumber() {
		return gPUDeviceNumberESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BeamlineUserType getBeamlineUser() {
		return beamlineUser;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBeamlineUser(BeamlineUserType newBeamlineUser, NotificationChain msgs) {
		BeamlineUserType oldBeamlineUser = beamlineUser;
		beamlineUser = newBeamlineUser;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__BEAMLINE_USER, oldBeamlineUser, newBeamlineUser);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBeamlineUser(BeamlineUserType newBeamlineUser) {
		if (newBeamlineUser != beamlineUser) {
			NotificationChain msgs = null;
			if (beamlineUser != null)
				msgs = ((InternalEObject)beamlineUser).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__BEAMLINE_USER, null, msgs);
			if (newBeamlineUser != null)
				msgs = ((InternalEObject)newBeamlineUser).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__BEAMLINE_USER, null, msgs);
			msgs = basicSetBeamlineUser(newBeamlineUser, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__BEAMLINE_USER, newBeamlineUser, newBeamlineUser));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLogFile() {
		return logFile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLogFile(String newLogFile) {
		String oldLogFile = logFile;
		logFile = newLogFile;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__LOG_FILE, oldLogFile, logFile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InputDataType getInputData() {
		return inputData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInputData(InputDataType newInputData, NotificationChain msgs) {
		InputDataType oldInputData = inputData;
		inputData = newInputData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__INPUT_DATA, oldInputData, newInputData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputData(InputDataType newInputData) {
		if (newInputData != inputData) {
			NotificationChain msgs = null;
			if (inputData != null)
				msgs = ((InternalEObject)inputData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__INPUT_DATA, null, msgs);
			if (newInputData != null)
				msgs = ((InternalEObject)newInputData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__INPUT_DATA, null, msgs);
			msgs = basicSetInputData(newInputData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__INPUT_DATA, newInputData, newInputData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FlatDarkFieldsType getFlatDarkFields() {
		return flatDarkFields;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFlatDarkFields(FlatDarkFieldsType newFlatDarkFields, NotificationChain msgs) {
		FlatDarkFieldsType oldFlatDarkFields = flatDarkFields;
		flatDarkFields = newFlatDarkFields;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__FLAT_DARK_FIELDS, oldFlatDarkFields, newFlatDarkFields);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFlatDarkFields(FlatDarkFieldsType newFlatDarkFields) {
		if (newFlatDarkFields != flatDarkFields) {
			NotificationChain msgs = null;
			if (flatDarkFields != null)
				msgs = ((InternalEObject)flatDarkFields).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__FLAT_DARK_FIELDS, null, msgs);
			if (newFlatDarkFields != null)
				msgs = ((InternalEObject)newFlatDarkFields).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__FLAT_DARK_FIELDS, null, msgs);
			msgs = basicSetFlatDarkFields(newFlatDarkFields, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__FLAT_DARK_FIELDS, newFlatDarkFields, newFlatDarkFields));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PreprocessingType getPreprocessing() {
		return preprocessing;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPreprocessing(PreprocessingType newPreprocessing, NotificationChain msgs) {
		PreprocessingType oldPreprocessing = preprocessing;
		preprocessing = newPreprocessing;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__PREPROCESSING, oldPreprocessing, newPreprocessing);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPreprocessing(PreprocessingType newPreprocessing) {
		if (newPreprocessing != preprocessing) {
			NotificationChain msgs = null;
			if (preprocessing != null)
				msgs = ((InternalEObject)preprocessing).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__PREPROCESSING, null, msgs);
			if (newPreprocessing != null)
				msgs = ((InternalEObject)newPreprocessing).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__PREPROCESSING, null, msgs);
			msgs = basicSetPreprocessing(newPreprocessing, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__PREPROCESSING, newPreprocessing, newPreprocessing));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TransformType getTransform() {
		return transform;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTransform(TransformType newTransform, NotificationChain msgs) {
		TransformType oldTransform = transform;
		transform = newTransform;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__TRANSFORM, oldTransform, newTransform);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTransform(TransformType newTransform) {
		if (newTransform != transform) {
			NotificationChain msgs = null;
			if (transform != null)
				msgs = ((InternalEObject)transform).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__TRANSFORM, null, msgs);
			if (newTransform != null)
				msgs = ((InternalEObject)newTransform).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__TRANSFORM, null, msgs);
			msgs = basicSetTransform(newTransform, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__TRANSFORM, newTransform, newTransform));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BackprojectionType getBackprojection() {
		return backprojection;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBackprojection(BackprojectionType newBackprojection, NotificationChain msgs) {
		BackprojectionType oldBackprojection = backprojection;
		backprojection = newBackprojection;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__BACKPROJECTION, oldBackprojection, newBackprojection);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBackprojection(BackprojectionType newBackprojection) {
		if (newBackprojection != backprojection) {
			NotificationChain msgs = null;
			if (backprojection != null)
				msgs = ((InternalEObject)backprojection).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__BACKPROJECTION, null, msgs);
			if (newBackprojection != null)
				msgs = ((InternalEObject)newBackprojection).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__BACKPROJECTION, null, msgs);
			msgs = basicSetBackprojection(newBackprojection, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__BACKPROJECTION, newBackprojection, newBackprojection));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OutputDataType getOutputData() {
		return outputData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOutputData(OutputDataType newOutputData, NotificationChain msgs) {
		OutputDataType oldOutputData = outputData;
		outputData = newOutputData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__OUTPUT_DATA, oldOutputData, newOutputData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputData(OutputDataType newOutputData) {
		if (newOutputData != outputData) {
			NotificationChain msgs = null;
			if (outputData != null)
				msgs = ((InternalEObject)outputData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__OUTPUT_DATA, null, msgs);
			if (newOutputData != null)
				msgs = ((InternalEObject)newOutputData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FBP_TYPE__OUTPUT_DATA, null, msgs);
			msgs = basicSetOutputData(newOutputData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FBP_TYPE__OUTPUT_DATA, newOutputData, newOutputData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.FBP_TYPE__DEFAULT_XML:
				return basicSetDefaultXml(null, msgs);
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
				return basicSetBeamlineUser(null, msgs);
			case HmPackage.FBP_TYPE__INPUT_DATA:
				return basicSetInputData(null, msgs);
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
				return basicSetFlatDarkFields(null, msgs);
			case HmPackage.FBP_TYPE__PREPROCESSING:
				return basicSetPreprocessing(null, msgs);
			case HmPackage.FBP_TYPE__TRANSFORM:
				return basicSetTransform(null, msgs);
			case HmPackage.FBP_TYPE__BACKPROJECTION:
				return basicSetBackprojection(null, msgs);
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				return basicSetOutputData(null, msgs);
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
			case HmPackage.FBP_TYPE__DEFAULT_XML:
				return getDefaultXml();
			case HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER:
				return getGPUDeviceNumber();
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
				return getBeamlineUser();
			case HmPackage.FBP_TYPE__LOG_FILE:
				return getLogFile();
			case HmPackage.FBP_TYPE__INPUT_DATA:
				return getInputData();
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
				return getFlatDarkFields();
			case HmPackage.FBP_TYPE__PREPROCESSING:
				return getPreprocessing();
			case HmPackage.FBP_TYPE__TRANSFORM:
				return getTransform();
			case HmPackage.FBP_TYPE__BACKPROJECTION:
				return getBackprojection();
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				return getOutputData();
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
			case HmPackage.FBP_TYPE__DEFAULT_XML:
				setDefaultXml((DefaultXmlType)newValue);
				return;
			case HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER:
				setGPUDeviceNumber((Integer)newValue);
				return;
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
				setBeamlineUser((BeamlineUserType)newValue);
				return;
			case HmPackage.FBP_TYPE__LOG_FILE:
				setLogFile((String)newValue);
				return;
			case HmPackage.FBP_TYPE__INPUT_DATA:
				setInputData((InputDataType)newValue);
				return;
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
				setFlatDarkFields((FlatDarkFieldsType)newValue);
				return;
			case HmPackage.FBP_TYPE__PREPROCESSING:
				setPreprocessing((PreprocessingType)newValue);
				return;
			case HmPackage.FBP_TYPE__TRANSFORM:
				setTransform((TransformType)newValue);
				return;
			case HmPackage.FBP_TYPE__BACKPROJECTION:
				setBackprojection((BackprojectionType)newValue);
				return;
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				setOutputData((OutputDataType)newValue);
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
			case HmPackage.FBP_TYPE__DEFAULT_XML:
				setDefaultXml((DefaultXmlType)null);
				return;
			case HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER:
				unsetGPUDeviceNumber();
				return;
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
				setBeamlineUser((BeamlineUserType)null);
				return;
			case HmPackage.FBP_TYPE__LOG_FILE:
				setLogFile(LOG_FILE_EDEFAULT);
				return;
			case HmPackage.FBP_TYPE__INPUT_DATA:
				setInputData((InputDataType)null);
				return;
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
				setFlatDarkFields((FlatDarkFieldsType)null);
				return;
			case HmPackage.FBP_TYPE__PREPROCESSING:
				setPreprocessing((PreprocessingType)null);
				return;
			case HmPackage.FBP_TYPE__TRANSFORM:
				setTransform((TransformType)null);
				return;
			case HmPackage.FBP_TYPE__BACKPROJECTION:
				setBackprojection((BackprojectionType)null);
				return;
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				setOutputData((OutputDataType)null);
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
			case HmPackage.FBP_TYPE__DEFAULT_XML:
				return defaultXml != null;
			case HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER:
				return isSetGPUDeviceNumber();
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
				return beamlineUser != null;
			case HmPackage.FBP_TYPE__LOG_FILE:
				return LOG_FILE_EDEFAULT == null ? logFile != null : !LOG_FILE_EDEFAULT.equals(logFile);
			case HmPackage.FBP_TYPE__INPUT_DATA:
				return inputData != null;
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
				return flatDarkFields != null;
			case HmPackage.FBP_TYPE__PREPROCESSING:
				return preprocessing != null;
			case HmPackage.FBP_TYPE__TRANSFORM:
				return transform != null;
			case HmPackage.FBP_TYPE__BACKPROJECTION:
				return backprojection != null;
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				return outputData != null;
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
		result.append(" (gPUDeviceNumber: ");
		if (gPUDeviceNumberESet) result.append(gPUDeviceNumber); else result.append("<unset>");
		result.append(", logFile: ");
		result.append(logFile);
		result.append(')');
		return result.toString();
	}

} //FBPTypeImpl
