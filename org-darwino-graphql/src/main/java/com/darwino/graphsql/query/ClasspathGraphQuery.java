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

package com.darwino.graphsql.query;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.darwino.commons.util.io.StreamUtil;

/**
 * GraphQL query stored in the classpath.
 * 
 * @author Philippe Riand
 */
public class ClasspathGraphQuery implements GraphQuery {

	private Class<?> refClass;
	private String resName;
	
	public ClasspathGraphQuery(Class<?> refClass, String resName) {
		this.refClass = refClass;
		this.resName = resName;
	}
	
	@Override
	public String loadQuery() throws IOException {
		Reader r = new InputStreamReader(refClass.getResourceAsStream(resName));
		try {
			String query = StreamUtil.readString(r);
			return query;
		} finally {
			StreamUtil.close(r);
		}
	}
}
