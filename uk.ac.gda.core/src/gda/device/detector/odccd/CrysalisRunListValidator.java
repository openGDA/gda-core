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

package gda.device.detector.odccd;


import gda.configuration.properties.LocalProperties;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * Class used to validate the diffractometer positions specified in a run list are valid
 *
 */
public class CrysalisRunListValidator {

	DoubleLimit domegaindeg, ddetectorindeg, dkappaindeg, dphiindeg, dscanwidthindeg,
			dscanspeedratio, dexposuretimeinsec;

	/**
	 * Constructor that uses validation in LocalProperties.get(LocalProperties.GDA_CONFIG) + "/xml/CrysalisRunListValidator.xml"
	 */
	public CrysalisRunListValidator() {
		this(null);
	}
	/**
	 * @param configPath Path to xml defining the validation criteria
	 */
	public CrysalisRunListValidator(String configPath) {
		this(configPath, false);
	}

	CrysalisRunListValidator(String configPath, boolean allowDefaults) {
		if (configPath == null) {
			configPath = LocalProperties.get(LocalProperties.GDA_CONFIG) + "/xml/CrysalisRunListValidator.xml";
		}
		try {
			XMLConfiguration config=null;
			try{
				config = new XMLConfiguration(configPath);
			}
			catch(Exception ex){
				if(!allowDefaults )
					throw ex;
			}
			ddetectorindeg = new DoubleLimit("ddetectorindeg", config);
			dkappaindeg = new DoubleLimit("dkappaindeg", config);
			domegaindeg = new DoubleLimit("domegaindeg", config);
			dphiindeg = new DoubleLimit("dphiindeg", config);
			dscanwidthindeg = new DoubleLimit("dscanwidthindeg", config);
			dscanspeedratio = new DoubleLimit("dscanspeedratio", config);
			dexposuretimeinsec = new DoubleLimit("dexposuretimeinsec", config);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to read configuration from " + configPath, ex);
		}
	}

	void checkValidity(CrysalisRun run) throws CrysalisValidityException {
		ddetectorindeg.checkValidity(run.ddetectorindeg);
		dkappaindeg.checkValidity(run.dkappaindeg);
		dscanspeedratio.checkValidity(run.dscanspeedratio);
		dexposuretimeinsec.checkValidity(run.dexposuretimeinsec);
		dscanwidthindeg.checkValidity(run.dscanwidthindeg);
		if(run.scanType() == CrysalisRun.SCANTYPE.PHI){
			domegaindeg.checkValidity(run.domegaindeg);
			dphiindeg.checkValidity(run.dscanstartindeg);
			dphiindeg.checkValidity(run.dscanendindeg);
		} else if(run.scanType() == CrysalisRun.SCANTYPE.OMEGA){
			dphiindeg.checkValidity(run.dphiindeg);
			domegaindeg.checkValidity(run.dscanstartindeg);
			domegaindeg.checkValidity(run.dscanendindeg);
		} else {
			throw new CrysalisValidityException("scanType is invalid - " + run.scanType());
		}
	}

	/**
	 * @param runList
	 * @throws CrysalisValidityException if not valid
	 */
	public void checkValidity(CrysalisRunList runList) throws CrysalisValidityException {
		for (CrysalisRun run : runList.runlist) {
			try{
				checkValidity(run);
			}
			catch(CrysalisValidityException ex){
				throw new CrysalisValidityException("Run - '" + run.name + "' is invalid", ex);
			}
		}
	}
	
	/**
	 * @param configPath  Save validation criteria to an XML doc
	 */
	public void saveToConfig(String configPath){
		if (configPath == null) {
			configPath = LocalProperties.get(LocalProperties.GDA_CONFIG) + "/xml/CrysalisRunListValidator.xml";
		}		
		try {
			XMLConfiguration config;
			config = new XMLConfiguration();
			
			domegaindeg.addToConfig(config);
			ddetectorindeg.addToConfig(config);
			dkappaindeg.addToConfig(config);
			dphiindeg.addToConfig(config);
			dscanwidthindeg.addToConfig(config);
			dscanspeedratio.addToConfig(config);
			dexposuretimeinsec.addToConfig(config);
			config.save(configPath);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to read configuration from " + configPath, ex);
		}
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CrysalisRunListValidator)) {
			return false;
		}

		CrysalisRunListValidator other = (CrysalisRunListValidator) o;
		return 
			this.domegaindeg.equals(other.domegaindeg) &&
			this.ddetectorindeg.equals(other.ddetectorindeg) &&
			this.dkappaindeg.equals(other.dkappaindeg) &&
			this.dphiindeg.equals(other.dphiindeg) &&
			this.dscanwidthindeg.equals(other.dscanwidthindeg) &&
			this.dscanspeedratio.equals(other.dscanspeedratio) &&
			this.dexposuretimeinsec.equals(other.dexposuretimeinsec);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}	
}

class DoubleLimit {
	double min, max;
	String name;

	DoubleLimit(String name, XMLConfiguration config) {
		this.name = name;
		this.min = config == null ? 0. : config.getDouble("min_" + name, 0.);
		this.max = config == null ? 0. : config.getDouble("max_" + name, 0.);
	}
	void addToConfig(XMLConfiguration config){
		config.addProperty("min_" + name, min);
		config.addProperty("max_" + name, max);
	}
	void checkValidity(double val) throws CrysalisValidityException {
		if (val < min)
			throw new CrysalisValidityException(name + "(" + val + ")" + " is less than minimum value (" + min  +")");
		if (val > max)
			throw new CrysalisValidityException(name + "(" + val + ")" + " is greater than maximum value (" + max +")");
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof DoubleLimit)) {
			return false;
		}

		DoubleLimit other = (DoubleLimit) o;
		return 
			this.name.equals(other.name) &&
			this.min ==other.min &&
			this.max == other.max;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}	
	
}