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
import org.eclipse.swt.widgets.Control;
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

	private static final int BIN_SIZE = 255;

	private static final double BIN_DOUBLE = BIN_SIZE;

	private final static Logger logger = LoggerFactory.getLogger(HistogramAdjuster.class);

	private int lowValue = 0;
	private int maxIntensity = 65536;
	private int highValue = maxIntensity;

	private ImageData workingImageData;

	/**
	 * This should be the clone of the image data and not the data itself.
	 * 
	 * @param imgData
	 */
	public void setImageData(ImageData imgData) {
		this.workingImageData = imgData;
	}

	public void dispose() {
		workingImageData = null;
	}

	public void setMaxIntensity(int maxIntensity) {
		this.maxIntensity = maxIntensity;
	}

	private UpdateHistogramJob updateHJob;

	private class UpdateHistogramJob extends UIJob {

		private FixedImageViewerComposite fullImageComposite;
		private int scaledX;
		private int scaledY;

		public UpdateHistogramJob(Display jobDisplay, String name) {
			super(jobDisplay, name);
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

			imgData = (ImageData) HistogramAdjuster.this.workingImageData.clone();
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

			logger.debug("Updating image");
			try {
				fullImageComposite.loadMainImage(imgData.scaledTo(scaledX, scaledY));
			} catch (Exception e) {
				logger.error("TODO put description of error here", e);
			}
			return Status.OK_STATUS;
		}
	}

	public void updateHistogramValues(final FixedImageViewerComposite fullImageComposite, int scaledX, int scaledY,
			double lowerLimit, double upperLimit) {

		this.lowValue = (int) lowerLimit;
		this.highValue = (int) upperLimit;

		UpdateHistogramJob job = getJob(fullImageComposite);
		job.setDisplayWindow(fullImageComposite);
		job.setScaledX(scaledX);
		job.setScaledY(scaledY);

		job.cancel();
		job.schedule(100);
	}

	private UpdateHistogramJob getJob(Control control) {
		if (updateHJob == null) {
			updateHJob = new UpdateHistogramJob(control.getDisplay(), "Update histogram");
		}
		return updateHJob;
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public double getMinIntensity() {
		return 0;
	}
}
