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

import static graphql.Scalars.GraphQLString;

import com.darwino.commons.Platform;
import com.darwino.commons.security.acl.User;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.security.acl.UserService;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphsql.GraphFactory;
import com.darwino.graphsql.model.BaseDataFetcher;
import com.darwino.graphsql.model.ObjectDataFetcher;
import com.darwino.graphsql.model.PojoAccessor;

import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;


/**
 * GraphQL User data type.
 * 
 * @author Philippe Riand
 */
public class UserGraphFactory extends GraphFactory {
	
	public static final UserGraphFactory instance = new UserGraphFactory();

	public static class UserAttrDataFetcher extends BaseDataFetcher<Object> {
		private String attrName;
		public UserAttrDataFetcher(String attrName) {
			this.attrName = attrName;
		}
	    @Override
		public Object get(DataFetchingEnvironment environment) {
	    	try {
	    		@SuppressWarnings("unchecked")
				User u = ((PojoAccessor<User>)environment.getSource()).getValue();
				return u.getAttribute(attrName);
			} catch (UserException ex) {
				Platform.log(ex);
				return null;
			}
	    }
	}
	
	public static final String TYPE = "User";
	
	public UserGraphFactory() {
	}
	
	@Override
	public void createTypes(Builder builders) {
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
		;
		
		builders.addDynamicFields(builder);
		
		builders.put(TYPE,builder);
	}

	public static GraphQLArgument dnArgument = new GraphQLArgument.Builder()
			.name("dn")
			.type(GraphQLString)
			.build(); 
	public static class UserFecther extends ObjectDataFetcher<User> {
		public UserFecther() {
		}
		@Override
		public PojoAccessor<User> get(DataFetchingEnvironment environment) {
			try {
				UserService svc = Platform.getService(UserService.class);
				
				String dn = getStringParameter(environment,"dn");
				if(StringUtil.isEmpty(dn)) {
					return null;
				}
				
				User user = svc.findUser(dn);
				if(user==null) {
					return null;
				}
				return new PojoAccessor<User>(environment,user) {
					@Override
					protected Object getProperty(String propName) {
						try {
							return getValue().getAttribute(propName);
						} catch (UserException e) {
							return null;
						}
					}
				};
			} catch(Exception ex) {
				throw new GraphQLException("Error while fetching user object",ex);
			}
		}
	};
	private static UserFecther userFetcher = new UserFecther();

	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("User")
				.argument(dnArgument)
				.type(new GraphQLTypeReference(TYPE))
				.dataFetcher(userFetcher)
			)
		;
	}
}
