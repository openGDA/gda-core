/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.plotting.model;

import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.gda.beans.ObservableModel;

import com.google.gson.annotations.Expose;

public abstract class LineTraceProviderNode extends Node {
	private TraceStyleDetails traceStyle;
	private boolean isHighlighted;
	private final ScanNode scanNode;
	private final boolean plotByDefault;

	public LineTraceProviderNode(ScanNode scanNode, boolean plotByDefault, Node parent, TraceStyleDetails traceStyle) {
		super(parent);
		this.scanNode = scanNode;
		this.plotByDefault = plotByDefault;
		this.setTraceStyle(traceStyle);
	}

	public TraceStyleDetails getTraceStyle() {
		return traceStyle;
	}

	public void setTraceStyle(TraceStyleDetails traceStyle) {
		this.traceStyle = traceStyle;
	}

	public abstract DoubleDataset getYAxisDataset();
	public abstract DoubleDataset getXAxisDataset();
	public boolean isPlotByDefault() {
		return plotByDefault;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	public ScanNode getScanNode() {
		return scanNode;
	}

	public static class TraceStyleDetails extends ObservableModel {
		public static final String COLOR_HAX_VALUE_PROP_NAME = "colorHexValue";
		@Expose
		private String colorHexValue = null;
		public static final String TRACE_TYPE_PROP_NAME = "traceType";
		@Expose
		private TraceType traceType;
		public static final String POINT_STYLE_PROP_NAME = "pointStyle";
		@Expose
		private PointStyle pointStyle;
		public static final String POINT_SIZE_PROP_NAME = "pointSize";
		@Expose
		private int pointSize;
		public static final String LINE_WIDTH_PROP_NAME = "lineWidth";
		@Expose
		private int lineWidth;

		public String getColorHexValue() {
			return colorHexValue;
		}

		public void setColorHexValue(String colorHexValue) {
			this.firePropertyChange(COLOR_HAX_VALUE_PROP_NAME, this.colorHexValue, this.colorHexValue = colorHexValue);
		}

		public void setTraceType(TraceType traceType) {
			this.firePropertyChange(TRACE_TYPE_PROP_NAME, this.traceType, this.traceType = traceType);
		}

		public void setPointStyle(PointStyle pointStyle) {
			this.firePropertyChange(POINT_STYLE_PROP_NAME, this.pointStyle, this.pointStyle = pointStyle);
		}

		public void setPointSize(int pointSize) {
			this.firePropertyChange(POINT_SIZE_PROP_NAME, this.pointSize, this.pointSize = pointSize);
		}

		public TraceType getTraceType() {
			return traceType;
		}

		public PointStyle getPointStyle() {
			return pointStyle;
		}

		public int getPointSize() {
			return pointSize;
		}


		public int getLineWidth() {
			return lineWidth;
		}

		public void setLineWidth(int lineWidth) {
			this.firePropertyChange(LINE_WIDTH_PROP_NAME, this.lineWidth, this.lineWidth = lineWidth);
		}

		public static TraceStyleDetails  createDefaultSolidTrace() {
			TraceStyleDetails traceStyle = new TraceStyleDetails();
			traceStyle.setTraceType(TraceType.SOLID_LINE);
			traceStyle.setPointStyle(PointStyle.NONE);
			traceStyle.setLineWidth(1);
			return traceStyle;
		}

		public static TraceStyleDetails createDefaultDashTrace() {
			TraceStyleDetails traceStyle = new TraceStyleDetails();
			traceStyle.setTraceType(TraceType.DASH_LINE);
			traceStyle.setPointStyle(PointStyle.FILLED_CIRCLE);
			traceStyle.setPointSize(3);
			traceStyle.setLineWidth(1);
			return traceStyle;
		}
	}
}
