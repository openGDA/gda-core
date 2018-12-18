/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.data;

import gda.data.ScanToElogExtender.SDP2ElogInfo;
import gda.device.Detector;
import gda.device.timer.FrameSet;
import gda.device.timer.Tfg;
import gda.scan.IScanDataPoint;

import java.util.List;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;

public class TfgSettingsExtractor implements SDP2ElogInfo {

	protected boolean doHTML = false;
	
	@Override
	public String extractInfo(IScanDataPoint sdp) {
		
		for(Detector det: sdp.getDetectors()) {
			if (det instanceof NcdDetectorSystem) {
				StringBuilder sb = new StringBuilder();
				NcdDetectorSystem ncddet = (NcdDetectorSystem) det;
				Tfg timer = (Tfg) ncddet.getTimer();
		
				List<FrameSet> framesets = timer.getFramesets();
				
				for (FrameSet f: framesets) {
					sb.append(f.toString());
					if (doHTML) sb.append("<br />\n");
					else sb.append("\n");
				}
				
				return sb.toString();
			}
		}
		
		return "";
	}
}