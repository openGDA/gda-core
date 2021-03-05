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

package gda.mscan.processor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

import gda.jython.InterfaceProvider;
import gda.mscan.ClausesContext;

public class ReRunFromFileElementProcessor extends ElementProcessorBase<String> {

    private String nexusPath;

	public ReRunFromFileElementProcessor(final String source) {
		super(source);
	}

	/**
	 * Checks for a fully specified path and if not appends the supplied filename to the current visit dir path
	 */
	@Override
	public void process(ClausesContext context,
			List<IClauseElementProcessor> clauseProcessors, int index) {
		throwIf(enclosed == null  || enclosed.isBlank(), "supplied filename is null or blank");
		throwIf(index != 0, "'rerun' must be first element");
		throwIf(clauseProcessors.size() != 1, "too many elements in 'rerun' clause");
		nexusPath = enclosed.contains(File.separator)
				? enclosed
				: InterfaceProvider.getPathConstructor().createFromDefaultProperty() + File.separator + enclosed;
		try {
			throwIf(Files.notExists(Paths.get(nexusPath)),  "supplied file does not exist");
		} catch (InvalidPathException ex) {
			throwOnInvalidCommand(ex.getMessage());
		}

	}

	@Override
	public String getElementValue() {
		return nexusPath;
	}

}
