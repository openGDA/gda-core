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

package gda.jython;

import java.io.Serializable;

/**
 * Event broadcast by the JythonServer when haltCurrentScript is called. For client-side objects wanting to react to
 * scripts being halted.
 * <p>
 * TODO the whole scan/script control system needs a review. It was fine when things were simpler, but now the use of
 * scripts and the GUI are more complex, we need a more powerful,unit testable system.
 */
public class ScanInterruptedEvent implements Serializable{
}
