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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of {@link BadChannelProvider} that stores a fixed list of bad
 * channels in memory.
 */
public class SimpleBadChannelProvider implements BadChannelProvider {
	
	private Set<Integer> badChannels;

	/**
	 * Creates a {@link SimpleBadChannelProvider} that holds the specified bad
	 * channels.
	 * 
	 * @param badChannels the bad channels
	 */
	public SimpleBadChannelProvider(Set<Integer> badChannels) {
		this.badChannels = Collections.unmodifiableSet(badChannels);
	}
	
	/**
	 * Creates a {@link SimpleBadChannelProvider} that holds the given bad
	 * channels.
	 * 
	 * @param badChannels the bad channels
	 */
	public SimpleBadChannelProvider(int... badChannels) {
		this.badChannels = new TreeSet<Integer>();
		for (int badChannel : badChannels) {
			this.badChannels.add(badChannel);
		}
	}
	
	@Override
	public Set<Integer> getBadChannels() {
		return badChannels;
	}

}
