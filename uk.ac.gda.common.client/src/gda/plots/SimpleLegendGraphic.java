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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.block.BlockResult;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.title.LegendGraphic;

/**
 * Extends LegendGraphic so that instead of just being something that gets drawn it also has a ChartEntity (actually a
 * SimpleLegendEntity) associated with it so that it can detect mouse clicks.
 */
class SimpleLegendGraphic extends LegendGraphic {
	private SimpleXYSeries sxys;

	/**
	 * Extends the LegendGraphic constructor to allow a SimpleXYSeries to be specified.
	 *
	 * @param shape
	 *            the Shape which is drawn
	 * @param fillPaint
	 *            Paint used for filling the shape
	 * @param sxys
	 *            the SimpleXYSeries
	 */
	SimpleLegendGraphic(Shape shape, Paint fillPaint, SimpleXYSeries sxys) {
		super(shape, fillPaint);
		this.sxys = sxys;
	}

	/**
	 * Overrides the super class method but is the same except that it creates a SimpleLegendEntity for itself and then
	 * returns a non-null BlockResult containing the entity.
	 *
	 * @param g2
	 *            the Graphics2D to use
	 * @param area
	 *            the area to draw in
	 * @param params
	 *            ignored
	 * @return a BlockResult containing a StandardEntityCollection containing the SimpleLegendEntity
	 */
	@Override
	public Object draw(Graphics2D g2, Rectangle2D area, Object params) {

		draw(g2, area);

		ChartEntity entity = null;
		// This was written by looking at what happens in LabelBlock's draw
		// method. The area has to be transformed into the actual coordinates
		// of the overall ChartPanel for the MouseEvents to work properly.
		entity = new SimpleLegendEntity(g2.getTransform().createTransformedShape(area), sxys.getName(), null, sxys);

		BlockResult result = new BlockResult();
		StandardEntityCollection sec = new StandardEntityCollection();
		sec.add(entity);
		result.setEntityCollection(sec);
		return result;
	}
}
