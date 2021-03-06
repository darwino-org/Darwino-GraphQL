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

import com.darwino.commons.json.JsonUtil;
import com.darwino.commons.json.jsonpath.JsonPath;
import com.darwino.commons.json.jsonpath.JsonPathFactory;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;


/**
 * JSON Standard fields..
 * 
 * @author Philippe Riand
 */
public class JsonStandardFields extends JsonProvider {
	
	public JsonStandardFields() {
	}

	@Override
	public void addJsonFields(GraphQLObjectType.Builder builder) {
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
			;
			
//			// Arrays
//			.field(newFieldDefinition()
//					.name("booleanArray")
//					.argument(pathArgument)
//					.argument(nameArgument)
//					.type(new GraphQLList(GraphQLBoolean))
//					.dataFetcher(booleanArrayFetcher))
//			.field(newFieldDefinition()
//					.name("numberArray")
//					.argument(pathArgument)
//					.argument(nameArgument)
//					.type(new GraphQLList(GraphQLFloat))
//					.dataFetcher(numberArrayFetcher))
//			.field(newFieldDefinition()
//					.name("stringArray")
//					.argument(pathArgument)
//					.argument(nameArgument)
//					.type(new GraphQLList(GraphQLString))
//					.dataFetcher(stringArrayFetcher));
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
				JsonAccessor source = (JsonAccessor)environment.getSource();
				Object o = source.path(path);
				return JsonUtil.coerceType(getType(), o, null);
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
				JsonAccessor source = (JsonAccessor)environment.getSource();
				Object o = source.path(path);
				return JsonUtil.coerceType(getType(), o, null);
			} catch(Exception ex) {
				return null;
			}
		}
	}
}
