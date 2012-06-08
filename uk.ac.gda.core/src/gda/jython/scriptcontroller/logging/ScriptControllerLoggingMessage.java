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

package gda.jython.scriptcontroller.logging;

import gda.commandqueue.CommandProgress;

import java.io.Serializable;

/**
 * Beans which are sent used by a script to send messages to its ScriptContoller.
 * <p>
 * The broadcast of the beans are logged in a database by the ScriptContoller. The method which provide data should be
 * marked by annotation.
 */
public interface ScriptControllerLoggingMessage extends Serializable, CommandProgress{
}
