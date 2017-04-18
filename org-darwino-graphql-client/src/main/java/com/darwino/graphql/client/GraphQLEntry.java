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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.darwino.commons.json.JsonException;

/**
 * Base class for a GraphQL Entry.
 * 
 * @author priand
 */
public class GraphQLEntry extends GraphQLObject {
	
	private String name;
	private Map<String,Object> attributes;
	private List<GraphQLDirective> directives;
	private List<GraphQLEntry> entries;
	
	public GraphQLEntry() {
	}

	public GraphQLEntry name(String name) {
		this.name = name;
		return this;
	}

	public Map<String,Object> getAttributes() {
		if(attributes==null) {
			attributes = new LinkedHashMap<String, Object>();
		}
		return attributes;
	}

	public List<GraphQLDirective> getDirectives() {
		if(directives==null) {
			directives = new ArrayList<GraphQLDirective>();
		}
		return directives;
	}

	public List<GraphQLEntry> getEntries() {
		if(entries==null) {
			entries = new ArrayList<GraphQLEntry>();
		}
		return entries;
	}

	public GraphQLEntry attribute(String name, Object value) {
		getAttributes().put(name, value);
		return this;
	}

	public GraphQLEntry directive(GraphQLDirective directive) {
		getDirectives().add(directive);
		return this;
	}

	public GraphQLEntry entry(GraphQLEntry entry) {
		getEntries().add(entry);
		return this;
	}
	
	@Override
	protected void toQuery(GraphQLBuilder b, boolean compact) throws JsonException {
		b.append(name);
		if(directives!=null && !directives.isEmpty()) {
			int count = directives.size();
			for(int i=0; i<count; i++) {
				b.append(' ');
				directives.get(i).toQuery();
			}
		}
		if(attributes!=null && !attributes.isEmpty()) {
			b.append("(");
			b.emitValue(attributes);
			b.append(")");
		}
		if(entries!=null && !entries.isEmpty()) {
			b.append(" {");
			if(!compact) { b.incIndent(); b.nl(); }
			int count = entries.size();
			for(int i=0; i<count; i++) {
				GraphQLEntry e = entries.get(i);
				e.toQuery(b, compact);
			}
			if(!compact) { b.decIndent(); b.nl(); }
			b.append("}");
		}
	}
}
