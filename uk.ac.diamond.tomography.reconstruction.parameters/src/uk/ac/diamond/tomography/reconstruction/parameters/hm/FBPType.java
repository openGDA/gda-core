/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>FBP Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getDefaultXml <em>Default Xml</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber <em>GPU Device Number</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBeamlineUser <em>Beamline User</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getLogFile <em>Log File</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getInputData <em>Input Data</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getFlatDarkFields <em>Flat Dark Fields</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getPreprocessing <em>Preprocessing</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getTransform <em>Transform</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBackprojection <em>Backprojection</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getOutputData <em>Output Data</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType()
 * @model extendedMetaData="name='FBP_._type' kind='elementOnly'"
 * @generated
 */
public interface FBPType extends EObject {
	/**
	 * Returns the value of the '<em><b>Default Xml</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Default Xml</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Default Xml</em>' containment reference.
	 * @see #setDefaultXml(DefaultXmlType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_DefaultXml()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='DefaultXml' namespace='##targetNamespace'"
	 * @generated
	 */
	DefaultXmlType getDefaultXml();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getDefaultXml <em>Default Xml</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default Xml</em>' containment reference.
	 * @see #getDefaultXml()
	 * @generated
	 */
	void setDefaultXml(DefaultXmlType value);

	/**
	 * Returns the value of the '<em><b>GPU Device Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>GPU Device Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>GPU Device Number</em>' attribute.
	 * @see #isSetGPUDeviceNumber()
	 * @see #unsetGPUDeviceNumber()
	 * @see #setGPUDeviceNumber(int)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_GPUDeviceNumber()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
	 *        extendedMetaData="kind='element' name='GPUDeviceNumber' namespace='##targetNamespace'"
	 * @generated
	 */
	int getGPUDeviceNumber();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber <em>GPU Device Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>GPU Device Number</em>' attribute.
	 * @see #isSetGPUDeviceNumber()
	 * @see #unsetGPUDeviceNumber()
	 * @see #getGPUDeviceNumber()
	 * @generated
	 */
	void setGPUDeviceNumber(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber <em>GPU Device Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetGPUDeviceNumber()
	 * @see #getGPUDeviceNumber()
	 * @see #setGPUDeviceNumber(int)
	 * @generated
	 */
	void unsetGPUDeviceNumber();

	/**
	 * Returns whether the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getGPUDeviceNumber <em>GPU Device Number</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>GPU Device Number</em>' attribute is set.
	 * @see #unsetGPUDeviceNumber()
	 * @see #getGPUDeviceNumber()
	 * @see #setGPUDeviceNumber(int)
	 * @generated
	 */
	boolean isSetGPUDeviceNumber();

	/**
	 * Returns the value of the '<em><b>Beamline User</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Beamline User</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Beamline User</em>' containment reference.
	 * @see #setBeamlineUser(BeamlineUserType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_BeamlineUser()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='BeamlineUser' namespace='##targetNamespace'"
	 * @generated
	 */
	BeamlineUserType getBeamlineUser();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBeamlineUser <em>Beamline User</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Beamline User</em>' containment reference.
	 * @see #getBeamlineUser()
	 * @generated
	 */
	void setBeamlineUser(BeamlineUserType value);

	/**
	 * Returns the value of the '<em><b>Log File</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Log File</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Log File</em>' attribute.
	 * @see #setLogFile(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_LogFile()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='LogFile' namespace='##targetNamespace'"
	 * @generated
	 */
	String getLogFile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getLogFile <em>Log File</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Log File</em>' attribute.
	 * @see #getLogFile()
	 * @generated
	 */
	void setLogFile(String value);

	/**
	 * Returns the value of the '<em><b>Input Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input Data</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input Data</em>' containment reference.
	 * @see #setInputData(InputDataType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_InputData()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='InputData' namespace='##targetNamespace'"
	 * @generated
	 */
	InputDataType getInputData();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getInputData <em>Input Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input Data</em>' containment reference.
	 * @see #getInputData()
	 * @generated
	 */
	void setInputData(InputDataType value);

	/**
	 * Returns the value of the '<em><b>Flat Dark Fields</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Flat Dark Fields</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Flat Dark Fields</em>' containment reference.
	 * @see #setFlatDarkFields(FlatDarkFieldsType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_FlatDarkFields()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='FlatDarkFields' namespace='##targetNamespace'"
	 * @generated
	 */
	FlatDarkFieldsType getFlatDarkFields();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getFlatDarkFields <em>Flat Dark Fields</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Flat Dark Fields</em>' containment reference.
	 * @see #getFlatDarkFields()
	 * @generated
	 */
	void setFlatDarkFields(FlatDarkFieldsType value);

	/**
	 * Returns the value of the '<em><b>Preprocessing</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Preprocessing</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Preprocessing</em>' containment reference.
	 * @see #setPreprocessing(PreprocessingType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_Preprocessing()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Preprocessing' namespace='##targetNamespace'"
	 * @generated
	 */
	PreprocessingType getPreprocessing();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getPreprocessing <em>Preprocessing</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Preprocessing</em>' containment reference.
	 * @see #getPreprocessing()
	 * @generated
	 */
	void setPreprocessing(PreprocessingType value);

	/**
	 * Returns the value of the '<em><b>Transform</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Transform</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transform</em>' containment reference.
	 * @see #setTransform(TransformType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_Transform()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Transform' namespace='##targetNamespace'"
	 * @generated
	 */
	TransformType getTransform();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getTransform <em>Transform</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Transform</em>' containment reference.
	 * @see #getTransform()
	 * @generated
	 */
	void setTransform(TransformType value);

	/**
	 * Returns the value of the '<em><b>Backprojection</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Backprojection</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Backprojection</em>' containment reference.
	 * @see #setBackprojection(BackprojectionType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_Backprojection()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Backprojection' namespace='##targetNamespace'"
	 * @generated
	 */
	BackprojectionType getBackprojection();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getBackprojection <em>Backprojection</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Backprojection</em>' containment reference.
	 * @see #getBackprojection()
	 * @generated
	 */
	void setBackprojection(BackprojectionType value);

	/**
	 * Returns the value of the '<em><b>Output Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Data</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Data</em>' containment reference.
	 * @see #setOutputData(OutputDataType)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getFBPType_OutputData()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='OutputData' namespace='##targetNamespace'"
	 * @generated
	 */
	OutputDataType getOutputData();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType#getOutputData <em>Output Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Data</em>' containment reference.
	 * @see #getOutputData()
	 * @generated
	 */
	void setOutputData(OutputDataType value);

} // FBPType
