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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.ImageLocationRelTheta;

/**
 * Canvas to show the stitched image
 */
public class StitchedImageCanvas extends Canvas {

	private Image screenImage;
	@SuppressWarnings("unused")
	private double scaleX;
	@SuppressWarnings("unused")
	private double scaleY;

	private static final Logger logger = LoggerFactory.getLogger(StitchedImageCanvas.class);
	@SuppressWarnings("unused")
	private List<StitchConfig> stitchConfig;
	private final ImageLocationRelTheta imgLocRelTheta;
	private ArrayList<StitchConfig> stitchConfigs = new ArrayList<StitchedImageCanvas.StitchConfig>(1);

	public StitchedImageCanvas(final Composite parent, ImageLocationRelTheta imgLocRelTheta, int style) {
		super(parent, style | SWT.BORDER | SWT.NO_BACKGROUND);
		this.imgLocRelTheta = imgLocRelTheta;

		addPaintListener(new PaintListener() { // paint listener.
			@Override
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});

		this.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
			}
		});
	}

	/* Paint function */
	@SuppressWarnings("unused")
	private void paint(GC gc) {
		Rectangle clientRect = getClientArea();
		if (!stitchConfigs.isEmpty()) {
			// Rectangle imageRect = SWT2Dutil.inverseTransformRect(transform, clientRect);
			// int gap = 2; /* find a better start point to render */
			int gap = 0;
			// imageRect.x -= gap;
			// imageRect.y -= gap;
			// imageRect.width += 2 * gap;
			// imageRect.height += 2 * gap;
			int numImages = stitchConfigs.size();
			if (screenImage != null) {
				screenImage.dispose();
			}
			screenImage = new Image(getDisplay(), clientRect.width, clientRect.height);
			GC newGC = new GC(screenImage);
			newGC.setClipping(clientRect);
			
			for (StitchConfig sc : stitchConfigs) {
				Rectangle imageBound = sc.getImage().getBounds();
				logger.debug("Source image rect:{}", imageBound);
				// imageRect = imageRect.intersection(imageBound);
				Rectangle destRect = clientRect;// SWT2Dutil.transformRect(transform, imageRect);

				// newGC.drawImage(sourceImage, imageRect.x, imageRect.y, imageRect.width, imageRect.height, destRect.x,
				// destRect.y, destRect.width, destRect.height);

				newGC.drawImage(sc.getImage(), clientRect.x, clientRect.y, clientRect.width, clientRect.height,
						destRect.x, destRect.y, destRect.width, destRect.height);

				gc.drawImage(screenImage, 0, 0);
			}
			newGC.dispose();
		} else {
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
			gc.drawText("No image to display", 100, 100);
		}
	}

	@Override
	public void dispose() {
		try {
			if (screenImage != null) {
				screenImage.dispose();
			}
		} catch (Exception ex) {
			logger.error("error disposing composites");
		}
	}

	/**
	 * Reload image from a file
	 */

	public void loadImage(List<StitchConfig> stitchConfig) {
		this.stitchConfig = stitchConfig;
	}

	public static class StitchConfig {
		private String id;

		private String imageLocationAt0;

		private String imageLocationAt90;

		private double verticalMotor;

		private double horizontalX;

		private double horizontalZ;

		private double theta;

		private double objectPixelSize;

		private Image image;

		public String getImageLocationAt0() {
			return imageLocationAt0;
		}

		public void setImageLocationAt0(String imageLocation) {
			this.imageLocationAt0 = imageLocation;
		}

		public double getVerticalMotor() {
			return verticalMotor;
		}

		public void setVerticalMotor(double verticalMotor) {
			this.verticalMotor = verticalMotor;
		}

		public double getHorizontalX() {
			return horizontalX;
		}

		public void setHorizontalX(double horizontalX) {
			this.horizontalX = horizontalX;
		}

		public double getHorizontalZ() {
			return horizontalZ;
		}

		public void setHorizontalZ(double horizontalZ) {
			this.horizontalZ = horizontalZ;
		}

		public double getTheta() {
			return theta;
		}

		public void setTheta(double theta) {
			this.theta = theta;
		}

		public double getObjectPixelSize() {
			return objectPixelSize;
		}

		public void setObjectPixelSize(double objectPixelSize) {
			this.objectPixelSize = objectPixelSize;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getImageLocationAt90() {
			return imageLocationAt90;
		}

		public void setImageLocationAt90(String imageLocationAt90) {
			this.imageLocationAt90 = imageLocationAt90;
		}

		public Image getImage() {
			return image;
		}

		public void setImage(Image img) {
			this.image = img;
		}
	}

	public void setStitchConfigs(ArrayList<StitchConfig> stitchConfigs) {
		disposePreviousStitchConfigs();
		this.stitchConfigs = stitchConfigs;
		evaluateStitchConfigs();
		redraw();
	}

	private void disposePreviousStitchConfigs() {
		for (StitchConfig sc : stitchConfigs) {
			if (sc.getImage() != null && !sc.getImage().isDisposed()) {
				sc.getImage().dispose();
			}
		}
	}

	private void evaluateStitchConfigs() {
		for (StitchConfig sc : stitchConfigs) {
			if (sc.getImage() != null && !sc.getImage().isDisposed()) {
				sc.getImage().dispose();
			}
			String imageLocation = null;
			if (imgLocRelTheta != null && imgLocRelTheta == ImageLocationRelTheta.THETA) {
				imageLocation = sc.getImageLocationAt0();
			} else {
				imageLocation = sc.getImageLocationAt90();
			}
			sc.setImage(new Image(getDisplay(), imageLocation));
		}
	}
}