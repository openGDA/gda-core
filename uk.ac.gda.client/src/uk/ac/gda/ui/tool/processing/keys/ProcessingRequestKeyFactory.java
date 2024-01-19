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

package uk.ac.gda.ui.tool.processing.keys;

import java.net.URL;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.DawnProcessingRequest;
import uk.ac.gda.api.acquisition.configuration.processing.FrameCaptureRequest;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;
import uk.ac.gda.api.acquisition.parameters.FrameRequestDocument;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * A factory for {@link ProcessingRequestKey} objects.
 *
 * <p>
 * Each {@link ProcessingRequestKey} is unique through all the application,
 * consequently when this produces a {@link ProcessingRequestKey} for the first time, it stores such instance from an internal Map
 * so that any further request will receive the same instance.
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ProcessingRequestKeyFactory {

	public enum ProcessKey {
		DAWN(DawnProcessingRequest.KEY),
		SAVU(SavuProcessingRequest.KEY),
		FRAME_CAPTURE(FrameCaptureRequest.KEY);

		private final String key;
		ProcessKey(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
	}

	private final Map<ProcessKey, ProcessingRequestKey<?>> processingRequestKeys = new EnumMap<>(ProcessKey.class);

	public ProcessingRequestKey<?> getProcessingKey(ProcessKey processKey) {
		switch (processKey) {
			case DAWN:
				return processingRequestKeys.computeIfAbsent(processKey, this::createDawnProcessKey);
			case SAVU:
				return processingRequestKeys.computeIfAbsent(processKey, this::createSavuProcessKey);
			case FRAME_CAPTURE:
				return processingRequestKeys.computeIfAbsent(processKey, this::createFrameCaptureProcessKey);
			default:
				return null;
		}
	}

	public ProcessingRequestKey<?> getProcessingKey(String processingRequestKey) {
		return Arrays.stream(ProcessKey.values())
			.filter(p -> p.getKey().equals(processingRequestKey))
			.findFirst()
			.map(this::getProcessingKey)
			.orElseGet(() -> null);
	}

	private ProcessingRequestKey<URL> createDawnProcessKey(ProcessKey processKey) {
		return new ProcessingRequestKey<>(URL.class, processKey.getKey(),
				ClientMessages.DAWN, ClientMessages.DAWN_TP,
				new DawnProcessingRequest.Builder());
	}

	private ProcessingRequestKey<URL> createSavuProcessKey(ProcessKey processKey) {
		return new ProcessingRequestKey<>(URL.class, processKey.getKey(),
				ClientMessages.SAVU, ClientMessages.SAVU_TP,
				new SavuProcessingRequest.Builder());
	}

	private ProcessingRequestKey<FrameRequestDocument> createFrameCaptureProcessKey(ProcessKey processKey) {
		return new ProcessingRequestKey<>(FrameRequestDocument.class, processKey.getKey(),
				ClientMessages.FRAME_CAPTURE, ClientMessages.FRAME_CAPTURE_TP,
				new FrameCaptureRequest.Builder());
	}
}
