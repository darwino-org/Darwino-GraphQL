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

package com.darwino.graphsql.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base GraphQL query factory.
 * 
 * @author Philippe Riand
 */
public class StaticGraphQueryFactory implements GraphQueryFactory {
	
	private Map<String,GraphQuery> queries = new HashMap<String, GraphQuery>();
	
	public StaticGraphQueryFactory() {
	}
	
	public StaticGraphQueryFactory add(String name, GraphQuery query) {
		queries.put(name, query);
		return this;
	}
	
	@Override
	public Set<String> getQueryNames() {
		return queries.keySet();
	}

	@Override
	public GraphQuery getQuery(String name) {
		return queries.get(name);
	}
}