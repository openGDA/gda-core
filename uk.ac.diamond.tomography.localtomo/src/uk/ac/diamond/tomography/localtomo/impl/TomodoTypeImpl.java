/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.localtomo.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.localtomo.ClusterType;
import uk.ac.diamond.tomography.localtomo.ImagekeyencodingType;
import uk.ac.diamond.tomography.localtomo.LocalTomoPackage;
import uk.ac.diamond.tomography.localtomo.NexusfileType;
import uk.ac.diamond.tomography.localtomo.SettingsfileType;
import uk.ac.diamond.tomography.localtomo.ShutterType;
import uk.ac.diamond.tomography.localtomo.TifimageType;
import uk.ac.diamond.tomography.localtomo.TomodoType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tomodo Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getShutter <em>Shutter</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getTifimage <em>Tifimage</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getNexusfile <em>Nexusfile</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getSettingsfile <em>Settingsfile</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getImagekeyencoding <em>Imagekeyencoding</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getCluster <em>Cluster</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.localtomo.impl.TomodoTypeImpl#getSegmentsToRemoveRelativeToNexusForOutdir <em>Segments To Remove Relative To Nexus For Outdir</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TomodoTypeImpl extends EObjectImpl implements TomodoType {
	/**
	 * The cached value of the '{@link #getShutter() <em>Shutter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutter()
	 * @generated
	 * @ordered
	 */
	protected ShutterType shutter;

	/**
	 * The cached value of the '{@link #getTifimage() <em>Tifimage</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTifimage()
	 * @generated
	 * @ordered
	 */
	protected TifimageType tifimage;

	/**
	 * The cached value of the '{@link #getNexusfile() <em>Nexusfile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNexusfile()
	 * @generated
	 * @ordered
	 */
	protected NexusfileType nexusfile;

	/**
	 * The cached value of the '{@link #getSettingsfile() <em>Settingsfile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSettingsfile()
	 * @generated
	 * @ordered
	 */
	protected SettingsfileType settingsfile;

	/**
	 * The cached value of the '{@link #getImagekeyencoding() <em>Imagekeyencoding</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImagekeyencoding()
	 * @generated
	 * @ordered
	 */
	protected ImagekeyencodingType imagekeyencoding;

	/**
	 * The cached value of the '{@link #getCluster() <em>Cluster</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCluster()
	 * @generated
	 * @ordered
	 */
	protected ClusterType cluster;

	/**
	 * The default value of the '{@link #getSegmentsToRemoveRelativeToNexusForOutdir() <em>Segments To Remove Relative To Nexus For Outdir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSegmentsToRemoveRelativeToNexusForOutdir()
	 * @generated
	 * @ordered
	 */
	protected static final int SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSegmentsToRemoveRelativeToNexusForOutdir() <em>Segments To Remove Relative To Nexus For Outdir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSegmentsToRemoveRelativeToNexusForOutdir()
	 * @generated
	 * @ordered
	 */
	protected int segmentsToRemoveRelativeToNexusForOutdir = SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR_EDEFAULT;

	/**
	 * This is true if the Segments To Remove Relative To Nexus For Outdir attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean segmentsToRemoveRelativeToNexusForOutdirESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TomodoTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LocalTomoPackage.Literals.TOMODO_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShutterType getShutter() {
		return shutter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetShutter(ShutterType newShutter, NotificationChain msgs) {
		ShutterType oldShutter = shutter;
		shutter = newShutter;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__SHUTTER, oldShutter, newShutter);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShutter(ShutterType newShutter) {
		if (newShutter != shutter) {
			NotificationChain msgs = null;
			if (shutter != null)
				msgs = ((InternalEObject)shutter).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__SHUTTER, null, msgs);
			if (newShutter != null)
				msgs = ((InternalEObject)newShutter).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__SHUTTER, null, msgs);
			msgs = basicSetShutter(newShutter, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__SHUTTER, newShutter, newShutter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TifimageType getTifimage() {
		return tifimage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTifimage(TifimageType newTifimage, NotificationChain msgs) {
		TifimageType oldTifimage = tifimage;
		tifimage = newTifimage;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__TIFIMAGE, oldTifimage, newTifimage);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTifimage(TifimageType newTifimage) {
		if (newTifimage != tifimage) {
			NotificationChain msgs = null;
			if (tifimage != null)
				msgs = ((InternalEObject)tifimage).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__TIFIMAGE, null, msgs);
			if (newTifimage != null)
				msgs = ((InternalEObject)newTifimage).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__TIFIMAGE, null, msgs);
			msgs = basicSetTifimage(newTifimage, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__TIFIMAGE, newTifimage, newTifimage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NexusfileType getNexusfile() {
		return nexusfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNexusfile(NexusfileType newNexusfile, NotificationChain msgs) {
		NexusfileType oldNexusfile = nexusfile;
		nexusfile = newNexusfile;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__NEXUSFILE, oldNexusfile, newNexusfile);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNexusfile(NexusfileType newNexusfile) {
		if (newNexusfile != nexusfile) {
			NotificationChain msgs = null;
			if (nexusfile != null)
				msgs = ((InternalEObject)nexusfile).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__NEXUSFILE, null, msgs);
			if (newNexusfile != null)
				msgs = ((InternalEObject)newNexusfile).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__NEXUSFILE, null, msgs);
			msgs = basicSetNexusfile(newNexusfile, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__NEXUSFILE, newNexusfile, newNexusfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SettingsfileType getSettingsfile() {
		return settingsfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSettingsfile(SettingsfileType newSettingsfile, NotificationChain msgs) {
		SettingsfileType oldSettingsfile = settingsfile;
		settingsfile = newSettingsfile;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE, oldSettingsfile, newSettingsfile);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSettingsfile(SettingsfileType newSettingsfile) {
		if (newSettingsfile != settingsfile) {
			NotificationChain msgs = null;
			if (settingsfile != null)
				msgs = ((InternalEObject)settingsfile).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE, null, msgs);
			if (newSettingsfile != null)
				msgs = ((InternalEObject)newSettingsfile).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE, null, msgs);
			msgs = basicSetSettingsfile(newSettingsfile, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE, newSettingsfile, newSettingsfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImagekeyencodingType getImagekeyencoding() {
		return imagekeyencoding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImagekeyencoding(ImagekeyencodingType newImagekeyencoding, NotificationChain msgs) {
		ImagekeyencodingType oldImagekeyencoding = imagekeyencoding;
		imagekeyencoding = newImagekeyencoding;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING, oldImagekeyencoding, newImagekeyencoding);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImagekeyencoding(ImagekeyencodingType newImagekeyencoding) {
		if (newImagekeyencoding != imagekeyencoding) {
			NotificationChain msgs = null;
			if (imagekeyencoding != null)
				msgs = ((InternalEObject)imagekeyencoding).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING, null, msgs);
			if (newImagekeyencoding != null)
				msgs = ((InternalEObject)newImagekeyencoding).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING, null, msgs);
			msgs = basicSetImagekeyencoding(newImagekeyencoding, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING, newImagekeyencoding, newImagekeyencoding));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ClusterType getCluster() {
		return cluster;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCluster(ClusterType newCluster, NotificationChain msgs) {
		ClusterType oldCluster = cluster;
		cluster = newCluster;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__CLUSTER, oldCluster, newCluster);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCluster(ClusterType newCluster) {
		if (newCluster != cluster) {
			NotificationChain msgs = null;
			if (cluster != null)
				msgs = ((InternalEObject)cluster).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__CLUSTER, null, msgs);
			if (newCluster != null)
				msgs = ((InternalEObject)newCluster).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LocalTomoPackage.TOMODO_TYPE__CLUSTER, null, msgs);
			msgs = basicSetCluster(newCluster, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__CLUSTER, newCluster, newCluster));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getSegmentsToRemoveRelativeToNexusForOutdir() {
		return segmentsToRemoveRelativeToNexusForOutdir;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSegmentsToRemoveRelativeToNexusForOutdir(int newSegmentsToRemoveRelativeToNexusForOutdir) {
		int oldSegmentsToRemoveRelativeToNexusForOutdir = segmentsToRemoveRelativeToNexusForOutdir;
		segmentsToRemoveRelativeToNexusForOutdir = newSegmentsToRemoveRelativeToNexusForOutdir;
		boolean oldSegmentsToRemoveRelativeToNexusForOutdirESet = segmentsToRemoveRelativeToNexusForOutdirESet;
		segmentsToRemoveRelativeToNexusForOutdirESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR, oldSegmentsToRemoveRelativeToNexusForOutdir, segmentsToRemoveRelativeToNexusForOutdir, !oldSegmentsToRemoveRelativeToNexusForOutdirESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetSegmentsToRemoveRelativeToNexusForOutdir() {
		int oldSegmentsToRemoveRelativeToNexusForOutdir = segmentsToRemoveRelativeToNexusForOutdir;
		boolean oldSegmentsToRemoveRelativeToNexusForOutdirESet = segmentsToRemoveRelativeToNexusForOutdirESet;
		segmentsToRemoveRelativeToNexusForOutdir = SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR_EDEFAULT;
		segmentsToRemoveRelativeToNexusForOutdirESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR, oldSegmentsToRemoveRelativeToNexusForOutdir, SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR_EDEFAULT, oldSegmentsToRemoveRelativeToNexusForOutdirESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSegmentsToRemoveRelativeToNexusForOutdir() {
		return segmentsToRemoveRelativeToNexusForOutdirESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case LocalTomoPackage.TOMODO_TYPE__SHUTTER:
				return basicSetShutter(null, msgs);
			case LocalTomoPackage.TOMODO_TYPE__TIFIMAGE:
				return basicSetTifimage(null, msgs);
			case LocalTomoPackage.TOMODO_TYPE__NEXUSFILE:
				return basicSetNexusfile(null, msgs);
			case LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE:
				return basicSetSettingsfile(null, msgs);
			case LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING:
				return basicSetImagekeyencoding(null, msgs);
			case LocalTomoPackage.TOMODO_TYPE__CLUSTER:
				return basicSetCluster(null, msgs);
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
			case LocalTomoPackage.TOMODO_TYPE__SHUTTER:
				return getShutter();
			case LocalTomoPackage.TOMODO_TYPE__TIFIMAGE:
				return getTifimage();
			case LocalTomoPackage.TOMODO_TYPE__NEXUSFILE:
				return getNexusfile();
			case LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE:
				return getSettingsfile();
			case LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING:
				return getImagekeyencoding();
			case LocalTomoPackage.TOMODO_TYPE__CLUSTER:
				return getCluster();
			case LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR:
				return getSegmentsToRemoveRelativeToNexusForOutdir();
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
			case LocalTomoPackage.TOMODO_TYPE__SHUTTER:
				setShutter((ShutterType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__TIFIMAGE:
				setTifimage((TifimageType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__NEXUSFILE:
				setNexusfile((NexusfileType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE:
				setSettingsfile((SettingsfileType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING:
				setImagekeyencoding((ImagekeyencodingType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__CLUSTER:
				setCluster((ClusterType)newValue);
				return;
			case LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR:
				setSegmentsToRemoveRelativeToNexusForOutdir((Integer)newValue);
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
			case LocalTomoPackage.TOMODO_TYPE__SHUTTER:
				setShutter((ShutterType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__TIFIMAGE:
				setTifimage((TifimageType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__NEXUSFILE:
				setNexusfile((NexusfileType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE:
				setSettingsfile((SettingsfileType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING:
				setImagekeyencoding((ImagekeyencodingType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__CLUSTER:
				setCluster((ClusterType)null);
				return;
			case LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR:
				unsetSegmentsToRemoveRelativeToNexusForOutdir();
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
			case LocalTomoPackage.TOMODO_TYPE__SHUTTER:
				return shutter != null;
			case LocalTomoPackage.TOMODO_TYPE__TIFIMAGE:
				return tifimage != null;
			case LocalTomoPackage.TOMODO_TYPE__NEXUSFILE:
				return nexusfile != null;
			case LocalTomoPackage.TOMODO_TYPE__SETTINGSFILE:
				return settingsfile != null;
			case LocalTomoPackage.TOMODO_TYPE__IMAGEKEYENCODING:
				return imagekeyencoding != null;
			case LocalTomoPackage.TOMODO_TYPE__CLUSTER:
				return cluster != null;
			case LocalTomoPackage.TOMODO_TYPE__SEGMENTS_TO_REMOVE_RELATIVE_TO_NEXUS_FOR_OUTDIR:
				return isSetSegmentsToRemoveRelativeToNexusForOutdir();
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
		result.append(" (segmentsToRemoveRelativeToNexusForOutdir: ");
		if (segmentsToRemoveRelativeToNexusForOutdirESet) result.append(segmentsToRemoveRelativeToNexusForOutdir); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //TomodoTypeImpl
