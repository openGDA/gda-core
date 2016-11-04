package uk.ac.gda.tomography.scan;

import org.eclipse.emf.ecore.EObject;

/**
 * @model
 *
 */
public interface Parameters extends EObject {

	/**
	 * @model required="true" unsettable="false" default="Unknown"
	 *
	 * @return the title of the scan
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

	/**
	 * @model required="true" unsettable="false" default="0."
	 *
	 * @return Position of X motor when sample is in the beam. In mm
	 */
	double getInBeamPosition();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getInBeamPosition <em>In Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>In Beam Position</em>' attribute.
	 * @see #getInBeamPosition()
	 * @generated
	 */
	void setInBeamPosition(double value);

	/**
	 * @model required="true" unsettable="false" default="0."
	 *
	 * @return Position of X motor when sample is out of the beam - used to take a flat image. In mm
	 */
	double getOutOfBeamPosition();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getOutOfBeamPosition <em>Out Of Beam Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Out Of Beam Position</em>' attribute.
	 * @see #getOutOfBeamPosition()
	 * @generated
	 */
	void setOutOfBeamPosition(double value);

	/**
	 * @model required="true" default="1.0" unsettable="false"
	 *
	 * @return Exposure Time in s
	 */
	double getExposureTime();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getExposureTime <em>Exposure Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Exposure Time</em>' attribute.
	 * @see #getExposureTime()
	 * @generated
	 */
	void setExposureTime(double value);

	/**
	 * @model required="true" default="0." unsettable="false"
	 *
	 * @return First rotation angle . In degrees
	 */
	double getStart();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getStart <em>Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start</em>' attribute.
	 * @see #getStart()
	 * @generated
	 */
	void setStart(double value);

	/**
	 * @model required="true" default="180." unsettable="false"
	 *
	 * @return Last rotation angle . In degrees
	 */
	double getStop();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getStop <em>Stop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stop</em>' attribute.
	 * @see #getStop()
	 * @generated
	 */
	void setStop(double value);

	/**
	 * @model required="true" default=".1" unsettable="false"
	 *
	 * @return Rotation step size (default = 0.1). In degrees
	 */
	double getStep();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getStep <em>Step</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Step</em>' attribute.
	 * @see #getStep()
	 * @generated
	 */
	void setStep(double value);

	/**
	 * @model required="true" default="0" unsettable="false"
	 *
	 * @return number of projections between each dark field. Note that a dark is always taken at the start and end of a tomogram (default=0)
	 */
	int getDarkFieldInterval();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getDarkFieldInterval <em>Dark Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dark Field Interval</em>' attribute.
	 * @see #getDarkFieldInterval()
	 * @generated
	 */
	void setDarkFieldInterval(int value);

	/**
	 * @model required="true" default="0" unsettable="false"
	 *
	 * @return number of projections between each flat field. Note that a dark is always taken at the start and end of a tomogram (default=0.)
	 */
	int getFlatFieldInterval();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getFlatFieldInterval <em>Flat Field Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Flat Field Interval</em>' attribute.
	 * @see #getFlatFieldInterval()
	 * @generated
	 */
	void setFlatFieldInterval(int value);

	/**
	 * @model required="true" default="1" unsettable="false"
	 *
	 * @return number of images to be taken for each dark default=1
	 */
	int getImagesPerDark();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getImagesPerDark <em>Images Per Dark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Images Per Dark</em>' attribute.
	 * @see #getImagesPerDark()
	 * @generated
	 */
	void setImagesPerDark(int value);

	/**
	 * @model required="true" default="1" unsettable="false"
	 *
	 * @return number of images to be taken for each flat
	 */
	int getImagesPerFlat();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getImagesPerFlat <em>Images Per Flat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Images Per Flat</em>' attribute.
	 * @see #getImagesPerFlat()
	 * @generated
	 */
	void setImagesPerFlat(int value);

