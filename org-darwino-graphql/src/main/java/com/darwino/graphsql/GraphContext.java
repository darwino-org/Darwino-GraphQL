/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Licensed under The MIT License (https://opensource.org/licenses/MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.darwino.graphsql;

import java.util.HashMap;
import java.util.Map;

/**
 * GraphQL execution context.
 * 
 * This is the context that is passed to the execution engine. 
 * 
 * @author Philippe Riand
 */
public class GraphContext {

	private Map<Class<?>,Object> contexts = new HashMap<Class<?>,Object>();
	
	public GraphContext() {
	}
	
	public Map<Class<?>,Object> getContexts() {
		return contexts;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> c) {
		return (T)contexts.get(c);
	}
	
	public GraphContext put(Class<?> c, Object context) {
		contexts.put(c,context);
		return this;
	}
}
