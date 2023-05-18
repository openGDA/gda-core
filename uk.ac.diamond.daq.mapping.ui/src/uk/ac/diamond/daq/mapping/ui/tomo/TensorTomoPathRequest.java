/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.diamond.daq.mapping.api.document.scanpath.IPathInfoRequest;

@JsonDeserialize(builder = TensorTomoPathRequest.Builder.class)
public class TensorTomoPathRequest implements IPathInfoRequest {

	private final IMapPathModel mapPathModel;

	private final IROI mapRegion;

	private IAxialModel angle1PathModel;

	private IAxialModel angle2PathModel;

	private TensorTomoPathRequest(
			IMapPathModel mapPathModel,
			IROI mapRegion,
			IAxialModel angle1PathModel,
			IAxialModel angle2PathModel) {
		super();
		this.mapPathModel = mapPathModel;
		this.mapRegion = mapRegion;
		this.angle1PathModel = angle1PathModel;
		this.angle2PathModel = angle2PathModel;
	}

	public IMapPathModel getMapPathModel() {
		return mapPathModel;
	}

	public IROI getMapRegion() {
		return mapRegion;
	}

	public IAxialModel getAngle1PathModel() {
		return angle1PathModel;
	}

	public IAxialModel getAngle2PathModel() {
		return angle2PathModel;
	}

	@Override
	public int hashCode() {
		return Objects.hash(angle1PathModel, angle2PathModel, mapPathModel, mapRegion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TensorTomoPathRequest other = (TensorTomoPathRequest) obj;
		return Objects.equals(angle1PathModel, other.angle1PathModel)
				&& Objects.equals(angle2PathModel, other.angle2PathModel)
				&& Objects.equals(mapPathModel, other.mapPathModel) && Objects.equals(mapRegion, other.mapRegion);
	}

	@Override
	public String toString() {
		return "TensorTomoPathRequest [mapPathModel=" + mapPathModel + ", mapRegion=" + mapRegion + ", angle1PathModel="
				+ angle1PathModel + ", angle2PathModel=" + angle2PathModel + "]";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder
	public static final class Builder {
		private IMapPathModel mapPathModel;
		private IROI mapRegion;
		private IAxialModel angle1PathModel;
		private IAxialModel angle2PathModel;

		public Builder withMapPathModel(IMapPathModel mapPathModel) {
			this.mapPathModel = mapPathModel;
			return this;
		}

		public Builder withMapRegion(IROI mapRegion) {
			this.mapRegion = mapRegion;
			return this;
		}

		public Builder withAngle1PathModel(IAxialModel angle1PathModel) {
			this.angle1PathModel = angle1PathModel;
			return this;
		}

		public Builder withAngle2PathModel(IAxialModel angle2PathModel) {
			this.angle2PathModel = angle2PathModel;
			return this;
		}

		public TensorTomoPathRequest build() {
			return new TensorTomoPathRequest(
					Objects.requireNonNull(mapPathModel),
					Objects.requireNonNull(mapRegion),
					Objects.requireNonNull(angle1PathModel),
					Objects.requireNonNull(angle2PathModel));
		}

	}

}