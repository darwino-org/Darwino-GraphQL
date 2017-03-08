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

package com.darwino.graphsql.service;

import java.util.Set;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.commons.util.QuickSort;
import com.darwino.graphsql.query.GraphQLSession;
import com.darwino.graphsql.query.GraphQueryFactory;


/**
 * Service that returns the list of available queries.
 * 
 * @author Philippe Riand
 */
public class GraphQLQueryListService extends HttpService {
	
	private GraphQLRestServiceFactory factory;	
	
	public GraphQLQueryListService(GraphQLRestServiceFactory factory) {
		this.factory = factory;
	}
	
	public GraphQLRestServiceFactory getFactory() {
		return factory;
	}
	
	public GraphQLSession getSession(HttpServiceContext context) throws JsonException {
		return factory.createSession(null);
	}
	
	@Override
	public void service(HttpServiceContext context) {
		try {
			GraphQLSession session = getSession(context);

			if(context.isGet()) {
				JsonArray a = new JsonArray();
				GraphQueryFactory qf = session.getQueryFactory();
				if(qf!=null) {
					Set<String> names = qf.getQueryNames();
					a.addAll(names);
					(new QuickSort.JavaList(a)).sort();
				}
				context.emitJson(a);
			} else {
				throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
			}
		} catch(JsonException ex) {
			throw HttpServiceError.error500(ex);
		}
	}
}
