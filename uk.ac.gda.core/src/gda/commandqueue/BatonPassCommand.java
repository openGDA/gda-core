/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

public class BatonPassCommand extends CommandBase {

	Processor processor;
	String receiverUsername;
	String batonHolderUsername;
	int receiverIndex;
	int batonHolderIndex;

	public BatonPassCommand(
			Processor processor,
			String receiverUsername,
			String batonHolderUsername,
			int receiverIndex,
			int batonHolderIndex) {

		super();
		this.processor = processor;
		this.receiverUsername = receiverUsername;
		this.batonHolderUsername = batonHolderUsername;
		this.receiverIndex = receiverIndex;
		this.batonHolderIndex = batonHolderIndex;
		String sb = new StringBuilder()
				.append("BATON PASS: Client #").append(batonHolderIndex).append(" ").append(batonHolderUsername)
				.append(" -> Client #").append(receiverIndex).append(" ").append(receiverUsername)
				.toString();
		setDescription(sb);
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		return new SimpleCommandDetails("BATON PASS");
	}

	@Override
	public void setDetails(String details) throws Exception {
		// No implementation
	}

	@Override
	public void run() throws Exception {
		ClientDetails[] clients = InterfaceProvider.getBatonStateProvider().getOtherClientInformation();
		boolean automatedClientExists = false;
		int automatedClientIndex = 0;
		for (ClientDetails client : clients) {
			if (client.isAutomatedUser()) {
				automatedClientIndex = client.getIndex();
				automatedClientExists = true;
				break;
			}
		}
		if (automatedClientExists && receiverIndex == automatedClientIndex) {
			// We don't want the command queue to continue if we're passing to an automated client so pause the queue
			processor.stopAfterCurrent();
		}
		InterfaceProvider.getBatonStateProvider().assignBaton(receiverIndex, batonHolderIndex);
		obsComp.notifyIObservers(this, new SimpleCommandProgress(100f, "BATON PASS completed"));
		endRun();
	}

	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}

	@Override
	public void pause() {
		// No implementation
	}
}
