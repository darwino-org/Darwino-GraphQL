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

package com.darwino.graphsql.query;

import com.darwino.commons.json.JsonException;

/**
 * Create a GraphQL client session factory.
 * 
 * This is the default factory to create a GraphQL session. This is used by the default GraphQL service
 * implementation and can also be used from Java code.
 * 
 * @author Philippe Riand
 */
public interface GraphQLSessionFactory {
	
	/**
	 * Create a GraphQL session for the current user.
	 * Multiple contexts can be available and selected from the contextName, although the null value is generally used when only
	 * one context is available.
	 * The session object has enough information to execute a GraphQL query.
	 * @param contextName the graphql session context name- might be null
	 * @return the session to use
	 * @throws JsonException if the session cannot be created
	 */
	public GraphQLSession createSession(String contextName) throws JsonException;
}
