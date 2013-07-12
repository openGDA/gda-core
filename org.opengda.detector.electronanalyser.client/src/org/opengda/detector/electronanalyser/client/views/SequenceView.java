package org.opengda.detector.electronanalyser.client.views;

import gda.configuration.properties.LocalProperties;
import gda.device.Device;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.RegionCommand;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.jobs.RegionJob;
import org.opengda.detector.electronanalyser.client.jobs.RegionJobRule;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceTableConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewContentProvider;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewLabelProvider;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.RegionViewExtensionFactory;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.SequenceFileChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Spectrum;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.OsUtil;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceView extends ViewPart implements ISelectionProvider, IRegionDefinitionView, ISaveablePart, IObserver, InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(SequenceView.class);

	private List<ISelectionChangedListener> selectionChangedListeners;
	private Camera camera;
	//private IObserver processorObserver;
	//private Processor processor;

	public SequenceView() {
		setTitleToolTip("Create a new or editing an existing sequence");
		// setContentDescription("A view for editing sequence parameters");
		setPartName("Sequence Editor");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	}

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Text txtNumberActives;
	private Text txtLocation;
	private Text txtUser;
	private Text txtSample;
	private Text txtFilename;
	private Text txtComments;
	private int nameCount;
	private String location;
	private String user;
	private String visit;

	private final String columnHeaders[] = { SequenceTableConstants.STATUS, SequenceTableConstants.ENABLED, SequenceTableConstants.REGION_NAME,
			SequenceTableConstants.LENS_MODE, SequenceTableConstants.PASS_ENERGY, SequenceTableConstants.X_RAY_SOURCE,
			SequenceTableConstants.ENERGY_MODE, SequenceTableConstants.LOW_ENERGY, SequenceTableConstants.HIGH_ENERGY,
			SequenceTableConstants.ENERGY_STEP, SequenceTableConstants.STEP_TIME, SequenceTableConstants.STEPS, SequenceTableConstants.TOTAL_TIME,
			SequenceTableConstants.X_CHANNEL_FROM, SequenceTableConstants.X_CHANNEL_TO, SequenceTableConstants.Y_CHANNEL_FROM,
			SequenceTableConstants.Y_CHANNEL_TO, SequenceTableConstants.SLICES, SequenceTableConstants.MODE };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 30, false), new ColumnWeightData(10, 30, false),
			new ColumnWeightData(80, 100, true), new ColumnWeightData(70, 90, false), new ColumnWeightData(40, 50, false),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 80, false), new ColumnWeightData(50, 70, true),
			new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 90, true), new ColumnWeightData(50, 70, true),
			new ColumnWeightData(50, 50, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true),
			new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 120, true) };

	private TableViewer sequenceTableViewer;
	private List<Region> regions;

	private Sequence sequence;

	private Spectrum spectrum;

	private Combo runMode;

	private Button btnNumberOfIterations;

	private Spinner spinner;

	private Button btnRepeatuntilStopped;

	private Button btnConfirmAfterEachInteration;

	protected boolean isDirty;

	private Resource resource;

	private Text txtEstimatedTime;

	private Action stopSequenceAction;

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
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

	@Override
	public void createPartControl(final Composite parent) {
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(gl_root);
		// new Label(rootComposite, SWT.NONE);

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
		// TableColumnLayout tableLayout = new TableColumnLayout();
		// tableViewerContainer.setLayout(tableLayout);

		// createColumns(sequenceTableViewer, tableLayout);

		tableViewerContainer.setLayout(new GridLayout());
		GridData gd1 = new GridData(GridData.FILL_BOTH);
		gd1.widthHint = 500;
		sequenceTableViewer.getTable().setLayoutData(gd1);
		createColumns(sequenceTableViewer, null);

		GridData layoutData5 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableViewerContainer.setLayoutData(layoutData5);

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
			logger.error("Cannot load resouce from file.", e2);
		}

		Composite controlArea = new Composite(rootComposite, SWT.None);
		// Contains region actions, sequence parameters, file saving info and
		// comments.
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(2, false));

		Composite leftArea = new Composite(controlArea, SWT.None);
		leftArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		leftArea.setLayout(new GridLayout(4, false));

		Composite rightArea = new Composite(controlArea, SWT.None);
		rightArea.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		rightArea.setLayout(new GridLayout());

		Group grpRegion = new Group(leftArea, SWT.NONE);
		GridData gd_grpRegion = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpRegion.grabExcessHorizontalSpace = false;
		gd_grpRegion.horizontalAlignment = SWT.LEFT;
		gd_grpRegion.widthHint = 300;
		grpRegion.setLayoutData(gd_grpRegion);
		grpRegion.setText("Region Control");
		grpRegion.setLayout(new RowLayout());

		Button btnNew = new Button(grpRegion, SWT.NONE);
		btnNew.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
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
		});
		btnNew.setText("New");

		Button button = new Button(grpRegion, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
						MessageDialog msgd = new MessageDialog(parent.getShell(), "No region selected", null,
								"You have not selected a region to duplicate.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		button.setText("Duplicate");

		Button btnDelete = new Button(grpRegion, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Region selectedRegion = getSelectedRegion();
					if (selectedRegion != null) {
						editingDomain.getCommandStack().execute(
								RemoveCommand.create(editingDomain, regionDefinitionResourceUtil.getSequence(),
										RegiondefinitionPackage.eINSTANCE.getSequence_Region(), selectedRegion));
					} else {
						MessageDialog msgd = new MessageDialog(parent.getShell(), "No region selected", null,
								"You have not selected a region to delete.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		});
		btnDelete.setText("Delete");

		Button btnUndo = new Button(grpRegion, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					editingDomain.getCommandStack().undo();
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		});
		btnUndo.setText("Undo");

		Button btnRedo = new Button(grpRegion, SWT.NONE);
		btnRedo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					editingDomain.getCommandStack().redo();
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		});
		btnRedo.setText("Redo");

		Group grpActiveRegions = new Group(leftArea, SWT.NONE);
		GridData gd_grpActiveRegions = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpActiveRegions.grabExcessHorizontalSpace = false;
		grpActiveRegions.setLayoutData(gd_grpActiveRegions);
		grpActiveRegions.setText("Active regions");
		grpActiveRegions.setLayout(new RowLayout());

		txtNumberActives = new Text(grpActiveRegions, SWT.BORDER | SWT.RIGHT);
		txtNumberActives.setLayoutData(new RowData(66, SWT.DEFAULT));
		txtNumberActives.setEditable(false);

		Group grpTotalTime = new Group(leftArea, SWT.None);
		GridData gd_grpTotalTime = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpTotalTime.grabExcessHorizontalSpace = false;
		grpTotalTime.setLayoutData(gd_grpTotalTime);
		grpTotalTime.setText("Estimated Time");
		grpTotalTime.setLayout(new RowLayout());

		txtEstimatedTime = new Text(grpTotalTime, SWT.BORDER | SWT.RIGHT);
		txtEstimatedTime.setLayoutData(new RowData(77, SWT.DEFAULT));
		txtEstimatedTime.setEditable(false);

		new Label(leftArea, SWT.NONE);

		Group grpInfo = new Group(leftArea, SWT.NONE);
		GridData layoutData1 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData1.horizontalSpan = 4;
		grpInfo.setLayoutData(layoutData1);
		grpInfo.setText("Info");
		grpInfo.setLayout(new GridLayout(3, false));

		Label lblLocation = new Label(grpInfo, SWT.NONE);
		lblLocation.setText("Location");

		txtLocation = new Text(grpInfo, SWT.BORDER | SWT.READ_ONLY);
		// this field is set in Spring configuration per beamline
		txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtLocation.setText("Location");

		Label lblComments = new Label(grpInfo, SWT.NONE);
		lblComments.setText("Add comments below:");

		Label lblUser = new Label(grpInfo, SWT.NONE);
		lblUser.setText("User");

		txtUser = new Text(grpInfo, SWT.BORDER );
		// this field is set dynamically to user proposal number in GDA
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtUser.setText("User");

		txtComments = new Text(grpInfo, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		txtComments.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (e.getSource().equals(txtComments)) {
					try {
						String[] comments;
						if (OsUtil.isWindows()) {
							comments = txtComments.getText().split("\r\n");
						} else {
							comments = txtComments.getText().split("\n");
						}
						List<String> commentList = new ArrayList<String>();
						for (String string : comments) {
							commentList.add(string);
						}
						Spectrum spectrum = regionDefinitionResourceUtil.getSpectrum();
						updateFeature(spectrum, RegiondefinitionPackage.eINSTANCE.getSpectrum_Comments(), commentList);
						updateFeature(spectrum, RegiondefinitionPackage.eINSTANCE.getSpectrum_NumberOfComments(), spectrum.getComments().size());
					} catch (Exception e1) {
						logger.error("Cannot get the spectrum from this sequence.", e1);
					}
				}
			}
		});
		txtComments.setText("comments");
		GridData gd_txtComments = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_txtComments.verticalSpan = 4;
		txtComments.setLayoutData(gd_txtComments);

		Label lblSample = new Label(grpInfo, SWT.NONE);
		lblSample.setText("Sample");

		txtSample = new Text(grpInfo, SWT.BORDER);
		txtSample.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(txtSample)) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSpectrum(), RegiondefinitionPackage.eINSTANCE.getSpectrum_SampleName(),
								txtSample.getText());
					} catch (Exception e1) {
						logger.error("Cannot get the spectrum from this sequence.", e1);
					}
				}
			}
		});
		txtSample.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtSample.setText("Sample");

		Label lblFileName = new Label(grpInfo, SWT.NONE);
		GridData gd_lblFileName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblFileName.widthHint = 59;
		lblFileName.setLayoutData(gd_lblFileName);
		lblFileName.setText("File Prefix");

		txtFilename = new Text(grpInfo, SWT.BORDER);
		txtFilename.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(txtFilename)) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSpectrum(), RegiondefinitionPackage.eINSTANCE.getSpectrum_FilenamePrefix(),
								txtFilename.getText());
					} catch (Exception e1) {
						logger.error("Cannot get the spectrum from this sequence.", e1);
					}
				}
			}
		});
		GridData gd_txtFilename = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_txtFilename.widthHint = 104;
		txtFilename.setLayoutData(gd_txtFilename);
		txtFilename.setText("Filename Prefix");

		Label lblNewLabel = new Label(grpInfo, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Name Format");

		txtfilenameformat = new Text(grpInfo, SWT.BORDER);
		txtfilenameformat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(txtfilenameformat)) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSpectrum(), RegiondefinitionPackage.eINSTANCE.getSpectrum_FilenameFormat(),
								txtfilenameformat.getText());
					} catch (Exception e1) {
						logger.error("Cannot get the spectrum from this sequence.", e1);
					}
				}
			}
		});
		txtfilenameformat.setText("%s_%5d_%3d_%s");
		txtfilenameformat.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Group grpSequnceRunMode = new Group(rightArea, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 220;
		layoutData.verticalSpan = 2;
		grpSequnceRunMode.setLayoutData(layoutData);
		grpSequnceRunMode.setLayout(new GridLayout(2, false));
		grpSequnceRunMode.setText("Sequence Run Mode");

		runMode = new Combo(grpSequnceRunMode, SWT.READ_ONLY);
		runMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(runMode) && runMode.isFocusControl()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_RunModeIndex(),
								runMode.getSelectionIndex());
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_RunMode(),
								runMode.getText());
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}
		});
		runMode.setItems(new String[] { "Normal", "Add Dimension" });
		runMode.setToolTipText("List of available sequence run modes");
		runMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		runMode.setText(runMode.getItem(0));

		new Label(grpSequnceRunMode, SWT.NONE);

		btnNumberOfIterations = new Button(grpSequnceRunMode, SWT.RADIO);
		btnNumberOfIterations.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnNumberOfIterations) && btnNumberOfIterations.getSelection()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_NumInterationOption(), true);
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_RepeatUntilStopped(),
								false);
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}
		});
		btnNumberOfIterations.setText("Number of iterations");

		spinner = new Spinner(grpSequnceRunMode, SWT.BORDER);
		spinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinner) && spinner.isFocusControl()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_NumIterations(),
								spinner.getSelection());
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource().equals(spinner) && spinner.isFocusControl()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_NumIterations(),
								spinner.getSelection());
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}
		});
		spinner.setMinimum(1);
		spinner.setToolTipText("Set number of iterations required");

		btnRepeatuntilStopped = new Button(grpSequnceRunMode, SWT.RADIO);
		btnRepeatuntilStopped.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnRepeatuntilStopped) && btnRepeatuntilStopped.getSelection()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_NumInterationOption(), false);
						updateFeature(regionDefinitionResourceUtil.getSequence(), RegiondefinitionPackage.eINSTANCE.getSequence_RepeatUntilStopped(),
								true);
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}
		});
		btnRepeatuntilStopped.setText("Repeat until stopped");

		new Label(grpSequnceRunMode, SWT.NONE);

		btnConfirmAfterEachInteration = new Button(grpSequnceRunMode, SWT.CHECK);
		btnConfirmAfterEachInteration.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnConfirmAfterEachInteration) && btnConfirmAfterEachInteration.isFocusControl()) {
					try {
						updateFeature(regionDefinitionResourceUtil.getSequence(),
								RegiondefinitionPackage.eINSTANCE.getSequence_ConfirmAfterEachIteration(),
								btnConfirmAfterEachInteration.getSelection());
					} catch (Exception e1) {
						logger.error("Cannot get the sequence", e);
					}
				}
			}
		});
		btnConfirmAfterEachInteration.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnConfirmAfterEachInteration.setText("Confirm after each iteration");
		btnConfirmAfterEachInteration.setEnabled(false);

		new Label(grpSequnceRunMode, SWT.NONE);

		Composite actionArea = new Composite(rootComposite, SWT.None);
		// Contains region editing, sequence parameters, file saving info and
		// comments.
		actionArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		actionArea.setLayout(new GridLayout(2, false));

		Label lblSequnceFile = new Label(actionArea, SWT.None);
		lblSequnceFile.setText("Sequence File: ");

		txtSequenceFilePath = new Text(actionArea, SWT.BORDER | SWT.READ_ONLY);
		txtSequenceFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		initialisation();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(RegionViewExtensionFactory.ID, selectionListener);

		Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Job job = event.getJob();
				if (job instanceof RegionJob) {
					RegionJob regionJob = (RegionJob) job;
					IStatus result = event.getResult();
					if (result.isOK()) {
						updateRegionStatus(regionJob, STATUS.COMPLETED);
					} else if (result.getSeverity() == IStatus.CANCEL) {
						updateRegionStatus(regionJob, STATUS.ABORTED);
					} else if (result.getSeverity() == IStatus.ERROR) {
						updateRegionStatus(regionJob, STATUS.ABORTED);
					}
					fireSelectionChanged(new RegionRunCompletedSelection());
					if (Job.getJobManager().find(RegionJob.FAMILY_REGION_JOB).length == 0) {
						logger.info("Sequence {} collection completed.", regionDefinitionResourceUtil.getFileName());
						runningonclient = false;
						updateActionIconsState();
					}
				}
				super.done(event);
			}

			@Override
			public void running(IJobChangeEvent event) {
				Job job = event.getJob();
				if (job instanceof RegionJob) {
					final RegionJob regionJob = (RegionJob) job;
					updateRegionStatus(regionJob, STATUS.RUNNING);
				}
				super.running(event);
			}
		});
		prepareRunOnClientActions();
		updateActionIconsState();
	}

	private void updateActionIconsState() {
		if (!runningonclient && !runningonserver) {
			startSequenceAction.setEnabled(true);
			stopSequenceAction.setEnabled(false);
			startRunOnServerAction.setEnabled(true);
			stopRunOnServerAction.setEnabled(false);
		} else if (runningonclient) {
			startSequenceAction.setEnabled(false);
			stopSequenceAction.setEnabled(true);
			startRunOnServerAction.setEnabled(false);
			stopRunOnServerAction.setEnabled(false);
		} else if (runningonserver) {
			startSequenceAction.setEnabled(false);
			stopSequenceAction.setEnabled(false);
			startRunOnServerAction.setEnabled(false);
			stopRunOnServerAction.setEnabled(true);
		}
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

	private boolean runningonclient = false;
	private boolean runningonserver = false;

	private void prepareRunOnClientActions() {
		startSequenceAction = new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Calling start");
				int order = 0;
				resetRegionStatus();
				runningonclient = true;
				updateActionIconsState();
				for (Region region : regions) {
					if (region.isEnabled()) {
						final RegionCommand command = new RegionCommand(region);
						command.setAnalyser(analyser); // TODO not good to have to set this.
						Job job = new RegionJob(command.getDescription(), command);
						job.setRule(new RegionJobRule(order));
						job.schedule();
						order++;
					}
				}
			}
		};
		startSequenceAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_START));
		startSequenceAction.setToolTipText("Run on client.");

		stopSequenceAction = new Action() {
			@Override
			public void run() {
				super.run();
				logger.info("Calling stop");
				Job.getJobManager().cancel(RegionJob.FAMILY_REGION_JOB);
				runningonclient = false;
				updateActionIconsState();
			}
		};
		stopSequenceAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_STOP));
		stopSequenceAction.setToolTipText("Stop run on client");

		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(startSequenceAction);
		toolBarManager.add(stopSequenceAction);

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

	//private Queue queue;

	//private IObserver queueObserver;

	private Action startRunOnServerAction;

	private Scriptcontroller scriptcontroller;

	private AnalyserStateListener analyserStateListener;

	private String statePV;

	private Channel stateChannel;

	private EpicsChannelManager channelmanager;

	private Device ew4000;

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
			runMode.setText(runMode.getItem(sequence.getRunMode().getValue()));
			btnNumberOfIterations.setSelection(!sequence.isRepeatUntilStopped());
			btnRepeatuntilStopped.setSelection(sequence.isRepeatUntilStopped());
			btnConfirmAfterEachInteration.setSelection(sequence.isConfirmAfterEachIteration());
			spinner.setSelection(sequence.getNumIterations());

			if (spectrum != null) {
				if (getLocation() != null) {
					txtLocation.setText(getLocation());
				} else {
					txtLocation.setText("Diamond Beamline");
				}
				if (!spectrum.getLocation().equals(txtLocation.getText())) {
					updateFeature(spectrum, RegiondefinitionPackage.eINSTANCE.getSpectrum_Location(), txtLocation.getText());
				}
				if (getVisit() != null) {
					// Obtain visit proposal from GDA property RCP_APP_VISIT
					txtUser.setText(getVisit());
				} else if (getUser() != null) {
					// set by Spring configuration
					txtUser.setText(getUser());
				} else {
					// default to user home folder
					txtUser.setText(System.getProperty("user.name"));
				}
				if (!spectrum.getUser().equals(txtUser.getText())) {
					updateFeature(spectrum, RegiondefinitionPackage.eINSTANCE.getSpectrum_User(), txtUser.getText());
				}
				txtSample.setText(spectrum.getSampleName());
				txtFilename.setText(spectrum.getFilenamePrefix());
				txtfilenameformat.setText(spectrum.getFilenameFormat());
				txtSequenceFilePath.setText(regionDefinitionResourceUtil.getFileName());

				String comments = "";
				for (String comment : spectrum.getComments()) {
					comments += comment + "\n";
				}
				txtComments.setText(comments);
				// System.out.println(txtComments.getText());
			}
			regions = sequence.getRegion();
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
		// add drag and drop support,must ensure editing domain not null at this
		// point.
		sequenceTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new ViewerDragAdapter(sequenceTableViewer));

		sequenceTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },
				new EditingDomainViewerDropAdapter(editingDomain, sequenceTableViewer));

		// sequenceTableViewer.setSelection(new StructuredSelection(
		// sequenceTableViewer.getElementAt(0)), true);
		updateCalculatedData();
		/* using command queue to process data collection specified in this table */
