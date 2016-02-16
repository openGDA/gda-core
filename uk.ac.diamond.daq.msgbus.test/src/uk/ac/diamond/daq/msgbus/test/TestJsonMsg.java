/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.msgbus.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import uk.ac.diamond.daq.msgbus.MsgBus.Msg;

public class TestJsonMsg implements Msg {
	public final UUID uuid = UUID.randomUUID();
	public final String s;
	public final int i;
	public final List<Float> fs;
	public final Map<String,Object> map;
	public TestJsonMsg(String s, int i) {
		this.s = s;
		this.i = i;
		this.fs = Arrays.asList(0.1f, 0.2f);
		this.map = new HashMap<>();
	}
}
