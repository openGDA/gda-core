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
import uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType;

/**
 * This is the item provider adapter for a {@link uk.ac.diamond.tomography.reconstruction.parameters.hm.OutputDataType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class OutputDataTypeItemProvider
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
	public OutputDataTypeItemProvider(AdapterFactory adapterFactory) {
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
			addNODPropertyDescriptor(object);
			addFileFirstPropertyDescriptor(object);
			addFileStepPropertyDescriptor(object);
			addBitsPropertyDescriptor(object);
			addValueMinPropertyDescriptor(object);
			addValueMaxPropertyDescriptor(object);
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
				 getString("_UI_OutputDataType_folder_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_folder_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__FOLDER,
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
				 getString("_UI_OutputDataType_prefix_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_prefix_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__PREFIX,
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
				 getString("_UI_OutputDataType_suffix_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_suffix_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__SUFFIX,
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
				 getString("_UI_OutputDataType_extension_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_extension_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__EXTENSION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the NOD feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNODPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_OutputDataType_nOD_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_nOD_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__NOD,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
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
				 getString("_UI_OutputDataType_fileFirst_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_fileFirst_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__FILE_FIRST,
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
				 getString("_UI_OutputDataType_fileStep_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_fileStep_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__FILE_STEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Bits feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBitsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_OutputDataType_bits_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_bits_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__BITS,
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
				 getString("_UI_OutputDataType_valueMin_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_valueMin_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__VALUE_MIN,
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
				 getString("_UI_OutputDataType_valueMax_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_OutputDataType_valueMax_feature", "_UI_OutputDataType_type"),
				 HmPackage.Literals.OUTPUT_DATA_TYPE__VALUE_MAX,
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
			childrenFeatures.add(HmPackage.Literals.OUTPUT_DATA_TYPE__TYPE);
			childrenFeatures.add(HmPackage.Literals.OUTPUT_DATA_TYPE__STATE);
			childrenFeatures.add(HmPackage.Literals.OUTPUT_DATA_TYPE__BITS_TYPE);
			childrenFeatures.add(HmPackage.Literals.OUTPUT_DATA_TYPE__RESTRICTIONS);
			childrenFeatures.add(HmPackage.Literals.OUTPUT_DATA_TYPE__SHAPE);
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
	 * This returns OutputDataType.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/OutputDataType"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((OutputDataType)object).getFolder();
		return label == null || label.length() == 0 ?
			getString("_UI_OutputDataType_type") :
			getString("_UI_OutputDataType_type") + " " + label;
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

		switch (notification.getFeatureID(OutputDataType.class)) {
			case HmPackage.OUTPUT_DATA_TYPE__FOLDER:
			case HmPackage.OUTPUT_DATA_TYPE__PREFIX:
			case HmPackage.OUTPUT_DATA_TYPE__SUFFIX:
			case HmPackage.OUTPUT_DATA_TYPE__EXTENSION:
			case HmPackage.OUTPUT_DATA_TYPE__NOD:
			case HmPackage.OUTPUT_DATA_TYPE__FILE_FIRST:
			case HmPackage.OUTPUT_DATA_TYPE__FILE_STEP:
			case HmPackage.OUTPUT_DATA_TYPE__BITS:
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MIN:
			case HmPackage.OUTPUT_DATA_TYPE__VALUE_MAX:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case HmPackage.OUTPUT_DATA_TYPE__TYPE:
			case HmPackage.OUTPUT_DATA_TYPE__STATE:
			case HmPackage.OUTPUT_DATA_TYPE__BITS_TYPE:
			case HmPackage.OUTPUT_DATA_TYPE__RESTRICTIONS:
			case HmPackage.OUTPUT_DATA_TYPE__SHAPE:
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
				(HmPackage.Literals.OUTPUT_DATA_TYPE__TYPE,
				 HmFactory.eINSTANCE.createTypeType2()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.OUTPUT_DATA_TYPE__STATE,
				 HmFactory.eINSTANCE.createStateType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.OUTPUT_DATA_TYPE__BITS_TYPE,
				 HmFactory.eINSTANCE.createBitsTypeType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.OUTPUT_DATA_TYPE__RESTRICTIONS,
				 HmFactory.eINSTANCE.createRestrictionsType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.OUTPUT_DATA_TYPE__SHAPE,
				 HmFactory.eINSTANCE.createShapeType()));
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
