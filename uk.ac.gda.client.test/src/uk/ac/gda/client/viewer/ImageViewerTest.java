/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.viewer;

import java.beans.PropertyChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.draw2d.RangeModel;
import org.junit.Assert;
import org.junit.Test;

public class ImageViewerTest {
	private static class RangeModelDummy implements RangeModel {
		private int extent;
		private int minimum;
		private int maximum;
		private int value;

		public RangeModelDummy(int extent, int minimum, int maximum) {
			if (minimum >= maximum || extent > maximum - minimum || extent < 1)
				Assert.fail("Invalid test");
			this.extent = extent;
			this.minimum = minimum;
			this.maximum = maximum;
		}
		
		/**
		 * Scroll the range model to a position. If the position is
		 * out of range, snaps into a valid position.
		 * @param position Position to scroll to. Integer.MAX_VALUE for
		 * all the way to the right, or Integer.MIN_VALUE for all the
		 * way to the left.
		 */
		public void scrollTo(int position) {
			 
			if (position > maximum - extent)
				value = maximum - extent;
			else if (position < minimum)
				value = minimum;
			else
				value = position;
		}
		
		@Override
		public int getExtent() {
			return extent;
		}

		@Override
		public int getMaximum() {
			return maximum;
		}

		@Override
		public int getMinimum() {
			return minimum;
		}

		@Override
		public int getValue() {
			return value;
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			throw new NotImplementedException();
		}

		@Override
		public boolean isEnabled() {
			throw new NotImplementedException();
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			throw new NotImplementedException();
		}

		@Override
		public void setAll(int min, int extent, int max) {
			throw new NotImplementedException();
		}

		@Override
		public void setExtent(int extent) {
			throw new NotImplementedException();
		}

		@Override
		public void setMaximum(int max) {
			throw new NotImplementedException();
		}

		@Override
		public void setMinimum(int min) {
			throw new NotImplementedException();
		}

		@Override
		public void setValue(int value) {
			throw new NotImplementedException();
		}

	}


	@Test
	public void testCalcScrollFactor() {
		RangeModelDummy t1 = new RangeModelDummy(10, 0, 115);
		t1.scrollTo(5);
		double factor = ImageViewer.calcScrollFactor(t1);
		Assert.assertEquals(0.05, factor, 0);
	}
	
	@Test
	public void testCalcPositionFromScrollFactor() {
		RangeModelDummy t1 = new RangeModelDummy(10, 0, 115);
		t1.scrollTo(5);
		double factor = 0.05;
		int value = ImageViewer.calcPositionFromScrollFactor(t1, factor);
		Assert.assertEquals(5, value);
	}
	
	@Test
	public void testScrollFactor_Identity() {
		RangeModelDummy t1 = new RangeModelDummy(10, 0, 115);
		t1.scrollTo(5);
		int value = ImageViewer.calcPositionFromScrollFactor(t1, ImageViewer.calcScrollFactor(t1));
		Assert.assertEquals(5, value);
	}
	
	@Test
	public void testScrollFactor_Identity_NegativeMin() {
		RangeModelDummy t1 = new RangeModelDummy(10, -12, 115);
		t1.scrollTo(5);
		int value = ImageViewer.calcPositionFromScrollFactor(t1, ImageViewer.calcScrollFactor(t1));
		Assert.assertEquals(5, value);
	}
	
	@Test
	public void testScrollFactor_Identity_NegativeValue() {
		RangeModelDummy t1 = new RangeModelDummy(10, -12, 115);
		t1.scrollTo(-5);
		int value = ImageViewer.calcPositionFromScrollFactor(t1, ImageViewer.calcScrollFactor(t1));
		Assert.assertEquals(-5, value);
	}
	
	@Test
	public void testScrollFactor_ScrollFarRight() {
		RangeModelDummy t1 = new RangeModelDummy(10, 0, 115);
		t1.scrollTo(Integer.MAX_VALUE);
		double factor = ImageViewer.calcScrollFactor(t1);
		Assert.assertEquals(Double.POSITIVE_INFINITY, factor, 0);
		int value = ImageViewer.calcPositionFromScrollFactor(t1, factor);
		Assert.assertEquals(105, value);
	}
	
	@Test
	public void testScrollFactor_ScrollFarLeft() {
		RangeModelDummy t1 = new RangeModelDummy(10, 0, 115);
		t1.scrollTo(Integer.MIN_VALUE);
		double factor = ImageViewer.calcScrollFactor(t1);
		Assert.assertEquals(0, factor, 0);
		int value = ImageViewer.calcPositionFromScrollFactor(t1, factor);
		Assert.assertEquals(0, value);
	}
	
	
}
