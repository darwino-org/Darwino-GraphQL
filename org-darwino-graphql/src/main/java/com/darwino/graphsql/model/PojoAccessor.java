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

import java.util.Collections;
import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.model.ModelAccessor;
import com.darwino.commons.model.ModelException;

import graphql.schema.DataFetchingEnvironment;

/**
 * Add access to a POJO object.
 * 
 * @author Philippe Riand
 */
public abstract class PojoAccessor<T> extends ObjectAccessor<T> {

	public PojoAccessor(DataFetchingEnvironment env, T value) {
		super(env,value);
	}
	
	@Override
	public Object readValue(JsonPath path) throws JsonException {
		Object[] parts = path.getSimpleParts();
		if(parts!=null && parts.length==1 && parts[0] instanceof String) {
			Object p = getProperty((String)parts[0]);
			return p;
		}
		return null;
	}
	
	@Override
	public List<?> readList(JsonPath path) throws JsonException {
		return Collections.singletonList(readValue(path));
	}
	
	protected Object getProperty(String propName) {
		try {
			ModelAccessor ac = getAccessor();
			if(ac!=null) {
				return ac.getMember(getValue(), propName);
			}
		} catch(ModelException ex) {
			// Ignore on purpose and return null
		}
		return null;
	}
	
	public ModelAccessor getAccessor() {
		return null;
	}
}
