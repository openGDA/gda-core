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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.tomography.reconstruction.Activator;

public class ProjectionsView extends ViewPart implements ISelectionListener {

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.projection";
	/**
	 * Setup the logging facilities
	 */
	transient private static final Logger logger = LoggerFactory
			.getLogger(ProjectionsView.class);

	private AbstractPlottingSystem plottingSystem;
	private Text textBox;

	private IFile nexusFile;
	
	public ProjectionsView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.None);
		// root.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		root.setLayout(layout);
		textBox = new Text(root, SWT.NONE);
		textBox.setText("20");
		textBox.addModifyListener(new ModifyListener() {
			
			

			@Override
			public void modifyText(ModifyEvent e) {
				if (nexusFile != null) {
					updateData(nexusFile);
				}
			}
		});
		
		
		Composite plotComposite = new Composite(root, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {

		}
		plottingSystem.createPlotPart(plotComposite, "", null, PlotType.IMAGE,
				null);
		createMouseFollowLineRegion();

		getViewSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(IPageLayout.ID_PROJECT_EXPLORER, this);
	}

	private void createMouseFollowLineRegion() {

		if (plottingSystem == null)
			return;
		try {
			IRegion xHair = null;
			if (xHair == null
					|| plottingSystem.getRegion(xHair.getName()) == null) {
				xHair = plottingSystem.createRegion(
						RegionUtils.getUniqueName("DragLine", plottingSystem),
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

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void updateData(IFile nexusFile) {

		// Load the nexus file
		try {
			String path = nexusFile.getLocation().toOSString();
			HDF5Loader hdf5Loader = new HDF5Loader(path);
			DataHolder loadFile = hdf5Loader.loadFile();
			ILazyDataset dataset = loadFile.getLazyDataset("/entry1/pco1_hw_tif/image_data");
			int pos = 20;
			try {
				pos = Integer.parseInt(textBox.getText());
			} catch (Exception e) {
				logger.debug("Failed to get a posistion from the textbox");
			}
			int[] shape = dataset.getShape();
			shape[0] = pos+1;
			IDataset slice = dataset.getSlice(new int[] {pos,0,0}, shape, new int[] {1,1,1});
			ILazyDataset squeeze = slice.squeeze();
			plottingSystem.createPlot2D((AbstractDataset)squeeze, null, new NullProgressMonitor());
			for (ITrace trace : plottingSystem.getTraces()) {
				if (trace instanceof IImageTrace) {
					IImageTrace imageTrace = (IImageTrace) trace;
					imageTrace.setPaletteData(PaletteFactory.makeGrayScalePalette());
				}
			}
			logger.debug(dataset.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			Object firstElement = iss.getFirstElement();
			if (firstElement instanceof IFile
					&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement)
							.getFileExtension())) {
				logger.debug("Found nxs file, ready to call updateData");
				nexusFile = (IFile)firstElement;
				updateData(nexusFile);
			}
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(IPageLayout.ID_PROJECT_EXPLORER, this);
		super.dispose();
	}
	
	
	public void displayReconstruction(IFile nexusFile, int pixelPosition) {
		
		File path = new File(nexusFile.getLocation().toOSString());
		File pathToRecon = new File(path.getParent(),"/processing/reconstruction/");
		File pathToImages =  new File(pathToRecon, path.getName() + "_data" );
		
		
		
	}
	

}
