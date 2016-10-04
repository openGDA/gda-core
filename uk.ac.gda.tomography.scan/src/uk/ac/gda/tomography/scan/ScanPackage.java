/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.scan;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

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
 * @see uk.ac.gda.tomography.scan.ScanFactory
 * @model kind="package"
 * @generated
 */
public interface ScanPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "scan";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///uk/ac/gda/tomography/scan.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "uk.ac.gda.tomography.scan";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ScanPackage eINSTANCE = uk.ac.gda.tomography.scan.impl.ScanPackageImpl
			.init();

	/**
	 * The meta object id for the '{@link uk.ac.gda.tomography.scan.impl.ParametersImpl <em>Parameters</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see uk.ac.gda.tomography.scan.impl.ParametersImpl
	 * @see uk.ac.gda.tomography.scan.impl.ScanPackageImpl#getParameters()
	 * @generated
	 */
	int PARAMETERS = 0;

	/**
	 * The feature id for the '<em><b>In Beam Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__IN_BEAM_POSITION = 0;

	/**
	 * The feature id for the '<em><b>Out Of Beam Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__OUT_OF_BEAM_POSITION = 1;

	/**
	 * The feature id for the '<em><b>Exposure Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__EXPOSURE_TIME = 2;

	/**
	 * The feature id for the '<em><b>Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__START = 3;

	/**
	 * The feature id for the '<em><b>Stop</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__STOP = 4;

	/**
	 * The feature id for the '<em><b>Step</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__STEP = 5;

	/**
	 * The feature id for the '<em><b>Dark Field Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__DARK_FIELD_INTERVAL = 6;

	/**
	 * The feature id for the '<em><b>Flat Field Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__FLAT_FIELD_INTERVAL = 7;

	/**
	 * The feature id for the '<em><b>Images Per Dark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__IMAGES_PER_DARK = 8;

	/**
	 * The feature id for the '<em><b>Images Per Flat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__IMAGES_PER_FLAT = 9;

	/**
	 * The feature id for the '<em><b>Min I</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__MIN_I = 10;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__TITLE = 11;

	/**
	 * The feature id for the '<em><b>Fly Scan</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__FLY_SCAN = 12;

	/**
	 * The feature id for the '<em><b>Extra Flats at End</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__EXTRA_FLATS_AT_END = 13;

	/**
	 * The feature id for the '<em><b>Number of Fly Scans</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__NUM_FLY_SCANS = 14;

	/**
	 * The feature id for the '<em><b>Delay Between Scans</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__FLY_SCAN_DELAY = 15;

	/**
	 * The feature id for the '<em><b>Approximate centre of rotation</b></em>' attribute.<br>
	 * <!-- begin-user-doc --><br>
	 * <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETERS__APPROX_CENTRE_OF_ROTATION = 16;

	/**
	 * The number of structural features of the '<em>Parameters</em>' class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 * @ordered
	 */
	int PARAMETERS_FEATURE_COUNT = 17;

	/**
	 * Returns the meta object for class '{@link uk.ac.gda.tomography.scan.Parameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameters</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters
	 * @generated
	 */
	EClass getParameters();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getInBeamPosition <em>In Beam Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>In Beam Position</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getInBeamPosition()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_InBeamPosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getOutOfBeamPosition <em>Out Of Beam Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Out Of Beam Position</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getOutOfBeamPosition()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_OutOfBeamPosition();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getExposureTime <em>Exposure Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Exposure Time</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getExposureTime()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_ExposureTime();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getStart <em>Start</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getStart()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_Start();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getStop <em>Stop</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stop</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getStop()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_Stop();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getStep <em>Step</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Step</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getStep()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_Step();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getDarkFieldInterval <em>Dark Field Interval</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dark Field Interval</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getDarkFieldInterval()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_DarkFieldInterval();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getFlatFieldInterval <em>Flat Field Interval</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Flat Field Interval</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getFlatFieldInterval()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_FlatFieldInterval();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getImagesPerDark <em>Images Per Dark</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Images Per Dark</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getImagesPerDark()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_ImagesPerDark();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getImagesPerFlat <em>Images Per Flat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Images Per Flat</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getImagesPerFlat()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_ImagesPerFlat();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getMinI <em>Min I</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min I</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getMinI()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_MinI();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getTitle()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_Title();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#isFlyScan <em>Fly Scan</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fly Scan</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#isFlyScan()
	 * @see #getParameters()
	 * @generated
	 */
	EAttribute getParameters_FlyScan();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getExtraFlatsAtEnd <em>Extra Flats At End</em>}'.<br>
	 * <!-- begin-user-doc --><br>
	 * <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Extra Flats At End</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getExtraFlatsAtEnd()
	 * @see #getParameters()
	 */
	EAttribute getParameters_ExtraFlatsAtEnd();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getNumFlyScans <em>Number of Fly Scans</em>}'.<br>
	 * <!-- begin-user-doc --><br>
	 * <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Number of Fly Scans</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getNumFlyScans()
	 * @see #getParameters()
	 */
	EAttribute getParameters_NumFlyScans();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getFlyScanDelay <em>Delay Between Scans</em>}'.<br>
	 * <!-- begin-user-doc --><br>
	 * <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Delay Between Scans</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getFlyScanDelay()
	 * @see #getParameters()
	 */
	EAttribute getParameters_FlyScanDelay();

	/**
	 * Returns the meta object for the attribute '{@link uk.ac.gda.tomography.scan.Parameters#getApproxCentreOfRotation <em>Approximate centre of rotation</em>}
	 * '.
	 * <p>
	 * <!-- begin-user-doc --><br>
	 * <!-- end-user-doc -->
	 *
	 * @return the meta object for the attribute '<em>Approximate centre of rotation</em>'.
	 * @see uk.ac.gda.tomography.scan.Parameters#getApproxCentreOfRotation()
	 * @see #getParameters()
	 */
	EAttribute getParameters_ApproxCentreOfRotation();

	/**
	 * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ScanFactory getScanFactory();

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
		 * The meta object literal for the '{@link uk.ac.gda.tomography.scan.impl.ParametersImpl <em>Parameters</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see uk.ac.gda.tomography.scan.impl.ParametersImpl
		 * @see uk.ac.gda.tomography.scan.impl.ScanPackageImpl#getParameters()
		 * @generated
		 */
		EClass PARAMETERS = eINSTANCE.getParameters();

		/**
		 * The meta object literal for the '<em><b>In Beam Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__IN_BEAM_POSITION = eINSTANCE
				.getParameters_InBeamPosition();

		/**
		 * The meta object literal for the '<em><b>Out Of Beam Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__OUT_OF_BEAM_POSITION = eINSTANCE
				.getParameters_OutOfBeamPosition();

		/**
		 * The meta object literal for the '<em><b>Exposure Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__EXPOSURE_TIME = eINSTANCE
				.getParameters_ExposureTime();

		/**
		 * The meta object literal for the '<em><b>Start</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__START = eINSTANCE.getParameters_Start();

		/**
		 * The meta object literal for the '<em><b>Stop</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__STOP = eINSTANCE.getParameters_Stop();

		/**
		 * The meta object literal for the '<em><b>Step</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__STEP = eINSTANCE.getParameters_Step();

		/**
		 * The meta object literal for the '<em><b>Dark Field Interval</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__DARK_FIELD_INTERVAL = eINSTANCE
				.getParameters_DarkFieldInterval();

		/**
		 * The meta object literal for the '<em><b>Flat Field Interval</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__FLAT_FIELD_INTERVAL = eINSTANCE
				.getParameters_FlatFieldInterval();

		/**
		 * The meta object literal for the '<em><b>Images Per Dark</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__IMAGES_PER_DARK = eINSTANCE
				.getParameters_ImagesPerDark();

		/**
		 * The meta object literal for the '<em><b>Images Per Flat</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__IMAGES_PER_FLAT = eINSTANCE
				.getParameters_ImagesPerFlat();

		/**
		 * The meta object literal for the '<em><b>Min I</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__MIN_I = eINSTANCE.getParameters_MinI();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__TITLE = eINSTANCE.getParameters_Title();

		/**
		 * The meta object literal for the '<em><b>Fly Scan</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__FLY_SCAN = eINSTANCE.getParameters_FlyScan();

		/**
		 * The meta object literal for the '<em><b>Approx. centre of rotation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETERS__APPROX_CENTRE_OF_ROTATION = eINSTANCE.getParameters_ApproxCentreOfRotation();
	}

} //ScanPackage
