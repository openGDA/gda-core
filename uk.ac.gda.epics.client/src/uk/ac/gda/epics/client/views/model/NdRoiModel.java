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

package uk.ac.gda.epics.client.views.model;

import uk.ac.gda.epics.client.views.controllers.INDROIModelViewController;

public interface NdRoiModel {

	int getMinX() throws Exception;

	int getMinY() throws Exception;

	int getSizeX() throws Exception;

	int getSizeY() throws Exception;

	void setSizeX(int sizeX) throws Exception;

	void setSizeY(int sizeY) throws Exception;

	void setStartX(int startX) throws Exception;

	void setStartY(int startY) throws Exception;

	int getBinX() throws Exception;

	int getBinY() throws Exception;

	void setBinX(int binX) throws Exception;

	void setBinY(int binY) throws Exception;

	boolean registerNDRoiModelViewController(INDROIModelViewController viewController);

	boolean removeNDRoiModelViewController(INDROIModelViewController viewController);

}