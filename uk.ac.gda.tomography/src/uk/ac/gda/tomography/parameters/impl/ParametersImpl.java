/**
 * <copyright> </copyright> $Id$
 */
package uk.ac.gda.tomography.parameters.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Parameters</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.impl.ParametersImpl#getConfigurationSet <em>Configuration Set</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ParametersImpl extends EObjectImpl implements Parameters {
	/**
	 * The cached value of the '{@link #getConfigurationSet() <em>Configuration Set</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getConfigurationSet()
	 * @generated
	 * @ordered
	 */
	protected EList<AlignmentConfiguration> configurationSet;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ParametersImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TomoParametersPackage.Literals.PARAMETERS;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AlignmentConfiguration> getConfigurationSet() {
		if (configurationSet == null) {
			configurationSet = new EObjectContainmentEList<AlignmentConfiguration>(AlignmentConfiguration.class, this, TomoParametersPackage.PARAMETERS__CONFIGURATION_SET);
		}
		return configurationSet;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated not
	 */
	@Override
	public AlignmentConfiguration getAlignmentConfiguration(String configurationId) {
		for (AlignmentConfiguration alignmentConfiguration : getConfigurationSet()) {
			if (alignmentConfiguration.getId().equals(configurationId)) {
				return alignmentConfiguration;
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public Integer getIndex(AlignmentConfiguration alignmentConfiguration) {
		for (int index = 0; index < getConfigurationSet().size(); index++) {
			if (getConfigurationSet().get(index).getId() == alignmentConfiguration.getId()) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case TomoParametersPackage.PARAMETERS__CONFIGURATION_SET:
				return ((InternalEList<?>)getConfigurationSet()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TomoParametersPackage.PARAMETERS__CONFIGURATION_SET:
				return getConfigurationSet();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TomoParametersPackage.PARAMETERS__CONFIGURATION_SET:
				getConfigurationSet().clear();
				getConfigurationSet().addAll((Collection<? extends AlignmentConfiguration>)newValue);
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
			case TomoParametersPackage.PARAMETERS__CONFIGURATION_SET:
				getConfigurationSet().clear();
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
			case TomoParametersPackage.PARAMETERS__CONFIGURATION_SET:
				return configurationSet != null && !configurationSet.isEmpty();
		}
		return super.eIsSet(featureID);
	}
	

} // ParametersImpl
