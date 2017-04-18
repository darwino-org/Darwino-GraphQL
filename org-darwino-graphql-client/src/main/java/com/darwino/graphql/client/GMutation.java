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
 * GraphQL Query Request.
 * 
 * @author priand
 */
public class GMutation extends GEntry {
	
	public GMutation() {
	}

	public GMutation(String name) {
		super(name);
	}
	
	@Override
	protected void buildQuery(Builder b, boolean compact) throws JsonException {
		b.append("mutation ");
		super.buildQuery(b, compact);
	}	
}
