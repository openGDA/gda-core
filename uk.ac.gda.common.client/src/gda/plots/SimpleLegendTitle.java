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

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.CenterArrangement;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.title.LegendGraphic;
import org.jfree.chart.title.LegendTitle;

/**
 * SimpleLegendTitle Class
 */
class SimpleLegendTitle extends LegendTitle {
	/**
	 * Exactly the same as the SuperClass constructor - necessary because there is no default constructor in
	 * LegendTitle.
	 *
	 * @param source
	 *            a source of LegendItems
	 */
	SimpleLegendTitle(LegendItemSource source) {
		super(source, new FlowArrangement(), new ColumnArrangement());
	}

	/**
	 * Overrides the super class method to create a SimpleLegendGraphic instead of a LegendGraphic and a
	 * SimpleLegendLabelBlock instead of a LabelBlock (otherwise identical except private field names replaced with
	 * method calls). Assumes it will be passed a SimpleLegendItem and uses it to get the SimpleXYSeries to pass on to
	 * the SimpleLegendGraphic and SimpleLegendLabelBlock.
	 *
	 * @param item
	 *            the LegendItem for which the LegendItemBlock is to be created
	 * @return the LegendItemBlock created
	 */
	@Override
	protected Block createLegendItemBlock(LegendItem item) {
		BlockContainer result = null;
		if (item instanceof SimpleLegendItem) {
			LegendGraphic lg = new SimpleLegendGraphic(item.getShape(), item.getFillPaint(), ((SimpleLegendItem) item)
					.getSeries());
			lg.setShapeFilled(item.isShapeFilled());
			lg.setLine(item.getLine());
			lg.setLineStroke(item.getLineStroke());
			lg.setLinePaint(item.getLinePaint());
			lg.setLineVisible(item.isLineVisible());
			lg.setShapeVisible(item.isShapeVisible());
			lg.setShapeOutlineVisible(item.isShapeOutlineVisible());
			lg.setOutlinePaint(item.getOutlinePaint());
			lg.setOutlineStroke(item.getOutlineStroke());
			lg.setPadding(getLegendItemGraphicPadding());

			BlockContainer legendItem = new BlockContainer(new BorderArrangement());
			lg.setShapeAnchor(getLegendItemGraphicAnchor());
			lg.setShapeLocation(getLegendItemGraphicLocation());
			legendItem.add(lg, getLegendItemGraphicEdge());
			LabelBlock labelBlock = new SimpleLegendLabelBlock(item.getLabel(), getItemFont(), getItemPaint(),
					((SimpleLegendItem) item).getSeries());
			labelBlock.setPadding(getItemLabelPadding());
			labelBlock.setToolTipText(item.getLabel());
			// labelBlock.setToolTipText("click");
			legendItem.add(labelBlock);

			result = new BlockContainer(new CenterArrangement());
			result.add(legendItem);
		}

		return result;
	}
}
