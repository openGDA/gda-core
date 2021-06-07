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

package gda.jython.translator;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliasingBase {
	private static final Logger logger = LoggerFactory.getLogger(AliasingBase.class);

	protected Collection<String> aliases = new CopyOnWriteArraySet<>();
	protected Collection<String> varargAliases = new CopyOnWriteArraySet<>();

	public void addAliasedCommand(String commandName) {
		aliases.add(commandName);
		logger.debug("Added alias command: {}", commandName);
	}

	public void addAliasedVarargCommand(String commandName) {
		varargAliases.add(commandName);
		logger.debug("Added vararg alias command: {}", commandName);
	}

	public Collection<String> getAliasedCommands() {
		return new HashSet<>(aliases);
	}

	public Collection<String> getAliasedVarargCommands() {
		return new HashSet<>(varargAliases);
	}

	public boolean hasAlias(String command) {
		return aliases.contains(command) || varargAliases.contains(command);
	}

	public void removeAlias(String command) {
		aliases.remove(command);
		varargAliases.remove(command);
	}

}