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

package uk.ac.gda.ui.tool.spring;

import java.util.Optional;

import org.springframework.stereotype.Service;

import gda.factory.Findable;
import gda.factory.Finder;

/**
 * Provides a service to retrieve {@link Findable} objects.
 *
 * @author Maurizio Nagni
 */
@Service
public class FinderService {

	/**
	 * @param <T> the expected class extending {@code Findable}
	 * @param findableId the findable object identificator
	 * @param findableType the the expected class extending {@code Findable}
	 * @return the required object oherwise {@link Optional#empty()}
	 */
	public synchronized <T extends Findable> Optional<T> getFindableObject(String findableId, Class<T> findableType) {
		return Finder.findOptionalOfType(findableId, findableType);
	}

}
