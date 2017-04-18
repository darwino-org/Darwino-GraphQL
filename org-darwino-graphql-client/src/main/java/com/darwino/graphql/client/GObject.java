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

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.text.JavaTextUtil;
import com.darwino.commons.util.text.TextBuilder;

/**
 * Base class for a GraphQL client objects.
 * 
 * @author priand
 */
public abstract class GObject {
	
	public static class Builder extends TextBuilder {
		
		public Builder() {
		}
		
		@SuppressWarnings("unchecked")
		public void emitValue(Object value) {
			if(value instanceof Map<?,?>) {
				append("{");
				emitObjectContent((Map<String, Object>)value);
				append("}");
			} else if(value instanceof List<?>) {
				append("[");
				emitArrayContent((List<Object>)value);
				append("]");
			} else if(value instanceof CharSequence) {
				append('"');
				append(JavaTextUtil.toJavaString((CharSequence)value));
				append('"');
			} else if(value instanceof Number) {
				append(string((Number)value));
			} else if(value instanceof Boolean) {
				append(((Boolean)value).booleanValue() ? "true" : "false");
			}
		}	
		private static String string(Number n) {
			double d = n.doubleValue();
			long l = (long)d;
			if(((double)l)==d) {
				return Long.toString(l);
			} else {
				return Double.toString(d);
			}
		}
		
		public void emitObjectContent(Map<String,Object> map) {
			boolean first = true;
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
		}	
		
		public void emitArrayContent(List<Object> list) {
			boolean first = true;
			for(Object value: list) {
				if(first) {
					first = false;
				} else {
					append(",");
				}
				emitValue(value);
			}
		}
	}
	
	
	public GObject() {
	}
	
	public String createQuery() throws JsonException {
		return createQuery(true);
	}
	public String createQuery(boolean compact) throws JsonException {
		Builder b = new Builder();
		buildQuery(b, compact);
		return b.toString();
	}

	protected abstract void buildQuery(Builder b, boolean compact) throws JsonException;
}
