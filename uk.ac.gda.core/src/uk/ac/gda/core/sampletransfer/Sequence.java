/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;


public enum Sequence {
    AIR_TO_VACUUM,
    VACUUM_TO_AIR,
    HOTEL_TO_DOME_PREPARE,
    HOTEL_TO_DOME_GRIP,
    SAMPLE_INTO_DOME,
    REMOVE_SAMPLE,
    PARK_SAMPLE_IN_HOTEL;

    public boolean requiresSampleSelection() {
        return this == HOTEL_TO_DOME_GRIP;
    }

    public boolean requiresSample() {
        return this == SAMPLE_INTO_DOME || this == REMOVE_SAMPLE || this == PARK_SAMPLE_IN_HOTEL;
    }
}
