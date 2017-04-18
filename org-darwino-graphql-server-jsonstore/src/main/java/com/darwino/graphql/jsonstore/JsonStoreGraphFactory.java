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

package com.darwino.graphql.jsonstore;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;

import java.util.ArrayList;
import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonJavaFactory;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphql.GraphContext;
import com.darwino.graphql.GraphFactory;
import com.darwino.graphql.factories.DynamicObjectGraphFactory;
import com.darwino.graphql.model.BaseDataFetcher;
import com.darwino.graphql.model.ObjectAccessor;
import com.darwino.graphql.model.ObjectDataFetcher;
import com.darwino.jsonstore.Cursor;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.callback.CursorEntry;
import com.darwino.jsonstore.callback.CursorHandler;
import com.darwino.jsonstore.callback.DocumentHandler;
import com.darwino.jsonstore.query.nodes.SpecialFieldNode;

import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

/**
 * Add access to JSON documents coming from the Darwino JSON store.
 * 
 * @author Philippe Riand
 */
public class JsonStoreGraphFactory extends GraphFactory {

	public static class Context {
		public static Context get(DataFetchingEnvironment environment) {
			Context ctx = ((GraphContext)environment.getContext()).get(Context.class);
			if(ctx==null) {
				throw new GraphQLException(StringUtil.format("Missing JSON database context"));
			}
			return ctx;
		}

		private Session session;
		private String database;
		
		public Context(Session session) {
			this(session, null);
		}
		public Context(Session session, String database) {
			this.session = session;
			this.database = database;
		}
		
		public Session getSession() {
			return session;
		}
		public String getDatabase() {
			return database;
		}
	}
	
	
	public static final String TYPE_DOCUMENT 	= "DwoDbDocument";
	public static final String TYPE_ENTRY 		= "DwoDbEntry";
	
	public JsonStoreGraphFactory() {
	}

