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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceTableConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewContentProvider;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewLabelProvider;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class SequenceCreatorView extends ViewPart implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(SequenceCreatorView.class);
	public static final String ID = "org.opengda.detector.electronanalyser.client.sequencecreator";

	private List<ISelectionChangedListener> selectionChangedListeners;
	private Camera camera;

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private int nameCount;

	private final String columnHeaders[] = { SequenceTableConstants.STATUS, SequenceTableConstants.ENABLED, SequenceTableConstants.REGION_NAME,
			SequenceTableConstants.LENS_MODE, SequenceTableConstants.PASS_ENERGY, SequenceTableConstants.X_RAY_SOURCE, SequenceTableConstants.ENERGY_MODE,
			SequenceTableConstants.LOW_ENERGY, SequenceTableConstants.HIGH_ENERGY, SequenceTableConstants.ENERGY_STEP, SequenceTableConstants.STEP_TIME,
			SequenceTableConstants.STEPS, SequenceTableConstants.TOTAL_TIME, SequenceTableConstants.X_CHANNEL_FROM, SequenceTableConstants.X_CHANNEL_TO,
			SequenceTableConstants.Y_CHANNEL_FROM, SequenceTableConstants.Y_CHANNEL_TO, SequenceTableConstants.SLICES, SequenceTableConstants.MODE };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 30, true), new ColumnWeightData(10, 30, true), new ColumnWeightData(80, 100, true),
			new ColumnWeightData(70, 90, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 80, true),
			new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 90, true), new ColumnWeightData(50, 70, true),
			new ColumnWeightData(50, 50, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 50, true), new ColumnWeightData(40, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 120, true) };

	private TableViewer sequenceTableViewer;
	private List<Region> regions;

	private Sequence sequence;
	private Spectrum spectrum;

	protected boolean isDirty;

	private Resource resource;

	private Action addAction;
	private Action copyAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;
	protected Action doubleClickAction;


	private String energyLensTableDir;

	private EditingDomain editingDomain;

	double totalSequenceTimes = 0.0;
	int numActives = 0;

	private Text txtSequenceFilePath;

	private Region currentRegion;
	protected int currentRegionNumber;
	protected volatile double time4RegionsCompletedInCurrentPoint;


	private double hardXRayEnergy;
	private double softXRayEnergy;

	private RegionValidator regionValidator;
	private String invalidRegionName;
	private Combo comboElementSet;


	private SelectionAdapter elementSetSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(comboElementSet)) {
				updateFeature(sequence, RegiondefinitionPackage.eINSTANCE.getSequence_ElementSet(), comboElementSet.getText());
			}
		}
	};

	private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
					&& !notification.getFeature().equals(RegiondefinitionPackage.eINSTANCE.getRegion_Status())) {
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		}
	};

	private ISelectionListener selectionListener = (part, selection) -> {
		if (selection instanceof EnergyChangedSelection) {
			Region region = ((EnergyChangedSelection) selection).getRegion();
			if (region.isEnabled()) {
				try {
					if (isValidRegion(region, true)) {
						runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), true));
					} else {
						runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), false));
					}
				} catch (Exception e) {
					logger.error("Error. ", e);
				}
			}
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object firstElement = sel.getFirstElement();
			if (firstElement instanceof Region) {
				sequenceTableViewer.setSelection(sel);
			}
		}
	};

	public SequenceCreatorView() {
		setTitleToolTip("Create a new or edit an existing sequence");
		setPartName("Sequence Editor");
		this.selectionChangedListeners = new ArrayList<>();
	}

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);

			column.setWidth(columnLayouts[i].minimumWidth);
			if (i == 0) {
				tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Region p = (Region) element;
						return p.getName();
					}

					@Override
					public String getToolTipText(Object element) {
						Region region = (Region) element;
						if (!isValidRegion(region, false)) {
							return region.getName() + " setting is outside energy range permitted.";
						} else {
							return null;
						}
					}

					@Override
					public Point getToolTipShift(Object object) {
						return new Point(5, 5);
					}

					@Override
					public int getToolTipDisplayDelayTime(Object object) {
						return 100; // msec
					}

					@Override
					public int getToolTipTimeDisplayed(Object object) {
						return 5000; // msec
					}
				});
			}

			tableViewerColumn.setEditingSupport(new SequenceColumnEditingSupport(tableViewer, tableViewerColumn));
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		GridLayout glRoot = new GridLayout();
		glRoot.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(glRoot);

		Composite tableViewerContainer = new Composite(rootComposite, SWT.None);

		sequenceTableViewer = new TableViewer(tableViewerContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		sequenceTableViewer.getTable().setHeaderVisible(true);
		sequenceTableViewer.getTable().setLinesVisible(true);

		sequenceTableViewer.addSelectionChangedListener(event -> {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region) {
					Region region = (Region) firstElement;
					fireSelectionChanged(region);
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
		SequenceViewLabelProvider labelProvider = new SequenceViewLabelProvider();
		labelProvider.setSourceSelectable(regionDefinitionResourceUtil.isSourceSelectable());
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			labelProvider.setXRaySourceEnergyLimit(regionDefinitionResourceUtil.getXRaySourceEnergyLimit());
		}
		labelProvider.setCamera(camera);
		sequenceTableViewer.setLabelProvider(labelProvider);
		regions = Collections.emptyList();

		Composite controlArea = new Composite(rootComposite, SWT.None);
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(3, false));

		Group grpElementset = new Group(controlArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpElementset);
		grpElementset.setLayout(new GridLayout());
		grpElementset.setText("Element Set");
		grpElementset.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		comboElementSet = new Combo(grpElementset, SWT.READ_ONLY);
		comboElementSet.setItems("Low", "High");
		comboElementSet.setToolTipText("Select an element set");
		comboElementSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboElementSet.setText(comboElementSet.getItem(0));

		Group grpSequenceFile = new Group(controlArea, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(grpSequenceFile);
		grpSequenceFile.setText("Sequence File in the table");
		grpSequenceFile.setLayout(new GridLayout());
		grpSequenceFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		txtSequenceFilePath = new Text(grpSequenceFile, SWT.NONE | SWT.READ_ONLY);
		txtSequenceFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSequenceFilePath.setEditable(false);

		initialisation();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(RegionCreatorView.ID, selectionListener);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sequenceTableViewer.getControl(), "org.opengda.analyser.ui.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		resetCurrentRegion();

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
					Region newRegion = RegiondefinitionFactory.eINSTANCE.createRegion();
					nameCount = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), newRegion.getName());
					if (nameCount != -1) {
						// increment the name
						nameCount++;
						newRegion.setName(newRegion.getName() + nameCount);
					}
					editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
							RegiondefinitionPackage.eINSTANCE.getSequence_Region(), newRegion));
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
					if (getSelectedRegion() != null) {
						Region copy = EcoreUtil.copy(getSelectedRegion());
						copy.setRegionId(EcoreUtil.generateUUID());
						String regionNamePrefix = StringUtils.prefixBeforeInt(copy.getName());
						int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getRegionNames(), regionNamePrefix);
						if (largestIntInNames != -1) {
							largestIntInNames++;
							copy.setName(regionNamePrefix + largestIntInNames);
						}
						editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_Region(), copy));
					} else {
						MessageDialog msgd = new MessageDialog(getViewSite().getShell(), "No region selected", null,
								"You have not selected a region to duplicate.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
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
					if (selectedRegion != null) {
						editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_Region(), selectedRegion));
					} else {
						MessageDialog msgd = new MessageDialog(getViewSite().getShell(), "No region selected", null,
								"You have not selected a region to delete.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					logger.error("Cannot delete region.", e1);
				}
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_DELETE_OBJ));
		deleteAction.setToolTipText("Delete selected region");
	}

	private void makeUndoAction() {
		undoAction = new Action() {

			@Override
			public void run() {
				try {
					editingDomain.getCommandStack().undo();
				} catch (Exception e1) {
					logger.error("Cannot undo action.", e1);
				}
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
				try {
					editingDomain.getCommandStack().redo();
				} catch (Exception e1) {
					logger.error("Cannot re-do action.", e1);
				}
			}
		};
		redoAction.setText("Redo");
		redoAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_REDO_EDIT));
		redoAction.setToolTipText("Redo");
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(manager -> SequenceCreatorView.this.fillContextMenu(manager));
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


	protected List<String> getRegionNames() {
		List<String> regionNames = new ArrayList<>();
		for (Region region : regions) {
			regionNames.add(region.getName());
		}
		return regionNames;
	}

	private Region getSelectedRegion() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof Region) {
				return (Region)firstElement;
			}
		}
		return null;
	}

	private void initialisation() {
		try {
			editingDomain = regionDefinitionResourceUtil.getEditingDomain();
		} catch (Exception e) {
			logger.error("Cannot get editing domain object.", e);
		}
		if (editingDomain == null) {
			throw new RuntimeException("Cannot get editing domain object.");
		}

		if (regionDefinitionResourceUtil != null) {
			try {
				sequence = regionDefinitionResourceUtil.getSequence();
			} catch (Exception e) {
				logger.error("Cannot get sequence from resource.", e);
			}
		}
		if (sequence != null) {
			spectrum = sequence.getSpectrum();
			if (spectrum != null) {
				txtSequenceFilePath.setText(regionDefinitionResourceUtil.getFileName());
			}
		} else {
			// start a new sequence
			if (regionDefinitionResourceUtil != null) {
				try {
					sequence = regionDefinitionResourceUtil.createSequence();
				} catch (Exception e) {
					logger.error("Cannot create new sequence file", e);
				}
			}
		}
		try {
			resource = regionDefinitionResourceUtil.getResource();
			resource.eAdapters().add(notifyListener);
			sequenceTableViewer.setInput(resource);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: " + regionDefinitionResourceUtil.getFileName(), e2);
		}

		// initialise region list
		regions = sequence.getRegion();
		// add drag and drop support,must ensure editing domain not null at this
		// point.
		sequenceTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new ViewerDragAdapter(sequenceTableViewer));

		sequenceTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new EditingDomainViewerDropAdapter(editingDomain, sequenceTableViewer));

		comboElementSet.addSelectionListener(elementSetSelAdaptor);
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

	private void fireSelectionChanged(Region region) {
		ISelection sel = StructuredSelection.EMPTY;
		if (region != null) {
			sel = new StructuredSelection(region);
		}
		fireSelectionChanged(sel);

	}

	private void fireSelectionChanged(ISelection sel) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, sel);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
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
			return SequenceTableConstants.ENABLED.equals(columnIdentifier);
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Region) {
				Region region = (Region) element;
				if (SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
					return region.isEnabled();
				}
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				Region region = (Region) element;
				if (value instanceof Boolean) {
					try {
						if ((boolean) value) {
							if (isValidRegion(region, true)) {
								runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), true));
							} else {
								runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), false));
							}
						}else {
							runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), value));
						}
						fireSelectionChanged(new RegionActivationSelection(region));
					} catch (Exception e) {
						logger.error("Unable to set value.", e);
					}
				}
			}
		}
	}

	protected void runCommand(final Command rmCommand) throws Exception {
		editingDomain.getCommandStack().execute(rmCommand);
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	@Override
	public void refreshTable(String seqFileName, boolean newFile) {
		logger.debug("refresh table with file: {}{}", FilenameUtils.getFullPath(seqFileName), FilenameUtils.getName(seqFileName));
		if (isDirty()) {
			doSave(new NullProgressMonitor());
		}
		if (txtSequenceFilePath.getText().trim().compareTo(seqFileName) == 0) {
			// same file no need to refresh
			return;
		}

		try {
			resource.eAdapters().remove(notifyListener);
			regionDefinitionResourceUtil.setFileName(seqFileName);
			if (newFile) {
				regionDefinitionResourceUtil.createSequence();
			}
			fireSelectionChanged(new FileSelection(seqFileName));
			Resource sequenceRes = regionDefinitionResourceUtil.getResource();
			sequenceTableViewer.setInput(sequenceRes);
			// update the resource in this view.
			resource = sequenceRes;
			resource.eAdapters().add(notifyListener);

			regions = regionDefinitionResourceUtil.getRegions();
			updateActiveRegionsExcitationEnergy(regions);

			if (regions.isEmpty()) {
				fireSelectionChanged(StructuredSelection.EMPTY);
			} else {
				resetCurrentRegion();
				if (currentRegion == null) {
					fireSelectionChanged(regions.get(0));
				} else {
					fireSelectionChanged(currentRegion);
				}
			}
			// update spectrum parameters
			spectrum = regionDefinitionResourceUtil.getSpectrum();
			txtSequenceFilePath.setText(regionDefinitionResourceUtil.getFileName());
			// update sequence run mode
			sequence = regionDefinitionResourceUtil.getSequence();
		} catch (Exception e) {
			logger.error("Cannot refresh table.", e);
		}
	}

	private void updateActiveRegionsExcitationEnergy(List<Region> regions) {
		for (Region region : regions) {
			if (region.isEnabled()) {
				updateRegionExcitationEnergy(region);
			}
		}
	}

	private void updateRegionExcitationEnergy(Region region) {
		double currentExcitationEnergy;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				currentExcitationEnergy = hardXRayEnergy;
			} else {
				currentExcitationEnergy = softXRayEnergy;
			}
		} else {
			currentExcitationEnergy = hardXRayEnergy;
		}
		if (currentExcitationEnergy != 0.0 && currentExcitationEnergy != region.getExcitationEnergy()) {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(), currentExcitationEnergy);
		}
	}

	@Override
	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	private boolean isAllRegionsValid() {
		boolean valid = true;
		for (Region region : regions) {
			if (region.isEnabled()) {
				// only check enabled regions. check stopped at first invalid region.
				if (valid) {
					boolean validRegion = isValidRegion(region, true);
					if (!validRegion) {
						invalidRegionName = region.getName();
					}
					valid = valid && validRegion;
				}
			}
		}
		return valid;
	}

	/**
	 * update the data model Status field according to region valid or not
	 *
	 * @param region
	 * @return
	 */
	private boolean isValidRegion(Region region, boolean showDialogIfInvalid) {
		String elementset = comboElementSet.getText().trim();
		if (regionValidator == null) {
			logger.info("No region validator provided, so region validation is NOT applied.");
			return true;
		}
		if (regionValidator.isValidRegion(region, elementset)) {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Status(), STATUS.READY);
			return true;
		} else {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Status(), STATUS.INVALID);
			if (showDialogIfInvalid) {
				String message = "Region '" + region.getName() + "' has energies (" + region.getLowEnergy() + " - " + region.getHighEnergy()
						+ ") outside the energy range (" + regionValidator.getEnergyRange(region, elementset) + ") permitted for \nElement Set: '"
						+ comboElementSet.getText() + "', Pass Energy: '" + region.getPassEnergy() + "' and Lens Mode: '" + region.getLensMode() + "'.\n";
				openMessageBox("Invalid Region", message, SWT.ICON_ERROR);
			}
			return false;
		}
	}

	private void openMessageBox(String title, String message, int iconStyle) {
		logger.debug("About to open message box with message: {}", message);
		MessageBox dialog = new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			regionDefinitionResourceUtil.getResource().save(null);
			isDirty = false;
			firePropertyChange(PROP_DIRTY);
			if (!isAllRegionsValid()) {
				logger.warn("File {} contains invalid active region {}.", regionDefinitionResourceUtil.getFileName(), invalidRegionName);
			} else {
				resetCurrentRegion();
				logger.info("All active regions in file {} are valid.", regionDefinitionResourceUtil.getFileName());
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
		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
		saveAsDialog.open();
		IPath path = saveAsDialog.getResult();
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null && resourceToSave != null) {
				String newFilename = file.getLocation().toOSString();
				regionDefinitionResourceUtil.saveAs(resourceToSave, newFilename);
				isDirty = false;
				firePropertyChange(PROP_DIRTY);
				refreshTable(newFilename, false);
			}
		}
		if (!isAllRegionsValid()) {
			logger.warn("File {} contains invalid active region {}.", regionDefinitionResourceUtil.getFileName(), invalidRegionName);
		} else {
			logger.info("All active regions in file {} are valid.", regionDefinitionResourceUtil.getFileName());
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
	public void dispose() {
		try {
			regionDefinitionResourceUtil.getResource().eAdapters().remove(notifyListener);
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(RegionCreatorView.ID, selectionListener);
		} catch (Exception e) {
			logger.error("An error occured while disposting SequenceView", e);
		}
		super.dispose();
	}

	// Update features when it changes in Region Editor
	private void updateFeature(EObject region, Object feature, Object value) {
		if (region != null && editingDomain != null) {
			Command setNameCmd = SetCommand.create(editingDomain, region, feature, value);
			editingDomain.getCommandStack().execute(setNameCmd);
		}
	}

	/**
	 * Sets {@link #currentRegion} to be the first enabled region from the list of regions
	 */
	private void resetCurrentRegion() {
		currentRegion = regions.stream().filter(Region::isEnabled).findFirst().orElse(regions.get(0));
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		// deleted logger message since analyser state pv is not needed
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


}
