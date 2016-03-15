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

package gda.rcp.ncd.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotViewStatsAndMaths;

/**
 * Panel that observes a PlotView to show additional information and tries to control the Ncd ListenerDispatcher to send
 * the desired updates.
 */
public class NcdDataSource extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(NcdDataSource.class);

	private Composite parentComp;
	protected String panelId;
	private String plotPanelName = "Saxs Plot";
	private DataBean dataBeanA, dataBeanB, currentBean;

	private PlotView plotView;
	private Text textMaxVal;
	private Text textMinVal;
	private Text textSum;
	private Text textMean;
	private Text textMaxPos;
	private Text textMinPos;

	private Button btnPlotA;
	private Button btnPlotAb;
	private Button btnPlotB;
	private Button btnPlotBa;
	private Button btnPlotAnB;
	private Button btnStoreCurrentAsA;
	private Button btnStoreCurrentAsB;

	private String doubleFormat = "%5.5g";
	private Combo combo;
	private Button btnStopUpdates;
	private Button btnStartUpdates;

	/**
	 * Controls the server side display of live or near live data and displays statistics
	 */
	public NcdDataSource() {
		panelId = "unknown";
	}

	@Override
	public void createPartControl(Composite parent) {
		// check if Plot View is open
		try {
			plotView = (PlotView) getSite().getPage().showView(panelId);
			plotView.addDataObserver(this);
			plotPanelName = plotView.getPlotViewName();

		} catch (PartInitException e) {
			logger.error("All over now! Cannot find plotview: " + plotPanelName, e);
		}

		GridLayout layout= new GridLayout();

		parent.setLayout(layout);
		parentComp = parent;

		Group grpSource = new Group(parent, SWT.NONE);
		grpSource.setText("Source for "+plotPanelName);
		grpSource.setLayout(new GridLayout(2, false));
		grpSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnStopUpdates = new Button(grpSource, SWT.NONE);
		btnStopUpdates.setText("Stop Updates");
		btnStopUpdates.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopUpdates();
			}
		});

		btnStartUpdates = new Button(grpSource, SWT.NONE);
		btnStartUpdates.setText("Start Updates");
		btnStartUpdates.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSourceCombo();
			}
		});

		combo = new Combo(grpSource, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		populateSourceCombo();
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSourceCombo();
				populateSourceCombo();
			}
		});

		if (combo.getItemCount() > 0) {
			combo.select(0);
			updateSourceCombo();
		}
		Group grpMaths = new Group(parent, SWT.NONE);
		grpMaths.setText("Maths");
		grpMaths.setLayout(new GridLayout(4, false));
		grpMaths.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnStoreCurrentAsA = new Button(grpMaths, SWT.NONE);
		btnStoreCurrentAsA.setText("Store current as A");
		btnStoreCurrentAsA.setEnabled(false);
		btnStoreCurrentAsA.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataBeanA = currentBean;
				enableDisableMaths();
			}
		});

		btnPlotA = new Button(grpMaths, SWT.NONE);
		btnPlotA.setText("Plot A");
		btnPlotA.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(dataBeanA);
			}
		});

		btnPlotAb = new Button(grpMaths, SWT.NONE);
		btnPlotAb.setText("Plot A-B");
		btnPlotAb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanSubtract(dataBeanA, dataBeanB));
			}
		});

		btnPlotAnB = new Button(grpMaths, SWT.NONE);
		btnPlotAnB.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		btnPlotAnB.setText("Plot A + B");
		btnPlotAnB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanAdd(dataBeanA, dataBeanB));
			}
		});

		btnStoreCurrentAsB = new Button(grpMaths, SWT.NONE);
		btnStoreCurrentAsB.setText("Store current as B");
		btnStoreCurrentAsB.setEnabled(false);
		btnStoreCurrentAsB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataBeanB = currentBean;
				enableDisableMaths();
			}
		});

		btnPlotB = new Button(grpMaths, SWT.NONE);
		btnPlotB.setText("Plot B");
		btnPlotB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(dataBeanB);
			}
		});

		btnPlotBa = new Button(grpMaths, SWT.NONE);
		btnPlotBa.setText("Plot B-A");
		btnPlotBa.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanSubtract(dataBeanB, dataBeanA));
			}
		});

		Group grpStatistics = new Group(parent, SWT.NONE);
		grpStatistics.setText("Statistics");
		grpStatistics.setLayout(new GridLayout(3, false));
		grpStatistics.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label txtValue = new Label(grpStatistics, SWT.NONE);
		txtValue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		txtValue.setAlignment(SWT.CENTER);
		txtValue.setText("value");

		Label txtPosition = new Label(grpStatistics, SWT.NONE);
		txtPosition.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		txtPosition.setAlignment(SWT.CENTER);
		txtPosition.setText("position");

		Label txtMax = new Label(grpStatistics, SWT.NONE);
		txtMax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtMax.setAlignment(SWT.RIGHT);
		txtMax.setText("max");

		textMaxVal = new Text(grpStatistics, SWT.RIGHT);
		textMaxVal.setText("0");
		GridData gd_textMaxVal = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textMaxVal.widthHint = 120;
		textMaxVal.setLayoutData(gd_textMaxVal);
		textMaxVal.setEditable(false);

		textMaxPos = new Text(grpStatistics, SWT.RIGHT);
		textMaxPos.setText("0");
		GridData gd_textMaxPos = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textMaxPos.widthHint = 120;
		textMaxPos.setLayoutData(gd_textMaxPos);
		textMaxPos.setEditable(false);

		Label txtMin = new Label(grpStatistics, SWT.NONE);
		txtMin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtMin.setAlignment(SWT.RIGHT);
		txtMin.setText("min");

		textMinVal = new Text(grpStatistics, SWT.RIGHT);
		textMinVal.setText("0");
		GridData gd_textMinVal = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textMinVal.widthHint = 120;
		textMinVal.setLayoutData(gd_textMinVal);
		textMinVal.setEditable(false);

		textMinPos = new Text(grpStatistics, SWT.RIGHT);
		textMinPos.setText("0");
		GridData gd_textMinPos = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textMinPos.widthHint = 120;
		textMinPos.setLayoutData(gd_textMinPos);
		textMinPos.setEditable(false);

		Label txtSum = new Label(grpStatistics, SWT.NONE);
		txtSum.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtSum.setAlignment(SWT.RIGHT);
		txtSum.setText("sum");

		textSum = new Text(grpStatistics, SWT.RIGHT);
		textSum.setText("0");
		GridData gd_textSum = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textSum.widthHint = 120;
		textSum.setLayoutData(gd_textSum);
		textSum.setEditable(false);

		Label txtMean = new Label(grpStatistics, SWT.NONE);
		txtMean.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtMean.setAlignment(SWT.RIGHT);
		txtMean.setText("mean");

		textMean = new Text(grpStatistics, SWT.RIGHT);
		textMean.setText("0");
		GridData gd_textMean = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_textMean.widthHint = 120;
		textMean.setLayoutData(gd_textMean);
		textMean.setEditable(false);

		enableDisableMaths();
	}

	protected void updateSourceCombo() {
		int selected = combo.getSelectionIndex();
		String selstr = combo.getItem(selected);
		if (selstr.startsWith("Live ")) {
			JythonServerFacade.getInstance().runCommand("finder.find(\"ncdlistener\").monitorLive(\"" + plotPanelName + "\",\"" + selstr.substring(5) + "\")");
		} else if (selstr.startsWith("SDP ")) {
			JythonServerFacade.getInstance().runCommand("finder.find(\"ncdlistener\").monitorSDP(\"" + plotPanelName + "\",\"" + selstr.substring(4) + "\")");
		}
	}

	protected void populateSourceCombo() {
		String oldSelected = "nothing";
		try {
			oldSelected = combo.getItem(combo.getSelectionIndex());
		} catch(Exception ignored) {

		}
		combo.removeAll();

		List<String> items = new ArrayList<String>();
		items.add("Live SAXS");
		items.add("Live WAXS");
		String names = JythonServerFacade.getInstance().evaluateCommand("finder.find('ncdlistener').getSDPDetectorNamesAsString()");
		if (names != null) {
		StringTokenizer st = new StringTokenizer(names, ",");
		while(st.hasMoreTokens()) {
			items.add("SDP "+st.nextToken());
		}
		}
		for(String thing: items) {
			combo.add(thing);
			if (thing.equals(oldSelected)) {
				combo.select(combo.getItemCount()-1);
			}
		}
	}

	protected void enableDisableMaths() {
		btnPlotA.setEnabled(dataBeanA != null);
		btnPlotB.setEnabled(dataBeanB != null);

		boolean samerank = false;
		if (dataBeanA != null && dataBeanB != null) {
			int[] dimA = dataBeanA.getData().get(0).getData().getShape();
			int[] dimB = dataBeanB.getData().get(0).getData().getShape();
			samerank = Arrays.equals(dimA, dimB);
		}
		btnPlotAb.setEnabled(samerank);
		btnPlotBa.setEnabled(samerank);
		btnPlotAnB.setEnabled(samerank);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (parentComp.isDisposed()) {
			return;
		}

		if (changeCode instanceof DataBean) {
			currentBean = (DataBean) changeCode;
			processData(currentBean);
		}
	}

	private void processData(DataBean bean) {
		// do stuff with new data
		List<DatasetWithAxisInformation> dc = bean.getData();
		final Dataset d = dc.get(0).getData();
		parentComp.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				btnStoreCurrentAsA.setEnabled(true);
				btnStoreCurrentAsB.setEnabled(true);
				textMaxVal.setText(String.format(doubleFormat, d.max()));
				textMaxPos.setText(formatIntArray(d.maxPos()));
				textMinVal.setText(String.format(doubleFormat, d.min()));
				textMinPos.setText(formatIntArray(d.minPos()));
				textSum.setText(String.format(doubleFormat, d.sum()));
				textMean.setText(String.format(doubleFormat, d.mean()));
			}
		});
	}

	private String formatIntArray(int[] array) {
		if (array == null || array.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(String.format("%d", array[0]));
		for (int i = 1; i < array.length; i++) {
			sb.append(String.format(", %d", array[array.length - i]));
		}
		return sb.toString();
	}

	private void stopUpdates() {
		logger.debug("Stopped saxs updates");
		JythonServerFacade.getInstance().runCommand("finder.find('ncdlistener').monitorStop('" + plotPanelName + "')");
	}

	private void pushToPlotView(DataBean dBean) {
		stopUpdates();
		if (plotView == null) {
			return;
		}

		plotView.processPlotUpdate(dBean, this);
		currentBean = dBean;
		processData(dBean);
	}
}
