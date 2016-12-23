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

package com.darwino.graphsql.json;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.json.jsonpath.JsonPathFactory;
import com.darwino.graphsql.GraphFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

/**
 * GraphQL factory dealing with JSON data.
 *
 * @author Philippe Riand
 */
public class JsonGraphFactory extends GraphFactory {
	
	public static final String JSON_TYPE = "Json";
	
	public static GraphQLArgument pathArgument = new GraphQLArgument.Builder()
			.name("path")
			.type(GraphQLString)
			.build();
	
	public JsonGraphFactory() {
		super("Json");
	}

	@Override
	public void createTypes(Builder builders) {
		// We create the JSON type
		GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
			.name(JSON_TYPE);
		
		// And we add the Json fields to it
		addJsonFields(builder);
		
		builders.put(JSON_TYPE,builder);
	}

	@Override
	public void extendTypes(Builder builders) {
	}

	@Override
	public void createQuery(Builder builders, GraphQLObjectType.Builder query) {
	}
	
	
	protected void addJsonFields(GraphQLObjectType.Builder builder) {
		builder
			// Scalar types
			.field(newFieldDefinition()
					.name("boolean")
					.argument(pathArgument)
					.type(GraphQLBoolean)
					.dataFetcher(new JsonValueFecther(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("number")
					.argument(pathArgument)
					.type(GraphQLFloat)
					.dataFetcher(new JsonValueFecther(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("string")
					.argument(pathArgument)
					.type(GraphQLString)
					.dataFetcher(new JsonValueFecther(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("object")
					.argument(pathArgument)
					.type(new GraphQLTypeReference(JSON_TYPE))
					.dataFetcher(new JsonValueFecther(JsonUtil.TYPE_OBJECT)))
			
			// Arrays
			.field(newFieldDefinition()
					.name("booleanArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLBoolean))
					.dataFetcher(new JsonArrayFecther(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("numberArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLFloat))
					.dataFetcher(new JsonArrayFecther(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("stringArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLString))
					.dataFetcher(new JsonArrayFecther(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("objectArray")
					.argument(pathArgument)
					.type(new GraphQLList(new GraphQLTypeReference(JSON_TYPE)))
					.dataFetcher(new JsonArrayFecther(JsonUtil.TYPE_OBJECT)))
			;
	}
	
	public static abstract class ValueDataFetcher implements DataFetcher {
		private int type;
		public ValueDataFetcher(int type) {
			this.type = type;
		}
		public int getType() {
			return type;
		}
	}
	public static class JsonValueFecther extends ValueDataFetcher {
		public JsonValueFecther(int type) {
			super(type);
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				String parent = (String)environment.getArgument("path");
				JsonPath path = JsonPathFactory.get(parent);
				GraphJsonAccessor source = (GraphJsonAccessor)environment.getSource();
				Object o = source.readValue(path);
				if(getType()==JsonUtil.TYPE_OBJECT) {
					o = new JsonAccessor(source, o);
				} else {
					o = JsonUtil.coerceType(getType(), o, null);
				}
				return o;
			} catch(Exception ex) {
				return null;
			}
		}
	}
	public static class JsonArrayFecther extends ValueDataFetcher {
		public JsonArrayFecther(int type) {
			super(type);
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				String parent = (String)environment.getArgument("path");
				JsonPath path = JsonPathFactory.get(parent);
				GraphJsonAccessor source = (GraphJsonAccessor)environment.getSource();
				@SuppressWarnings("unchecked")
				List<Object> items = (List<Object>)source.readList(path);
				for(int i=0; i<items.size(); i++) {
					Object o = items.get(i);
					if(getType()==JsonUtil.TYPE_OBJECT) {
						o = new JsonAccessor(source, o);
					} else {
						o = JsonUtil.coerceType(getType(), o, null);
					}
					items.set(i, o);
				}
				return items;
			} catch(Exception ex) {
				return null;
			}
		}
	}

	public static class JsonAccessor extends GraphJsonAccessor {
		
		private Object object;
		
		public JsonAccessor(Object parent, Object object) {
			super(parent);
			this.object = object;
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			return path.readValue(object);
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return path.readAsList(object);
		}
	}
	
}
