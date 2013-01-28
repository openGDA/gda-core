/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Region</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getName <em>Name</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getLensmode <em>Lensmode</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getPassEnergy <em>Pass Energy</em>}</li>
 *   <li>{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getRunMode <em>Run Mode</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRegion()
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
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRegion_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Lensmode</b></em>' attribute.
	 * The literals are from the enumeration {@link com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lensmode</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lensmode</em>' attribute.
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE
	 * @see #setLensmode(LENS_MODE)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRegion_Lensmode()
	 * @model
	 * @generated
	 */
	LENS_MODE getLensmode();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getLensmode <em>Lensmode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lensmode</em>' attribute.
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE
	 * @see #getLensmode()
	 * @generated
	 */
	void setLensmode(LENS_MODE value);

	/**
	 * Returns the value of the '<em><b>Pass Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pass Energy</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pass Energy</em>' attribute.
	 * @see #setPassEnergy(Integer)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRegion_PassEnergy()
	 * @model
	 * @generated
	 */
	Integer getPassEnergy();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getPassEnergy <em>Pass Energy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pass Energy</em>' attribute.
	 * @see #getPassEnergy()
	 * @generated
	 */
	void setPassEnergy(Integer value);

	/**
	 * Returns the value of the '<em><b>Run Mode</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Run Mode</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Run Mode</em>' containment reference.
	 * @see #setRunMode(RunMode)
	 * @see com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage#getRegion_RunMode()
	 * @model containment="true"
	 * @generated
	 */
	RunMode getRunMode();

	/**
	 * Sets the value of the '{@link com.opengda.detector.electronalyser.server.model.regiondefinition.Region#getRunMode <em>Run Mode</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Run Mode</em>' containment reference.
	 * @see #getRunMode()
	 * @generated
	 */
	void setRunMode(RunMode value);

} // Region
