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
import uk.ac.gda.excalibur.config.model.ReadoutNode;
import uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Readout Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl#getReadoutNodeFem <em>Readout Node Fem</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeImpl#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ReadoutNodeImpl extends BaseNodeImpl implements ReadoutNode {

	/**
	 * The cached value of the '{@link #getReadoutNodeFem() <em>Readout Node Fem</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReadoutNodeFem()
	 * @generated
	 * @ordered
	 */
	protected ReadoutNodeFemModel readoutNodeFem;

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final int ID_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected int id = ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReadoutNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.READOUT_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReadoutNodeFemModel getReadoutNodeFem() {
		return readoutNodeFem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetReadoutNodeFem(ReadoutNodeFemModel newReadoutNodeFem, NotificationChain msgs) {
		ReadoutNodeFemModel oldReadoutNodeFem = readoutNodeFem;
		readoutNodeFem = newReadoutNodeFem;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM, oldReadoutNodeFem, newReadoutNodeFem);
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
	public void setReadoutNodeFem(ReadoutNodeFemModel newReadoutNodeFem) {
		if (newReadoutNodeFem != readoutNodeFem) {
			NotificationChain msgs = null;
			if (readoutNodeFem != null)
				msgs = ((InternalEObject)readoutNodeFem).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM, null, msgs);
			if (newReadoutNodeFem != null)
				msgs = ((InternalEObject)newReadoutNodeFem).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM, null, msgs);
			msgs = basicSetReadoutNodeFem(newReadoutNodeFem, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM, newReadoutNodeFem, newReadoutNodeFem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(int newId) {
		int oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM:
				return basicSetReadoutNodeFem(null, msgs);
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
			case ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM:
				return getReadoutNodeFem();
			case ExcaliburConfigPackage.READOUT_NODE__ID:
				return getId();
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
			case ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM:
				setReadoutNodeFem((ReadoutNodeFemModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE__ID:
				setId((Integer)newValue);
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
			case ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM:
				setReadoutNodeFem((ReadoutNodeFemModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE__ID:
				setId(ID_EDEFAULT);
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
			case ExcaliburConfigPackage.READOUT_NODE__READOUT_NODE_FEM:
				return readoutNodeFem != null;
			case ExcaliburConfigPackage.READOUT_NODE__ID:
				return id != ID_EDEFAULT;
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
		result.append(" (id: ");
		result.append(id);
		result.append(')');
		return result.toString();
	}

} //ReadoutNodeImpl