//		processor = CommandQueueViewFactory.getProcessor();
//		queue = CommandQueueViewFactory.getQueue();
//		if (processor != null) {
//			processorObserver = new IObserver() {
//
//				@Override
//				public void update(Object source, final Object arg) {
//					updateState(source, arg);
//				}
//			};
//			processor.addIObserver(processorObserver);
//		}
//		if (queue != null) {
//			queueObserver = new IObserver() {
//
//				@Override
//				public void update(Object source, Object arg) {
//					if (source instanceof CommandQueue && arg instanceof QueueChangeEvent) {
//						try {
//							if (queue.getSummaryList().isEmpty()) {
//								// when queue processing completed, reset controls and all regions' status to READY.
//								runningonserver = false;
//								updateActionIconsState();
//								resetRegionStatus();
//							}
//						} catch (Exception e) {
//							logger.error("Cannot get summary list from queue.", e);
//						}
//					}
//				}
//			};
//			queue.addIObserver(queueObserver);
//		}
		prepareRunOnServerActions();
		channelmanager = new EpicsChannelManager(this);
		scriptcontroller = Finder.getInstance().find("SequenceFileObserver");
		scriptcontroller.addIObserver(this);
		regionScannable = Finder.getInstance().find("regions");
		regionScannable.addIObserver(this);
		ew4000 = Finder.getInstance().find("ew4000");
		ew4000.addIObserver(this);
		analyserStateListener = new AnalyserStateListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
	}

	private void createChannels() throws CAException, TimeoutException {
		first = true;
		stateChannel = channelmanager.createChannel(getDetectorStatePV(), analyserStateListener, MonitorType.NATIVE, false);
		channelmanager.creationPhaseCompleted();
		logger.debug("analyser state channel and monitor are created");
	}

