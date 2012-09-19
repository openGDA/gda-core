/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.localtomo;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Qsub Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getProjectname <em>Projectname</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getArgs <em>Args</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getSinoqueue <em>Sinoqueue</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getReconqueue <em>Reconqueue</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getQsubType()
 * @model extendedMetaData="name='qsub_._type' kind='elementOnly'"
 * @generated
 */
public interface QsubType extends EObject {
	/**
	 * Returns the value of the '<em><b>Projectname</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Projectname</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Projectname</em>' attribute.
	 * @see #setProjectname(String)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getQsubType_Projectname()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='projectname' namespace='##targetNamespace'"
	 * @generated
	 */
	String getProjectname();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getProjectname <em>Projectname</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Projectname</em>' attribute.
	 * @see #getProjectname()
	 * @generated
	 */
	void setProjectname(String value);

	/**
	 * Returns the value of the '<em><b>Args</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Args</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Args</em>' attribute.
	 * @see #setArgs(String)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getQsubType_Args()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='args' namespace='##targetNamespace'"
	 * @generated
	 */
	String getArgs();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getArgs <em>Args</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Args</em>' attribute.
	 * @see #getArgs()
	 * @generated
	 */
	void setArgs(String value);

	/**
	 * Returns the value of the '<em><b>Sinoqueue</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sinoqueue</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sinoqueue</em>' attribute.
	 * @see #setSinoqueue(String)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getQsubType_Sinoqueue()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='sinoqueue' namespace='##targetNamespace'"
	 * @generated
	 */
	String getSinoqueue();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getSinoqueue <em>Sinoqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sinoqueue</em>' attribute.
	 * @see #getSinoqueue()
	 * @generated
	 */
	void setSinoqueue(String value);

	/**
	 * Returns the value of the '<em><b>Reconqueue</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reconqueue</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reconqueue</em>' attribute.
	 * @see #setReconqueue(String)
	 * @see uk.ac.diamond.tomography.localtomo.localtomo.LocalTomoPackage#getQsubType_Reconqueue()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NormalizedString"
	 *        extendedMetaData="kind='element' name='reconqueue' namespace='##targetNamespace'"
	 * @generated
	 */
	String getReconqueue();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.localtomo.QsubType#getReconqueue <em>Reconqueue</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reconqueue</em>' attribute.
	 * @see #getReconqueue()
	 * @generated
	 */
	void setReconqueue(String value);

} // QsubType
