package org.openrdf.query.parser.bdpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.parser.bdpl.ast.ASTBasicGraphPattern;
import org.openrdf.query.parser.bdpl.ast.ASTBlankNode;
import org.openrdf.query.parser.bdpl.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.bdpl.ast.ASTCollection;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import org.openrdf.query.MalformedQueryException;


/**
 * Processes blank nodes in the query body, replacing them with variables while
 * retaining scope.
 * 
 * @author Arjohn Kampman
 */
public class BlankNodeVarProcessor extends eu.play_project.platformservices.bdpl.parser.ASTVisitorBase {

	
	public static Set<String> process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		try {
			BlankNodeToVarConverter converter = new BlankNodeToVarConverter();
			qc.jjtAccept(converter, null);
			return converter.getUsedBNodeIDs();
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e);
		}
	}

	/*-------------------------------------*
	 * Inner class BlankNodeToVarConverter *
	 *-------------------------------------*/

	private static class BlankNodeToVarConverter extends eu.play_project.platformservices.bdpl.parser.ASTVisitorBase {

		private int anonVarNo = 1;

		private Map<String, String> conversionMap = new HashMap<String, String>();

		private Set<String> usedBNodeIDs = new HashSet<String>();

		private String createAnonVarName() {
			return "-anon-" + anonVarNo++;
		}
		
		public Set<String> getUsedBNodeIDs() {
			usedBNodeIDs.addAll(conversionMap.keySet());
			return Collections.unmodifiableSet(usedBNodeIDs);
		}

		@Override
		public Object visit(ASTBasicGraphPattern node, Object data)
			throws VisitorException
		{
			// The same Blank node ID cannot be used across Graph Patterns
			usedBNodeIDs.addAll(conversionMap.keySet());

			// Blank nodes are scoped to Basic Graph Patterns
			conversionMap.clear();

			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTBlankNode node, Object data)
			throws VisitorException
		{
			String bnodeID = node.getID();
			String varName = findVarName(bnodeID);

			if (varName == null) {
				varName = createAnonVarName();

				if (bnodeID != null) {
					conversionMap.put(bnodeID, varName);
				}
			}

			ASTVar varNode = new ASTVar(SyntaxTreeBuilderTreeConstants.JJTVAR);
			varNode.setName(varName);
			varNode.setAnonymous(true);

			node.jjtReplaceWith(varNode);

			return super.visit(node, data);
		}

		private String findVarName(String bnodeID) throws VisitorException {
			if (bnodeID == null)
				return null;
			String varName = conversionMap.get(bnodeID);
			if (varName == null && usedBNodeIDs.contains(bnodeID))
				throw new VisitorException(
						"BNodeID already used in another scope: " + bnodeID);
			return varName;
		}

		@Override
		public Object visit(ASTBlankNodePropertyList node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}

		@Override
		public Object visit(ASTCollection node, Object data)
			throws VisitorException
		{
			node.setVarName(createAnonVarName());
			return super.visit(node, data);
		}
	}
}

