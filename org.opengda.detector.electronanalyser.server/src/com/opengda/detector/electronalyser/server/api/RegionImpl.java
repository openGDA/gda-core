package com.opengda.detector.electronalyser.server.api;

import org.opengda.detector.electroanalyser.api.Region;

import com.opengda.detector.electronalyser.server.model.regiondefinition.LENS_MODE;

public class RegionImpl implements Region {

	private String name;
	private LENS_MODE lensmode;

	public RegionImpl(String name, LENS_MODE lensmode) {
		this.name = name;
		this.lensmode = lensmode;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public int getLensMode() {
		return lensmode.ordinal();
	}

	@Override
	public void setLensMode(int lensMode) {
		this.lensmode = LENS_MODE.get(lensMode);
	}

}
