/*-
 * Copyright © 2022 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.sample.plate.management.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.sample.plate.management.ui.configurables.HolderSerialNumberList;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegisteredPlate;
import uk.ac.diamond.daq.sample.plate.management.ui.models.RegisteredSample;
import uk.ac.diamond.daq.sample.plate.management.ui.service.SpyCatApiConnector;
import uk.ac.diamond.ispyb.api.IspybSpyCatApi;
import uk.ac.diamond.ispyb.api.Session;
import uk.ac.diamond.ispyb.api.SessionPerson;
import uk.ac.gda.client.live.stream.view.SnapshotView;

public class PlateManagementView {

	public static final String ID = "uk.ac.diamond.daq.sample.plate.management.ui.PlateManagementView";

	private static final Logger logger = LoggerFactory.getLogger(PlateManagementView.class);

	@Inject
	IEventBroker broker;

	private Composite parent;

	private Composite child;

	private static final String DOUBLE_REGEX = "\\d*(\\.\\d*)?";

	final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	final Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

	private double[] xCalibratedAxis;
	private double[] yCalibratedAxis;

	private int currentId = 0;

	private boolean samplePositionChangeEnabled = false;

	private Map<String, ImmutablePair<String, Integer>> visits;

	private Table table;

	private TableViewer viewer;

	private TabFolder tabFolder;
	private TabItem addSampleTab;
	private TabItem editSampleTab;

	private Group sampleManagementGroup;
	private Group sampleAdditionGroup;
	private Group sampleEditingGroup;

	private Label sampleIdEditLabel;
	private Label sampleIdAddLabel;

	private Text summaryText;
	private Text descriptionText;
	private Text sampleLabelEditText;
	private Text sampleThicknessEditText;
	private Text sampleDescriptionEditText;

	private Text sampleLabelAddText;
	private Text samplePositionXAddText;
	private Text samplePositionYAddText;
	private Text sampleThicknessAddText;
	private Text sampleDescriptionAddText;

	private Button captureButton;
	private Button addItemButton;
	private Button samplePositionPickButton;
	private Button deleteSampleButton;
	private Button cancelUpdateButton;
	private Button saveChangesButton;
	private Button registerButton;

	private Combo holderCombo;
	private Combo visitCombo;

	String[] titles = {"ID", "Label", "Position", "Thickness", "Description"};

	private EventHandler pickPositionHandler = event -> {
    	if (samplePositionChangeEnabled) {
			ImmutablePair<?, ?> point = (ImmutablePair<?, ?>) event.getProperty(IEventBroker.DATA);
			if (!(point.left instanceof Double) || !(point.right instanceof Double)) {
				logger.error("Handling Pick Position event failed because of a type error. ImmutablePair<Double, Double> expected");
				return;
			}

			samplePositionXAddText.setText(point.getLeft().toString());
			samplePositionYAddText.setText(point.getRight().toString());
			samplePositionChangeEnabled = false;
			samplePositionPickButton.setSelection(false);

    	}
    };

	private EventHandler updatePositionAnnotationHandler = event -> {
		String[] sampleLocation = (String[]) event.getProperty(IEventBroker.DATA);

    	StringTokenizer sampleLocationTokenizer = new StringTokenizer(sampleLocation[0], ": ");
    	String sampleId = sampleLocationTokenizer.nextToken();
    	double sampleX = Double.parseDouble(sampleLocation[1]);
    	double sampleY = Double.parseDouble(sampleLocation[2]);

    	for (TableRow tableRow: DataModelProvider.getInstance().getItems()) {
    		if (tableRow.getData(0).equals(sampleId)) {
    			tableRow.editData(2, sampleX + ", " + sampleY);
    			break;
    		}
    	}
    	viewer.refresh();
    };

    private EventHandler returnCalibratedAxesHandler = event -> {
		ImmutablePair<?, ?> calibratedAxes = (ImmutablePair<?, ?>) event.getProperty(IEventBroker.DATA);
		if (!(calibratedAxes.left instanceof double[]) || !(calibratedAxes.right instanceof double[])) {
			logger.error("Handling Pick Position event failed because of a type error. ImmutablePair<double[], double[]> expected");
			return;
		}

		xCalibratedAxis = (double[]) calibratedAxes.getLeft();
		yCalibratedAxis = (double[]) calibratedAxes.getRight();
    };

	@Inject
	public PlateManagementView() {
		logger.trace("Constructor called");
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		this.parent = parent;
		ScrolledComposite scrollComp = new ScrolledComposite(this.parent, SWT.V_SCROLL);

		this.child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(child);

		captureButton = addButton(child, "Capture Snapshot", spanAndHint(4, 150, 75), true);
		captureButton.addSelectionListener(getCaptureButtonAdapter());

		addPlateDetails();
		addSampleManagement();

		registerButton = addButton(child, "Register", span(4), false);
		registerButton.addSelectionListener(getRegisterAdapter());

		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);

		// Expand both horizontally and vertically
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		logger.trace("Finished building composite");
	}

	private void addPlateDetails() {
		addLabel(child, "Visit:*", span(1).align(SWT.FILL, SWT.FILL));
		addVisitCombo();

		addLabel(child, "Summary:*", span(1).align(SWT.FILL, SWT.FILL));
		summaryText = addText(child, span(3).align(SWT.FILL, SWT.FILL).grab(true, false), true);

		addLabel(child, "Holder:*", span(1).align(SWT.FILL, SWT.FILL));
		holderCombo = new Combo(child, SWT.READ_ONLY);
		for (String serialNumber: Finder.listLocalFindablesOfType(HolderSerialNumberList.class).get(0).getHolderSerialNumberList()) {
			holderCombo.add(serialNumber);
		}
		GridDataFactory.swtDefaults().span(3, 1).applyTo(holderCombo);

		addLabel(child, "Description:", span(1).align(SWT.FILL, SWT.FILL));

		descriptionText = addMultilineText(child, 3, 3);
	}

	private void addVisitCombo() {
		visitCombo = new Combo(child, SWT.READ_ONLY);

		String[] visitsKeys = queryVisits().keySet().toArray(new String[0]);
		Arrays.sort(visitsKeys, (String s1, String s2) -> {
			Integer s1Number = Integer.valueOf(StringUtils.split(visits.get(s1).getLeft(), "-")[0].split("\\P{N}+")[1]);
			Integer s2Number = Integer.valueOf(StringUtils.split(visits.get(s2).getLeft(), "-")[0].split("\\P{N}+")[1]);

			Integer s1Visit = Integer.valueOf(StringUtils.split(visits.get(s1).getLeft(), "-")[1]);
			Integer s2Visit = Integer.valueOf(StringUtils.split(visits.get(s2).getLeft(), "-")[1]);

			if (s1Number.equals(s2Number)) {
				return Integer.compare(s1Visit, s2Visit);
			}

			return Integer.compare(s1Number, s2Number);
		});
		visitCombo.setItems(visitsKeys);
		visitCombo.add(PathscanConfigConstants.TEST_VISIT);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(visitCombo);
	}

	private Map<String, ImmutablePair<String, Integer>> queryVisits() {
		// parse SpyCat data
		visits = new HashMap<>();

		IspybSpyCatApi spyCatApi = SpyCatApiConnector.getIspybSpyCatApi();
		List<Session> sessions = spyCatApi.retrieveCurrentSessions(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME), 26280000);

		Iterator<Session> it = sessions.iterator();
		while (it.hasNext()) {
			String session = it.next().toString();
			String[] sessionParams = StringUtils.split(StringUtils.split(session, "[")[1], ",");
			String proposal = StringUtils.split(sessionParams[0], "=")[1];
			String startDate = StringUtils.split(sessionParams[1], "=")[1];
			// The end date can be obtained using: StringUtils.split(sessionParams[2], "=")[1];

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.s", Locale.ENGLISH);
			LocalDate sessionDate = LocalDate.parse(startDate, formatter);
			// The session time can be obtained using: LocalTime.parse(startDate, formatter);

			if (sessionDate.isBefore(LocalDate.now())) {
				continue;
			}

			String proposalCode = proposal.split("\\P{Alpha}+")[0];
			String proposalNumber = StringUtils.split(proposal, "-")[0].split("\\P{N}+")[1];
			String visitNumber = StringUtils.split(proposal, "-")[1];

			List<SessionPerson> persons = spyCatApi.retrievePersonsForSession(proposalCode,
					Integer.valueOf(proposalNumber), Integer.valueOf(visitNumber));

			String chosenPerson = "";
			for (SessionPerson person: persons) {
				if (person.getRole().equals("Principal Investigator")) {
					chosenPerson = "PI " + person.getFamilyName() + " ";
					break;
				}

			}
			visits.put(chosenPerson + proposal, new ImmutablePair<>(proposal, sessionDate.getYear()));
		}

		return visits;
	}

	private void addSampleManagement() {
		sampleManagementGroup = new Group(child, SWT.SHADOW_NONE);
		sampleManagementGroup.setText("Sample Management");
		GridData dataSampleManagementGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
		dataSampleManagementGroup.heightHint = 500;
		dataSampleManagementGroup.horizontalSpan = 4;
		sampleManagementGroup.setLayout(new GridLayout());
		sampleManagementGroup.setLayoutData(dataSampleManagementGroup);
		sampleManagementGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		viewer = new TableViewer(sampleManagementGroup, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
	    for (String title : titles) {
	    	TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
	    	tableViewerColumn.setLabelProvider(new TrimLabelProvider());
	    	tableViewerColumn.getColumn().setText(title);
		}
	    viewer.setContentProvider(ArrayContentProvider.getInstance());
	    viewer.setInput(DataModelProvider.getInstance().getItems());

		// Table
	    table = viewer.getTable();
	    table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData dataTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		dataTable.heightHint = 200;
		table.setLayoutData(dataTable);

		for (int i = 0; i < titles.length; ++i) {
			table.getColumn(i).pack();
		}

		tabFolder = new TabFolder(sampleManagementGroup, SWT.BORDER);
		tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4,1).applyTo(tabFolder);

		addSampleTab = new TabItem(tabFolder, SWT.NONE);
		addSampleTab.setText("Add Sample");
		addSampleTab.setControl(createAddSampleGroup(tabFolder));

		editSampleTab = new TabItem(tabFolder, SWT.NONE);
		editSampleTab.setText("Edit Sample");
		editSampleTab.setControl(createEditSampleGroup(tabFolder));

		table.addListener (SWT.Selection, event -> {
			enableSampleEditing();
			initializeSampleEditing();
			tabFolder.setSelection(editSampleTab);
		});
	}

	private Group createAddSampleGroup(Composite parent) {
		// Sample Addition
		sampleAdditionGroup = new Group(parent, SWT.SHADOW_NONE);
		sampleAdditionGroup.setLayout(GridLayoutFactory.swtDefaults().numColumns(6).create());
		sampleAdditionGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4,1).applyTo(sampleAdditionGroup);

		// Show potential ID
		sampleIdAddLabel = addLabel(sampleAdditionGroup,
				"A sample with ID " + currentId + " will be added",
				span(6).align(SWT.FILL, SWT.FILL).grab(true, false));

		// Add Label
		addLabel(sampleAdditionGroup, "Label:*", span(1).align(SWT.FILL, SWT.FILL));
		sampleLabelAddText = addText(sampleAdditionGroup, span(5).align(SWT.FILL, SWT.FILL).grab(true, false), true);

		// Add Position
		addLabel(sampleAdditionGroup, "Position:*", span(1).align(SWT.FILL, SWT.FILL));
		samplePositionXAddText = addText(sampleAdditionGroup, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);
		samplePositionYAddText = addText(sampleAdditionGroup, span(1).align(SWT.FILL, SWT.FILL).grab(true, false), false);

		samplePositionPickButton = new Button(sampleAdditionGroup, SWT.TOGGLE);
		samplePositionPickButton.setText("Pick");
		samplePositionPickButton.setEnabled(false);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(samplePositionPickButton);

		samplePositionPickButton.addSelectionListener(getPositionPickAdapter());

		broker.subscribe(SamplePlateConstants.TOPIC_PICK_POSITION, pickPositionHandler);
		broker.subscribe(SamplePlateConstants.TOPIC_UPDATE_POSITION_ANNOTATION, updatePositionAnnotationHandler);
		broker.subscribe(SamplePlateConstants.TOPIC_RETURN_CALIBRATED_AXES, returnCalibratedAxesHandler);

		// Add Thickness
		addLabel(sampleAdditionGroup, "Thickness:", span(1).align(SWT.FILL, SWT.FILL));
		sampleThicknessAddText = addText(sampleAdditionGroup, span(4).align(SWT.FILL, SWT.FILL).grab(true, false), true);
		addLabel(sampleAdditionGroup, "mm", span(1).align(SWT.FILL, SWT.FILL));

		sampleThicknessAddText.addVerifyListener(this::verifySampleThicknessAddText);

		// Add Description
		addLabel(sampleAdditionGroup, "Description:", span(1).align(SWT.FILL, SWT.FILL));
		sampleDescriptionAddText = addMultilineText(sampleAdditionGroup, 5, 3);

		// Add Sample
		addItemButton = addButton(sampleAdditionGroup, "Add Sample", span(6), false);
		addItemButton.addSelectionListener(getAddItemAdapter());

		return sampleAdditionGroup;
	}

	private Group createEditSampleGroup(Composite parent) {
		// Sample Editing
		sampleEditingGroup = new Group(parent, SWT.SHADOW_NONE);
		sampleEditingGroup.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		sampleEditingGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4,1).applyTo(sampleEditingGroup);

		// Show selected ID
		sampleIdEditLabel = addLabel(sampleEditingGroup, "No sample selected", span(4).align(SWT.FILL, SWT.FILL).grab(true, false));

		// Edit Label
		addLabel(sampleEditingGroup, "Label:*", span(1).align(SWT.FILL, SWT.FILL));
		sampleLabelEditText = addText(sampleEditingGroup, span(3).align(SWT.FILL, SWT.FILL).grab(true, false), true);

		// Edit Thickness
		addLabel(sampleEditingGroup, "Thickness:*", span(1).align(SWT.FILL, SWT.FILL));
		sampleThicknessEditText = addText(sampleEditingGroup, span(2).align(SWT.FILL, SWT.FILL).grab(true, false), true);
		addLabel(sampleEditingGroup, "mm", span(1).align(SWT.FILL, SWT.FILL));

		sampleThicknessEditText.addVerifyListener(this::verifySampleThicknessEditText);

		// Edit Description
		addLabel(sampleEditingGroup, "Description:", span(1).align(SWT.FILL, SWT.FILL));
		sampleDescriptionEditText = addMultilineText(sampleEditingGroup, 3, 3);

		// Delete, Cancel, Save
		deleteSampleButton = addButton(sampleEditingGroup, "Delete Sample", span(1), true);
		cancelUpdateButton = addButton(sampleEditingGroup, "Cancel Update", span(1), true);
		saveChangesButton = addButton(sampleEditingGroup, "Save Changes", span(1), true);

		disableSampleEditing();

		deleteSampleButton.addSelectionListener(getDeleteSampleAdapter());
		cancelUpdateButton.addSelectionListener(getCancelUpdateAdapter());
		saveChangesButton.addSelectionListener(getSaveChangesAdapter());

		return sampleEditingGroup;
	}

	private void clearPlateManagement() {
		tabFolder.setSelection(addSampleTab);

		broker.post(SamplePlateConstants.TOPIC_CLEAR_SNAPSHOT, null);

		visitCombo.deselectAll();
		summaryText.setText("");
		holderCombo.deselectAll();
		descriptionText.setText("");

		currentId = 0;
		clearSampleAddition();
		disableSampleEditing();

		samplePositionPickButton.setEnabled(false);
		addItemButton.setEnabled(false);
		registerButton.setEnabled(false);

		DataModelProvider.getInstance().getItems().clear();
		viewer.refresh();
	}


	private void clearSampleAddition() {
		sampleIdAddLabel.setText("A sample with the ID " + currentId + " will be added");
		sampleLabelAddText.setText("");
		samplePositionXAddText.setText("");
		samplePositionYAddText.setText("");
		sampleThicknessAddText.setText("");
		sampleDescriptionAddText.setText("");

		samplePositionPickButton.setSelection(false);
		samplePositionChangeEnabled = false;
	}

	private void disableSampleEditing() {
		sampleLabelEditText.setEnabled(false);
		sampleThicknessEditText.setEnabled(false);
		sampleDescriptionEditText.setEnabled(false);

		sampleIdEditLabel.setText("No sample selected");
		sampleLabelEditText.setText("");
		sampleThicknessEditText.setText("");
		sampleDescriptionEditText.setText("");

		deleteSampleButton.setEnabled(false);
		cancelUpdateButton.setEnabled(false);
		saveChangesButton.setEnabled(false);
	}

	private void enableSampleEditing() {
		sampleLabelEditText.setEnabled(true);
		sampleThicknessEditText.setEnabled(true);
		sampleDescriptionEditText.setEnabled(true);

		deleteSampleButton.setEnabled(true);
		cancelUpdateButton.setEnabled(true);
		saveChangesButton.setEnabled(true);
	}

	private void initializeSampleEditing() {
		TableItem sampleToEdit = table.getSelection()[0];
		TableRow rowToEdit = null;
		for (TableRow tableRow: DataModelProvider.getInstance().getItems()) {
			if (tableRow.getData(0).equals(sampleToEdit.getText(0))) {
				rowToEdit = tableRow;
			}
		}

		if (rowToEdit != null) {
			sampleIdEditLabel.setText("Currently editing the sample with ID " + sampleToEdit.getText(0));
			sampleLabelEditText.setText(rowToEdit.getData(1));
			sampleThicknessEditText.setText(rowToEdit.getData(3));
			sampleDescriptionEditText.setText(rowToEdit.getData(4));
		}
	}

	private static class DataModelProvider {
		private static DataModelProvider instance;
		private List<TableRow> items;

		public static DataModelProvider getInstance() {
			if (instance == null) {
				instance = new DataModelProvider();
			}

			return instance;
		}

		private DataModelProvider() {
			items = new ArrayList<>();
		}

		public void addItem(TableRow item) {
			items.add(item);
		}

		public List<TableRow> getItems() {
			return items;
		}
	}

	private static class TableRow {
		private String[] data;

		public TableRow(String[] input) {
			data = input;
		}

		public String getData(int index) {
			if (index < 0 || index >= data.length) {
				throw new IllegalArgumentException("Invalid index: " + index + ". Minimum: 0, Maximum: " + data.length);
			}
			return data[index];
		}

		public void editData(String[] newData) {
			if (newData.length != data.length) {
				throw new IllegalArgumentException("Invalid array size: should be" + data.length);
			}
			data = newData;
		}

		public void editData(int index, String newData) {
			if (index < 0 || index >= data.length) {
				throw new IllegalArgumentException("Invalid index: " + index + ". Minimum: 0, Maximum: " + data.length);
			}
			data[index] = newData;
		}
	}

	private static class TrimLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (element instanceof TableRow) {
				int columnIndex = cell.getColumnIndex();
				TableRow row = (TableRow) cell.getElement();
				String text = row.getData(columnIndex);

				cell.setText(text);
				if (columnIndex == 4) {
					final int maxTextLength = 80;
					if (text.length() > maxTextLength) {
						cell.setText(text.substring(0, maxTextLength) + " [...]");
					}
				} else if (columnIndex == 2) {
					StringTokenizer sampleLocationTokenizer = new StringTokenizer(text, ", ");
		        	double sampleX = Double.parseDouble(sampleLocationTokenizer.nextToken());
		        	double sampleY = Double.parseDouble(sampleLocationTokenizer.nextToken());
		        	String trimmedLocation = String.format("%.3f, %.3f", sampleX, sampleY);
		        	cell.setText(trimmedLocation);
				}
			}
		}
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);

		layout.applyTo(label);

		return label;
	}

	private Button addButton(Composite parent, String buttonText, GridDataFactory layout, boolean buttonEnabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(buttonText);
		button.setEnabled(buttonEnabled);

		layout.applyTo(button);

		return button;
	}

	private Text addText(Composite parent, GridDataFactory layout, boolean textEnabled) {
		Text text = new Text(parent, SWT.BORDER);
		text.setEnabled(textEnabled);

		layout.applyTo(text);

		return text;
	}

	private Text addMultilineText(Composite parent, int span, int lines) {
		Text text = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.heightHint = lines * text.getLineHeight();
		gridData.horizontalSpan = span;
		text.setLayoutData(gridData);

		return text;
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.swtDefaults().span(span, 1);
	}

	private GridDataFactory spanAndHint(int span, int hintX, int hintY) {
		return GridDataFactory.swtDefaults().span(span, 1).hint(hintX, hintY);
	}

	public void verifySampleThicknessAddText(VerifyEvent e) {
		// only verify users' input, not programmatically added input
		if (sampleThicknessAddText.isFocusControl()) {
			e.doit = (sampleThicknessAddText.getText() + e.text).matches(DOUBLE_REGEX);
		}
	}

	public void verifySampleThicknessEditText(VerifyEvent e) {
		// only verify users' input, not programmatically added input
		if (sampleThicknessEditText.isFocusControl()) {
			e.doit = (sampleThicknessEditText.getText() + e.text).matches(DOUBLE_REGEX);
		}
	}

	private SnapshotView getSnapshotView() {
		try {
			return (SnapshotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SnapshotView.ID, null, 3);
		} catch (Exception e) {
			logger.error("Could not get snapshot", e);
			MessageDialog.openError(getShell(), "Error", "Error saving the snapshot");
			return null;
		}
	}

	private boolean saveSnapshot(SnapshotView snapshotView, String path) {
		try {
			snapshotView.getPlottingSystem().savePlotting(path, "PNG/JPEG File");
		} catch (Exception e) {
			logger.error("Could not save snapshot", e);
			MessageDialog.openError(getShell(), "Error", "Error saving the snapshot");
			return false;
		}

		return true;
	}

	private boolean saveJson(String json, String path) {
		try (FileWriter writer = new FileWriter(path)) {
			writer.write(json);
			MessageDialog.openInformation(getShell(), "Register", "Plate registered successfully.");
		} catch (IOException e) {
			logger.error("Serialization failed", e);
			MessageDialog.openError(getShell(), "Failed", "Registration has failed.");
			return false;
		}

		return true;
	}

	private String buildJson(SnapshotView snapshotView) {
		List<RegisteredSample> sampleList = new ArrayList<>();
		for (TableRow item: DataModelProvider.getInstance().getItems()) {

			Double positionX = Double.valueOf(StringUtils.split(item.getData(2), ", ")[0]);
			Double positionY = Double.valueOf(StringUtils.split(item.getData(2), ", ")[1]);

			sampleList.add(new RegisteredSample(
					item.getData(0),
					item.getData(1),
					positionX,
					positionY,
					Double.valueOf(item.getData(3)),
					item.getData(4)
				));
		}

		RGBByteDataset dataset = (RGBByteDataset) snapshotView.getPlottingSystem().getTrace("Snapshot").getData();

		String[] visitTokens = StringUtils.split(visitCombo.getText(), " ");
		RegisteredPlate registeredPlate = new RegisteredPlate(
					dataset.getShape(),
					dataset.getData(),
					visitTokens[visitTokens.length - 1],
					summaryText.getText(),
					holderCombo.getText(),
					descriptionText.getText(),
					sampleList,
					xCalibratedAxis,
					yCalibratedAxis
				);

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();

		Gson gson = gsonBuilder.create();
		return gson.toJson(registeredPlate);
	}

	private boolean isRegistrationValid(Map<String, Boolean> checks) {
		boolean registrationValid = true;
		List<String> errors = new ArrayList<>();

		for (Entry<String, Boolean> check: checks.entrySet()) {
			if (Boolean.TRUE.equals(check.getValue())) {
				registrationValid = false;
				errors.add(" - " + check.getKey() + "\n");
			}
		}

		if (!registrationValid) {
			StringBuilder errorMsgBuilder = new StringBuilder();
			errorMsgBuilder.append("Please complete the following field(s):\n");
			for (String error: errors) {
				errorMsgBuilder.append(error);
			}
			MessageDialog.openWarning(getShell(), "Mandatory field(s) empty", errorMsgBuilder.toString());
			return false;
		}

		return true;
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}

	private SelectionAdapter getRegisterAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Map<String, Boolean> checks = new HashMap<>();
				checks.put("Visit", visitCombo.getText().equals(""));
				checks.put("Summary", summaryText.getText().equals(""));
				checks.put("Holder", holderCombo.getText().equals(""));

				if (!MessageDialog.openQuestion(getShell(), "Register", "Are you sure you want to register this plate?")
					|| !isRegistrationValid(checks)) {
					return;
				}

				SnapshotView snapshotView = getSnapshotView();
				if (snapshotView == null) {
					return;
				}

				String json = buildJson(snapshotView);
				String pathWithoutExtension;
				if (visitCombo.getText().equals(PathscanConfigConstants.TEST_VISIT)) {
					pathWithoutExtension = "/scratch/workspace/gda3/gda-master-default/gda_data_non_live/2022/0-0";
				} else {
					Map<String, String> overrides = new HashMap<>();
					overrides.put("visit",
						Boolean.TRUE.equals(Boolean.valueOf(LocalProperties.get(LocalProperties.GDA_DUMMY_MODE_ENABLED))) ? "0-0" : (visits.get(visitCombo.getText()).getLeft())
					);
					overrides.put("year", visits.get(visitCombo.getText()).getRight().toString());
					pathWithoutExtension = InterfaceProvider.getPathConstructor().createFromProperty(LocalProperties.GDA_VISIT_DIR, overrides);
				}
				pathWithoutExtension += "/xml/plates/";
				new File(pathWithoutExtension).mkdir();
				Integer plateNumber = new File(pathWithoutExtension).listFiles().length;
				pathWithoutExtension += "plate" + plateNumber + "/plate" + plateNumber;

				new File(pathWithoutExtension + ".json").getParentFile().mkdirs();

				if (!saveSnapshot(snapshotView, pathWithoutExtension + ".png") || !saveJson(json, pathWithoutExtension + ".json")) {
					return;
				}

				clearPlateManagement();
      		}
		};
	}

	private SelectionAdapter getAddItemAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Map<String, Boolean> checks = new HashMap<>();
				checks.put("Label", sampleLabelAddText.getText().equals(""));
				checks.put("Position", samplePositionXAddText.getText().equals("")
							|| samplePositionYAddText.getText().equals(""));

				if (!isRegistrationValid(checks)) {
					return;
				}

				if (sampleThicknessAddText.getText().equals("")) {
					sampleThicknessAddText.setText("0");
				}

				DataModelProvider.getInstance().addItem(new TableRow(
						new String[] {
								String.valueOf(currentId++),
								sampleLabelAddText.getText(),
								samplePositionXAddText.getText() + ", " + samplePositionYAddText.getText(),
								String.valueOf(Double.parseDouble(sampleThicknessAddText.getText())),
								sampleDescriptionAddText.getText()
						}));
				viewer.refresh();

				for (int i = 0; i < titles.length; ++i) {
					table.getColumn(i).pack();
				}

				broker.post(SamplePlateConstants.TOPIC_ADD_ANNOTATION,
					new String[] {
							String.valueOf(currentId - 1),
							sampleLabelAddText.getText(),
							samplePositionXAddText.getText(),
							samplePositionYAddText.getText(),
						}
					);

				clearSampleAddition();

      		}
		};
	}

	private SelectionAdapter getDeleteSampleAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TableItem sampleToDelete = table.getSelection()[0];
				boolean confirmDelete = MessageDialog.openQuestion(
						getShell(),
						"Deleting sample " + sampleToDelete.getText(),
						"Are you sure you want to delete the sample with ID " + sampleToDelete.getText() + "?");

				if (!confirmDelete) {
					return;
				}

				String annotationToDeleteName = sampleToDelete.getText(0) + ": " + sampleToDelete.getText(1);

				TableRow rowToDelete = null;
				for (TableRow tableRow: DataModelProvider.getInstance().getItems()) {
					if (tableRow.getData(0).equals(sampleToDelete.getText(0))) {
						rowToDelete = tableRow;
						break;
					}
				}
				if (rowToDelete != null) {
					DataModelProvider.getInstance().getItems().remove(rowToDelete);
				}
				viewer.refresh();

				table.deselectAll();
				disableSampleEditing();

				for (int i = 0; i < titles.length; ++i) {
					table.getColumn(i).pack();
				}

				broker.post(SamplePlateConstants.TOPIC_DELETE_ANNOTATION, annotationToDeleteName);

				tabFolder.setSelection(addSampleTab);
      		}
		};
	}

	private SelectionAdapter getCancelUpdateAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				table.deselectAll();
				disableSampleEditing();

				tabFolder.setSelection(addSampleTab);
			}
		};
	}

	private SelectionAdapter getSaveChangesAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Map<String, Boolean> checks = new HashMap<>();
				checks.put("Label", sampleLabelEditText.getText().equals(""));

				if (!isRegistrationValid(checks)) {
					return;
				}

				TableItem sampleToEdit = table.getSelection()[0];
				if (sampleThicknessEditText.getText().equals("")) {
					sampleThicknessEditText.setText("0");
				}

				for (TableRow tableRow: DataModelProvider.getInstance().getItems()) {
					if (tableRow.getData(0).equals(sampleToEdit.getText(0))) {
						String annotationToUpdateOldName = tableRow.getData(0) + ": " + tableRow.getData(1);
						String annotationToUpdateNewName = tableRow.getData(0) + ": " + sampleLabelEditText.getText();
						broker.post(SamplePlateConstants.TOPIC_UPDATE_LABEL_ANNOTATION, new String[] {
							annotationToUpdateOldName,
							annotationToUpdateNewName
						});

						tableRow.editData(
								new String[] {
										sampleToEdit.getText(0),
										sampleLabelEditText.getText(),
										tableRow.getData(2),
										String.valueOf(Double.parseDouble(sampleThicknessEditText.getText())),
										sampleDescriptionEditText.getText()
								});

						break;
					}
				}
				viewer.refresh();

				for (int i = 0; i < titles.length; ++i) {
					table.getColumn(i).pack();
				}

				table.deselectAll();
				disableSampleEditing();

				tabFolder.setSelection(addSampleTab);
      		}
		};

	}

	private SelectionAdapter getCaptureButtonAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				broker.post(SamplePlateConstants.TOPIC_TAKE_SNAPSHOT, null);
				addItemButton.setEnabled(true);
				registerButton.setEnabled(true);
				samplePositionPickButton.setEnabled(true);
      		}
		};
	}

	private SelectionAdapter getPositionPickAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				samplePositionChangeEnabled = !samplePositionChangeEnabled;
			}
		};
	}
}