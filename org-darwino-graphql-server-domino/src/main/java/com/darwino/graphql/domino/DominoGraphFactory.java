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

import static graphql.Scalars.GraphQLString;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphql.GraphContext;
import com.darwino.graphql.GraphFactory;
import com.darwino.graphql.factories.DynamicObjectGraphFactory;
import com.darwino.graphql.model.BaseDataFetcher;
import com.darwino.graphql.model.ObjectAccessor;
import com.darwino.graphql.model.ObjectDataFetcher;

import graphql.GraphQLException;
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
			if(StringUtil.isEmpty(server)) {
				if(StringUtil.isEmpty(database)) {
					if(StringUtil.isEmpty(defaultDatabase)) {
						throw new GraphQLException(StringUtil.format("Missing Domino database reference"));
					}
					database  = defaultDatabase;
				}
				String localPath = getLocalDatabasePath(database);
				return session.getDatabase(null, localPath);
			} else {
				if(StringUtil.isEmpty(database)) {
					throw new GraphQLException(StringUtil.format("Missing Domino database reference"));
				}
				return session.getDatabase(server, database);
			}
		}
		
		protected String getLocalDatabasePath(String database) {
			return database;
		}
	}

	public static enum SPECIAL_FIELD {
	    // Common attributes names
		UNID("_unid"), //$NON-NLS-1$   
		NOTEID("_noteid"), //$NON-NLS-1$
        // View attributes names
    	ENTRYID("_entryid"), //$NON-NLS-1$
    	POSITION("_position"), //$NON-NLS-1$
    	READ("_read"), //$NON-NLS-1$
    	SIBLINGS("_siblings"), //$NON-NLS-1$
    	DESCENDANTS("_descendants"), //$NON-NLS-1$
    	CHILDREN("_children"), //$NON-NLS-1$
    	INDENT("_indent"), //$NON-NLS-1$
    	CATEGORY("_category"), //$NON-NLS-1$
    	RESPONSE("_response"), //$NON-NLS-1$
    	// Document attributes names
    	PARENTID("_parentid"), //$NON-NLS-1$
    	CREATED("_created"), //$NON-NLS-1$
    	MODIFIED("_modified"), //$NON-NLS-1$
    	ACCESSED("_accessed") //$NON-NLS-1$
		;
		private String name;
		private SPECIAL_FIELD(String name) {
			this.name = name;
		}
		public static SPECIAL_FIELD getSpecialField(String field) throws JsonException {
			if(field.startsWith("_")) {
				for( SPECIAL_FIELD f: SPECIAL_FIELD.values() ) {
					if(f.name.equals(field)) {
						return f;
					}
				}
			}
			return null;
		}
	};	
	
	public static final String TYPE_DOCUMENT 	= "DominoDocument";
	public static final String TYPE_ENTRY 		= "DominoEntry";
	
	public DominoGraphFactory() {
	}

	@Override
	public void createTypes(Builder builders) {
		{
			// View entries
			GraphQLObjectType.Builder docBuilder = GraphQLObjectType.newObject()
				.name(TYPE_DOCUMENT)
				.field(GraphQLFieldDefinition.newFieldDefinition()
					.name(SPECIAL_FIELD.UNID.name)
					.type(GraphQLString)
					.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.UNID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.NOTEID.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.NOTEID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.PARENTID.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.PARENTID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.CREATED.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.CREATED.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.MODIFIED.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.MODIFIED.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.ACCESSED.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.ACCESSED.name,JsonUtil.TYPE_STRING))
					)
			;
			builders.addDynamicFields(docBuilder);
			builders.put(TYPE_DOCUMENT,docBuilder);
		}
		{
			// Documents
			GraphQLObjectType.Builder entBuilder = GraphQLObjectType.newObject()
				.name(TYPE_ENTRY)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.UNID.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.UNID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.NOTEID.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.NOTEID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.ENTRYID.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.ENTRYID.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.POSITION.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.POSITION.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.READ.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.READ.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.SIBLINGS.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.SIBLINGS.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.DESCENDANTS.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.DESCENDANTS.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.CHILDREN.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.CHILDREN.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.INDENT.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.INDENT.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.CATEGORY.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.CATEGORY.name,JsonUtil.TYPE_STRING))
					)
				.field(GraphQLFieldDefinition.newFieldDefinition()
						.name(SPECIAL_FIELD.RESPONSE.name)
						.type(GraphQLString)
						.dataFetcher(new DynamicObjectGraphFactory.ValueFetcher(SPECIAL_FIELD.RESPONSE.name,JsonUtil.TYPE_STRING))
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
					.name("ViewEntry")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_ENTRY))
					.dataFetcher(new ViewEntryFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("ViewEntries")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLList(new GraphQLTypeReference(TYPE_ENTRY)))
					.dataFetcher(new ViewEntriesFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("ViewDocument")
					.argument(ViewEntryAccessor.getArguments())
					.type(new GraphQLTypeReference(TYPE_DOCUMENT))
					.dataFetcher(new ViewDocumentFetcher())
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
					.name("ViewDocuments")
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

	public static abstract class DominoAccessor<T> extends ObjectAccessor<T> {
		public DominoAccessor(DataFetchingEnvironment env, T value) {
			super(env,value);
		}
		
		protected Object convertFromDomino(Object val) throws JsonException, NotesException {
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
		protected Object convertVectorFromDomino(Vector<?> val) throws JsonException, NotesException {
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
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			Object o = readValue(path);
			return o instanceof JsonArray ? (JsonArray)o : JsonArray.of(o);
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
	
				SPECIAL_FIELD sysField = SPECIAL_FIELD.getSpecialField(simpleField);
				if(sysField!=null) {
					switch(sysField) {
					    // Common attributes names
						case UNID:			return convertFromDomino(getValue().getUniversalID());   
						case NOTEID:		return convertFromDomino(getValue().getNoteID());
				    	// Document attributes names
						case PARENTID:		return convertFromDomino(getValue().getParentDocumentUNID());
						case CREATED:		return convertFromDomino(getValue().getCreated().toJavaDate());
						case MODIFIED:		return convertFromDomino(getValue().getLastModified().toJavaDate());
						case ACCESSED:		return convertFromDomino(getValue().getLastAccessed().toJavaDate());
						default:		throw new JsonException(null,"The system field {0} is invalid in this context", path);
					}
				} else {
					return convertVectorFromDomino(getValue().getItemValue(simpleField));
				}
			} catch(NotesException ex) {
				throw new JsonException(ex);
			}
		}
		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(serverArgument);
			args.add(databaseArgument);
			args.add(unidArgument);
			args.add(idArgument);
			return args;
		}
	}

	public static class ViewEntryAccessor extends DominoAccessor<ViewEntry> {
		private Vector<?> values;
		public ViewEntryAccessor(DataFetchingEnvironment env, ViewEntry entry) {
			super(env,entry);
		}
		// Can this be optimized?
		private Object getColumnValue(String name) throws NotesException {
			if(values==null) {
				this.values = getValue().getColumnValues();
			}
			View view = (View)getValue().getParent();
			Vector<?> names = view.getColumnNames();
			for(int i=0; i<names.size(); i++) {
				if(StringUtil.equals((String)names.get(i), name)) {
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
				
				SPECIAL_FIELD sysField = SPECIAL_FIELD.getSpecialField(simpleField);
				if(sysField!=null) {
					switch(sysField) {
					    // Common attributes names
						case UNID:				return convertFromDomino(getValue().getUniversalID());   
						case NOTEID:			return convertFromDomino(getValue().getNoteID());
				        // View attributes names
						case ENTRYID:			return convertFromDomino(getValue().getPosition('.')+'-'+getValue().getUniversalID());
						case POSITION:			return convertFromDomino(getValue().getPosition('.'));
						case READ:				return convertFromDomino(getValue().getRead());
						case SIBLINGS:			return convertFromDomino(getValue().getSiblingCount());
						case DESCENDANTS:		return convertFromDomino(getValue().getDescendantCount());
						case CHILDREN:			return convertFromDomino(getValue().getChildCount());
						case INDENT:			return convertFromDomino(getValue().getIndentLevel());
						case CATEGORY:			return convertFromDomino(getValue().isCategory());
						case RESPONSE: {			                
												Object op = getValue().getParent();
								                if(null != op && op instanceof ViewEntry){
								                    ViewEntry parent = (ViewEntry)op;
								                    return parent.isDocument();
								                }
						}
						default:		throw new JsonException(null,"The system field {0} is invalid in this context", path);
					}
				} else {
					return convertFromDomino(getColumnValue(simpleField));
				}
			} catch(NotesException ex) {
				throw new JsonException(ex);
			}
		}

		public static List<GraphQLArgument> getArguments() {
			List<GraphQLArgument> args = new ArrayList<GraphQLArgument>();
			args.add(serverArgument);
			args.add(databaseArgument);
			args.add(ftSearchArgument);
			args.add(parentIdArgument);
			args.add(keyArgument);
			args.add(skipArgument);
			args.add(limitArgument);
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
					throw new GraphQLException(StringUtil.format("Missing Domino database context"));
				}

				Session session = ctx.getSession();
				if(session==null) {
					throw new GraphQLException(StringUtil.format("Missing Domino session in the database context"));
				}
				
				String server = getStringParameter(environment,"server");
				String database = getStringParameter(environment,"database");

				Document doc = null;
				
				String unid = getStringParameter(environment,"unid");
				if(StringUtil.isNotEmpty(unid)) {
					doc = ctx.getDatabase(server,database).getDocumentByUNID(unid);
				} else {
					String id = getStringParameter(environment,"id");
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
		public static final int DEFAULT_LIMIT		= 100;
		public static final int MAX_LIMIT			= 500;
		
		View view;
		int skip;
		int limit;
		public ViewNav(View view, int skip, int limit) {
			this.view = view;
			this.skip = skip;
			this.limit = Math.max(MAX_LIMIT,limit>0 ? limit : DEFAULT_LIMIT);
		}
		public abstract List<ViewEntryAccessor> entries(DataFetchingEnvironment environment) throws NotesException;
		public abstract List<DocumentAccessor> documents(DataFetchingEnvironment environment) throws NotesException;
	}
	private static class ViewNavAll extends ViewNav {
		public ViewNavAll(View view, int skip, int limit) {
			super(view, skip, limit);
		}
		@Override
		public List<ViewEntryAccessor> entries(DataFetchingEnvironment environment) throws NotesException {
			final List<ViewEntryAccessor> entries = new ArrayList<ViewEntryAccessor>();
			ViewEntryCollection c=view.getAllEntries();
			for(ViewEntry ve=c.getFirstEntry(); ve!=null && entries.size()<limit; ve=c.getNextEntry()) {
				if(skip>0) {
					skip--; continue;
				}
				entries.add(new ViewEntryAccessor(environment,ve));
			}
			return entries;
		}
		@Override
		public List<DocumentAccessor> documents(DataFetchingEnvironment environment) throws NotesException {
			final List<DocumentAccessor> documents = new ArrayList<DocumentAccessor>();
			ViewEntryCollection c=view.getAllEntries();
			for(ViewEntry ve=c.getFirstEntry(); ve!=null && documents.size()<limit; ve=c.getNextEntry()) {
				documents.add(new DocumentAccessor(environment,ve.getDocument()));
			}
			return documents;
		}
	}
	
	public static abstract class BaseViewFetcher<T> extends BaseDataFetcher<T> {
		
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
				
				String server = getStringParameter(environment,"server");
				String database = getStringParameter(environment,"database");

				String view = getStringParameter(environment,"view");
				if(view==null) {
					throw new GraphQLException(StringUtil.format("Missing 'view' parameter in the query"));
				}

				View v = ctx.getDatabase(server,database).getView(view);
				int skip = 0;
				int limit = 0;
				ViewNav viewNav = new ViewNavAll(v, skip, limit);
//				args.add(serverArgument);
//				args.add(databaseArgument);
//				args.add(ftSearchArgument);
//				args.add(parentIdArgument);
//				args.add(keyArgument);
//				args.add(skipArgument);
//				args.add(limitArgument);
//				args.add(p0Argument);
//				args.add(p1Argument);
//				args.add(p2Argument);
//				args.add(p3Argument);
				
				return createAccessor(environment,viewNav);
			} catch(Exception ex) {
				throw new GraphQLException(StringUtil.format("Error while executing the JSON query, {0}",ex.getLocalizedMessage()),ex);
			}
		}
		protected abstract Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException;
	};
	
	public static class ViewEntryFetcher extends BaseViewFetcher<Object> {
		public ViewEntryFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			nav.limit = 1; // Force it
			List<ViewEntryAccessor> e = nav.entries(environment);
			return !e.isEmpty() ? e.get(0) : null; 
		}
	};
	public static class ViewEntriesFetcher extends BaseViewFetcher<Object> {
		public ViewEntriesFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			return nav.entries(environment);
		}
	};
	
	public static class ViewDocumentFetcher extends BaseViewFetcher<Object> {
		public ViewDocumentFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			nav.limit = 1; // Force it
			List<DocumentAccessor> e = nav.documents(environment);
			return !e.isEmpty() ? e.get(0) : null; 
		}
	};
	public static class ViewDocumentsFetcher extends BaseViewFetcher<Object> {
		public ViewDocumentsFetcher() {
		}
		@Override
		protected Object createAccessor(final DataFetchingEnvironment environment, ViewNav nav) throws JsonException, NotesException {
			return nav.documents(environment);
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
	public static GraphQLArgument idArgument = new GraphQLArgument.Builder()
			.name("id")
			.type(GraphQLString)
			.build(); 


	// View specific
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
	public static GraphQLArgument skipArgument = new GraphQLArgument.Builder()
			.name("skip")
			.type(GraphQLString)
			.build(); 
	public static GraphQLArgument limitArgument = new GraphQLArgument.Builder()
			.name("limit")
			.type(GraphQLString)
			.build(); 
	
	// Parameters
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
