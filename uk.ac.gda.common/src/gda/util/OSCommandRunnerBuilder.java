/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.util;

import java.util.List;

/**
 * Builder for {@link OSCommandRunner}.
 */
public class OSCommandRunnerBuilder {
	
	private OSCommandRunnerBuilder() {
		// do nothing
	}
	
	private List<String> command;
	private boolean keepOutput;
	private Object input;
	private String outputFilename;
	private int timeout;
	
	public static OSCommandRunnerBuilder defaults() {
		final OSCommandRunnerBuilder b = new OSCommandRunnerBuilder();
		
		// command must be specified
		b.keepOutput = false;
		b.input = null;
		b.outputFilename = null;
		b.noTimeout();
		
		return b;
	}
	
	public OSCommandRunnerBuilder command(List<String> command) {
		this.command = command;
		return this;
	}
	
	public OSCommandRunnerBuilder keepOutput(boolean keepOutput) {
		this.keepOutput = keepOutput;
		return this;
	}
	
	public OSCommandRunnerBuilder inputFilename(String inputFilename) {
		this.input = inputFilename;
		return this;
	}
	
	public OSCommandRunnerBuilder inputLines(String[] inputLines) {
		this.input = inputLines;
		return this;
	}
	
	public OSCommandRunnerBuilder outputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
		return this;
	}
	
	public OSCommandRunnerBuilder timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public OSCommandRunnerBuilder noTimeout() {
		return timeout(-1);
	}
	
	public OSCommandRunner build() {
		
		if (command == null) {
			throw new IllegalStateException("Command must be specified");
		}
		
		final OSCommandRunner oscr = new OSCommandRunner(command, keepOutput, input, outputFilename, null, null, null, timeout);
		return oscr;
	}

}
