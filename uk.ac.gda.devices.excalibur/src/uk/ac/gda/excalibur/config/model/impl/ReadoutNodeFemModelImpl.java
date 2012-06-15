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
import uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel;
import uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Readout Node Fem Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getCounterDepth <em>Counter Depth</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg1 <em>Mpxiii Chip Reg1</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg2 <em>Mpxiii Chip Reg2</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg3 <em>Mpxiii Chip Reg3</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg4 <em>Mpxiii Chip Reg4</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg5 <em>Mpxiii Chip Reg5</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg6 <em>Mpxiii Chip Reg6</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg7 <em>Mpxiii Chip Reg7</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.ReadoutNodeFemModelImpl#getMpxiiiChipReg8 <em>Mpxiii Chip Reg8</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ReadoutNodeFemModelImpl extends EObjectImpl implements ReadoutNodeFemModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getCounterDepth() <em>Counter Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepth()
	 * @generated
	 * @ordered
	 */
	protected static final int COUNTER_DEPTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCounterDepth() <em>Counter Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepth()
	 * @generated
	 * @ordered
	 */
	protected int counterDepth = COUNTER_DEPTH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg1() <em>Mpxiii Chip Reg1</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg1()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg1;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg2() <em>Mpxiii Chip Reg2</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg2()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg2;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg3() <em>Mpxiii Chip Reg3</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg3()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg3;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg4() <em>Mpxiii Chip Reg4</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg4()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg4;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg5() <em>Mpxiii Chip Reg5</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg5()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg5;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg6() <em>Mpxiii Chip Reg6</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg6()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg6;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg7() <em>Mpxiii Chip Reg7</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg7()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg7;

	/**
	 * The cached value of the '{@link #getMpxiiiChipReg8() <em>Mpxiii Chip Reg8</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMpxiiiChipReg8()
	 * @generated
	 * @ordered
	 */
	protected MpxiiiChipRegModel mpxiiiChipReg8;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReadoutNodeFemModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.READOUT_NODE_FEM_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getCounterDepth() {
		return counterDepth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCounterDepth(int newCounterDepth) {
		int oldCounterDepth = counterDepth;
		counterDepth = newCounterDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__COUNTER_DEPTH, oldCounterDepth, counterDepth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg1() {
		return mpxiiiChipReg1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg1(MpxiiiChipRegModel newMpxiiiChipReg1, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg1 = mpxiiiChipReg1;
		mpxiiiChipReg1 = newMpxiiiChipReg1;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1, oldMpxiiiChipReg1, newMpxiiiChipReg1);
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
	public void setMpxiiiChipReg1(MpxiiiChipRegModel newMpxiiiChipReg1) {
		if (newMpxiiiChipReg1 != mpxiiiChipReg1) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg1 != null)
				msgs = ((InternalEObject)mpxiiiChipReg1).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1, null, msgs);
			if (newMpxiiiChipReg1 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg1).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1, null, msgs);
			msgs = basicSetMpxiiiChipReg1(newMpxiiiChipReg1, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1, newMpxiiiChipReg1, newMpxiiiChipReg1));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg2() {
		return mpxiiiChipReg2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg2(MpxiiiChipRegModel newMpxiiiChipReg2, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg2 = mpxiiiChipReg2;
		mpxiiiChipReg2 = newMpxiiiChipReg2;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2, oldMpxiiiChipReg2, newMpxiiiChipReg2);
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
	public void setMpxiiiChipReg2(MpxiiiChipRegModel newMpxiiiChipReg2) {
		if (newMpxiiiChipReg2 != mpxiiiChipReg2) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg2 != null)
				msgs = ((InternalEObject)mpxiiiChipReg2).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2, null, msgs);
			if (newMpxiiiChipReg2 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg2).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2, null, msgs);
			msgs = basicSetMpxiiiChipReg2(newMpxiiiChipReg2, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2, newMpxiiiChipReg2, newMpxiiiChipReg2));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg3() {
		return mpxiiiChipReg3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg3(MpxiiiChipRegModel newMpxiiiChipReg3, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg3 = mpxiiiChipReg3;
		mpxiiiChipReg3 = newMpxiiiChipReg3;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3, oldMpxiiiChipReg3, newMpxiiiChipReg3);
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
	public void setMpxiiiChipReg3(MpxiiiChipRegModel newMpxiiiChipReg3) {
		if (newMpxiiiChipReg3 != mpxiiiChipReg3) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg3 != null)
				msgs = ((InternalEObject)mpxiiiChipReg3).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3, null, msgs);
			if (newMpxiiiChipReg3 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg3).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3, null, msgs);
			msgs = basicSetMpxiiiChipReg3(newMpxiiiChipReg3, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3, newMpxiiiChipReg3, newMpxiiiChipReg3));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg4() {
		return mpxiiiChipReg4;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg4(MpxiiiChipRegModel newMpxiiiChipReg4, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg4 = mpxiiiChipReg4;
		mpxiiiChipReg4 = newMpxiiiChipReg4;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4, oldMpxiiiChipReg4, newMpxiiiChipReg4);
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
	public void setMpxiiiChipReg4(MpxiiiChipRegModel newMpxiiiChipReg4) {
		if (newMpxiiiChipReg4 != mpxiiiChipReg4) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg4 != null)
				msgs = ((InternalEObject)mpxiiiChipReg4).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4, null, msgs);
			if (newMpxiiiChipReg4 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg4).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4, null, msgs);
			msgs = basicSetMpxiiiChipReg4(newMpxiiiChipReg4, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4, newMpxiiiChipReg4, newMpxiiiChipReg4));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg5() {
		return mpxiiiChipReg5;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg5(MpxiiiChipRegModel newMpxiiiChipReg5, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg5 = mpxiiiChipReg5;
		mpxiiiChipReg5 = newMpxiiiChipReg5;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5, oldMpxiiiChipReg5, newMpxiiiChipReg5);
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
	public void setMpxiiiChipReg5(MpxiiiChipRegModel newMpxiiiChipReg5) {
		if (newMpxiiiChipReg5 != mpxiiiChipReg5) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg5 != null)
				msgs = ((InternalEObject)mpxiiiChipReg5).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5, null, msgs);
			if (newMpxiiiChipReg5 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg5).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5, null, msgs);
			msgs = basicSetMpxiiiChipReg5(newMpxiiiChipReg5, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5, newMpxiiiChipReg5, newMpxiiiChipReg5));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg6() {
		return mpxiiiChipReg6;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg6(MpxiiiChipRegModel newMpxiiiChipReg6, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg6 = mpxiiiChipReg6;
		mpxiiiChipReg6 = newMpxiiiChipReg6;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6, oldMpxiiiChipReg6, newMpxiiiChipReg6);
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
	public void setMpxiiiChipReg6(MpxiiiChipRegModel newMpxiiiChipReg6) {
		if (newMpxiiiChipReg6 != mpxiiiChipReg6) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg6 != null)
				msgs = ((InternalEObject)mpxiiiChipReg6).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6, null, msgs);
			if (newMpxiiiChipReg6 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg6).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6, null, msgs);
			msgs = basicSetMpxiiiChipReg6(newMpxiiiChipReg6, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6, newMpxiiiChipReg6, newMpxiiiChipReg6));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg7() {
		return mpxiiiChipReg7;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg7(MpxiiiChipRegModel newMpxiiiChipReg7, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg7 = mpxiiiChipReg7;
		mpxiiiChipReg7 = newMpxiiiChipReg7;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7, oldMpxiiiChipReg7, newMpxiiiChipReg7);
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
	public void setMpxiiiChipReg7(MpxiiiChipRegModel newMpxiiiChipReg7) {
		if (newMpxiiiChipReg7 != mpxiiiChipReg7) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg7 != null)
				msgs = ((InternalEObject)mpxiiiChipReg7).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7, null, msgs);
			if (newMpxiiiChipReg7 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg7).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7, null, msgs);
			msgs = basicSetMpxiiiChipReg7(newMpxiiiChipReg7, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7, newMpxiiiChipReg7, newMpxiiiChipReg7));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MpxiiiChipRegModel getMpxiiiChipReg8() {
		return mpxiiiChipReg8;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMpxiiiChipReg8(MpxiiiChipRegModel newMpxiiiChipReg8, NotificationChain msgs) {
		MpxiiiChipRegModel oldMpxiiiChipReg8 = mpxiiiChipReg8;
		mpxiiiChipReg8 = newMpxiiiChipReg8;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8, oldMpxiiiChipReg8, newMpxiiiChipReg8);
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
	public void setMpxiiiChipReg8(MpxiiiChipRegModel newMpxiiiChipReg8) {
		if (newMpxiiiChipReg8 != mpxiiiChipReg8) {
			NotificationChain msgs = null;
			if (mpxiiiChipReg8 != null)
				msgs = ((InternalEObject)mpxiiiChipReg8).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8, null, msgs);
			if (newMpxiiiChipReg8 != null)
				msgs = ((InternalEObject)newMpxiiiChipReg8).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8, null, msgs);
			msgs = basicSetMpxiiiChipReg8(newMpxiiiChipReg8, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8, newMpxiiiChipReg8, newMpxiiiChipReg8));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1:
				return basicSetMpxiiiChipReg1(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2:
				return basicSetMpxiiiChipReg2(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3:
				return basicSetMpxiiiChipReg3(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4:
				return basicSetMpxiiiChipReg4(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5:
				return basicSetMpxiiiChipReg5(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6:
				return basicSetMpxiiiChipReg6(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7:
				return basicSetMpxiiiChipReg7(null, msgs);
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8:
				return basicSetMpxiiiChipReg8(null, msgs);
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
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__COUNTER_DEPTH:
				return getCounterDepth();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1:
				return getMpxiiiChipReg1();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2:
				return getMpxiiiChipReg2();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3:
				return getMpxiiiChipReg3();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4:
				return getMpxiiiChipReg4();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5:
				return getMpxiiiChipReg5();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6:
				return getMpxiiiChipReg6();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7:
				return getMpxiiiChipReg7();
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8:
				return getMpxiiiChipReg8();
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
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__COUNTER_DEPTH:
				setCounterDepth((Integer)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1:
				setMpxiiiChipReg1((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2:
				setMpxiiiChipReg2((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3:
				setMpxiiiChipReg3((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4:
				setMpxiiiChipReg4((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5:
				setMpxiiiChipReg5((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6:
				setMpxiiiChipReg6((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7:
				setMpxiiiChipReg7((MpxiiiChipRegModel)newValue);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8:
				setMpxiiiChipReg8((MpxiiiChipRegModel)newValue);
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
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__COUNTER_DEPTH:
				setCounterDepth(COUNTER_DEPTH_EDEFAULT);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1:
				setMpxiiiChipReg1((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2:
				setMpxiiiChipReg2((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3:
				setMpxiiiChipReg3((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4:
				setMpxiiiChipReg4((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5:
				setMpxiiiChipReg5((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6:
				setMpxiiiChipReg6((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7:
				setMpxiiiChipReg7((MpxiiiChipRegModel)null);
				return;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8:
				setMpxiiiChipReg8((MpxiiiChipRegModel)null);
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
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__COUNTER_DEPTH:
				return counterDepth != COUNTER_DEPTH_EDEFAULT;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG1:
				return mpxiiiChipReg1 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG2:
				return mpxiiiChipReg2 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG3:
				return mpxiiiChipReg3 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG4:
				return mpxiiiChipReg4 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG5:
				return mpxiiiChipReg5 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG6:
				return mpxiiiChipReg6 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG7:
				return mpxiiiChipReg7 != null;
			case ExcaliburConfigPackage.READOUT_NODE_FEM_MODEL__MPXIII_CHIP_REG8:
				return mpxiiiChipReg8 != null;
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
		result.append(" (counterDepth: ");
		result.append(counterDepth);
		result.append(')');
		return result.toString();
	}

} //ReadoutNodeFemModelImpl
