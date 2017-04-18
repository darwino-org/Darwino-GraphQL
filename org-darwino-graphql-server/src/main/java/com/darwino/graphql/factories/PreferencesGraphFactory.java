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

package com.darwino.graphql.factories;

import static graphql.Scalars.GraphQLString;

import com.darwino.commons.Platform;
import com.darwino.commons.preferences.Preferences;
import com.darwino.commons.preferences.PreferencesService;
import com.darwino.commons.util.StringUtil;
import com.darwino.graphql.GraphFactory;
import com.darwino.graphql.model.BaseDataFetcher;
import com.darwino.graphql.model.ObjectDataFetcher;
import com.darwino.graphql.model.PojoAccessor;

import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;


/**
 * GraphQL User Preferences data type.
 * 
 * @author Philippe Riand
 */
public class PreferencesGraphFactory extends GraphFactory {
	
	public static final String TYPE = "DwoPreferences";
	
	public static final PreferencesGraphFactory instance = new PreferencesGraphFactory();

	public static class PreferenceAttrDataFetcher extends BaseDataFetcher<Object> {
		private String prefName;
		public PreferenceAttrDataFetcher(String prefName) {
			this.prefName = prefName;
		}
	    @Override
		public Object get(DataFetchingEnvironment environment) {
    		@SuppressWarnings("unchecked")
			Preferences p = ((PojoAccessor<Preferences>)environment.getSource()).getValue();
			return p.get(prefName);
	    }
	}
	
	public PreferencesGraphFactory() {
	}
	
	@Override
	public void createTypes(Builder builders) {
		GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
			.name(TYPE)
		;
		builders.addDynamicFields(builder);
		builders.put(TYPE,builder);
	}

	public static GraphQLArgument prefsArguments = new GraphQLArgument.Builder()
			.name("prefs")
			.type(GraphQLString)
			.defaultValue(PreferencesService.DEFAULT_PREFS)
			.description("Indicates the preferences set name. Empty means default.")
			.build(); 
	public static GraphQLArgument userArguments = new GraphQLArgument.Builder()
			.name("user")
			.type(GraphQLString)
			.defaultValue("@me")
			.description("User DN, empty means @me.")
			.build(); 
	public static class PreferencesFecther extends ObjectDataFetcher<Preferences> {
		public PreferencesFecther() {
		}
		@Override
		public PojoAccessor<Preferences> get(DataFetchingEnvironment environment) {
			try {
				PreferencesService svc = Platform.getService(PreferencesService.class);
				
				String type = getStringParameter(environment,"type");
				if(StringUtil.isEmpty(type)) {
					type = PreferencesService.DEFAULT_PREFS;
				}
				
				String user = getStringParameter(environment,"user");
				if(StringUtil.isEmpty(user)) {
					return null;
				}
				if(StringUtil.equals(user, UserGraphFactory.ME_ID)) {
					user = UserGraphFactory.Context.get(environment).getCurrentUser().getDn();
				}
				Preferences pref = svc.getPreferences(user, type);
				if(pref==null) {
					return null;
				}
				return new PojoAccessor<Preferences>(environment,pref) {
					@Override
					protected Object getProperty(String propName) {
						return getValue().get(propName);
					}
				};
			} catch(Exception ex) {
				if(ex instanceof GraphQLException) {
					throw (GraphQLException)ex;
				}
				throw new GraphQLException("Error while fetching Preferences object",ex);
			}
		}
	};
	private static PreferencesFecther preferencesFetcher = new PreferencesFecther();

	@Override
	public void addDynamicFields(GraphQLObjectType.Builder builder) {
		builder
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("Preferences")
				.argument(prefsArguments)
				.argument(userArguments)
				.type(new GraphQLTypeReference(TYPE))
				.dataFetcher(preferencesFetcher)
			)
		;
	}
}
