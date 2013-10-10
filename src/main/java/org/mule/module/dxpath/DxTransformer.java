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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
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
import org.xml.sax.InputSource;

public class DxTransformer extends AbstractMessageTransformer {
	/**
	 * Result type.
	 */
	public enum ResultType {
		NODESET, NODE, STRING, BOOLEAN, NUMBER
	}

    // keep at least 1 XSLT Transformer ready by default
    private static final int MIN_IDLE_TRANSFORMERS = 1;
    // keep max. 32 XSLT Transformers around by default
    private static final int MAX_IDLE_TRANSFORMERS = 32;
    // MAX_IDLE is also the total limit
    private static final int MAX_ACTIVE_TRANSFORMERS = MAX_IDLE_TRANSFORMERS;

    protected final GenericObjectPool transformerPool;
    
	private volatile String expression;
	private volatile QName resultType;
	
	private volatile Map<String, Object> contextProperties;
	
	private volatile String[] keys;
	private volatile Object[] values;

	public DxTransformer() {
		super();
		
		transformerPool = new GenericObjectPool(new PooledDxTransformerFactory());
        transformerPool.setMinIdle(MIN_IDLE_TRANSFORMERS);
        transformerPool.setMaxIdle(MAX_IDLE_TRANSFORMERS);
        transformerPool.setMaxActive(MAX_ACTIVE_TRANSFORMERS);

		registerSourceType(DataTypeFactory.create(org.w3c.dom.Node.class));
		registerSourceType(DataTypeFactory.create(InputSource.class));
		registerSourceType(DataTypeFactory.create(NullPayload.class));

		contextProperties = new HashMap<String, Object>();
	}

	@Override
	public void initialise() throws InitialisationException {
		super.initialise();

		if (expression == null) {
			throw new InitialisationException(MessageFactory.createStaticMessage("An expression must be supplied to the Dynamic XPath Transformer"), this);
		}

        try {
        	transformerPool.addObject();
        } catch (Throwable te) {
        	throw new InitialisationException(te, this);
        }

        // transform context properties to thread safe alternative
        List<String> keys = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        
		for (Entry<String, Object> entryParameter : contextProperties.entrySet()) {
			keys.add(entryParameter.getKey());
			values.add(entryParameter.getValue());
		}

		this.keys = keys.toArray(new String[keys.size()]);
		this.values = values.toArray(new String[values.size()]);
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
		
		try {
			DxOperator operator = null;
			try {
				operator = (DxOperator) transformerPool.borrowObject();
				bindParameters(operator, message);

				Object src = message.getPayload();
				try {
					if (src instanceof InputSource) {
						return operator.evaluate((InputSource) src, expression, resultType);
					} else if (src instanceof NullPayload) {
						return operator.evaluateNull(expression, resultType);
					} else {
						return operator.evaluate(src, expression, resultType);
					}
				} catch (Exception e) {
					throw new TransformerException(this, e);
				}
			} finally {
				if(operator != null) {
					unbindParameters(operator);
					transformerPool.returnObject(operator);
				}
			}
		} catch (Exception e) {
			throw new TransformerException(this, e);
        }
	}
	
	protected void bindParameters(DxOperator operator, MuleMessage message) throws TransformerException, XPathExpressionException {
		operator.compile(expression);

		DxVariableResolver variableResolver = operator.getVariableResolver();
		if (contextProperties != null) {
			// resolve parameters dynamically
			for(int i = 0; i < keys.length; i++) {
				Object value = evaluateTransformParameter(keys[i], values[i], message);
				
				variableResolver.newVariable(keys[i], value);
			}
		}
	}
	
	/**
     * Removes any parameter bindings
     *
     * @param transformer the transformer to remove properties from
     */
    protected void unbindParameters(DxOperator operator)  {
		DxVariableResolver variableResolver = operator.getVariableResolver();
		variableResolver.clear();
    }

    
	/**
     * @return The current maximum number of allowable active transformer objects in
     *         the pool
     */
    public int getMaxActiveTransformers()
    {
        return transformerPool.getMaxActive();
    }

    /**
     * Sets the the current maximum number of active transformer objects allowed in the
     * pool
     *
     * @param maxActiveTransformers New maximum size to set
     */
    public void setMaxActiveTransformers(int maxActiveTransformers)
    {
        transformerPool.setMaxActive(maxActiveTransformers);
    }

    /**
     * @return The current maximum number of allowable idle transformer objects in the
     *         pool
     */
    public int getMaxIdleTransformers()
    {
        return transformerPool.getMaxIdle();
    }

    /**
     * Sets the the current maximum number of idle transformer objects allowed in the pool
     *
     * @param maxIdleTransformers New maximum size to set
     */
    public void setMaxIdleTransformers(int maxIdleTransformers)
    {
        transformerPool.setMaxIdle(maxIdleTransformers);
    }

	
	protected class PooledDxTransformerFactory extends BasePoolableObjectFactory
    {
        @Override
        public Object makeObject() throws Exception
        {
    		NamespaceManager namespaceManager = null;
    		try {
    			namespaceManager = muleContext.getRegistry().lookupObject(NamespaceManager.class);
    		} catch (RegistrationException e) {
    			throw new ExpressionRuntimeException(CoreMessages.failedToLoad("NamespaceManager"), e);
    		}


    		HashMap<String, String> prefixToNamespaceMap = new HashMap<String, String>();
    		if (namespaceManager != null) {
    			prefixToNamespaceMap.putAll(namespaceManager.getNamespaces());
    		}

    		MapNamespaceContext namespaceContext = new MapNamespaceContext(prefixToNamespaceMap);

    		DxVariableResolver variableResolver = new DxVariableResolver();

			DxOperator operator = new DxOperator(namespaceContext, variableResolver);

			return operator;
        }
    }

}