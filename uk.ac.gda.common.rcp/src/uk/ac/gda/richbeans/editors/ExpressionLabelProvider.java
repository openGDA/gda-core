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

package uk.ac.gda.richbeans.editors;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.swtdesigner.SWTResourceManager;

public class ExpressionLabelProvider extends LabelProvider implements ILabelProvider {

	private Collection<String> fields;
    private static Image       expressionImage,constantImage,operatorImage,functionImage;
    
	public ExpressionLabelProvider(Collection<String> fields) {
		this.fields    = Collections.unmodifiableCollection(fields);
		if (expressionImage==null) expressionImage = SWTResourceManager.getImage(ExpressionLabelProvider.class, "/icons/bullet_blue.png");
		if (constantImage  ==null) constantImage   = SWTResourceManager.getImage(ExpressionLabelProvider.class, "/icons/bullet_black.png");
		if (operatorImage  ==null) operatorImage   = SWTResourceManager.getImage(ExpressionLabelProvider.class, "/icons/bullet_purple.png");
		if (functionImage  ==null) functionImage   = SWTResourceManager.getImage(ExpressionLabelProvider.class, "/icons/bullet_star.png");
	}

	@Override
	public String getText(final Object element) {
		final IContentProposal prop  = (IContentProposal)element;
		final String           label =  prop.getContent();
		return label;
	}
	
	@Override
	public Image getImage(final Object element) {
		final IContentProposal prop  = (IContentProposal)element;
		final String           label =  prop.getContent();
		if (fields.contains(label))                                            return expressionImage;
		if (ExpressionUtils.getFunctionsWithOpeningBrackets().contains(label)) return functionImage;
		if (ExpressionUtils.getConstants().contains(label))                    return constantImage;
		if (ExpressionUtils.getOperators().contains(label))                    return operatorImage;
		return null;
	}

}
