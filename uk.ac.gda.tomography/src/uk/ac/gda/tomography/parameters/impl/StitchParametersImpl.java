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

import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stitch Parameters</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.StitchParametersImpl#getStitchingThetaAngle <em>Stitching Theta Angle</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.StitchParametersImpl#getImageAtTheta <em>Image At Theta</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.StitchParametersImpl#getImageAtThetaPlus90 <em>Image At Theta Plus90</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StitchParametersImpl extends EObjectImpl implements StitchParameters {
	/**
	 * The default value of the '{@link #getStitchingThetaAngle() <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStitchingThetaAngle()
	 * @generated
	 * @ordered
	 */
	protected static final double STITCHING_THETA_ANGLE_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getStitchingThetaAngle() <em>Stitching Theta Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStitchingThetaAngle()
	 * @generated
	 * @ordered
	 */
	protected double stitchingThetaAngle = STITCHING_THETA_ANGLE_EDEFAULT;

	/**
	 * This is true if the Stitching Theta Angle attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean stitchingThetaAngleESet;

	/**
	 * The default value of the '{@link #getImageAtTheta() <em>Image At Theta</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageAtTheta()
	 * @generated
	 * @ordered
	 */
	protected static final String IMAGE_AT_THETA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImageAtTheta() <em>Image At Theta</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageAtTheta()
	 * @generated
	 * @ordered
	 */
	protected String imageAtTheta = IMAGE_AT_THETA_EDEFAULT;

	/**
	 * The default value of the '{@link #getImageAtThetaPlus90() <em>Image At Theta Plus90</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageAtThetaPlus90()
	 * @generated
	 * @ordered
	 */
	protected static final String IMAGE_AT_THETA_PLUS90_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImageAtThetaPlus90() <em>Image At Theta Plus90</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageAtThetaPlus90()
	 * @generated
	 * @ordered
	 */
	protected String imageAtThetaPlus90 = IMAGE_AT_THETA_PLUS90_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StitchParametersImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.STITCH_PARAMETERS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getStitchingThetaAngle() {
		return stitchingThetaAngle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStitchingThetaAngle(double newStitchingThetaAngle) {
		double oldStitchingThetaAngle = stitchingThetaAngle;
		stitchingThetaAngle = newStitchingThetaAngle;
		boolean oldStitchingThetaAngleESet = stitchingThetaAngleESet;
		stitchingThetaAngleESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE, oldStitchingThetaAngle, stitchingThetaAngle, !oldStitchingThetaAngleESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetStitchingThetaAngle() {
		double oldStitchingThetaAngle = stitchingThetaAngle;
		boolean oldStitchingThetaAngleESet = stitchingThetaAngleESet;
		stitchingThetaAngle = STITCHING_THETA_ANGLE_EDEFAULT;
		stitchingThetaAngleESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE, oldStitchingThetaAngle, STITCHING_THETA_ANGLE_EDEFAULT, oldStitchingThetaAngleESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetStitchingThetaAngle() {
		return stitchingThetaAngleESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getImageAtTheta() {
		return imageAtTheta;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImageAtTheta(String newImageAtTheta) {
		String oldImageAtTheta = imageAtTheta;
		imageAtTheta = newImageAtTheta;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA, oldImageAtTheta, imageAtTheta));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getImageAtThetaPlus90() {
		return imageAtThetaPlus90;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImageAtThetaPlus90(String newImageAtThetaPlus90) {
		String oldImageAtThetaPlus90 = imageAtThetaPlus90;
		imageAtThetaPlus90 = newImageAtThetaPlus90;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90, oldImageAtThetaPlus90, imageAtThetaPlus90));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE:
				return getStitchingThetaAngle();
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA:
				return getImageAtTheta();
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90:
				return getImageAtThetaPlus90();
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
			case TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE:
				setStitchingThetaAngle((Double)newValue);
				return;
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA:
				setImageAtTheta((String)newValue);
				return;
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90:
				setImageAtThetaPlus90((String)newValue);
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
			case TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE:
				unsetStitchingThetaAngle();
				return;
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA:
				setImageAtTheta(IMAGE_AT_THETA_EDEFAULT);
				return;
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90:
				setImageAtThetaPlus90(IMAGE_AT_THETA_PLUS90_EDEFAULT);
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
			case TomoParametersPackage.STITCH_PARAMETERS__STITCHING_THETA_ANGLE:
				return isSetStitchingThetaAngle();
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA:
				return IMAGE_AT_THETA_EDEFAULT == null ? imageAtTheta != null : !IMAGE_AT_THETA_EDEFAULT.equals(imageAtTheta);
			case TomoParametersPackage.STITCH_PARAMETERS__IMAGE_AT_THETA_PLUS90:
				return IMAGE_AT_THETA_PLUS90_EDEFAULT == null ? imageAtThetaPlus90 != null : !IMAGE_AT_THETA_PLUS90_EDEFAULT.equals(imageAtThetaPlus90);
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
		result.append(" (stitchingThetaAngle: ");
		if (stitchingThetaAngleESet) result.append(stitchingThetaAngle); else result.append("<unset>");
		result.append(", imageAtTheta: ");
		result.append(imageAtTheta);
		result.append(", imageAtThetaPlus90: ");
		result.append(imageAtThetaPlus90);
		result.append(')');
		return result.toString();
	}

} //StitchParametersImpl
