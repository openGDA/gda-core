package org.opengda.lde.ui.views;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.events.CellChangedEvent;
import org.opengda.lde.events.DataReductionFailedEvent;
import org.opengda.lde.events.NewDataFileEvent;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleProcessingEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.events.StageChangedEvent;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.provider.LDEExperimentsItemProviderAdapterFactory;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.device.detector.pixium.events.ScanEndEvent;
import gda.device.detector.pixium.events.ScanPointStartEvent;
import gda.device.detector.pixium.events.ScanStartEvent;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
/**
 * display all samples contained in the EMF model.
 * @author fy65
 *
 */
public class DataCollectionStatus extends ViewPart implements IEditingDomainProvider, IObserver {

	public static final String ID = "org.opengda.lde.ui.views.SamplesView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(DataCollectionStatus.class);
	private LDEResourceUtil resUtil;

	private Text txtDataFilePath;
	private Text txtSamplesfile;
	private Text txtActivesamples;
	private Text txtNumberCalibrations;
	private Text txtTotalNumberCollections;
	private Text txtScanNumber;
	private Text txtSamplename;
	private Text txtCellname;
	private Text txtStagename;
	private Text txtCollectionNumber;
	private Text txtScanPointNumber;
	private ProgressBar progressBar;
	private Text txtProgressMessage;
	private Action startAction;
	protected boolean running;
	protected boolean paused;
	private Action stopAction;
	private Action pauseAction;
	private Action resumeAction;
	private Action skipAction;
	private List<Sample> samples;
	private List<Experiment> experiments;
	private int numActiveSamples;
	private Scriptcontroller eventAdmin;
	private String eventAdminName;
	protected int nameCount;
	private Sample currentSample;
	private long totalNumberOfPoints;
	protected long currentPointNumber;
	
	protected AdapterFactoryEditingDomain editingDomain;
	protected ComposedAdapterFactory adapterFactory;
	protected Viewer currentViewer;

	public DataCollectionStatus() {
		setTitleToolTip("Display data collection processing status on GDA server.");
		setPartName("Data Collection Status");
		
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new LDEExperimentsItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH | SWT.VERTICAL);

		Composite statusArea = new Composite(sashForm, SWT.NONE);
		GridData gd_statusArea = new GridData(SWT.FILL, SWT.FILL, true, false,1, 1);
		gd_statusArea.heightHint = 150;
		statusArea.setLayoutData(gd_statusArea);
		statusArea.setLayout(new GridLayout(5, false));

		sashForm.setWeights(new int[] { 3, 1 });

