/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Run Mode</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode <em>Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex <em>Run Mode Index</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped <em>Repeat Until Stopped</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isConfirmAfterEachIteration <em>Confirm After Each Iteration</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isNumIterationOption <em>Num Iteration Option</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode()
 * @model
 * @generated
 */
public interface RunMode extends EObject {
	/**
	 * Returns the value of the '<em><b>Mode</b></em>' attribute.
	 * The default value is <code>"NORMAL"</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see #isSetMode()
	 * @see #unsetMode()
	 * @see #setMode(RUN_MODES)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_Mode()
	 * @model default="NORMAL" unsettable="true"
	 * @generated
	 */
	RUN_MODES getMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see #isSetMode()
	 * @see #unsetMode()
	 * @see #getMode()
	 * @generated
	 */
	void setMode(RUN_MODES value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMode()
	 * @see #getMode()
	 * @see #setMode(RUN_MODES)
	 * @generated
	 */
	void unsetMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getMode <em>Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Mode</em>' attribute is set.
	 * @see #unsetMode()
	 * @see #getMode()
	 * @see #setMode(RUN_MODES)
	 * @generated
	 */
	boolean isSetMode();

	/**
	 * Returns the value of the '<em><b>Run Mode Index</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Run Mode Index</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Run Mode Index</em>' attribute.
	 * @see #isSetRunModeIndex()
	 * @see #unsetRunModeIndex()
	 * @see #setRunModeIndex(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_RunModeIndex()
	 * @model default="0" unsettable="true"
	 * @generated
	 */
	int getRunModeIndex();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Run Mode Index</em>' attribute.
	 * @see #isSetRunModeIndex()
	 * @see #unsetRunModeIndex()
	 * @see #getRunModeIndex()
	 * @generated
	 */
	void setRunModeIndex(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex <em>Run Mode Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRunModeIndex()
	 * @see #getRunModeIndex()
	 * @see #setRunModeIndex(int)
	 * @generated
	 */
	void unsetRunModeIndex();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getRunModeIndex <em>Run Mode Index</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Run Mode Index</em>' attribute is set.
	 * @see #unsetRunModeIndex()
	 * @see #getRunModeIndex()
	 * @see #setRunModeIndex(int)
	 * @generated
	 */
	boolean isSetRunModeIndex();

	/**
	 * Returns the value of the '<em><b>Num Iterations</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Num Iterations</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Num Iterations</em>' attribute.
	 * @see #isSetNumIterations()
	 * @see #unsetNumIterations()
	 * @see #setNumIterations(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_NumIterations()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getNumIterations();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Num Iterations</em>' attribute.
	 * @see #isSetNumIterations()
	 * @see #unsetNumIterations()
	 * @see #getNumIterations()
	 * @generated
	 */
	void setNumIterations(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumIterations()
	 * @see #getNumIterations()
	 * @see #setNumIterations(int)
	 * @generated
	 */
	void unsetNumIterations();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#getNumIterations <em>Num Iterations</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Num Iterations</em>' attribute is set.
	 * @see #unsetNumIterations()
	 * @see #getNumIterations()
	 * @see #setNumIterations(int)
	 * @generated
	 */
	boolean isSetNumIterations();

	/**
	 * Returns the value of the '<em><b>Repeat Until Stopped</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Repeat Until Stopped</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Repeat Until Stopped</em>' attribute.
	 * @see #isSetRepeatUntilStopped()
	 * @see #unsetRepeatUntilStopped()
	 * @see #setRepeatUntilStopped(boolean)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_RepeatUntilStopped()
	 * @model default="false" unsettable="true"
	 * @generated
	 */
	boolean isRepeatUntilStopped();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Repeat Until Stopped</em>' attribute.
	 * @see #isSetRepeatUntilStopped()
	 * @see #unsetRepeatUntilStopped()
	 * @see #isRepeatUntilStopped()
	 * @generated
	 */
	void setRepeatUntilStopped(boolean value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped <em>Repeat Until Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRepeatUntilStopped()
	 * @see #isRepeatUntilStopped()
	 * @see #setRepeatUntilStopped(boolean)
	 * @generated
	 */
	void unsetRepeatUntilStopped();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isRepeatUntilStopped <em>Repeat Until Stopped</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Repeat Until Stopped</em>' attribute is set.
	 * @see #unsetRepeatUntilStopped()
	 * @see #isRepeatUntilStopped()
	 * @see #setRepeatUntilStopped(boolean)
	 * @generated
	 */
	boolean isSetRepeatUntilStopped();

	/**
	 * Returns the value of the '<em><b>Confirm After Each Iteration</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Confirm After Each Iteration</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Confirm After Each Iteration</em>' attribute.
	 * @see #setConfirmAfterEachIteration(boolean)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_ConfirmAfterEachIteration()
	 * @model default="false"
	 * @generated
	 */
	boolean isConfirmAfterEachIteration();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isConfirmAfterEachIteration <em>Confirm After Each Iteration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Confirm After Each Iteration</em>' attribute.
	 * @see #isConfirmAfterEachIteration()
	 * @generated
	 */
	void setConfirmAfterEachIteration(boolean value);

	/**
	 * Returns the value of the '<em><b>Num Iteration Option</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Num Iteration Option</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Num Iteration Option</em>' attribute.
	 * @see #setNumIterationOption(boolean)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRunMode_NumIterationOption()
	 * @model default="true" transient="true"
	 * @generated
	 */
	boolean isNumIterationOption();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RunMode#isNumIterationOption <em>Num Iteration Option</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Num Iteration Option</em>' attribute.
	 * @see #isNumIterationOption()
	 * @generated
	 */
	void setNumIterationOption(boolean value);

} // RunMode
