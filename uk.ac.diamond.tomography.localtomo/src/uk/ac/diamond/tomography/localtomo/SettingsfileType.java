/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Settingsfile Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.SettingsfileType#getBlueprint <em>Blueprint</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.SettingsfileType#getSettingsDirPostfix <em>Settings Dir Postfix</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getSettingsfileType()
 * @model extendedMetaData="name='settingsfile_._type' kind='elementOnly'"
 * @generated
 */
public interface SettingsfileType extends EObject {
	/**
	 * Returns the value of the '<em><b>Blueprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Blueprint</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Blueprint</em>' attribute.
	 * @see #setBlueprint(String)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getSettingsfileType_Blueprint()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='blueprint' namespace='##targetNamespace'"
	 * @generated
	 */
	String getBlueprint();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.SettingsfileType#getBlueprint <em>Blueprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Blueprint</em>' attribute.
	 * @see #getBlueprint()
	 * @generated
	 */
	void setBlueprint(String value);

	/**
	 * Returns the value of the '<em><b>Settings Dir Postfix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Settings Dir Postfix</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Settings Dir Postfix</em>' attribute.
	 * @see #setSettingsDirPostfix(String)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getSettingsfileType_SettingsDirPostfix()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='settingsDirPostfix' namespace='##targetNamespace'"
	 * @generated
	 */
	String getSettingsDirPostfix();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.SettingsfileType#getSettingsDirPostfix <em>Settings Dir Postfix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Settings Dir Postfix</em>' attribute.
	 * @see #getSettingsDirPostfix()
	 * @generated
	 */
	void setSettingsDirPostfix(String value);

} // SettingsfileType
