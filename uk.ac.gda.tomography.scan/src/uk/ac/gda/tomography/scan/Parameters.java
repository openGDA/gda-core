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
}
