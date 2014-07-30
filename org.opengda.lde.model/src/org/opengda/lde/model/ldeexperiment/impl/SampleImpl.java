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
import org.opengda.lde.model.ldeexperiment.LdeexperimentPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getMailCount <em>Mail Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDataFileCount <em>Data File Count</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDataFilePath <em>Data File Path</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getX_start <em>Xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getX_stop <em>Xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getX_step <em>Xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getY_stop <em>Ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getY_step <em>Ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDriveID <em>Drive ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getPixium_x <em>Pixium x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getPixium_y <em>Pixium y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getPixium_z <em>Pixium z</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getY_start <em>Ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getEndDate <em>End Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCommand <em>Command</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleImpl extends MinimalEObjectImpl.Container implements Sample {
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
	 * The default value of the '{@link #getSampleID() <em>Sample ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSampleID()
	 * @generated
	 * @ordered
	 */
	protected static final String SAMPLE_ID_EDEFAULT = "";

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
	 * The default value of the '{@link #getCellID() <em>Cell ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCellID()
	 * @generated
	 * @ordered
	 */
	protected static final String CELL_ID_EDEFAULT = "ms1-1";

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
	 * The default value of the '{@link #getCalibrant() <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant()
	 * @generated NOT
	 * @ordered
	 */
	private static Date tomorrow() {
		 Calendar calendar=Calendar.getInstance();
		 calendar.add(Calendar.DAY_OF_YEAR, 1);
		 return (calendar.getTime());
	}
	
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
	 * The default value of the '{@link #getX_start() <em>Xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_start()
	 * @generated
	 * @ordered
	 */
	protected static final double XSTART_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getX_start() <em>Xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_start()
	 * @generated
	 * @ordered
	 */
	protected double x_start = XSTART_EDEFAULT;

	/**
	 * The default value of the '{@link #getX_stop() <em>Xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_stop()
	 * @generated
	 * @ordered
	 */
	protected static final double XSTOP_EDEFAULT = 1.0;

	/**
	 * The cached value of the '{@link #getX_stop() <em>Xstop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_stop()
	 * @generated
	 * @ordered
	 */
	protected double x_stop = XSTOP_EDEFAULT;

	/**
	 * The default value of the '{@link #getX_step() <em>Xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_step()
	 * @generated
	 * @ordered
	 */
	protected static final double XSTEP_EDEFAULT = 0.1;

	/**
	 * The cached value of the '{@link #getX_step() <em>Xstep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX_step()
	 * @generated
	 * @ordered
	 */
	protected double x_step = XSTEP_EDEFAULT;

	/**
	 * The default value of the '{@link #getY_stop() <em>Ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_stop()
	 * @generated
	 * @ordered
	 */
	protected static final Double YSTOP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getY_stop() <em>Ystop</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_stop()
	 * @generated
	 * @ordered
	 */
	protected Double y_stop = YSTOP_EDEFAULT;

	/**
	 * The default value of the '{@link #getY_step() <em>Ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_step()
	 * @generated
	 * @ordered
	 */
	protected static final Double YSTEP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getY_step() <em>Ystep</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_step()
	 * @generated
	 * @ordered
	 */
	protected Double y_step = YSTEP_EDEFAULT;

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
	 * The default value of the '{@link #getDriveID() <em>Drive ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriveID()
	 * @generated
	 * @ordered
	 */
	protected static final String DRIVE_ID_EDEFAULT = "i11-1";

	/**
	 * The cached value of the '{@link #getDriveID() <em>Drive ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriveID()
	 * @generated
	 * @ordered
	 */
	protected String driveID = DRIVE_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getPixium_x() <em>Pixium x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_x()
	 * @generated
	 * @ordered
	 */
	protected static final double PIXIUM_X_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getPixium_x() <em>Pixium x</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_x()
	 * @generated
	 * @ordered
	 */
	protected double pixium_x = PIXIUM_X_EDEFAULT;

	/**
	 * The default value of the '{@link #getPixium_y() <em>Pixium y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_y()
	 * @generated
	 * @ordered
	 */
	protected static final double PIXIUM_Y_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getPixium_y() <em>Pixium y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_y()
	 * @generated
	 * @ordered
	 */
	protected double pixium_y = PIXIUM_Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getPixium_z() <em>Pixium z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_z()
	 * @generated
	 * @ordered
	 */
	protected static final double PIXIUM_Z_EDEFAULT = 100.0;

	/**
	 * The cached value of the '{@link #getPixium_z() <em>Pixium z</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixium_z()
	 * @generated
	 * @ordered
	 */
	protected double pixium_z = PIXIUM_Z_EDEFAULT;

	/**
	 * The default value of the '{@link #getY_start() <em>Ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_start()
	 * @generated
	 * @ordered
	 */
	protected static final Double YSTART_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getY_start() <em>Ystart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY_start()
	 * @generated
	 * @ordered
	 */
	protected Double y_start = YSTART_EDEFAULT;

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
	protected static final Date END_DATE_EDEFAULT =tomorrow();

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
	protected static final String COMMAND_EDEFAULT = "safePosition";

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected SampleImpl() {
		super();
		setSampleID((EcoreUtil.generateUUID()));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LdeexperimentPackage.Literals.SAMPLE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__SAMPLE_ID, oldSampleID, sampleID, !oldSampleIDESet));
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
			eNotify(new ENotificationImpl(this, Notification.UNSET, LdeexperimentPackage.SAMPLE__SAMPLE_ID, oldSampleID, SAMPLE_ID_EDEFAULT, oldSampleIDESet));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__STATUS, oldStatus, status, !oldStatusESet));
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
			eNotify(new ENotificationImpl(this, Notification.UNSET, LdeexperimentPackage.SAMPLE__STATUS, oldStatus, STATUS_EDEFAULT, oldStatusESet));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__ACTIVE, oldActive, active));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__CELL_ID, oldCellID, cellID));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__VISIT_ID, oldVisitID, visitID));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__EMAIL, oldEmail, email));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__COMMAND, oldCommand, command));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__COMMENT, oldComment, comment));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__START_DATE, oldStartDate, startDate));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__END_DATE, oldEndDate, endDate));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__MAIL_COUNT, oldMailCount, mailCount));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT, oldDataFileCount, dataFileCount));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__DATA_FILE_PATH, oldDataFilePath, dataFilePath));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__CALIBRANT, oldCalibrant, calibrant));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__CALIBRANT_X, oldCalibrant_x, calibrant_x));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__CALIBRANT_Y, oldCalibrant_y, calibrant_y));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE, oldCalibrant_exposure, calibrant_exposure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getX_start() {
		return x_start;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX_start(double newX_start) {
		double oldX_start = x_start;
		x_start = newX_start;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__XSTART, oldX_start, x_start));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getX_stop() {
		return x_stop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX_stop(double newX_stop) {
		double oldX_stop = x_stop;
		x_stop = newX_stop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__XSTOP, oldX_stop, x_stop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getX_step() {
		return x_step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX_step(double newX_step) {
		double oldX_step = x_step;
		x_step = newX_step;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__XSTEP, oldX_step, x_step));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getY_stop() {
		return y_stop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY_stop(Double newY_stop) {
		Double oldY_stop = y_stop;
		y_stop = newY_stop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__YSTOP, oldY_stop, y_stop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getY_step() {
		return y_step;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY_step(Double newY_step) {
		Double oldY_step = y_step;
		y_step = newY_step;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__YSTEP, oldY_step, y_step));
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
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE, oldSample_exposure, sample_exposure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDriveID() {
		return driveID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDriveID(String newDriveID) {
		String oldDriveID = driveID;
		driveID = newDriveID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__DRIVE_ID, oldDriveID, driveID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getPixium_x() {
		return pixium_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPixium_x(double newPixium_x) {
		double oldPixium_x = pixium_x;
		pixium_x = newPixium_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__PIXIUM_X, oldPixium_x, pixium_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getPixium_y() {
		return pixium_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPixium_y(double newPixium_y) {
		double oldPixium_y = pixium_y;
		pixium_y = newPixium_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__PIXIUM_Y, oldPixium_y, pixium_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getPixium_z() {
		return pixium_z;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPixium_z(double newPixium_z) {
		double oldPixium_z = pixium_z;
		pixium_z = newPixium_z;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__PIXIUM_Z, oldPixium_z, pixium_z));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getY_start() {
		return y_start;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY_start(Double newY_start) {
		Double oldY_start = y_start;
		y_start = newY_start;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LdeexperimentPackage.SAMPLE__YSTART, oldY_start, y_start));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LdeexperimentPackage.SAMPLE__NAME:
				return getName();
			case LdeexperimentPackage.SAMPLE__SAMPLE_ID:
				return getSampleID();
			case LdeexperimentPackage.SAMPLE__STATUS:
				return getStatus();
			case LdeexperimentPackage.SAMPLE__ACTIVE:
				return isActive();
			case LdeexperimentPackage.SAMPLE__CELL_ID:
				return getCellID();
			case LdeexperimentPackage.SAMPLE__VISIT_ID:
				return getVisitID();
			case LdeexperimentPackage.SAMPLE__EMAIL:
				return getEmail();
			case LdeexperimentPackage.SAMPLE__COMMENT:
				return getComment();
			case LdeexperimentPackage.SAMPLE__MAIL_COUNT:
				return getMailCount();
			case LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT:
				return getDataFileCount();
			case LdeexperimentPackage.SAMPLE__DATA_FILE_PATH:
				return getDataFilePath();
			case LdeexperimentPackage.SAMPLE__CALIBRANT:
				return getCalibrant();
			case LdeexperimentPackage.SAMPLE__CALIBRANT_X:
				return getCalibrant_x();
			case LdeexperimentPackage.SAMPLE__CALIBRANT_Y:
				return getCalibrant_y();
			case LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE:
				return getCalibrant_exposure();
			case LdeexperimentPackage.SAMPLE__XSTART:
				return getX_start();
			case LdeexperimentPackage.SAMPLE__XSTOP:
				return getX_stop();
			case LdeexperimentPackage.SAMPLE__XSTEP:
				return getX_step();
			case LdeexperimentPackage.SAMPLE__YSTOP:
				return getY_stop();
			case LdeexperimentPackage.SAMPLE__YSTEP:
				return getY_step();
			case LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE:
				return getSample_exposure();
			case LdeexperimentPackage.SAMPLE__DRIVE_ID:
				return getDriveID();
			case LdeexperimentPackage.SAMPLE__PIXIUM_X:
				return getPixium_x();
			case LdeexperimentPackage.SAMPLE__PIXIUM_Y:
				return getPixium_y();
			case LdeexperimentPackage.SAMPLE__PIXIUM_Z:
				return getPixium_z();
			case LdeexperimentPackage.SAMPLE__YSTART:
				return getY_start();
			case LdeexperimentPackage.SAMPLE__START_DATE:
				return getStartDate();
			case LdeexperimentPackage.SAMPLE__END_DATE:
				return getEndDate();
			case LdeexperimentPackage.SAMPLE__COMMAND:
				return getCommand();
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
			case LdeexperimentPackage.SAMPLE__NAME:
				setName((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__SAMPLE_ID:
				setSampleID((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__STATUS:
				setStatus((STATUS)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__ACTIVE:
				setActive((Boolean)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__CELL_ID:
				setCellID((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__VISIT_ID:
				setVisitID((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__EMAIL:
				setEmail((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__COMMENT:
				setComment((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__MAIL_COUNT:
				setMailCount((Integer)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT:
				setDataFileCount((Integer)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_PATH:
				setDataFilePath((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT:
				setCalibrant((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_X:
				setCalibrant_x((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_Y:
				setCalibrant_y((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE:
				setCalibrant_exposure((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__XSTART:
				setX_start((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__XSTOP:
				setX_stop((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__XSTEP:
				setX_step((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__YSTOP:
				setY_stop((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__YSTEP:
				setY_step((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE:
				setSample_exposure((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__DRIVE_ID:
				setDriveID((String)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_X:
				setPixium_x((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Y:
				setPixium_y((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Z:
				setPixium_z((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__YSTART:
				setY_start((Double)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__START_DATE:
				setStartDate((Date)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__END_DATE:
				setEndDate((Date)newValue);
				return;
			case LdeexperimentPackage.SAMPLE__COMMAND:
				setCommand((String)newValue);
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
			case LdeexperimentPackage.SAMPLE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__SAMPLE_ID:
				unsetSampleID();
				return;
			case LdeexperimentPackage.SAMPLE__STATUS:
				unsetStatus();
				return;
			case LdeexperimentPackage.SAMPLE__ACTIVE:
				setActive(ACTIVE_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__CELL_ID:
				setCellID(CELL_ID_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__VISIT_ID:
				setVisitID(VISIT_ID_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__EMAIL:
				setEmail(EMAIL_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__COMMENT:
				setComment(COMMENT_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__MAIL_COUNT:
				setMailCount(MAIL_COUNT_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT:
				setDataFileCount(DATA_FILE_COUNT_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_PATH:
				setDataFilePath(DATA_FILE_PATH_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT:
				setCalibrant(CALIBRANT_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_X:
				setCalibrant_x(CALIBRANT_X_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_Y:
				setCalibrant_y(CALIBRANT_Y_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE:
				setCalibrant_exposure(CALIBRANT_EXPOSURE_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__XSTART:
				setX_start(XSTART_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__XSTOP:
				setX_stop(XSTOP_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__XSTEP:
				setX_step(XSTEP_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__YSTOP:
				setY_stop(YSTOP_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__YSTEP:
				setY_step(YSTEP_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE:
				setSample_exposure(SAMPLE_EXPOSURE_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__DRIVE_ID:
				setDriveID(DRIVE_ID_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_X:
				setPixium_x(PIXIUM_X_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Y:
				setPixium_y(PIXIUM_Y_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Z:
				setPixium_z(PIXIUM_Z_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__YSTART:
				setY_start(YSTART_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__START_DATE:
				setStartDate(START_DATE_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__END_DATE:
				setEndDate(END_DATE_EDEFAULT);
				return;
			case LdeexperimentPackage.SAMPLE__COMMAND:
				setCommand(COMMAND_EDEFAULT);
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
			case LdeexperimentPackage.SAMPLE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case LdeexperimentPackage.SAMPLE__SAMPLE_ID:
				return isSetSampleID();
			case LdeexperimentPackage.SAMPLE__STATUS:
				return isSetStatus();
			case LdeexperimentPackage.SAMPLE__ACTIVE:
				return active != ACTIVE_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__CELL_ID:
				return CELL_ID_EDEFAULT == null ? cellID != null : !CELL_ID_EDEFAULT.equals(cellID);
			case LdeexperimentPackage.SAMPLE__VISIT_ID:
				return VISIT_ID_EDEFAULT == null ? visitID != null : !VISIT_ID_EDEFAULT.equals(visitID);
			case LdeexperimentPackage.SAMPLE__EMAIL:
				return EMAIL_EDEFAULT == null ? email != null : !EMAIL_EDEFAULT.equals(email);
			case LdeexperimentPackage.SAMPLE__COMMENT:
				return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
			case LdeexperimentPackage.SAMPLE__MAIL_COUNT:
				return mailCount != MAIL_COUNT_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT:
				return dataFileCount != DATA_FILE_COUNT_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__DATA_FILE_PATH:
				return DATA_FILE_PATH_EDEFAULT == null ? dataFilePath != null : !DATA_FILE_PATH_EDEFAULT.equals(dataFilePath);
			case LdeexperimentPackage.SAMPLE__CALIBRANT:
				return CALIBRANT_EDEFAULT == null ? calibrant != null : !CALIBRANT_EDEFAULT.equals(calibrant);
			case LdeexperimentPackage.SAMPLE__CALIBRANT_X:
				return calibrant_x != CALIBRANT_X_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_Y:
				return calibrant_y != CALIBRANT_Y_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE:
				return calibrant_exposure != CALIBRANT_EXPOSURE_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__XSTART:
				return x_start != XSTART_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__XSTOP:
				return x_stop != XSTOP_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__XSTEP:
				return x_step != XSTEP_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__YSTOP:
				return YSTOP_EDEFAULT == null ? y_stop != null : !YSTOP_EDEFAULT.equals(y_stop);
			case LdeexperimentPackage.SAMPLE__YSTEP:
				return YSTEP_EDEFAULT == null ? y_step != null : !YSTEP_EDEFAULT.equals(y_step);
			case LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE:
				return sample_exposure != SAMPLE_EXPOSURE_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__DRIVE_ID:
				return DRIVE_ID_EDEFAULT == null ? driveID != null : !DRIVE_ID_EDEFAULT.equals(driveID);
			case LdeexperimentPackage.SAMPLE__PIXIUM_X:
				return pixium_x != PIXIUM_X_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Y:
				return pixium_y != PIXIUM_Y_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__PIXIUM_Z:
				return pixium_z != PIXIUM_Z_EDEFAULT;
			case LdeexperimentPackage.SAMPLE__YSTART:
				return YSTART_EDEFAULT == null ? y_start != null : !YSTART_EDEFAULT.equals(y_start);
			case LdeexperimentPackage.SAMPLE__START_DATE:
				return START_DATE_EDEFAULT == null ? startDate != null : !START_DATE_EDEFAULT.equals(startDate);
			case LdeexperimentPackage.SAMPLE__END_DATE:
				return END_DATE_EDEFAULT == null ? endDate != null : !END_DATE_EDEFAULT.equals(endDate);
			case LdeexperimentPackage.SAMPLE__COMMAND:
				return COMMAND_EDEFAULT == null ? command != null : !COMMAND_EDEFAULT.equals(command);
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
		result.append(" (name: ");
		result.append(name);
		result.append(", sampleID: ");
		if (sampleIDESet) result.append(sampleID); else result.append("<unset>");
		result.append(", status: ");
		if (statusESet) result.append(status); else result.append("<unset>");
		result.append(", active: ");
		result.append(active);
		result.append(", cellID: ");
		result.append(cellID);
		result.append(", visitID: ");
		result.append(visitID);
		result.append(", email: ");
		result.append(email);
		result.append(", comment: ");
		result.append(comment);
		result.append(", mailCount: ");
		result.append(mailCount);
		result.append(", dataFileCount: ");
		result.append(dataFileCount);
		result.append(", dataFilePath: ");
		result.append(dataFilePath);
		result.append(", calibrant: ");
		result.append(calibrant);
		result.append(", calibrant_x: ");
		result.append(calibrant_x);
		result.append(", calibrant_y: ");
		result.append(calibrant_y);
		result.append(", calibrant_exposure: ");
		result.append(calibrant_exposure);
		result.append(", x_start: ");
		result.append(x_start);
		result.append(", x_stop: ");
		result.append(x_stop);
		result.append(", x_step: ");
		result.append(x_step);
		result.append(", y_stop: ");
		result.append(y_stop);
		result.append(", y_step: ");
		result.append(y_step);
		result.append(", sample_exposure: ");
		result.append(sample_exposure);
		result.append(", driveID: ");
		result.append(driveID);
		result.append(", pixium_x: ");
		result.append(pixium_x);
		result.append(", pixium_y: ");
		result.append(pixium_y);
		result.append(", pixium_z: ");
		result.append(pixium_z);
		result.append(", y_start: ");
		result.append(y_start);
		result.append(", startDate: ");
		result.append(startDate);
		result.append(", endDate: ");
		result.append(endDate);
		result.append(", command: ");
		result.append(command);
		result.append(')');
		return result.toString();
	}

} //SampleImpl
