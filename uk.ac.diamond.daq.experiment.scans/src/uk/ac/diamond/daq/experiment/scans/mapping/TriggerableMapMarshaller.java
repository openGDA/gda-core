/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.scans.mapping;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshaller;

public class TriggerableMapMarshaller implements IMarshaller {

	@Override
	public Class<?> getObjectClass() {
		return null;
	}

	@Override
	public Class<?> getSerializerClass() {
		return null;
	}

	@Override
	public Class<?> getDeserializerClass() {
		return null;
	}

	@Override
	public Class<TriggerableMap> getMixinAnnotationType() {
		return TriggerableMap.class;
	}

	@Override
	public Class<TriggerableMapMixIn> getMixinAnnotationClass() {
		return TriggerableMapMixIn.class;
	}

}
