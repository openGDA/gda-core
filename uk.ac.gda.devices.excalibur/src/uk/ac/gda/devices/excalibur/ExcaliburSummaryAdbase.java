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

	int getGapFillConstant_RBV() throws Exception;

	void setGapFillConstant(int gapFillConstant) throws Exception;

	int getFrameDivisor() throws Exception;

	int getFrameDivisor_RBV() throws Exception;

	void setFrameDivisor(int frameDivisor) throws Exception;

	int getCounterDepth() throws Exception;

	void setCounterDepth(int counterDepth) throws Exception;

	int getReceiveCount1() throws Exception;

	int getReceiveCount2() throws Exception;

	int getReceiveCount3() throws Exception;

	int getReceiveCount4() throws Exception;

	int getReceiveCount5() throws Exception;

	int getReceiveCount6() throws Exception;

	int getLateStripes() throws Exception;

	int getIncompleteFrames() throws Exception;

	int getIncorrectSequence() throws Exception;

	void clearCounters() throws Exception;

	int getGapFillConstant() throws Exception;
}
