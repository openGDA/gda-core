/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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
import java.util.Collections;

/**
 * This translator performs no actions and returns the input unmodified.
 * Therefore aliasing is not supported.
 */
public class NoopTranslator implements Translator {

	@Override
	public Collection<String> getAliasedCommands() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getAliasedVarargCommands() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasAlias(String command) {
		return false;
	}

	@Override
	public String translate(String original_command) {
		return original_command;
	}

	@Override
	public void addAliasedCommand(String commandName) {
		// Do nothing
	}

	@Override
	public void addAliasedVarargCommand(String commandName) {
		// Do nothing
	}

	@Override
	public void removeAlias(String command) {
		// Do nothing
	}
}