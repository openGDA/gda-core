/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.GapModel;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Gap Model</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.GapModelImpl#getGapFillConstant <em>Gap Fill Constant</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.GapModelImpl#isGapFillingEnabled <em>Gap Filling Enabled</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.GapModelImpl#getGapFillMode <em>Gap Fill Mode</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GapModelImpl extends EObjectImpl implements GapModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getGapFillConstant() <em>Gap Fill Constant</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getGapFillConstant()
	 * @generated
	 * @ordered
	 */
	protected static final int GAP_FILL_CONSTANT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGapFillConstant() <em>Gap Fill Constant</em>}' attribute.
	 * <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * @see #getGapFillConstant()
	 * @generated
	 * @ordered
	 */
	protected int gapFillConstant = GAP_FILL_CONSTANT_EDEFAULT;

	/**
	 * The default value of the '{@link #isGapFillingEnabled() <em>Gap Filling Enabled</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isGapFillingEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean GAP_FILLING_ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isGapFillingEnabled() <em>Gap Filling Enabled</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isGapFillingEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean gapFillingEnabled = GAP_FILLING_ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #getGapFillMode() <em>Gap Fill Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGapFillMode()
	 * @generated
	 * @ordered
	 */
	protected static final int GAP_FILL_MODE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGapFillMode() <em>Gap Fill Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGapFillMode()
	 * @generated
	 * @ordered
	 */
	protected int gapFillMode = GAP_FILL_MODE_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected GapModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.GAP_MODEL;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public int getGapFillConstant() {
		return gapFillConstant;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setGapFillConstant(int newGapFillConstant) {
		int oldGapFillConstant = gapFillConstant;
		gapFillConstant = newGapFillConstant;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_CONSTANT, oldGapFillConstant, gapFillConstant));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isGapFillingEnabled() {
		return gapFillingEnabled;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setGapFillingEnabled(boolean newGapFillingEnabled) {
		boolean oldGapFillingEnabled = gapFillingEnabled;
		gapFillingEnabled = newGapFillingEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.GAP_MODEL__GAP_FILLING_ENABLED, oldGapFillingEnabled, gapFillingEnabled));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public int getGapFillMode() {
		return gapFillMode;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setGapFillMode(int newGapFillMode) {
		int oldGapFillMode = gapFillMode;
		gapFillMode = newGapFillMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_MODE, oldGapFillMode, gapFillMode));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_CONSTANT:
				return getGapFillConstant();
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILLING_ENABLED:
				return isGapFillingEnabled();
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_MODE:
				return getGapFillMode();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_CONSTANT:
				setGapFillConstant((Integer)newValue);
				return;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILLING_ENABLED:
				setGapFillingEnabled((Boolean)newValue);
				return;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_MODE:
				setGapFillMode((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_CONSTANT:
				setGapFillConstant(GAP_FILL_CONSTANT_EDEFAULT);
				return;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILLING_ENABLED:
				setGapFillingEnabled(GAP_FILLING_ENABLED_EDEFAULT);
				return;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_MODE:
				setGapFillMode(GAP_FILL_MODE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_CONSTANT:
				return gapFillConstant != GAP_FILL_CONSTANT_EDEFAULT;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILLING_ENABLED:
				return gapFillingEnabled != GAP_FILLING_ENABLED_EDEFAULT;
			case ExcaliburConfigPackage.GAP_MODEL__GAP_FILL_MODE:
				return gapFillMode != GAP_FILL_MODE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (gapFillConstant: ");
		result.append(gapFillConstant);
		result.append(", gapFillingEnabled: ");
		result.append(gapFillingEnabled);
		result.append(", gapFillMode: ");
		result.append(gapFillMode);
		result.append(')');
		return result.toString();
	}

} // GapModelImpl
