/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Scan Collected</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.ScanCollected#getScanNumber <em>Scan Number</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.ScanCollected#getStartTime <em>Start Time</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.ScanCollected#getEndTime <em>End Time</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getScanCollected()
 * @model
 * @generated
 */
public interface ScanCollected extends EObject {
	/**
	 * Returns the value of the '<em><b>Scan Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scan Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scan Number</em>' attribute.
	 * @see #setScanNumber(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getScanCollected_ScanNumber()
	 * @model
	 * @generated
	 */
	String getScanNumber();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.ScanCollected#getScanNumber <em>Scan Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scan Number</em>' attribute.
	 * @see #getScanNumber()
	 * @generated
	 */
	void setScanNumber(String value);

	/**
	 * Returns the value of the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Time</em>' attribute.
	 * @see #setStartTime(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getScanCollected_StartTime()
	 * @model
	 * @generated
	 */
	String getStartTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.ScanCollected#getStartTime <em>Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Time</em>' attribute.
	 * @see #getStartTime()
	 * @generated
	 */
	void setStartTime(String value);

	/**
	 * Returns the value of the '<em><b>End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Time</em>' attribute.
	 * @see #setEndTime(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getScanCollected_EndTime()
	 * @model
	 * @generated
	 */
	String getEndTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.ScanCollected#getEndTime <em>End Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Time</em>' attribute.
	 * @see #getEndTime()
	 * @generated
	 */
	void setEndTime(String value);

} // ScanCollected
