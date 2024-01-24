package org.opengda.detector.electronanalyser.client.sequenceeditor;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	private double xRaySourceEnergyLimit = 2100.0; // must be in eV
	private boolean sourceSelectable = false;
	private Camera camera;
	private Image defaultScalingImage = null;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SequenceViewLabelProvider.class);

	public SequenceViewLabelProvider() {
		//Bug 560298 - [GTK] SWT-ImageLoader load animated Gif produce wrong index order
		//Image order appears to be random via api ImageLoader.load method on linux only.
		//Work around is to load each frame data individually and recombine into single array
		//to get correct order
		Image[] runningIconFrames = new Image[] {
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_1),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_2),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_3),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_4),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_5),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_6),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_7),
			ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUNNING_FRAME_8)
		};

		AnimationHandler.getInstance().setImageFrames(runningIconFrames);
		AnimationHandler.getInstance().setFramesPerSecond(2);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Region region) {
			if (columnIndex == SequenceTableConstants.COL_VALID) {
				return getColumnValidImage(region);
			} else if (columnIndex == SequenceTableConstants.COL_STATUS) {
				return getColumnStatusImage(region);
			} else if (columnIndex == SequenceTableConstants.COL_ENABLED) {
				return getIsColumnEnabledImage(region);
			}
		}
		return null;
	}

	private Image getColumnValidImage(Region region) {
		if (region.isEnabled()) {
			if (region.getStatus() == STATUS.INVALID) {
				return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_INVALID_REGION_STATE);
			} else {
				return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_VALID_REGION_STATE);
			}
		}
		else {
			//This is a workaround for image scaling problem in the table. The first image in table determines the scaling for all images
			//in table. These images have different pixel sizes. The green and red images should be the size dictator, however because
			//it is only shown when a region is enabled, it wasn't always consistently being drawn first and therefore would lead to bug
			//where the image sizes changed shape depending on which sequence file was loaded. Workaround is to always load an image,
			//but if not enabled make it 100% transparent. This way you always get the same size icons (24x24 pixels).
			if (defaultScalingImage == null) {
				ImageData imageData = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_VALID_REGION_STATE).getImageData();
				imageData.alpha = 0;
				//Keep hold of instance as we must be responsible for disposing of the image
				defaultScalingImage = new Image(Display.getCurrent(), imageData);
			}
			return defaultScalingImage;
		}
	}

	private Image getColumnStatusImage(Region region) {
		Image image = null;

		if (region.getStatus()==STATUS.INVALID && region.isEnabled()) {
			image = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_INVALID_REGION);
		} else if (region.getStatus()==STATUS.READY && region.isEnabled()) {
			image = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUN_READY);
		} else if (region.getStatus()==STATUS.RUNNING) {
			image = AnimationHandler.getInstance().getCurrentImageFrame();
		}
		else if (region.getStatus()==STATUS.COMPLETED) {
			image = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUN_COMPLETE);
		} else if (region.getStatus()==STATUS.ABORTED) {
			image = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_RUN_FAILURE);
		} else if (region.getStatus() == STATUS.INVALID) {
			image = ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_WARNING);
		}
		return image;
	}

	private Image getIsColumnEnabledImage(Region region) {
		if (region.isEnabled()) {
			return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_CHECKED_STATE);
		} else {
			return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_UNCHECKED_STATE);
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Region region) {

			switch (columnIndex) {
			case SequenceTableConstants.COL_VALID:
				return "";

			case SequenceTableConstants.COL_STATUS:
				return "";

			case SequenceTableConstants.COL_ENABLED:
				return "";
			case SequenceTableConstants.COL_REGION_NAME:
				return region.getName();
			case SequenceTableConstants.COL_LENS_MODE:
				return region.getLensMode();
			case SequenceTableConstants.COL_PASS_ENERGY:
				return Integer.toString(region.getPassEnergy());
			case SequenceTableConstants.COL_X_RAY_SOURCE:
				if (isSourceSelectable()) {
					if (region.getExcitationEnergy() < xRaySourceEnergyLimit) {
						return "Soft";
					}
					return "Hard";
				}
				return Double.toString(region.getExcitationEnergy());
			case SequenceTableConstants.COL_ENERGY_MODE:
				return region.getEnergyMode().getLiteral();
			case SequenceTableConstants.COL_LOW_ENERGY:
				return Double.toString(region.getLowEnergy());
			case SequenceTableConstants.COL_HIGH_ENERGY:
				return Double.toString(region.getHighEnergy());
			case SequenceTableConstants.COL_ENERGY_STEP:
				return Double.toString(region.getEnergyStep());
			case SequenceTableConstants.COL_STEP_TIME:
				return Double.toString(region.getStepTime());
			case SequenceTableConstants.COL_STEPS:
				if (region.getAcquisitionMode()==ACQUISITION_MODE.SWEPT) {
					return Long.toString(calculatedSteps(region));
				} else {
					return Integer.toString(1);
				}
			case SequenceTableConstants.COL_TOTAL_TIME:
				if (region.getAcquisitionMode()==ACQUISITION_MODE.SWEPT) {
					return Double.toString(RegionStepsTimeEstimation.calculateTotalTime(region.getStepTime(), calculatedSteps(region), region.getRunMode().getNumIterations()));
				} else {
					return Double.toString(RegionStepsTimeEstimation.calculateTotalTime(region.getStepTime(), 1, region.getRunMode().getNumIterations()));
				}
			case SequenceTableConstants.COL_X_CHANNEL_FROM:
				return Integer.toString(region.getFirstXChannel());
			case SequenceTableConstants.COL_X_CHANNEL_TO:
				return Integer.toString(region.getLastXChannel());
			case SequenceTableConstants.COL_Y_CHANNEL_FROM:
				return Integer.toString(region.getFirstYChannel());
			case SequenceTableConstants.COL_Y_CHANNEL_TO:
				return Integer.toString(region.getLastYChannel());
			case SequenceTableConstants.COL_SLICES:
				return Integer.toString(region.getSlices());
			case SequenceTableConstants.COL_MODE:
				return region.getDetectorMode().getLiteral();
			}
		}
		return null;
	}

	private long calculatedSteps(Region region) {
		return RegionStepsTimeEstimation.calculateTotalSteps(
				(region.getHighEnergy() - region.getLowEnergy()),
				region.getEnergyStep(),
				camera.getEnergyResolution()*region.getPassEnergy()
						* (region.getLastXChannel() - region
								.getFirstXChannel()+1));
	}

	public double getXRaySourceEnergyLimit() {
		return xRaySourceEnergyLimit;
	}

	public void setXRaySourceEnergyLimit(double xRaySourceEnergyLimit) {
		this.xRaySourceEnergyLimit = xRaySourceEnergyLimit;
	}

	public boolean isSourceSelectable() {
		return sourceSelectable;
	}

	public void setSourceSelectable(boolean sourceSelectable) {
		this.sourceSelectable = sourceSelectable;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void dispose() {
		if (defaultScalingImage != null) {
			defaultScalingImage.dispose();
		}
		super.dispose();
	}
}