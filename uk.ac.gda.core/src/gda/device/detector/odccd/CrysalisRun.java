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

/**
 * Holds the parameters for a given Crysalis data collection
 */
public class CrysalisRun {
	String name = "";
	double domegaindeg, ddetectorindeg, dkappaindeg, dphiindeg, dscanstartindeg, dscanendindeg, dscanwidthindeg,
			dscanspeedratio, dexposuretimeinsec;
	int inum, irunscantype, dwnumofframes, dwnumofframesdone;

	/**
	 * Performs a deep copy of the given CrysalisRun
	 * 
	 * @param run
	 * @return CrysalisRun
	 */
	public static CrysalisRun newInstance(CrysalisRun run) {
		CrysalisRun copy = new CrysalisRun();
		copy.name = run.name;
		copy.ddetectorindeg = run.ddetectorindeg;
		copy.dexposuretimeinsec = run.dexposuretimeinsec;
		copy.domegaindeg = run.domegaindeg;
		copy.dkappaindeg = run.dkappaindeg;
		copy.dphiindeg = run.dphiindeg;
		copy.dscanstartindeg = run.dscanstartindeg;
		copy.dscanendindeg = run.dscanendindeg;
		copy.dscanwidthindeg = run.dscanwidthindeg;
		copy.dscanspeedratio = run.dscanspeedratio;
		copy.inum = run.inum;
		copy.irunscantype = run.irunscantype;
		copy.dwnumofframes = run.dwnumofframes;
		copy.dwnumofframesdone = run.dwnumofframesdone;
		return copy;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the domegaindeg
	 */
	public double getDomegaindeg() {
		return domegaindeg;
	}

	/**
	 * @param domegaindeg
	 *            the domegaindeg to set
	 */
	public void setDomegaindeg(double domegaindeg) {
		this.domegaindeg = domegaindeg;
	}

	/**
	 * @return the ddetectorindeg
	 */
	public double getDdetectorindeg() {
		return ddetectorindeg;
	}

	/**
	 * @param ddetectorindeg
	 *            the ddetectorindeg to set
	 */
	public void setDdetectorindeg(double ddetectorindeg) {
		this.ddetectorindeg = ddetectorindeg;
	}

	/**
	 * @return the dkappaindeg
	 */
	public double getDkappaindeg() {
		return dkappaindeg;
	}

	/**
	 * @param dkappaindeg
	 *            the dkappaindeg to set
	 */
	public void setDkappaindeg(double dkappaindeg) {
		this.dkappaindeg = dkappaindeg;
	}

	/**
	 * @return the dphiindeg
	 */
	public double getDphiindeg() {
		return dphiindeg;
	}

	/**
	 * @param dphiindeg
	 *            the dphiindeg to set
	 */
	public void setDphiindeg(double dphiindeg) {
		this.dphiindeg = dphiindeg;
	}

	/**
	 * @return the dscanstartindeg
	 */
	public double getDscanstartindeg() {
		return dscanstartindeg;
	}

	/**
	 * @param dscanstartindeg
	 *            the dscanstartindeg to set
	 */
	public void setDscanstartindeg(double dscanstartindeg) {
		this.dscanstartindeg = dscanstartindeg;
	}

	/**
	 * @return the dscanendindeg
	 */
	public double getDscanendindeg() {
		return dscanendindeg;
	}

	/**
	 * @param dscanendindeg
	 *            the dscanendindeg to set
	 */
	public void setDscanendindeg(double dscanendindeg) {
		this.dscanendindeg = dscanendindeg;
	}

	/**
	 * @return the dscanwidthindeg
	 */
	public double getDscanwidthindeg() {
		return dscanwidthindeg;
	}

	/**
	 * @param dscanwidthindeg
	 *            the dscanwidthindeg to set
	 */
	public void setDscanwidthindeg(double dscanwidthindeg) {
		this.dscanwidthindeg = dscanwidthindeg;
	}

	/**
	 * @return the dscanspeedratio
	 */
	public double getDscanspeedratio() {
		return dscanspeedratio;
	}

	/**
	 * @param dscanspeedratio
	 *            the dscanspeedratio to set
	 */
	public void setDscanspeedratio(double dscanspeedratio) {
		this.dscanspeedratio = dscanspeedratio;
	}

	/**
	 * @return the dexposuretimeinsec
	 */
	public double getDexposuretimeinsec() {
		return dexposuretimeinsec;
	}

	/**
	 * @param dexposuretimeinsec
	 *            the dexposuretimeinsec to set
	 */
	public void setDexposuretimeinsec(double dexposuretimeinsec) {
		this.dexposuretimeinsec = dexposuretimeinsec;
	}

	/**
	 * @return the inum
	 */
	public int getInum() {
		return inum;
	}

	/**
	 * @param inum
	 *            the inum to set
	 */
	public void setInum(int inum) {
		this.inum = inum;
	}

	/**
	 * @return the irunscantype
	 */
	public int getIrunscantype() {
		return irunscantype;
	}

	/**
	 * @param irunscantype
	 *            the irunscantype to set
	 */
	public void setIrunscantype(int irunscantype) {
		this.irunscantype = irunscantype;
	}

	/**
	 * @return the dwnumofframes
	 */
	public int getDwnumofframes() {
		return dwnumofframes;
	}

	/**
	 * @param dwnumofframes
	 *            the dwnumofframes to set
	 */
	public void setDwnumofframes(int dwnumofframes) {
		this.dwnumofframes = dwnumofframes;
	}

	/**
	 * @return the dwnumofframesdone
	 */
	public int getDwnumofframesdone() {
		return dwnumofframesdone;
	}

	/**
	 * @param dwnumofframesdone
	 *            the dwnumofframesdone to set
	 */
	public void setDwnumofframesdone(int dwnumofframesdone) {
		this.dwnumofframesdone = dwnumofframesdone;
	}

	/**
	 * @return dscanstartindeg
	 */
	public double getStart() {
		return dscanstartindeg;
	}

	/**
	 * @return dscanendindeg
	 */
	public double getEnd() {
		return dscanendindeg;
	}

	/**
	 * @return dwnumofframes
	 */
	public double getNumberSteps() {
		return dwnumofframes;
	}

	/**
	 * @return dphiindeg
	 */
	public double getPhiInDeg() {
		return dphiindeg;
	}

	/**
	 * @return dkappaindeg
	 */
	public double getKappaInDeg() {
		return dkappaindeg;
	}

	/**
	 * @return domegaindeg
	 */
	public double getOmegaInDeg() {
		return domegaindeg;
	}

	/**
	 * @return dexposuretimeinsec
	 */
	public double getExposureTime() {
		return dexposuretimeinsec;
	}

	/**
	 * Returns the step size based on start, end and number of steps.
	 * 
	 * @return double
	 */
	public double getStepSize() {
		return (getEnd() - getStart()) / getNumberSteps();
	}

	/**
	 * SCANTYPE as definable in Crysalis
	 */
	public enum SCANTYPE {
		/**
		 * Scan neither PHI nor OMEGA
		 */
		UNKNOWN, 
		/**
		 * Scan in oemga
		 */
		OMEGA, 
		/**
		 * Scan in phi
		 */
		PHI
	}

	/**
	 * Returns the type of scan defined by this CrysalisRun
	 * 
	 * @return SCANTYPE
	 */
	public SCANTYPE scanType() {
		switch (irunscantype) {
		case 0:
			return SCANTYPE.OMEGA;
		case 4:
			return SCANTYPE.PHI;
		}
		return SCANTYPE.UNKNOWN;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CrysalisRun)) {
			return false;
		}

