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

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.SummaryAdbaseModel;
import uk.ac.gda.excalibur.config.model.SummaryNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Summary Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.SummaryNodeImpl#getSummaryFem <em>Summary Fem</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SummaryNodeImpl extends EObjectImpl implements SummaryNode {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The cached value of the '{@link #getSummaryFem() <em>Summary Fem</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummaryFem()
	 * @generated
	 * @ordered
	 */
	protected SummaryAdbaseModel summaryFem;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SummaryNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.SUMMARY_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SummaryAdbaseModel getSummaryFem() {
		return summaryFem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSummaryFem(SummaryAdbaseModel newSummaryFem, NotificationChain msgs) {
		SummaryAdbaseModel oldSummaryFem = summaryFem;
		summaryFem = newSummaryFem;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM, oldSummaryFem, newSummaryFem);
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
	public void setSummaryFem(SummaryAdbaseModel newSummaryFem) {
		if (newSummaryFem != summaryFem) {
			NotificationChain msgs = null;
			if (summaryFem != null)
				msgs = ((InternalEObject)summaryFem).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM, null, msgs);
			if (newSummaryFem != null)
				msgs = ((InternalEObject)newSummaryFem).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM, null, msgs);
			msgs = basicSetSummaryFem(newSummaryFem, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM, newSummaryFem, newSummaryFem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM:
				return basicSetSummaryFem(null, msgs);
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
			case ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM:
				return getSummaryFem();
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
			case ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM:
				setSummaryFem((SummaryAdbaseModel)newValue);
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
			case ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM:
				setSummaryFem((SummaryAdbaseModel)null);
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
			case ExcaliburConfigPackage.SUMMARY_NODE__SUMMARY_FEM:
				return summaryFem != null;
		}
		return super.eIsSet(featureID);
	}

} //SummaryNodeImpl
