/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Step</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getFrames <em>Frames</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTime <em>Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getSize <em>Size</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalTime <em>Total Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalSteps <em>Total Steps</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep()
 * @model
 * @generated
 */
public interface Step extends EObject {
	/**
	 * Returns the value of the '<em><b>Frames</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Frames</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Frames</em>' attribute.
	 * @see #setFrames(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep_Frames()
	 * @model
	 * @generated
	 */
	int getFrames();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getFrames <em>Frames</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Frames</em>' attribute.
	 * @see #getFrames()
	 * @generated
	 */
	void setFrames(int value);

	/**
	 * Returns the value of the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time</em>' attribute.
	 * @see #setTime(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep_Time()
	 * @model
	 * @generated
	 */
	double getTime();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTime <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time</em>' attribute.
	 * @see #getTime()
	 * @generated
	 */
	void setTime(double value);

	/**
	 * Returns the value of the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Size</em>' attribute.
	 * @see #setSize(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep_Size()
	 * @model
	 * @generated
	 */
	double getSize();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getSize <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Size</em>' attribute.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(double value);

	/**
	 * Returns the value of the '<em><b>Total Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Time</em>' attribute.
	 * @see #setTotalTime(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep_TotalTime()
	 * @model
	 * @generated
	 */
	double getTotalTime();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalTime <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Time</em>' attribute.
	 * @see #getTotalTime()
	 * @generated
	 */
	void setTotalTime(double value);

	/**
	 * Returns the value of the '<em><b>Total Steps</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Steps</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Steps</em>' attribute.
	 * @see #setTotalSteps(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getStep_TotalSteps()
	 * @model
	 * @generated
	 */
	int getTotalSteps();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Step#getTotalSteps <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Steps</em>' attribute.
	 * @see #getTotalSteps()
	 * @generated
	 */
	void setTotalSteps(int value);

} // Step
