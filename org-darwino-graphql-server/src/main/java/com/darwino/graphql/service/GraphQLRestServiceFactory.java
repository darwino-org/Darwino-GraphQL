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

package com.darwino.graphql.service;

import java.util.List;

import com.darwino.commons.Platform;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.HttpServiceDescription;
import com.darwino.commons.services.impl.DarwinoHttpServiceDescription;
import com.darwino.commons.services.rest.RestServiceBinder;
import com.darwino.commons.services.rest.RestServiceFactory;
import com.darwino.graphql.query.GraphQLSession;
import com.darwino.graphql.query.GraphQLSessionFactory;


/**
 * GraphQL service factory.
 * 
 * @author Philippe Riand
 */
public class GraphQLRestServiceFactory extends RestServiceFactory {
	
	public GraphQLRestServiceFactory(String path) {
		super(path);
	}
	
	public GraphQLSession createSession(String contextName) throws JsonException {
		GraphQLSessionFactory factory = Platform.getServiceUnchecked(GraphQLSessionFactory.class);
		if(factory!=null) {
			return factory.createSession(contextName);
		}
		throw new JsonException(null,"Missing GraphQL session factory");
	}
	
	@Override
	public void getServicesDescriptions(List<HttpServiceDescription> list) {
		super.getServicesDescriptions(list);
		list.add( new DarwinoHttpServiceDescription(this,
			"graphql",
			"GraphQL",
			"GraphQL Services",
			"GraphQL Services",
			"/openapi/GraphQL.json"
		));
	}
	
	@Override
	protected void createServicesBinders(List<RestServiceBinder> binders) {
		/////////////////////////////////////////////////////////////////////////////////
		// GraphQL query
		binders.add(new RestServiceBinder() {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return newGraphQLService();
			}
		});
		// GraphQL predefined query list
		binders.add(new RestServiceBinder("list") {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return new GraphQLQueryListService(GraphQLRestServiceFactory.this);
			}
		});
	}
	
	protected GraphQLService newGraphQLService() {
		return new GraphQLService(this);
	}
}
