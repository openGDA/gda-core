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
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType;

/**
 * This is the item provider adapter for a {@link uk.ac.diamond.tomography.reconstruction.parameters.hm.TransformType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class TransformTypeItemProvider
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
	public TransformTypeItemProvider(AdapterFactory adapterFactory) {
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

			addRotationAnglePropertyDescriptor(object);
			addReCentreAnglePropertyDescriptor(object);
			addReCentreRadiusPropertyDescriptor(object);
			addCropTopPropertyDescriptor(object);
			addCropBottomPropertyDescriptor(object);
			addCropLeftPropertyDescriptor(object);
			addCropRightPropertyDescriptor(object);
			addScaleWidthPropertyDescriptor(object);
			addScaleHeightPropertyDescriptor(object);
			addExtrapolationPixelsPropertyDescriptor(object);
			addExtrapolationWidthPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Rotation Angle feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRotationAnglePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_rotationAngle_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_rotationAngle_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__ROTATION_ANGLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Re Centre Angle feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addReCentreAnglePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_reCentreAngle_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_reCentreAngle_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__RE_CENTRE_ANGLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Re Centre Radius feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addReCentreRadiusPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_reCentreRadius_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_reCentreRadius_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__RE_CENTRE_RADIUS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Crop Top feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCropTopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_cropTop_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_cropTop_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__CROP_TOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Crop Bottom feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCropBottomPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_cropBottom_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_cropBottom_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__CROP_BOTTOM,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Crop Left feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCropLeftPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_cropLeft_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_cropLeft_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__CROP_LEFT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Crop Right feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCropRightPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_cropRight_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_cropRight_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__CROP_RIGHT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Scale Width feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addScaleWidthPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_scaleWidth_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_scaleWidth_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__SCALE_WIDTH,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Scale Height feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addScaleHeightPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_scaleHeight_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_scaleHeight_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__SCALE_HEIGHT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Extrapolation Pixels feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExtrapolationPixelsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_extrapolationPixels_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_extrapolationPixels_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Extrapolation Width feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExtrapolationWidthPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_TransformType_extrapolationWidth_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_TransformType_extrapolationWidth_feature", "_UI_TransformType_type"),
				 HmPackage.Literals.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
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
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__MISSED_PROJECTIONS);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__SCALE_TYPE);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__EXTRAPOLATION_TYPE);
			childrenFeatures.add(HmPackage.Literals.TRANSFORM_TYPE__INTERPOLATION);
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
	 * This returns TransformType.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/TransformType"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		TransformType transformType = (TransformType)object;
		return getString("_UI_TransformType_type") + " " + transformType.getRotationAngle();
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

		switch (notification.getFeatureID(TransformType.class)) {
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE:
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_ANGLE:
			case HmPackage.TRANSFORM_TYPE__RE_CENTRE_RADIUS:
			case HmPackage.TRANSFORM_TYPE__CROP_TOP:
			case HmPackage.TRANSFORM_TYPE__CROP_BOTTOM:
			case HmPackage.TRANSFORM_TYPE__CROP_LEFT:
			case HmPackage.TRANSFORM_TYPE__CROP_RIGHT:
			case HmPackage.TRANSFORM_TYPE__SCALE_WIDTH:
			case HmPackage.TRANSFORM_TYPE__SCALE_HEIGHT:
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_PIXELS:
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_WIDTH:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS:
			case HmPackage.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE:
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE:
			case HmPackage.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS:
			case HmPackage.TRANSFORM_TYPE__SCALE_TYPE:
			case HmPackage.TRANSFORM_TYPE__EXTRAPOLATION_TYPE:
			case HmPackage.TRANSFORM_TYPE__INTERPOLATION:
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
				(HmPackage.Literals.TRANSFORM_TYPE__MISSED_PROJECTIONS,
				 HmFactory.eINSTANCE.createMissedProjectionsType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__MISSED_PROJECTIONS_TYPE,
				 HmFactory.eINSTANCE.createMissedProjectionsTypeType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__ROTATION_ANGLE_TYPE,
				 HmFactory.eINSTANCE.createRotationAngleTypeType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__ROTATION_ANGLE_END_POINTS,
				 HmFactory.eINSTANCE.createRotationAngleEndPointsType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__SCALE_TYPE,
				 HmFactory.eINSTANCE.createScaleTypeType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__EXTRAPOLATION_TYPE,
				 HmFactory.eINSTANCE.createExtrapolationTypeType()));

		newChildDescriptors.add
			(createChildParameter
				(HmPackage.Literals.TRANSFORM_TYPE__INTERPOLATION,
				 HmFactory.eINSTANCE.createInterpolationType()));
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
