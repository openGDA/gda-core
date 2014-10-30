package org.opengda.lde.ui.views;

//import gda.commandqueue.CommandId;
//import gda.commandqueue.CommandProgress;
//import gda.commandqueue.JythonCommandCommandProvider;
//import gda.commandqueue.Processor;
//import gda.commandqueue.ProcessorCurrentItem;
import gda.configuration.properties.LocalProperties;
import gda.device.detector.pixium.events.ScanEndEvent;
import gda.device.detector.pixium.events.ScanPointStartEvent;
import gda.device.detector.pixium.events.ScanStartEvent;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.formattedtext.FormattedTextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleProcessingEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.events.StageChangedEvent;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.SampleList;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.ui.cdatetime.CDateTimeCellEditor;
import org.opengda.lde.ui.jobs.SampleCollectionJob;
import org.opengda.lde.ui.providers.SampleGroupViewContentProvider;
import org.opengda.lde.ui.providers.SampleGroupViewLabelProvider;
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.ui.utils.StringUtils;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import uk.ac.gda.client.CommandQueueViewFactory;

/**
 * This sample view shows data obtained from the EMF model. 
 * The view is connected to the model using a content provider {@link SampleGroupViewContentProvider}.
 * <p>
 * The view uses a label provider {@link SampleGroupViewLabelProvider}to define how model
 * objects should be presented in the view. 
 * <p>
 */

public class SampleGroupView extends ViewPart implements ISelectionProvider, ISaveablePart, IObserver {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.opengda.lde.ui.views.SampleGroupView";
	public static final String DATA_DRIVER="dls";
	public static final String BEAMLINE_ID="i11-1";
	public static final String DATA_FOLDER="data";
	private static final Logger logger = LoggerFactory.getLogger(SampleGroupView.class);
	private List<ISelectionChangedListener> selectionChangedListeners;
	private String dataDriver=DATA_DRIVER;
	private String beamlineID=BEAMLINE_ID;
	private String dataFolder=DATA_FOLDER;
	private LDEResourceUtil resUtil;
	private EditingDomain editingDomain;
	private String[] cellIDs;
	private String[] calibrants;
	
