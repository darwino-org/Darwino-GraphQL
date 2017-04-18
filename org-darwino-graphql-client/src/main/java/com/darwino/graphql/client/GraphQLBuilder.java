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

import java.util.List;
import java.util.Map;

import com.darwino.commons.util.text.JavaTextUtil;
import com.darwino.commons.util.text.TextBuilder;

/**
 * Text builder for GraphQL queries.
 * 
 * @author priand
 */
public class GraphQLBuilder extends TextBuilder{
	
	public GraphQLBuilder() {
	}
	
	@SuppressWarnings("unchecked")
	public void emitValue(Object value) {
		if(value instanceof Map<?,?>) {
			emitObject((Map<String, Object>)value);
		} else if(value instanceof List<?>) {
			emitArray((List<Object>)value);
		} else if(value instanceof CharSequence) {
			append('"');
			append(JavaTextUtil.toJavaString((CharSequence)value));
			append('"');
		} else if(value instanceof Number) {
		} else if(value instanceof Boolean) {
			append(((Boolean)value).booleanValue() ? "true" : "false");
		}
	}	
	
	public void emitObject(Map<String,Object> map) {
		boolean first = true;
		append("{");
		for(Map.Entry<String, Object> e: map.entrySet()) {
			if(first) {
				first = false;
			} else {
				append(",");
			}
			String name =  e.getKey();
			append(name);
			append(": ");
			Object value = e.getValue();
			emitValue(value);
		}
		append("}");
	}	
	
	public void emitArray(List<Object> list) {
		boolean first = true;
		append("[");
		for(Object value: list) {
			if(first) {
				first = false;
			} else {
				append(",");
			}
			emitValue(value);
		}
		append("]");
	}
}
