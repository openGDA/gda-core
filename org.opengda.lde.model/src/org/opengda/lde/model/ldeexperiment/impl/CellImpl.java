/*******************************************************************************
 * Copyright © 2009, 2015 Diamond Light Source Ltd
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 * 	Diamond Light Source Ltd
 *******************************************************************************/
/**
 */
package org.opengda.lde.model.ldeexperiment.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.exceptions.NotFoundException;
import org.opengda.lde.model.ldeexperiment.util.LDEExperimentsValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cell</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getCellID <em>Cell ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getStage <em>Stage</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getSample <em>Sample</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getVisitID <em>Visit ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getCalibrant <em>Calibrant</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getCalibrant_x <em>Calibrant x</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getCalibrant_y <em>Calibrant y</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getCalibrant_exposure <em>Calibrant exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#isSpin <em>Spin</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getEnvScannableNames <em>Env Scannable Names</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getNumberOfSamples <em>Number Of Samples</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#getEndDate <em>End Date</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.CellImpl#isEnableAutoEmail <em>Enable Auto Email</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CellImpl extends MinimalEObjectImpl.Container implements Cell {
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
	 * The cached value of the '{@link #getSample() <em>Sample</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample()
	 * @generated
	 * @ordered
	 */
	protected EList<Sample> sample;

	/**
	 * The default value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated
	 * @ordered
	 */
	protected static final String VISIT_ID_EDEFAULT = null;

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
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

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
	 * The default value of the '{@link #getCalibrant() <em>Calibrant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCalibrant()
	 * @generated
	 * @ordered
	 */
	protected static final String CALIBRANT_EDEFAULT = "CeO2(NIST-SRM-674b)";

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
	 * The default value of the '{@link #isSpin() <em>Spin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSpin()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SPIN_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSpin() <em>Spin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSpin()
	 * @generated
	 * @ordered
	 */
	protected boolean spin = SPIN_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEnvScannableNames() <em>Env Scannable Names</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnvScannableNames()
	 * @generated
	 * @ordered
	 */
	protected EList<String> envScannableNames;

	/**
	 * The default value of the '{@link #getNumberOfSamples() <em>Number Of Samples</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfSamples()
	 * @generated
	 * @ordered
	 */
	protected static final int NUMBER_OF_SAMPLES_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getNumberOfSamples() <em>Number Of Samples</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumberOfSamples()
	 * @generated
	 * @ordered
	 */
	protected int numberOfSamples = NUMBER_OF_SAMPLES_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEmail() <em>Email</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected EList<String> email;

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
	 * The cached value of the '{@link #getEndDate() <em>End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndDate()
	 * @generated
	 * @ordered
	 */
	protected Date endDate = END_DATE_EDEFAULT;

	/**
	 * The default value of the '{@link #isEnableAutoEmail() <em>Enable Auto Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnableAutoEmail()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLE_AUTO_EMAIL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isEnableAutoEmail() <em>Enable Auto Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnableAutoEmail()
	 * @generated
	 * @ordered
	 */
	protected boolean enableAutoEmail = ENABLE_AUTO_EMAIL_EDEFAULT;

	private boolean processed;

	private boolean calibrated;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CellImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LDEExperimentsPackage.Literals.CELL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCellID() {
		return cellID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCellID(String newCellID) {
		String oldCellID = cellID;
		cellID = newCellID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__CELL_ID, oldCellID, cellID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getVisitID() {
		return visitID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVisitID(String newVisitID) {
		String oldVisitID = visitID;
		visitID = newVisitID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__VISIT_ID, oldVisitID, visitID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getEmail() {
		if (email == null) {
			email = new EDataTypeEList<String>(String.class, this, LDEExperimentsPackage.CELL__EMAIL);
		}
		return email;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStartDate(Date newStartDate) {
		Date oldStartDate = startDate;
		startDate = newStartDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__START_DATE, oldStartDate, startDate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEndDate(Date newEndDate) {
		Date oldEndDate = endDate;
		endDate = newEndDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__END_DATE, oldEndDate, endDate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isEnableAutoEmail() {
		return enableAutoEmail;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnableAutoEmail(boolean newEnableAutoEmail) {
		boolean oldEnableAutoEmail = enableAutoEmail;
		enableAutoEmail = newEnableAutoEmail;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL, oldEnableAutoEmail, enableAutoEmail));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCalibrant() {
		return calibrant;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCalibrant(String newCalibrant) {
		String oldCalibrant = calibrant;
		calibrant = newCalibrant;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__CALIBRANT, oldCalibrant, calibrant));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCalibrant_x() {
		return calibrant_x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCalibrant_x(double newCalibrant_x) {
		double oldCalibrant_x = calibrant_x;
		calibrant_x = newCalibrant_x;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__CALIBRANT_X, oldCalibrant_x, calibrant_x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCalibrant_y() {
		return calibrant_y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCalibrant_y(double newCalibrant_y) {
		double oldCalibrant_y = calibrant_y;
		calibrant_y = newCalibrant_y;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__CALIBRANT_Y, oldCalibrant_y, calibrant_y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCalibrant_exposure() {
		return calibrant_exposure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCalibrant_exposure(double newCalibrant_exposure) {
		double oldCalibrant_exposure = calibrant_exposure;
		calibrant_exposure = newCalibrant_exposure;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE, oldCalibrant_exposure, calibrant_exposure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSpin() {
		return spin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSpin(boolean newSpin) {
		boolean oldSpin = spin;
		spin = newSpin;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__SPIN, oldSpin, spin));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getEnvScannableNames() {
		if (envScannableNames == null) {
			envScannableNames = new EDataTypeUniqueEList<String>(String.class, this, LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES);
		}
		return envScannableNames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNumberOfSamples(int newNumberOfSamples) {
		int oldNumberOfSamples = numberOfSamples;
		numberOfSamples = newNumberOfSamples;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES, oldNumberOfSamples, numberOfSamples));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public Sample getSampleById(String sampleId) {

		for (Sample sample : getSample()) {
			if (sample.getSampleID().equals(sampleId)) {
				return sample;
			}
		}
		throw new NotFoundException("Sample with ID '"+sampleId+"' is not available.");
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Stage getStage() {
		if (eContainerFeatureID() != LDEExperimentsPackage.CELL__STAGE) return null;
		return (Stage)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStage(Stage newStage, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newStage, LDEExperimentsPackage.CELL__STAGE, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStage(Stage newStage) {
		if (newStage != eInternalContainer() || (eContainerFeatureID() != LDEExperimentsPackage.CELL__STAGE && newStage != null)) {
			if (EcoreUtil.isAncestor(this, newStage))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newStage != null)
				msgs = ((InternalEObject)newStage).eInverseAdd(this, LDEExperimentsPackage.STAGE__CELL, Stage.class, msgs);
			msgs = basicSetStage(newStage, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.CELL__STAGE, newStage, newStage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Sample> getSample() {
		if (sample == null) {
			sample = new EObjectContainmentWithInverseEList.Unsettable<Sample>(Sample.class, this, LDEExperimentsPackage.CELL__SAMPLE, LDEExperimentsPackage.SAMPLE__CELL);
		}
		return sample;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetSample() {
		if (sample != null) ((InternalEList.Unsettable<?>)sample).unset();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetSample() {
		return sample != null && ((InternalEList.Unsettable<?>)sample).isSet();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public Sample getSampleByName(String sampleName) {
		for (Sample sample : getSample()) {
			if (sample.getName().equals(sampleName)) {
				return sample;
			}
		}
		throw new NotFoundException("Sample '" +sampleName+"' is not available.");
	}

	/**
	 * <!-- begin-user-doc -->
	 * check if Cell ID property is set or not.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public boolean hasCellID(DiagnosticChain diagnostics, Map<?, ?> context) {
		// -> specify the condition that violates the invariant
		// -> verify the details of the diagnostic, including severity and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (getCellID()==null || getCellID().isEmpty()) {
			if (diagnostics != null) {
				diagnostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 LDEExperimentsValidator.DIAGNOSTIC_SOURCE,
						 LDEExperimentsValidator.CELL__HAS_CELL_ID,
						 SampledefinitionModelPlugin.INSTANCE.getString("_UI_CellIDInvariant_diagnostic", new Object[] { "hasCellID", EObjectValidator.getObjectLabel(this, (Map<Object, Object>) context) }),
						 new Object [] { this }));
			}
			return false;
		}
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * check if Visit ID is set or not
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public boolean hasVisitID(DiagnosticChain daignostics, Map<?, ?> context) {
		// -> specify the condition that violates the invariant
		// -> verify the details of the diagnostic, including severity and message
		// Ensure that you remove @generated or mark it @generated NOT
		if (getVisitID()==null || getVisitID().isEmpty()) {
			if (daignostics != null) {
				daignostics.add
					(new BasicDiagnostic
						(Diagnostic.ERROR,
						 LDEExperimentsValidator.DIAGNOSTIC_SOURCE,
						 LDEExperimentsValidator.CELL__HAS_VISIT_ID,
						 SampledefinitionModelPlugin.INSTANCE.getString("_UI_VisitIDInvariant_diagnostic", new Object[] { "hasVisitID", EObjectValidator.getObjectLabel(this, (Map<Object, Object>) context) }),
						 new Object [] { this }));
			}
			return false;
		}
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.CELL__STAGE:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetStage((Stage)otherEnd, msgs);
			case LDEExperimentsPackage.CELL__SAMPLE:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSample()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.CELL__STAGE:
				return basicSetStage(null, msgs);
			case LDEExperimentsPackage.CELL__SAMPLE:
				return ((InternalEList<?>)getSample()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case LDEExperimentsPackage.CELL__STAGE:
				return eInternalContainer().eInverseRemove(this, LDEExperimentsPackage.STAGE__CELL, Stage.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LDEExperimentsPackage.CELL__CELL_ID:
				return getCellID();
			case LDEExperimentsPackage.CELL__STAGE:
				return getStage();
			case LDEExperimentsPackage.CELL__SAMPLE:
				return getSample();
			case LDEExperimentsPackage.CELL__VISIT_ID:
				return getVisitID();
			case LDEExperimentsPackage.CELL__NAME:
				return getName();
			case LDEExperimentsPackage.CELL__CALIBRANT:
				return getCalibrant();
			case LDEExperimentsPackage.CELL__CALIBRANT_X:
				return getCalibrant_x();
			case LDEExperimentsPackage.CELL__CALIBRANT_Y:
				return getCalibrant_y();
			case LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE:
				return getCalibrant_exposure();
			case LDEExperimentsPackage.CELL__SPIN:
				return isSpin();
			case LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES:
				return getEnvScannableNames();
			case LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES:
				return getNumberOfSamples();
			case LDEExperimentsPackage.CELL__EMAIL:
				return getEmail();
			case LDEExperimentsPackage.CELL__START_DATE:
				return getStartDate();
			case LDEExperimentsPackage.CELL__END_DATE:
				return getEndDate();
			case LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL:
				return isEnableAutoEmail();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case LDEExperimentsPackage.CELL__CELL_ID:
				setCellID((String)newValue);
				return;
			case LDEExperimentsPackage.CELL__STAGE:
				setStage((Stage)newValue);
				return;
			case LDEExperimentsPackage.CELL__SAMPLE:
				getSample().clear();
				getSample().addAll((Collection<? extends Sample>)newValue);
				return;
			case LDEExperimentsPackage.CELL__VISIT_ID:
				setVisitID((String)newValue);
				return;
			case LDEExperimentsPackage.CELL__NAME:
				setName((String)newValue);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT:
				setCalibrant((String)newValue);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_X:
				setCalibrant_x((Double)newValue);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_Y:
				setCalibrant_y((Double)newValue);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE:
				setCalibrant_exposure((Double)newValue);
				return;
			case LDEExperimentsPackage.CELL__SPIN:
				setSpin((Boolean)newValue);
				return;
			case LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES:
				getEnvScannableNames().clear();
				getEnvScannableNames().addAll((Collection<? extends String>)newValue);
				return;
			case LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES:
				setNumberOfSamples((Integer)newValue);
				return;
			case LDEExperimentsPackage.CELL__EMAIL:
				getEmail().clear();
				getEmail().addAll((Collection<? extends String>)newValue);
				return;
			case LDEExperimentsPackage.CELL__START_DATE:
				setStartDate((Date)newValue);
				return;
			case LDEExperimentsPackage.CELL__END_DATE:
				setEndDate((Date)newValue);
				return;
			case LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL:
				setEnableAutoEmail((Boolean)newValue);
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
			case LDEExperimentsPackage.CELL__CELL_ID:
				setCellID(CELL_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__STAGE:
				setStage((Stage)null);
				return;
			case LDEExperimentsPackage.CELL__SAMPLE:
				unsetSample();
				return;
			case LDEExperimentsPackage.CELL__VISIT_ID:
				setVisitID(VISIT_ID_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__NAME:
				setName(NAME_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT:
				setCalibrant(CALIBRANT_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_X:
				setCalibrant_x(CALIBRANT_X_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_Y:
				setCalibrant_y(CALIBRANT_Y_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE:
				setCalibrant_exposure(CALIBRANT_EXPOSURE_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__SPIN:
				setSpin(SPIN_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES:
				getEnvScannableNames().clear();
				return;
			case LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES:
				setNumberOfSamples(NUMBER_OF_SAMPLES_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__EMAIL:
				getEmail().clear();
				return;
			case LDEExperimentsPackage.CELL__START_DATE:
				setStartDate(START_DATE_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__END_DATE:
				setEndDate(END_DATE_EDEFAULT);
				return;
			case LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL:
				setEnableAutoEmail(ENABLE_AUTO_EMAIL_EDEFAULT);
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
			case LDEExperimentsPackage.CELL__CELL_ID:
				return CELL_ID_EDEFAULT == null ? cellID != null : !CELL_ID_EDEFAULT.equals(cellID);
			case LDEExperimentsPackage.CELL__STAGE:
				return getStage() != null;
			case LDEExperimentsPackage.CELL__SAMPLE:
				return isSetSample();
			case LDEExperimentsPackage.CELL__VISIT_ID:
				return VISIT_ID_EDEFAULT == null ? visitID != null : !VISIT_ID_EDEFAULT.equals(visitID);
			case LDEExperimentsPackage.CELL__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case LDEExperimentsPackage.CELL__CALIBRANT:
				return CALIBRANT_EDEFAULT == null ? calibrant != null : !CALIBRANT_EDEFAULT.equals(calibrant);
			case LDEExperimentsPackage.CELL__CALIBRANT_X:
				return calibrant_x != CALIBRANT_X_EDEFAULT;
			case LDEExperimentsPackage.CELL__CALIBRANT_Y:
				return calibrant_y != CALIBRANT_Y_EDEFAULT;
			case LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE:
				return calibrant_exposure != CALIBRANT_EXPOSURE_EDEFAULT;
			case LDEExperimentsPackage.CELL__SPIN:
				return spin != SPIN_EDEFAULT;
			case LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES:
				return envScannableNames != null && !envScannableNames.isEmpty();
			case LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES:
				return numberOfSamples != NUMBER_OF_SAMPLES_EDEFAULT;
			case LDEExperimentsPackage.CELL__EMAIL:
				return email != null && !email.isEmpty();
			case LDEExperimentsPackage.CELL__START_DATE:
				return START_DATE_EDEFAULT == null ? startDate != null : !START_DATE_EDEFAULT.equals(startDate);
			case LDEExperimentsPackage.CELL__END_DATE:
				return END_DATE_EDEFAULT == null ? endDate != null : !END_DATE_EDEFAULT.equals(endDate);
			case LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL:
				return enableAutoEmail != ENABLE_AUTO_EMAIL_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case LDEExperimentsPackage.CELL___GET_SAMPLE_BY_ID__STRING:
				return getSampleById((String)arguments.get(0));
			case LDEExperimentsPackage.CELL___GET_SAMPLE_BY_NAME__STRING:
				return getSampleByName((String)arguments.get(0));
			case LDEExperimentsPackage.CELL___HAS_CELL_ID__DIAGNOSTICCHAIN_MAP:
				return hasCellID((DiagnosticChain)arguments.get(0), (Map<?, ?>)arguments.get(1));
			case LDEExperimentsPackage.CELL___HAS_VISIT_ID__DIAGNOSTICCHAIN_MAP:
				return hasVisitID((DiagnosticChain)arguments.get(0), (Map<?, ?>)arguments.get(1));
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (cellID: ");
		result.append(cellID);
		result.append(", visitID: ");
		result.append(visitID);
		result.append(", name: ");
		result.append(name);
		result.append(", calibrant: ");
		result.append(calibrant);
		result.append(", calibrant_x: ");
		result.append(calibrant_x);
		result.append(", calibrant_y: ");
		result.append(calibrant_y);
		result.append(", calibrant_exposure: ");
		result.append(calibrant_exposure);
		result.append(", spin: ");
		result.append(spin);
		result.append(", envScannableNames: ");
		result.append(envScannableNames);
		result.append(", numberOfSamples: ");
		result.append(numberOfSamples);
		result.append(", email: ");
		result.append(email);
		result.append(", startDate: ");
		result.append(startDate);
		result.append(", endDate: ");
		result.append(endDate);
		result.append(", enableAutoEmail: ");
		result.append(enableAutoEmail);
		result.append(')');
		return result.toString();
	}

	@Override
	public void setProcessed(boolean b) {
		this.processed=b;
	}
	@Override
	public boolean isProcessed() {
		return processed;
	}
	@Override
	public void setCalibrated(boolean b) {
		this.calibrated=b;
	}
	@Override
	public boolean isCalibrated() {
		return calibrated;
	}

} //CellImpl
