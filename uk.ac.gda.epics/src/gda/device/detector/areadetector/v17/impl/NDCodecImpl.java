/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import static gda.epics.LazyPVFactory.newEnumPV;
import static gda.epics.LazyPVFactory.newIntegerPV;
import static gda.epics.LazyPVFactory.newReadOnlyDoublePV;
import static gda.epics.LazyPVFactory.newReadOnlyEnumPV;
import static gda.epics.LazyPVFactory.newReadOnlyIntegerPV;
import static gda.epics.LazyPVFactory.newStringPV;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDCodec;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;

public class NDCodecImpl extends NDBaseImpl implements NDCodec, InitializingBean, Configurable {

	private String basePv;

	private PV<Mode> modePV;
	private PV<Compressor> compressorPV;
	private ReadOnlyPV<Double> compressionFactorPV;
	private PV<Integer> jpegQualityPV;
	private PV<BloscMode> bloscModePV;
	private PV<Integer> bloscLevelPV;
	private PV<BloscShuffle> bloscShuttlePV;
	private PV<Integer> bloscThreadsPV;
	private ReadOnlyPV<Status> statusPV;
	private PV<String> errorPV;

	@Override
	public void afterPropertiesSet() {
		if (basePv == null) {
			throw new IllegalStateException("basePv has not been configured");
		}
		if (getPluginBase() == null) {
			var base = new NDPluginBaseImpl();
			base.setBasePVName(basePv + ":");
			setPluginBase(base);
		}
	}

	@Override
	public void configure() {
		modePV = newEnumPV(basePv + ":MODE", Mode.class);
		compressorPV = newEnumPV(basePv + ":Compressor", Compressor.class);
		compressionFactorPV = newReadOnlyDoublePV(basePv + ":CompFactor_RBV");
		jpegQualityPV = new PVWithSeparateReadback<>(
				newIntegerPV(basePv + ":JPEGQuality"),
				newReadOnlyIntegerPV(basePv + ":JPEGQuality_RBV"));
		bloscModePV = newEnumPV(basePv + ":BloscCompressor", BloscMode.class);
		bloscLevelPV = newIntegerPV(basePv + ":BloscCLevel");
		bloscShuttlePV = newEnumPV(basePv + ":BloscShuffle", BloscShuffle.class);
		bloscThreadsPV = new PVWithSeparateReadback<>(
				newIntegerPV(basePv + ":BloscNumThreads"),
				newReadOnlyIntegerPV(basePv + ":BloscNumThreads_RBV"));
		statusPV = newReadOnlyEnumPV(basePv + ":CodecStatus", Status.class);
		errorPV = newStringPV(basePv + ":CodecError");
	}

	@Override
	public Mode getMode() throws IOException {
		return modePV.get();
	}

	@Override
	public void setMode(Mode mode) throws IOException {
		modePV.putWait(mode);
	}

	@Override
	public Compressor getCompressor() throws IOException {
		return compressorPV.get();
	}

	@Override
	public void setCompressor(Compressor comp) throws IOException {
		compressorPV.putWait(comp);
	}

	@Override
	public double getCompressionFactor() throws IOException {
		return compressionFactorPV.get();
	}

	@Override
	public int getJpegQuality() throws IOException {
		return jpegQualityPV.get();
	}

	@Override
	public void setJpegQuality(int quality) throws IOException {
		jpegQualityPV.putWait(quality);
	}

	@Override
	public BloscMode getBloscMode() throws IOException {
		return bloscModePV.get();
	}

	@Override
	public void setBloscMode(BloscMode mode) throws IOException {
		bloscModePV.putWait(mode);
	}

	@Override
	public int getBloscLevel() throws IOException {
		return bloscLevelPV.get();
	}

	@Override
	public void setBloscLevel(int level) throws IOException {
		bloscLevelPV.putWait(level);
	}

	@Override
	public BloscShuffle getBloscShuttle() throws IOException {
		return bloscShuttlePV.get();
	}

	@Override
	public void setBloscShuttle(BloscShuffle mode) throws IOException {
		bloscShuttlePV.putWait(mode);
	}

	@Override
	public int getBloscThreads() throws IOException {
		return bloscThreadsPV.get();
	}

	@Override
	public void setBloscThreads(int threads) throws IOException {
		bloscThreadsPV.putWait(threads);
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

	@Override
	public Status getStatus() throws IOException {
		return statusPV.get();
	}

	@Override
	public String getError() throws IOException {
		return errorPV.get();
	}

	@Override
	public boolean isConfigured() {
		return bloscLevelPV != null;
	}

	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

}
