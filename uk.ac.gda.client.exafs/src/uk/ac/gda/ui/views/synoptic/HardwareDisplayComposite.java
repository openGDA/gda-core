/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HardwareDisplayComposite {

	private static final Logger logger = LoggerFactory.getLogger(HardwareDisplayComposite.class);

	protected Composite parent;

	private String viewName;

	/** Pixel position of where the background image is located (origin for relative positions) */
	private Point imageStart = new Point(0,0);

	/** Size of image used for background image */
	private Point imageSize = new Point(0,0);

	/** Image used for background of parent composite */
	private Image backgroundImage;

	public HardwareDisplayComposite() {
	}

	public HardwareDisplayComposite(final Composite parent, int style) {
		createControls(parent, null);
	}

	public HardwareDisplayComposite(final Composite parent, int style, Layout layout) {
		createControls(parent, layout);
	}

	public void createControls(final Composite parent, Layout layout) {
		this.parent = parent;

		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,  SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite comp = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(comp);
		comp.setLayout(layout);
		try {
			createControls(comp);
		} catch (Exception e) {
			logger.error("Problem creating hardware view : ", e);
		}
		//Compute size needed to fully display the composite
		Point widgetSize = comp.computeSize(SWT.DEFAULT, SWT.DEFAULT); // does not include extent of background image -
		// increase widgetSize if using background image...
		if (imageSize!=null && imageStart!=null) {
			widgetSize.x = Math.max(widgetSize.x, imageSize.x+imageStart.x);
			widgetSize.y = Math.max(widgetSize.y, imageSize.y+imageStart.y);
		}

		// size of widget below which scroll bars get added
		scrolledComposite.setMinSize(widgetSize);

		// Dispose of the background image
		parent.addDisposeListener(e -> {
			if (backgroundImage != null) {
				backgroundImage.dispose();
			}
		});
	}

	public void setFocus() {
	}

	protected abstract void createControls(Composite parent) throws Exception;

	/**
	 * Return relative (percentage) position from absolute (pixel) position.
	 * @param x
	 * @param y
	 * @return
	 */
	private Point getRelativePositionFromAbsolute(int x, int y) {
		int relX = 100*(x - imageStart.x)/imageSize.x;
		int relY = 100*(y - imageStart.y)/imageSize.y;
		return new Point(relX, relY);
	}

	public void setBounds(Control control, int x, int y, int width) {
		Point totalSize = control.computeSize(width, SWT.DEFAULT);
		control.setBounds(x, y, totalSize.x, totalSize.y);
	}

	/**
	 * Set absolute (pixel) position of widget based on it's relative position in parent composite.
	 * @param control
	 */
	protected void setWidgetPosition(Control control, int x, int y) {
		setWidgetPosition(control, x, y, SWT.DEFAULT);
	}

	protected void setWidgetPosition(Control control, int x, int y, int width) {
		int absX = (int) (imageStart.x + 0.01 * imageSize.x * x);
		int absY = (int) (imageStart.y + 0.01 * imageSize.y * y);
		setBounds(control, absX, absY, width);
	}

	protected void setAbsoluteWidgetPosition(Control control, int x, int y) {
		setBounds(control, x, y, SWT.DEFAULT);
	}

	/**
	 * Set background image of parent composite.
	 * @param image
	 * @param imageStart position of image inside composite. Origin used for relative coordinate system.
	 */
	protected void setBackgroundImage(final Image image, final Point imageStart) {
		backgroundImage = image;
		this.imageStart = imageStart;
		imageSize = new Point(0,0);
		imageSize.x = backgroundImage.getBounds().width;
		imageSize.y = backgroundImage.getBounds().height;
	}

	/**
	 * Return new image, with dimensions {@code newsize}, with original image at location {@code startPos}
	 *
	 * @param image
	 * @param newsize
	 * @param startPos
	 * @return
	 */
	private Image getPaddedImage(Image image, Point newsize, Point startPos) {
		final Image newImage = new Image(parent.getDisplay(), newsize.x, newsize.y);
		GC gc = new GC(newImage);
		if (image != null) {
			gc.drawImage(image, startPos.x, startPos.y);
		}
		gc.dispose();
		return newImage;
	}

	protected void addResizeListener(final Composite parent) {
		parent.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				// Create background image large enough to completely fill parent -
				// to avoid having the background image tiled.
				Rectangle size = parent.getBounds();
				if (size.width > 0 && size.height > 0) {
					if (parent.getBackgroundImage() != null) {
						parent.getBackgroundImage().dispose();
					}
					Image paddedImage = getPaddedImage(backgroundImage, new Point(size.width, size.height), imageStart);
					parent.setBackgroundImage(paddedImage);
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
	}

	protected Image getImageFromPlugin(String pathToImage) throws IOException {
		URL imageURL = this.getClass().getResource("/"+pathToImage);
		return ImageDescriptor.createFromURL(imageURL).createImage();
	}

	/**
	 * Print absolute pixel position and position relative to image origin (as %)
		(useful for getting correct % position for controls, images etc.)
	 */
	protected void addMousePositionOutput(Composite parent) {
		parent.addMouseMoveListener( mouseEvent -> logger.debug("Mouse pixel position in '{}' view : {}, {}", viewName, mouseEvent.x, mouseEvent.y));
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public Color getSystemColour(int swtColour) {
		return parent.getDisplay().getSystemColor(swtColour);
	}

	/**
	 * Set background of widget to given SWT colour.
	 * @param controls
	 * @param swtColour
	 */
	public void setBackGround(Control controls, int swtColour) {
		controls.setBackground(getSystemColour(swtColour));
	}
}
