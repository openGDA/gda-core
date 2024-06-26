/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.service.command.strategy;

import uk.ac.gda.common.entity.Document;

/**
 * A set of methods to format the output.
 * 
 * @author Maurizio Nagni
 *
 */
public class OutputStrategyFactory {

	private OutputStrategyFactory() {}
	
	public static final <T extends Document> OutputStrategy<T>  getJSONCollectionOutputStrategy() {
		return new JSONOutputStrategy<>();
	}
		
	public static final <T> OutputStrategy<T>  getJSONOutputStrategy() {
		return new JSONOutputStrategy<>();
	}
}
