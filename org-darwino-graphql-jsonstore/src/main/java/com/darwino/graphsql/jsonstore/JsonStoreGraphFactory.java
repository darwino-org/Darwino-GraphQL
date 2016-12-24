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
import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonJavaFactory;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphsql.GraphContext;
import com.darwino.graphsql.GraphFactory;
import com.darwino.graphsql.factories.json.JsonGraphFactory;
import com.darwino.graphsql.model.BaseDataFetcher;
import com.darwino.graphsql.model.ObjectAccessor;
import com.darwino.graphsql.model.ObjectDataFetcher;
import com.darwino.jsonstore.Cursor;
import com.darwino.jsonstore.Document;
import com.darwino.jsonstore.Session;
import com.darwino.jsonstore.callback.CursorEntry;
import com.darwino.jsonstore.callback.CursorHandler;
import com.darwino.jsonstore.callback.DocumentHandler;
import com.darwino.jsonstore.query.nodes.SpecialFieldNode;

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

	public JsonStoreGraphFactory() {
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
					.argument(DocumentAccessor.getArguments())
					.type(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE))
					.dataFetcher(new DocumentFecther())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorEntry")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE))
					.dataFetcher(new CursorEntryFecther())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorEntries")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE)))
					.dataFetcher(new CursorEntriesFecther())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorDocument")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE))
					.dataFetcher(new CursorDocumentFecther())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("CursorDocuments")
					.argument(CursorEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(JsonGraphFactory.JSON_TYPE)))
					.dataFetcher(new CursorDocumentsFecther())
			)
		;
	}
	
	public static final class Context {

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
			return args;
		}
	}

	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Document fetcher
	//
	/////////////////////////////////////////////////////////////////////////////////
	
	public static class DocumentFecther extends ObjectDataFetcher<Document> {
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

				Document doc = null;
				
				String unid = getStringParameter(environment,"unid");
				if(StringUtil.isNotEmpty(unid)) {
					String store = getStringParameter(environment,"store");
					if(StringUtil.isEmpty(store)) {
						return null;
					}
					doc = session.getDatabase(database).getStore(store).loadDocument(unid);
				} else {
					int id = getIntParameter(environment,"id");
					if(id!=0) {
						doc = session.getDatabase(database).loadDocumentById(id);
					}
				}
				
				return doc!=null ? new DocumentAccessor(environment,doc) : null;
			} catch(Exception ex) {
				return null;
			}
		}
	};

	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Cursor accessor
	//
	/////////////////////////////////////////////////////////////////////////////////

	public static abstract class BaseCursorFecther<T> extends BaseDataFetcher<T> {
		
		public static final int DEFAULT_LIMIT		= 100;
		public static final int MAX_LIMIT			= 500;
		
		public BaseCursorFecther() {
		}
		
		@Override
		public Object get(DataFetchingEnvironment environment) {
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
				return null;
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
	
	public static class CursorEntryFecther extends BaseCursorFecther<Object> {
		public CursorEntryFecther() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			CursorEntry entry = c.findOne();
			return entry!=null ? new CursorEntryAccessor(environment, entry) : null;
		}
	};
	public static class CursorEntriesFecther extends BaseCursorFecther<Object> {
		public CursorEntriesFecther() {
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
	
	public static class CursorDocumentFecther extends BaseCursorFecther<Object> {
		public CursorDocumentFecther() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, Cursor c) throws JsonException {
			Document doc = c.findOneDocument();
			return doc!=null ? new DocumentAccessor(environment, doc) : null;
		}
	};
	public static class CursorDocumentsFecther extends BaseCursorFecther<Object> {
		public CursorDocumentsFecther() {
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
}
