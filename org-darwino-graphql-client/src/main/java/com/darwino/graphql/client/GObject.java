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
import com.darwino.commons.util.text.JavaScriptUtil;
import com.darwino.commons.util.text.TextBuilder;

/**
 * Base class for a GraphQL client objects.
 * 
 * @author priand
 */
public abstract class GObject {
	
	public static final int ASCII_MIN = 32;
	public static final int ASCII_MAX = 126;

	
	public static class Builder extends TextBuilder {
		
		public Builder() {
		}
		
		@SuppressWarnings("unchecked")
		public void emitValue(Object value, boolean compact) {
			if(value instanceof Map<?,?>) {
				append("{");
				emitObjectContent((Map<String, Object>)value,compact);
				append("}");
			} else if(value instanceof List<?>) {
				append("[");
				emitArrayContent((List<Object>)value,compact);
				append("]");
			} else if(value instanceof CharSequence) {
				append('"');
				escape((CharSequence)value);
				append('"');
			} else if(value instanceof Number) {
				append(string((Number)value));
			} else if(value instanceof Boolean) {
				append(((Boolean)value).booleanValue() ? "true" : "false");
			}
		}
		private void escape(CharSequence s) {
			// http://facebook.github.io/graphql/#sec-String-Value
	        int length = s.length();
	        for( int i=0; i<length; i++ ) {
	            char c = s.charAt(i);
	            switch(c) {
	                case '\b':  append( "\\b" );  break;
	                case '\t':  append( "\\t" );  break;
	                case '\n':  append( "\\n" );  break;
	                case '\f':  append( "\\f" );  break;
	                case '\r':  append( "\\r" );  break;
	                case '/':  append( "\\/" ); break;
	                case '\"':  append( "\\\"" ); break;
	                case '\\':  append( "\\\\" ); break;
	                default : {
	                    if((c<ASCII_MIN) || (c > ASCII_MAX)) {
	                    	append( "\\u" );
	                    	append( com.darwino.commons.util.StringUtil.toUnsignedHex(c,4) );
	                    } else {
	                    	append(c);
	                    }
	                }
	            }
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
		
		public void emitObjectContent(Map<String,Object> map, boolean compact) {
			boolean first = true;
			for(Map.Entry<String, Object> e: map.entrySet()) {
				if(first) {
					first = false;
				} else {
					append(",");
				}
				String name =  e.getKey();
				append(name);
				append(':');
				if(!compact) {
					append(' ');
				}
				Object value = e.getValue();
				emitValue(value,compact);
			}
		}	
		
		public void emitArrayContent(List<Object> list, boolean compact) {
			boolean first = true;
			for(Object value: list) {
				if(first) {
					first = false;
				} else {
					append(",");
				}
				emitValue(value,compact);
			}
		}
	}
	
	
	public GObject() {
	}
	
	public String build() throws JsonException {
		return build(true);
	}
	public String build(boolean compact) throws JsonException {
		Builder b = new Builder();
		buildQuery(b, compact);
		return b.toString();
	}

	protected abstract void buildQuery(Builder b, boolean compact) throws JsonException;
}
