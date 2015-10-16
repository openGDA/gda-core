package org.opengda.lde.ui.views;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
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
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
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
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyColumnLabelProvider;
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
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.ui.utils.StringUtils;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;

@SuppressWarnings("restriction")
public class ChildrenTableView extends ViewPart implements IEditingDomainProvider, ISelectionProvider {

	public static final String ID = "org.opengda.lde.ui.views.ChildrenTableView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(ChildrenTableView.class);
	private List<ISelectionChangedListener> selectionChangedListeners;
	private LDEResourceUtil resUtil;
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
	protected AnimationEngine animation=null;
	private Composite rootComposite;
	private CustomisedAdapterFactoryContentProvider contentprovider;
	
	private final String sampleColumnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.PROGRESS, SampleTableConstants.ACTIVE, 
			SampleTableConstants.SAMPLE_NAME, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, SampleTableConstants.SAMPLE_X_STEP, 
			SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP, 
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.COMMAND, SampleTableConstants.COMMENT, SampleTableConstants.DATA_FILE,
			SampleTableConstants.VISIT_ID, SampleTableConstants.CELL_ID, SampleTableConstants.CALIBRANT_NAME, 
			SampleTableConstants.CALIBRANT_X, SampleTableConstants.CALIBRANT_Y, SampleTableConstants.CALIBRANT_EXPOSURE, 
			SampleTableConstants.ENV_SCANNABLE_NAMES, SampleTableConstants.EMAIL, SampleTableConstants.START_DATE, SampleTableConstants.END_DATE, 
			SampleTableConstants.CALIBRATION_FILE, SampleTableConstants.STAGE_ID, SampleTableConstants.DETECTOR_X, SampleTableConstants.DETECTOR_Y, SampleTableConstants.DETECTOR_Z
			};

	private ColumnWeightData sampleColumnLayouts[] = { new ColumnWeightData(10, 50, false),new ColumnWeightData(10, 70, false), new ColumnWeightData(10, 35, false),
			new ColumnWeightData(80, 110, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), 
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),	
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 300, true), new ColumnWeightData(50, 300, true), new ColumnWeightData(50, 300, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 110, true), 
			new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 200, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true),
			new ColumnWeightData(50, 300, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true) 
			};
	
	private ISelectionListener selectionListener;
	protected ComposedAdapterFactory adapterFactory;
	protected TableViewerColumn progressColumn;
	protected TableViewerColumn statusColumn;
	protected AdapterFactoryEditingDomain editingDomain;
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
		new Label(plainComposite, SWT.None).setText("No parent is selected yet.");
		 
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
			tableViewerColumn.setLabelProvider(
					new PropertyColumnLabelProvider(adapterFactoryContentProvider, descriptor.getId()) {
						@Override
						public Image getImage(Object object) {
							// get ride of default image provided by EMFPlugin
							return null;
						}

						@Override
						public String getText(Object object) {
							// A hack to trap the date string so we can format it
							SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
							String text = super.getText(object);
							try {
								Date date=format.parse(text);
								return new SimpleDateFormat("dd/MM/yyyy").format(date);
							} catch (ParseException e) {
								//do nothing
							}
							return text;
						}
					});
				// make table cell editable cause tree node selection to move and data just typed in not shown until re-selection the same node.
//				tableViewerColumn.setEditingSupport(new PropertyEditingSupport(tableViewer, adapterFactoryContentProvider,descriptor.getId()));			
			}
	 }

	private void initialisation() {

		selectionListener= new INullSelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (part instanceof LDEExperimentsEditor) {
					if (selection.isEmpty()) {
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


	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);		
	}
}
