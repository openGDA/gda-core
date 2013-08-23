/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Class used to adapter an NXPlugin whose callReadBeforeNextExposure returns true
 * to one that return false.
 */

public class NonStreamingNXPluginAdapter implements NXPlugin{

	NXPlugin nonStreamingDelegate;

	public NonStreamingNXPluginAdapter(NXPlugin nonStreamingDelegate) {
		super();
		this.nonStreamingDelegate = nonStreamingDelegate;
	}
	
	@Override
	public String getName() {
		return nonStreamingDelegate.getName();
	}

	@Override
	public boolean willRequireCallbacks() {
		return nonStreamingDelegate.willRequireCallbacks();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		nonStreamingDelegate.prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	@Override
	public void prepareForLine() throws Exception {
		nonStreamingDelegate.prepareForLine();
	}

	@Override
	public void completeLine() throws Exception {
		nonStreamingDelegate.completeLine();
	}

	@Override
	public void completeCollection() throws Exception {
		nonStreamingDelegate.completeCollection();
	}

	@Override
	public void atCommandFailure() throws Exception {
		nonStreamingDelegate.atCommandFailure();
	}

	@Override
	public void stop() throws Exception {
		nonStreamingDelegate.stop();
	}

	@Override
	public List<String> getInputStreamNames() {
		return nonStreamingDelegate.getInputStreamNames();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return nonStreamingDelegate.getInputStreamFormats();
	}

	Vector<NXDetectorDataAppender> cache = new Vector<NXDetectorDataAppender> ();
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		Vector<NXDetectorDataAppender> items = new Vector<NXDetectorDataAppender>();
		for( int i = 0; i< maxToRead && cache.size()>0; i++){
			items.add( cache.remove(0));
		}
		return items;
	}

	@Override
	public boolean callReadBeforeNextExposure() {
		return false;
	}

	public void addToCache() throws NoSuchElementException, InterruptedException, DeviceException {
		cache.add(nonStreamingDelegate.read(1).get(0));
	}
	
	
}
