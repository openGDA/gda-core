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

import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.CollectionOutputStrategy;
import uk.ac.gda.common.command.ExecuteCommand;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * An {@link ExecuteCommand} to insert a {@link Document} document
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class InsertDocumentCollectionCommand<T extends Document> implements ExecuteCommand {

	private final CollectionCommandReceiver<T> ccr;
	private final T document;
	private final CollectionOutputStrategy<T> outputStrategy;

	/**
	 * Constructor for the delete command
	 * @param ccr defines how collect the document
	 * @param document the document identifier 
	 * @param outputStrategy defines how write the output
	 */
	public InsertDocumentCollectionCommand(CollectionCommandReceiver<T> ccr, T document, CollectionOutputStrategy<T> outputStrategy) {
		super();
		this.ccr = ccr;
		this.document = document;
		this.outputStrategy = outputStrategy;
	}

	@Override
	public void execute() throws GDAServiceException {
		ccr.insertDocument(document, outputStrategy);
	}
}
