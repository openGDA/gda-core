/**
 */
package org.opengda.lde.model.ldeexperiment.provider;


import java.text.DateFormat;
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
import org.opengda.lde.model.edit.CellTableConstants;
import org.opengda.lde.model.edit.ImageConstants;
import org.opengda.lde.model.ldeexperiment.Cell;
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
			childrenFeatures.add(LDEExperimentsPackage.Literals.STAGE__CELLS);
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
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case LDEExperimentsPackage.STAGE__CELLS:
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
				(LDEExperimentsPackage.Literals.STAGE__CELLS,
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
	public Object getColumnImage(Object object, int columnIndex) {
		if (object instanceof Cell) {
			Cell cell = (Cell)object;
			if (columnIndex == CellTableConstants.COL_AUTO_EMAIL) {
				if (cell.isEnableAutoEmail()) {
					return getResourceLocator().getImage(ImageConstants.ICON_CHECKED_STATE);
				} else {
					return getResourceLocator().getImage(ImageConstants.ICON_UNCHECKED_STATE);
				}
			}		
		}
		return super.getColumnImage(object, columnIndex);
	}
	@Override
	public String getColumnText(Object object, int columnIndex) {
		if (object instanceof Cell) {
			Cell cell = (Cell)object;
			switch(columnIndex) {
			case CellTableConstants.COL_CELL_NAME:
				return cell.getName();
			case CellTableConstants.COL_CELL_ID:
				return cell.getCellID();
			case CellTableConstants.COL_VISIT_ID:
				return cell.getVisitID();
			case CellTableConstants.COL_CALIBRANT_NAME:
				return cell.getCalibrant();
			case CellTableConstants.COL_CALIBRANT_X:
				return String.valueOf(cell.getCalibrant_x());
			case CellTableConstants.COL_CALIBRANT_Y:
				return String.valueOf(cell.getCalibrant_y());
			case CellTableConstants.COL_CALIBRANT_EXPOSURE:
				return String.valueOf(cell.getCalibrant_exposure());
			case CellTableConstants.COL_ENV_SCANNABLE_NAMES:
				return cell.getEvnScannableNames();
			case CellTableConstants.COL_ENV_SAMPLING_INTERVAL:
				return String.valueOf(cell.getEnvSamplingInterval());
			case CellTableConstants.COL_START_DATE:
				return DateFormat.getInstance().format(cell.getStartDate());
			case CellTableConstants.COL_END_DATE: 
				return DateFormat.getInstance().format(cell.getEndDate());
			case CellTableConstants.COL_EMAIL:
				return cell.getEmail();
			case CellTableConstants.COL_AUTO_EMAIL:
				return "";
			default:
				break;
			}
		}
		return super.getColumnText(object, columnIndex);
	}
}
