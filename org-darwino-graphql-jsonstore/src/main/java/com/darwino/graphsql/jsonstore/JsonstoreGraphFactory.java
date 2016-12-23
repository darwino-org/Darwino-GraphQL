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

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphsql.GraphContext;
import com.darwino.graphsql.GraphFactory;
import com.darwino.graphsql.GraphFactory.Builder;
import com.darwino.graphsql.json.GraphJsonAccessor;
import com.darwino.graphsql.json.GraphJsonDataFetcher;
import com.darwino.graphsql.json.JsonGraphFactory;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Session;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

/**
 * Add access to JSON documents coming from the Darwino JSON store.
 * 
 * @author Philippe Riand
 */
public class JsonstoreGraphFactory extends GraphFactory {

	public JsonstoreGraphFactory() {
		super("Json Store");
	}
	
	@Override
	public void extendTypes(Builder builders) {
		// We extend the JSON type with new fields
		GraphQLObjectType.Builder builder = builders.getObjectTypes().get(JsonGraphFactory.JSON_TYPE);
		if(builder!=null) {
			addJsonFields(builder);
		}
	}
	
	@Override
	public void createQuery(Builder builders, GraphQLObjectType.Builder query) {
		addJsonFields(query);
	}

	protected void addJsonFields(GraphQLObjectType.Builder builder) {
		builder
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("Document")
					.argument(databaseArgument)
					.argument(storeArgument)
					.argument(unidArgument)
					.argument(idArgument)
					.type(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE))
					.dataFetcher(documentFecther)
			)
		;
	}
	
	public static class DocumentAccessor extends GraphJsonAccessor {
		
		private Document document;
		
		public DocumentAccessor(Object parent, Document document) {
			super(parent);
			this.document = document;
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			return path.readValue(document.getJson());
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return path.readAsList(document.getJson());
		}
	}
	
	public static final class Context {
		
		private Session session;
		private String database;
		private String store;
		
		public Context(Session session) {
			this(session, null, null);
		}
		public Context(Session session, String database) {
			this(session, database, null);
		}
		public Context(Session session, String database, String store) {
			this.session = session;
			this.database = database;
			this.store = store;
		}
		
		public Session getSession() {
			return session;
		}
		public String getDatabase() {
			return database;
		}
		public String getStore() {
			return store;
		}
	}

	public static class DocumentFecther extends GraphJsonDataFetcher {
		public DocumentFecther() {
		}
		@Override
		public DocumentAccessor get(DataFetchingEnvironment environment) {
			try {
				Context ctx = (Context)((GraphContext)environment.getContext()).get(Context.class);
				if(ctx==null) {
					return null;
				}

				Session session = ctx.getSession();
				if(session==null) {
					return null;
				}
				
				String database = getStringParameter(environment,"database");
				if(StringUtil.isEmpty(database)) {
					database = ctx.getDatabase();
					if(StringUtil.isEmpty(database)) {
						return null;
					}
				}
				String store = getStringParameter(environment,"store");
				if(StringUtil.isEmpty(store)) {
					store = ctx.getStore();
					if(StringUtil.isEmpty(store)) {
						return null;
					}
				}

				Document doc = null;
				
				String unid = getStringParameter(environment,"unid");
				if(StringUtil.isNotEmpty(unid)) {
					doc = session.getDatabase(database).getStore(store).loadDocument(unid);
				} else {
					// More with requests...
					return null;
				}
				
				return new DocumentAccessor(environment.getSource(),doc);
			} catch(Exception ex) {
				return null;
			}
		}
	};
	public static DocumentFecther documentFecther = new DocumentFecther();
	
	
	public static GraphQLArgument databaseArgument = new GraphQLArgument.Builder()
		.name("database")
		.type(GraphQLString)
		.build(); 
	public static GraphQLArgument storeArgument = new GraphQLArgument.Builder()
		.name("store")
		.type(GraphQLString)
		.build(); 
	public static GraphQLArgument unidArgument = new GraphQLArgument.Builder()
		.name("unid")
		.type(GraphQLString)
		.build(); 
	public static GraphQLArgument idArgument = new GraphQLArgument.Builder()
		.name("id")
		.type(GraphQLString)
		.build(); 
}
