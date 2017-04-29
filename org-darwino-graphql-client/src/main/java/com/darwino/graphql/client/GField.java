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

/**
 * Base class for a GraphQL Entry.
 * 
 * @author priand
 */
public class GField extends GBaseField<GField> {

	private String alias;
	
	public GField() {
	}

	public GField(String name) {
		super(name);
	}

	public GField(String alias, String name) {
		super(name);
		this.alias = alias;
	}
	
	public GField alias(String alias) {
		this.alias = alias;
		return this;
	}
	@Override
	protected String getAlias() {
		return alias;
	}

}
