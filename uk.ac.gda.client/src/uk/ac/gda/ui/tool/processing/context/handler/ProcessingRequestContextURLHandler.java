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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.core.tool.URLFactory;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestPairFactory;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory;

/**
 * Handles {@code ProcessingRequestContext<URL>} objects
 *
 * @see ProcessRequestContextHandler
 *
 * @author Maurizio Nagni
 */
@Component
public class ProcessingRequestContextURLHandler implements ProcessRequestContextHandler {

	@Autowired
	private ProcessingRequestKeyFactory processingRequestKeyFactory;

	@Autowired
	private ProcessingRequestPairFactory processingRequestPairFactory;

	@Override
	public ProcessingRequestPair<?> handle(ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair((ProcessingRequestContext<URL>) context);
		}
		return null;
	}

	@Override
	public ProcessingRequestPair<?> handle(Shell shell, ProcessingRequestContext<?> context) {
		if (canHandle(context)) {
			return insertProcessingRequestPair(shell, (ProcessingRequestContext<URL>) context);
		}
		return null;
	}

	@Override
	public String assembleTooltip(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			return assembleTooltipURL((ProcessingRequestPair<URL>)processingPair);
		}
		return null;
	}

	@Override
	public boolean removeProcessingRequest(ProcessingRequestPair<?> processingPair) {
		if (canHandle(processingPair)) {
			removeProcessingRequestURL((ProcessingRequestPair<URL>)processingPair);
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandle(ProcessingRequestContext<?> context) {
		return context.getKey().getDocumentClass().equals(URL.class);
	}

	@Override
	public boolean canHandle(ProcessingRequestPair<?> processingPair) {
		return getProcessingRequestKeyFactory().getProcessingKey(processingPair.getKey()).getDocumentClass().equals(URL.class);
	}

	private final ProcessingRequestPair<URL> insertProcessingRequestPair(Shell shell, ProcessingRequestContext<URL> processingContext) {
		var fileDialog = new FileDialog(shell, SWT.OPEN);
		Optional.ofNullable(processingContext.getConfigurationSource())
			.map(URL::getFile)
			.ifPresent(fileDialog::setFilterPath);

		List<URL> selections = new ArrayList<>();
		Optional.ofNullable(fileDialog.open())
			.map(ProcessingRequestContextURLHandler::generateURL)
			.ifPresent(selections::add);
		return insertProcessingRequestPair(selections, processingContext.getKey().getBuilder());
	}

	private final ProcessingRequestPair<URL> insertProcessingRequestPair(List<URL> selections, ProcessingRequestBuilder<URL> processingRequestBuilder) {
		return getProcessingRequestPairFactory().insertProcessingRequestPair(processingRequestBuilder, selections);
	}

	private final ProcessingRequestPair<URL> insertProcessingRequestPair(ProcessingRequestContext<URL> processingContext) {
		List<URL> selections = new ArrayList<>();
		selections.add(processingContext.getDefaultConfiguration().get(0));
		return insertProcessingRequestPair(selections,  processingContext.getKey().getBuilder());
	}

	private final String assembleTooltipURL(ProcessingRequestPair<URL> processingPair) {
		return processingPair.getValue().stream()
				.map(URL::getPath)
				.collect(Collectors.joining(", "));
	}

	public void removeProcessingRequestURL(ProcessingRequestPair<URL> processingPair) {
		processingRequestPairFactory.removeProcessingRequest(processingPair);
	}

	private static final URLFactory urlFactory = new URLFactory();
	/**
	 * Generates a URL from a {@code String}
	 *
	 * @param path
	 *            the location to convert
	 * @return the converted string to URL, otherwise {@code null}
	 */
	private static URL generateURL(String path) {
		try {
			return urlFactory.generateUrl(path);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return processingRequestKeyFactory;
	}

	private ProcessingRequestPairFactory getProcessingRequestPairFactory() {
		return processingRequestPairFactory;
	}
}
