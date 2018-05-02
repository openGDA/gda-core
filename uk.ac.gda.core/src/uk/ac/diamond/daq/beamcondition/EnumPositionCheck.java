/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.beamcondition;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.observable.IObserver;

public class EnumPositionCheck extends BeamConditionBase implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(EnumPositionCheck.class);

	private enum Mode {
		/** Any position not in the collection is allowed */
		PREVENT((c, p) -> !p.contains(c)),
		/** Only positions in the collection are allowed */
		ALLOW((c, p) -> p.contains(c)),
		/** All positions are allowed - no checks are made */
		UNCHECKED((c, p) -> true);

		/** The function used to check if a position is allowed */
		private BiPredicate<String, Collection<String>> condition;
		private Mode(BiPredicate<String, Collection<String>> check) {
			condition = check;
		}
		public boolean check(String position, Set<String> positions) {
			return condition.test(position, positions);
		}
	}

	private EnumPositioner positioner;
	private final Set<String> positions = new LinkedHashSet<>();
	private Mode mode = Mode.UNCHECKED;

	private volatile boolean beamStatus;

	public static EnumPositionCheck isAt(EnumPositioner positioner, String... positions) {
		EnumPositionCheck epc = new EnumPositionCheck();
		epc.setPositioner(positioner);
		epc.setAllowedPositions(positions);
		return epc;
	}

	public static EnumPositionCheck isNotAt(EnumPositioner positioner, String... positions) {
		EnumPositionCheck epc = new EnumPositionCheck();
		epc.setPositioner(positioner);
		epc.setRestrictedPositions(positions);
		return epc;
	}


	@Override
	public boolean beamOn() {
		return beamStatus;
	}

	public void setPositioner(EnumPositioner positioner) {
		if (this.positioner != null) {
			this.positioner.deleteIObserver(this);
		}
		this.positioner = positioner;
		if (positioner != null) {
			this.positioner.addIObserver(this);
			updateBeamStatus();
		} else {
			beamStatus = false;
		}
		updateName();
	}

	public void setAllowedPositions(String... positions) {
		mode = Mode.ALLOW;
		setPositions(positions);
	}

	public void setRestrictedPositions(String... positions) {
		mode = positions.length == 0 ? Mode.UNCHECKED : Mode.PREVENT;
		setPositions(positions);
	}

	private void setPositions(String[] positions) {
		this.positions.clear();
		this.positions.addAll(asList(positions));
		updateName();
		updateBeamStatus();
	}

	@Override
	public void update(Object source, Object arg) {
		logger.debug("Update {} from {}", arg, source);
		updateBeamStatus();
	}

	private void updateBeamStatus() {
		String position;
		try {
			if (positioner == null) {
				position = "UNKNOWN";
			} else {
				position = String.valueOf(positioner.getPosition());
			}
		} catch (DeviceException e) {
			position = "UNAVAILABLE";
			logger.error("Couldn't get position of positioner: {}", positioner.getName(), e);
		}
		beamStatus = mode.check(position, positions);
	}

	private void updateName() {
		StringBuilder sb = new StringBuilder("PositionCheck(");
		sb.append(positioner == null ? "???" : positioner.getName());
		switch (mode) {
		case ALLOW:
			sb.append(" is at ");
			sb.append(positionList());
			break;
		case PREVENT:
			sb.append(" is not at ");
			sb.append(positionList());
			break;
		case UNCHECKED:
			sb.append(" (no restrictions)");
		}
		sb.append(")");
		setName(sb.toString());
	}

	private String positionList() {
		if (positions.size() == 1) {
			return positions.stream().findFirst().get(); // will be there as positions is not empty
		} else {
			return positions.stream().collect(joining(", ", "one of {", "}"));
		}
	}
}
