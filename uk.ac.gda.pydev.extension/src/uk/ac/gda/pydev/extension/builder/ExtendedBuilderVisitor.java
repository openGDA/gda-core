/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.pydev.extension.builder;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.builder.PyDevBuilderVisitor;
import org.python.pydev.shared_core.callbacks.ICallback0;

public class ExtendedBuilderVisitor extends PyDevBuilderVisitor {


	@Override
	public void visitChangedResource(IResource arg0, ICallback0<IDocument> arg1, IProgressMonitor arg2) {
		//not sure what to do here so provide default implementation
	}

	@Override
	public void visitRemovedResource(IResource arg0, ICallback0<IDocument> arg1, IProgressMonitor arg2) {
		//not sure what to do here so provide default implementation
		
	}

}
