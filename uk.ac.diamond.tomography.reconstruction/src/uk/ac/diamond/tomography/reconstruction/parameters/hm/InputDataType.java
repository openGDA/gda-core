/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import java.math.BigDecimal;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Data Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFolder <em>Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getSuffix <em>Suffix</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getExtension <em>Extension</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getNOD <em>NOD</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMax <em>Memory Size Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMin <em>Memory Size Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getOrientation <em>Orientation</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst <em>File First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast <em>File Last</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep <em>File Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageFirst <em>Image First</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageLast <em>Image Last</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageStep <em>Image Step</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRaw <em>Raw</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFirstImageIndex <em>First Image Index</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile <em>Images Per File</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRestrictions <em>Restrictions</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMin <em>Value Min</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMax <em>Value Max</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getShape <em>Shape</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPixelParam <em>Pixel Param</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType()
 * @model extendedMetaData="name='InputData_._type' kind='elementOnly'"
 * @generated
 */
public interface InputDataType extends EObject {
	/**
	 * Returns the value of the '<em><b>Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Folder</em>' attribute.
	 * @see #setFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Folder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Folder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFolder <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Folder</em>' attribute.
	 * @see #getFolder()
	 * @generated
	 */
	void setFolder(String value);

	/**
	 * Returns the value of the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Prefix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prefix</em>' attribute.
	 * @see #setPrefix(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Prefix()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Prefix' namespace='##targetNamespace'"
	 * @generated
	 */
	String getPrefix();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPrefix <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prefix</em>' attribute.
	 * @see #getPrefix()
	 * @generated
	 */
	void setPrefix(String value);

	/**
	 * Returns the value of the '<em><b>Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Suffix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Suffix</em>' attribute.
	 * @see #setSuffix(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Suffix()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Suffix' namespace='##targetNamespace'"
	 * @generated
	 */
	String getSuffix();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getSuffix <em>Suffix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Suffix</em>' attribute.
	 * @see #getSuffix()
	 * @generated
	 */
	void setSuffix(String value);

	/**
	 * Returns the value of the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extension</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extension</em>' attribute.
	 * @see #setExtension(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Extension()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='Extension' namespace='##targetNamespace'"
	 * @generated
	 */
	String getExtension();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getExtension <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extension</em>' attribute.
	 * @see #getExtension()
	 * @generated
	 */
	void setExtension(String value);

	/**
	 * Returns the value of the '<em><b>NOD</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>NOD</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>NOD</em>' containment reference.
	 * @see #setNOD(NODType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_NOD()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='NOD' namespace='##targetNamespace'"
	 * @generated
	 */
	NODType getNOD();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getNOD <em>NOD</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>NOD</em>' containment reference.
	 * @see #getNOD()
	 * @generated
	 */
	void setNOD(NODType value);

	/**
	 * Returns the value of the '<em><b>Memory Size Max</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Memory Size Max</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Memory Size Max</em>' containment reference.
	 * @see #setMemorySizeMax(MemorySizeMaxType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_MemorySizeMax()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='MemorySizeMax' namespace='##targetNamespace'"
	 * @generated
	 */
	MemorySizeMaxType getMemorySizeMax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMax <em>Memory Size Max</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Memory Size Max</em>' containment reference.
	 * @see #getMemorySizeMax()
	 * @generated
	 */
	void setMemorySizeMax(MemorySizeMaxType value);

	/**
	 * Returns the value of the '<em><b>Memory Size Min</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Memory Size Min</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Memory Size Min</em>' containment reference.
	 * @see #setMemorySizeMin(MemorySizeMinType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_MemorySizeMin()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='MemorySizeMin' namespace='##targetNamespace'"
	 * @generated
	 */
	MemorySizeMinType getMemorySizeMin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getMemorySizeMin <em>Memory Size Min</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Memory Size Min</em>' containment reference.
	 * @see #getMemorySizeMin()
	 * @generated
	 */
	void setMemorySizeMin(MemorySizeMinType value);

