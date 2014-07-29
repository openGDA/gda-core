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
import org.opengda.lde.model.ldeexperiment.LdeexperimentPackage;
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

			addNamePropertyDescriptor(object);
			addSampleIDPropertyDescriptor(object);
			addStatusPropertyDescriptor(object);
			addActivePropertyDescriptor(object);
			addCellIDPropertyDescriptor(object);
			addVisitIDPropertyDescriptor(object);
			addEmailPropertyDescriptor(object);
			addCommandPropertyDescriptor(object);
			addCommentPropertyDescriptor(object);
			addMailCountPropertyDescriptor(object);
			addDataFileCountPropertyDescriptor(object);
			addDataFilePathPropertyDescriptor(object);
			addCalibrantPropertyDescriptor(object);
			addCalibrant_xPropertyDescriptor(object);
			addCalibrant_yPropertyDescriptor(object);
			addCalibrant_exposurePropertyDescriptor(object);
			addX_startPropertyDescriptor(object);
			addX_stopPropertyDescriptor(object);
			addX_stepPropertyDescriptor(object);
			addY_stopPropertyDescriptor(object);
			addY_stepPropertyDescriptor(object);
			addSample_exposurePropertyDescriptor(object);
			addDriverIDPropertyDescriptor(object);
			addPixium_xPropertyDescriptor(object);
			addPixium_yPropertyDescriptor(object);
			addPixium_zPropertyDescriptor(object);
			addY_startPropertyDescriptor(object);
			addStartDatePropertyDescriptor(object);
			addEndDatePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
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
				 LdeexperimentPackage.Literals.SAMPLE__NAME,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
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
				 LdeexperimentPackage.Literals.SAMPLE__SAMPLE_ID,
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
				 LdeexperimentPackage.Literals.SAMPLE__STATUS,
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
				 LdeexperimentPackage.Literals.SAMPLE__ACTIVE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
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
				 LdeexperimentPackage.Literals.SAMPLE__CELL_ID,
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
				 LdeexperimentPackage.Literals.SAMPLE__VISIT_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
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
				 LdeexperimentPackage.Literals.SAMPLE__EMAIL,
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
				 LdeexperimentPackage.Literals.SAMPLE__COMMAND,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
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
				 LdeexperimentPackage.Literals.SAMPLE__COMMENT,
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
				 LdeexperimentPackage.Literals.SAMPLE__START_DATE,
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
				 LdeexperimentPackage.Literals.SAMPLE__END_DATE,
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
				 LdeexperimentPackage.Literals.SAMPLE__MAIL_COUNT,
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
				 LdeexperimentPackage.Literals.SAMPLE__DATA_FILE_COUNT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
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
				 LdeexperimentPackage.Literals.SAMPLE__DATA_FILE_PATH,
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
				 LdeexperimentPackage.Literals.SAMPLE__CALIBRANT,
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
				 LdeexperimentPackage.Literals.SAMPLE__CALIBRANT_X,
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
				 LdeexperimentPackage.Literals.SAMPLE__CALIBRANT_Y,
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
				 LdeexperimentPackage.Literals.SAMPLE__CALIBRANT_EXPOSURE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Xstart feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addX_startPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_x_start_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_x_start_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__XSTART,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Xstop feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addX_stopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_x_stop_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_x_stop_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__XSTOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Xstep feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addX_stepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_x_step_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_x_step_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__XSTEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ystop feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addY_stopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_y_stop_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_y_stop_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__YSTOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ystep feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addY_stepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_y_step_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_y_step_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__YSTEP,
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
				 LdeexperimentPackage.Literals.SAMPLE__SAMPLE_EXPOSURE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Driver ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDriverIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_driverID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_driverID_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__DRIVER_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Pixium x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPixium_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_pixium_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_pixium_x_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__PIXIUM_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Pixium y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPixium_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_pixium_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_pixium_y_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__PIXIUM_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Pixium z feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPixium_zPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_pixium_z_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_pixium_z_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__PIXIUM_Z,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Ystart feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addY_startPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_y_start_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_y_start_feature", "_UI_Sample_type"),
				 LdeexperimentPackage.Literals.SAMPLE__YSTART,
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
			case LdeexperimentPackage.SAMPLE__NAME:
			case LdeexperimentPackage.SAMPLE__SAMPLE_ID:
			case LdeexperimentPackage.SAMPLE__STATUS:
			case LdeexperimentPackage.SAMPLE__ACTIVE:
			case LdeexperimentPackage.SAMPLE__CELL_ID:
			case LdeexperimentPackage.SAMPLE__VISIT_ID:
			case LdeexperimentPackage.SAMPLE__EMAIL:
			case LdeexperimentPackage.SAMPLE__COMMAND:
			case LdeexperimentPackage.SAMPLE__COMMENT:
			case LdeexperimentPackage.SAMPLE__MAIL_COUNT:
			case LdeexperimentPackage.SAMPLE__DATA_FILE_COUNT:
			case LdeexperimentPackage.SAMPLE__DATA_FILE_PATH:
			case LdeexperimentPackage.SAMPLE__CALIBRANT:
			case LdeexperimentPackage.SAMPLE__CALIBRANT_X:
			case LdeexperimentPackage.SAMPLE__CALIBRANT_Y:
			case LdeexperimentPackage.SAMPLE__CALIBRANT_EXPOSURE:
			case LdeexperimentPackage.SAMPLE__XSTART:
			case LdeexperimentPackage.SAMPLE__XSTOP:
			case LdeexperimentPackage.SAMPLE__XSTEP:
			case LdeexperimentPackage.SAMPLE__YSTOP:
			case LdeexperimentPackage.SAMPLE__YSTEP:
			case LdeexperimentPackage.SAMPLE__SAMPLE_EXPOSURE:
			case LdeexperimentPackage.SAMPLE__DRIVER_ID:
			case LdeexperimentPackage.SAMPLE__PIXIUM_X:
			case LdeexperimentPackage.SAMPLE__PIXIUM_Y:
			case LdeexperimentPackage.SAMPLE__PIXIUM_Z:
			case LdeexperimentPackage.SAMPLE__YSTART:
			case LdeexperimentPackage.SAMPLE__START_DATE:
			case LdeexperimentPackage.SAMPLE__END_DATE:
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
