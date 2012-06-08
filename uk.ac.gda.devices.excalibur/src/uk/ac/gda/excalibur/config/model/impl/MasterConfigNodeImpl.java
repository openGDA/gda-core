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

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.MasterConfigAdbaseModel;
import uk.ac.gda.excalibur.config.model.FixModel;
import uk.ac.gda.excalibur.config.model.MasterConfigNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Master Config Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MasterConfigNodeImpl#getConfigFem <em>Config Fem</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MasterConfigNodeImpl extends BaseNodeImpl implements MasterConfigNode {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The cached value of the '{@link #getConfigFem() <em>Config Fem</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConfigFem()
	 * @generated
	 * @ordered
	 */
	protected MasterConfigAdbaseModel configFem;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MasterConfigNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.MASTER_CONFIG_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MasterConfigAdbaseModel getConfigFem() {
		return configFem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetConfigFem(MasterConfigAdbaseModel newConfigFem, NotificationChain msgs) {
		MasterConfigAdbaseModel oldConfigFem = configFem;
		configFem = newConfigFem;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM, oldConfigFem, newConfigFem);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConfigFem(MasterConfigAdbaseModel newConfigFem) {
		if (newConfigFem != configFem) {
			NotificationChain msgs = null;
			if (configFem != null)
				msgs = ((InternalEObject)configFem).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM, null, msgs);
			if (newConfigFem != null)
				msgs = ((InternalEObject)newConfigFem).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM, null, msgs);
			msgs = basicSetConfigFem(newConfigFem, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM, newConfigFem, newConfigFem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM:
				return basicSetConfigFem(null, msgs);
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
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM:
				return getConfigFem();
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
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM:
				setConfigFem((MasterConfigAdbaseModel)newValue);
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
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM:
				setConfigFem((MasterConfigAdbaseModel)null);
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
			case ExcaliburConfigPackage.MASTER_CONFIG_NODE__CONFIG_FEM:
				return configFem != null;
		}
		return super.eIsSet(featureID);
	}

} //MasterConfigNodeImpl
