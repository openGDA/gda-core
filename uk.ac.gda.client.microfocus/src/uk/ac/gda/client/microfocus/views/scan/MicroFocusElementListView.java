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
import gda.data.PathConstructor;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
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
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.microfocus.controller.MicroFocusDisplayController;
import uk.ac.gda.client.microfocus.util.MicroFocusScanLoader;

public class MicroFocusElementListView extends ViewPart implements Overlay2DConsumer, SelectionListener, FocusListener,
		IObservable, IObserver {
	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.gda.client.microfocus.ElementListView";
	private Overlay2DProvider provider;
	int boxPrimID = -1;
	private List xspressList;
	private MicroFocusDisplayController displayController;
	private MicroFocusScanLoader scanLoader;
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusElementListView.class);
	private FileDialog openDialog;
	private String loadedDetectorFileName;
	private double[] xyz = new double[3];
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean loadMapForScan = false;
	private int plotX = 0;
	private int plotY = 0;
	private PlotView plotView;
	private String selectedElement;

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
		Scriptcontroller find = (Scriptcontroller) Finder.getInstance().find("elementListScriptController");
		find.addIObserver(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		logger.info("Part Control the title is " + this.getTitle());
		Composite xspressComposite = new Composite(parent, SWT.NONE);
		xspressComposite.setLayout(new GridLayout(2, false));
		xspressList = new List(xspressComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);
		gridData.widthHint = 598;
		gridData.heightHint = 377;
		xspressList.setLayoutData(gridData);

		String configDir = PathConstructor.createFromProperty("gda.params");

		setDefaultDetectorFile(configDir + "/templates/Xspress_Parameters.xml");
		setDetectorFile(configDir + "/templates/Xspress_Parameters.xml");

		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get("gda.data.scan.datawriter.datadir"));
		selectedElement = xspressList.getItemCount() > 0 ? xspressList.getItem(0) : null;
		xspressList.addSelectionListener(this);
		xspressList.setToolTipText(loadedDetectorFileName);

		this.setTitleToolTip(getTitle() + "Dectector Elements list");

		try {
			// get an instance of the plotView we want to use
			IViewPart newview = getSite().getPage().showView("uk.ac.gda.beamline.i18.MapView", null,
					IWorkbenchPage.VIEW_CREATE);
			if (newview instanceof PlotView) {
				plotView = (PlotView) newview;
				plotView.updatePlotMode(GuiPlotMode.TWOD);
				// then register this with the plot.
				plotView.getMainPlotter().registerOverlay(this);
				logger.info("The Plot View is " + plotView);
			}
		} catch (Exception e) {
			logger.error("Error while finding the plot view", e);
		}
	}

	private void setDefaultDetectorFile(String path) {
		displayController.setDefaultDetectorFile(path);
	}

	private void setDetectorFile(String path) {
		displayController.setDetectorFile(path);
	}

	private void populateLists(String xmlfile) {
		String location = xmlfile.substring(0, xmlfile.lastIndexOf('/')) + "/";
		String filename = xmlfile.substring(xmlfile.lastIndexOf('/') + 1);
		IRichBean beanObject = null;

		xspressList.removeAll();

		try {
			beanObject = BeansFactory.getBeanObject(location, filename);
		} catch (Exception e) {
			logger.error("Could not create beans from xml file " + xmlfile, e);
		}

		if (beanObject instanceof XspressParameters) {
			XspressParameters xspress = (XspressParameters) beanObject;
			java.util.List<XspressROI> regionList = xspress.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				xspressList.add(regionList.get(i).getRoiName());
			}
		}

		else if (beanObject instanceof VortexParameters) {
			VortexParameters vortex = (VortexParameters) beanObject;
			java.util.List<RegionOfInterest> regionList = vortex.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				xspressList.add(regionList.get(i).getRoiName());
			}
		}

		loadedDetectorFileName = xmlfile;
	}

	@Override
	public void setFocus() {
	}

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
						logger.error("Error displaying the plot", e);
						e.printStackTrace();
						showErrorMessage("Error displaying the plot " + e.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		xspressList.dispose();
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

	@Override
	public void update(Object source, Object arg) {
		final String detectorConfig = (String) arg;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				populateLists(detectorConfig);
			}
		});

	}
}
