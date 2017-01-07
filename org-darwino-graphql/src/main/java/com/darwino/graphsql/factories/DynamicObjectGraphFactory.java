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

package com.darwino.graphsql.factories;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
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
public class DynamicObjectGraphFactory extends GraphFactory {
	
	public static final String DYNAMIC_TYPE = "DwoObject";
	
	public static GraphQLArgument pathArgument = new GraphQLArgument.Builder()
			.name("path")
			.type(GraphQLString)
			.build();
	
	public DynamicObjectGraphFactory() {
	}

	@Override
	public void createTypes(Builder builders) {
		// We create the JSON type
		GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
			.name(DYNAMIC_TYPE);
		builders.addDynamicFields(builder);
		
		builders.put(DYNAMIC_TYPE,builder);
	}

	@Override
	public void createQuery(Builder builders, GraphQLObjectType.Builder query) {
	}
	
	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
			// Scalar types
			.field(newFieldDefinition()
					.name("boolean")
					.argument(pathArgument)
					.type(GraphQLBoolean)
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("number")
					.argument(pathArgument)
					.type(GraphQLFloat)
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("string")
					.argument(pathArgument)
					.type(GraphQLString)
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("object")
					.argument(pathArgument)
					.type(new GraphQLTypeReference(DYNAMIC_TYPE))
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_OBJECT)))
			.field(newFieldDefinition()
					.name("value")
					.argument(pathArgument)
					.type(DynamicObjectGraphFactory.GraphQLDynamicValue)
					.dataFetcher(new JsonValueFetcher()))

			.field(newFieldDefinition()
					.name("int")
					.argument(pathArgument)
					.type(GraphQLInt)
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_INT)))
			.field(newFieldDefinition()
					.name("long")
					.argument(pathArgument)
					.type(GraphQLLong)
					.dataFetcher(new ValueFetcher(JsonUtil.TYPE_LONG)))
			
			// Arrays
			.field(newFieldDefinition()
					.name("booleanArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLBoolean))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_BOOLEAN)))
			.field(newFieldDefinition()
					.name("numberArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLFloat))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_NUMBER)))
			.field(newFieldDefinition()
					.name("stringArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLString))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_STRING)))
			.field(newFieldDefinition()
					.name("objectArray")
					.argument(pathArgument)
					.type(new GraphQLList(new GraphQLTypeReference(DYNAMIC_TYPE)))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_OBJECT)))

			.field(newFieldDefinition()
					.name("intArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLInt))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_INT)))
			.field(newFieldDefinition()
					.name("longArray")
					.argument(pathArgument)
					.type(new GraphQLList(GraphQLLong))
					.dataFetcher(new ArrayFetcher(JsonUtil.TYPE_LONG)))
			;
	}
	
	public static abstract class BaseFetcher implements DataFetcher {
		private int type;
		public BaseFetcher(int type) {
			this.type = type;
		}
		public int getType() {
			return type;
		}
	}
	public static class ValueFetcher implements DataFetcher {
		private JsonPath path;
		private int type;
		public ValueFetcher(int type) {
			this.type = type;
		}
		public ValueFetcher(String path, int type) {
			try {
				this.path = JsonPathFactory.get(path);
				this.type = type;
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				JsonPath path = this.path!=null ? this.path : JsonPathFactory.get((String)environment.getArgument("path"));
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				Object o = source.readValue(path);
				if(type==JsonUtil.TYPE_OBJECT) {
					o = new DynamicObjectAccessor(environment, o);
				} else {
					o = JsonUtil.coerceType(type, o, null);
				}
				return o;
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
	}
	public static class JsonValueFetcher implements DataFetcher {
		private JsonPath path;
		public JsonValueFetcher() {
		}
		public JsonValueFetcher(String path) {
			try {
				this.path = JsonPathFactory.get(path);
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				JsonPath path = this.path!=null ? this.path : JsonPathFactory.get((String)environment.getArgument("path"));
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				Object o = source.readValue(path);
				return o;
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
	}
	public static class ArrayFetcher implements DataFetcher {
		private JsonPath path;
		private int type;
		public ArrayFetcher(int type) {
			this.type = type;
		}
		public ArrayFetcher(String path, int type) {
			try {
				this.path = JsonPathFactory.get(path);
				this.type = type;
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
		@Override
		public Object get(DataFetchingEnvironment environment) {
			try {
				JsonPath path = this.path!=null ? this.path : JsonPathFactory.get((String)environment.getArgument("path"));
				ObjectAccessor<?> source = (ObjectAccessor<?>)environment.getSource();
				@SuppressWarnings("unchecked")
				List<Object> items = (List<Object>)source.readList(path);
				for(int i=0; i<items.size(); i++) {
					Object o = items.get(i);
					if(type==JsonUtil.TYPE_OBJECT) {
						o = new DynamicObjectAccessor(environment, o);
					} else {
						o = JsonUtil.coerceType(type, o, null);
					}
					items.set(i, o);
				}
				return items;
			} catch(Exception ex) {
				throw new GraphQLException(ex);
			}
		}
	}

	public static class DynamicObjectAccessor extends ObjectAccessor<Object> {
		
		public DynamicObjectAccessor(DataFetchingEnvironment env, Object value) {
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
	
    public static GraphQLScalarType GraphQLDynamicValue = new GraphQLScalarType("DynamicObject", "Any value that is understood as a dynamic value", new Coercing() {
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
