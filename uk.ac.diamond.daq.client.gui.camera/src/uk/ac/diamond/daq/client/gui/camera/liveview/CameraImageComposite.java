package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationMode;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

public class CameraImageComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(CameraImageComposite.class);
	
	private static final String IMAGE_DIMENSION_FORMAT = "%6.2fmm";

	private AbstractCameraConfigurationController controller;
	private LivePlottingComposite plottingComposite;
	private IPlottingSystem<Composite> plottingSystem;
	private boolean frozen = false;
	
	private SnapshotData lastSnapshot;
	
	private Label xLabel;
	private Label yLabel;
	private Label widthLabel;
	private Label heightLabel;
	
	private RegionWrapper roiSelectionRegion;
	private RegionWrapper highFluxRegion;
	private RegionWrapper lowFluxRegion;
	
	private class ROIListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent event) {
			//Not needed
		}

		@Override
		public void roiChanged(ROIEvent event) {
			IRectangularROI roi = event.getROI().getBounds();
			xLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, roi.getPointX()));
			yLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, roi.getPointY()));
			widthLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, roi.getLength(0)));
			heightLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, roi.getLength(1)));
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			//Not needed
		}
	}
	
	private class RegionListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent evt) {
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
			}
		}
	}
	
	private class ClickListener implements IClickListener {
		private RegionWrapper lastCreated = lowFluxRegion;
		@Override
		public void clickPerformed(ClickEvent event) {
			try {
				if (!frozen) {
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
	
	public CameraImageComposite(Composite parent, AbstractCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection, CameraConfiguration cameraConfiguration, 
			int style) throws Exception {
		super(parent, style);
		
		this.controller = controller;
		ModeListener modeListener = new ModeListener();
		controller.addListener(modeListener);
				
		roiSelectionRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_GREEN), "ROI", new ROIListener());
		highFluxRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_RED), "High Flux", new RegionListener());
		lowFluxRegion = new RegionWrapper(SWTResourceManager.getColor(SWT.COLOR_BLUE), "Low Flux", new RegionListener());

		Label label;

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View", liveStreamConnection);
		plottingComposite.setShowAxes(cameraConfiguration.getCalibratedAxesProvider() != null);
		plottingComposite.setShowTitle(true);
		plottingSystem = plottingComposite.getPlottingSystem();
		plottingSystem.addClickListener(new ClickListener());
		if (!liveStreamConnection.isConnected()) {
			plottingComposite.connect();
		}
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);

		Composite roiPanel = new Composite(this, SWT.None);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(roiPanel);

		GridLayoutFactory.fillDefaults().numColumns(6).applyTo(roiPanel);

		Button roiButton = new Button(roiPanel, SWT.TOGGLE);
		roiButton.setText("ROI");
		GridDataFactory.swtDefaults().span(1, 2).applyTo(roiButton);

		// expanding label
		label = new Label(roiPanel, SWT.LEFT);
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.FILL, SWT.CENTER).applyTo(label);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("X:");
		GridDataFactory.swtDefaults().applyTo(label);

		xLabel = new Label(roiPanel, SWT.LEFT);
		xLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, 0.0));
		GridDataFactory.swtDefaults().applyTo(xLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("Width:");
		GridDataFactory.swtDefaults().applyTo(label);

		widthLabel = new Label(roiPanel, SWT.LEFT);
		widthLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, 0.0));
		GridDataFactory.swtDefaults().applyTo(widthLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("Y:");
		GridDataFactory.swtDefaults().applyTo(label);

		yLabel = new Label(roiPanel, SWT.LEFT);
		yLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, 0.0));
		GridDataFactory.swtDefaults().applyTo(yLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("Height:");
		GridDataFactory.swtDefaults().applyTo(label);

		heightLabel = new Label(roiPanel, SWT.LEFT);
		heightLabel.setText(String.format(IMAGE_DIMENSION_FORMAT, 0.0));
		GridDataFactory.swtDefaults().applyTo(heightLabel);

		addListener(SWT.Dispose, e -> {
			plottingComposite.disconnect();
			controller.removeListener(modeListener);
		});
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}
}
