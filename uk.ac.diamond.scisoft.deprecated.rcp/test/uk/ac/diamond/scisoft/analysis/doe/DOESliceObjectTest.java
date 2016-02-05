/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.doe;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DimsData;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DimsDataList;
import uk.ac.gda.doe.DOEUtils;

/**
 * Unit test for DOE algorithm which is recursive and expands out
 * simulation sets using weightings defined in annotations.
 * 
 * If you have a case that does not expand correctly, add it here.
 * Probably a fix to DOEUtils.readAnnoations(...) will be what you
 * then need. 
 */
public class DOESliceObjectTest {
	
	@Test
	public void testSliceData() throws Throwable {
		
		final DimsData range = new DimsData(2);
		range.setSliceRange("100;200;1");
		
		final List<? extends Object> expanded = DOEUtils.expand(range);
		
		Assert.assertTrue(expanded.size()==101);
		
	}

	@Test
	public void testSliceDataHolder() throws Throwable {
		
		final DimsDataList holder = new DimsDataList();
		holder.add(new DimsData(0));
		holder.add(new DimsData(1));
		
		final DimsData range = new DimsData(2);
		range.setSliceRange("100;200;1");
		holder.add(range);
		
		final List<? extends Object> expanded = DOEUtils.expand(holder);
		
		Assert.assertTrue(expanded.size()==101);
		
	}

}
