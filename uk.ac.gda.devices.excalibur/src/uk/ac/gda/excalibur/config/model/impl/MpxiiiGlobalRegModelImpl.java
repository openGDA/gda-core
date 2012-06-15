/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.MpxiiiGlobalRegModel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mpxiii Global Reg Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getColourMode <em>Colour Mode</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getColourModeAsString <em>Colour Mode As String</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getColourModeLabels <em>Colour Mode Labels</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNumber <em>Dac Number</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameCalc1 <em>Dac Name Calc1</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameCalc2 <em>Dac Name Calc2</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameCalc3 <em>Dac Name Calc3</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameSel1 <em>Dac Name Sel1</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameSel2 <em>Dac Name Sel2</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacNameSel3 <em>Dac Name Sel3</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getDacName <em>Dac Name</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getCounterDepthLabels <em>Counter Depth Labels</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getCounterDepth <em>Counter Depth</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.MpxiiiGlobalRegModelImpl#getCounterDepthAsString <em>Counter Depth As String</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MpxiiiGlobalRegModelImpl extends EObjectImpl implements MpxiiiGlobalRegModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getColourMode() <em>Colour Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourMode()
	 * @generated
	 * @ordered
	 */
	protected static final int COLOUR_MODE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getColourMode() <em>Colour Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourMode()
	 * @generated
	 * @ordered
	 */
	protected int colourMode = COLOUR_MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getColourModeAsString() <em>Colour Mode As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourModeAsString()
	 * @generated
	 * @ordered
	 */
	protected static final String COLOUR_MODE_AS_STRING_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColourModeAsString() <em>Colour Mode As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourModeAsString()
	 * @generated
	 * @ordered
	 */
	protected String colourModeAsString = COLOUR_MODE_AS_STRING_EDEFAULT;

	/**
	 * The default value of the '{@link #getColourModeLabels() <em>Colour Mode Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourModeLabels()
	 * @generated
	 * @ordered
	 */
	protected static final String[] COLOUR_MODE_LABELS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColourModeLabels() <em>Colour Mode Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColourModeLabels()
	 * @generated
	 * @ordered
	 */
	protected String[] colourModeLabels = COLOUR_MODE_LABELS_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNumber() <em>Dac Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNumber()
	 * @generated
	 * @ordered
	 */
	protected static final double DAC_NUMBER_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDacNumber() <em>Dac Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNumber()
	 * @generated
	 * @ordered
	 */
	protected double dacNumber = DAC_NUMBER_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameCalc1() <em>Dac Name Calc1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc1()
	 * @generated
	 * @ordered
	 */
	protected static final double DAC_NAME_CALC1_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDacNameCalc1() <em>Dac Name Calc1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc1()
	 * @generated
	 * @ordered
	 */
	protected double dacNameCalc1 = DAC_NAME_CALC1_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameCalc2() <em>Dac Name Calc2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc2()
	 * @generated
	 * @ordered
	 */
	protected static final double DAC_NAME_CALC2_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDacNameCalc2() <em>Dac Name Calc2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc2()
	 * @generated
	 * @ordered
	 */
	protected double dacNameCalc2 = DAC_NAME_CALC2_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameCalc3() <em>Dac Name Calc3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc3()
	 * @generated
	 * @ordered
	 */
	protected static final double DAC_NAME_CALC3_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDacNameCalc3() <em>Dac Name Calc3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameCalc3()
	 * @generated
	 * @ordered
	 */
	protected double dacNameCalc3 = DAC_NAME_CALC3_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameSel1() <em>Dac Name Sel1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel1()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_NAME_SEL1_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacNameSel1() <em>Dac Name Sel1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel1()
	 * @generated
	 * @ordered
	 */
	protected int dacNameSel1 = DAC_NAME_SEL1_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameSel2() <em>Dac Name Sel2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel2()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_NAME_SEL2_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacNameSel2() <em>Dac Name Sel2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel2()
	 * @generated
	 * @ordered
	 */
	protected int dacNameSel2 = DAC_NAME_SEL2_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacNameSel3() <em>Dac Name Sel3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel3()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_NAME_SEL3_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacNameSel3() <em>Dac Name Sel3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacNameSel3()
	 * @generated
	 * @ordered
	 */
	protected int dacNameSel3 = DAC_NAME_SEL3_EDEFAULT;

	/**
	 * The default value of the '{@link #getDacName() <em>Dac Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacName()
	 * @generated
	 * @ordered
	 */
	protected static final String DAC_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDacName() <em>Dac Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacName()
	 * @generated
	 * @ordered
	 */
	protected String dacName = DAC_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getCounterDepthLabels() <em>Counter Depth Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepthLabels()
	 * @generated
	 * @ordered
	 */
	protected static final String[] COUNTER_DEPTH_LABELS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCounterDepthLabels() <em>Counter Depth Labels</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepthLabels()
	 * @generated
	 * @ordered
	 */
	protected String[] counterDepthLabels = COUNTER_DEPTH_LABELS_EDEFAULT;

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
	 * The default value of the '{@link #getCounterDepthAsString() <em>Counter Depth As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepthAsString()
	 * @generated
	 * @ordered
	 */
	protected static final String COUNTER_DEPTH_AS_STRING_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCounterDepthAsString() <em>Counter Depth As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounterDepthAsString()
	 * @generated
	 * @ordered
	 */
	protected String counterDepthAsString = COUNTER_DEPTH_AS_STRING_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MpxiiiGlobalRegModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.MPXIII_GLOBAL_REG_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getColourMode() {
		return colourMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColourMode(int newColourMode) {
		int oldColourMode = colourMode;
		colourMode = newColourMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE, oldColourMode, colourMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getColourModeAsString() {
		return colourModeAsString;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColourModeAsString(String newColourModeAsString) {
		String oldColourModeAsString = colourModeAsString;
		colourModeAsString = newColourModeAsString;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING, oldColourModeAsString, colourModeAsString));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String[] getColourModeLabels() {
		return colourModeLabels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColourModeLabels(String[] newColourModeLabels) {
		String[] oldColourModeLabels = colourModeLabels;
		colourModeLabels = newColourModeLabels;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS, oldColourModeLabels, colourModeLabels));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getDacNumber() {
		return dacNumber;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNumber(double newDacNumber) {
		double oldDacNumber = dacNumber;
		dacNumber = newDacNumber;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER, oldDacNumber, dacNumber));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getDacNameCalc1() {
		return dacNameCalc1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameCalc1(double newDacNameCalc1) {
		double oldDacNameCalc1 = dacNameCalc1;
		dacNameCalc1 = newDacNameCalc1;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1, oldDacNameCalc1, dacNameCalc1));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getDacNameCalc2() {
		return dacNameCalc2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameCalc2(double newDacNameCalc2) {
		double oldDacNameCalc2 = dacNameCalc2;
		dacNameCalc2 = newDacNameCalc2;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2, oldDacNameCalc2, dacNameCalc2));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getDacNameCalc3() {
		return dacNameCalc3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameCalc3(double newDacNameCalc3) {
		double oldDacNameCalc3 = dacNameCalc3;
		dacNameCalc3 = newDacNameCalc3;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3, oldDacNameCalc3, dacNameCalc3));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacNameSel1() {
		return dacNameSel1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameSel1(int newDacNameSel1) {
		int oldDacNameSel1 = dacNameSel1;
		dacNameSel1 = newDacNameSel1;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1, oldDacNameSel1, dacNameSel1));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacNameSel2() {
		return dacNameSel2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameSel2(int newDacNameSel2) {
		int oldDacNameSel2 = dacNameSel2;
		dacNameSel2 = newDacNameSel2;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2, oldDacNameSel2, dacNameSel2));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDacNameSel3() {
		return dacNameSel3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacNameSel3(int newDacNameSel3) {
		int oldDacNameSel3 = dacNameSel3;
		dacNameSel3 = newDacNameSel3;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3, oldDacNameSel3, dacNameSel3));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDacName() {
		return dacName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDacName(String newDacName) {
		String oldDacName = dacName;
		dacName = newDacName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME, oldDacName, dacName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String[] getCounterDepthLabels() {
		return counterDepthLabels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCounterDepthLabels(String[] newCounterDepthLabels) {
		String[] oldCounterDepthLabels = counterDepthLabels;
		counterDepthLabels = newCounterDepthLabels;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS, oldCounterDepthLabels, counterDepthLabels));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH, oldCounterDepth, counterDepth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCounterDepthAsString() {
		return counterDepthAsString;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCounterDepthAsString(String newCounterDepthAsString) {
		String oldCounterDepthAsString = counterDepthAsString;
		counterDepthAsString = newCounterDepthAsString;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING, oldCounterDepthAsString, counterDepthAsString));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE:
				return getColourMode();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING:
				return getColourModeAsString();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS:
				return getColourModeLabels();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER:
				return getDacNumber();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1:
				return getDacNameCalc1();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2:
				return getDacNameCalc2();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3:
				return getDacNameCalc3();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1:
				return getDacNameSel1();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2:
				return getDacNameSel2();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3:
				return getDacNameSel3();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME:
				return getDacName();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS:
				return getCounterDepthLabels();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH:
				return getCounterDepth();
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING:
				return getCounterDepthAsString();
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
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE:
				setColourMode((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING:
				setColourModeAsString((String)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS:
				setColourModeLabels((String[])newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER:
				setDacNumber((Double)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1:
				setDacNameCalc1((Double)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2:
				setDacNameCalc2((Double)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3:
				setDacNameCalc3((Double)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1:
				setDacNameSel1((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2:
				setDacNameSel2((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3:
				setDacNameSel3((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME:
				setDacName((String)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS:
				setCounterDepthLabels((String[])newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH:
				setCounterDepth((Integer)newValue);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING:
				setCounterDepthAsString((String)newValue);
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
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE:
				setColourMode(COLOUR_MODE_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING:
				setColourModeAsString(COLOUR_MODE_AS_STRING_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS:
				setColourModeLabels(COLOUR_MODE_LABELS_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER:
				setDacNumber(DAC_NUMBER_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1:
				setDacNameCalc1(DAC_NAME_CALC1_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2:
				setDacNameCalc2(DAC_NAME_CALC2_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3:
				setDacNameCalc3(DAC_NAME_CALC3_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1:
				setDacNameSel1(DAC_NAME_SEL1_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2:
				setDacNameSel2(DAC_NAME_SEL2_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3:
				setDacNameSel3(DAC_NAME_SEL3_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME:
				setDacName(DAC_NAME_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS:
				setCounterDepthLabels(COUNTER_DEPTH_LABELS_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH:
				setCounterDepth(COUNTER_DEPTH_EDEFAULT);
				return;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING:
				setCounterDepthAsString(COUNTER_DEPTH_AS_STRING_EDEFAULT);
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
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE:
				return colourMode != COLOUR_MODE_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_AS_STRING:
				return COLOUR_MODE_AS_STRING_EDEFAULT == null ? colourModeAsString != null : !COLOUR_MODE_AS_STRING_EDEFAULT.equals(colourModeAsString);
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COLOUR_MODE_LABELS:
				return COLOUR_MODE_LABELS_EDEFAULT == null ? colourModeLabels != null : !COLOUR_MODE_LABELS_EDEFAULT.equals(colourModeLabels);
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NUMBER:
				return dacNumber != DAC_NUMBER_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC1:
				return dacNameCalc1 != DAC_NAME_CALC1_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC2:
				return dacNameCalc2 != DAC_NAME_CALC2_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_CALC3:
				return dacNameCalc3 != DAC_NAME_CALC3_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL1:
				return dacNameSel1 != DAC_NAME_SEL1_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL2:
				return dacNameSel2 != DAC_NAME_SEL2_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME_SEL3:
				return dacNameSel3 != DAC_NAME_SEL3_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__DAC_NAME:
				return DAC_NAME_EDEFAULT == null ? dacName != null : !DAC_NAME_EDEFAULT.equals(dacName);
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_LABELS:
				return COUNTER_DEPTH_LABELS_EDEFAULT == null ? counterDepthLabels != null : !COUNTER_DEPTH_LABELS_EDEFAULT.equals(counterDepthLabels);
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH:
				return counterDepth != COUNTER_DEPTH_EDEFAULT;
			case ExcaliburConfigPackage.MPXIII_GLOBAL_REG_MODEL__COUNTER_DEPTH_AS_STRING:
				return COUNTER_DEPTH_AS_STRING_EDEFAULT == null ? counterDepthAsString != null : !COUNTER_DEPTH_AS_STRING_EDEFAULT.equals(counterDepthAsString);
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
		result.append(" (colourMode: ");
		result.append(colourMode);
		result.append(", colourModeAsString: ");
		result.append(colourModeAsString);
		result.append(", colourModeLabels: ");
		result.append(colourModeLabels);
		result.append(", dacNumber: ");
		result.append(dacNumber);
		result.append(", dacNameCalc1: ");
		result.append(dacNameCalc1);
		result.append(", dacNameCalc2: ");
		result.append(dacNameCalc2);
		result.append(", dacNameCalc3: ");
		result.append(dacNameCalc3);
		result.append(", dacNameSel1: ");
		result.append(dacNameSel1);
		result.append(", dacNameSel2: ");
		result.append(dacNameSel2);
		result.append(", dacNameSel3: ");
		result.append(dacNameSel3);
		result.append(", dacName: ");
		result.append(dacName);
		result.append(", counterDepthLabels: ");
		result.append(counterDepthLabels);
		result.append(", counterDepth: ");
		result.append(counterDepth);
		result.append(", counterDepthAsString: ");
		result.append(counterDepthAsString);
		result.append(')');
		return result.toString();
	}

} //MpxiiiGlobalRegModelImpl
