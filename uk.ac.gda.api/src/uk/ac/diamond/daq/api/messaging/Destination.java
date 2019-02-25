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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to specify the default destination a message will be sent to. It should be applied to a class also implementing
 * the {@link Message} interface. It is required when sendig a message using
 * {@link MessagingService#sendMessage(Message)}.
 * <p>
 * This can be overridden if {@link MessagingService#sendMessage(Message, String)} is used to specify a different
 * destination.
 *
 * @author James Mudd
 * @since GDA 9.12
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Destination {

	String value();
}
