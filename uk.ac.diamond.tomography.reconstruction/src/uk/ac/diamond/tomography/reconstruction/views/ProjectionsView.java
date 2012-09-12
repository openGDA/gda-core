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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

public class ProjectionsView extends ViewPart implements ISelectionListener {

	private static final String FILE_NAME = "File name";
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.projection";

	private final String LBL_PREVIOUS = "PREVIOUS";
	private final String LBL_NEXT = "NEXT";

	private final int SPLITS = 100;
	/**
	 * Setup the logging facilities
	 */
	transient private static final Logger logger = LoggerFactory.getLogger(ProjectionsView.class);

	private AbstractPlottingSystem plottingSystem;

	private IFile nexusFile;

	private Text fileName;

	private Stepper slicingStepper;

	private UIJob refreshJob;

	private ILazyDataset dataset;
	private IRegion xHair = null;
	private PageBook pgBook;
	private Composite emptyPage;
	private Label lblEmptyPageLabel;
	private Composite plotPage;

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

		pgBook = new PageBook(parent, SWT.None);

		emptyPage = new Composite(pgBook, SWT.None);
		emptyPage.setLayout(new FillLayout());

		lblEmptyPageLabel = new Label(emptyPage, SWT.None);
		lblEmptyPageLabel.setText("Selection has no plot to display");

		plotPage = new Composite(pgBook, SWT.None);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		plotPage.setLayout(layout);
		// row 1
		slicingStepper = new Stepper(plotPage, SWT.None, false);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		slicingStepper.setLayoutData(layoutData);
		slicingStepper.addStepperSelectionListener(slicingSelectionStepperListener);

		// row 2
		Composite plotContainingComposite = new Composite(plotPage, SWT.None);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		plotContainingComposite.setLayout(layout);
		plotContainingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite btnsComposite = new Composite(plotContainingComposite, SWT.None);
		btnsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		plotPage.setLayout(layout);
		btnsComposite.setLayout(layout);

