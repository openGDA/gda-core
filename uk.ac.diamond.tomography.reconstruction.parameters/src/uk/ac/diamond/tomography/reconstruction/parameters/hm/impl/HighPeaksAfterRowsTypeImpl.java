/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import java.math.BigDecimal;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType12;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>High Peaks After Rows Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl#getNumberPixels <em>Number Pixels</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.HighPeaksAfterRowsTypeImpl#getJump <em>Jump</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class HighPeaksAfterRowsTypeImpl extends EObjectImpl implements HighPeaksAfterRowsType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType12 type;

	/**
	 * The default value of the '{@link #getNumberPixels() <em>Number Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberPixels()
	 * @generated
	 * @ordered
	 */
	protected static final int NUMBER_PIXELS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getNumberPixels() <em>Number Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberPixels()
	 * @generated
	 * @ordered
	 */
	protected int numberPixels = NUMBER_PIXELS_EDEFAULT;

	/**
	 * This is true if the Number Pixels attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean numberPixelsESet;

	/**
	 * The default value of the '{@link #getJump() <em>Jump</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJump()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal JUMP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJump() <em>Jump</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJump()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal jump = JUMP_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HighPeaksAfterRowsTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.HIGH_PEAKS_AFTER_ROWS_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType12 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType12 newType, NotificationChain msgs) {
		TypeType12 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType12 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNumberPixels() {
		return numberPixels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumberPixels(int newNumberPixels) {
		int oldNumberPixels = numberPixels;
		numberPixels = newNumberPixels;
		boolean oldNumberPixelsESet = numberPixelsESet;
		numberPixelsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS, oldNumberPixels, numberPixels, !oldNumberPixelsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetNumberPixels() {
		int oldNumberPixels = numberPixels;
		boolean oldNumberPixelsESet = numberPixelsESet;
		numberPixels = NUMBER_PIXELS_EDEFAULT;
		numberPixelsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS, oldNumberPixels, NUMBER_PIXELS_EDEFAULT, oldNumberPixelsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetNumberPixels() {
		return numberPixelsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getJump() {
		return jump;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setJump(BigDecimal newJump) {
		BigDecimal oldJump = jump;
		jump = newJump;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP, oldJump, jump));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE:
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
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE:
				return getType();
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS:
				return getNumberPixels();
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP:
				return getJump();
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
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE:
				setType((TypeType12)newValue);
				return;
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS:
				setNumberPixels((Integer)newValue);
				return;
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP:
				setJump((BigDecimal)newValue);
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
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE:
				setType((TypeType12)null);
				return;
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS:
				unsetNumberPixels();
				return;
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP:
				setJump(JUMP_EDEFAULT);
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
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__TYPE:
				return type != null;
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__NUMBER_PIXELS:
				return isSetNumberPixels();
			case HmPackage.HIGH_PEAKS_AFTER_ROWS_TYPE__JUMP:
				return JUMP_EDEFAULT == null ? jump != null : !JUMP_EDEFAULT.equals(jump);
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
		result.append(" (numberPixels: ");
		if (numberPixelsESet) result.append(numberPixels); else result.append("<unset>");
		result.append(", jump: ");
		result.append(jump);
		result.append(')');
		return result.toString();
	}

} //HighPeaksAfterRowsTypeImpl
