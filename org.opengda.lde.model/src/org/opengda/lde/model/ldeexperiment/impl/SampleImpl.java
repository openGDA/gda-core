/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getComment <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleImpl extends MinimalEObjectImpl.Container implements Sample {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSampleID() <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleID()
	 * @generated
	 * @ordered
	 */
	protected static final String SAMPLE_ID_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getSampleID() <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleID()
	 * @generated
	 * @ordered
	 */
	protected String sampleID = SAMPLE_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final STATUS STATUS_EDEFAULT = STATUS.READY;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected STATUS status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean enabled = ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #getCellID() <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCellID()
	 * @generated
	 * @ordered
	 */
	protected static final String CELL_ID_EDEFAULT = "i11-0";

	/**
	 * The cached value of the '{@link #getCellID() <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCellID()
	 * @generated
	 * @ordered
	 */
	protected String cellID = CELL_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated
	 * @ordered
	 */
	protected static final String VISIT_ID_EDEFAULT = "0-0";

	/**
	 * The cached value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated
	 * @ordered
	 */
	protected String visitID = VISIT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected static final String EMAIL_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected String email = EMAIL_EDEFAULT;

	/**
	 * The default value of the '{@link #getCommand() <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommand() <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected String command = COMMAND_EDEFAULT;

	/**
	 * The default value of the '{@link #getComment() <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComment() <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected String comment = COMMENT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SampleImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LDEExperimentsPackage.Literals.SAMPLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSampleID() {
		return sampleID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSampleID(String newSampleID) {
		String oldSampleID = sampleID;
		sampleID = newSampleID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_ID, oldSampleID, sampleID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStatus(STATUS newStatus) {
		STATUS oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnabled(boolean newEnabled) {
		boolean oldEnabled = enabled;
		enabled = newEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__ENABLED, oldEnabled, enabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCellID() {
		return cellID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCellID(String newCellID) {
		String oldCellID = cellID;
		cellID = newCellID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CELL_ID, oldCellID, cellID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getVisitID() {
		return visitID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisitID(String newVisitID) {
		String oldVisitID = visitID;
		visitID = newVisitID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__VISIT_ID, oldVisitID, visitID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEmail(String newEmail) {
		String oldEmail = email;
		email = newEmail;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__EMAIL, oldEmail, email));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommand(String newCommand) {
		String oldCommand = command;
		command = newCommand;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComment(String newComment) {
		String oldComment = comment;
		comment = newComment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__COMMENT, oldComment, comment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String buildDataFilePath(String filePath) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String buildFilename(String name) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public STATUS executeCommand(String command) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateFilePath(String filrPath) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCommand(String command) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LDEExperimentsPackage.SAMPLE__NAME:
				return getName();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return getSampleID();
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return getStatus();
			case LDEExperimentsPackage.SAMPLE__ENABLED:
				return isEnabled();
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				return getCellID();
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				return getVisitID();
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				return getEmail();
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return getCommand();
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				return getComment();
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
			case LDEExperimentsPackage.SAMPLE__NAME:
				setName((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				setSampleID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__STATUS:
				setStatus((STATUS)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				setCellID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				setVisitID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				setEmail((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				setComment((String)newValue);
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
			case LDEExperimentsPackage.SAMPLE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				setSampleID(SAMPLE_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				setCellID(CELL_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				setVisitID(VISIT_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				setEmail(EMAIL_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand(COMMAND_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				setComment(COMMENT_EDEFAULT);
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
			case LDEExperimentsPackage.SAMPLE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return SAMPLE_ID_EDEFAULT == null ? sampleID != null : !SAMPLE_ID_EDEFAULT.equals(sampleID);
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return status != STATUS_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				return CELL_ID_EDEFAULT == null ? cellID != null : !CELL_ID_EDEFAULT.equals(cellID);
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				return VISIT_ID_EDEFAULT == null ? visitID != null : !VISIT_ID_EDEFAULT.equals(visitID);
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				return EMAIL_EDEFAULT == null ? email != null : !EMAIL_EDEFAULT.equals(email);
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return COMMAND_EDEFAULT == null ? command != null : !COMMAND_EDEFAULT.equals(command);
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case LDEExperimentsPackage.SAMPLE___BUILD_DATA_FILE_PATH__STRING:
				return buildDataFilePath((String)arguments.get(0));
			case LDEExperimentsPackage.SAMPLE___BUILD_FILENAME__STRING:
				return buildFilename((String)arguments.get(0));
			case LDEExperimentsPackage.SAMPLE___EXECUTE_COMMAND__STRING:
				return executeCommand((String)arguments.get(0));
			case LDEExperimentsPackage.SAMPLE___VALIDATE_FILE_PATH__STRING:
				return validateFilePath((String)arguments.get(0));
			case LDEExperimentsPackage.SAMPLE___VALIDATE_COMMAND__STRING:
				return validateCommand((String)arguments.get(0));
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (name: ");
		result.append(name);
		result.append(", sampleID: ");
		result.append(sampleID);
		result.append(", status: ");
		result.append(status);
		result.append(", enabled: ");
		result.append(enabled);
		result.append(", cellID: ");
		result.append(cellID);
		result.append(", visitID: ");
		result.append(visitID);
		result.append(", email: ");
		result.append(email);
		result.append(", command: ");
		result.append(command);
		result.append(", comment: ");
		result.append(comment);
		result.append(')');
		return result.toString();
	}

} //SampleImpl
