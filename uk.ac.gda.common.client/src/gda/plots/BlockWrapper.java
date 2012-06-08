/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.plots;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;

import org.jfree.chart.block.Block;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.title.Title;
import org.jfree.ui.Size2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for wrapping any arbitrary JComponent so that it can appear on a SimplePlot. The code trying out this
 * class is currently commented out so it will appear to be unused - PLEASE DO NOT REMOVE IT.
 */
public class BlockWrapper extends Title implements Block, ImageObserver {
	private static final Logger logger = LoggerFactory.getLogger(BlockWrapper.class);

	JComponent jc;

	/**
	 * Constructor.
	 * 
	 * @param jc
	 *            the component which is to be wrapped
	 */
	public BlockWrapper(JComponent jc) {
		this.jc = jc;
	}

	/**
	 * @return should return the ID (currently returns null)
	 */
	@Override
	public String getID() {
		return null;
	}

	/**
	 * Should set the ID (currently not working)
	 * 
	 * @param id
	 *            the id
	 */
	@Override
	public void setID(String id) {
	}

	/**
	 * Usually this returns the Size2D that the object would like to have given the constraints applied - we just return
	 * the width and height of our wrapped JComponent.
	 * 
	 * @param g2
	 *            the Graphics2D into which the object will be drawn
	 * @return a Size2D that the object requires for successful drawing.
	 */
	@Override
	public Size2D arrange(Graphics2D g2) {
		return arrange(g2, null);
	}

	/**
	 * Usually this returns the Size2D that the object would like to have given the constraints applied - we just return
	 * the width and height of our wrapped JComponent.
	 * 
	 * @param g2
	 *            the Graphics2D into which the object will be drawn
	 * @param constraint
	 *            a set of constraints (ignored here)
	 * @return a Size2D that the object requires for successful drawing.
	 */
	@Override
	public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
		logger.debug("BW constraint is " + constraint);
		Size2D rtrn = new Size2D(jc.getWidth(), jc.getHeight());
		logger.debug("BW rtrn is " + rtrn);
		Dimension d = jc.getPreferredSize();
		rtrn = new Size2D(d.getWidth(), d.getHeight());
		logger.debug("BW rtrn is " + d);
		return rtrn;
	}

	/**
	 * Returns the current bounds of the block (currently null).
	 * 
	 * @return The bounds.
	 */
	@Override
	public Rectangle2D getBounds() {
		logger.debug("AHAHAHHA");
		return null;
	}

	/**
	 * Sets the bounds of the block (currently does nothing).
	 * 
	 * @param bounds
	 *            the bounds.
	 */
	@Override
	public void setBounds(Rectangle2D bounds) {
		// Deliberately does nothing
	}

	/**
	 * JFreeChart will actually call this from its drawTitle method expecting the Block to draw itself at this point.
	 * Our JComponent will already have been drawn as a consequence of being a simple child of the ChartPanel (which
	 * extends JPanel). However we can use the information passed to the Block here to get the JComponents bounds set
	 * correctly.
	 * 
	 * @param g2
	 *            the Graphics2D into which to draw (not used here)
	 * @param area
	 *            the Rectangle2D into which the object should draw itself)
	 * @param params
	 *            some parameters not yet understood
	 * @return an Object else null
	 */
	@Override
	public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
		logger.debug("BW area is " + area);
		logger.debug("BW params is " + params);
		logger.debug("BW g2 is " + g2);
		logger.debug("BW g2.transform is " + g2.getTransform());
		jc.setBounds((int) Math.round(area.getX()), (int) Math.round(area.getY()), (int) Math.round(area.getWidth()),
				(int) Math.round(area.getHeight()));

		return null;
	}

	/**
	 * Draws the block within the specified area. Refer to the documentation for the implementing class for information
	 * about the <code>params</code> and return value supported.
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param area
	 *            the area.
	 */
	@Override
	public void draw(Graphics2D g2, Rectangle2D area) {
		// Deliberately does nothing
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false;
	}

}
