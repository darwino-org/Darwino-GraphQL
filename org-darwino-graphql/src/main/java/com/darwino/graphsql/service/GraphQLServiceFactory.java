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

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.rest.RestServiceBinder;
import com.darwino.commons.services.rest.RestServiceFactory;
import com.darwino.graphsql.GraphContext;
import com.darwino.graphsql.query.GraphQueryFactory;

import graphql.schema.GraphQLSchema;


/**
 * GraphQL service factory.
 * 
 * @author Philippe Riand
 */
public class GraphQLServiceFactory extends RestServiceFactory {
	
	private GraphQLSchema schema;
	private GraphQueryFactory queryFactory;
	
	public GraphQLServiceFactory(String path, GraphQLSchema schema, GraphQueryFactory queryFactory) {
		super(path);
		this.schema = schema;
		this.queryFactory = queryFactory;
	}
	
	public GraphQLSchema getSchema() {
		return schema;
	}
	
	public GraphContext createContext() throws JsonException {
		return new GraphContext();
	}
	
	public GraphQueryFactory getQueryFactory() throws JsonException {
		return queryFactory;
	}
	
	@Override
	protected void createServicesBinders(List<RestServiceBinder> binders) {
		/////////////////////////////////////////////////////////////////////////////////
		// GraphQL query
		binders.add(new RestServiceBinder("query") {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return newGraphQLService(null);
			}
		});
		// GraphQL predefined query list
		binders.add(new RestServiceBinder("queries") {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return new GraphQLQueryListService(GraphQLServiceFactory.this);
			}
		});
		// GraphQL predefined queries
		binders.add(new RestServiceBinder("queries",null) {
			@Override
			public HttpService createService(HttpServiceContext context, String[] parts) {
				return newGraphQLService(parts[1]);
			}
		});
	}
	
	protected GraphQLService newGraphQLService(String queryName) {
		return new GraphQLService(this,queryName);
	}
}
