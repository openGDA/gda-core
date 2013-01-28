/**
 */
package org.opengda.detector.electronalyser.server.model.regiondefinition;

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
 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionFactory
 * @model kind="package"
 * @generated
 */
public interface RegiondefinitionPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "regiondefinition";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://diamond.ac.uk/regiondefinition";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RegiondefinitionPackage eINSTANCE = org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.DocumentRootImpl
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDocumentRoot()
	 * @generated
	 */
	int DOCUMENT_ROOT = 0;

	/**
	 * The feature id for the '<em><b>Sequence</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT__SEQUENCE = 0;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_ROOT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl <em>Region</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRegion()
	 * @generated
	 */
	int REGION = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__NAME = 0;

	/**
	 * The feature id for the '<em><b>Lensmode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__LENSMODE = 1;

	/**
	 * The feature id for the '<em><b>Pass Energy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__PASS_ENERGY = 2;

	/**
	 * The feature id for the '<em><b>Run Mode</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION__RUN_MODE = 3;

	/**
	 * The number of structural features of the '<em>Region</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGION_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl <em>Run Mode</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRunMode()
	 * @generated
	 */
	int RUN_MODE = 2;

	/**
	 * The feature id for the '<em><b>Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__MODE = 0;

	/**
	 * The feature id for the '<em><b>Num Iterations</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__NUM_ITERATIONS = 1;

	/**
	 * The feature id for the '<em><b>Repeat Unitil Stopped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE__REPEAT_UNITIL_STOPPED = 2;

	/**
	 * The number of structural features of the '<em>Run Mode</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RUN_MODE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.SequenceImpl <em>Sequence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.SequenceImpl
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSequence()
	 * @generated
	 */
	int SEQUENCE = 3;

	/**
	 * The feature id for the '<em><b>Region</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE__REGION = 0;

	/**
	 * The number of structural features of the '<em>Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEQUENCE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE <em>LENS MODE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getLENS_MODE()
	 * @generated
	 */
	int LENS_MODE = 4;

	/**
	 * The meta object id for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES <em>RUN MODES</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRUN_MODES()
	 * @generated
	 */
	int RUN_MODES = 5;


	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot#getSequence <em>Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Sequence</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.DocumentRoot#getSequence()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_Sequence();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Region <em>Region</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Region</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Region
	 * @generated
	 */
	EClass getRegion();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getName()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getLensmode <em>Lensmode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lensmode</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getLensmode()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_Lensmode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getPassEnergy <em>Pass Energy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pass Energy</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getPassEnergy()
	 * @see #getRegion()
	 * @generated
	 */
	EAttribute getRegion_PassEnergy();

	/**
	 * Returns the meta object for the containment reference '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getRunMode <em>Run Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Run Mode</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Region#getRunMode()
	 * @see #getRegion()
	 * @generated
	 */
	EReference getRegion_RunMode();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode <em>Run Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Run Mode</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode
	 * @generated
	 */
	EClass getRunMode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#getMode <em>Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mode</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#getMode()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_Mode();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isNumIterations <em>Num Iterations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Num Iterations</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isNumIterations()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_NumIterations();

	/**
	 * Returns the meta object for the attribute '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isRepeatUnitilStopped <em>Repeat Unitil Stopped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Repeat Unitil Stopped</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode#isRepeatUnitilStopped()
	 * @see #getRunMode()
	 * @generated
	 */
	EAttribute getRunMode_RepeatUnitilStopped();

	/**
	 * Returns the meta object for class '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Sequence <em>Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sequence</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Sequence
	 * @generated
	 */
	EClass getSequence();

	/**
	 * Returns the meta object for the containment reference list '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.Sequence#getRegion <em>Region</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Region</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.Sequence#getRegion()
	 * @see #getSequence()
	 * @generated
	 */
	EReference getSequence_Region();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE <em>LENS MODE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>LENS MODE</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE
	 * @generated
	 */
	EEnum getLENS_MODE();

	/**
	 * Returns the meta object for enum '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES <em>RUN MODES</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>RUN MODES</em>'.
	 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES
	 * @generated
	 */
	EEnum getRUN_MODES();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RegiondefinitionFactory getRegiondefinitionFactory();

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
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.DocumentRootImpl
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getDocumentRoot()
		 * @generated
		 */
		EClass DOCUMENT_ROOT = eINSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Sequence</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__SEQUENCE = eINSTANCE.getDocumentRoot_Sequence();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl <em>Region</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegionImpl
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRegion()
		 * @generated
		 */
		EClass REGION = eINSTANCE.getRegion();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__NAME = eINSTANCE.getRegion_Name();

		/**
		 * The meta object literal for the '<em><b>Lensmode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__LENSMODE = eINSTANCE.getRegion_Lensmode();

		/**
		 * The meta object literal for the '<em><b>Pass Energy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGION__PASS_ENERGY = eINSTANCE.getRegion_PassEnergy();

		/**
		 * The meta object literal for the '<em><b>Run Mode</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGION__RUN_MODE = eINSTANCE.getRegion_RunMode();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl <em>Run Mode</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RunModeImpl
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRunMode()
		 * @generated
		 */
		EClass RUN_MODE = eINSTANCE.getRunMode();

		/**
		 * The meta object literal for the '<em><b>Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__MODE = eINSTANCE.getRunMode_Mode();

		/**
		 * The meta object literal for the '<em><b>Num Iterations</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__NUM_ITERATIONS = eINSTANCE.getRunMode_NumIterations();

		/**
		 * The meta object literal for the '<em><b>Repeat Unitil Stopped</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RUN_MODE__REPEAT_UNITIL_STOPPED = eINSTANCE.getRunMode_RepeatUnitilStopped();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.impl.SequenceImpl <em>Sequence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.SequenceImpl
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getSequence()
		 * @generated
		 */
		EClass SEQUENCE = eINSTANCE.getSequence();

		/**
		 * The meta object literal for the '<em><b>Region</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SEQUENCE__REGION = eINSTANCE.getSequence_Region();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE <em>LENS MODE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getLENS_MODE()
		 * @generated
		 */
		EEnum LENS_MODE = eINSTANCE.getLENS_MODE();

		/**
		 * The meta object literal for the '{@link org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES <em>RUN MODES</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.RUN_MODES
		 * @see org.opengda.detector.electronalyser.server.model.regiondefinition.impl.RegiondefinitionPackageImpl#getRUN_MODES()
		 * @generated
		 */
		EEnum RUN_MODES = eINSTANCE.getRUN_MODES();

	}

} //RegiondefinitionPackage
