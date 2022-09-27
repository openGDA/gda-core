/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.hrpd.sample;

import java.util.Optional;

import gda.hrpd.sample.api.ResultMetadata;
import gda.hrpd.sample.api.SampleMetadata;
import gda.hrpd.sample.api.ScanDescription;

public class SampleMetadataBean implements SampleMetadata {
	public interface Info {
		int getCarouselPosition();
		String getName();
		String getComment();
		String getTitle();
		String getDirectory();
		String getVisit();
		ScanDescription getScan();
	}
	private ResultMetadata results;
	private final int position;
	private final String name;
	private final String comment;
	private final String title;
	private final String directory;
	private final String visit;
	private final ScanDescription scan;

	protected SampleMetadataBean(Info builder) {
		position = builder.getCarouselPosition();
		name = builder.getName();
		comment = builder.getComment();
		title = builder.getTitle();
		directory = builder.getDirectory();
		visit = builder.getVisit();
		scan = builder.getScan();
	}

	@Override
	public void setResults(ResultMetadata results) {
		this.results = results;
	}
	@Override
	public Optional<ResultMetadata> getResults() {
		return Optional.ofNullable(results);
	}
	@Override
	public int getCarouselPosition() {
		return position;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getComment() {
		return comment;
	}
	@Override
	public String getTitle() {
		return title;
	}
	@Override
	public String getDirectory() {
		return directory;
	}
	@Override
	public String getVisit() {
		return visit;
	}
	@Override
	public ScanDescription getScan() {
		return scan;
	}

	@Override
	public String toString() {
		return "SampleMetadataBean [position=" + position + ", name=" + name + "]";
	}
}
