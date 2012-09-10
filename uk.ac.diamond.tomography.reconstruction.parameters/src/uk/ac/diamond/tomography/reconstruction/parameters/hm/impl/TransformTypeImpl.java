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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.ExtrapolationTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InterpolationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MissedProjectionsTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleEndPointsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RotationAngleTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ScaleTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Transform Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getMissedProjections <em>Missed Projections</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getMissedProjectionsType <em>Missed Projections Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getRotationAngleType <em>Rotation Angle Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getRotationAngle <em>Rotation Angle</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getRotationAngleEndPoints <em>Rotation Angle End Points</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getReCentreAngle <em>Re Centre Angle</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getReCentreRadius <em>Re Centre Radius</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getCropTop <em>Crop Top</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getCropBottom <em>Crop Bottom</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getCropLeft <em>Crop Left</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getCropRight <em>Crop Right</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getScaleType <em>Scale Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getScaleWidth <em>Scale Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getScaleHeight <em>Scale Height</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getExtrapolationType <em>Extrapolation Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getExtrapolationPixels <em>Extrapolation Pixels</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getExtrapolationWidth <em>Extrapolation Width</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.TransformTypeImpl#getInterpolation <em>Interpolation</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TransformTypeImpl extends EObjectImpl implements TransformType {
	/**
	 * The cached value of the '{@link #getMissedProjections() <em>Missed Projections</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMissedProjections()
	 * @generated
	 * @ordered
	 */
	protected MissedProjectionsType missedProjections;

	/**
	 * The cached value of the '{@link #getMissedProjectionsType() <em>Missed Projections Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMissedProjectionsType()
	 * @generated
	 * @ordered
	 */
	protected MissedProjectionsTypeType missedProjectionsType;

	/**
	 * The cached value of the '{@link #getRotationAngleType() <em>Rotation Angle Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRotationAngleType()
	 * @generated
	 * @ordered
	 */
	protected RotationAngleTypeType rotationAngleType;

	/**
	 * The default value of the '{@link #getRotationAngle() <em>Rotation Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRotationAngle()
	 * @generated
	 * @ordered
	 */
	protected static final int ROTATION_ANGLE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getRotationAngle() <em>Rotation Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRotationAngle()
	 * @generated
	 * @ordered
	 */
	protected int rotationAngle = ROTATION_ANGLE_EDEFAULT;

	/**
	 * This is true if the Rotation Angle attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean rotationAngleESet;

	/**
	 * The cached value of the '{@link #getRotationAngleEndPoints() <em>Rotation Angle End Points</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRotationAngleEndPoints()
	 * @generated
	 * @ordered
	 */
	protected RotationAngleEndPointsType rotationAngleEndPoints;

	/**
	 * The default value of the '{@link #getReCentreAngle() <em>Re Centre Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReCentreAngle()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal RE_CENTRE_ANGLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReCentreAngle() <em>Re Centre Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReCentreAngle()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal reCentreAngle = RE_CENTRE_ANGLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getReCentreRadius() <em>Re Centre Radius</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReCentreRadius()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal RE_CENTRE_RADIUS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReCentreRadius() <em>Re Centre Radius</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReCentreRadius()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal reCentreRadius = RE_CENTRE_RADIUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getCropTop() <em>Crop Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropTop()
	 * @generated
	 * @ordered
	 */
	protected static final int CROP_TOP_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCropTop() <em>Crop Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropTop()
	 * @generated
	 * @ordered
	 */
	protected int cropTop = CROP_TOP_EDEFAULT;

	/**
	 * This is true if the Crop Top attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean cropTopESet;

	/**
	 * The default value of the '{@link #getCropBottom() <em>Crop Bottom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropBottom()
	 * @generated
	 * @ordered
	 */
	protected static final int CROP_BOTTOM_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCropBottom() <em>Crop Bottom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropBottom()
	 * @generated
	 * @ordered
	 */
	protected int cropBottom = CROP_BOTTOM_EDEFAULT;

	/**
	 * This is true if the Crop Bottom attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean cropBottomESet;

	/**
	 * The default value of the '{@link #getCropLeft() <em>Crop Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropLeft()
	 * @generated
	 * @ordered
	 */
	protected static final int CROP_LEFT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCropLeft() <em>Crop Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropLeft()
	 * @generated
	 * @ordered
	 */
	protected int cropLeft = CROP_LEFT_EDEFAULT;

	/**
	 * This is true if the Crop Left attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean cropLeftESet;

	/**
	 * The default value of the '{@link #getCropRight() <em>Crop Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropRight()
	 * @generated
	 * @ordered
	 */
	protected static final int CROP_RIGHT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCropRight() <em>Crop Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCropRight()
	 * @generated
	 * @ordered
	 */
	protected int cropRight = CROP_RIGHT_EDEFAULT;

	/**
	 * This is true if the Crop Right attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean cropRightESet;

	/**
	 * The cached value of the '{@link #getScaleType() <em>Scale Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScaleType()
	 * @generated
	 * @ordered
	 */
	protected ScaleTypeType scaleType;

	/**
	 * The default value of the '{@link #getScaleWidth() <em>Scale Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScaleWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int SCALE_WIDTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getScaleWidth() <em>Scale Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScaleWidth()
	 * @generated
	 * @ordered
	 */
	protected int scaleWidth = SCALE_WIDTH_EDEFAULT;

	/**
	 * This is true if the Scale Width attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean scaleWidthESet;

	/**
	 * The default value of the '{@link #getScaleHeight() <em>Scale Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScaleHeight()
	 * @generated
	 * @ordered
	 */
	protected static final int SCALE_HEIGHT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getScaleHeight() <em>Scale Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScaleHeight()
	 * @generated
	 * @ordered
	 */
	protected int scaleHeight = SCALE_HEIGHT_EDEFAULT;

	/**
	 * This is true if the Scale Height attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean scaleHeightESet;

	/**
	 * The cached value of the '{@link #getExtrapolationType() <em>Extrapolation Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtrapolationType()
	 * @generated
	 * @ordered
	 */
	protected ExtrapolationTypeType extrapolationType;

	/**
	 * The default value of the '{@link #getExtrapolationPixels() <em>Extrapolation Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtrapolationPixels()
	 * @generated
	 * @ordered
	 */
	protected static final int EXTRAPOLATION_PIXELS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getExtrapolationPixels() <em>Extrapolation Pixels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtrapolationPixels()
	 * @generated
	 * @ordered
	 */
	protected int extrapolationPixels = EXTRAPOLATION_PIXELS_EDEFAULT;

	/**
	 * This is true if the Extrapolation Pixels attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean extrapolationPixelsESet;

	/**
	 * The default value of the '{@link #getExtrapolationWidth() <em>Extrapolation Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtrapolationWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int EXTRAPOLATION_WIDTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getExtrapolationWidth() <em>Extrapolation Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtrapolationWidth()
	 * @generated
	 * @ordered
	 */
	protected int extrapolationWidth = EXTRAPOLATION_WIDTH_EDEFAULT;

	/**
	 * This is true if the Extrapolation Width attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean extrapolationWidthESet;

	/**
	 * The cached value of the '{@link #getInterpolation() <em>Interpolation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInterpolation()
	 * @generated
	 * @ordered
	 */
	protected InterpolationType interpolation;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TransformTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.TRANSFORM_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MissedProjectionsType getMissedProjections() {
		return missedProjections;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMissedProjections(MissedProjectionsType newMissedProjections, NotificationChain msgs) {
		MissedProjectionsType oldMissedProjections = missedProjections;
		missedProjections = newMissedProjections;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS, oldMissedProjections, newMissedProjections);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMissedProjections(MissedProjectionsType newMissedProjections) {
		if (newMissedProjections != missedProjections) {
			NotificationChain msgs = null;
			if (missedProjections != null)
				msgs = ((InternalEObject)missedProjections).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS, null, msgs);
			if (newMissedProjections != null)
				msgs = ((InternalEObject)newMissedProjections).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS, null, msgs);
			msgs = basicSetMissedProjections(newMissedProjections, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS, newMissedProjections, newMissedProjections));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MissedProjectionsTypeType getMissedProjectionsType() {
		return missedProjectionsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMissedProjectionsType(MissedProjectionsTypeType newMissedProjectionsType, NotificationChain msgs) {
		MissedProjectionsTypeType oldMissedProjectionsType = missedProjectionsType;
		missedProjectionsType = newMissedProjectionsType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE, oldMissedProjectionsType, newMissedProjectionsType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMissedProjectionsType(MissedProjectionsTypeType newMissedProjectionsType) {
		if (newMissedProjectionsType != missedProjectionsType) {
			NotificationChain msgs = null;
			if (missedProjectionsType != null)
				msgs = ((InternalEObject)missedProjectionsType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE, null, msgs);
			if (newMissedProjectionsType != null)
				msgs = ((InternalEObject)newMissedProjectionsType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE, null, msgs);
			msgs = basicSetMissedProjectionsType(newMissedProjectionsType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE, newMissedProjectionsType, newMissedProjectionsType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RotationAngleTypeType getRotationAngleType() {
		return rotationAngleType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRotationAngleType(RotationAngleTypeType newRotationAngleType, NotificationChain msgs) {
		RotationAngleTypeType oldRotationAngleType = rotationAngleType;
		rotationAngleType = newRotationAngleType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE, oldRotationAngleType, newRotationAngleType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRotationAngleType(RotationAngleTypeType newRotationAngleType) {
		if (newRotationAngleType != rotationAngleType) {
			NotificationChain msgs = null;
			if (rotationAngleType != null)
				msgs = ((InternalEObject)rotationAngleType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE, null, msgs);
			if (newRotationAngleType != null)
				msgs = ((InternalEObject)newRotationAngleType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE, null, msgs);
			msgs = basicSetRotationAngleType(newRotationAngleType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE, newRotationAngleType, newRotationAngleType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getRotationAngle() {
		return rotationAngle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRotationAngle(int newRotationAngle) {
		int oldRotationAngle = rotationAngle;
		rotationAngle = newRotationAngle;
		boolean oldRotationAngleESet = rotationAngleESet;
		rotationAngleESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE, oldRotationAngle, rotationAngle, !oldRotationAngleESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRotationAngle() {
		int oldRotationAngle = rotationAngle;
		boolean oldRotationAngleESet = rotationAngleESet;
		rotationAngle = ROTATION_ANGLE_EDEFAULT;
		rotationAngleESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE, oldRotationAngle, ROTATION_ANGLE_EDEFAULT, oldRotationAngleESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRotationAngle() {
		return rotationAngleESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RotationAngleEndPointsType getRotationAngleEndPoints() {
		return rotationAngleEndPoints;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRotationAngleEndPoints(RotationAngleEndPointsType newRotationAngleEndPoints, NotificationChain msgs) {
		RotationAngleEndPointsType oldRotationAngleEndPoints = rotationAngleEndPoints;
		rotationAngleEndPoints = newRotationAngleEndPoints;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS, oldRotationAngleEndPoints, newRotationAngleEndPoints);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRotationAngleEndPoints(RotationAngleEndPointsType newRotationAngleEndPoints) {
		if (newRotationAngleEndPoints != rotationAngleEndPoints) {
			NotificationChain msgs = null;
			if (rotationAngleEndPoints != null)
				msgs = ((InternalEObject)rotationAngleEndPoints).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS, null, msgs);
			if (newRotationAngleEndPoints != null)
				msgs = ((InternalEObject)newRotationAngleEndPoints).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS, null, msgs);
			msgs = basicSetRotationAngleEndPoints(newRotationAngleEndPoints, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS, newRotationAngleEndPoints, newRotationAngleEndPoints));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getReCentreAngle() {
		return reCentreAngle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReCentreAngle(BigDecimal newReCentreAngle) {
		BigDecimal oldReCentreAngle = reCentreAngle;
		reCentreAngle = newReCentreAngle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE, oldReCentreAngle, reCentreAngle));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getReCentreRadius() {
		return reCentreRadius;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReCentreRadius(BigDecimal newReCentreRadius) {
		BigDecimal oldReCentreRadius = reCentreRadius;
		reCentreRadius = newReCentreRadius;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS, oldReCentreRadius, reCentreRadius));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCropTop() {
		return cropTop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCropTop(int newCropTop) {
		int oldCropTop = cropTop;
		cropTop = newCropTop;
		boolean oldCropTopESet = cropTopESet;
		cropTopESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__CROP_TOP, oldCropTop, cropTop, !oldCropTopESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCropTop() {
		int oldCropTop = cropTop;
		boolean oldCropTopESet = cropTopESet;
		cropTop = CROP_TOP_EDEFAULT;
		cropTopESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__CROP_TOP, oldCropTop, CROP_TOP_EDEFAULT, oldCropTopESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCropTop() {
		return cropTopESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCropBottom() {
		return cropBottom;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCropBottom(int newCropBottom) {
		int oldCropBottom = cropBottom;
		cropBottom = newCropBottom;
		boolean oldCropBottomESet = cropBottomESet;
		cropBottomESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__CROP_BOTTOM, oldCropBottom, cropBottom, !oldCropBottomESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCropBottom() {
		int oldCropBottom = cropBottom;
		boolean oldCropBottomESet = cropBottomESet;
		cropBottom = CROP_BOTTOM_EDEFAULT;
		cropBottomESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__CROP_BOTTOM, oldCropBottom, CROP_BOTTOM_EDEFAULT, oldCropBottomESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCropBottom() {
		return cropBottomESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCropLeft() {
		return cropLeft;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCropLeft(int newCropLeft) {
		int oldCropLeft = cropLeft;
		cropLeft = newCropLeft;
		boolean oldCropLeftESet = cropLeftESet;
		cropLeftESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__CROP_LEFT, oldCropLeft, cropLeft, !oldCropLeftESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCropLeft() {
		int oldCropLeft = cropLeft;
		boolean oldCropLeftESet = cropLeftESet;
		cropLeft = CROP_LEFT_EDEFAULT;
		cropLeftESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__CROP_LEFT, oldCropLeft, CROP_LEFT_EDEFAULT, oldCropLeftESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCropLeft() {
		return cropLeftESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCropRight() {
		return cropRight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCropRight(int newCropRight) {
		int oldCropRight = cropRight;
		cropRight = newCropRight;
		boolean oldCropRightESet = cropRightESet;
		cropRightESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__CROP_RIGHT, oldCropRight, cropRight, !oldCropRightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCropRight() {
		int oldCropRight = cropRight;
		boolean oldCropRightESet = cropRightESet;
		cropRight = CROP_RIGHT_EDEFAULT;
		cropRightESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__CROP_RIGHT, oldCropRight, CROP_RIGHT_EDEFAULT, oldCropRightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCropRight() {
		return cropRightESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ScaleTypeType getScaleType() {
		return scaleType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetScaleType(ScaleTypeType newScaleType, NotificationChain msgs) {
		ScaleTypeType oldScaleType = scaleType;
		scaleType = newScaleType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__SCALE_TYPE, oldScaleType, newScaleType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setScaleType(ScaleTypeType newScaleType) {
		if (newScaleType != scaleType) {
			NotificationChain msgs = null;
			if (scaleType != null)
				msgs = ((InternalEObject)scaleType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__SCALE_TYPE, null, msgs);
			if (newScaleType != null)
				msgs = ((InternalEObject)newScaleType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__SCALE_TYPE, null, msgs);
			msgs = basicSetScaleType(newScaleType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__SCALE_TYPE, newScaleType, newScaleType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getScaleWidth() {
		return scaleWidth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setScaleWidth(int newScaleWidth) {
		int oldScaleWidth = scaleWidth;
		scaleWidth = newScaleWidth;
		boolean oldScaleWidthESet = scaleWidthESet;
		scaleWidthESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__SCALE_WIDTH, oldScaleWidth, scaleWidth, !oldScaleWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetScaleWidth() {
		int oldScaleWidth = scaleWidth;
		boolean oldScaleWidthESet = scaleWidthESet;
		scaleWidth = SCALE_WIDTH_EDEFAULT;
		scaleWidthESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__SCALE_WIDTH, oldScaleWidth, SCALE_WIDTH_EDEFAULT, oldScaleWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetScaleWidth() {
		return scaleWidthESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getScaleHeight() {
		return scaleHeight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setScaleHeight(int newScaleHeight) {
		int oldScaleHeight = scaleHeight;
		scaleHeight = newScaleHeight;
		boolean oldScaleHeightESet = scaleHeightESet;
		scaleHeightESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT, oldScaleHeight, scaleHeight, !oldScaleHeightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetScaleHeight() {
		int oldScaleHeight = scaleHeight;
		boolean oldScaleHeightESet = scaleHeightESet;
		scaleHeight = SCALE_HEIGHT_EDEFAULT;
		scaleHeightESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT, oldScaleHeight, SCALE_HEIGHT_EDEFAULT, oldScaleHeightESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetScaleHeight() {
		return scaleHeightESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExtrapolationTypeType getExtrapolationType() {
		return extrapolationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetExtrapolationType(ExtrapolationTypeType newExtrapolationType, NotificationChain msgs) {
		ExtrapolationTypeType oldExtrapolationType = extrapolationType;
		extrapolationType = newExtrapolationType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE, oldExtrapolationType, newExtrapolationType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExtrapolationType(ExtrapolationTypeType newExtrapolationType) {
		if (newExtrapolationType != extrapolationType) {
			NotificationChain msgs = null;
			if (extrapolationType != null)
				msgs = ((InternalEObject)extrapolationType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE, null, msgs);
			if (newExtrapolationType != null)
				msgs = ((InternalEObject)newExtrapolationType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE, null, msgs);
			msgs = basicSetExtrapolationType(newExtrapolationType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE, newExtrapolationType, newExtrapolationType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getExtrapolationPixels() {
		return extrapolationPixels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExtrapolationPixels(int newExtrapolationPixels) {
		int oldExtrapolationPixels = extrapolationPixels;
		extrapolationPixels = newExtrapolationPixels;
		boolean oldExtrapolationPixelsESet = extrapolationPixelsESet;
		extrapolationPixelsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS, oldExtrapolationPixels, extrapolationPixels, !oldExtrapolationPixelsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetExtrapolationPixels() {
		int oldExtrapolationPixels = extrapolationPixels;
		boolean oldExtrapolationPixelsESet = extrapolationPixelsESet;
		extrapolationPixels = EXTRAPOLATION_PIXELS_EDEFAULT;
		extrapolationPixelsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS, oldExtrapolationPixels, EXTRAPOLATION_PIXELS_EDEFAULT, oldExtrapolationPixelsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetExtrapolationPixels() {
		return extrapolationPixelsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getExtrapolationWidth() {
		return extrapolationWidth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExtrapolationWidth(int newExtrapolationWidth) {
		int oldExtrapolationWidth = extrapolationWidth;
		extrapolationWidth = newExtrapolationWidth;
		boolean oldExtrapolationWidthESet = extrapolationWidthESet;
		extrapolationWidthESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH, oldExtrapolationWidth, extrapolationWidth, !oldExtrapolationWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetExtrapolationWidth() {
		int oldExtrapolationWidth = extrapolationWidth;
		boolean oldExtrapolationWidthESet = extrapolationWidthESet;
		extrapolationWidth = EXTRAPOLATION_WIDTH_EDEFAULT;
		extrapolationWidthESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH, oldExtrapolationWidth, EXTRAPOLATION_WIDTH_EDEFAULT, oldExtrapolationWidthESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetExtrapolationWidth() {
		return extrapolationWidthESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InterpolationType getInterpolation() {
		return interpolation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInterpolation(InterpolationType newInterpolation, NotificationChain msgs) {
		InterpolationType oldInterpolation = interpolation;
		interpolation = newInterpolation;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__INTERPOLATION, oldInterpolation, newInterpolation);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInterpolation(InterpolationType newInterpolation) {
		if (newInterpolation != interpolation) {
			NotificationChain msgs = null;
			if (interpolation != null)
				msgs = ((InternalEObject)interpolation).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__INTERPOLATION, null, msgs);
			if (newInterpolation != null)
				msgs = ((InternalEObject)newInterpolation).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.TRANSFORM_TYPE__INTERPOLATION, null, msgs);
			msgs = basicSetInterpolation(newInterpolation, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.TRANSFORM_TYPE__INTERPOLATION, newInterpolation, newInterpolation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
				return basicSetMissedProjections(null, msgs);
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
				return basicSetMissedProjectionsType(null, msgs);
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
				return basicSetRotationAngleType(null, msgs);
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
				return basicSetRotationAngleEndPoints(null, msgs);
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
				return basicSetScaleType(null, msgs);
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
				return basicSetExtrapolationType(null, msgs);
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
				return basicSetInterpolation(null, msgs);
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
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
				return getMissedProjections();
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
				return getMissedProjectionsType();
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
				return getRotationAngleType();
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE:
				return getRotationAngle();
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
				return getRotationAngleEndPoints();
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE:
				return getReCentreAngle();
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS:
				return getReCentreRadius();
			case HmPackage.TRANSFORM_TYPE__CROP_TOP:
				return getCropTop();
			case HmPackage.TRANSFORM_TYPE__CROP_BOTTOM:
				return getCropBottom();
			case HmPackage.TRANSFORM_TYPE__CROP_LEFT:
				return getCropLeft();
			case HmPackage.TRANSFORM_TYPE__CROP_RIGHT:
				return getCropRight();
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
				return getScaleType();
			case HmPackage.TRANSFORM_TYPE__SCALE_WIDTH:
				return getScaleWidth();
			case HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT:
				return getScaleHeight();
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
				return getExtrapolationType();
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS:
				return getExtrapolationPixels();
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH:
				return getExtrapolationWidth();
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
				return getInterpolation();
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
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
				setMissedProjections((MissedProjectionsType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
				setMissedProjectionsType((MissedProjectionsTypeType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
				setRotationAngleType((RotationAngleTypeType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE:
				setRotationAngle((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
				setRotationAngleEndPoints((RotationAngleEndPointsType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE:
				setReCentreAngle((BigDecimal)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS:
				setReCentreRadius((BigDecimal)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_TOP:
				setCropTop((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_BOTTOM:
				setCropBottom((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_LEFT:
				setCropLeft((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_RIGHT:
				setCropRight((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
				setScaleType((ScaleTypeType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_WIDTH:
				setScaleWidth((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT:
				setScaleHeight((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
				setExtrapolationType((ExtrapolationTypeType)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS:
				setExtrapolationPixels((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH:
				setExtrapolationWidth((Integer)newValue);
				return;
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
				setInterpolation((InterpolationType)newValue);
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
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
				setMissedProjections((MissedProjectionsType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
				setMissedProjectionsType((MissedProjectionsTypeType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
				setRotationAngleType((RotationAngleTypeType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE:
				unsetRotationAngle();
				return;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
				setRotationAngleEndPoints((RotationAngleEndPointsType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE:
				setReCentreAngle(RE_CENTRE_ANGLE_EDEFAULT);
				return;
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS:
				setReCentreRadius(RE_CENTRE_RADIUS_EDEFAULT);
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_TOP:
				unsetCropTop();
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_BOTTOM:
				unsetCropBottom();
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_LEFT:
				unsetCropLeft();
				return;
			case HmPackage.TRANSFORM_TYPE__CROP_RIGHT:
				unsetCropRight();
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
				setScaleType((ScaleTypeType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_WIDTH:
				unsetScaleWidth();
				return;
			case HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT:
				unsetScaleHeight();
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
				setExtrapolationType((ExtrapolationTypeType)null);
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS:
				unsetExtrapolationPixels();
				return;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH:
				unsetExtrapolationWidth();
				return;
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
				setInterpolation((InterpolationType)null);
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
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
				return missedProjections != null;
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
				return missedProjectionsType != null;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
				return rotationAngleType != null;
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE:
				return isSetRotationAngle();
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
				return rotationAngleEndPoints != null;
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE:
				return RE_CENTRE_ANGLE_EDEFAULT == null ? reCentreAngle != null : !RE_CENTRE_ANGLE_EDEFAULT.equals(reCentreAngle);
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS:
				return RE_CENTRE_RADIUS_EDEFAULT == null ? reCentreRadius != null : !RE_CENTRE_RADIUS_EDEFAULT.equals(reCentreRadius);
			case HmPackage.TRANSFORM_TYPE__CROP_TOP:
				return isSetCropTop();
			case HmPackage.TRANSFORM_TYPE__CROP_BOTTOM:
				return isSetCropBottom();
			case HmPackage.TRANSFORM_TYPE__CROP_LEFT:
				return isSetCropLeft();
			case HmPackage.TRANSFORM_TYPE__CROP_RIGHT:
				return isSetCropRight();
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
				return scaleType != null;
			case HmPackage.TRANSFORM_TYPE__SCALE_WIDTH:
				return isSetScaleWidth();
			case HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT:
				return isSetScaleHeight();
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
				return extrapolationType != null;
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS:
				return isSetExtrapolationPixels();
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH:
				return isSetExtrapolationWidth();
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
				return interpolation != null;
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
		result.append(" (rotationAngle: ");
		if (rotationAngleESet) result.append(rotationAngle); else result.append("<unset>");
		result.append(", reCentreAngle: ");
		result.append(reCentreAngle);
		result.append(", reCentreRadius: ");
		result.append(reCentreRadius);
		result.append(", cropTop: ");
		if (cropTopESet) result.append(cropTop); else result.append("<unset>");
		result.append(", cropBottom: ");
		if (cropBottomESet) result.append(cropBottom); else result.append("<unset>");
		result.append(", cropLeft: ");
		if (cropLeftESet) result.append(cropLeft); else result.append("<unset>");
		result.append(", cropRight: ");
		if (cropRightESet) result.append(cropRight); else result.append("<unset>");
		result.append(", scaleWidth: ");
		if (scaleWidthESet) result.append(scaleWidth); else result.append("<unset>");
		result.append(", scaleHeight: ");
		if (scaleHeightESet) result.append(scaleHeight); else result.append("<unset>");
		result.append(", extrapolationPixels: ");
		if (extrapolationPixelsESet) result.append(extrapolationPixels); else result.append("<unset>");
		result.append(", extrapolationWidth: ");
		if (extrapolationWidthESet) result.append(extrapolationWidth); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //TransformTypeImpl
