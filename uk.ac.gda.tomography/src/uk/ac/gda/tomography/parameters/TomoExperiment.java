/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import java.util.Date;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tomo Experiment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.gda.tomography.parameters.TomoExperiment#getParameters <em>Parameters</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription <em>Description</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.TomoExperiment#getTotalTimeToRun <em>Total Time To Run</em>}</li>
 *   <li>{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion <em>Version</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getTomoExperiment()
 * @model
 * @generated
 */
public interface TomoExperiment extends EObject {
	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference.
	 * @see #setParameters(Parameters)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getTomoExperiment_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	Parameters getParameters();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getParameters <em>Parameters</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameters</em>' containment reference.
	 * @see #getParameters()
	 * @generated
	 */
	void setParameters(Parameters value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #isSetDescription()
	 * @see #unsetDescription()
	 * @see #setDescription(String)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getTomoExperiment_Description()
	 * @model unsettable="true"
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #isSetDescription()
	 * @see #unsetDescription()
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDescription()
	 * @see #getDescription()
	 * @see #setDescription(String)
	 * @generated
	 */
	void unsetDescription();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getDescription <em>Description</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Description</em>' attribute is set.
	 * @see #unsetDescription()
	 * @see #getDescription()
	 * @see #setDescription(String)
	 * @generated
	 */
	boolean isSetDescription();

	/**
	 * Returns the value of the '<em><b>Total Time To Run</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Time To Run</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Time To Run</em>' attribute.
	 * @see #setTotalTimeToRun(Date)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getTomoExperiment_TotalTimeToRun()
	 * @model
	 * @generated
	 */
	Date getTotalTimeToRun();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getTotalTimeToRun <em>Total Time To Run</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Time To Run</em>' attribute.
	 * @see #getTotalTimeToRun()
	 * @generated
	 */
	void setTotalTimeToRun(Date value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #isSetVersion()
	 * @see #unsetVersion()
	 * @see #setVersion(int)
	 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getTomoExperiment_Version()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getVersion();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #isSetVersion()
	 * @see #unsetVersion()
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(int value);

	/**
	 * Unsets the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetVersion()
	 * @see #getVersion()
	 * @see #setVersion(int)
	 * @generated
	 */
	void unsetVersion();

	/**
	 * Returns whether the value of the '{@link uk.ac.gda.tomography.parameters.TomoExperiment#getVersion <em>Version</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Version</em>' attribute is set.
	 * @see #unsetVersion()
	 * @see #getVersion()
	 * @see #setVersion(int)
	 * @generated
	 */
	boolean isSetVersion();

} // TomoExperiment
