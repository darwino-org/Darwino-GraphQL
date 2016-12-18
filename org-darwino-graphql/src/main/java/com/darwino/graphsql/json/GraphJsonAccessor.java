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

package com.darwino.graphsql.json;

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.jsonpath.JsonPath;

/**
 * Add access to JSON documents coming from the Darwino JSON store.
 * 
 * @author Philippe Riand
 */
public abstract class GraphJsonAccessor {
	
	public static final class Empty extends GraphJsonAccessor {
		public Empty(GraphJsonAccessor parent) {
			super(parent);
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

	private GraphJsonAccessor parent;
	
	public GraphJsonAccessor(Object parent) {
		if(parent instanceof GraphJsonAccessor) {
			this.parent = (GraphJsonAccessor)parent;
		}
	}
	
	public GraphJsonAccessor getParent() {
		return parent;
	}
	
	public abstract Object readValue(JsonPath path) throws JsonException;
	public abstract List<?> readList(JsonPath path) throws JsonException;
}
