package org.opengda.lde.ui.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
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
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
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
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.model.ldeexperiment.presentation.LDEExperimentsEditor;
import org.opengda.lde.model.ldeexperiment.provider.LDEExperimentsItemProviderAdapterFactory;
import org.opengda.lde.ui.providers.SampleGroupViewContentProvider;
import org.opengda.lde.ui.providers.SampleGroupViewLabelProvider;
import org.opengda.lde.ui.providers.SampleTableConstants;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildrenTableView extends ViewPart implements IEditingDomainProvider {

	public static final String ID = "org.opengda.lde.ui.views.ChildrenTableView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(ChildrenTableView.class);
	private LDEResourceUtil resUtil;
	private TableViewer viewer;
	private Resource resource;
	private Composite rootComposite;
	private CustomisedAdapterFactoryContentProvider contentprovider;

	private ISelectionListener selectionListener;
	protected ComposedAdapterFactory adapterFactory;
	protected TableViewerColumn progressColumn;
	protected AdapterFactoryEditingDomain editingDomain;
	private PageBook pageBook;
	private Composite plainComposite;

	public ChildrenTableView() {
		setTitleToolTip("List of children for the seleceted tree node.");
		setPartName("Children");
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
		new Label(plainComposite, SWT.None).setText("Children are not avaialable.");

		rootComposite = new Composite(pageBook,SWT.None);
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

		viewer.setInput(eObject);

		initialisation();

		// register as selection listener of sample editor if exist
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
	}

	/**
	 * Create table columns using property descriptors and
	 * AdapterFactoryContentProvider
	 *
	 * @param tableViewer
	 * @param propertyDescriptors
	 * @param adapterFactoryContentProvider
	 */
	private void createColumns(TableViewer tableViewer, IPropertySource propertySource,
			AdapterFactoryContentProvider adapterFactoryContentProvider) {
		IPropertyDescriptor[] propertyDescriptors = propertySource.getPropertyDescriptors();
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
							// A hack to trap the date string so we can format
							// it
							SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
							String text = super.getText(object);
							try {
								Date date = format.parse(text);
								return new SimpleDateFormat("dd/MM/yyyy").format(date);
							} catch (ParseException e) {
								// do nothing
							}
							return text;
						}
					});
		}
	}
	private final String sampleColumnHeaders[] = { SampleTableConstants.STATUS, SampleTableConstants.PROGRESS, SampleTableConstants.ACTIVE,
			SampleTableConstants.SAMPLE_NAME, SampleTableConstants.SAMPLE_X_START, SampleTableConstants.SAMPLE_X_STOP, SampleTableConstants.SAMPLE_X_STEP,
			SampleTableConstants.SAMPLE_Y_START, SampleTableConstants.SAMPLE_Y_STOP, SampleTableConstants.SAMPLE_Y_STEP,
			SampleTableConstants.SAMPLE_EXPOSURE, SampleTableConstants.COMMAND, SampleTableConstants.COMMENT, SampleTableConstants.DATA_FILE,
			SampleTableConstants.VISIT_ID, SampleTableConstants.CELL_ID, SampleTableConstants.CALIBRANT_NAME,
			SampleTableConstants.CALIBRANT_X, SampleTableConstants.CALIBRANT_Y, SampleTableConstants.CALIBRANT_EXPOSURE, SampleTableConstants.SPIN,
			SampleTableConstants.ENV_SCANNABLE_NAMES, SampleTableConstants.EMAIL, SampleTableConstants.START_DATE, SampleTableConstants.END_DATE,
			SampleTableConstants.CALIBRATION_FILE, SampleTableConstants.STAGE_ID, SampleTableConstants.DETECTOR_X, SampleTableConstants.DETECTOR_Y, SampleTableConstants.DETECTOR_Z
			};

	private ColumnWeightData sampleColumnLayouts[] = { new ColumnWeightData(10, 50, false),new ColumnWeightData(10, 70, false), new ColumnWeightData(10, 35, false),
			new ColumnWeightData(80, 110, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true), new ColumnWeightData(40, 65, true),
			new ColumnWeightData(40, 75, true), new ColumnWeightData(40, 300, true), new ColumnWeightData(50, 300, true), new ColumnWeightData(50, 300, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 110, true),
			new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true),
			new ColumnWeightData(40, 90, true), new ColumnWeightData(40, 200, true), new ColumnWeightData(50, 120, true), new ColumnWeightData(50, 120, true),
			new ColumnWeightData(50, 300, true), new ColumnWeightData(40, 55, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true), new ColumnWeightData(40, 80, true)
			};

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
			column.setMoveable(true);
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
							setTitleToolTip("List of Experiments.");
							setPartName("Experiments");
							parent.layout(true);
							pageBook.showPage(rootComposite);
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
							setTitleToolTip("List of Stages in experiment "+experiment.getName());
							setPartName("Stages (" + experiment.getName() + ")");
							parent.layout(true);
							pageBook.showPage(rootComposite);
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
							setTitleToolTip("List of Cells in stage "+stage.getStageID());
							setPartName("Cells (" + stage.getStageID() + ")");
							parent.layout(true);
							pageBook.showPage(rootComposite);
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
							setTitleToolTip("List of Samples in cell "+ cell.getName());
							setPartName("Samples (" + cell.getName() + ")");
							parent.layout(true);
							pageBook.showPage(rootComposite);
						} else if (firstElement instanceof Sample) {
							createColumns(viewer, sampleColumnHeaders,sampleColumnLayouts);

							viewer.setContentProvider(new SampleGroupViewContentProvider(getResUtil()));
							viewer.setLabelProvider(new SampleGroupViewLabelProvider());
							IFile file = getViewSite().getPage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
							String filename = file.getRawLocation().toOSString();
							try {
								List<Sample> samples=new ArrayList<Sample>();
								for (Sample sample : getResUtil().getSamples(filename).values()) {
									samples.add(sample);
								}
								viewer.setInput(samples);
							} catch (Exception e) {
								logger.error("Cannot load all samples from file: "+ filename, e);
							}
							viewer.getTable().getColumn(SampleTableConstants.COL_STATUS).setWidth(0);
							viewer.getTable().getColumn(SampleTableConstants.COL_PROGRESS).setWidth(0);
							setTitleToolTip("List of all Samples in file: "+ filename);
							setPartName("Samples (Client)");
							parent.layout(true);
							pageBook.showPage(rootComposite);
//							pageBook.showPage(plainComposite);
						}
					}
				}
			}
		};
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		try {
			getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionListener);
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
