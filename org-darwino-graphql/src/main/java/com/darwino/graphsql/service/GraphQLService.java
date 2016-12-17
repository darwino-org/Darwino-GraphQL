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
import java.util.Map;

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.commons.util.StringUtil;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;


/**
 * GraphQL Service.
 * 
 * This service executes a GraphQL request over HTTP.
 * See: http://graphql.org/learn/serving-over-http/
 * 
 * @author Philippe Riand
 */
public class GraphQLService extends HttpService {
	
	private GraphQLSchema schema;	
	private boolean schemaJson;
	
	public GraphQLService(GraphQLSchema schema, boolean schemaJson) {
		this.schema = schema;
		this.schemaJson = schemaJson;
	}
	
	@Override
	public void service(HttpServiceContext context) {
		if(schemaJson) {
			if(context.isGet()) {
		    	// TODO...
		        //query(CharStreams.toString(new InputStreamReader(GraphQLServlet.class.getResourceAsStream("introspectionQuery"))), null, new HashMap<>(), schema, req, resp, context);
				throw HttpServiceError.errorNotImplemented("Accessing the GraphQL schema is not implemented");
			} else {
				throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
			}
		} else {
			if(context.isGet()) {
				processGet(context);
			} else if(context.isPost()) {
				processPost(context);
			} else {
				throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
			}
		}
	}
	
	public GraphQLSchema getSchema() {
		return schema;
	}

	protected void processGet(HttpServiceContext context) {
		String query = context.getQueryParameterString("query");
		if(StringUtil.isEmpty(query)) {
			query = context.getQueryParameterString("q"); // seems supported
		}
		if(StringUtil.isNotEmpty(query)) {
			String operationName = context.getQueryParameterString("operationName");
			String sv = context.getQueryParameterString("variables");
			Map<String,Object> variables;
			try {
				variables = StringUtil.isNotEmpty(sv) ? JsonObject.fromJson(sv) : null;
			} catch(JsonException ex) {
				throw HttpServiceError.error500(ex,"Error while parsing the variables parameter");
			}
			processRequest(context, getSchema(), query, operationName, variables);
		}
	}

	protected void processPost(HttpServiceContext context) {
		JsonObject ct = (JsonObject)context.getContentAsJson();
		String query = ct.getString("query");
		if(StringUtil.isNotEmpty(query)) {
			String operationName = ct.getString("operationName");
			Map<String,Object> variables = ct.getObject("variables");
			processRequest(context, getSchema(), query, operationName, variables);
		}
	}
	
	
	protected void processRequest(HttpServiceContext context, GraphQLSchema schema, String query, String operationName, Map<String,Object> variables) {
		ExecutionResult result = new GraphQL(schema).execute(query,operationName,context,variables);
		if (result.getErrors().isEmpty()) {
			JsonObject root = new JsonObject();
			root.put("data", result.getData());
			context.emitJson(root);
		} else {
			JsonObject extra = new JsonObject();
			JsonArray errlist = new JsonArray();
			List<GraphQLError> errors = result.getErrors();
			for(GraphQLError e: errors) {
				String err = e.toString();
				errlist.add(err);
			}
			extra.put("GraphQL", errlist);
			
			// Is there a std format for GraphQL errors?
			// To be changed when 1.5.1 i sout
			//throw HttpServiceError.error(null,HttpBase.SC_INTERNAL_SERVER_ERROR,extra,"Error while executing the GraphQL request");
			throw HttpServiceError.error(null,HttpBase.SC_INTERNAL_SERVER_ERROR,"Error while executing the GraphQL request");
		}
	}	
}
