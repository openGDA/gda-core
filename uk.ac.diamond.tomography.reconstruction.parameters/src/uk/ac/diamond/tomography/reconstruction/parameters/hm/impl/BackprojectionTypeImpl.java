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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.CirclesType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ClockwiseRotationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.CoordinateSystemType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FilterType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.PolarCartesianInterpolationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TiltType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Backprojection Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getFilter <em>Filter</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getImageCentre <em>Image Centre</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getClockwiseRotation <em>Clockwise Rotation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getTilt <em>Tilt</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getCoordinateSystem <em>Coordinate System</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getCircles <em>Circles</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getROI <em>ROI</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.BackprojectionTypeImpl#getPolarCartesianInterpolation <em>Polar Cartesian Interpolation</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BackprojectionTypeImpl extends EObjectImpl implements BackprojectionType {
	/**
	 * The cached value of the '{@link #getFilter() <em>Filter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilter()
	 * @generated
	 * @ordered
	 */
	protected FilterType filter;

	/**
	 * The default value of the '{@link #getImageCentre() <em>Image Centre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageCentre()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal IMAGE_CENTRE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImageCentre() <em>Image Centre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageCentre()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal imageCentre = IMAGE_CENTRE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getClockwiseRotation() <em>Clockwise Rotation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClockwiseRotation()
	 * @generated
	 * @ordered
	 */
	protected ClockwiseRotationType clockwiseRotation;

	/**
	 * The cached value of the '{@link #getTilt() <em>Tilt</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTilt()
	 * @generated
	 * @ordered
	 */
	protected TiltType tilt;

	/**
	 * The cached value of the '{@link #getCoordinateSystem() <em>Coordinate System</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoordinateSystem()
	 * @generated
	 * @ordered
	 */
	protected CoordinateSystemType coordinateSystem;

	/**
	 * The cached value of the '{@link #getCircles() <em>Circles</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCircles()
	 * @generated
	 * @ordered
	 */
	protected CirclesType circles;

	/**
	 * The cached value of the '{@link #getROI() <em>ROI</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getROI()
	 * @generated
	 * @ordered
	 */
	protected ROIType rOI;

	/**
	 * The cached value of the '{@link #getPolarCartesianInterpolation() <em>Polar Cartesian Interpolation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolarCartesianInterpolation()
	 * @generated
	 * @ordered
	 */
	protected PolarCartesianInterpolationType polarCartesianInterpolation;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BackprojectionTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.BACKPROJECTION_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FilterType getFilter() {
		return filter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFilter(FilterType newFilter, NotificationChain msgs) {
		FilterType oldFilter = filter;
		filter = newFilter;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__FILTER, oldFilter, newFilter);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilter(FilterType newFilter) {
		if (newFilter != filter) {
			NotificationChain msgs = null;
			if (filter != null)
				msgs = ((InternalEObject)filter).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__FILTER, null, msgs);
			if (newFilter != null)
				msgs = ((InternalEObject)newFilter).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__FILTER, null, msgs);
			msgs = basicSetFilter(newFilter, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__FILTER, newFilter, newFilter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getImageCentre() {
		return imageCentre;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImageCentre(BigDecimal newImageCentre) {
		BigDecimal oldImageCentre = imageCentre;
		imageCentre = newImageCentre;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE, oldImageCentre, imageCentre));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ClockwiseRotationType getClockwiseRotation() {
		return clockwiseRotation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetClockwiseRotation(ClockwiseRotationType newClockwiseRotation, NotificationChain msgs) {
		ClockwiseRotationType oldClockwiseRotation = clockwiseRotation;
		clockwiseRotation = newClockwiseRotation;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION, oldClockwiseRotation, newClockwiseRotation);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setClockwiseRotation(ClockwiseRotationType newClockwiseRotation) {
		if (newClockwiseRotation != clockwiseRotation) {
			NotificationChain msgs = null;
			if (clockwiseRotation != null)
				msgs = ((InternalEObject)clockwiseRotation).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION, null, msgs);
			if (newClockwiseRotation != null)
				msgs = ((InternalEObject)newClockwiseRotation).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION, null, msgs);
			msgs = basicSetClockwiseRotation(newClockwiseRotation, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION, newClockwiseRotation, newClockwiseRotation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TiltType getTilt() {
		return tilt;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTilt(TiltType newTilt, NotificationChain msgs) {
		TiltType oldTilt = tilt;
		tilt = newTilt;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__TILT, oldTilt, newTilt);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTilt(TiltType newTilt) {
		if (newTilt != tilt) {
			NotificationChain msgs = null;
			if (tilt != null)
				msgs = ((InternalEObject)tilt).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__TILT, null, msgs);
			if (newTilt != null)
				msgs = ((InternalEObject)newTilt).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__TILT, null, msgs);
			msgs = basicSetTilt(newTilt, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__TILT, newTilt, newTilt));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CoordinateSystemType getCoordinateSystem() {
		return coordinateSystem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCoordinateSystem(CoordinateSystemType newCoordinateSystem, NotificationChain msgs) {
		CoordinateSystemType oldCoordinateSystem = coordinateSystem;
		coordinateSystem = newCoordinateSystem;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM, oldCoordinateSystem, newCoordinateSystem);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCoordinateSystem(CoordinateSystemType newCoordinateSystem) {
		if (newCoordinateSystem != coordinateSystem) {
			NotificationChain msgs = null;
			if (coordinateSystem != null)
				msgs = ((InternalEObject)coordinateSystem).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM, null, msgs);
			if (newCoordinateSystem != null)
				msgs = ((InternalEObject)newCoordinateSystem).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM, null, msgs);
			msgs = basicSetCoordinateSystem(newCoordinateSystem, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM, newCoordinateSystem, newCoordinateSystem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CirclesType getCircles() {
		return circles;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCircles(CirclesType newCircles, NotificationChain msgs) {
		CirclesType oldCircles = circles;
		circles = newCircles;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__CIRCLES, oldCircles, newCircles);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCircles(CirclesType newCircles) {
		if (newCircles != circles) {
			NotificationChain msgs = null;
			if (circles != null)
				msgs = ((InternalEObject)circles).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__CIRCLES, null, msgs);
			if (newCircles != null)
				msgs = ((InternalEObject)newCircles).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__CIRCLES, null, msgs);
			msgs = basicSetCircles(newCircles, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__CIRCLES, newCircles, newCircles));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ROIType getROI() {
		return rOI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetROI(ROIType newROI, NotificationChain msgs) {
		ROIType oldROI = rOI;
		rOI = newROI;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__ROI, oldROI, newROI);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setROI(ROIType newROI) {
		if (newROI != rOI) {
			NotificationChain msgs = null;
			if (rOI != null)
				msgs = ((InternalEObject)rOI).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__ROI, null, msgs);
			if (newROI != null)
				msgs = ((InternalEObject)newROI).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__ROI, null, msgs);
			msgs = basicSetROI(newROI, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__ROI, newROI, newROI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PolarCartesianInterpolationType getPolarCartesianInterpolation() {
		return polarCartesianInterpolation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPolarCartesianInterpolation(PolarCartesianInterpolationType newPolarCartesianInterpolation, NotificationChain msgs) {
		PolarCartesianInterpolationType oldPolarCartesianInterpolation = polarCartesianInterpolation;
		polarCartesianInterpolation = newPolarCartesianInterpolation;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION, oldPolarCartesianInterpolation, newPolarCartesianInterpolation);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPolarCartesianInterpolation(PolarCartesianInterpolationType newPolarCartesianInterpolation) {
		if (newPolarCartesianInterpolation != polarCartesianInterpolation) {
			NotificationChain msgs = null;
			if (polarCartesianInterpolation != null)
				msgs = ((InternalEObject)polarCartesianInterpolation).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION, null, msgs);
			if (newPolarCartesianInterpolation != null)
				msgs = ((InternalEObject)newPolarCartesianInterpolation).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION, null, msgs);
			msgs = basicSetPolarCartesianInterpolation(newPolarCartesianInterpolation, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION, newPolarCartesianInterpolation, newPolarCartesianInterpolation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
				return basicSetFilter(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
				return basicSetClockwiseRotation(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__TILT:
				return basicSetTilt(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
				return basicSetCoordinateSystem(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
				return basicSetCircles(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__ROI:
				return basicSetROI(null, msgs);
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
				return basicSetPolarCartesianInterpolation(null, msgs);
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
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
				return getFilter();
			case HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE:
				return getImageCentre();
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
				return getClockwiseRotation();
			case HmPackage.BACKPROJECTION_TYPE__TILT:
				return getTilt();
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
				return getCoordinateSystem();
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
				return getCircles();
			case HmPackage.BACKPROJECTION_TYPE__ROI:
				return getROI();
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
				return getPolarCartesianInterpolation();
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
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
				setFilter((FilterType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE:
				setImageCentre((BigDecimal)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
				setClockwiseRotation((ClockwiseRotationType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__TILT:
				setTilt((TiltType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
				setCoordinateSystem((CoordinateSystemType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
				setCircles((CirclesType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__ROI:
				setROI((ROIType)newValue);
				return;
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
				setPolarCartesianInterpolation((PolarCartesianInterpolationType)newValue);
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
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
				setFilter((FilterType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE:
				setImageCentre(IMAGE_CENTRE_EDEFAULT);
				return;
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
				setClockwiseRotation((ClockwiseRotationType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__TILT:
				setTilt((TiltType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
				setCoordinateSystem((CoordinateSystemType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
				setCircles((CirclesType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__ROI:
				setROI((ROIType)null);
				return;
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
				setPolarCartesianInterpolation((PolarCartesianInterpolationType)null);
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
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
				return filter != null;
			case HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE:
				return IMAGE_CENTRE_EDEFAULT == null ? imageCentre != null : !IMAGE_CENTRE_EDEFAULT.equals(imageCentre);
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
				return clockwiseRotation != null;
			case HmPackage.BACKPROJECTION_TYPE__TILT:
				return tilt != null;
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
				return coordinateSystem != null;
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
				return circles != null;
			case HmPackage.BACKPROJECTION_TYPE__ROI:
				return rOI != null;
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
				return polarCartesianInterpolation != null;
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
		result.append(" (imageCentre: ");
		result.append(imageCentre);
		result.append(')');
		return result.toString();
	}

} //BackprojectionTypeImpl
