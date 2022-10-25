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

package uk.ac.diamond.daq.experiment.plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;

@Service
public class SpringPayloadService implements PayloadService  {

	@Autowired
	private List<PayloadHandler<? extends Payload>> handlers;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Payload> T wrap(Object rawPayload) {
		return (T) handlers.stream()
				.filter(handler -> handler.getSourceClass().equals(rawPayload.getClass()))
				.map(handler -> (PayloadHandler<? extends Payload>) handler)
				.findAny().orElseThrow().wrap(rawPayload);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Payload> Object handle(T payload) {
		return handlers.stream()
				.filter(handler -> handler.getTargetClass().equals(payload.getClass()))
				.map(handler -> (PayloadHandler<T>) handler)
				.findAny().orElseThrow().handle(payload);
	}

}
