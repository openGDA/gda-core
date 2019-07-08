package uk.ac.diamond.daq.server.configuration.commands;

import gda.factory.FactoryException;

public interface ServerCommand {

	void execute() throws FactoryException;
}
