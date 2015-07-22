/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.SampleList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Experiment Definition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.ExperimentDefinitionImpl#getSamplelist <em>Samplelist</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExperimentDefinitionImpl extends MinimalEObjectImpl.Container implements ExperimentDefinition {
	/**
	 * The cached value of the '{@link #getSamplelist() <em>Samplelist</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSamplelist()
	 * @generated
	 * @ordered
	 */
	protected SampleList samplelist;

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
	@Override
	public SampleList getSamplelist() {
		return samplelist;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSamplelist(SampleList newSamplelist, NotificationChain msgs) {
		SampleList oldSamplelist = samplelist;
		samplelist = newSamplelist;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST, oldSamplelist, newSamplelist);
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
	public void setSamplelist(SampleList newSamplelist) {
		if (newSamplelist != samplelist) {
			NotificationChain msgs = null;
			if (samplelist != null)
				msgs = ((InternalEObject)samplelist).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST, null, msgs);
			if (newSamplelist != null)
				msgs = ((InternalEObject)newSamplelist).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST, null, msgs);
			msgs = basicSetSamplelist(newSamplelist, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST, newSamplelist, newSamplelist));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST:
				return basicSetSamplelist(null, msgs);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST:
				return getSamplelist();
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST:
				setSamplelist((SampleList)newValue);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST:
				setSamplelist((SampleList)null);
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
			case LDEExperimentsPackage.EXPERIMENT_DEFINITION__SAMPLELIST:
				return samplelist != null;
		}
		return super.eIsSet(featureID);
	}

} //ExperimentDefinitionImpl
