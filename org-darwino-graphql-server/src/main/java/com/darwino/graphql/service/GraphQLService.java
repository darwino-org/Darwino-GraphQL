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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonJavaFactory;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.services.HttpService;
import com.darwino.commons.services.HttpServiceContext;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphql.GraphContext;
import com.darwino.graphql.query.GraphQLSession;
import com.darwino.graphql.query.GraphQuery;
import com.darwino.graphql.query.GraphQueryFactory;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;


/**
 * GraphQL Service.
 * 
 * This service executes a GraphQL request over HTTP.
 * See: http://graphql.org/learn/serving-over-http/
 * 
 * @author Philippe Riand
 */
public class GraphQLService extends HttpService {
	
	private GraphQLRestServiceFactory factory;	
	
	public GraphQLService(GraphQLRestServiceFactory factory) {
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
			
			String queryName = context.getQueryParameterString("name");
			if(StringUtil.isNotEmpty(queryName)) {
				if(context.isGet()) {
					GraphQueryFactory qf = session.getQueryFactory();
					if(qf!=null) {
						GraphQuery query = qf.getQuery(queryName);
						if(query==null && queryName.endsWith(".graphql")) {
							query = qf.getQuery(queryName.substring(0, queryName.length()-".graphql".length()));
						}
						if(query!=null) {
							processRequest(context, session, query.loadQuery(), null, null);
							return;
						}
					}
					throw HttpServiceError.errorNotFound(null,"GraphQL query {0} is not found",queryName);
				} else {
					throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
				}
			} else {
				if(context.isGet()) {
					processGet(context,session);
				} else if(context.isPost()) {
					processPost(context,session);
				} else {
					throw HttpServiceError.errorUnsupportedMethod(context.getMethod());
				}
			}
		} catch(JsonException ex) {
			throw HttpServiceError.error500(ex);
		}
	}

	protected void processGet(HttpServiceContext context, GraphQLSession session) throws JsonException {
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
			processRequest(context, session, query, operationName, variables);
		}
	}
	
	protected Map<String,Object> getParametersFromString(String sv) {
		if(StringUtil.isNotEmpty(sv)) {
			try {
				return JsonObject.fromJson(sv);
			} catch(JsonException ex) {
				throw HttpServiceError.error500(ex,"Error while parsing the viariables");
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected void processPost(HttpServiceContext context, GraphQLSession session) throws JsonException {
		JsonObject ct = (JsonObject)context.getContentAsJson();
		String query = ct.getString("query");
		if(StringUtil.isNotEmpty(query)) {
			String operationName = ct.getString("operationName");
			Object variables = ct.get("variables");
			if(variables instanceof String) {
				variables = JsonJavaFactory.instance.fromJson((String)variables);
			}
			processRequest(context, session, query, operationName, (Map<String, Object>)variables);
		}
	}
	
	
	protected void processRequest(HttpServiceContext context, GraphQLSession session, String query, String operationName, Map<String,Object> variables) throws JsonException {
		if(variables==null) {
			variables = Collections.emptyMap();
		}
		
        GraphContext graphContext = session.getContext();
        
		ExecutionResult result = new GraphQL(session.getSchema()).execute(query,operationName,graphContext,variables);
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
			throw HttpServiceError.error(null,HttpBase.SC_INTERNAL_SERVER_ERROR,extra,"Error while executing the GraphQL request");
		}
	}	
}
