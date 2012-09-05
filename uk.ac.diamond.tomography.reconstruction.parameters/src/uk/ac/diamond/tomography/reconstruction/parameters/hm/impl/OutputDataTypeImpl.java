/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import java.math.BigDecimal;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BitsTypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.StateType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType2;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Output Data Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getState <em>State</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getFolder <em>Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getSuffix <em>Suffix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getExtension <em>Extension</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getNOD <em>NOD</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getFileFirst <em>File First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getFileStep <em>File Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getBitsType <em>Bits Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getBits <em>Bits</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getRestrictions <em>Restrictions</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.OutputDataTypeImpl#getShape <em>Shape</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OutputDataTypeImpl extends EObjectImpl implements OutputDataType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType2 type;

	/**
	 * The cached value of the '{@link #getState() <em>State</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getState()
	 * @generated
	 * @ordered
	 */
	protected StateType state;

	/**
	 * The default value of the '{@link #getFolder() <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFolder()
	 * @generated
	 * @ordered
	 */
	protected static final String FOLDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFolder() <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFolder()
	 * @generated
	 * @ordered
	 */
	protected String folder = FOLDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String PREFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected String prefix = PREFIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getSuffix() <em>Suffix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSuffix()
	 * @generated
	 * @ordered
	 */
	protected static final String SUFFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSuffix() <em>Suffix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSuffix()
	 * @generated
	 * @ordered
	 */
	protected String suffix = SUFFIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getExtension() <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtension()
	 * @generated
	 * @ordered
	 */
	protected static final String EXTENSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getExtension() <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtension()
	 * @generated
	 * @ordered
	 */
	protected String extension = EXTENSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getNOD() <em>NOD</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNOD()
	 * @generated
	 * @ordered
	 */
	protected static final int NOD_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getNOD() <em>NOD</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNOD()
	 * @generated
	 * @ordered
	 */
	protected int nOD = NOD_EDEFAULT;

	/**
	 * This is true if the NOD attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean nODESet;

	/**
	 * The default value of the '{@link #getFileFirst() <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileFirst()
	 * @generated
	 * @ordered
	 */
	protected static final int FILE_FIRST_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFileFirst() <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileFirst()
	 * @generated
	 * @ordered
	 */
	protected int fileFirst = FILE_FIRST_EDEFAULT;

	/**
	 * This is true if the File First attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean fileFirstESet;

	/**
	 * The default value of the '{@link #getFileStep() <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileStep()
	 * @generated
	 * @ordered
	 */
	protected static final int FILE_STEP_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFileStep() <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileStep()
	 * @generated
	 * @ordered
	 */
	protected int fileStep = FILE_STEP_EDEFAULT;

	/**
	 * This is true if the File Step attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean fileStepESet;

	/**
	 * The cached value of the '{@link #getBitsType() <em>Bits Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBitsType()
	 * @generated
	 * @ordered
	 */
	protected BitsTypeType bitsType;

	/**
	 * The default value of the '{@link #getBits() <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBits()
	 * @generated
	 * @ordered
	 */
	protected static final int BITS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getBits() <em>Bits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBits()
	 * @generated
	 * @ordered
	 */
	protected int bits = BITS_EDEFAULT;

	/**
	 * This is true if the Bits attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean bitsESet;

	/**
	 * The cached value of the '{@link #getRestrictions() <em>Restrictions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestrictions()
	 * @generated
	 * @ordered
	 */
	protected RestrictionsType restrictions;

	/**
	 * The default value of the '{@link #getValueMin() <em>Value Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMin()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal VALUE_MIN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValueMin() <em>Value Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMin()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal valueMin = VALUE_MIN_EDEFAULT;

	/**
	 * The default value of the '{@link #getValueMax() <em>Value Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMax()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal VALUE_MAX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValueMax() <em>Value Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueMax()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal valueMax = VALUE_MAX_EDEFAULT;

	/**
	 * The cached value of the '{@link #getShape() <em>Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShape()
	 * @generated
	 * @ordered
	 */
	protected ShapeType shape;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OutputDataTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.OUTPUT_DATA_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType2 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType2 newType, NotificationChain msgs) {
		TypeType2 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType2 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StateType getState() {
		return state;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetState(StateType newState, NotificationChain msgs) {
		StateType oldState = state;
		state = newState;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__STATE, oldState, newState);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setState(StateType newState) {
		if (newState != state) {
			NotificationChain msgs = null;
			if (state != null)
				msgs = ((InternalEObject)state).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__STATE, null, msgs);
			if (newState != null)
				msgs = ((InternalEObject)newState).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__STATE, null, msgs);
			msgs = basicSetState(newState, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__STATE, newState, newState));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFolder(String newFolder) {
		String oldFolder = folder;
		folder = newFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__FOLDER, oldFolder, folder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPrefix(String newPrefix) {
		String oldPrefix = prefix;
		prefix = newPrefix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__PREFIX, oldPrefix, prefix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSuffix(String newSuffix) {
		String oldSuffix = suffix;
		suffix = newSuffix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__SUFFIX, oldSuffix, suffix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExtension(String newExtension) {
		String oldExtension = extension;
		extension = newExtension;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__EXTENSION, oldExtension, extension));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNOD() {
		return nOD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNOD(int newNOD) {
		int oldNOD = nOD;
		nOD = newNOD;
		boolean oldNODESet = nODESet;
		nODESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__NOD, oldNOD, nOD, !oldNODESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetNOD() {
		int oldNOD = nOD;
		boolean oldNODESet = nODESet;
		nOD = NOD_EDEFAULT;
		nODESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.OUTPUT_DATA_TYPE__NOD, oldNOD, NOD_EDEFAULT, oldNODESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetNOD() {
		return nODESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFileFirst() {
		return fileFirst;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileFirst(int newFileFirst) {
		int oldFileFirst = fileFirst;
		fileFirst = newFileFirst;
		boolean oldFileFirstESet = fileFirstESet;
		fileFirstESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST, oldFileFirst, fileFirst, !oldFileFirstESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFileFirst() {
		int oldFileFirst = fileFirst;
		boolean oldFileFirstESet = fileFirstESet;
		fileFirst = FILE_FIRST_EDEFAULT;
		fileFirstESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST, oldFileFirst, FILE_FIRST_EDEFAULT, oldFileFirstESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFileFirst() {
		return fileFirstESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFileStep() {
		return fileStep;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileStep(int newFileStep) {
		int oldFileStep = fileStep;
		fileStep = newFileStep;
		boolean oldFileStepESet = fileStepESet;
		fileStepESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__FILE_STEP, oldFileStep, fileStep, !oldFileStepESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFileStep() {
		int oldFileStep = fileStep;
		boolean oldFileStepESet = fileStepESet;
		fileStep = FILE_STEP_EDEFAULT;
		fileStepESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.OUTPUT_DATA_TYPE__FILE_STEP, oldFileStep, FILE_STEP_EDEFAULT, oldFileStepESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFileStep() {
		return fileStepESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BitsTypeType getBitsType() {
		return bitsType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBitsType(BitsTypeType newBitsType, NotificationChain msgs) {
		BitsTypeType oldBitsType = bitsType;
		bitsType = newBitsType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE, oldBitsType, newBitsType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBitsType(BitsTypeType newBitsType) {
		if (newBitsType != bitsType) {
			NotificationChain msgs = null;
			if (bitsType != null)
				msgs = ((InternalEObject)bitsType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE, null, msgs);
			if (newBitsType != null)
				msgs = ((InternalEObject)newBitsType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE, null, msgs);
			msgs = basicSetBitsType(newBitsType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE, newBitsType, newBitsType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getBits() {
		return bits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBits(int newBits) {
		int oldBits = bits;
		bits = newBits;
		boolean oldBitsESet = bitsESet;
		bitsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__BITS, oldBits, bits, !oldBitsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetBits() {
		int oldBits = bits;
		boolean oldBitsESet = bitsESet;
		bits = BITS_EDEFAULT;
		bitsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.OUTPUT_DATA_TYPE__BITS, oldBits, BITS_EDEFAULT, oldBitsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetBits() {
		return bitsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RestrictionsType getRestrictions() {
		return restrictions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRestrictions(RestrictionsType newRestrictions, NotificationChain msgs) {
		RestrictionsType oldRestrictions = restrictions;
		restrictions = newRestrictions;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS, oldRestrictions, newRestrictions);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRestrictions(RestrictionsType newRestrictions) {
		if (newRestrictions != restrictions) {
			NotificationChain msgs = null;
			if (restrictions != null)
				msgs = ((InternalEObject)restrictions).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS, null, msgs);
			if (newRestrictions != null)
				msgs = ((InternalEObject)newRestrictions).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS, null, msgs);
			msgs = basicSetRestrictions(newRestrictions, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS, newRestrictions, newRestrictions));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getValueMin() {
		return valueMin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueMin(BigDecimal newValueMin) {
		BigDecimal oldValueMin = valueMin;
		valueMin = newValueMin;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN, oldValueMin, valueMin));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getValueMax() {
		return valueMax;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValueMax(BigDecimal newValueMax) {
		BigDecimal oldValueMax = valueMax;
		valueMax = newValueMax;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX, oldValueMax, valueMax));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShapeType getShape() {
		return shape;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShape(ShapeType newShape, NotificationChain msgs) {
		ShapeType oldShape = shape;
		shape = newShape;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__SHAPE, oldShape, newShape);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShape(ShapeType newShape) {
		if (newShape != shape) {
			NotificationChain msgs = null;
			if (shape != null)
				msgs = ((InternalEObject)shape).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__SHAPE, null, msgs);
			if (newShape != null)
				msgs = ((InternalEObject)newShape).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.OUTPUT_DATA_TYPE__SHAPE, null, msgs);
			msgs = basicSetShape(newShape, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.OUTPUT_DATA_TYPE__SHAPE, newShape, newShape));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
				return basicSetState(null, msgs);
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
				return basicSetBitsType(null, msgs);
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
				return basicSetRestrictions(null, msgs);
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
				return basicSetShape(null, msgs);
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
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
				return getType();
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
				return getState();
			case HmPackage.OUTPUT_DATA_TYPE__FOLDER:
				return getFolder();
			case HmPackage.OUTPUT_DATA_TYPE__PREFIX:
				return getPrefix();
			case HmPackage.OUTPUT_DATA_TYPE__SUFFIX:
				return getSuffix();
			case HmPackage.OUTPUT_DATA_TYPE__EXTENSION:
				return getExtension();
			case HmPackage.OUTPUT_DATA_TYPE__NOD:
				return getNOD();
			case HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST:
				return getFileFirst();
			case HmPackage.OUTPUT_DATA_TYPE__FILE_STEP:
				return getFileStep();
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
				return getBitsType();
			case HmPackage.OUTPUT_DATA_TYPE__BITS:
				return getBits();
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
				return getRestrictions();
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN:
				return getValueMin();
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX:
				return getValueMax();
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
				return getShape();
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
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
				setType((TypeType2)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
				setState((StateType)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FOLDER:
				setFolder((String)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__PREFIX:
				setPrefix((String)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__SUFFIX:
				setSuffix((String)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__EXTENSION:
				setExtension((String)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__NOD:
				setNOD((Integer)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST:
				setFileFirst((Integer)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FILE_STEP:
				setFileStep((Integer)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
				setBitsType((BitsTypeType)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__BITS:
				setBits((Integer)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
				setRestrictions((RestrictionsType)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN:
				setValueMin((BigDecimal)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX:
				setValueMax((BigDecimal)newValue);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
				setShape((ShapeType)newValue);
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
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
				setType((TypeType2)null);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
				setState((StateType)null);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FOLDER:
				setFolder(FOLDER_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__PREFIX:
				setPrefix(PREFIX_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__SUFFIX:
				setSuffix(SUFFIX_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__EXTENSION:
				setExtension(EXTENSION_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__NOD:
				unsetNOD();
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST:
				unsetFileFirst();
				return;
			case HmPackage.OUTPUT_DATA_TYPE__FILE_STEP:
				unsetFileStep();
				return;
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
				setBitsType((BitsTypeType)null);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__BITS:
				unsetBits();
				return;
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
				setRestrictions((RestrictionsType)null);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN:
				setValueMin(VALUE_MIN_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX:
				setValueMax(VALUE_MAX_EDEFAULT);
				return;
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
				setShape((ShapeType)null);
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
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
				return type != null;
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
				return state != null;
			case HmPackage.OUTPUT_DATA_TYPE__FOLDER:
				return FOLDER_EDEFAULT == null ? folder != null : !FOLDER_EDEFAULT.equals(folder);
			case HmPackage.OUTPUT_DATA_TYPE__PREFIX:
				return PREFIX_EDEFAULT == null ? prefix != null : !PREFIX_EDEFAULT.equals(prefix);
			case HmPackage.OUTPUT_DATA_TYPE__SUFFIX:
				return SUFFIX_EDEFAULT == null ? suffix != null : !SUFFIX_EDEFAULT.equals(suffix);
			case HmPackage.OUTPUT_DATA_TYPE__EXTENSION:
				return EXTENSION_EDEFAULT == null ? extension != null : !EXTENSION_EDEFAULT.equals(extension);
			case HmPackage.OUTPUT_DATA_TYPE__NOD:
				return isSetNOD();
			case HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST:
				return isSetFileFirst();
			case HmPackage.OUTPUT_DATA_TYPE__FILE_STEP:
				return isSetFileStep();
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
				return bitsType != null;
			case HmPackage.OUTPUT_DATA_TYPE__BITS:
				return isSetBits();
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
				return restrictions != null;
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN:
				return VALUE_MIN_EDEFAULT == null ? valueMin != null : !VALUE_MIN_EDEFAULT.equals(valueMin);
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX:
				return VALUE_MAX_EDEFAULT == null ? valueMax != null : !VALUE_MAX_EDEFAULT.equals(valueMax);
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
				return shape != null;
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
		result.append(" (folder: ");
		result.append(folder);
		result.append(", prefix: ");
		result.append(prefix);
		result.append(", suffix: ");
		result.append(suffix);
		result.append(", extension: ");
		result.append(extension);
		result.append(", nOD: ");
		if (nODESet) result.append(nOD); else result.append("<unset>");
		result.append(", fileFirst: ");
		if (fileFirstESet) result.append(fileFirst); else result.append("<unset>");
		result.append(", fileStep: ");
		if (fileStepESet) result.append(fileStep); else result.append("<unset>");
		result.append(", bits: ");
		if (bitsESet) result.append(bits); else result.append("<unset>");
		result.append(", valueMin: ");
		result.append(valueMin);
		result.append(", valueMax: ");
		result.append(valueMax);
		result.append(')');
		return result.toString();
	}

} //OutputDataTypeImpl
