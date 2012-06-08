/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.server.collisionAvoidance;

import gda.factory.Findable;

/**
 * Interface defining a collision checker for use with the collision avoidance system.S
 */
public interface CollisionChecker extends Findable {

	/**
	 * Checks to see if a move is safe. This will be called by the CAC with which the checker will have been registered.
	 * 
	 * @param currentRangeStart
	 *            Each value will be either the corresponding field's current position or, if the field is moving, the
	 *            position where it began its move.
	 * @param currentRangeEnd
	 *            Each value will be null unless the field is moving, when it will be the position where it should
	 *            finish its move.
	 * @param requestedFinal
	 *            The requested final position. NOTE: currently some values are allowed to by None, this will be removed
	 *            ASAP.
	 * @return Null if the move is allowed, otherwise a string describing why it is not.
	 * @throws CacException
	 */
	public String[] checkMove(Double[] currentRangeStart, Double[] currentRangeEnd, Double[] requestedFinal)
			throws CacException;

	/**
	 * Returns a useful description of the checker's configuration.
	 * 
	 * @return the string
	 */
	@Override
	public String toString();

}
