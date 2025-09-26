/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import uk.ac.diamond.daq.mapping.ui.sampletransfer.HolderSelectionComposite.Position;

/**
 * Represents the state of a sample holder in the system.
 *
 * <p>This class stores the holder's position, whether it's currently in use (busy),
 * and the name of the sample assigned to it.</p>
 *
 * <p>Used for both in-memory tracking and serialization to/from JSON files.</p>
 */
public class HolderState {

	/**
     * The position of the holder (e.g., POSITION_3, SPIGOT).
     */
    private Position position;

    /**
     * Indicates whether the holder is currently occupied (true) or available (false).
     */
    private boolean busy;

    /**
     * The name of the sample assigned to this holder position.
     */
    private String sampleName;

    public HolderState() {
    	// Default constructor needed for Jackson deserialization
    }

    public HolderState(Position position, boolean busy, String sampleName) {
        this.position = position;
        this.busy = busy;
        this.sampleName = sampleName;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    @Override
    public String toString() {
        return "HolderState{" +
                "position=" + position +
                ", busy=" + busy +
                ", sampleName='" + sampleName + '\'' +
                '}';
    }
}