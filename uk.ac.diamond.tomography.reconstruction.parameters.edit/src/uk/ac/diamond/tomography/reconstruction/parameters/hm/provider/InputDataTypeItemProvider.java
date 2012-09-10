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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmFactory;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType;

/**
 * This is the item provider adapter for a {@link uk.ac.diamond.tomography.reconstruction.parameters.hm.InputDataType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class InputDataTypeItemProvider
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
	public InputDataTypeItemProvider(AdapterFactory adapterFactory) {
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

			addFolderPropertyDescriptor(object);
			addPrefixPropertyDescriptor(object);
			addSuffixPropertyDescriptor(object);
			addExtensionPropertyDescriptor(object);
			addFileFirstPropertyDescriptor(object);
			addFileLastPropertyDescriptor(object);
			addFileStepPropertyDescriptor(object);
			addImagesPerFilePropertyDescriptor(object);
			addValueMinPropertyDescriptor(object);
			addValueMaxPropertyDescriptor(object);
			addPixelParamPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Folder feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFolderPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_folder_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_folder_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__FOLDER,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Prefix feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPrefixPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_prefix_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_prefix_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__PREFIX,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Suffix feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSuffixPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_suffix_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_suffix_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__SUFFIX,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Extension feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExtensionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_extension_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_extension_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__EXTENSION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the File First feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFileFirstPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_fileFirst_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_fileFirst_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__FILE_FIRST,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the File Last feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFileLastPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_fileLast_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_fileLast_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__FILE_LAST,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the File Step feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFileStepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_fileStep_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_fileStep_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__FILE_STEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Images Per File feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addImagesPerFilePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_imagesPerFile_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_imagesPerFile_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__IMAGES_PER_FILE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Value Min feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addValueMinPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_valueMin_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_valueMin_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__VALUE_MIN,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Value Max feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addValueMaxPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_valueMax_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_valueMax_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__VALUE_MAX,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Pixel Param feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPixelParamPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_InputDataType_pixelParam_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_InputDataType_pixelParam_feature", "_UI_InputDataType_type"),
				 HmPackage.Literals.INPUT_DATA_TYPE__PIXEL_PARAM,
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
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__NOD);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__MEMORY_SIZE_MAX);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__MEMORY_SIZE_MIN);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__ORIENTATION);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_FIRST);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_LAST);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_STEP);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__RAW);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__RESTRICTIONS);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__TYPE);
			childrenFeatures.add(HmPackage.Literals.INPUT_DATA_TYPE__SHAPE);
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
	 * This returns InputDataType.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/InputDataType"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((InputDataType)object).getFolder();
		return label == null || label.length() == 0 ?
			getString("_UI_InputDataType_type") :
			getString("_UI_InputDataType_type") + " " + label;
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

		switch (notification.getFeatureID(InputDataType.class)) {
			case HmPackage.INPUT_DATA_TYPE__FOLDER:
			case HmPackage.INPUT_DATA_TYPE__PREFIX:
			case HmPackage.INPUT_DATA_TYPE__SUFFIX:
			case HmPackage.INPUT_DATA_TYPE__EXTENSION:
			case HmPackage.INPUT_DATA_TYPE__FILE_FIRST:
			case HmPackage.INPUT_DATA_TYPE__FILE_LAST:
			case HmPackage.INPUT_DATA_TYPE__FILE_STEP:
			case HmPackage.INPUT_DATA_TYPE__IMAGES_PER_FILE:
			case HmPackage.INPUT_DATA_TYPE__VALUE_MIN:
			case HmPackage.INPUT_DATA_TYPE__VALUE_MAX:
			case HmPackage.INPUT_DATA_TYPE__PIXEL_PARAM:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case HmPackage.INPUT_DATA_TYPE__NOD:
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MAX:
			case HmPackage.INPUT_DATA_TYPE__MEMORY_SIZE_MIN:
			case HmPackage.INPUT_DATA_TYPE__ORIENTATION:
			case HmPackage.INPUT_DATA_TYPE__IMAGE_FIRST:
			case HmPackage.INPUT_DATA_TYPE__IMAGE_LAST:
			case HmPackage.INPUT_DATA_TYPE__IMAGE_STEP:
			case HmPackage.INPUT_DATA_TYPE__RAW:
			case HmPackage.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX:
			case HmPackage.INPUT_DATA_TYPE__RESTRICTIONS:
			case HmPackage.INPUT_DATA_TYPE__TYPE:
			case HmPackage.INPUT_DATA_TYPE__SHAPE:
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
				(HmPackage.Literals.INPUT_DATA_TYPE__NOD,
				 HmFactory.eINSTANCE.createNODType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__MEMORY_SIZE_MAX,
				 HmFactory.eINSTANCE.createMemorySizeMaxType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__MEMORY_SIZE_MIN,
				 HmFactory.eINSTANCE.createMemorySizeMinType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__ORIENTATION,
				 HmFactory.eINSTANCE.createOrientationType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_FIRST,
				 HmFactory.eINSTANCE.createImageFirstType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_LAST,
				 HmFactory.eINSTANCE.createImageLastType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__IMAGE_STEP,
				 HmFactory.eINSTANCE.createImageStepType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__RAW,
				 HmFactory.eINSTANCE.createRawType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__FIRST_IMAGE_INDEX,
				 HmFactory.eINSTANCE.createFirstImageIndexType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__RESTRICTIONS,
				 HmFactory.eINSTANCE.createRestrictionsType1()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__TYPE,
				 HmFactory.eINSTANCE.createTypeType14()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.INPUT_DATA_TYPE__SHAPE,
				 HmFactory.eINSTANCE.createShapeType1()));
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
