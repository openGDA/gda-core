/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import uk.ac.gda.excalibur.config.model.ExcaliburConfig;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.MasterConfigNode;
import uk.ac.gda.excalibur.config.model.ReadoutNode;
import uk.ac.gda.excalibur.config.model.SummaryNode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Excalibur Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl#getReadoutNodes <em>Readout Nodes</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl#getConfigNode <em>Config Node</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ExcaliburConfigImpl#getSummaryNode <em>Summary Node</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExcaliburConfigImpl extends EObjectImpl implements ExcaliburConfig {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The cached value of the '{@link #getReadoutNodes() <em>Readout Nodes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReadoutNodes()
	 * @generated
	 * @ordered
	 */
	protected EList<ReadoutNode> readoutNodes;

	/**
	 * The cached value of the '{@link #getConfigNode() <em>Config Node</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConfigNode()
	 * @generated
	 * @ordered
	 */
	protected MasterConfigNode configNode;

	/**
	 * The cached value of the '{@link #getSummaryNode() <em>Summary Node</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummaryNode()
	 * @generated
	 * @ordered
	 */
	protected SummaryNode summaryNode;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExcaliburConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.EXCALIBUR_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ReadoutNode> getReadoutNodes() {
		if (readoutNodes == null) {
			readoutNodes = new EObjectContainmentEList<ReadoutNode>(ReadoutNode.class, this, ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES);
		}
		return readoutNodes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MasterConfigNode getConfigNode() {
		return configNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetConfigNode(MasterConfigNode newConfigNode, NotificationChain msgs) {
		MasterConfigNode oldConfigNode = configNode;
		configNode = newConfigNode;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE, oldConfigNode, newConfigNode);
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
	public void setConfigNode(MasterConfigNode newConfigNode) {
		if (newConfigNode != configNode) {
			NotificationChain msgs = null;
			if (configNode != null)
				msgs = ((InternalEObject)configNode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE, null, msgs);
			if (newConfigNode != null)
				msgs = ((InternalEObject)newConfigNode).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE, null, msgs);
			msgs = basicSetConfigNode(newConfigNode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE, newConfigNode, newConfigNode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SummaryNode getSummaryNode() {
		return summaryNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSummaryNode(SummaryNode newSummaryNode, NotificationChain msgs) {
		SummaryNode oldSummaryNode = summaryNode;
		summaryNode = newSummaryNode;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE, oldSummaryNode, newSummaryNode);
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
	public void setSummaryNode(SummaryNode newSummaryNode) {
		if (newSummaryNode != summaryNode) {
			NotificationChain msgs = null;
			if (summaryNode != null)
				msgs = ((InternalEObject)summaryNode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE, null, msgs);
			if (newSummaryNode != null)
				msgs = ((InternalEObject)newSummaryNode).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE, null, msgs);
			msgs = basicSetSummaryNode(newSummaryNode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE, newSummaryNode, newSummaryNode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES:
				return ((InternalEList<?>)getReadoutNodes()).basicRemove(otherEnd, msgs);
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE:
				return basicSetConfigNode(null, msgs);
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE:
				return basicSetSummaryNode(null, msgs);
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
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES:
				return getReadoutNodes();
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE:
				return getConfigNode();
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE:
				return getSummaryNode();
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
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES:
				getReadoutNodes().clear();
				getReadoutNodes().addAll((Collection<? extends ReadoutNode>)newValue);
				return;
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE:
				setConfigNode((MasterConfigNode)newValue);
				return;
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE:
				setSummaryNode((SummaryNode)newValue);
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
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES:
				getReadoutNodes().clear();
				return;
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE:
				setConfigNode((MasterConfigNode)null);
				return;
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE:
				setSummaryNode((SummaryNode)null);
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
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__READOUT_NODES:
				return readoutNodes != null && !readoutNodes.isEmpty();
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__CONFIG_NODE:
				return configNode != null;
			case ExcaliburConfigPackage.EXCALIBUR_CONFIG__SUMMARY_NODE:
				return summaryNode != null;
		}
		return super.eIsSet(featureID);
	}

} //ExcaliburConfigImpl
