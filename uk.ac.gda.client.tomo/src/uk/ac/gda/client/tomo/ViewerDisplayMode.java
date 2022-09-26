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

package uk.ac.gda.client.tomo;

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController;

/**
 * Enum defining the states of the left window view in the tomo alignment view.
 */
public enum ViewerDisplayMode {
	SAMPLE_STREAM_LIVE(TomoAlignmentView.SAMPLE_LIVE_STREAM), FLAT_STREAM_LIVE(TomoAlignmentView.FLAT_LIVE_STREAM), STREAM_STOPPED(
			TomoAlignmentView.STREAM_STOPPED),

	SAMPLE_SINGLE(TomoAlignmentView.SAMPLE_SINGLE) {
		@Override
		public String getFileName(TomoAlignmentController tomoAlignmentController) throws Exception {
			return tomoAlignmentController.getDemandRawTiffFullFileName();
			// return "/dls_sw/dasc/tomo/ravi.tif";

		}

	},
	STATIC_FLAT(TomoAlignmentView.STATIC_FLAT) {
		@Override
		public String getFileName(TomoAlignmentController tomoAlignmentController) {
			return tomoAlignmentController.getFlatImageFullFileName();
		}

	},
	FLAT_SINGLE(TomoAlignmentView.FLAT_SINGLE) {
		@Override
		public String getFileName(TomoAlignmentController tomoAlignmentController) throws Exception {
			return tomoAlignmentController.getDemandRawTiffFullFileName();
		}

	},
	DARK_SINGLE(TomoAlignmentView.STATIC_DARK) {
		@Override
		public String getFileName(TomoAlignmentController tomoAlignmentController) throws Exception {
			return tomoAlignmentController.getDarkFieldImageFullFileName();
		}
	},

	ROTATION_AXIS(TomoAlignmentView.FIND_ROTATION_AXIS_DISPLAY_INFO) {
		@Override
		public String getFileName(TomoAlignmentController tomoAlignmentController) throws Exception {
			return null;
		}
	},
	;

	private final String val;

	ViewerDisplayMode(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}

	@SuppressWarnings("unused")
	public String getFileName(TomoAlignmentController tomoAlignmentController) throws Exception {
		return null;
	}
	public static ViewerDisplayMode getDisplayMode(String info) {
		for (ViewerDisplayMode mode : values()) {
			if (info.equals(mode.getVal())) {
				return mode;
			}
		}
		throw new IllegalArgumentException("Display Mode doesn't exist");
	}
}
