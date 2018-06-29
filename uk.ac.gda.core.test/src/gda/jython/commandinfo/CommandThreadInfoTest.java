/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython.commandinfo;

import static gda.jython.commandinfo.CommandThreadType.COMMAND;
import static java.lang.Thread.State.RUNNABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;

public class CommandThreadInfoTest {

	private static final UUID ID = UUID.fromString("e34dc90d-1034-425a-8484-2926dda240de");
	private static final LocalDateTime DATETIME = LocalDateTime.of(2018, 06, 25, 14, 15, 16);

	private static CommandThreadInfo getCTI() {
		return CommandThreadInfo.builder()
				.command("test command")
				.name("test_thread")
				.datetime(DATETIME)
				.id(27)
				.interrupted(false)
				.jythonServerThreadId(ID.toString())
				.priority(3)
				.state(RUNNABLE)
				.threadType(COMMAND)
				.build();
	}

	@Test
	public void testNormalUseBuildsCorrectInfo() throws Exception {
		CommandThreadInfo cti = getCTI();
		assertThat(cti.getCommand(), is("test command"));
		assertThat(cti.getName(), is("test_thread"));
		assertThat(cti.getDate(), is("2018-06-25"));
		assertThat(cti.getTime(), is("14:15:16"));
		assertThat(cti.getId(), is(27L));
		assertThat(cti.isInterrupted(), is(false));
		assertThat(cti.getJythonServerThreadId(), is(ID.toString()));
		assertThat(cti.getPriority(), is(3));
		assertThat(cti.getState(), is("RUNNABLE"));
		assertThat(cti.getCommandThreadType(), is("COMMAND"));
	}

	@Test
	public void testToString() throws Exception {
		String string = "CommandThreadInfo [threadType=COMMAND, "
				+ "id=27, "
				+ "jythonServerThreadId=e34dc90d-1034-425a-8484-2926dda240de, "
				+ "priority=3, "
				+ "name=test_thread, "
				+ "state=RUNNABLE, "
				+ "date=2018-06-25, "
				+ "time=14:15:16, "
				+ "isInterrupted=false, "
				+ "command=test command]";
		CommandThreadInfo cti = getCTI();
		String fromToString = cti.toString();

		assertThat(fromToString, is(string));
		// string should be cached
		assertThat(cti.toString(), is(sameInstance(fromToString)));
	}

	@Test(expected=NullPointerException.class)
	public void testEmptyBuilderFails() {
		CommandThreadInfo.builder().build();
	}

	@Test(expected=NullPointerException.class)
	public void testNullCommandCheck() {
		CommandThreadInfo.builder().command(null);
	}

	@Test(expected=NullPointerException.class)
	public void testNullDateCheck() {
		CommandThreadInfo.builder().datetime(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeIDCheck() {
		CommandThreadInfo.builder().id(-1);
	}

	@Test(expected=NullPointerException.class)
	public void testNullThreadIdCheck() {
		CommandThreadInfo.builder().jythonServerThreadId(null);
	}

	@Test(expected=NullPointerException.class)
	public void testNullNameCheck() {
		CommandThreadInfo.builder().name(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativePriorityCheck() {
		CommandThreadInfo.builder().priority(-1);
	}

	@Test(expected=NullPointerException.class)
	public void testNullStateCheck() {
		CommandThreadInfo.builder().state(null);
	}


}
