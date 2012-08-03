/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.parameters.SampleStage;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.ValueUnit;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample Stage</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getVertical <em>Vertical</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getCenterX <em>Center X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getCenterZ <em>Center Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getTiltX <em>Tilt X</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getTiltZ <em>Tilt Z</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.SampleStageImpl#getBaseX <em>Base X</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleStageImpl extends EObjectImpl implements SampleStage {
	/**
	 * The cached value of the '{@link #getVertical() <em>Vertical</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVertical()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit vertical;

	/**
	 * This is true if the Vertical containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean verticalESet;

	/**
	 * The cached value of the '{@link #getCenterX() <em>Center X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterX()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit centerX;

	/**
	 * This is true if the Center X containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean centerXESet;

	/**
	 * The cached value of the '{@link #getCenterZ() <em>Center Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCenterZ()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit centerZ;

	/**
	 * This is true if the Center Z containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean centerZESet;

	/**
	 * The cached value of the '{@link #getTiltX() <em>Tilt X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltX()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit tiltX;

	/**
	 * This is true if the Tilt X containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tiltXESet;

	/**
	 * The cached value of the '{@link #getTiltZ() <em>Tilt Z</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTiltZ()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit tiltZ;

	/**
	 * This is true if the Tilt Z containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tiltZESet;

	/**
	 * The cached value of the '{@link #getBaseX() <em>Base X</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseX()
	 * @generated
	 * @ordered
	 */
	protected ValueUnit baseX;

	/**
	 * This is true if the Base X containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean baseXESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SampleStageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.SAMPLE_STAGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getVertical() {
		return vertical;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetVertical(ValueUnit newVertical, NotificationChain msgs) {
		ValueUnit oldVertical = vertical;
		vertical = newVertical;
		boolean oldVerticalESet = verticalESet;
		verticalESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__VERTICAL, oldVertical, newVertical, !oldVerticalESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVertical(ValueUnit newVertical) {
		if (newVertical != vertical) {
			NotificationChain msgs = null;
			if (vertical != null)
				msgs = ((InternalEObject)vertical).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__VERTICAL, null, msgs);
			if (newVertical != null)
				msgs = ((InternalEObject)newVertical).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__VERTICAL, null, msgs);
			msgs = basicSetVertical(newVertical, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldVerticalESet = verticalESet;
			verticalESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__VERTICAL, newVertical, newVertical, !oldVerticalESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetVertical(NotificationChain msgs) {
		ValueUnit oldVertical = vertical;
		vertical = null;
		boolean oldVerticalESet = verticalESet;
		verticalESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__VERTICAL, oldVertical, null, oldVerticalESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetVertical() {
		if (vertical != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)vertical).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__VERTICAL, null, msgs);
			msgs = basicUnsetVertical(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldVerticalESet = verticalESet;
			verticalESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__VERTICAL, null, null, oldVerticalESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetVertical() {
		return verticalESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getCenterX() {
		return centerX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCenterX(ValueUnit newCenterX, NotificationChain msgs) {
		ValueUnit oldCenterX = centerX;
		centerX = newCenterX;
		boolean oldCenterXESet = centerXESet;
		centerXESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__CENTER_X, oldCenterX, newCenterX, !oldCenterXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCenterX(ValueUnit newCenterX) {
		if (newCenterX != centerX) {
			NotificationChain msgs = null;
			if (centerX != null)
				msgs = ((InternalEObject)centerX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_X, null, msgs);
			if (newCenterX != null)
				msgs = ((InternalEObject)newCenterX).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_X, null, msgs);
			msgs = basicSetCenterX(newCenterX, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldCenterXESet = centerXESet;
			centerXESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__CENTER_X, newCenterX, newCenterX, !oldCenterXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetCenterX(NotificationChain msgs) {
		ValueUnit oldCenterX = centerX;
		centerX = null;
		boolean oldCenterXESet = centerXESet;
		centerXESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__CENTER_X, oldCenterX, null, oldCenterXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCenterX() {
		if (centerX != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)centerX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_X, null, msgs);
			msgs = basicUnsetCenterX(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldCenterXESet = centerXESet;
			centerXESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__CENTER_X, null, null, oldCenterXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCenterX() {
		return centerXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getCenterZ() {
		return centerZ;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCenterZ(ValueUnit newCenterZ, NotificationChain msgs) {
		ValueUnit oldCenterZ = centerZ;
		centerZ = newCenterZ;
		boolean oldCenterZESet = centerZESet;
		centerZESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, oldCenterZ, newCenterZ, !oldCenterZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCenterZ(ValueUnit newCenterZ) {
		if (newCenterZ != centerZ) {
			NotificationChain msgs = null;
			if (centerZ != null)
				msgs = ((InternalEObject)centerZ).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, null, msgs);
			if (newCenterZ != null)
				msgs = ((InternalEObject)newCenterZ).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, null, msgs);
			msgs = basicSetCenterZ(newCenterZ, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldCenterZESet = centerZESet;
			centerZESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, newCenterZ, newCenterZ, !oldCenterZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetCenterZ(NotificationChain msgs) {
		ValueUnit oldCenterZ = centerZ;
		centerZ = null;
		boolean oldCenterZESet = centerZESet;
		centerZESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, oldCenterZ, null, oldCenterZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCenterZ() {
		if (centerZ != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)centerZ).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, null, msgs);
			msgs = basicUnsetCenterZ(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldCenterZESet = centerZESet;
			centerZESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__CENTER_Z, null, null, oldCenterZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCenterZ() {
		return centerZESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getTiltX() {
		return tiltX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTiltX(ValueUnit newTiltX, NotificationChain msgs) {
		ValueUnit oldTiltX = tiltX;
		tiltX = newTiltX;
		boolean oldTiltXESet = tiltXESet;
		tiltXESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__TILT_X, oldTiltX, newTiltX, !oldTiltXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTiltX(ValueUnit newTiltX) {
		if (newTiltX != tiltX) {
			NotificationChain msgs = null;
			if (tiltX != null)
				msgs = ((InternalEObject)tiltX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_X, null, msgs);
			if (newTiltX != null)
				msgs = ((InternalEObject)newTiltX).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_X, null, msgs);
			msgs = basicSetTiltX(newTiltX, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldTiltXESet = tiltXESet;
			tiltXESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__TILT_X, newTiltX, newTiltX, !oldTiltXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetTiltX(NotificationChain msgs) {
		ValueUnit oldTiltX = tiltX;
		tiltX = null;
		boolean oldTiltXESet = tiltXESet;
		tiltXESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__TILT_X, oldTiltX, null, oldTiltXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTiltX() {
		if (tiltX != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)tiltX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_X, null, msgs);
			msgs = basicUnsetTiltX(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldTiltXESet = tiltXESet;
			tiltXESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__TILT_X, null, null, oldTiltXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTiltX() {
		return tiltXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getTiltZ() {
		return tiltZ;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTiltZ(ValueUnit newTiltZ, NotificationChain msgs) {
		ValueUnit oldTiltZ = tiltZ;
		tiltZ = newTiltZ;
		boolean oldTiltZESet = tiltZESet;
		tiltZESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__TILT_Z, oldTiltZ, newTiltZ, !oldTiltZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTiltZ(ValueUnit newTiltZ) {
		if (newTiltZ != tiltZ) {
			NotificationChain msgs = null;
			if (tiltZ != null)
				msgs = ((InternalEObject)tiltZ).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_Z, null, msgs);
			if (newTiltZ != null)
				msgs = ((InternalEObject)newTiltZ).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_Z, null, msgs);
			msgs = basicSetTiltZ(newTiltZ, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldTiltZESet = tiltZESet;
			tiltZESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__TILT_Z, newTiltZ, newTiltZ, !oldTiltZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetTiltZ(NotificationChain msgs) {
		ValueUnit oldTiltZ = tiltZ;
		tiltZ = null;
		boolean oldTiltZESet = tiltZESet;
		tiltZESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__TILT_Z, oldTiltZ, null, oldTiltZESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTiltZ() {
		if (tiltZ != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)tiltZ).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__TILT_Z, null, msgs);
			msgs = basicUnsetTiltZ(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldTiltZESet = tiltZESet;
			tiltZESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__TILT_Z, null, null, oldTiltZESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTiltZ() {
		return tiltZESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueUnit getBaseX() {
		return baseX;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBaseX(ValueUnit newBaseX, NotificationChain msgs) {
		ValueUnit oldBaseX = baseX;
		baseX = newBaseX;
		boolean oldBaseXESet = baseXESet;
		baseXESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__BASE_X, oldBaseX, newBaseX, !oldBaseXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBaseX(ValueUnit newBaseX) {
		if (newBaseX != baseX) {
			NotificationChain msgs = null;
			if (baseX != null)
				msgs = ((InternalEObject)baseX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__BASE_X, null, msgs);
			if (newBaseX != null)
				msgs = ((InternalEObject)newBaseX).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__BASE_X, null, msgs);
			msgs = basicSetBaseX(newBaseX, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldBaseXESet = baseXESet;
			baseXESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, TomoParametersPackage.SAMPLE_STAGE__BASE_X, newBaseX, newBaseX, !oldBaseXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetBaseX(NotificationChain msgs) {
		ValueUnit oldBaseX = baseX;
		baseX = null;
		boolean oldBaseXESet = baseXESet;
		baseXESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__BASE_X, oldBaseX, null, oldBaseXESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetBaseX() {
		if (baseX != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)baseX).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TomoParametersPackage.SAMPLE_STAGE__BASE_X, null, msgs);
			msgs = basicUnsetBaseX(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldBaseXESet = baseXESet;
			baseXESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, TomoParametersPackage.SAMPLE_STAGE__BASE_X, null, null, oldBaseXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetBaseX() {
		return baseXESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.SAMPLE_STAGE__VERTICAL:
				return basicUnsetVertical(msgs);
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_X:
				return basicUnsetCenterX(msgs);
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_Z:
				return basicUnsetCenterZ(msgs);
			case TomoParametersPackage.SAMPLE_STAGE__TILT_X:
				return basicUnsetTiltX(msgs);
			case TomoParametersPackage.SAMPLE_STAGE__TILT_Z:
				return basicUnsetTiltZ(msgs);
			case TomoParametersPackage.SAMPLE_STAGE__BASE_X:
				return basicUnsetBaseX(msgs);
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
			case TomoParametersPackage.SAMPLE_STAGE__VERTICAL:
				return getVertical();
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_X:
				return getCenterX();
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_Z:
				return getCenterZ();
			case TomoParametersPackage.SAMPLE_STAGE__TILT_X:
				return getTiltX();
			case TomoParametersPackage.SAMPLE_STAGE__TILT_Z:
				return getTiltZ();
			case TomoParametersPackage.SAMPLE_STAGE__BASE_X:
				return getBaseX();
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
			case TomoParametersPackage.SAMPLE_STAGE__VERTICAL:
				setVertical((ValueUnit)newValue);
				return;
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_X:
				setCenterX((ValueUnit)newValue);
				return;
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_Z:
				setCenterZ((ValueUnit)newValue);
				return;
			case TomoParametersPackage.SAMPLE_STAGE__TILT_X:
				setTiltX((ValueUnit)newValue);
				return;
			case TomoParametersPackage.SAMPLE_STAGE__TILT_Z:
				setTiltZ((ValueUnit)newValue);
				return;
			case TomoParametersPackage.SAMPLE_STAGE__BASE_X:
				setBaseX((ValueUnit)newValue);
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
			case TomoParametersPackage.SAMPLE_STAGE__VERTICAL:
				unsetVertical();
				return;
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_X:
				unsetCenterX();
				return;
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_Z:
				unsetCenterZ();
				return;
			case TomoParametersPackage.SAMPLE_STAGE__TILT_X:
				unsetTiltX();
				return;
			case TomoParametersPackage.SAMPLE_STAGE__TILT_Z:
				unsetTiltZ();
				return;
			case TomoParametersPackage.SAMPLE_STAGE__BASE_X:
				unsetBaseX();
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
			case TomoParametersPackage.SAMPLE_STAGE__VERTICAL:
				return isSetVertical();
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_X:
				return isSetCenterX();
			case TomoParametersPackage.SAMPLE_STAGE__CENTER_Z:
				return isSetCenterZ();
			case TomoParametersPackage.SAMPLE_STAGE__TILT_X:
				return isSetTiltX();
			case TomoParametersPackage.SAMPLE_STAGE__TILT_Z:
				return isSetTiltZ();
			case TomoParametersPackage.SAMPLE_STAGE__BASE_X:
				return isSetBaseX();
		}
		return super.eIsSet(featureID);
	}

} //SampleStageImpl
