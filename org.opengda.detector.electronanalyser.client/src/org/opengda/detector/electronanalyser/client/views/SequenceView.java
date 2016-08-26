package org.opengda.detector.electronanalyser.client.views;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.jobs.RegionJob;
import org.opengda.detector.electronanalyser.client.selection.EnergyChangedSelection;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.client.sequenceeditor.AnimatedTableItemFeedback;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceTableConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewContentProvider;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewLabelProvider;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.event.SequenceFileChangeEvent;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class SequenceView extends ViewPart implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, IObserver, InitializationListener {
	public static final String ID = "org.opengda.detector.electronanalyser.client.sequenceeditor";
	private static final Logger logger = LoggerFactory.getLogger(SequenceView.class);

	private List<ISelectionChangedListener> selectionChangedListeners;
	private Camera camera;

	public SequenceView() {
		setTitleToolTip("Create a new or editing an existing sequence");
		// setContentDescription("A view for editing sequence parameters");
		setPartName("Sequence Editor");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	}

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Text txtNumberActives;
	private int nameCount;

	private final String columnHeaders[] = { SequenceTableConstants.STATUS, SequenceTableConstants.ENABLED, SequenceTableConstants.REGION_NAME,
			SequenceTableConstants.LENS_MODE, SequenceTableConstants.PASS_ENERGY, SequenceTableConstants.X_RAY_SOURCE,
			SequenceTableConstants.ENERGY_MODE, SequenceTableConstants.LOW_ENERGY, SequenceTableConstants.HIGH_ENERGY,
			SequenceTableConstants.ENERGY_STEP, SequenceTableConstants.STEP_TIME, SequenceTableConstants.STEPS, SequenceTableConstants.TOTAL_TIME,
			SequenceTableConstants.X_CHANNEL_FROM, SequenceTableConstants.X_CHANNEL_TO, SequenceTableConstants.Y_CHANNEL_FROM,
			SequenceTableConstants.Y_CHANNEL_TO, SequenceTableConstants.SLICES, SequenceTableConstants.MODE };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 30, true), new ColumnWeightData(10, 30, true),
			new ColumnWeightData(80, 100, true), new ColumnWeightData(70, 90, true), new ColumnWeightData(40, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(50, 70, true),
			new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 90, true), new ColumnWeightData(50, 70, true),
			new ColumnWeightData(50, 50, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 120, true) };

	private TableViewer sequenceTableViewer;
	private List<Region> regions;

	private Sequence sequence;

	private Spectrum spectrum;

	protected boolean isDirty;

	private Resource resource;

	private Text txtEstimatedTime;

	private Action addAction;
	private Action copyAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;
	protected Action doubleClickAction;
	private Text txtDataFilePath;
	private Text txtPointValue;
	private Text txtRegionValue;
	private Text txtTimeRemaining;
	private ProgressBar progressBar;

	private int currentPointNumber;
	private int totalNumberOfPoints;
	private int crrentRegionNumber;
	private Text txtScanNumberValue;
	private Composite hardShutterState;
	private Button btnSoftShutter;
	private Composite softShutterState;

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(
					tableViewer, SWT.None);
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
							return region.getName()
									+ " setting is outside energy range permitted.";
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
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(gl_root);

		Composite tableViewerContainer = new Composite(rootComposite, SWT.None);

		sequenceTableViewer = new TableViewer(tableViewerContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		sequenceTableViewer.getTable().setHeaderVisible(true);
		sequenceTableViewer.getTable().setLinesVisible(true);

		sequenceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
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

		try {
			resource = regionDefinitionResourceUtil.getResource();
			resource.eAdapters().add(notifyListener);
			sequenceTableViewer.setInput(resource);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: "+regionDefinitionResourceUtil.getFileName(), e2);
		}

		Composite controlArea = new Composite(rootComposite, SWT.None);
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(3, false));

		if (getHardShutterPV()!=null || getSoftShutterPV()!=null) {
			Group grpShutters=new Group(controlArea, SWT.BORDER);
			GridData gd_grpShutters = new GridData(GridData.FILL_HORIZONTAL);
			gd_grpShutters.horizontalSpan=3;
			gd_grpShutters.grabExcessHorizontalSpace = true;
			grpShutters.setLayoutData(gd_grpShutters);
			grpShutters.setText("Fast Shutters");
			grpShutters.setLayout(new GridLayout(2, true));

			if (getHardShutterPV()!=null) {
				Group grpHardShutter=new Group(grpShutters, SWT.NONE);
				GridData gd_grpHardShutter = new GridData(SWT.LEFT, SWT.CENTER, true, false);
				grpHardShutter.setLayoutData(gd_grpHardShutter);
				grpHardShutter.setLayout(new GridLayout(3, false));

				Label lblHardXray=new Label(grpHardShutter, SWT.None);
				lblHardXray.setText("Hard X-Ray: ");

				/* Composite to contain the status composite so that a border can be displayed. */
				GridData fillHorizontalGD = new GridData();
				Composite borderComposite =  new Composite(grpHardShutter, SWT.NONE);
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				FillLayout fillLayout = new FillLayout();
				fillLayout.marginWidth = 2;
				fillLayout.marginHeight = 2;
				borderComposite.setLayout(fillLayout);
				fillHorizontalGD.horizontalIndent = 3;
				fillHorizontalGD.widthHint = 20;
				fillHorizontalGD.heightHint = 20;
				borderComposite.setLayoutData(fillHorizontalGD);
				hardShutterState = new Composite(borderComposite, SWT.FILL);

				btnHardShutter = new Button(grpHardShutter, SWT.PUSH);
				btnHardShutter.setText("Close");
				btnHardShutter.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if (event.getSource()==btnHardShutter) {
							if (btnHardShutter.getText().equalsIgnoreCase("Open")) {
								try {
									EpicsController.getInstance().caput(hardShutterChannel, 0);
								} catch (CAException | InterruptedException e) {
									logger.error("Failed to open fast shutter for hard X-ray",e);
								}
							} else if (btnHardShutter.getText().equalsIgnoreCase("Close")) {
								try {
									EpicsController.getInstance().caput(hardShutterChannel, 1);
								} catch (CAException | InterruptedException e) {
									logger.error("Failed to close fast shutter for hard X-ray",e);
								}
							}
						}
					}
				});

			} else {
				new Label(grpShutters, SWT.None);
			}

			if (getSoftShutterPV()!=null) {
				Group grpSoftShutter=new Group(grpShutters, SWT.NONE);
				GridData gd_grpSoftShutter = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
				grpSoftShutter.setLayoutData(gd_grpSoftShutter);
				grpSoftShutter.setLayout(new GridLayout(3, false));

				Label lblSoftXray=new Label(grpSoftShutter, SWT.None);
				lblSoftXray.setText("Soft X-Ray: ");

				/* Composite to contain the status composite so that a border can be displayed. */
				GridData fillHorizontalGD = new GridData();
				Composite borderComposite = new Composite(grpSoftShutter, SWT.NONE);
				borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				FillLayout fillLayout = new FillLayout();
				fillLayout.marginWidth = 2;
				fillLayout.marginHeight = 2;
				borderComposite.setLayout(fillLayout);
				fillHorizontalGD.horizontalIndent = 3;
				fillHorizontalGD.widthHint = 20;
				fillHorizontalGD.heightHint = 20;
				borderComposite.setLayoutData(fillHorizontalGD);
				softShutterState = new Composite(borderComposite, SWT.FILL);

				btnSoftShutter = new Button(grpSoftShutter, SWT.PUSH);
				btnSoftShutter.setText("CLose");
				btnSoftShutter.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if (event.getSource()==btnSoftShutter) {
							if (btnSoftShutter.getText().equalsIgnoreCase("Open")) {
								try {
									EpicsController.getInstance().caput(softShutterChannel, 0);
								} catch (CAException | InterruptedException e) {
									logger.error("Failed to open fast shutter for soft X-ray",e);
								}
							} else if (btnSoftShutter.getText().equalsIgnoreCase("Close")) {
								try {
									EpicsController.getInstance().caput(softShutterChannel, 1);
								} catch (CAException | InterruptedException e) {
									logger.error("Failed to close fast shutter for soft X-ray",e);
								}
							}
						}
					}
				});
			} else {
				new Label(grpShutters, SWT.None);
			}
		}
		Group grpElementset = new Group(controlArea, SWT.NONE);
		GridData gd_grpElementset = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpElementset.grabExcessHorizontalSpace = false;
		gd_grpElementset.widthHint = 120;
		grpElementset.setLayoutData(gd_grpElementset);
		grpElementset.setLayout(new GridLayout());
		grpElementset.setText("Element Set");
		comboElementSet = new Combo(grpElementset, SWT.READ_ONLY);
		comboElementSet.setItems(new String[] {"Low","High"});
		comboElementSet.setToolTipText("Select an element set");
		comboElementSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboElementSet.setText(comboElementSet.getItem(0));

		Group grpActiveRegions = new Group(controlArea, SWT.NONE);
		GridData gd_grpActiveRegions = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpActiveRegions.grabExcessHorizontalSpace = false;
		grpActiveRegions.setLayoutData(gd_grpActiveRegions);
		grpActiveRegions.setText("Number of Active Regions");
		grpActiveRegions.setLayout(new GridLayout());
		txtNumberActives = new Text(grpActiveRegions, SWT.BORDER | SWT.RIGHT);
		txtNumberActives.setForeground(ColorConstants.green);
		txtNumberActives.setBackground(ColorConstants.black);
		txtNumberActives.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumberActives.setEditable(false);

		Group grpTotalTime = new Group(controlArea, SWT.None);
		GridData gd_grpTotalTime = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpTotalTime.horizontalAlignment = SWT.LEFT;
		gd_grpTotalTime.widthHint = 100;
		gd_grpTotalTime.grabExcessHorizontalSpace = false;
		grpTotalTime.setLayoutData(gd_grpTotalTime);
		grpTotalTime.setText("Total Sequence Time");
		grpTotalTime.setLayout(new GridLayout());
		txtEstimatedTime = new Text(grpTotalTime, SWT.BORDER | SWT.RIGHT);
		txtEstimatedTime.setForeground(ColorConstants.green);
		txtEstimatedTime.setBackground(ColorConstants.black);
		txtEstimatedTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtEstimatedTime.setEditable(false);

		Group grpSequenceFile = new Group(controlArea, SWT.None);
		GridData gd_grpSequenceFile = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpSequenceFile.horizontalSpan=3;
		gd_grpSequenceFile.grabExcessHorizontalSpace = true;
		grpSequenceFile.setLayoutData(gd_grpSequenceFile);
		grpSequenceFile.setText("Sequence File in the table");
		grpSequenceFile.setLayout(new GridLayout());
		txtSequenceFilePath = new Text(grpSequenceFile, SWT.BORDER | SWT.READ_ONLY);
		txtSequenceFilePath.setForeground(ColorConstants.green);
		txtSequenceFilePath.setBackground(ColorConstants.black);
		txtSequenceFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSequenceFilePath.setEditable(false);

		Group grpDataFile = new Group(controlArea, SWT.None);
		GridData gd_grpDataFile = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpDataFile.horizontalSpan=3;
		gd_grpDataFile.grabExcessHorizontalSpace = true;
		grpDataFile.setLayoutData(gd_grpDataFile);
		grpDataFile.setText("Data File");
		grpDataFile.setLayout(new GridLayout());
		txtDataFilePath = new Text(grpDataFile, SWT.BORDER | SWT.READ_ONLY);
		txtDataFilePath.setBackground(ColorConstants.black);
		txtDataFilePath.setForeground(ColorConstants.green);
		txtDataFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtDataFilePath.setEditable(false);
		txtDataFilePath.setText("Data file to be collected");

		Group grpScanProgress=new Group(controlArea, SWT.BORDER);
		GridData gd_grpScanProgress = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpScanProgress.horizontalSpan=3;
		gd_grpScanProgress.grabExcessHorizontalSpace = true;
		grpScanProgress.setLayoutData(gd_grpScanProgress);
		grpScanProgress.setText("Analyser Scan Progress");
		grpScanProgress.setLayout(new GridLayout(4, false));

		Label lblScanNumber=new Label(grpScanProgress, SWT.None);
		lblScanNumber.setText("Current Scan Number: ");

		txtScanNumberValue = new Text(grpScanProgress, SWT.BORDER);
		GridData gd_txtScanNumberValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtScanNumberValue.widthHint = 50;
		txtScanNumberValue.setLayoutData(gd_txtScanNumberValue);
		txtScanNumberValue.setForeground(ColorConstants.green);
		txtScanNumberValue.setEditable(false);
		txtScanNumberValue.setBackground(ColorConstants.black);
		try {
			txtScanNumberValue.setText(String.format("%d",new NumTracker(LocalProperties.GDA_BEAMLINE_NAME).getCurrentFileNumber()));
		} catch (IOException e) {
			logger.warn("Failed to get scan number tracker");
		}

		Label lblPoint=new Label(grpScanProgress, SWT.None);
		lblPoint.setText("Scan Point Number: ");

		txtPointValue = new Text(grpScanProgress, SWT.BORDER);
		GridData gd_lblIterationValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblIterationValue.widthHint = 50;
		txtPointValue.setLayoutData(gd_lblIterationValue);
		txtPointValue.setForeground(ColorConstants.green);
		txtPointValue.setEditable(false);
		txtPointValue.setBackground(ColorConstants.black);
		updateScanPointNumber(currentPointNumber, totalNumberOfPoints);

		Label lblRegion = new Label(grpScanProgress, SWT.NONE);
		lblRegion.setText("Active Region Number:");

		txtRegionValue = new Text(grpScanProgress, SWT.BORDER);
		txtRegionValue.setForeground(ColorConstants.green);
		txtRegionValue.setEditable(false);
		txtRegionValue.setBackground(ColorConstants.black);
		txtRegionValue.setText("0");
		GridData gd_txtCurrentPoint = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_txtCurrentPoint.widthHint = 50;
		txtRegionValue.setLayoutData(gd_txtCurrentPoint);

		Label lblTimeRemaining = new Label(grpScanProgress, SWT.NONE);
		lblTimeRemaining.setText("Time Remaining:");

		txtTimeRemaining = new Text(grpScanProgress, SWT.BORDER);
