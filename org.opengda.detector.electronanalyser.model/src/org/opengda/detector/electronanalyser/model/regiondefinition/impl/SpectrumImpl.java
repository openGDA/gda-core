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
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.impl.SpectrumImpl#getFilenameFormet <em>Filename Formet</em>}</li>
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
	 * The default value of the '{@link #getUser() <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUser()
	 * @generated
	 * @ordered
	 */
	protected static final String USER_EDEFAULT = null;

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
	 * The default value of the '{@link #getSampleName() <em>Sample Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleName()
	 * @generated
	 * @ordered
	 */
	protected static final String SAMPLE_NAME_EDEFAULT = null;

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
	 * The default value of the '{@link #getFilenamePrefix() <em>Filename Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenamePrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_PREFIX_EDEFAULT = null;

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
	 * The default value of the '{@link #getBaseDirectory() <em>Base Directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseDirectory()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_DIRECTORY_EDEFAULT = null;

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
	 * The default value of the '{@link #getFilenameFormet() <em>Filename Formet</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenameFormet()
	 * @generated
	 * @ordered
	 */
	protected static final String FILENAME_FORMET_EDEFAULT = "%s_%5d_%3d_%s";

	/**
	 * The cached value of the '{@link #getFilenameFormet() <em>Filename Formet</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilenameFormet()
	 * @generated
	 * @ordered
	 */
	protected String filenameFormet = FILENAME_FORMET_EDEFAULT;

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
	 * The default value of the '{@link #getNumberOfComments() <em>Number Of Comments</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfComments()
	 * @generated
	 * @ordered
	 */
	protected static final int NUMBER_OF_COMMENTS_EDEFAULT = 0;

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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__LOCATION, oldLocation, location));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__USER, oldUser, user));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME, oldSampleName, sampleName));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX, oldFilenamePrefix, filenamePrefix));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY, oldBaseDirectory, baseDirectory));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFilenameFormet() {
		return filenameFormet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFilenameFormet(String newFilenameFormet) {
		String oldFilenameFormet = filenameFormet;
		filenameFormet = newFilenameFormet;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILENAME_FORMET, oldFilenameFormet, filenameFormet));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION, oldFileExtension, fileExtension));
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
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS, oldNumberOfComments, numberOfComments));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getComments() {
		if (comments == null) {
			comments = new EDataTypeUniqueEList<String>(String.class, this, RegiondefinitionPackage.SPECTRUM__COMMENTS);
		}
		return comments;
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
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMET:
				return getFilenameFormet();
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
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMET:
				setFilenameFormet((String)newValue);
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
				setLocation(LOCATION_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__USER:
				setUser(USER_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				setSampleName(SAMPLE_NAME_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				setFilenamePrefix(FILENAME_PREFIX_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				setBaseDirectory(BASE_DIRECTORY_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMET:
				setFilenameFormet(FILENAME_FORMET_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				setFileExtension(FILE_EXTENSION_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				setNumberOfComments(NUMBER_OF_COMMENTS_EDEFAULT);
				return;
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				getComments().clear();
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
				return LOCATION_EDEFAULT == null ? location != null : !LOCATION_EDEFAULT.equals(location);
			case RegiondefinitionPackage.SPECTRUM__USER:
				return USER_EDEFAULT == null ? user != null : !USER_EDEFAULT.equals(user);
			case RegiondefinitionPackage.SPECTRUM__SAMPLE_NAME:
				return SAMPLE_NAME_EDEFAULT == null ? sampleName != null : !SAMPLE_NAME_EDEFAULT.equals(sampleName);
			case RegiondefinitionPackage.SPECTRUM__FILENAME_PREFIX:
				return FILENAME_PREFIX_EDEFAULT == null ? filenamePrefix != null : !FILENAME_PREFIX_EDEFAULT.equals(filenamePrefix);
			case RegiondefinitionPackage.SPECTRUM__BASE_DIRECTORY:
				return BASE_DIRECTORY_EDEFAULT == null ? baseDirectory != null : !BASE_DIRECTORY_EDEFAULT.equals(baseDirectory);
			case RegiondefinitionPackage.SPECTRUM__FILENAME_FORMET:
				return FILENAME_FORMET_EDEFAULT == null ? filenameFormet != null : !FILENAME_FORMET_EDEFAULT.equals(filenameFormet);
			case RegiondefinitionPackage.SPECTRUM__FILE_EXTENSION:
				return FILE_EXTENSION_EDEFAULT == null ? fileExtension != null : !FILE_EXTENSION_EDEFAULT.equals(fileExtension);
			case RegiondefinitionPackage.SPECTRUM__NUMBER_OF_COMMENTS:
				return numberOfComments != NUMBER_OF_COMMENTS_EDEFAULT;
			case RegiondefinitionPackage.SPECTRUM__COMMENTS:
				return comments != null && !comments.isEmpty();
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
		result.append(" (location: ");
		result.append(location);
		result.append(", User: ");
		result.append(user);
		result.append(", sampleName: ");
		result.append(sampleName);
		result.append(", filenamePrefix: ");
		result.append(filenamePrefix);
		result.append(", baseDirectory: ");
		result.append(baseDirectory);
		result.append(", filenameFormet: ");
		result.append(filenameFormet);
		result.append(", fileExtension: ");
		result.append(fileExtension);
		result.append(", numberOfComments: ");
		result.append(numberOfComments);
		result.append(", comments: ");
		result.append(comments);
		result.append(')');
		return result.toString();
	}

} //SpectrumImpl