		Group grpDataFile = new Group(statusArea, SWT.NONE);
		grpDataFile.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false,1, 1);
		layoutData.widthHint = 290;
		grpDataFile.setLayoutData(layoutData);
		grpDataFile.setText("Data File");

		txtDataFilePath = new Text(grpDataFile, SWT.BORDER);
		txtDataFilePath.setText("Current data file path");
		txtDataFilePath.setForeground(ColorConstants.lightGreen);
		txtDataFilePath.setBackground(ColorConstants.black);

		Group grpSampleDefinitionFile = new Group(statusArea, SWT.NONE);
		grpSampleDefinitionFile.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpSampleDefinitionFile.setLayoutData(new GridData(SWT.FILL,SWT.CENTER, true, false, 1, 1));
		grpSampleDefinitionFile.setText("Sample Definition File");

		txtSamplesfile = new Text(grpSampleDefinitionFile, SWT.BORDER);
		txtSamplesfile.setEditable(false);
		txtSamplesfile.setForeground(ColorConstants.lightGreen);
		txtSamplesfile.setBackground(ColorConstants.black);
		txtSamplesfile.setText("samples definition file path");

		Group grpNoActiveSamples = new Group(statusArea, SWT.NONE);
		GridData gd_grpNoSamplesTo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpNoSamplesTo.widthHint = 111;
		gd_grpNoSamplesTo.heightHint = 28;
		grpNoActiveSamples.setLayoutData(gd_grpNoSamplesTo);
		grpNoActiveSamples.setText("No. Active Samples");
		grpNoActiveSamples.setLayout(new RowLayout(SWT.HORIZONTAL));

		txtActivesamples = new Text(grpNoActiveSamples, SWT.BORDER | SWT.RIGHT);
		txtActivesamples.setLayoutData(new RowData(117, SWT.DEFAULT));
		txtActivesamples.setEditable(false);
		txtActivesamples.setForeground(ColorConstants.lightGreen);
		txtActivesamples.setBackground(ColorConstants.black);
		txtActivesamples.setText("0");
		
		Group grpNoCalibrations = new Group(statusArea, SWT.NONE);
		grpNoCalibrations.setText("No. Calibrations");
		grpNoCalibrations.setLayout(new RowLayout(SWT.HORIZONTAL));

		txtNumberCalibrations = new Text(grpNoCalibrations, SWT.BORDER | SWT.RIGHT);
		txtNumberCalibrations.setLayoutData(new RowData(100, SWT.DEFAULT));
		txtNumberCalibrations.setForeground(ColorConstants.lightGreen);
		txtNumberCalibrations.setText("0");
		txtNumberCalibrations.setEditable(false);
		txtNumberCalibrations.setBackground(ColorConstants.black);
		
		Group grpTotalCollections = new Group(statusArea, SWT.NONE);
		grpTotalCollections.setText("Total Collections");
		grpTotalCollections.setLayout(new RowLayout(SWT.HORIZONTAL));

		txtTotalNumberCollections = new Text(grpTotalCollections, SWT.BORDER | SWT.RIGHT);
		txtTotalNumberCollections.setLayoutData(new RowData(100, SWT.DEFAULT));
		txtTotalNumberCollections.setForeground(ColorConstants.lightGreen);
		txtTotalNumberCollections.setText("0");
		txtTotalNumberCollections.setEditable(false);
		txtTotalNumberCollections.setBackground(ColorConstants.black);
		
		Group grpDataCollectionProgress = new Group(statusArea, SWT.NONE);
		grpDataCollectionProgress.setLayout(new GridLayout(12, false));
		GridData gd_grpDataCollectionProgress = new GridData(SWT.FILL,SWT.FILL, false, true, 5, 1);
		gd_grpDataCollectionProgress.heightHint = 67;
		grpDataCollectionProgress.setLayoutData(gd_grpDataCollectionProgress);
		grpDataCollectionProgress.setText("Data Collection Progress");

		Label lblCurrentScanNumber = new Label(grpDataCollectionProgress,SWT.NONE);
		lblCurrentScanNumber.setText("Scan Number:");

		txtScanNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtScanNumber.setEditable(false);
		txtScanNumber.setForeground(ColorConstants.lightGreen);
		txtScanNumber.setBackground(ColorConstants.black);
		txtScanNumber.setText("display current scan number");
		GridData gd_txtScanNumber = new GridData(SWT.FILL, SWT.CENTER, true,false, 1, 1);
		gd_txtScanNumber.widthHint = 60;
		txtScanNumber.setLayoutData(gd_txtScanNumber);

		Label lblCurrentSample = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCurrentSample.setText("Sample:");

		txtSamplename = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtSamplename.setEditable(false);
		txtSamplename.setForeground(ColorConstants.lightGreen);
		txtSamplename.setBackground(ColorConstants.black);
		txtSamplename.setText("display current sample name");
		GridData gd_txtSamplename = new GridData(SWT.FILL, SWT.CENTER, true,false, 1, 1);
		gd_txtSamplename.widthHint = 100;
		txtSamplename.setLayoutData(gd_txtSamplename);
		
		Label lblCurrentCell = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCurrentCell.setText("Cell:");

		txtCellname = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtCellname.setEditable(false);
		txtCellname.setForeground(ColorConstants.lightGreen);
		txtCellname.setBackground(ColorConstants.black);
		txtCellname.setText("display current cell name");
		GridData gd_txtCellname = new GridData(SWT.FILL, SWT.CENTER, true,false, 1, 1);
		gd_txtCellname.widthHint = 100;
		txtCellname.setLayoutData(gd_txtCellname);
		
		Label lblCurrentStage = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCurrentStage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCurrentStage.setText("Stage:");
				
		txtStagename = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtStagename.setEditable(false);
		txtStagename.setForeground(ColorConstants.lightGreen);
		txtStagename.setBackground(ColorConstants.black);
		txtStagename.setText("display current stage name");
		txtStagename.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblCollectionNumber = new Label(grpDataCollectionProgress, SWT.NONE);
		lblCollectionNumber.setText("Collection:");

		txtCollectionNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtCollectionNumber.setEditable(false);
		txtCollectionNumber.setBackground(ColorConstants.black);
		txtCollectionNumber.setForeground(ColorConstants.lightGreen);
		txtCollectionNumber.setText("0/0");
		GridData gd_txtCollectionNumber = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_txtCollectionNumber.widthHint = 40;
		txtCollectionNumber.setLayoutData(gd_txtCollectionNumber);

		Label lblScanPointNumber = new Label(grpDataCollectionProgress,SWT.NONE);
		lblScanPointNumber.setText("Scan Point:");

		txtScanPointNumber = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtScanPointNumber.setEditable(false);
		txtScanPointNumber.setForeground(ColorConstants.lightGreen);
		txtScanPointNumber.setBackground(ColorConstants.black);
		txtScanPointNumber.setText("0/0");
		GridData gd_txtScanPointNumber = new GridData(SWT.FILL, SWT.CENTER,true, false, 1, 1);
		gd_txtScanPointNumber.widthHint = 40;
		txtScanPointNumber.setLayoutData(gd_txtScanPointNumber);

		Label lblProgress = new Label(grpDataCollectionProgress, SWT.NONE);
		lblProgress.setText("Acquisition Progress:");
		
		progressBar = new ProgressBar(grpDataCollectionProgress, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false, 5, 1));
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		
		Label lblProgressMessage = new Label(grpDataCollectionProgress,SWT.NONE);
		lblProgressMessage.setText("Progress Message:");

		txtProgressMessage = new Text(grpDataCollectionProgress, SWT.BORDER);
		txtProgressMessage.setForeground(ColorConstants.lightGreen);
		txtProgressMessage.setBackground(ColorConstants.black);
		txtProgressMessage.setEditable(false);
		txtProgressMessage.setText("progressMessage");
		txtProgressMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false, 5, 1));

		initialisation();
		// register as selection listener of sample editor if exist