//	private IObserver commandObserver = new IObserver() {
//
//		@Override
//		public void update(Object source, Object arg) {
//			if (source instanceof RegionCommand) {
//				RegionCommand command = (RegionCommand) source;
//				Region region = command.getRegion();
//				if (arg instanceof gda.commandqueue.Command.STATE) {
//					gda.commandqueue.Command.STATE cmdState = (gda.commandqueue.Command.STATE) arg;
//					if (cmdState.equals(gda.commandqueue.Command.STATE.PAUSED)) {
//						// not supported
//					} else if (cmdState.equals(gda.commandqueue.Command.STATE.RUNNING)) {
//						region.setStatus(STATUS.RUNNING);
//					} else if (cmdState.equals(gda.commandqueue.Command.STATE.COMPLETED)) {
//						region.setStatus(STATUS.COMPLETED);
//						command.deleteIObserver(commandObserver);
//					} else if (cmdState.equals(gda.commandqueue.Command.STATE.ABORTED)) {
//						region.setStatus(STATUS.ABORTED);
//						command.deleteIObserver(commandObserver);
//					} else if (cmdState.equals(gda.commandqueue.Command.STATE.ERROR)) {
//						region.setStatus(STATUS.ABORTED);
//						command.deleteIObserver(commandObserver);
//					} else if (cmdState.equals(gda.commandqueue.Command.STATE.NOT_STARTED)) {
//						region.setStatus(STATUS.READY);
//					}
//				}
//				sequenceTableViewer.refresh();
//			}
//		}
//	};

	private Action stopRunOnServerAction;

	private void prepareRunOnServerActions() {
		startRunOnServerAction = new Action() {
			@Override
			public void run() {
				super.run();
				logger.info("Calling start on server.");
				runningonserver = true;
				updateActionIconsState();
//				for (Region region : regions) {
//					if (region.isEnabled()) {
//						final RegionCommand command = new RegionCommand(region);
//						command.addIObserver(commandObserver);
//						try {
//							queue.addToTail(command);
//						} catch (Exception e) {
//							logger.error("Cannot add region " + command.getRegion().getName() + " to the processing queue.", e);
//						}
//					}
//				}
				try {
//					processor.start(500);
					JythonServerFacade jsf=JythonServerFacade.getCurrentInstance();
					jsf.runCommand(String.format("analyserscan regions '%s' ew4000", regionDefinitionResourceUtil.getFileName()));
				} catch (Exception e) {
					logger.error("exception throws on start queue processor.", e);
					runningonserver = false;
					updateActionIconsState();
				}
			}
		};
		startRunOnServerAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry()
				.getDescriptor(ImageConstants.ICON_RUN_ON_SERVER));
		startRunOnServerAction.setToolTipText("Run on server");

		stopRunOnServerAction = new Action() {
			@Override
			public void run() {
				super.run();
				logger.info("Calling stop on server");
				try {
					JythonServerFacade jsf=JythonServerFacade.getCurrentInstance();
					jsf.haltCurrentScan();
//					processor.stop(1000);
//					if (!queue.getSummaryList().isEmpty()) {
//						queue.removeAll();
//					}
					runningonserver = false;
				} catch (Exception e) {
					logger.error("exception throws on stop queue processor.", e);
				}
				updateActionIconsState();
			}
		};
		stopRunOnServerAction.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry()
				.getDescriptor(ImageConstants.ICON_STOP_SERVER));
		stopRunOnServerAction.setToolTipText("Stop run on server");

		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(startRunOnServerAction);
		toolBarManager.add(stopRunOnServerAction);
		toolBarManager.add(new Separator());

	}

