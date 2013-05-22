/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.provider;


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
import org.eclipse.emf.edit.provider.ITableItemLabelProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;

/**
 * This is the item provider adapter for a {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SequenceItemProvider
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SequenceItemProvider(AdapterFactory adapterFactory) {
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

			addFilenamePropertyDescriptor(object);
			addRunModePropertyDescriptor(object);
			addRunModeIndexPropertyDescriptor(object);
			addNumIterationsPropertyDescriptor(object);
			addNumInterationOptionPropertyDescriptor(object);
			addRepeatUntilStoppedPropertyDescriptor(object);
			addConfirmAfterEachIterationPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Run Mode feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRunModePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_runMode_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_runMode_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__RUN_MODE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Run Mode Index feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRunModeIndexPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_runModeIndex_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_runModeIndex_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__RUN_MODE_INDEX,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Num Iterations feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNumIterationsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_numIterations_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_numIterations_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__NUM_ITERATIONS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Num Interation Option feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNumInterationOptionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_numInterationOption_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_numInterationOption_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__NUM_INTERATION_OPTION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Repeat Until Stopped feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRepeatUntilStoppedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_repeatUntilStopped_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_repeatUntilStopped_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__REPEAT_UNTIL_STOPPED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Confirm After Each Iteration feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addConfirmAfterEachIterationPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_confirmAfterEachIteration_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_confirmAfterEachIteration_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Filename feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFilenamePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Sequence_filename_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Sequence_filename_feature", "_UI_Sequence_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.SEQUENCE__FILENAME,
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
			childrenFeatures.add(RegiondefinitionPackage.Literals.SEQUENCE__REGION);
			childrenFeatures.add(RegiondefinitionPackage.Literals.SEQUENCE__SPECTRUM);
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
	 * This returns Sequence.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Sequence")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Sequence)object).getFilename();
		return label == null || label.length() == 0 ?
			getString("_UI_Sequence_type") : //$NON-NLS-1$
			getString("_UI_Sequence_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(Sequence.class)) {
			case RegiondefinitionPackage.SEQUENCE__FILENAME:
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE:
			case RegiondefinitionPackage.SEQUENCE__RUN_MODE_INDEX:
			case RegiondefinitionPackage.SEQUENCE__NUM_ITERATIONS:
			case RegiondefinitionPackage.SEQUENCE__NUM_INTERATION_OPTION:
			case RegiondefinitionPackage.SEQUENCE__REPEAT_UNTIL_STOPPED:
			case RegiondefinitionPackage.SEQUENCE__CONFIRM_AFTER_EACH_ITERATION:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case RegiondefinitionPackage.SEQUENCE__REGION:
			case RegiondefinitionPackage.SEQUENCE__SPECTRUM:
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
				(RegiondefinitionPackage.Literals.SEQUENCE__REGION,
				 RegiondefinitionFactory.eINSTANCE.createRegion()));

		newChildDescriptors.add
			(createChildParameter
				(RegiondefinitionPackage.Literals.SEQUENCE__SPECTRUM,
				 RegiondefinitionFactory.eINSTANCE.createSpectrum()));
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return RegiondefinitionEditPlugin.INSTANCE;
	}
	
}
