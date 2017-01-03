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

package com.darwino.graphsql;

import java.util.HashMap;
import java.util.Map;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;

/**
 * GraphQL data type factory.
 *
 * This interface is used to contribute new data types to a Schema and/or contribute members
 * to existing objects.
 * 
 * @author Philippe Riand
 */
public abstract class GraphFactory {
	
	public static abstract class Builder {
		
		private Map<String,GraphQLObjectType.Builder> objectTypes = new HashMap<String, GraphQLObjectType.Builder>();
		private Map<String,GraphQLInterfaceType.Builder> interfaceTypes = new HashMap<String, GraphQLInterfaceType.Builder>();
		
		public Builder() {
		}
		
		public Map<String,GraphQLObjectType.Builder> getObjectTypes() {
			return objectTypes;
		}
		public Builder put(String name, GraphQLObjectType.Builder builder) {
			objectTypes.put(name,builder);
			return this;
		}
		
		public Map<String,GraphQLInterfaceType.Builder> getInterfaceTypes() {
			return interfaceTypes;
		}
		public Builder put(String name, GraphQLInterfaceType.Builder builder) {
			interfaceTypes.put(name,builder);
			return this;
		}

		public abstract void addDynamicFields(GraphQLObjectType.Builder builder);
	}
	
	protected GraphFactory() {
	}

	/**
	 * Contribute types
	 * @param builders
	 */
	public void createTypes(Builder builders) {
	}
	
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
	}

	/**
	 * Create the fields to the main query
	 * @param builders
	 */
	public void createQuery(Builder builders, GraphQLObjectType.Builder query) {
		addDynamicFields(query);
	}
}
