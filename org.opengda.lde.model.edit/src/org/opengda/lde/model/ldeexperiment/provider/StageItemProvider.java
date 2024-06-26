/**
 */
package org.opengda.lde.model.ldeexperiment.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
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
import org.opengda.lde.model.edit.StageTableConstants;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Stage;

/**
 * This is the item provider adapter for a {@link org.opengda.lde.model.ldeexperiment.Stage} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class StageItemProvider 
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StageItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	/**
	 * disable add command if the number of children exceeded limit set in its property {@link LDEExperimentsPackage#STAGE__NUMBER_OF_CELLS}.
	 * This will disable or enable the global 'Paste' Action in the editor context menu. 
	 */
	@Override
	protected Command createAddCommand(EditingDomain domain, EObject owner, EStructuralFeature feature,
			Collection<?> collection, int index) {
		return new AddCommand(domain, owner, feature, collection, index) {
			@Override
			protected boolean prepare() {
				if (owner instanceof Stage) {
					if (ownerList.size() >= ((Stage) owner).getNumberOfCells()) {
						return false;
					}
				}
				return super.prepare();
			}
		};
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

			addStageIDPropertyDescriptor(object);
			addDetector_xPropertyDescriptor(object);
			addDetector_yPropertyDescriptor(object);
			addDetector_zPropertyDescriptor(object);
			addCamera_xPropertyDescriptor(object);
			addCamera_yPropertyDescriptor(object);
			addCamera_zPropertyDescriptor(object);
			addNumberOfCellsPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Stage ID feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStageIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_stageID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_stageID_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__STAGE_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_detector_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_detector_x_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__DETECTOR_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_detector_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_detector_y_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__DETECTOR_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Detector z feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDetector_zPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_detector_z_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_detector_z_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__DETECTOR_Z,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Camera x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCamera_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_camera_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_camera_x_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__CAMERA_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Camera y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCamera_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_camera_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_camera_y_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__CAMERA_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Camera z feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCamera_zPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_camera_z_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_camera_z_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__CAMERA_Z,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Number Of Cells feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNumberOfCellsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Stage_numberOfCells_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Stage_numberOfCells_feature", "_UI_Stage_type"),
				 LDEExperimentsPackage.Literals.STAGE__NUMBER_OF_CELLS,
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
			childrenFeatures.add(LDEExperimentsPackage.Literals.STAGE__CELL);
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
	 * This returns Stage.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Stage"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Stage)object).getStageID();
		return label == null || label.length() == 0 ?
			getString("_UI_Stage_type") :
			getString("_UI_Stage_type") + " " + label;
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

		switch (notification.getFeatureID(Stage.class)) {
			case LDEExperimentsPackage.STAGE__STAGE_ID:
			case LDEExperimentsPackage.STAGE__DETECTOR_X:
			case LDEExperimentsPackage.STAGE__DETECTOR_Y:
			case LDEExperimentsPackage.STAGE__DETECTOR_Z:
			case LDEExperimentsPackage.STAGE__CAMERA_X:
			case LDEExperimentsPackage.STAGE__CAMERA_Y:
			case LDEExperimentsPackage.STAGE__CAMERA_Z:
			case LDEExperimentsPackage.STAGE__NUMBER_OF_CELLS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case LDEExperimentsPackage.STAGE__CELL:
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
				(LDEExperimentsPackage.Literals.STAGE__CELL,
				 LDEExperimentsFactory.eINSTANCE.createCell()));
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
	
	@Override
	public String getColumnText(Object object, int columnIndex) {
		if (object instanceof Stage) {
			Stage stage=(Stage)object;
			switch (columnIndex) {
			case StageTableConstants.COL_STAGE_ID:
				return stage.getStageID();
			case StageTableConstants.COL_DETECTOR_X:
				return String.valueOf(stage.getDetector_x());
			case StageTableConstants.COL_DETECTOR_Y:
				return String.valueOf(stage.getDetector_y());
			case StageTableConstants.COL_DETECTOR_Z:
				return String.valueOf(stage.getDetector_z());
			case StageTableConstants.COL_CAMERA_X:
				return String.valueOf(stage.getCamera_x());
			case StageTableConstants.COL_CAMERA_Y:
				return String.valueOf(stage.getCamera_y());
			case StageTableConstants.COL_CAMERA_Z:
				return String.valueOf(stage.getCamera_z());
			case StageTableConstants.COL_NUMBER_OF_CELLS:
				return String.valueOf(stage.getNumberOfCells());
			default:
				break;
			}
		}
		return super.getColumnText(object, columnIndex);
	}

}
