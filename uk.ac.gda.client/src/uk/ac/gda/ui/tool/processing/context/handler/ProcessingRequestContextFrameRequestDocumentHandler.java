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

package uk.ac.gda.ui.tool.processing.context.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.FrameRequestDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.acquisition.processing.FrameCaptureProperties;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.document.ClientPropertiesHelper;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestPairFactory;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory;

/**
 * {@link ProcessRequestContextHandler} for {@link FrameRequestDocument}s
 */
@Component
public class ProcessingRequestContextFrameRequestDocumentHandler implements ProcessRequestContextHandler {

	private Logger logger = LoggerFactory.getLogger(ProcessingRequestContextFrameRequestDocumentHandler.class);

	@Autowired
	private ProcessingRequestKeyFactory processingRequestKeyFactory;

	@Autowired
	private ProcessingRequestPairFactory processingRequestPairFactory;

	@SuppressWarnings("unchecked")
	@Override
	public ProcessingRequestPair<?> handle(ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair((ProcessingRequestContext<FrameRequestDocument>) context);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessingRequestPair<?> handle(Shell shell, ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair(shell, (ProcessingRequestContext<FrameRequestDocument>) context);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String assembleTooltip(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			return assembleTooltipInternal((ProcessingRequestPair<FrameRequestDocument>)processingPair);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeProcessingRequest(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			removeProcessingRequestInternal((ProcessingRequestPair<FrameRequestDocument>)processingPair);
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandle(ProcessingRequestContext<?> context) {
		return context.getKey().getDocumentClass().equals(FrameRequestDocument.class);
	}

	@Override
	public boolean canHandle(ProcessingRequestPair<?> processingPair) {
		return getProcessingRequestKeyFactory().getProcessingKey(processingPair.getKey()).getDocumentClass().equals(FrameRequestDocument.class);
	}

	private final ProcessingRequestPair<FrameRequestDocument> insertProcessingRequestPair(Shell shell, ProcessingRequestContext<FrameRequestDocument> processingContext) {
		var selector = new FrameRequestDocumentSelectorDialog(processingContext.getDefaultConfiguration());
		List<FrameRequestDocument> selections = new ArrayList<>();
		Optional.ofNullable(selector.open(shell))
			.ifPresent(selections::addAll);
		return insertProcessingRequestPair(selections, processingContext.getKey().getBuilder());
	}

	private final ProcessingRequestPair<FrameRequestDocument> insertProcessingRequestPair(List<FrameRequestDocument> selections, ProcessingRequestBuilder<FrameRequestDocument> processingRequestBuilder) {
		return getProcessingRequestPairFactory().insertProcessingRequestPair(processingRequestBuilder, selections);
	}

	private final ProcessingRequestPair<FrameRequestDocument> insertProcessingRequestPair(ProcessingRequestContext<FrameRequestDocument> processingContext) {
		List<FrameRequestDocument> selections = new ArrayList<>();
		selections.add(processingContext.getDefaultConfiguration().get(0));
		return insertProcessingRequestPair(selections,  processingContext.getKey().getBuilder());
	}

	private final String assembleTooltipInternal(ProcessingRequestPair<FrameRequestDocument> processingPair) {
		return processingPair.getValue().stream()
				.map(FrameRequestDocument::getName)
				.collect(Collectors.joining(", "));
	}

	public void removeProcessingRequestInternal(ProcessingRequestPair<FrameRequestDocument> processingPair) {
		processingRequestPairFactory.removeProcessingRequest(processingPair);
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return processingRequestKeyFactory;
	}

	private ProcessingRequestPairFactory getProcessingRequestPairFactory() {
		return processingRequestPairFactory;
	}

	class FrameRequestDocumentSelectorDialog {

		private List<FrameRequestDocument> input;

		public FrameRequestDocumentSelectorDialog(List<FrameRequestDocument> input) {
			this.input = input;
		}

		public List<FrameRequestDocument> open(Shell shell) {
			var selectionDialog = new ListSelectionDialog(shell, input, this::listToArray, getLabelProvider(), "Capture Frame Cameras");
			selectionDialog.open();

			List<FrameRequestDocument> frameRequests = new ArrayList<>();

			Stream.of(selectionDialog.getResult())
				.map(FrameCaptureProperties.class::cast)
				.forEach(c -> {
					FrameRequestDocument.Builder frameDocument = new FrameRequestDocument.Builder()
							.withMalcolmDetectorName(c.getMalcolmDetectorName())
							.withExposure(getExposure(c.getCameraId()))
							.withName(c.getMalcolm());
					getClientPropertiesHelper().getAcquisitionPropertiesDocuments(c.getCameraId())
						.map(CameraConfigurationProperties::getCameraControl)
						.map(frameDocument::withDetectorController)
						.map(FrameRequestDocument.Builder::build)
						.ifPresent(frameRequests::add);
				});
			return frameRequests;
		}

		private double getExposure(String cameraId) {
			CameraControl control = Finder.find(getClientPropertiesHelper().getAcquisitionPropertiesDocuments(cameraId).orElseThrow().getCameraControl());
			try {
				return control.getAcquireTime();
			} catch (DeviceException e) {
				logger.error("Could not read acquire time", e);
				return 0;
			}
		}



		private ILabelProvider getLabelProvider() {
			return new LabelProvider() {

				@Override
				public String getText(Object element) {
					Optional<CameraConfigurationProperties> cameraProperties = Optional.ofNullable(element)
							.map(FrameCaptureProperties.class::cast)
							.map(FrameCaptureProperties::getCameraId)
							.map(id -> getClientPropertiesHelper().getAcquisitionPropertiesDocuments(id))
							.orElseGet(Optional::empty);

					return cameraProperties
							.map(CameraConfigurationProperties::getName)
							.orElseGet(() -> "Undefined");
				}

				@Override
				public Image getImage(Object element) {
					return ClientSWTElements.getImage(ClientImages.CAMERA);
				}
			};
		}

		@SuppressWarnings("unchecked")
		private FrameCaptureProperties[] listToArray(Object inputElement) {
			return Optional.ofNullable(inputElement)
					.map(el -> ((List<FrameCaptureProperties>) inputElement))
					.map(list -> list.toArray(new FrameCaptureProperties[list.size()]))
					.orElseGet(() -> new FrameCaptureProperties[0]);
		}

		private ClientPropertiesHelper getClientPropertiesHelper() {
			return SpringApplicationContextFacade.getBean(ClientPropertiesHelper.class);
		}
	}

}
