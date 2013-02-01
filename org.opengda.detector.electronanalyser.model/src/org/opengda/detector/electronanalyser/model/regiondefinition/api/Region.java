/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Region</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode <em>Lensmode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergy <em>Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStep <em>Step</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetector <em>Detector</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion()
 * @model
 * @generated
 */
public interface Region extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Lensmode</b></em>' attribute.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lensmode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lensmode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
	 * @see #isSetLensmode()
	 * @see #unsetLensmode()
	 * @see #setLensmode(LENS_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Lensmode()
	 * @model unsettable="true"
	 * @generated
	 */
	LENS_MODE getLensmode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode <em>Lensmode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lensmode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE
	 * @see #isSetLensmode()
	 * @see #unsetLensmode()
	 * @see #getLensmode()
	 * @generated
	 */
	void setLensmode(LENS_MODE value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode <em>Lensmode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLensmode()
	 * @see #getLensmode()
	 * @see #setLensmode(LENS_MODE)
	 * @generated
	 */
	void unsetLensmode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensmode <em>Lensmode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Lensmode</em>' attribute is set.
	 * @see #unsetLensmode()
	 * @see #getLensmode()
	 * @see #setLensmode(LENS_MODE)
	 * @generated
	 */
	boolean isSetLensmode();

	/**
	 * Returns the value of the '<em><b>Pass Energy</b></em>' attribute.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pass Energy</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pass Energy</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY
	 * @see #isSetPassEnergy()
	 * @see #unsetPassEnergy()
	 * @see #setPassEnergy(PASS_ENERGY)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_PassEnergy()
	 * @model unsettable="true"
	 * @generated
	 */
	PASS_ENERGY getPassEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pass Energy</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY
	 * @see #isSetPassEnergy()
	 * @see #unsetPassEnergy()
	 * @see #getPassEnergy()
	 * @generated
	 */
	void setPassEnergy(PASS_ENERGY value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPassEnergy()
	 * @see #getPassEnergy()
	 * @see #setPassEnergy(PASS_ENERGY)
	 * @generated
	 */
	void unsetPassEnergy();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Pass Energy</em>' attribute is set.
	 * @see #unsetPassEnergy()
	 * @see #getPassEnergy()
	 * @see #setPassEnergy(PASS_ENERGY)
	 * @generated
	 */
	boolean isSetPassEnergy();

	/**
	 * Returns the value of the '<em><b>Run Mode</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Run Mode</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Run Mode</em>' containment reference.
	 * @see #isSetRunMode()
	 * @see #unsetRunMode()
	 * @see #setRunMode(RunMode)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_RunMode()
	 * @model containment="true" unsettable="true"
	 * @generated
	 */
	RunMode getRunMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Run Mode</em>' containment reference.
	 * @see #isSetRunMode()
	 * @see #unsetRunMode()
	 * @see #getRunMode()
	 * @generated
	 */
	void setRunMode(RunMode value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRunMode()
	 * @see #getRunMode()
	 * @see #setRunMode(RunMode)
	 * @generated
	 */
	void unsetRunMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}' containment reference is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Run Mode</em>' containment reference is set.
	 * @see #unsetRunMode()
	 * @see #getRunMode()
	 * @see #setRunMode(RunMode)
	 * @generated
	 */
	boolean isSetRunMode();

	/**
	 * Returns the value of the '<em><b>Acquisition Mode</b></em>' attribute.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Acquisition Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Acquisition Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
	 * @see #isSetAcquisitionMode()
	 * @see #unsetAcquisitionMode()
	 * @see #setAcquisitionMode(ACQUIAITION_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_AcquisitionMode()
	 * @model unsettable="true"
	 * @generated
	 */
	ACQUIAITION_MODE getAcquisitionMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Acquisition Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUIAITION_MODE
	 * @see #isSetAcquisitionMode()
	 * @see #unsetAcquisitionMode()
	 * @see #getAcquisitionMode()
	 * @generated
	 */
	void setAcquisitionMode(ACQUIAITION_MODE value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetAcquisitionMode()
	 * @see #getAcquisitionMode()
	 * @see #setAcquisitionMode(ACQUIAITION_MODE)
	 * @generated
	 */
	void unsetAcquisitionMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Acquisition Mode</em>' attribute is set.
	 * @see #unsetAcquisitionMode()
	 * @see #getAcquisitionMode()
	 * @see #setAcquisitionMode(ACQUIAITION_MODE)
	 * @generated
	 */
	boolean isSetAcquisitionMode();

	/**
	 * Returns the value of the '<em><b>Energy Mode</b></em>' attribute.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see #setEnergyMode(ENERGY_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_EnergyMode()
	 * @model
	 * @generated
	 */
	ENERGY_MODE getEnergyMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see #getEnergyMode()
	 * @generated
	 */
	void setEnergyMode(ENERGY_MODE value);

	/**
	 * Returns the value of the '<em><b>Energy</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy</em>' containment reference.
	 * @see #setEnergy(Energy)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Energy()
	 * @model containment="true"
	 * @generated
	 */
	Energy getEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergy <em>Energy</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy</em>' containment reference.
	 * @see #getEnergy()
	 * @generated
	 */
	void setEnergy(Energy value);

	/**
	 * Returns the value of the '<em><b>Step</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Step</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Step</em>' containment reference.
	 * @see #setStep(Step)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Step()
	 * @model containment="true"
	 * @generated
	 */
	Step getStep();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStep <em>Step</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Step</em>' containment reference.
	 * @see #getStep()
	 * @generated
	 */
	void setStep(Step value);

	/**
	 * Returns the value of the '<em><b>Detector</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector</em>' containment reference.
	 * @see #setDetector(Detector)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Detector()
	 * @model containment="true"
	 * @generated
	 */
	Detector getDetector();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetector <em>Detector</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector</em>' containment reference.
	 * @see #getDetector()
	 * @generated
	 */
	void setDetector(Detector value);

} // Region
