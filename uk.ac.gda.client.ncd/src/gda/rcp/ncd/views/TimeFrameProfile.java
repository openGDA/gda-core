/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.timer.FrameSet;
import gda.rcp.ncd.Activator;
import gda.rcp.ncd.NcdController;
import gda.rcp.ncd.ui.preferences.TfgPreferencePage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.beans.FrameSetParameters;
import uk.ac.gda.server.ncd.beans.InputTriggerParameters;
import uk.ac.gda.server.ncd.beans.TimeProfileParameters;

public class TimeFrameProfile extends TabItem {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameProfile.class);

	private static final String[] columnNames = { "Group", "Frames", "Wait Time", "Wait Units", "Run Time",
			"Run Units", "Wait Pause", "Run Pause", "Wait Pulses", "Run Pulses" };
	public static final String[] startLabelList = { "Software", "\u2191 BM Trigger", "\u2191 ADC chan 0", "\u2191 ADC chan 1",
			"\u2191 ADC chan 2", "\u2191 ADC chan 3", "\u2191 ADC chan 4", "\u2191 ADC chan 5", "\u2191 TTL trig 0",
			"\u2191 TTL trig 1", "\u2191 TTL trig 2", "\u2191 TTL trig 3", "\u2191 LVDS Lemo ", "\u2191 TFG cable 1",
			"\u2191 TFG cable 2", "\u2191 TFG cable 3", "\u2191 Var thrshld", "\u2193 BM Trigger",
			"\u2193 ADC chan 0", "\u2193 ADC chan 1", "\u2193 ADC chan 2", "\u2193 ADC chan 3", "\u2193 ADC chan 4",
			"\u2193 ADC chan 5", "\u2193 TTL trig 0", "\u2193 TTL trig 1", "\u2193 TTL trig 2", "\u2193 TTL trig 3",
			"\u2193 LVDS Lemo", "\u2193 TFG cable 1", "\u2193 TFG cable 2", "\u2193 TFG cable 3",
			"\u2193 Var thrshld" };
	public static final String[] displayPause = { "Software", "No Pause", "\u2191 BM Trigger", "\u2191 ADC chan 0",
			"\u2191 ADC chan 1", "\u2191 ADC chan 2", "\u2191 ADC chan 3", "\u2191 ADC chan 4", "\u2191 ADC chan 5",
			"\u2191 TTL trig 0", "\u2191 TTL trig 1", "\u2191 TTL trig 2", "\u2191 TTL trig 3", "\u2191 LVDS Lemo ",
			"\u2191 TFG cable 1", "\u2191 TFG cable 2", "\u2191 TFG cable 3", "\u2191 Var thrshld",
			"\u2193 BM Trigger", "\u2193 ADC chan 0", "\u2193 ADC chan 1", "\u2193 ADC chan 2", "\u2193 ADC chan 3",
			"\u2193 ADC chan 4", "\u2193 ADC chan 5", "\u2193 TTL trig 0", "\u2193 TTL trig 1", "\u2193 TTL trig 2",
			"\u2193 TTL trig 3", "\u2193 LVDS Lemo", "\u2193 TFG cable 1", "\u2193 TFG cable 2", "\u2193 TFG cable 3",
			"\u2193 Var thrshld" };
	public static final String[] outputTriggerLabels = { "Output1", "Output2", "Output3", "Output4", "Output5", "Output6",
			"Output7", "Output8", };
	
	private static final String rising = "\u2191";
	private static final String falling = "\u2193";
	
	private Table table;
	private TableViewer tableViewer;

	private Label label1;
	private Label label2;
	private Label label3;
	private Label label4;
	private Label label5;
	private Text text1;
	private Text text2;
	private Text text3;
	private Text text4;
	private Button button1;
	private Composite composite3;
	private Composite composite5;
	private Composite composite6;
	private Combo combo1;

	private List<FrameSetParameters> timeFrameGroups;
	private TimeProfileParameters tpp;
	private TabFolder tabFolder;
	private static Timer timer = null;
	private InputTriggerTable inputTriggerTable;
	
	@Override
	protected void checkSubclass() {
	}

	public TimeFrameProfile(TabFolder parent, int style, TimeProfileParameters tpp_in) {
		super(parent, style);
		this.tabFolder = parent;
		this.tpp = tpp_in;
		composite3 = new Composite(parent, SWT.NONE);
		GridLayout composite3Layout = new GridLayout();
		composite3Layout.makeColumnsEqualWidth = true;
		composite3.setLayout(composite3Layout);
		setControl(composite3);
		// Create the table
		createTable(composite3);
		composite5 = new Composite(composite3, SWT.NONE);
		GridLayout composite5Layout = new GridLayout(9, false);

		composite5.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH));
		composite5.setLayout(composite5Layout);
		{
			label2 = new Label(composite5, SWT.NONE);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.END;
			label2.setLayoutData(gridData);
			label2.setText("Total Frames");
		}
		{
			text1 = new Text(composite5, SWT.NONE);
			text1.setText("1");
			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = 100;
			text1.setLayoutData(gridData);
			text1.setEditable(false);
		}
		{
			label4 = new Label(composite5, SWT.NONE);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.END;
			gridData.horizontalIndent = 10;
			label4.setLayoutData(gridData);
			label4.setText("Total Time");
		}
		{
			text4 = new Text(composite5, SWT.NONE);
			text4.setText("");
			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = 100;
			text4.setLayoutData(gridData);
			text4.setEditable(false);
		}
		{
			label1 = new Label(composite5, SWT.NONE);
			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
			gridData.horizontalAlignment = SWT.END;
			gridData.horizontalIndent = 10;
			label1.setLayoutData(gridData);
			label1.setText("No. of Cycles");
		}
		{
			text2 = new Text(composite5, SWT.NONE);
			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = 100;
			text2.setLayoutData(gridData);
			text2.setText(tpp.getCycles().toString());
			text2.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String newText = text2.getText();
					if (!"".equals(newText)) {
						tpp.setCycles(Integer.valueOf(newText));
					}
				}
			});
		}
		{
			label3 = new Label(composite5, SWT.NONE);
			GridData gridData = new GridData();
			gridData.horizontalIndent = 10;
			gridData.horizontalAlignment = SWT.END;
			gridData.horizontalSpan = 2;
			label3.setLayoutData(gridData);
			label3.setText("No of sequence repeats");
		}
		{
			text3 = new Text(composite5, SWT.NONE);
			GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = 100;
			text3.setLayoutData(gridData);
			text3.setText(tpp.getRepeat().toString());
			text3.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String newText = text3.getText();
					if (!"".equals(newText)) {
						tpp.setRepeat(Integer.valueOf(newText));
						displayTotalTime();
					}
				}
			});
		}
		{
			button1 = new Button(composite5, SWT.CHECK | SWT.LEFT);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.BEGINNING;
			gridData.horizontalSpan = 2;
			button1.setLayoutData(gridData);
			button1.setText("External Inhibit");
			button1.setSelection(tpp.getExtInhibit());
			button1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tpp.setExtInhibit(button1.getSelection());
				}
			});
		}
		{
			label5 = new Label(composite5, SWT.NONE);
			GridData gridData = new GridData();
			gridData.horizontalIndent = 10;
			gridData.horizontalAlignment = SWT.END;
			label5.setLayoutData(gridData);
			label5.setText("Start by");
		}
		{
			combo1 = new Combo(composite5, SWT.READ_ONLY);
			combo1.setItems(startLabelList);
			combo1.setText(getInternalLabel(tpp.getStartMethod()));
			combo1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = combo1.getSelectionIndex();
					String sm = getExternalName(startLabelList[index]);
					tpp.setStartMethod(sm);
				}
			});
		}
		

		composite6 = new Composite(composite3, SWT.NONE);
		GridLayout composite6Layout = new GridLayout(3, false);
		composite6.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH));
		composite6.setLayout(composite6Layout);

		ExpandableComposite advancedExpandableComposite2 = new ExpandableComposite(composite6, SWT.NONE);
		advancedExpandableComposite2.setText("Advanced input trigger details");
		advancedExpandableComposite2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		advancedExpandableComposite2.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				composite6.layout();
				tabFolder.getParent().pack();
				((ScrolledComposite)tabFolder.getParent().getParent()).setMinSize(tabFolder.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});
		Group advanced2 = new Group(advancedExpandableComposite2, SWT.V_SCROLL);
		advanced2.setLayout(new GridLayout(1, false));
		advancedExpandableComposite2.setClient(advanced2);

		inputTriggerTable = new InputTriggerTable(advanced2, tpp.getInputTriggerParameters());

		ExpandableComposite advancedExpandableComposite = new ExpandableComposite(composite6, SWT.NONE);
		advancedExpandableComposite.setText("Advanced output trigger details");
		advancedExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		advancedExpandableComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				composite6.layout();
				tabFolder.getParent().pack();
				((ScrolledComposite)tabFolder.getParent().getParent()).setMinSize(tabFolder.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		Group advanced = new Group(advancedExpandableComposite, SWT.V_SCROLL);
		advanced.setLayout(new GridLayout(3, false));
		advancedExpandableComposite.setClient(advanced);

		String[] inversionOptions = { "idle low", "idle high" };
		String[] driveOptions = { "full drive", "50 Ω terminated" };
		String outputTriggerInversion = tpp.getOutputTriggerInversion();
		String outputTriggerDrive = tpp.getOutputTriggerDrive();
		Label label1 = new Label(advanced, SWT.NONE);
		label1.setText("Trigger");
		Label label2 = new Label(advanced, SWT.NONE);
		label2.setText("Inversion");
		Label label3 = new Label(advanced, SWT.NONE);
		label3.setText("Drive");

		//Get output trigger labels from the preference page
		setOutputTriggerLabels();
		for (int i=0; i<8; i++) {
			Label usr = new Label(advanced, SWT.NONE);
			usr.setText(outputTriggerLabels[i]);

			final int nos = i;
			Combo combo1 = new Combo(advanced, SWT.READ_ONLY);
			combo1.setItems(inversionOptions);
			int index = Integer.parseInt("" + outputTriggerInversion.charAt(i));
			combo1.setText(inversionOptions[index]);
			combo1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String inversion = tpp.getOutputTriggerInversion();
					StringBuffer sb = new StringBuffer(inversion);
					int index = ((Combo) e.widget).getSelectionIndex();
					char ch = (index == 0) ? '0' : '1';
					sb.setCharAt(nos, ch);
					tpp.setOutputTriggerInversion(sb.toString());
				}
			});
			Combo combo2 = new Combo(advanced, SWT.READ_ONLY);
			combo2.setItems(driveOptions);
			index = Integer.parseInt("" + outputTriggerDrive.charAt(i));
			combo2.setText(driveOptions[index]);
			combo2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String drive = tpp.getOutputTriggerDrive();
					StringBuffer sb = new StringBuffer(drive);
					int index = ((Combo) e.widget).getSelectionIndex();
					char ch = (index == 0) ? '0' : '1';
					sb.setCharAt(nos, ch);
					tpp.setOutputTriggerDrive(sb.toString());
				}
			});
		}
		{
			Composite composite7 = new Composite(composite6, SWT.NONE);
			GridLayout composite7Layout = new GridLayout(1, false);
			composite7.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			composite7.setLayout(composite7Layout);

			Button configureButton = new Button(composite7, SWT.NONE);
			configureButton.setText("Configure");
			configureButton.setToolTipText("Configure the time frame generator");
			configureButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						configureHardware();
					} catch (Exception ne) {
						logger.error("Cannot open time frame parameters", ne);
					}
				}
			});
		}

		// Create and setup the TableViewer
		createTableViewer();
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<FrameSetParameters>) inputElement).toArray();
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				tabFolder.pack();
				tabFolder.getParent().pack();
			}
		});
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setUseHashlookup(true);

		// The input for the table viewer is the instance of
		timeFrameGroups = tpp.getFrameSetParameters();
		tableViewer.setInput(timeFrameGroups);
		displayTotalFrames();
		displayTotalTime();
	}

	/**
	 * Create the Table
	 */
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// Create columns
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columnNames[i]);
			column.setWidth(100);
		}
	}

	/**
	 * Create the TableViewer
	 */
	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(false);
		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];
		CellEditor editor;
		for (int i = 0; i < columnNames.length; i++) {
			if (i == 3 || i == 5) {
				editor = new ComboBoxCellEditor(table, FrameSetParameters.displayUnits);
			} else if (i == 6 || i == 7) {
				editor = new ComboBoxCellEditor(table, displayPause);
			} else if (i == 8 || i == 9) {
				editor = new MenuCellEditor(table, outputTriggerLabels);
			} else {
				editor = new TextCellEditor(table);
				((Text) editor.getControl()).setTextLimit(60);
			}
			editors[i] = editor;
		}
		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);
		// We are not allowing the table to be sorted
		tableViewer.setSorter(null);
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new CellModifier());
	}

	public void addFrameSet() {
		// find the currently selected group
		IStructuredSelection iss = (IStructuredSelection) tableViewer.getSelection();
		FrameSetParameters fp = (FrameSetParameters) iss.getFirstElement();
		int index = 0;
		if (fp != null) {
			for (FrameSetParameters fsp : timeFrameGroups) {
				index++;
				if (fsp.equals(fp))
					break;
			}
		} else {
			fp = timeFrameGroups.get(0);
		}

		FrameSetParameters newfp = new FrameSetParameters(fp);
		timeFrameGroups.add(index, newfp);
		// re-oder the group numbering
		int group = 1;
		for (FrameSetParameters fsp : timeFrameGroups) {
			fsp.setGroup(group++);
		}
		tpp.setFrameSetParameters(timeFrameGroups);
		tableViewer.setInput(timeFrameGroups);
		table.update();
		if (table.getItemCount() < 10) {
			tabFolder.pack();
			tabFolder.getParent().pack();
		}
		displayTotalFrames();
		displayTotalTime();
	}

	public void deleteFrameSet() {
		// Delete the currently selected FrameSet
		IStructuredSelection iss = (IStructuredSelection) tableViewer.getSelection();
		FrameSetParameters fp = (FrameSetParameters) iss.getFirstElement();
		timeFrameGroups.remove(fp);
		int group = 1;
		for (FrameSetParameters fsp : timeFrameGroups) {
			fsp.setGroup(group++);
		}
		tpp.setFrameSetParameters(timeFrameGroups);
		tableViewer.setInput(timeFrameGroups);
		tabFolder.pack();
		tabFolder.getParent().pack();
		displayTotalFrames();
		displayTotalTime();
	}

	/**
	 * @return List containing column names
	 */
	public java.util.List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}

	/**
	 * This class implements an ICellModifier An ICellModifier is called when the user modifies a cell in the
	 * tableViewer
	 */
	public class CellModifier implements ICellModifier {

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean canModify(Object element, String property) {
			int columnIndex = getColumnNames().indexOf(property);
			return (columnIndex == 0) ? false : true;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object getValue(Object element, String property) {

			// Find the index of the column
			int columnIndex = getColumnNames().indexOf(property);

			Object result = null;
			FrameSetParameters frameSet = (FrameSetParameters) element;
			int index;
			switch (columnIndex) {
			case 1:
				result = String.valueOf(frameSet.getNframes());
				break;
			case 2:
				result = String.valueOf(frameSet.getNwait());
				break;
			case 3:
				index = 0;
				for (String s : FrameSetParameters.displayUnits) {
					if (frameSet.getWaitUnit().equals(s)) {
						result = index;
						break;
					}
					index++;
				}
				break;
			case 4:
				result = String.valueOf(frameSet.getNrun());
				break;
			case 5:
				index = 0;
				for (String s : FrameSetParameters.displayUnits) {
					if (frameSet.getRunUnit().equals(s)) {
						result = index;
						break;
					}
					index++;
				}
				break;
			case 6:
				index = 0;
				for (String s : displayPause) {
					if (getInternalLabel(frameSet.getWaitPause()).equals(s)) {
						result = index;
						break;
					}
					index++;
				}
				break;
			case 7:
				index = 0;
				for (String s : displayPause) {
					if (getInternalLabel(frameSet.getRunPause()).equals(s)) {
						result = index;
						break;
					}
					index++;
				}
				break;
			case 8:
				result = frameSet.getWaitPulse();
				break;
			case 9:
				result = frameSet.getRunPulse();
				break;
			default:
				result = "";
			}
			return result;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		@Override
		public void modify(Object element, String property, Object value) {
			int columnIndex = getColumnNames().indexOf(property);
			TableItem item = (TableItem) element;
			if (item != null) {
				FrameSetParameters frameSet = (FrameSetParameters) item.getData();

				switch (columnIndex) {
				case 0:
					break;
				case 1:
					try {
						frameSet.setNframes(Integer.parseInt((String) value));
					} catch (NumberFormatException nfe) {
						logger.error("Invalid integer number");
					}
					displayTotalFrames();
					displayTotalTime();
					break;
				case 2:
					try {
						frameSet.setNwait(Integer.parseInt((String) value));
					} catch (NumberFormatException nfe) {
						logger.error("Invalid integer number");
					}
					displayTotalTime();
					break;
				case 3:
					frameSet.setWaitUnit(FrameSetParameters.displayUnits[(Integer) value]);
					displayTotalTime();
					break;
				case 4:
					try {
						frameSet.setNrun(Integer.parseInt((String) value));
					} catch (NumberFormatException nfe) {
						logger.error("Invalid integer number");
					}
					displayTotalTime();
					break;
				case 5:
					frameSet.setRunUnit(FrameSetParameters.displayUnits[(Integer) value]);
					displayTotalTime();
					break;
				case 6:
					frameSet.setWaitPause(getExternalName(displayPause[(Integer) value]));
					break;
				case 7:
					frameSet.setRunPause(getExternalName(displayPause[(Integer) value]));
					break;
				case 8:
					frameSet.setWaitPulse((String) value);
					break;
				case 9:
					frameSet.setRunPulse((String) value);
					break;
				default:
				}
				tableViewer.refresh(true);
			}
		}
	}

	public class MyLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			FrameSetParameters frameSet = (FrameSetParameters) element;
			switch (columnIndex) {
			case 0:
				return String.valueOf(frameSet.getGroup());
			case 1:
				return String.valueOf(frameSet.getNframes());
			case 2:
				return String.valueOf(frameSet.getNwait());
			case 3:
				return String.valueOf(frameSet.getWaitUnit());
			case 4:
				return String.valueOf(frameSet.getNrun());
			case 5:
				return String.valueOf(frameSet.getRunUnit());
			case 6:
				return String.valueOf(getInternalLabel(frameSet.getWaitPause()));
			case 7:
				return String.valueOf(getInternalLabel(frameSet.getRunPause()));
			case 8:
				return String.valueOf(frameSet.getWaitPulse());
			case 9:
				return String.valueOf(frameSet.getRunPulse());
			default:
				return "";
			}
		}
	}

	public TimeProfileParameters getTimeProfileParameters() {
		StringBuffer debounce = new StringBuffer();
		StringBuffer threshold = new StringBuffer();
		for (InputTriggerParameters itp : inputTriggerTable.getInputTriggerParameters()) {
			debounce.append(itp.getDebounce().toString() + " ");
			threshold.append(itp.getThreshold().toString() + " ");
		}
		tpp.setInputTriggerDebounce(debounce.toString().trim());
		tpp.setInputTriggerThreshold(threshold.toString().trim());
		return tpp;
	}

	public void displayTotalFrames() {
		int frames = 0;
		for (FrameSetParameters fp : timeFrameGroups) {
			frames += fp.getNframes();
		}
		frames *= tpp.getRepeat();
		text1.setText(String.valueOf(frames));

		// NcdMemoryUsage nmu = null;
		// IWorkbench wb = PlatformUI.getWorkbench();
		// if (wb != null) {
		// IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
		// if (wbw != null) {
		// IWorkbenchPage iwp = wbw.getActivePage();
		// if (iwp != null) {
		// nmu = (NcdMemoryUsage) iwp.findView("gda.rcp.ncd.views.NcdMemoryUsage");
		// if (nmu != null) {
		// nmu.setTimeFrameCount(frames);
		// }
		// }
		// }
		// }
		// ExptDataModel.getInstance().setTotalFrames(frames);
	}

	public void displayTotalTime() {
		double total = 0;
		for (FrameSetParameters fp : timeFrameGroups) {
			total += (fp.getActualRunTime() + fp.getActualWaitTime()) * fp.getNframes() / 1000;
		}
		total *= tpp.getRepeat();
		StringBuilder sb = new StringBuilder("");
		int min = (int) (total / 60);
		if (min > 0) {
			sb.append(String.format("%d min ", min));
			total -= min * 60;
		}
		sb.append(String.format("%5.3f sec", total % 60));
		text4.setText(sb.toString());
	}

	public void configureHardware() throws DeviceException {
		if (timer == null) {
			timer = NcdController.getInstance().getTfg();
		}
		if (timer == null) {
			throw new DeviceException("No timer found!");
		}
		if (timer.getStatus() != gda.device.Timer.IDLE) {
			throw new DeviceException("Cannot configure while TFG running!");
		}

		List<Double> debounceValues = new ArrayList<Double>();
		List<Double> thresholdValues = new ArrayList<Double>();
		for (InputTriggerParameters itp : inputTriggerTable.getInputTriggerParameters()) {
			Double d = itp.getDebounce();
			debounceValues.add(d);
			Double t = itp.getThreshold();
			thresholdValues.add(t);
		}
		timer.setAttribute("Debounce", debounceValues);
		timer.setAttribute("Threshold", thresholdValues);

		timer.setAttribute("Inversion", tpp.getInversionValue());
		timer.setAttribute("Drive", tpp.getDriveValue());
		int index;
		for (index = 0; index < startLabelList.length; index++) {
			if (startLabelList[index].equals(getInternalLabel(tpp.getStartMethod())))
				break;
		}
		timer.setAttribute("Start-Method", index);
		timer.setAttribute("Ext-Inhibit", tpp.getExtInhibit());
		timer.setCycles(tpp.getCycles());
		timer.clearFrameSets();
		for (int i = 0; i < tpp.getRepeat(); i++) {
			for (FrameSetParameters fp : tpp.getFrameSetParameters()) {
				timer.addFrameSet(fp.getNframes().intValue(), fp.getActualWaitTime(), fp.getActualRunTime(),
						fp.getWaitPort(), fp.getRunPort(), getWaitPauseValue(getInternalLabel(fp.getWaitPause())),
						getRunPauseValue(getInternalLabel(fp.getRunPause())));
			}
		}
	timer.loadFrameSets();
	}

	private String getInternalLabel(String name) {
		String internalLabel;
		if (name.startsWith("Falling")) {
			internalLabel = falling + name.substring(7);
		} else if (name.startsWith("Rising")) {
			internalLabel = rising + name.substring(6);
		} else {
			internalLabel = name;
		}
		return internalLabel;
	}
	
	private static String getExternalName(String label) {
		String externalName;
		if (label.startsWith(falling)) {
			externalName = "Falling" + label.substring(1);
		} else if (label.startsWith(rising)) {
			externalName = "Rising" + label.substring(1);
		} else {
			externalName = label;
		}
		return externalName;
	}
			
	/**
	 * @return the numeric value of the wait pause bits.
	 */
	private int getWaitPauseValue(String waitPause) {
		int pause = -1;
		for (String item : TimeFrameProfile.displayPause) {
			if (item.equals(waitPause))
				break;
			pause++;
		}
		if (pause > 16)
			pause = ((pause - 16) | 0x20);
		return pause;
	}

	/**
	 * @return the numeric value of the run pause bits.
	 */
	private int getRunPauseValue(String runPause) {
		int pause = -1;
		for (String item : TimeFrameProfile.displayPause) {
			if (item.equals(runPause))
				break;
			pause++;
		}
		if (pause > 16)
			pause = ((pause - 16) | 0x20);
		return pause;
	}
	
	private void setOutputTriggerLabels() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		outputTriggerLabels[0] = store.getString(TfgPreferencePage.USR0);
		outputTriggerLabels[1] = store.getString(TfgPreferencePage.USR1);
		outputTriggerLabels[2] = store.getString(TfgPreferencePage.USR2);
		outputTriggerLabels[3] = store.getString(TfgPreferencePage.USR3);
		outputTriggerLabels[4] = store.getString(TfgPreferencePage.USR4);
		outputTriggerLabels[5] = store.getString(TfgPreferencePage.USR5);
		outputTriggerLabels[6] = store.getString(TfgPreferencePage.USR6);
		outputTriggerLabels[7] = store.getString(TfgPreferencePage.USR7);
	}

	@SuppressWarnings("unchecked")
	public static List<TimeProfileParameters> getHardwareConfig() {
		List<TimeProfileParameters> timeProfileParameters = null;
		if (timer == null) {
			timer = NcdController.getInstance().getTfg();
		}
		if (timer != null) {
			try {
				int repeat = 1;
				if ((Boolean) timer.getAttribute("FramesLoaded")) {
					List<FrameSet> frameSetList = (List<FrameSet>) timer.getAttribute("FrameSets");
					TimeProfileParameters tpp = new TimeProfileParameters();
					FrameSetParameters lastfp = null;
					for (FrameSet fs : frameSetList) {
						FrameSetParameters fp = new FrameSetParameters();
						fp.setNframes(fs.getFrameCount());
						fp.setRunTimes(fs.getRequestedLiveTime());
						fp.setWaitTimes(fs.getRequestedDeadTime());
						int index = fs.getDeadPause();
						index = (index > 16) ? index-16 : index;
						fp.setWaitPause(getExternalName(TimeFrameProfile.displayPause[index+1]));
						index = fs.getLivePause();
						index = (index > 16) ? index-16 : index;
						fp.setRunPause(getExternalName(TimeFrameProfile.displayPause[index+1]));
						int deadPort = fs.getDeadPort();
						StringBuffer sbdp = new StringBuffer("00000000");
						if (deadPort > 0) {
							for (int i=0; i<8; i++) {
								char ch = ((deadPort & (1<<i)) == 0) ? '0' : '1';
								sbdp.setCharAt(i, ch);
							}
						}
						fp.setWaitPulse(sbdp.toString());
						int livePort = fs.getLivePort();
						StringBuffer sblp = new StringBuffer("00000000");
						if (livePort > 0) {
							for (int i=0; i<8; i++) {
								char ch = ((livePort & (1<<i)) == 0) ? '0' : '1';
								sblp.setCharAt(i, ch);
							}
						}
						fp.setRunPulse(sblp.toString());
						if (fp.equals(lastfp)) {
							repeat++;
						} else {
							tpp.addFrameSetParameter(fp);
							lastfp = fp;
						}
					}
					tpp.setRepeat(repeat);
					tpp.setCycles((Integer) timer.getAttribute("Cycles"));
					tpp.setExtInhibit((Boolean) timer.getAttribute("Ext-Inhibit"));
					int index = (Integer)timer.getAttribute("Start-Method");
					tpp.setStartMethod(getExternalName(TimeFrameProfile.startLabelList[index]));
					ArrayList<Double> debounceValues = (ArrayList<Double>) timer.getAttribute("Debounce");
					ArrayList<Double> thresholdValues = (ArrayList<Double>) timer.getAttribute("Threshold");
					StringBuffer dbuf = new StringBuffer();
					StringBuffer tbuf = new StringBuffer();
					for (int i=0; i< 16; i++) {
						dbuf.append(debounceValues.get(i) + " ");
						tbuf.append(thresholdValues.get(i) + " ");						
					}
					tpp.setInputTriggerDebounce(dbuf.toString().trim());
					tpp.setInputTriggerThreshold(tbuf.toString().trim());
					Integer driveValue = (Integer) timer.getAttribute("Drive");
					StringBuffer drive = new StringBuffer("00000000");
					if (driveValue > 0) {
						for (int i=0; i<8; i++) {
							char ch = ((driveValue & (1<<i)) == 0) ? '0' : '1';
							drive.setCharAt(i, ch);
						}
					}
					tpp.setOutputTriggerDrive(drive.toString());
					Integer inversionValue = (Integer) timer.getAttribute("Inversion");
					StringBuffer inversion = new StringBuffer("00000000");
					if (inversionValue > 0) {
						for (int i=0; i<8; i++) {
							char ch = ((inversionValue & (1<<i)) == 0) ? '0' : '1';
							inversion.setCharAt(i, ch);
						}
					}
					tpp.setOutputTriggerInversion(inversion.toString());

					timeProfileParameters = new ArrayList<TimeProfileParameters>();
					timeProfileParameters.add(tpp);
				}
			} catch (DeviceException e) {
				// do nothing, just return null.
			}
		}
		return timeProfileParameters;
	}

	public class MenuCellEditor extends CellEditor {
		private Text text;
		private String[] itemList;
		private static final int defaultStyle = SWT.SINGLE;
		private Shell shell;
		private Menu menu;

		public MenuCellEditor(Composite parent, String[] items) {
			this(parent, defaultStyle, items);
		}

		public MenuCellEditor(Composite parent, int style, String[] items) {
			super(parent, style);
			itemList = items;

		}

		@Override
		protected Control createControl(Composite parent) {
			shell = parent.getShell();
			text = new Text(parent, defaultStyle);
			return text;
		}

		@Override
		protected Object doGetValue() {
			String txt = text.getText();
			return txt;
		}

		@Override
		protected int getDoubleClickTimeout() {
			return 1;
		}
		
		@Override
		protected void doSetFocus() {
			if (text != null) {
				text.selectAll();
				menu = new Menu(shell, SWT.POP_UP);
				menu.addMenuListener(new MenuAdapter() {
					@Override
					public void menuHidden(MenuEvent e) {
						// We add this here to deal with the the popup being shown
						// but the user does not change any value but selects another table entry.
						deactivate();
					}
				});
				
				for (int i = 0; i < itemList.length; i++) {
					MenuItem item = new MenuItem(menu, SWT.CHECK);
					item.setText(itemList[i]);
					String txt = text.getText();
					boolean state = (txt.charAt(i) == '0') ? false : true;
					item.setSelection(state);
					item.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							boolean state = ((MenuItem) e.widget).getSelection();
							StringBuffer sb = new StringBuffer(text.getText());
							int i;
							for (i = 0; i < itemList.length; i++) {
								if (itemList[i].equals(((MenuItem) e.widget).getText()))
									break;
							}
							sb.setCharAt(i, (state ? '1' : '0'));
							text.setText(sb.toString());
							fireApplyEditorValue();
						}
					});
				}
				menu.setVisible(true);
			}
		}

		@Override
		protected void doSetValue(Object value) {
			text.setText((String) value);
		}

		@Override
		protected void keyReleaseOccured(KeyEvent keyEvent) {
			if (keyEvent.character == '\u001b') { // Escape character
				fireCancelEditor();
			} else if (keyEvent.character == '\t') { // tab key
				fireCancelEditor();
			}
		}
		
		@Override
		public void deactivate() {
			super.deactivate();
			super.deactivate();
		}
	}
}
