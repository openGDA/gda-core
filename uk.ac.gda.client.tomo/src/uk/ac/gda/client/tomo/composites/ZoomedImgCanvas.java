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

import java.awt.geom.AffineTransform;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.components.PointInDouble;

/**
 * Canvas to show the zoomed image.
 */
public class ZoomedImgCanvas extends Canvas {

	private Image sourceImage;
	private AffineTransform transform;
	private Image screenImage;
	private double scaleX;
	private double scaleY;

	private static final Logger logger = LoggerFactory.getLogger(ZoomedImgCanvas.class);

	public ZoomedImgCanvas(final Composite parent, int style) {
		super(parent, style | SWT.BORDER | SWT.NO_BACKGROUND);
		addPaintListener(new PaintListener() { /* paint listener. */
			@Override
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});
		this.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (sourceImage != null) {
					logger.info("Disposing source image in dispose listener");
					sourceImage.dispose();
				}
			}
		});
	}

	/* Paint function */
	private void paint(GC gc) {
		Rectangle clientRect = getClientArea(); /* Canvas' painting area */
		if (sourceImage != null) {
			Rectangle imageRect = SWT2Dutil.inverseTransformRect(transform, clientRect);
			int gap = 2; /* find a better start point to render */
			imageRect.x -= gap;
			imageRect.y -= gap;
			imageRect.width += 2 * gap;
			imageRect.height += 2 * gap;

			Rectangle imageBound = sourceImage.getBounds();
			imageRect = imageRect.intersection(imageBound);
			Rectangle destRect = SWT2Dutil.transformRect(transform, imageRect);
			if (screenImage != null) {
				screenImage.dispose();
			}
			screenImage = new Image(getDisplay(), clientRect.width, clientRect.height);
			GC newGC = new GC(screenImage);
			newGC.setClipping(clientRect);
			newGC.drawImage(sourceImage, imageRect.x, imageRect.y, imageRect.width, imageRect.height, destRect.x,
					destRect.y, destRect.width, destRect.height);
			newGC.dispose();

			gc.drawImage(screenImage, 0, 0);
		} else {
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
			gc.drawText("No image to zoom", 10, 10);
			// initScrollBars();
			if (screenImage != null) {
				screenImage.dispose();
			}
		}
	}

	private void scale(PointInDouble scale, boolean center) {
		if (sourceImage == null)
			return;
		AffineTransform af = transform;
		scaleX = scale.x;// (double) this.getBounds().width / (double) rectangle.width;
		scaleY = scale.y;// (double) this.getBounds().height / (double) rectangle.height;
		/* update transform. */
		Rectangle rect = getClientArea();
		int w = rect.width, h = rect.height;
		double dx = (((double) w)) / 2;
		double dy = (((double) h)) / 2;

		af.preConcatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
		// af.preConcatenate(AffineTransform.getTranslateInstance(-dx ,-dy));
		// af.preConcatenate(AffineTransform.getTranslateInstance(-(scaleX * dimension.width),
		// -(scaleY * dimension.height)));
		transform = af;
		if (center) {
			center();
		}
		syncScroll();
	}

	public void clearZoomWindow() {
		if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
		}

		redraw();
	}

	/* Scroll */
	public void scroll(Dimension differenceMoved) {
		double dx = -differenceMoved.width, dy = -differenceMoved.height;
		if (sourceImage == null) {
			return;
		}

		AffineTransform af = transform;
		af.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		transform = af;
		syncScroll();
	}

	/* Scroll */
	public void scroll(PointInDouble differenceMoved) {
		double dx = -differenceMoved.x, dy = -differenceMoved.y;
		if (sourceImage == null) {
			return;
		}

		AffineTransform af = transform;
		af.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		transform = af;
		syncScroll();
	}

	/**
	 * Synchronize the scrollbar with the image. If the transform is out of range, it will correct it. This function
	 * considers only following factors :<b> transform, image size, client area</b>.
	 */
	public void syncScroll() {
		if (sourceImage == null) {
			redraw();
			return;
		}

		redraw();
	}

	private void center() {
		AffineTransform af = transform;
		double sx = af.getScaleX(), sy = af.getScaleY();
		double tx = af.getTranslateX(), ty = af.getTranslateY();
		if (tx > 0) {
			tx = 0;
		}
		if (ty > 0) {
			ty = 0;
		}
		Rectangle imageBound = sourceImage.getBounds();
		int cw = getClientArea().width, ch = getClientArea().height;
		double xImageWidth = imageBound.width * sx;
		if (xImageWidth > cw) {
			int hMax = (int) xImageWidth;
			if (((int) -tx) > hMax - cw) {
				tx = -hMax + cw;
			} else {
				tx = (ch - xImageWidth) / 2; // center if too small.
			}
		}

		double yImageHeight = imageBound.height * sy;
		if (yImageHeight > ch) {
			int vMax = (int) yImageHeight;
			if (((int) -ty) > vMax - ch) {
				ty = -vMax + ch;
			} else { /* image is less higher than client area */
				ty = (ch - yImageHeight) / 2; // center if too small.
			}

		}
		/* update transform. */
		af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
		transform = af;
	}

	public void zoomUpdate(Dimension difference) {
		scroll(difference.scale(scaleX, scaleY));
	}

	@Override
	public void dispose() {
		try {
			if (screenImage != null) {
				screenImage.dispose();
			}
			if (sourceImage != null) {
				sourceImage.getImageData().data = null;
				sourceImage.dispose();
			}
			sourceImage = null;
			transform = null;
		} catch (Exception ex) {
			logger.error("error disposing composites");
		}
		//
	}

	public void loadImage(String tiffFullFileName, PointInDouble demandRawScale, boolean center) {
		if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
		}
		sourceImage = new Image(getDisplay(), tiffFullFileName);
		AffineTransform af = new AffineTransform();
		transform = af;
		scale(demandRawScale, center);
	}

	/**
	 * Reload image from a file
	 * 
	 * @param appliedSaturation
	 *            image file
	 * @param scale
	 */

	public void loadImage(ImageData appliedSaturation, PointInDouble scale, boolean center) {
		// logger.info("Loading image");
		if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
		}
		sourceImage = new Image(getDisplay(), appliedSaturation);
		AffineTransform af = new AffineTransform();
		transform = af;
		scale(scale, center);
	}

}