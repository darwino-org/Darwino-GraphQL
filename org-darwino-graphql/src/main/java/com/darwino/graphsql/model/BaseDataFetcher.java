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

package com.darwino.graphsql.model;

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
		return JsonUtil.asString(getParameter(environment, argName));
	}
	protected int getIntParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		return JsonUtil.asInt(getParameter(environment, argName));
	}
	protected boolean getBooleanParameter(DataFetchingEnvironment environment, String argName) throws JsonException {
		return JsonUtil.asBoolean(getParameter(environment, argName));
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
