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

import org.dawb.common.services.IPaletteService;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.MouseEvent;
import org.dawnsci.plotting.api.region.MouseListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
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
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

public class ProjectionsView extends ViewPart implements ISelectionListener {

	private static final String PLOT_VIEW_TO_DISPLAY_RECON_IMAGE = "Plot 1";
	private static final String JOB_UPDATE_RECONSTRUCTION_DISPLAY = "Update Reconstruction Display";
	private static final String REGION_DRAG_LINE_NAME = "DragLine";
	private static final String ERR_MESSAGE_UNABLE_TO_FIND_DATASET = "Unable to find dataset";
	private static final String ERR_TITLE_DISPLAYING_PROJECTIONS = "Error while displaying projections";
	private static final String UPDATING_DATA = "Updating data";
	private static final String PROJECTIONS_PLOT = "Projections Plot";
	private static final String FILE_NAME = "File name";
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.projection";

	private static final String PATH_TO_DATA_IN_NEXUS = "/entry1/tomo_entry/data/data";
	private final String LBL_PREVIOUS = "PREV";
	private final String LBL_NEXT = "NEXT";

	public final static int SPLITS = 128;
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

	private IStepperSelectionListener slicingSelectionStepperListener = new IStepperSelectionListener() {

		@Override
		public void stepperChanged(StepperChangedEvent e) {
			updateDataToPosition(e.getPosition());
		}

	};

	private boolean isPartActive;

	synchronized void setPartActive(boolean isActive) {
		this.isPartActive = isActive;
	}

	private IPartListener2 partAdapter = new IPartListener2() {

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ProjectionsView.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ProjectionsView.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ProjectionsView.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ProjectionsView.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {

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
		setGridLayoutMinimumSetting(layout);
		plotPage.setLayout(layout);
		// row 1
		slicingStepper = new Stepper(plotPage, SWT.None, false);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		slicingStepper.setLayoutData(layoutData);
		slicingStepper.addStepperSelectionListener(slicingSelectionStepperListener);

		// row 2
		Composite plotContainingComposite = new Composite(plotPage, SWT.None);
		layout = new GridLayout(2, false);
		setGridLayoutMinimumSetting(layout);
		plotContainingComposite.setLayout(layout);
		plotContainingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite btnsComposite = new Composite(plotContainingComposite, SWT.None);
		btnsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		layout = new GridLayout();
		setGridLayoutMinimumSetting(layout);
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
					IROI roi = xHair.getROI();
					if (roi.getPointY() >= SPLITS) {
						roi.setPoint(roi.getPointX(), roi.getPointY() - SPLITS);
						IROI roiModified = getYBounds(roi);
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
					IROI roi = xHair.getROI();
					if (roi.getPointY() < max - SPLITS) {
						roi.setPoint(roi.getPointX(), roi.getPointY() + SPLITS);
						IROI roiModified = getYBounds(roi);
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
		plottingSystem.createPlotPart(plotComposite, PROJECTIONS_PLOT, getViewSite().getActionBars(), PlotType.IMAGE,
				this);
		createMouseFollowLineRegion();

		// row 3
		Composite extrasComposite = new Composite(plotPage, SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		extrasComposite.setLayoutData(layoutData);
		layout = new GridLayout();
		setGridLayoutMinimumSetting(layout);

		extrasComposite.setLayout(layout);

		fileName = new Text(extrasComposite, SWT.BORDER);
		fileName.setEditable(false);
		fileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileName.setText(FILE_NAME);

		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		getViewSite().getWorkbenchWindow().getPartService().addPartListener(partAdapter);
		doCreateRefreshJob();
	}

	private void setGridLayoutMinimumSetting(GridLayout layout) {
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
	}

	private int position = -1;

	private void doCreateRefreshJob() {
		refreshJob = new UIJob(getViewSite().getShell().getDisplay(), UPDATING_DATA) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				monitor.beginTask("Plotting Data from " + nexusFile.getName(), IProgressMonitor.UNKNOWN);
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
							plottingSystem.updatePlot2D((AbstractDataset) squeeze, null, new NullProgressMonitor());
							fileName.setText(nexusFile.getLocation().toOSString());
							//
							slicingStepper.setSelection(position);
							for (ITrace trace : plottingSystem.getTraces()) {
								if (trace instanceof IImageTrace) {
									IImageTrace imageTrace = (IImageTrace) trace;
									final IPaletteService service = (IPaletteService) PlatformUI.getWorkbench()
											.getService(IPaletteService.class);
									imageTrace.setPaletteData(service.getPaletteData(""));
								}
							}
						}
					});
					logger.debug(dataset.getName());
				} else {
					showErrorMessage(ERR_TITLE_DISPLAYING_PROJECTIONS, new IllegalArgumentException(
							ERR_MESSAGE_UNABLE_TO_FIND_DATASET));
				}
				monitor.done();
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
				xHair = plottingSystem.createRegion(RegionUtils.getUniqueName(REGION_DRAG_LINE_NAME, plottingSystem),
						IRegion.RegionType.YAXIS_LINE);
				xHair.setRegionColor(ColorConstants.red);
				xHair.setVisible(true);
				xHair.setTrackMouse(true);
				xHair.setUserRegion(false);
				plottingSystem.addRegion(xHair);
				xHair.addROIListener(mouseFollowRoiListener);

				xHair.addMouseListener(mouseFollowRegionMouseListner);
			}
		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private IROI yBounds;

