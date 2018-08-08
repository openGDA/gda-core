/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link BadChannelProvider} that reads a list of bad channels from a file.
 */
public class FileBadChannelProvider implements BadChannelProvider {

	private Set<Integer> badChannels;
	
	/**
	 * Creates a bad channel provider using the bad channels in the specified
	 * file.
	 * 
	 * @param badChannelFile the file containing the bad channels
	 */
	public FileBadChannelProvider(File badChannelFile) {
		Set<Integer> badChannels = new LinkedHashSet<Integer>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(badChannelFile)));
			String line;
			while ((line = br.readLine()) != null) {
				int badChannel = Integer.parseInt(line);
				badChannels.add(badChannel);
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not load bad channels from " + badChannelFile, e);
		}
		this.badChannels = Collections.unmodifiableSet(badChannels);
	}
	
	@Override
	public Set<Integer> getBadChannels() {
		return badChannels;
	}

}
