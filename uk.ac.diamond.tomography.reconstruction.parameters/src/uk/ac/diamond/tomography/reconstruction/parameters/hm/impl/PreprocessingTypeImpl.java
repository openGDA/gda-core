/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterColumnsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksAfterRowsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HighPeaksBeforeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.IntensityType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.PreprocessingType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Preprocessing Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl#getHighPeaksBefore <em>High Peaks Before</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl#getRingArtefacts <em>Ring Artefacts</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl#getIntensity <em>Intensity</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl#getHighPeaksAfterRows <em>High Peaks After Rows</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.PreprocessingTypeImpl#getHighPeaksAfterColumns <em>High Peaks After Columns</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PreprocessingTypeImpl extends EObjectImpl implements PreprocessingType {
	/**
	 * The cached value of the '{@link #getHighPeaksBefore() <em>High Peaks Before</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHighPeaksBefore()
	 * @generated
	 * @ordered
	 */
	protected HighPeaksBeforeType highPeaksBefore;

	/**
	 * The cached value of the '{@link #getRingArtefacts() <em>Ring Artefacts</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRingArtefacts()
	 * @generated
	 * @ordered
	 */
	protected RingArtefactsType ringArtefacts;

	/**
	 * The cached value of the '{@link #getIntensity() <em>Intensity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntensity()
	 * @generated
	 * @ordered
	 */
	protected IntensityType intensity;

	/**
	 * The cached value of the '{@link #getHighPeaksAfterRows() <em>High Peaks After Rows</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHighPeaksAfterRows()
	 * @generated
	 * @ordered
	 */
	protected HighPeaksAfterRowsType highPeaksAfterRows;

	/**
	 * The cached value of the '{@link #getHighPeaksAfterColumns() <em>High Peaks After Columns</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHighPeaksAfterColumns()
	 * @generated
	 * @ordered
	 */
	protected HighPeaksAfterColumnsType highPeaksAfterColumns;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PreprocessingTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.PREPROCESSING_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HighPeaksBeforeType getHighPeaksBefore() {
		return highPeaksBefore;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHighPeaksBefore(HighPeaksBeforeType newHighPeaksBefore, NotificationChain msgs) {
		HighPeaksBeforeType oldHighPeaksBefore = highPeaksBefore;
		highPeaksBefore = newHighPeaksBefore;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE, oldHighPeaksBefore, newHighPeaksBefore);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHighPeaksBefore(HighPeaksBeforeType newHighPeaksBefore) {
		if (newHighPeaksBefore != highPeaksBefore) {
			NotificationChain msgs = null;
			if (highPeaksBefore != null)
				msgs = ((InternalEObject)highPeaksBefore).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE, null, msgs);
			if (newHighPeaksBefore != null)
				msgs = ((InternalEObject)newHighPeaksBefore).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE, null, msgs);
			msgs = basicSetHighPeaksBefore(newHighPeaksBefore, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE, newHighPeaksBefore, newHighPeaksBefore));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RingArtefactsType getRingArtefacts() {
		return ringArtefacts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRingArtefacts(RingArtefactsType newRingArtefacts, NotificationChain msgs) {
		RingArtefactsType oldRingArtefacts = ringArtefacts;
		ringArtefacts = newRingArtefacts;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS, oldRingArtefacts, newRingArtefacts);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRingArtefacts(RingArtefactsType newRingArtefacts) {
		if (newRingArtefacts != ringArtefacts) {
			NotificationChain msgs = null;
			if (ringArtefacts != null)
				msgs = ((InternalEObject)ringArtefacts).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS, null, msgs);
			if (newRingArtefacts != null)
				msgs = ((InternalEObject)newRingArtefacts).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS, null, msgs);
			msgs = basicSetRingArtefacts(newRingArtefacts, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS, newRingArtefacts, newRingArtefacts));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IntensityType getIntensity() {
		return intensity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetIntensity(IntensityType newIntensity, NotificationChain msgs) {
		IntensityType oldIntensity = intensity;
		intensity = newIntensity;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__INTENSITY, oldIntensity, newIntensity);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIntensity(IntensityType newIntensity) {
		if (newIntensity != intensity) {
			NotificationChain msgs = null;
			if (intensity != null)
				msgs = ((InternalEObject)intensity).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__INTENSITY, null, msgs);
			if (newIntensity != null)
				msgs = ((InternalEObject)newIntensity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__INTENSITY, null, msgs);
			msgs = basicSetIntensity(newIntensity, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__INTENSITY, newIntensity, newIntensity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HighPeaksAfterRowsType getHighPeaksAfterRows() {
		return highPeaksAfterRows;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHighPeaksAfterRows(HighPeaksAfterRowsType newHighPeaksAfterRows, NotificationChain msgs) {
		HighPeaksAfterRowsType oldHighPeaksAfterRows = highPeaksAfterRows;
		highPeaksAfterRows = newHighPeaksAfterRows;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS, oldHighPeaksAfterRows, newHighPeaksAfterRows);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHighPeaksAfterRows(HighPeaksAfterRowsType newHighPeaksAfterRows) {
		if (newHighPeaksAfterRows != highPeaksAfterRows) {
			NotificationChain msgs = null;
			if (highPeaksAfterRows != null)
				msgs = ((InternalEObject)highPeaksAfterRows).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS, null, msgs);
			if (newHighPeaksAfterRows != null)
				msgs = ((InternalEObject)newHighPeaksAfterRows).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS, null, msgs);
			msgs = basicSetHighPeaksAfterRows(newHighPeaksAfterRows, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS, newHighPeaksAfterRows, newHighPeaksAfterRows));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HighPeaksAfterColumnsType getHighPeaksAfterColumns() {
		return highPeaksAfterColumns;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHighPeaksAfterColumns(HighPeaksAfterColumnsType newHighPeaksAfterColumns, NotificationChain msgs) {
		HighPeaksAfterColumnsType oldHighPeaksAfterColumns = highPeaksAfterColumns;
		highPeaksAfterColumns = newHighPeaksAfterColumns;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS, oldHighPeaksAfterColumns, newHighPeaksAfterColumns);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHighPeaksAfterColumns(HighPeaksAfterColumnsType newHighPeaksAfterColumns) {
		if (newHighPeaksAfterColumns != highPeaksAfterColumns) {
			NotificationChain msgs = null;
			if (highPeaksAfterColumns != null)
				msgs = ((InternalEObject)highPeaksAfterColumns).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS, null, msgs);
			if (newHighPeaksAfterColumns != null)
				msgs = ((InternalEObject)newHighPeaksAfterColumns).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS, null, msgs);
			msgs = basicSetHighPeaksAfterColumns(newHighPeaksAfterColumns, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS, newHighPeaksAfterColumns, newHighPeaksAfterColumns));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE:
				return basicSetHighPeaksBefore(null, msgs);
			case HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS:
				return basicSetRingArtefacts(null, msgs);
			case HmPackage.PREPROCESSING_TYPE__INTENSITY:
				return basicSetIntensity(null, msgs);
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS:
				return basicSetHighPeaksAfterRows(null, msgs);
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS:
				return basicSetHighPeaksAfterColumns(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE:
				return getHighPeaksBefore();
			case HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS:
				return getRingArtefacts();
			case HmPackage.PREPROCESSING_TYPE__INTENSITY:
				return getIntensity();
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS:
				return getHighPeaksAfterRows();
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS:
				return getHighPeaksAfterColumns();
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
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE:
				setHighPeaksBefore((HighPeaksBeforeType)newValue);
				return;
			case HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS:
				setRingArtefacts((RingArtefactsType)newValue);
				return;
			case HmPackage.PREPROCESSING_TYPE__INTENSITY:
				setIntensity((IntensityType)newValue);
				return;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS:
				setHighPeaksAfterRows((HighPeaksAfterRowsType)newValue);
				return;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS:
				setHighPeaksAfterColumns((HighPeaksAfterColumnsType)newValue);
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
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE:
				setHighPeaksBefore((HighPeaksBeforeType)null);
				return;
			case HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS:
				setRingArtefacts((RingArtefactsType)null);
				return;
			case HmPackage.PREPROCESSING_TYPE__INTENSITY:
				setIntensity((IntensityType)null);
				return;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS:
				setHighPeaksAfterRows((HighPeaksAfterRowsType)null);
				return;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS:
				setHighPeaksAfterColumns((HighPeaksAfterColumnsType)null);
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
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_BEFORE:
				return highPeaksBefore != null;
			case HmPackage.PREPROCESSING_TYPE__RING_ARTEFACTS:
				return ringArtefacts != null;
			case HmPackage.PREPROCESSING_TYPE__INTENSITY:
				return intensity != null;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_ROWS:
				return highPeaksAfterRows != null;
			case HmPackage.PREPROCESSING_TYPE__HIGH_PEAKS_AFTER_COLUMNS:
				return highPeaksAfterColumns != null;
		}
		return super.eIsSet(featureID);
	}

} //PreprocessingTypeImpl
