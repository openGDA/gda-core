/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.ncd.widgets;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.None;

import java.util.Map;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import gda.configuration.properties.LocalProperties;
import gda.device.EnumPositioner;
import gda.factory.Finder;

public class ShutterControls extends Composite {

	@SuppressWarnings("unused") // The shutter groups appear unused
	public ShutterControls(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		Map<String, EnumPositioner> shutterDevices = Finder.listFindablesOfType(EnumPositioner.class).stream()
				.collect(toMap(EnumPositioner::getName, ep -> ep));
		String[] shuttersToDisplay = LocalProperties.getStringArray("gda.rcp.ncd.views.shutterOrder");
		stream(shuttersToDisplay).map(row -> row.split(":")).forEach(row -> {
			var rowComp = new Composite(this, None);
			rowComp.setLayout(new GridLayout(row.length, true));
			rowComp.setLayoutData(new GridData(FILL, FILL, true, false));
			for (var shutter: row) {
				new ShutterGroup(rowComp, None, shutterDevices.get(shutter));
			}
		});
	}
}
