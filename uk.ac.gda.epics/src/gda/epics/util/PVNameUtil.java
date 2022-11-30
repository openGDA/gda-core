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

package gda.epics.util;

import java.util.Arrays;

public class PVNameUtil {

	private static final String PV_SEGMENT_SEPARATOR = ":";

	private static final String[] NO_SEGMENTS = new String[0];

	private PVNameUtil() {
		// private constructor to prevent instantiation
	}

	/**
	 * Returns the common base PV name of the given PV names, if one exists, otherwise <code>null</code>.
	 * This value returned consists of the common segments of the given PV names, where ':' is
	 * pv segment separator. For example, the base PV of {@code BL05I-DI-PHDGN-07:IONC:Y:MP:SELECT.VAL}
	 * and {@code BL05I-DI-PHDGN-07:IONC:Y:MP:INPOS.VAL} is {@code BL05I-DI-PHDGN-07:IONC:Y:MP}.
	 * <p>
	 * Note that '-' is not treated as a separator, so the PVs {@code BL18B-DI-PHDGN-01} and
	 * {@code BL18B-DI-PHDGN-02} are not considered to have a common base PV. In that case this this method
	 * will return {@code null} rather than {@code BL18B-DI-PHDGN}.
	 * @param pvNames
	 * @return common base PV name or <code>null</code>
	 */
	public static String getBasePvName(String... pvNames) {
		return Arrays.stream(pvNames)
			.map(str -> str == null ? NO_SEGMENTS : str.split(PV_SEGMENT_SEPARATOR))
			.reduce(PVNameUtil::getCommonSegments)
			.map(segments -> String.join(PV_SEGMENT_SEPARATOR, segments))
			.map(basePv -> basePv.isBlank() ? null : basePv)
			.orElse(null);
	}

	private static String[] getCommonSegments(String[] pv1Segments, String[] pv2Segments) {
		if (pv1Segments.length == 0 || pv2Segments.length == 0) {
			return NO_SEGMENTS;
		}

		for (int i = 0; i < pv1Segments.length; i++) {
			if (pv2Segments.length <= i || !pv1Segments[i].equals(pv2Segments[i])) {
				// found a non-matching segment, return previous segments
				return Arrays.copyOf(pv1Segments, i);
			}
		}

		return pv1Segments; // both pvs are the same
	}

}
