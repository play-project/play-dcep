/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;


public interface ElementVisitor
{
	
    public void visit(ElementTriplesBlock el) ;
    public void visit(ElementPathBlock el) ;
    public void visit(ElementFilter el) ;
    public void visit(ElementAssign el) ;
    public void visit(ElementBind el) ;
    public void visit(ElementData el) ;
    public void visit(ElementUnion el) ;
    public void visit(ElementOptional el) ;
    public void visit(ElementGroup el) ;
    public void visit(ElementDataset el) ;
    public void visit(ElementNamedGraph el) ;
    public void visit(ElementExists el) ;
    public void visit(ElementNotExists el) ;
    public void visit(ElementMinus el) ;
    public void visit(ElementService el) ;
    public void visit(ElementFetch el) ;
    public void visit(ElementSubQuery el) ;
	
    public void visit(RelationalOperator relationalOperator) ;
    public void visit(ElementEventGraph el) ;
	public void visit(ElementEventBinOperator el);
	public void visit(ElementEventFilter el);
	public void visit(BooleanOperator booleanOperator);
	public void visit(ElementFnAbsFilter elementFnAbsFilter);

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */