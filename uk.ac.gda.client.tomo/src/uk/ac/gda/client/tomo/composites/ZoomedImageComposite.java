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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ZoomedImageComposite extends FixedImageViewerComposite {
	private final static Logger logger = LoggerFactory.getLogger(ZoomedImageComposite.class);

	public ZoomedImageComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint) {
		org.eclipse.swt.graphics.Point computedSize = super.computeSize(wHint, hHint);
		return computedSize;
	}

	@Override
	public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint, boolean changed) {
		org.eclipse.swt.graphics.Point computedSize = super.computeSize(wHint, hHint, changed);
		return computedSize;
	}

	public void zoomUpdate(Dimension figureTopLeftRelativeImgBounds, Dimension difference) {
		// TODO-Ravi Auto-generated method stub

	}

	public void clearZoomWindow() {
		// TODO-Ravi Auto-generated method stub

	}

	/**
	 * Reload image from a provided ImageData
	 * 
	 * @param imageDataIn
	 *            ImageData
	 */
	@Override
	public void loadMainImage(final ImageData imageDataIn) {
		try {
			if (!getCanvas().isDisposed()) {
				Rectangle bounds = getCanvas().getBounds();
				int sqx = bounds.width;
				if (bounds.height < sqx) {
					sqx = bounds.height;
				}

				final Image newImage = new Image(getCanvas().getDisplay(), imageDataIn);//.scaledTo(sqx, sqx));
				if (!getCanvas().isDisposed()) {
					if (mainImage != null && !mainImage.isDisposed()) {
						mainImage.dispose();
					}
					mainImage = newImage;
					mainImgData = imageDataIn;
					mainImgFig.setImage(mainImage);
				}
			}
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
		}
	}
}