	private MouseListener mouseFollowRegionMouseListner = new MouseListener.Stub() {

		@Override
		public void mousePressed(MouseEvent me) {
			try {
				xHair.setTrackMouse(false);
				IROI roi = getYBounds(yBounds);
				xHair.setROI(roi);
				roiSet(roi.getPointY());
			} catch (Exception e) {
				showErrorMessage("Problem creating region to plot", e);
			}
		}

	};

	private IROI getYBounds(IROI bounds) {
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
			IROI roi = region.getROI();
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
				IROI roi = getYBounds(evt.getROI());
				xHair.setROI(roi);
				roiSet(roi.getPointY());
			}
		}

		@Override
		public void roiSelected(ROIEvent evt) {

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
		int positionToUpdateTo = 0;
		try {
			loadFile = hdf5Loader.loadFile();
			dataset = loadFile.getLazyDataset(PATH_TO_DATA_IN_NEXUS);
			if (dataset != null) {
				int[] shape = dataset.getShape();
				int datasetShape = shape[0];
				slicingStepper.setSteps(datasetShape);
				if (datasetShape > 2) {
					positionToUpdateTo = datasetShape / 2;
					slicingStepper.setSelection(positionToUpdateTo);
				} else {
					slicingStepper.setSelection(0);
				}
			} else {
				throw new IllegalArgumentException(ERR_MESSAGE_UNABLE_TO_FIND_DATASET);
			}
		} catch (ScanFileHolderException e1) {
			showErrorMessage("Cannot load hdf file", e1);
		} catch (IllegalArgumentException e2) {
			showErrorMessage(e2.getMessage(), e2);
		}
		updateDataToPosition(positionToUpdateTo);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isPartActive) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object firstElement = iss.getFirstElement();
				if (firstElement instanceof IFile
						&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
					IFile file = (IFile) firstElement;
					if (!file.equals(nexusFile)) {
						nexusFile = file;
						try {
							getViewSite()
									.getActionBars()
									.getStatusLineManager()
									.setMessage(
											String.format("Loading file %s ...", nexusFile.getFullPath().toOSString()));
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
		}
	}

	private void showErrorMessage(String message, Exception e) {
		logger.error("Problem with displaying dataset:" + message, e);
		lblEmptyPageLabel.setText(String.format("%s: %s", message, e.getMessage()));
		pgBook.showPage(emptyPage);
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		getViewSite().getWorkbenchWindow().getPartService().removePartListener(partAdapter);
		super.dispose();
	}

	public void displayReconstruction(final String nexusFileLocation, final int pixelPosition) {

		UIJob displayJob = new UIJob(getViewSite().getShell().getDisplay(), JOB_UPDATE_RECONSTRUCTION_DISPLAY) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				monitor.beginTask("", 5);
				// Update monitor
				monitor.worked(1);
				String pathToImages = ReconUtil.getReconstructedReducedDataDirectoryPath(nexusFileLocation);

				File imageFile = new File(pathToImages, String.format(ReconUtil.RECONSTRUCTED_IMAGE_FILE_FORMAT,
						pixelPosition / SPLITS));
				logger.debug("Looking for image file {}", imageFile.getPath());
				if (imageFile.exists()) {
					// update monitor
					monitor.worked(1);

					try {
						DataHolder data = new TIFFImageLoader(imageFile.getAbsolutePath()).loadFile();

						// update monitor
						monitor.worked(1);

						AbstractDataset image = data.getDataset(0);
						image.isubtract(image.min());
						image.imultiply(1000.0);

						// update monitor
						monitor.worked(1);

						SDAPlotter.imagePlot(PLOT_VIEW_TO_DISPLAY_RECON_IMAGE, image);

						// update monitor
						monitor.worked(1);
					} catch (Exception e) {
						logger.error("Cannot load recon image for display", e);
						return Status.CANCEL_STATUS;
					}
				} else {
					MessageDialog
							.openError(
									getViewSite().getShell(),
									"Problem loading data",
									"Unable to locate image for the slice. \n\nIt may be advisable to run a Preview Recon(from the Parameters View) and try loading the slice again. ");
				}
				return Status.OK_STATUS;
			}
		};

		displayJob.setUser(true);
		displayJob.setRule(new ReconSchedulingRule(nexusFile));
		displayJob.schedule();

	}

	private void roiSet(double d) {
		logger.debug("roi set:{}", d);
		displayReconstruction(nexusFile.getLocation().toOSString(), (int) d);
	}

}
