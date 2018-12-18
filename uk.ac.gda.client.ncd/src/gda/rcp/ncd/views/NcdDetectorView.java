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

import gda.device.DeviceException;
import gda.device.detector.DataDimension;
import gda.rcp.ncd.NcdController;
import gda.swing.ncd.MemoryUsage;

import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;

/**
 * A view to select the SAXS or WAXS detector in the server's NcdDetectorSystem as well as their resolutions
 */
public class NcdDetectorView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(NcdDetectorView.class);

	public static final String ID = "gda.rcp.ncd.views.NcdDetectorView"; //$NON-NLS-1$
	private List<DetectorPack> packs = new Vector<DetectorPack>();
	private NcdController ncdController = NcdController.getInstance();

	private class DetectorPack {
		String type;
		INcdSubDetector detector;
		String detectorName;
		List<String> availableDetectors = new Vector<String>();
		Combo detectorChoice;
		Combo resolutionChoice;

		DetectorPack(String type, List<String> availableDetectors) {
			this.type = type;
			this.availableDetectors = availableDetectors;
		}

		void addListenersAndInit() {
			initDetectorCombo();
			initResolutionCombo();

			detectorChoice.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String detName = ((Combo) e.getSource()).getText();
					setDetector(detName);
				}
			});

			resolutionChoice.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String resSpec = ((Combo) e.getSource()).getText();
					setResolution(resSpec);
				}
			});
		}

		void initDetectorCombo() {
			int index = 0;
			this.detectorName = ncdController.getDetectorName(type);
			this.detector = ncdController.getDetectorByName(detectorName);

			detectorChoice.removeAll();			
			for (String name : availableDetectors) {
				detectorChoice.add(name);
				if (name.equals(detectorName)) {
					detectorChoice.select(index);
				}
				index++;
			}
		}

		void initResolutionCombo() {
			resolutionChoice.removeAll();
			if (detector == null) {
				setMemoryUsage(null);
				return;
			}
			try {
				int[] selectedDims = detector.getDataDimensions();
				setMemoryUsage(selectedDims);
				String selected = selectedDims[0] + "x" + selectedDims[1];
				List<DataDimension> supportedDimensions = detector.getSupportedDimensions();
				List<String> items = new Vector<String>();

				if (supportedDimensions != null && supportedDimensions.size() > 0) {
					for (DataDimension d : supportedDimensions) {
						items.add(d.pixels + "x" + d.rasters);
					}
				} else {
					items.add(selected);
				}

				int index = 0;
				for (String name : items) {
					resolutionChoice.add(name);
					if (name.equals(selected)) {
						resolutionChoice.select(index);
					}
					index++;
				}
			} catch (Exception e) {
				// box empty on failure, not too bad
			}
		}

		void setDetector(String newDetectorName) {
			if (detector != null) {
				try {
					detector.close();
					detector = null;
				} catch (DeviceException de) {
					logger.warn("error closing detector", de);
				}
			}
			logger.debug("selected " + type + " is " + newDetectorName);
			
			INcdSubDetector newDetector = ncdController.getDetectorByName(newDetectorName);
			logger.debug("detector found is " + newDetector);
			try {
				if (newDetector != null) {
					newDetector.reconfigure();
				}
				ncdController.setDetector(type, newDetectorName);

			} catch (Exception de) {
				logger.error("wooah: " + de);
			}
			
			initDetectorCombo();
			initResolutionCombo();
		}

		void setResolution(String newRes) {
			if (detector == null) {
				return;
			}
			String[] xy = newRes.split("x");
			int[] dims = new int[] { Integer.parseInt(xy[0]), Integer.parseInt(xy[1]) };
			setMemoryUsage(dims);
			try {
				detector.setDataDimensions(dims);
			} catch (Exception de) {
				logger.error("wooah: " + de);
			}
		}

		void setMemoryUsage(int[] dims) {
			double memoryRatio = 0;
			if (dims != null && detector != null) {
				long memorySize;
				try {
					memorySize = detector.getMemorySize();
					if (memorySize > 0) {
						memoryRatio = new Double(dims[0] * dims[1]) / memorySize;
					}
				} catch (DeviceException e) {
				}
			}
			MemoryUsage.getInstance().setMemoryRatio(type, memoryRatio);
		}
	}

	private void initPacks() {
		List<String> available;
		for (String label : new String[] { "SAXS", "WAXS" }) {
			available = new Vector<String>();
			available.add(NcdController.NODETECTOR);
			available.addAll(ncdController.getDetectorNames(label));
			packs.add(new DetectorPack(label, available));
		}
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		initPacks();

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		for (DetectorPack pack : packs) {
			Composite composite = new Composite(container, SWT.NONE);
			composite.setLayout(new FillLayout(SWT.HORIZONTAL));

			Group grpSaxs = new Group(composite, SWT.NONE);
			grpSaxs.setLayout(new GridLayout(2, false));
			grpSaxs.setText(pack.type);
			{
				Label lblDetector = new Label(grpSaxs, SWT.NONE);
				lblDetector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblDetector.setText("Detector");
				pack.detectorChoice = new Combo(grpSaxs, SWT.DROP_DOWN | SWT.SINGLE | SWT.READ_ONLY);
				pack.detectorChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

				Label lblResolution = new Label(grpSaxs, SWT.NONE);
				lblResolution.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblResolution.setText("Resolution");
				pack.resolutionChoice = new Combo(grpSaxs, SWT.DROP_DOWN | SWT.SINGLE | SWT.READ_ONLY);
				pack.resolutionChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			pack.addListenersAndInit();
		}

		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// no actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public int getSizeFlags(boolean width) {
		return (width ? SWT.FILL | SWT.MIN : 0);
	}
}