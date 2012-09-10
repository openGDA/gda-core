/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EStructuralFeature;

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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmFactory;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;

/**
 * This is the item provider adapter for a {@link uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class FBPTypeItemProvider
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
	public FBPTypeItemProvider(AdapterFactory adapterFactory) {
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

			addGPUDeviceNumberPropertyDescriptor(object);
			addLogFilePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the GPU Device Number feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addGPUDeviceNumberPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_FBPType_gPUDeviceNumber_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_FBPType_gPUDeviceNumber_feature", "_UI_FBPType_type"),
				 HmPackage.Literals.FBP_TYPE__GPU_DEVICE_NUMBER,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Log File feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLogFilePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_FBPType_logFile_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_FBPType_logFile_feature", "_UI_FBPType_type"),
				 HmPackage.Literals.FBP_TYPE__LOG_FILE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__DEFAULT_XML);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__BEAMLINE_USER);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__INPUT_DATA);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__FLAT_DARK_FIELDS);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__PREPROCESSING);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__TRANSFORM);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__BACKPROJECTION);
			childrenFeatures.add(HmPackage.Literals.FBP_TYPE__OUTPUT_DATA);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns FBPType.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/FBPType"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		FBPType fbpType = (FBPType)object;
		return getString("_UI_FBPType_type") + " " + fbpType.getGPUDeviceNumber();
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

		switch (notification.getFeatureID(FBPType.class)) {
			case HmPackage.FBP_TYPE__GPU_DEVICE_NUMBER:
			case HmPackage.FBP_TYPE__LOG_FILE:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case HmPackage.FBP_TYPE__DEFAULT_XML:
			case HmPackage.FBP_TYPE__BEAMLINE_USER:
			case HmPackage.FBP_TYPE__INPUT_DATA:
			case HmPackage.FBP_TYPE__FLAT_DARK_FIELDS:
			case HmPackage.FBP_TYPE__PREPROCESSING:
			case HmPackage.FBP_TYPE__TRANSFORM:
			case HmPackage.FBP_TYPE__BACKPROJECTION:
			case HmPackage.FBP_TYPE__OUTPUT_DATA:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
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

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__DEFAULT_XML,
				 HmFactory.eINSTANCE.createDefaultXmlType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__BEAMLINE_USER,
				 HmFactory.eINSTANCE.createBeamlineUserType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__INPUT_DATA,
				 HmFactory.eINSTANCE.createInputDataType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__FLAT_DARK_FIELDS,
				 HmFactory.eINSTANCE.createFlatDarkFieldsType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__PREPROCESSING,
				 HmFactory.eINSTANCE.createPreprocessingType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__TRANSFORM,
				 HmFactory.eINSTANCE.createTransformType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__BACKPROJECTION,
				 HmFactory.eINSTANCE.createBackprojectionType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.FBP_TYPE__OUTPUT_DATA,
				 HmFactory.eINSTANCE.createOutputDataType()));
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return HmEditPlugin.INSTANCE;
	}

}
