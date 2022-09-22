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

import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Detector Bin</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl#getBinX <em>Bin X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.DetectorBinImpl#getBinY <em>Bin Y</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DetectorBinImpl extends EObjectImpl implements DetectorBin {
	/**
	 * The default value of the '{@link #getBinX() <em>Bin X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinX()
	 * @generated
	 * @ordered
	 */
	protected static final Integer BIN_X_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBinX() <em>Bin X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinX()
	 * @generated
	 * @ordered
	 */
	protected Integer binX = BIN_X_EDEFAULT;

	/**
	 * This is true if the Bin X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean binXESet;

	/**
	 * The default value of the '{@link #getBinY() <em>Bin Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinY()
	 * @generated
	 * @ordered
	 */
	protected static final Integer BIN_Y_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBinY() <em>Bin Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinY()
	 * @generated
	 * @ordered
	 */
	protected Integer binY = BIN_Y_EDEFAULT;

	/**
	 * This is true if the Bin Y attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean binYESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DetectorBinImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.DETECTOR_BIN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getBinX() {
		return binX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBinX(Integer newBinX) {
		Integer oldBinX = binX;
		binX = newBinX;
		boolean oldBinXESet = binXESet;
		binXESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_BIN__BIN_X, oldBinX, binX, !oldBinXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetBinX() {
		Integer oldBinX = binX;
		boolean oldBinXESet = binXESet;
		binX = BIN_X_EDEFAULT;
		binXESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_BIN__BIN_X, oldBinX, BIN_X_EDEFAULT, oldBinXESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetBinX() {
		return binXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getBinY() {
		return binY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBinY(Integer newBinY) {
		Integer oldBinY = binY;
		binY = newBinY;
		boolean oldBinYESet = binYESet;
		binYESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.DETECTOR_BIN__BIN_Y, oldBinY, binY, !oldBinYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetBinY() {
		Integer oldBinY = binY;
		boolean oldBinYESet = binYESet;
		binY = BIN_Y_EDEFAULT;
		binYESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.DETECTOR_BIN__BIN_Y, oldBinY, BIN_Y_EDEFAULT, oldBinYESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetBinY() {
		return binYESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.DETECTOR_BIN__BIN_X:
				return getBinX();
			case TomoParametersPackage.DETECTOR_BIN__BIN_Y:
				return getBinY();
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
			case TomoParametersPackage.DETECTOR_BIN__BIN_X:
				setBinX((Integer)newValue);
				return;
			case TomoParametersPackage.DETECTOR_BIN__BIN_Y:
				setBinY((Integer)newValue);
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
			case TomoParametersPackage.DETECTOR_BIN__BIN_X:
				unsetBinX();
				return;
			case TomoParametersPackage.DETECTOR_BIN__BIN_Y:
				unsetBinY();
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
			case TomoParametersPackage.DETECTOR_BIN__BIN_X:
				return isSetBinX();
			case TomoParametersPackage.DETECTOR_BIN__BIN_Y:
				return isSetBinY();
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
		result.append(" (binX: ");
		if (binXESet) result.append(binX); else result.append("<unset>");
		result.append(", binY: ");
		if (binYESet) result.append(binY); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //DetectorBinImpl
