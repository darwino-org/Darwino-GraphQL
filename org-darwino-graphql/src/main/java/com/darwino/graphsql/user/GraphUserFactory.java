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

package com.darwino.graphsql.user;

import static graphql.Scalars.GraphQLString;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;


/**
 * GraphQL User data type.
 * 
 * @author Philippe Riand
 */
public class GraphUserFactory {
	
	public static final GraphUserFactory instance = new GraphUserFactory();

	public static class UserAttrDataFetcher implements DataFetcher {
		private String attrName;
		public UserAttrDataFetcher(String attrName) {
			this.attrName = attrName;
		}
	    @Override
		public Object get(DataFetchingEnvironment environment) {
	    	try {
				return ((User)environment).getAttribute(attrName);
			} catch (UserException ex) {
				Platform.log(ex);
				return null;
			}
	    }
	}
	
	public static final String TYPE = "User";
	
	public GraphUserFactory() {
	}
	
	public GraphQLObjectType createType() {
		GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
			.name(TYPE)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("dn")
				.type(GraphQLString)
				.dataFetcher(new UserAttrDataFetcher(User.ATTR_DN))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("cn")
				.type(GraphQLString)
				.dataFetcher(new UserAttrDataFetcher(User.ATTR_CN))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("email")
				.type(GraphQLString)
				.dataFetcher(new UserAttrDataFetcher(User.ATTR_EMAIL))
			)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("photoUrl")
				.type(GraphQLString)
				.dataFetcher(new UserAttrDataFetcher(User.ATTR_PHOTOURL))
			)
			// Todo: add groups, roles...
		;
		
		
		return builder.build();
	}
}
