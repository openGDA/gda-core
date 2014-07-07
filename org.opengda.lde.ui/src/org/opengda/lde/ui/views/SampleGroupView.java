	package org.opengda.lde.ui.views;


import gda.commandqueue.JythonCommandCommandProvider;
import gda.observable.IObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.SampleList;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.ui.jobs.SampleCollectionJob;
import org.opengda.lde.ui.providers.SampleGroupViewContentProvider;
import org.opengda.lde.ui.providers.SampleGroupViewLabelProvider;
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.ui.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SampleGroupView extends ViewPart implements ISelectionProvider, ISaveablePart, IObserver {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.opengda.lde.ui.views.SampleGroupView";
	public static final String DATA_DRIVER="dls";
	public static final String DATA_FOLDER="data";
	private static final Logger logger = LoggerFactory.getLogger(SampleGroupView.class);
	private List<ISelectionChangedListener> selectionChangedListeners;
	private LDEResourceUtil resUtil;
	private EditingDomain editingDomain;
	
	private final String columnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.ACTIVE, SampleTableConstants.SAMPLE_NAME,
			SampleTableConstants.CELL_ID, SampleTableConstants.VISIT_ID, SampleTableConstants.EMAIL,
			SampleTableConstants.COMMAND, SampleTableConstants.COMMENT, SampleTableConstants.START_DATE,
			SampleTableConstants.END_DATE, SampleTableConstants.MAIL_COUNT, SampleTableConstants.DATA_FILE_COUNT };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 30, false), new ColumnWeightData(10, 30, false),new ColumnWeightData(80, 100, true), 
			new ColumnWeightData(70, 90, false), new ColumnWeightData(40, 50, false), new ColumnWeightData(40, 50, true), 
			new ColumnWeightData(40, 80, false), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 70, true), 
			new ColumnWeightData(50, 90, true), new ColumnWeightData(50, 70, true), new ColumnWeightData(50, 50, true) };
	
	private TableViewer viewer;
	private List<Sample> sample;
	private SampleList sampleList;

	private Action doubleClickAction;
	private Action startAction;
	private Action stopAction;
	private boolean paused;
	private Action pauseAction;
	private Action resumeAction;
	private Action skipAction;
	private boolean running;
	
	private List<Sample> samples;
	private Resource resource;
	private boolean isDirty;
	private Text txtFilePath;
	private Text txtDataFilename;
	private Text txtCurrentPosition;
	

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
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout());
//		rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer = new TableViewer(rootComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		Table table = viewer.getTable();
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_table.heightHint = 386;
		gd_table.widthHint = 586;
		table.setLayoutData(gd_table);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		createColumns(viewer);
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("New Column");
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("New Column");
		
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText("New TableItem");
		
		TableItem tableItem_1 = new TableItem(table, SWT.NONE);
		tableItem_1.setText("New TableItem");
		
		viewer.setContentProvider(new SampleGroupViewContentProvider(resUtil));
		viewer.setLabelProvider(new SampleGroupViewLabelProvider());
		
		samples = Collections.emptyList();
		try {
			resource = resUtil.getResource();
			resource.eAdapters().add(notifyListener);
			viewer.setInput(resource);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: "+resUtil.getFileName(), e2);
		}

		Composite statusArea=new Composite(rootComposite, SWT.NONE);
		statusArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		statusArea.setLayout(new GridLayout(4, false));
		Label lblSampleListFile = new Label(statusArea, SWT.None);
		lblSampleListFile.setText("Sample List File: ");

		txtFilePath = new Text(statusArea, SWT.BORDER | SWT.READ_ONLY);
		txtFilePath.setToolTipText("show the filename holding the sample list displayed in the table above");
		txtFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFilePath.setEditable(false);
		
		Label lblDataFile = new Label(statusArea, SWT.NONE);
		lblDataFile.setText("Current Data File:");
		
		txtDataFilename = new Text(statusArea, SWT.BORDER);
		txtDataFilename.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDataFilename.setEditable(false);
		txtDataFilename.setToolTipText("Data file to be written for the current collection");
		
		Label lblProgress = new Label(statusArea, SWT.NONE);
		lblProgress.setText("List Progress:");
		
		ProgressBar progressBar = new ProgressBar(statusArea, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		progressBar.setToolTipText("show the data collection progress based on the list of active samples displayed in the table");
		
		Label lblCurrentState = new Label(statusArea, SWT.NONE);
		lblCurrentState.setText("Current Process:");
		
		txtCurrentPosition = new Text(statusArea, SWT.BORDER);
		txtCurrentPosition.setEditable(false);
		txtCurrentPosition.setToolTipText("show the current process position out of the total number of active processes ");
		txtCurrentPosition.setText("1/1");
		
		initialisation();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
//		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(RegionViewExtensionFactory.ID, selectionListener);
		
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
						logger.info("Samples {} collection completed.", resUtil.getFileName());
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
	}

	private void initialisation() {
		try {
			editingDomain=resUtil.getEditingDomain();
		} catch (Exception e) {
			logger.error("Cannot get editing domain object.", e);
			throw new RuntimeException("Cannot get editing domain object.");
		}
		CommandQueueViewFactory.getProcessor().addIObserver(this);
	}

	private void makeActions() {
		startAction= new Action() {

			@Override
			public void run() {
				super.run();
				logger.info("Start data collection on GDA server.");
				running = true;
				paused=false;
				updateActionIconsState();
				try {
					for (Sample sample : samples) {
						if (sample.isActive()) {
							JythonCommandCommandProvider command = new JythonCommandCommandProvider(sample.getCommand(), sample.getName(), null);
							CommandQueueViewFactory.getQueue().addToTail(command);
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
		protected CellEditor getCellEditor(Object element) {
			if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
				return new CheckboxCellEditor(table);
			} else if (SampleTableConstants.SAMPLE_NAME.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
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
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
				return true;
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
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
				} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
					return sample.getEmail();
				} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
					return sample.getCommand();
				} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
					return sample.getComment();
				} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
					return sample.getStartDate();
				} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
					return sample.getEndDate();
				} 
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (SampleTableConstants.ACTIVE.equals(columnIdentifier)) {
				if (value instanceof Boolean) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
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
						if (isValidCellID((String)value)) {
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
			} 
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

		private boolean isValidCellID(String value) {
			File dir=new File(File.separator+DATA_DRIVER+File.separator+value);
			if (dir.exists()) {
				return true;
			}
			String message="Cannot find the data directory '" + dir.getAbsolutePath()+"' for this sample on data storage driver.\n";
			openMessageBox(message, "Invalid Cell ID");
			return false;
		}

		private boolean isValidVisitID(Sample sample, String value) {
			if (sample.getCellID()== null || sample.getCellID().isEmpty()) {
				String message="Cell ID must be set before visit ID.\n";
				openMessageBox(message, "Cell ID Missing");
				return false;
			}
			File dir=new File(File.separator+DATA_DRIVER+File.separator+sample.getCellID()+File.separator+DATA_FOLDER+File.separator+Calendar.getInstance().get(Calendar.YEAR)+File.separator+value);
			if (dir.exists()) {
				return true;
			}
			String message="Cannot find the data directory '" + dir.getAbsolutePath()+"' for this sample on data storage driver.\n";
			openMessageBox(message, "Invalid Visit ID");
			return false;
		}
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
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(pauseAction);
		manager.add(resumeAction);
		manager.add(skipAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(pauseAction);
		manager.add(resumeAction);
		manager.add(skipAction);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void update(Object source, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}