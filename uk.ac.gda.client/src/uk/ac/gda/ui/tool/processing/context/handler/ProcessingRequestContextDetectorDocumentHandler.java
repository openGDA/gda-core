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
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestPairFactory;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory;

/**
 * Implementation of {@link ProcessRequestContextHandler}
 *
 *
 * @author Maurizio Nagni
 */
@Component
public class ProcessingRequestContextDetectorDocumentHandler implements ProcessRequestContextHandler {

	@Autowired
	private ProcessingRequestKeyFactory processingRequestKeyFactory;

	@Autowired
	private ProcessingRequestPairFactory processingRequestPairFactory;

	@SuppressWarnings("unchecked")
	@Override
	public ProcessingRequestPair<?> handle(ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair((ProcessingRequestContext<DetectorDocument>) context);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcessingRequestPair<?> handle(Shell shell, ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair(shell, (ProcessingRequestContext<DetectorDocument>) context);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String assembleTooltip(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			return assembleTooltipInternal((ProcessingRequestPair<DetectorDocument>)processingPair);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeProcessingRequest(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			removeProcessingRequestInternal((ProcessingRequestPair<DetectorDocument>)processingPair);
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandle(ProcessingRequestContext<?> context) {
		return context.getKey().getDocumentClass().equals(DetectorDocument.class);
	}

	@Override
	public boolean canHandle(ProcessingRequestPair<?> processingPair) {
		return getProcessingRequestKeyFactory().getProcessingKey(processingPair.getKey()).getDocumentClass().equals(DetectorDocument.class);
	}

	private final ProcessingRequestPair<DetectorDocument> insertProcessingRequestPair(Shell shell, ProcessingRequestContext<DetectorDocument> processingContext) {
		var selector = new DetectorDocumentSelectorDialog(processingContext.getDefaultConfiguration());
		List<DetectorDocument> selections = new ArrayList<>();
		Optional.ofNullable(selector.open(shell))
			.ifPresent(selections::addAll);
		return insertProcessingRequestPair(selections, processingContext.getKey().getBuilder());
	}

	private final ProcessingRequestPair<DetectorDocument> insertProcessingRequestPair(List<DetectorDocument> selections, ProcessingRequestBuilder<DetectorDocument> processingRequestBuilder) {
		return getProcessingRequestPairFactory().insertProcessingRequestPair(processingRequestBuilder, selections);
	}

	private final ProcessingRequestPair<DetectorDocument> insertProcessingRequestPair(ProcessingRequestContext<DetectorDocument> processingContext) {
		List<DetectorDocument> selections = new ArrayList<>();
		selections.add(processingContext.getDefaultConfiguration().get(0));
		return insertProcessingRequestPair(selections,  processingContext.getKey().getBuilder());
	}

	private final String assembleTooltipInternal(ProcessingRequestPair<DetectorDocument> processingPair) {
		return processingPair.getValue().stream()
				.map(DetectorDocument::getName)
				.collect(Collectors.joining(", "));
	}

	public void removeProcessingRequestInternal(ProcessingRequestPair<DetectorDocument> processingPair) {
		processingRequestPairFactory.removeProcessingRequest(processingPair);
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return processingRequestKeyFactory;
	}

	private ProcessingRequestPairFactory getProcessingRequestPairFactory() {
		return processingRequestPairFactory;
	}

	class DetectorDocumentSelectorDialog {

		private List<DetectorDocument> input;

		public DetectorDocumentSelectorDialog(List<DetectorDocument> input) {
			this.input = input;
		}

		public List<DetectorDocument> open(Shell shell) {
			var selectionDialog = new ListSelectionDialog(shell, getInput(), this::listToArray, getILabelProvider(), "Capture Frame Cameras");
			selectionDialog.open();

			return Stream.of(selectionDialog.getResult())
				.map(DetectorDocument.class::cast)
				.collect(Collectors.toList());
		}

		private ILabelProvider getILabelProvider() {
			return new ILabelProvider() {

				@Override
				public void removeListener(ILabelProviderListener listener) {
					// not necesary
				}

				@Override
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}

				@Override
				public void dispose() {
					// not necesary
				}

				@Override
				public void addListener(ILabelProviderListener listener) {
					// not necesary
				}

				@Override
				public String getText(Object element) {

					return Optional.ofNullable(element)
						.map(DetectorDocument.class::cast)
						.map(DetectorDocument::getName)
						.orElseGet(() -> "Undefined");
				}

				@Override
				public Image getImage(Object element) {
					return ClientSWTElements.getImage(ClientImages.CAMERA);
				}
			};
		}

		private Object getInput() {
			return input;
		}

		@SuppressWarnings("unchecked")
		private DetectorDocument[] listToArray(Object inputElement) {
			return Optional.ofNullable(inputElement)
					.map(el -> ((List<DetectorDocument>) inputElement))
					.map(list -> list.toArray(new DetectorDocument[list.size()]))
					.orElseGet(() -> new DetectorDocument[0]);
		}
	}
}
