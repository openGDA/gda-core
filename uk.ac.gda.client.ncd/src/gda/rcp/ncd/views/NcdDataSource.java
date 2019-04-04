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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.UnaryOperator;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.layout.GridDataFactory;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotViewStatsAndMaths;

/**
 * Panel that observes a PlotView to show additional information and tries to control the NcdListenerDispatcher to send
 * the desired updates.
 */
public class NcdDataSource extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(NcdDataSource.class);

	private static final String DISABLE_SYMMETRY = "Disable Symmetry";
	private static final String ENABLE_SYMMETRY = "Enable Symmetry";
	private static final String PLOT_ACTION = "symmetry_action";

	protected final Runnable noReflection = (Runnable)() -> reflectData(d -> d);

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

	protected String default_source;

	private DataBean originalData;

	private Spinner centreY;
	private Spinner centreX;

	private Button enableSymmetry;

	protected Optional<Runnable> reflection = Optional.empty();


	/**
	 * Controls the server side display of live or near live data and displays statistics
	 */
	public NcdDataSource() {
		panelId = "unknown";
		default_source = "nothing";
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
				pushToPlotView(dataBeanA, true);
			}
		});

		btnPlotAb = new Button(grpMaths, SWT.NONE);
		btnPlotAb.setText("Plot A-B");
		btnPlotAb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanSubtract(dataBeanA, dataBeanB), true);
			}
		});

		btnPlotAnB = new Button(grpMaths, SWT.NONE);
		btnPlotAnB.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
		btnPlotAnB.setText("Plot A + B");
		btnPlotAnB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanAdd(dataBeanA, dataBeanB), true);
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
				pushToPlotView(dataBeanB, true);
			}
		});

		btnPlotBa = new Button(grpMaths, SWT.NONE);
		btnPlotBa.setText("Plot B-A");
		btnPlotBa.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pushToPlotView(PlotViewStatsAndMaths.dataBeanSubtract(dataBeanB, dataBeanA), true);
			}
		});

		Group grpStatistics = new Group(parent, SWT.NONE);
		grpStatistics.setText("Statistics");
		grpStatistics.setLayout(new GridLayout(3, false));
		grpStatistics.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridDataFactory labelGdf = GridDataFactory.createFrom(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		GridDataFactory valueGdf = GridDataFactory.createFrom(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		valueGdf.hint(120, SWT.DEFAULT);

		Label spacer = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(spacer);
		Label txtValue = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtValue);
		txtValue.setAlignment(SWT.CENTER);
		txtValue.setText("value");

		Label txtPosition = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtPosition);
		txtPosition.setAlignment(SWT.CENTER);
		txtPosition.setText("position");

		Label txtMax = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtMax);
		txtMax.setAlignment(SWT.RIGHT);
		txtMax.setText("max");

		textMaxVal = new Text(grpStatistics, SWT.RIGHT);
		textMaxVal.setText("0");
		valueGdf.applyTo(textMaxVal);
		textMaxVal.setEditable(false);

		textMaxPos = new Text(grpStatistics, SWT.RIGHT);
		textMaxPos.setText("0");
		valueGdf.applyTo(textMaxPos);
		textMaxPos.setEditable(false);

		Label txtMin = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtMin);
		txtMin.setAlignment(SWT.RIGHT);
		txtMin.setText("min");

		textMinVal = new Text(grpStatistics, SWT.RIGHT);
		textMinVal.setText("0");
		valueGdf.applyTo(textMinVal);
		textMinVal.setEditable(false);

		textMinPos = new Text(grpStatistics, SWT.RIGHT);
		textMinPos.setText("0");
		valueGdf.applyTo(textMinPos);
		textMinPos.setEditable(false);

		Label txtSum = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtSum);
		txtSum.setAlignment(SWT.RIGHT);
		txtSum.setText("sum");

		textSum = new Text(grpStatistics, SWT.RIGHT);
		textSum.setText("0");
		valueGdf.applyTo(textSum);
		textSum.setEditable(false);
		valueGdf.applyTo(new Label(grpStatistics, SWT.NONE));

		Label txtMean = new Label(grpStatistics, SWT.NONE);
		labelGdf.applyTo(txtMean);
		txtMean.setAlignment(SWT.RIGHT);
		txtMean.setText("mean");

		textMean = new Text(grpStatistics, SWT.RIGHT);
		textMean.setText("0");
		valueGdf.applyTo(textMean);
		textMean.setEditable(false);
		valueGdf.applyTo(new Label(grpStatistics, SWT.NONE));

		enableDisableMaths();

		Group symmetryGroup = new Group(parent, SWT.NONE);
		symmetryGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		symmetryGroup.setText("Symmetry");
		symmetryGroup.setLayout(new GridLayout(2, false));

		enableSymmetry = new Button(symmetryGroup, SWT.TOGGLE);
		enableSymmetry.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		enableSymmetry.setText(ENABLE_SYMMETRY);
		enableSymmetry.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableSymmetry.setText(enableSymmetry.getSelection() ? DISABLE_SYMMETRY : ENABLE_SYMMETRY);
				runReflection();
			}
		});

		Label xLabel = new Label(symmetryGroup, SWT.NONE);
		xLabel.setText("Beam Centre X");
		xLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		centreX = new Spinner(symmetryGroup, SWT.NONE);
		centreX.setMaximum(Integer.MAX_VALUE);
		centreX.setMinimum(0);
		centreX.addModifyListener(e -> this.runReflection());

		Label yLabel = new Label(symmetryGroup, SWT.NONE);
		yLabel.setText("Beam Centre Y");
		yLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		centreY = new Spinner(symmetryGroup, SWT.NONE);
		centreY.setMaximum(Integer.MAX_VALUE);
		centreY.setMinimum(0);
		centreY.addModifyListener(e -> this.runReflection());

		Composite symmetryTypes = new Composite(symmetryGroup, SWT.NONE);
		symmetryTypes.setLayout(new GridLayout(3, false));
		symmetryTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button)e.getSource();
				if (source.getSelection()) {
					logger.debug("Selected: {}", e.getSource());
				}
				reflection = Optional.of((Runnable)source.getData(PLOT_ACTION));
				runReflection();
			}
		};

		Button twoFoldVertical = new Button(symmetryTypes, SWT.RADIO);
		twoFoldVertical.setText("2-fold Vertical");
		Runnable defaultReflection = (Runnable)() -> reflectData(d -> SymmetryUtil.reflectVertical(d, valueOf(centreX)));
		twoFoldVertical.setData(PLOT_ACTION, defaultReflection);
		twoFoldVertical.addSelectionListener(listener);
		twoFoldVertical.setSelection(true);
		// Setting selection programmatically here doesn't call the listener so set the reflection separately
		reflection = Optional.of(defaultReflection);

		Button twoFoldHorizontal = new Button(symmetryTypes, SWT.RADIO);
		twoFoldHorizontal.setText("2-fold Horizontal");
		twoFoldHorizontal.setData(PLOT_ACTION, (Runnable)() -> reflectData(d -> SymmetryUtil.reflectHorizontal(d, valueOf(centreY))));
		twoFoldHorizontal.addSelectionListener(listener);

		Button fourFold = new Button(symmetryTypes, SWT.RADIO);
		fourFold.setText("4-fold");
		fourFold.addSelectionListener(listener);
		fourFold.setData(PLOT_ACTION, (Runnable)() -> reflectData(d -> SymmetryUtil.reflectBoth(d, valueOf(centreX), valueOf(centreY))));
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
		String oldSelected = default_source;
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
				updateSourceCombo();
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
			originalData = (DataBean) changeCode;
			processData(currentBean);
			parentComp.getDisplay().asyncExec(this::runReflection);
		}
	}

	private void runReflection() {
		if (enableSymmetry.getSelection()) {
			reflection.ifPresent(Runnable::run);
		} else {
			noReflection.run();
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
				textMaxVal.setText(String.format(doubleFormat, d.max().doubleValue()));
				textMaxPos.setText(formatIntArray(d.maxPos()));
				textMinVal.setText(String.format(doubleFormat, d.min().doubleValue()));
				textMinPos.setText(formatIntArray(d.minPos()));
				textSum.setText(String.format(doubleFormat, ((Number)d.sum()).doubleValue()));
				textMean.setText(String.format(doubleFormat, ((Number)d.mean()).doubleValue()));
				int[] shape = originalData.getData().get(0).getData().getShape();
				centreX.setMaximum(shape[1]-1);
				centreY.setMaximum(shape[0]-1);
			}
		});
	}

	private String formatIntArray(int[] array) {
		if (array == null) {
			return "";
		}
		return Arrays.stream(array)
				.mapToObj(Integer::toString)
				.collect(joining(", "));
	}

	private void stopUpdates() {
		logger.debug("Stopped saxs updates");
		JythonServerFacade.getInstance().runCommand("finder.find('ncdlistener').monitorStop('" + plotPanelName + "')");
	}

	private void reflectData(UnaryOperator<Dataset> reflection) {
		if (originalData == null) {
			return;
		}
		Dataset data = originalData.getData().stream().findFirst().get().getData();
		Dataset newData = reflection.apply(data);
		newData.setName(data.getName());
		DatasetWithAxisInformation dwai = new DatasetWithAxisInformation();
		dwai.setData(newData);
		DataBean db = new DataBean();
		db.setData(Arrays.asList(dwai));
		db.setGuiPlotMode(GuiPlotMode.TWOD);
		pushToPlotView(db, false);
	}

	private void pushToPlotView(DataBean dBean, boolean pauseUpdates) {
		if (pauseUpdates) {
			stopUpdates();
		}
		if (plotView == null) {
			return;
		}

		plotView.processPlotUpdate(dBean, this);
		currentBean = dBean;
		processData(dBean);
	}

	private int valueOf(Spinner spinner) {
		return Integer.valueOf(spinner.getText());
	}
}
