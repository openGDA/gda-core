/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.provider;


import java.math.BigDecimal;

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

import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmFactory;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;

/**
 * This is the item provider adapter for a {@link uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class BackprojectionTypeItemProvider
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
	public BackprojectionTypeItemProvider(AdapterFactory adapterFactory) {
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

			addImageCentrePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Image Centre feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addImageCentrePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_BackprojectionType_imageCentre_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_BackprojectionType_imageCentre_feature", "_UI_BackprojectionType_type"),
				 HmPackage.Literals.BACKPROJECTION_TYPE__IMAGE_CENTRE,
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
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__FILTER);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__TILT);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__COORDINATE_SYSTEM);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__CIRCLES);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__ROI);
			childrenFeatures.add(HmPackage.Literals.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION);
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
	 * This returns BackprojectionType.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/BackprojectionType"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		BigDecimal labelValue = ((BackprojectionType)object).getImageCentre();
		String label = labelValue == null ? null : labelValue.toString();
		return label == null || label.length() == 0 ?
			getString("_UI_BackprojectionType_type") :
			getString("_UI_BackprojectionType_type") + " " + label;
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

		switch (notification.getFeatureID(BackprojectionType.class)) {
			case HmPackage.BACKPROJECTION_TYPE__IMAGE_CENTRE:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case HmPackage.BACKPROJECTION_TYPE__FILTER:
			case HmPackage.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION:
			case HmPackage.BACKPROJECTION_TYPE__TILT:
			case HmPackage.BACKPROJECTION_TYPE__COORDINATE_SYSTEM:
			case HmPackage.BACKPROJECTION_TYPE__CIRCLES:
			case HmPackage.BACKPROJECTION_TYPE__ROI:
			case HmPackage.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION:
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
				(HmPackage.Literals.BACKPROJECTION_TYPE__FILTER,
				 HmFactory.eINSTANCE.createFilterType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__CLOCKWISE_ROTATION,
				 HmFactory.eINSTANCE.createClockwiseRotationType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__TILT,
				 HmFactory.eINSTANCE.createTiltType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__COORDINATE_SYSTEM,
				 HmFactory.eINSTANCE.createCoordinateSystemType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__CIRCLES,
				 HmFactory.eINSTANCE.createCirclesType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__ROI,
				 HmFactory.eINSTANCE.createROIType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.BACKPROJECTION_TYPE__POLAR_CARTESIAN_INTERPOLATION,
				 HmFactory.eINSTANCE.createPolarCartesianInterpolationType()));
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
