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

package com.darwino.graphsql.factories.json;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.util.List;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonJavaFactory;
import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.json.jsonpath.JsonPathFactory;
import com.darwino.graphsql.GraphFactory;
import com.darwino.graphsql.model.ObjectAccessor;

import graphql.GraphQLException;
import graphql.schema.Coercing;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;

/**
 * GraphQL factory dealing with JSON data.
 *
 * @author Philippe Riand
 */
public class JsonGraphFactory extends GraphFactory {
	
	public static final String JSON_TYPE = "JsonContent";
	
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
					.dataFetcher(new JsonValueFetcher(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("number")
					.argument(pathArgument)
					.type(GraphQLFloat)
					.dataFetcher(new JsonValueFetcher(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("string")
					.argument(pathArgument)
					.type(GraphQLString)
					.dataFetcher(new JsonValueFetcher(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("object")
					.argument(pathArgument)
					.type(new GraphQLTypeReference(JSON_TYPE))
					.dataFetcher(new JsonValueFetcher(JsonUtil.TYPE_OBJECT)))
			.field(newFieldDefinition()
					.name("value")
					.argument(pathArgument)
					.type(JsonGraphFactory.GraphQLJsonValue)
					.dataFetcher(new JsonFetcher()))
			
			// Arrays
			.field(newFieldDefinition()
					.name("booleanArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLBoolean))
					.dataFetcher(new JsonArrayFetcher(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("numberArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLFloat))
					.dataFetcher(new JsonArrayFetcher(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("stringArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLString))
					.dataFetcher(new JsonArrayFetcher(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("objectArray")
					.argument(pathArgument)
					.type(new GraphQLList(new GraphQLTypeReference(JSON_TYPE)))
					.dataFetcher(new JsonArrayFetcher(JsonUtil.TYPE_OBJECT)))
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
	public static class JsonValueFetcher extends ValueDataFetcher {
		public JsonValueFetcher(int type) {
			super(type);
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				String parent = (String)environment.getArgument("path");
				JsonPath path = JsonPathFactory.get(parent);
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				Object o = source.readValue(path);
				if(getType()==JsonUtil.TYPE_OBJECT) {
					o = new JsonAccessor(environment, o);
				} else {
					o = JsonUtil.coerceType(getType(), o, null);
				}
				return o;
			} catch(Exception ex) {
				return null;
			}
		}
	}
	public static class JsonFetcher implements DataFetcher {
		public JsonFetcher() {
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				String parent = (String)environment.getArgument("path");
				JsonPath path = JsonPathFactory.get(parent);
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				Object o = source.readValue(path);
				return o;
			} catch(Exception ex) {
				return null;
			}
		}
	}
	public static class JsonArrayFetcher extends ValueDataFetcher {
		public JsonArrayFetcher(int type) {
			super(type);
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				String parent = (String)environment.getArgument("path");
				JsonPath path = JsonPathFactory.get(parent);
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				@SuppressWarnings("unchecked")
				List<Object> items = (List<Object>)source.readList(path);
				for(int i=0; i<items.size(); i++) {
					Object o = items.get(i);
					if(getType()==JsonUtil.TYPE_OBJECT) {
						o = new JsonAccessor(environment, o);
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

	public static class JsonAccessor extends ObjectAccessor<Object> {
		
		public JsonAccessor(DataFetchingEnvironment env, Object value) {
			super(env,value);
		}
		@Override
		public Object readValue(JsonPath path) throws JsonException {
			return path.readValue(getValue());
		}
		@Override
		public List<?> readList(JsonPath path) throws JsonException {
			return path.readAsList(getValue());
		}
	}
	
    public static GraphQLScalarType GraphQLJsonValue = new GraphQLScalarType("JsonValue", "Any value that is understood as a JSON value", new Coercing() {
        @Override
        public Object serialize(Object input) {
        	return input;
        }
        @Override
        public Object parseValue(Object input) {
            return input;
        }
        @Override
        public Object parseLiteral(Object input) {
        	if(input instanceof String) {
        		try {
        			return JsonJavaFactory.instance.fromJson((String)input);
        		} catch(JsonException ex) {
        			throw new GraphQLException(ex);
        		}
        	}
            return null;
        }
    });	
}
