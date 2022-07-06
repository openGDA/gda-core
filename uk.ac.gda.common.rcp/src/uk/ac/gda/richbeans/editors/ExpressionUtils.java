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

package uk.ac.gda.richbeans.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.nfunk.jep.ASTConstant;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.FunctionTable;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.Operator;
import org.nfunk.jep.OperatorSet;

/**
 * A lot of caching goes on here.
 */
public class ExpressionUtils {

	private static JEP PARSER;
	
	private static JEP getParser() {
	    if (PARSER==null) {
	    	PARSER = new JEP();
	    	PARSER.addStandardFunctions();
	    	PARSER.addStandardConstants();
	    	PARSER.setAllowUndeclared(true);
	    }
        return PARSER;
	}
	
	private static List<String> OPERATORS;

	public static final List<String> getOperators() {
		if (OPERATORS==null) {
			List<String> tmp = new ArrayList<String>(31);
			final JEP jepParser = getParser();
			final OperatorSet ops = jepParser.getOperatorSet();
			for (Operator operator : ops.getOperators()) {
				tmp.add(operator.getSymbol());
			}
			OPERATORS = Collections.unmodifiableList(tmp);
		}
		return OPERATORS;
	}
	
	public static boolean isOperator(final String posOp) {
		return getOperators().contains(posOp);
	}
	
	private static List<String> CONSTANTS;

	public static final List<String> getConstants() {
		if (CONSTANTS==null) {
			List<String> tmp = new ArrayList<String>(31);
			final JEP jepParser = getParser();
			for (Object var : jepParser.getSymbolTable().keySet()) {
				tmp.add(var.toString());
	 		}
			CONSTANTS = Collections.unmodifiableList(tmp);
		}
		return CONSTANTS;
	}
	public static boolean isConstant(final String posConst) {
		return getConstants().contains(posConst);
	}

	/**
	 * Attempts to parse contents as expression. If empty string
	 * is returned there is no last term and we are ready for a new term.
	 */
	public static String getLastTerm(String contents) {
		
		if (contents==null||"".equals(contents)) return "";
		
		// If ends with operator, we have no term
		for (String op : getOperators()) {
			if (contents.endsWith(op)) return "";
		}
		if (contents.endsWith("(")) return "";
		
		try {
			final JEP parser = getParser();
			final Node node  = parser.parseExpression(contents);
			final List<Node> last = new ArrayList<Node>(1);
			getLastNode(node, last);
			
			final Node lastNode = last.get(0);
			if (lastNode instanceof ASTVarNode) {
				final String tempName =  ((ASTVarNode)lastNode).getName();
				return tempName;
			} else if (lastNode instanceof ASTConstant) {
				final String tempName =  ((ASTConstant)lastNode).getValue().toString();
				return tempName;
			}
			
		} catch (Exception ne) {
			return "";
		}
		
		return "";
	}

	
	private static void getLastNode(final Node node, final List<Node> last) {
		if (node.jjtGetNumChildren()<1) {
			last.clear();
			last.add(node);
			return;
		} 
		for (int i = 0; i<node.jjtGetNumChildren(); ++i) {
			final Node c = node.jjtGetChild(i);
			getLastNode(c, last);
		}
	}

	private static List<String> FUNCTIONS;
	private static List<String> FUNCTIONS_BRACKETS;
	
	public static final List<String> getFunctions() {
		if (FUNCTIONS==null) {
			List<String> tmp1 = new ArrayList<String>(31);
			List<String> tmp2 = new ArrayList<String>(31);
			final JEP jepParser = getParser();
			final FunctionTable table = jepParser.getFunctionTable();
			for (Object function : table.keySet()) {
				tmp1.add(function.toString());
				tmp2.add(function.toString()+"(");
			}
			FUNCTIONS          = Collections.unmodifiableList(tmp1);
			FUNCTIONS_BRACKETS = Collections.unmodifiableList(tmp2);
		}
		return FUNCTIONS;
	}

	public static Collection<? extends String> getFunctionsWithOpeningBrackets() {
		getFunctions();
		return FUNCTIONS_BRACKETS;
	}

}
