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

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.jsonpath.JsonPath;

import graphql.schema.DataFetchingEnvironment;

/**
 * Wrap an object returned by a Data Fetcher.
 * 
 * @author Philippe Riand
 */
public abstract class ObjectAccessor<T> {
	
	public static final class Empty extends ObjectAccessor<Void> {
		public Empty(DataFetchingEnvironment env, ObjectAccessor<?> parent) {
			super(env,null);
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			return null;
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return null;
		}
	};

	private ObjectAccessor<?> parent;
	private T value;
	
	public ObjectAccessor(DataFetchingEnvironment env, T value) {
		Object src = env.getSource();
		if(src instanceof ObjectAccessor<?>) {
			this.parent = (ObjectAccessor<?>)src;
		}
		this.value = value;
	}
	
	public ObjectAccessor<?> getParent() {
		return parent;
	}
	
	public T getValue() {
		return value;
	}

	/**
	 * Extract the name of a simple field.
	 * A simple field path is of the form $.XXXX and then XXXX is returned
	 * @param path the source path
	 * @return the extracted field
	 * @throws JsonException
	 */
	public String getSimpleField(JsonPath path) throws JsonException {
		Object[] parts = path.getSimpleParts();
		if(parts.length==1 && parts[0] instanceof String) {
			return (String)parts[0];
		}
		return null;
	}
	
	public abstract Object readValue(JsonPath path) throws JsonException;
	public abstract List<?> readList(JsonPath path) throws JsonException;
}
