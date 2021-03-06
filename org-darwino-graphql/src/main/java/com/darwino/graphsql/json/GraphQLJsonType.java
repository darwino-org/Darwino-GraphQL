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

import java.util.ArrayList;
import java.util.List;

import graphql.schema.GraphQLObjectType;


/**
 * JSON GraphQL data type.
 * 
 * @author Philippe Riand
 */
public class GraphQLJsonType {
	
	public static final String TYPE = "json";
	
	private List<JsonProvider> providers = new ArrayList<JsonProvider>();
	
	public GraphQLJsonType(JsonProvider...providers) {
		initDefaultProviders(this.providers);
		for(int i=0; i<providers.length; i++) {
			this.providers.add(providers[i]);
		}
	}
	
	protected void initDefaultProviders(List<JsonProvider> providers) {
		providers.add(new JsonStandardFields());
	}
	
	public List<JsonProvider> getProviders() {
		return providers;
	}
	
	public GraphQLObjectType createType() {
		GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
			.name(TYPE)
		;
		
		for(JsonProvider p: providers) {
			p.addJsonFields(builder);
		}
		
		return builder.build();
	}
}
