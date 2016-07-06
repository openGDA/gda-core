/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.microfocus.controller.MicroFocusDisplayController;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class MicroFocusElementListView extends ViewPart implements SelectionListener, IObservable, IObserver {

	public static final String ID = "uk.ac.gda.client.microfocus.ElementListView";
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusElementListView.class);

	protected final IExperimentEditorManager controller = ExperimentFactory.getExperimentEditorManager();

	private Combo cmbChannelChoice;
	private List elementList;
	private MicroFocusDisplayController displayController;
	private FileDialog openDialog;
	private String loadedDetectorFileName;
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean loadMapForScan = false;
	private XspressParameters xspressBean;
	private VortexParameters vortexBean;
	private Xspress3Parameters xspress3Bean;
	private double pointX;
	private double pointY;

	public MicroFocusElementListView() {
		super();
		displayController = new MicroFocusDisplayController();
		Scriptcontroller find = (Scriptcontroller) Finder.getInstance().find("elementListScriptController");
		find.addIObserver(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite xspressComposite = new Composite(parent, SWT.NONE);
		xspressComposite.setLayout(new GridLayout(2, false));

		Label lblCombo = new Label(xspressComposite, SWT.NONE);
		lblCombo.setText("Detector Channel:");
		GridDataFactory.swtDefaults().applyTo(lblCombo);
		cmbChannelChoice = new Combo(xspressComposite, SWT.NONE);
		cmbChannelChoice.setItems(new String[] { "0" });
		cmbChannelChoice.select(0);
		cmbChannelChoice.addSelectionListener(this);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(cmbChannelChoice);

		elementList = new List(xspressComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		elementList.addSelectionListener(this);
		elementList.setToolTipText(loadedDetectorFileName);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).hint(598, 377).applyTo(elementList);

		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.getBaseDataDir());

		this.setTitleToolTip("Choose detector channels and element ROIs to plot");
	}

	private void populateLists(String xmlfile) {
		String location = xmlfile.substring(0, xmlfile.lastIndexOf('/')) + "/";
		String filename = xmlfile.substring(xmlfile.lastIndexOf('/') + 1);
		XMLRichBean beanObject = null;

		elementList.removeAll();

		try {
			beanObject = XMLHelpers.getBeanObject(location, filename);
		} catch (Exception e) {
			logger.error("Could not create beans from xml file " + xmlfile, e);
		}

		if (beanObject instanceof XspressParameters) {
			XspressParameters xspress = (XspressParameters) beanObject;
			updateDetectorChannelCombo(xspress.getDetectorList().size());
			java.util.List<DetectorROI> regionList = xspress.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				elementList.add(regionList.get(i).getRoiName());
			}
		} else if (beanObject instanceof Xspress3Parameters) {
			Xspress3Parameters vortex = (Xspress3Parameters) beanObject;
			updateDetectorChannelCombo(vortex.getDetectorList().size());
			java.util.List<DetectorROI> regionList = vortex.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				elementList.add(regionList.get(i).getRoiName());
			}
		}

		else if (beanObject instanceof VortexParameters) {
			VortexParameters vortex = (VortexParameters) beanObject;
			updateDetectorChannelCombo(vortex.getDetectorList().size());
			java.util.List<DetectorROI> regionList = vortex.getDetector(0).getRegionList();
			for (int i = 0; i < regionList.size(); i++) {
				elementList.add(regionList.get(i).getRoiName());
			}
		}

		elementList.add("I0");
		elementList.add("It");

		loadedDetectorFileName = xmlfile;
	}

	private void updateDetectorChannelCombo(int size) {
		String[] channels = new String[size];
		for (int i = 0; i < size; i++) {
			channels[i] = Integer.toString(i);
		}
		int currentSelection = cmbChannelChoice.getSelectionIndex();
		cmbChannelChoice.setItems(channels);
		if (currentSelection < size) {
			cmbChannelChoice.select(currentSelection);
		} else {
			cmbChannelChoice.select(0);
		}
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void loadXspressNexus(Tree tree, final String filePath) {
		NodeLink nl = tree.findNodeLink("/entry1/xml/DetectorConfigurationParameters");
		if (nl == null) {
			nl = tree.findNodeLink("/entry1/before_scan/DetectorConfigurationParameters");
		}
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

		java.util.List<DetectorROI> regionList = xspressBean.getDetector(0).getRegionList();
		int numElements = regionList.size();
		String[] elements = new String[numElements];

		int numChannels = xspressBean.getDetectorList().size();
		updateNumberDetectorChannels(numChannels);

		elementList.removeAll();
		for (int i = 0; i < numElements; i++) {
			String roiName = regionList.get(i).getRoiName();
			elements[i] = roiName;
			elementList.add(roiName);
		}

		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			final Integer selectedChannel = getSelectedChannel();
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						displayController.displayMap(xspressBean.getDetector(0).getRegionList().get(0).getRoiName(),
								filePath, xspressBean, selectedChannel);
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

	private void updateNumberDetectorChannels(final int numChannels) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				String[] array = new String[numChannels];
				for (int i = 0; i < numChannels; i++) {
					array[i] = Integer.toString(i);
				}
				cmbChannelChoice.setItems(array);
				cmbChannelChoice.select(0);
			}
		});
	}

	public void loadXmapNexus(Tree tree, final String filePath) {
		NodeLink nl = tree.findNodeLink("/entry1/xml/DetectorConfigurationParameters");
		if (nl == null) {
			nl = tree.findNodeLink("/entry1/before_scan/DetectorConfigurationParameters");
		}
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

		java.util.List<DetectorROI> regionList = vortexBean.getDetector(0).getRegionList();
		int numElements = regionList.size();
		String[] elements = new String[numElements];

		int numChannels = vortexBean.getDetectorList().size();
		updateNumberDetectorChannels(numChannels);

		elementList.removeAll();
		for (int i = 0; i < numElements; i++) {
			String roiName = regionList.get(i).getRoiName();
			elements[i] = roiName;
			elementList.add(roiName);
		}

		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			final Integer selectedChannel = getSelectedChannel();
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						displayController.displayMap(vortexBean.getDetector(0).getRegionList().get(0).getRoiName(),
								filePath, vortexBean, selectedChannel);
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

	public void loadFile() {

		final String filePath = openDialog.open();
		HDF5Loader hdf5Loader = new HDF5Loader(filePath);

		Tree tree = null;
		try {
			tree = hdf5Loader.loadTree(null);
		} catch (ScanFileHolderException e2) {
			logger.error("Could not load tree from " + filePath, e2);
			return;
		}

		DataHolder dataHolder = null;
		try {
			dataHolder = hdf5Loader.loadFile();
		} catch (ScanFileHolderException e1) {
			logger.error("Could not load nexus file " + filePath, e1);
			return;
		}

		// get detector type xspress/vortex from nexus
		IMetadata metadata = dataHolder.getMetadata();

		elementList.removeAll();

		try {
			String metaNames = metadata.getMetaNames().toString();
			if (metaNames.contains("xspress3")) {
				loadXspress3Nexus(tree, filePath);
			} else if (metaNames.contains("raster_xspress3")) {
				loadXspress3Nexus(tree, filePath);
			} else if (metaNames.contains("xspress2system")) {
				loadXspressNexus(tree, filePath);
			} else if (metaNames.contains("raster_xspress")) {
				loadXspressNexus(tree, filePath);
			} else if (metaNames.contains("xmapMca")) {
				loadXmapNexus(tree, filePath);
			}

		} catch (Exception e) {
			logger.error("Cannot retreive metadata from nexus file " + filePath, e);
			return;
		}

		elementList.add("I0");
		elementList.add("It");
	}

	private void loadXspress3Nexus(final Tree tree, final String filePath) {

		NodeLink nl = tree.findNodeLink("/entry1/xml/DetectorConfigurationParameters");
		if (nl == null) {
			nl = tree.findNodeLink("/entry1/before_scan/DetectorConfigurationParameters");
		}
		String xml = nl.toString();
		xml = xml.substring(xml.indexOf("<?xml"));
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
		InputSource source = new InputSource(stream);

		try {
			xspress3Bean = (Xspress3Parameters) XMLHelpers.createFromXML(Xspress3Parameters.mappingURL,
					Xspress3Parameters.class, Xspress3Parameters.schemaURL, source);
		} catch (Exception e2) {
			logger.error("Could not create Xspress3Parameters bean from nexus file", e2);
		}

		java.util.List<DetectorROI> regionList = xspress3Bean.getDetector(0).getRegionList();
		int numElements = regionList.size();
		String[] elements = new String[numElements];

		int numChannels = xspress3Bean.getDetectorList().size();
		updateNumberDetectorChannels(numChannels);

		elementList.removeAll();
		for (int i = 0; i < numElements; i++) {
			String roiName = regionList.get(i).getRoiName();
			elements[i] = roiName;
			elementList.add(roiName);
		}

		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			final Integer selectedChannel = getSelectedChannel();
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						displayController.displayMap(xspress3Bean.getDetector(0).getRegionList().get(0).getRoiName(),
								filePath, xspress3Bean, selectedChannel);
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
		final String selectedElement = elementList.getSelection()[0];
		final Integer selectedChannel = getSelectedChannel();
		final String msg = "Loading the map for  " + selectedElement + " channel " + selectedChannel;
		Job job = new Job(msg) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					displayController.displayMap(selectedElement, selectedChannel);
				} catch (Exception e1) {
					logger.error("Error displaying the map ", e1);
					showErrorMessage("Error displaying the map " + e1.getMessage());
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private Integer getSelectedChannel() {
		String selectedChannelString = cmbChannelChoice.getItems()[cmbChannelChoice.getSelectionIndex()];
		final Integer selectedChannel = Integer.parseInt(selectedChannelString);
		return selectedChannel;
	}

	@Override
	public void dispose() {
		super.dispose();
		elementList.dispose();
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
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	public boolean isLoadMapForScan() {
		return loadMapForScan;
	}

	public void toggleLoadMapForScan() {
		this.loadMapForScan = !loadMapForScan;
	}

	@Override
	public void update(Object source, Object arg) {
		final String detectorConfig = (String) arg;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// this update is coming from a MicroFocusWrieterExtender on the server during an active map scan, so
				// tell the displayController not to use any old opened Nexus file as its source now but to assume data
				// is coming from the server-side object
				populateLists(detectorConfig);
				displayController.setDetectorFile(detectorConfig);
				displayController.setFileIsDataSource(false);
			}
		});

	}

	/**
	 * Set the X and Y selected point, as data values, not the index numbers of the data array
	 *
	 * @param pointX
	 * @param pointY
	 */
	public void setLastXYSelection(double pointX, double pointY) {
		this.pointX = pointX;
		this.pointY = pointY;
	}

	public Double[] getLastXYZSelection() {
		return new Double[] { pointX, pointY, displayController.getZ() };
	}
}
