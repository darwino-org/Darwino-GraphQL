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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.graphql.GraphContext;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

/**
 * Session for executing a GraphQL query.
 * 
 * @author Philippe Riand
 */
public abstract class GraphQLSession {
	
	public GraphQLSession() {
	}
	
	public abstract GraphQLSchema getSchema() throws JsonException;
	
	public abstract GraphQueryFactory getQueryFactory() throws JsonException;

	public abstract GraphContext getContext() throws JsonException;
	
	/**
	 * Utility to execute a predefined query.
	 */
	public Object executePredefined(String queryName) throws JsonException {
		return executePredefined(queryName, null, null);
	}
	public Object executePredefined(String queryName, Map<String,Object> variables) throws JsonException {
		return executePredefined(queryName, null, variables);
	}
	public Object executePredefined(String queryName, String operationName, Map<String,Object> variables) throws JsonException {
		GraphQueryFactory qf = getQueryFactory();
		if(qf!=null) {
			GraphQuery query = qf.getQuery(queryName);
			if(query!=null) {
				return execute(query.loadQuery(), operationName, variables);
			}
		}
		throw HttpServiceError.errorNotFound(null,"GraphQL query {0} is not found",queryName);
	}
	
	/**
	 * Utility to execute an inline query.
	 */
	public Object execute(String query) throws JsonException {
		return execute(query, null, null);
	}
	public Object execute(String query, Map<String,Object> variables) throws JsonException {
		return execute(query, null, variables);
	}
	public Object execute(String query, String operationName, Map<String,Object> variables) throws JsonException {
		GraphContext graphContext = getContext();
		if(variables==null) variables=Collections.emptyMap();
		ExecutionResult result = new GraphQL(getSchema()).execute(query,operationName,graphContext,variables);
		if (result.getErrors().isEmpty()) {
			JsonObject root = new JsonObject();
			root.put("data", result.getData());
			return root;
		} else {
			JsonArray errlist = new JsonArray();
			List<GraphQLError> errors = result.getErrors();
			for(GraphQLError e: errors) {
				String err = e.toString();
				errlist.add(err);
			}
			
			JsonException ex = new JsonException(null,"Error while executing the GraphQL request");
			ex.getExtraInformation().put("GraphQL", errlist);
			throw ex;
		}
	}
}