	private final String columnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.ACTIVE, SampleTableConstants.SAMPLE_NAME,
			SampleTableConstants.CELL_ID, SampleTableConstants.VISIT_ID, SampleTableConstants.CALIBRANT_NAME, SampleTableConstants.CALIBRANT_X, 
			SampleTableConstants.CALIBRANT_Y, SampleTableConstants.CALIBRANT_EXPOSURE, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, 
			SampleTableConstants.SAMPLE_X_STEP, SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.DETECTOR_X, SampleTableConstants.DETECTOR_Y, SampleTableConstants.DETECTOR_Z, 
			SampleTableConstants.EMAIL, SampleTableConstants.START_DATE, SampleTableConstants.END_DATE, SampleTableConstants.COMMAND, 
			SampleTableConstants.MAIL_COUNT, SampleTableConstants.DATA_FILE_COUNT,SampleTableConstants.COMMENT };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 50, false), new ColumnWeightData(10, 35, false),new ColumnWeightData(80, 110, true), 
			new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true),
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true),
			new ColumnWeightData(40, 200, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(40, 300, true),
			new ColumnWeightData(10, 50, false), new ColumnWeightData(10, 50, false),new ColumnWeightData(50, 300, true) };
	
	private TableViewer viewer;
	private SampleList sampleList;
	private List<Sample> samples;

	private Action doubleClickAction;
	private Action startAction;
	private Action stopAction;
	private Action pauseAction;
	private Action resumeAction;
	private Action skipAction;
	
	private boolean paused;
	private boolean running;
	
	private Action addAction;
	private Action deleteAction;
	private Action copyAction;
	private Action undoAction;
	private Action redoAction;

	private Resource resource;
	private boolean isDirty;
	protected int nameCount;
	private String eventAdminName;
	private Scriptcontroller eventAdmin;
	
	/**
	 * The constructor.
	 */
	public SampleGroupView() {
		setTitleToolTip("Create a new or editing an existing sample");
		// setContentDescription("A view for editing sample parameters");
		setPartName("Samples");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialise it.
	 */
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());
//		rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer = new TableViewer(rootComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = viewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.heightHint = 386;
		gd_table.widthHint = 1000;
		table.setLayoutData(gd_table);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		createColumns(viewer);
		
		viewer.setContentProvider(new SampleGroupViewContentProvider(getResUtil()));
		viewer.setLabelProvider(new SampleGroupViewLabelProvider());
		
		samples = Collections.emptyList();
		try {
			resource = getResUtil().getResource();
			resource.eAdapters().add(notifyListener);
			viewer.setInput(resource);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: "+getResUtil().getFileName(), e2);
		}

		Composite statusArea=new Composite(rootComposite, SWT.NONE);
		GridData gd_statusArea = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_statusArea.heightHint = 150;
		statusArea.setLayoutData(gd_statusArea);
		statusArea.setLayout(new GridLayout(4, false));
		
		Group grpDataFile = new Group(statusArea, SWT.NONE);
		grpDataFile.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_grpDataFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_grpDataFile.widthHint = 290;
		grpDataFile.setLayoutData(gd_grpDataFile);
		grpDataFile.setText("Data File");
		
		txtDataFilePath = new Text(grpDataFile, SWT.BORDER);
		txtDataFilePath.setText("Current data file path");
		txtDataFilePath.setForeground(ColorConstants.lightGreen);
		txtDataFilePath.setBackground(ColorConstants.black);
		
		Group grpSamplesFileShown = new Group(statusArea, SWT.NONE);
		grpSamplesFileShown.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpSamplesFileShown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSamplesFileShown.setText("Samples file shown in the table above");
		
		txtSamplesfile = new Text(grpSamplesFileShown, SWT.BORDER);
		txtSamplesfile.setEditable(false);
		txtSamplesfile.setForeground(ColorConstants.lightGreen);
		txtSamplesfile.setBackground(ColorConstants.black);
		txtSamplesfile.setText("samples definition file path");
		
		Group grpNoSamplesTo = new Group(statusArea, SWT.NONE);
		GridData gd_grpNoSamplesTo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpNoSamplesTo.widthHint = 111;
		gd_grpNoSamplesTo.heightHint = 28;
		grpNoSamplesTo.setLayoutData(gd_grpNoSamplesTo);
		grpNoSamplesTo.setText("No. samples collect");
		grpNoSamplesTo.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		txtActivesamples = new Text(grpNoSamplesTo, SWT.BORDER | SWT.RIGHT);
		txtActivesamples.setLayoutData(new RowData(117, SWT.DEFAULT));
		txtActivesamples.setEditable(false);
		txtActivesamples.setForeground(ColorConstants.lightGreen);
		txtActivesamples.setBackground(ColorConstants.black);
		txtActivesamples.setText("0");

		Group grpMinColltionTime = new Group(statusArea, SWT.NONE);
		grpMinColltionTime.setText("Min. colltion time");
		grpMinColltionTime.setLayout(new RowLayout(SWT.HORIZONTAL));

		txtTotalTime = new Text(grpMinColltionTime, SWT.BORDER | SWT.RIGHT);
		txtTotalTime.setLayoutData(new RowData(100, SWT.DEFAULT));
		txtTotalTime.setForeground(ColorConstants.lightGreen);
		txtTotalTime.setText("0");
		txtTotalTime.setEditable(false);
		txtTotalTime.setBackground(ColorConstants.black);

		Group grpDataCollectionProgress = new Group(statusArea, SWT.NONE);
		grpDataCollectionProgress.setLayout(new GridLayout(10, false));
		GridData gd_grpDataCollectionProgress = new GridData(SWT.FILL, SWT.TOP,
				false, true, 4, 1);
		gd_grpDataCollectionProgress.heightHint = 67;
		grpDataCollectionProgress.setLayoutData(gd_grpDataCollectionProgress);
		grpDataCollectionProgress.setText("Data Collection Progress");

		Label lblCurrentScanNumber = new Label(grpDataCollectionProgress,
				SWT.NONE);
		lblCurrentScanNumber.setText("Current Scan Number:");

		txtScanNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtScanNumber.setEditable(false);
		txtScanNumber.setForeground(ColorConstants.lightGreen);
		txtScanNumber.setBackground(ColorConstants.black);
		txtScanNumber.setText("12345");
		GridData gd_txtScanNumber = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_txtScanNumber.widthHint = 60;
		txtScanNumber.setLayoutData(gd_txtScanNumber);

		Label lblCurrentSample = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCurrentSample.setText("Current Sample:");

		txtSamplename = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtSamplename.setEditable(false);
		txtSamplename.setForeground(ColorConstants.lightGreen);
		txtSamplename.setBackground(ColorConstants.black);
		txtSamplename.setText("sampleName");
		GridData gd_txtSamplename = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_txtSamplename.widthHint = 100;
		txtSamplename.setLayoutData(gd_txtSamplename);
		
		Label lblCurrentStage = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCurrentStage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCurrentStage.setText("Current Stage:");
				
		txtStagename = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtStagename.setEditable(false);
		txtStagename.setForeground(ColorConstants.lightGreen);
		txtStagename.setBackground(ColorConstants.black);
		txtStagename.setText("stageName");
		txtStagename.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSampleNumber = new Label(grpDataCollectionProgress, SWT.NONE);
		lblSampleNumber.setText("Sample Number:");

		txtSampleNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtSampleNumber.setEditable(false);
		txtSampleNumber.setBackground(ColorConstants.black);
		txtSampleNumber.setForeground(ColorConstants.lightGreen);
		txtSampleNumber.setText("0/0");
		GridData gd_txtSampleNumber = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_txtSampleNumber.widthHint = 40;
		txtSampleNumber.setLayoutData(gd_txtSampleNumber);

		Label lblScanPointNumber = new Label(grpDataCollectionProgress,SWT.NONE);
		lblScanPointNumber.setText("Scan Point Number:");
		
		txtScanPointNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtScanPointNumber.setEditable(false);
		txtScanPointNumber.setForeground(ColorConstants.lightGreen);
		txtScanPointNumber.setBackground(ColorConstants.black);
		txtScanPointNumber.setText("1/3");
		GridData gd_txtScanPointNumber = new GridData(SWT.FILL, SWT.CENTER,true, false, 1, 1);
		gd_txtScanPointNumber.widthHint = 40;
		txtScanPointNumber.setLayoutData(gd_txtScanPointNumber);
				
		Label lblProgress = new Label(grpDataCollectionProgress, SWT.NONE);
		lblProgress.setText("Acquisition Progress:");
		
		progressBar = new ProgressBar(grpDataCollectionProgress, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,false, 4, 1));
		
		Label lblProgressMessage = new Label(grpDataCollectionProgress,SWT.NONE);
		lblProgressMessage.setText("Progress Message:");

		txtProgressMessage = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtProgressMessage.setForeground(ColorConstants.lightGreen);
		txtProgressMessage.setBackground(ColorConstants.black);
		txtProgressMessage.setEditable(false);
		txtProgressMessage.setText("progressMessage");
		txtProgressMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false, 4, 1));

		initialisation();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		// register as selection listener of sample view if exist
