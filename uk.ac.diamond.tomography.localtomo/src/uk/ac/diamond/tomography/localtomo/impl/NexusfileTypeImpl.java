/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.ImgkeyNXSPathType;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.NexusfileType;
import uk.ac.diamond.tomography.localtomo.ShutterNXSPathType;
import uk.ac.diamond.tomography.localtomo.StagePosNXSPathType;
import uk.ac.diamond.tomography.localtomo.StageRotNXSPathType;
import uk.ac.diamond.tomography.localtomo.TifNXSPathType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Nexusfile Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.NexusfileTypeImpl#getShutterNXSPath <em>Shutter NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.NexusfileTypeImpl#getStagePosNXSPath <em>Stage Pos NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.NexusfileTypeImpl#getStageRotNXSPath <em>Stage Rot NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.NexusfileTypeImpl#getTifNXSPath <em>Tif NXS Path</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.NexusfileTypeImpl#getImgkeyNXSPath <em>Imgkey NXS Path</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NexusfileTypeImpl extends EObjectImpl implements NexusfileType {
	/**
	 * The cached value of the '{@link #getShutterNXSPath() <em>Shutter NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutterNXSPath()
	 * @generated
	 * @ordered
	 */
	protected ShutterNXSPathType shutterNXSPath;

	/**
	 * The cached value of the '{@link #getStagePosNXSPath() <em>Stage Pos NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStagePosNXSPath()
	 * @generated
	 * @ordered
	 */
	protected StagePosNXSPathType stagePosNXSPath;

	/**
	 * The cached value of the '{@link #getStageRotNXSPath() <em>Stage Rot NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStageRotNXSPath()
	 * @generated
	 * @ordered
	 */
	protected StageRotNXSPathType stageRotNXSPath;

	/**
	 * The cached value of the '{@link #getTifNXSPath() <em>Tif NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTifNXSPath()
	 * @generated
	 * @ordered
	 */
	protected TifNXSPathType tifNXSPath;

	/**
	 * The cached value of the '{@link #getImgkeyNXSPath() <em>Imgkey NXS Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImgkeyNXSPath()
	 * @generated
	 * @ordered
	 */
	protected ImgkeyNXSPathType imgkeyNXSPath;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NexusfileTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.NEXUSFILE_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterNXSPathType getShutterNXSPath() {
		return shutterNXSPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShutterNXSPath(ShutterNXSPathType newShutterNXSPath, NotificationChain msgs) {
		ShutterNXSPathType oldShutterNXSPath = shutterNXSPath;
		shutterNXSPath = newShutterNXSPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH, oldShutterNXSPath, newShutterNXSPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShutterNXSPath(ShutterNXSPathType newShutterNXSPath) {
		if (newShutterNXSPath != shutterNXSPath) {
			NotificationChain msgs = null;
			if (shutterNXSPath != null)
				msgs = ((InternalEObject)shutterNXSPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH, null, msgs);
			if (newShutterNXSPath != null)
				msgs = ((InternalEObject)newShutterNXSPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH, null, msgs);
			msgs = basicSetShutterNXSPath(newShutterNXSPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH, newShutterNXSPath, newShutterNXSPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StagePosNXSPathType getStagePosNXSPath() {
		return stagePosNXSPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStagePosNXSPath(StagePosNXSPathType newStagePosNXSPath, NotificationChain msgs) {
		StagePosNXSPathType oldStagePosNXSPath = stagePosNXSPath;
		stagePosNXSPath = newStagePosNXSPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH, oldStagePosNXSPath, newStagePosNXSPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStagePosNXSPath(StagePosNXSPathType newStagePosNXSPath) {
		if (newStagePosNXSPath != stagePosNXSPath) {
			NotificationChain msgs = null;
			if (stagePosNXSPath != null)
				msgs = ((InternalEObject)stagePosNXSPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH, null, msgs);
			if (newStagePosNXSPath != null)
				msgs = ((InternalEObject)newStagePosNXSPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH, null, msgs);
			msgs = basicSetStagePosNXSPath(newStagePosNXSPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH, newStagePosNXSPath, newStagePosNXSPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StageRotNXSPathType getStageRotNXSPath() {
		return stageRotNXSPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStageRotNXSPath(StageRotNXSPathType newStageRotNXSPath, NotificationChain msgs) {
		StageRotNXSPathType oldStageRotNXSPath = stageRotNXSPath;
		stageRotNXSPath = newStageRotNXSPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH, oldStageRotNXSPath, newStageRotNXSPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStageRotNXSPath(StageRotNXSPathType newStageRotNXSPath) {
		if (newStageRotNXSPath != stageRotNXSPath) {
			NotificationChain msgs = null;
			if (stageRotNXSPath != null)
				msgs = ((InternalEObject)stageRotNXSPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH, null, msgs);
			if (newStageRotNXSPath != null)
				msgs = ((InternalEObject)newStageRotNXSPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH, null, msgs);
			msgs = basicSetStageRotNXSPath(newStageRotNXSPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH, newStageRotNXSPath, newStageRotNXSPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TifNXSPathType getTifNXSPath() {
		return tifNXSPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTifNXSPath(TifNXSPathType newTifNXSPath, NotificationChain msgs) {
		TifNXSPathType oldTifNXSPath = tifNXSPath;
		tifNXSPath = newTifNXSPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH, oldTifNXSPath, newTifNXSPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTifNXSPath(TifNXSPathType newTifNXSPath) {
		if (newTifNXSPath != tifNXSPath) {
			NotificationChain msgs = null;
			if (tifNXSPath != null)
				msgs = ((InternalEObject)tifNXSPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH, null, msgs);
			if (newTifNXSPath != null)
				msgs = ((InternalEObject)newTifNXSPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH, null, msgs);
			msgs = basicSetTifNXSPath(newTifNXSPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH, newTifNXSPath, newTifNXSPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImgkeyNXSPathType getImgkeyNXSPath() {
		return imgkeyNXSPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImgkeyNXSPath(ImgkeyNXSPathType newImgkeyNXSPath, NotificationChain msgs) {
		ImgkeyNXSPathType oldImgkeyNXSPath = imgkeyNXSPath;
		imgkeyNXSPath = newImgkeyNXSPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH, oldImgkeyNXSPath, newImgkeyNXSPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImgkeyNXSPath(ImgkeyNXSPathType newImgkeyNXSPath) {
		if (newImgkeyNXSPath != imgkeyNXSPath) {
			NotificationChain msgs = null;
			if (imgkeyNXSPath != null)
				msgs = ((InternalEObject)imgkeyNXSPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH, null, msgs);
			if (newImgkeyNXSPath != null)
				msgs = ((InternalEObject)newImgkeyNXSPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH, null, msgs);
			msgs = basicSetImgkeyNXSPath(newImgkeyNXSPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH, newImgkeyNXSPath, newImgkeyNXSPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH:
				return basicSetShutterNXSPath(null, msgs);
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH:
				return basicSetStagePosNXSPath(null, msgs);
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH:
				return basicSetStageRotNXSPath(null, msgs);
			case LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH:
				return basicSetTifNXSPath(null, msgs);
			case LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH:
				return basicSetImgkeyNXSPath(null, msgs);
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
			case LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH:
				return getShutterNXSPath();
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH:
				return getStagePosNXSPath();
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH:
				return getStageRotNXSPath();
			case LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH:
				return getTifNXSPath();
			case LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH:
				return getImgkeyNXSPath();
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
			case LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH:
				setShutterNXSPath((ShutterNXSPathType)newValue);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH:
				setStagePosNXSPath((StagePosNXSPathType)newValue);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH:
				setStageRotNXSPath((StageRotNXSPathType)newValue);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH:
				setTifNXSPath((TifNXSPathType)newValue);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH:
				setImgkeyNXSPath((ImgkeyNXSPathType)newValue);
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
			case LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH:
				setShutterNXSPath((ShutterNXSPathType)null);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH:
				setStagePosNXSPath((StagePosNXSPathType)null);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH:
				setStageRotNXSPath((StageRotNXSPathType)null);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH:
				setTifNXSPath((TifNXSPathType)null);
				return;
			case LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH:
				setImgkeyNXSPath((ImgkeyNXSPathType)null);
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
			case LocalTomoPackage.NEXUSFILE_TYPE__SHUTTER_NXS_PATH:
				return shutterNXSPath != null;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_POS_NXS_PATH:
				return stagePosNXSPath != null;
			case LocalTomoPackage.NEXUSFILE_TYPE__STAGE_ROT_NXS_PATH:
				return stageRotNXSPath != null;
			case LocalTomoPackage.NEXUSFILE_TYPE__TIF_NXS_PATH:
				return tifNXSPath != null;
			case LocalTomoPackage.NEXUSFILE_TYPE__IMGKEY_NXS_PATH:
				return imgkeyNXSPath != null;
		}
		return super.eIsSet(featureID);
	}

} //NexusfileTypeImpl