	/**
	 * Returns the value of the '<em><b>Orientation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Orientation</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Orientation</em>' containment reference.
	 * @see #setOrientation(OrientationType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Orientation()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Orientation' namespace='##targetNamespace'"
	 * @generated
	 */
	OrientationType getOrientation();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getOrientation <em>Orientation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Orientation</em>' containment reference.
	 * @see #getOrientation()
	 * @generated
	 */
	void setOrientation(OrientationType value);

	/**
	 * Returns the value of the '<em><b>File First</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File First</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File First</em>' attribute.
	 * @see #isSetFileFirst()
	 * @see #unsetFileFirst()
	 * @see #setFileFirst(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_FileFirst()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='FileFirst' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFileFirst();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File First</em>' attribute.
	 * @see #isSetFileFirst()
	 * @see #unsetFileFirst()
	 * @see #getFileFirst()
	 * @generated
	 */
	void setFileFirst(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst <em>File First</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileFirst()
	 * @see #getFileFirst()
	 * @see #setFileFirst(int)
	 * @generated
	 */
	void unsetFileFirst();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileFirst <em>File First</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File First</em>' attribute is set.
	 * @see #unsetFileFirst()
	 * @see #getFileFirst()
	 * @see #setFileFirst(int)
	 * @generated
	 */
	boolean isSetFileFirst();

	/**
	 * Returns the value of the '<em><b>File Last</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Last</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Last</em>' attribute.
	 * @see #isSetFileLast()
	 * @see #unsetFileLast()
	 * @see #setFileLast(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_FileLast()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='FileLast' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFileLast();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast <em>File Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Last</em>' attribute.
	 * @see #isSetFileLast()
	 * @see #unsetFileLast()
	 * @see #getFileLast()
	 * @generated
	 */
	void setFileLast(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast <em>File Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileLast()
	 * @see #getFileLast()
	 * @see #setFileLast(int)
	 * @generated
	 */
	void unsetFileLast();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileLast <em>File Last</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File Last</em>' attribute is set.
	 * @see #unsetFileLast()
	 * @see #getFileLast()
	 * @see #setFileLast(int)
	 * @generated
	 */
	boolean isSetFileLast();

	/**
	 * Returns the value of the '<em><b>File Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Step</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Step</em>' attribute.
	 * @see #isSetFileStep()
	 * @see #unsetFileStep()
	 * @see #setFileStep(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_FileStep()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='FileStep' namespace='##targetNamespace'"
	 * @generated
	 */
	int getFileStep();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Step</em>' attribute.
	 * @see #isSetFileStep()
	 * @see #unsetFileStep()
	 * @see #getFileStep()
	 * @generated
	 */
	void setFileStep(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep <em>File Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFileStep()
	 * @see #getFileStep()
	 * @see #setFileStep(int)
	 * @generated
	 */
	void unsetFileStep();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFileStep <em>File Step</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>File Step</em>' attribute is set.
	 * @see #unsetFileStep()
	 * @see #getFileStep()
	 * @see #setFileStep(int)
	 * @generated
	 */
	boolean isSetFileStep();

	/**
	 * Returns the value of the '<em><b>Image First</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image First</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image First</em>' containment reference.
	 * @see #setImageFirst(ImageFirstType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ImageFirst()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ImageFirst' namespace='##targetNamespace'"
	 * @generated
	 */
	ImageFirstType getImageFirst();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageFirst <em>Image First</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image First</em>' containment reference.
	 * @see #getImageFirst()
	 * @generated
	 */
	void setImageFirst(ImageFirstType value);

	/**
	 * Returns the value of the '<em><b>Image Last</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image Last</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image Last</em>' containment reference.
	 * @see #setImageLast(ImageLastType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ImageLast()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ImageLast' namespace='##targetNamespace'"
	 * @generated
	 */
	ImageLastType getImageLast();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageLast <em>Image Last</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image Last</em>' containment reference.
	 * @see #getImageLast()
	 * @generated
	 */
	void setImageLast(ImageLastType value);

	/**
	 * Returns the value of the '<em><b>Image Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image Step</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image Step</em>' containment reference.
	 * @see #setImageStep(ImageStepType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ImageStep()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='ImageStep' namespace='##targetNamespace'"
	 * @generated
	 */
	ImageStepType getImageStep();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImageStep <em>Image Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image Step</em>' containment reference.
	 * @see #getImageStep()
	 * @generated
	 */
	void setImageStep(ImageStepType value);

	/**
	 * Returns the value of the '<em><b>Raw</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Raw</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Raw</em>' containment reference.
	 * @see #setRaw(RawType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Raw()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Raw' namespace='##targetNamespace'"
	 * @generated
	 */
	RawType getRaw();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRaw <em>Raw</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Raw</em>' containment reference.
	 * @see #getRaw()
	 * @generated
	 */
	void setRaw(RawType value);

	/**
	 * Returns the value of the '<em><b>First Image Index</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>First Image Index</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Image Index</em>' containment reference.
	 * @see #setFirstImageIndex(FirstImageIndexType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_FirstImageIndex()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='FirstImageIndex' namespace='##targetNamespace'"
	 * @generated
	 */
	FirstImageIndexType getFirstImageIndex();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getFirstImageIndex <em>First Image Index</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Image Index</em>' containment reference.
	 * @see #getFirstImageIndex()
	 * @generated
	 */
	void setFirstImageIndex(FirstImageIndexType value);

	/**
	 * Returns the value of the '<em><b>Images Per File</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Images Per File</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Images Per File</em>' attribute.
	 * @see #isSetImagesPerFile()
	 * @see #unsetImagesPerFile()
	 * @see #setImagesPerFile(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ImagesPerFile()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='ImagesPerFile' namespace='##targetNamespace'"
	 * @generated
	 */
	int getImagesPerFile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile <em>Images Per File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Images Per File</em>' attribute.
	 * @see #isSetImagesPerFile()
	 * @see #unsetImagesPerFile()
	 * @see #getImagesPerFile()
	 * @generated
	 */
	void setImagesPerFile(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile <em>Images Per File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetImagesPerFile()
	 * @see #getImagesPerFile()
	 * @see #setImagesPerFile(int)
	 * @generated
	 */
	void unsetImagesPerFile();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getImagesPerFile <em>Images Per File</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Images Per File</em>' attribute is set.
	 * @see #unsetImagesPerFile()
	 * @see #getImagesPerFile()
	 * @see #setImagesPerFile(int)
	 * @generated
	 */
	boolean isSetImagesPerFile();

	/**
	 * Returns the value of the '<em><b>Restrictions</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Restrictions</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Restrictions</em>' containment reference.
	 * @see #setRestrictions(RestrictionsType1)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Restrictions()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Restrictions' namespace='##targetNamespace'"
	 * @generated
	 */
	RestrictionsType1 getRestrictions();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getRestrictions <em>Restrictions</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Restrictions</em>' containment reference.
	 * @see #getRestrictions()
	 * @generated
	 */
	void setRestrictions(RestrictionsType1 value);

	/**
	 * Returns the value of the '<em><b>Value Min</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Min</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Min</em>' attribute.
	 * @see #setValueMin(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ValueMin()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ValueMin' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getValueMin();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMin <em>Value Min</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Min</em>' attribute.
	 * @see #getValueMin()
	 * @generated
	 */
	void setValueMin(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Value Max</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value Max</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Max</em>' attribute.
	 * @see #setValueMax(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_ValueMax()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='ValueMax' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getValueMax();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getValueMax <em>Value Max</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Max</em>' attribute.
	 * @see #getValueMax()
	 * @generated
	 */
	void setValueMax(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType14)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType14 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType14 value);

	/**
	 * Returns the value of the '<em><b>Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shape</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shape</em>' containment reference.
	 * @see #setShape(ShapeType1)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_Shape()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Shape' namespace='##targetNamespace'"
	 * @generated
	 */
	ShapeType1 getShape();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getShape <em>Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shape</em>' containment reference.
	 * @see #getShape()
	 * @generated
	 */
	void setShape(ShapeType1 value);

	/**
	 * Returns the value of the '<em><b>Pixel Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pixel Param</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pixel Param</em>' attribute.
	 * @see #setPixelParam(BigDecimal)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getInputDataType_PixelParam()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='PixelParam' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getPixelParam();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType#getPixelParam <em>Pixel Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pixel Param</em>' attribute.
	 * @see #getPixelParam()
	 * @generated
	 */
	void setPixelParam(BigDecimal value);

} // InputDataType
