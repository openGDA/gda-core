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

package uk.ac.diamond.daq.guigenerator;

import java.lang.reflect.Method;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiAction;
import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.inspector.impl.BaseTraitStyleConfig;
import org.metawidget.inspector.impl.actionstyle.metawidget.MetawidgetActionStyle;
import org.metawidget.util.ClassUtils;

public class RichbeansActionStyle extends MetawidgetActionStyle{
	public RichbeansActionStyle() {
		this(new BaseTraitStyleConfig());
	}

	public RichbeansActionStyle(BaseTraitStyleConfig config) {
		super(config);
	}

	@Override
	public boolean matchAction(Method method) {
		if ( ClassUtils.getOriginalAnnotation( method, UiAction.class ) == null ) {
			return super.matchAction(method);
		}

		if ( method.getParameterTypes().length > 0 ) {
			throw InspectorException.newException( "@UiAction " + method + " must not take any parameters" );
		}

		return true;
	}

}
