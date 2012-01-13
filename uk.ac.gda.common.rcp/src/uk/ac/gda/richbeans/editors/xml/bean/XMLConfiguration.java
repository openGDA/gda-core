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

package uk.ac.gda.richbeans.editors.xml.bean;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.schema.SchemaReader;

import com.swtdesigner.SWTResourceManager;

/**
 * @author fcp94556
 *
 */
public class XMLConfiguration extends SourceViewerConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(XMLConfiguration.class);
	
	private XMLDoubleClickStrategy doubleClickStrategy;
	private XMLTagScanner  tagScanner;
	private XMLScanner     scanner;
	private ColorManager   colorManager;
	private SchemaReader   schemaReader;

	/**
	 * @param colorManager
	 * @param schemaUrl 
	 * @throws Exception 
	 */
	public XMLConfiguration(ColorManager colorManager, final URL schemaUrl) throws Exception{
		this.colorManager = colorManager;
		try {
		    if (schemaUrl!=null) this.schemaReader = new SchemaReader(schemaUrl);
		} catch (NullPointerException ne) {
			logger.error("Cannot create Schema reader for "+schemaUrl);
		}
	}
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			XMLPartitionScanner.XML_COMMENT,
			XMLPartitionScanner.XML_TAG };
	}
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new XMLDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected XMLScanner getXMLScanner() {
		if (scanner == null) {
			scanner = new XMLScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.DEFAULT))));
		}
		return scanner;
	}
	protected XMLTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new XMLTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.TAG))));
		}
		return tagScanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, XMLPartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_TAG);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(IXMLColorConstants.XML_COMMENT)));
	
		reconciler.setDamager(ndr, XMLPartitionScanner.XML_COMMENT);
		reconciler.setRepairer(ndr, XMLPartitionScanner.XML_COMMENT);
		

		return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.setContentAssistProcessor(new XMLCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new XMLCompletionProcessor(), XMLPartitionScanner.XML_TAG);
		assistant.setContentAssistProcessor(new XMLCompletionProcessor(), XMLPartitionScanner.XML_COMMENT);
		
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(1);
		//assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		//assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

		return assistant;
	}
	

	private static Pattern END     = Pattern.compile("^.*\\<\\/(.+)\\>.*$");
	private static Pattern SIBLING = Pattern.compile("^.*\\<(.+)\\>.*$");

	/**
	 * @param viewer
	 * @param offset
	 * @return par tag for position
	 */
	@SuppressWarnings("unchecked")
	public List<String> getParents(final ITextViewer viewer, final int offset) {
		try {
			final ITypedRegion type = viewer.getDocument().getPartition(offset);
			final String       seg  = viewer.getDocument().get(type.getOffset(), type.getLength());
			
			final String clean = seg.trim().replace("\n", "");
			Matcher matcher = END.matcher(clean);
			if (matcher.matches()) {
				return Arrays.asList(new String[]{matcher.group(1)});
			} 
			
			matcher = SIBLING.matcher(clean);

			if (matcher.matches()) {
				final String sibling = matcher.group(1);
				return schemaReader!=null ? schemaReader.getParents(sibling) : Collections.EMPTY_LIST;
			}

				
		} catch (Exception e) {
			logger.debug("Problem determining tag at position.", e);
			return null; // We get no suggestions, the developer can then fix them if this is occurring.
		}
		
		return null;
	}
	
	private void filterCurrentlyTyped( final ITextViewer  viewer,
									   final int          offset, 
									   final List<String> allTags) {
		
		try {
			final ITypedRegion type = viewer.getDocument().getPartition(offset);
			final String       seg  = viewer.getDocument().get(type.getOffset(), type.getLength());
			
			final String frag = seg.substring(seg.indexOf("<")+1, seg.indexOf('\n')).trim();
			
			if (frag!=null&&!"".equals(frag)) {
	 			for (Iterator<String> it = allTags.iterator(); it.hasNext();) {
					String name = it.next();
					if (!name.startsWith(frag)) it.remove();
				}
			}
		} catch (Exception e) {
			logger.debug("Problem filtering tags.", e);
		}

	}

	private int getNumberCharactersTyped(final ITextViewer viewer, final int offset) {
		try {
			String c    = viewer.getDocument().get(offset, 1);
			
			int chars = 0;
			while(!"<".equals(c)) {
				++chars;
				c = viewer.getDocument().get(offset-chars, 1);
			}
			return chars-1;
			
		} catch (Exception e) {
			logger.debug("Problem filtering tags.", e);
			return 0;
		}
	}

	/**
	 * @author fcp94556
	 *
	 */
	public class XMLCompletionProcessor implements IContentAssistProcessor {

		private final Image tagImage = SWTResourceManager.getImage(XMLConfiguration.class, "/icons/tag.png");
		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
			
			final List<String> parents = getParents(viewer, offset);
			
			// Currently just add all possible tags
			List<String> allTags = new ArrayList<String>(31);
			if (schemaReader!=null) try {
				for (String par : parents) {
					allTags.addAll(schemaReader.getChildTags(par));
				}
			} catch (Exception e) {
				return null;
			}
			
			filterCurrentlyTyped(viewer, offset, allTags);
			
			final int pos = getNumberCharactersTyped(viewer, offset);
			
			final ICompletionProposal[] props = new ICompletionProposal[allTags.size()];
			int iTag = 0;
			for (String tag : allTags) {
				final CompletionProposal prop = new CompletionProposal(tag.substring(pos)+"></"+tag+">", offset, 0, tag.length()+1-pos, tagImage, tag+"></"+tag+">", null, null);
				props[iTag] = prop;
				++iTag;
			}
			return props;
		}


		@Override
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public char[] getCompletionProposalAutoActivationCharacters() {
			return CHARS;
		}

		private final char [] CHARS = new char[]{'<'};
		@Override
		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		@Override
		public IContextInformationValidator getContextInformationValidator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getErrorMessage() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}