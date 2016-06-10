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

import org.dawnsci.common.widgets.stepper.IStepperSelectionListener;
import org.dawnsci.common.widgets.stepper.Stepper;
import org.dawnsci.common.widgets.stepper.StepperChangedEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.MouseEvent;
import org.eclipse.dawnsci.plotting.api.region.MouseListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;

public class ProjectionsView extends BaseTomoReconPart implements ISelectionListener, ISelectionProvider {

	public static final String PLOT_VIEW_TO_DISPLAY_RECON_IMAGE = "Plot 1";
	private static final String REGION_DRAG_LINE_NAME = "DragLine";
	private static final String ERR_MESSAGE_UNABLE_TO_FIND_DATASET = "Unable to find dataset";
	private static final String ERR_TITLE_DISPLAYING_PROJECTIONS = "Error while displaying projections";
	private static final String UPDATING_DATA = "Updating data(%s)";
	private static final String PROJECTIONS_PLOT = "Projections Plot";
	private static final String FILE_NAME = "File name";
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.projection";

	private final String LBL_PREVIOUS = "PREV";
	private final String LBL_NEXT = "NEXT";

	public final static int SPLITS = 128;
	/**
	 * Setup the logging facilities
	 */
	transient private static final Logger logger = LoggerFactory.getLogger(ProjectionsView.class);

	private IPlottingSystem<Composite> plottingSystem;

	private Text fileName;

