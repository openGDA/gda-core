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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.FirstImageIndexType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageFirstType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageLastType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ImageStepType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMaxType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.MemorySizeMinType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.NODType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OrientationType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RawType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RestrictionsType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ShapeType1;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType14;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Data Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getFolder <em>Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getSuffix <em>Suffix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getExtension <em>Extension</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getNOD <em>NOD</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getMemorySizeMax <em>Memory Size Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getMemorySizeMin <em>Memory Size Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getOrientation <em>Orientation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getFileFirst <em>File First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getFileLast <em>File Last</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getFileStep <em>File Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getImageFirst <em>Image First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getImageLast <em>Image Last</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getImageStep <em>Image Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getRaw <em>Raw</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getFirstImageIndex <em>First Image Index</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getImagesPerFile <em>Images Per File</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getRestrictions <em>Restrictions</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getShape <em>Shape</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.InputDataTypeImpl#getPixelParam <em>Pixel Param</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class InputDataTypeImpl extends EObjectImpl implements InputDataType {
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
	 * The cached value of the '{@link #getNOD() <em>NOD</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNOD()
	 * @generated
	 * @ordered
	 */
	protected NODType nOD;

	/**
	 * The cached value of the '{@link #getMemorySizeMax() <em>Memory Size Max</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMemorySizeMax()
	 * @generated
	 * @ordered
	 */
	protected MemorySizeMaxType memorySizeMax;

	/**
	 * The cached value of the '{@link #getMemorySizeMin() <em>Memory Size Min</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMemorySizeMin()
	 * @generated
	 * @ordered
	 */
	protected MemorySizeMinType memorySizeMin;

	/**
	 * The cached value of the '{@link #getOrientation() <em>Orientation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrientation()
	 * @generated
	 * @ordered
	 */
	protected OrientationType orientation;

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
	 * The default value of the '{@link #getFileLast() <em>File Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileLast()
	 * @generated
	 * @ordered
	 */
	protected static final int FILE_LAST_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFileLast() <em>File Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileLast()
	 * @generated
	 * @ordered
	 */
	protected int fileLast = FILE_LAST_EDEFAULT;

	/**
	 * This is true if the File Last attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean fileLastESet;

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
	 * The cached value of the '{@link #getImageFirst() <em>Image First</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageFirst()
	 * @generated
	 * @ordered
	 */
	protected ImageFirstType imageFirst;

	/**
	 * The cached value of the '{@link #getImageLast() <em>Image Last</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageLast()
	 * @generated
	 * @ordered
	 */
	protected ImageLastType imageLast;

	/**
	 * The cached value of the '{@link #getImageStep() <em>Image Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageStep()
	 * @generated
	 * @ordered
	 */
	protected ImageStepType imageStep;

	/**
	 * The cached value of the '{@link #getRaw() <em>Raw</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRaw()
	 * @generated
	 * @ordered
	 */
	protected RawType raw;

	/**
	 * The cached value of the '{@link #getFirstImageIndex() <em>First Image Index</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstImageIndex()
	 * @generated
	 * @ordered
	 */
	protected FirstImageIndexType firstImageIndex;

	/**
	 * The default value of the '{@link #getImagesPerFile() <em>Images Per File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerFile()
	 * @generated
	 * @ordered
	 */
	protected static final int IMAGES_PER_FILE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getImagesPerFile() <em>Images Per File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerFile()
	 * @generated
	 * @ordered
	 */
	protected int imagesPerFile = IMAGES_PER_FILE_EDEFAULT;

	/**
	 * This is true if the Images Per File attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean imagesPerFileESet;

	/**
	 * The cached value of the '{@link #getRestrictions() <em>Restrictions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestrictions()
	 * @generated
	 * @ordered
	 */
	protected RestrictionsType1 restrictions;

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
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType14 type;

	/**
	 * The cached value of the '{@link #getShape() <em>Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShape()
	 * @generated
	 * @ordered
	 */
	protected ShapeType1 shape;

	/**
	 * The default value of the '{@link #getPixelParam() <em>Pixel Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixelParam()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal PIXEL_PARAM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPixelParam() <em>Pixel Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixelParam()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal pixelParam = PIXEL_PARAM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InputDataTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.INPUT_DATA_TYPE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FOLDER, oldFolder, folder));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__PREFIX, oldPrefix, prefix));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__SUFFIX, oldSuffix, suffix));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__EXTENSION, oldExtension, extension));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NODType getNOD() {
		return nOD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNOD(NODType newNOD, NotificationChain msgs) {
		NODType oldNOD = nOD;
		nOD = newNOD;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__NOD, oldNOD, newNOD);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNOD(NODType newNOD) {
		if (newNOD != nOD) {
			NotificationChain msgs = null;
			if (nOD != null)
				msgs = ((InternalEObject)nOD).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__NOD, null, msgs);
			if (newNOD != null)
				msgs = ((InternalEObject)newNOD).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__NOD, null, msgs);
			msgs = basicSetNOD(newNOD, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__NOD, newNOD, newNOD));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MemorySizeMaxType getMemorySizeMax() {
		return memorySizeMax;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMemorySizeMax(MemorySizeMaxType newMemorySizeMax, NotificationChain msgs) {
		MemorySizeMaxType oldMemorySizeMax = memorySizeMax;
		memorySizeMax = newMemorySizeMax;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX, oldMemorySizeMax, newMemorySizeMax);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMemorySizeMax(MemorySizeMaxType newMemorySizeMax) {
		if (newMemorySizeMax != memorySizeMax) {
			NotificationChain msgs = null;
			if (memorySizeMax != null)
				msgs = ((InternalEObject)memorySizeMax).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX, null, msgs);
			if (newMemorySizeMax != null)
				msgs = ((InternalEObject)newMemorySizeMax).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX, null, msgs);
			msgs = basicSetMemorySizeMax(newMemorySizeMax, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX, newMemorySizeMax, newMemorySizeMax));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MemorySizeMinType getMemorySizeMin() {
		return memorySizeMin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMemorySizeMin(MemorySizeMinType newMemorySizeMin, NotificationChain msgs) {
		MemorySizeMinType oldMemorySizeMin = memorySizeMin;
		memorySizeMin = newMemorySizeMin;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN, oldMemorySizeMin, newMemorySizeMin);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMemorySizeMin(MemorySizeMinType newMemorySizeMin) {
		if (newMemorySizeMin != memorySizeMin) {
			NotificationChain msgs = null;
			if (memorySizeMin != null)
				msgs = ((InternalEObject)memorySizeMin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN, null, msgs);
			if (newMemorySizeMin != null)
				msgs = ((InternalEObject)newMemorySizeMin).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN, null, msgs);
			msgs = basicSetMemorySizeMin(newMemorySizeMin, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN, newMemorySizeMin, newMemorySizeMin));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OrientationType getOrientation() {
		return orientation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOrientation(OrientationType newOrientation, NotificationChain msgs) {
		OrientationType oldOrientation = orientation;
		orientation = newOrientation;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__ORIENTATION, oldOrientation, newOrientation);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOrientation(OrientationType newOrientation) {
		if (newOrientation != orientation) {
			NotificationChain msgs = null;
			if (orientation != null)
				msgs = ((InternalEObject)orientation).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__ORIENTATION, null, msgs);
			if (newOrientation != null)
				msgs = ((InternalEObject)newOrientation).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__ORIENTATION, null, msgs);
			msgs = basicSetOrientation(newOrientation, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__ORIENTATION, newOrientation, newOrientation));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FILE_FIRST, oldFileFirst, fileFirst, !oldFileFirstESet));
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
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INPUT_DATA_TYPE__FILE_FIRST, oldFileFirst, FILE_FIRST_EDEFAULT, oldFileFirstESet));
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
	public int getFileLast() {
		return fileLast;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileLast(int newFileLast) {
		int oldFileLast = fileLast;
		fileLast = newFileLast;
		boolean oldFileLastESet = fileLastESet;
		fileLastESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FILE_LAST, oldFileLast, fileLast, !oldFileLastESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFileLast() {
		int oldFileLast = fileLast;
		boolean oldFileLastESet = fileLastESet;
		fileLast = FILE_LAST_EDEFAULT;
		fileLastESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INPUT_DATA_TYPE__FILE_LAST, oldFileLast, FILE_LAST_EDEFAULT, oldFileLastESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFileLast() {
		return fileLastESet;
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FILE_STEP, oldFileStep, fileStep, !oldFileStepESet));
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
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INPUT_DATA_TYPE__FILE_STEP, oldFileStep, FILE_STEP_EDEFAULT, oldFileStepESet));
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
	public ImageFirstType getImageFirst() {
		return imageFirst;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImageFirst(ImageFirstType newImageFirst, NotificationChain msgs) {
		ImageFirstType oldImageFirst = imageFirst;
		imageFirst = newImageFirst;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST, oldImageFirst, newImageFirst);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImageFirst(ImageFirstType newImageFirst) {
		if (newImageFirst != imageFirst) {
			NotificationChain msgs = null;
			if (imageFirst != null)
				msgs = ((InternalEObject)imageFirst).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST, null, msgs);
			if (newImageFirst != null)
				msgs = ((InternalEObject)newImageFirst).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST, null, msgs);
			msgs = basicSetImageFirst(newImageFirst, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST, newImageFirst, newImageFirst));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImageLastType getImageLast() {
		return imageLast;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImageLast(ImageLastType newImageLast, NotificationChain msgs) {
		ImageLastType oldImageLast = imageLast;
		imageLast = newImageLast;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_LAST, oldImageLast, newImageLast);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImageLast(ImageLastType newImageLast) {
		if (newImageLast != imageLast) {
			NotificationChain msgs = null;
			if (imageLast != null)
				msgs = ((InternalEObject)imageLast).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_LAST, null, msgs);
			if (newImageLast != null)
				msgs = ((InternalEObject)newImageLast).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_LAST, null, msgs);
			msgs = basicSetImageLast(newImageLast, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_LAST, newImageLast, newImageLast));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImageStepType getImageStep() {
		return imageStep;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImageStep(ImageStepType newImageStep, NotificationChain msgs) {
		ImageStepType oldImageStep = imageStep;
		imageStep = newImageStep;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_STEP, oldImageStep, newImageStep);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImageStep(ImageStepType newImageStep) {
		if (newImageStep != imageStep) {
			NotificationChain msgs = null;
			if (imageStep != null)
				msgs = ((InternalEObject)imageStep).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_STEP, null, msgs);
			if (newImageStep != null)
				msgs = ((InternalEObject)newImageStep).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__IMAGE_STEP, null, msgs);
			msgs = basicSetImageStep(newImageStep, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGE_STEP, newImageStep, newImageStep));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RawType getRaw() {
		return raw;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRaw(RawType newRaw, NotificationChain msgs) {
		RawType oldRaw = raw;
		raw = newRaw;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__RAW, oldRaw, newRaw);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRaw(RawType newRaw) {
		if (newRaw != raw) {
			NotificationChain msgs = null;
			if (raw != null)
				msgs = ((InternalEObject)raw).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__RAW, null, msgs);
			if (newRaw != null)
				msgs = ((InternalEObject)newRaw).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__RAW, null, msgs);
			msgs = basicSetRaw(newRaw, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__RAW, newRaw, newRaw));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FirstImageIndexType getFirstImageIndex() {
		return firstImageIndex;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFirstImageIndex(FirstImageIndexType newFirstImageIndex, NotificationChain msgs) {
		FirstImageIndexType oldFirstImageIndex = firstImageIndex;
		firstImageIndex = newFirstImageIndex;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX, oldFirstImageIndex, newFirstImageIndex);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFirstImageIndex(FirstImageIndexType newFirstImageIndex) {
		if (newFirstImageIndex != firstImageIndex) {
			NotificationChain msgs = null;
			if (firstImageIndex != null)
				msgs = ((InternalEObject)firstImageIndex).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX, null, msgs);
			if (newFirstImageIndex != null)
				msgs = ((InternalEObject)newFirstImageIndex).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX, null, msgs);
			msgs = basicSetFirstImageIndex(newFirstImageIndex, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX, newFirstImageIndex, newFirstImageIndex));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getImagesPerFile() {
		return imagesPerFile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImagesPerFile(int newImagesPerFile) {
		int oldImagesPerFile = imagesPerFile;
		imagesPerFile = newImagesPerFile;
		boolean oldImagesPerFileESet = imagesPerFileESet;
		imagesPerFileESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE, oldImagesPerFile, imagesPerFile, !oldImagesPerFileESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetImagesPerFile() {
		int oldImagesPerFile = imagesPerFile;
		boolean oldImagesPerFileESet = imagesPerFileESet;
		imagesPerFile = IMAGES_PER_FILE_EDEFAULT;
		imagesPerFileESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE, oldImagesPerFile, IMAGES_PER_FILE_EDEFAULT, oldImagesPerFileESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetImagesPerFile() {
		return imagesPerFileESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RestrictionsType1 getRestrictions() {
		return restrictions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRestrictions(RestrictionsType1 newRestrictions, NotificationChain msgs) {
		RestrictionsType1 oldRestrictions = restrictions;
		restrictions = newRestrictions;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__RESTRICTIONS, oldRestrictions, newRestrictions);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRestrictions(RestrictionsType1 newRestrictions) {
		if (newRestrictions != restrictions) {
			NotificationChain msgs = null;
			if (restrictions != null)
				msgs = ((InternalEObject)restrictions).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__RESTRICTIONS, null, msgs);
			if (newRestrictions != null)
				msgs = ((InternalEObject)newRestrictions).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__RESTRICTIONS, null, msgs);
			msgs = basicSetRestrictions(newRestrictions, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__RESTRICTIONS, newRestrictions, newRestrictions));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__VALUE_MIN, oldValueMin, valueMin));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__VALUE_MAX, oldValueMax, valueMax));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TypeType14 getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType14 newType, NotificationChain msgs) {
		TypeType14 oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(TypeType14 newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShapeType1 getShape() {
		return shape;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShape(ShapeType1 newShape, NotificationChain msgs) {
		ShapeType1 oldShape = shape;
		shape = newShape;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__SHAPE, oldShape, newShape);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShape(ShapeType1 newShape) {
		if (newShape != shape) {
			NotificationChain msgs = null;
			if (shape != null)
				msgs = ((InternalEObject)shape).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__SHAPE, null, msgs);
			if (newShape != null)
				msgs = ((InternalEObject)newShape).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.INPUT_DATA_TYPE__SHAPE, null, msgs);
			msgs = basicSetShape(newShape, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__SHAPE, newShape, newShape));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getPixelParam() {
		return pixelParam;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPixelParam(BigDecimal newPixelParam) {
		BigDecimal oldPixelParam = pixelParam;
		pixelParam = newPixelParam;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM, oldPixelParam, pixelParam));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.INPUT_DATA_TYPE__NOD:
				return basicSetNOD(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
				return basicSetMemorySizeMax(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
				return basicSetMemorySizeMin(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
				return basicSetOrientation(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
				return basicSetImageFirst(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
				return basicSetImageLast(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
				return basicSetImageStep(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__RAW:
				return basicSetRaw(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
				return basicSetFirstImageIndex(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
				return basicSetRestrictions(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__TYPE:
				return basicSetType(null, msgs);
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
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
			case HmPackage.INPUT_DATA_TYPE__FOLDER:
				return getFolder();
			case HmPackage.INPUT_DATA_TYPE__PREFIX:
				return getPrefix();
			case HmPackage.INPUT_DATA_TYPE__SUFFIX:
				return getSuffix();
			case HmPackage.INPUT_DATA_TYPE__EXTENSION:
				return getExtension();
			case HmPackage.INPUT_DATA_TYPE__NOD:
				return getNOD();
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
				return getMemorySizeMax();
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
				return getMemorySizeMin();
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
				return getOrientation();
			case HmPackage.INPUT_DATA_TYPE__FILE_FIRST:
				return getFileFirst();
			case HmPackage.INPUT_DATA_TYPE__FILE_LAST:
				return getFileLast();
			case HmPackage.INPUT_DATA_TYPE__FILE_STEP:
				return getFileStep();
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
				return getImageFirst();
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
				return getImageLast();
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
				return getImageStep();
			case HmPackage.INPUT_DATA_TYPE__RAW:
				return getRaw();
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
				return getFirstImageIndex();
			case HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE:
				return getImagesPerFile();
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
				return getRestrictions();
			case HmPackage.INPUT_DATA_TYPE__VALUE_MIN:
				return getValueMin();
			case HmPackage.INPUT_DATA_TYPE__VALUE_MAX:
				return getValueMax();
			case HmPackage.INPUT_DATA_TYPE__TYPE:
				return getType();
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
				return getShape();
			case HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM:
				return getPixelParam();
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
			case HmPackage.INPUT_DATA_TYPE__FOLDER:
				setFolder((String)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__PREFIX:
				setPrefix((String)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__SUFFIX:
				setSuffix((String)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__EXTENSION:
				setExtension((String)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__NOD:
				setNOD((NODType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
				setMemorySizeMax((MemorySizeMaxType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
				setMemorySizeMin((MemorySizeMinType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
				setOrientation((OrientationType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_FIRST:
				setFileFirst((Integer)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_LAST:
				setFileLast((Integer)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_STEP:
				setFileStep((Integer)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
				setImageFirst((ImageFirstType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
				setImageLast((ImageLastType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
				setImageStep((ImageStepType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__RAW:
				setRaw((RawType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
				setFirstImageIndex((FirstImageIndexType)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE:
				setImagesPerFile((Integer)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
				setRestrictions((RestrictionsType1)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__VALUE_MIN:
				setValueMin((BigDecimal)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__VALUE_MAX:
				setValueMax((BigDecimal)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__TYPE:
				setType((TypeType14)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
				setShape((ShapeType1)newValue);
				return;
			case HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM:
				setPixelParam((BigDecimal)newValue);
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
			case HmPackage.INPUT_DATA_TYPE__FOLDER:
				setFolder(FOLDER_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__PREFIX:
				setPrefix(PREFIX_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__SUFFIX:
				setSuffix(SUFFIX_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__EXTENSION:
				setExtension(EXTENSION_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__NOD:
				setNOD((NODType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
				setMemorySizeMax((MemorySizeMaxType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
				setMemorySizeMin((MemorySizeMinType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
				setOrientation((OrientationType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_FIRST:
				unsetFileFirst();
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_LAST:
				unsetFileLast();
				return;
			case HmPackage.INPUT_DATA_TYPE__FILE_STEP:
				unsetFileStep();
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
				setImageFirst((ImageFirstType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
				setImageLast((ImageLastType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
				setImageStep((ImageStepType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__RAW:
				setRaw((RawType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
				setFirstImageIndex((FirstImageIndexType)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE:
				unsetImagesPerFile();
				return;
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
				setRestrictions((RestrictionsType1)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__VALUE_MIN:
				setValueMin(VALUE_MIN_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__VALUE_MAX:
				setValueMax(VALUE_MAX_EDEFAULT);
				return;
			case HmPackage.INPUT_DATA_TYPE__TYPE:
				setType((TypeType14)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
				setShape((ShapeType1)null);
				return;
			case HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM:
				setPixelParam(PIXEL_PARAM_EDEFAULT);
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
			case HmPackage.INPUT_DATA_TYPE__FOLDER:
				return FOLDER_EDEFAULT == null ? folder != null : !FOLDER_EDEFAULT.equals(folder);
			case HmPackage.INPUT_DATA_TYPE__PREFIX:
				return PREFIX_EDEFAULT == null ? prefix != null : !PREFIX_EDEFAULT.equals(prefix);
			case HmPackage.INPUT_DATA_TYPE__SUFFIX:
				return SUFFIX_EDEFAULT == null ? suffix != null : !SUFFIX_EDEFAULT.equals(suffix);
			case HmPackage.INPUT_DATA_TYPE__EXTENSION:
				return EXTENSION_EDEFAULT == null ? extension != null : !EXTENSION_EDEFAULT.equals(extension);
			case HmPackage.INPUT_DATA_TYPE__NOD:
				return nOD != null;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
				return memorySizeMax != null;
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
				return memorySizeMin != null;
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
				return orientation != null;
			case HmPackage.INPUT_DATA_TYPE__FILE_FIRST:
				return isSetFileFirst();
			case HmPackage.INPUT_DATA_TYPE__FILE_LAST:
				return isSetFileLast();
			case HmPackage.INPUT_DATA_TYPE__FILE_STEP:
				return isSetFileStep();
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
				return imageFirst != null;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
				return imageLast != null;
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
				return imageStep != null;
			case HmPackage.INPUT_DATA_TYPE__RAW:
				return raw != null;
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
				return firstImageIndex != null;
			case HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE:
				return isSetImagesPerFile();
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
				return restrictions != null;
			case HmPackage.INPUT_DATA_TYPE__VALUE_MIN:
				return VALUE_MIN_EDEFAULT == null ? valueMin != null : !VALUE_MIN_EDEFAULT.equals(valueMin);
			case HmPackage.INPUT_DATA_TYPE__VALUE_MAX:
				return VALUE_MAX_EDEFAULT == null ? valueMax != null : !VALUE_MAX_EDEFAULT.equals(valueMax);
			case HmPackage.INPUT_DATA_TYPE__TYPE:
				return type != null;
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
				return shape != null;
			case HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM:
				return PIXEL_PARAM_EDEFAULT == null ? pixelParam != null : !PIXEL_PARAM_EDEFAULT.equals(pixelParam);
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
		result.append(", fileFirst: ");
		if (fileFirstESet) result.append(fileFirst); else result.append("<unset>");
		result.append(", fileLast: ");
		if (fileLastESet) result.append(fileLast); else result.append("<unset>");
		result.append(", fileStep: ");
		if (fileStepESet) result.append(fileStep); else result.append("<unset>");
		result.append(", imagesPerFile: ");
		if (imagesPerFileESet) result.append(imagesPerFile); else result.append("<unset>");
		result.append(", valueMin: ");
		result.append(valueMin);
		result.append(", valueMax: ");
		result.append(valueMax);
		result.append(", pixelParam: ");
		result.append(pixelParam);
		result.append(')');
		return result.toString();
	}

} //InputDataTypeImpl