//	private void updateState(Object source, final Object arg) {
//		if (source instanceof IFindableQueueProcessor) {
//			IFindableQueueProcessor processor = (IFindableQueueProcessor) source;
//			try {
//				ProcessorCurrentItem currentItem = processor.getCurrentItem();
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					if (arg instanceof CommandProgress) {
//						// nothing to do, we use EPICS Monitor to update Region Command Progress independently
//					} else if (arg instanceof gda.commandqueue.Command.STATE) {
//						gda.commandqueue.Command.STATE cmdState = (gda.commandqueue.Command.STATE) arg;
//						if (cmdState.equals(gda.commandqueue.Command.STATE.PAUSED)) {
//							// not supported
//						} else if (cmdState.equals(gda.commandqueue.Command.STATE.RUNNING)) {
//
//						} else if (cmdState.equals(gda.commandqueue.Command.STATE.COMPLETED)) {
//							// TODO set COMPLETED image
//						} else if (cmdState.equals(gda.commandqueue.Command.STATE.ABORTED)) {
//							// TODO set ERROR image
//						}
//					} else if (arg instanceof Processor.STATE) {
//						Processor.STATE prcState = (Processor.STATE) arg;
//						switch (prcState) {
//						case PROCESSING_ITEMS:
//
//							break;
//						case UNKNOWN:
//							break;
//						case WAITING_QUEUE:
//							break;
//						case WAITING_START:
//							// reset all region state to their deafult: selected
//							// -
//							// ready, unselected to null/empty/no image.
//							break;
//						}
//					}
//				}
//			});
//		}
//	}

	private void updateCalculatedData() {
		int numActives = 0;
		double totalTimes = 0.0;
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
				if (value instanceof Boolean) {
					try {
						runCommand(SetCommand.create(editingDomain, element, RegiondefinitionPackage.eINSTANCE.getRegion_Enabled(), value));
						fireSelectionChanged(new RegionActivationSelection());
						updateCalculatedData();
					} catch (Exception e) {
						e.printStackTrace();
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
			MessageDialog msgDialog = new MessageDialog(getViewSite().getShell(), "Unsaved Data", null,
					"Current sequence contains unsaved data. Do you want to save them first?", MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);
			int result = msgDialog.open();
			if (result == 0) {
				doSave(new NullProgressMonitor());
			} else {
				isDirty = false;
				firePropertyChange(PROP_DIRTY);
			}
		}
		try {
			resource.eAdapters().remove(notifyListener);
			//
			regionDefinitionResourceUtil.setFileName(seqFileName);
			if (newFile) {
				regionDefinitionResourceUtil.createSequence();
			}
			// regionDefinitionResourceUtil.setFileChanged(true);
			fireSelectionChanged(new FileSelection());
			Resource sequenceRes = regionDefinitionResourceUtil.getResource();
			sequenceTableViewer.setInput(sequenceRes);
			// update the resource in this view.
			resource = sequenceRes;
			resource.eAdapters().add(notifyListener);

			// update existing regions list
			regions = regionDefinitionResourceUtil.getRegions();
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
			if (spectrum != null) {
				txtSample.setText(spectrum.getSampleName());
				txtFilename.setText(spectrum.getFilenamePrefix());
				txtfilenameformat.setText(spectrum.getFilenameFormat());
			} else {
				txtSample.setText("");
				txtFilename.setText("");
				txtfilenameformat.setText("");
			}
			txtSequenceFilePath.setText(regionDefinitionResourceUtil.getFileName());
			// update sequence run mode
			sequence = regionDefinitionResourceUtil.getSequence();
			if (sequence != null) {
				runMode.setText(sequence.getRunMode().getLiteral());
				btnNumberOfIterations.setSelection(!sequence.isRepeatUntilStopped());
				btnRepeatuntilStopped.setSelection(sequence.isRepeatUntilStopped());
				btnConfirmAfterEachInteration.setSelection(sequence.isConfirmAfterEachIteration());
				spinner.setSelection(sequence.getNumIterations());
			}
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
		} catch (IOException e) {
			logger.error("Cannot save the resource to a file.", e);
		} catch (Exception e) {
			logger.error("Cannot get resource from RegionDefinitionResourceUtil.", e);
		}
	}

	@Override
	public void doSaveAs() {
		Resource resource = null;
		try {
			resource = regionDefinitionResourceUtil.getResource();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	private Text txtfilenameformat;

	private Text txtSequenceFilePath;

	private Action startSequenceAction;

	private IVGScientaAnalyser analyser;

	private Device regionScannable;

	private Region currentRegion;

	@Override
	public void dispose() {
		try {
			regionDefinitionResourceUtil.getResource().eAdapters().remove(notifyListener);
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(RegionViewExtensionFactory.ID, selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		if (processor != null && processorObserver != null) {
//			processor.deleteIObserver(processorObserver);
//			processor = null;
//		}
//		if (queue != null) {
//			queue = null;
//		}
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getVisit() {
		if (visit != null) {
			visit = LocalProperties.get(LocalProperties.RCP_APP_VISIT);
		}
		return visit;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source == scriptcontroller && arg instanceof SequenceFileChangeEvent) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					logger.debug("Sequence file changed to {}", ((SequenceFileChangeEvent)arg).getFilename());
					refreshTable(((SequenceFileChangeEvent) arg).getFilename(), false);
				}
			});
		}
		// if (source.equals(regionScannable)) {
		if (source == regionScannable) {
			if (arg instanceof RegionChangeEvent) {
				String regionId = ((RegionChangeEvent) arg).getRegionId();
				for (Region region : regions) {
					if (region.getRegionId().equalsIgnoreCase(regionId)) {
						currentRegion = region;
					}
				}
				fireSelectionChanged(currentRegion);
				// TODO auto select this region in the viewer????
				//sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
			} 
//			else if (arg instanceof RegionStatusEvent) {
//				Status status = ((RegionStatusEvent) arg).getStatus();
//				if (currentRegion == null) {
//					String regionId = ((RegionStatusEvent) arg).getRegionId();
//					for (Region region : regions) {
//						if (region.getRegionId().equalsIgnoreCase(regionId)) {
//							currentRegion = region;
//						}
//					}
//				}
//				switch (status) {
//				case READY:
//					updateRegionStatus(currentRegion, STATUS.READY);
//					break;
//				case RUNNING:
//					updateRegionStatus(currentRegion, STATUS.RUNNING);
//					break;
//				case ABORTED:
//					updateRegionStatus(currentRegion, STATUS.ABORTED);
//					break;
//				case COMPLETED:
//					updateRegionStatus(currentRegion, STATUS.COMPLETED);
//					break;
//				case ERROR:
//					updateRegionStatus(currentRegion, STATUS.ABORTED);
//					break;
//				default:
//					break;
//				}
//			}
		}
		if (source == ew4000) {
			if (arg instanceof RegionChangeEvent) {
				String regionId = ((RegionChangeEvent) arg).getRegionId();
				for (Region region : regions) {
					if (region.getRegionId().equalsIgnoreCase(regionId)) {
						currentRegion = region;
					}
				}
				fireSelectionChanged(currentRegion);
				// TODO auto select this region in the viewer????
//				sequenceTableViewer.setSelection(new StructuredSelection(currentRegion));
			} 
		}
	
		// TODO update current region status from detector or EPICS IOC

	}

	public String getDetectorStatePV() {
		return statePV;
	}

	public void setDetectorStatePV(String statePV) {
		this.statePV = statePV;
	}

	private boolean first = true;

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
					updateRegionStatus(currentRegion, STATUS.RUNNING);
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

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.debug("EPICS channel {} initialisation completed.", getDetectorStatePV());
	}

}
