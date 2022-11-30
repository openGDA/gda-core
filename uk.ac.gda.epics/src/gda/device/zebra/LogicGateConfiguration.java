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

package gda.device.zebra;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Holds configuration for a Zebra logic gate (AND or OR). Cannot be instantiated directly; create instances using the builder class, {@link Builder}.
 */
public class LogicGateConfiguration {

	/**
	 * Builder for {@link LogicGateConfiguration} objects.
	 *
	 * <p>
	 * Specify an input first, then the source for that input, then (optional) whether the source should be inverted. Examples:
	 *
	 * <pre>
	 * new LogicGateConfiguration.Builder().input(1).source(60).invert().build(); // INP1 = 60 (SOFT_IN1), inverted
	 * new LogicGateConfiguration.Builder().input(2).source(61).build(); // INP2 = 61 (SOFT_IN2), not inverted</pre>
	 *
	 * <p>
	 * Specifications for multiple inputs can be chained. Example:
	 *
	 * <pre>
	 * new LogicGateConfiguration.Builder().input(1).source(32).input(2).source(33).build(); // INP1 = 32 (AND1); INP2 = 33 (AND2)</pre>
	 *
	 * <p>
	 * Inputs not explicitly mentioned are disabled, and their sources are set to 0 (DISCONNECT):
	 *
	 * <pre>
	 * new LogicGateConfiguration.Builder().build(); // disable all four inputs; set all four sources to 0 (DISCONNECT)</pre>
	 */
	public static class Builder {

		private int inputNumber;

		private final boolean[] use = new boolean[4];

		private final int[] sources = new int[4];

		private final boolean[] invert = new boolean[4];

		public Builder input(int inputNumber) {
			Preconditions.checkArgument(isValidInputNumber(inputNumber), "Input number must be between 1 and 4");
			this.inputNumber = inputNumber;
			use[inputNumber - 1] = true;
			return this;
		}

		public Builder source(int source) {
			Preconditions.checkArgument(isValidInputNumber(inputNumber), "Specify an input number with input(...) before using source(...)");
			Preconditions.checkArgument(isValidSource(source), "Source must be between 0 and 63 inclusive");
			sources[inputNumber - 1] = source;
			return this;
		}

		public Builder invert() {
			Preconditions.checkArgument(isValidInputNumber(inputNumber), "Specify an input number with input(...) before using invert()");
			invert[inputNumber - 1] = true;
			return this;
		}

		public LogicGateConfiguration build() {
			final LogicGateConfiguration config = new LogicGateConfiguration(use, sources, invert);
			return config;
		}
	}

	private static boolean isValidInputNumber(int input) {
		return (1 <= input) && (input <= 4);
	}

	private static boolean isValidSource(int source) {
		return (0 <= source) && (source <= 63);
	}

	private LogicGateConfiguration(boolean[] use, int[] sources, boolean[] invert) {
		this.use = use;
		this.sources = sources;
		this.invert = invert;
	}

	private final boolean[] use;

	private final int[] sources;

	private final boolean[] invert;

	public boolean[] getUse() {
		return use;
	}

	public int[] getSources() {
		return sources;
	}

	public boolean[] getInvert() {
		return invert;
	}

	@Override
	public String toString() {
		final List<String> bits = new ArrayList<>();
		for (int input = 1; input <= 4; input++) {
			if (use[input - 1]) {
				String bit = "INP" + input + "=";
				if (invert[input - 1]) {
					bit += "!";
				}
				bit += sources[input - 1];
				bits.add(bit);
			}
		}
		return LogicGateConfiguration.class.getSimpleName() + bits;
	}

}
