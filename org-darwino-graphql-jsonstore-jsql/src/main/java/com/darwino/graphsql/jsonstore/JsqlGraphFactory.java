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

package com.darwino.graphsql.jsonstore;

import static graphql.Scalars.GraphQLString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphsql.GraphContext;
import com.darwino.graphsql.GraphFactory;
import com.darwino.graphsql.model.JsonAccessor;
import com.darwino.graphsql.model.ObjectDataFetcher;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.sql.jsql.JsqlExecutor;
import com.darwino.rdbc.SqlResultSet;

import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

/**
 * Add access to JSQL queried.
 * 
 * @author Philippe Riand
 */
public class JsqlGraphFactory extends GraphFactory {

	public static final String TYPE_JSQL 	= "DwoJsql";
	
	public JsqlGraphFactory() {
	}

	@Override
	public void createTypes(Builder builders) {
		{
			// Everything is dynamic here...
			GraphQLObjectType.Builder docBuilder = GraphQLObjectType.newObject()
				.name(TYPE_JSQL);
			builders.addDynamicFields(docBuilder);
			builders.put(TYPE_JSQL,docBuilder);
		}
	}
	
	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
//			.field(GraphQLFieldDefinition.newFieldDefinition()
//				.name("Jsql")
//				.argument(getArguments())
//				.type(new GraphQLTypeReference(TYPE_JSQL))
//				.dataFetcher(new JsqlFetcher())
//			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("Jsql")
				.argument(getArguments())
				.type(new GraphQLList(new GraphQLTypeReference(TYPE_JSQL)))
				.dataFetcher(new JsqlFetcher())
			)
		;
	}
	public static List<GraphQLArgument> getArguments() {
		List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
		args.add(databaseArgument);
		args.add(sqlArgument);
		args.add(skipArgument);
		args.add(limitArgument);
		args.add(p0Argument);
		args.add(p1Argument);
		args.add(p2Argument);
		args.add(p3Argument);
		return args;
	}
		
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Document fetcher
	//
	/////////////////////////////////////////////////////////////////////////////////
	
	public static class JsqlFetcher extends ObjectDataFetcher<Document> {
		public JsqlFetcher() {
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				JsonStoreGraphFactory.Context ctx = ((GraphContext)environment.getContext()).get(JsonStoreGraphFactory.Context.class);
				if(ctx==null) {
					throw new GraphQLException(StringUtil.format("Missing JSON database context"));
				}

				Session session = ctx.getSession();
				if(session==null) {
					throw new GraphQLException(StringUtil.format("Missing JSON session in the database context"));
				}
				
				String database = getStringParameter(environment,"database");
				if(StringUtil.isEmpty(database)) {
					database = ctx.getDatabase();
				}

				String sql = getStringParameter(environment,"sql");
				if(StringUtil.isEmpty(sql)) {
					throw new GraphQLException(StringUtil.format("Missing SQL query in the Jsql request"));
				}
				Map<String,Object> params = new HashMap<String, Object>();
				addParam(environment, params, "p0");
				addParam(environment, params, "p1");
				addParam(environment, params, "p2");
				addParam(environment, params, "p3");

				JsqlExecutor jExecutor = new JsqlExecutor(session, database);
				
				List<Object> rows = new ArrayList<Object>();
				SqlResultSet rs = jExecutor.executeQuery(sql, params);
				try {
					int skip = getIntParameter(environment,"skip");
					if(skip>0) {
						rs.skip(skip);
					}
					int limit = getIntParameter(environment,"limit");
					if(limit<=0) {
						// Should we limit the default limit?
						limit = Integer.MAX_VALUE;
					}
					for(int i=0; i<limit; i++) {
						Object row = jExecutor.loadRow(rs,false);
						if(row==null) {
							break;
						}
						rows.add(new JsonAccessor(environment,row));
					}
				} finally {
					rs.close();
				}
				
				return rows;
			} catch(Exception ex) {
				if(ex instanceof GraphQLException) {
					throw (GraphQLException)ex;
				}
				throw new GraphQLException(StringUtil.format("Error while executing the query, {0}",ex.getLocalizedMessage()),ex);
			}
		}
		private void addParam(DataFetchingEnvironment env, Map<String,Object> params, String name) throws JsonException {
			Object value = getStringParameter(env, name);
			if(value!=null) {
				params.put(name, value);
			}
		}
	};
	
	
	// Common
	public static GraphQLArgument databaseArgument = new GraphQLArgument.Builder()
			.name("database")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument sqlArgument = new GraphQLArgument.Builder()
			.name("sql")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument skipArgument = new GraphQLArgument.Builder()
			.name("skip")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument limitArgument = new GraphQLArgument.Builder()
			.name("limit")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument arrayArgument = new GraphQLArgument.Builder()
			.name("array")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument p0Argument = new GraphQLArgument.Builder()
			.name("p0")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument p1Argument = new GraphQLArgument.Builder()
			.name("p1")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument p2Argument = new GraphQLArgument.Builder()
			.name("p2")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument p3Argument = new GraphQLArgument.Builder()
			.name("p3")
			.type(GraphQLString)
			.build(); 
}
