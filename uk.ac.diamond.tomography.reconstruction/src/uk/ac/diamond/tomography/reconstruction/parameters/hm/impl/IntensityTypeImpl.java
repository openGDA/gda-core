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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType6;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Intensity Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl#getColumnLeft <em>Column Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl#getColumnRight <em>Column Right</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl#getZeroLeft <em>Zero Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.IntensityTypeImpl#getZeroRight <em>Zero Right</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class IntensityTypeImpl extends EObjectImpl implements IntensityType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType6 type;

	/**
	 * The default value of the '{@link #getColumnLeft() <em>Column Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumnLeft()
	 * @generated
	 * @ordered
	 */
	protected static final String COLUMN_LEFT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColumnLeft() <em>Column Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumnLeft()
	 * @generated
	 * @ordered
	 */
	protected String columnLeft = COLUMN_LEFT_EDEFAULT;

	/**
	 * The default value of the '{@link #getColumnRight() <em>Column Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumnRight()
	 * @generated
	 * @ordered
	 */
	protected static final String COLUMN_RIGHT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColumnRight() <em>Column Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumnRight()
	 * @generated
	 * @ordered
	 */
	protected String columnRight = COLUMN_RIGHT_EDEFAULT;

	/**
	 * The default value of the '{@link #getZeroLeft() <em>Zero Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZeroLeft()
	 * @generated
	 * @ordered
	 */
	protected static final int ZERO_LEFT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getZeroLeft() <em>Zero Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZeroLeft()
	 * @generated
	 * @ordered
	 */
	protected int zeroLeft = ZERO_LEFT_EDEFAULT;

	/**
	 * This is true if the Zero Left attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean zeroLeftESet;

	/**
	 * The default value of the '{@link #getZeroRight() <em>Zero Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZeroRight()
	 * @generated
	 * @ordered
	 */
	protected static final int ZERO_RIGHT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getZeroRight() <em>Zero Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZeroRight()
	 * @generated
	 * @ordered
	 */
	protected int zeroRight = ZERO_RIGHT_EDEFAULT;

	/**
	 * This is true if the Zero Right attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean zeroRightESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntensityTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.INTENSITY_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType6 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType6 newType, NotificationChain msgs) {
		TypeType6 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType6 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INTENSITY_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INTENSITY_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getColumnLeft() {
		return columnLeft;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setColumnLeft(String newColumnLeft) {
		String oldColumnLeft = columnLeft;
		columnLeft = newColumnLeft;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__COLUMN_LEFT, oldColumnLeft, columnLeft));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getColumnRight() {
		return columnRight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setColumnRight(String newColumnRight) {
		String oldColumnRight = columnRight;
		columnRight = newColumnRight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__COLUMN_RIGHT, oldColumnRight, columnRight));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getZeroLeft() {
		return zeroLeft;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZeroLeft(int newZeroLeft) {
		int oldZeroLeft = zeroLeft;
		zeroLeft = newZeroLeft;
		boolean oldZeroLeftESet = zeroLeftESet;
		zeroLeftESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__ZERO_LEFT, oldZeroLeft, zeroLeft, !oldZeroLeftESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetZeroLeft() {
		int oldZeroLeft = zeroLeft;
		boolean oldZeroLeftESet = zeroLeftESet;
		zeroLeft = ZERO_LEFT_EDEFAULT;
		zeroLeftESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INTENSITY_TYPE__ZERO_LEFT, oldZeroLeft, ZERO_LEFT_EDEFAULT, oldZeroLeftESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetZeroLeft() {
		return zeroLeftESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getZeroRight() {
		return zeroRight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZeroRight(int newZeroRight) {
		int oldZeroRight = zeroRight;
		zeroRight = newZeroRight;
		boolean oldZeroRightESet = zeroRightESet;
		zeroRightESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INTENSITY_TYPE__ZERO_RIGHT, oldZeroRight, zeroRight, !oldZeroRightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetZeroRight() {
		int oldZeroRight = zeroRight;
		boolean oldZeroRightESet = zeroRightESet;
		zeroRight = ZERO_RIGHT_EDEFAULT;
		zeroRightESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INTENSITY_TYPE__ZERO_RIGHT, oldZeroRight, ZERO_RIGHT_EDEFAULT, oldZeroRightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetZeroRight() {
		return zeroRightESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.INTENSITY_TYPE__TYPE:
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
			case HmPackage.INTENSITY_TYPE__TYPE:
				return getType();
			case HmPackage.INTENSITY_TYPE__COLUMN_LEFT:
				return getColumnLeft();
			case HmPackage.INTENSITY_TYPE__COLUMN_RIGHT:
				return getColumnRight();
			case HmPackage.INTENSITY_TYPE__ZERO_LEFT:
				return getZeroLeft();
			case HmPackage.INTENSITY_TYPE__ZERO_RIGHT:
				return getZeroRight();
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
			case HmPackage.INTENSITY_TYPE__TYPE:
				setType((TypeType6)newValue);
				return;
			case HmPackage.INTENSITY_TYPE__COLUMN_LEFT:
				setColumnLeft((String)newValue);
				return;
			case HmPackage.INTENSITY_TYPE__COLUMN_RIGHT:
				setColumnRight((String)newValue);
				return;
			case HmPackage.INTENSITY_TYPE__ZERO_LEFT:
				setZeroLeft((Integer)newValue);
				return;
			case HmPackage.INTENSITY_TYPE__ZERO_RIGHT:
				setZeroRight((Integer)newValue);
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
			case HmPackage.INTENSITY_TYPE__TYPE:
				setType((TypeType6)null);
				return;
			case HmPackage.INTENSITY_TYPE__COLUMN_LEFT:
				setColumnLeft(COLUMN_LEFT_EDEFAULT);
				return;
			case HmPackage.INTENSITY_TYPE__COLUMN_RIGHT:
				setColumnRight(COLUMN_RIGHT_EDEFAULT);
				return;
			case HmPackage.INTENSITY_TYPE__ZERO_LEFT:
				unsetZeroLeft();
				return;
			case HmPackage.INTENSITY_TYPE__ZERO_RIGHT:
				unsetZeroRight();
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
			case HmPackage.INTENSITY_TYPE__TYPE:
				return type != null;
			case HmPackage.INTENSITY_TYPE__COLUMN_LEFT:
				return COLUMN_LEFT_EDEFAULT == null ? columnLeft != null : !COLUMN_LEFT_EDEFAULT.equals(columnLeft);
			case HmPackage.INTENSITY_TYPE__COLUMN_RIGHT:
				return COLUMN_RIGHT_EDEFAULT == null ? columnRight != null : !COLUMN_RIGHT_EDEFAULT.equals(columnRight);
			case HmPackage.INTENSITY_TYPE__ZERO_LEFT:
				return isSetZeroLeft();
			case HmPackage.INTENSITY_TYPE__ZERO_RIGHT:
				return isSetZeroRight();
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
		result.append(" (columnLeft: ");
		result.append(columnLeft);
		result.append(", columnRight: ");
		result.append(columnRight);
		result.append(", zeroLeft: ");
		if (zeroLeftESet) result.append(zeroLeft); else result.append("<unset>");
		result.append(", zeroRight: ");
		if (zeroRightESet) result.append(zeroRight); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //IntensityTypeImpl
