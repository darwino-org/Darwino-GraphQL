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

package com.darwino.graphql.domino;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonJavaFactory;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphql.GraphContext;
import com.darwino.graphql.GraphFactory;
import com.darwino.graphql.model.BaseDataFetcher;
import com.darwino.graphql.model.ObjectAccessor;
import com.darwino.graphql.model.ObjectDataFetcher;

import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewColumn;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

/**
 * Add access to JSON documents coming from the Darwino JSON store.
 * 
 * @author Philippe Riand
 */
public class DominoGraphFactory extends GraphFactory {

	public static class Context {
		public static Context get(DataFetchingEnvironment environment) {
			Context ctx = ((GraphContext)environment.getContext()).get(Context.class);
			if(ctx==null) {
				throw new GraphQLException(StringUtil.format("Missing Domino database context"));
			}
			return ctx;
		}

		private Session session;
		private String defaultDatabase;
		
		public Context(Session session) {
			this(session, null);
		}
		public Context(Session session, String defaultDatabase) {
			this.session = session;
			this.defaultDatabase = defaultDatabase;
		}
		
		public Session getSession() {
			return session;
		}
		public String getDefaultDatabase() {
			return defaultDatabase;
		}
		
		public Database getDatabase(String server, String database) throws NotesException, GraphQLException {
			Database db = null;
			if(StringUtil.isEmpty(server)) {
				if(StringUtil.isEmpty(database)) {
					if(StringUtil.isEmpty(defaultDatabase)) {
						throw new GraphQLException(StringUtil.format("Missing Domino database reference"));
					}
					database  = defaultDatabase;
				}
				String localPath = getLocalDatabasePath(database);
				db = session.getDatabase(null, localPath, false);
			} else {
				if(StringUtil.isEmpty(database)) {
					throw new GraphQLException(StringUtil.format("Missing Domino database reference"));
				}
				db = session.getDatabase(server, database, false);
			}
			if(db==null) {
				throw new GraphQLException(StringUtil.format("Cannot open database {0},{1}",server,database));
			}
			return db;
		}
		
		protected String getLocalDatabasePath(String database) {
			return database;
		}
	}

	public static enum SPECIAL_FIELD {
	    // Common attributes names
		UNID("unid"), //$NON-NLS-1$   
		NOTEID("noteId"), //$NON-NLS-1$
        // View attributes names
    	POSITION("position"), //$NON-NLS-1$
    	READ("read"), //$NON-NLS-1$
    	SIBLINGCOUNT("siblingCount"), //$NON-NLS-1$
    	DESCENDANTCOUNT("descendantCount"), //$NON-NLS-1$
    	CHILDCOUNT("childCount"), //$NON-NLS-1$
    	INDENTLEVEL("indentLevel"), //$NON-NLS-1$
    	ISCATEGORY("isCategory"), //$NON-NLS-1$
    	// Document attributes names
    	PARENTUNID("parentUnid"), //$NON-NLS-1$
    	CREATED("created"), //$NON-NLS-1$
    	LASTMODIFIED("lastModified"), //$NON-NLS-1$
    	LASTACCESSED("lastAccessed") //$NON-NLS-1$
		;
		private String name;
		private SPECIAL_FIELD(String name) {
			this.name = name;
		}
	};	
	
	public static final String TYPE_DOCUMENT 	= "DominoDocument";
	public static final String TYPE_ENTRY 		= "DominoEntry";
	
	public DominoGraphFactory() {
	}

