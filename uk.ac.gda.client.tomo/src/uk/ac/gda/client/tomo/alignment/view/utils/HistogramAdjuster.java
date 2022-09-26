/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;

/**
 * Class that adjusts the histogram values for a given image data. This takes an image data clones it and every request
 * to update the histogram returns a clone of the {@link ImageData}
 */
public class HistogramAdjuster {

	public enum UpdatingImage {
		OVERLAY, MAIN;

	}

	private static final int BIN_SIZE = 255;

	private static final double BIN_DOUBLE = BIN_SIZE;

	private final static Logger logger = LoggerFactory.getLogger(HistogramAdjuster.class);

	private int lowValue = 0;
	private int maxIntensity = 65536;
	private int highValue = maxIntensity;

	public HistogramAdjuster(Display display) {
		updateOverlayImageJob = new UpdateHistogramJob(display, "Update Overlay Image", UpdatingImage.OVERLAY);

		updateMainImageJob = new UpdateHistogramJob(display, "Update Main Image", UpdatingImage.MAIN);
	}

	public void setMaxIntensity(int maxIntensity) {
		this.maxIntensity = maxIntensity;
	}

	private UpdateHistogramJob updateMainImageJob;

	private UpdateHistogramJob updateOverlayImageJob;

	private class UpdateHistogramJob extends UIJob {

		private FixedImageViewerComposite fullImageComposite;
		private int scaledX;
		private int scaledY;
		private ImageData workingImageData;
		private final UpdatingImage updatingImage;

		public UpdateHistogramJob(Display jobDisplay, String name, UpdatingImage updatingImage) {
			super(jobDisplay, name);
			this.updatingImage = updatingImage;
		}

		public void setDisplayWindow(final FixedImageViewerComposite fullImageComposite) {
			this.fullImageComposite = fullImageComposite;
		}

		public void setScaledX(int scaledX) {
			this.scaledX = scaledX;
		}

		public void setScaledY(int scaledY) {
			this.scaledY = scaledY;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			ImageData imgData = null;
			logger.debug("Updating histogram");
			int[] valArray = new int[BIN_SIZE];
			if (workingImageData != null) {
				workingImageData.palette.getRGBs();

				int cBin = (int) (((double) lowValue / (double) maxIntensity) * BIN_DOUBLE);
				int dBin = (int) (((double) highValue / (double) maxIntensity) * BIN_DOUBLE);

				double width = dBin - cBin;

				int step = (int) (BIN_DOUBLE / width);

				RGB rgb0 = new RGB(0, 0, 0);
				PaletteData palette = workingImageData.palette;
				int pixel0 = palette.getPixel(rgb0);
				for (int i = 0; i <= cBin; i++) {
					valArray[i] = pixel0;
				}

				// RGB rgb255 = new RGB(dBin - 1, dBin - 1, dBin - 1);
				RGB rgb255 = new RGB(255, 255, 255);
				int pixel255 = palette.getPixel(rgb255);
				for (int i = dBin - 1; i < BIN_SIZE; i++) {
					valArray[i] = pixel255;
				}

				for (int i = cBin + 1; i < dBin - 1; i++) {
					int stepIntensity = (i - cBin) * step;
					RGB rgb = new RGB(stepIntensity, stepIntensity, stepIntensity);
					valArray[i] = palette.getPixel(rgb);
				}

				// Need to consider a clone because once the original data is set below the high values then it can be
				// reverted
				// for values above.

				imgData = (ImageData) this.workingImageData.clone();
				for (int h = 0; h < workingImageData.height; h++) {
					for (int w = 0; w < workingImageData.width; w++) {
						int pixel = workingImageData.getPixel(w, h);

						// to equate it to a 10 bit number of 1024 shift it right by 14
						int actualIntensityVal = pixel >> 8;
						double offset = (double) actualIntensityVal / (double) maxIntensity;
						int index = (int) (offset * BIN_SIZE);
						imgData.setPixel(w, h, valArray[index]);
					}
				}

				if (UpdatingImage.MAIN == updatingImage) {
					try {
						fullImageComposite.loadMainImage(imgData.scaledTo(scaledX, scaledY));
					} catch (Exception e) {
						logger.error("Unable to apply histogram to main image", e);
					}
				} else {
					try {
						fullImageComposite.loadOverlayImage(imgData.scaledTo(scaledX, scaledY));
					} catch (Exception e) {
						logger.error("Unable to apply histogram to overlay image", e);
					}
				}
			}
			return Status.OK_STATUS;
		}

		public void setWorkingImageData(ImageData imgData) {
			this.workingImageData = imgData;
		}
	}

	public void setMainImageData(ImageData imgData) {
		if (updateMainImageJob != null) {
			updateMainImageJob.setWorkingImageData(imgData);
		}
	}

	public void setOverlayImageData(ImageData imgData) {
		if (updateOverlayImageJob != null) {
			updateOverlayImageJob.setWorkingImageData(imgData);
		}
	}

	public void updateMainImageHistogramValues(final FixedImageViewerComposite fullImageComposite, int scaledX,
			int scaledY, double lowerLimit, double upperLimit) {

		this.lowValue = (int) lowerLimit;
		this.highValue = (int) upperLimit;

		scheduleJob(fullImageComposite, scaledX, scaledY, updateMainImageJob);
	}

	public void updateOverlayImageHistogramValues(final FixedImageViewerComposite fullImageComposite, int scaledX,
			int scaledY, double lowerLimit, double upperLimit) {

		this.lowValue = (int) lowerLimit;
		this.highValue = (int) upperLimit;

		scheduleJob(fullImageComposite, scaledX, scaledY, updateOverlayImageJob);
	}

	private void scheduleJob(final FixedImageViewerComposite fullImageComposite, int scaledX, int scaledY,
			UpdateHistogramJob job) {
		job.setDisplayWindow(fullImageComposite);
		job.setScaledX(scaledX);
		job.setScaledY(scaledY);

		job.cancel();
		job.schedule(100);
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public double getMinIntensity() {
		return 0;
	}

}
