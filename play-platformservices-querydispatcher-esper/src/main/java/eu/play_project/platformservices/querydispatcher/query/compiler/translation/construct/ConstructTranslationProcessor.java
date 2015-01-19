/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct;


import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.parser.bdpl.ast.ASTArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstructArrayVar;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstructObjectList;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstructPropertyListPath;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLConstructTriplesSameSubjectPath;
import org.openrdf.query.parser.bdpl.ast.ASTBDPLWhereClause;
import org.openrdf.query.parser.bdpl.ast.ASTBaseDecl;
import org.openrdf.query.parser.bdpl.ast.ASTBlankNode;
import org.openrdf.query.parser.bdpl.ast.ASTBlankNodePropertyList;
import org.openrdf.query.parser.bdpl.ast.ASTCollection;
import org.openrdf.query.parser.bdpl.ast.ASTDatasetClause;
import org.openrdf.query.parser.bdpl.ast.ASTDynamicArrayDecl;
import org.openrdf.query.parser.bdpl.ast.ASTFalse;
import org.openrdf.query.parser.bdpl.ast.ASTIRI;
import org.openrdf.query.parser.bdpl.ast.ASTNumericLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTOperationContainer;
import org.openrdf.query.parser.bdpl.ast.ASTPathAlternative;
import org.openrdf.query.parser.bdpl.ast.ASTPathElt;
import org.openrdf.query.parser.bdpl.ast.ASTPathSequence;
import org.openrdf.query.parser.bdpl.ast.ASTPrefixDecl;
import org.openrdf.query.parser.bdpl.ast.ASTQName;
import org.openrdf.query.parser.bdpl.ast.ASTRDFLiteral;
import org.openrdf.query.parser.bdpl.ast.ASTTrue;
import org.openrdf.query.parser.bdpl.ast.ASTVar;
import org.openrdf.query.parser.bdpl.ast.Node;
import org.openrdf.query.parser.bdpl.ast.VisitorException;

import eu.play_project.platformservices.bdpl.parser.ASTVisitorBase;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplate;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TripleObject;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TriplePredicate;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TripleSubject;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;

/**
 * @author ningyuan 
 * 
 * Oct 16, 2014
 *
 */
public class ConstructTranslationProcessor {
	
	public static ConstructTemplate process(ASTOperationContainer qc, BDPLArrayTable arrayTable)
			throws TranslateException{
		ConstructTranslator translator = new ConstructTranslator();
		
		ConstructTranslatorData data = new ConstructTranslatorData(arrayTable);
		
		try {
			qc.jjtAccept(translator, data);
			
			ConstructTemplate ret = data.getConstructTemplate();
			/*if(ret.getRdfType() == null){
				throw new VisitorException("No rdf:type in construct clause");
			}
			else if(ret.getStreamName() == null){
				throw new VisitorException("No "+BDPLConstants.URI_STREAM+" in construct clause");
			}*/
			
			return ret;
			
		} catch (VisitorException e) {
			throw new TranslateException(e.getMessage());
		}
	}
	
	private static class ConstructTranslatorData{
		
		private BDPLArrayTable arrayTable;
		
		private TripleSubject currentSub;
		
		private ConstructTemplate constructTemplate = new ConstructTemplate();
		
		// -1: wrong subject 0: wrong predicate, 1: wrong object
		private int flagType = -1;
		
		private int flagStream = -1;
		
		private ConstructTranslatorData(BDPLArrayTable at){
			arrayTable = at;
		}
		
		private void setRDFType(String rt){
			constructTemplate.setRdfType(rt);
		}
		
		private void setStreamName(String sn){
			constructTemplate.setStreamName(sn);
		}
		
		private BDPLArrayTable getArrayTable() {
			return this.arrayTable;
		}
		
		private ConstructTemplate getConstructTemplate(){
			return constructTemplate;
		}
		
		private void addSubject(TripleSubject s){
			constructTemplate.addSubject(s);
		}
		
		private void setCurrentSubject(TripleSubject s){
			currentSub = s;
		}
		
		private TripleSubject getCurrentSubject() {
			return currentSub;
		}

	}
	
	private static class ConstructTranslator extends ASTVisitorBase {
		
		/*
		 * skipped nodes
		 */
		@Override
		public Object visit(ASTBaseDecl node, Object data)
				throws VisitorException
		{
			
			return data;
		}
		
		@Override
		public Object visit(ASTPrefixDecl node, Object data)
				throws VisitorException
		{
			
			return data;
		}
		
		@Override
		public Object visit(ASTDatasetClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTDynamicArrayDecl node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTBDPLWhereClause node, Object data)
				throws VisitorException
		{
			return data;
		}
		
