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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.ByteOrderType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.GapType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OffsetType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType16;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Raw Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getBits <em>Bits</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getOffset <em>Offset</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getByteOrder <em>Byte Order</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getXlen <em>Xlen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getYlen <em>Ylen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getZlen <em>Zlen</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getGap <em>Gap</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.RawTypeImpl#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RawTypeImpl extends EObjectImpl implements RawType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType16 type;

	/**
	 * The default value of the '{@link #getBits() <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBits()
	 * @generated
	 * @ordered
	 */
	protected static final int BITS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getBits() <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBits()
	 * @generated
	 * @ordered
	 */
	protected int bits = BITS_EDEFAULT;

	/**
	 * This is true if the Bits attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean bitsESet;

	/**
	 * The cached value of the '{@link #getOffset() <em>Offset</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOffset()
	 * @generated
	 * @ordered
	 */
	protected OffsetType offset;

	/**
	 * The cached value of the '{@link #getByteOrder() <em>Byte Order</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getByteOrder()
	 * @generated
	 * @ordered
	 */
	protected ByteOrderType byteOrder;

	/**
	 * The default value of the '{@link #getXlen() <em>Xlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXlen()
	 * @generated
	 * @ordered
	 */
	protected static final int XLEN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getXlen() <em>Xlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXlen()
	 * @generated
	 * @ordered
	 */
	protected int xlen = XLEN_EDEFAULT;

	/**
	 * This is true if the Xlen attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean xlenESet;

	/**
	 * The default value of the '{@link #getYlen() <em>Ylen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYlen()
	 * @generated
	 * @ordered
	 */
	protected static final int YLEN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getYlen() <em>Ylen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYlen()
	 * @generated
	 * @ordered
	 */
	protected int ylen = YLEN_EDEFAULT;

	/**
	 * This is true if the Ylen attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean ylenESet;

	/**
	 * The default value of the '{@link #getZlen() <em>Zlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZlen()
	 * @generated
	 * @ordered
	 */
	protected static final int ZLEN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getZlen() <em>Zlen</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getZlen()
	 * @generated
	 * @ordered
	 */
	protected int zlen = ZLEN_EDEFAULT;

	/**
	 * This is true if the Zlen attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean zlenESet;

	/**
	 * The cached value of the '{@link #getGap() <em>Gap</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGap()
	 * @generated
	 * @ordered
	 */
	protected GapType gap;

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
	protected RawTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.RAW_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType16 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType16 newType, NotificationChain msgs) {
		TypeType16 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType16 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getBits() {
		return bits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBits(int newBits) {
		int oldBits = bits;
		bits = newBits;
		boolean oldBitsESet = bitsESet;
		bitsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__BITS, oldBits, bits, !oldBitsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetBits() {
		int oldBits = bits;
		boolean oldBitsESet = bitsESet;
		bits = BITS_EDEFAULT;
		bitsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.RAW_TYPE__BITS, oldBits, BITS_EDEFAULT, oldBitsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetBits() {
		return bitsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OffsetType getOffset() {
		return offset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOffset(OffsetType newOffset, NotificationChain msgs) {
		OffsetType oldOffset = offset;
		offset = newOffset;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__OFFSET, oldOffset, newOffset);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOffset(OffsetType newOffset) {
		if (newOffset != offset) {
			NotificationChain msgs = null;
			if (offset != null)
				msgs = ((InternalEObject)offset).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__OFFSET, null, msgs);
			if (newOffset != null)
				msgs = ((InternalEObject)newOffset).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__OFFSET, null, msgs);
			msgs = basicSetOffset(newOffset, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__OFFSET, newOffset, newOffset));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ByteOrderType getByteOrder() {
		return byteOrder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetByteOrder(ByteOrderType newByteOrder, NotificationChain msgs) {
		ByteOrderType oldByteOrder = byteOrder;
		byteOrder = newByteOrder;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__BYTE_ORDER, oldByteOrder, newByteOrder);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setByteOrder(ByteOrderType newByteOrder) {
		if (newByteOrder != byteOrder) {
			NotificationChain msgs = null;
			if (byteOrder != null)
				msgs = ((InternalEObject)byteOrder).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__BYTE_ORDER, null, msgs);
			if (newByteOrder != null)
				msgs = ((InternalEObject)newByteOrder).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__BYTE_ORDER, null, msgs);
			msgs = basicSetByteOrder(newByteOrder, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__BYTE_ORDER, newByteOrder, newByteOrder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getXlen() {
		return xlen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setXlen(int newXlen) {
		int oldXlen = xlen;
		xlen = newXlen;
		boolean oldXlenESet = xlenESet;
		xlenESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__XLEN, oldXlen, xlen, !oldXlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetXlen() {
		int oldXlen = xlen;
		boolean oldXlenESet = xlenESet;
		xlen = XLEN_EDEFAULT;
		xlenESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.RAW_TYPE__XLEN, oldXlen, XLEN_EDEFAULT, oldXlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetXlen() {
		return xlenESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getYlen() {
		return ylen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYlen(int newYlen) {
		int oldYlen = ylen;
		ylen = newYlen;
		boolean oldYlenESet = ylenESet;
		ylenESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__YLEN, oldYlen, ylen, !oldYlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetYlen() {
		int oldYlen = ylen;
		boolean oldYlenESet = ylenESet;
		ylen = YLEN_EDEFAULT;
		ylenESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.RAW_TYPE__YLEN, oldYlen, YLEN_EDEFAULT, oldYlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetYlen() {
		return ylenESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getZlen() {
		return zlen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZlen(int newZlen) {
		int oldZlen = zlen;
		zlen = newZlen;
		boolean oldZlenESet = zlenESet;
		zlenESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__ZLEN, oldZlen, zlen, !oldZlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetZlen() {
		int oldZlen = zlen;
		boolean oldZlenESet = zlenESet;
		zlen = ZLEN_EDEFAULT;
		zlenESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.RAW_TYPE__ZLEN, oldZlen, ZLEN_EDEFAULT, oldZlenESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetZlen() {
		return zlenESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GapType getGap() {
		return gap;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGap(GapType newGap, NotificationChain msgs) {
		GapType oldGap = gap;
		gap = newGap;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__GAP, oldGap, newGap);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGap(GapType newGap) {
		if (newGap != gap) {
			NotificationChain msgs = null;
			if (gap != null)
				msgs = ((InternalEObject)gap).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__GAP, null, msgs);
			if (newGap != null)
				msgs = ((InternalEObject)newGap).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.RAW_TYPE__GAP, null, msgs);
			msgs = basicSetGap(newGap, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__GAP, newGap, newGap));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.RAW_TYPE__DONE, oldDone, done));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.RAW_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.RAW_TYPE__OFFSET:
				return basicSetOffset(null, msgs);
			case HmPackage.RAW_TYPE__BYTE_ORDER:
				return basicSetByteOrder(null, msgs);
			case HmPackage.RAW_TYPE__GAP:
				return basicSetGap(null, msgs);
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
			case HmPackage.RAW_TYPE__TYPE:
				return getType();
			case HmPackage.RAW_TYPE__BITS:
				return getBits();
			case HmPackage.RAW_TYPE__OFFSET:
				return getOffset();
			case HmPackage.RAW_TYPE__BYTE_ORDER:
				return getByteOrder();
			case HmPackage.RAW_TYPE__XLEN:
				return getXlen();
			case HmPackage.RAW_TYPE__YLEN:
				return getYlen();
			case HmPackage.RAW_TYPE__ZLEN:
				return getZlen();
			case HmPackage.RAW_TYPE__GAP:
				return getGap();
			case HmPackage.RAW_TYPE__DONE:
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
			case HmPackage.RAW_TYPE__TYPE:
				setType((TypeType16)newValue);
				return;
			case HmPackage.RAW_TYPE__BITS:
				setBits((Integer)newValue);
				return;
			case HmPackage.RAW_TYPE__OFFSET:
				setOffset((OffsetType)newValue);
				return;
			case HmPackage.RAW_TYPE__BYTE_ORDER:
				setByteOrder((ByteOrderType)newValue);
				return;
			case HmPackage.RAW_TYPE__XLEN:
				setXlen((Integer)newValue);
				return;
			case HmPackage.RAW_TYPE__YLEN:
				setYlen((Integer)newValue);
				return;
			case HmPackage.RAW_TYPE__ZLEN:
				setZlen((Integer)newValue);
				return;
			case HmPackage.RAW_TYPE__GAP:
				setGap((GapType)newValue);
				return;
			case HmPackage.RAW_TYPE__DONE:
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
			case HmPackage.RAW_TYPE__TYPE:
				setType((TypeType16)null);
				return;
			case HmPackage.RAW_TYPE__BITS:
				unsetBits();
				return;
			case HmPackage.RAW_TYPE__OFFSET:
				setOffset((OffsetType)null);
				return;
			case HmPackage.RAW_TYPE__BYTE_ORDER:
				setByteOrder((ByteOrderType)null);
				return;
			case HmPackage.RAW_TYPE__XLEN:
				unsetXlen();
				return;
			case HmPackage.RAW_TYPE__YLEN:
				unsetYlen();
				return;
			case HmPackage.RAW_TYPE__ZLEN:
				unsetZlen();
				return;
			case HmPackage.RAW_TYPE__GAP:
				setGap((GapType)null);
				return;
			case HmPackage.RAW_TYPE__DONE:
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
			case HmPackage.RAW_TYPE__TYPE:
				return type != null;
			case HmPackage.RAW_TYPE__BITS:
				return isSetBits();
			case HmPackage.RAW_TYPE__OFFSET:
				return offset != null;
			case HmPackage.RAW_TYPE__BYTE_ORDER:
				return byteOrder != null;
			case HmPackage.RAW_TYPE__XLEN:
				return isSetXlen();
			case HmPackage.RAW_TYPE__YLEN:
				return isSetYlen();
			case HmPackage.RAW_TYPE__ZLEN:
				return isSetZlen();
			case HmPackage.RAW_TYPE__GAP:
				return gap != null;
			case HmPackage.RAW_TYPE__DONE:
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
		result.append(" (bits: ");
		if (bitsESet) result.append(bits); else result.append("<unset>");
		result.append(", xlen: ");
		if (xlenESet) result.append(xlen); else result.append("<unset>");
		result.append(", ylen: ");
		if (ylenESet) result.append(ylen); else result.append("<unset>");
		result.append(", zlen: ");
		if (zlenESet) result.append(zlen); else result.append("<unset>");
		result.append(", done: ");
		result.append(done);
		result.append(')');
		return result.toString();
	}

} //RawTypeImpl
