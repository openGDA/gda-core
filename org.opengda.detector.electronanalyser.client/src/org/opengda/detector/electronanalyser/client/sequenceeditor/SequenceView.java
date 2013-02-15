package org.opengda.detector.electronanalyser.client.sequenceeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.provider.RegiondefinitionItemProviderAdapterFactory;

public class SequenceView extends ViewPart implements ISelectionProvider {
	private List<ISelectionChangedListener> selectionChangedListeners;

	public SequenceView() {
		setTitleToolTip("Create a new or editing an existing region");
		setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	}

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Text text;
	private Text txtLocation;
	private Text txtUser;
	private Text txtSample;
	private Text txtFilename;
	private Text txtComments;

	private final String columnHeaders[] = { SequenceTableConstants.STATUS,
			SequenceTableConstants.ENABLED, SequenceTableConstants.REGION_NAME,
			SequenceTableConstants.LENS_MODE,
			SequenceTableConstants.PASS_ENERGY,
			SequenceTableConstants.EXCITATION_ENERGY,
			SequenceTableConstants.ENERGY_MODE,
			SequenceTableConstants.LOW_ENERGY,
			SequenceTableConstants.HIGH_ENERGY,
			SequenceTableConstants.ENERGY_STEP,
			SequenceTableConstants.STEP_TIME, SequenceTableConstants.STEPS,
			SequenceTableConstants.TOTAL_TIME,
			SequenceTableConstants.X_CHANNEL_FROM,
			SequenceTableConstants.X_CHANNEL_TO,
			SequenceTableConstants.Y_CHANNEL_FROM,
			SequenceTableConstants.Y_CHANNEL_TO, SequenceTableConstants.SLICES,
			SequenceTableConstants.MODE };