//		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SampleViewExtensionFactory.ID, selectionListener);
		
		Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Job job = event.getJob();
				if (job instanceof SampleCollectionJob) {
					SampleCollectionJob regionJob = (SampleCollectionJob) job;
					IStatus result = event.getResult();
					if (result.isOK()) {
						updateSampleStatus(regionJob, STATUS.COMPLETED);
					} else if (result.getSeverity() == IStatus.CANCEL) {
						updateSampleStatus(regionJob, STATUS.ABORTED);
					} else if (result.getSeverity() == IStatus.ERROR) {
						updateSampleStatus(regionJob, STATUS.ABORTED);
					}

					if (Job.getJobManager().find(SampleCollectionJob.FAMILY_SAMPLE_JOB).length == 0) {
						logger.info("Samples {} collection completed.", getResUtil().getFileName());
						updateActionIconsState();
					}
				}
				super.done(event);
			}

			@Override
			public void running(IJobChangeEvent event) {
				Job job = event.getJob();
				if (job instanceof SampleCollectionJob) {
					final SampleCollectionJob regionJob = (SampleCollectionJob) job;
					updateSampleStatus(regionJob, STATUS.RUNNING);
				}
				super.running(event);
			}
		});
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.opengda.lde.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		updateActionIconsState();
	}

	private void initialisation() {
		try {
			editingDomain=getResUtil().getEditingDomain();
		} catch (Exception e) {
			logger.error("Cannot get editing domain object.", e);
			throw new RuntimeException("Cannot get editing domain object.");
		}
		if (getResUtil() != null) {
			try {
				sampleList=getResUtil().getSampleList();
			} catch (Exception e) {
				logger.error("Cannot get sample list from resource.", e);
			}
		}
		if (sampleList==null) {
			if (getResUtil() != null) {
				try {
					sampleList=getResUtil().createSampleList();
				} catch (Exception e) {
					logger.error("Cannot create new sample list", e);
				}
			}
		}
		if (getResUtil() != null) {
			txtSamplesfile.setText(getResUtil().getFileName());
		}
		
		samples=sampleList.getSamples();
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new ViewerDragAdapter(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new EditingDomainViewerDropAdapter(editingDomain, viewer));
		if (getEventAdminName()!=null) {
			eventAdmin = Finder.getInstance().find(getEventAdminName());
			if (eventAdmin!=null) {
				eventAdmin.addIObserver(this);
			}
		}

		CommandQueueViewFactory.getProcessor().addIObserver(this);
	}

