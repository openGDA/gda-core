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

import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Detector Roi</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl#getMinX <em>Min X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl#getMaxX <em>Max X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl#getMinY <em>Min Y</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorRoiImpl#getMaxY <em>Max Y</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DetectorRoiImpl extends EObjectImpl implements DetectorRoi {
	/**
	 * The default value of the '{@link #getMinX() <em>Min X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinX()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MIN_X_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMinX() <em>Min X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinX()
	 * @generated
	 * @ordered
	 */
	protected Integer minX = MIN_X_EDEFAULT;

	/**
	 * This is true if the Min X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean minXESet;

	/**
	 * The default value of the '{@link #getMaxX() <em>Max X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxX()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MAX_X_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMaxX() <em>Max X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxX()
	 * @generated
	 * @ordered
	 */
	protected Integer maxX = MAX_X_EDEFAULT;

	/**
	 * This is true if the Max X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean maxXESet;

	/**
	 * The default value of the '{@link #getMinY() <em>Min Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinY()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MIN_Y_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMinY() <em>Min Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinY()
	 * @generated
	 * @ordered
	 */
	protected Integer minY = MIN_Y_EDEFAULT;

	/**
	 * This is true if the Min Y attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean minYESet;

	/**
	 * The default value of the '{@link #getMaxY() <em>Max Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxY()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MAX_Y_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMaxY() <em>Max Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxY()
	 * @generated
	 * @ordered
	 */
	protected Integer maxY = MAX_Y_EDEFAULT;

	/**
	 * This is true if the Max Y attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean maxYESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DetectorRoiImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.DETECTOR_ROI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMinX() {
		return minX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMinX(Integer newMinX) {
		Integer oldMinX = minX;
		minX = newMinX;
		boolean oldMinXESet = minXESet;
		minXESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_ROI__MIN_X, oldMinX, minX, !oldMinXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetMinX() {
		Integer oldMinX = minX;
		boolean oldMinXESet = minXESet;
		minX = MIN_X_EDEFAULT;
		minXESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_ROI__MIN_X, oldMinX, MIN_X_EDEFAULT, oldMinXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetMinX() {
		return minXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMaxX() {
		return maxX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxX(Integer newMaxX) {
		Integer oldMaxX = maxX;
		maxX = newMaxX;
		boolean oldMaxXESet = maxXESet;
		maxXESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_ROI__MAX_X, oldMaxX, maxX, !oldMaxXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetMaxX() {
		Integer oldMaxX = maxX;
		boolean oldMaxXESet = maxXESet;
		maxX = MAX_X_EDEFAULT;
		maxXESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_ROI__MAX_X, oldMaxX, MAX_X_EDEFAULT, oldMaxXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetMaxX() {
		return maxXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMinY() {
		return minY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMinY(Integer newMinY) {
		Integer oldMinY = minY;
		minY = newMinY;
		boolean oldMinYESet = minYESet;
		minYESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_ROI__MIN_Y, oldMinY, minY, !oldMinYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetMinY() {
		Integer oldMinY = minY;
		boolean oldMinYESet = minYESet;
		minY = MIN_Y_EDEFAULT;
		minYESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_ROI__MIN_Y, oldMinY, MIN_Y_EDEFAULT, oldMinYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetMinY() {
		return minYESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMaxY() {
		return maxY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxY(Integer newMaxY) {
		Integer oldMaxY = maxY;
		maxY = newMaxY;
		boolean oldMaxYESet = maxYESet;
		maxYESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_ROI__MAX_Y, oldMaxY, maxY, !oldMaxYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetMaxY() {
		Integer oldMaxY = maxY;
		boolean oldMaxYESet = maxYESet;
		maxY = MAX_Y_EDEFAULT;
		maxYESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_ROI__MAX_Y, oldMaxY, MAX_Y_EDEFAULT, oldMaxYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetMaxY() {
		return maxYESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.DETECTOR_ROI__MIN_X:
				return getMinX();
			case TomoParametersPackage.DETECTOR_ROI__MAX_X:
				return getMaxX();
			case TomoParametersPackage.DETECTOR_ROI__MIN_Y:
				return getMinY();
			case TomoParametersPackage.DETECTOR_ROI__MAX_Y:
				return getMaxY();
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
			case TomoParametersPackage.DETECTOR_ROI__MIN_X:
				setMinX((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_ROI__MAX_X:
				setMaxX((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_ROI__MIN_Y:
				setMinY((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_ROI__MAX_Y:
				setMaxY((Integer)newValue);
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
			case TomoParametersPackage.DETECTOR_ROI__MIN_X:
				unsetMinX();
				return;
			case TomoParametersPackage.DETECTOR_ROI__MAX_X:
				unsetMaxX();
				return;
			case TomoParametersPackage.DETECTOR_ROI__MIN_Y:
				unsetMinY();
				return;
			case TomoParametersPackage.DETECTOR_ROI__MAX_Y:
				unsetMaxY();
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
			case TomoParametersPackage.DETECTOR_ROI__MIN_X:
				return isSetMinX();
			case TomoParametersPackage.DETECTOR_ROI__MAX_X:
				return isSetMaxX();
			case TomoParametersPackage.DETECTOR_ROI__MIN_Y:
				return isSetMinY();
			case TomoParametersPackage.DETECTOR_ROI__MAX_Y:
				return isSetMaxY();
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
		result.append(" (minX: ");
		if (minXESet) result.append(minX); else result.append("<unset>");
		result.append(", maxX: ");
		if (maxXESet) result.append(maxX); else result.append("<unset>");
		result.append(", minY: ");
		if (minYESet) result.append(minY); else result.append("<unset>");
		result.append(", maxY: ");
		if (maxYESet) result.append(maxY); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //DetectorRoiImpl
