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

package uk.ac.gda.server.ncd.samplerack;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class SampleConfiguration implements Serializable {
	private List<Sample> samples = new ArrayList<>();
	private String background;

	public Sample[] samples() {
		return samples.stream().toArray(Sample[]::new);
	}
	public void addSample(Sample sample) {
		samples.add(sample);
	}
	public void removeSample(Sample sample) {
		samples.remove(sample);
	}
	public void saveTo(String file) throws IOException {
		var gson = new Gson();
		String json = gson.toJson(this);
		Files.writeString(Paths.get(file), json);
	}

	public static SampleConfiguration fromFile(String file) throws IOException {
		var gson = new Gson();
		var json = Files.readString(Paths.get(file));
		return gson.fromJson(json, SampleConfiguration.class);
	}

	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}

	public Sample[] getActiveSamples() {
		return samples.stream().filter(Sample::isActive).toArray(Sample[]::new);
	}

}
