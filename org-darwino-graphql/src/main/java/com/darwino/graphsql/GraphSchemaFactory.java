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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;


/**
 * GraphQL data type factory.
 *
 * This interface is used to contribute new data types to a Schema and/or contribute members
 * to existing objects.
 * 
 * @author Philippe Riand
 */
public class GraphSchemaFactory {
	
	public static final String ROOTNAME = "root";
	
	private String rootName;
	private GraphFactory[] factories;
	
	public GraphSchemaFactory(GraphFactory...factories) {
		this.factories = factories;
		this.rootName = ROOTNAME;
	}
	
	public String getRootName() {
		return rootName;
	}
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public GraphFactory[] getFactories() {
		return factories;
	}
	public void setFactories(GraphFactory[] factories) {
		this.factories = factories;
	}

	
	public GraphQLSchema createSchema() {
		List<GraphFactory> factories = findFactories();

		GraphFactory.Builder builders = new GraphFactory.Builder(); 
        
        // Create all the types from the factories 
		for(int i=0; i<factories.size(); i++) {
			GraphFactory f = factories.get(i);
			f.createTypes(builders);
		}
		
		// Once the types are created, then extend them
		for(int i=0; i<factories.size(); i++) {
			GraphFactory f = factories.get(i);
			f.extendTypes(builders);
		}
		
		// Create the main type
		
		// Build all the types
		Set<GraphQLType> types = new HashSet<GraphQLType>();
		for(GraphQLObjectType.Builder b: builders.getObjectTypes().values()) {
			GraphQLObjectType t = b.build();
			types.add(t);
		}
		for(GraphQLInterfaceType.Builder b: builders.getInterfaceTypes().values()) {
			GraphQLInterfaceType t = b.build();
			types.add(t);
		}
		
		// Create the query (main fields)
		GraphQLObjectType.Builder queryType = GraphQLObjectType.newObject()
			.name(getRootName())
		;
		for(int i=0; i<factories.size(); i++) {
			GraphFactory f = factories.get(i);
			f.createQuery(builders,queryType);
		}
        GraphQLSchema schema = GraphQLSchema.newSchema()
            .query(queryType)
            .build(types);
        return schema;
	}

	protected List<GraphFactory> findFactories() {
		List<GraphFactory> list = new ArrayList<GraphFactory>();
		
		// Get the static factories
		if(factories!=null) {
			for(int i=0; i<factories.length; i++) {
				list.add(factories[i]);
			}
		}
		
		// Add the factories from an extension point?
		// think deeper about that...
		//Platform.findExtensions(GraphFactory.class);
		
		return list;
	}
}