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
 * A representation of the model object '<em><b>Tomodo Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getShutter <em>Shutter</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getTifimage <em>Tifimage</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getNexusfile <em>Nexusfile</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getSettingsfile <em>Settingsfile</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getImagekeyencoding <em>Imagekeyencoding</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.TomodoType#getCluster <em>Cluster</em>}</li>
 * </ul>
 * </p>
 *
 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType()
 * @model extendedMetaData="name='tomodo_._type' kind='elementOnly'"
 * @generated
 */
public interface TomodoType extends EObject {
	/**
	 * Returns the value of the '<em><b>Shutter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shutter</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shutter</em>' containment reference.
	 * @see #setShutter(ShutterType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Shutter()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='shutter' namespace='##targetNamespace'"
	 * @generated
	 */
	ShutterType getShutter();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getShutter <em>Shutter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shutter</em>' containment reference.
	 * @see #getShutter()
	 * @generated
	 */
	void setShutter(ShutterType value);

	/**
	 * Returns the value of the '<em><b>Tifimage</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tifimage</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tifimage</em>' containment reference.
	 * @see #setTifimage(TifimageType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Tifimage()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='tifimage' namespace='##targetNamespace'"
	 * @generated
	 */
	TifimageType getTifimage();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getTifimage <em>Tifimage</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tifimage</em>' containment reference.
	 * @see #getTifimage()
	 * @generated
	 */
	void setTifimage(TifimageType value);

	/**
	 * Returns the value of the '<em><b>Nexusfile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nexusfile</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nexusfile</em>' containment reference.
	 * @see #setNexusfile(NexusfileType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Nexusfile()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='nexusfile' namespace='##targetNamespace'"
	 * @generated
	 */
	NexusfileType getNexusfile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getNexusfile <em>Nexusfile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nexusfile</em>' containment reference.
	 * @see #getNexusfile()
	 * @generated
	 */
	void setNexusfile(NexusfileType value);

	/**
	 * Returns the value of the '<em><b>Settingsfile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Settingsfile</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Settingsfile</em>' containment reference.
	 * @see #setSettingsfile(SettingsfileType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Settingsfile()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='settingsfile' namespace='##targetNamespace'"
	 * @generated
	 */
	SettingsfileType getSettingsfile();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getSettingsfile <em>Settingsfile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Settingsfile</em>' containment reference.
	 * @see #getSettingsfile()
	 * @generated
	 */
	void setSettingsfile(SettingsfileType value);

	/**
	 * Returns the value of the '<em><b>Imagekeyencoding</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Imagekeyencoding</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Imagekeyencoding</em>' containment reference.
	 * @see #setImagekeyencoding(ImagekeyencodingType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Imagekeyencoding()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='imagekeyencoding' namespace='##targetNamespace'"
	 * @generated
	 */
	ImagekeyencodingType getImagekeyencoding();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getImagekeyencoding <em>Imagekeyencoding</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Imagekeyencoding</em>' containment reference.
	 * @see #getImagekeyencoding()
	 * @generated
	 */
	void setImagekeyencoding(ImagekeyencodingType value);

	/**
	 * Returns the value of the '<em><b>Cluster</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cluster</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cluster</em>' containment reference.
	 * @see #setCluster(ClusterType)
	 * @see uk.ac.diamond.tomography.localtomo.LocalTomoPackage#getTomodoType_Cluster()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='cluster' namespace='##targetNamespace'"
	 * @generated
	 */
	ClusterType getCluster();

	/**
	 * Sets the value of the '{@link uk.ac.diamond.tomography.localtomo.TomodoType#getCluster <em>Cluster</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cluster</em>' containment reference.
	 * @see #getCluster()
	 * @generated
	 */
	void setCluster(ClusterType value);

} // TomodoType
