/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
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

import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;

/**
 * This is the item provider adapter for a {@link org.opengda.detector.electronanalyser.model.regiondefinition.api.Region} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class RegionItemProvider
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegionItemProvider(AdapterFactory adapterFactory) {
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

			addRegionIdPropertyDescriptor(object);
			addStatusPropertyDescriptor(object);
			addEnabledPropertyDescriptor(object);
			addNamePropertyDescriptor(object);
			addLensModePropertyDescriptor(object);
			addPassEnergyPropertyDescriptor(object);
			addExcitationEnergyPropertyDescriptor(object);
			addAcquisitionModePropertyDescriptor(object);
			addEnergyModePropertyDescriptor(object);
			addFixEnergyPropertyDescriptor(object);
			addLowEnergyPropertyDescriptor(object);
			addHighEnergyPropertyDescriptor(object);
			addEnergyStepPropertyDescriptor(object);
			addStepTimePropertyDescriptor(object);
			addFirstXChannelPropertyDescriptor(object);
			addLastXChannelPropertyDescriptor(object);
			addFirstYChannelPropertyDescriptor(object);
			addLastYChannelPropertyDescriptor(object);
			addSlicesPropertyDescriptor(object);
			addDetectorModePropertyDescriptor(object);
			addADCMaskPropertyDescriptor(object);
			addDiscriminatorLevelPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Region Id feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRegionIdPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_regionId_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_regionId_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__REGION_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
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
				 getString("_UI_Region_name_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_name_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__NAME,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Lens Mode feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLensModePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_lensMode_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_lensMode_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__LENS_MODE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Acquisition Mode feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAcquisitionModePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_acquisitionMode_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_acquisitionMode_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__ACQUISITION_MODE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Energy Mode feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnergyModePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_energyMode_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_energyMode_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__ENERGY_MODE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Fix Energy feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFixEnergyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_fixEnergy_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_fixEnergy_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__FIX_ENERGY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Low Energy feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLowEnergyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_lowEnergy_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_lowEnergy_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__LOW_ENERGY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the High Energy feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addHighEnergyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_highEnergy_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_highEnergy_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__HIGH_ENERGY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Energy Step feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnergyStepPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_energyStep_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_energyStep_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__ENERGY_STEP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Step Time feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStepTimePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_stepTime_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_stepTime_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__STEP_TIME,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the First XChannel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFirstXChannelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_firstXChannel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_firstXChannel_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__FIRST_XCHANNEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Last XChannel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLastXChannelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_lastXChannel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_lastXChannel_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__LAST_XCHANNEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the First YChannel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFirstYChannelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_firstYChannel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_firstYChannel_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__FIRST_YCHANNEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Last YChannel feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLastYChannelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_lastYChannel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_lastYChannel_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__LAST_YCHANNEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Slices feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSlicesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_slices_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_slices_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__SLICES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector Mode feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetectorModePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_detectorMode_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_detectorMode_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__DETECTOR_MODE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the ADC Mask feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addADCMaskPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_ADCMask_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_ADCMask_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__ADC_MASK,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Discriminator Level feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDiscriminatorLevelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_discriminatorLevel_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_discriminatorLevel_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__DISCRIMINATOR_LEVEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
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
				 getString("_UI_Region_Status_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_Status_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__STATUS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Enabled feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnabledPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_Enabled_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_Enabled_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__ENABLED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Pass Energy feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPassEnergyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_passEnergy_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_passEnergy_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__PASS_ENERGY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Excitation Energy feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExcitationEnergyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Region_excitationEnergy_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Region_excitationEnergy_feature", "_UI_Region_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 RegiondefinitionPackage.Literals.REGION__EXCITATION_ENERGY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
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
			childrenFeatures.add(RegiondefinitionPackage.Literals.REGION__RUN_MODE);
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
	 * This returns Region.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Region")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Region)object).getName();
		return label == null || label.length() == 0 ?
			getString("_UI_Region_type") : //$NON-NLS-1$
			getString("_UI_Region_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(Region.class)) {
			case RegiondefinitionPackage.REGION__REGION_ID:
			case RegiondefinitionPackage.REGION__STATUS:
			case RegiondefinitionPackage.REGION__ENABLED:
			case RegiondefinitionPackage.REGION__NAME:
			case RegiondefinitionPackage.REGION__LENS_MODE:
			case RegiondefinitionPackage.REGION__PASS_ENERGY:
			case RegiondefinitionPackage.REGION__EXCITATION_ENERGY:
			case RegiondefinitionPackage.REGION__ACQUISITION_MODE:
			case RegiondefinitionPackage.REGION__ENERGY_MODE:
			case RegiondefinitionPackage.REGION__FIX_ENERGY:
			case RegiondefinitionPackage.REGION__LOW_ENERGY:
			case RegiondefinitionPackage.REGION__HIGH_ENERGY:
			case RegiondefinitionPackage.REGION__ENERGY_STEP:
			case RegiondefinitionPackage.REGION__STEP_TIME:
			case RegiondefinitionPackage.REGION__FIRST_XCHANNEL:
			case RegiondefinitionPackage.REGION__LAST_XCHANNEL:
			case RegiondefinitionPackage.REGION__FIRST_YCHANNEL:
			case RegiondefinitionPackage.REGION__LAST_YCHANNEL:
			case RegiondefinitionPackage.REGION__SLICES:
			case RegiondefinitionPackage.REGION__DETECTOR_MODE:
			case RegiondefinitionPackage.REGION__ADC_MASK:
			case RegiondefinitionPackage.REGION__DISCRIMINATOR_LEVEL:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case RegiondefinitionPackage.REGION__RUN_MODE:
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
				(RegiondefinitionPackage.Literals.REGION__RUN_MODE,
				 RegiondefinitionFactory.eINSTANCE.createRunMode()));
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
