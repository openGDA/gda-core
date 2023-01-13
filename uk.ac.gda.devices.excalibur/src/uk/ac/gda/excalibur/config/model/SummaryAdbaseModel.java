package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EObject;

/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

/**
 * @model
 */
public interface SummaryAdbaseModel extends EObject {

	/**
	 * @model
	 */
	int getGapFillConstant();

	/**
	 * 
	 */
	public void setGapFillConstant(int gapFillConstant);

	/**
	 * @model
	 */
	public int getFrameDivisor();

	/**
	 * 
	 */

	public void setFrameDivisor(int frameDivisor);

	/**
	 * @model
	 */

	public int getCounterDepth();

	/**
	 * 
	 */

	public void setCounterDepth(int counterDepth);

}