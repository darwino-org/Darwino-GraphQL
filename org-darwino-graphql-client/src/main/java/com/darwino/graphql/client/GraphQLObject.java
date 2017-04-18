/*!COPYRIGHT HEADER! - CONFIDENTIAL 
 *
 * Darwino Inc Confidential.
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Notice: The information contained in the source code for these files is the property 
 * of Darwino Inc. which, with its licensors, if any, owns all the intellectual property 
 * rights, including all copyright rights thereto.  Such information may only be used 
 * for debugging, troubleshooting and informational purposes.  All other uses of this information, 
 * including any production or commercial uses, are prohibited. 
 */

package com.darwino.graphql.client;

import com.darwino.commons.json.JsonException;

/**
 * Base class for a GraphQL client objects.
 * 
 * @author priand
 */
public abstract class GraphQLObject {
	
	public GraphQLObject() {
	}
	
	public String toQuery() throws JsonException {
		return toQuery(true);
	}
	public String toQuery(boolean compact) throws JsonException {
		GraphQLBuilder b = new GraphQLBuilder();
		toQuery(b, compact);
		return b.toString();
	}

	protected abstract void toQuery(GraphQLBuilder b, boolean compact) throws JsonException;
}
