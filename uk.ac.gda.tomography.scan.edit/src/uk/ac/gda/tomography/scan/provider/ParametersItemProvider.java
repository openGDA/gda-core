/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.scan.provider;

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

import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * This is the item provider adapter for a {@link uk.ac.gda.tomography.scan.Parameters} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ParametersItemProvider extends ItemProviderAdapter implements
		IEditingDomainItemProvider, IStructuredItemContentProvider,
		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ParametersItemProvider(AdapterFactory adapterFactory) {
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

			addInBeamPositionPropertyDescriptor(object);
			addOutOfBeamPositionPropertyDescriptor(object);
			addExposureTimePropertyDescriptor(object);
			addStartPropertyDescriptor(object);
			addStopPropertyDescriptor(object);
			addStepPropertyDescriptor(object);
			addDarkFieldIntervalPropertyDescriptor(object);
			addFlatFieldIntervalPropertyDescriptor(object);
			addImagesPerDarkPropertyDescriptor(object);
			addImagesPerFlatPropertyDescriptor(object);
			addMinIPropertyDescriptor(object);
			addTitlePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the In Beam Position feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addInBeamPositionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_inBeamPosition_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_inBeamPosition_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__IN_BEAM_POSITION, true, false,
				false, ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Out Of Beam Position feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOutOfBeamPositionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_outOfBeamPosition_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_outOfBeamPosition_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__OUT_OF_BEAM_POSITION, true,
				false, false, ItemPropertyDescriptor.REAL_VALUE_IMAGE, null,
				null));
	}

	/**
	 * This adds a property descriptor for the Exposure Time feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExposureTimePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_exposureTime_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_exposureTime_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__EXPOSURE_TIME, true, false,
				false, ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Start feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStartPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_start_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_start_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__START, true, false, false,
				ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Stop feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_stop_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_stop_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__STOP, true, false, false,
				ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Step feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_step_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_step_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__STEP, true, false, false,
				ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Dark Field Interval feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDarkFieldIntervalPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_darkFieldInterval_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_darkFieldInterval_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__DARK_FIELD_INTERVAL, true,
				false, false, ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				null, null));
	}

	/**
	 * This adds a property descriptor for the Flat Field Interval feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFlatFieldIntervalPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_flatFieldInterval_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_flatFieldInterval_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__FLAT_FIELD_INTERVAL, true,
				false, false, ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				null, null));
	}

	/**
	 * This adds a property descriptor for the Images Per Dark feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addImagesPerDarkPropertyDescriptor(Object object) {
		itemPropertyDescriptors
				.add(createItemPropertyDescriptor(
						((ComposeableAdapterFactory) adapterFactory)
								.getRootAdapterFactory(),
						getResourceLocator(),
						getString("_UI_Parameters_imagesPerDark_feature"), //$NON-NLS-1$
						getString("_UI_Parameters_imagesPerDark_description"), //$NON-NLS-1$
						ScanPackage.Literals.PARAMETERS__IMAGES_PER_DARK, true,
						false, false,
						ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Images Per Flat feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addImagesPerFlatPropertyDescriptor(Object object) {
		itemPropertyDescriptors
				.add(createItemPropertyDescriptor(
						((ComposeableAdapterFactory) adapterFactory)
								.getRootAdapterFactory(),
						getResourceLocator(),
						getString("_UI_Parameters_imagesPerFlat_feature"), //$NON-NLS-1$
						getString("_UI_Parameters_imagesPerFlat_description"), //$NON-NLS-1$
						ScanPackage.Literals.PARAMETERS__IMAGES_PER_FLAT, true,
						false, false,
						ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Min I feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addMinIPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_minI_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_minI_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__MIN_I, true, false, false,
				ItemPropertyDescriptor.REAL_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Title feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTitlePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory)
						.getRootAdapterFactory(),
				getResourceLocator(),
				getString("_UI_Parameters_title_feature"), //$NON-NLS-1$
				getString("_UI_Parameters_title_description"), //$NON-NLS-1$
				ScanPackage.Literals.PARAMETERS__TITLE, true, false, false,
				ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This returns Parameters.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object,
				getResourceLocator().getImage("full/obj16/Parameters")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		Parameters parameters = (Parameters) object;
		return getString("_UI_Parameters_type") + " " + parameters.getInBeamPosition(); //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(Parameters.class)) {
		case ScanPackage.PARAMETERS__IN_BEAM_POSITION:
		case ScanPackage.PARAMETERS__OUT_OF_BEAM_POSITION:
		case ScanPackage.PARAMETERS__EXPOSURE_TIME:
		case ScanPackage.PARAMETERS__START:
		case ScanPackage.PARAMETERS__STOP:
		case ScanPackage.PARAMETERS__STEP:
		case ScanPackage.PARAMETERS__DARK_FIELD_INTERVAL:
		case ScanPackage.PARAMETERS__FLAT_FIELD_INTERVAL:
		case ScanPackage.PARAMETERS__IMAGES_PER_DARK:
		case ScanPackage.PARAMETERS__IMAGES_PER_FLAT:
		case ScanPackage.PARAMETERS__MIN_I:
		case ScanPackage.PARAMETERS__TITLE:
			fireNotifyChanged(new ViewerNotification(notification,
					notification.getNotifier(), false, true));
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
	protected void collectNewChildDescriptors(
			Collection<Object> newChildDescriptors, Object object) {
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
		return ScanEditPlugin.INSTANCE;
	}

}