		@Override
		public Object visit(ASTQName node, Object data)
			throws VisitorException
		{
			throw new VisitorException("QNames must be resolved before EPL translation.");
		}
		
		
		
		
		/*
		 * visited nodes
		 */
		
		@Override
		public Object visit(ASTBDPLConstructTriplesSameSubjectPath node, Object data)
				throws VisitorException
		{
			
			// subject
			Node snode = node.jjtGetChild(0);
			TripleSubject sub;
			
			if(snode instanceof ASTVar){
				sub = new TripleSubject(BDPLConstants.TYPE_VAR);
				sub.getContent().add(((ASTVar) snode).getName());
				((ConstructTranslatorData)data).setCurrentSubject(sub);
			}
			else if(snode instanceof ASTIRI){
				String iri = ((ASTIRI) snode).getValue();
				if(iri.equals(BDPLConstants.URI_CONSTRUCT_SUBJECT)){
					((ConstructTranslatorData)data).flagType = 0;
					((ConstructTranslatorData)data).flagStream = 0;
				}
				
				sub = new TripleSubject(BDPLConstants.TYPE_IRI);
				sub.getContent().add(iri);
				((ConstructTranslatorData)data).setCurrentSubject(sub);
			}
			else if(snode instanceof ASTRDFLiteral){
				throw new VisitorException("Subject of triple in construct clause can not be literal");
			}
			else if(snode instanceof ASTNumericLiteral){
				throw new VisitorException("Subject of triple in construct clause can not be literal");
			}
			else if(snode instanceof ASTTrue){
				throw new VisitorException("Subject of triple in construct clause can not be literal");
			}
			else if(snode instanceof ASTFalse){
				throw new VisitorException("Subject of triple in construct clause can not be literal");
			}
			else if(snode instanceof ASTBlankNode){
				sub = new TripleSubject(BDPLConstants.TYPE_BN);
				//TODO
				sub.getContent().add("bn");
				((ConstructTranslatorData)data).setCurrentSubject(sub);
			}
			else if(snode instanceof ASTCollection){
				sub = new TripleSubject(BDPLConstants.TYPE_COLLECTION);
				//TODO
				sub.getContent().add("collection");
				((ConstructTranslatorData)data).setCurrentSubject(sub);
			}
			else if(snode instanceof ASTBlankNodePropertyList){
				sub = new TripleSubject(BDPLConstants.TYPE_BNL);
				//TODO
				sub.getContent().add("bnl");
				((ConstructTranslatorData)data).setCurrentSubject(sub);
			}
			else{
				throw new VisitorException("Unknown subject of triple in construct clause");
			}
			
			// property list
			ASTBDPLConstructPropertyListPath propertyList = node.jjtGetChild(ASTBDPLConstructPropertyListPath.class);
			if(propertyList != null){
				propertyList.jjtAccept(this, data);
			}
			
			((ConstructTranslatorData)data).addSubject(sub);
			((ConstructTranslatorData)data).setCurrentSubject(null);
			
			((ConstructTranslatorData)data).flagType = -1;
			((ConstructTranslatorData)data).flagStream = -1;
			
				//for test
				System.out.println("ConstructTranslationProcessor add subject:");
				List<String> co = sub.getContent();
				for(int i = 0; i < co.size(); i++){
					System.out.print(co.get(i)+" ");
				}
				System.out.println();
				
				List<TriplePredicate> ps = sub.getPredicates();
				for(int i = 0; i < ps.size(); i++){
					System.out.print("\t");
					TriplePredicate p = ps.get(i);
					co = p.getContent();
					for(int j = 0; j < co.size(); j++){
						System.out.print(co.get(j)+" ");
					}
					System.out.println();
					
					List<TripleObject> os = p.getObjects();
					for(int j = 0; j < os.size(); j++){
						System.out.print("\t\t");
						TripleObject o = os.get(j);
						co = o.getContent();
						for(int k = 0; k < co.size(); k++){
							System.out.print(co.get(k)+" ");
						}
						System.out.println();
					}
				}
				
				
			return data;
		}
		
		
		@Override
		public Object visit(ASTBDPLConstructArrayVar node, Object data)
				throws VisitorException
		{
			/*
			 * array var
			 * content[0]: function iri
			 * content[1]: array name
			 * content[2]: array index
			 */
			TripleSubject sub = ((ConstructTranslatorData)data).getCurrentSubject();
			
			if(sub != null){
				BDPLArrayTable arrayTable = ((ConstructTranslatorData)data).getArrayTable();
				
				// content of subject
				List<String> content = sub.getContent();
				
				// content of object
				if(content.size() > 0){
					List<TriplePredicate> pres = sub.getPredicates();
					if(pres.size() > 0){
						List<TripleObject> objs = pres.get(pres.size()-1).getObjects();
						if(objs.size() > 0){
							content = objs.get(objs.size()-1).getContent();
						}
						else{
							throw new VisitorException("Object instance not created for ASTBDPLConstructArrayVar");
						}
					}
					else{
						throw new VisitorException("No predicate for ASTBDPLConstructArrayVar");
					}
				}	
					
				Node fNode = node.jjtGetChild(0);
				if(fNode instanceof ASTIRI){
					content.add(((ASTIRI) fNode).getValue());
					fNode = node.jjtGetChild(1);
						
					if(fNode instanceof ASTArrayVar){
						String varName = ((ASTVar)fNode.jjtGetChild(0)).getName();
						if(arrayTable.contain(varName)){
							content.add(varName);
							content.add(((ASTArrayVar) fNode).getSize());
						}
						else{
							throw new VisitorException("Array variable "+varName+"() in construct clause is not declared");
						}
					}
					else{
						throw new VisitorException("Sytaxtree node uncorrect in ASTBDPLConstructArrayVar");
					}
				}
				else if(fNode instanceof ASTArrayVar){
					content.add(null);
					String varName = ((ASTVar)fNode.jjtGetChild(0)).getName();
					if(arrayTable.contain(varName)){
						content.add(varName);
						content.add(((ASTArrayVar) fNode).getSize());
					}
					else{
						throw new VisitorException("Array variable "+varName+"() in construct clause is not declared");
					}
				}
				else{
					throw new VisitorException("Sytaxtree node uncorrect in ASTBDPLConstructArrayVar");
				}
			}
			
			return data;
		}
		
