/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.device.Timer;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.rcp.ncd.NcdController;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DAppearance;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DGraphTable;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.OverlayType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.Plot1DStyles;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.PrimitiveType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.events.AbstractOverlayConsumer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.events.OverlayDrawingEvent;
import uk.ac.gda.server.ncd.beans.TemperatureProfileParameters;

import com.swtdesigner.SWTResourceManager;

public class NcdTemperatureView extends ViewPart implements IObserver {
	public NcdTemperatureView() {
	}

	public static final String ID = "gda.rcp.ncd.views.NcdTemperatureView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(NcdTemperatureView.class);
	private static final String[] columnNames = { "Ramp", "Start Temp", "End Temp", "Rate (deg/min)", "Dwell Time (min)",
	"Cooling Speed" };
	private static double DSCFRAMEMARKER = 32766;
	private Temperature temperature;
	private boolean doingSet;
	private String labels[] = { "Probe" };
	private Text textField;
	private Text dataField;
	private Text statusField;
	private Text upperLimit;
	private Text lowerLimit;
	private Text setField;
	private Text startRamp;
	private Combo source;
	private Table table;
	private TableViewer tableViewer;
	private Button setButton;
	private Button autoButton;
	private Button manualButton;
	private Spinner pumpSpeed;
	private Label pumpSpeedLabel;
	private DataSetPlotter plotter;
	protected FrameMarkerOverlay frameMarkerOverlay;
	private Shell shell;

	
	private List<TemperatureRamp> rampList;
	private TemperatureProfileParameters temperatureProfileParameters;
	private double lastXValue = 0.0;
	private boolean coolingSpeedEditable = false;

	private List<Double> temperatureData = null;
	private List<Double> dscData = null;
	private List<Double> xData = null;
	private String name = "Linkam"; // Hard coded UGH!!!!!!!!!!!!!!!!!!!
	private Timer tfg;
	
	@Override
	public void createPartControl(Composite parent) {

		shell = parent.getShell();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		Composite composite1 = new Composite(composite, SWT.NONE);
		composite1.setLayout(new GridLayout(3, false));
		composite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		{
			textField = new Text(composite1, SWT.BORDER);
			textField.setText("0.0");
			textField.setFont(SWTResourceManager.getFont("Sans", 18, SWT.NORMAL));
			textField.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			textField.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			dataField = new Text(composite1, SWT.BORDER);
			dataField.setFont(SWTResourceManager.getFont("Sans", 18, SWT.NORMAL));
			dataField.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			dataField.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			dataField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			statusField = new Text(composite1, SWT.BORDER);
			statusField.setFont(SWTResourceManager.getFont("Sans", 18, SWT.NORMAL));
			statusField.setForeground(SWTResourceManager.getColor(0, 0, 0));
			statusField.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			statusField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		Composite composite2 = new Composite(composite, SWT.NONE);
		composite2.setLayout(new GridLayout(8, false));
		composite2.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		{
			source = new Combo(composite2, SWT.NONE);
			source.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			source.setText(labels[0]);
			source.setEnabled(false);
			source.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					String sel = ((Combo)evt.getSource()).getText();
					try {
						if (temperature != null)
							temperature.setProbe(sel);
					} catch (Exception e) {
						((Combo) evt.getSource()).setText(labels[0]);
					}
				}
			});

			Label label1 = new Label(composite2, SWT.NONE);
			label1.setText("Upper Limit");		

			upperLimit = new Text(composite2, SWT.BORDER);
		
			Label label2 = new Label(composite2, SWT.NONE);
			label2.setText("Lower Limit");
		
			lowerLimit = new Text(composite2, SWT.BORDER);
		
			Label label3 = new Label(composite2, SWT.NONE);
			label3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label3.setText("Start TFG at frame");
		
			startRamp = new Text(composite2, SWT.BORDER);
			startRamp.setText("1");
			startRamp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			pumpSpeedLabel = new Label(composite2, SWT.NONE);
			pumpSpeedLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			pumpSpeedLabel.setText("Pump Speed");			
			pumpSpeedLabel.setEnabled(false);			

