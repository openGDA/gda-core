/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sequence</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRegion <em>Region</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations <em>Num Iterations</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum <em>Spectrum</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename <em>Filename</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence()
 * @model
 * @generated
 */
public interface Sequence extends EObject {
	/**
	 * Returns the value of the '<em><b>Region</b></em>' containment reference list.
	 * The list contents are of type {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Region</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Region</em>' containment reference list.
	 * @see #isSetRegion()
	 * @see #unsetRegion()
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_Region()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	EList<Region> getRegion();

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRegion <em>Region</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRegion()
	 * @see #getRegion()
	 * @generated
	 */
	void unsetRegion();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRegion <em>Region</em>}' containment reference list is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Region</em>' containment reference list is set.
	 * @see #unsetRegion()
	 * @see #getRegion()
	 * @generated
	 */
	boolean isSetRegion();

	/**
	 * Returns the value of the '<em><b>Run Mode</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Run Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Run Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see #isSetRunMode()
	 * @see #unsetRunMode()
	 * @see #setRunMode(RUN_MODES)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_RunMode()
	 * @model default="" unique="false" unsettable="true"
	 * @generated
	 */
	RUN_MODES getRunMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Run Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES
	 * @see #isSetRunMode()
	 * @see #unsetRunMode()
	 * @see #getRunMode()
	 * @generated
	 */
	void setRunMode(RUN_MODES value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode <em>Run Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRunMode()
	 * @see #getRunMode()
	 * @see #setRunMode(RUN_MODES)
	 * @generated
	 */
	void unsetRunMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getRunMode <em>Run Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Run Mode</em>' attribute is set.
	 * @see #unsetRunMode()
	 * @see #getRunMode()
	 * @see #setRunMode(RUN_MODES)
	 * @generated
	 */
	boolean isSetRunMode();

	/**
	 * Returns the value of the '<em><b>Num Iterations</b></em>' attribute.
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
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_NumIterations()
	 * @model unsettable="true"
	 * @generated
	 */
	int getNumIterations();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations <em>Num Iterations</em>}' attribute.
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
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations <em>Num Iterations</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetNumIterations()
	 * @see #getNumIterations()
	 * @see #setNumIterations(int)
	 * @generated
	 */
	void unsetNumIterations();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getNumIterations <em>Num Iterations</em>}' attribute is set.
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
	 * Returns the value of the '<em><b>Repeat Unitil Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Repeat Unitil Stopped</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Repeat Unitil Stopped</em>' attribute.
	 * @see #isSetRepeatUnitilStopped()
	 * @see #unsetRepeatUnitilStopped()
	 * @see #setRepeatUnitilStopped(boolean)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_RepeatUnitilStopped()
	 * @model unsettable="true"
	 * @generated
	 */
	boolean isRepeatUnitilStopped();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Repeat Unitil Stopped</em>' attribute.
	 * @see #isSetRepeatUnitilStopped()
	 * @see #unsetRepeatUnitilStopped()
	 * @see #isRepeatUnitilStopped()
	 * @generated
	 */
	void setRepeatUnitilStopped(boolean value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRepeatUnitilStopped()
	 * @see #isRepeatUnitilStopped()
	 * @see #setRepeatUnitilStopped(boolean)
	 * @generated
	 */
	void unsetRepeatUnitilStopped();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Repeat Unitil Stopped</em>' attribute is set.
	 * @see #unsetRepeatUnitilStopped()
	 * @see #isRepeatUnitilStopped()
	 * @see #setRepeatUnitilStopped(boolean)
	 * @generated
	 */
	boolean isSetRepeatUnitilStopped();

	/**
	 * Returns the value of the '<em><b>Spectrum</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Spectrum</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Spectrum</em>' containment reference.
	 * @see #isSetSpectrum()
	 * @see #unsetSpectrum()
	 * @see #setSpectrum(Spectrum)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_Spectrum()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	Spectrum getSpectrum();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum <em>Spectrum</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Spectrum</em>' containment reference.
	 * @see #isSetSpectrum()
	 * @see #unsetSpectrum()
	 * @see #getSpectrum()
	 * @generated
	 */
	void setSpectrum(Spectrum value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum <em>Spectrum</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSpectrum()
	 * @see #getSpectrum()
	 * @see #setSpectrum(Spectrum)
	 * @generated
	 */
	void unsetSpectrum();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getSpectrum <em>Spectrum</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Spectrum</em>' containment reference is set.
	 * @see #unsetSpectrum()
	 * @see #getSpectrum()
	 * @see #setSpectrum(Spectrum)
	 * @generated
	 */
	boolean isSetSpectrum();

	/**
	 * Returns the value of the '<em><b>Filename</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filename</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Filename</em>' attribute.
	 * @see #isSetFilename()
	 * @see #unsetFilename()
	 * @see #setFilename(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getSequence_Filename()
	 * @model unsettable="true"
	 * @generated
	 */
	String getFilename();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filename</em>' attribute.
	 * @see #isSetFilename()
	 * @see #unsetFilename()
	 * @see #getFilename()
	 * @generated
	 */
	void setFilename(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename <em>Filename</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFilename()
	 * @see #getFilename()
	 * @see #setFilename(String)
	 * @generated
	 */
	void unsetFilename();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence#getFilename <em>Filename</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Filename</em>' attribute is set.
	 * @see #unsetFilename()
	 * @see #getFilename()
	 * @see #setFilename(String)
	 * @generated
	 */
	boolean isSetFilename();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Region getRegion(String regionName);

} // Sequence