	@Override
	public void createTypes(Builder builders) {
		{
			// Documents
			GraphQLObjectType.Builder docBuilder = GraphQLObjectType.newObject()
				.name(TYPE_DOCUMENT)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.UNID.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.UNID))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.NOTEID.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.NOTEID))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.PARENTUNID.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.PARENTUNID))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.CREATED.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.CREATED))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.LASTMODIFIED.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.LASTMODIFIED))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.LASTACCESSED.name)
						.type(GraphQLString)
						.dataFetcher(new DocumentSystemValueFetcher(SPECIAL_FIELD.LASTACCESSED))
					)
			;
			builders.addDynamicFields(docBuilder);
			builders.put(TYPE_DOCUMENT,docBuilder);
		}
		{
			// View entries
			GraphQLObjectType.Builder entBuilder = GraphQLObjectType.newObject()
				.name(TYPE_ENTRY)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.UNID.name)
						.type(GraphQLString)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.UNID))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.NOTEID.name)
						.type(GraphQLString)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.NOTEID))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.POSITION.name)
						.type(GraphQLString)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.POSITION))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.READ.name)
						.type(GraphQLBoolean)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.READ))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.SIBLINGCOUNT.name)
						.type(GraphQLInt)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.SIBLINGCOUNT))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.DESCENDANTCOUNT.name)
						.type(GraphQLInt)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.DESCENDANTCOUNT))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.CHILDCOUNT.name)
						.type(GraphQLInt)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.CHILDCOUNT))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.INDENTLEVEL.name)
						.type(GraphQLInt)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.INDENTLEVEL))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.ISCATEGORY.name)
						.type(GraphQLBoolean)
						.dataFetcher(new ViewEntrySystemValueFetcher(SPECIAL_FIELD.ISCATEGORY))
					)
			;
			builders.addDynamicFields(entBuilder);
			builders.put(TYPE_ENTRY,entBuilder);
		}
	}
	
	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("DominoDocument")
					.argument(DocumentAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_DOCUMENT))
					.dataFetcher(new DocumentFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("DominoViewEntry")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_ENTRY))
					.dataFetcher(new ViewEntryFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("DominoViewEntries")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(TYPE_ENTRY)))
					.dataFetcher(new ViewEntriesFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("DominoViewDocument")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_DOCUMENT))
					.dataFetcher(new ViewDocumentFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("DominoViewDocuments")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(TYPE_DOCUMENT)))
					.dataFetcher(new ViewDocumentsFetcher())
			)
		;
	}
		
	
	/////////////////////////////////////////////////////////////////////////////////
	//
	// Data accessors
	//
	/////////////////////////////////////////////////////////////////////////////////

	private static Object convertFromDomino(Object val) throws JsonException, NotesException {
		if(val instanceof String || val instanceof Number || val instanceof Boolean) {
			return val;
		}
		if(val instanceof DateTime) {
			DateTime dt = (DateTime)val;
			String s = JsonUtil.dateToString(dt.toJavaDate());
			dt.recycle();
			return s;
		}
		return null;
	}
	private static Object convertVectorFromDomino(Vector<?> val) throws JsonException, NotesException {
		if(val!=null) {
			if(val.size()==1) {
				return convertFromDomino(val.get(0));
			}
			if(val.size()>=2) {
				JsonArray a = new JsonArray();
				for(int i=0; i<val.size(); i++) {
					a.add(convertFromDomino(val.get(i)));
				}
				return a;
			}
			
		}
		return null;
	}
	
	public static abstract class DominoAccessor<T> extends ObjectAccessor<T> {
		public DominoAccessor(DataFetchingEnvironment env, T value) {
			super(env,value);
		}
		
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			Object o = readValue(path);
			return o instanceof JsonArray ? (JsonArray)o : JsonArray.of(o);
		}
	}
	
	public static class DocumentSystemValueFetcher implements DataFetcher<Object> {
		private SPECIAL_FIELD field;
		public DocumentSystemValueFetcher(SPECIAL_FIELD field) {
			this.field = field;
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				Document doc = ((DocumentAccessor)environment.getSource()).getValue();
				switch(field) {
				    // Common attributes names
					case UNID:				return convertFromDomino(doc.getUniversalID());   
					case NOTEID:			return convertFromDomino(doc.getNoteID());
			    	// Document attributes names
					case PARENTUNID:		return convertFromDomino(doc.getParentDocumentUNID());
					case CREATED:			return convertFromDomino(JsonUtil.dateToString(doc.getCreated().toJavaDate()));
					case LASTMODIFIED:		return convertFromDomino(JsonUtil.dateToString(doc.getLastModified().toJavaDate()));
					case LASTACCESSED:		return convertFromDomino(JsonUtil.dateToString(doc.getLastAccessed().toJavaDate()));
					default:		throw new JsonException(null,"The system field {0} is invalid in this context", field);
				}
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
	}
	public static class DocumentAccessor extends DominoAccessor<Document> {
		
		public DocumentAccessor(DataFetchingEnvironment env, Document document) {
			super(env,document);
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			try {
				String simpleField = getSimpleField(path);
				if(simpleField==null) {
					throw new JsonException(null,"The path {0} does not reference a simple field",path);
				}
				return convertVectorFromDomino(getValue().getItemValue(simpleField));
			} catch(NotesException ex) {
				throw new JsonException(ex);
			}
		}
		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(serverArgument);
			args.add(databaseArgument);
			args.add(unidArgument);
			args.add(noteIdArgument);
			// Future extension for a collection of documents?
			// Another solution is to have a "children" property at the document level
			//args.add(parentUnidArgument);
			return args;
		}
	}

	public static class ViewEntrySystemValueFetcher implements DataFetcher<Object> {
		private SPECIAL_FIELD field;
		public ViewEntrySystemValueFetcher(SPECIAL_FIELD field) {
			this.field = field;
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				ViewEntry ve = ((ViewEntryAccessor)environment.getSource()).getValue();
				switch(field) {
				    // Common attributes names
					case UNID:				return convertFromDomino(ve.getUniversalID());   
					case NOTEID:			return convertFromDomino(ve.getNoteID());
			        // View attributes names
					case POSITION:			return convertFromDomino(ve.getPosition('.'));
					case READ:				return convertFromDomino(ve.getRead());
					case SIBLINGCOUNT:		return convertFromDomino(ve.getSiblingCount());
					case DESCENDANTCOUNT:	return convertFromDomino(ve.getDescendantCount());
					case CHILDCOUNT:		return convertFromDomino(ve.getChildCount());
					case INDENTLEVEL:		return convertFromDomino(ve.getIndentLevel());
					case ISCATEGORY:		return convertFromDomino(ve.isCategory());
					default:				throw new JsonException(null,"The system field {0} is invalid in this context", field);
				}
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
	}
	public static class ViewEntryAccessor extends DominoAccessor<ViewEntry> {
		@SuppressWarnings("unused")
		private ViewNav viewNav;
		private Vector<ViewColumn> columns;
		private Vector<Object> values;
		@SuppressWarnings("unchecked")
		public ViewEntryAccessor(DataFetchingEnvironment env, ViewNav viewNav, ViewEntry entry) throws NotesException {
			super(env,entry);
			this.viewNav = viewNav;
			this.columns = viewNav.view.getColumns();
			this.values = entry.getColumnValues();
		}
		private Object getColumnValue(String name) throws NotesException {
			int sz = columns.size();
			// Programmatic name
	        for(int i=0; i<sz; i++) {
	            String s = columns.get(i).getItemName();
	            if(name.equalsIgnoreCase(s)) {
	                return values.get(i);
	            }
	        }
	        // The column title
	        for(int i=0; i<sz; i++) {
	            String s = columns.get(i).getTitle();
	            if(name.equalsIgnoreCase(s)) {
	                return values.get(i);
	            }
	        }
			return null;
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			try {
				String simpleField = getSimpleField(path);
				if(simpleField==null) {
					throw new JsonException(null,"The path {0} does not reference a simple field",path);
				}
				return convertFromDomino(getColumnValue(simpleField));
			} catch(NotesException ex) {
				throw new JsonException(ex);
			}
		}

		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(serverArgument);
			args.add(databaseArgument);
			args.add(nameArgument);
			args.add(ftSearchArgument);
			args.add(keyArgument);
			args.add(keysArgument);
			args.add(skipArgument);
			args.add(limitArgument);
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
					throw new GraphQLException(StringUtil.format("Missing Domino database context"));
				}

				Session session = ctx.getSession();
				if(session==null) {
					throw new GraphQLException(StringUtil.format("Missing Domino session in the database context"));
				}
				
				String server = getStringParameter(environment,serverArgument.getName());
				String database = getStringParameter(environment,databaseArgument.getName());

				Document doc = null;
				
				String unid = getStringParameter(environment,unidArgument.getName());
				if(StringUtil.isNotEmpty(unid)) {
					doc = ctx.getDatabase(server,database).getDocumentByUNID(unid);
				} else {
					String id = getStringParameter(environment,noteIdArgument.getName());
					doc = ctx.getDatabase(server,database).getDocumentByID(id);
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

	private static abstract class ViewNav {
		
		View view;
		boolean documents;
		int skip;
		int limit;
		String search;
		public ViewNav(View view) {
			this.view = view;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void addEntry(DataFetchingEnvironment environment, List<?> list, ViewEntry ve) throws NotesException {
			if(documents) {
				((List)list).add(new DocumentAccessor(environment,ve.getDocument()));
			} else {
				((List)list).add(new ViewEntryAccessor(environment,this,ve));
			}
		}
			
		public List<?> browse(DataFetchingEnvironment environment) throws NotesException {
			if(documents) {
				final List<DocumentAccessor> documents = new ArrayList<DocumentAccessor>();
				browse(environment, documents);
				return documents;
			} else {
				final List<ViewEntryAccessor> entries = new ArrayList<ViewEntryAccessor>();
				browse(environment, entries);
				return entries;
			}
		}
		
		public abstract void browse(DataFetchingEnvironment environment, List<?> entries) throws NotesException;
	}
	private static class ViewNavAll extends ViewNav {
		public ViewNavAll(View view) {
			super(view);
		}
		@Override
		public void browse(DataFetchingEnvironment environment, List<?> entries) throws NotesException {
			ViewEntryCollection c=getViewEntryCollection();
			if(StringUtil.isNotEmpty(search)) {
				c.FTSearch(search);
			}
			for(ViewEntry ve=c.getFirstEntry(); ve!=null && entries.size()<limit; ve=c.getNextEntry()) {
				if(skip>0) {
					skip--; continue;
				}
				addEntry(environment, entries, ve);
			}
		}
		protected ViewEntryCollection getViewEntryCollection() throws NotesException {
			return view.getAllEntries();
		}
	}
	private static class ViewNavKey extends ViewNavAll {
		private String key;
		public ViewNavKey(View view, String key) {
			super(view);
			this.key = key;
		}
		@Override
		protected ViewEntryCollection getViewEntryCollection() throws NotesException {
			return view.getAllEntriesByKey(key);
		}
	}
	private static class ViewNavKeys extends ViewNavAll {
		@SuppressWarnings("rawtypes")
		private Vector keys = new Vector();
		@SuppressWarnings("unchecked")
		public ViewNavKeys(View view, String _keys) throws JsonException {
			super(view);
			Object val = JsonJavaFactory.instance.fromJson(_keys);
			if(val instanceof JsonArray) {
				JsonArray a = (JsonArray)val;
				for(int i=0; i<a.length(); i++) {
					keys.add(a.get(i));
				}
			} else if(val instanceof JsonObject) {
				throw new JsonException(null,"Object parameter is not allowed");
			} else {
				keys.add(val);
			}
		}
		@Override
		protected ViewEntryCollection getViewEntryCollection() throws NotesException {
			return view.getAllEntriesByKey(keys);
		}
	}
	
	public static abstract class BaseViewFetcher<T> extends BaseDataFetcher<T> {
		public static final int DEFAULT_LIMIT		= 100;
		public static final int MAX_LIMIT			= 500;
		
		public BaseViewFetcher() {
		}
		
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				Context ctx = Context.get(environment);

				Session session = ctx.getSession();
				if(session==null) {
					throw new GraphQLException(StringUtil.format("Missing Domino session in the database context"));
				}
				
				String server = getStringParameter(environment,serverArgument.getName());
				String database = getStringParameter(environment,databaseArgument.getName());

				String viewName = getStringParameter(environment,nameArgument.getName());
				if(viewName==null) {
					throw new GraphQLException(StringUtil.format("Missing 'name' parameter in the query"));
				}

				View view = ctx.getDatabase(server,database).getView(viewName);
				ViewNav viewNav = null;
				
				String key = getStringParameter(environment,keyArgument.getName());
				if(StringUtil.isNotEmpty(key)) {
					viewNav = new ViewNavKey(view, key);
				}
				if(viewNav==null) {
					String keys = getStringParameter(environment,keysArgument.getName());
					if(StringUtil.isNotEmpty(keys)) {
						viewNav = new ViewNavKeys(view, keys);
					}
				}
				if(viewNav==null) {
					viewNav = new ViewNavAll(view);
				}
				
				// Common nav parameters
				viewNav.documents = documents();
				viewNav.skip = getIntParameter(environment,skipArgument.getName());
				viewNav.limit = Math.min(MAX_LIMIT,getIntParameter(environment,limitArgument.getName(),DEFAULT_LIMIT));
				
				return createAccessor(environment,viewNav);
			} catch(Exception ex) {
				throw new GraphQLException(StringUtil.format("Error while executing the JSON query, {0}",ex.getLocalizedMessage()),ex);
			}
		}
		protected abstract boolean documents();
		protected abstract Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException;
	};
	
	public static class ViewEntryFetcher extends BaseViewFetcher<Object> {
		public ViewEntryFetcher() {
		}
		@Override
		protected boolean documents() {
			return false;
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			nav.limit = 1; // Force it
			List<?> e = nav.browse(environment);
			return !e.isEmpty() ? e.get(0) : null; 
		}
	};
	public static class ViewEntriesFetcher extends BaseViewFetcher<Object> {
		public ViewEntriesFetcher() {
		}
		@Override
		protected boolean documents() {
			return false;
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			return nav.browse(environment);
		}
	};
	
	public static class ViewDocumentFetcher extends BaseViewFetcher<Object> {
		public ViewDocumentFetcher() {
		}
		@Override
		protected boolean documents() {
			return true;
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			nav.limit = 1; // Force it
			List<?> e = nav.browse(environment);
			return !e.isEmpty() ? e.get(0) : null; 
		}
	};
	public static class ViewDocumentsFetcher extends BaseViewFetcher<Object> {
		public ViewDocumentsFetcher() {
		}
		@Override
		protected boolean documents() {
			return true;
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			return nav.browse(environment);
		}
	};
	
	
	// Common
	public static GraphQLArgument serverArgument = new GraphQLArgument.Builder()
			.name("server")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument databaseArgument = new GraphQLArgument.Builder()
			.name("database")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument unidArgument = new GraphQLArgument.Builder()
			.name("unid")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument noteIdArgument = new GraphQLArgument.Builder()
			.name("noteId")
			.type(GraphQLString)
			.build(); 

	// Document specific
//	public static GraphQLArgument parentUnidArgument = new GraphQLArgument.Builder()
//			.name("parentUnid")
//			.type(GraphQLString)
//			.build(); 

	// View specific
	public static GraphQLArgument nameArgument = new GraphQLArgument.Builder()
			.name("name")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument ftSearchArgument = new GraphQLArgument.Builder()
			.name("ftsearch")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument keyArgument = new GraphQLArgument.Builder()
			.name("key")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument keysArgument = new GraphQLArgument.Builder()
			.name("keys")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument skipArgument = new GraphQLArgument.Builder()
			.name("skip")
			.type(GraphQLInt)
			.build(); 
	public static GraphQLArgument limitArgument = new GraphQLArgument.Builder()
			.name("limit")
			.type(GraphQLInt)
			.build(); 
}
