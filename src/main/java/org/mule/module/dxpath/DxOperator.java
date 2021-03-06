/***************************************************************************
 * 
 * This file is part of the 'dxpath mule module' project at
 * https://github.com/skjolber/mule-module-dxpath
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ****************************************************************************/

package org.mule.module.dxpath;

import java.util.Hashtable;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathVariableResolver;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.MessageFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

// http://stackoverflow.com/questions/5663285/how-to-use-java-string-variables-inside-xpath-query/5664394#5664394

public class DxOperator {

    protected XPath xPath;
    protected XPathFactory xPathFactory;

    private Hashtable<String, XPathExpression> compiled = new Hashtable<String, XPathExpression>();
    private DxVariableResolver variableResolver;
	private Document nullPayloadDocument;

    private void initEmptyDocument() throws ParserConfigurationException {
		// create an empty document for null payloads / instances where the xpath expression itself does not need any document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		nullPayloadDocument = builder.newDocument();
    }
    
    protected void initFactory() throws XPathFactoryConfigurationException {
        xPathFactory = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL);
    }

    protected void initXPath(NamespaceContext context, DxVariableResolver variableResolver) {
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(context);
		xPath.setXPathVariableResolver(variableResolver);
    }

    public DxOperator(NamespaceContext context, DxVariableResolver variableResolver) throws XPathFactoryConfigurationException, ParserConfigurationException {
    	this.variableResolver = variableResolver; 
        initFactory();
        initXPath(context, variableResolver);
        initEmptyDocument();
    }
    
    public DxVariableResolver getVariableResolver() {
		return variableResolver;
	}

    public Object evaluateNull(String expression, QName value) throws XPathExpressionException {

        // create an XPath expression - http://www.zvon.org/xxl/XPathTutorial/General/examples.html
        XPathExpression findStatements = compile(expression);

        // execute the XPath expression against the document
        return findStatements.evaluate(nullPayloadDocument, value);
    }

    public Object evaluate(Object sourceDoc, String expression, QName value) throws XPathExpressionException {

        // create an XPath expression - http://www.zvon.org/xxl/XPathTutorial/General/examples.html
        XPathExpression findStatements = compile(expression);

        // execute the XPath expression against the document
        return findStatements.evaluate(sourceDoc, value);
    }

    public Object evaluate(InputSource sourceDoc, String expression, QName value) throws XPathExpressionException {

        // create an XPath expression - http://www.zvon.org/xxl/XPathTutorial/General/examples.html
        XPathExpression findStatements = compile(expression);

        // execute the XPath expression against the document
        return findStatements.evaluate(sourceDoc, value);
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        if(compiled.containsKey(expression)) {
            return (XPathExpression) compiled.get(expression);
        }

        XPathExpression xpath = xPath.compile(expression);

        compiled.put(expression, xpath);

        return xpath;
    }
}