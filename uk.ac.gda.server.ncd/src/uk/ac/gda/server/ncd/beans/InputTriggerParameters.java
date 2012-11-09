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

package uk.ac.gda.server.ncd.beans;

import org.apache.commons.beanutils.BeanUtils;

public class InputTriggerParameters {
	public static final String[] triggers = { "BM Trigger", "ADC chan 0", "ADC chan 1", "ADC chan 2", "ADC chan 3",
		"ADC chan 4", "ADC chan 5", "TTL trig 0", "TTL trig 1", "TTL trig 2", "TTL trig 3", "LVDS Lemo",
		"TFG cable 1", "TFG cable 2", "TFG cable 3", "Var thrshld" };
	private String name;
	private Double debounce;
	private Double threshold;

	public InputTriggerParameters() {
	}

	public String getName() {
		return name;
	}

	public Double getDebounce() {
		return debounce;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDebounce(Double debounce) {
		this.debounce = debounce;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((debounce == null) ? 0 : debounce.hashCode());
		result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InputTriggerParameters other = (InputTriggerParameters) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (debounce == null) {
			if (other.debounce != null) {
				return false;
			}
		} else if (!debounce.equals(other.debounce)) {
			return false;
		}
		if (threshold == null) {
			if (other.threshold != null) {
				return false;
			}
		} else if (!threshold.equals(other.threshold)) {
			return false;
		}
		return true;
	}
}
