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

import uk.ac.gda.excalibur.config.model.BaseNode;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.FixModel;
import uk.ac.gda.excalibur.config.model.GapModel;
import uk.ac.gda.excalibur.config.model.MasterModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Base Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl#getGap <em>Gap</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl#getMst <em>Mst</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.BaseNodeImpl#getFix <em>Fix</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BaseNodeImpl extends EObjectImpl implements BaseNode {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The cached value of the '{@link #getGap() <em>Gap</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGap()
	 * @generated
	 * @ordered
	 */
	protected GapModel gap;

	/**
	 * The cached value of the '{@link #getMst() <em>Mst</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMst()
	 * @generated
	 * @ordered
	 */
	protected MasterModel mst;

	/**
	 * The cached value of the '{@link #getFix() <em>Fix</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFix()
	 * @generated
	 * @ordered
	 */
	protected FixModel fix;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BaseNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.BASE_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GapModel getGap() {
		return gap;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGap(GapModel newGap, NotificationChain msgs) {
		GapModel oldGap = gap;
		gap = newGap;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__GAP, oldGap, newGap);
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
	public void setGap(GapModel newGap) {
		if (newGap != gap) {
			NotificationChain msgs = null;
			if (gap != null)
				msgs = ((InternalEObject)gap).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__GAP, null, msgs);
			if (newGap != null)
				msgs = ((InternalEObject)newGap).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__GAP, null, msgs);
			msgs = basicSetGap(newGap, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__GAP, newGap, newGap));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MasterModel getMst() {
		return mst;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMst(MasterModel newMst, NotificationChain msgs) {
		MasterModel oldMst = mst;
		mst = newMst;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__MST, oldMst, newMst);
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
	public void setMst(MasterModel newMst) {
		if (newMst != mst) {
			NotificationChain msgs = null;
			if (mst != null)
				msgs = ((InternalEObject)mst).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__MST, null, msgs);
			if (newMst != null)
				msgs = ((InternalEObject)newMst).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__MST, null, msgs);
			msgs = basicSetMst(newMst, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__MST, newMst, newMst));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FixModel getFix() {
		return fix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFix(FixModel newFix, NotificationChain msgs) {
		FixModel oldFix = fix;
		fix = newFix;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__FIX, oldFix, newFix);
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
	public void setFix(FixModel newFix) {
		if (newFix != fix) {
			NotificationChain msgs = null;
			if (fix != null)
				msgs = ((InternalEObject)fix).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__FIX, null, msgs);
			if (newFix != null)
				msgs = ((InternalEObject)newFix).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.BASE_NODE__FIX, null, msgs);
			msgs = basicSetFix(newFix, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.BASE_NODE__FIX, newFix, newFix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.BASE_NODE__GAP:
				return basicSetGap(null, msgs);
			case ExcaliburConfigPackage.BASE_NODE__MST:
				return basicSetMst(null, msgs);
			case ExcaliburConfigPackage.BASE_NODE__FIX:
				return basicSetFix(null, msgs);
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
			case ExcaliburConfigPackage.BASE_NODE__GAP:
				return getGap();
			case ExcaliburConfigPackage.BASE_NODE__MST:
				return getMst();
			case ExcaliburConfigPackage.BASE_NODE__FIX:
				return getFix();
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
			case ExcaliburConfigPackage.BASE_NODE__GAP:
				setGap((GapModel)newValue);
				return;
			case ExcaliburConfigPackage.BASE_NODE__MST:
				setMst((MasterModel)newValue);
				return;
			case ExcaliburConfigPackage.BASE_NODE__FIX:
				setFix((FixModel)newValue);
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
			case ExcaliburConfigPackage.BASE_NODE__GAP:
				setGap((GapModel)null);
				return;
			case ExcaliburConfigPackage.BASE_NODE__MST:
				setMst((MasterModel)null);
				return;
			case ExcaliburConfigPackage.BASE_NODE__FIX:
				setFix((FixModel)null);
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
			case ExcaliburConfigPackage.BASE_NODE__GAP:
				return gap != null;
			case ExcaliburConfigPackage.BASE_NODE__MST:
				return mst != null;
			case ExcaliburConfigPackage.BASE_NODE__FIX:
				return fix != null;
		}
		return super.eIsSet(featureID);
	}

} //BaseNodeImpl
