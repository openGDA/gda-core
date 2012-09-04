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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType17;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Beamline User Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getBeamlineName <em>Beamline Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getYear <em>Year</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getMonth <em>Month</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getDate <em>Date</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getVisitNumber <em>Visit Number</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getInputDataFolder <em>Input Data Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getInputScanFolder <em>Input Scan Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getOutputDataFolder <em>Output Data Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getOutputScanFolder <em>Output Scan Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BeamlineUserTypeImpl#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BeamlineUserTypeImpl extends EObjectImpl implements BeamlineUserType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType17 type;

	/**
	 * The default value of the '{@link #getBeamlineName() <em>Beamline Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBeamlineName()
	 * @generated
	 * @ordered
	 */
	protected static final String BEAMLINE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBeamlineName() <em>Beamline Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBeamlineName()
	 * @generated
	 * @ordered
	 */
	protected String beamlineName = BEAMLINE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getYear() <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYear()
	 * @generated
	 * @ordered
	 */
	protected static final String YEAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getYear() <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYear()
	 * @generated
	 * @ordered
	 */
	protected String year = YEAR_EDEFAULT;

	/**
	 * The default value of the '{@link #getMonth() <em>Month</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMonth()
	 * @generated
	 * @ordered
	 */
	protected static final String MONTH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMonth() <em>Month</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMonth()
	 * @generated
	 * @ordered
	 */
	protected String month = MONTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected static final String DATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected String date = DATE_EDEFAULT;

	/**
	 * The default value of the '{@link #getVisitNumber() <em>Visit Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitNumber()
	 * @generated
	 * @ordered
	 */
	protected static final String VISIT_NUMBER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVisitNumber() <em>Visit Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitNumber()
	 * @generated
	 * @ordered
	 */
	protected String visitNumber = VISIT_NUMBER_EDEFAULT;

	/**
	 * The default value of the '{@link #getInputDataFolder() <em>Input Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputDataFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String INPUT_DATA_FOLDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getInputDataFolder() <em>Input Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputDataFolder()
	 * @generated
	 * @ordered
	 */
	protected String inputDataFolder = INPUT_DATA_FOLDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getInputScanFolder() <em>Input Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputScanFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String INPUT_SCAN_FOLDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getInputScanFolder() <em>Input Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputScanFolder()
	 * @generated
	 * @ordered
	 */
	protected String inputScanFolder = INPUT_SCAN_FOLDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutputDataFolder() <em>Output Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputDataFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String OUTPUT_DATA_FOLDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOutputDataFolder() <em>Output Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputDataFolder()
	 * @generated
	 * @ordered
	 */
	protected String outputDataFolder = OUTPUT_DATA_FOLDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutputScanFolder() <em>Output Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputScanFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String OUTPUT_SCAN_FOLDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOutputScanFolder() <em>Output Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputScanFolder()
	 * @generated
	 * @ordered
	 */
	protected String outputScanFolder = OUTPUT_SCAN_FOLDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getDone() <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDone()
	 * @generated
	 * @ordered
	 */
	protected static final String DONE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDone() <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDone()
	 * @generated
	 * @ordered
	 */
	protected String done = DONE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BeamlineUserTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.BEAMLINE_USER_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType17 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType17 newType, NotificationChain msgs) {
		TypeType17 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType17 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BEAMLINE_USER_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BEAMLINE_USER_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBeamlineName() {
		return beamlineName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBeamlineName(String newBeamlineName) {
		String oldBeamlineName = beamlineName;
		beamlineName = newBeamlineName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__BEAMLINE_NAME, oldBeamlineName, beamlineName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getYear() {
		return year;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYear(String newYear) {
		String oldYear = year;
		year = newYear;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__YEAR, oldYear, year));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMonth() {
		return month;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMonth(String newMonth) {
		String oldMonth = month;
		month = newMonth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__MONTH, oldMonth, month));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDate() {
		return date;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDate(String newDate) {
		String oldDate = date;
		date = newDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__DATE, oldDate, date));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getVisitNumber() {
		return visitNumber;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisitNumber(String newVisitNumber) {
		String oldVisitNumber = visitNumber;
		visitNumber = newVisitNumber;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__VISIT_NUMBER, oldVisitNumber, visitNumber));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getInputDataFolder() {
		return inputDataFolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputDataFolder(String newInputDataFolder) {
		String oldInputDataFolder = inputDataFolder;
		inputDataFolder = newInputDataFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER, oldInputDataFolder, inputDataFolder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getInputScanFolder() {
		return inputScanFolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputScanFolder(String newInputScanFolder) {
		String oldInputScanFolder = inputScanFolder;
		inputScanFolder = newInputScanFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER, oldInputScanFolder, inputScanFolder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOutputDataFolder() {
		return outputDataFolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputDataFolder(String newOutputDataFolder) {
		String oldOutputDataFolder = outputDataFolder;
		outputDataFolder = newOutputDataFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER, oldOutputDataFolder, outputDataFolder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOutputScanFolder() {
		return outputScanFolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputScanFolder(String newOutputScanFolder) {
		String oldOutputScanFolder = outputScanFolder;
		outputScanFolder = newOutputScanFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER, oldOutputScanFolder, outputScanFolder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDone() {
		return done;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDone(String newDone) {
		String oldDone = done;
		done = newDone;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BEAMLINE_USER_TYPE__DONE, oldDone, done));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.BEAMLINE_USER_TYPE__TYPE:
				return basicSetType(null, msgs);
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
			case HmPackage.BEAMLINE_USER_TYPE__TYPE:
				return getType();
			case HmPackage.BEAMLINE_USER_TYPE__BEAMLINE_NAME:
				return getBeamlineName();
			case HmPackage.BEAMLINE_USER_TYPE__YEAR:
				return getYear();
			case HmPackage.BEAMLINE_USER_TYPE__MONTH:
				return getMonth();
			case HmPackage.BEAMLINE_USER_TYPE__DATE:
				return getDate();
			case HmPackage.BEAMLINE_USER_TYPE__VISIT_NUMBER:
				return getVisitNumber();
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER:
				return getInputDataFolder();
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER:
				return getInputScanFolder();
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER:
				return getOutputDataFolder();
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER:
				return getOutputScanFolder();
			case HmPackage.BEAMLINE_USER_TYPE__DONE:
				return getDone();
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
			case HmPackage.BEAMLINE_USER_TYPE__TYPE:
				setType((TypeType17)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__BEAMLINE_NAME:
				setBeamlineName((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__YEAR:
				setYear((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__MONTH:
				setMonth((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__DATE:
				setDate((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__VISIT_NUMBER:
				setVisitNumber((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER:
				setInputDataFolder((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER:
				setInputScanFolder((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER:
				setOutputDataFolder((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER:
				setOutputScanFolder((String)newValue);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__DONE:
				setDone((String)newValue);
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
			case HmPackage.BEAMLINE_USER_TYPE__TYPE:
				setType((TypeType17)null);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__BEAMLINE_NAME:
				setBeamlineName(BEAMLINE_NAME_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__YEAR:
				setYear(YEAR_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__MONTH:
				setMonth(MONTH_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__DATE:
				setDate(DATE_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__VISIT_NUMBER:
				setVisitNumber(VISIT_NUMBER_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER:
				setInputDataFolder(INPUT_DATA_FOLDER_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER:
				setInputScanFolder(INPUT_SCAN_FOLDER_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER:
				setOutputDataFolder(OUTPUT_DATA_FOLDER_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER:
				setOutputScanFolder(OUTPUT_SCAN_FOLDER_EDEFAULT);
				return;
			case HmPackage.BEAMLINE_USER_TYPE__DONE:
				setDone(DONE_EDEFAULT);
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
			case HmPackage.BEAMLINE_USER_TYPE__TYPE:
				return type != null;
			case HmPackage.BEAMLINE_USER_TYPE__BEAMLINE_NAME:
				return BEAMLINE_NAME_EDEFAULT == null ? beamlineName != null : !BEAMLINE_NAME_EDEFAULT.equals(beamlineName);
			case HmPackage.BEAMLINE_USER_TYPE__YEAR:
				return YEAR_EDEFAULT == null ? year != null : !YEAR_EDEFAULT.equals(year);
			case HmPackage.BEAMLINE_USER_TYPE__MONTH:
				return MONTH_EDEFAULT == null ? month != null : !MONTH_EDEFAULT.equals(month);
			case HmPackage.BEAMLINE_USER_TYPE__DATE:
				return DATE_EDEFAULT == null ? date != null : !DATE_EDEFAULT.equals(date);
			case HmPackage.BEAMLINE_USER_TYPE__VISIT_NUMBER:
				return VISIT_NUMBER_EDEFAULT == null ? visitNumber != null : !VISIT_NUMBER_EDEFAULT.equals(visitNumber);
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_DATA_FOLDER:
				return INPUT_DATA_FOLDER_EDEFAULT == null ? inputDataFolder != null : !INPUT_DATA_FOLDER_EDEFAULT.equals(inputDataFolder);
			case HmPackage.BEAMLINE_USER_TYPE__INPUT_SCAN_FOLDER:
				return INPUT_SCAN_FOLDER_EDEFAULT == null ? inputScanFolder != null : !INPUT_SCAN_FOLDER_EDEFAULT.equals(inputScanFolder);
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_DATA_FOLDER:
				return OUTPUT_DATA_FOLDER_EDEFAULT == null ? outputDataFolder != null : !OUTPUT_DATA_FOLDER_EDEFAULT.equals(outputDataFolder);
			case HmPackage.BEAMLINE_USER_TYPE__OUTPUT_SCAN_FOLDER:
				return OUTPUT_SCAN_FOLDER_EDEFAULT == null ? outputScanFolder != null : !OUTPUT_SCAN_FOLDER_EDEFAULT.equals(outputScanFolder);
			case HmPackage.BEAMLINE_USER_TYPE__DONE:
				return DONE_EDEFAULT == null ? done != null : !DONE_EDEFAULT.equals(done);
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
		result.append(" (beamlineName: ");
		result.append(beamlineName);
		result.append(", year: ");
		result.append(year);
		result.append(", month: ");
		result.append(month);
		result.append(", date: ");
		result.append(date);
		result.append(", visitNumber: ");
		result.append(visitNumber);
		result.append(", inputDataFolder: ");
		result.append(inputDataFolder);
		result.append(", inputScanFolder: ");
		result.append(inputScanFolder);
		result.append(", outputDataFolder: ");
		result.append(outputDataFolder);
		result.append(", outputScanFolder: ");
		result.append(outputScanFolder);
		result.append(", done: ");
		result.append(done);
		result.append(')');
		return result.toString();
	}

} //BeamlineUserTypeImpl
