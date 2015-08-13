/*******************************************************************************
 * Copyright © 2009, 2014 Diamond Light Source Ltd
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
 *
 * Contributors:
 * 	Diamond Light Source Ltd
 *******************************************************************************/
/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.exceptions.NotFoundException;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Experiment Definition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl#getExperiments <em>Experiments</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl#getFilename <em>Filename</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ExperimentDefinitionImpl extends MinimalEObjectImpl.Container implements ExperimentDefinition {
	/**
	 * The cached value of the '{@link #getExperiments() <em>Experiments</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExperiments()
	 * @generated
	 * @ordered
	 */
	protected EList<Experiment> experiments;
	/**
	 * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_EDEFAULT = "samples";
	/**
	 * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilename()
	 * @generated
	 * @ordered
	 */
	protected String filename = FILENAME_EDEFAULT;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExperimentDefinitionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LDEExperimentsPackage.Literals.EXPERIMENT_DEFINITION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Experiment> getExperiments() {
		if (experiments == null) {
			experiments = new EObjectContainmentEList<Experiment>(Experiment.class, this, LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS);
		}
		return experiments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilename(String newFilename) {
		String oldFilename = filename;
		filename = newFilename;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.EXPERIMENT_DEFINITION__FILENAME, oldFilename, filename));
	}

	/**
	 * <!-- begin-user-doc -->
	 * get the named instance of {@link Experiment}
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Experiment getExperiment(String name) {
		for (Experiment experiment : getExperiments()) {
			if (experiment.getName().equals(name)) {
				return experiment;
			}
		}
		throw new NotFoundException("Named experiment '" +name+"' is not available." );
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS:
				return ((InternalEList<?>)getExperiments()).basicRemove(otherEnd, msgs);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS:
				return getExperiments();
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__FILENAME:
				return getFilename();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS:
				getExperiments().clear();
				getExperiments().addAll((Collection<? extends Experiment>)newValue);
				return;
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__FILENAME:
				setFilename((String)newValue);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS:
				getExperiments().clear();
				return;
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__FILENAME:
				setFilename(FILENAME_EDEFAULT);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__EXPERIMENTS:
				return experiments != null && !experiments.isEmpty();
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__FILENAME:
				return FILENAME_EDEFAULT == null ? filename != null : !FILENAME_EDEFAULT.equals(filename);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION___GET_EXPERIMENT__STRING:
				return getExperiment((String)arguments.get(0));
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (filename: ");
		result.append(filename);
		result.append(')');
		return result.toString();
	}

} //ExperimentDefinitionImpl
