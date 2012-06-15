/**
 * 
 * Copyright © 2011 Diamond Light Source Ltd.
 * 
 * This file is part of GDA.
 * 
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.FixModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Fix Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.FixModelImpl#isStatisticsEnabled <em>Statistics Enabled</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.FixModelImpl#isScaleEdgePixelsEnabled <em>Scale Edge Pixels Enabled</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FixModelImpl extends EObjectImpl implements FixModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #isStatisticsEnabled() <em>Statistics Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStatisticsEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean STATISTICS_ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isStatisticsEnabled() <em>Statistics Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStatisticsEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean statisticsEnabled = STATISTICS_ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #isScaleEdgePixelsEnabled() <em>Scale Edge Pixels Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isScaleEdgePixelsEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SCALE_EDGE_PIXELS_ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isScaleEdgePixelsEnabled() <em>Scale Edge Pixels Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isScaleEdgePixelsEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean scaleEdgePixelsEnabled = SCALE_EDGE_PIXELS_ENABLED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FixModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.FIX_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatisticsEnabled(boolean newStatisticsEnabled) {
		boolean oldStatisticsEnabled = statisticsEnabled;
		statisticsEnabled = newStatisticsEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.FIX_MODEL__STATISTICS_ENABLED, oldStatisticsEnabled, statisticsEnabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isScaleEdgePixelsEnabled() {
		return scaleEdgePixelsEnabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScaleEdgePixelsEnabled(boolean newScaleEdgePixelsEnabled) {
		boolean oldScaleEdgePixelsEnabled = scaleEdgePixelsEnabled;
		scaleEdgePixelsEnabled = newScaleEdgePixelsEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED, oldScaleEdgePixelsEnabled, scaleEdgePixelsEnabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.FIX_MODEL__STATISTICS_ENABLED:
				return isStatisticsEnabled();
			case ExcaliburConfigPackage.FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED:
				return isScaleEdgePixelsEnabled();
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
			case ExcaliburConfigPackage.FIX_MODEL__STATISTICS_ENABLED:
				setStatisticsEnabled((Boolean)newValue);
				return;
			case ExcaliburConfigPackage.FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED:
				setScaleEdgePixelsEnabled((Boolean)newValue);
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
			case ExcaliburConfigPackage.FIX_MODEL__STATISTICS_ENABLED:
				setStatisticsEnabled(STATISTICS_ENABLED_EDEFAULT);
				return;
			case ExcaliburConfigPackage.FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED:
				setScaleEdgePixelsEnabled(SCALE_EDGE_PIXELS_ENABLED_EDEFAULT);
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
			case ExcaliburConfigPackage.FIX_MODEL__STATISTICS_ENABLED:
				return statisticsEnabled != STATISTICS_ENABLED_EDEFAULT;
			case ExcaliburConfigPackage.FIX_MODEL__SCALE_EDGE_PIXELS_ENABLED:
				return scaleEdgePixelsEnabled != SCALE_EDGE_PIXELS_ENABLED_EDEFAULT;
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
		result.append(" (statisticsEnabled: ");
		result.append(statisticsEnabled);
		result.append(", scaleEdgePixelsEnabled: ");
		result.append(scaleEdgePixelsEnabled);
		result.append(')');
		return result.toString();
	}

} //FixModelImpl
