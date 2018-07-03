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

package uk.ac.gda.remoting.client;

import gda.util.SpringObjectServer;

/**
 * Marker interface that is implemented by RMI proxies created by {@link GdaRmiProxyFactoryBean}. Allows
 * {@link SpringObjectServer} to determine if an object is an RMI proxy - in which case, its {@code configure()} method
 * will not be called at client/server startup time.
 */
public interface RmiProxyMarker {

}
