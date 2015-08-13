package org.opengda.lde.ui.views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.formattedtext.FormattedTextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.opengda.lde.events.CellChangedEvent;
import org.opengda.lde.events.DataReductionFailedEvent;
import org.opengda.lde.events.NewDataFileEvent;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleProcessingEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.events.StageChangedEvent;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.provider.LDEExperimentsItemProviderAdapterFactory;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.ui.cdatetime.CDateTimeCellEditor;
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.ui.utils.AnimatedTableItemFeedback;
import org.opengda.lde.ui.utils.StringUtils;
import org.opengda.lde.utils.LDEResourceUtil;
import org.opengda.lde.utils.SampleGroupEditingDomain;
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

public class SampleView extends ViewPart implements IEditingDomainProvider, ISelectionProvider, ISaveablePart, IObserver {

	public static final String ID = "org.opengda.lde.ui.views.SampleView"; //$NON-NLS-1$
	public static final String DATA_DRIVER = "dls";
	public static final String BEAMLINE_ID = "i11-1";
	public static final String DATA_FOLDER = "data";
	private static final Logger logger = LoggerFactory.getLogger(SampleView.class);
	private List<ISelectionChangedListener> selectionChangedListeners;
	private String dataDriver = DATA_DRIVER;
	private String beamlineID = BEAMLINE_ID;
	private String dataFolder = DATA_FOLDER;
	private LDEResourceUtil resUtil;
	private String[] cellIDs;
	private String[] calibrants;
	private Table childrentable;
	private Table propertytable;
	private TreeViewer selectionViewer;
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
	private Action addAction;
	private Action copyAction;
	private Action deleteAction;
	private Action undoAction;
	private Action redoAction;
	private List<Sample> samples;
	private List<Experiment> experiments;
	private int numActiveSamples;
	private Scriptcontroller eventAdmin;
	private String eventAdminName;
	private Image[] images;
	private TableViewer childrenTableViewer;
	protected int nameCount;
	private boolean isDirty;
	private Resource resource;
	private Sample currentSample;
	private long totalNumberOfPoints;
	protected long currentPointNumber;
	@SuppressWarnings("restriction")
	protected AnimationEngine animation=null;
	
	private final String columnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.PROGRESS, SampleTableConstants.ACTIVE, SampleTableConstants.SAMPLE_NAME,
			SampleTableConstants.CELL_ID, SampleTableConstants.VISIT_ID, SampleTableConstants.CALIBRANT_NAME, SampleTableConstants.CALIBRANT_X, 
			SampleTableConstants.CALIBRANT_Y, SampleTableConstants.CALIBRANT_EXPOSURE, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, 
			SampleTableConstants.SAMPLE_X_STEP, SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.DETECTOR_X, SampleTableConstants.DETECTOR_Y, SampleTableConstants.DETECTOR_Z, 
			SampleTableConstants.EMAIL, SampleTableConstants.START_DATE, SampleTableConstants.END_DATE, SampleTableConstants.COMMAND, 
			SampleTableConstants.MAIL_COUNT, SampleTableConstants.DATA_FILE_COUNT,SampleTableConstants.COMMENT };

	private ColumnWeightData columnLayouts[] = { new ColumnWeightData(10, 50, false),new ColumnWeightData(10, 70, false), new ColumnWeightData(10, 35, false),new ColumnWeightData(80, 110, true), 
			new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true),
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true),
			new ColumnWeightData(40, 200, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(40, 300, true),
			new ColumnWeightData(10, 50, false), new ColumnWeightData(10, 50, false),new ColumnWeightData(50, 300, true) };
	/**
	 * This keeps track of the editing domain that is used to track all changes to the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AdapterFactoryEditingDomain editingDomain;

	/**
	 * This is the one adapter factory used for providing views of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComposedAdapterFactory adapterFactory;
	/**
	 * This keeps track of the active content viewer, which may be either one of the viewers in the pages or the content outline viewer.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Viewer currentViewer;
	
	/**
	 * This is the property sheet page.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected List<PropertySheetPage> propertySheetPages = new ArrayList<PropertySheetPage>();

	public SampleView() {
		setTitleToolTip("Create a new or editing an existing experiment model");
		setContentDescription("A view for editing experiment model");
		setPartName("Experiment Model");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
		initializeEditingDomain();
	}

	/**
	 * This sets up the editing domain for the model viewer.
	 */
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new LDEExperimentsItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener
			(new CommandStackListener() {
				 public void commandStackChanged(final EventObject event) {
					 getViewSite().getShell().getDisplay().asyncExec
						 (new Runnable() {
							  public void run() {
								  firePropertyChange(PROP_DIRTY);

								  // Try to select the affected objects.
								  //
								  Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
								  if (mostRecentCommand != null) {
									  setSelectionToViewer(mostRecentCommand.getAffectedObjects());
								  }
								  for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext(); ) {
									  PropertySheetPage propertySheetPage = i.next();
									  if (propertySheetPage.getControl().isDisposed()) {
										  i.remove();
									  }
									  else {
										  propertySheetPage.refresh();
									  }
								  }
							  }
						  });
				 }
			 });

		// Create the editing domain with a special command stack.
		//
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
	}
	
	/**
	 * This sets the selection into whichever viewer is active.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelectionToViewer(Collection<?> collection) {
		final Collection<?> theSelection = collection;
		// Make sure it's okay.
		//
		if (theSelection != null && !theSelection.isEmpty()) {
			Runnable runnable =
				new Runnable() {
					public void run() {
						// Try to select the items in the current content viewer of the editor.
						//
						if (currentViewer != null) {
							currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
						}
					}
				};
			getSite().getShell().getDisplay().asyncExec(runnable);
		}
	}
	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm mainSashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH | SWT.VERTICAL);

		SashForm topSashForm = new SashForm(mainSashForm, SWT.BORDER | SWT.SMOOTH);
		topSashForm.setSashWidth(2);
		topSashForm.setWeights(new int[] { 4, 1 });
		
		Composite treecomposite = new Composite(topSashForm, SWT.NONE);
		treecomposite.setLayout(new TreeColumnLayout());
		
		selectionViewer = new TreeViewer(treecomposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		selectionViewer.setContentProvider(new AdapterFactoryContentProvider(SampleGroupEditingDomain.INSTANCE.getAdapterFactory()));
		selectionViewer.setLabelProvider(new AdapterFactoryLabelProvider(SampleGroupEditingDomain.INSTANCE.getAdapterFactory()));
		Tree tree = selectionViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		getSite().setSelectionProvider(selectionViewer);

		SashForm rightSashForm = new SashForm(topSashForm, SWT.VERTICAL);
		rightSashForm.setWeights(new int[] { 1, 2 });

		Composite childrencomposite = new Composite(rightSashForm, SWT.NONE);
		childrencomposite.setLayout(new TableColumnLayout());

		childrenTableViewer = new TableViewer(childrencomposite,SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		childrenTableViewer.setContentProvider(new AdapterFactoryContentProvider(SampleGroupEditingDomain.INSTANCE.getAdapterFactory()));
		childrenTableViewer.setLabelProvider(new AdapterFactoryLabelProvider(SampleGroupEditingDomain.INSTANCE.getAdapterFactory()));
		childrentable = childrenTableViewer.getTable();
		GridData gd_childrentable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_childrentable.heightHint = 386;
		gd_childrentable.widthHint = 1000;
		childrentable.setLayoutData(gd_childrentable);		
		childrentable.setHeaderVisible(true);
		childrentable.setLinesVisible(true);
		
		selectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection instanceof ExperimentDefinition) {
					childrenTableViewer.setInput(((ExperimentDefinition)selection).getExperiments());
				} else if (selection instanceof Experiment) {
					childrenTableViewer.setInput(((Experiment)selection).getStages());
				} else if (selection instanceof Stage) {
					childrenTableViewer.setInput(((Stage)selection).getCells());
				} else if (selection instanceof Cell) {
					childrenTableViewer.setInput(((Cell)selection).getSamples());
				} else if (selection instanceof Sample) {
					childrenTableViewer.setInput(Collections.EMPTY_LIST);
				}
				
			}
		});

		Composite propertycomposite = new Composite(rightSashForm, SWT.NONE);
		propertycomposite.setLayout(new TableColumnLayout());

		TableViewer propertyTableViewer = new TableViewer(propertycomposite,SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		propertytable = propertyTableViewer.getTable();
		GridData gd_propertytable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_propertytable.heightHint = 386;
		gd_propertytable.widthHint = 1000;
		propertytable.setLayoutData(gd_propertytable);		
		propertytable.setHeaderVisible(true);
		propertytable.setLinesVisible(true);

		SashForm bottomSsashForm = new SashForm(mainSashForm, SWT.BORDER | SWT.SMOOTH);

		Composite statusArea = new Composite(bottomSsashForm, SWT.NONE);
		GridData gd_statusArea = new GridData(SWT.FILL, SWT.FILL, true, false,1, 1);
		gd_statusArea.heightHint = 150;
		statusArea.setLayoutData(gd_statusArea);
		statusArea.setLayout(new GridLayout(5, false));

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
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
		// register as selection listener of sample editor if exist
//		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SampleViewExtensionFactory.ID, selectionListener);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(selectionViewer.getControl(), "org.opengda.lde.ui.views.sampleview");
		createActions();
		initializeToolBar();
		initializeMenu();

		updateActionIconsState();
	}
	
	private void createColumns(String[] columnHeaders, ColumnWeightData[] columnLayouts, TableViewer tableViewer) {
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


	private void initialisation() {
		try {
			resource = getResUtil().getResource();
			resource.eAdapters().add(notifyListener);
			selectionViewer.setInput(resource);
			selectionViewer.setSelection(new StructuredSelection(resource), true);
		} catch (Exception e2) {
			logger.error("Cannot load resouce from file: "+getResUtil().getFileName(), e2);
		}
		selectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection=(IStructuredSelection) event.getSelection();
				Object firstElement = selection.getFirstElement();
				childrenTableViewer.setInput(firstElement);
			}
		});

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
		
		selectionViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new ViewerDragAdapter(selectionViewer));
		selectionViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new EditingDomainViewerDropAdapter(editingDomain, selectionViewer));
		updateNumberActiveSamples();
		
		if (getEventAdminName()!=null) {
			eventAdmin = Finder.getInstance().find(getEventAdminName());
			if (eventAdmin!=null) {
				eventAdmin.addIObserver(this);
			}
		}
		//TODO only available for sample table, not cell or stage table
		images = loadAnimatedGIF(childrenTableViewer.getControl().getDisplay(), ImageConstants.ICON_RUNNING);
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
	
	private Image[] loadAnimatedGIF(Display display, String imagePath) {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(imagePath), null);
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

					@SuppressWarnings("restriction")
					@Override
					public void run() {
						if (currentSample!=null) {
							updateSampleStatus(currentSample, STATUS.COMPLETED);
						}
						if (animation!=null) {
							animation.cancelAnimation();
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
					@SuppressWarnings("restriction")
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
						selectionViewer.setSelection(new StructuredSelection(currentSample));
						if (animation!=null) {
							animation.cancelAnimation();
						}
						try {
							//TODO this should only apply to sample table.
							TableItem tableItem = childrenTableViewer.getTable().getItem(samples.indexOf(currentSample));
							AnimatedTableItemFeedback feedback = new AnimatedTableItemFeedback(childrenTableViewer.getControl().getShell(),images, tableItem,SampleTableConstants.COL_STATUS);
							animation= new AnimationEngine(feedback,-1,100);
							animation.schedule();
						} catch (Exception e) {
							e.printStackTrace();
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
						if (status==STATUS.PAUSED) {
							animation.sleep();
						} else if (status==STATUS.RUNNING) {
							animation.wakeUp();
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
				selectionViewer.refresh();
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
					final String usersEmail=sample.getCell().getEmail();
					final String[] recipients = usersEmail.split(" ");
					for (int i=0; i<recipients.length; i++) {
						recipients[i] = recipients[i].trim();
					}
					final String senderName=LocalProperties.get("org.opengda.mail.sender.name","i11-LDE");
					//TODO changeto i11-LDE operation email account please
					final String senderEmail=LocalProperties.get("org.opengda.mail.sender.email","chiu.tang@diamond.ac.uk");
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
					if (isDirty()) {
						doSave(new NullProgressMonitor());
					}
					InterfaceProvider.getCommandRunner().runCommand("datacollection.collectData("+getResUtil().getFileName()+")");
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
		
		addAction = new Action() {

			@Override
			public void run() {
				try {
					//TODO implement add experiment, stage, cell as well based on selected tree node type.
					Sample newSample = LDEExperimentsFactory.eINSTANCE.createSample();
					nameCount = StringUtils.largestIntAtEndStringsWithPrefix(getSampleNames(), newSample.getName());
					if (nameCount != -1) {
						// increment the name
						nameCount++;
						newSample.setName(newSample.getName() + nameCount);
					}
					editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, getResUtil().getSamples(), LDEExperimentsPackage.SAMPLE, newSample));
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
					//TODO implement copy experiment, stage, cell as well, based on selected tree node type.
					if (getSelectedSample() != null) {
						Sample copy = EcoreUtil.copy(getSelectedSample());
						copy.setSampleID(EcoreUtil.generateUUID());
						String sampleNamePrefix = StringUtils.prefixBeforeInt(copy.getName());
						int largestIntInNames = StringUtils.largestIntAtEndStringsWithPrefix(getSampleNames(), sampleNamePrefix);
						if (largestIntInNames != -1) {
							largestIntInNames++;
							copy.setName(sampleNamePrefix + largestIntInNames);
						}
						editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, getResUtil().getSamples(), LDEExperimentsPackage.SAMPLE, copy));
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
				//TODO implement delete experiment, stage, cell depending on selecet
				try {
					Sample selectedSample = getSelectedSample();
					if (selectedSample != null) {
						editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, getResUtil().getSamples(), LDEExperimentsPackage.SAMPLE, selectedSample));
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
		toolbarManager.add(addAction);
		toolbarManager.add(deleteAction);
		toolbarManager.add(copyAction);
		toolbarManager.add(undoAction);
		toolbarManager.add(redoAction);
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
		menuManager.add(addAction);
		menuManager.add(deleteAction);
		menuManager.add(copyAction);
		menuManager.add(undoAction);
		menuManager.add(redoAction);
		menuManager.add(new Separator());
	}

	@Override
	public void setFocus() {
		selectionViewer.getControl().setFocus();
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
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		}
	};

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
				resUtil.createExperiments()
				;
			}
			resource = resUtil.getResource();
			selectionViewer.setInput(resource);
			resource.eAdapters().add(notifyListener);

			// update existing sample list
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
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
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);		
	}

	@Override
	public ISelection getSelection() {
		return selectionViewer.getSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		selectionViewer.setSelection(selection);		
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
					return sample.getCell().getCellID();
				} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
					return sample.getCell().getVisitID();
				} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
					return sample.getCell().getCalibrant();
				} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
					return sample.getCell().getCalibrant_x();
				} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
					return sample.getCell().getCalibrant_y();
				} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
					return sample.getCell().getCalibrant_exposure();
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
					return sample.getCell().getStage().getDetector_x();
				} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
					return sample.getCell().getStage().getDetector_y();
				} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
					return sample.getCell().getStage().getDetector_z();
				} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
					return sample.getCell().getEmail();
				} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
					return sample.getCell().getStartDate();
				} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
					return sample.getCell().getEndDate();
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
							if (isDatesValid((Sample)element) && isValidCellID((Sample)element, ((Sample)element).getCell().getCellID())) {
								runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
							}
						} else {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.ACTIVE+" field for sample "+((Sample)element).getName(), e);
					}
					updateNumberActiveSamples();
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
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CELL_ID, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.CELL_ID+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.VISIT_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidVisitID((Sample)element, (String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__VISIT_ID, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.VISIT_ID+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_NAME+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_X, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_X+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_Y, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.CALIBRANT_Y+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE, value));
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
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_X, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_X+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_Y, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_Y+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_Z, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+SampleTableConstants.DETECTOR_Z+" field for sample "+((Sample)element).getName(), e);
				}
			} else if (SampleTableConstants.EMAIL.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidEmail((String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__EMAIL, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.EMAIL+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.START_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__START_DATE, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.START_DATE+" field for sample "+((Sample)element).getName(), e);
					}
				}
			} else if (SampleTableConstants.END_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__END_DATE, value));
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
			boolean startLessEnd = sample.getCell().getStartDate().compareTo(sample.getCell().getEndDate())<=0;
			boolean nowInBetween = now.compareTo(sample.getCell().getStartDate())>=0 && now.compareTo(sample.getCell().getEndDate())<0;
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
				if (element != sample && value.equals(sample.getCell().getCellID())) {
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
		dataDir += Calendar.getInstance().get(Calendar.YEAR)+File.separator+sample.getCell().getVisitID();
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

	@Override
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}
}
