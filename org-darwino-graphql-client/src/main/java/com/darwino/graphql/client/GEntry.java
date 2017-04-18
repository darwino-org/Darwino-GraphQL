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
import com.darwino.commons.util.StringUtil;

/**
 * Base class for a GraphQL Entry.
 * 
 * @author priand
 */
public class GEntry extends GObject {
	
	private String name;
	private Map<String,Object> attributes;
	private List<GDirective> directives;
	private List<GEntry> entries;
	
	public GEntry() {
	}

	public GEntry(String name) {
		name(name);
	}

	public GEntry name(String name) {
		this.name = name;
		return this;
	}

	public Map<String,Object> getAttributes() {
		if(attributes==null) {
			attributes = new LinkedHashMap<String, Object>();
		}
		return attributes;
	}

	public List<GDirective> getDirectives() {
		if(directives==null) {
			directives = new ArrayList<GDirective>();
		}
		return directives;
	}

	public List<GEntry> getEntries() {
		if(entries==null) {
			entries = new ArrayList<GEntry>();
		}
		return entries;
	}

	public GEntry attribute(String name, Object value) {
		getAttributes().put(name, value);
		return this;
	}

	public GEntry directive(GDirective directive) {
		getDirectives().add(directive);
		return this;
	}

	public GEntry entry(GEntry entry) {
		getEntries().add(entry);
		return this;
	}
	
	@Override
	protected void buildQuery(Builder b, boolean compact) throws JsonException {
		if(StringUtil.isEmpty(name)) {
			throw new JsonException(null,"Graph entry is missing a name");
		}
		b.append(name);
		if(directives!=null && !directives.isEmpty()) {
			int count = directives.size();
			for(int i=0; i<count; i++) {
				b.append(' ');
				directives.get(i).buildQuery(b,compact);
			}
		}
		if(attributes!=null && !attributes.isEmpty()) {
			b.append("(");
			b.emitObjectContent(attributes);
			b.append(")");
		}
		if(entries!=null && !entries.isEmpty()) {
			b.append(" {");
			if(!compact) { b.incIndent(); }
			int count = entries.size();
			for(int i=0; i<count; i++) {
				if(!compact) b.nl();
				GEntry e = entries.get(i);
				e.buildQuery(b, compact);
			}
			if(!compact) { b.decIndent(); b.nl(); }
			b.append("}");
		}
	}
}
