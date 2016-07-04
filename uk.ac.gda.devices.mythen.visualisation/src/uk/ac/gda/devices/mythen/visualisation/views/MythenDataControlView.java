/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.visualisation.views;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import gda.device.detector.mythen.data.MythenDataFileUtils;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.gda.devices.mythen.visualisation.commands.AddFilesCommand;
import uk.ac.gda.devices.mythen.visualisation.commands.AddSrsFilesCommand;

public class MythenDataControlView extends ViewPart {

	private static final double DEFAULT_MIN_ANGLE = 10;

	private static final double DEFAULT_MAX_ANGLE = 13;

	private static final double DEFAULT_BIN_SIZE = 0.004;

	public static final String ID = MythenDataControlView.class.getName();

	private static final String PLOT_VIEW_ID = "uk.ac.gda.devices.mythen.visualisation.views.mythen_plot";

	public static MythenDataControlView getInstance() {
		return (MythenDataControlView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ID);
	}

	private Table dataFileTable;

	private Spinner minAngleSpinner;
	private Spinner maxAngleSpinner;
	private Spinner binSizeSpinner;

	private Button plot1DButton;
	private Button plotAgainstChannel;
	private Button plot2D3DButton;

	private boolean useChannel = false;

	IDataset anglesDataset;
	IDataset channelsDataset;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		GridData gridData;

		createDataFileTable(parent);

		Group optionsGroup = createOptionsGroup(parent);
		Group plotsGroup = createPlotsGroup(parent);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		dataFileTable.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		optionsGroup.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		plotsGroup.setLayoutData(gridData);

