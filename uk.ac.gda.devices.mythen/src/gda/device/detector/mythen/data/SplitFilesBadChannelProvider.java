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

package gda.device.detector.mythen.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * For the B18 Mythen where the bad channels are split into a file per module and need to be merged before performing
 * corrections.
 */
public class SplitFilesBadChannelProvider extends SplitCalibrationFilesBase implements BadChannelProvider {

	Set<Integer> storedBadChannels;
	String storedMode = "";
	String storedCalibrationFolder = "";

	@Override
	public Set<Integer> getBadChannels() {

		if (storedBadChannels != null && storedMode.equals(modules.getMode())
				&& storedCalibrationFolder.equals(modules.getCalibrationFolder())) {
			return storedBadChannels;
		}

		Set<Integer> badChannels = new LinkedHashSet<Integer>();

		for (int moduleNum = 0; moduleNum < modules.getModules().size(); moduleNum++) {

			String badChannelFile = modules.getCalibrationFolder() + "/" + modules.getModules().get(moduleNum) + "/"
					+ modules.getMode() + "/badch/" + modules.getCalibrationFilePrefix() + "Bad."
					+ modules.getModules().get(moduleNum).toLowerCase();

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(badChannelFile)));
				String line;
				while ((line = br.readLine()) != null) {
					int badChannel = Integer.parseInt(line);
					badChannels.add(badChannel + moduleNum * CHANNELSPERMODULE);
				}
				br.close();
			} catch (IOException e) {
				throw new RuntimeException("Could not load bad channels from " + badChannelFile, e);
			}

		}

		storedBadChannels = badChannels;
		storedMode = modules.getMode();
		storedCalibrationFolder = modules.getCalibrationFolder();

		return badChannels;
	}

}
