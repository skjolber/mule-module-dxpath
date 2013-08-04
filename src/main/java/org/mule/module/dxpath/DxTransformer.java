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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionRuntimeException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;
import org.mule.module.xml.stax.MapNamespaceContext;
import org.mule.module.xml.util.NamespaceManager;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DxTransformer extends AbstractMessageTransformer {
	/**
	 * Result type.
	 */
	public enum ResultType {
		NODESET, NODE, STRING, BOOLEAN, NUMBER
	}

	private volatile DxOperator operator;
	private volatile DxVariableResolver variableResolver;
	private volatile String expression;
	private volatile QName resultType;
	private volatile Document nullPayloadDocument;
	
	private volatile Map<String, Object> contextProperties;

	public DxTransformer() {
		registerSourceType(DataTypeFactory.create(org.w3c.dom.Node.class));
		registerSourceType(DataTypeFactory.create(InputSource.class));
		registerSourceType(DataTypeFactory.create(NullPayload.class));

		contextProperties = new HashMap<String, Object>();
	}

	@Override
	public void initialise() throws InitialisationException {
		super.initialise();

		NamespaceManager namespaceManager = null;
		try {
			namespaceManager = muleContext.getRegistry().lookupObject(NamespaceManager.class);
		} catch (RegistrationException e) {
			throw new ExpressionRuntimeException(CoreMessages.failedToLoad("NamespaceManager"), e);
		}

		if (expression == null) {
			throw new InitialisationException(MessageFactory.createStaticMessage("An expression must be supplied to the Dynamic XPath Transformer"), this);
		}

		HashMap<String, String> prefixToNamespaceMap = new HashMap<String, String>();
		if (namespaceManager != null) {
			prefixToNamespaceMap.putAll(namespaceManager.getNamespaces());
		}

		MapNamespaceContext namespaceContext = new MapNamespaceContext(prefixToNamespaceMap);

		variableResolver = new DxVariableResolver();

		try {
			operator = new DxOperator(namespaceContext, variableResolver);

			operator.compile(expression);
		} catch (Exception e) {
			throw new InitialisationException(MessageFactory.createStaticMessage("Problem initializing xpath"), e, this);
		}

		try {
			// create an empty document for null payloads / instances where the xpath expression itself does not need any document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			nullPayloadDocument = builder.newDocument();
		} catch (Exception e) {
			throw new InitialisationException(MessageFactory.createStaticMessage("Problem initializing xpath"), e, this);
		}
		

	}

	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            The expression to set.
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Result type from this transformer.
	 * 
	 * @param resultType
	 *            Result type from this transformer.
	 */
	public void setResultType(ResultType resultTypeType) {
		QName resultType;
		switch (resultTypeType) {
		case BOOLEAN:
			resultType = XPathConstants.BOOLEAN;
			break;
		case NODE:
			resultType = XPathConstants.NODE;
			break;
		case NODESET:
			resultType = XPathConstants.NODESET;
			break;
		case NUMBER:
			resultType = XPathConstants.NUMBER;
			break;
		default:
			resultType = XPathConstants.STRING;
			break;
		}
		this.resultType = resultType;
	}

	/**
	 * Gets the parameters to be used when applying the transformation
	 * 
	 * @return a map of the parameter names and associated values
	 * @see javax.xml.transform.Transformer#setParameter(java.lang.String,
	 *      java.lang.Object)
	 */
	public Map<String, Object> getContextProperties() {
		return contextProperties;
	}

	/**
	 * Sets the parameters to be used when applying the transformation
	 * 
	 * @param contextProperties
	 *            a map of the parameter names and associated values
	 * @see javax.xml.transform.Transformer#setParameter(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setContextProperties(Map<String, Object> contextProperties) {
		this.contextProperties = contextProperties;
	}
	
	protected Object evaluateTransformParameter(String key, Object value, MuleMessage message) throws TransformerException {
		if (value instanceof String) {
			return muleContext.getExpressionManager().parse(value.toString(), message);
		}

		return value;
	}

	@Override
	public Object transformMessage(MuleMessage message, String putputEncoding) throws TransformerException {
		Object src = message.getPayload();
		try {

			if (contextProperties != null) {
				// resolve parameters dynamically
				for (Entry<String, Object> entryParameter : contextProperties.entrySet()) {
					String key = entryParameter.getKey();

					Object value = evaluateTransformParameter(key, entryParameter.getValue(), message);
					
					variableResolver.newVariable(key, value);
				}
			}

			if (src instanceof InputSource) {
				return operator.evaluate((InputSource) src, expression, resultType);
			} else if (src instanceof NullPayload) {
				return operator.evaluate(nullPayloadDocument, expression, resultType);
			} else {
				return operator.evaluate(src, expression, resultType);
			}
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			variableResolver.clear();
		}
	}

}