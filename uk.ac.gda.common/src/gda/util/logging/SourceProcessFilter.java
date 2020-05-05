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

package gda.util.logging;

import static ch.qos.logback.core.spi.FilterReply.ACCEPT;
import static ch.qos.logback.core.spi.FilterReply.DENY;
import static gda.util.logging.LogbackUtils.SOURCE_PROPERTY_NAME;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Logger contexts for GDA client and server processes are configured with
 * {@link LogbackUtils#SOURCE_PROPERTY_NAME} which corresponds to the "name" of the
 * process they exist in. Each process contains its own appenders for
 * console/graylog however all events are sent to the server so for console/Graylog appenders
 * filter to only log server events
 */
public class SourceProcessFilter extends Filter<ILoggingEvent> {

	private String contextSourceName = "UNDEFINED";

	@Override
	public FilterReply decide(ILoggingEvent event) {
		String eventSourceName = event.getLoggerContextVO().getPropertyMap().get(SOURCE_PROPERTY_NAME);
		if (contextSourceName.equals(eventSourceName)) {
			return ACCEPT;
		}
		return DENY;
	}

	public String getContextSourceName() {
		return contextSourceName;
	}

	/**
	 * Can be set from xml e.g
	 * <pre>&ltlocalSourceName&gt${GDA_SOURCE}&lt/localSourceName&gt</pre>
	 * @param contextSourceName
	 */
	public void setContextSourceName(String contextSourceName) {
		this.contextSourceName = contextSourceName;
	}

}
