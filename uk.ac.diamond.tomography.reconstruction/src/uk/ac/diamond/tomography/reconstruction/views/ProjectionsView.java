/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
package uk.ac.diamond.tomography.reconstruction.views;

import gda.analysis.io.ScanFileHolderException;

import java.io.File;

import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

public class ProjectionsView extends ViewPart implements ISelectionListener {

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.projection";
	/**
	 * Setup the logging facilities
	 */
	transient private static final Logger logger = LoggerFactory.getLogger(ProjectionsView.class);

	private AbstractPlottingSystem plottingSystem;

	private IFile nexusFile;

	private Label fileName;

	private Stepper slicingStepper;

	public ProjectionsView() {
		// TODO Auto-generated constructor stub
	}

	private IStepperSelectionListener slicingSelectionStepperListener = new IStepperSelectionListener() {

		@Override
		public void stepperChanged(StepperChangedEvent e) {
			updateDataToPosition(e.getPosition());
		}

	};

	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.None);
		// root.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		root.setLayout(layout);

		slicingStepper = new Stepper(root, SWT.None, false);
		slicingStepper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		slicingStepper.addStepperSelectionListener(slicingSelectionStepperListener);

		Composite plotComposite = new Composite(root, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {

		}
		plottingSystem.createPlotPart(plotComposite, "Projections Plot", getViewSite().getActionBars(), PlotType.IMAGE,
				this);
		createMouseFollowLineRegion();

		fileName = new Label(root, SWT.BORDER);
		fileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileName.setText("File name");

		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	private void createMouseFollowLineRegion() {

		if (plottingSystem == null)
			return;
		try {
			IRegion xHair = null;
			if (xHair == null || plottingSystem.getRegion(xHair.getName()) == null) {
				xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("DragLine", plottingSystem),
						IRegion.RegionType.YAXIS_LINE);

				xHair.addROIListener(mouseFollowRoiListener);

				addMouseFollowLineRegion(xHair);
			}

		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private void addMouseFollowLineRegion(IRegion region) {
		region.setVisible(true);
		region.setTrackMouse(true);
		// region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change
										// it!
		plottingSystem.addRegion(region);
	}

	private IROIListener mouseFollowRoiListener = new IROIListener() {

		private void update(ROIEvent evt) {
			final IRegion region = (IRegion) evt.getSource();
			logger.debug("Mouse clicked");
		}

		@Override
		public void roiDragged(ROIEvent evt) {
			// update(evt);
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			update(evt);
		}

	};
	private ILazyDataset dataset;

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void updateDataToPosition(final int pos) {
		UIJob updateDataJob = new UIJob(getViewSite().getShell().getDisplay(), "Updating data") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// Load the nexus file
				String path = nexusFile.getLocation().toOSString();
				HDF5Loader hdf5Loader = new HDF5Loader(path);
				DataHolder loadFile;
				if (dataset != null) {
					int[] shape = dataset.getShape();
					shape[0] = pos + 1;
					IDataset slice = dataset.getSlice(new int[] { pos, 0, 0 }, shape, new int[] { 1, 1, 1 });
					ILazyDataset squeeze = slice.squeeze();
					plottingSystem.createPlot2D((AbstractDataset) squeeze, null, new NullProgressMonitor());
					fileName.setText(nexusFile.getFullPath().toOSString());
					for (ITrace trace : plottingSystem.getTraces()) {
						if (trace instanceof IImageTrace) {
							IImageTrace imageTrace = (IImageTrace) trace;
							imageTrace.setPaletteData(PaletteFactory.makeGrayScalePalette());
						}
					}
					logger.debug(dataset.getName());
				} else {
					throw new IllegalArgumentException("Unable to find dataset");
				}

				return Status.OK_STATUS;
			}
		};

		//updateDataJob.setSystem(true);
		updateDataJob.schedule();

	}

	public void updateData() {
		String path = nexusFile.getLocation().toOSString();
		HDF5Loader hdf5Loader = new HDF5Loader(path);
		DataHolder loadFile;
		try {
			loadFile = hdf5Loader.loadFile();
			dataset = loadFile.getLazyDataset("/entry1/pco1_hw_tif/image_data");
			if (dataset != null) {
				int[] shape = dataset.getShape();
				slicingStepper.setSteps(shape[0]);
			} else {
				throw new IllegalArgumentException("Unable to find dataset");
			}
		} catch (ScanFileHolderException e1) {
			showErrorMessage("Cannot load hdf file", e1);
		}
		updateDataToPosition(0);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			Object firstElement = iss.getFirstElement();
			if (firstElement instanceof IFile
					&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
				nexusFile = (IFile) firstElement;
				try {
					getViewSite().getActionBars().getStatusLineManager().setMessage(String.format("Loading file %s ...", nexusFile.getFullPath().toOSString()));
					updateData();
					getViewSite().getActionBars().getStatusLineManager().setMessage(null);
				} catch (Exception e) {
					showErrorMessage("Problem with displaying dataset", e);
				}
			}
		}
	}

	private void showErrorMessage(String string, Exception e) {
		MessageDialog.openError(getViewSite().getShell(), "Problem with displaying dataset",
				string + ":" + e.getMessage());
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	public void displayReconstruction(IFile nexusFile, int pixelPosition) {

		File path = new File(nexusFile.getLocation().toOSString());
		File pathToRecon = new File(path.getParent(), "/processing/reconstruction/");
		File pathToImages = new File(pathToRecon, path.getName() + "_data");

	}

}
