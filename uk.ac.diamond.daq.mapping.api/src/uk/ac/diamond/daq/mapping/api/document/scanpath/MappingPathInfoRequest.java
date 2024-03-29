/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Document containing information needed to generate a {@link MappingPathInfo} object
 */
@JsonDeserialize(builder = MappingPathInfoRequest.Builder.class)
public class MappingPathInfoRequest implements IPathInfoRequest {
	private static final int DEFAULT_MAX_POINTS = 100000;

	private final UUID eventId;

	/**
	 * The id of the source for this request, e.g. the mapping view
	 */
	private final String sourceId;

	/**
	 * The model the path of the scan points
	 */
	private final IMapPathModel scanPathModel;

	/**
	 * The model describing the region of interest for the scan
	 */
	private final IROI scanRegion;

	/**
	 * A list of models describing outer scan paths into which to
	 * project the mapping scan (can be empty).
	 */
	private final List<IAxialModel> outerScannables;

	/**
	 * The maximum number of points to be generated inside a
	 * {@link MappingPathInfo} object
	 */
	private final int maxPoints;

	private MappingPathInfoRequest(
			UUID eventId,
			String sourceId,
			IMapPathModel scanPathModel,
			IROI scanRegion,
			List<IAxialModel> outerScannables,
			int maxPoints) {
		super();
		this.eventId = eventId;
		this.sourceId = sourceId;
		this.scanPathModel = scanPathModel;
		this.scanRegion = scanRegion;
		this.outerScannables = outerScannables;
		this.maxPoints = maxPoints;
	}

	public UUID getEventId() {
		return eventId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public IMapPathModel getScanPathModel() {
		return scanPathModel;
	}

	public IROI getScanRegion() {
		return scanRegion;
	}

	public List<? extends IScanPointGeneratorModel> getOuterScannables() {
		return outerScannables;
	}

	public int getMaxPoints() {
		return maxPoints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
		result = prime * result + (maxPoints ^ (maxPoints >>> 32));
		result = prime * result + ((outerScannables == null) ? 0 : outerScannables.hashCode());
		result = prime * result + ((scanPathModel == null) ? 0 : scanPathModel.hashCode());
		result = prime * result + ((scanRegion == null) ? 0 : scanRegion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingPathInfoRequest other = (MappingPathInfoRequest) obj;
		if (sourceId == null) {
			if (other.sourceId != null)
				return false;
		} else if (!sourceId.equals(other.sourceId)) {
			return false;
		} if (maxPoints != other.maxPoints)
			return false;
		if (outerScannables == null) {
			if (other.outerScannables != null)
				return false;
		} else if (!outerScannables.equals(other.outerScannables))
			return false;
		if (scanPathModel == null) {
			if (other.scanPathModel != null)
				return false;
		} else if (!scanPathModel.equals(other.scanPathModel))
			return false;
		if (scanRegion == null) {
			if (other.scanRegion != null)
				return false;
		} else if (!scanRegion.equals(other.scanRegion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PathInfoRequest [scanPathModel=" + scanPathModel + ", scanRegion=" + scanRegion + ", outerScannables="
				+ outerScannables + ", maxPoints=" + maxPoints + "]";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder
	public static final class Builder {
		private UUID eventId;
		private String sourceId;
		private IMapPathModel scanPathModel;
		private IROI scanRegion;
		private List<IAxialModel> outerScannables;
		private Integer maxPoints;

		public Builder withEventId(UUID eventId) {
			this.eventId = eventId;
			return this;
		}

		public Builder withSourceId(String sourceId) {
			this.sourceId = sourceId;
			return this;
		}

		public Builder withScanPathModel(IMapPathModel scanPathModel) {
			this.scanPathModel = scanPathModel;
			return this;
		}

		public Builder withScanRegion(IROI scanRegion) {
			this.scanRegion = scanRegion;
			return this;
		}

		public Builder withOuterScannables(List<IAxialModel> outerScannables) {
			this.outerScannables = outerScannables;
			return this;
		}

		public Builder withMaxPoints(int maxPoints) {
			this.maxPoints = maxPoints;
			return this;
		}

		public MappingPathInfoRequest build() {
			return new MappingPathInfoRequest(
					Objects.requireNonNull(eventId),
					Objects.requireNonNull(sourceId),
					Objects.requireNonNull(scanPathModel),
					Objects.requireNonNull(scanRegion),
					Objects.requireNonNullElse(outerScannables, Collections.emptyList()),
					Objects.requireNonNullElse(maxPoints, DEFAULT_MAX_POINTS));
		}
	}
}
