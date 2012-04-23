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

package uk.ac.gda.client.microfocus.views.scan;

import gda.configuration.properties.LocalProperties;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.File;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.PrimitiveType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay2DConsumer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay2DProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.IImagePositionEvent;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.ui.experimentqueue.ControllerStatus;
import uk.ac.gda.client.experimentdefinition.ui.experimentqueue.ControllerStatusEvent;
import uk.ac.gda.client.experimentdefinition.ui.experimentqueue.ControllerStatusListener;
import uk.ac.gda.client.microfocus.controller.MicroFocusDisplayController;
import uk.ac.gda.client.microfocus.util.MicroFocusScanLoader;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.richbeans.xml.XMLEditorManager;

public class MicroFocusElementListView extends ViewPart implements Overlay2DConsumer, SelectionListener,
		ControllerStatusListener, FocusListener, IObservable

{
	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.gda.client.microfocus.ui.views.MicroFocusElementListView";
	private Overlay2DProvider provider;
	int boxPrimID = -1;
	private List xspressList;
	private MicroFocusDisplayController displayController;
	private MicroFocusScanLoader scanLoader;
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusElementListView.class);
	private FileDialog openDialog;
	private String detectorFileName;
	private String defaultDetectorFileName;
	private String loadedDetectorFileName;
	private final String detectorConfiguration = "uk.ac.gda.microfocus.display.detectorfile";
	private final String defaultDetectorConfiguration = "uk.ac.gda.microfocus.display.default.detectorfile";
	private double[] xyz = new double[3];
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean loadMapForScan = false;
	private int plotX = 0;
	private int plotY = 0;

	public boolean isLoadMapForScan() {
		return loadMapForScan;
	}

	public void toggleLoadMapForScan() {
		this.loadMapForScan = !loadMapForScan;
	}

	protected final IExperimentEditorManager controller = ExperimentFactory.getExperimentEditorManager();
	private Spinner elementSpinner;

	public MicroFocusElementListView() {
		super();
		displayController = new MicroFocusDisplayController();
		scanLoader = new MicroFocusScanLoader();
	}

	@Override
	public void createPartControl(Composite parent) {
		logger.info("Part Control the title is " + this.getTitle());
		Composite xspressComposite = new Composite(parent, SWT.NONE);
		xspressComposite.setLayout(new GridLayout(2, false));
		xspressList = new List(xspressComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		{

			{
				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);
				gridData.widthHint = 598;
				gridData.heightHint = 377;
				xspressList.setLayoutData(gridData);
			}
		}
		/*
		 * xspressLoad = new Button(xspressComposite, SWT.BORDER); xspressLoad.setText("Load File");
		 */
		getDetectorFiles();
		populateLists(detectorFileName, defaultDetectorFileName);
		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get("gda.data.scan.datawriter.datadir"));
		selectedElement = xspressList.getItem(0);
		xspressList.addSelectionListener(this);
		xspressList.setToolTipText(loadedDetectorFileName);
		Label elementLabel = new Label(xspressComposite, SWT.NONE);
		elementLabel.setText("Detector Element:");
		elementSpinner = new Spinner(xspressComposite, SWT.BORDER);
		{
			GridData gridData1 = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
			gridData1.widthHint = 22;
			gridData1.heightHint = 27;
			elementSpinner.setLayoutData(gridData1);
		}
		
		elementSpinner.setMinimum(0);
		populateElementSpinner(detectorFileName);
		elementSpinner.addSelectionListener(this);

		// xspressLoad.addSelectionListener(this);
		this.setTitleToolTip(getTitle() + "Dectector Elements list");

		try {
			// get an instance of the plotView we want to use
			IViewPart newview = getSite().getPage().showView("uk.ac.gda.beamline.i18.MapView", null,
					IWorkbenchPage.VIEW_CREATE);
			if (newview instanceof PlotView){
				plotView = (PlotView) newview;
	
				plotView.updatePlotMode(GuiPlotMode.TWOD);
	
				// then register this with the plot.
				plotView.getMainPlotter().registerOverlay(this);
				logger.info("The Plot View is " + plotView);
				ExperimentFactory.getScanController().addControllerStatusListener(this);
			}
			/*
			 * IPlotUI plotUI = plotView.getPlotUI(); if(plotUI instanceof Plot2DUI) { sidePlotView =
			 * ((Plot2DUI)plotUI).getSidePlotView(); }
			 */
		} catch (Exception e) {
			logger.error("Error while finding the plot view", e);
		}

	}

	private void getDetectorFiles() {
		defaultDetectorFileName = getFileName(defaultDetectorConfiguration, true);
		detectorFileName = getFileName(detectorConfiguration, false);
		displayController.setDetectorFile(detectorFileName);
		displayController.setDefaultDetectorFile(defaultDetectorFileName);
	}

	private String getFileName(String configuration, boolean defaultFile) {
		logger.info("the tilte of this view is " + getTitle());
		String fileName = "";
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(configuration);
		for (IConfigurationElement e : config) {

			fileName = e.getAttribute("path");
			logger.info("the mode is " + fileName);
			if (!fileName.contains(this.getTitle()) && !defaultFile)
				continue;
			if (fileName.contains("$")) {
				StringTokenizer tokens = new StringTokenizer(fileName, "/");
				StringBuffer buf = new StringBuffer();
				while (tokens.hasMoreElements()) {
					String tok = tokens.nextToken();
					if (tok.startsWith("$")) {
						tok = LocalProperties.get(tok.substring(1));
					}
					if (tokens.hasMoreElements())
						buf.append(tok + File.separator);
					else
						buf.append(tok);
				}
				fileName = buf.toString();
			}
			break;
		}
		return fileName;
	}

	private void populateLists(String xmlfile, String defaultXmlFile) {
		// Run command and block.
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		String result = jythonServerFacade.evaluateCommand("showElementsListString(\"" + xmlfile + "\")");
		if (result != null && !result.isEmpty()) {
			StringTokenizer s = new StringTokenizer(result.trim(), ",");
			if (xspressList.getItemCount() != 0) {
				xspressList.removeAll();
			}
			while (s.hasMoreElements()) {
				xspressList.add(new String(s.nextToken()));
			}
			// now add the default list
			String defaultsResult = jythonServerFacade.evaluateCommand("showElementsListString(\"" + defaultXmlFile
					+ "\")");
			if (defaultsResult != null && !defaultsResult.isEmpty()) {
				s = new StringTokenizer(defaultsResult.trim(), ",");
				while (s.hasMoreElements()) {
					xspressList.add(new String(s.nextToken()));
				}
			}
			xspressList.setSelection(0);
		}
		loadedDetectorFileName = xmlfile;
	}

	private void populateElementSpinner(String xmlfile) {
		// Run command and block.
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		String command = "getNumberOfDetectors(\"" + xmlfile + "\")";
		String reply = jythonServerFacade.evaluateCommand(command);
		elementSpinner.setMaximum(Integer.parseInt(reply) - 1);
	}

	public void refresh() {
		String currentDetectorFile = null;
		//if(!isScanRunning()){
			try {
				currentDetectorFile = findCurrentDetectorFile();
			} catch (Exception e) {
				logger.warn("Unable to refresh the elements list, using default config ");
			}
		if (currentDetectorFile != null)
				populateLists(currentDetectorFile, defaultDetectorFileName);
		//}
	
	}

	private boolean isScanRunning() {
		if(JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING)
		{
			//ExperimentFactory.getScanController().getCurrentScan().
			return true;
		}
		return false;
	}

	private String findCurrentDetectorFile() throws Exception {
		if (isScanRunning()) {
			ScanObject runningScan = (ScanObject) ExperimentFactory.getScanController().getCurrentScan();
			if (runningScan.isMicroFocus()) {
				String currentDetector = runningScan.getDetectorParameters().getFluorescenceParameters()
						.getConfigFileName();
				if (currentDetector.contains(this.getTitle())) {
					// find the full file path
					return controller.getSelectedFolder().findMember(currentDetector).getLocationURI().getPath();
				}
				return null;
			}
			return null;
		}
		{
			if (!((ScanObject) controller.getSelectedScan()).isMicroFocus())
				return null;
			String currentDetector = ((ScanObject) controller.getSelectedScan()).getDetectorParameters()
					.getFluorescenceParameters().getConfigFileName();
			if (currentDetector.contains(this.getTitle())) {
				// find the full file path
				return controller.getSelectedFolder().findMember(currentDetector).getLocationURI().getPath();

			}
			return null;
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private PlotView plotView;
	private String selectedElement;
	private boolean scanRunning;

	@Override
	public void hideOverlays() {
		System.out.println("Calling Hide");
		if (provider != null) {
			provider.setPrimitiveVisible(boxPrimID, false);
		}
	}

	@Override
	public void showOverlays() {
		System.out.println("Calling Show");
		if (provider != null) {
			boxPrimID = provider.registerPrimitive(PrimitiveType.BOX);
			provider.setPrimitiveVisible(boxPrimID, true);
		}
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider) provider;
		boxPrimID = provider.registerPrimitive(PrimitiveType.BOX);
		System.out.println("BoxID =" + boxPrimID);
	}

	@Override
	public void removePrimitives() {
		// provider.remove
		boxPrimID = -1;
	}

	@Override
	public void unregisterProvider() {

	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		logger.info("the image dragged click position is " + event.getImagePosition()[0] + " "
				+ event.getImagePosition()[1]);

	}

	@Override
	public void imageFinished(final IImagePositionEvent event) {
		logger.info("the image finished click position is " + event.getImagePosition()[0] + " "
				+ event.getImagePosition()[1]);

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					if (getViewSite().getPage().isPartVisible(MicroFocusElementListView.this)) {
						plotY = event.getImagePosition()[1];
						plotX = event.getImagePosition()[0];
						displayController.displayPlot(event.getImagePosition()[0], event.getImagePosition()[1]);
					}
				} catch (Exception e) {

					// TODO show error message
					logger.error("Error displaying the plot", e);
					e.printStackTrace();
					showErrorMessage("Error displaying the plot " + e.getMessage());
				}
			}

		});

	}

	public double[] getXYZ() {
		return xyz;
	}

	@Override
	public void imageStart(final IImagePositionEvent event) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					if (getViewSite().getPage().isPartVisible(MicroFocusElementListView.this)) {
						logger.info("the image start click position is " + event.getImagePosition()[0] + " "
								+ event.getImagePosition()[1]);

						if (event.getFlags() == IImagePositionEvent.RIGHTMOUSEBUTTON) {
							double xy[] = displayController.getXY(event.getImagePosition()[1],
									event.getImagePosition()[0]);
							if (xy != null) {
								logger.info("the image start click position x y z values are is " + xy[0] + " " + xy[1]
										+ " " + xy[2]);
								xyz = xy;
								notifyIObservers(this, xyz);

							}
						}
					}
				} catch (Exception e) {

					// TODO show error message
					logger.error("Error displaying the plot", e);
					e.printStackTrace();
					showErrorMessage("Error displaying the plot " + e.getMessage());
				}
			}

		});

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void loadFile() {

		final String filePath = openDialog.open();
		if (selectedElement != null && filePath != null) {
			final String msg = ("Loading map from " + filePath);
			Job job = new Job(msg) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						displayController.displayMap(selectedElement, filePath);
						if (loadMapForScan)
							scanLoader.loadMapXmlForScan(controller.getSelectedFolder(), filePath);
						/*else {
							final java.util.List<String> files = scanLoader.loadMapXmlForView(filePath);
							if (files.size() > 0) {
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										try {
											XMLEditorManager.openXmlEditorsFromStrings(files);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}

								});
							}
						}*/

					} catch (Exception e) {
						logger.error("Error displaying the map ", e);
						showErrorMessage("Error displaying the map " + e.getMessage());
					} catch (Throwable ne) {
						logger.error("Cannot open file " + filePath, ne);
					}
					return Status.OK_STATUS;
				}

			};
			job.setUser(true);
			job.schedule();
		}

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		selectedElement = xspressList.getSelection()[0];
		final String msg = "Loading the map for  " + selectedElement;
		if (e.getSource().equals(xspressList)) {
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {

					try {
						displayController.displayMap(selectedElement);
					} catch (Exception e1) {

						// TODO show error status
						logger.error("Error displaying the map ", e1);
						showErrorMessage("Error displaying the map " + e1.getMessage());
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		} else if (e.getSource().equals(elementSpinner)) {
			displayController.setDetectorElementNumber(elementSpinner.getSelection());
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						if (getViewSite().getPage().isPartVisible(MicroFocusElementListView.this)) {
							displayController.displayPlot(plotX, plotY);
						}
					} catch (Exception e) {

						// TODO show error message
						logger.error("Error displaying the plot", e);
						e.printStackTrace();
						showErrorMessage("Error displaying the plot " + e.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void dispose()

	{
		super.dispose();
		xspressList.dispose();
		// xspressLoad.dispose();
		plotView.dispose();

	}

	public void showErrorMessage(final String messag) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openError(null, "Error", messag);
			}

		});
	}

	@Override
	public void statusChangePerformed(ControllerStatusEvent evnt) {
		scanRunning = (evnt.getState() == ControllerStatus.PLAYING);
		if (scanRunning) {
			logger.info("the current scan is " + evnt.getCurrentRun().getRunName());
			displayController.disableProvider();

		}
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
	}

	/**
	 * Add an object to this objects's list of IObservers.
	 * 
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);

	}

	/**
	 * Delete an object from this objects's list of IObservers.
	 * 
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);

	}

	/**
	 * delete all IObservers from list of observing objects
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();

	}

	/**
	 * Notify all observers on the list of the requested change.
	 * 
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

}
