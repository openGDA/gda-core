/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.QsubType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Qsub Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.QsubTypeImpl#getProjectname <em>Projectname</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.QsubTypeImpl#getArgs <em>Args</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.QsubTypeImpl#getSinoqueue <em>Sinoqueue</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.QsubTypeImpl#getReconqueue <em>Reconqueue</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class QsubTypeImpl extends EObjectImpl implements QsubType {
	/**
	 * The default value of the '{@link #getProjectname() <em>Projectname</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProjectname()
	 * @generated
	 * @ordered
	 */
	protected static final String PROJECTNAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProjectname() <em>Projectname</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProjectname()
	 * @generated
	 * @ordered
	 */
	protected String projectname = PROJECTNAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getArgs() <em>Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArgs()
	 * @generated
	 * @ordered
	 */
	protected static final String ARGS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getArgs() <em>Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArgs()
	 * @generated
	 * @ordered
	 */
	protected String args = ARGS_EDEFAULT;

	/**
	 * The default value of the '{@link #getSinoqueue() <em>Sinoqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSinoqueue()
	 * @generated
	 * @ordered
	 */
	protected static final String SINOQUEUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSinoqueue() <em>Sinoqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSinoqueue()
	 * @generated
	 * @ordered
	 */
	protected String sinoqueue = SINOQUEUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getReconqueue() <em>Reconqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReconqueue()
	 * @generated
	 * @ordered
	 */
	protected static final String RECONQUEUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReconqueue() <em>Reconqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReconqueue()
	 * @generated
	 * @ordered
	 */
	protected String reconqueue = RECONQUEUE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected QsubTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.QSUB_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getProjectname() {
		return projectname;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProjectname(String newProjectname) {
		String oldProjectname = projectname;
		projectname = newProjectname;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.QSUB_TYPE__PROJECTNAME, oldProjectname, projectname));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getArgs() {
		return args;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArgs(String newArgs) {
		String oldArgs = args;
		args = newArgs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.QSUB_TYPE__ARGS, oldArgs, args));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSinoqueue() {
		return sinoqueue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSinoqueue(String newSinoqueue) {
		String oldSinoqueue = sinoqueue;
		sinoqueue = newSinoqueue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.QSUB_TYPE__SINOQUEUE, oldSinoqueue, sinoqueue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getReconqueue() {
		return reconqueue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReconqueue(String newReconqueue) {
		String oldReconqueue = reconqueue;
		reconqueue = newReconqueue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.QSUB_TYPE__RECONQUEUE, oldReconqueue, reconqueue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LocalTomoPackage.QSUB_TYPE__PROJECTNAME:
				return getProjectname();
			case LocalTomoPackage.QSUB_TYPE__ARGS:
				return getArgs();
			case LocalTomoPackage.QSUB_TYPE__SINOQUEUE:
				return getSinoqueue();
			case LocalTomoPackage.QSUB_TYPE__RECONQUEUE:
				return getReconqueue();
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
			case LocalTomoPackage.QSUB_TYPE__PROJECTNAME:
				setProjectname((String)newValue);
				return;
			case LocalTomoPackage.QSUB_TYPE__ARGS:
				setArgs((String)newValue);
				return;
			case LocalTomoPackage.QSUB_TYPE__SINOQUEUE:
				setSinoqueue((String)newValue);
				return;
			case LocalTomoPackage.QSUB_TYPE__RECONQUEUE:
				setReconqueue((String)newValue);
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
			case LocalTomoPackage.QSUB_TYPE__PROJECTNAME:
				setProjectname(PROJECTNAME_EDEFAULT);
				return;
			case LocalTomoPackage.QSUB_TYPE__ARGS:
				setArgs(ARGS_EDEFAULT);
				return;
			case LocalTomoPackage.QSUB_TYPE__SINOQUEUE:
				setSinoqueue(SINOQUEUE_EDEFAULT);
				return;
			case LocalTomoPackage.QSUB_TYPE__RECONQUEUE:
				setReconqueue(RECONQUEUE_EDEFAULT);
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
			case LocalTomoPackage.QSUB_TYPE__PROJECTNAME:
				return PROJECTNAME_EDEFAULT == null ? projectname != null : !PROJECTNAME_EDEFAULT.equals(projectname);
			case LocalTomoPackage.QSUB_TYPE__ARGS:
				return ARGS_EDEFAULT == null ? args != null : !ARGS_EDEFAULT.equals(args);
			case LocalTomoPackage.QSUB_TYPE__SINOQUEUE:
				return SINOQUEUE_EDEFAULT == null ? sinoqueue != null : !SINOQUEUE_EDEFAULT.equals(sinoqueue);
			case LocalTomoPackage.QSUB_TYPE__RECONQUEUE:
				return RECONQUEUE_EDEFAULT == null ? reconqueue != null : !RECONQUEUE_EDEFAULT.equals(reconqueue);
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
		result.append(" (projectname: ");
		result.append(projectname);
		result.append(", args: ");
		result.append(args);
		result.append(", sinoqueue: ");
		result.append(sinoqueue);
		result.append(", reconqueue: ");
		result.append(reconqueue);
		result.append(')');
		return result.toString();
	}

} //QsubTypeImpl
