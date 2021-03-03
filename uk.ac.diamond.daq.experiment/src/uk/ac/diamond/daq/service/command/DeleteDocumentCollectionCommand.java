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

package uk.ac.diamond.daq.service.command;

import java.util.UUID;

import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.command.ExecuteCommand;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * An {@link ExecuteCommand} to delete any  {@link Document} document
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class DeleteDocumentCollectionCommand<T extends Document> implements ExecuteCommand {

	private final CollectionCommandReceiver<T> ccr;
	private final UUID id;
	private final OutputStrategy<T> outputStrategy;

	/**
	 * Constructor for the delete command
	 * @param ccr defines how collect the document
	 * @param id the document identifier 
	 * @param outputStrategy defines how write the output
	 */
	public DeleteDocumentCollectionCommand(CollectionCommandReceiver<T> ccr, UUID id, OutputStrategy<T> outputStrategy) {
		super();
		this.ccr = ccr;
		this.id = id;
		this.outputStrategy = outputStrategy;
	}

	@Override
	public void execute() throws GDAServiceException {
		ccr.deleteDocument(id, outputStrategy);
	}
}