		updateButtonState();
	}

	private void createDataFileTable(Composite parent) {
		dataFileTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		dataFileTable.setLinesVisible(true);
		dataFileTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();
			}
		});

		TableColumn filenameColumn = new TableColumn(dataFileTable, SWT.NULL);
		filenameColumn.setText("Filename");
		filenameColumn.pack();

		createFileTableMenu(dataFileTable);
	}

	private void createFileTableMenu(Table table) {
		Menu menu = new Menu(table);
		table.setMenu(menu);

		MenuItem addDataFilesMenuItem = new MenuItem(menu, SWT.NULL);
		addDataFilesMenuItem.setText("Add data files");
		addDataFilesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					new AddFilesCommand().execute(null);
				} catch (ExecutionException ee) {
					ee.printStackTrace();
				}
			}
		});

		MenuItem addSrsDataFilesMenuItem = new MenuItem(menu, SWT.NULL);
		addSrsDataFilesMenuItem.setText("Add data files using GDA scan data file");
		addSrsDataFilesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					new AddSrsFilesCommand().execute(null);
				} catch (ExecutionException ee) {
					ee.printStackTrace();
				}
			}
		});

		MenuItem removeSelectedFilesMenuItem = new MenuItem(menu, SWT.NULL);
		removeSelectedFilesMenuItem.setText("Remove selected data files");
		removeSelectedFilesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedFilesFromTable();
			}
		});

		MenuItem removeAllFilesMenuItem = new MenuItem(menu, SWT.NULL);
		removeAllFilesMenuItem.setText("Remove all data files");
		removeAllFilesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeAllFilesFromTable();
			}
		});
	}

	private Group createOptionsGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);
		group.setText("Options");
		group.setLayout(new GridLayout(2, false));

		Label minAngleLabel = new Label(group, SWT.NULL);
		minAngleLabel.setText("Minimum angle:");
		minAngleLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		minAngleSpinner = new Spinner(group, SWT.BORDER);
		minAngleSpinner.setValues((int) (DEFAULT_MIN_ANGLE * 10), -1800, 1800, 1, 1, 10);
		minAngleSpinner.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));

		Label maxAngleLabel = new Label(group, SWT.NULL);
		maxAngleLabel.setText("Maximum angle:");
		maxAngleLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		maxAngleSpinner = new Spinner(group, SWT.BORDER);
		maxAngleSpinner.setValues((int) (DEFAULT_MAX_ANGLE * 10), -1800, 1800, 1, 1, 10);
		maxAngleSpinner.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));

		Label binSizeLabel = new Label(group, SWT.NULL);
		binSizeLabel.setText("Bin size:");
		binSizeLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		binSizeSpinner = new Spinner(group, SWT.BORDER);
		binSizeSpinner.setValues((int) (DEFAULT_BIN_SIZE * 1000), 1, 100, 3, 1, 10);
		binSizeSpinner.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));

		return group;
	}

	private Group createPlotsGroup(Composite parent) {
		Group plotsGroup = new Group(parent, SWT.NULL);
		plotsGroup.setText("Plots");
		plotsGroup.setLayout(new GridLayout(2, false));

		plot1DButton = new Button(plotsGroup, SWT.NULL);
		plot1DButton.setText("1D plot");

		plot1DButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double minAngle = Double.parseDouble(minAngleSpinner.getText());
				double maxAngle = Double.parseDouble(maxAngleSpinner.getText());
				double binSize = Double.parseDouble(binSizeSpinner.getText());

				double[][][] data = getSelectedDataFromTable();

				do1DPlot(data, minAngle, maxAngle, binSize);
			}
		});

		plotAgainstChannel = new Button(plotsGroup, SWT.CHECK);
		plotAgainstChannel.setText("Use channel as x axis");
		plotAgainstChannel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useChannel = plotAgainstChannel.getSelection();
			}
		});

		plot2D3DButton = new Button(plotsGroup, SWT.NULL);
		plot2D3DButton.setText("2D/3D plot");

		plot2D3DButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double minAngle = Double.parseDouble(minAngleSpinner.getText());
				double maxAngle = Double.parseDouble(maxAngleSpinner.getText());
				double binSize = Double.parseDouble(binSizeSpinner.getText());

				double[][][] data = getSelectedDataFromTable();

				do2D3DPlot(data, minAngle, maxAngle, binSize);
			}
		});

		return plotsGroup;
	}

	private void do1DPlot(double[][][] data, double minAngle, double maxAngle, double binSize) {

		// Bin & restrict data
		if (!useChannel) {
			data = MythenDataFileUtils.binMythenData(data, binSize);
			data = MythenDataFileUtils.getDataSubset(data, minAngle, maxAngle);
		}
		// Build DataSets
		List<IDataset> datasets = new Vector<IDataset>();
		for (int i = 0; i < data.length; i++) {
			double[] dataSubset = column(data[i], 1);
			IDataset dataset = DatasetFactory.createFromObject(dataSubset);
			dataset.setName("data");
			datasets.add(dataset);
		}

		// Build angle axis dataset

		double[] angles;
		double[] channels;

		if (useChannel) {
			// Build channel axis dataset
			channels = column(data[0], 3);
			channelsDataset = DatasetFactory.createFromObject(channels);
			channelsDataset.setName("channel");
		}

		else {
			// Build angle axis dataset

			angles = column(data[0], 0);
			anglesDataset = DatasetFactory.createFromObject(angles);
			anglesDataset.setName("angle");
		}

		// Open plot view, if not already open
		ensureMythenPlotViewIsOpen();
		PlotView plotView = getPlotView();

		AxisMapBean amb = new AxisMapBean();

		DataBean dataBean = new DataBean(GuiPlotMode.ONED);

		for (IDataset d : datasets) {
			DatasetWithAxisInformation axisData = new DatasetWithAxisInformation();
			axisData.setAxisMap(amb);
			axisData.setData(d);

			try {
				dataBean.addData(axisData);
			} catch (DataBeanException e) {
				e.printStackTrace();
			}
		}

		if (useChannel)
			dataBean.addAxis(AxisMapBean.XAXIS, channelsDataset);
		else
			dataBean.addAxis(AxisMapBean.XAXIS, anglesDataset);

		try {
			plotView.processPlotUpdate(dataBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void do2D3DPlot(double[][][] data, double minAngle, double maxAngle, double binSize) {

		// Bin & restrict data
		data = MythenDataFileUtils.binMythenData(data, binSize);
		data = MythenDataFileUtils.getDataSubset(data, minAngle, maxAngle);

		IDataset dataset = DatasetFactory.zeros(DoubleDataset.class, data.length, data[0].length);

		// Build DataSet
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				dataset.set(data[i][j][1], i, j);
			}
		}


//		// Build DataSet
//		double[][] two_d_data = new double[data.length][];
//		for (int i = 0; i < data.length; i++) {
//			two_d_data[i] = column(data[i], 1);
//		}
//		IDataset dataset = new DataSet("data", two_d_data);

		// Build angle axis dataset
		double[] angles = column(data[0], 0);
		IDataset anglesDataset = DatasetFactory.createFromObject(angles);
		anglesDataset.setName("angle");

		// Open plot view, if not already open
		ensureMythenPlotViewIsOpen();
		PlotView plotView = getPlotView();

		// Set plot mode of plot view
		try {
			plotView.updatePlotMode(GuiPlotMode.SURF2D);
		} catch (Exception e) {
			e.printStackTrace();
		}

		AxisMapBean amb = new AxisMapBean();

		DatasetWithAxisInformation axisData = new DatasetWithAxisInformation();
		axisData.setAxisMap(amb);
		axisData.setData(dataset);

		DataBean dataBean = new DataBean();
		try {
			dataBean.addData(axisData);
		} catch (DataBeanException e) {
			e.printStackTrace();
		}

		dataBean.addAxis(AxisMapBean.XAXIS, anglesDataset);

		try {
			plotView.processPlotUpdate(dataBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a 2D array of {@code double}s, and a column <em>n</em>, returns the <em>n</em>th 'column' from that array -
	 * i.e. a 1D array consisting of the <em>n</em>th value from each row.
	 *
	 * @param data
	 *            a 2D array of data
	 * @param column
	 *            the index of the value to extract from each column
	 * @return the values from each row
	 */
	private static double[] column(double[][] data, int column) {
		double[] result = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = data[i][column];
		}
		return result;
	}

	public void addFileToTable(String filename) {
		File file = new File(filename);
		TableItem item = new TableItem(dataFileTable, SWT.NULL);
		item.setText(new String[] { file.getName() });
		System.out.println("Loading " + filename);
		double[][] data = MythenDataFileUtils.readMythenProcessedDataFile(filename, true);
		item.setData(data);
		updateButtonState();
	}

	public void removeSelectedFilesFromTable() {
		dataFileTable.remove(dataFileTable.getSelectionIndices());
		updateButtonState();
	}

	public void removeAllFilesFromTable() {
		dataFileTable.removeAll();
		updateButtonState();
	}

	private void updateButtonState() {
		int numItems = dataFileTable.getItemCount();
		int numSelected = dataFileTable.getSelectionCount();
		int effectiveSelectionSize = (numSelected > 0) ? numSelected : numItems;
		plot1DButton.setEnabled(effectiveSelectionSize > 0);
		plotAgainstChannel.setEnabled(effectiveSelectionSize > 0);
		plot2D3DButton.setEnabled(effectiveSelectionSize > 1);
	}

	private static void ensureMythenPlotViewIsOpen() {
		showView(PLOT_VIEW_ID);
	}

	private static void showView(String id) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private static PlotView getPlotView() {
		return (PlotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PLOT_VIEW_ID);
	}

	private double[][][] getSelectedDataFromTable() {
		// Get the selected table items. If nothing is selected, we take that
		// to mean that *everything* is selected.
		TableItem[] tableItems = dataFileTable.getSelection();
		if (tableItems.length == 0) {
			tableItems = dataFileTable.getItems();
		}

		double[][][] selectedData = new double[tableItems.length][][];
		for (int i = 0; i < selectedData.length; i++) {
			selectedData[i] = (double[][]) tableItems[i].getData();
		}

		return selectedData;
	}

	@Override
	public void setFocus() {
	}

}
