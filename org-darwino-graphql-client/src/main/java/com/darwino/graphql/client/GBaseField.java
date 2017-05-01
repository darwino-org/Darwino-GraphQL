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
public abstract class GBaseField<T extends GBaseField<?>> extends GObject {
	
	private String name;
	private Map<String,Object> attributes;
	private List<GDirective> directives;
	private List<GBaseField<?>> entries;
	
	public GBaseField() {
	}

	public GBaseField(String name) {
		name(name);
	}

	@SuppressWarnings("unchecked")
	public T name(String name) {
		this.name = name;
		return (T)this;
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

	public List<GBaseField<?>> getEntries() {
		if(entries==null) {
			entries = new ArrayList<GBaseField<?>>();
		}
		return entries;
	}

	@SuppressWarnings("unchecked")
	public T attribute(String name, Object value) {
		getAttributes().put(name, value);
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	public T directive(GDirective directive) {
		getDirectives().add(directive);
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	public T field(GBaseField<?> entry) {
		getEntries().add(entry);
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	public T field(String fieldName) {
		getEntries().add(new GField(fieldName));
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	public T field(String alias, String fieldName) {
		getEntries().add(new GField(alias,fieldName));
		return (T)this;
	}
	
	//
	// Schemaless extensions
	//
	@SuppressWarnings("unchecked")
	public T stringField(String alias, String jsonPath) {
		getEntries().add(new GField(alias,"string").attribute("path", jsonPath));
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public T booleanField(String alias, String jsonPath) {
		getEntries().add(new GField(alias,"boolean").attribute("path", jsonPath));
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public T numberField(String alias, String jsonPath) {
		getEntries().add(new GField(alias,"number").attribute("path", jsonPath));
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public T intField(String alias, String jsonPath) {
		getEntries().add(new GField(alias,"int").attribute("path", jsonPath));
		return (T)this;
	}
	@SuppressWarnings("unchecked")
	public T longField(String alias, String jsonPath) {
		getEntries().add(new GField(alias,"long").attribute("path", jsonPath));
		return (T)this;
	}
	
	@Override
	protected void buildQuery(Builder b, boolean compact) throws JsonException {
		if(StringUtil.isEmpty(name)) {
			throw new JsonException(null,"Graph entry is missing a name");
		}
		String alias = getAlias();
		if(StringUtil.isNotEmpty(alias)) {
			b.append(alias);
			b.append(':');
			if(!compact) {
				b.append(' ');
			}
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
			b.emitObjectContent(attributes,compact);
			b.append(")");
		}
		if(entries!=null && !entries.isEmpty()) {
			if(!compact) {
				b.append(' ');
			}
			b.append('{');
			if(!compact) { b.incIndent(); }
			int count = entries.size();
			for(int i=0; i<count; i++) {
				if(i>0) {
					b.append(',');
				}
				if(!compact) b.nl();
				GBaseField<?> e = entries.get(i);
				e.buildQuery(b, compact);
			}
			if(!compact) { b.decIndent(); b.nl(); }
			b.append("}");
		}
	}
	protected String getAlias() {
		return null;
	}
}
