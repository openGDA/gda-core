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

import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Defines methods for Chain-of-responsibility pattern to handle the creation and remove of {@link ProcessingRequestPair}.
 *
 * <p>
 * Each {@code ProcessingRequestPair} is generated from a {@link ProcessingRequestContext} which may vary depending on the document it represents.
 * To handle multiple type of {@code ProcessingRequestContext} is then necessary to have specfic handler of reach of them
 * </p>
 *
 * @author Maurizio Nagni
 */
public interface ProcessRequestContextHandler {

	/**
	 * Generates a {@code ProcessingRequestPair}
	 *
	 * @param processingContext the data from where generate the {@code ProcessingRequestPair}
	 * @return the created pair or {@code null} if cannot handle.
	 */
	ProcessingRequestPair<?> handle(ProcessingRequestContext<?> processingContext);

	/**
	 * Generates a {@code ProcessingRequestPair} supported by some GUI component, {@code Dialog} for example.
	 *
	 * @param shell the GUI shell
	 * @param processingContext the data from where generate the {@code ProcessingRequestPair}
	 * @return the created pair or {@code null} if cannot handle.
	 */
	ProcessingRequestPair<?> handle(Shell shell, ProcessingRequestContext<?> processingContext);

	/**
	 * Creates a human readable version of {@link ProcessingRequestPair#getValue()}
	 * @param processingPair the processing pair to parse
	 * @return a {@code String} or {@code null} if cannot handle.
	 */
	String assembleTooltip(ProcessingRequestPair<?> processingPair);

	/**
	 * Removes a {@code ProcessingRequestPair} from the {@link ScanningAcquisition} contained in {@link ClientSpringContext#getAcquisitionController()}
	 * @param processingPair the processing pair to parse
	 * @return a {@code true} or {@code false} if cannot handle.
	 */
	boolean removeProcessingRequest(ProcessingRequestPair<?> processingPair);

	boolean canHandle(ProcessingRequestContext<?> context);

	boolean canHandle(ProcessingRequestPair<?> processingPair);

}
