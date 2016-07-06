/*
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.swtdesigner.SWTResourceManager;

public class DiffractionNumericalSpotExaminer extends Composite {

//	private static final Logger logger = LoggerFactory.getLogger(DiffractionNumericalSpotExaminer.class);
	
	private static final double BACKGROUND_NOISE = 50;
	private String htmlTable = "<html><body></html></body>";
	public int MAX_NUMERICAL_SIZE = 100;
	private Browser browser;

	public DiffractionNumericalSpotExaminer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());

		Composite composite = new Composite(this, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(100);
		fd_composite.right = new FormAttachment(100);
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		try {
			browser = new Browser(composite, SWT.NONE);
			browser.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			browser.setText(htmlTable);
		} catch (Throwable ne) {// Sometimes browser throws exception
			browser = null;
			//logger.error("Cannot create browser for differaction viewer.", ne);
		}

	}

	public void populateTable(IDataset data, RectangularROI rectROI) {
		if (rectROI.getLengths()[0] <= 1 || rectROI.getLengths()[1] <= 1 || data.getSize() <= 1)
			return;
		int[] startPoint = rectROI.getIntPoint();
		int[] stopPoint = rectROI.getIntPoint(1, 1);
		IDataset ROIdata = data.getSlice(new int[] { startPoint[1], startPoint[0] }, new int[] { stopPoint[1],
				stopPoint[0] }, new int[] { 1, 1 });
		double min = ROIdata.min().doubleValue();
		double roiDataRange = ROIdata.max().doubleValue() - min;
		htmlTable = "<html><body><table style=\"{align:right;font-size:10px}\">";
		int[] roiSize = ROIdata.getShape();

		if (roiSize[0] > MAX_NUMERICAL_SIZE || roiSize[1] > MAX_NUMERICAL_SIZE) {
			htmlTable = "<html><body><p>Cannot plot numerical tabe of this size <br> Maximum Table size is "
					+ MAX_NUMERICAL_SIZE + " X " + MAX_NUMERICAL_SIZE + "</p></html></body>";
		} else {
			for (int i = 0; i < roiSize[0]; i++) { // horizontal first
				htmlTable += "<tr align=\"center\">";
				for (int j = 0; j < roiSize[1]; j++) {
					int pixVal;
					try {
						pixVal = ROIdata.getInt(i, j);
					} catch (IndexOutOfBoundsException e) {
						System.out.println("fast is " + j + " and slow is " + i);
						throw new IndexOutOfBoundsException();

					}
					if (roiDataRange > BACKGROUND_NOISE) {
						if (pixVal > (min + (roiDataRange * 0.50)))
							htmlTable += "<td style=\"{color:red}\">" + pixVal + "</td>";
						else if (pixVal > (min + (roiDataRange * 0.20)))
							htmlTable += "<td style=\"{color:blue}\">" + pixVal + "</td>";
						else if (pixVal > (min + (roiDataRange * 0.05)))
							htmlTable += "<td style=\"{color:green}\">" + pixVal + "</td>";
						else
							htmlTable += "<td>" + pixVal + "</td>";
					} else
						htmlTable += "<td>" + pixVal + "</td>";

				}
				htmlTable += "</tr>";
			}
			htmlTable += "</table></html></body>";
		}
		
		if (browser!=null) {
			browser.getDisplay().asyncExec(new Runnable() {
	
				@Override
				public void run() {
					browser.setText(htmlTable);
				}
			});
		}

	}
}
