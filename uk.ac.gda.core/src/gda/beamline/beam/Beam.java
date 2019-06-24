/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.beamline.beam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.beamline.BeamInfo;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class describes the photon beam properties of a Beamline or Station, including
 * <ul>
 * <li>wavelength (</li>
 * <li>energy</li>
 * </ul>
 *
 */
@ServiceInterface(BeamInfo.class)
public class Beam extends FindableConfigurableBase implements BeamInfo {
	private static final Logger logger = LoggerFactory.getLogger(Beam.class);
	private static final String NAME = "beam";
	private double energy = Double.NaN;
	private double wavelength = Double.NaN;

	private Metadata metadata;
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean configureAtStartup = false;
	private boolean calibrated = false;

	/**
	 * default constructor
	 */
	public Beam() {
		setName(NAME);
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			metadata = GDAMetadataProvider.getInstance();
			setConfigured(true);
		}

	}

	/**
	 * sets beam energy in keV.
	 */
	@Override
	public void setEnergy(double energy) {
		this.energy = energy;
		this.wavelength = 12.39842 / energy;
		setCalibrated(true);
		notifyIObservers(this, this.wavelength);
		try {
			MetadataEntry energydata = new StoredMetadataEntry("energy", String.valueOf(energy));
			metadata.addMetadataEntry(energydata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set energy value to metadata list");
		}
		try {
			MetadataEntry wavelengthdata = new StoredMetadataEntry("wavelength", String.valueOf(wavelength));
			metadata.addMetadataEntry(wavelengthdata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set wavelength value to metadata list");
		}
	}

	@Override
	public double getEnergy() {
		return this.energy;
	}
	/**
	 * set beam wavelength in Angstrom.
	 */
	@Override
	public void setWavelength(double wavelength) {
		this.wavelength = wavelength;
		this.energy = 12.39842 / wavelength;
		setCalibrated(true);
		notifyIObservers(this, this.wavelength);
		try {
			MetadataEntry energydata = new StoredMetadataEntry("energy", String.valueOf(energy));
			metadata.addMetadataEntry(energydata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set energy value to metadata list");
		}
		try {
			MetadataEntry wavelengthdata = new StoredMetadataEntry("wavelength", String.valueOf(wavelength));
			metadata.addMetadataEntry(wavelengthdata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set wavelength value to metadata list");
		}
	}
	/**
	 * sets the beam energy using Mono's enegy setting.
	 */
	@Override
	public void setEnergy() throws DeviceException {
		Finder finder = Finder.getInstance();
		ScannableGroup sdcm = (ScannableGroup)finder.find("DCM");
		ScannableMotor energy1=(ScannableMotor)sdcm.getGroupMember("energy");
		if (energy1.getHardwareUnitString().isEmpty() || energy1.getHardwareUnitString()== null) {
			logger.warn("{} has no unit being set. Treat the value as in 'keV' here.", energy1.getName());
		} else {
			// photon energy here must be in keV
			energy1.setUserUnits("keV");
		}
		this.energy = PositionConvertorFunctions.toDouble(energy1.getPosition());
		this.wavelength = 12.39842 / this.energy;
		setCalibrated(false);
		notifyIObservers(this, this.wavelength);

		try {
			MetadataEntry energydata = new StoredMetadataEntry("energy", String.valueOf(energy));
			metadata.addMetadataEntry(energydata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set energy value to metadata list");
		}
		try {
			MetadataEntry wavelengthdata = new StoredMetadataEntry("wavelength", String.valueOf(wavelength));
			metadata.addMetadataEntry(wavelengthdata);
		} catch (DeviceException e) {
			logger.warn("Can not add or set wavelength value to metadata list");
		}
	}

	@Override
	public double getWavelength() {
		return this.wavelength;
	}

	/**
	 * Check whether the configure method should be called when the server is instantiated.
	 *
	 * @return true if configuration is required at startup.
	 */
	@Override
	public boolean isConfigureAtStartup() {
		return configureAtStartup;
	}

	/**
	 * Set a flag to inform the server whether the configure method should be called at startup.
	 *
	 * @param configureAtStartup
	 *            true to configure at startup.
	 */
	public void setConfigureAtStartup(boolean configureAtStartup) {
		this.configureAtStartup = configureAtStartup;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	private void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	@Override
	public boolean isCalibrated() {
		return calibrated;
	}

	/**
	 * @param calibrated
	 */
	public void setCalibrated(boolean calibrated) {
		this.calibrated = calibrated;
	}
}
