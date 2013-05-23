/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.emf.ecore.util.InternalEList;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Spectrum</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getLocation <em>Location</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getUser <em>User</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getSampleName <em>Sample Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getFilenamePrefix <em>Filename Prefix</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getBaseDirectory <em>Base Directory</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getFilenameFormat <em>Filename Format</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getFileExtension <em>File Extension</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getNumberOfComments <em>Number Of Comments</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getComments <em>Comments</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SpectrumImpl extends EObjectImpl implements Spectrum {
	/**
	 * The default value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCATION_EDEFAULT = "Diamond I09";

	/**
	 * The cached value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected String location = LOCATION_EDEFAULT;

	/**
	 * This is true if the Location attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean locationESet;

	/**
	 * The default value of the '{@link #getUser() <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUser()
	 * @generated
	 * @ordered
	 */
	protected static final String USER_EDEFAULT = "cm5933-1";

	/**
	 * The cached value of the '{@link #getUser() <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUser()
	 * @generated
	 * @ordered
	 */
	protected String user = USER_EDEFAULT;

	/**
	 * This is true if the User attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean userESet;

	/**
	 * The default value of the '{@link #getSampleName() <em>Sample Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleName()
	 * @generated
	 * @ordered
	 */
	protected static final String SAMPLE_NAME_EDEFAULT = "mySample";

	/**
	 * The cached value of the '{@link #getSampleName() <em>Sample Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleName()
	 * @generated
	 * @ordered
	 */
	protected String sampleName = SAMPLE_NAME_EDEFAULT;

	/**
	 * This is true if the Sample Name attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleNameESet;

	/**
	 * The default value of the '{@link #getFilenamePrefix() <em>Filename Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenamePrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_PREFIX_EDEFAULT = "myPrefix";

	/**
	 * The cached value of the '{@link #getFilenamePrefix() <em>Filename Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenamePrefix()
	 * @generated
	 * @ordered
	 */
	protected String filenamePrefix = FILENAME_PREFIX_EDEFAULT;

	/**
	 * This is true if the Filename Prefix attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean filenamePrefixESet;

	/**
	 * The default value of the '{@link #getBaseDirectory() <em>Base Directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseDirectory()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_DIRECTORY_EDEFAULT = "myBaseDirectory";

	/**
	 * The cached value of the '{@link #getBaseDirectory() <em>Base Directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseDirectory()
	 * @generated
	 * @ordered
	 */
	protected String baseDirectory = BASE_DIRECTORY_EDEFAULT;

	/**
	 * This is true if the Base Directory attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean baseDirectoryESet;

	/**
	 * The default value of the '{@link #getFilenameFormat() <em>Filename Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenameFormat()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_FORMAT_EDEFAULT = "%s_%5d_%3d_%s"; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getFilenameFormat() <em>Filename Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenameFormat()
	 * @generated
	 * @ordered
	 */
	protected String filenameFormat = FILENAME_FORMAT_EDEFAULT;

	/**
	 * This is true if the Filename Format attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean filenameFormatESet;

	/**
	 * The default value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileExtension()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_EXTENSION_EDEFAULT = ".txt";

	/**
	 * The cached value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileExtension()
	 * @generated
	 * @ordered
	 */
	protected String fileExtension = FILE_EXTENSION_EDEFAULT;

	/**
	 * This is true if the File Extension attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean fileExtensionESet;

	/**
	 * The default value of the '{@link #getNumberOfComments() <em>Number Of Comments</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfComments()
	 * @generated
	 * @ordered
	 */
	protected static final int NUMBER_OF_COMMENTS_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getNumberOfComments() <em>Number Of Comments</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfComments()
	 * @generated
	 * @ordered
	 */
	protected int numberOfComments = NUMBER_OF_COMMENTS_EDEFAULT;

	/**
	 * This is true if the Number Of Comments attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean numberOfCommentsESet;

	/**
	 * The cached value of the '{@link #getComments() <em>Comments</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComments()
	 * @generated
	 * @ordered
	 */
	protected EList<String> comments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SpectrumImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegiondefinitionPackage.Literals.SPECTRUM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLocation(String newLocation) {
		String oldLocation = location;
		location = newLocation;
		boolean oldLocationESet = locationESet;
		locationESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__LOCATION, oldLocation, location, !oldLocationESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetLocation() {
		String oldLocation = location;
		boolean oldLocationESet = locationESet;
		location = LOCATION_EDEFAULT;
		locationESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__LOCATION, oldLocation, LOCATION_EDEFAULT, oldLocationESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetLocation() {
		return locationESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getUser() {
		return user;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUser(String newUser) {
		String oldUser = user;
		user = newUser;
		boolean oldUserESet = userESet;
		userESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__USER, oldUser, user, !oldUserESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetUser() {
		String oldUser = user;
		boolean oldUserESet = userESet;
		user = USER_EDEFAULT;
		userESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__USER, oldUser, USER_EDEFAULT, oldUserESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetUser() {
		return userESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSampleName() {
		return sampleName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSampleName(String newSampleName) {
		String oldSampleName = sampleName;
		sampleName = newSampleName;
		boolean oldSampleNameESet = sampleNameESet;
		sampleNameESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME, oldSampleName, sampleName, !oldSampleNameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetSampleName() {
		String oldSampleName = sampleName;
		boolean oldSampleNameESet = sampleNameESet;
		sampleName = SAMPLE_NAME_EDEFAULT;
		sampleNameESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME, oldSampleName, SAMPLE_NAME_EDEFAULT, oldSampleNameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSampleName() {
		return sampleNameESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilenamePrefix() {
		return filenamePrefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilenamePrefix(String newFilenamePrefix) {
		String oldFilenamePrefix = filenamePrefix;
		filenamePrefix = newFilenamePrefix;
		boolean oldFilenamePrefixESet = filenamePrefixESet;
		filenamePrefixESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX, oldFilenamePrefix, filenamePrefix, !oldFilenamePrefixESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFilenamePrefix() {
		String oldFilenamePrefix = filenamePrefix;
		boolean oldFilenamePrefixESet = filenamePrefixESet;
		filenamePrefix = FILENAME_PREFIX_EDEFAULT;
		filenamePrefixESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX, oldFilenamePrefix, FILENAME_PREFIX_EDEFAULT, oldFilenamePrefixESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFilenamePrefix() {
		return filenamePrefixESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBaseDirectory(String newBaseDirectory) {
		String oldBaseDirectory = baseDirectory;
		baseDirectory = newBaseDirectory;
		boolean oldBaseDirectoryESet = baseDirectoryESet;
		baseDirectoryESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY, oldBaseDirectory, baseDirectory, !oldBaseDirectoryESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetBaseDirectory() {
		String oldBaseDirectory = baseDirectory;
		boolean oldBaseDirectoryESet = baseDirectoryESet;
		baseDirectory = BASE_DIRECTORY_EDEFAULT;
		baseDirectoryESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY, oldBaseDirectory, BASE_DIRECTORY_EDEFAULT, oldBaseDirectoryESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetBaseDirectory() {
		return baseDirectoryESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilenameFormat() {
		return filenameFormat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilenameFormat(String newFilenameFormat) {
		String oldFilenameFormat = filenameFormat;
		filenameFormat = newFilenameFormat;
		boolean oldFilenameFormatESet = filenameFormatESet;
		filenameFormatESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT, oldFilenameFormat, filenameFormat, !oldFilenameFormatESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFilenameFormat() {
		String oldFilenameFormat = filenameFormat;
		boolean oldFilenameFormatESet = filenameFormatESet;
		filenameFormat = FILENAME_FORMAT_EDEFAULT;
		filenameFormatESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT, oldFilenameFormat, FILENAME_FORMAT_EDEFAULT, oldFilenameFormatESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFilenameFormat() {
		return filenameFormatESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileExtension(String newFileExtension) {
		String oldFileExtension = fileExtension;
		fileExtension = newFileExtension;
		boolean oldFileExtensionESet = fileExtensionESet;
		fileExtensionESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION, oldFileExtension, fileExtension, !oldFileExtensionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFileExtension() {
		String oldFileExtension = fileExtension;
		boolean oldFileExtensionESet = fileExtensionESet;
		fileExtension = FILE_EXTENSION_EDEFAULT;
		fileExtensionESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION, oldFileExtension, FILE_EXTENSION_EDEFAULT, oldFileExtensionESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFileExtension() {
		return fileExtensionESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNumberOfComments() {
		return numberOfComments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumberOfComments(int newNumberOfComments) {
		int oldNumberOfComments = numberOfComments;
		numberOfComments = newNumberOfComments;
		boolean oldNumberOfCommentsESet = numberOfCommentsESet;
		numberOfCommentsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS, oldNumberOfComments, numberOfComments, !oldNumberOfCommentsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetNumberOfComments() {
		int oldNumberOfComments = numberOfComments;
		boolean oldNumberOfCommentsESet = numberOfCommentsESet;
		numberOfComments = NUMBER_OF_COMMENTS_EDEFAULT;
		numberOfCommentsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS, oldNumberOfComments, NUMBER_OF_COMMENTS_EDEFAULT, oldNumberOfCommentsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetNumberOfComments() {
		return numberOfCommentsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getComments() {
		if (comments == null) {
			comments = new EDataTypeUniqueEList.Unsettable<String>(String.class, this, RegiondefinitionPackage.SPECTRUM__COMMENTS);
		}
		return comments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetComments() {
		if (comments != null) ((InternalEList.Unsettable<?>)comments).unset();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetComments() {
		return comments != null && ((InternalEList.Unsettable<?>)comments).isSet();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegiondefinitionPackage.SPECTRUM__LOCATION:
				return getLocation();
			case RegiondefinitionPackage.SPECTRUM__USER:
				return getUser();
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				return getSampleName();
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				return getFilenamePrefix();
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				return getBaseDirectory();
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT:
				return getFilenameFormat();
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				return getFileExtension();
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				return getNumberOfComments();
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				return getComments();
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
			case RegiondefinitionPackage.SPECTRUM__LOCATION:
				setLocation((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__USER:
				setUser((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				setSampleName((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				setFilenamePrefix((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				setBaseDirectory((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT:
				setFilenameFormat((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				setFileExtension((String)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				setNumberOfComments((Integer)newValue);
				return;
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				getComments().clear();
				getComments().addAll((Collection<? extends String>)newValue);
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
			case RegiondefinitionPackage.SPECTRUM__LOCATION:
				unsetLocation();
				return;
			case RegiondefinitionPackage.SPECTRUM__USER:
				unsetUser();
				return;
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				unsetSampleName();
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				unsetFilenamePrefix();
				return;
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				unsetBaseDirectory();
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT:
				unsetFilenameFormat();
				return;
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				unsetFileExtension();
				return;
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				unsetNumberOfComments();
				return;
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				unsetComments();
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
			case RegiondefinitionPackage.SPECTRUM__LOCATION:
				return isSetLocation();
			case RegiondefinitionPackage.SPECTRUM__USER:
				return isSetUser();
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				return isSetSampleName();
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				return isSetFilenamePrefix();
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				return isSetBaseDirectory();
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMAT:
				return isSetFilenameFormat();
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				return isSetFileExtension();
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				return isSetNumberOfComments();
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				return isSetComments();
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
		result.append(" (location: "); //$NON-NLS-1$
		if (locationESet) result.append(location); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", User: "); //$NON-NLS-1$
		if (userESet) result.append(user); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", sampleName: "); //$NON-NLS-1$
		if (sampleNameESet) result.append(sampleName); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", filenamePrefix: "); //$NON-NLS-1$
		if (filenamePrefixESet) result.append(filenamePrefix); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", baseDirectory: "); //$NON-NLS-1$
		if (baseDirectoryESet) result.append(baseDirectory); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", filenameFormat: "); //$NON-NLS-1$
		if (filenameFormatESet) result.append(filenameFormat); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", fileExtension: "); //$NON-NLS-1$
		if (fileExtensionESet) result.append(fileExtension); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", numberOfComments: "); //$NON-NLS-1$
		if (numberOfCommentsESet) result.append(numberOfComments); else result.append("<unset>"); //$NON-NLS-1$
		result.append(", comments: "); //$NON-NLS-1$
		result.append(comments);
		result.append(')');
		return result.toString();
	}

} //SpectrumImpl
