/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.parser.bdpl.ast;

import java.util.List;

/**
 * Abstract supertype of {@link ASTQueryContainer} and
 * {@link ASTUpdateContainer}
 * 
 * @author Jeen Broekstra, Ningyuan Pan
 */
public abstract class ASTOperationContainer extends SimpleNode {

	/**
	 * @param id
	 */
	public ASTOperationContainer(int id) {
		super(id);
	}

	public ASTOperationContainer(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}
	
	// Ninyuan changed
	public ASTBaseDecl getBaseDecl() {
		//return super.jjtGetChild(ASTBaseDecl.class);
		return (super.jjtGetChild(ASTProlog.class)).jjtGetChild(ASTBaseDecl.class);
	}
	
	public ASTOperation getOperation() {
		return super.jjtGetChild(ASTOperation.class);
	}
	
	// Ninyuan changed
	public List<ASTPrefixDecl> getPrefixDeclList() {
		//return super.jjtGetChildren(ASTPrefixDecl.class);
		return (super.jjtGetChild(ASTProlog.class)).jjtGetChildren(ASTPrefixDecl.class);
	}
	
	public abstract void setSourceString(String source);
	
	public abstract String getSourceString();

}