	private Stepper slicingStepper;

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

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getViewSite().setSelectionProvider(this);
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
				null);

		disablePlottingSystemActions(plottingSystem);
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
		getRefreshJob();
	}

	protected void disablePlottingSystemActions(IPlottingSystem<Composite> plottingSystem) {
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.REMOVE_REGION);
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.CONFIG.getId() + BasePlottingConstants.CONFIG_SETTINGS);
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.CONFIG.getId() + BasePlottingConstants.XY_SHOWLEGEND);
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.HISTO.getId());
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.CONFIG.getId());
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.REGION.getId());

		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.RESCALE);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_INDEX);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_X_AXIS);
	}

	private void setGridLayoutMinimumSetting(GridLayout layout) {
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
	}

	private int position = -1;
	private Dataset datasetToPlot;

	private Job getRefreshJob() {
		Job refreshJob = new Job("") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(String.format("Plotting Data (%s)", nexusFile.getName()), IProgressMonitor.UNKNOWN);
				if (nexusFile == null) {
					return Status.CANCEL_STATUS;
				}

				if (dataset != null) {
					int[] shape = dataset.getShape();
					shape[0] = position + 1;
					Dataset slice = null;
					try {
						slice = DatasetUtils.convertToDataset(dataset.getSlice(new int[] { position, 0, 0 }, shape, new int[] { 1, 1, 1 }));
					} catch (DatasetException e) {
						logger.error("Could not get data from lazy dataset", e);
					}

					if (slice != null) {
						datasetToPlot = slice.squeeze();
						getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								plottingSystem.updatePlot2D(datasetToPlot, null, new NullProgressMonitor());
								fileName.setText(nexusFile.getLocation().toOSString());
								//
								for (ITrace trace : plottingSystem.getTraces()) {
									if (trace instanceof IImageTrace) {
										IImageTrace imageTrace = (IImageTrace) trace;
										final IPaletteService service = (IPaletteService) PlatformUI.getWorkbench().getService(IPaletteService.class);
										imageTrace.setPaletteData(service.getDirectPaletteData(""));
									}
								}
							}
						});
					}
					logger.debug(dataset.getName());
				} else {
					 showErrorMessage(ERR_TITLE_DISPLAYING_PROJECTIONS, new IllegalArgumentException(
					 ERR_MESSAGE_UNABLE_TO_FIND_DATASET));

				}
				monitor.done();
				return Status.OK_STATUS;
			}

		};
		if (nexusFile != null) {
			refreshJob.setName(String.format(UPDATING_DATA, nexusFile.getName()));
		}
		return refreshJob;
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

				xHair.addMouseListener(mouseFollowRegionMouseListener);
			}
		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private IROI yBounds;

	private MouseListener mouseFollowRegionMouseListener = new MouseListener.Stub() {
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
	private int sliceNumber;

	@Override
	public void setFocus() {
		slicingStepper.setFocus();
	}

	public void updateDataToPosition(final int pos) {
		this.position = pos;
		if (nexusFile != null) {
			// refreshJob.cancel();
			getRefreshJob().setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
			getRefreshJob().schedule(200);
		}
	}

	public void updateData() {
		Job findPositionToUpdate = new Job(String.format("Finding position to update (%s)", nexusFile.getName())) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				int positionToUpdateTo = 0;
				try {
					dataset = getDatasetFromNexusFile();
					if (dataset != null) {
						int[] shape = dataset.getShape();
						final int datasetShape = shape[0];
						positionToUpdateTo = datasetShape / 2;
						final int newStepperSelection = positionToUpdateTo;
						new UIJob(getViewSite().getShell().getDisplay(), "Updating stepper") {

							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								slicingStepper.setSteps(datasetShape);
								if (datasetShape > 2) {
									slicingStepper.setSelection(newStepperSelection);
								} else {
									slicingStepper.setSelection(0);
								}
								return Status.OK_STATUS;
							}
						}.schedule();

					} else {
						throw new IllegalArgumentException(ERR_MESSAGE_UNABLE_TO_FIND_DATASET);
					}
				} catch (ScanFileHolderException e1) {
					showErrorMessage("Cannot load hdf file", e1);
				} catch (IllegalArgumentException e2) {
					showErrorMessage(e2.getMessage(), e2);
				}

				updateDataToPosition(positionToUpdateTo);

				return Status.OK_STATUS;
			}
		};

		findPositionToUpdate.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		findPositionToUpdate.schedule();

	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isPartActive()) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object firstElement = iss.getFirstElement();
				if (firstElement instanceof IFile
						&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
					IFile file = (IFile) firstElement;
					if (!file.equals(nexusFile)) {
						nexusFile = file;
						processNewNexusFile();
					}
				}
			}
		}
	}

	@Override
	protected void processNewNexusFile() {
		try {
			getViewSite().getActionBars().getStatusLineManager()
					.setMessage(String.format("Loading file %s ...", nexusFile.getFullPath().toOSString()));

			updateData();

			UpdatePlotJob updatePlotJob = new UpdatePlotJob(this);
			updatePlotJob.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
			updatePlotJob.setPixelPosition(sliceNumber);
			updatePlotJob.setName(String.format("Update plot after reconstruction:%s", nexusFile.getName()));
			updatePlotJob.setNexusFileLocation(nexusFile.getLocation().toOSString());
			updatePlotJob.schedule();

			pgBook.showPage(plotPage);
		} catch (Exception e) {
			showErrorMessage("Problem with displaying dataset", e);
		} finally {
			getViewSite().getActionBars().getStatusLineManager().setMessage(null);
		}
	}

	private void showErrorMessage(final String message, final Exception e) {
		logger.error("Problem with displaying dataset:" + message, e);
		Display display = getViewSite().getShell().getDisplay();

		if (display != null && !display.isDisposed()) {
			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					lblEmptyPageLabel.setText(String.format("%s: %s", message, e.getMessage()));
					pgBook.showPage(emptyPage);
				}
			});
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	public void displayReconstruction(final String nexusFileLocation, final int pixelPosition) {
		UpdatePlotJob updatePlotJob = new UpdatePlotJob(this);
		if (nexusFile != null) {
			updatePlotJob.setName(String.format(UPDATING_DATA, nexusFile.getName()));
		}

		updatePlotJob.setNexusFileLocation(nexusFileLocation);
		updatePlotJob.setPixelPosition(pixelPosition);

		updatePlotJob.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		updatePlotJob.schedule();

	}

	private void roiSet(double d) {
		logger.debug("roi set:{}", d);

		sliceNumber = (int) d;
		displayReconstruction(nexusFile.getLocation().toOSString(), sliceNumber);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// not intending to publish events yet

	}

	@Override
	public ISelection getSelection() {
		ProjectionSliceSelection projectionSliceSelection = new ProjectionSliceSelection(sliceNumber);
		projectionSliceSelection.setDataSetPlotted(datasetToPlot);
		return projectionSliceSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {

	}

	@Override
	public void setSelection(ISelection selection) {
		// no intention of set selection now.
	}

}
