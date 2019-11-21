/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;

public class TwoAxisLissajousModelExpresser extends AbstractPointsModelExpresser<TwoAxisLissajousModel> {

	@Override
	String pyExpress(TwoAxisLissajousModel model, Collection<IROI> rois, boolean verbose) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("lissajous(");
		sb.append(verbose?"axes=":"");
		sb.append("('"+model.getxAxisName()+"', '");
		sb.append(model.getyAxisName()+"'), ");
		sb.append(verbose?"start=":"");
		sb.append("("+formatValue(model.getBoundingBox().getxAxisStart())+", "+formatValue(model.getBoundingBox().getyAxisStart())+"), ");
		sb.append(verbose?"stop=":"");
		sb.append("("+formatValue(model.getBoundingBox().getxAxisEnd())+", "+formatValue(model.getBoundingBox().getyAxisEnd())+"), ");
		sb.append(verbose?"a=":"");
		sb.append(model.getA()+", ");
		sb.append(verbose?"b=":"");
		sb.append(model.getB()+", ");
		sb.append(verbose?"points=":"");
		sb.append(model.getPoints());
		appendCommonProperties(sb, model, verbose);
		sb.append(getROIPyExpression(rois, verbose));
		sb.append(")");

		return sb.toString();
	}
}
