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
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.events.CellChangedEvent;
import org.opengda.lde.events.DataReductionFailedEvent;
import org.opengda.lde.events.NewDataFileEvent;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.events.SampleProcessingEvent;
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

	public static final String ID = "org.opengda.lde.ui.views.dataCollectionStatusView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(DataCollectionStatus.class);
	private FormToolkit toolkit;
	private ScrolledForm form;
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
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Server Data Collection Status Report");
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		 
		layout.numColumns = 2;
	    Section section1=toolkit.createSection(form.getBody(),  Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section1.setLayoutData(td);
		section1.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section1.setText("Experiment Setup");
		section1.setDescription("Summary information on the samples to be processed on the server.");
		
		Composite section1Client=toolkit.createComposite(section1);
		section1Client.setLayout(new GridLayout(4,false));
		
		Label label=toolkit.createLabel(section1Client,"Sample configuration file: ");
		GridData gd=new GridData();
		gd.horizontalSpan=1;
		label.setLayoutData(gd);
		txtSamplesfile = toolkit.createText(section1Client, "samples definition file path", SWT.READ_ONLY|SWT.WRAP);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		gd.grabExcessHorizontalSpace=true;
		txtSamplesfile.setLayoutData(gd);

		label=toolkit.createLabel(section1Client,"Number of Active Samples: ");
		txtActivesamples = toolkit.createText(section1Client,"0", SWT.READ_ONLY);
		txtActivesamples.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label=toolkit.createLabel(section1Client,"Number of Calibrations: ");
		txtNumberCalibrations = toolkit.createText(section1Client, "0", SWT.READ_ONLY);
		txtNumberCalibrations.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label=toolkit.createLabel(section1Client,"Total Number of Collections: ");
		txtTotalNumberCollections = toolkit.createText(section1Client,"0", SWT.READ_ONLY);
		txtTotalNumberCollections.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		section1.setClient(section1Client);
		toolkit.paintBordersFor(section1Client);
		
		Section section=toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Experiment Progress");
		section.setDescription("Display data collection progress informtion of the samples on the server.");

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout(4, false));
		
		label=toolkit.createLabel(sectionClient, "Data file to collect: ");
		gd=new GridData();
		gd.horizontalSpan=1;
		label.setLayoutData(gd);
		txtDataFilePath=toolkit.createText(sectionClient,"Current data file path", SWT.READ_ONLY|SWT.WRAP);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		gd.grabExcessHorizontalSpace=true;
		txtDataFilePath.setLayoutData(gd);

		label=toolkit.createLabel(sectionClient,"Acquisition Progress: ");
		gd=new GridData();
		gd.horizontalSpan=1;
		label.setLayoutData(gd);		
		progressBar = new ProgressBar(sectionClient, SWT.NONE);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		gd.grabExcessHorizontalSpace=true;
		progressBar.setLayoutData(gd);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setData(FormToolkit.KEY_DRAW_BORDER,FormToolkit.TEXT_BORDER);
		
		label=toolkit.createLabel(sectionClient,"Progress Message: ");
		gd=new GridData();
		gd.horizontalSpan=1;
		label.setLayoutData(gd);		
		txtProgressMessage = toolkit.createText(sectionClient,"display progress messages here.", SWT.READ_ONLY|SWT.WRAP);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		gd.grabExcessHorizontalSpace=true;
		txtProgressMessage.setLayoutData(gd);
		
		label=toolkit.createLabel(sectionClient,"Current Scan Number: ");
		txtScanNumber = toolkit.createText(sectionClient,"display current scan number", SWT.READ_ONLY);
		txtScanNumber.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label=toolkit.createLabel(sectionClient,"Current Scan Point: ");
		txtScanPointNumber = toolkit.createText(sectionClient,"0/0", SWT.READ_ONLY);
		txtScanPointNumber.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label=toolkit.createLabel(sectionClient,"Current Sample: ");
		txtSamplename = toolkit.createText(sectionClient,"display current sample name", SWT.READ_ONLY);
		txtSamplename.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label=toolkit.createLabel(sectionClient,"Current Cell: ");
		txtCellname =  toolkit.createText(sectionClient,"display current cell name", SWT.READ_ONLY);
		txtCellname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label=toolkit.createLabel(sectionClient,"Current Stage: ");
		txtStagename = toolkit.createText(sectionClient,"display current stage name", SWT.READ_ONLY);
		txtStagename.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label=toolkit.createLabel(sectionClient,"Current Collection: ");
		txtCollectionNumber = toolkit.createText(sectionClient,"0/0", SWT.READ_ONLY);
		txtCollectionNumber.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		section.setClient(sectionClient);
		toolkit.paintBordersFor(sectionClient);
		
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
	/**
	 * send email to registered user email accounts.
	 * @param sample
	 */
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
					// process samples in the active editor, ensure any modifications are saved before start server data collection.
					IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
					if (activeEditor.isDirty()) {
						activeEditor.doSave(new NullProgressMonitor());
					}
					
					IFile file = (IFile) activeEditor.getEditorInput().getAdapter(IFile.class);
					if (file==null) throw new FileNotFoundException();

					String filename = file.getRawLocation().toOSString();
					//TODO must ensure all model elements are valid before starting server data collection
					EList<EObject> contents = resUtil.getResource(filename).getContents();
					for (EObject eobject : contents){
						Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eobject);
						if (diagnostic.getSeverity()==Diagnostic.ERROR || diagnostic.getSeverity()==Diagnostic.WARNING) {
						    String title = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_title");
						    String message = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_message");
						    message +="\n\n Please make sure the data model is valid before click the Run button to start server data collection process.";
						    int result = DiagnosticDialog.open
						    	        (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message, diagnostic, Diagnostic.ERROR | Diagnostic.WARNING);
						} else {
							InterfaceProvider.getCommandRunner().runCommand("datacollection.collectData("+filename+")");
							IViewPart showView = getSite().getWorkbenchWindow().getActivePage().showView(SampleGroupView.ID);
							if (showView instanceof SampleGroupView) {
								((SampleGroupView)showView).refreshTable(filename);
							}
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
		toolkit.dispose();
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

	protected void runCommand(final Command rmCommand) throws Exception {
		editingDomain.getCommandStack().execute(rmCommand);
	}

	@Override
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	@Override
	public void setFocus() {
		form.setFocus();		
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);		
	}
}
