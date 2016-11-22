/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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


public class TestCommand extends CommandBase {

	public boolean skip = false;
	public boolean pause = false;
	public int steps = 10;
	public int currentStep = 0;
	Processor processor;
	public boolean throwException=false;


	public TestCommand(Processor processor, long max_time_out) {
		this(processor, max_time_out, 10);
	}

	public TestCommand(Processor processor, @SuppressWarnings("unused") long max_time_out, int steps) {
		super();
		this.processor = processor;
		this.steps = steps;
	}

	@Override
	public void run() throws Exception {
		super.beginRun();
		while (currentStep < steps ) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// do nothing
			}
			if( currentStep == 2 ){
				if(pause){
					/*
					 * we need to go into pause state and inform the processor
					 */
					pause();
				}
				if(skip){
					/*
					 * we need to go into pause state and inform the processor
					 */
					abort();
				}
				if(throwException){
					throw new Exception("TestCommand exception");
				}
			}
			if( getState() != STATE.PAUSED)
				currentStep++;
		}
		if( getState() != STATE.ABORTED)
			super.endRun();
	}

	@Override
	public CommandSummary getCommandSummary() {
		return new SimpleCommandSummary(getDescription());
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		return new SimpleCommandDetails("Not editable");
	}

	@Override
	public void setDetails(String details) throws Exception {
	}


}