		CrysalisRun other = (CrysalisRun) o;
		return this.name.equals(other.name) && this.ddetectorindeg == other.ddetectorindeg
				&& this.dkappaindeg == other.dkappaindeg && this.domegaindeg == other.domegaindeg
				&& this.dphiindeg == other.dphiindeg && this.dscanstartindeg == other.dscanstartindeg
				&& this.dscanendindeg == other.dscanendindeg && this.dscanwidthindeg == other.dscanwidthindeg
				&& this.dexposuretimeinsec == other.dexposuretimeinsec && this.dwnumofframes == other.dwnumofframes
				&& this.dwnumofframesdone == other.dwnumofframesdone && this.inum == other.inum
				&& this.irunscantype == other.irunscantype;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Run - " + name + "\n"
				+ "ddetectorindeg = %f\ndexposuretimeinsec = %f\ndkappaindeg = %f\n"
				+ "domegaindeg = %f\ndphiindeg = %f\ndscanendindeg = %f\n"
				+ "dscanspeedratio = %f\ndscanstartindeg = %f\ndscanwidthindeg = %f\n"
				+ "dwnumofframes = %d\ndwnumofframesdone = %d\n" + "inum = %d\nirunscantype = %d", ddetectorindeg,
				dexposuretimeinsec, dkappaindeg, domegaindeg, dphiindeg, dscanendindeg, dscanspeedratio,
				dscanstartindeg, dscanwidthindeg, dwnumofframes, dwnumofframesdone, inum, irunscantype);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
