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

import gda.analysis.io.ScanFileHolderException;
import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.ByteArrayInputStream;

import org.dawnsci.plotting.jreality.overlay.Overlay2DConsumer;
import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;
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
import org.xml.sax.InputSource;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
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
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class MicroFocusElementListView extends ViewPart implements Overlay2DConsumer, SelectionListener, FocusListener,
		IObservable, IObserver {
	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.gda.client.microfocus.ElementListView";
	private Overlay2DProvider provider;
	int boxPrimID = -1;
	private List listOfElements;
	private MicroFocusDisplayController displayController;
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
	private XspressParameters xspressBean;
	private VortexParameters vortexBean;

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
		Scriptcontroller find = (Scriptcontroller) Finder.getInstance().find("elementListScriptController");
		find.addIObserver(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		logger.info("Part Control the title is " + this.getTitle());
		Composite xspressComposite = new Composite(parent, SWT.NONE);
		xspressComposite.setLayout(new GridLayout(2, false));
		listOfElements = new List(xspressComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);
		gridData.widthHint = 598;
		gridData.heightHint = 377;
		listOfElements.setLayoutData(gridData);

		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get("gda.data.scan.datawriter.datadir"));
		selectedElement = listOfElements.getItemCount() > 0 ? listOfElements.getItem(0) : null;
		listOfElements.addSelectionListener(this);
		listOfElements.setToolTipText(loadedDetectorFileName);

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

	private void populateLists(String xmlfile) {
		String location = xmlfile.substring(0, xmlfile.lastIndexOf('/')) + "/";
		String filename = xmlfile.substring(xmlfile.lastIndexOf('/') + 1);
		IRichBean beanObject = null;

		listOfElements.removeAll();

		try {
			beanObject = BeansFactory.getBeanObject(location, filename);
		} catch (Exception e) {
			logger.error("Could not create beans from xml file " + xmlfile, e);
		}

		if (beanObject instanceof XspressParameters) {
			XspressParameters xspress = (XspressParameters) beanObject;
			java.util.List<XspressROI> regionList = xspress.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				listOfElements.add(regionList.get(i).getRoiName());
			}
		}

		else if (beanObject instanceof VortexParameters) {
			VortexParameters vortex = (VortexParameters) beanObject;
			java.util.List<RegionOfInterest> regionList = vortex.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				listOfElements.add(regionList.get(i).getRoiName());
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
		HDF5Loader hdf5Loader = new HDF5Loader(filePath);

		HDF5File tree = null;
		try {
			tree = hdf5Loader.loadTree(null);
		} catch (ScanFileHolderException e2) {
			logger.error("Could not load tree from " + filePath, e2);
		}

		DataHolder dataHolder = null;
		try {
			dataHolder = hdf5Loader.loadFile();
		} catch (ScanFileHolderException e1) {
			logger.error("Could not load nexus file " + filePath, e1);
		}

		// get detector type xspress/vortex from nexus
		IMetaData metadata = dataHolder.getMetadata();

		String metaNames = null;
		try {
			metaNames = metadata.getMetaNames().toString();
		} catch (Exception e1) {
			logger.error("Cannot retreive metadata from nexus file " + filePath, e1);
		}

		if (metaNames.contains("xspress2system")) {
			HDF5NodeLink nl = tree.findNodeLink("/entry1/xml/XspressParameters");
			String xml = nl.toString();
			xml = xml.substring(xml.indexOf("<?xml"));
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			InputSource source = new InputSource(stream);

			try {
				xspressBean = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
						XspressParameters.class, XspressParameters.schemaURL, source);
			} catch (Exception e2) {
				logger.error("Could not create XspressParameters bean from nexus file", e2);
			}

			java.util.List<XspressROI> elementList = xspressBean.getDetector(0).getRegionList();
			int numElements = elementList.size();
			String[] elements = new String[numElements];

			listOfElements.removeAll();
			for (int i = 0; i < numElements; i++) {
				String roiName = elementList.get(i).getRoiName();
				elements[i] = roiName;
				listOfElements.add(roiName);
			}
			selectedElement = listOfElements.getItem(0);

			if (selectedElement != null && filePath != null) {
				final String msg = ("Loading map from " + filePath);
				Job job = new Job(msg) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							displayController.displayMap(
									xspressBean.getDetector(0).getRegionList().get(0).getRoiName(), filePath,
									xspressBean);
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

		if (metaNames.contains("xmapMca")) {
			HDF5NodeLink nl = tree.findNodeLink("/entry1/xml/VortexParameters");
			String xml = nl.toString();
			xml = xml.substring(xml.indexOf("<?xml"));
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			InputSource source = new InputSource(stream);

			try {
				vortexBean = (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL,
						VortexParameters.class, VortexParameters.schemaURL, source);
			} catch (Exception e2) {
				logger.error("Could not create VortexParameters bean from nexus file", e2);
			}

			java.util.List<RegionOfInterest> elementList = vortexBean.getDetector(0).getRegionList();
			int numElements = elementList.size();
			String[] elements = new String[numElements];

			listOfElements.removeAll();
			for (int i = 0; i < numElements; i++) {
				String roiName = elementList.get(i).getRoiName();
				elements[i] = roiName;
				listOfElements.add(roiName);
			}
			selectedElement = listOfElements.getItem(0);

			if (selectedElement != null && filePath != null) {
				final String msg = ("Loading map from " + filePath);
				Job job = new Job(msg) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							displayController.displayMap(vortexBean.getDetector(0).getRegionList().get(0).getRoiName(),
									filePath, vortexBean);
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

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		selectedElement = listOfElements.getSelection()[0];
		final String msg = "Loading the map for  " + selectedElement;
		if (e.getSource().equals(listOfElements)) {
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
		listOfElements.dispose();
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
				displayController.setDetectorFile(detectorConfig);
			}
		});

	}
}
