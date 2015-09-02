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

package gda.device.detector.addetector.triggering;


public class MedipixTriggerModeProvider extends SimpleTriggerModeProvider {

	final public static TriggerMode TriggerInternal=new TriggerMode("Internal",0);
	final public static TriggerMode TriggerEnable=new TriggerMode("Enable",1);
	final public static TriggerMode TriggerStartRising=new TriggerMode("StartRising",2);
	final public static TriggerMode TriggerStartFalling=new TriggerMode("StartFalling",3);
	final public static TriggerMode TriggerBothRising=new TriggerMode("BothRising",4);
	final public static TriggerMode TriggerSoftware=new TriggerMode("Software",5);
	
	public MedipixTriggerModeProvider(TriggerMode mode) {
		super(mode);
	}

	public void setTriggerInternal(){
		setTriggerMode(TriggerInternal);
	}

	public void setTriggerEnable(){
		setTriggerMode(TriggerEnable);
	}

	public void setTriggerStartRising(){
		setTriggerMode(TriggerStartRising);
	}

	public void setTriggerStartFalling(){
		setTriggerMode(TriggerStartFalling);
	}

	public void setTriggerBothRising(){
		setTriggerMode(TriggerBothRising);
	}

	public void setTriggerSoftware(){
		setTriggerMode(TriggerSoftware);
	}
	
}
