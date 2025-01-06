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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
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
import org.opengda.detector.electronanalyser.api.Command;
import org.opengda.detector.electronanalyser.api.SESExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSequenceHelper;
import org.opengda.detector.electronanalyser.api.SESSettingsService;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.selection.CaptureSequenceSnapshot;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.ExcitationEnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionValidationMessage;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceTableConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewLabelProvider;
import org.opengda.detector.electronanalyser.lenstable.IRegionValidator;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class SequenceViewCreator extends ViewPart implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, InitializationListener {

	public static final String ID = "org.opengda.detector.electronanalyser.client.sequencecreator";
	private final Logger logger = LoggerFactory.getLogger(SequenceViewCreator.class);

	public static final String ELEMENTSET_UNKNOWN = "UNKNOWN";
	public static final String FILE_NOT_SELECTED = "File not selected.";

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
	protected SESSequence sequence;
	protected List<SESRegion> regions = new ArrayList<>();
	private Command savedStates;
	private IRegionValidator regionValidator;
	private List<ISelectionChangedListener> selectionChangedListeners;

	private boolean isDirty;
	private Action addAction;
	private Action copyAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;

	private Combo comboElementSet;
	protected  String elementSet = ELEMENTSET_UNKNOWN;

	private StyledText txtSequenceFilePath;

	private Camera camera;
	private String regionViewID = RegionViewCreator.ID;
	private boolean showInvalidDialogOnSave = true;
	private boolean canEnableInvalidRegions = true;
	protected boolean canEdit = true;

	private ISelectionListener selectionListener = SequenceViewCreator.this::selectionListenerDetectedUpdate;

	private final PropertyChangeListener sequenceListener = event -> {
		//Don't do anything if property is the same
		final boolean isValidPropertyChange = validPropertyChange(event);
		if (!isValidPropertyChange) {
			return;
		}
		sequenceTableViewer.getControl().getDisplay().asyncExec(() -> {
			if (event.getSource().equals(sequence)) {
				sequenceTableViewer.refresh();
			} else if(event.getSource() instanceof SESRegion region) {
				sequenceTableViewer.refresh(region);
			}
			updateCalculatedData();
		});
		isDirty = true;
		firePropertyChange(PROP_DIRTY);
	};

	protected boolean validPropertyChange(PropertyChangeEvent event ) {
		return event.getOldValue() != event.getNewValue() && !event.getPropertyName().equals(SESRegion.STATUS) && canEdit;
	}

	//Have this method be only way to update elementSet value and display to UI
	protected void setElementSet(String newElementSet) {
		if(sequence != null && !elementSet.equals(newElementSet)) {
			logger.info("Updating elementSet from {} to {}", elementSet, newElementSet);
			elementSet = newElementSet;
			sequence.setElementSet(newElementSet);
		}
		comboElementSet.setText(newElementSet);
		validateAllRegions();
	}

	private void selectionListenerDetectedUpdate(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof EnergyChangedSelection energyChangeSelection) {
			final boolean showDialogIfInvalid = energyChangeSelection.isShowInvalidDialog();
			final SESRegion region = energyChangeSelection.getRegion();
			final boolean valid = isValidRegion(region, showDialogIfInvalid);
			if (!valid) {
				region.setEnabled(valid);
			}

		} else if (selection instanceof ExcitationEnergyChangedSelection excitationEnergyChangedSelection) {
			List<SESRegion> regionsAtExcitationEnergyValue = regions;
			final String excitationEnergySourceName = excitationEnergyChangedSelection.getExcitationEnergySourceName();
			if (ServiceProvider.getService(SESSettingsService.class).isExcitationEnergySourceSelectable()) {
				//Only validate regions that are with the associated source and energy mode is binding.
				regionsAtExcitationEnergyValue = regions.stream().filter(
					r -> r.getExcitationEnergySource().equals(excitationEnergySourceName) && r.isEnergyModeBinding()
				).toList();
			}
			logger.info("About to validate {} from source {}", regionsAtExcitationEnergyValue.stream().map(SESRegion::getName).toList(), sequence.getExcitationEnergySourceByName(excitationEnergySourceName));
			regionsAtExcitationEnergyValue.stream().forEach(r -> isValidRegion(r, false));
		} else if (selection instanceof IStructuredSelection sel) {
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof SESRegion region) {
				sequenceTableViewer.refresh(region);
				sequenceTableViewer.setSelection(sel);
			}
		} else if (selection instanceof CaptureSequenceSnapshot) {
			savedStates.addCommand(sequence);
		}
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
		final Composite rootComposite = createRootComposite(parent);
		createSequenceTableArea(rootComposite);
		final Composite controlArea = createControlArea(rootComposite, numberOfColumns);
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
			final ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection sel) {
				final Object firstElement = sel.getFirstElement();
				if (firstElement instanceof SESRegion firstElementRegion) {
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

		final SequenceViewLabelProvider labelProvider = new SequenceViewLabelProvider();
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
		final Group grpElementset = new Group(controlArea, SWT.NONE);
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
		final Group grpSequenceFile = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1).applyTo(grpSequenceFile);
		grpSequenceFile.setText("Sequence File in the table");
		grpSequenceFile.setLayout(new GridLayout());
		grpSequenceFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		txtSequenceFilePath = new StyledText(grpSequenceFile, SWT.NONE | SWT.READ_ONLY | SWT.H_SCROLL);
		txtSequenceFilePath.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		txtSequenceFilePath.setEditable(false);
		txtSequenceFilePath.setText(FILE_NOT_SELECTED);

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
		// add drag and drop support
		sequenceTableViewer.addDragSupport(
			DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK,
			new Transfer[] { LocalTransfer.getInstance() },
			new ViewerDragAdapter(sequenceTableViewer)
		);
		ViewerDropAdapter dropListener = new ViewerDropAdapter(sequenceTableViewer) {
			@Override
			public boolean performDrop(Object data) {
				if (data == null) return false;
				final TableViewer viewer = (TableViewer) getViewer();
				final List<SESRegion> tableData = Arrays.stream(viewer.getTable().getItems()).map(t -> (SESRegion) t.getData()).collect(Collectors.toList());
				final int targetIndex = tableData.indexOf(getCurrentTarget());
				final int selectedIndex = tableData.indexOf(getSelectedObject());
				if (targetIndex == -1 || selectedIndex == -1) return false;
				tableData.remove(selectedIndex);
				tableData.add(targetIndex, (SESRegion) getSelectedObject());
				sequence.setRegions(tableData);
				savedStates.addCommand(sequence);
				viewer.getTable().getDisplay().asyncExec(viewer::refresh);
				return true;
			}

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return canEdit;
			}
		};
		sequenceTableViewer.addDropSupport(
				DND.DROP_MOVE,
				new Transfer[] { LocalTransfer.getInstance() },
				dropListener
		);
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
				final SESRegion selectedRegion = getSelectedRegion();
				final int prevIndex = regions.indexOf(selectedRegion);
				final SESRegion newRegion = new SESRegion();
				final int nameCount = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), newRegion.getName()) + 1;
				//Update region with new name and ID
				newRegion.setName(newRegion.getName() + nameCount);
				newRegion.setRegionId(EcoreUtil.generateUUID());
				sequence.addRegion(newRegion);
				isValidRegion(newRegion, false);
				correctSelectionAfterAction(selectedRegion.getRegionId(), prevIndex);
				savedStates.addCommand(sequence);
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
				final SESRegion selectedRegion = getSelectedRegion();
				final int prevIndex = regions.indexOf(selectedRegion);
				if(getSelectedRegion() == null) {
					openMessageBox("No region selected", "You have not selected a region to duplicate.", MessageDialog.ERROR);
					return;
				}
				final SESRegion copy = new SESRegion(getSelectedRegion());
				final String regionNamePrefix = StringUtils.prefixBeforeInt(copy.getName());
				final int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), regionNamePrefix) +1;
				//Update region with new name and ID
				copy.setName(regionNamePrefix + largestIntInNames);
				copy.setRegionId(EcoreUtil.generateUUID());
				sequence.addRegion(copy);
				isValidRegion(copy, false);
				correctSelectionAfterAction(selectedRegion.getRegionId(), prevIndex);
				savedStates.addCommand(sequence);
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
				final SESRegion selectedRegion = getSelectedRegion();
				if(getSelectedRegion() == null) {
					openMessageBox("No region selected", "You have not selected a region to delete.", MessageDialog.ERROR);
					return;
				}
				final int prevIndex = regions.indexOf(selectedRegion);
				sequence.removeRegion(selectedRegion);
				correctSelectionAfterAction(selectedRegion.getRegionId(), prevIndex);
				savedStates.addCommand(sequence);
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_DELETE_OBJ));
		deleteAction.setToolTipText("Delete selected region");
	}

	private void correctSelectionAfterAction(String regionId, int prevIndex) {
		//Needed to update table with any removed or added regions
		sequenceTableViewer.refresh();
		final List<String> regionIds = regions.stream().map(r -> r.getRegionId()).toList();
		int index = regionIds.indexOf(regionId);
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
			final SESRegion newSelectedRegion = regions.get(index);
			sequenceTableViewer.setSelection(new StructuredSelection(newSelectedRegion));
		}
	}

	private void makeUndoAction() {
		undoAction = new Action() {
			@Override
			public void run() {
				final boolean undo = true;
				undoRedoAction(undo);
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
				final boolean undo = false;
				undoRedoAction(undo);
			}
		};
		redoAction.setText("Redo");
		redoAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_REDO_EDIT));
		redoAction.setToolTipText("Redo");
	}

	private void undoRedoAction(boolean undo) {
		final SESRegion selectedRegion = getSelectedRegion();
		final String regionId = selectedRegion != null ? selectedRegion.getRegionId() : "";
		final int prevIndex = regions.indexOf(selectedRegion);
		final String prevElementSet = sequence.getElementSet();

		final HashMap<String, Double> previousNameToExcitationEnergy = new HashMap<>();
		sequence.getExcitationEnergySources().stream().map(e-> previousNameToExcitationEnergy.put(e.getName(), e.getValue())).collect(Collectors.toList());
		if (undo) savedStates.undo(); else savedStates.redo();
		if (comboElementSet == null) {
			setElementSet(prevElementSet);
			previousNameToExcitationEnergy.forEach((name, value) -> sequence.getExcitationEnergySourceByName(name).setValue(value));
		}
		validateAllRegions();
		correctSelectionAfterAction(regionId, prevIndex);
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
		if (addAction != null) manager.add(addAction);
		if (deleteAction != null) manager.add(deleteAction);
		if (copyAction != null) manager.add(copyAction);
		if (undoAction != null) manager.add(undoAction);
		if (redoAction != null) manager.add(redoAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		if (addAction != null) manager.add(addAction);
		if (deleteAction != null) manager.add(deleteAction);
		if (copyAction != null) manager.add(copyAction);
		if (undoAction != null) manager.add(undoAction);
		if (redoAction != null) manager.add(redoAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		if (addAction != null) manager.add(addAction);
		if (deleteAction != null) manager.add(deleteAction);
		if (copyAction != null) manager.add(copyAction);
		if (undoAction != null) manager.add(undoAction);
		if (redoAction != null) manager.add(redoAction);
	}

	protected SESRegion getSelectedRegion() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection structuredSel) {
			Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof SESRegion firstElementRegion) {
				return firstElementRegion;
			}
		}
		return null;
	}

	protected void validateAllRegions() {
		regions.stream().forEach(r -> isValidRegion(r, false));
	}

	protected void fireSelectionChanged(SESRegion region) {
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

	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	@Override
	public void refreshTable(String seqFileName, boolean newFile) {
		logger.debug("refresh table with file: {}", seqFileName);
		if (sequence != null) {
			sequence.removePropertyChangeListener(sequenceListener);
		}
		sequenceTableViewer.setContentProvider(new ArrayContentProvider());
		regions = new ArrayList<>();
		if (sequence != null) sequence.removePropertyChangeListener(sequenceListener);
		sequence = null;
		try {
			if (newFile) {
				logger.debug("Creating new file {}", seqFileName);
				sequence = new SESSequence();
				SESSequenceHelper.saveSequence(sequence, seqFileName);
			}
			else {
				if (SESSequenceHelper.isFileXMLFormat(seqFileName)) {
					sequence = SESSequenceHelper.convertSequenceFileFromXMLToJSON(seqFileName);
				} else {
					sequence = SESSequenceHelper.loadSequence(seqFileName);
				}
			}
			if(sequence == null) throw new Exception("Sequence is null");
			sequence.addPropertyChangeListener(sequenceListener);
		} catch (Exception e) {
			logger.error("Error opening file.", e);
			String errorMessage = "Cannot open file \"" + seqFileName + "\". Encountered error: " + e;
			Display.getCurrent().asyncExec(() -> openMessageBox("Error opening file", errorMessage, SWT.ICON_ERROR));
			sequenceTableViewer.setInput(null); //Clear table as the sequence and regions are now null.
			sequenceTableViewer.refresh();
			txtSequenceFilePath.setText(FILE_NOT_SELECTED);
			return;
		}
		regions = sequence.getRegions();
		sequenceTableViewer.setInput(regions);
		txtSequenceFilePath.setText(seqFileName);

		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
		final List<SESExcitationEnergySource> excitationEnergySources = settings.getSESExcitationEnergySourceList();
		//If single source, we need to convert all regions to be that single source.
		if (!settings.isExcitationEnergySourceSelectable() && !excitationEnergySources.isEmpty())
			regions.stream().forEach(r -> r.setExcitationEnergySource(excitationEnergySources.get(0).getName()));

		updateCalculatedData();
		resetSelection();
		fireSelectionChanged(new FileSelection(seqFileName, sequence));

		if (comboElementSet != null) {
			setElementSet(sequence.getElementSet());
		} else {
			setElementSet(elementSet);
			//Only validate once on startup. setElementSet(value) will do validation for us. If not called, do it here instead.
			validateAllRegions();
		}
		logger.info("Sequence file {} loaded successfully.", seqFileName);
		sequenceTableViewer.refresh();
		if (savedStates == null) {
			savedStates = new Command(sequence);
		} else {
			savedStates.reset(sequence);
		}
	}

	private void resetSelection() {
		final SESRegion newSelectedRegion = getFirstEnabledRegion();
		if (newSelectedRegion == null) {
			fireSelectionChanged(StructuredSelection.EMPTY);
			return;
		}
		fireSelectionChanged(newSelectedRegion);
		sequenceTableViewer.setSelection(new StructuredSelection(newSelectedRegion));
	}

	protected SESRegion getFirstEnabledRegion() {
		return regions.stream().filter(SESRegion::isEnabled).findFirst().orElse(regions.isEmpty() ? null : regions.get(0));
	}

	private boolean isAllRegionsValid() {
		return regions.stream().filter(SESRegion::isEnabled).map(r -> isValidRegion(r, false)).allMatch(valid -> valid);
	}

	/**
	 * update the data model Status field according to region valid or not
	 *
	 * @param region
	 * @return
	 */
	protected boolean isValidRegion(SESRegion region, boolean showDialogIfInvalid) {
		if (regionValidator == null) {
			logger.info("No region validator provided, so region validation is NOT applied.");
			return true;
		}
		if(elementSet.equals(ELEMENTSET_UNKNOWN)) {
			logger.warn("Cannot validate region {} because elementSet is {}", region.getName(), ELEMENTSET_UNKNOWN);
			return true;
		}
		//Only do region validation if not during a scan.
		if (region.getStatus() == SESRegion.Status.RUNNING || region.getStatus() == SESRegion.Status.COMPLETED) {
			return true;
		}
		final double excitationEnergy = sequence.getExcitationEnergySourceByRegion(region).getValue();
		final boolean valid = regionValidator.isValidRegion(region, elementSet, excitationEnergy);
		final String message = valid ? "" : regionValidator.getErrorMessage();
		final Double minEnergy = regionValidator.getMinKE(elementSet, region);
		final Double maxEnergy = regionValidator.getMaxKE(elementSet, region);
		final boolean isKinetic = region.isEnergyModeKinetic();
		final Double lowEnergy = isKinetic ? minEnergy : maxEnergy;
		final Double highEnergy = isKinetic ? maxEnergy : minEnergy;
		final RegionValidationMessage regionValidationMessage = new RegionValidationMessage(region, message, lowEnergy, highEnergy);
		final SESRegion.Status status = valid ? SESRegion.Status.READY: SESRegion.Status.INVALID;
		updateRegionStatus(region, status);

		sequenceTableViewer.getTable().getDisplay().asyncExec(() -> fireSelectionChanged(regionValidationMessage));

		if (showDialogIfInvalid && !valid) {
			openMessageBox("Invalid Region", message, SWT.ICON_ERROR);
		}
		sequenceTableViewer.refresh(region);

		return valid;
	}

	protected void updateRegionStatus(final SESRegion region, final SESRegion.Status newStatus) {
		if (!region.getStatus().equals(newStatus)) {
			logger.info("Updating status of region {} from {} to {}", region.getName(), region.getStatus(), newStatus);
			region.setStatus(newStatus);
		}
		sequenceTableViewer.refresh(region);
	}

	@Override
	public void dispose() {
		try {
			sequence.removePropertyChangeListener(sequenceListener);
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(getRegionViewID(), selectionListener);
		} catch (Exception e) {
			logger.error("An error occured while disposting SequenceView", e);
		}
		super.dispose();
	}

	protected void openMessageBox(String title, String message, int iconStyle) {
		logger.debug("About to open message box with message: {}", message);
		final MessageBox dialog = new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if(sequence == null) return;
		final String fileName = getFilename();
		try {
			SESSequenceHelper.saveSequence(sequence, fileName);
		} catch (IOException e) {
			openMessageBox("Error on save", "An error occured saving sequence file \"" + fileName + "\" " + e, PROP_TITLE);
			return;
		}
		isDirty = false;
		firePropertyChange(PROP_DIRTY);
		if (!isAllRegionsValid()) {
			openMessageBox("Invalid region", "\"" + getFilename() + "\"contains invalid regions", PROP_TITLE);
		}
	}

	@Override
	public void doSaveAs() {
		final FileDialog fileDialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		final String filterPath = SESSequenceHelper.getDefaultFilePath();
		fileDialog.setFilterPath(filterPath);
		fileDialog.setOverwrite(true);
		fileDialog.setFilterExtensions(new String[] {"*.seq", "*.json"});
		final String fileName = fileDialog.open();
		if (fileName == null ) {
			logger.warn("File name selected is null on save as!");
			return;
		}
		try {
			SESSequenceHelper.saveSequence(sequence, fileName);
			refreshTable(fileName, false);
		} catch (IOException e) {
			openMessageBox("Error on save", "An error occured saving sequence file \"" + fileName + "\" " + e, PROP_TITLE);
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
		return regions.stream().map(SESRegion::getName).toList();
	}

	@Override
	public void setFocus() {
		sequenceTableViewer.getTable().setFocus();
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
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

	public void setRegionValidator(IRegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}

	public IRegionValidator getRegionValidator() {
		return regionValidator;
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

	public String getFilename() {
		if (txtSequenceFilePath != null) {
			return txtSequenceFilePath.getText();
		}
		return null;
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
			if (element instanceof SESRegion region && SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				return region.isEnabled();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (!SequenceTableConstants.ENABLED.equals(columnIdentifier) && !(value instanceof Boolean)) {
				return;
			}
			final SESRegion region = (SESRegion) element;
			Boolean enable = (Boolean) value;
			if (Boolean.TRUE.equals(enable)) {
				boolean valid = isValidRegion(region, true);
				if (getCanEnableInvalidRegions()) {
					valid = true;
				}
				enable = valid;
			}
			region.setEnabled(enable);
			savedStates.addCommand(sequence);
			sequenceTableViewer.refresh(region);
		}
	}
}