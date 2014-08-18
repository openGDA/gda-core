/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_start <em>Sample xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_stop <em>Sample xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_step <em>Sample xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_start <em>Sample ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_stop <em>Sample ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_step <em>Sample ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDetector_x <em>Detector x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDetector_y <em>Detector y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDetector_z <em>Detector z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getEndDate <em>End Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getMailCount <em>Mail Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDataFileCount <em>Data File Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDataFilePath <em>Data File Path</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleImpl extends MinimalEObjectImpl.Container implements Sample {
	/**
	 * The default value of the '{@link #getSampleID() <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleID()
	 * @generated
	 * @ordered
	 */
	protected static final String SAMPLE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSampleID() <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleID()
	 * @generated
	 * @ordered
	 */
	protected String sampleID = SAMPLE_ID_EDEFAULT;

	/**
	 * This is true if the Sample ID attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean sampleIDESet;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final STATUS STATUS_EDEFAULT = STATUS.READY;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected STATUS status = STATUS_EDEFAULT;

	/**
	 * This is true if the Status attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean statusESet;

	/**
	 * The default value of the '{@link #isActive() <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isActive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ACTIVE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isActive() <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isActive()
	 * @generated
	 * @ordered
	 */
	protected boolean active = ACTIVE_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = "new_sample";

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getCellID() <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCellID()
	 * @generated
	 * @ordered
	 */
	protected static final String CELL_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCellID() <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCellID()
	 * @generated
	 * @ordered
	 */
	protected String cellID = CELL_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated
	 * @ordered
	 */
	protected static final String VISIT_ID_EDEFAULT = "0-0";

	/**
	 * The cached value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated
	 * @ordered
	 */
	protected String visitID = VISIT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getCalibrant() <em>Calibrant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant()
	 * @generated
	 * @ordered
	 */
	protected static final String CALIBRANT_EDEFAULT = "Si";

	/**
	 * The cached value of the '{@link #getCalibrant() <em>Calibrant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant()
	 * @generated
	 * @ordered
	 */
	protected String calibrant = CALIBRANT_EDEFAULT;

	/**
	 * The default value of the '{@link #getCalibrant_x() <em>Calibrant x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_x()
	 * @generated
	 * @ordered
	 */
	protected static final double CALIBRANT_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCalibrant_x() <em>Calibrant x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_x()
	 * @generated
	 * @ordered
	 */
	protected double calibrant_x = CALIBRANT_X_EDEFAULT;

	/**
	 * The default value of the '{@link #getCalibrant_y() <em>Calibrant y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_y()
	 * @generated
	 * @ordered
	 */
	protected static final double CALIBRANT_Y_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCalibrant_y() <em>Calibrant y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_y()
	 * @generated
	 * @ordered
	 */
	protected double calibrant_y = CALIBRANT_Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getCalibrant_exposure() <em>Calibrant exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_exposure()
	 * @generated
	 * @ordered
	 */
	protected static final double CALIBRANT_EXPOSURE_EDEFAULT = 1.0;

	/**
	 * The cached value of the '{@link #getCalibrant_exposure() <em>Calibrant exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant_exposure()
	 * @generated
	 * @ordered
	 */
	protected double calibrant_exposure = CALIBRANT_EXPOSURE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_x_start() <em>Sample xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_start()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTART_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_x_start() <em>Sample xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_start()
	 * @generated
	 * @ordered
	 */
	protected Double sample_x_start = SAMPLE_XSTART_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_x_stop() <em>Sample xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_stop()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTOP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_x_stop() <em>Sample xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_stop()
	 * @generated
	 * @ordered
	 */
	protected Double sample_x_stop = SAMPLE_XSTOP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_x_step() <em>Sample xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_step()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTEP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_x_step() <em>Sample xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_step()
	 * @generated
	 * @ordered
	 */
	protected Double sample_x_step = SAMPLE_XSTEP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_y_start() <em>Sample ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_start()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTART_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_y_start() <em>Sample ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_start()
	 * @generated
	 * @ordered
	 */
	protected Double sample_y_start = SAMPLE_YSTART_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_y_stop() <em>Sample ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_stop()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTOP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_y_stop() <em>Sample ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_stop()
	 * @generated
	 * @ordered
	 */
	protected Double sample_y_stop = SAMPLE_YSTOP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_y_step() <em>Sample ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_step()
	 * @generated
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTEP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSample_y_step() <em>Sample ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_y_step()
	 * @generated
	 * @ordered
	 */
	protected Double sample_y_step = SAMPLE_YSTEP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSample_exposure() <em>Sample exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_exposure()
	 * @generated
	 * @ordered
	 */
	protected static final double SAMPLE_EXPOSURE_EDEFAULT = 5.0;

	/**
	 * The cached value of the '{@link #getSample_exposure() <em>Sample exposure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_exposure()
	 * @generated
	 * @ordered
	 */
	protected double sample_exposure = SAMPLE_EXPOSURE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetector_x() <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_x()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDetector_x() <em>Detector x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_x()
	 * @generated
	 * @ordered
	 */
	protected double detector_x = DETECTOR_X_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetector_y() <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_y()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_Y_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getDetector_y() <em>Detector y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_y()
	 * @generated
	 * @ordered
	 */
	protected double detector_y = DETECTOR_Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getDetector_z() <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_z()
	 * @generated
	 * @ordered
	 */
	protected static final double DETECTOR_Z_EDEFAULT = 100.0;

	/**
	 * The cached value of the '{@link #getDetector_z() <em>Detector z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetector_z()
	 * @generated
	 * @ordered
	 */
	protected double detector_z = DETECTOR_Z_EDEFAULT;

	/**
	 * The default value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected static final String EMAIL_EDEFAULT = "chiu.tang@diamond.ac.uk";

	/**
	 * The cached value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected String email = EMAIL_EDEFAULT;

	/**
	 * The default value of the '{@link #getStartDate() <em>Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartDate()
	 * @generated NOT
	 * @ordered
	 */
	protected static final Date START_DATE_EDEFAULT = Calendar.getInstance().getTime();


	/**
	 * The cached value of the '{@link #getStartDate() <em>Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartDate()
	 * @generated
	 * @ordered
	 */
	protected Date startDate = START_DATE_EDEFAULT;

	/**
	 * The default value of the '{@link #getEndDate() <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndDate()
	 * @generated NOT
	 * @ordered
	 */
	protected static final Date END_DATE_EDEFAULT = threeMonths();

	/**
	 * The default value for the '{@link #getEndDate() <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndDate()
	 * @generated NOT
	 * @ordered
	 */
	private static Date threeMonths() {
		 Calendar calendar=Calendar.getInstance();
		 calendar.add(Calendar.DAY_OF_YEAR, 91);
		 return (calendar.getTime());
	}

	/**
	 * The cached value of the '{@link #getEndDate() <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndDate()
	 * @generated
	 * @ordered
	 */
	protected Date endDate = END_DATE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCommand() <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_EDEFAULT = "parkingAll";

	/**
	 * The cached value of the '{@link #getCommand() <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected String command = COMMAND_EDEFAULT;

	/**
	 * The default value of the '{@link #getMailCount() <em>Mail Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMailCount()
	 * @generated
	 * @ordered
	 */
	protected static final int MAIL_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMailCount() <em>Mail Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMailCount()
	 * @generated
	 * @ordered
	 */
	protected int mailCount = MAIL_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getDataFileCount() <em>Data File Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFileCount()
	 * @generated
	 * @ordered
	 */
	protected static final int DATA_FILE_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDataFileCount() <em>Data File Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFileCount()
	 * @generated
	 * @ordered
	 */
	protected int dataFileCount = DATA_FILE_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getComment() <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMENT_EDEFAULT = "Please add your comment here";

	/**
	 * The cached value of the '{@link #getComment() <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected String comment = COMMENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getDataFilePath() <em>Data File Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFilePath()
	 * @generated
	 * @ordered
	 */
	protected static final String DATA_FILE_PATH_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getDataFilePath() <em>Data File Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFilePath()
	 * @generated
	 * @ordered
	 */
	protected String dataFilePath = DATA_FILE_PATH_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected SampleImpl() {
		super();
		setSampleID(EcoreUtil.generateUUID());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LDEExperimentsPackage.Literals.SAMPLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSampleID() {
		return sampleID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSampleID(String newSampleID) {
		String oldSampleID = sampleID;
		sampleID = newSampleID;
		boolean oldSampleIDESet = sampleIDESet;
		sampleIDESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_ID, oldSampleID, sampleID, !oldSampleIDESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetSampleID() {
		String oldSampleID = sampleID;
		boolean oldSampleIDESet = sampleIDESet;
		sampleID = SAMPLE_ID_EDEFAULT;
		sampleIDESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LDEExperimentsPackage.SAMPLE__SAMPLE_ID, oldSampleID, SAMPLE_ID_EDEFAULT, oldSampleIDESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSampleID() {
		return sampleIDESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStatus(STATUS newStatus) {
		STATUS oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		boolean oldStatusESet = statusESet;
		statusESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__STATUS, oldStatus, status, !oldStatusESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetStatus() {
		STATUS oldStatus = status;
		boolean oldStatusESet = statusESet;
		status = STATUS_EDEFAULT;
		statusESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LDEExperimentsPackage.SAMPLE__STATUS, oldStatus, STATUS_EDEFAULT, oldStatusESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetStatus() {
		return statusESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActive(boolean newActive) {
		boolean oldActive = active;
		active = newActive;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__ACTIVE, oldActive, active));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCellID() {
		return cellID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCellID(String newCellID) {
		String oldCellID = cellID;
		cellID = newCellID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CELL_ID, oldCellID, cellID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getVisitID() {
		return visitID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisitID(String newVisitID) {
		String oldVisitID = visitID;
		visitID = newVisitID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__VISIT_ID, oldVisitID, visitID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCalibrant() {
		return calibrant;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCalibrant(String newCalibrant) {
		String oldCalibrant = calibrant;
		calibrant = newCalibrant;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CALIBRANT, oldCalibrant, calibrant));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCalibrant_x() {
		return calibrant_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCalibrant_x(double newCalibrant_x) {
		double oldCalibrant_x = calibrant_x;
		calibrant_x = newCalibrant_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CALIBRANT_X, oldCalibrant_x, calibrant_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCalibrant_y() {
		return calibrant_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCalibrant_y(double newCalibrant_y) {
		double oldCalibrant_y = calibrant_y;
		calibrant_y = newCalibrant_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CALIBRANT_Y, oldCalibrant_y, calibrant_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getCalibrant_exposure() {
		return calibrant_exposure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCalibrant_exposure(double newCalibrant_exposure) {
		double oldCalibrant_exposure = calibrant_exposure;
		calibrant_exposure = newCalibrant_exposure;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE, oldCalibrant_exposure, calibrant_exposure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_x_start() {
		return sample_x_start;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_x_start(Double newSample_x_start) {
		Double oldSample_x_start = sample_x_start;
		sample_x_start = newSample_x_start;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART, oldSample_x_start, sample_x_start));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_x_stop() {
		return sample_x_stop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_x_stop(Double newSample_x_stop) {
		Double oldSample_x_stop = sample_x_stop;
		sample_x_stop = newSample_x_stop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP, oldSample_x_stop, sample_x_stop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_x_step() {
		return sample_x_step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_x_step(Double newSample_x_step) {
		Double oldSample_x_step = sample_x_step;
		sample_x_step = newSample_x_step;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP, oldSample_x_step, sample_x_step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_y_start() {
		return sample_y_start;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_y_start(Double newSample_y_start) {
		Double oldSample_y_start = sample_y_start;
		sample_y_start = newSample_y_start;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART, oldSample_y_start, sample_y_start));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_y_stop() {
		return sample_y_stop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_y_stop(Double newSample_y_stop) {
		Double oldSample_y_stop = sample_y_stop;
		sample_y_stop = newSample_y_stop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP, oldSample_y_stop, sample_y_stop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSample_y_step() {
		return sample_y_step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_y_step(Double newSample_y_step) {
		Double oldSample_y_step = sample_y_step;
		sample_y_step = newSample_y_step;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP, oldSample_y_step, sample_y_step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getSample_exposure() {
		return sample_exposure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSample_exposure(double newSample_exposure) {
		double oldSample_exposure = sample_exposure;
		sample_exposure = newSample_exposure;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE, oldSample_exposure, sample_exposure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_x() {
		return detector_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_x(double newDetector_x) {
		double oldDetector_x = detector_x;
		detector_x = newDetector_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__DETECTOR_X, oldDetector_x, detector_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_y() {
		return detector_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_y(double newDetector_y) {
		double oldDetector_y = detector_y;
		detector_y = newDetector_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__DETECTOR_Y, oldDetector_y, detector_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getDetector_z() {
		return detector_z;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDetector_z(double newDetector_z) {
		double oldDetector_z = detector_z;
		detector_z = newDetector_z;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__DETECTOR_Z, oldDetector_z, detector_z));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEmail(String newEmail) {
		String oldEmail = email;
		email = newEmail;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__EMAIL, oldEmail, email));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStartDate(Date newStartDate) {
		Date oldStartDate = startDate;
		startDate = newStartDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__START_DATE, oldStartDate, startDate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEndDate(Date newEndDate) {
		Date oldEndDate = endDate;
		endDate = newEndDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__END_DATE, oldEndDate, endDate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommand(String newCommand) {
		String oldCommand = command;
		command = newCommand;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getMailCount() {
		return mailCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMailCount(int newMailCount) {
		int oldMailCount = mailCount;
		mailCount = newMailCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__MAIL_COUNT, oldMailCount, mailCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDataFileCount() {
		return dataFileCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDataFileCount(int newDataFileCount) {
		int oldDataFileCount = dataFileCount;
		dataFileCount = newDataFileCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT, oldDataFileCount, dataFileCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComment(String newComment) {
		String oldComment = comment;
		comment = newComment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__COMMENT, oldComment, comment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDataFilePath() {
		return dataFilePath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDataFilePath(String newDataFilePath) {
		String oldDataFilePath = dataFilePath;
		dataFilePath = newDataFilePath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH, oldDataFilePath, dataFilePath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return getSampleID();
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return getStatus();
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				return isActive();
			case LDEExperimentsPackage.SAMPLE__NAME:
				return getName();
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				return getCellID();
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				return getVisitID();
			case LDEExperimentsPackage.SAMPLE__CALIBRANT:
				return getCalibrant();
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_X:
				return getCalibrant_x();
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_Y:
				return getCalibrant_y();
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE:
				return getCalibrant_exposure();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
				return getSample_x_start();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
				return getSample_x_stop();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
				return getSample_x_step();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
				return getSample_y_start();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
				return getSample_y_stop();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
				return getSample_y_step();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
				return getSample_exposure();
			case LDEExperimentsPackage.SAMPLE__DETECTOR_X:
				return getDetector_x();
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Y:
				return getDetector_y();
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Z:
				return getDetector_z();
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				return getEmail();
			case LDEExperimentsPackage.SAMPLE__START_DATE:
				return getStartDate();
			case LDEExperimentsPackage.SAMPLE__END_DATE:
				return getEndDate();
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return getCommand();
			case LDEExperimentsPackage.SAMPLE__MAIL_COUNT:
				return getMailCount();
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT:
				return getDataFileCount();
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				return getComment();
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH:
				return getDataFilePath();
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
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				setSampleID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__STATUS:
				setStatus((STATUS)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				setActive((Boolean)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__NAME:
				setName((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				setCellID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				setVisitID((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT:
				setCalibrant((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_X:
				setCalibrant_x((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_Y:
				setCalibrant_y((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE:
				setCalibrant_exposure((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
				setSample_x_start((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
				setSample_x_stop((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
				setSample_x_step((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
				setSample_y_start((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
				setSample_y_stop((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
				setSample_y_step((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
				setSample_exposure((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_X:
				setDetector_x((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Y:
				setDetector_y((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Z:
				setDetector_z((Double)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				setEmail((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__START_DATE:
				setStartDate((Date)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__END_DATE:
				setEndDate((Date)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__MAIL_COUNT:
				setMailCount((Integer)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT:
				setDataFileCount((Integer)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				setComment((String)newValue);
				return;
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH:
				setDataFilePath((String)newValue);
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
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				unsetSampleID();
				return;
			case LDEExperimentsPackage.SAMPLE__STATUS:
				unsetStatus();
				return;
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				setActive(ACTIVE_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				setCellID(CELL_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				setVisitID(VISIT_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT:
				setCalibrant(CALIBRANT_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_X:
				setCalibrant_x(CALIBRANT_X_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_Y:
				setCalibrant_y(CALIBRANT_Y_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE:
				setCalibrant_exposure(CALIBRANT_EXPOSURE_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
				setSample_x_start(SAMPLE_XSTART_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
				setSample_x_stop(SAMPLE_XSTOP_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
				setSample_x_step(SAMPLE_XSTEP_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
				setSample_y_start(SAMPLE_YSTART_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
				setSample_y_stop(SAMPLE_YSTOP_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
				setSample_y_step(SAMPLE_YSTEP_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
				setSample_exposure(SAMPLE_EXPOSURE_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_X:
				setDetector_x(DETECTOR_X_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Y:
				setDetector_y(DETECTOR_Y_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Z:
				setDetector_z(DETECTOR_Z_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				setEmail(EMAIL_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__START_DATE:
				setStartDate(START_DATE_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__END_DATE:
				setEndDate(END_DATE_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand(COMMAND_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__MAIL_COUNT:
				setMailCount(MAIL_COUNT_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT:
				setDataFileCount(DATA_FILE_COUNT_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				setComment(COMMENT_EDEFAULT);
				return;
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH:
				setDataFilePath(DATA_FILE_PATH_EDEFAULT);
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
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return isSetSampleID();
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return isSetStatus();
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				return active != ACTIVE_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
				return CELL_ID_EDEFAULT == null ? cellID != null : !CELL_ID_EDEFAULT.equals(cellID);
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
				return VISIT_ID_EDEFAULT == null ? visitID != null : !VISIT_ID_EDEFAULT.equals(visitID);
			case LDEExperimentsPackage.SAMPLE__CALIBRANT:
				return CALIBRANT_EDEFAULT == null ? calibrant != null : !CALIBRANT_EDEFAULT.equals(calibrant);
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_X:
				return calibrant_x != CALIBRANT_X_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_Y:
				return calibrant_y != CALIBRANT_Y_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE:
				return calibrant_exposure != CALIBRANT_EXPOSURE_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
				return SAMPLE_XSTART_EDEFAULT == null ? sample_x_start != null : !SAMPLE_XSTART_EDEFAULT.equals(sample_x_start);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
				return SAMPLE_XSTOP_EDEFAULT == null ? sample_x_stop != null : !SAMPLE_XSTOP_EDEFAULT.equals(sample_x_stop);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
				return SAMPLE_XSTEP_EDEFAULT == null ? sample_x_step != null : !SAMPLE_XSTEP_EDEFAULT.equals(sample_x_step);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
				return SAMPLE_YSTART_EDEFAULT == null ? sample_y_start != null : !SAMPLE_YSTART_EDEFAULT.equals(sample_y_start);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
				return SAMPLE_YSTOP_EDEFAULT == null ? sample_y_stop != null : !SAMPLE_YSTOP_EDEFAULT.equals(sample_y_stop);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
				return SAMPLE_YSTEP_EDEFAULT == null ? sample_y_step != null : !SAMPLE_YSTEP_EDEFAULT.equals(sample_y_step);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
				return sample_exposure != SAMPLE_EXPOSURE_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_X:
				return detector_x != DETECTOR_X_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Y:
				return detector_y != DETECTOR_Y_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Z:
				return detector_z != DETECTOR_Z_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__EMAIL:
				return EMAIL_EDEFAULT == null ? email != null : !EMAIL_EDEFAULT.equals(email);
			case LDEExperimentsPackage.SAMPLE__START_DATE:
				return START_DATE_EDEFAULT == null ? startDate != null : !START_DATE_EDEFAULT.equals(startDate);
			case LDEExperimentsPackage.SAMPLE__END_DATE:
				return END_DATE_EDEFAULT == null ? endDate != null : !END_DATE_EDEFAULT.equals(endDate);
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return COMMAND_EDEFAULT == null ? command != null : !COMMAND_EDEFAULT.equals(command);
			case LDEExperimentsPackage.SAMPLE__MAIL_COUNT:
				return mailCount != MAIL_COUNT_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT:
				return dataFileCount != DATA_FILE_COUNT_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__COMMENT:
				return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH:
				return DATA_FILE_PATH_EDEFAULT == null ? dataFilePath != null : !DATA_FILE_PATH_EDEFAULT.equals(dataFilePath);
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
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (sampleID: ");
		if (sampleIDESet) result.append(sampleID); else result.append("<unset>");
		result.append(", status: ");
		if (statusESet) result.append(status); else result.append("<unset>");
		result.append(", active: ");
		result.append(active);
		result.append(", name: ");
		result.append(name);
		result.append(", cellID: ");
		result.append(cellID);
		result.append(", visitID: ");
		result.append(visitID);
		result.append(", calibrant: ");
		result.append(calibrant);
		result.append(", calibrant_x: ");
		result.append(calibrant_x);
		result.append(", calibrant_y: ");
		result.append(calibrant_y);
		result.append(", calibrant_exposure: ");
		result.append(calibrant_exposure);
		result.append(", sample_x_start: ");
		result.append(sample_x_start);
		result.append(", sample_x_stop: ");
		result.append(sample_x_stop);
		result.append(", sample_x_step: ");
		result.append(sample_x_step);
		result.append(", sample_y_start: ");
		result.append(sample_y_start);
		result.append(", sample_y_stop: ");
		result.append(sample_y_stop);
		result.append(", sample_y_step: ");
		result.append(sample_y_step);
		result.append(", sample_exposure: ");
		result.append(sample_exposure);
		result.append(", detector_x: ");
		result.append(detector_x);
		result.append(", detector_y: ");
		result.append(detector_y);
		result.append(", detector_z: ");
		result.append(detector_z);
		result.append(", email: ");
		result.append(email);
		result.append(", startDate: ");
		result.append(startDate);
		result.append(", endDate: ");
		result.append(endDate);
		result.append(", command: ");
		result.append(command);
		result.append(", mailCount: ");
		result.append(mailCount);
		result.append(", dataFileCount: ");
		result.append(dataFileCount);
		result.append(", comment: ");
		result.append(comment);
		result.append(", dataFilePath: ");
		result.append(dataFilePath);
		result.append(')');
		return result.toString();
	}

} //SampleImpl
