/*******************************************************************************
 * Copyright © 2009, 2015 Diamond Light Source Ltd
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
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Stage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stage</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getStageID <em>Stage ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getExperiment <em>Experiment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getCells <em>Cells</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getDetector_x <em>Detector x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getDetector_y <em>Detector y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getDetector_z <em>Detector z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getCamera_x <em>Camera x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getCamera_y <em>Camera y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.StageImpl#getCamera_z <em>Camera z</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StageImpl extends MinimalEObjectImpl.Container implements Stage {
	/**
	 * The default value of the '{@link #getStageID() <em>Stage ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStageID()
	 * @generated
	 * @ordered
	 */
	protected static final String STAGE_ID_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getStageID() <em>Stage ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStageID()
	 * @generated
	 * @ordered
	 */
	protected String stageID = STAGE_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getCells() <em>Cells</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCells()
	 * @generated
	 * @ordered
	 */
	protected EList<Cell> cells;

	/**
	 * The default value of the '{@link #getDetector_x() <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_x()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDetector_x() <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_x()
	 * @generated
	 * @ordered
	 */
	protected double detector_x = DETECTOR_X_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetector_y() <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_y()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_Y_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDetector_y() <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_y()
	 * @generated
	 * @ordered
	 */
	protected double detector_y = DETECTOR_Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetector_z() <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_z()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_Z_EDEFAULT = 400.0;

	/**
	 * The cached value of the '{@link #getDetector_z() <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_z()
	 * @generated
	 * @ordered
	 */
	protected double detector_z = DETECTOR_Z_EDEFAULT;

	/**
	 * The default value of the '{@link #getCamera_x() <em>Camera x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_x()
	 * @generated
	 * @ordered
	 */
	protected static final double CAMERA_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCamera_x() <em>Camera x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_x()
	 * @generated
	 * @ordered
	 */
	protected double camera_x = CAMERA_X_EDEFAULT;

	/**
	 * The default value of the '{@link #getCamera_y() <em>Camera y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_y()
	 * @generated
	 * @ordered
	 */
	protected static final double CAMERA_Y_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCamera_y() <em>Camera y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_y()
	 * @generated
	 * @ordered
	 */
	protected double camera_y = CAMERA_Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getCamera_z() <em>Camera z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_z()
	 * @generated
	 * @ordered
	 */
	protected static final double CAMERA_Z_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCamera_z() <em>Camera z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCamera_z()
	 * @generated
	 * @ordered
	 */
	protected double camera_z = CAMERA_Z_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LDEExperimentsPackage.Literals.STAGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getStageID() {
		return stageID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStageID(String newStageID) {
		String oldStageID = stageID;
		stageID = newStageID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__STAGE_ID, oldStageID, stageID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Cell> getCells() {
		if (cells == null) {
			cells = new EObjectContainmentWithInverseEList<Cell>(Cell.class, this, LDEExperimentsPackage.STAGE__CELLS, LDEExperimentsPackage.CELL__STAGE);
		}
		return cells;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_x() {
		return detector_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_x(double newDetector_x) {
		double oldDetector_x = detector_x;
		detector_x = newDetector_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__DETECTOR_X, oldDetector_x, detector_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_y() {
		return detector_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_y(double newDetector_y) {
		double oldDetector_y = detector_y;
		detector_y = newDetector_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__DETECTOR_Y, oldDetector_y, detector_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_z() {
		return detector_z;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_z(double newDetector_z) {
		double oldDetector_z = detector_z;
		detector_z = newDetector_z;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__DETECTOR_Z, oldDetector_z, detector_z));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCamera_x() {
		return camera_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCamera_x(double newCamera_x) {
		double oldCamera_x = camera_x;
		camera_x = newCamera_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__CAMERA_X, oldCamera_x, camera_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCamera_y() {
		return camera_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCamera_y(double newCamera_y) {
		double oldCamera_y = camera_y;
		camera_y = newCamera_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__CAMERA_Y, oldCamera_y, camera_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCamera_z() {
		return camera_z;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCamera_z(double newCamera_z) {
		double oldCamera_z = camera_z;
		camera_z = newCamera_z;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__CAMERA_Z, oldCamera_z, camera_z));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell getCellByID(String cellId) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetExperiment((Experiment)otherEnd, msgs);
			case LDEExperimentsPackage.STAGE__CELLS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getCells()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Experiment getExperiment() {
		if (eContainerFeatureID() != LDEExperimentsPackage.STAGE__EXPERIMENT) return null;
		return (Experiment)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetExperiment(Experiment newExperiment, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newExperiment, LDEExperimentsPackage.STAGE__EXPERIMENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExperiment(Experiment newExperiment) {
		if (newExperiment != eInternalContainer() || (eContainerFeatureID() != LDEExperimentsPackage.STAGE__EXPERIMENT && newExperiment != null)) {
			if (EcoreUtil.isAncestor(this, newExperiment))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newExperiment != null)
				msgs = ((InternalEObject)newExperiment).eInverseAdd(this, LDEExperimentsPackage.EXPERIMENT__STAGES, Experiment.class, msgs);
			msgs = basicSetExperiment(newExperiment, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.STAGE__EXPERIMENT, newExperiment, newExperiment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				return basicSetExperiment(null, msgs);
			case LDEExperimentsPackage.STAGE__CELLS:
				return ((InternalEList<?>)getCells()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				return eInternalContainer().eInverseRemove(this, LDEExperimentsPackage.EXPERIMENT__STAGES, Experiment.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LDEExperimentsPackage.STAGE__STAGE_ID:
				return getStageID();
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				return getExperiment();
			case LDEExperimentsPackage.STAGE__CELLS:
				return getCells();
			case LDEExperimentsPackage.STAGE__DETECTOR_X:
				return getDetector_x();
			case LDEExperimentsPackage.STAGE__DETECTOR_Y:
				return getDetector_y();
			case LDEExperimentsPackage.STAGE__DETECTOR_Z:
				return getDetector_z();
			case LDEExperimentsPackage.STAGE__CAMERA_X:
				return getCamera_x();
			case LDEExperimentsPackage.STAGE__CAMERA_Y:
				return getCamera_y();
			case LDEExperimentsPackage.STAGE__CAMERA_Z:
				return getCamera_z();
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
			case LDEExperimentsPackage.STAGE__STAGE_ID:
				setStageID((String)newValue);
				return;
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				setExperiment((Experiment)newValue);
				return;
			case LDEExperimentsPackage.STAGE__CELLS:
				getCells().clear();
				getCells().addAll((Collection<? extends Cell>)newValue);
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_X:
				setDetector_x((Double)newValue);
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_Y:
				setDetector_y((Double)newValue);
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_Z:
				setDetector_z((Double)newValue);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_X:
				setCamera_x((Double)newValue);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_Y:
				setCamera_y((Double)newValue);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_Z:
				setCamera_z((Double)newValue);
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
			case LDEExperimentsPackage.STAGE__STAGE_ID:
				setStageID(STAGE_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				setExperiment((Experiment)null);
				return;
			case LDEExperimentsPackage.STAGE__CELLS:
				getCells().clear();
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_X:
				setDetector_x(DETECTOR_X_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_Y:
				setDetector_y(DETECTOR_Y_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__DETECTOR_Z:
				setDetector_z(DETECTOR_Z_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_X:
				setCamera_x(CAMERA_X_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_Y:
				setCamera_y(CAMERA_Y_EDEFAULT);
				return;
			case LDEExperimentsPackage.STAGE__CAMERA_Z:
				setCamera_z(CAMERA_Z_EDEFAULT);
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
			case LDEExperimentsPackage.STAGE__STAGE_ID:
				return STAGE_ID_EDEFAULT == null ? stageID != null : !STAGE_ID_EDEFAULT.equals(stageID);
			case LDEExperimentsPackage.STAGE__EXPERIMENT:
				return getExperiment() != null;
			case LDEExperimentsPackage.STAGE__CELLS:
				return cells != null && !cells.isEmpty();
			case LDEExperimentsPackage.STAGE__DETECTOR_X:
				return detector_x != DETECTOR_X_EDEFAULT;
			case LDEExperimentsPackage.STAGE__DETECTOR_Y:
				return detector_y != DETECTOR_Y_EDEFAULT;
			case LDEExperimentsPackage.STAGE__DETECTOR_Z:
				return detector_z != DETECTOR_Z_EDEFAULT;
			case LDEExperimentsPackage.STAGE__CAMERA_X:
				return camera_x != CAMERA_X_EDEFAULT;
			case LDEExperimentsPackage.STAGE__CAMERA_Y:
				return camera_y != CAMERA_Y_EDEFAULT;
			case LDEExperimentsPackage.STAGE__CAMERA_Z:
				return camera_z != CAMERA_Z_EDEFAULT;
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
			case LDEExperimentsPackage.STAGE___GET_CELL_BY_ID__STRING:
				return getCellByID((String)arguments.get(0));
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
		result.append(" (stageID: ");
		result.append(stageID);
		result.append(", detector_x: ");
		result.append(detector_x);
		result.append(", detector_y: ");
		result.append(detector_y);
		result.append(", detector_z: ");
		result.append(detector_z);
		result.append(", camera_x: ");
		result.append(camera_x);
		result.append(", camera_y: ");
		result.append(camera_y);
		result.append(", camera_z: ");
		result.append(camera_z);
		result.append(')');
		return result.toString();
	}

} //StageImpl
