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
 * A representation of the model object '<em><b>Beamline User Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getBeamlineName <em>Beamline Name</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getYear <em>Year</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getMonth <em>Month</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDate <em>Date</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getVisitNumber <em>Visit Number</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputDataFolder <em>Input Data Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputScanFolder <em>Input Scan Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputDataFolder <em>Output Data Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputScanFolder <em>Output Scan Folder</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDone <em>Done</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType()
 * @model extendedMetaData="name='BeamlineUser_._type' kind='elementOnly'"
 * @generated
 */
public interface BeamlineUserType extends EObject {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' containment reference.
	 * @see #setType(TypeType17)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_Type()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='Type' namespace='##targetNamespace'"
	 * @generated
	 */
	TypeType17 getType();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getType <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' containment reference.
	 * @see #getType()
	 * @generated
	 */
	void setType(TypeType17 value);

	/**
	 * Returns the value of the '<em><b>Beamline Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Beamline Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Beamline Name</em>' attribute.
	 * @see #setBeamlineName(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_BeamlineName()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='BeamlineName' namespace='##targetNamespace'"
	 * @generated
	 */
	String getBeamlineName();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getBeamlineName <em>Beamline Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Beamline Name</em>' attribute.
	 * @see #getBeamlineName()
	 * @generated
	 */
	void setBeamlineName(String value);

	/**
	 * Returns the value of the '<em><b>Year</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Year</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Year</em>' attribute.
	 * @see #setYear(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_Year()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Year' namespace='##targetNamespace'"
	 * @generated
	 */
	String getYear();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getYear <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Year</em>' attribute.
	 * @see #getYear()
	 * @generated
	 */
	void setYear(String value);

	/**
	 * Returns the value of the '<em><b>Month</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Month</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Month</em>' attribute.
	 * @see #setMonth(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_Month()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Month' namespace='##targetNamespace'"
	 * @generated
	 */
	String getMonth();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getMonth <em>Month</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Month</em>' attribute.
	 * @see #getMonth()
	 * @generated
	 */
	void setMonth(String value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_Date()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='Date' namespace='##targetNamespace'"
	 * @generated
	 */
	String getDate();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(String value);

	/**
	 * Returns the value of the '<em><b>Visit Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visit Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visit Number</em>' attribute.
	 * @see #setVisitNumber(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_VisitNumber()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='VisitNumber' namespace='##targetNamespace'"
	 * @generated
	 */
	String getVisitNumber();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getVisitNumber <em>Visit Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visit Number</em>' attribute.
	 * @see #getVisitNumber()
	 * @generated
	 */
	void setVisitNumber(String value);

	/**
	 * Returns the value of the '<em><b>Input Data Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input Data Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input Data Folder</em>' attribute.
	 * @see #setInputDataFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_InputDataFolder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='InputDataFolder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getInputDataFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputDataFolder <em>Input Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input Data Folder</em>' attribute.
	 * @see #getInputDataFolder()
	 * @generated
	 */
	void setInputDataFolder(String value);

	/**
	 * Returns the value of the '<em><b>Input Scan Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input Scan Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input Scan Folder</em>' attribute.
	 * @see #setInputScanFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_InputScanFolder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='InputScanFolder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getInputScanFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getInputScanFolder <em>Input Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input Scan Folder</em>' attribute.
	 * @see #getInputScanFolder()
	 * @generated
	 */
	void setInputScanFolder(String value);

	/**
	 * Returns the value of the '<em><b>Output Data Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Data Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Data Folder</em>' attribute.
	 * @see #setOutputDataFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_OutputDataFolder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='OutputDataFolder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getOutputDataFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputDataFolder <em>Output Data Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Data Folder</em>' attribute.
	 * @see #getOutputDataFolder()
	 * @generated
	 */
	void setOutputDataFolder(String value);

	/**
	 * Returns the value of the '<em><b>Output Scan Folder</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Scan Folder</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Scan Folder</em>' attribute.
	 * @see #setOutputScanFolder(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_OutputScanFolder()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='OutputScanFolder' namespace='##targetNamespace'"
	 * @generated
	 */
	String getOutputScanFolder();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getOutputScanFolder <em>Output Scan Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Scan Folder</em>' attribute.
	 * @see #getOutputScanFolder()
	 * @generated
	 */
	void setOutputScanFolder(String value);

	/**
	 * Returns the value of the '<em><b>Done</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Done</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Done</em>' attribute.
	 * @see #setDone(String)
	 * @see uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage#getBeamlineUserType_Done()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString" required="true"
	 *        extendedMetaData="kind='attribute' name='done' namespace='##targetNamespace'"
	 * @generated
	 */
	String getDone();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BeamlineUserType#getDone <em>Done</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Done</em>' attribute.
	 * @see #getDone()
	 * @generated
	 */
	void setDone(String value);

} // BeamlineUserType
