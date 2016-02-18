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

package uk.ac.diamond.daq.msgbus;

import static uk.ac.diamond.daq.msgbus.MsgBus.publish;
import static uk.ac.diamond.daq.msgbus.MsgBus.subscribe;
import static uk.ac.diamond.daq.msgbus.MsgBus.unsubscribe;
import uk.ac.diamond.daq.msgbus.MsgBus.Msg;

public class FluentMsgBus {

	public static <T extends Msg> T published(T msg) {
		publish(msg);
		return msg;
	}

	public static <T> T subscribed(T subscriber) {
		subscribe(subscriber);
		return subscriber;
	}

	public static <T> T unsubscribed(T subscriber) {
		unsubscribe(subscriber);
		return subscriber;
	}

}