			pumpSpeed = new Spinner(composite2, SWT.BORDER);
			pumpSpeed.setPageIncrement(1);
			pumpSpeed.setMaximum(30);
			pumpSpeed.setMinimum(0);
			pumpSpeed.setSelection(0);
			pumpSpeed.setEnabled(false);
			pumpSpeed.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					try {
						if (temperature != null)
							temperature.setAttribute("LNPumpSpeed", new Integer(pumpSpeed.getText()));
					} catch (DeviceException e) {
						logger.debug(e.getStackTrace().toString());
					}
				}
			});

			manualButton = new Button(composite2, SWT.RADIO);
			manualButton.setText("Manual");
			manualButton.setEnabled(false);
			manualButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					pumpSpeed.setEnabled(true);
					try {
						if (temperature != null) {
							temperature.setAttribute("LNPumpAuto", new Boolean(false));
							temperature.setAttribute("LNPumpSpeed", new Integer(pumpSpeed.getText()));
						}
					} catch (DeviceException e) {
						logger.debug(e.getStackTrace().toString());
					}
				}
			});
			
			autoButton = new Button(composite2, SWT.RADIO);
			autoButton.setSelection(true);
			autoButton.setText("Auto");
			autoButton.setEnabled(false);
			autoButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					pumpSpeed.setEnabled(false);
					try {
						if (temperature != null)
							temperature.setAttribute("LNPumpAuto", new Boolean(true));
					} catch (DeviceException e) {
						logger.debug(e.getStackTrace().toString());
					}
				}
			});
		
			Label label4 = new Label(composite2, SWT.NONE);
			label4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label4.setText("SetPoint");
		
			setField = new Text(composite2, SWT.BORDER);
			setButton = new Button(composite2, SWT.NONE);
			setButton.setText("Set");
			setButton.setEnabled(false);
			setButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					try {
						doingSet = true;
						double d = new Double(setField.getText()).doubleValue();
						if (temperature != null)
							temperature.setTargetTemperature(d);
					} catch (DeviceException e) {
						logger.error(e.getMessage());
					}
				}
			});
		}

		Composite composite3 = new Composite(composite, SWT.NONE);
		composite3.setLayout(new GridLayout(1, false));
		composite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// Create the table
		createTable(composite3);
		// Create and setup the TableViewer
		createTableViewer();
		// createDefaultProfile();
		// The input for the table viewer is the instance of
		
		Composite composite4 = new Composite(composite, SWT.NONE);
		composite4.setLayout(new GridLayout(1, false));
		composite4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotter = new DataSetPlotter(PlottingMode.ONED, composite4, true);
		plotter.getComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotter.setXAxisLabel("Time");
		plotter.setYAxisLabel("Temperature");
		plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		plotter.updateAllAppearance();
		plotter.refresh(false);

		rampList = new ArrayList<TemperatureRamp>();
		rampList.add(new TemperatureRamp(0));
		temperatureProfileParameters = new TemperatureProfileParameters();
		temperatureProfileParameters.setTemperatureRampList(rampList);
		tableViewer.setInput(rampList);
		tfg = NcdController.getInstance().getTfg();
		if (tfg != null) {
			tfg.addIObserver(this);
		}
		updatePlot();
		createActions();
	}
	
	@SuppressWarnings("unchecked")
	private void updatePlot() {
		List<Double> profile = new ArrayList<Double>();
		List<Double> time = new ArrayList<Double>();
		List<FrameSet> frameSetList = null;
		ArrayList<Double> frameTimeList = new ArrayList<Double>();
		
		double t = 0.0;
		time.add(t);
		profile.add(rampList.get(0).getStartTemperature());
		for (TemperatureRamp rmp : rampList) {
			profile.add(rmp.getEndTemperature());
			t += (rmp.getEndTemperature() - rmp.getStartTemperature()) / rmp.getRate();
			time.add(t);
			if (rmp.getDwellTime() > 0.0) {
				profile.add(rmp.getEndTemperature());
				t += rmp.getDwellTime();
				time.add(t);
			}
		}
			
		try {
			List<IDataset> plotList = new ArrayList<IDataset>();
			plotList.add(DoubleDataset.createFromObject(profile.toArray()));
			List<AxisValues> axisList = new ArrayList<AxisValues>();
			axisList.add(new AxisValues(time));

			if (xData == null) {
				xData = new ArrayList<Double>();
				temperatureData = new ArrayList<Double>();
				dscData = new ArrayList<Double>();
			} else {
				xData.clear();
				temperatureData.clear();
				dscData.clear();				
			}

			xData.add(0.0);
			temperatureData.add(rampList.get(0).getStartTemperature());
			plotList.add(DoubleDataset.createFromObject(temperatureData.toArray()));
			axisList.add(new AxisValues(xData));

			dscData.add(rampList.get(0).getStartTemperature());
			plotList.add(DoubleDataset.createFromObject(dscData.toArray()));
			axisList.add(new AxisValues(xData));

			Plot1DGraphTable legend = plotter.getColourTable();
			legend.clearLegend();
			legend.addEntryOnLegend(0, new Plot1DAppearance(java.awt.Color.BLUE, Plot1DStyles.SOLID, "Profile"));
			legend.addEntryOnLegend(1, new Plot1DAppearance(java.awt.Color.RED, Plot1DStyles.SOLID, "Temperature"));
			legend.addEntryOnLegend(2, new Plot1DAppearance(java.awt.Color.GREEN, Plot1DStyles.SOLID, "DSC"));

			plotter.resetView();
			plotter.replaceAllPlots(plotList, axisList);
			plotter.updateAllAppearance();
			plotter.refresh(false);

			if (tfg != null) {
				frameSetList = (List<FrameSet>) tfg.getAttribute("FrameSets");
				double sum = 0.0;
				for (FrameSet fs : frameSetList) {
					for (int i = 0; i < fs.getFrameCount(); i++) {
						sum += (fs.getRequestedDeadTime() + fs.getRequestedLiveTime()) / 60000.0; // convert from millseconds to minutes
						frameTimeList.add(sum);
					}
				}
			}

			frameMarkerOverlay = new FrameMarkerOverlay(getSite().getShell().getDisplay(), new java.awt.Color[] {
					java.awt.Color.MAGENTA});
			if (frameMarkerOverlay != null) {
				plotter.unRegisterOverlay(frameMarkerOverlay);
				frameMarkerOverlay.setXValues(frameTimeList);
				frameMarkerOverlay.setY((DoubleDataset) plotList.get(0));
				plotter.registerOverlay(frameMarkerOverlay);
			}
		} catch (PlotException e) {
			e.printStackTrace();
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}

	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
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

	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(false);
		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];
		CellEditor editor;
		for (int i = 0; i < columnNames.length; i++) {
			editor = new TextCellEditor(table);
			((Text) editor.getControl()).setTextLimit(60);
			editors[i] = editor;
		}
		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);
		// We are not allowing the table to be sorted
		tableViewer.setSorter(null);
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new CellModifier());
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<TemperatureRamp>) inputElement).toArray();
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setUseHashlookup(true);
	}

	@Override
	public void setFocus() {
	}

	public void insertRamp() {
		// find the currently selected ramp
		IStructuredSelection iss = (IStructuredSelection) tableViewer.getSelection();
		TemperatureRamp ramp = (TemperatureRamp) iss.getFirstElement();
		int index = 0;
		if (ramp != null) {
			for (TemperatureRamp rmp : rampList) {
				index++;
				if (rmp.equals(ramp))
					break;
			}
		} else {
			index++;
			ramp = rampList.get(0);
		}

		TemperatureRamp newramp = ramp.copy();
		newramp.setStartTemperature(ramp.getEndTemperature());
		rampList.add(index, newramp);		
		// re-oder the ramp numbering
		int rampNumber = 0;
		for (TemperatureRamp rmp : rampList) {
			rmp.setRampNumber(rampNumber++);
		}
		temperatureProfileParameters.setTemperatureRampList(rampList);
		tableViewer.setInput(rampList);
		table.update();
		updatePlot();
		table.redraw();
	}

	public void deleteRamp() {
		// Delete the currently selected ramp
		IStructuredSelection iss = (IStructuredSelection) tableViewer.getSelection();
		TemperatureRamp ramp = (TemperatureRamp) iss.getFirstElement();
		rampList.remove(ramp);
		int rampNumber = 1;
		for (TemperatureRamp rmp : rampList) {
			rmp.setRampNumber(rampNumber++);
		}
		temperatureProfileParameters.setTemperatureRampList(rampList);
		tableViewer.setInput(rampList);
		updatePlot();
	}

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
			return (columnIndex == 0 || (columnIndex == 5 && !coolingSpeedEditable)) ? false : true;
		}

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object getValue(Object element, String property) {

			// Find the index of the column
			int columnIndex = getColumnNames().indexOf(property);

			Object result = null;
			TemperatureRamp ramp = (TemperatureRamp) element;
			switch (columnIndex) {
			case 1:
				result = String.valueOf(ramp.getStartTemperature());
				break;
			case 2:
				result = String.valueOf(ramp.getEndTemperature());
				break;
			case 3:
				result = String.valueOf(ramp.getRate());
				break;
			case 4:
				result = String.valueOf(ramp.getDwellTime());
				break;
			case 5:
				result = String.valueOf(ramp.getCoolingSpeed());
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
				TemperatureRamp ramp = (TemperatureRamp) item.getData();

				switch (columnIndex) {
				case 0:
					break;
				case 1:
					ramp.setStartTemperature(Double.parseDouble((String) value));
					int index = 0;
					TemperatureRamp previousRamp = rampList.get(0);
					for (TemperatureRamp rmp : rampList) {
						if (rmp.equals(ramp) && index > 0) {
							previousRamp.setEndTemperature(ramp.getStartTemperature());
						} else {
							index++;
							previousRamp = rmp;
						}
					}						
					updatePlot();
					break;
				case 2:
					ramp.setEndTemperature(Double.parseDouble((String) value));
					updatePlot();
					break;
				case 3:
					ramp.setRate(Double.parseDouble((String) value));
					updatePlot();
					break;
				case 4:
					ramp.setDwellTime(Double.parseDouble((String) value));
					updatePlot();
					break;
				case 5:
					try {
						ramp.setCoolingSpeed(Integer.parseInt((String) value));
					} catch (NumberFormatException nfe) {
						logger.error("Invalid integer number");
					}
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
			TemperatureRamp ramp = (TemperatureRamp) element;
			switch (columnIndex) {
			case 0:
				return String.valueOf(ramp.getRampNumber());
			case 1:
				return String.valueOf(ramp.getStartTemperature());
			case 2:
				return String.valueOf(ramp.getEndTemperature());
			case 3:
				return String.valueOf(ramp.getRate());
			case 4:
				return String.valueOf(ramp.getDwellTime());
			case 5:
				return String.valueOf(ramp.getCoolingSpeed());
			default:
				return "";
			}
		}
	}

	/**
	 * Returns basic actions with an load and a save.
	 */
	private void createActions() {
		try {
			IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
			manager.add(getInsertRampAction(this));
			manager.add(getDeleteRampAction(this));
			manager.add(new Separator());
			manager.add(getStartAction(this));
			manager.add(getHoldAction(this));
			manager.add(getStopAction(this));
			manager.add(new Separator());
			manager.add(getOpenXMLAction(this));
			manager.add(getSaveXMLAction(this));
			manager.add(getConfigureAction(this));
			manager.add(new Separator());
			manager.add(getConnectAction(this));
		} catch (Exception e) {
			// if this occurs we're created by main()
		}
	}
	public IAction getSaveXMLAction(final ViewPart view) {
		IAction saveXMLAction = new Action() {
			FileDialog dialog;

			@Override
			public void run() {

				if (dialog == null) {
					dialog = new FileDialog(view.getSite().getShell(), SWT.SAVE);
					dialog.setText("Save temperature profile parameters");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(LocalProperties.getBaseDataDir());
				}

				String path = dialog.open();
				if (path == null)
					return;
				if (!path.toLowerCase().endsWith(".xml"))
					path = path + ".xml";

				final File toSave = new File(path);
				if (toSave.exists()) {
					final boolean ok = MessageDialog.openConfirm(view.getSite().getShell(), "Confirm Overwrite File",
							"The file '" + toSave.getName() + "' already exists.\n\nWould you like to overwrite?");
					if (!ok)
						return;
				}
				try {
					temperatureProfileParameters.setTemperatureRampList(rampList);
					TemperatureProfileParameters.writeToXML(temperatureProfileParameters, path);
				} catch (Exception ne) {
					logger.error("Cannot save temperature profile parameters", ne);
				}
			}
		};

		saveXMLAction.setText("Save");
		saveXMLAction.setToolTipText("Save temperature profile parameters to XML");
		saveXMLAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/save.png"));
		return saveXMLAction;
	}

	public IAction getOpenXMLAction(final ViewPart view) {
		IAction openXMLAction = new Action() {
			FileDialog dialog;

			@Override
			public void run() {
				if (dialog == null) {
					dialog = new FileDialog(view.getSite().getShell(), SWT.OPEN);
					dialog.setText("Load temperature profile parameters");
					dialog.setFilterExtensions(new String[] { "*.xml" });
					dialog.setFilterPath(LocalProperties.getBaseDataDir());
				}

				String path = dialog.open();
				if (path == null)
					return;

				final File toOpen = new File(path);
				if (!toOpen.exists())
					return;

				try {
					TemperatureProfileParameters temperatureProfileParameters = 
						TemperatureProfileParameters.createFromXML(path);
					rampList.clear();
					rampList = temperatureProfileParameters.getTemperatureRampList();
					tableViewer.setInput(rampList);
					table.update();
					updatePlot();
					table.redraw();
				} catch (Exception ne) {
					logger.error("Cannot open temperature profile parameters", ne);
				}
			}
		};

		openXMLAction.setText("Load");
		openXMLAction.setToolTipText("Load temperature profile parameters from XML");
		openXMLAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/open.png"));
		return openXMLAction;
	}

	public IAction getConfigureAction(final ViewPart view) {
		IAction configureAction = new Action() {
			@Override
			public void run() {
				try {
					configureHardware();
				} catch (Exception ne) {
					logger.error("Cannot configure temperature ramp parameters", ne);
				}
			}
		};

		configureAction.setText("Configure HW");
		configureAction.setToolTipText("Configure temperature ramp parameters");
		configureAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/configure.png"));
		return configureAction;
	}
	
	public IAction getConnectAction(final ViewPart view) {
		IAction connectAction = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				
				try {
					if (this.isChecked()) {
						connect();
					} else {
						disconnect();
					}
				} catch (Exception ne) {
					logger.error("Cannot connect to Linkam", ne);
				}
			}
		};

		connectAction.setText("Connect/disconnect");
		connectAction.setToolTipText("Connect/disconnect temperature device");
		connectAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/server_connect.png"));
		return connectAction;
	}

	public IAction getStartAction(final ViewPart view) {
		IAction startAction = new Action() {
			@Override
			public void run() {
				try {
					logger.debug("TemperaturePanel start() called");
						if (temperature != null) {
							temperature.start();
							doingSet = false;
						}
				} catch (Exception ne) {
					logger.error("Cannot start the temperature profile", ne);
				}
			}
		};

		startAction.setText("Start");
		startAction.setToolTipText("Start temperature profile");
		startAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/control_play_blue.png"));
		return startAction;
	}
	
	public IAction getHoldAction(final ViewPart view) {
		IAction holdAction = new Action() {
			@Override
			public void run() {
				try {
					logger.debug("TemperaturePanel hold() called");
						if (temperature != null) {
							temperature.hold();
							if (getText().startsWith("Hold"))
								setText("Resume temperature Profile");
							else
								setText("Hold temperature Profile");
						}
				} catch (Exception ne) {
					logger.error("Cannot hold/resume the temperature profile", ne);
				}
			}
		};

		holdAction.setText("Hold temperature Profile");
		holdAction.setToolTipText("Hold/Resume the temperature profile");
		holdAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/control_pause_blue.png"));
		return holdAction;
	}

	public IAction getStopAction(final ViewPart view) {
		IAction stopAction = new Action() {
			@Override
			public void run() {
				try {
					logger.debug("TemperaturePanel stop() called");
						if (temperature != null)
							temperature.stop();

				} catch (Exception ne) {
					logger.error("Cannot stop the temperature profile", ne);
				}
			}
		};

		stopAction.setText("Stop the temperature Profile");
		stopAction.setToolTipText("Stop the temperature profile");
		stopAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/control_stop_blue.png"));
		return stopAction;
	}
	
	public IAction getInsertRampAction(final ViewPart view) {
		IAction insertRampAction = new Action() {
			@Override
			public void run() {
				try {
					insertRamp();
				} catch (Exception ne) {
					logger.error("Cannot add temperature ramp", ne);
				}
			}
		};

		insertRampAction.setText("Add temperature ramp");
		insertRampAction.setToolTipText("Insert temperature ramp after the current selection");
		insertRampAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/table_add.png"));
		return insertRampAction;
	}

	public IAction getDeleteRampAction(final ViewPart view) {
		IAction deleteRampAction = new Action() {
			@Override
			public void run() {
				try {
					deleteRamp();
				} catch (Exception ne) {
					logger.error("Cannot delete temperature Ramp", ne);
				}
			}
		};

		deleteRampAction.setText("Delete temperature ramp");
		deleteRampAction.setToolTipText("Delete the currently selected ramp");
		deleteRampAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(view.getSite().getPluginId(),
				"icons/table_delete.png"));
		return deleteRampAction;
	}

	@Override
	public void update(Object iObservable, Object arg) {
		Display.getDefault().asyncExec(new Updater(iObservable, arg));
	}

	private class Updater implements Runnable {
		private Object iObservable;
		private Object arg;

		/**
		 * @param iObservable
		 * @param arg
		 */
		public Updater(Object iObservable, Object arg) {
			this.iObservable = iObservable;
			this.arg = arg;
		}

		@Override
		public void run() {
			if (arg != null && iObservable instanceof Timer) {
				updatePlot();
			}			
			if (arg != null && arg instanceof TemperatureStatus) {
				TemperatureStatus status = (TemperatureStatus) arg;

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(3);
				nf.setMinimumFractionDigits(1);
				String textValue = nf.format(status.getCurrentTemperature());
				textField.setText(textValue);

				String statusString;
				int ramp = status.getCurrentRamp();
				if (ramp == -1 || doingSet) {
					statusString = status.getStatus();
				} else {
					statusString = "Ramp " + ramp + " " + status.getStatus();
				}
				statusField.setText(statusString);

				String dataString = status.getAdditionalData();
				StringTokenizer strtok = new StringTokenizer(dataString);

				double xValue = Double.valueOf(strtok.nextToken()).doubleValue();
				// Convert to minutes for plotting.
				xValue /= 60.0;
				strtok.nextToken();
				boolean hasDscData = strtok.hasMoreTokens();
				double yValue = 0.0;
				if (hasDscData) {
					yValue = Double.valueOf(strtok.nextToken()).doubleValue();
					dataField.setText(String.valueOf(yValue));
				}
				if (xValue >= 0.0) {
					if (plotter != null) {
						try {
							// There may be a data file associated with the
							// temperature. If so use its name as the plot title
							String title = (String) temperature.getAttribute("DataFilename");
							logger.debug("TemperatureView: plot title is " + title);
							if (title != null) {
								plotter.setTitle(title);
							}
						} catch (DeviceException de) {
							logger.error("TemperaturePanel: " + de.getMessage());
						}

						logger.debug("Temperature values " + xValue + " " + status.getCurrentTemperature() + " " + lastXValue);
						xData.add(xValue);
						temperatureData.add(status.getCurrentTemperature());
						if (yValue != DSCFRAMEMARKER && hasDscData) {
							logger.debug("DSC values " + xValue + " " + yValue + " " + lastXValue);
							dscData.add(yValue);
						}
						try {
							plotter.replaceAPlot(DoubleDataset.createFromObject(temperatureData), new AxisValues(xData), 1);
							plotter.replaceAPlot(DoubleDataset.createFromObject(dscData), new AxisValues(xData), 2);
						} catch (PlotException e) {
							logger.error("Plot error: ", e);
						}
						lastXValue = xValue;
					}
				}
			}
		}
	}

	public void configureHardware() {
		if (temperature != null) {
			try {
				temperature.setRamps((ArrayList<TemperatureRamp>)rampList);
				temperature.setAttribute("StartingRamp", new Integer(startRamp.getText()));
			} catch (NumberFormatException nfe) {
				logger.error(nfe.getMessage());
			} catch (DeviceException de) {
				logger.error(de.getMessage());
			}
		} else {
			logger.warn("You have not connected to the temperature device");
			MessageDialog.openWarning(shell, "Configure Failure", "You have not connected to the " + getName() + "temperature device");
		}

	}

	public void configure() {
		try {
			if (temperature != null) {
				double d = temperature.getCurrentTemperature();
				setField.setText(new Double(d).toString());

				source.removeAll();
				ArrayList<String> names = temperature.getProbeNames();
				if (names.size() > 0) {
					for (String name : names)
						source.add(name);
				} else {
					source.add(labels[0]);
				}

				if (temperature.getAttribute("NeedsCoolerSpeedSetting") != null) {
					coolingSpeedEditable = true;
					startRamp.setEditable(true);
				}
			} else {
				source.add(labels[0]);
			}
			double upperTemp = temperature.getUpperTemp();
			upperLimit.setText(Double.toString(upperTemp));
			double lowerTemp = temperature.getLowerTemp();
			lowerLimit.setText(Double.toString(lowerTemp));

		} catch (DeviceException e) {
			logger.debug(e.getStackTrace().toString());
		}
	}

	public void connect() {
		try {
			logger.debug("Finding instance of " + getName());
			temperature = (Temperature) Finder.getInstance().find(getName());
			if (temperature != null) {
				temperature.reconfigure();
				setButton.setEnabled(true);
				source.setEnabled(true);
				configure();
				if (temperature.getAttribute("NeedsCooler") != null) {
					setPumpSpeedControls(true);
				}
				temperature.addIObserver(this);
				NcdController.getInstance().getNcdDetectorSystem().setAttribute("addScannable", temperature);
			}
		} catch (DeviceException e) {
			logger.error("Error temperature attribute" + e.getMessage());

		} catch (FactoryException e) {
			logger.error("Error closing temperature " + getName());
		}
	}

	public void disconnect() {
		try {
			NcdController.getInstance().getNcdDetectorSystem().setAttribute("removeScannable", temperature);
			if (temperature != null) {
				setPumpSpeedControls(false);
			}
			temperature.deleteIObserver(this);
			temperature.close();
			setButton.setEnabled(false);
			source.setEnabled(false);
		} catch (DeviceException e) {
			logger.error("Error closing temperature " + getName());
		}
	}
	
	private void setPumpSpeedControls(boolean enabled) {
		pumpSpeedLabel.setEnabled(enabled);
		pumpSpeed.setEnabled(enabled);
		autoButton.setEnabled(enabled);
		manualButton.setEnabled(enabled);
	}
	
	private String getName() {
		return name;
	}

	public class FrameMarkerOverlay extends AbstractOverlayConsumer {

		private List<Double> xValues;
		private java.awt.Color[] colours;
		private DoubleDataset y;

		public FrameMarkerOverlay(Display display, final java.awt.Color[] colours) {
			super(display);
			this.colours = colours;
		}

		@Override
		protected int[] createDrawingParts(OverlayProvider provider) {
			final int[] lines = new int[xValues.size()];
			for (int i = 0; i < lines.length; i++) {
				lines[i] = provider.registerPrimitive(PrimitiveType.LINE);
			}
			return lines;
		}

		public void setY(final DoubleDataset y) {
			this.y = y;
		}

		@Override
		protected void drawOverlay(OverlayDrawingEvent evt) {
			draw();
		}

		private void draw() {
			for (int i = 0; i < xValues.size(); i++) {
				drawLine(xValues.get(i), colours[0], i);
			}
		}

		private void drawLine(double x, java.awt.Color color, int partIndex) {
			provider.begin(OverlayType.VECTOR2D);
			provider.setColour(parts[partIndex], color);
			((Overlay1DProvider) provider).drawLine(parts[partIndex], x, y.min().doubleValue(), x, y.max()
					.doubleValue());
			provider.end(OverlayType.VECTOR2D);
		}

		public void setXValues(List<Double> xValues) {
			this.xValues = xValues;
		}
	}

//	public static void main(String[] args) {
//		Display display = new Display();
//		Shell shell = new Shell(display);
//		shell.setText("Temperature Configure");
//		shell.setLayout(new FillLayout());
//		NcdTemperatureView sd = new NcdTemperatureView();
//		sd.createPartControl(shell);
//		shell.open();
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch())
//				display.sleep();
//		}
//		display.dispose();
//	}
}
