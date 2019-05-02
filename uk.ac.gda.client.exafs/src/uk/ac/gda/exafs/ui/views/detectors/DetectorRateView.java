
/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
package uk.ac.gda.exafs.ui.views.detectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProviderInterface;
import gda.factory.Finder;
import gda.jython.IScanDataPointObserver;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent;

public class DetectorRateView extends ViewPart implements IPartListener2, IObserver, IScanDataPointObserver {
	protected static final Logger logger = LoggerFactory.getLogger(DetectorRateView.class);
	public static final String ID = "uk.ac.gda.exafs.ui.views.detectors.DetectorRateView";

	/** View configuration - set by {@link DetectorRateViewFactory} when creating view, or selected by user in GUI */
	private DetectorRateViewConfig viewConfig = null;

	/** collection time in seconds */
	private double collectionTime = 1.0;

	private DetectorMonitorDataProviderInterface dataProvider = null;
	private List<String> columnNames = new ArrayList<>();
	private Table table;
	private Button startButton;
	private Button stopButton;

	private volatile boolean stopCollection = true;
	private volatile boolean collectionIsRunning = false;

	private Executor threadExecutor = Executors.newSingleThreadExecutor();

	public DetectorRateView() {
		super();
	}
	@Override
	public void init(IViewSite site) throws PartInitException {
		JythonServerFacade.getInstance().addScanEventObserver(this);
		JythonServerFacade.getInstance().addIScanDataPointObserver(this);
		super.init(site);
	}

