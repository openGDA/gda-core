package uk.ac.gda.server.exafs.scan.preparers;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import uk.ac.gda.server.exafs.scan.OutputPreparer;

public class B18OutputPreparer extends OutputPreparerBase implements OutputPreparer {

	public B18OutputPreparer(AsciiDataWriterConfiguration datawriterconfig, NXMetaDataProvider metashop) {
		super(datawriterconfig, metashop);
	}


}