/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Run Mode</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#getMode <em>Mode</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRunMode()
 * @model
 * @generated
 */
public interface RunMode extends EObject {
	/**
	 * Returns the value of the '<em><b>Mode</b></em>' attribute.
	 * The literals are from the enumeration {@link com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mode</em>' attribute.
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES
	 * @see #setMode(RUN_MODES)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRunMode_Mode()
	 * @model
	 * @generated
	 */
	RUN_MODES getMode();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#getMode <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mode</em>' attribute.
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES
	 * @see #getMode()
	 * @generated
	 */
	void setMode(RUN_MODES value);

	/**
	 * Returns the value of the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Num Iterations</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Num Iterations</em>' attribute.
	 * @see #setNumIterations(boolean)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRunMode_NumIterations()
	 * @model
	 * @generated
	 */
	boolean isNumIterations();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isNumIterations <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Num Iterations</em>' attribute.
	 * @see #isNumIterations()
	 * @generated
	 */
	void setNumIterations(boolean value);

	/**
	 * Returns the value of the '<em><b>Repeat Unitil Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Repeat Unitil Stopped</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Repeat Unitil Stopped</em>' attribute.
	 * @see #setRepeatUnitilStopped(boolean)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRunMode_RepeatUnitilStopped()
	 * @model
	 * @generated
	 */
	boolean isRepeatUnitilStopped();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Repeat Unitil Stopped</em>' attribute.
	 * @see #isRepeatUnitilStopped()
	 * @generated
	 */
	void setRepeatUnitilStopped(boolean value);

} // RunMode
