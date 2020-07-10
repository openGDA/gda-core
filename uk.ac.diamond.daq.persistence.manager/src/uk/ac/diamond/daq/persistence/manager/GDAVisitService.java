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

package uk.ac.diamond.daq.persistence.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import uk.ac.diamond.daq.application.persistence.service.VisitService;
import uk.ac.diamond.daq.application.persistence.service.VisitServiceListener;

/**
 * Determines the current visit for the Persistence service
 */
public class GDAVisitService implements VisitService {
	private static final Logger log = LoggerFactory.getLogger(GDAVisitService.class);

	private final static String ERROR_VISIT_ID = "no-visit";
	private List<VisitServiceListener> listeners = new ArrayList<>();

	@Override
	public void addListener(VisitServiceListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(VisitServiceListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String getCurrentVisitId() {
		try {
			return GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException e) {
			log.error("Unable to find current visit", e);
			return ERROR_VISIT_ID;
		}
	}

	@Override
	public void setCurrentVisitId(String currentVisitId) {
		try {
			GDAMetadataProvider.getInstance().setMetadataValue("visit", currentVisitId);
			for (VisitServiceListener listener : listeners) {
				listener.currentVisitUpdated(currentVisitId);
			}
		} catch (DeviceException e) {
			log.error("Unable to set current visit", e);
		}
	}

}