//		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SampleViewExtensionFactory.ID, selectionListener);
		createActions();
		initializeToolBar();
		initializeMenu();

		updateActionIconsState();
	}
	
	private void initialisation() {
		try {
			editingDomain=(AdapterFactoryEditingDomain) getResUtil().getEditingDomain();
		} catch (Exception e) {
			logger.error("Cannot get editing domain object.", e);
			throw new RuntimeException("Cannot get editing domain object.");
		}

		if (getResUtil() != null) {
			try {
				samples=getResUtil().getSamples();
			} catch (Exception e) {
				logger.error("Cannot get sample list from resource.", e);
			}
		}
		//TODO handle no samples case
		if (samples==null) {
			if (getResUtil() != null) {
				try {
					experiments=getResUtil().createExperiments();
				} catch (Exception e) {
					logger.error("Cannot create new sample list", e);
				}
			}
		}
		if (getResUtil() != null) {
			txtSamplesfile.setText(getResUtil().getFileName());
		}
		
		updateNumberActiveSamples();
		
		if (getEventAdminName()!=null) {
			eventAdmin = Finder.getInstance().find(getEventAdminName());
			if (eventAdmin!=null) {
				eventAdmin.addIObserver(this);
			}
		}
		String beamline=null;
		if ((beamline=LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME))!=null) {
			NumTracker tracker;
			try {
				tracker = new NumTracker(beamline);
				int currentFileNumber = tracker.getCurrentFileNumber();
				txtScanNumber.setText(String.valueOf(currentFileNumber));
			} catch (IOException e) {
				logger.error("Failed on getting file tracker", e);
			}
		}
	}
	
	@Override
	public void update(Object source, Object arg) {
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
				final String currentStage = event.getStageName();
				final int numberOfCells = event.getNumberOfCells();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtStagename.setText(currentStage+": "+numberOfCells+" cells.");
					}
				});
			} else if (arg instanceof CellChangedEvent) {
				CellChangedEvent event = ((CellChangedEvent)arg);
				final String currentCell = event.getCellName();
				final int numberOfSamples = event.getNumberOfSamples();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtCellname.setText(currentCell+": "+numberOfSamples+" samples.");
					}
				});
			} else if (arg instanceof SampleProcessingEvent) {
				SampleProcessingEvent event = ((SampleProcessingEvent)arg);
				final String currentSampleName = event.getCurrentSampleName();
				final int currentSampleNumber = event.getCurrentSampleNumber();
				final int totalNumberActiveSamples = event.getTotalNumberActiveSamples();
				final int currentCalibrationNumber = event.getCurrentCalibrationNumber();
				final int totalNumberCalibrations = event.getTotalNumberCalibrations();
				
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						txtSamplename.setText(currentSampleName);
						txtTotalNumberCollections.setText(String.valueOf(totalNumberActiveSamples+totalNumberCalibrations));
						updateCollectionNumber(currentSampleNumber+currentCalibrationNumber,totalNumberActiveSamples+totalNumberCalibrations);
						progressBar.setSelection(((currentSampleNumber+currentCalibrationNumber)*100)/(totalNumberActiveSamples+totalNumberCalibrations));
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
			} else if (arg instanceof ProcessMessage) {
				final String message = ((ProcessMessage)arg).getMessage();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						txtProgressMessage.setText(message);
					}
				});
				
			} else if (arg instanceof NewDataFileEvent) {
				NewDataFileEvent event = ((NewDataFileEvent)arg);
				String sampleID = event.getSampleID();
				for (Sample sample : samples) {
					if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
						sendEmailToUsers(sample);
					}
				}
			} else if (arg instanceof DataReductionFailedEvent) {
				final String message = ((DataReductionFailedEvent)arg).getMesaage();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						txtProgressMessage.setText(message);
					}
				});
			}
		}
	}
	
	private void updateCollectionNumber(int currentSampleNumber,int totalNumberActiveSamples) {
		txtCollectionNumber.setText(String.valueOf(currentSampleNumber) + '/'+ String.valueOf(totalNumberActiveSamples));
	}

	private void updateScanPointNumber(long currentPointNumber,long totalNumberOfPoints) {
		txtScanPointNumber.setText(String.valueOf(currentPointNumber) + '/'+ String.valueOf(totalNumberOfPoints));
	}

	protected void updateSampleStatus(final Sample sample, final STATUS status) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				sample.setStatus(status);
			}
		});
	}

	private void updateNumberActiveSamples() {
		int numActives = 0;
		if (!samples.isEmpty()) {
			for (Sample sample : samples) {
				if (sample.isActive()) {
					numActives++;
				}
			}
		}
		txtActivesamples.setText(String.format("%d", numActives));
		this.numActiveSamples=numActives;
	}

	protected void sendEmailToUsers(final Sample sample) {
		Job job = new Job("Send users email") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try{
					final String subject = LocalProperties.get("org.opengda.mail.subject","Data now available to download and view");
					final EList<String> usersEmail=sample.getCell().getEmail();
					final String[] recipients = usersEmail.toArray(new String[0]);
					for (int i=0; i<recipients.length; i++) {
						recipients[i] = recipients[i].trim();
					}
					final String senderName=LocalProperties.get("org.opengda.mail.sender.name","i11-LDE");
					//TODO changeto i11-LDE operation email account please
					final String senderEmail=LocalProperties.get("org.opengda.mail.sender.email","diamondi11-lde@diamond.ac.uk");
					String description="Data for sample "+sample.getName()+" are available now for download and view.\n";
					description+="To download raw data files, please log into http://icat.diamond.ac.uk \n";
					description+= "To view and download reducted data please visit http://ispyb.diamond.ac.uk/dc/visit/"+sample.getCell().getVisitID()+"\n";
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

	/**
	 * Create the actions.
	 */
	private void createActions() {

		startAction= new Action() {

			@Override
			public void run() {
				super.run();

				logger.info("Start data collection on GDA server.");
				running = true;
				paused=false;
				updateActionIconsState();
				try {
					IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
					if (activeEditor.isDirty()) {
						activeEditor.doSave(new NullProgressMonitor());
					}
					IFile file = (IFile) activeEditor.getEditorInput().getAdapter(IFile.class);
					if (file==null) throw new FileNotFoundException();
					InterfaceProvider.getCommandRunner().runCommand("datacollection.collectData("+file.getRawLocation().toOSString()+")");
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
					InterfaceProvider.getCommandAborter().abortCommands();
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
					InterfaceProvider.getCommandRunner().runCommand("datacollection.pause()");
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
					InterfaceProvider.getCommandRunner().runCommand("datacollection.resume()");
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
					InterfaceProvider.getCommandRunner().runCommand("datacollection.skip()");
				} catch (Exception e) {
					logger.error("exception throws on stop GDA server queue processor.", e);
				}
				updateActionIconsState();
			}
		};
		skipAction.setText("Skip");
		skipAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_SKIP));
		skipAction.setToolTipText("Skip the current sample data collection on GDA server");
		

	}

	protected List<String> getSampleNames() {
		List<String> sampleNames=new ArrayList<String>();
		for (Sample sample : samples) {
			sampleNames.add(sample.getName());
		}
		return sampleNames;
	}
	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(startAction);
		toolbarManager.add(stopAction);
		toolbarManager.add(pauseAction);
		toolbarManager.add(resumeAction);
		toolbarManager.add(skipAction);
		toolbarManager.add(new Separator());
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(startAction);
		menuManager.add(stopAction);
		menuManager.add(pauseAction);
		menuManager.add(resumeAction);
		menuManager.add(skipAction);
		menuManager.add(new Separator());
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
	}	private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
					&& (!notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_Status()))) {

			}
		}
	};

	@Override
	public void dispose() {
		try {
			resUtil.getResource().eAdapters().remove(notifyListener);
//			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(SampleViewExtensionFactory.ID, selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
	}
	
	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
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

	@Override
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
