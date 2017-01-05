/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationFactory
 * @model kind="package"
 * @generated
 */
public interface EdxdcalibrationPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "edxdcalibration";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://opengda.org/edxdcalibration";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ec";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	EdxdcalibrationPackage eINSTANCE = uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl.init();

	/**
	 * The meta object id for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl <em>Edxd Calibration</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getEdxdCalibration()
	 * @generated
	 */
	int EDXD_CALIBRATION = 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl <em>Calibration Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getCalibrationConfig()
	 * @generated
	 */
	int CALIBRATION_CONFIG = 0;

	/**
	 * The feature id for the '<em><b>File Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALIBRATION_CONFIG__FILE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Last Calibrated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALIBRATION_CONFIG__LAST_CALIBRATED = 1;

	/**
	 * The number of structural features of the '<em>Calibration Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CALIBRATION_CONFIG_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.DocumentRootImpl
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getDocumentRoot()
	 * @generated
	 */
	int DOCUMENT_ROOT = 1;

	/**
	 * The feature id for the '<em><b>Edxd Calibration</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__EDXD_CALIBRATION = 0;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Hutch</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDXD_CALIBRATION__HUTCH = 0;

	/**
	 * The feature id for the '<em><b>Collimator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDXD_CALIBRATION__COLLIMATOR = 1;

	/**
	 * The feature id for the '<em><b>Energy Calibration</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDXD_CALIBRATION__ENERGY_CALIBRATION = 2;

	/**
	 * The feature id for the '<em><b>QCalibration</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDXD_CALIBRATION__QCALIBRATION = 3;

	/**
	 * The number of structural features of the '<em>Edxd Calibration</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDXD_CALIBRATION_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH <em>HUTCH</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getHUTCH()
	 * @generated
	 */
	int HUTCH = 3;

	/**
	 * The meta object id for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR <em>COLLIMATOR</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getCOLLIMATOR()
	 * @generated
	 */
	int COLLIMATOR = 4;


	/**
	 * Returns the meta object for class '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration <em>Edxd Calibration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Edxd Calibration</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration
	 * @generated
	 */
	EClass getEdxdCalibration();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getHutch <em>Hutch</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Hutch</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getHutch()
	 * @see #getEdxdCalibration()
	 * @generated
	 */
	EAttribute getEdxdCalibration_Hutch();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getCollimator <em>Collimator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Collimator</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getCollimator()
	 * @see #getEdxdCalibration()
	 * @generated
	 */
	EAttribute getEdxdCalibration_Collimator();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getEnergyCalibration <em>Energy Calibration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Energy Calibration</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getEnergyCalibration()
	 * @see #getEdxdCalibration()
	 * @generated
	 */
	EReference getEdxdCalibration_EnergyCalibration();

	/**
	 * Returns the meta object for the containment reference '{@link uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getQCalibration <em>QCalibration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>QCalibration</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration#getQCalibration()
	 * @see #getEdxdCalibration()
	 * @generated
	 */
	EReference getEdxdCalibration_QCalibration();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig <em>Calibration Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Calibration Config</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig
	 * @generated
	 */
	EClass getCalibrationConfig();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getFileName <em>File Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Name</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getFileName()
	 * @see #getCalibrationConfig()
	 * @generated
	 */
	EAttribute getCalibrationConfig_FileName();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getLastCalibrated <em>Last Calibrated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Calibrated</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig#getLastCalibrated()
	 * @see #getCalibrationConfig()
	 * @generated
	 */
	EAttribute getCalibrationConfig_LastCalibrated();

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the containment reference list '{@link uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot#getEdxdCalibration <em>Edxd Calibration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Edxd Calibration</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot#getEdxdCalibration()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_EdxdCalibration();

	/**
	 * Returns the meta object for enum '{@link uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH <em>HUTCH</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>HUTCH</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH
	 * @generated
	 */
	EEnum getHUTCH();

	/**
	 * Returns the meta object for enum '{@link uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR <em>COLLIMATOR</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>COLLIMATOR</em>'.
	 * @see uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR
	 * @generated
	 */
	EEnum getCOLLIMATOR();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	EdxdcalibrationFactory getEdxdcalibrationFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl <em>Edxd Calibration</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdCalibrationImpl
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getEdxdCalibration()
		 * @generated
		 */
		EClass EDXD_CALIBRATION = eINSTANCE.getEdxdCalibration();

		/**
		 * The meta object literal for the '<em><b>Hutch</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EDXD_CALIBRATION__HUTCH = eINSTANCE.getEdxdCalibration_Hutch();

		/**
		 * The meta object literal for the '<em><b>Collimator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EDXD_CALIBRATION__COLLIMATOR = eINSTANCE.getEdxdCalibration_Collimator();

		/**
		 * The meta object literal for the '<em><b>Energy Calibration</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDXD_CALIBRATION__ENERGY_CALIBRATION = eINSTANCE.getEdxdCalibration_EnergyCalibration();

		/**
		 * The meta object literal for the '<em><b>QCalibration</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EDXD_CALIBRATION__QCALIBRATION = eINSTANCE.getEdxdCalibration_QCalibration();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl <em>Calibration Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.CalibrationConfigImpl
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getCalibrationConfig()
		 * @generated
		 */
		EClass CALIBRATION_CONFIG = eINSTANCE.getCalibrationConfig();

		/**
		 * The meta object literal for the '<em><b>File Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CALIBRATION_CONFIG__FILE_NAME = eINSTANCE.getCalibrationConfig_FileName();

		/**
		 * The meta object literal for the '<em><b>Last Calibrated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CALIBRATION_CONFIG__LAST_CALIBRATED = eINSTANCE.getCalibrationConfig_LastCalibrated();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.DocumentRootImpl
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getDocumentRoot()
		 * @generated
		 */
		EClass DOCUMENT_ROOT = eINSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Edxd Calibration</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__EDXD_CALIBRATION = eINSTANCE.getDocumentRoot_EdxdCalibration();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH <em>HUTCH</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getHUTCH()
		 * @generated
		 */
		EEnum HUTCH = eINSTANCE.getHUTCH();

		/**
		 * The meta object literal for the '{@link uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR <em>COLLIMATOR</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR
		 * @see uk.ac.gda.edxd.calibration.edxdcalibration.impl.EdxdcalibrationPackageImpl#getCOLLIMATOR()
		 * @generated
		 */
		EEnum COLLIMATOR = eINSTANCE.getCOLLIMATOR();

	}

} //EdxdcalibrationPackage
