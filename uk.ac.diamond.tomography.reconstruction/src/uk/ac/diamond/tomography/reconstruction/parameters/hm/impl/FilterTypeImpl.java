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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NameType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NormalisationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType7;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.WindowNameType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Filter Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getName <em>Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getBandwidth <em>Bandwidth</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getWindowName <em>Window Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getNormalisation <em>Normalisation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.FilterTypeImpl#getPixelSize <em>Pixel Size</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FilterTypeImpl extends EObjectImpl implements FilterType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType7 type;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected NameType name;

	/**
	 * The default value of the '{@link #getBandwidth() <em>Bandwidth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBandwidth()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal BANDWIDTH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBandwidth() <em>Bandwidth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBandwidth()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal bandwidth = BANDWIDTH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getWindowName() <em>Window Name</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWindowName()
	 * @generated
	 * @ordered
	 */
	protected WindowNameType windowName;

	/**
	 * The cached value of the '{@link #getNormalisation() <em>Normalisation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNormalisation()
	 * @generated
	 * @ordered
	 */
	protected NormalisationType normalisation;

	/**
	 * The default value of the '{@link #getPixelSize() <em>Pixel Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixelSize()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal PIXEL_SIZE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPixelSize() <em>Pixel Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixelSize()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal pixelSize = PIXEL_SIZE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FilterTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.FILTER_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType7 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType7 newType, NotificationChain msgs) {
		TypeType7 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType7 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NameType getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetName(NameType newName, NotificationChain msgs) {
		NameType oldName = name;
		name = newName;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__NAME, oldName, newName);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(NameType newName) {
		if (newName != name) {
			NotificationChain msgs = null;
			if (name != null)
				msgs = ((InternalEObject)name).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__NAME, null, msgs);
			if (newName != null)
				msgs = ((InternalEObject)newName).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__NAME, null, msgs);
			msgs = basicSetName(newName, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__NAME, newName, newName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getBandwidth() {
		return bandwidth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBandwidth(BigDecimal newBandwidth) {
		BigDecimal oldBandwidth = bandwidth;
		bandwidth = newBandwidth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__BANDWIDTH, oldBandwidth, bandwidth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WindowNameType getWindowName() {
		return windowName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetWindowName(WindowNameType newWindowName, NotificationChain msgs) {
		WindowNameType oldWindowName = windowName;
		windowName = newWindowName;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__WINDOW_NAME, oldWindowName, newWindowName);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWindowName(WindowNameType newWindowName) {
		if (newWindowName != windowName) {
			NotificationChain msgs = null;
			if (windowName != null)
				msgs = ((InternalEObject)windowName).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__WINDOW_NAME, null, msgs);
			if (newWindowName != null)
				msgs = ((InternalEObject)newWindowName).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__WINDOW_NAME, null, msgs);
			msgs = basicSetWindowName(newWindowName, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__WINDOW_NAME, newWindowName, newWindowName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NormalisationType getNormalisation() {
		return normalisation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNormalisation(NormalisationType newNormalisation, NotificationChain msgs) {
		NormalisationType oldNormalisation = normalisation;
		normalisation = newNormalisation;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__NORMALISATION, oldNormalisation, newNormalisation);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNormalisation(NormalisationType newNormalisation) {
		if (newNormalisation != normalisation) {
			NotificationChain msgs = null;
			if (normalisation != null)
				msgs = ((InternalEObject)normalisation).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__NORMALISATION, null, msgs);
			if (newNormalisation != null)
				msgs = ((InternalEObject)newNormalisation).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.FILTER_TYPE__NORMALISATION, null, msgs);
			msgs = basicSetNormalisation(newNormalisation, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__NORMALISATION, newNormalisation, newNormalisation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getPixelSize() {
		return pixelSize;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPixelSize(BigDecimal newPixelSize) {
		BigDecimal oldPixelSize = pixelSize;
		pixelSize = newPixelSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.FILTER_TYPE__PIXEL_SIZE, oldPixelSize, pixelSize));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.FILTER_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.FILTER_TYPE__NAME:
				return basicSetName(null, msgs);
			case HmPackage.FILTER_TYPE__WINDOW_NAME:
				return basicSetWindowName(null, msgs);
			case HmPackage.FILTER_TYPE__NORMALISATION:
				return basicSetNormalisation(null, msgs);
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
			case HmPackage.FILTER_TYPE__TYPE:
				return getType();
			case HmPackage.FILTER_TYPE__NAME:
				return getName();
			case HmPackage.FILTER_TYPE__BANDWIDTH:
				return getBandwidth();
			case HmPackage.FILTER_TYPE__WINDOW_NAME:
				return getWindowName();
			case HmPackage.FILTER_TYPE__NORMALISATION:
				return getNormalisation();
			case HmPackage.FILTER_TYPE__PIXEL_SIZE:
				return getPixelSize();
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
			case HmPackage.FILTER_TYPE__TYPE:
				setType((TypeType7)newValue);
				return;
			case HmPackage.FILTER_TYPE__NAME:
				setName((NameType)newValue);
				return;
			case HmPackage.FILTER_TYPE__BANDWIDTH:
				setBandwidth((BigDecimal)newValue);
				return;
			case HmPackage.FILTER_TYPE__WINDOW_NAME:
				setWindowName((WindowNameType)newValue);
				return;
			case HmPackage.FILTER_TYPE__NORMALISATION:
				setNormalisation((NormalisationType)newValue);
				return;
			case HmPackage.FILTER_TYPE__PIXEL_SIZE:
				setPixelSize((BigDecimal)newValue);
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
			case HmPackage.FILTER_TYPE__TYPE:
				setType((TypeType7)null);
				return;
			case HmPackage.FILTER_TYPE__NAME:
				setName((NameType)null);
				return;
			case HmPackage.FILTER_TYPE__BANDWIDTH:
				setBandwidth(BANDWIDTH_EDEFAULT);
				return;
			case HmPackage.FILTER_TYPE__WINDOW_NAME:
				setWindowName((WindowNameType)null);
				return;
			case HmPackage.FILTER_TYPE__NORMALISATION:
				setNormalisation((NormalisationType)null);
				return;
			case HmPackage.FILTER_TYPE__PIXEL_SIZE:
				setPixelSize(PIXEL_SIZE_EDEFAULT);
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
			case HmPackage.FILTER_TYPE__TYPE:
				return type != null;
			case HmPackage.FILTER_TYPE__NAME:
				return name != null;
			case HmPackage.FILTER_TYPE__BANDWIDTH:
				return BANDWIDTH_EDEFAULT == null ? bandwidth != null : !BANDWIDTH_EDEFAULT.equals(bandwidth);
			case HmPackage.FILTER_TYPE__WINDOW_NAME:
				return windowName != null;
			case HmPackage.FILTER_TYPE__NORMALISATION:
				return normalisation != null;
			case HmPackage.FILTER_TYPE__PIXEL_SIZE:
				return PIXEL_SIZE_EDEFAULT == null ? pixelSize != null : !PIXEL_SIZE_EDEFAULT.equals(pixelSize);
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
		result.append(" (bandwidth: ");
		result.append(bandwidth);
		result.append(", pixelSize: ");
		result.append(pixelSize);
		result.append(')');
		return result.toString();
	}

} //FilterTypeImpl
