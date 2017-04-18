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

import java.util.Map;

import com.darwino.commons.json.JsonException;

/**
 * Base class for a GraphQL Entry.
 * 
 * @author priand
 */
public class GraphQLDirective extends GraphQLObject {
	
	private String name;
	private Map<String,Object> attributes;
	
	public GraphQLDirective() {
	}

	public GraphQLDirective name(String name) {
		this.name = name;
		return this;
	}

	@Override
	protected void toQuery(GraphQLBuilder b, boolean compact) throws JsonException {
		b.append(name);
		if(attributes!=null && !attributes.isEmpty()) {
			b.append("(");
			b.emitValue(attributes);
			b.append(")");
		}
	}
}