//	Map<CommandId, Sample> sampleMap=new HashMap<CommandId, Sample>();
//	Processor.STATE processorState;
//	gda.commandqueue.Command.STATE commandState;
	private Sample currentSample;
	private long totalNumberOfPoints;
	private long currentPointNumber;
	@Override
	public void update(Object source, final Object arg) {
		if (source==eventAdmin) {
			if (arg instanceof ScanStartEvent) {
				ScanStartEvent event = ((ScanStartEvent)arg);
				totalNumberOfPoints = event.getNumberOfPoints();
				final String scanFilename = event.getScanFilename();
				final long scanNumber = event.getScanNumber();

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtDataFilePath.setText(scanFilename);
						txtScanNumber.setText(String.valueOf(scanNumber));
						updateScanPointNumber(currentPointNumber,totalNumberOfPoints);
					}
				});
			} else if (arg instanceof ScanPointStartEvent) {
				currentPointNumber=((ScanPointStartEvent)arg).getCurrentPointNumber();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						updateScanPointNumber(currentPointNumber,totalNumberOfPoints);
					}
				});
				
			} else if (arg instanceof ScanEndEvent) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (currentSample!=null) {
							updateSampleStatus(currentSample, STATUS.COMPLETED);
						}
					}
				});
			} else if (arg instanceof StageChangedEvent) {
				StageChangedEvent event = ((StageChangedEvent)arg);
				final String currentStage = event.getCurrentStage();
				final int numberOfSamples = event.getNumberOfSamples();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtStagename.setText(currentStage+": "+numberOfSamples+" samples.");
					}
				});
			} else if (arg instanceof SampleProcessingEvent) {
				SampleProcessingEvent event = ((SampleProcessingEvent)arg);
				final String currentSampleName = event.getCurrentSampleName();
				final int currentSampleNumber = event.getCurrentSampleNumber();
				final int totalNumberActiveSamples = event.getTotalNumberActiveSamples();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtSamplename.setText(currentSampleName);
						updateSampleNumber(currentSampleNumber,totalNumberActiveSamples);
					}
				});
			} else if (arg instanceof SampleChangedEvent) {
				SampleChangedEvent event = (SampleChangedEvent)arg;
				final String sampleID = event.getSampleID();
				logger.debug("sample update to {}",sampleID);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (Sample sample : samples) {
							if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
								if (currentSample != sample) {
									updateSampleStatus(currentSample, STATUS.COMPLETED);
								}
								currentSample = sample;
							}
						}
						viewer.setSelection(new StructuredSelection(currentSample));
					}
				});
			} else if (arg instanceof SampleStatusEvent) {
				SampleStatusEvent event = (SampleStatusEvent)arg;
				final String sampleID = event.getSampleID();
				final STATUS status = event.getStatus();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						logger.debug("sample {} update to {}",sampleID, status);
						for (Sample sample : samples) {
							if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
								updateSampleStatus(sample, status);
							}
						}
					}
				});
				
			}
		}
	}
	private void updateSampleNumber(int currentSampleNumber,int totalNumberActiveSamples) {
		txtSampleNumber.setText(String.valueOf(currentSampleNumber) + '/'+ String.valueOf(totalNumberActiveSamples));
	}

	private void updateScanPointNumber(long currentPointNumber,long totalNumberOfPoints) {
		txtScanPointNumber.setText(String.valueOf(currentPointNumber) + '/'+ String.valueOf(totalNumberOfPoints));
	}

	protected void sendEmailToUsers(final Sample sample) {
		Job job = new Job("Send users email") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try{
					final String subject = LocalProperties.get("org.opengda.mail.subject","Data now available to download and view");
					final String usersEmail=sample.getEmail();
					final String[] recipients = usersEmail.split(" ");
					for (int i=0; i<recipients.length; i++) {
						recipients[i] = recipients[i].trim();
					}
					final String senderName=LocalProperties.get("org.opengda.mail.sender.name","i11-LDE");
					//TODO changeto i11-LDE operation email account please
					final String senderEmail=LocalProperties.get("org.opengda.mail.sender.email","dag-group@diamond.ac.uk");
					final String description="Data for sample "+sample.getName()+" are available now for download/view at " +sample.getDataFilePath()+".";
					
					final String from = String.format("%s <%s>", senderName, senderEmail);
					
					final String beamlineName = LocalProperties.get("gda.beamline.name","Beamline Unknown");
					final String mailSubject = String.format("[LDE Notification - %s] %s", beamlineName.toUpperCase(), subject);
					
					final String smtpHost = LocalProperties.get("org.opengda.mail.smtp.host","localhost");
					
					JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
					mailSender.setHost(smtpHost);
					
					SimpleMailMessage message = new SimpleMailMessage();
					message.setFrom(from);
					message.setTo(recipients);
					message.setSubject(mailSubject);
					message.setText(description);
					
					mailSender.send(message);
					return Status.OK_STATUS;
				} catch(Exception ex){
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, "Error sending email", ex);
				}
				
			}
		};
		
		job.schedule();
		
	}

	protected void updateSampleStatus(final Sample sample, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				sample.setStatus(status);
				viewer.refresh();
			}
		});
	}
	
	private void makeActions() {
		startAction= new Action() {

			@Override
			public void run() {
				super.run();
//				if (!sampleMap.isEmpty()) {
//					sampleMap.clear();
//				}
				logger.info("Start data collection on GDA server.");
				running = true;
				paused=false;
				updateActionIconsState();
				try {
					for (Sample sample : samples) {
						if (sample.isActive()) {
							//set data file path for each sample before data collection
							//TODO using dataCollection script
							String commandString="LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, "+ getDataDirectory(sample) + ");";
							commandString += sample.getCommand();
//							JythonCommandCommandProvider command = new JythonCommandCommandProvider(commandString, sample.getName(), null);
//							CommandId addToTail = CommandQueueViewFactory.getQueue().addToTail(command);
//							sampleMap.put(addToTail, sample);
						}
					}
				} catch (Exception e) {
					logger.error("exception throws on start queue processor.", e);
					running = false;
					updateActionIconsState();
				}
			}
		};
		startAction.setText("Start");
		startAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_START));
		startAction.setToolTipText("Start data collection for the active samples on GDA server");
		
		stopAction= new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Stop data collection on GDA server.");
				try {
					CommandQueueViewFactory.getProcessor().stop(100000);
					running=false;
					paused=false;
				} catch (Exception e) {
					logger.error("exception throws on stop GDA server queue processor.", e);
				}
				updateActionIconsState();
			}
		};
		stopAction.setText("Stop");
		stopAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_STOP));
		stopAction.setToolTipText("Stop data collection immediately on GDA server");
		
		pauseAction= new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Pause data collection on GDA server.");
				try {
					CommandQueueViewFactory.getProcessor().pause(100000);
					running=false;
					paused=true;
				} catch (Exception e) {
					logger.error("exception throws on stop GDA server queue processor.", e);
				}
				updateActionIconsState();
			}
		};
		pauseAction.setText("Pause");
		pauseAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_PAUSE));
		pauseAction.setToolTipText("Pause data collection on GDA server");
		
		resumeAction= new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Resume data collection on GDA server.");
				running=true;
				paused=false;
				updateActionIconsState();
				try {
					CommandQueueViewFactory.getProcessor().start(100000);
				} catch (Exception e) {
					logger.error("exception throws on stop GDA server queue processor.", e);
					running = false;
					updateActionIconsState();
				}
			}
		};
		resumeAction.setText("Resume");
		resumeAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_RESUME));
		resumeAction.setToolTipText("Resume data collection on GDA server");
		
		skipAction= new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Skip the current sample data collection on GDA server.");
				try {
					CommandQueueViewFactory.getProcessor().skip(100000);
				} catch (Exception e) {
					logger.error("exception throws on stop GDA server queue processor.", e);
				}
				updateActionIconsState();
			}
		};
		skipAction.setText("Skip");
		skipAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_SKIP));
		skipAction.setToolTipText("Skip the current sample data collection on GDA server");
		
		addAction = new Action() {

			@Override
			public void run() {
				try {
					Sample newSample = LDEExperimentsFactory.eINSTANCE.createSample();
					nameCount = StringUtils.largestIntAtEndStringsWithPrefix(getSampleNames(), newSample.getName());
					if (nameCount != -1) {
						// increment the name
						nameCount++;
						newSample.setName(newSample.getName() + nameCount);
					}
					editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, getResUtil().getSampleList(), LDEExperimentsPackage.eINSTANCE.getSampleList_Samples(), newSample));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		addAction.setText("Add");
		addAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_ADD_OBJ));
		addAction.setToolTipText("Add a new sample");
		
		copyAction = new Action() {

			@Override
			public void run() {
				try {
					if (getSelectedSample() != null) {
						Sample copy = EcoreUtil.copy(getSelectedSample());
						copy.setSampleID(EcoreUtil.generateUUID());
						String sampleNamePrefix = StringUtils.prefixBeforeInt(copy.getName());
						int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getSampleNames(), sampleNamePrefix);
						if (largestIntInNames != -1) {
							largestIntInNames++;
							copy.setName(sampleNamePrefix + largestIntInNames);
						}
						editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, getResUtil().getSampleList(), LDEExperimentsPackage.eINSTANCE.getSampleList_Samples(), copy));
					} else {
						MessageDialog msgd = new MessageDialog(getSite().getShell(), "No Sample Selected", null,
								"You must selecte a sample to copy from.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		copyAction.setText("Copy");
		copyAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_COPY_EDIT));
		copyAction.setToolTipText("Copy selected sample");

		deleteAction = new Action() {

			@Override
			public void run() {
				try {
					Sample selectedSample = getSelectedSample();
					if (selectedSample != null) {
						editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, getResUtil().getSampleList(), LDEExperimentsPackage.eINSTANCE.getSampleList_Samples(), selectedSample));
					} else {
						MessageDialog msgd = new MessageDialog(getSite().getShell(), "No Sample Selected", null,
								"You must selecte a sample to delete.", MessageDialog.ERROR, new String[] { "OK" }, 0);
						msgd.open();
					}
				} catch (Exception e1) {
					logger.error("Cannot not get Editing Domain object.", e1);
				}
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_DELETE_OBJ));
		deleteAction.setToolTipText("Delete selected sample");

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
		undoAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_UNDO_EDIT));
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
		redoAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_REDO_EDIT));
		redoAction.setToolTipText("Redo");

	}
	
	private Sample getSelectedSample() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			Object firstElement = structuredSel.getFirstElement();
			if (firstElement instanceof Sample) {
				Sample sample = (Sample) firstElement;
				return sample;
			}
		}
		return null;
	}

	protected List<String> getSampleNames() {
		List<String> sampleNames=new ArrayList<String>();
		for (Sample sample : samples) {
			sampleNames.add(sample.getName());
		}
		return sampleNames;
	}

	private void createColumns(TableViewer tableViewer) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);

			column.setWidth(columnLayouts[i].minimumWidth);
			tableViewerColumn.setEditingSupport(new TableColumnEditingSupport(tableViewer, tableViewerColumn));
		}
	}

	private class TableColumnEditingSupport extends EditingSupport {
		
		private String columnIdentifier;
		private Table table;
		public TableColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
			super(viewer);
			table=((TableViewer)viewer).getTable();
			columnIdentifier=tableViewerColumn.getColumn().getText();
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
				return new CheckboxCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_NAME.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
				final ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table, SWT.READ_ONLY);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getCellIDs());
				return ce;
			} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
				final ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getVisitIDs());
				return ce;
