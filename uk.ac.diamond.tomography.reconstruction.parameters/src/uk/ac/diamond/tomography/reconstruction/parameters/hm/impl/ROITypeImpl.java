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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputWidthTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType3;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>ROI Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getXmin <em>Xmin</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getXmax <em>Xmax</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getYmin <em>Ymin</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getYmax <em>Ymax</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getOutputWidthType <em>Output Width Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getOutputWidth <em>Output Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ROITypeImpl#getAngle <em>Angle</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ROITypeImpl extends EObjectImpl implements ROIType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType3 type;

	/**
	 * The default value of the '{@link #getXmin() <em>Xmin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXmin()
	 * @generated
	 * @ordered
	 */
	protected static final int XMIN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getXmin() <em>Xmin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXmin()
	 * @generated
	 * @ordered
	 */
	protected int xmin = XMIN_EDEFAULT;

	/**
	 * This is true if the Xmin attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean xminESet;

	/**
	 * The default value of the '{@link #getXmax() <em>Xmax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXmax()
	 * @generated
	 * @ordered
	 */
	protected static final int XMAX_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getXmax() <em>Xmax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXmax()
	 * @generated
	 * @ordered
	 */
	protected int xmax = XMAX_EDEFAULT;

	/**
	 * This is true if the Xmax attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean xmaxESet;

	/**
	 * The default value of the '{@link #getYmin() <em>Ymin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYmin()
	 * @generated
	 * @ordered
	 */
	protected static final int YMIN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getYmin() <em>Ymin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYmin()
	 * @generated
	 * @ordered
	 */
	protected int ymin = YMIN_EDEFAULT;

	/**
	 * This is true if the Ymin attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean yminESet;

	/**
	 * The default value of the '{@link #getYmax() <em>Ymax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYmax()
	 * @generated
	 * @ordered
	 */
	protected static final int YMAX_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getYmax() <em>Ymax</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYmax()
	 * @generated
	 * @ordered
	 */
	protected int ymax = YMAX_EDEFAULT;

	/**
	 * This is true if the Ymax attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean ymaxESet;

	/**
	 * The cached value of the '{@link #getOutputWidthType() <em>Output Width Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputWidthType()
	 * @generated
	 * @ordered
	 */
	protected OutputWidthTypeType outputWidthType;

	/**
	 * The default value of the '{@link #getOutputWidth() <em>Output Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int OUTPUT_WIDTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getOutputWidth() <em>Output Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputWidth()
	 * @generated
	 * @ordered
	 */
	protected int outputWidth = OUTPUT_WIDTH_EDEFAULT;

	/**
	 * This is true if the Output Width attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean outputWidthESet;

	/**
	 * The default value of the '{@link #getAngle() <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAngle()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal ANGLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAngle() <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAngle()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal angle = ANGLE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ROITypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.ROI_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType3 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType3 newType, NotificationChain msgs) {
		TypeType3 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType3 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.ROI_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.ROI_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getXmin() {
		return xmin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setXmin(int newXmin) {
		int oldXmin = xmin;
		xmin = newXmin;
		boolean oldXminESet = xminESet;
		xminESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__XMIN, oldXmin, xmin, !oldXminESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetXmin() {
		int oldXmin = xmin;
		boolean oldXminESet = xminESet;
		xmin = XMIN_EDEFAULT;
		xminESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.ROI_TYPE__XMIN, oldXmin, XMIN_EDEFAULT, oldXminESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetXmin() {
		return xminESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getXmax() {
		return xmax;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setXmax(int newXmax) {
		int oldXmax = xmax;
		xmax = newXmax;
		boolean oldXmaxESet = xmaxESet;
		xmaxESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__XMAX, oldXmax, xmax, !oldXmaxESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetXmax() {
		int oldXmax = xmax;
		boolean oldXmaxESet = xmaxESet;
		xmax = XMAX_EDEFAULT;
		xmaxESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.ROI_TYPE__XMAX, oldXmax, XMAX_EDEFAULT, oldXmaxESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetXmax() {
		return xmaxESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getYmin() {
		return ymin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYmin(int newYmin) {
		int oldYmin = ymin;
		ymin = newYmin;
		boolean oldYminESet = yminESet;
		yminESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__YMIN, oldYmin, ymin, !oldYminESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetYmin() {
		int oldYmin = ymin;
		boolean oldYminESet = yminESet;
		ymin = YMIN_EDEFAULT;
		yminESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.ROI_TYPE__YMIN, oldYmin, YMIN_EDEFAULT, oldYminESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetYmin() {
		return yminESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getYmax() {
		return ymax;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYmax(int newYmax) {
		int oldYmax = ymax;
		ymax = newYmax;
		boolean oldYmaxESet = ymaxESet;
		ymaxESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__YMAX, oldYmax, ymax, !oldYmaxESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetYmax() {
		int oldYmax = ymax;
		boolean oldYmaxESet = ymaxESet;
		ymax = YMAX_EDEFAULT;
		ymaxESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.ROI_TYPE__YMAX, oldYmax, YMAX_EDEFAULT, oldYmaxESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetYmax() {
		return ymaxESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OutputWidthTypeType getOutputWidthType() {
		return outputWidthType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOutputWidthType(OutputWidthTypeType newOutputWidthType, NotificationChain msgs) {
		OutputWidthTypeType oldOutputWidthType = outputWidthType;
		outputWidthType = newOutputWidthType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE, oldOutputWidthType, newOutputWidthType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputWidthType(OutputWidthTypeType newOutputWidthType) {
		if (newOutputWidthType != outputWidthType) {
			NotificationChain msgs = null;
			if (outputWidthType != null)
				msgs = ((InternalEObject)outputWidthType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE, null, msgs);
			if (newOutputWidthType != null)
				msgs = ((InternalEObject)newOutputWidthType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE, null, msgs);
			msgs = basicSetOutputWidthType(newOutputWidthType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE, newOutputWidthType, newOutputWidthType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getOutputWidth() {
		return outputWidth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputWidth(int newOutputWidth) {
		int oldOutputWidth = outputWidth;
		outputWidth = newOutputWidth;
		boolean oldOutputWidthESet = outputWidthESet;
		outputWidthESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__OUTPUT_WIDTH, oldOutputWidth, outputWidth, !oldOutputWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetOutputWidth() {
		int oldOutputWidth = outputWidth;
		boolean oldOutputWidthESet = outputWidthESet;
		outputWidth = OUTPUT_WIDTH_EDEFAULT;
		outputWidthESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.ROI_TYPE__OUTPUT_WIDTH, oldOutputWidth, OUTPUT_WIDTH_EDEFAULT, oldOutputWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetOutputWidth() {
		return outputWidthESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getAngle() {
		return angle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAngle(BigDecimal newAngle) {
		BigDecimal oldAngle = angle;
		angle = newAngle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.ROI_TYPE__ANGLE, oldAngle, angle));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.ROI_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE:
				return basicSetOutputWidthType(null, msgs);
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
			case HmPackage.ROI_TYPE__TYPE:
				return getType();
			case HmPackage.ROI_TYPE__XMIN:
				return getXmin();
			case HmPackage.ROI_TYPE__XMAX:
				return getXmax();
			case HmPackage.ROI_TYPE__YMIN:
				return getYmin();
			case HmPackage.ROI_TYPE__YMAX:
				return getYmax();
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE:
				return getOutputWidthType();
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH:
				return getOutputWidth();
			case HmPackage.ROI_TYPE__ANGLE:
				return getAngle();
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
			case HmPackage.ROI_TYPE__TYPE:
				setType((TypeType3)newValue);
				return;
			case HmPackage.ROI_TYPE__XMIN:
				setXmin((Integer)newValue);
				return;
			case HmPackage.ROI_TYPE__XMAX:
				setXmax((Integer)newValue);
				return;
			case HmPackage.ROI_TYPE__YMIN:
				setYmin((Integer)newValue);
				return;
			case HmPackage.ROI_TYPE__YMAX:
				setYmax((Integer)newValue);
				return;
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE:
				setOutputWidthType((OutputWidthTypeType)newValue);
				return;
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH:
				setOutputWidth((Integer)newValue);
				return;
			case HmPackage.ROI_TYPE__ANGLE:
				setAngle((BigDecimal)newValue);
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
			case HmPackage.ROI_TYPE__TYPE:
				setType((TypeType3)null);
				return;
			case HmPackage.ROI_TYPE__XMIN:
				unsetXmin();
				return;
			case HmPackage.ROI_TYPE__XMAX:
				unsetXmax();
				return;
			case HmPackage.ROI_TYPE__YMIN:
				unsetYmin();
				return;
			case HmPackage.ROI_TYPE__YMAX:
				unsetYmax();
				return;
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE:
				setOutputWidthType((OutputWidthTypeType)null);
				return;
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH:
				unsetOutputWidth();
				return;
			case HmPackage.ROI_TYPE__ANGLE:
				setAngle(ANGLE_EDEFAULT);
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
			case HmPackage.ROI_TYPE__TYPE:
				return type != null;
			case HmPackage.ROI_TYPE__XMIN:
				return isSetXmin();
			case HmPackage.ROI_TYPE__XMAX:
				return isSetXmax();
			case HmPackage.ROI_TYPE__YMIN:
				return isSetYmin();
			case HmPackage.ROI_TYPE__YMAX:
				return isSetYmax();
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH_TYPE:
				return outputWidthType != null;
			case HmPackage.ROI_TYPE__OUTPUT_WIDTH:
				return isSetOutputWidth();
			case HmPackage.ROI_TYPE__ANGLE:
				return ANGLE_EDEFAULT == null ? angle != null : !ANGLE_EDEFAULT.equals(angle);
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
		result.append(" (xmin: ");
		if (xminESet) result.append(xmin); else result.append("<unset>");
		result.append(", xmax: ");
		if (xmaxESet) result.append(xmax); else result.append("<unset>");
		result.append(", ymin: ");
		if (yminESet) result.append(ymin); else result.append("<unset>");
		result.append(", ymax: ");
		if (ymaxESet) result.append(ymax); else result.append("<unset>");
		result.append(", outputWidth: ");
		if (outputWidthESet) result.append(outputWidth); else result.append("<unset>");
		result.append(", angle: ");
		result.append(angle);
		result.append(')');
		return result.toString();
	}

} //ROITypeImpl