		Button btnPrevious = new Button(btnsComposite, SWT.ARROW | SWT.PUSH | SWT.UP);
		GridData layoutData2 = new GridData(GridData.FILL_VERTICAL);
		layoutData2.widthHint = 30;
		btnPrevious.setLayoutData(layoutData2);
		btnPrevious.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!xHair.isTrackMouse()) {
					ROIBase roi = xHair.getROI();
					if (roi.getPointY() >= SPLITS) {
						roi.setPoint(roi.getPointX(), roi.getPointY() - SPLITS);
						ROIBase roiModified = getYBounds(roi);
						xHair.setROI(roiModified);
						roiSet(roiModified.getPointY());
					}

				}
			}
		});

		btnPrevious.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int y = 2;
				for (char c : LBL_PREVIOUS.toCharArray()) {
					gc.drawText(new String(new char[] { c }), 10, y += 20, true);
				}
			}
		});

		Button btnNext = new Button(btnsComposite, SWT.ARROW | SWT.DOWN | SWT.PUSH);
		GridData layoutData3 = new GridData(GridData.FILL_VERTICAL);
		layoutData3.widthHint = 30;
		btnNext.setLayoutData(layoutData3);
		btnNext.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int y = 2;
				for (char c : LBL_NEXT.toCharArray()) {
					gc.drawText(new String(new char[] { c }), 10, y += 20, true);
				}
			}
		});
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!xHair.isTrackMouse() && dataset != null && dataset.getShape().length == 3) {
					int max = dataset.getShape()[1];
					ROIBase roi = xHair.getROI();
					if (roi.getPointY() < max - SPLITS) {
						roi.setPoint(roi.getPointX(), roi.getPointY() + SPLITS);
						ROIBase roiModified = getYBounds(roi);
						xHair.setROI(roiModified);
						roiSet(roiModified.getPointY());
					}

				}
			}
		});
		Composite plotComposite = new Composite(plotContainingComposite, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {

		}
		plottingSystem.createPlotPart(plotComposite, "Projections Plot", getViewSite().getActionBars(), PlotType.IMAGE,
				this);
		createMouseFollowLineRegion();

		// row 3
		Composite extrasComposite = new Composite(plotPage, SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		extrasComposite.setLayoutData(layoutData);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;

		extrasComposite.setLayout(layout);

		fileName = new Text(extrasComposite, SWT.BORDER);
		fileName.setEditable(false);
		fileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileName.setText(FILE_NAME);

		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		doCreateRefreshJob();
	}

	private int position = -1;

	private void doCreateRefreshJob() {
		refreshJob = new UIJob(getViewSite().getShell().getDisplay(), "Updating data") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (nexusFile == null) {
					return Status.CANCEL_STATUS;
				}
				if (dataset != null) {
					int[] shape = dataset.getShape();
					shape[0] = position + 1;
					IDataset slice = dataset.getSlice(new int[] { position, 0, 0 }, shape, new int[] { 1, 1, 1 });
					final ILazyDataset squeeze = slice.squeeze();

					getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							plottingSystem.createPlot2D((AbstractDataset) squeeze, null, new NullProgressMonitor());
							fileName.setText(nexusFile.getLocation().toOSString());
							for (ITrace trace : plottingSystem.getTraces()) {
								if (trace instanceof IImageTrace) {
									IImageTrace imageTrace = (IImageTrace) trace;
									imageTrace.setPaletteData(PaletteFactory.makeGrayScalePalette());
								}
							}

						}
					});
					logger.debug(dataset.getName());
				} else {
//					throw new IllegalArgumentException("Unable to find dataset");
					showErrorMessage("Error while displaying projections", new IllegalArgumentException("Unable to find dataset"));
				}

				return Status.OK_STATUS;
			}

		};
	}

	private void createMouseFollowLineRegion() {

		if (plottingSystem == null)
			return;
		try {
			xHair = null;
			if (xHair == null || plottingSystem.getRegion(xHair.getName()) == null) {
				xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("DragLine", plottingSystem),
						IRegion.RegionType.YAXIS_LINE);
				xHair.setRegionColor(ColorConstants.red);
				xHair.setVisible(true);
				xHair.setTrackMouse(true);
				xHair.setUserRegion(false); // They cannot see preferences or change
											// it!
				plottingSystem.addRegion(xHair);
				xHair.addROIListener(mouseFollowRoiListener);

				xHair.addMouseListener(mouseFollowRegionMouseListner);
			}

		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private ROIBase yBounds;

	private MouseListener mouseFollowRegionMouseListner = new MouseListener.Stub() {
		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {
			try {
				xHair.setTrackMouse(false);
				ROIBase roi = getYBounds(yBounds);
				xHair.setROI(roi);
				roiSet(roi.getPointY());
			} catch (Exception e) {
				showErrorMessage("Problem creating region to plot", e);
			}
		}

	};

	private ROIBase getYBounds(ROIBase bounds) {
		double pointY = bounds.getPointY();

		double rem = pointY % SPLITS;
		int q = (int) pointY / SPLITS;
		int yPoint = q * SPLITS;
		if (rem > ((0.5) * SPLITS)) {
			yPoint = (q + 1) * SPLITS;
		}

		bounds.setPoint(bounds.getPointX(), yPoint);
		return bounds;
	}

	private IROIListener mouseFollowRoiListener = new IROIListener() {

		private boolean dragged = false;

		private void update(ROIEvent evt) {
			final IRegion region = (IRegion) evt.getSource();
			ROIBase roi = region.getROI();
			yBounds = roi;
		}

		@Override
		public void roiDragged(ROIEvent evt) {
			update(evt);
			if (!xHair.isTrackMouse()) {
				dragged = true;
			}
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			update(evt);
			if (dragged && !(xHair.isTrackMouse())) {
				dragged = false;
				ROIBase roi = getYBounds(evt.getROI());
				xHair.setROI(roi);
				roiSet(roi.getPointY());

			}
		}

	};

	@Override
	public void setFocus() {

	}

	public void updateDataToPosition(final int pos) {
		this.position = pos;
		refreshJob.cancel();
		refreshJob.schedule(200);
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
				slicingStepper.setSelection(0);
			} else {
				throw new IllegalArgumentException("Unable to find dataset");
			}
		} catch (ScanFileHolderException e1) {
			showErrorMessage("Cannot load hdf file", e1);
		} catch (IllegalArgumentException e2) {
			showErrorMessage(e2.getMessage(), e2);
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
					getViewSite().getActionBars().getStatusLineManager()
							.setMessage(String.format("Loading file %s ...", nexusFile.getFullPath().toOSString()));
					updateData();
					pgBook.showPage(plotPage);
				} catch (Exception e) {
					showErrorMessage("Problem with displaying dataset", e);
				} finally {
					getViewSite().getActionBars().getStatusLineManager().setMessage(null);
				}
			}
		}
	}

	private void showErrorMessage(String message, Exception e) {
		logger.error("Problem with displaying dataset:" + message, e);
		lblEmptyPageLabel.setText(message + ": " + e.getMessage());
		pgBook.showPage(emptyPage);
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	public void displayReconstruction(final IFile nexusFile, final int pixelPosition) {

		UIJob displayJob = new UIJob(getViewSite().getShell().getDisplay(), "Update Reconstruction Display") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				File path = new File(nexusFile.getLocation().toOSString());
				File pathToRecon = ReconUtil.getPathToWriteTo(nexusFile);
				File pathToImages = new File(pathToRecon, path.getName().replace(".nxs", "") + "_data_quick");
				File imageFile = new File(pathToImages, String.format("image_%05d.tif", pixelPosition));

				try {
					DataHolder data = new TIFFImageLoader(imageFile.getAbsolutePath()).loadFile();
					AbstractDataset image = data.getDataset(0);
					image.isubtract(image.min());
					image.imultiply(1000.0);
					SDAPlotter.imagePlot("Plot 1", image);
				} catch (Exception e) {
					logger.error("TODO cannot Load reconstruction for display", e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		displayJob.schedule();

	}

	private void roiSet(double d) {
		logger.debug("roi set:{}", d);
		displayReconstruction(nexusFile, (int) d);
	}

}
