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

package uk.ac.gda.client.hrpd.viewextensionfactories;

import org.eclipse.ui.ExtensionFactory;

import uk.ac.gda.client.hrpd.viewfactories.LivePlotViewFactory;
import uk.ac.gda.client.hrpd.views.LivePlotView;
import gda.rcp.views.ViewFactory;
/**
 * an implementation of {@link ExtensionFactory} mechanism to delegate the {@link LivePlotView} extension 
 * contribution to {@link org.eclipse.ui.views} extension-point to its {@link LivePlotViewFactory} factory 
 * instance as 
 * {@code uk.ac.gda.client.hrpd.viewextensionfactories.LivePlotViewExtensionFactory:macliveplotviewfactory}
 * where {@code macliveplotviewfactory} is a Spring bean of {@link LivePlotViewFactory} type.
 */
public class LivePlotViewExtensionFactory extends ViewFactory {
	public static final String ID = "uk.ac.gda.client.hrpd.views.liveplotview";
}
