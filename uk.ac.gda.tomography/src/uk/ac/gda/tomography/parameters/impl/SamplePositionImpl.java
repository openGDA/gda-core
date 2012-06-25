/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.parameters.SamplePosition;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample Position</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl#getVertical <em>Vertical</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl#getCenterX <em>Center X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl#getCenterZ <em>Center Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl#getTiltX <em>Tilt X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SamplePositionImpl#getTiltZ <em>Tilt Z</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SamplePositionImpl extends EObjectImpl implements SamplePosition {
	/**
	 * The default value of the '{@link #getVertical() <em>Vertical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVertical()
	 * @generated
	 * @ordered
	 */
	protected static final double VERTICAL_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getVertical() <em>Vertical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVertical()
	 * @generated
	 * @ordered
	 */
	protected double vertical = VERTICAL_EDEFAULT;

	/**
	 * This is true if the Vertical attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean verticalESet;

	/**
	 * The default value of the '{@link #getCenterX() <em>Center X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterX()
	 * @generated
	 * @ordered
	 */
	protected static final double CENTER_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCenterX() <em>Center X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterX()
	 * @generated
	 * @ordered
	 */
	protected double centerX = CENTER_X_EDEFAULT;

	/**
	 * This is true if the Center X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean centerXESet;

	/**
	 * The default value of the '{@link #getCenterZ() <em>Center Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterZ()
	 * @generated
	 * @ordered
	 */
	protected static final double CENTER_Z_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCenterZ() <em>Center Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterZ()
	 * @generated
	 * @ordered
	 */
	protected double centerZ = CENTER_Z_EDEFAULT;

	/**
	 * This is true if the Center Z attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean centerZESet;

	/**
	 * The default value of the '{@link #getTiltX() <em>Tilt X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltX()
	 * @generated
	 * @ordered
	 */
	protected static final double TILT_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTiltX() <em>Tilt X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltX()
	 * @generated
	 * @ordered
	 */
	protected double tiltX = TILT_X_EDEFAULT;

	/**
	 * This is true if the Tilt X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tiltXESet;

	/**
	 * The default value of the '{@link #getTiltZ() <em>Tilt Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltZ()
	 * @generated
	 * @ordered
	 */
	protected static final double TILT_Z_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTiltZ() <em>Tilt Z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltZ()
	 * @generated
	 * @ordered
	 */
	protected double tiltZ = TILT_Z_EDEFAULT;

	/**
	 * This is true if the Tilt Z attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tiltZESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SamplePositionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.SAMPLE_POSITION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getVertical() {
		return vertical;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVertical(double newVertical) {
		double oldVertical = vertical;
		vertical = newVertical;
		boolean oldVerticalESet = verticalESet;
		verticalESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_POSITION__VERTICAL, oldVertical, vertical, !oldVerticalESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetVertical() {
		double oldVertical = vertical;
		boolean oldVerticalESet = verticalESet;
		vertical = VERTICAL_EDEFAULT;
		verticalESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_POSITION__VERTICAL, oldVertical, VERTICAL_EDEFAULT, oldVerticalESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetVertical() {
		return verticalESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCenterX() {
		return centerX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCenterX(double newCenterX) {
		double oldCenterX = centerX;
		centerX = newCenterX;
		boolean oldCenterXESet = centerXESet;
		centerXESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_POSITION__CENTER_X, oldCenterX, centerX, !oldCenterXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetCenterX() {
		double oldCenterX = centerX;
		boolean oldCenterXESet = centerXESet;
		centerX = CENTER_X_EDEFAULT;
		centerXESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_POSITION__CENTER_X, oldCenterX, CENTER_X_EDEFAULT, oldCenterXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetCenterX() {
		return centerXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCenterZ() {
		return centerZ;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCenterZ(double newCenterZ) {
		double oldCenterZ = centerZ;
		centerZ = newCenterZ;
		boolean oldCenterZESet = centerZESet;
		centerZESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_POSITION__CENTER_Z, oldCenterZ, centerZ, !oldCenterZESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetCenterZ() {
		double oldCenterZ = centerZ;
		boolean oldCenterZESet = centerZESet;
		centerZ = CENTER_Z_EDEFAULT;
		centerZESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_POSITION__CENTER_Z, oldCenterZ, CENTER_Z_EDEFAULT, oldCenterZESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetCenterZ() {
		return centerZESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getTiltX() {
		return tiltX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTiltX(double newTiltX) {
		double oldTiltX = tiltX;
		tiltX = newTiltX;
		boolean oldTiltXESet = tiltXESet;
		tiltXESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_POSITION__TILT_X, oldTiltX, tiltX, !oldTiltXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetTiltX() {
		double oldTiltX = tiltX;
		boolean oldTiltXESet = tiltXESet;
		tiltX = TILT_X_EDEFAULT;
		tiltXESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_POSITION__TILT_X, oldTiltX, TILT_X_EDEFAULT, oldTiltXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetTiltX() {
		return tiltXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getTiltZ() {
		return tiltZ;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTiltZ(double newTiltZ) {
		double oldTiltZ = tiltZ;
		tiltZ = newTiltZ;
		boolean oldTiltZESet = tiltZESet;
		tiltZESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_POSITION__TILT_Z, oldTiltZ, tiltZ, !oldTiltZESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetTiltZ() {
		double oldTiltZ = tiltZ;
		boolean oldTiltZESet = tiltZESet;
		tiltZ = TILT_Z_EDEFAULT;
		tiltZESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_POSITION__TILT_Z, oldTiltZ, TILT_Z_EDEFAULT, oldTiltZESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetTiltZ() {
		return tiltZESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.SAMPLE_POSITION__VERTICAL:
				return getVertical();
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_X:
				return getCenterX();
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_Z:
				return getCenterZ();
			case TomoParametersPackage.SAMPLE_POSITION__TILT_X:
				return getTiltX();
			case TomoParametersPackage.SAMPLE_POSITION__TILT_Z:
				return getTiltZ();
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
			case TomoParametersPackage.SAMPLE_POSITION__VERTICAL:
				setVertical((Double)newValue);
				return;
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_X:
				setCenterX((Double)newValue);
				return;
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_Z:
				setCenterZ((Double)newValue);
				return;
			case TomoParametersPackage.SAMPLE_POSITION__TILT_X:
				setTiltX((Double)newValue);
				return;
			case TomoParametersPackage.SAMPLE_POSITION__TILT_Z:
				setTiltZ((Double)newValue);
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
			case TomoParametersPackage.SAMPLE_POSITION__VERTICAL:
				unsetVertical();
				return;
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_X:
				unsetCenterX();
				return;
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_Z:
				unsetCenterZ();
				return;
			case TomoParametersPackage.SAMPLE_POSITION__TILT_X:
				unsetTiltX();
				return;
			case TomoParametersPackage.SAMPLE_POSITION__TILT_Z:
				unsetTiltZ();
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
			case TomoParametersPackage.SAMPLE_POSITION__VERTICAL:
				return isSetVertical();
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_X:
				return isSetCenterX();
			case TomoParametersPackage.SAMPLE_POSITION__CENTER_Z:
				return isSetCenterZ();
			case TomoParametersPackage.SAMPLE_POSITION__TILT_X:
				return isSetTiltX();
			case TomoParametersPackage.SAMPLE_POSITION__TILT_Z:
				return isSetTiltZ();
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
		result.append(" (vertical: ");
		if (verticalESet) result.append(vertical); else result.append("<unset>");
		result.append(", centerX: ");
		if (centerXESet) result.append(centerX); else result.append("<unset>");
		result.append(", centerZ: ");
		if (centerZESet) result.append(centerZ); else result.append("<unset>");
		result.append(", tiltX: ");
		if (tiltXESet) result.append(tiltX); else result.append("<unset>");
		result.append(", tiltZ: ");
		if (tiltZESet) result.append(tiltZ); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //SamplePositionImpl
