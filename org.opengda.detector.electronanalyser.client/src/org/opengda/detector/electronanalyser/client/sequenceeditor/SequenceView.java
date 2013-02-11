package org.opengda.detector.electronanalyser.client.sequenceeditor;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.util.RegionDefinitionResourceUtil;

public class SequenceView extends ViewPart {
	public SequenceView() {
		setTitleToolTip("Create a new or editing an existing region");
		setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
	}

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Table table;
	private Text text;
	private Text txtLocation;
	private Text txtUser;
	private Text txtSample;
	private Text txtFilename;
	private Text txtComments;

	private final String columnHeaders[] = { "Status", "Enabled",
			"Region Name", "Lens Mode", "Pass Energy", "Excitation Energy",
			"Energy Mode", "Energy Low",
			"Energy High", "Energy Step", "Step Time", "Steps", "Total Time",
			"X-Channel From", "X-Channel To", "Y-Channel from", "Y-Channel To",
			"Slices", "Mode" };
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
			new ColumnWeightData(40, true)
			};

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(
					tableViewer, SWT.None);
			tableViewerColumn.getColumn().setResizable(
					columnLayouts[i].resizable);
			tableViewerColumn.getColumn().setText(columnHeaders[i]);
			tableViewerColumn.getColumn().setToolTipText(columnHeaders[i]);
			layout.setColumnData(tableViewerColumn.getColumn(),
					columnLayouts[i]);
			// tableViewerColumn.setEditingSupport(new TomoColumnEditingSupport(
			// tableViewer, tableViewerColumn));
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(gl_root);

		Composite tableViewerContainer = new Composite(rootComposite, SWT.None);

		TableViewer tableViewer = new TableViewer(tableViewerContainer,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		//table = tableViewer.getTable();
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableViewerContainer.setLayout(tableLayout);

		createColumns(tableViewer, tableLayout);

		// table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));
		tableViewerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));
		
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
		//TODO set active or enable region counts
		text.setEditable(false);
		
		Group grpBeamControl = new Group(leftArea, SWT.NONE);
		grpBeamControl.setLayout(new RowLayout());
		grpBeamControl.setText("X-Ray Source");
		
		Button btnHard = new Button(grpBeamControl, SWT.RADIO);
		btnHard.setText("Hard");
		
		Button btnSoft = new Button(grpBeamControl, SWT.RADIO);
		btnSoft.setText("Soft");
		
		Group grpShutter = new Group(leftArea, SWT.NONE);
		grpShutter.setLayout(new RowLayout());
		grpShutter.setText("Shutter");
		
		Button btnOpen = new Button(grpShutter, SWT.NONE);
		btnOpen.setText("Open");
		
		Group grpInfo = new Group(leftArea, SWT.NONE);
		GridData layoutData1 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData1.horizontalSpan =4;
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
		// for (Region region : getRegions()) {
		// Label lblName = new Label(parent, SWT.None);
		// lblName.setText(region.getName());
		// GridData layoutData = new GridData();
		// layoutData.horizontalSpan = 2;
		// lblName.setLayoutData(layoutData);
		// }

	}

	private void initaliseValues() {
	}

	private List<Region> getRegions() {
		if (regionDefinitionResourceUtil != null) {
			return regionDefinitionResourceUtil.getRegions(false);
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
}
