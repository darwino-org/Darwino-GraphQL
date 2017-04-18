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

package com.darwino.graphql.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.darwino.commons.json.JsonException;

/**
 * GraphQL query factory.
 * 
 * @author Philippe Riand
 */
public class MulticastGraphQLQueryFactory implements GraphQueryFactory {
	
	private List<GraphQueryFactory> factories;
	
	public MulticastGraphQLQueryFactory() {
		this.factories = new ArrayList<GraphQueryFactory>();
	}
	
	public List<GraphQueryFactory> getFactories() {
		return factories;
	}
	
	@Override
	public Set<String> getQueryNames() throws JsonException {
		Set<String> res = new HashSet<String>();
		for(int i=0; i<factories.size(); i++) {
			Set<String> s = factories.get(i).getQueryNames();
			if(s!=null) {
				res.addAll(s);
			}
		}
		return res;
	}

	@Override
	public GraphQuery getQuery(String name) throws JsonException {
		for(int i=0; i<factories.size(); i++) {
			GraphQuery ms = factories.get(i).getQuery(name); 
			if(ms!=null) {
				return ms;
			}
		}
		return null;
	}
}