//				return new TextCellEditor(table);
			} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table, SWT.READ_ONLY);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getCalibrants());
				return ce;
			} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_X_START.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_X_STOP.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_X_STEP.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_Y_START.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_Y_STOP.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_Y_STEP.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_EXPOSURE.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
			} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_NAME.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_X_START.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_X_STOP.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_X_STEP.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_Y_START.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_Y_STOP.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_Y_STEP.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.SAMPLE_EXPOSURE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
				return true;
			} 
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Sample) {
				Sample sample = (Sample) element;
				if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
					return sample.isActive();
				} else if (SampleTableConstants.SAMPLE_NAME.equals(columnIdentifier)) {
					return sample.getName();
				} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
					return sample.getCellID();
				} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
					return sample.getVisitID();
				} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
					return sample.getCalibrant();
				} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
					return sample.getCalibrant_x();
				} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
					return sample.getCalibrant_y();
				} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
					return sample.getCalibrant_exposure();
				} else if (SampleTableConstants.SAMPLE_X_START.equals(columnIdentifier)) {
					return sample.getSample_x_start();
				} else if (SampleTableConstants.SAMPLE_X_STOP.equals(columnIdentifier)) {
					return sample.getSample_x_stop();
				} else if (SampleTableConstants.SAMPLE_X_STEP.equals(columnIdentifier)) {
					return sample.getSample_x_step();
				} else if (SampleTableConstants.SAMPLE_Y_START.equals(columnIdentifier)) {
					return sample.getSample_y_start();
				} else if (SampleTableConstants.SAMPLE_Y_STOP.equals(columnIdentifier)) {
					return sample.getSample_y_stop();
				} else if (SampleTableConstants.SAMPLE_Y_STEP.equals(columnIdentifier)) {
					return sample.getSample_y_step();
				} else if (SampleTableConstants.SAMPLE_EXPOSURE.equals(columnIdentifier)) {
					return sample.getSample_exposure();
				} else if (SampleTableConstants.DETECTOR_X.equals(columnIdentifier)) {
					return sample.getDetector_x();
				} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
					return sample.getDetector_y();
				} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
					return sample.getDetector_z();
				} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
					return sample.getEmail();
				} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
					return sample.getStartDate();
				} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
					return sample.getEndDate();
				} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
					return sample.getCommand();
				} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
					return sample.getComment();
				} 
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
				if (value instanceof Boolean) {
					try {
						if ((boolean)value==true) {
							if (isDatesValid((Sample)element) && isValidCellID((Sample)element, ((Sample)element).getCellID())) {
								runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
							}
						} else {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.ACTIVE+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.SAMPLE_NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Name(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.SAMPLE_NAME+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidCellID((Sample)element,(String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_CellID(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.CELL_ID+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidVisitID((Sample)element, (String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_VisitID(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.VISIT_ID+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Calibrant(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_NAME+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Calibrant_x(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_X+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Calibrant_y(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_Y+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Calibrant_exposure(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_EXPOSURE+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_X_START.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_x_start(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_X_START+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_X_STOP.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_x_stop(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_X_STOP+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_X_STEP.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_x_step(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_X_STEP+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_Y_START.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_y_start(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_Y_START+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_Y_STOP.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_y_stop(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_Y_STOP+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_Y_STEP.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_y_step(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_Y_STEP+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.SAMPLE_EXPOSURE.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Sample_exposure(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.SAMPLE_EXPOSURE+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Detector_x(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_X+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Detector_y(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_Y+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Detector_z(), value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_Z+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidEmail((String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Email(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.EMAIL+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_StartDate(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.START_DATE+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_EndDate(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.END_DATE+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidCommand((String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Command(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.COMMAND+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Comment(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.COMMENT+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} 
		}

		private boolean isDatesValid(Sample sample) {
			Date now=new Date();
			boolean startLessEnd = sample.getStartDate().compareTo(sample.getEndDate())<=0;
			boolean nowInBetween = now.compareTo(sample.getStartDate())>=0 && now.compareTo(sample.getEndDate())<0;
			if (startLessEnd && nowInBetween) {
				return true;
			}
			String message="";
			if (!startLessEnd) {
				message="Sample start date must be before the end date.";
			}
			if (!nowInBetween) {
				message="Cannot active this sample because the current date time is outside its date time range set.";
			}
			openMessageBox(message, "Activation Failed - Invalid dates ");
			return false;
		}

		private boolean isValidCommand(String value) {
			// TODO Implement GDA command validator?
			// validate single/multiple commands, e.g. scan, pos, scripts, etc. HOW???
			return true;
		}

		private boolean isValidEmail(String value) {
			String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
			if (value.matches(EMAIL_REGEX)) {
				try {
					InternetAddress emailAddr=new InternetAddress(value);
					return true;
				} catch (AddressException e) {
					String message=e.getMessage();
					openMessageBox(message, "Invalid Email Address");
					return false;
				}
			}
			String message="Email: " + value +" is incorrectly formatted.";
			openMessageBox(message, "Invalid Email Address");
			return false;
		}

		private boolean isValidCellID(Sample element, String value) {
			if (value == null || value.isEmpty()) {
				String message="You must select a Sample Cell ID.\n";
				openMessageBox(message, "Invalid Cell ID");
				return false;
			}
			for (Sample sample : samples) {
				if (element != sample && value.equals(sample.getCellID())) {
					String message="Sample Cell is already used.\n";
					openMessageBox(message, "Invalid Cell ID");
					return false;
				}
			}
			return true;
		}

		private boolean isValidVisitID(Sample sample, String value) {
			if (value.contentEquals("0-0")){ // Commissioning folder
				return true;
			}
			File dir=new File(getDataDirectory(sample));
			if (dir.exists()) {
				return true;
			}
			String message="Cannot find the data directory '" + dir.getAbsolutePath()+"' for this sample on data storage driver.\n";
			openMessageBox(message, "Invalid Visit ID");
			return false;
		}
	}
	
	private String getDataDirectory(Sample sample) {
		String dataDir=File.separator;
		if (getDataDriver()!=null && !getDataDriver().isEmpty()) {
			dataDir += getDataDriver()+File.separator;
		}
		if (getBeamlineID()!=null && !getBeamlineID().isEmpty()) {
			dataDir += getBeamlineID()+File.separator;
		}
		if (getDataFolder()!=null && !getDataFolder().isEmpty()) {
			dataDir += getDataFolder()+File.separator;
		}
		dataDir += Calendar.getInstance().get(Calendar.YEAR)+File.separator+sample.getVisitID();
		return dataDir;
	}
	
	private String[] getVisitIDs() {
		String dataDir=File.separator;
		if (getDataDriver()!=null && !getDataDriver().isEmpty()) {
			dataDir += getDataDriver()+File.separator;
		}
		if (getBeamlineID()!=null && !getBeamlineID().isEmpty()) {
			dataDir += getBeamlineID()+File.separator;
		}
		if (getDataFolder()!=null && !getDataFolder().isEmpty()) {
			dataDir += getDataFolder()+File.separator;
		}
		dataDir += Calendar.getInstance().get(Calendar.YEAR);
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
		return dirList.toArray(new String[0]);
	}

	private void openMessageBox(String message, String title) {
		MessageBox dialog=new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
	
	protected void runCommand(final Command rmCommand) throws Exception {
		editingDomain.getCommandStack().execute(rmCommand);
	}
	

	private void updateActionIconsState() {
		if (running) {
			startAction.setEnabled(false);
			stopAction.setEnabled(true);
			skipAction.setEnabled(true);
			if (paused) {
				pauseAction.setEnabled(false);
				resumeAction.setEnabled(true);
				
			} else {
				pauseAction.setEnabled(true);
				resumeAction.setEnabled(false);
			}
		} else {
			startAction.setEnabled(true);
			stopAction.setEnabled(false);
			pauseAction.setEnabled(false);
			resumeAction.setEnabled(false);
			skipAction.setEnabled(false);
		}
	}
	
	private void updateSampleStatus(final SampleCollectionJob sampleJob, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				Sample sample = sampleJob.getSample();
				sample.setStatus(status);
				viewer.refresh();
			}
		});
	}
	
	private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
					&& (!notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_Status()) 
						|| !notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_MailCount()) 
						|| !notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_DataFileCount()))) {
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		}
	};
	private Text txtTotalTime;
	private Text txtSamplesfile;
	private Text txtActivesamples;
	private Text txtDataFilePath;
	private Text txtSamplename;
	private Text txtScanNumber;
	private Text txtSampleNumber;
	private Text txtScanPointNumber;
	private Text txtProgressMessage;
	private ProgressBar progressBar;
	private Text txtStagename;

	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SampleGroupView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(pauseAction);
		manager.add(resumeAction);
		manager.add(skipAction);
		manager.add(new Separator());
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(pauseAction);
		manager.add(resumeAction);
		manager.add(skipAction);
		manager.add(new Separator());
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(pauseAction);
		manager.add(resumeAction);
		manager.add(skipAction);
		manager.add(new Separator());
		manager.add(addAction);
		manager.add(deleteAction);
		manager.add(copyAction);
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				//TODO implement this editor
//				doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),"Sample View",	message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			resUtil.getResource().save(null);
			isDirty = false;
			firePropertyChange(PROP_DIRTY);
		} catch (IOException e) {
			logger.error("Cannot save the resource to a file.", e);
		} catch (Exception e) {
			logger.error("Cannot get resource from resUtil.", e);
		}
	}

	@Override
	public void doSaveAs() {
		Resource resource = null;
		try {
			resource = resUtil.getResource();
		} catch (Exception e1) {
			logger.error("Cannot get resource from resUtil.", e1);
		}
		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
		saveAsDialog.open();
		IPath path = saveAsDialog.getResult();
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null && resource != null) {
				String newFilename = file.getLocation().toOSString();
				resUtil.saveAs(resource, newFilename);
				isDirty = false;
				firePropertyChange(PROP_DIRTY);
				refreshTable(newFilename, false);
			}
		}
	}

	/**
	 * refresh the table viewer with the sequence file name provided. If it is a new file, an empty sequence will be created.
	 */
	public void refreshTable(String seqFileName, boolean newFile) {
		logger.debug("refresh table with file: {}{}", FilenameUtils.getFullPath(seqFileName), FilenameUtils.getName(seqFileName));
		if (isDirty()) {
//			InterfaceProvider.getCurrentScanController().pauseCurrentScan();
			MessageDialog msgDialog = new MessageDialog(getViewSite().getShell(), "Unsaved Data", null,
					"Current sample list contains unsaved data. Do you want to save them first?", MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);
			int result = msgDialog.open();
			if (result == 0) {
				doSave(new NullProgressMonitor());
			} else {
				isDirty = false;
				firePropertyChange(PROP_DIRTY);
			}
//			InterfaceProvider.getCurrentScanController().resumeCurrentScan();
		}
		try {
			resource.eAdapters().remove(notifyListener); // remove old resource listener
			resUtil.setFileName(seqFileName);
			if (newFile) {
				resUtil.createSampleList();
			}
			Resource sequenceRes = resUtil.getResource();
			viewer.setInput(sequenceRes);
			// update the resource in this view.
			resource = sequenceRes;
			resource.eAdapters().add(notifyListener);

			// update existing regions list
			samples = resUtil.getSamples();
			for (Sample sample : samples) {
				if (sample.isActive()) {
					currentSample=sample;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Cannot refresh table.", e);
		}
	}
	
	@Override
	public void dispose() {
		try {
			resUtil.getResource().eAdapters().remove(notifyListener);
//			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(SampleViewExtensionFactory.ID, selectionListener);
			CommandQueueViewFactory.getProcessor().deleteIObserver(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
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
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);		
	}

	@Override
	public ISelection getSelection() {
		return viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);		
	}

	@Override
	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);		
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	public String getDataDriver() {
		return dataDriver;
	}

	public void setDataDriver(String dataDriver) {
		this.dataDriver = dataDriver;
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	public String[] getCellIDs() {
		return cellIDs;
	}

	public void setCellIDs(String[] cellIDs) {
		this.cellIDs = cellIDs;
	}

	public String[] getCalibrants() {
		return calibrants;
	}

	public void setCalibrants(String[] calibrants) {
		this.calibrants = calibrants;
	}

	public String getBeamlineID() {
		return beamlineID;
	}

	public void setBeamlineID(String beamlineID) {
		this.beamlineID = beamlineID;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}
}