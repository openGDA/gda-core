/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.commandqueue;

public class StopCommand extends CommandBase{

	Processor processor;
	long max_time_out;
	
	public StopCommand(Processor processor, long max_time_out) {
		super();
		this.processor = processor;
		this.max_time_out = max_time_out;
		setDescription("STOP");
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		return new SimpleCommandDetails("STOP");
	}

	@Override
	public void setDetails(String details) throws Exception {
		//do nothing
	}

	@Override
	public void run() throws Exception {
		obsComp.notifyIObservers(this, new SimpleCommandProgress(100f, "STOP completed"));
		endRun();
		processor.stop(max_time_out);
	}

	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}

	@Override
	public void pause() {
		//do nothing
	}
}