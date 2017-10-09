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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.block.BlockResult;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.StandardEntityCollection;

/**
 * Extends LabelBlock so that it can carry a reference to a SimpleXYSeries for use in Legends.
 */
class SimpleLegendLabelBlock extends LabelBlock {
	private SimpleXYSeries sxys;

	/**
	 * Extends the LabelBlock constructor to allow a SimpleXYSeries to be specified.
	 *
	 * @param text
	 *            the text
	 * @param font
	 *            the Font
	 * @param paint
	 *            the Paint
	 * @param sxys
	 *            the SimpleXYSeries
	 */
	SimpleLegendLabelBlock(String text, Font font, Paint paint, SimpleXYSeries sxys) {
		super(text, font, paint);
		this.sxys = sxys;
	}

	/**
	 * Overrides super class method to provide a BlockResult which contains a SimpleLegendEntity instead of an ordinary
	 * ChartEntity.
	 *
	 * @param g2
	 *            the Graphics to use
	 * @param area
	 *            the area to draw in
	 * @param params
	 *            ignored
	 * @return a BlockResult containing the SimpleLegendEntity
	 */
	@Override
	public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
		// It was impossible to reuse the super class code because of
		// private fields with no accessor methods. So run the method
		// then replace the EntityCollection with one containing a
		// SimpleLegendEntity. The super class method may return null
		// (e.g. when being drawn for printing). If it does then we
		// should too.
		BlockResult result = (BlockResult) super.draw(g2, area, params);
		if (result != null) {
			StandardEntityCollection sec = new StandardEntityCollection();
			ChartEntity ce = result.getEntityCollection().getEntity(0);
			sec.add(new SimpleLegendEntity(ce, sxys));
			result.setEntityCollection(sec);
		}
		return result;
	}

}
