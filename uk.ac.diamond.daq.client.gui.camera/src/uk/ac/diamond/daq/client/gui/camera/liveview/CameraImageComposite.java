package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationMode;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

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
	
	private RegionWrapper roiSelectionRegion;
	private RegionWrapper highFluxRegion;
	private RegionWrapper lowFluxRegion;

	private boolean ignoreControllerUpdate = false;
	
	private class ROIListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent event) {
			//Do nothing
		}

		@Override
		public void roiChanged(ROIEvent event) {
			RectangularROI roi = ((RectangularROI)event.getROI().getBounds()).copy();
			RectangularROI currentRoi = controller.getCurrentRoi();
			
			roi.setPoint(roi.getIntPoint()[0] + currentRoi.getIntPoint()[0], roi.getIntPoint()[1] + currentRoi.getIntPoint()[1]);
			ignoreControllerUpdate = true;
			controller.setROI(roi);
		}

		@Override
		public void roiSelected(ROIEvent event) {
			//do nothing
		}
	}
	
	private class RegionListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent event) {
			//do nothing
		}
		
		private int getRawData (RegionWrapper regionWrapper) {
			IRectangularROI roi = regionWrapper.region.getROI().getBounds();
			int[] xy = roi.getIntPoint();
			int[] length = roi.getIntLengths();
			
			int[] start = new int[]{ xy[0], xy[1] };
			int[] end = new int[]{ xy[0] + length[0], xy[1] + length[1] };
			int[] step = new int[]{ 1, 1 };
			
			Dataset dataset = DatasetUtils.convertToDataset(
					lastSnapshot.getDataset().getSliceView(start, end, step));
			
			double val = 0;
			int count = 0;
			IndexIterator iterator = dataset.getIterator();
			while (iterator.hasNext()) {
				double value = dataset.getElementDoubleAbs(iterator.index);
				if (!Double.isNaN(value)) {
					val+=value;
					count++;
				}
			}
			if (count == 0) {
				count = 1;
			}
			return (int)Math.round(val / count);
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			if (lowFluxRegion.region == null || highFluxRegion.region == null || lastSnapshot == null) {
				return;
			}
			
			int low = getRawData(lowFluxRegion);
			int high = getRawData(highFluxRegion);
			controller.calculateRatio(high, low);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			//do nothing
		}
		
	}
		
	private class RegionWrapper {
		private IRegion region = null;
		private Color color;
		private String name;
		private IROIListener roiListener;
		
		private RegionWrapper(Color color, String name, IROIListener roiListener) {
			super();
			this.color = color;
			this.name = name;
			this.roiListener = roiListener;
		}
		
		void create (boolean newRegion) throws Exception {
			remove(newRegion);
			region = plottingSystem.getRegion(name);
			if (region == null && newRegion) {
				region = plottingSystem.createRegion(name, RegionType.BOX);
				region.setRegionColor(color);
				if (roiListener != null) {
					region.addROIListener(roiListener);
				}
				log.info("Creating ROI: {}", name);
			} else if (region != null) {
				log.info("Showing ROI: {}", name);
				region.setVisible(true);
			}
		}
		
		void remove(boolean delete) {
			region = plottingSystem.getRegion(name);
			if (region != null) {
				if (delete) {
					log.info("Deleting ROI: {}", name);
					plottingSystem.removeRegion(region);
					region = null;
				} else {
					log.info("Hiding ROI: {}", name);
					region.setVisible(false);
				}
			} else {
				log.info("region {} not found", name);
			}
		}
		
		void change (RectangularROI roi) {
			region = plottingSystem.getRegion(name);
			if (region != null) {
				IROI iroi = region.getROI();
				if (iroi instanceof RectangularROI) {
					RectangularROI rectangularROI = (RectangularROI)iroi;
					rectangularROI.setPoint(roi.getPoint());
					rectangularROI.setLengths(roi.getLengths());
				}
			}
		}
	}
	
	private class ClickListener implements IClickListener {
		private RegionWrapper lastCreated = lowFluxRegion;
		
		private boolean inROI (String name, double xValue, double yValue) {
			IRegion region = plottingSystem.getRegion(name);
			if (region == null) {
				return false;
			}
			IRectangularROI rectROI = region.getROI().getBounds();
			return rectROI.getPointX() <= xValue && xValue < rectROI.getPointX() + rectROI.getLength(0)
					&& rectROI.getPointY() <= yValue && yValue < rectROI.getPointY() + rectROI.getLength(1);
		}
		
		@Override
		public void clickPerformed(ClickEvent event) {
			try {
				if (!frozen) {
					if (inROI(roiSelectionRegion.name, event.getxValue(), event.getyValue())) {
						return;
					}
					roiSelectionRegion.create(true);
				} else {
					if (lastCreated == highFluxRegion) {
						lowFluxRegion.create(true);
						lastCreated = lowFluxRegion;
					} else {
						highFluxRegion.create(true);
						lastCreated = highFluxRegion;
					}
				}
			} catch (Exception e) {
				log.error("Failed to add region", e);
			}
		}

		@Override
		public void doubleClickPerformed(ClickEvent evt) {
			// Not required
		}
	}
	
	public class ModeListener extends CameraConfigurationAdapter {
		@Override
		public void setCameraConfigurationMode(CameraConfigurationMode cameraConfigurationMode) {
			try {
				if (cameraConfigurationMode == CameraConfigurationMode.absorption && !frozen) {
					ITrace liveTrace = plottingComposite.getITrace();
					lastSnapshot = new SnapshotData("Adsorption Snapshot", liveTrace.getData().clone());
					plottingComposite.disconnect();
					plottingSystem.clear();
					plottingSystem.createPlot2D(lastSnapshot.getDataset(), null, "Snap!", new NullProgressMonitor());
					plottingSystem.setTitle(lastSnapshot.getTitle());
					frozen = true;
					
					roiSelectionRegion.remove(false);
	
					highFluxRegion.create(false);
					lowFluxRegion.create(false);
				} else if (cameraConfigurationMode == CameraConfigurationMode.exposure && frozen) {
					plottingComposite.connect();
					frozen = false;
					
					highFluxRegion.remove(false);
					lowFluxRegion.remove(false);
					
					roiSelectionRegion.create(false);
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
			roiSelectionRegion.remove(true);
			
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
			roiSelectionRegion.change(imageRoi);
		}
	}
	
	public CameraImageComposite(Composite parent, AbstractCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection, int style) throws Exception {
		super(parent, style);
		
		this.controller = controller;
		ModeListener modeListener = new ModeListener();
		controller.addListener(modeListener);
		
		roiSelectionRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_GREEN), "ROI", new ROIListener());
		highFluxRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_RED), "High Flux", new RegionListener());
		lowFluxRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_BLUE), "Low Flux", new RegionListener());

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View", liveStreamConnection);
		plottingComposite.setShowTitle(true);
		plottingSystem = plottingComposite.getPlottingSystem();
		plottingSystem.addClickListener(new ClickListener());
		if (!liveStreamConnection.isConnected()) {
			plottingComposite.connect();
		}
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);

		addListener(SWT.Dispose, e -> {
			plottingComposite.disconnect();
			controller.removeListener(modeListener);
		});
		
		Composite imageSizeComposite = createImageSizeComposite();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(imageSizeComposite);
		
		CameraRegionOfInterestListener cameraRegionOfInterestListener = new CameraRegionOfInterestListener();
		cameraRegionOfInterestListener.setROI(controller.getROI());
		controller.addListener(cameraRegionOfInterestListener);
		
		updateImageSizeLabels();
	}
	
	private Composite createImageSizeComposite () {
		Label label;
		Composite composite = new Composite(this, SWT.NONE);
		
		GridLayoutFactory.swtDefaults().numColumns(5).applyTo(composite);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Width: ");
		GridDataFactory.swtDefaults().applyTo(label);
		
		widthLabel = new Label(composite, SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(IMAGE_DIMENSION_LABEL_WIDTH, SWT.DEFAULT).applyTo(widthLabel);
		
		label = new Label(composite, SWT.NONE);
		label.setText(" px, Height: ");
		GridDataFactory.swtDefaults().applyTo(label);
		
		heightLabel = new Label(composite, SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(IMAGE_DIMENSION_LABEL_WIDTH, SWT.DEFAULT).applyTo(heightLabel);
		
		label = new Label(composite, SWT.NONE);
		label.setText(" px");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		return composite;
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
}