	/**
	 * Show a dialog box for user to select a detector rate view from one of the ones available locally.
	 * @param parent
	 * @return Selected view config, or null if none have been selected or none are available.
	 */
	public DetectorRateViewConfig askUserToSelectViewConfig(Composite parent) {
		Map<String, DetectorRateViewConfig> rateViewConfigObjects = Finder.getInstance().getLocalFindablesOfType(DetectorRateViewConfig.class);

		if (rateViewConfigObjects.isEmpty()) {
			MessageDialog.openInformation(parent.getShell(), "Problem opening detector rates",
					"Cannot open detector rate view - no detector rate configuration were found."+
					"Please contact your GDA support representative if this is a problem.");
			return null;
		}

		ListDialog listDialog = new ListDialog(parent.getShell());
		listDialog.setTitle("Select detector rates view to open");
		listDialog.setContentProvider(new ArrayContentProvider());
		listDialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DetectorRateViewConfig) {
					return ((DetectorRateViewConfig)element).getViewDescription();
				}
				return "";
			}
		});
		listDialog.setInput(rateViewConfigObjects.values());
		listDialog.setBlockOnOpen(true);
		if (listDialog.open() != Window.OK) {
			return null;
		}

		return (DetectorRateViewConfig) listDialog.getResult()[0];
	}

	@Override
	public void createPartControl(Composite parent) {

		// Present list of available view configurations for user to choose from
		if (viewConfig == null) {
			viewConfig = askUserToSelectViewConfig(parent);
		}

		// Can't create view without a viewConfig!
		if (viewConfig == null) {
			logger.info("No view configuration has been set - view will not be created");
			return;
		}

		dataProvider = viewConfig.getDataProvider();
		collectionTime = viewConfig.getCollectionTime();
		setupGui(parent);
	}

	private void setupGui(Composite parent) {
		// Set the title for the view
		setPartName(viewConfig.getViewDescription());
		// Setup main group
		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());
		// Get the column names by looking up extraNames for each detector/scannable
		columnNames = dataProvider.getOutputFields(viewConfig.getDetectorNames());
		// Setup table used for data and label the columns
		createTable(grpCurrentCountRates);
		createControlButtons(grpCurrentCountRates);

		// Add listener to stop detector rates on server when view is disposed
		// Doing this in dispose() does not work when collection is running (hangs the client...).
		parent.addDisposeListener(event -> {
			logger.info("Dispose listener called");
			forceStop();
		});
	}

	/**
	 * Create new Table to put the detector rates; set the column titles in first row of table.
	 * @param parent
	 */
	protected void createTable(Composite parent) {
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.NO_FOCUS);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setItemCount(1);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 40;
		table.setLayoutData(gd);
		final List<TableColumn> columns = new ArrayList<>();
		for(String columnName : columnNames) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(columnName);
			column.setAlignment(SWT.CENTER);
			columns.add(column);
		}
		for (int i = 0; i < columns.size(); i++) {
			table.getItem(0).setText(i, "                 "); // this string helps set the default width of the column
		}
		for (int i = 0; i < columns.size(); i++) {
			table.getColumn(i).pack();
		}
	}
	/**
	 * Create GUI controls for starting and stopping detector rate collection.
	 * @param parent
	 */
	protected void createControlButtons(Composite parent) {
		Composite group = new Group(parent, SWT.DEFAULT);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		startButton = new Button(group, SWT.PUSH);
		startButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));
		startButton.setText("Start");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//start the collection thread, only if not already running
				if (!collectionIsRunning) {
					threadExecutor.execute(DetectorRateView.this::runCollection);
				}
			}
		});
		// Disable start button if scan or script are currently running
		startButton.setEnabled(!dataProvider.isScriptOrScanIsRunning());

		stopButton = new Button(group, SWT.PUSH);
		stopButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));
		stopButton.setText("Stop");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopCollection = true;
			}
		});
		stopButton.setEnabled(false);
	}

	private synchronized void runCollection() {
		try {
			if (dataProvider.getCollectionIsRunning()) {
				logger.info("Collection is already running in another thread");
				return;
			}

			setEnabled(startButton, false);
			setEnabled(stopButton, true);

			collectionIsRunning = true;
			stopCollection = false;

			dataProvider.setNumberFormat(viewConfig.getNumberFormatMap());
			dataProvider.setCollectionTime(collectionTime);
			while(!stopCollection && !dataProvider.isScriptOrScanIsRunning() && dataProvider.getCollectionAllowed()) {
				logger.info("Collect frame of data");
				List<String> dataValues = dataProvider.collectData(viewConfig.getDetectorNames());
				updateTable(dataValues);
			}
		} catch (Exception e) {
			logger.error("Problem running scan for detector rates view {}", viewConfig.getName(), e);
		}
		logger.info("Detector rate collection finished");
		collectionIsRunning = false;
		setEnabled(startButton, true);
		setEnabled(stopButton, false);
	}

	/**
	 * Update the table contents using values from a list of strings
	 * @param values
	 */
	protected void updateTable(List<String> values) {
		for(int i=0; i<values.size(); i++) {
			final int index = i;
			PlatformUI.getWorkbench().getDisplay().asyncExec( () -> table.getItem(0).setText(index, values.get(index)) );
		}
	}

	@Override
	public void update(final Object source, final Object arg) {
		if (arg instanceof ScanEvent) {
			// get scan status, update start, stop buttons if necessary
			ScanEvent event = (ScanEvent) arg;
			ScanStatus status = event.getLatestStatus();
			logger.debug("ScanStatus : {}", status.toString());
			if (status.isRunning()) {
				stopCollection = false;
				setEnabled(startButton, false);
				setEnabled(stopButton, false); // only enable stop button for view that shows current data
			} else if (status.isComplete() || status.isAborting()) {
				stopCollection = true;
				setEnabled(startButton, true);
				setEnabled(stopButton, false);
			}
		}
	}

	private void setEnabled(Control controlWidget, boolean enabled) {
		if (!controlWidget.isDisposed()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> controlWidget.setEnabled(enabled));
		}
	}

	public void setViewConfig(DetectorRateViewConfig viewConfig) {
		this.viewConfig = viewConfig;
	}

	/**
	 * Stop detector collection after frame currently being collected finishes.
	 */
	private void forceStop() {
		logger.debug("forceStop() called");
		if (collectionIsRunning) {
			try {
				logger.debug("Stopping detector rate on server");
				stopCollection = true; // notify client side collection loop to stop
				dataProvider.stop(); // wait for frame currently being collected on server to stop.
			} catch (DeviceException e) {
				logger.error("Problem waiting for detector rates to finish", e);
			}
		}
		logger.info("forceStop() finished");
	}

	@Override
	public void dispose() {
		logger.debug("dispose() called for '{}'", viewConfig.getViewDescription());
		forceStop();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// No implementation required
	}
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// No implementation required
	}
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// No implementation required
	}
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// No implementation required
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// No implementation required
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// No implementation required
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// No implementation required
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// No implementation required
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// No implementation required
	}
}