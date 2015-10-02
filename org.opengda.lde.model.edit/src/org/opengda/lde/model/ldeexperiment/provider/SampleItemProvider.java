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
import org.eclipse.emf.edit.provider.ITableItemLabelProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.opengda.lde.model.edit.ImageConstants;
import org.opengda.lde.model.edit.SampleTableConstants;
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
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
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

			addStatusPropertyDescriptor(object);
			addActivePropertyDescriptor(object);
			addNamePropertyDescriptor(object);
			addSampleIDPropertyDescriptor(object);
			addSample_x_startPropertyDescriptor(object);
			addSample_x_stopPropertyDescriptor(object);
			addSample_x_stepPropertyDescriptor(object);
			addSample_y_startPropertyDescriptor(object);
			addSample_y_stopPropertyDescriptor(object);
			addSample_y_stepPropertyDescriptor(object);
			addSample_exposurePropertyDescriptor(object);
			addCommandPropertyDescriptor(object);
			addCommentPropertyDescriptor(object);
			addCalibrationFilePathPropertyDescriptor(object);
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
	 * This adds a property descriptor for the Calibration File Path feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrationFilePathPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sample_calibrationFilePath_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Sample_calibrationFilePath_feature", "_UI_Sample_type"),
				 LDEExperimentsPackage.Literals.SAMPLE__CALIBRATION_FILE_PATH,
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
			case LDEExperimentsPackage.SAMPLE__STATUS:
			case LDEExperimentsPackage.SAMPLE__ACTIVE:
			case LDEExperimentsPackage.SAMPLE__NAME:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_ID:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTART:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTOP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_XSTEP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTART:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTOP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_YSTEP:
			case LDEExperimentsPackage.SAMPLE__SAMPLE_EXPOSURE:
			case LDEExperimentsPackage.SAMPLE__COMMAND:
			case LDEExperimentsPackage.SAMPLE__COMMENT:
			case LDEExperimentsPackage.SAMPLE__CALIBRATION_FILE_PATH:
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
	
	//support for display ALL samples in a TableViewer for tracking data collection progress when Sample node is selected even though Sample node does not have children.
	@Override
	public Object getColumnImage(Object element, int columnIndex) {
		if (element instanceof Sample) {
			Sample sample = (Sample) element;
			if (columnIndex == SampleTableConstants.COL_ACTIVE) {
				if (sample.isActive()) {
					return getResourceLocator().getImage(ImageConstants.ICON_CHECKED_STATE);
				} else {
					return getResourceLocator().getImage(ImageConstants.ICON_UNCHECKED_STATE);
				}
			} 
		}
		return super.getColumnImage(element, columnIndex);
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Sample) {
			Sample sample = (Sample) element;
			switch (columnIndex) {
			case SampleTableConstants.COL_ACTIVE:
				return "";
			case SampleTableConstants.COL_SAMPLE_NAME:
				return sample.getName();
			case SampleTableConstants.COL_SAMPLE_X_START:
				return String.valueOf(sample.getSample_x_start());
			case SampleTableConstants.COL_SAMPLE_X_STOP:
				return String.valueOf(sample.getSample_x_stop());
			case SampleTableConstants.COL_SAMPLE_X_STEP:
				return String.valueOf(sample.getSample_x_step());
			case SampleTableConstants.COL_SAMPLE_Y_START:
				return String.valueOf(sample.getSample_y_start());
			case SampleTableConstants.COL_SAMPLE_Y_STOP:
				return String.valueOf(sample.getSample_y_stop());
			case SampleTableConstants.COL_SAMPLE_Y_STEP:
				return String.valueOf(sample.getSample_y_step());
			case SampleTableConstants.COL_SAMPLE_EXPOSURE:
				return String.valueOf(sample.getSample_exposure());
			case SampleTableConstants.COL_COMMAND:
				return sample.getCommand();
			case SampleTableConstants.COL_COMMENT:
				return sample.getComment();
			default:
				break;
			}
		}
		return super.getColumnText(element, columnIndex);
	}


}
