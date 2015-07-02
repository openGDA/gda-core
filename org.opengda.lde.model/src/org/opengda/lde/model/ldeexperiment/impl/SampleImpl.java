/*******************************************************************************
 * Copyright © 2009, 2014 Diamond Light Source Ltd
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

import gda.configuration.properties.LocalProperties;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opengda.lde.model.ldeexperiment.Cell;
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
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCell <em>Cell</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#isActive <em>Active</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSampleID <em>Sample ID</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_start <em>Sample xstart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_stop <em>Sample xstop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_x_step <em>Sample xstep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_start <em>Sample ystart</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_stop <em>Sample ystop</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_y_step <em>Sample ystep</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getSample_exposure <em>Sample exposure</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getComment <em>Comment</em>}</li>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.impl.SampleImpl#getDataFilePath <em>Data File Path</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SampleImpl extends MinimalEObjectImpl.Container implements Sample {
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
	protected static final boolean ACTIVE_EDEFAULT = false;

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
	 * The default value of the '{@link #getVisitID() <em>Visit ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVisitID()
	 * @generated NOT
	 * @ordered
	 */
	protected static final String VISIT_ID_EDEFAULT = LocalProperties.get(LocalProperties.RCP_APP_VISIT);

	/**
	 * The default value of the '{@link #getSample_x_start() <em>Sample xstart</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSample_x_start()
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTART_EDEFAULT = Double.NaN;

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
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTOP_EDEFAULT = Double.NaN;

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
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_XSTEP_EDEFAULT = Double.NaN;

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
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTART_EDEFAULT = Double.NaN;

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
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTOP_EDEFAULT = Double.NaN;

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
	 * @generated NOT
	 * @ordered
	 */
	protected static final Double SAMPLE_YSTEP_EDEFAULT = Double.NaN;

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
	 * The default value of the '{@link #getStartDate() <em>Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartDate()
	 * @generated NOT
	 * @ordered
	 */
	protected static final Date START_DATE_EDEFAULT = Calendar.getInstance().getTime();


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
	 * The default value of the '{@link #getCommand() <em>Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_EDEFAULT = null;

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
	 * The default value of the '{@link #getComment() <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComment()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMENT_EDEFAULT = "comment here";

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
	protected static final String DATA_FILE_PATH_EDEFAULT = null;

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
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LDEExperimentsPackage.SAMPLE__CELL:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetCell((Cell)otherEnd, msgs);
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
			case LDEExperimentsPackage.SAMPLE__CELL:
				return basicSetCell(null, msgs);
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
			case LDEExperimentsPackage.SAMPLE__CELL:
				return eInternalContainer().eInverseRemove(this, LDEExperimentsPackage.CELL__SAMPLES, Cell.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Cell getCell() {
		if (eContainerFeatureID() != LDEExperimentsPackage.SAMPLE__CELL) return null;
		return (Cell)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCell(Cell newCell, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newCell, LDEExperimentsPackage.SAMPLE__CELL, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCell(Cell newCell) {
		if (newCell != eInternalContainer() || (eContainerFeatureID() != LDEExperimentsPackage.SAMPLE__CELL && newCell != null)) {
			if (EcoreUtil.isAncestor(this, newCell))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newCell != null)
				msgs = ((InternalEObject)newCell).eInverseAdd(this, LDEExperimentsPackage.CELL__SAMPLES, Cell.class, msgs);
			msgs = basicSetCell(newCell, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LDEExperimentsPackage.SAMPLE__CELL, newCell, newCell));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case LDEExperimentsPackage.SAMPLE__CELL:
				return getCell();
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return getStatus();
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				return isActive();
			case LDEExperimentsPackage.SAMPLE__NAME:
				return getName();
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return getSampleID();
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
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return getCommand();
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
			case LDEExperimentsPackage.SAMPLE__CELL:
				setCell((Cell)newValue);
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
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				setSampleID((String)newValue);
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
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand((String)newValue);
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
			case LDEExperimentsPackage.SAMPLE__CELL:
				setCell((Cell)null);
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
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				unsetSampleID();
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
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				setCommand(COMMAND_EDEFAULT);
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
			case LDEExperimentsPackage.SAMPLE__CELL:
				return getCell() != null;
			case LDEExperimentsPackage.SAMPLE__STATUS:
				return isSetStatus();
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
				return active != ACTIVE_EDEFAULT;
			case LDEExperimentsPackage.SAMPLE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
				return isSetSampleID();
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
			case LDEExperimentsPackage.SAMPLE__COMMAND:
				return COMMAND_EDEFAULT == null ? command != null : !COMMAND_EDEFAULT.equals(command);
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
		result.append(" (status: ");
		if (statusESet) result.append(status); else result.append("<unset>");
		result.append(", active: ");
		result.append(active);
		result.append(", name: ");
		result.append(name);
		result.append(", sampleID: ");
		if (sampleIDESet) result.append(sampleID); else result.append("<unset>");
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
		result.append(", command: ");
		result.append(command);
		result.append(", comment: ");
		result.append(comment);
		result.append(", dataFilePath: ");
		result.append(dataFilePath);
		result.append(')');
		return result.toString();
	}

} //SampleImpl