	@Override
	public void createTypes(Builder builders) {
		{
			GraphQLObjectType.Builder docBuilder = GraphQLObjectType.newObject()
				.name(TYPE_DOCUMENT)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.ID)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.ID,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.UNID)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.UNID,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.STORE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.STORE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.PSTORE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.PSTORE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.PARENT)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.PARENT,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.CREATEDATE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.CREATEDATE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.CREATEUSER)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.CREATEUSER,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.LASTMODDATE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.LASTMODDATE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.LASTMODUSER)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.LASTMODUSER,JsonUtil.TYPE_STRING))
				);
			builders.addDynamicFields(docBuilder);
			builders.put(TYPE_DOCUMENT,docBuilder);
		}
		{
			GraphQLObjectType.Builder entBuilder = GraphQLObjectType.newObject()
				.name(TYPE_ENTRY)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.ID)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.ID,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.UNID)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.UNID,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.STORE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.STORE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.PSTORE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.PSTORE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.PARENT)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.PARENT,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.CREATEDATE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.CREATEDATE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.CREATEUSER)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.CREATEUSER,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.LASTMODDATE)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.LASTMODDATE,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.LASTMODUSER)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.LASTMODUSER,JsonUtil.TYPE_STRING))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.POSITION)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.POSITION,JsonUtil.TYPE_INT))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.HIERLEVEL)
					.type(GraphQLInt)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.HIERLEVEL,JsonUtil.TYPE_INT))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.ISCATEGORY)
					.type(GraphQLBoolean)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.ISCATEGORY,JsonUtil.TYPE_BOOLEAN))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.CATEGORYCOUNT)
					.type(GraphQLInt)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.CATEGORYCOUNT,JsonUtil.TYPE_INT))
				)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SpecialFieldNode.INDENTLEVEL)
					.type(GraphQLInt)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SpecialFieldNode.INDENTLEVEL,JsonUtil.TYPE_INT))
				);
			builders.addDynamicFields(entBuilder);
			builders.put(TYPE_ENTRY,entBuilder);
		}
	}
	
	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("Document")
					.argument(DocumentAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_DOCUMENT))
					.dataFetcher(new DocumentFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorEntry")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_ENTRY))
					.dataFetcher(new CursorEntryFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorEntries")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(TYPE_ENTRY)))
					.dataFetcher(new CursorEntriesFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorDocument")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_DOCUMENT))
					.dataFetcher(new CursorDocumentFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorDocuments")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(TYPE_DOCUMENT)))
					.dataFetcher(new CursorDocumentsFetcher())
			)
		;
	}
		
	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Data accessors
	//
	/////////////////////////////////////////////////////////////////////////////////
	
	public static class DocumentAccessor extends ObjectAccessor<Document> {
		
		public DocumentAccessor(DataFetchingEnvironment env, Document document) {
			super(env,document);
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			String sysField = asSystemField(path);
			if(sysField!=null) {
				if(sysField.equals(SpecialFieldNode.ID)) {
					return getValue().getDocId();
				}
				if(sysField.equals(SpecialFieldNode.UNID)) {
					return getValue().getUnid();
				}
				if(sysField.equals(SpecialFieldNode.STORE)) {
					return getValue().getStore().getId();
				}
				if(sysField.equals(SpecialFieldNode.PSTORE)) {
					String id = getValue().getParentUnid();
					if(StringUtil.isNotEmpty(id)) {
						int pos = id.indexOf(Document.STORE_UNID_SEP);
						return pos>=0 ? id.substring(pos+1) : getValue().getStore().getId();
					}
					return null;
				}
				if(sysField.equals(SpecialFieldNode.PARENT)) {
					String id = getValue().getParentUnid();
					if(StringUtil.isNotEmpty(id)) {
						int pos = id.indexOf(Document.STORE_UNID_SEP);
						return pos>=0 ? id.substring(0,pos) : id;
					}
					return null;
				}
				if(sysField.equals(SpecialFieldNode.CREATEDATE)) {
					return JsonUtil.asString(getValue().getCreationDate(),getValue().getDatabase().getJavaTimeZone());
				}
				if(sysField.equals(SpecialFieldNode.CREATEUSER)) {
					return getValue().getCreationUser();
				}
				if(sysField.equals(SpecialFieldNode.LASTMODDATE)) {
					return JsonUtil.asString(getValue().getLastModificationDate(),getValue().getDatabase().getJavaTimeZone());
				}
				if(sysField.equals(SpecialFieldNode.LASTMODUSER)) {
					return getValue().getLastModificationUser();
				}
			}
			return path.readValue(getValue().getJson());
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return path.readAsList(getValue().getJson());
		}
		
		protected String asSystemField(JsonPath path) throws JsonException {
			Object[] parts = path.getSimpleParts();
			if(parts.length==1 && parts[0] instanceof String) {
				String field = (String)parts[0];
				if(field.startsWith(Document.SYSTEM_PREFIX)) {
					return field;
				}
			}
			return null;
		}
		
		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(databaseArgument);
			args.add(storeArgument);
			args.add(unidArgument);
			args.add(idArgument);
			return args;
		}
	}

	public static class CursorEntryAccessor extends ObjectAccessor<CursorEntry> {
		
		public CursorEntryAccessor(DataFetchingEnvironment env, CursorEntry entry) {
			super(env,entry);
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			String sysField = asSystemField(path);
			if(sysField!=null) {
				// Documents related fields
				if(sysField.equals(SpecialFieldNode.ID)) {
					return getValue().getDocId();
				}
				if(sysField.equals(SpecialFieldNode.UNID)) {
					return getValue().getUnid();
				}
				if(sysField.equals(SpecialFieldNode.STORE)) {
					return getValue().getStore().getId();
				}
				if(sysField.equals(SpecialFieldNode.PSTORE)) {
					String id = getValue().getParentUnid();
					if(StringUtil.isNotEmpty(id)) {
						int pos = id.indexOf(Document.STORE_UNID_SEP);
						return pos>=0 ? id.substring(pos+1) : getValue().getStore().getId();
					}
					return null;
				}
				if(sysField.equals(SpecialFieldNode.PARENT)) {
					String id = getValue().getParentUnid();
					if(StringUtil.isNotEmpty(id)) {
						int pos = id.indexOf(Document.STORE_UNID_SEP);
						return pos>=0 ? id.substring(0,pos) : id;
					}
					return null;
				}
				if(sysField.equals(SpecialFieldNode.CREATEDATE)) {
					return JsonUtil.asString(getValue().getCreationDate(),getValue().getDatabase().getJavaTimeZone());
				}
				if(sysField.equals(SpecialFieldNode.CREATEUSER)) {
					return getValue().getCreationUser();
				}
				if(sysField.equals(SpecialFieldNode.LASTMODDATE)) {
					return JsonUtil.asString(getValue().getLastModificationDate(),getValue().getDatabase().getJavaTimeZone());
				}
				if(sysField.equals(SpecialFieldNode.LASTMODUSER)) {
					return getValue().getLastModificationUser();
				}
				// View entry specific
				if(sysField.equals(SpecialFieldNode.POSITION)) {
					return getValue().getPosition();
				}
				if(sysField.equals(SpecialFieldNode.HIERLEVEL)) {
					return getValue().getHierarchicalLevel();
				}
				if(sysField.equals(SpecialFieldNode.ISCATEGORY)) {
					return getValue().isCategory();
				}
				if(sysField.equals(SpecialFieldNode.CATEGORYCOUNT)) {
					return getValue().getCategoryCount();
				}
				if(sysField.equals(SpecialFieldNode.INDENTLEVEL)) {
					return getValue().getIndentLevel();
				}
			}
			
			return path.readValue(getValue().getValue());
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return path.readAsList(getValue().getValue());
		}
		
		protected String asSystemField(JsonPath path) throws JsonException {
			Object[] parts = path.getSimpleParts();
			if(parts.length==1 && parts[0] instanceof String) {
				String field = (String)parts[0];
				if(field.startsWith(Document.SYSTEM_PREFIX)) {
					return field;
				}
			}
			return null;
		}

		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(databaseArgument);
			args.add(storeArgument);
			args.add(indexArgument);
			args.add(ftSearchArgument);
			args.add(parentIdArgument);
			args.add(keyArgument);
			args.add(partialKeyArgument);
			args.add(tagArgument);
			args.add(startKeyArgument);
			args.add(excludeStartArgument);
			args.add(endKeyArgument);
			args.add(excludeEndArgument);
			args.add(skipArgument);
			args.add(limitArgument);
			args.add(hierarchicalArgument);
			args.add(categoryCountArgument);
			args.add(categoryStartArgument);
			args.add(descendingArgument);
			args.add(orderByArgument);
			args.add(optionsArgument);
			args.add(queryArgument);
			args.add(extractArgument);
			args.add(aggregateArgument);
			args.add(subqueriesArgument);
			args.add(p0Argument);
			args.add(p1Argument);
			args.add(p2Argument);
			args.add(p3Argument);
			return args;
		}
	}

	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Document fetcher
	//
	/////////////////////////////////////////////////////////////////////////////////
	
	public static class DocumentFetcher extends ObjectDataFetcher<Document> {
		public DocumentFetcher() {
		}
		@Override
		public DocumentAccessor get(DataFetchingEnvironment environment) {
			try {
				Context ctx = ((GraphContext)environment.getContext()).get(Context.class);
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
					if(StringUtil.isEmpty(database)) {
						throw new GraphQLException(StringUtil.format("Missing JSON database reference"));
					}
				}

				Document doc = null;
				
				String unid = getStringParameter(environment,"unid");
				if(StringUtil.isNotEmpty(unid)) {
					String store = getStringParameter(environment,"store");
					if(StringUtil.isEmpty(store)) {
						return null;
					}
					try {
						doc = session.getDatabase(database).getStore(store).loadDocument(unid);
					} catch(JsonException ex) {
					}
				} else {
					int id = getIntParameter(environment,"id");
					if(id!=0) {
						try {
							doc = session.getDatabase(database).loadDocumentById(id);
						} catch(JsonException ex) {
						}
					}
				}
				
				return doc!=null ? new DocumentAccessor(environment,doc) : null;
			} catch(Exception ex) {
				if(ex instanceof GraphQLException) {
					throw (GraphQLException)ex;
				}
				throw new GraphQLException(StringUtil.format("Error while loading the document, {0}",ex.getLocalizedMessage()),ex);
			}
		}
	};

	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Cursor accessor
	//
	/////////////////////////////////////////////////////////////////////////////////

	public static abstract class BaseCursorFetcher<T> extends BaseDataFetcher<T> {
		
		public static final int DEFAULT_LIMIT		= 100;
		public static final int MAX_LIMIT			= 500;
		
		public BaseCursorFetcher() {
		}
		
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				Context ctx = Context.get(environment);

				Session session = ctx.getSession();
				if(session==null) {
					throw new GraphQLException(StringUtil.format("Missing JSON session in the database context"));
				}
				
				String database = getStringParameter(environment,"database");
				if(StringUtil.isEmpty(database)) {
					database = ctx.getDatabase();
					if(StringUtil.isEmpty(database)) {
						throw new GraphQLException(StringUtil.format("Missing JSON database reference"));
					}
				}
				String store = getStringParameter(environment,"store");
				String index = getStringParameter(environment,"index");

				
				Cursor c;
				if(index!=null) {
					c = session.getDatabase(database).getStore(store).getIndex(index).openCursor();
				} else {
					if(store!=null) {
						c = session.getDatabase(database).getStore(store).openCursor();
					} else {
						c = session.getDatabase(database).openCursor();
					}
				}
				initCursor(environment,c);

				return createAccessor(environment,c);
			} catch(Exception ex) {
				throw new GraphQLException(StringUtil.format("Error while executing the JSON query, {0}",ex.getLocalizedMessage()),ex);
			}
		}
		protected abstract Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException;

		protected Cursor initCursor(DataFetchingEnvironment env, Cursor c) throws JsonException {
			String ftsearch = getStringParameter(env,"ftsearch");
			if(StringUtil.isNotEmpty(ftsearch)) {
				c.ftSearch(ftsearch);
			}
			
			int id = getIntParameter(env,"id");
			if(id!=0) {
				c.id(id);
			}
			
			String unid = getStringParameter(env,"unid");
			if(StringUtil.isNotEmpty(unid)) {
				c.unid(unid);
			}
			
			String parentId = getStringParameter(env,"parentid");
			if(StringUtil.isNotEmpty(parentId)) {
				c.parentUnid(parentId);
			}
			
			String key = getStringParameter(env,"key");
			if(StringUtil.isNotEmpty(key)) {
				c.key(keyFromString(key));
			}
			
			String partialKey = getStringParameter(env,"partialkey");
			if(StringUtil.isNotEmpty(partialKey)) {
				c.partialKey(keyFromString(partialKey));
			}
			
			String tags = getStringParameter(env,"tags");
			if(StringUtil.isNotEmpty(tags)) {
				c.tags(StringUtil.splitString(tags,','));
			}
			
			String startKey = getStringParameter(env,"startkey");
			if(StringUtil.isNotEmpty(startKey)) {
				c.startKey(keyFromString(startKey),getBooleanParameter(env,"excludestart"));
			}
			
			String endKey = getStringParameter(env,"endkey");
			if(StringUtil.isNotEmpty(endKey)) {
				c.endKey(keyFromString(endKey),getBooleanParameter(env,"excludeend"));
			}

			int skip = getIntParameter(env,"skip");
			if(skip<0) skip = 0;
			int limit = getIntParameter(env,"limit");
			if(limit<0) limit = DEFAULT_LIMIT;
			c.range(skip,limit);
			
			int hierarchical = getIntParameter(env,"hierarchical"); 
			if(hierarchical>0) {
				c.hierarchical(hierarchical);
			}
			
			// Not documented yet as this is
			String hIndexes = getStringParameter(env,"hierarchysources"); 
			if(StringUtil.isNotEmpty(hIndexes)) {
				c.hierarchySources(StringUtil.splitString(hIndexes, ','));
			}

			int categoryCount = getIntParameter(env,"categorycount");
			int categoryStart = getIntParameter(env,"categorystart");
			if(categoryCount!=0 || categoryStart!=0) {
				c.categories(categoryCount,categoryStart);
			}
			if(getBooleanParameter(env,"descending")) {
				c.descending();
			}

			String orderBy = getStringParameter(env,"orderby"); 
			if(StringUtil.isNotEmpty(orderBy)) {
				c.orderBy(StringUtil.splitString(orderBy, ',', true));
			}
			
			String options = getStringParameter(env,"options");
			if(StringUtil.isNotEmpty(options)) {
				c.options(optionsFromString(options));
			}
			
			String query = getStringParameter(env,"query");
			if(StringUtil.isNotEmpty(query)) {
				c.query(query);
				addParam(env, c, "p0");
				addParam(env, c, "p1");
				addParam(env, c, "p2");
				addParam(env, c, "p3");
			}
			
			String extract = getStringParameter(env,"extract");
			if(StringUtil.isNotEmpty(extract)) {
				c.extract(extract);
			}
			
			String agg = getStringParameter(env,"aggregate");
			if(StringUtil.isNotEmpty(agg)) {
				c.aggregate(agg);
			}
			
			String subQueries = getStringParameter(env,"subqueries");
			if(StringUtil.isNotEmpty(subQueries)) {
				c.subQueries(subQueries);
			}
			
			return c;
		}
		private void addParam(DataFetchingEnvironment env, Cursor c, String name) throws JsonException {
			Object value = getStringParameter(env, name);
			if(value!=null) {
				c.param(name, value);
			}
		}
		private int optionsFromString(String s) {
			// If it is a number, then just parse it
			Number n = JsonUtil.parseNumber(s);
			if(n!=null) {
				return n.intValue();
			}
			// Else, interpret the string
			int options = 0;
			String[] ss = StringUtil.splitString(s, '|', true);
			for(int i=0; i<ss.length; i++) {
				String o = ss[i];
				if(o.equals("DATA_VALUE")) {
					options |= Cursor.DATA_VALUE;
				} else if(o.equals("DATA_NOVALUE")) {
					options |= Cursor.DATA_NOVALUE;
				} else if(o.equals("DATA_DOCUMENT")) {
					options |= Cursor.DATA_DOCUMENT;
				} else if(o.equals("DATA_CATONLY")) {
					options |= Cursor.DATA_CATONLY;
				} else if(o.equals("DATA_MODDATES")) {
					options |= Cursor.DATA_MODDATES;
				} else if(o.equals("DATA_READMARK")) {
					options |= Cursor.DATA_READMARK;
				} else if(o.equals("DATA_WRITEACC")) {
					options |= Cursor.DATA_WRITEACC;
				} else if(o.equals("HIERARCHY_NOSQL")) {
					options |= Cursor.HIERARCHY_NOSQL;
				} else if(o.equals("QUERY_NOSQL")) {
					options |= Cursor.QUERY_NOSQL;
				} else if(o.equals("RANGE_ROOT")) {
					options |= Cursor.RANGE_ROOT;
				} else if(o.equals("TAGS_OR")) {
					options |= Cursor.TAGS_OR;
				}
			}
			return options;
		}
		private static Object keyFromString(String key) throws JsonException {
			// This can be a json string or a simple one
			try {
				return JsonJavaFactory.instance.fromJson(key);
			} catch(JsonException ex) {
				return key;
			}
		}
	};
	
	public static class CursorEntryFetcher extends BaseCursorFetcher<Object> {
		public CursorEntryFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			CursorEntry entry = c.findOne();
			return entry!=null ? new CursorEntryAccessor(environment, entry) : null;
		}
	};
	public static class CursorEntriesFetcher extends BaseCursorFetcher<Object> {
		public CursorEntriesFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			final List<CursorEntryAccessor> entries = new ArrayList<CursorEntryAccessor>();
			c.find(new CursorHandler() {
				@Override
				public boolean handle(CursorEntry entry) throws JsonException {
					entries.add(new CursorEntryAccessor(environment,entry));
					return true;
				}
			});
			return entries;
		}
	};
	
	public static class CursorDocumentFetcher extends BaseCursorFetcher<Object> {
		public CursorDocumentFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			Document doc = c.findOneDocument();
			return doc!=null ? new DocumentAccessor(environment, doc) : null;
		}
	};
	public static class CursorDocumentsFetcher extends BaseCursorFetcher<Object> {
		public CursorDocumentsFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			final List<DocumentAccessor> entries = new ArrayList<DocumentAccessor>();
			c.findDocuments(new DocumentHandler() {
				@Override
				public boolean handle(Document doc) throws JsonException {
					entries.add(new DocumentAccessor(environment,doc));
					return true;
				}
			});
			return entries;
		}
	};
	
	
	// Common
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


	// Cursor specific
	public static GraphQLArgument indexArgument = new GraphQLArgument.Builder()
			.name("index")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument ftSearchArgument = new GraphQLArgument.Builder()
			.name("ftsearch")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument parentIdArgument = new GraphQLArgument.Builder()
			.name("parentid")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument keyArgument = new GraphQLArgument.Builder()
			.name("key")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument partialKeyArgument = new GraphQLArgument.Builder()
			.name("query")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument tagArgument = new GraphQLArgument.Builder()
			.name("tag")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument startKeyArgument = new GraphQLArgument.Builder()
			.name("startkey")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument excludeStartArgument = new GraphQLArgument.Builder()
			.name("excludestart")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument endKeyArgument = new GraphQLArgument.Builder()
			.name("endkey")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument excludeEndArgument = new GraphQLArgument.Builder()
			.name("excludeend")
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
	public static GraphQLArgument hierarchicalArgument = new GraphQLArgument.Builder()
			.name("hierarchical")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument categoryCountArgument = new GraphQLArgument.Builder()
			.name("categorycount")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument categoryStartArgument = new GraphQLArgument.Builder()
			.name("categorystart")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument descendingArgument = new GraphQLArgument.Builder()
			.name("descending")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument orderByArgument = new GraphQLArgument.Builder()
			.name("orderby")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument optionsArgument = new GraphQLArgument.Builder()
			.name("options")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument queryArgument = new GraphQLArgument.Builder()
			.name("query")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument extractArgument = new GraphQLArgument.Builder()
			.name("extract")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument aggregateArgument = new GraphQLArgument.Builder()
			.name("aggregate")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument subqueriesArgument = new GraphQLArgument.Builder()
			.name("subqueries")
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