		@Override
		public Object visit(ASTRDFLiteral node, Object data)
				throws VisitorException
		{	
			/*
			 * rdf literal
			 * content[0]: label
			 * content[1]: language tag
			 * content[0]: data type
			 */
			TripleSubject sub = ((ConstructTranslatorData)data).getCurrentSubject();
			
			if(sub != null){
				// content of subject
				List<String> content = sub.getContent();
				
				// content of object
				if(content.size() > 0){
					List<TriplePredicate> pres = sub.getPredicates();
					if(pres.size() > 0){
						List<TripleObject> objs = pres.get(pres.size()-1).getObjects();
						if(objs.size() > 0){
							content = objs.get(objs.size()-1).getContent();
						}
						else{
							throw new VisitorException("Object instance not created for ASTBDPLConstructArrayVar");
						}
					}
					else{
						throw new VisitorException("No predicate for ASTBDPLConstructArrayVar");
					}
				}		
				
				content.add(node.getLabel().getValue());
				content.add(node.getLang());
				ASTIRI dataType = node.getDatatype();
				if(dataType != null){
					content.add(dataType.getValue());
				}
				else{
					content.add(null);
				}
			}
			
			return data;
		}
		
		@Override
		public Object visit(ASTBDPLConstructPropertyListPath node, Object data)
				throws VisitorException
		{	
			TripleSubject sub = ((ConstructTranslatorData)data).getCurrentSubject();
			
			if(sub != null){
				
				// predicate
				List<TriplePredicate> pres = sub.getPredicates();
				Node fNode = node.jjtGetChild(0);
				
				if(fNode instanceof ASTVar){
					TriplePredicate pre = new TriplePredicate(BDPLConstants.TYPE_VAR);
					pre.getContent().add(((ASTVar) fNode).getName());
					pres.add(pre);
				}
				else if(fNode instanceof ASTPathAlternative){
					
					TriplePredicate pre = new TriplePredicate(BDPLConstants.TYPE_IRI);
					List<ASTPathSequence> path = ((ASTPathAlternative) fNode).jjtGetChildren(ASTPathSequence.class);
					if(path != null){
						if(path.size() == 1){
							List<ASTPathElt> path1 = path.get(0).jjtGetChildren(ASTPathElt.class);
							
							if(path1 != null){
								if(path1.size() == 1){
									Node inode = path1.get(0).jjtGetChild(0);
									
									if(inode instanceof ASTIRI){
										String iri = ((ASTIRI) inode).getValue();
										pre.getContent().add(iri);
										pres.add(pre);
											
										if(iri.equals(RDF.TYPE.toString())){
											if(((ConstructTranslatorData)data).flagType == 0){
												((ConstructTranslatorData)data).flagType = 1;
											}
										}
										else if(iri.equals(BDPLConstants.URI_STREAM)){
											if(((ConstructTranslatorData)data).flagStream == 0){
												((ConstructTranslatorData)data).flagStream = 1;
											}
										}
									}
									else{
										throw new VisitorException("Property path is not allowed in construct clause");
									}
								}
								else{
									throw new VisitorException("Property path is not allowed in construct clause");
								}
							}
							else{
								throw new VisitorException("Sytaxtree node uncorrect in ASTPathElt");
							}
						}
						else{
							throw new VisitorException("Property path is not allowed in construct clause");
						}
					}
					else{
						throw new VisitorException("Sytaxtree node uncorrect in ASTPathAlternative");
					}
				}
				else{
					throw new VisitorException("Sytaxtree node uncorrect in ASTBDPLConstructPropertyListPath");
				}
				
				// object list
				ASTBDPLConstructObjectList objList = (ASTBDPLConstructObjectList)node.jjtGetChild(1);
				objList.jjtAccept(this, data);
				
				
				// more properties
				ASTBDPLConstructPropertyListPath nextPropertyList = node.jjtGetChild(ASTBDPLConstructPropertyListPath.class);
				if(nextPropertyList != null){
					nextPropertyList.jjtAccept(this, data);
				}
			}
			
			return data;
		}
		
