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

package uk.ac.diamond.daq.guigenerator.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiAction;
import org.junit.Test;
import org.metawidget.inspector.iface.InspectorException;

import uk.ac.diamond.daq.guigenerator.RichbeansActionStyle;

public class RichbeansActionStyleTest {
	private final RichbeansActionStyle style = new RichbeansActionStyle();

	@Test
	public void testFindsRichbeansUiActionAnnotation() throws NoSuchMethodException, SecurityException{
		assertTrue(style.matchAction(TestExample.class.getMethod("method")));
	}

	@Test(expected = InspectorException.class)
	public void testThrowsErrorIfMethodHasParameters() throws NoSuchMethodException, SecurityException{
		assertTrue(style.matchAction(TestExample.class.getMethod("methodWithParams", String.class)));
	}

	@Test
	public void testDoesntFindOtherMethods() throws NoSuchMethodException, SecurityException{
		assertFalse(style.matchAction(TestExample.class.getMethod("methodWithoutUiAction")));
	}

	@Test
	public void testFindsMetawidgetUiActionAnnotation() throws NoSuchMethodException, SecurityException{
		assertTrue(style.matchAction(TestExample.class.getMethod("metawidgetAnnotationMethod")));
	}

	private interface TestExample{
		@UiAction
		public void method();
		@UiAction
		public void methodWithParams(String param1);

		public void methodWithoutUiAction();

		@org.metawidget.inspector.annotation.UiAction
		public void metawidgetAnnotationMethod();

	}
}
