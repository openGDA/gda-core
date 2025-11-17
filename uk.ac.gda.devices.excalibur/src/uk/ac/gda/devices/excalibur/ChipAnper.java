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
 *
 */
public interface ChipAnper {

	int getPreamp() throws Exception;

	void setPreamp(int preamp) throws Exception;

	int getIkrum() throws Exception;

	void setIkrum(int ikrum) throws Exception;

	int getShaper() throws Exception;

	void setShaper(int shaper) throws Exception;

	int getDisc() throws Exception;

	void setDisc(int disc) throws Exception;

	int getDiscls() throws Exception;

	void setDiscls(int discls) throws Exception;

	int getThresholdn() throws Exception;

	void setThresholdn(int thresholdn) throws Exception;

	int getDacPixel() throws Exception;

	void setDacPixel(int dacPixel) throws Exception;

	int getDelay() throws Exception;

	void setDelay(int delay) throws Exception;

	int getTpBufferIn() throws Exception;

	void setTpBufferIn(int tpBufferIn) throws Exception;

	int getTpBufferOut() throws Exception;

	void setTpBufferOut(int tpBufferOut) throws Exception;

	int getRpz() throws Exception;

	void setRpz(int rpz) throws Exception;

	int getGnd() throws Exception;

	void setGnd(int gnd) throws Exception;

	int getTpref() throws Exception;

	void setTpref(int tpref) throws Exception;

	int getFbk() throws Exception;

	void setFbk(int fbk) throws Exception;

	int getCas() throws Exception;

	void setCas(int cas) throws Exception;

	int getTprefA() throws Exception;

	void setTprefA(int tprefA) throws Exception;

	int getTprefB() throws Exception;

	void setTprefB(int tprefB) throws Exception;

	int getThreshold0() throws Exception;

	void setThreshold0(int threshold) throws Exception;

	int getThreshold1() throws Exception;

	void setThreshold1(int threshold) throws Exception;

	int getThreshold2() throws Exception;

	void setThreshold2(int threshold) throws Exception;

	int getThreshold3() throws Exception;

	void setThreshold3(int threshold) throws Exception;

	int getThreshold4() throws Exception;

	void setThreshold4(int threshold) throws Exception;

	int getThreshold5() throws Exception;

	void setThreshold5(int threshold) throws Exception;

	int getThreshold6() throws Exception;

	void setThreshold6(int threshold) throws Exception;

	int getThreshold7() throws Exception;

	void setThreshold7(int threshold) throws Exception;
}