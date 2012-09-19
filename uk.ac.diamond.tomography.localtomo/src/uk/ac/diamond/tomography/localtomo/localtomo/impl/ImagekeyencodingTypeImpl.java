/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.localtomo.ImagekeyencodingType;
import uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Imagekeyencoding Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl#getDarkfield <em>Darkfield</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl#getFlatfield <em>Flatfield</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.impl.ImagekeyencodingTypeImpl#getProjection <em>Projection</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImagekeyencodingTypeImpl extends EObjectImpl implements ImagekeyencodingType {
	/**
	 * The default value of the '{@link #getDarkfield() <em>Darkfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDarkfield()
	 * @generated
	 * @ordered
	 */
	protected static final int DARKFIELD_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDarkfield() <em>Darkfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDarkfield()
	 * @generated
	 * @ordered
	 */
	protected int darkfield = DARKFIELD_EDEFAULT;

	/**
	 * This is true if the Darkfield attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean darkfieldESet;

	/**
	 * The default value of the '{@link #getFlatfield() <em>Flatfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatfield()
	 * @generated
	 * @ordered
	 */
	protected static final int FLATFIELD_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFlatfield() <em>Flatfield</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatfield()
	 * @generated
	 * @ordered
	 */
	protected int flatfield = FLATFIELD_EDEFAULT;

	/**
	 * This is true if the Flatfield attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean flatfieldESet;

	/**
	 * The default value of the '{@link #getProjection() <em>Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProjection()
	 * @generated
	 * @ordered
	 */
	protected static final int PROJECTION_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getProjection() <em>Projection</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProjection()
	 * @generated
	 * @ordered
	 */
	protected int projection = PROJECTION_EDEFAULT;

	/**
	 * This is true if the Projection attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean projectionESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImagekeyencodingTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.IMAGEKEYENCODING_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDarkfield() {
		return darkfield;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDarkfield(int newDarkfield) {
		int oldDarkfield = darkfield;
		darkfield = newDarkfield;
		boolean oldDarkfieldESet = darkfieldESet;
		darkfieldESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD, oldDarkfield, darkfield, !oldDarkfieldESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetDarkfield() {
		int oldDarkfield = darkfield;
		boolean oldDarkfieldESet = darkfieldESet;
		darkfield = DARKFIELD_EDEFAULT;
		darkfieldESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD, oldDarkfield, DARKFIELD_EDEFAULT, oldDarkfieldESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetDarkfield() {
		return darkfieldESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFlatfield() {
		return flatfield;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFlatfield(int newFlatfield) {
		int oldFlatfield = flatfield;
		flatfield = newFlatfield;
		boolean oldFlatfieldESet = flatfieldESet;
		flatfieldESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD, oldFlatfield, flatfield, !oldFlatfieldESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFlatfield() {
		int oldFlatfield = flatfield;
		boolean oldFlatfieldESet = flatfieldESet;
		flatfield = FLATFIELD_EDEFAULT;
		flatfieldESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD, oldFlatfield, FLATFIELD_EDEFAULT, oldFlatfieldESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFlatfield() {
		return flatfieldESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getProjection() {
		return projection;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProjection(int newProjection) {
		int oldProjection = projection;
		projection = newProjection;
		boolean oldProjectionESet = projectionESet;
		projectionESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION, oldProjection, projection, !oldProjectionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetProjection() {
		int oldProjection = projection;
		boolean oldProjectionESet = projectionESet;
		projection = PROJECTION_EDEFAULT;
		projectionESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION, oldProjection, PROJECTION_EDEFAULT, oldProjectionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetProjection() {
		return projectionESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD:
				return getDarkfield();
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD:
				return getFlatfield();
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION:
				return getProjection();
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
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD:
				setDarkfield((Integer)newValue);
				return;
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD:
				setFlatfield((Integer)newValue);
				return;
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION:
				setProjection((Integer)newValue);
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
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD:
				unsetDarkfield();
				return;
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD:
				unsetFlatfield();
				return;
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION:
				unsetProjection();
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
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__DARKFIELD:
				return isSetDarkfield();
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__FLATFIELD:
				return isSetFlatfield();
			case LocalTomoPackage.IMAGEKEYENCODING_TYPE__PROJECTION:
				return isSetProjection();
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
		result.append(" (darkfield: ");
		if (darkfieldESet) result.append(darkfield); else result.append("<unset>");
		result.append(", flatfield: ");
		if (flatfieldESet) result.append(flatfield); else result.append("<unset>");
		result.append(", projection: ");
		if (projectionESet) result.append(projection); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //ImagekeyencodingTypeImpl
