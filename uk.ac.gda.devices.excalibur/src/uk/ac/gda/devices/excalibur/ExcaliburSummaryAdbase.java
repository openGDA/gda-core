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

package uk.ac.gda.devices.excalibur;


/**
 * @model
 */
public interface ExcaliburSummaryAdbase {

	/**
	 * @model
	 */
	public int getGapFillConstant_RBV() throws Exception;

	/**
	 * 
	 */
	public void setGapFillConstant(int gapFillConstant) throws Exception;

	/**
	 * @model
	 */
	public int getFrameDivisor() throws Exception;

	/**
	 * @model
	 */
	public int getFrameDivisor_RBV() throws Exception;

	/**
	 * 
	 */
	public void setFrameDivisor(int frameDivisor) throws Exception;

	/**
	 * @model
	 */
	public int getCounterDepth() throws Exception;

	/**
	 * 
	 */
	public void setCounterDepth(int counterDepth) throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount1() throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount2() throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount3() throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount4() throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount5() throws Exception;

	/**
	 * @model
	 */
	public int getReceiveCount6() throws Exception;

	/**
	 * @model
	 */
	public int getLateStripes() throws Exception;

	/**
	 * @model
	 */
	public int getIncompleteFrames() throws Exception;

	/**
	 * @model
	 */
	public int getIncorrectSequence() throws Exception;

	/**
	 */
	public void clearCounters() throws Exception;

	/**
	 * @model
	 */
	int getGapFillConstant() throws Exception;

}
