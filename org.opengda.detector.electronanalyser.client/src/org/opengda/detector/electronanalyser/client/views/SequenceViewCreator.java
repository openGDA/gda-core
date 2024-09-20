/*-
 * Copyright © 2021 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.opengda.detector.electronanalyser.client.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.FeatureNotFoundException;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RefreshRegionDisplaySelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionValidationMessage;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceTableConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewContentProvider;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewLabelProvider;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class SequenceViewCreator extends ViewPart implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, InitializationListener {

	public static final String ID = "org.opengda.detector.electronanalyser.client.sequencecreator";
	private final Logger logger = LoggerFactory.getLogger(SequenceViewCreator.class);

	public static final String ELEMENTSET_UNKNOWN = "UNKNOWN";

	private final String[] columnHeaders = {
		SequenceTableConstants.VALID, SequenceTableConstants.STATUS, SequenceTableConstants.ENABLED, SequenceTableConstants.REGION_NAME,
		SequenceTableConstants.LENS_MODE, SequenceTableConstants.PASS_ENERGY, SequenceTableConstants.X_RAY_SOURCE, SequenceTableConstants.ENERGY_MODE,
		SequenceTableConstants.LOW_ENERGY, SequenceTableConstants.HIGH_ENERGY, SequenceTableConstants.ENERGY_STEP, SequenceTableConstants.STEP_TIME,
		SequenceTableConstants.STEPS, SequenceTableConstants.TOTAL_TIME, SequenceTableConstants.X_CHANNEL_FROM, SequenceTableConstants.X_CHANNEL_TO,
		SequenceTableConstants.Y_CHANNEL_FROM, SequenceTableConstants.Y_CHANNEL_TO, SequenceTableConstants.SLICES, SequenceTableConstants.MODE
	};

	private ColumnWeightData[] columnLayouts = {
		new ColumnWeightData(10, 30, true), new ColumnWeightData(10, 30, true), new ColumnWeightData(10, 30, true), new ColumnWeightData(80, 100, true),
		new ColumnWeightData(70, 90, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 80, true),
		new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 90, true), new ColumnWeightData(50, 70, true),
		new ColumnWeightData(50, 50, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 50, true), new ColumnWeightData(40, 50, true),
		new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 120, true)
	};

	protected TableViewer sequenceTableViewer;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private EditingDomain editingDomain;
	private Resource resource;
	protected Sequence sequence;
	protected List<Region> regions = new ArrayList<>();
	protected Region currentRegion;

	private RegionValidator regionValidator;
	private String invalidRegionName;

	private List<ISelectionChangedListener> selectionChangedListeners;

	private boolean isDirty;
	private Action addAction;
	private Action copyAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;

	private Combo comboElementSet;
	protected String elementSet = ELEMENTSET_UNKNOWN;
	private StyledText txtSequenceFilePath;

	private Camera camera;
	private String energyLensTableDir;
	private String regionViewID = RegionViewCreator.ID;
	private boolean showInvalidDialogOnSave = true;
	private boolean canEnableInvalidRegions = true;
	private boolean useCache = false;
	protected boolean canEdit = true;

	protected CompoundCommand groupCommand = new CompoundCommand();

	private ISelectionListener selectionListener = SequenceViewCreator.this::selectionListenerDetectedUpdate;

	//Have this method be only way to update elementSet value and display to UI
	protected void setElementSet(String newElementSet) {
		if(!elementSet.equals(newElementSet)) {
			logger.info("Updating elementSet from {} to {}", elementSet, newElementSet);
			elementSet = newElementSet;
			if(sequence != null) {
				addCommandToGroupToUpdateFeature(sequence, RegiondefinitionPackage.eINSTANCE.getSequence_ElementSet(), elementSet, sequence.getElementSet());
				executeCommand(groupCommand);
				validateAllRegions();
			}
			comboElementSet.setText(elementSet);
		}
	}

	protected void selectionListenerDetectedUpdate(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof EnergyChangedSelection energyChangeSelection) {

			for (Region region : energyChangeSelection.getRegions()) {
				boolean valid = isValidRegion(region, false);
				boolean isFromExcitationEnergyChange = energyChangeSelection.isExcitationEnergyChange();

				if (!valid && !isFromExcitationEnergyChange) {
					try {
						addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), valid, region.isEnabled());
						executeCommand(groupCommand);
					} catch (Exception e) {
						logger.error("Unable to update status and show popup", e);
					}
				}
			}
		}
		else if (selection instanceof IStructuredSelection sel) {
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof Region region) {
				sequenceTableViewer.setSelection(sel);
				sequenceTableViewer.refresh(region);
			}
		}
	}

	private Adapter notifyListener = new EContentAdapter() {
		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null &&
					!notification.getFeature().equals(RegiondefinitionPackage.eINSTANCE.getRegion_Status())
					 &&  (notifySaveNeeded(notification))) {

				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		}
	};

	protected boolean notifySaveNeeded(Notification notification) {
		Object oldValue = notification.getOldValue();
		Object newValue = notification.getNewValue();
		//If same value, file hasn't changed so don't make file dirty
		return !(oldValue != null && newValue != null && oldValue.equals(newValue));
	}

	private SelectionAdapter elementSetSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(comboElementSet)) {
				setElementSet(comboElementSet.getText());
			}
		}
	};

	public SequenceViewCreator() {
		setTitleToolTip("Create a new or edit an existing sequence");
		setPartName("Sequence Editor");
		setCanEnableInvalidRegions(false);
		this.selectionChangedListeners = new ArrayList<>();

	}

	@Override
	public void createPartControl(final Composite parent) {
		int numberOfColumns = 2;

		Composite rootComposite = createRootComposite(parent);
		createSequenceTableArea(rootComposite);

		Composite controlArea = createControlArea(rootComposite, numberOfColumns);
		createElementSet(controlArea);
		createSequenceFile(controlArea, 1);

		registerSelectionProviderAndCreateHelpContext();
	}

	protected Composite createRootComposite(final Composite parent) {
		GridLayout glRoot = new GridLayout();
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(glRoot);
		return rootComposite;
	}

	protected void createSequenceTableArea(final Composite rootComposite) {
		Composite tableViewerContainer = new Composite(rootComposite, SWT.None);

		sequenceTableViewer = new TableViewer(tableViewerContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		sequenceTableViewer.getTable().setHeaderVisible(true);
		sequenceTableViewer.getTable().setLinesVisible(true);

		sequenceTableViewer.addSelectionChangedListener(event -> {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection sel) {
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region firstElementRegion) {
					fireSelectionChanged(firstElementRegion);
				} else {
					fireSelectionChanged(sel);
				}
			}
		});

		tableViewerContainer.setLayout(new GridLayout());
		GridData gd1 = new GridData(GridData.FILL_BOTH);
		gd1.widthHint = 786;
		sequenceTableViewer.getTable().setLayoutData(gd1);
		ColumnViewerToolTipSupport.enableFor(sequenceTableViewer, ToolTip.NO_RECREATE);
		createColumns(sequenceTableViewer, null);
		tableViewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		sequenceTableViewer.setContentProvider(new SequenceViewContentProvider(regionDefinitionResourceUtil));
		final SequenceViewLabelProvider labelProvider = new SequenceViewLabelProvider();
		labelProvider.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		labelProvider.setCamera(camera);
		sequenceTableViewer.setLabelProvider(labelProvider);
		regions = Collections.emptyList();
	}

	private void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);
			column.setWidth(columnLayouts[i].minimumWidth);

			tableViewerColumn.setEditingSupport(new SequenceColumnEditingSupport(tableViewer, tableViewerColumn));
		}
	}

	protected Composite createControlArea(final Composite rootComposite, int columns) {
		Composite controlArea = new Composite(rootComposite, SWT.None);
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(columns, false));
		return controlArea;
	}

	protected void createElementSet(Composite controlArea) {
		Group grpElementset = new Group(controlArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(grpElementset);
		grpElementset.setLayout(new GridLayout());
		grpElementset.setText("Element Set");
		grpElementset.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		comboElementSet = new Combo(grpElementset, SWT.READ_ONLY);
		comboElementSet.setItems("Low", "High");
		comboElementSet.setToolTipText("Select an element set");
		comboElementSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboElementSet.addSelectionListener(elementSetSelAdaptor);
	}

	protected void createSequenceFile(Composite controlArea, int horizontalSpan) {
		Group grpSequenceFile = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1).applyTo(grpSequenceFile);
		grpSequenceFile.setText("Sequence File in the table");
		grpSequenceFile.setLayout(new GridLayout());
		grpSequenceFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		txtSequenceFilePath = new StyledText(grpSequenceFile, SWT.NONE | SWT.READ_ONLY | SWT.H_SCROLL);
		txtSequenceFilePath.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		txtSequenceFilePath.setEditable(false);

		//adjust size to be slightly bigger to allow room for vertical scrollbar
		gridData.heightHint = (int) Math.ceil(txtSequenceFilePath.getLineHeight() * 1.25) ;
		txtSequenceFilePath.setLayoutData(gridData);
	}

	protected void registerSelectionProviderAndCreateHelpContext() {
		initialisation();

		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(getRegionViewID(), selectionListener);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sequenceTableViewer.getControl(), "org.opengda.analyser.ui.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	protected void initialisation() {
		String fileName = regionDefinitionResourceUtil.getFileName();
		try {
			editingDomain = regionDefinitionResourceUtil.getEditingDomain();
		} catch (Exception e) {
			logger.error("Cannot get editin g domain object.", e);
		}
		if (editingDomain == null) {
			throw new RuntimeException("Cannot get editing domain object.");
		}

		// add drag and drop support,must ensure editing domain not null at this point.
		sequenceTableViewer.addDragSupport(
			DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK,
			new Transfer[] { LocalTransfer.getInstance() },
			new ViewerDragAdapter(sequenceTableViewer)
		);

		EditingDomainViewerDropAdapter dropTargetListener = new EditingDomainViewerDropAdapter(editingDomain, sequenceTableViewer) {
			@Override
			public void dropAccept(DropTargetEvent event) {
				super.dropAccept(event);
				//Force update of the table to correctly display the new order
				sequenceTableViewer.getTable().getDisplay().asyncExec(() -> {
					sequenceTableViewer.refresh();
				});
			}
		};
		sequenceTableViewer.addDropSupport(
			DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK,
			new Transfer[] { LocalTransfer.getInstance() },
			dropTargetListener
		);
		Display.getCurrent().asyncExec(() -> {
			final File file = new File(fileName);
			final boolean createFile = !file.isFile();
			refreshTable(fileName, createFile);
		});
	}

	private void makeActions() {
		makeAddAction();
		makeCopyAction();
		makeDeleteAction();
		makeUndoAction();
		makeRedoAction();
	}

	private void makeAddAction() {
		addAction = new Action() {
			@Override
			public void run() {
				try {
					Region selectedRegion = getSelectedRegion();
					int prevIndex = regions.indexOf(selectedRegion);

					Region newRegion = RegiondefinitionFactory.eINSTANCE.createRegion();
					int nameCount = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), newRegion.getName());
					if (nameCount != -1) {
						// increment the name
						nameCount++;
						newRegion.setName(newRegion.getName() + nameCount);
					}

					//Populate region with correct excitation energy
					if (!regions.isEmpty()) {
						double excitationEnergy = regions.get(0).getExcitationEnergy();
						newRegion.setExcitationEnergy(excitationEnergy);
					}

					executeCommand(
						AddCommand.create(
							editingDomain,
							regionDefinitionResourceUtil.getSequence(),
							RegiondefinitionPackage.eINSTANCE.getSequence_Region(),
							newRegion
						)
					);
					isValidRegion(newRegion, false);
					correctSelectionAfterAction(selectedRegion, prevIndex);
					sequenceTableViewer.refresh();
				} catch (Exception e1) {
					logger.error("Cannot add region.", e1);
				}
			}
		};

		addAction.setText("Add");
		addAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_ADD_OBJ));
		addAction.setToolTipText("Add a new region");
	}

	private void makeCopyAction() {
		copyAction = new Action() {
			@Override
			public void run() {
				try {
					Region selectedRegion = getSelectedRegion();
					int prevIndex = regions.indexOf(selectedRegion);

					if (getSelectedRegion() != null) {
						Region copy = EcoreUtil.copy(getSelectedRegion());
						copy.setRegionId(EcoreUtil.generateUUID());
						String regionNamePrefix = StringUtils.prefixBeforeInt(copy.getName());
						int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), regionNamePrefix);
						if (largestIntInNames != -1) {
							largestIntInNames++;
							copy.setName(regionNamePrefix + largestIntInNames);
						}
						executeCommand(
							AddCommand.create(
								editingDomain,
								regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_Region(),
								copy
							)
						);
						isValidRegion(copy, false);
						correctSelectionAfterAction(selectedRegion, prevIndex);
						sequenceTableViewer.refresh();
					} else {
						openMessageBox("No region selected", "You have not selected a region to duplicate.", MessageDialog.ERROR);
					}
				} catch (Exception e1) {
					logger.error("Cannot copy region.", e1);
				}
			}
		};
		copyAction.setText("Copy");
		copyAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_COPY_EDIT));
		copyAction.setToolTipText("Copy selected region");
	}

	private void makeDeleteAction() {
		deleteAction = new Action() {
			@Override
			public void run() {
				try {
					Region selectedRegion = getSelectedRegion();
					int prevIndex = regions.indexOf(selectedRegion);
					if (selectedRegion != null) {
						executeCommand(
							RemoveCommand.create(
								editingDomain,
								regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_Region(),
								selectedRegion
							)
						);
					} else {
						openMessageBox("No region selected", "You have not selected a region to delete.", MessageDialog.ERROR);
					}
					correctSelectionAfterAction(selectedRegion, prevIndex);
					sequenceTableViewer.refresh();
				} catch (Exception e1) {
					logger.error("Cannot delete region.", e1);
				}
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_DELETE_OBJ));
		deleteAction.setToolTipText("Delete selected region");
	}

	private void correctSelectionAfterAction(Region selectedRegion, int prevIndex) {
		int index = regions.indexOf(selectedRegion);
		if (index == -1) {
			index  = prevIndex;
			if (index > regions.size() -1) {
				index = regions.size() -1;
			}
			else if(index < 0) {
				index = 0;
			}
		}
		if (!regions.isEmpty()) {
			currentRegion = regions.get(index);
			fireSelectionChanged(currentRegion);
			sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
		}
	}

	private void makeUndoAction() {
		undoAction = new Action() {
			@Override
			public void run() {
				Region selectedRegion = getSelectedRegion();
				int prevIndex = regions.indexOf(selectedRegion);
				try {
					editingDomain.getCommandStack().undo();
				} catch (Exception e1) {
					logger.error("Cannot undo action.", e1);
				}
				validateAllRegions();
				//Needed to update table with any removed or added regions
				sequenceTableViewer.refresh();
				//Tell region editor which file to get and load regions from AFTER we have reset cache (if turned on).
				correctSelectionAfterAction(selectedRegion, prevIndex);
				if (comboElementSet != null) {
					setElementSet(sequence.getElementSet());
				}
				fireSelectionChanged(new RefreshRegionDisplaySelection());
			}
		};
		undoAction.setText("Undo");
		undoAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_UNDO_EDIT));
		undoAction.setToolTipText("Undo");
	}

	private void makeRedoAction() {
		redoAction = new Action() {
			@Override
			public void run() {
				Region selectedRegion = getSelectedRegion();
				int prevIndex = regions.indexOf(selectedRegion);
				try {
					editingDomain.getCommandStack().redo();
				} catch (Exception e1) {
					logger.error("Cannot re-do action.", e1);
				}
				validateAllRegions();
				//Needed to update table with any removed or added regions
				sequenceTableViewer.refresh();
				//Tell region editor which file to get and load regions from AFTER we have reset cache (if turned on).
				correctSelectionAfterAction(selectedRegion, prevIndex);
				if (comboElementSet != null && sequence != null) {
					setElementSet(sequence.getElementSet());
				}
				fireSelectionChanged(new RefreshRegionDisplaySelection());
			}
		};
		redoAction.setText("Redo");
		redoAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_REDO_EDIT));
		redoAction.setToolTipText("Redo");
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this::fillContextMenu);
		Menu menu = menuMgr.createContextMenu(sequenceTableViewer.getControl());
		sequenceTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, sequenceTableViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
	}

	private Region getSelectedRegion() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection structuredSel) {
			Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof Region firstElementRegion) {
				return firstElementRegion;
			}
		}
		return null;
	}

	protected void validateAllRegions() {
		for (Region r : regions) {
			isValidRegion(r, false);
		}
	}

	protected void fireSelectionChanged(Region region) {
		ISelection sel = StructuredSelection.EMPTY;
		if (region != null) {
			sel = new StructuredSelection(region);
		}
		fireSelectionChanged(sel);
	}

	protected void fireSelectionChanged(ISelection sel) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, sel);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

	protected void updateCalculatedData() {
		//This is overwritten by subclass if needed
	}

	private class SequenceColumnEditingSupport extends EditingSupport {

		private String columnIdentifier;
		private Table table;

		public SequenceColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
			super(viewer);
			table = ((TableViewer) viewer).getTable();
			columnIdentifier = tableViewerColumn.getColumn().getText();
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				return new CheckboxCellEditor(table);
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			return SequenceTableConstants.ENABLED.equals(columnIdentifier) && canEdit;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Region region && SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				return region.isEnabled();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				Region region = (Region) element;
				if (value instanceof Boolean enable) {
					if (Boolean.TRUE.equals(enable)) {
						boolean valid = isValidRegion(region, true);

						if (getCanEnableInvalidRegions()) {
							valid = true;
						}
						enable = valid;
					}
					addCommandToGroupToUpdateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), enable, region.isEnabled());
					executeCommand(groupCommand);
					fireSelectionChanged(new RegionActivationSelection(region));
					sequenceTableViewer.refresh(region);
				}
			}
		}
	}

	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	@Override
	public void refreshTable(String seqFileName, boolean newFile) {
		logger.debug("refresh table with file: {}{}", FilenameUtils.getFullPath(seqFileName), FilenameUtils.getName(seqFileName));
		// same file no need to refresh
		if (txtSequenceFilePath.getText().trim().compareTo(seqFileName) == 0 && useCache) {
			return;
		}
		if (resource != null && resource.eAdapters().contains(notifyListener)) {
			resource.eAdapters().remove(notifyListener);
			resource = null;
		}
		regions = new ArrayList<>();
		sequence = null;
		regionDefinitionResourceUtil.setFileName(seqFileName);
		try {
			resource = getSequenceResource(getUseCache());
			if (newFile) {
				regionDefinitionResourceUtil.createSequence();
			}
			sequenceTableViewer.setInput(resource);
			// update the resource in this view.
			resource.eAdapters().add(notifyListener);
			sequence = regionDefinitionResourceUtil.getSequence();
			regions = regionDefinitionResourceUtil.getRegions();
		}
		catch (Exception e) {
			logger.error("Error opening file.", e);
			String errorMessage = "Cannot open file \"" + seqFileName + "\". Encountered error: " + e;
			Display.getCurrent().asyncExec(() -> openMessageBox("Error opening file", errorMessage, SWT.ICON_ERROR));
			return;
		}
		updateCalculatedData();
		if (comboElementSet != null) {
			setElementSet(sequence.getElementSet());
		}
		else {
			//Only validate once on startup. setElementSet(value) will do validation for us. If not called, do it here instead.
			validateAllRegions();
		}
		resetSelection();
		fireSelectionChanged(new FileSelection(seqFileName));
		sequenceTableViewer.refresh();
		// update spectrum parameters
		txtSequenceFilePath.setText(seqFileName);
		logger.info("Sequence file {} loaded successfully.", seqFileName);
	}

	private Resource getSequenceResource(boolean useCache) throws Exception {
		if (!useCache && regionDefinitionResourceUtil.isResourceLoaded()) {
			regionDefinitionResourceUtil.clearCache();
		}
		try {
			return regionDefinitionResourceUtil.getResource();
		}
		catch (Exception e) {
			logger.warn("An error occured getting sequence resource for file: {}", regionDefinitionResourceUtil.getFileName(), e);
			if (!(e.getCause() instanceof FeatureNotFoundException)) {
				throw e;
			}
			logger.warn("Found invalid features in file {}. Attempting to reload file without invalid features... ", regionDefinitionResourceUtil.getFileName());
			return regionDefinitionResourceUtil.getResource();
		}
	}

	private void resetSelection() {
		resetCurrentRegion();

		if (regions.isEmpty()) {
			fireSelectionChanged(StructuredSelection.EMPTY);
		} else {
			fireSelectionChanged(currentRegion);
			sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
		}
	}

	private boolean isAllRegionsValid(boolean showDialogIfInvalid) {
		for (Region region : regions) {
			if (region.isEnabled()) {
				// only check enabled regions. check stopped at first invalid region.
				boolean valid = isValidRegion(region, showDialogIfInvalid);
				if (!valid) {
					invalidRegionName = region.getName();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * update the data model Status field according to region valid or not
	 *
	 * @param region
	 * @return
	 */
	protected boolean isValidRegion(Region region, boolean showDialogIfInvalid) {

		if (regionValidator == null) {
			logger.info("No region validator provided, so region validation is NOT applied.");
			return true;
		}
		if(elementSet.equals(ELEMENTSET_UNKNOWN)) {
			logger.warn("Cannot validate region {} because elementSet is {}", region.getName(), ELEMENTSET_UNKNOWN);
			return true;
		}

		final boolean valid = regionValidator.isValidRegion(region, elementSet);
		final String message = valid ? "" : regionValidator.getErrorMessage();
		final RegionValidationMessage regionValidationMessage;
		String energyRange = regionValidator.getEnergyRange(region, elementSet);
		if (energyRange.equals("none")) {
			regionValidationMessage = new RegionValidationMessage(region, message);
		}
		else {
			List<String> limits = Splitter.on("-").splitToList(energyRange);
			final boolean isKinetic = region.getEnergyMode() == ENERGY_MODE.KINETIC;
			final double lowEnergy = Double.parseDouble(isKinetic ? limits.get(0) : limits.get(1));
			final double highEnergy = Double.parseDouble(isKinetic ? limits.get(1) : limits.get(0));
			regionValidationMessage = new RegionValidationMessage(region, message, lowEnergy, highEnergy);
		}

		final STATUS status = valid ? STATUS.READY : STATUS.INVALID;
		//Only update region status to READY or INVALID if not during a scan.
		if (region.getStatus() != STATUS.RUNNING && region.getStatus() != STATUS.COMPLETED) {
			updateRegionStatus(region, status);
		}
		else {
			logger.debug("Unable to set region {} to new status. Current value = {}, New value = {}", region.getName(), region.getStatus(), status);
		}
		sequenceTableViewer.getTable().getDisplay().asyncExec(() -> fireSelectionChanged(regionValidationMessage));

		if (showDialogIfInvalid && !valid) {
			openMessageBox("Invalid Region", message, SWT.ICON_ERROR);
		}
		return valid;
	}

	protected void updateRegionStatus(final Region region, final STATUS newStatus) {
		if (region.getStatus() != newStatus) {
			logger.info("Updating status of region {} from {} to {}", region.getName(), region.getStatus(), newStatus);
			region.setStatus(newStatus);
		}
		sequenceTableViewer.refresh(region);
	}

	//Add a sub groupCommand to update a feature in the model. This means it can be used in undo / redo.
	protected void addCommandToGroupToUpdateFeature(EObject eObject, Object feature, Object newValue, Object oldValue) {
		if (eObject != null && editingDomain != null && !oldValue .equals(newValue)) {
			Command newCommand = SetCommand.create(editingDomain, eObject, feature, newValue);
			groupCommand.append(newCommand);
		}
	}

	//Execute groupCommand that contains all sub commands when ready. Allows for clean redo / undo of group changes.
	//Wipe once done for new set commands to be executed next
	protected void executeCommand(Command commandToExecute) {
		editingDomain.getCommandStack().execute(commandToExecute);
		if (commandToExecute.equals(groupCommand)) {
			groupCommand.dispose();
			groupCommand = new CompoundCommand();
		}
	}

	@Override
	public void dispose() {
		try {
			regionDefinitionResourceUtil.getResource().eAdapters().remove(notifyListener);
			if (!getUseCache() &&  (regionDefinitionResourceUtil.getResource().isLoaded())) {
				regionDefinitionResourceUtil.getResource().unload();
			}
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(getRegionViewID(), selectionListener);
		} catch (Exception e) {
			logger.error("An error occured while disposting SequenceView", e);
		}
		super.dispose();
	}

	protected void openMessageBox(String title, String message, int iconStyle) {
		logger.debug("About to open message box with message: {}", message);
		MessageBox dialog = new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			if (resource != null) {
				resource.save(null);
			}
			isDirty = false;
			firePropertyChange(PROP_DIRTY);
			if (isAllRegionsValid(getShowInvalidDialogOnSave())) {
				resetCurrentRegion();
				logger.info("All active regions in file {} are valid.", regionDefinitionResourceUtil.getFileName());
			} else {
				logger.warn("File {} contains invalid active region {}.", regionDefinitionResourceUtil.getFileName(), invalidRegionName);
			}
		} catch (IOException e) {
			logger.error("Cannot save the resource to a file.", e);
		} catch (Exception e) {
			logger.error("Cannot get resource from RegionDefinitionResourceUtil.", e);
		}
	}

	@Override
	public void doSaveAs() {
		Resource resourceToSave = null;
		try {
			resourceToSave = regionDefinitionResourceUtil.getResource();
		} catch (Exception e1) {
			logger.warn("Cannot find the resouce from sequence file.");
		}
		FileDialog fileDialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		String filterPath = getRegionDefinitionResourceUtil().getTgtDataRootPath();
		fileDialog.setFilterPath(filterPath);
		fileDialog.setOverwrite(true);
		fileDialog.setFilterExtensions(new String[] {"*.seq"});
		String fileName = fileDialog.open();
		if (fileName != null && resourceToSave != null) {
			regionDefinitionResourceUtil.saveAs(resourceToSave, fileName);
			refreshTable(fileName, false);
		}
		if (isAllRegionsValid(getShowInvalidDialogOnSave())) {
			logger.info("All active regions in file {} are valid.", regionDefinitionResourceUtil.getFileName());
		}
		else {
			logger.warn("File {} contains invalid active region {}.", regionDefinitionResourceUtil.getFileName(), invalidRegionName);
		}
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		// deleted logger message since analyser state pv is not needed
	}

	private List<String> getRegionNames() {
		return regions.stream().map(Region::getName).toList();
	}

	@Override
	public void setFocus() {
		sequenceTableViewer.getTable().setFocus();
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinition) {
		this.regionDefinitionResourceUtil = regionDefinition;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return sequenceTableViewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	/**
	 * Sets {@link #currentRegion} to be the first enabled region from the list of regions
	 */
	protected void resetCurrentRegion() {
		currentRegion = regions.stream().filter(Region::isEnabled).findFirst().orElse(!regions.isEmpty() ? regions.get(0) : null);
	}

	public String getEnergyLensTableDir() {
		return energyLensTableDir;
	}

	public void setEnergyLensTableDir(String energyLensTableDir) {
		this.energyLensTableDir = energyLensTableDir;
	}

	public void setRegionValidator(RegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}

	public RegionValidator getRegionValidator() {
		return regionValidator;
	}

	@Override
	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	public String getRegionViewID() {
		return regionViewID;
	}

	public void setRegionViewID(String regionViewID) {
		this.regionViewID = regionViewID;
	}

	public boolean getCanEnableInvalidRegions() {
		return canEnableInvalidRegions;
	}

	public void setCanEnableInvalidRegions(boolean canEnableInvalidRegions) {
		this.canEnableInvalidRegions = canEnableInvalidRegions;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void setShowInvalidDialogOnSave(boolean showInvalidDialogOnSave) {
		this.showInvalidDialogOnSave = showInvalidDialogOnSave;
	}

	public boolean getShowInvalidDialogOnSave() {
		return showInvalidDialogOnSave;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	public boolean getUseCache() {
		return useCache;
	}
}