	private ColumnLayoutData columnLayouts[] = {
			new ColumnWeightData(10, false), new ColumnWeightData(10, false),
			new ColumnWeightData(80, true), new ColumnWeightData(70, false),
			new ColumnWeightData(40, false), new ColumnWeightData(50, true),
			new ColumnWeightData(40, false), new ColumnWeightData(50, true),
			new ColumnWeightData(50, true), new ColumnWeightData(60, true),
			new ColumnWeightData(40, true), new ColumnWeightData(40, true),
			new ColumnWeightData(40, true), new ColumnWeightData(40, true),
			new ColumnWeightData(40, true), new ColumnWeightData(40, true),
			new ColumnWeightData(40, true), new ColumnWeightData(40, true),
			new ColumnWeightData(40, true) };
	private ComposedAdapterFactory adapterFactory;
	private TableViewer sequenceTableViewer;

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(
					tableViewer, SWT.None);
			TableColumn column = tableViewerColumn.getColumn();
			column.setResizable(columnLayouts[i].resizable);
			column.setText(columnHeaders[i]);
			column.setToolTipText(columnHeaders[i]);
			layout.setColumnData(column, columnLayouts[i]);
			// tableViewerColumn.setEditingSupport(new TomoColumnEditingSupport(
			// tableViewer, tableViewerColumn));
		}
	}

	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory
				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new RegiondefinitionItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(gl_root);

		Composite tableViewerContainer = new Composite(rootComposite, SWT.None);

		sequenceTableViewer = new TableViewer(tableViewerContainer, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI);
		sequenceTableViewer.getTable().setHeaderVisible(true);
		sequenceTableViewer.getTable().setLinesVisible(true);
		sequenceTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection) {
							IStructuredSelection sel = (IStructuredSelection) selection;
							Object firstElement = sel.getFirstElement();
							if (firstElement instanceof Region) {
								Region region = (Region) firstElement;
								fireSelectionChanged(region);
							}

						}
					}
				});
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableViewerContainer.setLayout(tableLayout);

		createColumns(sequenceTableViewer, tableLayout);

		// table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));
		tableViewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));

		Resource resource = null;
		try {
			resource = regionDefinitionResourceUtil.getResource();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		sequenceTableViewer.setContentProvider(new SequenceViewContentProvider(
				resource));
		sequenceTableViewer.setLabelProvider(new SequenceViewLabelProvider());
		List<Region> regions = Collections.EMPTY_LIST;
		try {
			regions = regionDefinitionResourceUtil.getRegions(false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sequenceTableViewer.setInput(regions);
		// initializeEditingDomain();
		// sequenceTableViewer
		// .setContentProvider(new AdapterFactoryContentProvider(
		// adapterFactory));
		// sequenceTableViewer.setLabelProvider(new AdapterFactoryLabelProvider(
		// adapterFactory));
		// EditingDomain sequenceEditingDomain = null;
		// try {
		// sequenceEditingDomain = ElectronAnalyserClientPlugin.getDefault()
		// .getSequenceEditingDomain();
		// } catch (Exception e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// if (sequenceEditingDomain != null) {
		// ResourceSet resourceSet = sequenceEditingDomain.getResourceSet();
		// Resource resource = (Resource)resourceSet.getResources().get(0);
		// sequenceTableViewer
		// .setInput(resource.getContents());
		// }
		//
		Composite controlArea = new Composite(rootComposite, SWT.None);
		// Contains region editing, sequence parameters, file saving info and
		// comments.
		controlArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlArea.setLayout(new GridLayout(2, false));

		Composite leftArea = new Composite(controlArea, SWT.None);
		leftArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		leftArea.setLayout(new GridLayout(4, false));

		Composite rightArea = new Composite(controlArea, SWT.None);
		rightArea.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false,
				1, 1));
		rightArea.setLayout(new GridLayout());

		Group grpRegion = new Group(leftArea, SWT.NONE);
		GridData gd_grpRegion = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpRegion.grabExcessHorizontalSpace = false;
		gd_grpRegion.horizontalAlignment = SWT.LEFT;
		gd_grpRegion.widthHint = 226;
		grpRegion.setLayoutData(gd_grpRegion);
		grpRegion.setText("Region Control");
		grpRegion.setLayout(new RowLayout());

		Button btnNew = new Button(grpRegion, SWT.NONE);
		btnNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNew.setText("New");

		Button button = new Button(grpRegion, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		button.setText("Copy");

		Button btnDelete = new Button(grpRegion, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnDelete.setText("Delete");

		Button btnUndo = new Button(grpRegion, SWT.NONE);
		btnUndo.setText("Undo");

		Button btnRedo = new Button(grpRegion, SWT.NONE);
		btnRedo.setText("Redo");

		Group grpActiveRegions = new Group(leftArea, SWT.NONE);
		GridData gd_grpActiveRegions = new GridData(GridData.FILL_HORIZONTAL);
		gd_grpActiveRegions.grabExcessHorizontalSpace = false;
		grpActiveRegions.setLayoutData(gd_grpActiveRegions);
		grpActiveRegions.setText("Active regions");
		grpActiveRegions.setLayout(new RowLayout());

		text = new Text(grpActiveRegions, SWT.BORDER);
		// TODO set active or enable region counts
		text.setEditable(false);
		new Label(leftArea, SWT.NONE);
		new Label(leftArea, SWT.NONE);

		Group grpInfo = new Group(leftArea, SWT.NONE);
		GridData layoutData1 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData1.horizontalSpan = 4;
		grpInfo.setLayoutData(layoutData1);
		grpInfo.setText("Info");
		grpInfo.setLayout(new GridLayout(3, false));

		Label lblLocation = new Label(grpInfo, SWT.NONE);
		lblLocation.setText("Location");

		txtLocation = new Text(grpInfo, SWT.BORDER);
		txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		txtLocation.setText("Location");

		Label lblComments = new Label(grpInfo, SWT.NONE);
		lblComments.setText("Comments");

		Label lblUser = new Label(grpInfo, SWT.NONE);
		lblUser.setText("User");

		txtUser = new Text(grpInfo, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,
				1));
		txtUser.setText("User");

		txtComments = new Text(grpInfo, SWT.BORDER | SWT.MULTI);
		txtComments.setText("comments");
		GridData gd_txtComments = new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1);
		gd_txtComments.verticalSpan = 3;
		txtComments.setLayoutData(gd_txtComments);

		Label lblSample = new Label(grpInfo, SWT.NONE);
		lblSample.setText("Sample");

		txtSample = new Text(grpInfo, SWT.BORDER);
		txtSample.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		txtSample.setText("Sample");

		Label lblFileName = new Label(grpInfo, SWT.NONE);
		lblFileName.setText("File Name");

		txtFilename = new Text(grpInfo, SWT.BORDER);
		txtFilename.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		txtFilename.setText("Filename");

		Group grpSequnceRunMode = new Group(rightArea, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 220;
		layoutData.verticalSpan = 2;
		grpSequnceRunMode.setLayoutData(layoutData);
		grpSequnceRunMode.setLayout(new GridLayout(2, false));
		grpSequnceRunMode.setText("Sequence Run Mode");

		Combo runMode = new Combo(grpSequnceRunMode, SWT.READ_ONLY);
		runMode.setItems(new String[] { "Normal", "Add Dimension" });
		runMode.setToolTipText("List of available run modes");
		runMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		runMode.setText(runMode.getItem(0));

		new Label(grpSequnceRunMode, SWT.NONE);

		Button btnNumberOfIterations = new Button(grpSequnceRunMode, SWT.RADIO);
		btnNumberOfIterations.setText("Number of iterations");

		Spinner spinner = new Spinner(grpSequnceRunMode, SWT.BORDER);
		spinner.setMinimum(1);
		spinner.setToolTipText("Set number of iterations required");

		Button btnRepeatuntilStopped = new Button(grpSequnceRunMode, SWT.RADIO);
		btnRepeatuntilStopped.setText("Repeat until stopped");

		new Label(grpSequnceRunMode, SWT.NONE);

		Button btnConfirmAfterEachInteration = new Button(grpSequnceRunMode,
				SWT.CHECK);
		btnConfirmAfterEachInteration.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		btnConfirmAfterEachInteration.setText("Confirm after each iteration");
		new Label(grpSequnceRunMode, SWT.NONE);

		Composite actionArea = new Composite(rootComposite, SWT.None);
		// Contains region editing, sequence parameters, file saving info and
		// comments.
		actionArea.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
				false, 1, 1));
		actionArea.setLayout(new RowLayout());

		Button btnStart = new Button(actionArea, SWT.NONE);
		btnStart.setText("Start");

		Button btnOK = new Button(actionArea, SWT.NONE);
		btnOK.setText("OK");
		Button btnCancel = new Button(actionArea, SWT.NONE);
		btnCancel.setText("Cancel");
		initaliseValues();
		// register as selection provider to the SelectionService
		getViewSite().setSelectionProvider(this);
	}

	private void initaliseValues() {
	}

	private List<Region> getRegions() {
		if (regionDefinitionResourceUtil != null) {
			try {
				return regionDefinitionResourceUtil.getRegions(false);
			} catch (Exception e) {
				// FIXME - logger
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void setFocus() {

	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinition) {
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
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	private void fireSelectionChanged(Region region) {
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(new SelectionChangedEvent(this,
					new StructuredSelection(region)));

		}

	}
}
