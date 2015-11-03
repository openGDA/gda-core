/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.epics.EpicsConstants;

public interface EpicsAreaDetectorConstants extends EpicsConstants {
	public enum DataType {
		Int8, UInt8, Int16, UInt16, Int32, UInt32, Float32, Float64;
	}

	public enum ColourMode {
		Mono, Bayer, RGB1, RGB2, RGB3, YUV444, YUV422, YUV421;
	}

	public enum ImageMode {
		Single, Multiple, Continuous;
	}

	public enum Acquire {
		Done, Acquire;
	}

	public enum ShutterControl {
		Close, Open;
	}

	public enum FrameType {
		Normal, Background, FlatField, DblCorrelation;
	}
	public enum TriggerMode {
		Internal, External;
	}
}
