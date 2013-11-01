/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Spectrum</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation <em>Location</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser <em>User</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName <em>Sample Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix <em>Filename Prefix</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory <em>Base Directory</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormat <em>Filename Format</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension <em>File Extension</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments <em>Number Of Comments</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getComments <em>Comments</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum()
 * @model
 * @generated
 */
public interface Spectrum extends EObject {
	/**
	 * Returns the value of the '<em><b>Location</b></em>' attribute.
	 * The default value is <code>"Diamond I09"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Location</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Location</em>' attribute.
	 * @see #isSetLocation()
	 * @see #unsetLocation()
	 * @see #setLocation(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_Location()
	 * @model default="Diamond I09" unsettable="true"
	 * @generated
	 */
	String getLocation();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Location</em>' attribute.
	 * @see #isSetLocation()
	 * @see #unsetLocation()
	 * @see #getLocation()
	 * @generated
	 */
	void setLocation(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLocation()
	 * @see #getLocation()
	 * @see #setLocation(String)
	 * @generated
	 */
	void unsetLocation();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getLocation <em>Location</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Location</em>' attribute is set.
	 * @see #unsetLocation()
	 * @see #getLocation()
	 * @see #setLocation(String)
	 * @generated
	 */
	boolean isSetLocation();

	/**
	 * Returns the value of the '<em><b>User</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' attribute.
	 * @see #isSetUser()
	 * @see #unsetUser()
	 * @see #setUser(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_User()
	 * @model default="" unsettable="true"
	 * @generated
	 */
	String getUser();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User</em>' attribute.
	 * @see #isSetUser()
	 * @see #unsetUser()
	 * @see #getUser()
	 * @generated
	 */
	void setUser(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetUser()
	 * @see #getUser()
	 * @see #setUser(String)
	 * @generated
	 */
	void unsetUser();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getUser <em>User</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>User</em>' attribute is set.
	 * @see #unsetUser()
	 * @see #getUser()
	 * @see #setUser(String)
	 * @generated
	 */
	boolean isSetUser();

	/**
	 * Returns the value of the '<em><b>Sample Name</b></em>' attribute.
	 * The default value is <code>"sample name"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sample Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sample Name</em>' attribute.
	 * @see #isSetSampleName()
	 * @see #unsetSampleName()
	 * @see #setSampleName(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_SampleName()
	 * @model default="sample name" unsettable="true"
	 * @generated
	 */
	String getSampleName();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName <em>Sample Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sample Name</em>' attribute.
	 * @see #isSetSampleName()
	 * @see #unsetSampleName()
	 * @see #getSampleName()
	 * @generated
	 */
	void setSampleName(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName <em>Sample Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSampleName()
	 * @see #getSampleName()
	 * @see #setSampleName(String)
	 * @generated
	 */
	void unsetSampleName();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getSampleName <em>Sample Name</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Sample Name</em>' attribute is set.
	 * @see #unsetSampleName()
	 * @see #getSampleName()
	 * @see #setSampleName(String)
	 * @generated
	 */
	boolean isSetSampleName();

	/**
	 * Returns the value of the '<em><b>Filename Prefix</b></em>' attribute.
	 * The default value is <code>"FilenamePrefix"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filename Prefix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filename Prefix</em>' attribute.
	 * @see #isSetFilenamePrefix()
	 * @see #unsetFilenamePrefix()
	 * @see #setFilenamePrefix(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_FilenamePrefix()
	 * @model default="FilenamePrefix" unsettable="true"
	 * @generated
	 */
	String getFilenamePrefix();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix <em>Filename Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filename Prefix</em>' attribute.
	 * @see #isSetFilenamePrefix()
	 * @see #unsetFilenamePrefix()
	 * @see #getFilenamePrefix()
	 * @generated
	 */
	void setFilenamePrefix(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix <em>Filename Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFilenamePrefix()
	 * @see #getFilenamePrefix()
	 * @see #setFilenamePrefix(String)
	 * @generated
	 */
	void unsetFilenamePrefix();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenamePrefix <em>Filename Prefix</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Filename Prefix</em>' attribute is set.
	 * @see #unsetFilenamePrefix()
	 * @see #getFilenamePrefix()
	 * @see #setFilenamePrefix(String)
	 * @generated
	 */
	boolean isSetFilenamePrefix();

	/**
	 * Returns the value of the '<em><b>Base Directory</b></em>' attribute.
	 * The default value is <code>"myBaseDirectory"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Base Directory</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Base Directory</em>' attribute.
	 * @see #isSetBaseDirectory()
	 * @see #unsetBaseDirectory()
	 * @see #setBaseDirectory(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_BaseDirectory()
	 * @model default="myBaseDirectory" unsettable="true"
	 * @generated
	 */
	String getBaseDirectory();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory <em>Base Directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Directory</em>' attribute.
	 * @see #isSetBaseDirectory()
	 * @see #unsetBaseDirectory()
	 * @see #getBaseDirectory()
	 * @generated
	 */
	void setBaseDirectory(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory <em>Base Directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetBaseDirectory()
	 * @see #getBaseDirectory()
	 * @see #setBaseDirectory(String)
	 * @generated
	 */
	void unsetBaseDirectory();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getBaseDirectory <em>Base Directory</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Base Directory</em>' attribute is set.
	 * @see #unsetBaseDirectory()
	 * @see #getBaseDirectory()
	 * @see #setBaseDirectory(String)
	 * @generated
	 */
	boolean isSetBaseDirectory();

	/**
	 * Returns the value of the '<em><b>Filename Format</b></em>' attribute.
	 * The default value is <code>"%s_%05d_%s"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filename Format</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filename Format</em>' attribute.
	 * @see #isSetFilenameFormat()
	 * @see #unsetFilenameFormat()
	 * @see #setFilenameFormat(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_FilenameFormat()
	 * @model default="%s_%05d_%s" unsettable="true"
	 * @generated
	 */
	String getFilenameFormat();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormat <em>Filename Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filename Format</em>' attribute.
	 * @see #isSetFilenameFormat()
	 * @see #unsetFilenameFormat()
	 * @see #getFilenameFormat()
	 * @generated
	 */
	void setFilenameFormat(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormat <em>Filename Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFilenameFormat()
	 * @see #getFilenameFormat()
	 * @see #setFilenameFormat(String)
	 * @generated
	 */
	void unsetFilenameFormat();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFilenameFormat <em>Filename Format</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Filename Format</em>' attribute is set.
	 * @see #unsetFilenameFormat()
	 * @see #getFilenameFormat()
	 * @see #setFilenameFormat(String)
	 * @generated
	 */
	boolean isSetFilenameFormat();

	/**
	 * Returns the value of the '<em><b>File Extension</b></em>' attribute.
	 * The default value is <code>".nxs"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Extension</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Extension</em>' attribute.
	 * @see #isSetFileExtension()
	 * @see #unsetFileExtension()
	 * @see #setFileExtension(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_FileExtension()
	 * @model default=".nxs" unsettable="true"
	 * @generated
	 */
	String getFileExtension();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension <em>File Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Extension</em>' attribute.
	 * @see #isSetFileExtension()
	 * @see #unsetFileExtension()
	 * @see #getFileExtension()
	 * @generated
	 */
	void setFileExtension(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension <em>File Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileExtension()
	 * @see #getFileExtension()
	 * @see #setFileExtension(String)
	 * @generated
	 */
	void unsetFileExtension();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getFileExtension <em>File Extension</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File Extension</em>' attribute is set.
	 * @see #unsetFileExtension()
	 * @see #getFileExtension()
	 * @see #setFileExtension(String)
	 * @generated
	 */
	boolean isSetFileExtension();

	/**
	 * Returns the value of the '<em><b>Number Of Comments</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number Of Comments</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number Of Comments</em>' attribute.
	 * @see #isSetNumberOfComments()
	 * @see #unsetNumberOfComments()
	 * @see #setNumberOfComments(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_NumberOfComments()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getNumberOfComments();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments <em>Number Of Comments</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number Of Comments</em>' attribute.
	 * @see #isSetNumberOfComments()
	 * @see #unsetNumberOfComments()
	 * @see #getNumberOfComments()
	 * @generated
	 */
	void setNumberOfComments(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments <em>Number Of Comments</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumberOfComments()
	 * @see #getNumberOfComments()
	 * @see #setNumberOfComments(int)
	 * @generated
	 */
	void unsetNumberOfComments();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getNumberOfComments <em>Number Of Comments</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Number Of Comments</em>' attribute is set.
	 * @see #unsetNumberOfComments()
	 * @see #getNumberOfComments()
	 * @see #setNumberOfComments(int)
	 * @generated
	 */
	boolean isSetNumberOfComments();

	/**
	 * Returns the value of the '<em><b>Comments</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comments</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comments</em>' attribute list.
	 * @see #isSetComments()
	 * @see #unsetComments()
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSpectrum_Comments()
	 * @model default="Put your comments here, They will be saved along with your region definitions in the sequence file." unsettable="true"
	 * @generated
	 */
	EList<String> getComments();

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getComments <em>Comments</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetComments()
	 * @see #getComments()
	 * @generated
	 */
	void unsetComments();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum#getComments <em>Comments</em>}' attribute list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Comments</em>' attribute list is set.
	 * @see #unsetComments()
	 * @see #getComments()
	 * @generated
	 */
	boolean isSetComments();

} // Spectrum
