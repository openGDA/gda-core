package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationMode;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

/**
 * 
 * 
 * @author Eliot Hall
 * @author Maurzio Nagni
 */
public class CameraImageComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(CameraImageComposite.class);
	private static final int IMAGE_DIMENSION_LABEL_WIDTH = 40;

	private AbstractCameraConfigurationController controller;
	private LivePlottingComposite plottingComposite;
	private IPlottingSystem<Composite> plottingSystem;
	private boolean frozen = false;

	private Label widthLabel;
	private Label heightLabel;

	private SnapshotData lastSnapshot;

	private DrawableRegion roiSelectionRegion;
	private DrawableRegion highFluxRegion;
	private DrawableRegion lowFluxRegion;

	private boolean ignoreControllerUpdate = false;

	private class ROIListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent event) {
			// Do nothing
		}

		@Override
		public void roiChanged(ROIEvent event) {
			RectangularROI roi = ((RectangularROI) event.getROI().getBounds()).copy();
			RectangularROI currentRoi = controller.getCurrentRoi();

			roi.setPoint(roi.getIntPoint()[0] + currentRoi.getIntPoint()[0],
					roi.getIntPoint()[1] + currentRoi.getIntPoint()[1]);
			ignoreControllerUpdate = true;
			controller.setROI(roi);
		}

		@Override
		public void roiSelected(ROIEvent event) {
			// do nothing
		}
	}

	private class RegionListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent event) {
			// do nothing
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			calculateRatio();
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			// do nothing
		}

	}

	public class ModeListener extends CameraConfigurationAdapter {
		private void refreshSnapshot(boolean reconnect) throws Exception {
			if (reconnect) {
				plottingComposite.connect();
			}
			ITrace liveTrace = plottingComposite.getITrace();
			lastSnapshot = new SnapshotData("Adsorption Snapshot", liveTrace.getData().clone());
			plottingComposite.disconnect();
			plottingSystem.clear();
			plottingSystem.createPlot2D(lastSnapshot.getDataset(), null, "Snap!", new NullProgressMonitor());
			plottingSystem.setTitle(lastSnapshot.getTitle());
		}

		@Override
		public void refreshSnapshot() {
			try {
				refreshSnapshot(true);
				calculateRatio();
			} catch (Exception e) {
				log.error("Unable to open connection", e);
			}
		}

		@Override
		public void setCameraConfigurationMode(CameraConfigurationMode cameraConfigurationMode) {
			try {
				if (cameraConfigurationMode == CameraConfigurationMode.absorption && !frozen) {
					refreshSnapshot(false);
					frozen = true;

					roiSelectionRegion.setActive(false);

					highFluxRegion.setActive(true);
					lowFluxRegion.setActive(true);
					calculateRatio();
				} else if (cameraConfigurationMode == CameraConfigurationMode.exposure && frozen) {
					plottingComposite.connect();
					frozen = false;

					highFluxRegion.setActive(false);
					lowFluxRegion.setActive(false);

					roiSelectionRegion.setActive(true);
				}
			} catch (LiveStreamException e) {
				MessageDialog.openError(plottingComposite.getShell(), "Imaging Camera",
						"Error connecting to imaging camera");
				log.error("Error connecting to imaging camera", e);
			} catch (Exception e) {
				log.error("Error recreating ROIs", e);
			}
		}
	}
	
	private class CameraRegionOfInterestListener extends CameraConfigurationAdapter {
		@Override
		public void clearRegionOfInterest() {
			roiSelectionRegion.clear();

			updateImageSizeLabels();
		}

		@Override
		public void setROI(RectangularROI roi) {
			updateImageSizeLabels();

			if (ignoreControllerUpdate) {
				ignoreControllerUpdate = false;
				return;
			}

			RectangularROI currentRoi = controller.getCurrentRoi();
			RectangularROI imageRoi = roi.copy();

			imageRoi.setPoint(imageRoi.getIntPoint()[0] - currentRoi.getIntPoint()[0],
					imageRoi.getIntPoint()[1] - currentRoi.getIntPoint()[1]);
			roiSelectionRegion.setRegion(imageRoi);

			highFluxRegion.clear();
			lowFluxRegion.clear();
		}
	}

	public CameraImageComposite(Composite parent, AbstractCameraConfigurationController controller, int style) throws GDAClientException {
		super(parent, style);
		this.controller = controller;
		ModeListener modeListener = new ModeListener();
		controller.addListener(modeListener);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View");
		plottingComposite.setShowTitle(true);
		plottingSystem = plottingComposite.getPlottingSystem();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);

		roiSelectionRegion = new DrawableRegion(plottingSystem, SWTResourceManager.getColor(SWT.COLOR_GREEN), "ROI",
				new ROIListener());
		highFluxRegion = new DrawableRegion(plottingSystem, SWTResourceManager.getColor(SWT.COLOR_RED), "High Flux",
				new RegionListener());
		lowFluxRegion = new DrawableRegion(plottingSystem, SWTResourceManager.getColor(SWT.COLOR_BLUE), "Low Flux",
				new RegionListener());

		roiSelectionRegion.setActive(true);

		addListener(SWT.Dispose, e -> {
			plottingComposite.disconnect();
			controller.removeListener(modeListener);
		});
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
	}

	private void updateImageSizeLabels() {
		try {
			RectangularROI currentRoi = controller.getCurrentRoi();
			if (currentRoi.getLength(0) == 0.0 && currentRoi.getLength(1) == 0.0) {
				currentRoi = controller.getMaximumSizedROI();
			}
			widthLabel.setText(String.format("%4.0f", currentRoi.getLength(0)));
			heightLabel.setText(String.format("%4.0f", currentRoi.getLength(1)));
		} catch (DeviceException e) {
			widthLabel.setText("Err");
			heightLabel.setText("Err");
		}
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	private int getRawData(DrawableRegion regionWrapper) {
		IRectangularROI roi = regionWrapper.getRegion();
		int[] xy = roi.getIntPoint();
		int[] length = roi.getIntLengths();

		int[] start = new int[] { xy[0], xy[1] };
		int[] end = new int[] { xy[0] + length[0], xy[1] + length[1] };
		int[] step = new int[] { 1, 1 };

		Dataset dataset = DatasetUtils.convertToDataset(lastSnapshot.getDataset().getSliceView(start, end, step));

		double val = 0;
		int count = 0;
		IndexIterator iterator = dataset.getIterator();
		while (iterator.hasNext()) {
			double value = dataset.getElementDoubleAbs(iterator.index);
			if (!Double.isNaN(value)) {
				val += value;
				count++;
			}
		}
		if (count == 0) {
			count = 1;
		}
		return (int) Math.round(val / count);
	}

	private void calculateRatio() {
		if (lowFluxRegion.getRegion() == null || highFluxRegion.getRegion() == null || lastSnapshot == null) {
			return;
		}

		int low = getRawData(lowFluxRegion);
		int high = getRawData(highFluxRegion);
		controller.calculateRatio(high, low);
	}
}
