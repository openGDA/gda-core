/**
 */
package org.opengda.lde.model.ldeexperiment.provider;


import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import gda.configuration.properties.LocalProperties;

/**
 * This is the item provider adapter for a {@link org.opengda.lde.model.ldeexperiment.Cell} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class CellItemProvider 
	extends ItemProviderAdapter
	implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource, ITableItemLabelProvider {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CellItemProvider(AdapterFactory adapterFactory) {
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

			addCellIDPropertyDescriptor(object);
			addVisitIDPropertyDescriptor(object);
			addNamePropertyDescriptor(object);
			addEmailPropertyDescriptor(object);
			addStartDatePropertyDescriptor(object);
			addEndDatePropertyDescriptor(object);
			addEnableAutoEmailPropertyDescriptor(object);
			addCalibrantPropertyDescriptor(object);
			addCalibrant_xPropertyDescriptor(object);
			addCalibrant_yPropertyDescriptor(object);
			addCalibrant_exposurePropertyDescriptor(object);
			addEnvSamplingIntervalsPropertyDescriptor(object);
			addEnvScannableNamesPropertyDescriptor(object);
			addNumberOfSamplesPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Cell ID feature.
	 * <!-- begin-user-doc -->
	 * override {@link ItemPropertyDescriptor#getChoiceOfValues(Object)} to dynamically generated unique Cell IDs
	 * for a stage, and filter out Cell IDs that are already been used as the physical cell can only be used once. 
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected void addCellIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(new ItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_cellID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_cellID_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__CELL_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null)
				{
				@Override
				public Collection<?> getChoiceOfValues(Object object){
					List<String> choiceOfValues = new ArrayList<String>();
					Cell cell=(Cell)object;
					String stageName=cell.getStage().getStageID();
					if (stageName==null) {
						SampledefinitionEditPlugin.INSTANCE.log("Stage must have an ID");
					}
					// update the used Cell IDs for this stage
					List<String> usedCellIDs=new ArrayList<String>();
					for (Cell usedcell : cell.getStage().getCell()) {
						usedCellIDs.add(usedcell.getCellID());
					}
					//Dynamically generate cell ID for the stage.
					List<String> choiceOfValues2 = new ArrayList<String>();
					for (int i=1; i<= cell.getStage().getNumberOfCells(); i++) {
						choiceOfValues2.add(stageName+"-"+i);
					}
					// filter the Cell IDs to ensure one cell ID can only be used once.
					for (String each : choiceOfValues2) {
						if (stageName!=null && each.startsWith(stageName)) {
							if (!usedCellIDs.contains(each.toString())) {
								choiceOfValues.add(each);
							}
						}
					}
					return choiceOfValues;
				}
			});
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
				 getString("_UI_Cell_name_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_name_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__NAME,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Visit ID feature.
	 * <!-- begin-user-doc -->
	 * dynamically fill the drop down list with all existing folders for current year
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected void addVisitIDPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(new ItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_visitID_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_visitID_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__VISIT_ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null)
				{
				@Override
				public Collection<?> getChoiceOfValues(Object object) {
					return getVisitIDs();
				}
				});
	}
	
	private List<String> getVisitIDs() {
		String string = LocalProperties.get(LocalProperties.GDA_DATA, "/dls/i11-1/data");
		String dataDir = string+File.separator+Calendar.getInstance().get(Calendar.YEAR);
		File dir=new File(dataDir);
		String[] list = dir.list();
		List<String> dirList=new ArrayList<String>();
		if (list != null) {
			for (String s : list) {
				File file=new File(dataDir+File.separator+s);
				if (file.isDirectory()) {
					dirList.add(s);
				}
			}
		}
		return dirList;
	}

	/**
	 * This adds a property descriptor for the Email feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEmailPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_email_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_email_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__EMAIL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Start Date feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStartDatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_startDate_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_startDate_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__START_DATE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the End Date feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEndDatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_endDate_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_endDate_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__END_DATE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Enable Auto Email feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnableAutoEmailPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_enableAutoEmail_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_enableAutoEmail_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__ENABLE_AUTO_EMAIL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrantPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_calibrant_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_calibrant_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__CALIBRANT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant x feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_xPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_calibrant_x_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_calibrant_x_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__CALIBRANT_X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_yPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_calibrant_y_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_calibrant_y_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__CALIBRANT_Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Calibrant exposure feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCalibrant_exposurePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_calibrant_exposure_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_calibrant_exposure_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__CALIBRANT_EXPOSURE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Env Sampling Intervals feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnvSamplingIntervalsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_envSamplingIntervals_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_envSamplingIntervals_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__ENV_SAMPLING_INTERVALS,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Env Scannable Names feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addEnvScannableNamesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_envScannableNames_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_envScannableNames_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__ENV_SCANNABLE_NAMES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Number Of Samples feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNumberOfSamplesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Cell_numberOfSamples_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_Cell_numberOfSamples_feature", "_UI_Cell_type"),
				 LDEExperimentsPackage.Literals.CELL__NUMBER_OF_SAMPLES,
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
			childrenFeatures.add(LDEExperimentsPackage.Literals.CELL__SAMPLE);
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
	 * This returns Cell.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Cell"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((Cell)object).getCellID();
		return label == null || label.length() == 0 ?
			getString("_UI_Cell_type") :
			getString("_UI_Cell_type") + " " + label;
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

		switch (notification.getFeatureID(Cell.class)) {
			case LDEExperimentsPackage.CELL__CELL_ID:
			case LDEExperimentsPackage.CELL__VISIT_ID:
			case LDEExperimentsPackage.CELL__NAME:
			case LDEExperimentsPackage.CELL__EMAIL:
			case LDEExperimentsPackage.CELL__START_DATE:
			case LDEExperimentsPackage.CELL__END_DATE:
			case LDEExperimentsPackage.CELL__ENABLE_AUTO_EMAIL:
			case LDEExperimentsPackage.CELL__CALIBRANT:
			case LDEExperimentsPackage.CELL__CALIBRANT_X:
			case LDEExperimentsPackage.CELL__CALIBRANT_Y:
			case LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE:
			case LDEExperimentsPackage.CELL__ENV_SAMPLING_INTERVALS:
			case LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES:
			case LDEExperimentsPackage.CELL__NUMBER_OF_SAMPLES:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case LDEExperimentsPackage.CELL__SAMPLE:
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
				(LDEExperimentsPackage.Literals.CELL__SAMPLE,
				 LDEExperimentsFactory.eINSTANCE.createSample()));
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
			case CellTableConstants.COL_NUMBER_OF_SAMPLES:
				return String.valueOf(cell.getNumberOfSamples());
			case CellTableConstants.COL_ENV_SCANNABLE_NAMES:
				return cell.getEnvScannableNames().toString();
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
