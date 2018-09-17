/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.sequencer.nexus;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates the simple fields in NXentry
 *
 */
public class NXEntryFieldBuilder {

	private static final Logger logger = LoggerFactory.getLogger(NXEntryFieldBuilder.class);

	private static final int[] SINGLE_SHAPE = new int[] { 1 };

	private NXentry entry;
	private LazyWriteableDataset scanEndTimeDataset;
	private LazyWriteableDataset scanDurationDataset;
	private ZonedDateTime scanStartTime;

	public NXEntryFieldBuilder(NXentry entry) {
		this.entry = entry;
	}

	public void start() {
		scanStartTime = ZonedDateTime.now();
		entry.setStart_time(DatasetFactory.createFromObject(ISO_OFFSET_DATE_TIME.format(scanStartTime)));
		scanEndTimeDataset = new LazyWriteableDataset(NXentry.NX_END_TIME, String.class, SINGLE_SHAPE, SINGLE_SHAPE, SINGLE_SHAPE, null);
		entry.createDataNode(NXentry.NX_END_TIME, scanEndTimeDataset);
		scanDurationDataset = new LazyWriteableDataset(NXentry.NX_DURATION, Long.class, SINGLE_SHAPE, SINGLE_SHAPE, SINGLE_SHAPE, null);
		DataNode durationNode = entry.createDataNode(NXentry.NX_DURATION, scanDurationDataset);
		Attribute attribute = TreeFactory.createAttribute("units");
		attribute.setValue("s");
		durationNode.addAttribute(attribute);
		try {
			entry.setExperiment_identifierScalar(ServiceHolder.getFilePathService().getVisit());
		} catch (Exception e) {
			logger.error("Could not read visit",e);
		}
	}

	public void end() {

		ZonedDateTime scanEndTime = ZonedDateTime.now();
		Duration scanDuration = Duration.between(scanStartTime, scanEndTime);

		try {
			scanEndTimeDataset.setSlice(null, DatasetFactory.createFromObject(ISO_OFFSET_DATE_TIME.format(ZonedDateTime.from(scanEndTime))), null, null, null);
		} catch (DatasetException e) {
			logger.error("Could not set scan end",e);
		}

		try {
			scanDurationDataset.setSlice(null, DatasetFactory.createFromObject(scanDuration.getSeconds()), null, null, null);
		} catch (DatasetException e) {
			logger.error("Could set scan duration",e);
		}
	}

}
