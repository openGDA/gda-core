/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.api.messaging;

/**
 * Marker interface for classes that can be sent as messages. By implementing this interface you must ensure you can be
 * serialised to a cross-language compatible format e.g. JSON, XML, YAML etc. The recommendation is to stick close to
 * POJO objects. If you want to specify the destination see the {@link Destination} annotation.
 *
 * @see Destination
 * @see MessagingService
 *
 * @author James Mudd
 * @since GDA 9.12
 */
public interface Message {

}
