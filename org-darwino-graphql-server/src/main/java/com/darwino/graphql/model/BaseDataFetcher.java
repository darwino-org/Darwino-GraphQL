/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Licensed under The MIT License (https://opensource.org/licenses/MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.darwino.graphql.model;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.json.jsonpath.JsonPathFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Base class for a data fetcher.
 * 
 * @author Philippe Riand
 */
public abstract class BaseDataFetcher<T> implements DataFetcher {
	
	public static final String EXPR_START	= "${";
	public static final String EXPR_END		= "}";
	
	public BaseDataFetcher() {
	}
    
	protected String getStringParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		return getStringParameter(environment, argName, null);
	}
	protected String getStringParameter(DataFetchingEnvironment environment, String argName, String defaultValue) throws JsonException {
		Object v = getParameter(environment, argName);
		return v!=null ? JsonUtil.asString(v) : defaultValue;
	}
	
	protected int getIntParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		return getIntParameter(environment, argName, 0);
	}
	protected int getIntParameter(DataFetchingEnvironment environment, String argName, int defaultValue) throws JsonException {
		Object v = getParameter(environment, argName);
		return v!=null ? JsonUtil.asInt(v) : defaultValue;
	}
	
	protected boolean getBooleanParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		return getBooleanParameter(environment, argName, false);
	}
	protected boolean getBooleanParameter(DataFetchingEnvironment environment, String argName, boolean defaultValue) throws JsonException {
		Object v = getParameter(environment, argName);
		return v!=null ? JsonUtil.asBoolean(v) : defaultValue;
	}
	
	private Object getParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		Object value = environment.getArgument(argName);
		if(value instanceof String) {
			String s = (String)value;
			if(s.startsWith(EXPR_START) && s.endsWith(EXPR_END)) {
				// TODO: Look for pipes
				String expr = s.substring(2, s.length()-1);
				Object source = environment.getSource();
				if(source instanceof ObjectAccessor) {
					JsonPath p = JsonPathFactory.get(expr);
					return ((ObjectAccessor<?>)source).readValue(p);
				}
			}
		}
		return value;
	}
}
