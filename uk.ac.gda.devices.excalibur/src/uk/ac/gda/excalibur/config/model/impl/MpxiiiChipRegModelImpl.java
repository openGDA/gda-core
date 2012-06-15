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
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacSense <em>Dac Sense</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacSenseDecode <em>Dac Sense Decode</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacSenseName <em>Dac Sense Name</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacExternal <em>Dac External</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacExternalDecode <em>Dac External Decode</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getDacExternalName <em>Dac External Name</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getAnper <em>Anper</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiChipRegModelImpl#getPixel <em>Pixel</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MpxiiiChipRegModelImpl extends EObjectImpl implements MpxiiiChipRegModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getDacSense() <em>Dac Sense</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSense()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_SENSE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacSense() <em>Dac Sense</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSense()
	 * @generated
	 * @ordered
	 */
	protected int dacSense = DAC_SENSE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacSenseDecode() <em>Dac Sense Decode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSenseDecode()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_SENSE_DECODE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacSenseDecode() <em>Dac Sense Decode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSenseDecode()
	 * @generated
	 * @ordered
	 */
	protected int dacSenseDecode = DAC_SENSE_DECODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacSenseName() <em>Dac Sense Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSenseName()
	 * @generated
	 * @ordered
	 */
	protected static final String DAC_SENSE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDacSenseName() <em>Dac Sense Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacSenseName()
	 * @generated
	 * @ordered
	 */
	protected String dacSenseName = DAC_SENSE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacExternal() <em>Dac External</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternal()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_EXTERNAL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacExternal() <em>Dac External</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternal()
	 * @generated
	 * @ordered
	 */
	protected int dacExternal = DAC_EXTERNAL_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacExternalDecode() <em>Dac External Decode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternalDecode()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_EXTERNAL_DECODE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacExternalDecode() <em>Dac External Decode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternalDecode()
	 * @generated
	 * @ordered
	 */
	protected int dacExternalDecode = DAC_EXTERNAL_DECODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacExternalName() <em>Dac External Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternalName()
	 * @generated
	 * @ordered
	 */
	protected static final String DAC_EXTERNAL_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDacExternalName() <em>Dac External Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacExternalName()
	 * @generated
	 * @ordered
	 */
	protected String dacExternalName = DAC_EXTERNAL_NAME_EDEFAULT;

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
	public int getDacSense() {
		return dacSense;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacSense(int newDacSense) {
		int oldDacSense = dacSense;
		dacSense = newDacSense;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE, oldDacSense, dacSense));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacSenseDecode() {
		return dacSenseDecode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacSenseDecode(int newDacSenseDecode) {
		int oldDacSenseDecode = dacSenseDecode;
		dacSenseDecode = newDacSenseDecode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE, oldDacSenseDecode, dacSenseDecode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDacSenseName() {
		return dacSenseName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacSenseName(String newDacSenseName) {
		String oldDacSenseName = dacSenseName;
		dacSenseName = newDacSenseName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME, oldDacSenseName, dacSenseName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacExternal() {
		return dacExternal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacExternal(int newDacExternal) {
		int oldDacExternal = dacExternal;
		dacExternal = newDacExternal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL, oldDacExternal, dacExternal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacExternalDecode() {
		return dacExternalDecode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacExternalDecode(int newDacExternalDecode) {
		int oldDacExternalDecode = dacExternalDecode;
		dacExternalDecode = newDacExternalDecode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE, oldDacExternalDecode, dacExternalDecode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDacExternalName() {
		return dacExternalName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacExternalName(String newDacExternalName) {
		String oldDacExternalName = dacExternalName;
		dacExternalName = newDacExternalName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME, oldDacExternalName, dacExternalName));
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE:
				return getDacSense();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE:
				return getDacSenseDecode();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME:
				return getDacSenseName();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL:
				return getDacExternal();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE:
				return getDacExternalDecode();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME:
				return getDacExternalName();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				return getAnper();
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				return getPixel();
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE:
				setDacSense((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE:
				setDacSenseDecode((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME:
				setDacSenseName((String)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL:
				setDacExternal((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE:
				setDacExternalDecode((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME:
				setDacExternalName((String)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				setAnper((AnperModel)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				setPixel((PixelModel)newValue);
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE:
				setDacSense(DAC_SENSE_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE:
				setDacSenseDecode(DAC_SENSE_DECODE_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME:
				setDacSenseName(DAC_SENSE_NAME_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL:
				setDacExternal(DAC_EXTERNAL_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE:
				setDacExternalDecode(DAC_EXTERNAL_DECODE_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME:
				setDacExternalName(DAC_EXTERNAL_NAME_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				setAnper((AnperModel)null);
				return;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				setPixel((PixelModel)null);
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
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE:
				return dacSense != DAC_SENSE_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_DECODE:
				return dacSenseDecode != DAC_SENSE_DECODE_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_SENSE_NAME:
				return DAC_SENSE_NAME_EDEFAULT == null ? dacSenseName != null : !DAC_SENSE_NAME_EDEFAULT.equals(dacSenseName);
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL:
				return dacExternal != DAC_EXTERNAL_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_DECODE:
				return dacExternalDecode != DAC_EXTERNAL_DECODE_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__DAC_EXTERNAL_NAME:
				return DAC_EXTERNAL_NAME_EDEFAULT == null ? dacExternalName != null : !DAC_EXTERNAL_NAME_EDEFAULT.equals(dacExternalName);
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__ANPER:
				return anper != null;
			case ExcaliburConfigPackage.MPXIII_CHIP_REG_MODEL__PIXEL:
				return pixel != null;
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
		result.append(" (dacSense: ");
		result.append(dacSense);
		result.append(", dacSenseDecode: ");
		result.append(dacSenseDecode);
		result.append(", dacSenseName: ");
		result.append(dacSenseName);
		result.append(", dacExternal: ");
		result.append(dacExternal);
		result.append(", dacExternalDecode: ");
		result.append(dacExternalDecode);
		result.append(", dacExternalName: ");
		result.append(dacExternalName);
		result.append(')');
		return result.toString();
	}

} //MpxiiiChipRegModelImpl