	/**
	 * @model required="true" default="-1." unsettable="false"
	 *
	 * @return minimum value of ion chamber current required to take an image (default is -1) . A negative value means that the value is not checked )
	 */
	double getMinI();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getMinI <em>Min I</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min I</em>' attribute.
	 * @see #getMinI()
	 * @generated
	 */
	void setMinI(double value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 *
	 * @return True if the scan is to be a flyscan
	 */
	boolean isFlyScan();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#isFlyScan <em>Fly Scan</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fly Scan</em>' attribute.
	 * @see #isFlyScan()
	 * @generated
	 */
	void setFlyScan(boolean value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return True if we should collect additional flat images at the end of a tomography scan
	 */
	boolean getExtraFlatsAtEnd();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getExtraFlatsAtEnd <em>Extra Flats at End</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Extra Flats at End</em>' attribute.
	 */
	void setExtraFlatsAtEnd(boolean value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return number of fly scans to run (default is 1)
	 */
	int getNumFlyScans();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getNumFlyScans <em>Number of Fly Scans</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Number of Fly Scans</em>' attribute.
	 */
	void setNumFlyScans(int value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return delay (in seconds) between each fly scan
	 */
	double getFlyScanDelay();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getFlyScanDelay <em>Delay Between Scans</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Delay Between Scans</em>' attribute.
	 */
	void setFlyScanDelay(double value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return approximate centre of rotation (default is null)
	 */
	String getApproxCentreOfRotation();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getApproxCentreOfRotation <em>Approximate centre of rotation</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Approximate centre of rotation</em>' attribute.
	 */
	void setApproxCentreOfRotation(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return detector to sample distance (default is null)
	 */
	String getDetectorToSampleDistance();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getDetectorToSampleDistance <em>Detector to sample distance</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Detector to sample distance</em>' attribute.
	 */
	void setDetectorToSampleDistance(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return detector to sample distance units (default is null)
	 */
	String getDetectorToSampleDistanceUnits();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getDetectorToSampleDistanceUnits <em>Detector to sample distance</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Detector to sample distance</em>' attribute.
	 */
	void setDetectorToSampleDistanceUnits(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return x pixel size (default is null)
	 */
	String getXPixelSize();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getXPixelSize <em>x pixel size</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>x pixel size</em>' attribute.
	 */
	void setXPixelSize(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return x pixel size units (default is null)
	 */
	String getXPixelSizeUnits();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getXPixelSizeUnits <em>x pixel size units</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>x pixel size units</em>' attribute.
	 */
	void setXPixelSizeUnits(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return y pixel size (default is null)
	 */
	String getYPixelSize();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getYPixelSize <em>y pixel size</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>y pixel size</em>' attribute.
	 */
	void setYPixelSize(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return y pixel size (default is null)
	 */
	String getYPixelSizeUnits();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getYPixelSizeUnits <em>y pixel size units</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>y pixel size units</em>' attribute.
	 */
	void setYPixelSizeUnits(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return rotation stage (default is null)
	 */
	String getRotationStage();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getRotationStage <em>Rotation stage</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Rotation stage</em>' attribute.
	 */
	void setRotationStage(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return linear stage (default is null)
	 */
	String getLinearStage();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getLinearStage <em>Linear stage</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Linear stage</em>' attribute.
	 */
	void setLinearStage(String value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return close shutter after last scan (default is false)
	 */
	boolean getCloseShutterAfterLastScan();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getCloseShutterAfterLastScan <em>Close shutter after last scan</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Close shutter after last scan</em>' attribute.
	 */
	void setCloseShutterAfterLastScan(boolean value);

	/**
	 * @model required="true" default="false" unsettable="false"
	 * @return send data to temporary directory (default is false)
	 */
	boolean getSendDataToTemporaryDirectory();

	/**
	 * Sets the value of the '{@link uk.ac.gda.tomography.scan.Parameters#getSendDataToTemporaryDirectory <em>Send data to temporary directory</em>}' attribute.
	 *
	 * @param value
	 *            the new value of the '<em>Send data to temporary directory</em>' attribute.
	 */
	void setSendDataToTemporaryDirectory(boolean value);

}
