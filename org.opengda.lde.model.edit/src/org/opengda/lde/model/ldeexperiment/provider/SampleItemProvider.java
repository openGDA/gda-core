/**
 */
package org.opengda.lde.model.ldeexperiment.provider;


import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Sample;

/**
 * This is the item provider adapter for a {@link org.opengda.lde.model.ldeexperiment.Sample} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SampleItemProvider 
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider,
		IStructuredItemContentProvider,
		ITreeItemContentProvider,
		IItemLabelProvider,
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SampleItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addSampleIDPropertyDescriptor(object);
			addStatusPropertyDescriptor(object);
			addActivePropertyDescriptor(object);
			addNamePropertyDescriptor(object);
			addCellIDPropertyDescriptor(object);
			addVisitIDPropertyDescriptor(object);
			addCalibrantPropertyDescriptor(object);
			addCalibrant_xPropertyDescriptor(object);
			addCalibrant_yPropertyDescriptor(object);
			addCalibrant_exposurePropertyDescriptor(object);
			addSample_x_startPropertyDescriptor(object);
			addSample_x_stopPropertyDescriptor(object);
			addSample_x_stepPropertyDescriptor(object);
			addSample_y_startPropertyDescriptor(object);
			addSample_y_stopPropertyDescriptor(object);
			addSample_y_stepPropertyDescriptor(object);
			addSample_exposurePropertyDescriptor(object);
			addDetector_xPropertyDescriptor(object);
			addDetector_yPropertyDescriptor(object);
			addDetector_zPropertyDescriptor(object);
			addEmailPropertyDescriptor(object);
			addStartDatePropertyDescriptor(object);
			addEndDatePropertyDescriptor(object);
			addCommandPropertyDescriptor(object);
			addDriveIDPropertyDescriptor(object);
			addMailCountPropertyDescriptor(object);
			addDataFileCountPropertyDescriptor(object);
			addCommentPropertyDescriptor(object);
			addDataFilePathPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Sample ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSampleIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sampleID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sampleID_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Status feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStatusPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_status_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_status_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__STATUS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Active feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addActivePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_active_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_active_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__ACTIVE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Name feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNamePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_name_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_name_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__NAME,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Cell ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCellIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_cellID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_cellID_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CELL_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Visit ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVisitIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_visitID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_visitID_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__VISIT_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrantPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_calibrant_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_calibrant_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CALIBRANT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_calibrant_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_calibrant_x_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CALIBRANT_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_calibrant_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_calibrant_y_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CALIBRANT_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant exposure feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_exposurePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_calibrant_exposure_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_calibrant_exposure_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CALIBRANT_EXPOSURE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample xstart feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_x_startPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_x_start_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_x_start_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_XSTART,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample xstop feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_x_stopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_x_stop_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_x_stop_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_XSTOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample xstep feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_x_stepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_x_step_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_x_step_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_XSTEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample ystart feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_y_startPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_y_start_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_y_start_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_YSTART,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample ystop feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_y_stopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_y_stop_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_y_stop_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_YSTOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample ystep feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_y_stepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_y_step_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_y_step_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_YSTEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Sample exposure feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSample_exposurePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_sample_exposure_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_sample_exposure_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__SAMPLE_EXPOSURE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_detector_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_detector_x_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DETECTOR_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_detector_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_detector_y_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DETECTOR_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector z feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_zPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_detector_z_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_detector_z_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DETECTOR_Z,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Email feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEmailPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_email_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_email_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__EMAIL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Start Date feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStartDatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_startDate_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_startDate_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__START_DATE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the End Date feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEndDatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_endDate_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_endDate_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__END_DATE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Command feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCommandPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_command_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_command_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__COMMAND,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Drive ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDriveIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_driveID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_driveID_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DRIVE_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Mail Count feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addMailCountPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_mailCount_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_mailCount_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__MAIL_COUNT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Data File Count feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDataFileCountPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_dataFileCount_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_dataFileCount_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DATA_FILE_COUNT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Comment feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCommentPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_comment_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_comment_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__COMMENT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Data File Path feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDataFilePathPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_dataFilePath_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_dataFilePath_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__DATA_FILE_PATH,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns Sample.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Sample"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Sample)object).getName();
		return label == null || label.length() == 0 ?
			getString("_UI_Sample_type") :
			getString("_UI_Sample_type") + " " + label;
	}
	

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(Sample.class)) {
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
			case LDEExperimentsPackage.SAMPLE__STATUS:
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
			case LDEExperimentsPackage.SAMPLE__NAME:
			case LDEExperimentsPackage.SAMPLE__CELL_ID:
			case LDEExperimentsPackage.SAMPLE__VISIT_ID:
			case LDEExperimentsPackage.SAMPLE__CALIBRANT:
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_X:
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_Y:
			case LDEExperimentsPackage.SAMPLE__CALIBRANT_EXPOSURE:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
			case LDEExperimentsPackage.SAMPLE__DETECTOR_X:
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Y:
			case LDEExperimentsPackage.SAMPLE__DETECTOR_Z:
			case LDEExperimentsPackage.SAMPLE__EMAIL:
			case LDEExperimentsPackage.SAMPLE__START_DATE:
			case LDEExperimentsPackage.SAMPLE__END_DATE:
			case LDEExperimentsPackage.SAMPLE__COMMAND:
			case LDEExperimentsPackage.SAMPLE__DRIVE_ID:
			case LDEExperimentsPackage.SAMPLE__MAIL_COUNT:
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_COUNT:
			case LDEExperimentsPackage.SAMPLE__COMMENT:
			case LDEExperimentsPackage.SAMPLE__DATA_FILE_PATH:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return SampledefinitionEditPlugin.INSTANCE;
	}

}
