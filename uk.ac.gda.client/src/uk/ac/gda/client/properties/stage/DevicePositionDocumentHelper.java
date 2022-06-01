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

package uk.ac.gda.client.properties.stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.exception.GDAClientException;

/**
 * Utility class which moves asynchronously the device specified by a {@link DevicePositionDocument}
 *
 * @author Maurizio Nagni
 */
@Component
public class DevicePositionDocumentHelper {

	private static final Logger logger = LoggerFactory.getLogger(DevicePositionDocumentHelper.class);

	private ExecutorService executor = Executors.newFixedThreadPool(10);

	/**
	 * Moves asynchronously the device specified by a {@link DevicePositionDocument}
	 *
	 * @param positionDocument the document to use
	 */
	public void moveTo(DevicePositionDocument positionDocument) {
		executor.submit(runnableMonitor(positionDocument));
	}

	private Runnable runnableMonitor(DevicePositionDocument positionDocument) {
		return () -> {
			try {
				new DevicePositionDocumentHelperInt(positionDocument).moveTo();
			} catch (GDAClientException e) {
				logger.error("Cannot move motor", e);
			}
		};
	}

	private class DevicePositionDocumentHelperInt {
		private final DevicePositionDocument positionDocument;

		public DevicePositionDocumentHelperInt(DevicePositionDocument positionDocument) {
			this.positionDocument = positionDocument;
		}

		public void moveTo() throws GDAClientException {
			ScannableProperties scannablePropertiesDocument = new ScannableProperties();
			scannablePropertiesDocument.setScannable(positionDocument.getDevice());
			moveTo(scannablePropertiesDocument);
		}

		private void moveTo(ScannableProperties scannablePropertiesDocument) throws GDAClientException {
			ManagedScannable<Object> ms = new ManagedScannable<>(scannablePropertiesDocument);
			ms.moveTo(positionDocument.getPosition());
		}
	}


}
