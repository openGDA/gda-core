/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.AnperModel;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel;
import uk.ac.gda.excalibur.config.model.PixelModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mpxiii Chip Reg Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getAnper <em>Anper</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getPixel <em>Pixel</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#isChipDisable <em>Chip Disable</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MpxiiiChipRegModelImpl extends EObjectImpl implements MpxiiiChipRegModel {
	/**
	 * The cached value of the '{@link #getAnper() <em>Anper</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAnper()
	 * @generated
	 * @ordered
	 */
	protected AnperModel anper;

	/**
	 * The cached value of the '{@link #getPixel() <em>Pixel</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixel()
	 * @generated
	 * @ordered
	 */
	protected PixelModel pixel;

	/**
	 * The default value of the '{@link #isChipDisable() <em>Chip Disable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isChipDisable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CHIP_DISABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isChipDisable() <em>Chip Disable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isChipDisable()
	 * @generated
	 * @ordered
	 */
	protected boolean chipDisable = CHIP_DISABLE_EDEFAULT;

	/**
	 * This is true if the Chip Disable attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean chipDisableESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MpxiiiChipRegModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.MPXIII_CHIP_REG_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnperModel getAnper() {
		return anper;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAnper(AnperModel newAnper, NotificationChain msgs) {
		AnperModel oldAnper = anper;
		anper = newAnper;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER, oldAnper, newAnper);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAnper(AnperModel newAnper) {
		if (newAnper != anper) {
			NotificationChain msgs = null;
			if (anper != null)
				msgs = ((InternalEObject)anper).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER, null, msgs);
			if (newAnper != null)
				msgs = ((InternalEObject)newAnper).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER, null, msgs);
			msgs = basicSetAnper(newAnper, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER, newAnper, newAnper));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PixelModel getPixel() {
		return pixel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPixel(PixelModel newPixel, NotificationChain msgs) {
		PixelModel oldPixel = pixel;
		pixel = newPixel;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL, oldPixel, newPixel);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPixel(PixelModel newPixel) {
		if (newPixel != pixel) {
			NotificationChain msgs = null;
			if (pixel != null)
				msgs = ((InternalEObject)pixel).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL, null, msgs);
			if (newPixel != null)
				msgs = ((InternalEObject)newPixel).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL, null, msgs);
			msgs = basicSetPixel(newPixel, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL, newPixel, newPixel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isChipDisable() {
		return chipDisable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChipDisable(boolean newChipDisable) {
		boolean oldChipDisable = chipDisable;
		chipDisable = newChipDisable;
		boolean oldChipDisableESet = chipDisableESet;
		chipDisableESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE, oldChipDisable, chipDisable, !oldChipDisableESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetChipDisable() {
		boolean oldChipDisable = chipDisable;
		boolean oldChipDisableESet = chipDisableESet;
		chipDisable = CHIP_DISABLE_EDEFAULT;
		chipDisableESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE, oldChipDisable, CHIP_DISABLE_EDEFAULT, oldChipDisableESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetChipDisable() {
		return chipDisableESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				return basicSetAnper(null, msgs);
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				return basicSetPixel(null, msgs);
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				return getAnper();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				return getPixel();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE:
				return isChipDisable();
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				setAnper((AnperModel)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				setPixel((PixelModel)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE:
				setChipDisable((Boolean)newValue);
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				setAnper((AnperModel)null);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				setPixel((PixelModel)null);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE:
				unsetChipDisable();
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				return anper != null;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				return pixel != null;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__CHIP_DISABLE:
				return isSetChipDisable();
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
		result.append(" (chipDisable: ");
		if (chipDisableESet) result.append(chipDisable); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //MpxiiiChipRegModelImpl
