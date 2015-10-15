package org.opengda.lde.ui.views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.formattedtext.FormattedTextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyColumnLabelProvider;
import org.opengda.lde.model.edit.CellTableConstants;
import org.opengda.lde.model.edit.ExperimentTableConstants;
import org.opengda.lde.model.edit.SampleTableConstants;
import org.opengda.lde.model.edit.StageTableConstants;
import org.opengda.lde.model.editor.ui.provider.CDateTimeCellEditor;
import org.opengda.lde.model.editor.ui.provider.CustomisedAdapterFactoryContentProvider;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.presentation.LDEExperimentsEditor;
import org.opengda.lde.model.ldeexperiment.provider.LDEExperimentsItemProviderAdapterFactory;
import org.opengda.lde.ui.Activator;
import org.opengda.lde.ui.ImageConstants;
import org.opengda.lde.ui.providers.ProgressLabelProvider;
import org.opengda.lde.ui.providers.SampleGroupViewContentProvider;
import org.opengda.lde.ui.providers.SampleGroupViewLabelProvider;
import org.opengda.lde.ui.utils.StringUtils;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;

public class ChildrenTableView extends ViewPart implements IEditingDomainProvider, ISelectionProvider {

	public static final String ID = "org.opengda.lde.ui.views.ChildrenTableView"; //$NON-NLS-1$
	public static final String DATA_DRIVER = "dls";
	public static final String BEAMLINE_ID = "i11-1";
	public static final String DATA_FOLDER = "data";
	private static final Logger logger = LoggerFactory.getLogger(ChildrenTableView.class);
	private List<ISelectionChangedListener> selectionChangedListeners;
	private String dataDriver = DATA_DRIVER;
	private String beamlineID = BEAMLINE_ID;
	private String dataFolder = DATA_FOLDER;
	private LDEResourceUtil resUtil;
	private String[] cellIDs;
	private String[] calibrants;
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
	private TableViewer viewer;
	protected int nameCount;
	private Resource resource;
	private Sample currentSample;
	private long totalNumberOfPoints;
	protected long currentPointNumber;
	@SuppressWarnings("restriction")
	protected AnimationEngine animation=null;
	
	private final String sampleColumnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.PROGRESS, SampleTableConstants.ACTIVE, 
			SampleTableConstants.SAMPLE_NAME, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, SampleTableConstants.SAMPLE_X_STEP, 
			SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.COMMAND, SampleTableConstants.COMMENT,
			SampleTableConstants.CELL_ID, SampleTableConstants.STAGE_ID, SampleTableConstants.DATA_FILE, SampleTableConstants.CALIBRATION_FILE
			};

	private final String sampleColumnHeadersForCell[] = {SampleTableConstants.ACTIVE, 
			SampleTableConstants.SAMPLE_NAME, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, SampleTableConstants.SAMPLE_X_STEP, 
			SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.COMMAND, SampleTableConstants.COMMENT
			};

	private final String cellColumnHeaders[] = { CellTableConstants.CELL_NAME, CellTableConstants.CELL_ID, CellTableConstants.VISIT_ID, 
			CellTableConstants.CALIBRANT_NAME, CellTableConstants.CALIBRANT_X, CellTableConstants.CALIBRANT_Y, CellTableConstants.CALIBRANT_EXPOSURE, 
			CellTableConstants.NUMBER_OF_SAMPLES, CellTableConstants.ENV_SCANNABLE_NAMES, 
			CellTableConstants.START_DATE, CellTableConstants.END_DATE, CellTableConstants.EMAIL, CellTableConstants.AUTO_EMAILING 
			};

	private final String stageColumnHeaders[] = { StageTableConstants.STAGE_ID, 
			StageTableConstants.DETECTOR_X, StageTableConstants.DETECTOR_Y, StageTableConstants.DETECTOR_Z, 
			StageTableConstants.CAMERA_X, StageTableConstants.CAMERA_Y, StageTableConstants.CAMERA_Z, StageTableConstants.NUMBER_OF_CELLS
			};

	private final String experimentColumnHeaders[] = { ExperimentTableConstants.NAME,ExperimentTableConstants.DESCRIPTION, ExperimentTableConstants.NUMBER_OF_STAGES};

	private ColumnWeightData sampleColumnLayouts[] = { new ColumnWeightData(10, 50, false),new ColumnWeightData(10, 70, false), new ColumnWeightData(10, 35, false),
			new ColumnWeightData(80, 110, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), 
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),	
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 300, true), new ColumnWeightData(50, 300, true),
			new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(50, 300, true), new ColumnWeightData(50, 300, true)
			};
	
	private ColumnWeightData sampleColumnLayoutsForCell[] = { new ColumnWeightData(10, 35, false),
			new ColumnWeightData(80, 110, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), 
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),	
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 300, true), new ColumnWeightData(50, 300, true)
			};

	private ColumnWeightData cellColumnLayouts[] = { new ColumnWeightData(10, 50, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 90, true), 
			new ColumnWeightData(40, 110, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 50, true), new ColumnWeightData(40, 80, true), 
			new ColumnWeightData(40, 90, false), new ColumnWeightData(40, 90, true), 
			new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(40, 200, true),  new ColumnWeightData(40, 60, true),
			};
	
	private ColumnWeightData stageColumnLayouts[] = { new ColumnWeightData(10, 70, true),
			new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), 
			new ColumnWeightData(40, 75, true),	new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 75, true), new ColumnWeightData(10, 50, true)
			};
	private ColumnWeightData experimentColumnLayouts[] = { new ColumnWeightData(80, 150, true), new ColumnWeightData(50, 500, true), new ColumnWeightData(10, 50, false)};

	private ISelectionListener selectionListener;
	protected ComposedAdapterFactory adapterFactory;
	protected TableViewerColumn progressColumn;
	protected TableViewerColumn statusColumn;
	protected AdapterFactoryEditingDomain editingDomain;
	private LinkedHashMap<EAttribute, String> cellAttributeMap;
	private LinkedHashMap<EAttribute, String> sampleAttributeMap;
	private PageBook pageBook;
	private Composite plainComposite;

	public ChildrenTableView() {
		setTitleToolTip("List of children for the seleceted tree node.");
//		setContentDescription("A table for editing child properties.");
		setPartName("Children List");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new LDEExperimentsItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		BasicCommandStack commandStack = new BasicCommandStack();
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		pageBook=new PageBook(parent,SWT.None);
		plainComposite=new Composite(pageBook, SWT.None);
		plainComposite.setLayout(new FillLayout());
		 
		rootComposite = new Composite(pageBook, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		rootComposite.setLayout(new GridLayout());

		viewer = new TableViewer(rootComposite,SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = viewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.heightHint = 386;
		gd_table.widthHint = 1000;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		String name = getResUtil().getFileName();
		try {
			resource = getResUtil().getResource(name);
		} catch (Exception e) {
			logger.error("Cannot load resouce from file: "+name, e);
		}
		EObject eObject = resource.getContents().get(0);
		contentprovider = new CustomisedAdapterFactoryContentProvider(adapterFactory);
		viewer.setContentProvider(contentprovider);
		viewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		createColumns(viewer, contentprovider.getPropertySource(((ExperimentDefinition)eObject).getExperiment().get(0)), contentprovider);

		samples = Collections.emptyList();
		resource.eAdapters().add(notifyListener);
		viewer.setInput(eObject);
		
		initialisation();
		// register as selection provider to the SelectionService
//		getViewSite().setSelectionProvider(this);
		// register as selection listener of sample editor if exist
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
		// Create the help context id for the viewer's control
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(childrenTableViewer.getControl(), "org.opengda.lde.ui.views.childerntableview");
		createClientActions();
		initializeToolBar();
		initializeMenu();
	}
	/**
	 * create table columns using customised Content Provider, Label Provider, and Input using List or array of Objects to be displayed
	 * @param tableViewer
	 * @param columnHeaders
	 * @param columnLayouts
	 * @param firstElement
	 */
	private void createColumns(TableViewer tableViewer, String[] columnHeaders, ColumnWeightData[] columnLayouts) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);
			column.setWidth(columnLayouts[i].minimumWidth);
		}
	}
	/**
	 * Create table columns using property descriptors and  AdapterFactoryContentProvider
	 * @param tableViewer
	 * @param propertyDescriptors
	 * @param adapterFactoryContentProvider
	 */
	private void createColumns(TableViewer tableViewer, IPropertySource propertySource, AdapterFactoryContentProvider adapterFactoryContentProvider) {
		IPropertyDescriptor[] propertyDescriptors=propertySource.getPropertyDescriptors();
			for (IPropertyDescriptor descriptor : propertyDescriptors) {
				TableColumn column = new TableColumn(tableViewer.getTable(), SWT.None);
				column.setText(descriptor.getDisplayName());
				column.setToolTipText(descriptor.getDescription());
				column.setWidth(150);
				column.setMoveable(true);
				column.setResizable(true);
				TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, column);
				tableViewerColumn.setLabelProvider(new PropertyColumnLabelProvider(adapterFactoryContentProvider, descriptor.getId()) {
					@Override
					public Image getImage(Object object) {
						//get ride of default image provided by EMFPlugin
						return null;
					}
				});
				// make table cell editable cause tree node selection to move and data just typed in not shown until re-selection the same node.
//				tableViewerColumn.setEditingSupport(new PropertyEditingSupport(tableViewer, adapterFactoryContentProvider,descriptor.getId()));			
			}
	 }

	private void initialisation() {

		selectionListener= new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (part instanceof LDEExperimentsEditor) {
					if (selection.isEmpty()) {
						new Label(plainComposite, SWT.None).setText("Children are not available.");
						pageBook.showPage(plainComposite);
					} else {
						Object firstElement = ((IStructuredSelection)selection).getFirstElement();
						Table oldtable = viewer.getTable();
						Composite parent=oldtable.getParent();
						oldtable.dispose();
						
						viewer=new TableViewer(rootComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
						Table table = viewer.getTable();
						GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
						gd_table.heightHint = 386;
						gd_table.widthHint = 1000;
						table.setLayoutData(gd_table);
						table.setHeaderVisible(true);
						table.setLinesVisible(true);

						ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
						//change order within a cell group
						viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new ViewerDragAdapter(viewer));
						viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] { LocalTransfer.getInstance() },new EditingDomainViewerDropAdapter(editingDomain, viewer));

						viewer.setContentProvider(contentprovider);

						if (firstElement instanceof ExperimentDefinition) {
							ExperimentDefinition experimentDefinition = (ExperimentDefinition) firstElement;
							EList<Experiment> experiments2 = experimentDefinition.getExperiment();
							Experiment experiment;
							if (experiments2.isEmpty()) {
								experiment = LDEExperimentsFactory.eINSTANCE.createExperiment();
							} else {
								experiment = experiments2.get(0);
							}
							createColumns(viewer, contentprovider.getPropertySource(experiment), contentprovider);
							viewer.setInput(experimentDefinition);
							setPartName("Experiments");
						} else if (firstElement instanceof Experiment) {
							Experiment experiment = (Experiment) firstElement;
							EList<Stage> stages = experiment.getStage();
							Stage stage;
							if (stages.isEmpty()) {
								stage = LDEExperimentsFactory.eINSTANCE.createStage();
							} else {
								stage = stages.get(0);
							}
							createColumns(viewer, contentprovider.getPropertySource(stage), contentprovider);
							viewer.setInput(experiment);
							setPartName("Stages (" + experiment.getName() + ")");
						} else if (firstElement instanceof Stage) {
							Stage stage = (Stage) firstElement;
							EList<Cell> cells = stage.getCell();
							Cell cell;
							if (cells.isEmpty()) {
								cell = LDEExperimentsFactory.eINSTANCE.createCell();
							} else {
								cell = cells.get(0);
							}
							createColumns(viewer, contentprovider.getPropertySource(cell), contentprovider);
							viewer.setInput(stage);
							setPartName("Cells (" + stage.getStageID() + ")");
						} else if (firstElement instanceof Cell) {
							Cell cell = (Cell) firstElement;
							EList<Sample> samples2 = cell.getSample();
							Sample sample;
							if (samples2.isEmpty()) {
								sample = LDEExperimentsFactory.eINSTANCE.createSample();
							} else {
								sample = samples2.get(0);
							}
							createColumns(viewer, contentprovider.getPropertySource(sample), contentprovider);
							viewer.setInput(cell);
							setPartName("Samples (" + cell.getName() + ")");
						} else if (firstElement instanceof Sample) {
								//display all samples with progress and collected data info.
							createColumns(viewer, sampleColumnHeaders,sampleColumnLayouts);
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
							
							images = loadAnimatedGIF(viewer.getControl().getDisplay(), ImageConstants.ICON_RUNNING);

							progressColumn = new TableViewerColumn(viewer, viewer.getTable().getColumn(1));
							ProgressLabelProvider progressLabelProvider = new ProgressLabelProvider(viewer, samples);
							if (eventAdmin != null) {
								progressLabelProvider.setEventAdmin(eventAdmin);
								eventAdmin.addIObserver(progressLabelProvider);
							}
							progressColumn.setLabelProvider(progressLabelProvider);
							try {
								viewer.setInput(getResUtil().getSamples());
							} catch (Exception e) {
								logger.error("Fail to get all samples from sample definition file", e);
							}
							setPartName("Samples ("
									+ ((Sample) firstElement).getCell().getStage().getExperiment().getName()+")");

							createServerActions();
							addServerActionsToToolBar();
							addserverActionsToMenu();
							updateActionIconsState();
						}
						parent.layout(true);
						pageBook.showPage(rootComposite);
					}
				}
			}
		};

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
	/**
	 * Create the actions.
	 */
	private void createServerActions() {

		startAction= new Action() {

			@Override
			public void run() {
				super.run();

				logger.info("Start data collection on GDA server.");
				running = true;
				paused=false;
				updateActionIconsState();
				try {
					IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if (activeEditor.isDirty()) {
						activeEditor.doSave(new NullProgressMonitor());
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
	}
	
	
	private void createClientActions() {
		
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
		toolbarManager.add(addAction);
		toolbarManager.add(deleteAction);
		toolbarManager.add(copyAction);
		toolbarManager.add(undoAction);
		toolbarManager.add(redoAction);
		toolbarManager.add(new Separator());		
	}

	private void addServerActionsToToolBar() {
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
		menuManager.add(addAction);
		menuManager.add(deleteAction);
		menuManager.add(copyAction);
		menuManager.add(undoAction);
		menuManager.add(redoAction);
		menuManager.add(new Separator());
	}
	private void addserverActionsToMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(startAction);
		menuManager.add(stopAction);
		menuManager.add(pauseAction);
		menuManager.add(resumeAction);
		menuManager.add(skipAction);
		menuManager.add(new Separator());
	}
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
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
		private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
					&& (!notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_Status()))) {
				//TODO what need here to sync view with editor???
			}
		}
	};
	private String[] stageIDs;
	private Composite childrencomposite;
	private LinkedHashMap<EAttribute, String> experimentAttributeMap;
	private LinkedHashMap<EAttribute, String> stageAttributeMap;
	private Composite rootComposite;
	private CustomisedAdapterFactoryContentProvider contentprovider;



	@Override
	public void dispose() {
		try {
			resUtil.getResource().eAdapters().remove(notifyListener);
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(LDEExperimentsEditor.ID, selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.dispose();
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
		return viewer.getSelection();
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

	private class ExperimentTableColumnEditingSupport extends EditingSupport {
		
		private String columnIdentifier;
		private Table table;

		public ExperimentTableColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
			super(viewer);
			table=((TableViewer)viewer).getTable();
			columnIdentifier=tableViewerColumn.getColumn().getText();
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (ExperimentTableConstants.NAME.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (ExperimentTableConstants.DESCRIPTION.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (ExperimentTableConstants.NUMBER_OF_STAGES.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (ExperimentTableConstants.NAME.equals(columnIdentifier)) {
				return true;
			} else if (ExperimentTableConstants.DESCRIPTION.equals(columnIdentifier)) {
				return true;
			} else if (ExperimentTableConstants.NUMBER_OF_STAGES.equals(columnIdentifier)) {
				return true;
			}
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Experiment) {
				Experiment experiment = (Experiment) element;
				 if (ExperimentTableConstants.NAME.equals(columnIdentifier)) {
					return experiment.getName();
				} else if (ExperimentTableConstants.DESCRIPTION.equals(columnIdentifier)) {
					return experiment.getDescription();
				} else if (ExperimentTableConstants.NUMBER_OF_STAGES.equals(columnIdentifier)) {
					return experiment.getNumberOfStages();
				} 
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (ExperimentTableConstants.NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.EXPERIMENT__NAME, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+ExperimentTableConstants.NAME+" field for experiment "+((Experiment)element).getName(), e);
					}
				}
			} else if (ExperimentTableConstants.DESCRIPTION.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.EXPERIMENT__DESCRIPTION, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+ExperimentTableConstants.DESCRIPTION+" field for experiment "+((Experiment)element).getName(), e);
					}
				}
			} else if (ExperimentTableConstants.DESCRIPTION.equals(columnIdentifier)) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.EXPERIMENT__NUMBER_OF_STAGES, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+ExperimentTableConstants.NUMBER_OF_STAGES+" field for experiment "+((Experiment)element).getName(), e);
				}
			} 
		}
	}
	
	private class StageTableColumnEditingSupport extends EditingSupport {
		
		private String columnIdentifier;
		private Table table;

		public StageTableColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
			super(viewer);
			table=((TableViewer)viewer).getTable();
			columnIdentifier=tableViewerColumn.getColumn().getText();
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (StageTableConstants.STAGE_ID.equals(columnIdentifier)) {
				final ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table, SWT.READ_ONLY);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getStageIDs());
				return ce;
			} else if (StageTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (StageTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (StageTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (StageTableConstants.CAMERA_X.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (StageTableConstants.CAMERA_Y.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (StageTableConstants.CAMERA_Z.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (StageTableConstants.STAGE_ID.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.CAMERA_X.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.CAMERA_Y.equals(columnIdentifier)) {
				return true;
			} else if (StageTableConstants.CAMERA_Z.equals(columnIdentifier)) {
				return true;
			} 
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Stage) {
				Stage stage = (Stage) element;
				if (StageTableConstants.STAGE_ID.equals(columnIdentifier)) {
					return stage.getStageID();
				} else if (StageTableConstants.DETECTOR_X.equals(columnIdentifier)) {
					return stage.getDetector_x();
				} else if (StageTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
					return stage.getDetector_y();
				} else if (StageTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
					return stage.getDetector_z();
				} else if (StageTableConstants.CAMERA_X.equals(columnIdentifier)) {
					return stage.getCamera_x();
				} else if (StageTableConstants.CAMERA_Y.equals(columnIdentifier)) {
					return stage.getCamera_y();
				} else if (StageTableConstants.CAMERA_Z.equals(columnIdentifier)) {
					return stage.getCamera_z();
				} 
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (StageTableConstants.STAGE_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidStageID((Stage)element,(String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__STAGE_ID, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+StageTableConstants.STAGE_ID+" field for stage "+((Stage)element).getStageID(), e);
					}
				}
			} else if (StageTableConstants.DETECTOR_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_X, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.DETECTOR_X+" field for stage "+((Stage)element).getStageID(), e);
				}
			} else if (StageTableConstants.DETECTOR_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_Y, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.DETECTOR_Y+" field for stage "+((Stage)element).getStageID(), e);
				}
			} else if (StageTableConstants.DETECTOR_Z.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__DETECTOR_Z, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.DETECTOR_Z+" field for stage "+((Stage)element).getStageID(), e);
				}
			} else if (StageTableConstants.CAMERA_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__CAMERA_X, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.CAMERA_X+" field for stage "+((Stage)element).getStageID(), e);
				}
			} else if (StageTableConstants.CAMERA_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__CAMERA_Y, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.CAMERA_Y+" field for stage "+((Stage)element).getStageID(), e);
				}
			} else if (StageTableConstants.CAMERA_Z.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.STAGE__CAMERA_Z, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+StageTableConstants.CAMERA_Z+" field for stage "+((Stage)element).getStageID(), e);
				}
			}
		}
	}
	private boolean isValidStageID(Stage element, String value) {
		if (value == null || value.isEmpty()) {
			String message="You must select a Sample Stage ID.\n";
			openMessageBox(message, "Invalid Stage ID");
			return false;
		}
		
		try {
			for (Stage stage : resUtil.getStages()) {
				if (element != stage && value.equals(stage.getStageID())) {
					String message="Sample Stage is already used.\n";
					openMessageBox(message, "Invalid Satge ID");
					return false;
				}
			}
		} catch (Exception e) {
			logger.error("Fail to get all stages in a resource.", e);
		}
		return true;
	}

	private class CellTableColumnEditingSupport extends EditingSupport {
		
		private String columnIdentifier;
		private Table table;

		public CellTableColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
			super(viewer);
			table=((TableViewer)viewer).getTable();
			columnIdentifier=tableViewerColumn.getColumn().getText();
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (CellTableConstants.CELL_NAME.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (CellTableConstants.CELL_ID.equals(columnIdentifier)) {
				final ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table, SWT.READ_ONLY);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getCellIDs());
				return ce;
			} else if (CellTableConstants.VISIT_ID.equals(columnIdentifier)) {
				final ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getVisitIDs());
				return ce;
//				return new TextCellEditor(table);
			} else if (CellTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				ComboBoxViewerCellEditor ce = new ComboBoxViewerCellEditor(table, SWT.READ_ONLY);
				ce.setLabelProvider(new LabelProvider());
				ce.setContentProvider(new ArrayContentProvider());
				ce.setInput(getCalibrants());
				return ce;
			} else if (CellTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (CellTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (CellTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				return new FormattedTextCellEditor(table);
			} else if (CellTableConstants.ENV_SCANNABLE_NAMES.equals(columnIdentifier)) {
				//TODO using a list of environment scannables
				return new TextCellEditor(table);
			} else if (CellTableConstants.START_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
			} else if (CellTableConstants.END_DATE.equals(columnIdentifier)){
				return new CDateTimeCellEditor(table);
			} else if (CellTableConstants.EMAIL.equals(columnIdentifier)) {
				return new TextCellEditor(table);
			} else if (CellTableConstants.AUTO_EMAILING.equals(columnIdentifier)) {
				return new CheckboxCellEditor(table);
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (CellTableConstants.CELL_NAME.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.CELL_ID.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.VISIT_ID.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.ENV_SCANNABLE_NAMES.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.START_DATE.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.END_DATE.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.EMAIL.equals(columnIdentifier)) {
				return true;
			} else if (CellTableConstants.AUTO_EMAILING.equals(columnIdentifier)) {
				return true;
			} 
			return false;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Cell) {
				Cell cell = (Cell) element;
				if (CellTableConstants.CELL_NAME.equals(columnIdentifier)) {
					return cell.getName();
				} else if (CellTableConstants.CELL_ID.equals(columnIdentifier)) {
					return cell.getCellID();
				} else if (CellTableConstants.VISIT_ID.equals(columnIdentifier)) {
					return cell.getVisitID();
				} else if (CellTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
					return cell.getCalibrant();
				} else if (CellTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
					return cell.getCalibrant_x();
				} else if (CellTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
					return cell.getCalibrant_y();
				} else if (CellTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
					return cell.getCalibrant_exposure();
				} else if (CellTableConstants.ENV_SCANNABLE_NAMES.equals(columnIdentifier)) {
					return cell.getEnvScannableNames();
				} else if (CellTableConstants.START_DATE.equals(columnIdentifier)) {
					return cell.getStartDate();
				} else if (CellTableConstants.END_DATE.equals(columnIdentifier)) {
					return cell.getEndDate();
				} else if (CellTableConstants.EMAIL.equals(columnIdentifier)) {
					return cell.getEmail();
				} else if (CellTableConstants.AUTO_EMAILING.equals(columnIdentifier)) {
					return cell.isEnableAutoEmail();
				} 
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (CellTableConstants.CELL_NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__NAME, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.CELL_NAME+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.CELL_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidCellID((Cell)element,(String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CELL_ID, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.CELL_ID+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.VISIT_ID.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidVisitID((Sample)element, (String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__VISIT_ID, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.VISIT_ID+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.CALIBRANT_NAME.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.CALIBRANT_NAME+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.CALIBRANT_X.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_X, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+CellTableConstants.CALIBRANT_X+" field for cell "+((Cell)element).getName(), e);
				}
			} else if (CellTableConstants.CALIBRANT_Y.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_Y, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+CellTableConstants.CALIBRANT_Y+" field for cell "+((Cell)element).getName(), e);
				}
			} else if (CellTableConstants.CALIBRANT_EXPOSURE.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__CALIBRANT_EXPOSURE, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+CellTableConstants.CALIBRANT_EXPOSURE+" field for cell "+((Cell)element).getName(), e);
				}
			} else if (CellTableConstants.ENV_SCANNABLE_NAMES.equals(columnIdentifier)) {
				try {
					runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__ENV_SCANNABLE_NAMES, value));
				} catch (Exception e) {
					logger.error("Exception on setting "+CellTableConstants.ENV_SCANNABLE_NAMES+" field for cell "+((Cell)element).getName(), e);
				}
			} else if (CellTableConstants.START_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__START_DATE, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.START_DATE+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.END_DATE.equals(columnIdentifier)) {
				if (value instanceof Date) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__END_DATE, value));
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.END_DATE+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.EMAIL.equals(columnIdentifier)) {
				if (value instanceof String) {
					try {
						if (isValidEmail((String)value)) {
							runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.CELL__EMAIL, value));
						}
					} catch (Exception e) {
						logger.error("Exception on setting "+CellTableConstants.EMAIL+" field for cell "+((Cell)element).getName(), e);
					}
				}
			} else if (CellTableConstants.AUTO_EMAILING.equals(columnIdentifier)) {
				if (value instanceof Boolean) {
					try {
						runCommand(SetCommand.create(editingDomain, element, LDEExperimentsPackage.eINSTANCE.getSample_Active(), value));
					} catch (Exception e) {
						logger.error("Exception on setting "+SampleTableConstants.ACTIVE+" field for sample "+((Sample)element).getName(), e);
					}
				}
			}
		}

	}

	private class SampleTableColumnEditingSupport extends EditingSupport {
		
		private String columnIdentifier;
		private Table table;

		public SampleTableColumnEditingSupport(ColumnViewer viewer, TableViewerColumn tableViewerColumn) {
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
				} else if (SampleTableConstants.COMMAND.equals(columnIdentifier)) {
					return sample.getCommand();
				} else if (SampleTableConstants.COMMENT.equals(columnIdentifier)) {
					return sample.getComment();
				} else if (SampleTableConstants.CELL_ID.equals(columnIdentifier)) {
					return sample.getCell().getCellID();
				} else if (SampleTableConstants.STAGE_ID.equals(columnIdentifier)) {
					return sample.getCell().getStage().getStageID();
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
							if (isDatesValid((Sample)element)) {
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
	
	public String[] getStageIDs() {
		return stageIDs;
	}
	public void setStageIDs(String[] stageIDs) {
		this.stageIDs = stageIDs;
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
	
	private boolean isValidCellID(Cell element, String value) {
		if (value == null || value.isEmpty()) {
			String message="You must select a Sample Cell ID.\n";
			openMessageBox(message, "Invalid Cell ID");
			return false;
		}
		try {
			for (Cell cell : resUtil.getCells()) {
				if (element != cell && value.equals(cell.getCellID())) {
					String message="Sample Cell is already used.\n";
					openMessageBox(message, "Invalid Cell ID");
					return false;
				}
			}
		} catch (Exception e) {
			logger.error("Fail to get all cells from resource.",e);
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


	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);		
	}
}
