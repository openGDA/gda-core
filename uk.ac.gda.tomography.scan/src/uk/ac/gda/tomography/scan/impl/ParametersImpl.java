/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.scan.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Parameters</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getInBeamPosition <em>In Beam Position</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getOutOfBeamPosition <em>Out Of Beam Position</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getExposureTime <em>Exposure Time</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getStart <em>Start</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getStop <em>Stop</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getStep <em>Step</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getDarkFieldInterval <em>Dark Field Interval</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getFlatFieldInterval <em>Flat Field Interval</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getImagesPerDark <em>Images Per Dark</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getImagesPerFlat <em>Images Per Flat</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getMinI <em>Min I</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getTitle <em>Title</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#isFlyScan <em>Fly Scan</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getExtraFlatsAtEnd <em>Extra Flats at End</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getNumFlyScans <em>Number of Fly Scans</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getFlyScanDelay <em>Fly Scan Delay</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getApproxCentreOfRotation <em>Approximate centre of rotation</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getDetectorToSampleDistance <em>Detector to sample distance</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getDetectorToSampleDistanceUnits <em>Detector to sample distance units</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getXPixelSize <em>x pixel size</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getXPixelSizeUnits <em>x pixel size units</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getYPixelSize <em>y pixel size</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getYPixelSizeUnits <em>y pixel size units</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getRotationStage <em>Rotation stage</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getLinearStage <em>Linear stage</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getCloseShutterAfterLastScan <em>Close shutter after last scan</em>}</li>
 * <li>{@link uk.ac.gda.tomography.scan.impl.ParametersImpl#getSendDataToTemporaryDirectory <em>Send data to temporary directory</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ParametersImpl extends EObjectImpl implements Parameters {
	/**
	 * The default value of the '{@link #getInBeamPosition() <em>In Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected static final double IN_BEAM_POSITION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getInBeamPosition() <em>In Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected double inBeamPosition = IN_BEAM_POSITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutOfBeamPosition() <em>Out Of Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutOfBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected static final double OUT_OF_BEAM_POSITION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getOutOfBeamPosition() <em>Out Of Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutOfBeamPosition()
	 * @generated
	 * @ordered
	 */
	protected double outOfBeamPosition = OUT_OF_BEAM_POSITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getExposureTime() <em>Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExposureTime()
	 * @generated
	 * @ordered
	 */
	protected static final double EXPOSURE_TIME_EDEFAULT = 1.0;

	/**
	 * The cached value of the '{@link #getExposureTime() <em>Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExposureTime()
	 * @generated
	 * @ordered
	 */
	protected double exposureTime = EXPOSURE_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getStart() <em>Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStart()
	 * @generated
	 * @ordered
	 */
	protected static final double START_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getStart() <em>Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStart()
	 * @generated
	 * @ordered
	 */
	protected double start = START_EDEFAULT;

	/**
	 * The default value of the '{@link #getStop() <em>Stop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStop()
	 * @generated
	 * @ordered
	 */
	protected static final double STOP_EDEFAULT = 180.0;

	/**
	 * The cached value of the '{@link #getStop() <em>Stop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStop()
	 * @generated
	 * @ordered
	 */
	protected double stop = STOP_EDEFAULT;

	/**
	 * The default value of the '{@link #getStep() <em>Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected static final double STEP_EDEFAULT = 0.1;

	/**
	 * The cached value of the '{@link #getStep() <em>Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStep()
	 * @generated
	 * @ordered
	 */
	protected double step = STEP_EDEFAULT;

	/**
	 * The default value of the '{@link #getDarkFieldInterval() <em>Dark Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDarkFieldInterval()
	 * @generated
	 * @ordered
	 */
	protected static final int DARK_FIELD_INTERVAL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDarkFieldInterval() <em>Dark Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDarkFieldInterval()
	 * @generated
	 * @ordered
	 */
	protected int darkFieldInterval = DARK_FIELD_INTERVAL_EDEFAULT;

	/**
	 * The default value of the '{@link #getFlatFieldInterval() <em>Flat Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatFieldInterval()
	 * @generated
	 * @ordered
	 */
	protected static final int FLAT_FIELD_INTERVAL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFlatFieldInterval() <em>Flat Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlatFieldInterval()
	 * @generated
	 * @ordered
	 */
	protected int flatFieldInterval = FLAT_FIELD_INTERVAL_EDEFAULT;

	/**
	 * The default value of the '{@link #getImagesPerDark() <em>Images Per Dark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerDark()
	 * @generated
	 * @ordered
	 */
	protected static final int IMAGES_PER_DARK_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getImagesPerDark() <em>Images Per Dark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerDark()
	 * @generated
	 * @ordered
	 */
	protected int imagesPerDark = IMAGES_PER_DARK_EDEFAULT;

	/**
	 * The default value of the '{@link #getImagesPerFlat() <em>Images Per Flat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerFlat()
	 * @generated
	 * @ordered
	 */
	protected static final int IMAGES_PER_FLAT_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getImagesPerFlat() <em>Images Per Flat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagesPerFlat()
	 * @generated
	 * @ordered
	 */
	protected int imagesPerFlat = IMAGES_PER_FLAT_EDEFAULT;

	/**
	 * The default value of the '{@link #getMinI() <em>Min I</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinI()
	 * @generated
	 * @ordered
	 */
	protected static final double MIN_I_EDEFAULT = -1.0;

	/**
	 * The cached value of the '{@link #getMinI() <em>Min I</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinI()
	 * @generated
	 * @ordered
	 */
	protected double minI = MIN_I_EDEFAULT;

	/**
	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected static final String TITLE_EDEFAULT = "Unknown";

	/**
	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTitle()
	 * @generated
	 * @ordered
	 */
	protected String title = TITLE_EDEFAULT;

	/**
	 * The default value of the '{@link #isFlyScan() <em>Fly Scan</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFlyScan()
	 * @generated
	 * @ordered
	 */
	protected static final boolean FLY_SCAN_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isFlyScan() <em>Fly Scan</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFlyScan()
	 * @generated
	 * @ordered
	 */
	protected boolean flyScan = FLY_SCAN_EDEFAULT;

	/**
	 * The default value of the '{@link #getExtraFlatsAtEnd() <em>Extra Flats at End</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtraFlatsAtEnd()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXTRA_FLATS_AT_END_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #getExtraFlatsAtEnd() <em>Extra Flats at End</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtraFlatsAtEnd()
	 * @generated
	 * @ordered
	 */
	protected boolean extraFlatsAtEnd = EXTRA_FLATS_AT_END_EDEFAULT;

	/**
	 * The default value of the '{@link #getNumFlyScans() <em>Number of Fly Scans</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumFlyScans()
	 * @generated
	 * @ordered
	 */
	protected static final int NUM_FLY_SCANS_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getNumFlyScans() <em>Number of Fly Scans</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumFlyScans()
	 * @generated
	 * @ordered
	 */
	protected int numFlyScans = NUM_FLY_SCANS_EDEFAULT;

	/**
	 * The default value of the '{@link #getFlyScanDelay() <em>Fly Scan Delay</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlyScanDelay()
	 * @generated
	 * @ordered
	 */
	protected static final double FLY_SCAN_DELAY_EDEFAULT = 0.0;


	/**
	 * The cached value of the '{@link #getFlyScanDelay() <em>Fly Scan Delay</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFlyScanDelay()
	 * @generated
	 * @ordered
	 */
	protected double flyScanDelay = FLY_SCAN_DELAY_EDEFAULT;

	/**
	 * The default value of the '{@link #getApproxCentreOfRotation() <em>Approximate centre of rotation</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @see #getApproxCentreOfRotation()
	 * @generated
	 * @ordered
	 */
	protected static final String APPROX_CENTRE_OF_ROTATION_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getApproxCentreOfRotation() <em>Approximate centre of rotation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * The value is passed as a String, an empty string meaning that the (device dependent) system default should be taken.
	 * <!-- end-user-doc -->
	 *
	 * @see #getApproxCentreOfRotation()
	 * @generated
	 * @ordered
	 */
	protected String approxCentreOfRotation = APPROX_CENTRE_OF_ROTATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetectorToSampleDistance() <em>Detector to sample distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getDetectorToSampleDistance()
	 * @generated
	 * @ordered
	 */
	protected static final String DETECTOR_TO_SAMPLE_DISTANCE_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getApproxCentreOfRotation() <em>Approximate centre of rotation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * The value is passed as a String, an empty string meaning that the (device dependent) system default should be taken.
	 * <!-- end-user-doc -->
	 *
	 * @see #getApproxCentreOfRotation()
	 * @generated
	 * @ordered
	 */
	protected String detectorToSampleDistance = DETECTOR_TO_SAMPLE_DISTANCE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetectorToSampleDistance() <em>Detector to sample distance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getDetectorToSampleDistance()
	 * @generated
	 * @ordered
	 */
	protected static final String DETECTOR_TO_SAMPLE_DISTANCE_UNITS_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getApproxCentreOfRotation() <em>Approximate centre of rotation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getApproxCentreOfRotation()
	 * @generated
	 * @ordered
	 */
	protected String detectorToSampleDistanceUnits = DETECTOR_TO_SAMPLE_DISTANCE_UNITS_EDEFAULT;

	/**
	 * The default value of the '{@link #getXPixelSize() <em>x pixel size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getXPixelSize()
	 * @generated
	 * @ordered
	 */
	protected static final String X_PIXEL_SIZE_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getXPixelSize() <em>x pixel size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getXPixelSize()
	 * @generated
	 * @ordered
	 */
	protected String xPixelSize = X_PIXEL_SIZE_EDEFAULT;

	/**
	 * The default value of the '{@link #getXPixelSizeUnits() <em>x pixel size units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getXPixelSizeUnits()
	 * @generated
	 * @ordered
	 */
	protected static final String X_PIXEL_SIZE_UNITS_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getXPixelSizeUnits() <em>x pixel size units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getXPixelSizeUnits()
	 * @generated
	 * @ordered
	 */
	protected String xPixelSizeUnits = X_PIXEL_SIZE_UNITS_EDEFAULT;

	/**
	 * The default value of the '{@link #getYPixelSize() <em>x pixel size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getYPixelSize()
	 * @generated
	 * @ordered
	 */
	protected static final String Y_PIXEL_SIZE_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getYPixelSize() <em>x pixel size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getYPixelSize()
	 * @generated
	 * @ordered
	 */
	protected String yPixelSize = Y_PIXEL_SIZE_EDEFAULT;

	/**
	 * The default value of the '{@link #getYPixelSizeUnits() <em>x pixel size units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getYPixelSizeUnits()
	 * @generated
	 * @ordered
	 */
	protected static final String Y_PIXEL_SIZE_UNITS_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getYPixelSizeUnits() <em>x pixel size units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getYPixelSizeUnits()
	 * @generated
	 * @ordered
	 */
	protected String yPixelSizeUnits = Y_PIXEL_SIZE_UNITS_EDEFAULT;

	/**
	 * The default value of the '{@link #getRotationStage() <em>Rotation stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getRotationStage()
	 * @generated
	 * @ordered
	 */
	protected static final String ROTATION_STAGE_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getRotationStage() <em>Rotation stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getRotationStage()
	 * @generated
	 * @ordered
	 */
	protected String rotationStage = ROTATION_STAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getLinearStage() <em>Linear stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getLinearStage()
	 * @generated
	 * @ordered
	 */
	protected static final String LINEAR_STAGE_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getLinearStage() <em>Linear stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getLinearStage()
	 * @generated
	 * @ordered
	 */
	protected String linearStage = LINEAR_STAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCloseShutterAfterLastScan() <em>Close shutter after last scan</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getCloseShutterAfterLastScan()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CLOSE_SHUTTER_AFTER_LAST_SCAN_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #getCloseShutterAfterLastScan() <em>Close shutter after last scan</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getCloseShutterAfterLastScan()
	 * @generated
	 * @ordered
	 */
	protected boolean closeShutterAfterLastScan = CLOSE_SHUTTER_AFTER_LAST_SCAN_EDEFAULT;

	/**
	 * The default value of the '{@link #getSendDataToTemporaryDirectory() <em>Send data to temporary directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getSendDataToTemporaryDirectory()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SEND_DATA_TO_TEMPORARY_DIRECTORY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #getSendDataToTemporaryDirectory() <em>Send data to temporary directory</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 * @see #getSendDataToTemporaryDirectory()
	 * @generated
	 * @ordered
	 */
	protected boolean sendDataToTemporaryDirectory = SEND_DATA_TO_TEMPORARY_DIRECTORY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ParametersImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ScanPackage.Literals.PARAMETERS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getInBeamPosition() {
		return inBeamPosition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInBeamPosition(double newInBeamPosition) {
		double oldInBeamPosition = inBeamPosition;
		inBeamPosition = newInBeamPosition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__IN_BEAM_POSITION,
					oldInBeamPosition, inBeamPosition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getOutOfBeamPosition() {
		return outOfBeamPosition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOutOfBeamPosition(double newOutOfBeamPosition) {
		double oldOutOfBeamPosition = outOfBeamPosition;
		outOfBeamPosition = newOutOfBeamPosition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION,
					oldOutOfBeamPosition, outOfBeamPosition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getExposureTime() {
		return exposureTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExposureTime(double newExposureTime) {
		double oldExposureTime = exposureTime;
		exposureTime = newExposureTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__EXPOSURE_TIME, oldExposureTime,
					exposureTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getStart() {
		return start;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStart(double newStart) {
		double oldStart = start;
		start = newStart;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__START, oldStart, start));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getStop() {
		return stop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStop(double newStop) {
		double oldStop = stop;
		stop = newStop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__STOP, oldStop, stop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getStep() {
		return step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStep(double newStep) {
		double oldStep = step;
		step = newStep;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__STEP, oldStep, step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDarkFieldInterval() {
		return darkFieldInterval;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDarkFieldInterval(int newDarkFieldInterval) {
		int oldDarkFieldInterval = darkFieldInterval;
		darkFieldInterval = newDarkFieldInterval;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL,
					oldDarkFieldInterval, darkFieldInterval));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getFlatFieldInterval() {
		return flatFieldInterval;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFlatFieldInterval(int newFlatFieldInterval) {
		int oldFlatFieldInterval = flatFieldInterval;
		flatFieldInterval = newFlatFieldInterval;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL,
					oldFlatFieldInterval, flatFieldInterval));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getImagesPerDark() {
		return imagesPerDark;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImagesPerDark(int newImagesPerDark) {
		int oldImagesPerDark = imagesPerDark;
		imagesPerDark = newImagesPerDark;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__IMAGES_PER_DARK, oldImagesPerDark,
					imagesPerDark));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getImagesPerFlat() {
		return imagesPerFlat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImagesPerFlat(int newImagesPerFlat) {
		int oldImagesPerFlat = imagesPerFlat;
		imagesPerFlat = newImagesPerFlat;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__IMAGES_PER_FLAT, oldImagesPerFlat,
					imagesPerFlat));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getMinI() {
		return minI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMinI(double newMinI) {
		double oldMinI = minI;
		minI = newMinI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__MIN_I, oldMinI, minI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTitle(String newTitle) {
		String oldTitle = title;
		title = newTitle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__TITLE, oldTitle, title));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isFlyScan() {
		return flyScan;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFlyScan(boolean newFlyScan) {
		boolean oldFlyScan = flyScan;
		flyScan = newFlyScan;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__FLY_SCAN, oldFlyScan, flyScan));
	}

	@Override
	public boolean getExtraFlatsAtEnd() {
		return extraFlatsAtEnd;
	}

	@Override
	public void setExtraFlatsAtEnd(boolean value) {
		boolean oldExtraFlatsAtEnd = extraFlatsAtEnd;
		extraFlatsAtEnd = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END, oldExtraFlatsAtEnd, extraFlatsAtEnd));
		}
	}

	@Override
	public int getNumFlyScans() {
		return numFlyScans;
	}

	@Override
	public void setNumFlyScans(int value) {
		int oldNumFlyScans = numFlyScans;
		numFlyScans = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__NUM_FLY_SCANS, oldNumFlyScans, numFlyScans));
		}
	}

	@Override
	public double getFlyScanDelay() {
		return flyScanDelay;
	}

	@Override
	public void setFlyScanDelay(double value) {
		double oldFlyScanDelay = flyScanDelay;
		flyScanDelay = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET,
					ScanPackage.PARAMETERS__FLY_SCAN_DELAY, oldFlyScanDelay, flyScanDelay));
		}
	}

	@Override
	public String getApproxCentreOfRotation() {
		return approxCentreOfRotation;
	}

	@Override
	public void setApproxCentreOfRotation(String value) {
		String oldApproxCentreOfRotation = approxCentreOfRotation;
		approxCentreOfRotation = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION, oldApproxCentreOfRotation,
					approxCentreOfRotation));
		}
	}

	@Override
	public String getDetectorToSampleDistance() {
		return detectorToSampleDistance;
	}

	@Override
	public void setDetectorToSampleDistance(String value) {
		final String oldDetectorToSampleDistance = detectorToSampleDistance;
		detectorToSampleDistance = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE, oldDetectorToSampleDistance, value));
		}
	}

	@Override
	public String getDetectorToSampleDistanceUnits() {
		return detectorToSampleDistanceUnits;
	}

	@Override
	public void setDetectorToSampleDistanceUnits(String value) {
		final String oldDetectorToSampleDistanceUnits = detectorToSampleDistanceUnits;
		detectorToSampleDistanceUnits = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS,
					oldDetectorToSampleDistanceUnits, value));
		}
	}

	@Override
	public String getXPixelSize() {
		return xPixelSize;
	}

	@Override
	public void setXPixelSize(String value) {
		final String oldXPixelSize = xPixelSize;
		xPixelSize = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__X_PIXEL_SIZE,
					oldXPixelSize, value));
		}
	}

	@Override
	public String getXPixelSizeUnits() {
		return xPixelSizeUnits;
	}

	@Override
	public void setXPixelSizeUnits(String value) {
		final String oldXPixelSizeUnits = xPixelSizeUnits;
		xPixelSizeUnits = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS,
					oldXPixelSizeUnits, value));
		}
	}

	@Override
	public String getYPixelSize() {
		return yPixelSize;
	}

	@Override
	public void setYPixelSize(String value) {
		final String oldYPixelSize = yPixelSize;
		yPixelSize = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__Y_PIXEL_SIZE,
					oldYPixelSize, value));
		}
	}

	@Override
	public String getYPixelSizeUnits() {
		return yPixelSizeUnits;
	}

	@Override
	public void setYPixelSizeUnits(String value) {
		final String oldYPixelSizeUnits = yPixelSizeUnits;
		yPixelSizeUnits = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS,
					oldYPixelSizeUnits, value));
		}
	}

	@Override
	public String getRotationStage() {
		return rotationStage;
	}

	@Override
	public void setRotationStage(String value) {
		final String oldRotationStage = rotationStage;
		rotationStage = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__ROTATION_STAGE,
					oldRotationStage, value));
		}
	}

	@Override
	public String getLinearStage() {
		return linearStage;
	}

	@Override
	public void setLinearStage(String value) {
		final String oldLinearStage = linearStage;
		linearStage = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__LINEAR_STAGE,
					oldLinearStage, value));
		}
	}

	@Override
	public boolean getCloseShutterAfterLastScan() {
		return closeShutterAfterLastScan;
	}

	@Override
	public void setCloseShutterAfterLastScan(boolean value) {
		final boolean oldCloseShutterAfterLastScan = closeShutterAfterLastScan;
		closeShutterAfterLastScan = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN,
					oldCloseShutterAfterLastScan, value));
		}
	}

	@Override
	public boolean getSendDataToTemporaryDirectory() {
		return sendDataToTemporaryDirectory;
	}

	@Override
	public void setSendDataToTemporaryDirectory(boolean value) {
		final boolean oldSendDataToTemporaryDirectory = sendDataToTemporaryDirectory;
		sendDataToTemporaryDirectory = value;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY,
					oldSendDataToTemporaryDirectory, value));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case ScanPackage.PARAMETERS__IN_BEAM_POSITION:
			return getInBeamPosition();
		case ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION:
			return getOutOfBeamPosition();
		case ScanPackage.PARAMETERS__EXPOSURE_TIME:
			return getExposureTime();
		case ScanPackage.PARAMETERS__START:
			return getStart();
		case ScanPackage.PARAMETERS__STOP:
			return getStop();
		case ScanPackage.PARAMETERS__STEP:
			return getStep();
		case ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL:
			return getDarkFieldInterval();
		case ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL:
			return getFlatFieldInterval();
		case ScanPackage.PARAMETERS__IMAGES_PER_DARK:
			return getImagesPerDark();
		case ScanPackage.PARAMETERS__IMAGES_PER_FLAT:
			return getImagesPerFlat();
		case ScanPackage.PARAMETERS__MIN_I:
			return getMinI();
		case ScanPackage.PARAMETERS__TITLE:
			return getTitle();
		case ScanPackage.PARAMETERS__FLY_SCAN:
			return isFlyScan();
		case ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END:
			return getExtraFlatsAtEnd();
		case ScanPackage.PARAMETERS__NUM_FLY_SCANS:
			return getNumFlyScans();
		case ScanPackage.PARAMETERS__FLY_SCAN_DELAY:
			return getFlyScanDelay();
		case ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION:
			return getApproxCentreOfRotation();
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE:
			return getDetectorToSampleDistance();
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS:
			return getDetectorToSampleDistanceUnits();
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE:
			return getXPixelSize();
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS:
			return getXPixelSizeUnits();
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE:
			return getYPixelSize();
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS:
			return getYPixelSizeUnits();
		case ScanPackage.PARAMETERS__ROTATION_STAGE:
			return getRotationStage();
		case ScanPackage.PARAMETERS__LINEAR_STAGE:
			return getLinearStage();
		case ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN:
			return getCloseShutterAfterLastScan();
		case ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY:
			return getSendDataToTemporaryDirectory();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case ScanPackage.PARAMETERS__IN_BEAM_POSITION:
			setInBeamPosition((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION:
			setOutOfBeamPosition((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__EXPOSURE_TIME:
			setExposureTime((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__START:
			setStart((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__STOP:
			setStop((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__STEP:
			setStep((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL:
			setDarkFieldInterval((Integer) newValue);
			return;
		case ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL:
			setFlatFieldInterval((Integer) newValue);
			return;
		case ScanPackage.PARAMETERS__IMAGES_PER_DARK:
			setImagesPerDark((Integer) newValue);
			return;
		case ScanPackage.PARAMETERS__IMAGES_PER_FLAT:
			setImagesPerFlat((Integer) newValue);
			return;
		case ScanPackage.PARAMETERS__MIN_I:
			setMinI((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__TITLE:
			setTitle((String) newValue);
			return;
		case ScanPackage.PARAMETERS__FLY_SCAN:
			setFlyScan((Boolean) newValue);
			return;
		case ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END:
			setExtraFlatsAtEnd((Boolean) newValue);
			return;
		case ScanPackage.PARAMETERS__NUM_FLY_SCANS:
			setNumFlyScans((Integer) newValue);
			return;
		case ScanPackage.PARAMETERS__FLY_SCAN_DELAY:
			setFlyScanDelay((Double) newValue);
			return;
		case ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION:
			setApproxCentreOfRotation((String) newValue);
			return;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE:
			setDetectorToSampleDistance((String) newValue);
			return;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS:
			setDetectorToSampleDistanceUnits((String) newValue);
			return;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE:
			setXPixelSize((String) newValue);
			return;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS:
			setXPixelSizeUnits((String) newValue);
			return;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE:
			setYPixelSize((String) newValue);
			return;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS:
			setYPixelSizeUnits((String) newValue);
			return;
		case ScanPackage.PARAMETERS__ROTATION_STAGE:
			setRotationStage((String) newValue);
			return;
		case ScanPackage.PARAMETERS__LINEAR_STAGE:
			setLinearStage((String) newValue);
			return;
		case ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN:
			setCloseShutterAfterLastScan((boolean) newValue);
			return;
		case ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY:
			setSendDataToTemporaryDirectory((boolean) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case ScanPackage.PARAMETERS__IN_BEAM_POSITION:
			setInBeamPosition(IN_BEAM_POSITION_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION:
			setOutOfBeamPosition(OUT_OF_BEAM_POSITION_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__EXPOSURE_TIME:
			setExposureTime(EXPOSURE_TIME_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__START:
			setStart(START_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__STOP:
			setStop(STOP_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__STEP:
			setStep(STEP_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL:
			setDarkFieldInterval(DARK_FIELD_INTERVAL_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL:
			setFlatFieldInterval(FLAT_FIELD_INTERVAL_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__IMAGES_PER_DARK:
			setImagesPerDark(IMAGES_PER_DARK_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__IMAGES_PER_FLAT:
			setImagesPerFlat(IMAGES_PER_FLAT_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__MIN_I:
			setMinI(MIN_I_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__TITLE:
			setTitle(TITLE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__FLY_SCAN:
			setFlyScan(FLY_SCAN_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END:
			setExtraFlatsAtEnd(EXTRA_FLATS_AT_END_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__NUM_FLY_SCANS:
			setNumFlyScans(NUM_FLY_SCANS_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__FLY_SCAN_DELAY:
			setFlyScanDelay(FLY_SCAN_DELAY_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION:
			setApproxCentreOfRotation(APPROX_CENTRE_OF_ROTATION_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE:
			setDetectorToSampleDistance(DETECTOR_TO_SAMPLE_DISTANCE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS:
			setDetectorToSampleDistanceUnits(DETECTOR_TO_SAMPLE_DISTANCE_UNITS_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE:
			setXPixelSize(X_PIXEL_SIZE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS:
			setXPixelSizeUnits(X_PIXEL_SIZE_UNITS_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE:
			setYPixelSize(Y_PIXEL_SIZE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS:
			setYPixelSizeUnits(Y_PIXEL_SIZE_UNITS_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__ROTATION_STAGE:
			setRotationStage(ROTATION_STAGE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__LINEAR_STAGE:
			setLinearStage(LINEAR_STAGE_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN:
			setCloseShutterAfterLastScan(CLOSE_SHUTTER_AFTER_LAST_SCAN_EDEFAULT);
			return;
		case ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY:
			setSendDataToTemporaryDirectory(SEND_DATA_TO_TEMPORARY_DIRECTORY_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case ScanPackage.PARAMETERS__IN_BEAM_POSITION:
			return inBeamPosition != IN_BEAM_POSITION_EDEFAULT;
		case ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION:
			return outOfBeamPosition != OUT_OF_BEAM_POSITION_EDEFAULT;
		case ScanPackage.PARAMETERS__EXPOSURE_TIME:
			return exposureTime != EXPOSURE_TIME_EDEFAULT;
		case ScanPackage.PARAMETERS__START:
			return start != START_EDEFAULT;
		case ScanPackage.PARAMETERS__STOP:
			return stop != STOP_EDEFAULT;
		case ScanPackage.PARAMETERS__STEP:
			return step != STEP_EDEFAULT;
		case ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL:
			return darkFieldInterval != DARK_FIELD_INTERVAL_EDEFAULT;
		case ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL:
			return flatFieldInterval != FLAT_FIELD_INTERVAL_EDEFAULT;
		case ScanPackage.PARAMETERS__IMAGES_PER_DARK:
			return imagesPerDark != IMAGES_PER_DARK_EDEFAULT;
		case ScanPackage.PARAMETERS__IMAGES_PER_FLAT:
			return imagesPerFlat != IMAGES_PER_FLAT_EDEFAULT;
		case ScanPackage.PARAMETERS__MIN_I:
			return minI != MIN_I_EDEFAULT;
		case ScanPackage.PARAMETERS__TITLE:
			return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT
					.equals(title);
		case ScanPackage.PARAMETERS__FLY_SCAN:
			return flyScan != FLY_SCAN_EDEFAULT;
		case ScanPackage.PARAMETERS__EXTRA_FLATS_AT_END:
			return extraFlatsAtEnd != EXTRA_FLATS_AT_END_EDEFAULT;
		case ScanPackage.PARAMETERS__NUM_FLY_SCANS:
			return numFlyScans != NUM_FLY_SCANS_EDEFAULT;
		case ScanPackage.PARAMETERS__FLY_SCAN_DELAY:
			return flyScanDelay != FLY_SCAN_DELAY_EDEFAULT;
		case ScanPackage.PARAMETERS__APPROX_CENTRE_OF_ROTATION:
			return approxCentreOfRotation != APPROX_CENTRE_OF_ROTATION_EDEFAULT;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE:
			return detectorToSampleDistance != DETECTOR_TO_SAMPLE_DISTANCE_EDEFAULT;
		case ScanPackage.PARAMETERS__DETECTOR_TO_SAMPLE_DISTANCE_UNITS:
			return detectorToSampleDistanceUnits != DETECTOR_TO_SAMPLE_DISTANCE_UNITS_EDEFAULT;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE:
			return xPixelSize != X_PIXEL_SIZE_EDEFAULT;
		case ScanPackage.PARAMETERS__X_PIXEL_SIZE_UNITS:
			return xPixelSizeUnits != X_PIXEL_SIZE_UNITS_EDEFAULT;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE:
			return yPixelSize != Y_PIXEL_SIZE_EDEFAULT;
		case ScanPackage.PARAMETERS__Y_PIXEL_SIZE_UNITS:
			return yPixelSizeUnits != Y_PIXEL_SIZE_UNITS_EDEFAULT;
		case ScanPackage.PARAMETERS__ROTATION_STAGE:
			return rotationStage != ROTATION_STAGE_EDEFAULT;
		case ScanPackage.PARAMETERS__LINEAR_STAGE:
			return linearStage != LINEAR_STAGE_EDEFAULT;
		case ScanPackage.PARAMETERS__CLOSE_SHUTTER_AFTER_LAST_SCAN:
			return closeShutterAfterLastScan != CLOSE_SHUTTER_AFTER_LAST_SCAN_EDEFAULT;
		case ScanPackage.PARAMETERS__SEND_DATA_TO_TEMPORARY_DIRECTORY:
			return sendDataToTemporaryDirectory != SEND_DATA_TO_TEMPORARY_DIRECTORY_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (inBeamPosition: "); //$NON-NLS-1$
		result.append(inBeamPosition);
		result.append(", outOfBeamPosition: "); //$NON-NLS-1$
		result.append(outOfBeamPosition);
		result.append(", exposureTime: "); //$NON-NLS-1$
		result.append(exposureTime);
		result.append(", start: "); //$NON-NLS-1$
		result.append(start);
		result.append(", stop: "); //$NON-NLS-1$
		result.append(stop);
		result.append(", step: "); //$NON-NLS-1$
		result.append(step);
		result.append(", darkFieldInterval: "); //$NON-NLS-1$
		result.append(darkFieldInterval);
		result.append(", flatFieldInterval: "); //$NON-NLS-1$
		result.append(flatFieldInterval);
		result.append(", imagesPerDark: "); //$NON-NLS-1$
		result.append(imagesPerDark);
		result.append(", imagesPerFlat: "); //$NON-NLS-1$
		result.append(imagesPerFlat);
		result.append(", minI: "); //$NON-NLS-1$
		result.append(minI);
		result.append(", title: "); //$NON-NLS-1$
		result.append(title);
		result.append(", flyScan: "); //$NON-NLS-1$
		result.append(flyScan);
		result.append(", extraFlatsAtEnd: ");
		result.append(extraFlatsAtEnd);
		result.append(", numFlyScans: ");
		result.append(numFlyScans);
		result.append(", flyScanDelay: ");
		result.append(flyScanDelay);
		result.append(", approxCentreOfRotation: ");
		result.append(approxCentreOfRotation);
		result.append(", detectorToSampleDistance: ");
		result.append(detectorToSampleDistance);
		result.append(", detectorToSampleDistanceUnits: ");
		result.append(detectorToSampleDistanceUnits);
		result.append(", xPixelSize: ");
		result.append(xPixelSize);
		result.append(", xPixelSizeUnits: ");
		result.append(xPixelSizeUnits);
		result.append(", yPixelSize: ");
		result.append(yPixelSize);
		result.append(", yPixelSizeUnits: ");
		result.append(yPixelSizeUnits);
		result.append(", rotationStage: ");
		result.append(rotationStage);
		result.append(", linearStage: ");
		result.append(linearStage);
		result.append(", closeShutterAfterLastScan: ");
		result.append(closeShutterAfterLastScan);
		result.append(", sendDataToTemporaryDirectory: ");
		result.append(sendDataToTemporaryDirectory);
		result.append(')');

		return result.toString();
	}

} //ParametersImpl
