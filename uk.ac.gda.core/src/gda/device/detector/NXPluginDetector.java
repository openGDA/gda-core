/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector;

import java.util.List;
import java.util.Map;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.scannable.PositionCallableProvider;

public interface NXPluginDetector extends NexusDetector, PositionCallableProvider<NexusTreeProvider> {

	void setAdditionalPluginList(List<NXPluginBase> additionalPluginList);

	List<NXPluginBase> getAdditionalPluginList();

	void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy);

	NXCollectionStrategyPlugin getCollectionStrategy();

	List<NXPluginBase> getPluginList();

	Map<String, NXPluginBase> getPluginMap();
}