//		gd_txtIterationTimeRemaining.widthHint=40;
		txtTimeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		txtTimeRemaining.setForeground(ColorConstants.green);
		txtTimeRemaining.setBackground(ColorConstants.black);
		GridData gd_txtTimeRemaining = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_txtTimeRemaining.widthHint = 50;
		txtTimeRemaining.setLayoutData(gd_txtTimeRemaining);
		txtTimeRemaining.setEditable(false);

		Label lblProgress = new Label(grpScanProgress, SWT.NONE);
		lblProgress.setText("Scan Progress:");

		progressBar = new ProgressBar(grpScanProgress, SWT.HORIZONTAL);
		GridData gd_progressBar = new GridData(GridData.FILL_HORIZONTAL);
		gd_progressBar.grabExcessHorizontalSpace = false;
		gd_progressBar.horizontalSpan = 3;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);

		initialisation();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(RegionView.ID, selectionListener);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sequenceTableViewer.getControl(), "org.opengda.analyser.ui.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void setShutterState(Composite shutterState,int status) {
		setColourControl(shutterState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_RED);
	}

	private void setColourControl(final Control control, final int statusInt, final int openColour,
			final int closeColour) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.isDisposed()) {
						if (statusInt == 0) {
							control.setBackground(control.getDisplay().getSystemColor(openColour));
						} else if (statusInt == 1) {
							control.setBackground(control.getDisplay().getSystemColor(closeColour));
						}
					}
				}
			});
		}
	}
	private void updateRegionNumber(int currentRegionNumber, int totalActiveRegions) {
		txtRegionValue.setText(String.valueOf(currentRegionNumber) + '/'+ String.valueOf(totalActiveRegions));
	}

	private void updateScanPointNumber(int currentPointNumber,
			int totalNumberOfPoints) {
		txtPointValue.setText(String.valueOf(currentPointNumber) + '/'+ String.valueOf(totalNumberOfPoints));
	}

	private void makeActions() {
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
					editingDomain.getCommandStack().execute(
							AddCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
									RegiondefinitionPackage.eINSTANCE.getSequence_Region(), newRegion));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		addAction.setText("Add");
		addAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_ADD_OBJ));
		addAction.setToolTipText("Add a new region");

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
						editingDomain.getCommandStack().execute(
								AddCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
										RegiondefinitionPackage.eINSTANCE.getSequence_Region(), copy));
					} else {
						MessageDialog msgd = new MessageDialog(getViewSite().getShell(), "No region selected", null,
								"You have not selected a region to duplicate.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		copyAction.setText("Copy");
		copyAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_COPY_EDIT));
		copyAction.setToolTipText("Copy selected region");

		deleteAction = new Action() {

			@Override
			public void run() {
				try {
					Region selectedRegion = getSelectedRegion();
					if (selectedRegion != null) {
						editingDomain.getCommandStack().execute(
								RemoveCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
										RegiondefinitionPackage.eINSTANCE.getSequence_Region(), selectedRegion));
					} else {
						MessageDialog msgd = new MessageDialog(getViewSite().getShell(), "No region selected", null,
								"You have not selected a region to delete.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_DELETE_OBJ));
		deleteAction.setToolTipText("Delete selected region");

		undoAction = new Action() {

			@Override
			public void run() {
				try {
					editingDomain.getCommandStack().undo();
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		};
		undoAction.setText("Undo");
		undoAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_UNDO_EDIT));
		undoAction.setToolTipText("Undo");


		redoAction = new Action() {

			@Override
			public void run() {
				try {
					editingDomain.getCommandStack().redo();
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
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
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				SequenceView.this.fillContextMenu(manager);
			}
		});
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

	protected void updateRegionStatus(final RegionJob regionJob, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				Region region = regionJob.getRegion();
				region.setStatus(status);
				sequenceTableViewer.refresh();
			}
		});
	}

	protected void updateRegionStatus(final Region region, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				region.setStatus(status);
				sequenceTableViewer.refresh();
			}
		});
	}

	protected void resetRegionStatus() {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				for (Region region : regions) {
					if (region.isEnabled()) {
						region.setStatus(STATUS.READY);
					}
				}
				sequenceTableViewer.refresh();
			}
		});
	}

	protected List<String> getRegionNames() {
		List<String> regionNames = new ArrayList<String>();
		for (Region region : regions) {
			regionNames.add(region.getName());
		}
		return regionNames;
	}

	private ISelectionListener selectionListener = new INullSelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof TotalTimeSelection) {
				updateCalculatedData();
			} else if (selection instanceof EnergyChangedSelection) {
				Region region=((EnergyChangedSelection)selection).getRegion();
				if (region.isEnabled()) {
					try {
						if(isValidRegion(region, true)) {
							runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), true));
						} else {
							runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), false));
						}
						updateCalculatedData();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region) {
					sequenceTableViewer.setSelection(sel);
				}
			}
		}
	};

	private EditingDomain editingDomain;

	private Scriptcontroller scriptcontroller;

	private AnalyserStateListener analyserStateListener;
	private AnalyserTotalTimeRemainingListener analyserTotalTimeRemainingListener;
	private SoftShutterStateListener softShutterStateListener;
	private HardShutterStateListener hardShutterStateListener;

	private String analyserStatePV;
	private String analyserTotalTimeRemianingPV;
	private String hardShutterPV;
	private String softShutterPV;


	private Channel analyserStateChannel;
	private Channel analyserTotalTimeRemainingChannel;
	private Channel hardShutterChannel;
	private Channel softShutterChannel;

	private EpicsChannelManager channelmanager;

	private String energyLensTableDir;

	private Region getSelectedRegion() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof Region) {
				Region region = (Region) firstElement;
				return region;
			}
		}
		return null;
	}

	private IVGScientaAnalyser analyser;

	private SelectionAdapter elementSetSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(comboElementSet)) {
				updateFeature(sequence, RegiondefinitionPackage.eINSTANCE.getSequence_ElementSet(), comboElementSet.getText());
				if (!isAllRegionsValid()) {
					openMessageBox("Invalid Regions", "There are invalid active regions in the Sequence Editor. \nCorrect and validate all active regions by de-select and re-select them." , SWT.ICON_WARNING);
					return;
				}
			}
		}
	};

	private void initialisation() {
		try { // populate Combo list from EPICS PV
			comboElementSet.removeAll();
			comboElementSet.setItems(getAnalyser().getElementSet());
		} catch (DeviceException e) {
			logger.error("Cannot get element set list from analyser.", e);
			e.printStackTrace();
		}
		try { // initialise with the current PV value
			comboElementSet.setText(getAnalyser().getElement());
		} catch (Exception e) {
			logger.error("Cannot get the current element set from analyser.", e);
			e.printStackTrace();
		}

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
		// initialise region list
		regions= sequence.getRegion();
		// add drag and drop support,must ensure editing domain not null at this
		// point.
		sequenceTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new ViewerDragAdapter(sequenceTableViewer));

		sequenceTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new EditingDomainViewerDropAdapter(editingDomain, sequenceTableViewer));

		updateCalculatedData();
		//server event admin or handler
		scriptcontroller = Finder.getInstance().find("SequenceFileObserver");
		scriptcontroller.addIObserver(this);
		//EPICS monitor to update current region status
		channelmanager = new EpicsChannelManager(this);
		analyserStateListener = new AnalyserStateListener();
		analyserTotalTimeRemainingListener=new AnalyserTotalTimeRemainingListener();
		hardShutterStateListener=new HardShutterStateListener();
		softShutterStateListener=new SoftShutterStateListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
		comboElementSet.addSelectionListener(elementSetSelAdaptor);
		updateRegionNumber(crrentRegionNumber, numActives);
		images = loadAnimatedGIF(sequenceTableViewer.getControl().getDisplay(), ImageConstants.ICON_RUNNING);
		dcmenergy = Finder.getInstance().find("dcmenergy");
		if (dcmenergy == null) {
			logger.error("Finder failed to find 'dcmenergy'");
		} else {
			dcmenergy.addIObserver(this);
		}
		pgmenergy = Finder.getInstance().find("pgmenergy");
		if (pgmenergy == null) {
			logger.error("Finder failed to find 'pgmenergy'");
		} else {
			pgmenergy.addIObserver(this);
		}
		try {
			hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
		} catch (DeviceException e) {
			logger.error("Cannot get X-ray energy from DCM.", e);
		}
		try {
			softXRayEnergy = (double) pgmenergy.getPosition();
		} catch (DeviceException e) {
			logger.error("Cannot get X-ray energy from PGM.", e);
		}

	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	private void createChannels() throws CAException, TimeoutException {
		first = true;
		if (getDetectorStatePV()!=null) {
			analyserStateChannel = channelmanager.createChannel(getDetectorStatePV(), analyserStateListener, MonitorType.NATIVE, false);
		}
		if (getAnalyserTotalTimeRemianingPV()!=null) {
			analyserTotalTimeRemainingChannel=channelmanager.createChannel(getAnalyserTotalTimeRemianingPV(), analyserTotalTimeRemainingListener,MonitorType.NATIVE, false);
		}
		if (getHardShutterPV()!=null) {
			hardShutterChannel=channelmanager.createChannel(getHardShutterPV(), hardShutterStateListener,MonitorType.NATIVE, false);
		}
		if (getSoftShutterPV()!=null) {
			softShutterChannel=channelmanager.createChannel(getSoftShutterPV(), softShutterStateListener,MonitorType.NATIVE, false);
		}
		channelmanager.creationPhaseCompleted();
		logger.debug("analyser state channel and monitor are created");
	}

	double totalTimes = 0.0;
	int numActives = 0;
	private String invalidRegionName;

	private void updateCalculatedData() {
		double totalTimes = 0.0;
		int numActives = 0;
		if (!regions.isEmpty()) {
			for (Region region : regions) {
				if (region.isEnabled()) {
					numActives++;
					if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
						totalTimes += region.getStepTime()
								* RegionStepsTimeEstimation.calculateTotalSteps((region.getHighEnergy() - region.getLowEnergy()),
										region.getEnergyStep(), camera.getEnergyResolution() * region.getPassEnergy()
												* (region.getLastXChannel() - region.getFirstXChannel() + 1));
					} else if (region.getAcquisitionMode() == ACQUISITION_MODE.FIXED) {
						totalTimes += region.getStepTime() * 1;
					}
				}
			}
		}
		txtNumberActives.setText(String.format("%d", numActives));
		txtEstimatedTime.setText(String.format("%.3f", totalTimes));
		this.totalTimes=totalTimes;
		this.numActives=numActives;
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
			if (SequenceTableConstants.ENABLED.equals(columnIdentifier)) {
				return true;
			}
			return false;
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
				Region region = (Region)element;
				if (value instanceof Boolean) {
					try {
						if ((boolean) value) {
							if(isValidRegion(region, true)) {
								runCommand(SetCommand.create(editingDomain, element, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), true));
							} else {
								runCommand(SetCommand.create(editingDomain, element, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), false));
							}
						} else {
							runCommand(SetCommand.create(editingDomain, region, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), value));
						}
						fireSelectionChanged(new RegionActivationSelection(region));
						updateCalculatedData();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void openMessageBox(String title, String message, int iconStyle) {
		MessageBox dialog=new MessageBox(getSite().getShell(), iconStyle | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
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
			InterfaceProvider.getCurrentScanController().pauseCurrentScan();
			InterfaceProvider.getScriptController().pauseCurrentScript();
//			MessageDialog msgDialog = new MessageDialog(getViewSite().getShell(), "Unsaved Data", null,
//					"Current sequence contains unsaved data. Do you want to save them first?", MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);
//			int result = msgDialog.open();
//			if (result == 0) {
//				doSave(new NullProgressMonitor());
//			} else {
//				isDirty = false;
//				firePropertyChange(PROP_DIRTY);
//			}
			doSave(new NullProgressMonitor());
			InterfaceProvider.getScriptController().resumeCurrentScript();
			InterfaceProvider.getCurrentScanController().restartCurrentScan();
		}
		if (txtSequenceFilePath.getText().trim().compareTo(seqFileName)==0) {
			//same file no need refresh
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

			// update existing active regions' excitation energy
			regions = regionDefinitionResourceUtil.getRegions();
			for (Region region : regions) {
				if (region.isEnabled()) {
					double currentExcitationEnergy = 0.0;
					if (regionDefinitionResourceUtil.isSourceSelectable()) {
						if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
							currentExcitationEnergy = hardXRayEnergy;
						} else {
							currentExcitationEnergy = softXRayEnergy;
						}
					} else {
						currentExcitationEnergy = hardXRayEnergy;
					}
					if (currentExcitationEnergy != 0.0
							&& currentExcitationEnergy != region
									.getExcitationEnergy()) {
						updateFeature(region,
								RegiondefinitionPackage.eINSTANCE
										.getRegion_ExcitationEnergy(),
								currentExcitationEnergy);
					}
					if (!isValidRegion(region, false)) {
						updateFeature(region,RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), false);
					}
				}
			}

			if (regions.isEmpty()) {
				fireSelectionChanged(StructuredSelection.EMPTY);
			} else {
				for (Region region : regions) {
					if (region.isEnabled()) {
						currentRegion = region;
						break;
					}
				}
				if (currentRegion==null) {
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
			updateCalculatedData();
		} catch (Exception e) {
			logger.error("Cannot refresh table.", e);
		}
	}

	@Override
	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
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
				logger.info("All active regions in file {} are valid.", regionDefinitionResourceUtil.getFileName());
			}
		} catch (IOException e) {
			logger.error("Cannot save the resource to a file.", e);
		} catch (Exception e) {
			logger.error("Cannot get resource from RegionDefinitionResourceUtil.", e);
		}
	}

	private boolean isAllRegionsValid() {
		boolean valid = true;
		for (Region region : regions) {
			if (region.isEnabled()) {
				// only check enabled regions. check stopped at first invalid region.
				if (valid) {
					boolean validRegion = isValidRegion(region, true);
					if (!validRegion) {
						invalidRegionName=region.getName();
					}
					valid = valid && validRegion;
				}
			}
		}
		return valid;
	}
	/**
	 * update the data model Status field according to region valid or not
	 * @param region
	 * @return
	 */
	private boolean isValidRegion(Region region, boolean showDialogIfInvalid) {
		String elementset = comboElementSet.getText().trim();
		if (regionValidator == null) {
			logger.info("No region validator provided, so region validation is NOT applied.");
			return true;
		}
		//use current beam photon energy, not what recorded in the region
		double currentExcitationEnergy=0.0;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy()>regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				currentExcitationEnergy = hardXRayEnergy;
			} else {
				currentExcitationEnergy = softXRayEnergy;
			}
		} else {
			currentExcitationEnergy = hardXRayEnergy;
		}
		if (regionValidator.isValidRegion(region, elementset, currentExcitationEnergy)) {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Status(), STATUS.READY);
			return true;
		} else {
			updateFeature(region, RegiondefinitionPackage.eINSTANCE.getRegion_Status(), STATUS.INVALID);
			if (showDialogIfInvalid) {
				String message="Region '" + region.getName()+"' has energies ("+region.getLowEnergy()+" - "+region.getHighEnergy()+") outside the energy range ("+regionValidator.getEnergyrange()+") permitted for \nElement Set: '"+comboElementSet.getText()+"', Pass Energy: '"+region.getPassEnergy()+"' and Lens Mode: '"+region.getLensMode()+"'.\n";
				openMessageBox("Invalid Region", message, SWT.ICON_ERROR);
			}
			return false;
		}
	}

	@Override
	public void doSaveAs() {
		Resource resource = null;
		try {
			resource = regionDefinitionResourceUtil.getResource();
		} catch (Exception e1) {
			logger.warn("Cannot find the resouce from sequence file.");
		}
		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
		saveAsDialog.open();
		IPath path = saveAsDialog.getResult();
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null && resource != null) {
				String newFilename = file.getLocation().toOSString();
				regionDefinitionResourceUtil.saveAs(resource, newFilename);
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

	private Text txtSequenceFilePath;

	private Region currentRegion;
	protected int currentRegionNumber;
	private double totalScanTime;
	private double time4ScanPointsDone;
	private double time4RegionsToDo;

	@Override
	public void dispose() {
		try {
			regionDefinitionResourceUtil.getResource().eAdapters().remove(notifyListener);
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(RegionView.ID, selectionListener);
			// analyserStateChannel.dispose();
			scriptcontroller.deleteIObserver(this);
//			if (animation != null) {
//				animation.cancelAnimation();
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
	}

	// Update features when it changes in Region Editor
	private void updateFeature(EObject region, Object feature, Object value) {
		if (region != null) {
			if (editingDomain != null) {
				Command setNameCmd = SetCommand.create(editingDomain, region, feature, value);
				editingDomain.getCommandStack().execute(setNameCmd);
			}
		}
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
	AnimationEngine animation = null;
	private double hardXRayEnergy;
	private double softXRayEnergy;

	@Override
	@SuppressWarnings("restriction")
	public void update(Object source, final Object arg) {
		if (source == scriptcontroller) {
			if (arg instanceof SequenceFileChangeEvent) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						logger.debug("Sequence file changed to {}",((SequenceFileChangeEvent) arg).getFilename());
						refreshTable(((SequenceFileChangeEvent) arg).getFilename(),false);
					}
				});
			}
			if (arg instanceof RegionChangeEvent) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						logger.debug("region update to {}",((RegionChangeEvent) arg).getRegionName());
						String regionId = ((RegionChangeEvent) arg).getRegionId();
						for (Region region : regions) {
							if (region.getRegionId().equalsIgnoreCase(regionId)) {
								if (currentRegion != region) {
									updateRegionStatus(currentRegion, STATUS.COMPLETED);
								}
								currentRegion = region;
							}
						}
						fireSelectionChanged(currentRegion);
						sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
						if (animation!=null) {
							animation.cancelAnimation();
						}
						try {
							TableItem tableItem = sequenceTableViewer.getTable().getItem(regions.indexOf(currentRegion));
							AnimatedTableItemFeedback feedback = new AnimatedTableItemFeedback(sequenceTableViewer.getControl().getShell(),images, tableItem,SequenceTableConstants.COL_STATUS);
							animation= new AnimationEngine(feedback,-1,100);
							animation.schedule();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			if (arg instanceof RegionStatusEvent) {
				final String regionId = ((RegionStatusEvent) arg).getRegionId();
				final STATUS status = ((RegionStatusEvent) arg).getStatus();
				currentRegionNumber = ((RegionStatusEvent) arg).getRegionNumber();
				if (status==STATUS.RUNNING) {
					time4RegionsToDo=getRemainingRegionsTimeTotal(currentRegionNumber);
				} else if (status==STATUS.COMPLETED) {
					time4RegionsToDo=getRemainingRegionsTimeTotal(currentRegionNumber+1);
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateRegionNumber(currentRegionNumber, numActives);
						logger.debug("region {} update to {}",regionId, status);
						for (Region region : regions) {
							if (region.getRegionId().equalsIgnoreCase(regionId)) {
								updateRegionStatus(region, status);
							}
						}

						if (status==STATUS.COMPLETED) {
							fireSelectionChanged(new RegionRunCompletedSelection());
						}
					}
				});
			}
			if (arg instanceof ScanStartEvent) {
				ScanStartEvent evt = (ScanStartEvent) arg;
				totalNumberOfPoints = evt.getNumberOfPoints();
				final String scanFilename = evt.getScanFilename();
				final int scanNumber = evt.getScanNumber();
				totalScanTime=totalNumberOfPoints*totalTimes;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						updateScanPointNumber(currentPointNumber,totalNumberOfPoints);
						txtDataFilePath.setText(scanFilename);
						txtScanNumberValue.setText(String.valueOf(scanNumber));
					}
				});
				firstTime = true;
				taskTimer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								double scanTimeRemaining = totalScanTime - time4ScanPointsDone + time4RegionsToDo + currentregiontimeremaining;
								if (scanTimeRemaining<1) {
									scanTimeRemaining=0;
								}
								if (firstTime) {
									txtTimeRemaining.setText(String.format("%.3f",scanTimeRemaining));
									firstTime=false;
								} else if (scanTimeRemaining<Double.valueOf(txtTimeRemaining.getText().trim())) {
									txtTimeRemaining.setText(String.format("%.3f",scanTimeRemaining));
								}
								progressBar.setSelection((int)(100 * ((totalScanTime - scanTimeRemaining) / totalScanTime)));
							}
						});
					}
				}, 1000, 2000);
			}
			if (arg instanceof ScanPointStartEvent) {
				currentPointNumber=((ScanPointStartEvent)arg).getCurrentPointNumber();
				time4ScanPointsDone=currentPointNumber*totalTimes;
				time4RegionsToDo=getRemainingRegionsTimeTotal(0);
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						updateScanPointNumber(currentPointNumber,totalNumberOfPoints);
					}
				});
			}
			if (arg instanceof ScanEndEvent) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						for (Region region : regions) {
							if (region.isEnabled()) {
								updateRegionStatus(region, STATUS.READY);
							}
						}
						if (animation!=null) {
							animation.cancelAnimation();
						}
						taskTimer.cancel();
					}
				});
			}
		}
		if (source == dcmenergy && arg instanceof ScannableStatus) {
			if (((ScannableStatus) arg).getStatus() == ScannableStatus.IDLE) {
				try {
					hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from DCM.", e);
				}
			}
		}
		if (source == pgmenergy && arg instanceof ScannableStatus) {
			if (((ScannableStatus) arg).getStatus() == ScannableStatus.IDLE) {
				try {
					softXRayEnergy = (double) pgmenergy.getPosition();
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from PGM.", e);
				}
			}
		}

	}
	private Image[] loadAnimatedGIF(Display display, String imagePath) {
		URL url = FileLocator.find(ElectronAnalyserClientPlugin.getDefault().getBundle(), new Path(imagePath), null);
		ImageLoader imageLoader = new ImageLoader();
		try {
			imageLoader.load(url.openStream());
		} catch (IOException e) {
			logger.error("Cannot load animated gif file {}", url.getPath());
		}
		Image[] images = new Image[imageLoader.data.length];
		for (int i = 0; i < imageLoader.data.length; ++i) {
			ImageData nextFrameData = imageLoader.data[i];
			images[i] = new Image(display, nextFrameData);
		}
		return images;
	}

	public String getDetectorStatePV() {
		return analyserStatePV;
	}

	public void setDetectorStatePV(String statePV) {
		this.analyserStatePV = statePV;
	}

	private boolean first = true;

	private Combo comboElementSet;
	private Image[] images;
	private double currentregiontimeremaining;
	private Timer taskTimer=new Timer();
	private boolean firstTime;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private RegionValidator regionValidator;
	private Button btnHardShutter;

	private class AnalyserTotalTimeRemainingListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				currentregiontimeremaining = ((DBR_Double) dbr).getDoubleValue()[0];
				logger.debug("iteration time remaining changed to {}",currentregiontimeremaining);
			}
		}
	}

	private double getRemainingRegionsTimeTotal(int currentRegionNumber2) {
		double timeToGo = 0.0;
		int i=0;
		for (Region region : regions) {
			if (region.isEnabled()) {
				i++;
				if (i>currentRegionNumber2) {
					timeToGo += region.getTotalTime();
				}
			}
		}
		logger.warn("=========regions to do time {}, current region number {}", timeToGo,currentRegionNumber2);
		return timeToGo;
	}

	private class AnalyserStateListener implements MonitorListener {

		private boolean running=false;

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("analyser state listener connected.");
				return;
			}
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
			}
			if (currentRegion != null) {
				switch (state) {
				case 0:
					if (running) {
						updateRegionStatus(currentRegion, STATUS.COMPLETED);
						running=false;
						logger.debug("analyser is in completed state for current region: {}", currentRegion.toString());
					} else {
						updateRegionStatus(currentRegion, STATUS.READY);
						logger.debug("analyser is in ready state for current region: {}", currentRegion.toString());
					}
					break;
				case 1:
//					updateRegionStatus(currentRegion, STATUS.RUNNING);
					running=true;
					logger.debug("analyser is in running state for current region: {}", currentRegion.toString());
					break;
				case 6:
					updateRegionStatus(currentRegion, STATUS.ABORTED);
					running=false;
					logger.error("analyser in error state for region; {}", currentRegion.toString());
					break;
				case 10:
					updateRegionStatus(currentRegion, STATUS.ABORTED);
					running=false;
					logger.warn("analyser is in aborted state for currentregion: {}", currentRegion.toString());
					break;
				default:
					logger.debug("analysre is in a unhandled state: {}", state);
					break;

				}
			} else {
				logger.debug("currentRegion object is null, no region state to update!!!");
			}
		}
	}

	private class HardShutterStateListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
				setShutterState(hardShutterState,state);
				setShutterControlButtonText(btnHardShutter, state);
			}
		}
	}

	private class SoftShutterStateListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			short state = 0;
			if (dbr.isENUM()) {
				state = ((DBR_Enum) dbr).getEnumValue()[0];
				setShutterState(softShutterState,state);
				setShutterControlButtonText(btnSoftShutter, state);
			}
		}
	}
	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.debug("EPICS channel {} initialisation completed.", getDetectorStatePV());
	}

	private void setShutterControlButtonText(final Button btnShutter, final short state) {
		if (btnShutter != null && !btnShutter.isDisposed()) {
			btnShutter.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!btnShutter.isDisposed()) {
						if (state==0) {
							btnShutter.setText("Close");
						} else if (state==1) {
							btnShutter.setText("Open");
						} else {
							btnShutter.setText("Unknown");
							btnShutter.setForeground(btnShutter.getDisplay().getSystemColor(SWT.COLOR_RED));
						}
					}
				}
			});
		}
	}

	public String getEnergyLensTableDir() {
		return energyLensTableDir;
	}

	public void setEnergyLensTableDir(String energyLensTableDir) {
		this.energyLensTableDir = energyLensTableDir;
	}

	public String getAnalyserTotalTimeRemianingPV() {
		return analyserTotalTimeRemianingPV;
	}

	public void setAnalyserTotalTimeRemianingPV(
			String analyserTotalTimeRemianingPV) {
		this.analyserTotalTimeRemianingPV = analyserTotalTimeRemianingPV;
	}

	public void setRegionValidator(RegionValidator regionValidator) {
		this.regionValidator=regionValidator;
	}
	public RegionValidator getRegionValidator() {
		return regionValidator;
	}

	public String getHardShutterPV() {
		return hardShutterPV;
	}

	public void setHardShutterPV(String hardShutterPV) {
		this.hardShutterPV = hardShutterPV;
	}

	public String getSoftShutterPV() {
		return softShutterPV;
	}

	public void setSoftShutterPV(String softShutterPV) {
		this.softShutterPV = softShutterPV;
	}


}
