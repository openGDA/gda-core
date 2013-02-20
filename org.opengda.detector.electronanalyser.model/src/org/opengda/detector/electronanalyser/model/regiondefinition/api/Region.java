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
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRegionId <em>Region Id</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode <em>Lens Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRunMode <em>Run Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy <em>Excitation Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy <em>Fix Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy <em>Low Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy <em>High Energy</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep <em>Energy Step</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime <em>Step Time</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel <em>First XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel <em>Last XChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel <em>First YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel <em>Last YChannel</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices <em>Slices</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode <em>Detector Mode</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask <em>ADC Mask</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel <em>Discriminator Level</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalSteps <em>Total Steps</em>}</li>
 *   <li>{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalTime <em>Total Time</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion()
 * @model
 * @generated
 */
public interface Region extends EObject {
	/**
	 * Returns the value of the '<em><b>Region Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Region Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Region Id</em>' attribute.
	 * @see #setRegionId(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_RegionId()
	 * @model id="true"
	 * @generated
	 */
	String getRegionId();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getRegionId <em>Region Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Region Id</em>' attribute.
	 * @see #getRegionId()
	 * @generated
	 */
	void setRegionId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * The default value is <code>"New Region"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #isSetName()
	 * @see #unsetName()
	 * @see #setName(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Name()
	 * @model default="New Region" unsettable="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #isSetName()
	 * @see #unsetName()
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetName()
	 * @see #getName()
	 * @see #setName(String)
	 * @generated
	 */
	void unsetName();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getName <em>Name</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Name</em>' attribute is set.
	 * @see #unsetName()
	 * @see #getName()
	 * @see #setName(String)
	 * @generated
	 */
	boolean isSetName();

	/**
	 * Returns the value of the '<em><b>Lens Mode</b></em>' attribute.
	 * The default value is <code>"Transmission"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lens Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lens Mode</em>' attribute.
	 * @see #isSetLensMode()
	 * @see #unsetLensMode()
	 * @see #setLensMode(String)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_LensMode()
	 * @model default="Transmission" unsettable="true"
	 * @generated
	 */
	String getLensMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode <em>Lens Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lens Mode</em>' attribute.
	 * @see #isSetLensMode()
	 * @see #unsetLensMode()
	 * @see #getLensMode()
	 * @generated
	 */
	void setLensMode(String value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode <em>Lens Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLensMode()
	 * @see #getLensMode()
	 * @see #setLensMode(String)
	 * @generated
	 */
	void unsetLensMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLensMode <em>Lens Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Lens Mode</em>' attribute is set.
	 * @see #unsetLensMode()
	 * @see #getLensMode()
	 * @see #setLensMode(String)
	 * @generated
	 */
	boolean isSetLensMode();

	/**
	 * Returns the value of the '<em><b>Pass Energy</b></em>' attribute.
	 * The default value is <code>"10"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pass Energy</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pass Energy</em>' attribute.
	 * @see #isSetPassEnergy()
	 * @see #unsetPassEnergy()
	 * @see #setPassEnergy(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_PassEnergy()
	 * @model default="10" unsettable="true"
	 * @generated
	 */
	int getPassEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pass Energy</em>' attribute.
	 * @see #isSetPassEnergy()
	 * @see #unsetPassEnergy()
	 * @see #getPassEnergy()
	 * @generated
	 */
	void setPassEnergy(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getPassEnergy <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPassEnergy()
	 * @see #getPassEnergy()
	 * @see #setPassEnergy(int)
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
	 * @see #setPassEnergy(int)
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
	 * Returns the value of the '<em><b>Excitation Energy</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Excitation Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Excitation Energy</em>' attribute.
	 * @see #isSetExcitationEnergy()
	 * @see #unsetExcitationEnergy()
	 * @see #setExcitationEnergy(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_ExcitationEnergy()
	 * @model default="0.0" unsettable="true"
	 * @generated
	 */
	double getExcitationEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy <em>Excitation Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Excitation Energy</em>' attribute.
	 * @see #isSetExcitationEnergy()
	 * @see #unsetExcitationEnergy()
	 * @see #getExcitationEnergy()
	 * @generated
	 */
	void setExcitationEnergy(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy <em>Excitation Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetExcitationEnergy()
	 * @see #getExcitationEnergy()
	 * @see #setExcitationEnergy(double)
	 * @generated
	 */
	void unsetExcitationEnergy();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getExcitationEnergy <em>Excitation Energy</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Excitation Energy</em>' attribute is set.
	 * @see #unsetExcitationEnergy()
	 * @see #getExcitationEnergy()
	 * @see #setExcitationEnergy(double)
	 * @generated
	 */
	boolean isSetExcitationEnergy();

	/**
	 * Returns the value of the '<em><b>Acquisition Mode</b></em>' attribute.
	 * The default value is <code>"SWEPT"</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Acquisition Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Acquisition Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE
	 * @see #isSetAcquisitionMode()
	 * @see #unsetAcquisitionMode()
	 * @see #setAcquisitionMode(ACQUISITION_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_AcquisitionMode()
	 * @model default="SWEPT" unsettable="true"
	 * @generated
	 */
	ACQUISITION_MODE getAcquisitionMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Acquisition Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE
	 * @see #isSetAcquisitionMode()
	 * @see #unsetAcquisitionMode()
	 * @see #getAcquisitionMode()
	 * @generated
	 */
	void setAcquisitionMode(ACQUISITION_MODE value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getAcquisitionMode <em>Acquisition Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetAcquisitionMode()
	 * @see #getAcquisitionMode()
	 * @see #setAcquisitionMode(ACQUISITION_MODE)
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
	 * @see #setAcquisitionMode(ACQUISITION_MODE)
	 * @generated
	 */
	boolean isSetAcquisitionMode();

	/**
	 * Returns the value of the '<em><b>Energy Mode</b></em>' attribute.
	 * The default value is <code>"KINTETIC"</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see #isSetEnergyMode()
	 * @see #unsetEnergyMode()
	 * @see #setEnergyMode(ENERGY_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_EnergyMode()
	 * @model default="KINTETIC" unsettable="true"
	 * @generated
	 */
	ENERGY_MODE getEnergyMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE
	 * @see #isSetEnergyMode()
	 * @see #unsetEnergyMode()
	 * @see #getEnergyMode()
	 * @generated
	 */
	void setEnergyMode(ENERGY_MODE value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetEnergyMode()
	 * @see #getEnergyMode()
	 * @see #setEnergyMode(ENERGY_MODE)
	 * @generated
	 */
	void unsetEnergyMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyMode <em>Energy Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Energy Mode</em>' attribute is set.
	 * @see #unsetEnergyMode()
	 * @see #getEnergyMode()
	 * @see #setEnergyMode(ENERGY_MODE)
	 * @generated
	 */
	boolean isSetEnergyMode();

	/**
	 * Returns the value of the '<em><b>Fix Energy</b></em>' attribute.
	 * The default value is <code>"9.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Fix Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fix Energy</em>' attribute.
	 * @see #isSetFixEnergy()
	 * @see #unsetFixEnergy()
	 * @see #setFixEnergy(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_FixEnergy()
	 * @model default="9.0" unsettable="true"
	 * @generated
	 */
	double getFixEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy <em>Fix Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fix Energy</em>' attribute.
	 * @see #isSetFixEnergy()
	 * @see #unsetFixEnergy()
	 * @see #getFixEnergy()
	 * @generated
	 */
	void setFixEnergy(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy <em>Fix Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFixEnergy()
	 * @see #getFixEnergy()
	 * @see #setFixEnergy(double)
	 * @generated
	 */
	void unsetFixEnergy();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFixEnergy <em>Fix Energy</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Fix Energy</em>' attribute is set.
	 * @see #unsetFixEnergy()
	 * @see #getFixEnergy()
	 * @see #setFixEnergy(double)
	 * @generated
	 */
	boolean isSetFixEnergy();

	/**
	 * Returns the value of the '<em><b>Low Energy</b></em>' attribute.
	 * The default value is <code>"8.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Low Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Low Energy</em>' attribute.
	 * @see #isSetLowEnergy()
	 * @see #unsetLowEnergy()
	 * @see #setLowEnergy(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_LowEnergy()
	 * @model default="8.0" unsettable="true"
	 * @generated
	 */
	double getLowEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy <em>Low Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Low Energy</em>' attribute.
	 * @see #isSetLowEnergy()
	 * @see #unsetLowEnergy()
	 * @see #getLowEnergy()
	 * @generated
	 */
	void setLowEnergy(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy <em>Low Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLowEnergy()
	 * @see #getLowEnergy()
	 * @see #setLowEnergy(double)
	 * @generated
	 */
	void unsetLowEnergy();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLowEnergy <em>Low Energy</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Low Energy</em>' attribute is set.
	 * @see #unsetLowEnergy()
	 * @see #getLowEnergy()
	 * @see #setLowEnergy(double)
	 * @generated
	 */
	boolean isSetLowEnergy();

	/**
	 * Returns the value of the '<em><b>High Energy</b></em>' attribute.
	 * The default value is <code>"10.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>High Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>High Energy</em>' attribute.
	 * @see #isSetHighEnergy()
	 * @see #unsetHighEnergy()
	 * @see #setHighEnergy(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_HighEnergy()
	 * @model default="10.0" unsettable="true"
	 * @generated
	 */
	double getHighEnergy();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy <em>High Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>High Energy</em>' attribute.
	 * @see #isSetHighEnergy()
	 * @see #unsetHighEnergy()
	 * @see #getHighEnergy()
	 * @generated
	 */
	void setHighEnergy(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy <em>High Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetHighEnergy()
	 * @see #getHighEnergy()
	 * @see #setHighEnergy(double)
	 * @generated
	 */
	void unsetHighEnergy();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getHighEnergy <em>High Energy</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>High Energy</em>' attribute is set.
	 * @see #unsetHighEnergy()
	 * @see #getHighEnergy()
	 * @see #setHighEnergy(double)
	 * @generated
	 */
	boolean isSetHighEnergy();

	/**
	 * Returns the value of the '<em><b>Energy Step</b></em>' attribute.
	 * The default value is <code>"200.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Energy Step</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Energy Step</em>' attribute.
	 * @see #isSetEnergyStep()
	 * @see #unsetEnergyStep()
	 * @see #setEnergyStep(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_EnergyStep()
	 * @model default="200.0" unsettable="true"
	 * @generated
	 */
	double getEnergyStep();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep <em>Energy Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Energy Step</em>' attribute.
	 * @see #isSetEnergyStep()
	 * @see #unsetEnergyStep()
	 * @see #getEnergyStep()
	 * @generated
	 */
	void setEnergyStep(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep <em>Energy Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetEnergyStep()
	 * @see #getEnergyStep()
	 * @see #setEnergyStep(double)
	 * @generated
	 */
	void unsetEnergyStep();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getEnergyStep <em>Energy Step</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Energy Step</em>' attribute is set.
	 * @see #unsetEnergyStep()
	 * @see #getEnergyStep()
	 * @see #setEnergyStep(double)
	 * @generated
	 */
	boolean isSetEnergyStep();

	/**
	 * Returns the value of the '<em><b>Step Time</b></em>' attribute.
	 * The default value is <code>"0.043"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Step Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Step Time</em>' attribute.
	 * @see #isSetStepTime()
	 * @see #unsetStepTime()
	 * @see #setStepTime(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_StepTime()
	 * @model default="0.043" unsettable="true"
	 * @generated
	 */
	double getStepTime();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime <em>Step Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Step Time</em>' attribute.
	 * @see #isSetStepTime()
	 * @see #unsetStepTime()
	 * @see #getStepTime()
	 * @generated
	 */
	void setStepTime(double value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime <em>Step Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStepTime()
	 * @see #getStepTime()
	 * @see #setStepTime(double)
	 * @generated
	 */
	void unsetStepTime();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStepTime <em>Step Time</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Step Time</em>' attribute is set.
	 * @see #unsetStepTime()
	 * @see #getStepTime()
	 * @see #setStepTime(double)
	 * @generated
	 */
	boolean isSetStepTime();

	/**
	 * Returns the value of the '<em><b>First XChannel</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>First XChannel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First XChannel</em>' attribute.
	 * @see #isSetFirstXChannel()
	 * @see #unsetFirstXChannel()
	 * @see #setFirstXChannel(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_FirstXChannel()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getFirstXChannel();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First XChannel</em>' attribute.
	 * @see #isSetFirstXChannel()
	 * @see #unsetFirstXChannel()
	 * @see #getFirstXChannel()
	 * @generated
	 */
	void setFirstXChannel(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel <em>First XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFirstXChannel()
	 * @see #getFirstXChannel()
	 * @see #setFirstXChannel(int)
	 * @generated
	 */
	void unsetFirstXChannel();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstXChannel <em>First XChannel</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>First XChannel</em>' attribute is set.
	 * @see #unsetFirstXChannel()
	 * @see #getFirstXChannel()
	 * @see #setFirstXChannel(int)
	 * @generated
	 */
	boolean isSetFirstXChannel();

	/**
	 * Returns the value of the '<em><b>Last XChannel</b></em>' attribute.
	 * The default value is <code>"1024"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last XChannel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last XChannel</em>' attribute.
	 * @see #isSetLastXChannel()
	 * @see #unsetLastXChannel()
	 * @see #setLastXChannel(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_LastXChannel()
	 * @model default="1024" unsettable="true"
	 * @generated
	 */
	int getLastXChannel();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last XChannel</em>' attribute.
	 * @see #isSetLastXChannel()
	 * @see #unsetLastXChannel()
	 * @see #getLastXChannel()
	 * @generated
	 */
	void setLastXChannel(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel <em>Last XChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLastXChannel()
	 * @see #getLastXChannel()
	 * @see #setLastXChannel(int)
	 * @generated
	 */
	void unsetLastXChannel();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastXChannel <em>Last XChannel</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Last XChannel</em>' attribute is set.
	 * @see #unsetLastXChannel()
	 * @see #getLastXChannel()
	 * @see #setLastXChannel(int)
	 * @generated
	 */
	boolean isSetLastXChannel();

	/**
	 * Returns the value of the '<em><b>First YChannel</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>First YChannel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First YChannel</em>' attribute.
	 * @see #isSetFirstYChannel()
	 * @see #unsetFirstYChannel()
	 * @see #setFirstYChannel(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_FirstYChannel()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getFirstYChannel();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First YChannel</em>' attribute.
	 * @see #isSetFirstYChannel()
	 * @see #unsetFirstYChannel()
	 * @see #getFirstYChannel()
	 * @generated
	 */
	void setFirstYChannel(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel <em>First YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetFirstYChannel()
	 * @see #getFirstYChannel()
	 * @see #setFirstYChannel(int)
	 * @generated
	 */
	void unsetFirstYChannel();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getFirstYChannel <em>First YChannel</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>First YChannel</em>' attribute is set.
	 * @see #unsetFirstYChannel()
	 * @see #getFirstYChannel()
	 * @see #setFirstYChannel(int)
	 * @generated
	 */
	boolean isSetFirstYChannel();

	/**
	 * Returns the value of the '<em><b>Last YChannel</b></em>' attribute.
	 * The default value is <code>"1024"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last YChannel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last YChannel</em>' attribute.
	 * @see #isSetLastYChannel()
	 * @see #unsetLastYChannel()
	 * @see #setLastYChannel(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_LastYChannel()
	 * @model default="1024" unsettable="true"
	 * @generated
	 */
	int getLastYChannel();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last YChannel</em>' attribute.
	 * @see #isSetLastYChannel()
	 * @see #unsetLastYChannel()
	 * @see #getLastYChannel()
	 * @generated
	 */
	void setLastYChannel(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel <em>Last YChannel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLastYChannel()
	 * @see #getLastYChannel()
	 * @see #setLastYChannel(int)
	 * @generated
	 */
	void unsetLastYChannel();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getLastYChannel <em>Last YChannel</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Last YChannel</em>' attribute is set.
	 * @see #unsetLastYChannel()
	 * @see #getLastYChannel()
	 * @see #setLastYChannel(int)
	 * @generated
	 */
	boolean isSetLastYChannel();

	/**
	 * Returns the value of the '<em><b>Slices</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Slices</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Slices</em>' attribute.
	 * @see #isSetSlices()
	 * @see #unsetSlices()
	 * @see #setSlices(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Slices()
	 * @model default="1" unsettable="true"
	 * @generated
	 */
	int getSlices();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Slices</em>' attribute.
	 * @see #isSetSlices()
	 * @see #unsetSlices()
	 * @see #getSlices()
	 * @generated
	 */
	void setSlices(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices <em>Slices</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSlices()
	 * @see #getSlices()
	 * @see #setSlices(int)
	 * @generated
	 */
	void unsetSlices();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getSlices <em>Slices</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Slices</em>' attribute is set.
	 * @see #unsetSlices()
	 * @see #getSlices()
	 * @see #setSlices(int)
	 * @generated
	 */
	boolean isSetSlices();

	/**
	 * Returns the value of the '<em><b>Detector Mode</b></em>' attribute.
	 * The default value is <code>"ADC"</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Detector Mode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Detector Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
	 * @see #isSetDetectorMode()
	 * @see #unsetDetectorMode()
	 * @see #setDetectorMode(DETECTOR_MODE)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_DetectorMode()
	 * @model default="ADC" unsettable="true"
	 * @generated
	 */
	DETECTOR_MODE getDetectorMode();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Detector Mode</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE
	 * @see #isSetDetectorMode()
	 * @see #unsetDetectorMode()
	 * @see #getDetectorMode()
	 * @generated
	 */
	void setDetectorMode(DETECTOR_MODE value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode <em>Detector Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDetectorMode()
	 * @see #getDetectorMode()
	 * @see #setDetectorMode(DETECTOR_MODE)
	 * @generated
	 */
	void unsetDetectorMode();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDetectorMode <em>Detector Mode</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Detector Mode</em>' attribute is set.
	 * @see #unsetDetectorMode()
	 * @see #getDetectorMode()
	 * @see #setDetectorMode(DETECTOR_MODE)
	 * @generated
	 */
	boolean isSetDetectorMode();

	/**
	 * Returns the value of the '<em><b>ADC Mask</b></em>' attribute.
	 * The default value is <code>"255"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>ADC Mask</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>ADC Mask</em>' attribute.
	 * @see #isSetADCMask()
	 * @see #unsetADCMask()
	 * @see #setADCMask(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_ADCMask()
	 * @model default="255" unsettable="true"
	 * @generated
	 */
	int getADCMask();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask <em>ADC Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>ADC Mask</em>' attribute.
	 * @see #isSetADCMask()
	 * @see #unsetADCMask()
	 * @see #getADCMask()
	 * @generated
	 */
	void setADCMask(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask <em>ADC Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetADCMask()
	 * @see #getADCMask()
	 * @see #setADCMask(int)
	 * @generated
	 */
	void unsetADCMask();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getADCMask <em>ADC Mask</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>ADC Mask</em>' attribute is set.
	 * @see #unsetADCMask()
	 * @see #getADCMask()
	 * @see #setADCMask(int)
	 * @generated
	 */
	boolean isSetADCMask();

	/**
	 * Returns the value of the '<em><b>Discriminator Level</b></em>' attribute.
	 * The default value is <code>"10"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Discriminator Level</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Discriminator Level</em>' attribute.
	 * @see #isSetDiscriminatorLevel()
	 * @see #unsetDiscriminatorLevel()
	 * @see #setDiscriminatorLevel(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_DiscriminatorLevel()
	 * @model default="10" unsettable="true"
	 * @generated
	 */
	int getDiscriminatorLevel();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel <em>Discriminator Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Discriminator Level</em>' attribute.
	 * @see #isSetDiscriminatorLevel()
	 * @see #unsetDiscriminatorLevel()
	 * @see #getDiscriminatorLevel()
	 * @generated
	 */
	void setDiscriminatorLevel(int value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel <em>Discriminator Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDiscriminatorLevel()
	 * @see #getDiscriminatorLevel()
	 * @see #setDiscriminatorLevel(int)
	 * @generated
	 */
	void unsetDiscriminatorLevel();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getDiscriminatorLevel <em>Discriminator Level</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Discriminator Level</em>' attribute is set.
	 * @see #unsetDiscriminatorLevel()
	 * @see #getDiscriminatorLevel()
	 * @see #setDiscriminatorLevel(int)
	 * @generated
	 */
	boolean isSetDiscriminatorLevel();

	/**
	 * Returns the value of the '<em><b>Total Steps</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Steps</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Steps</em>' attribute.
	 * @see #setTotalSteps(int)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_TotalSteps()
	 * @model default="0" transient="true"
	 * @generated
	 */
	int getTotalSteps();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalSteps <em>Total Steps</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Steps</em>' attribute.
	 * @see #getTotalSteps()
	 * @generated
	 */
	void setTotalSteps(int value);

	/**
	 * Returns the value of the '<em><b>Total Time</b></em>' attribute.
	 * The default value is <code>"0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Time</em>' attribute.
	 * @see #setTotalTime(double)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_TotalTime()
	 * @model default="0.0" transient="true"
	 * @generated
	 */
	double getTotalTime();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getTotalTime <em>Total Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Time</em>' attribute.
	 * @see #getTotalTime()
	 * @generated
	 */
	void setTotalTime(double value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"READY"</code>.
	 * The literals are from the enumeration {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS
	 * @see #setStatus(STATUS)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Status()
	 * @model default="READY" transient="true"
	 * @generated
	 */
	STATUS getStatus();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(STATUS value);

	/**
	 * Returns the value of the '<em><b>Enabled</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Enabled</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enabled</em>' attribute.
	 * @see #isSetEnabled()
	 * @see #unsetEnabled()
	 * @see #setEnabled(boolean)
	 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRegion_Enabled()
	 * @model default="true" unsettable="true"
	 * @generated
	 */
	boolean isEnabled();

	/**
	 * Sets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enabled</em>' attribute.
	 * @see #isSetEnabled()
	 * @see #unsetEnabled()
	 * @see #isEnabled()
	 * @generated
	 */
	void setEnabled(boolean value);

	/**
	 * Unsets the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetEnabled()
	 * @see #isEnabled()
	 * @see #setEnabled(boolean)
	 * @generated
	 */
	void unsetEnabled();

	/**
	 * Returns whether the value of the '{@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region#isEnabled <em>Enabled</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Enabled</em>' attribute is set.
	 * @see #unsetEnabled()
	 * @see #isEnabled()
	 * @see #setEnabled(boolean)
	 * @generated
	 */
	boolean isSetEnabled();

} // Region