		@Override
		public Object visit(ASTBDPLConstructObjectList node, Object data)
				throws VisitorException
		{	
			TripleSubject sub = ((ConstructTranslatorData)data).getCurrentSubject();
			
			if(sub != null){
				// content of subject
				List<String> content = sub.getContent();
				
				// content of object
				if(content.size() > 0){
					List<TriplePredicate> pres = sub.getPredicates();
					if(pres.size() > 0){
						List<TripleObject> objs = pres.get(pres.size()-1).getObjects();
						
						// object
						Node fNode = node.jjtGetChild(0);
						
						if(fNode instanceof ASTBDPLConstructArrayVar){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_ARRAY);
							objs.add(obj);
							
							fNode.jjtAccept(this, data);
						}
						else if(fNode instanceof ASTVar){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_VAR);
							obj.getContent().add(((ASTVar) fNode).getName());
							objs.add(obj);
						}
						else if(fNode instanceof ASTIRI){
							String iri = ((ASTIRI) fNode).getValue();
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_IRI);
							obj.getContent().add(iri);
							objs.add(obj);
							
							// type and stream only be uri.
							if(((ConstructTranslatorData)data).flagType == 1){
								((ConstructTranslatorData)data).setRDFType(iri);
							}
							if(((ConstructTranslatorData)data).flagStream == 1){
								((ConstructTranslatorData)data).setStreamName(iri);
							}
							
						}
						else if(fNode instanceof ASTRDFLiteral){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_LITERAL);
							objs.add(obj);
							
							fNode.jjtAccept(this, data);
						}
						else if(fNode instanceof ASTNumericLiteral){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_LITERAL);
							obj.getContent().add(((ASTNumericLiteral) fNode).getValue());
							obj.getContent().add(null);
							URI dataType = ((ASTNumericLiteral) fNode).getDatatype();
							if(dataType != null){
								obj.getContent().add(dataType.toString());
							}
							else{
								obj.getContent().add(null);
							}
							objs.add(obj);
							
						}
						else if(fNode instanceof ASTTrue){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_LITERAL);
							obj.getContent().add("true");
							obj.getContent().add(null);
							obj.getContent().add(XMLSchema.BOOLEAN.toString());
							objs.add(obj);
							
						}
						else if(fNode instanceof ASTFalse){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_LITERAL);
							obj.getContent().add("false");
							obj.getContent().add(null);
							obj.getContent().add(XMLSchema.BOOLEAN.toString());
							objs.add(obj);
							
						}
						else if(fNode instanceof ASTBlankNode){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_BN);
							//TODO
							obj.getContent().add("bn");
							objs.add(obj);
						}
						else if(fNode instanceof ASTCollection){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_COLLECTION);
							//TODO
							obj.getContent().add("collection");
							objs.add(obj);
						}
						else if(fNode instanceof ASTBlankNodePropertyList){
							TripleObject obj = new TripleObject(BDPLConstants.TYPE_BNL);
							//TODO
							obj.getContent().add("bnlist");
							objs.add(obj);
						}
						else{
							throw new VisitorException("Unknown subject of triple in construct clause");
						}
						
						// more objects
						ASTBDPLConstructObjectList nextObjectList = node.jjtGetChild(ASTBDPLConstructObjectList.class);
						if(nextObjectList != null){
							nextObjectList.jjtAccept(this, data);
						}
					}
					else{
						throw new VisitorException("No predicate for ASTBDPLConstructArrayVar");
					}
				}
				else{
					throw new VisitorException("No subject for ASTBDPLConstructObjectList");
				}
			}
			
			return data;
		}
	}

}
