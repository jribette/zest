/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.zest.test.v1;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.zest.core.v1.ZestActionSleep;
import org.mozilla.zest.core.v1.ZestClientFailException;
import org.mozilla.zest.core.v1.ZestClientLaunch;
import org.mozilla.zest.core.v1.ZestClientWindowClose;
import org.mozilla.zest.core.v1.ZestJSON;
import org.mozilla.zest.core.v1.ZestScript;
import org.mozilla.zest.impl.ZestBasicRunner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 */
public class ZestClientLaunchUnitTest {

	private static final int PORT = 8888;
	
	HttpServer server = null;
	
	@Before
	public void before() throws IOException {
		server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/test", new HttpHandler(){
			@Override
			public void handle(HttpExchange t) throws IOException {
				String response = "This is the response";
	            t.sendResponseHeaders(200, response.length());
	            OutputStream os = t.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
			}});
        server.setExecutor(null); // creates a default executor
        server.start();
	}

	@After
	public void after() throws IOException {
		server.stop(0);
	}

	@Test
	public void testHtmlUnitLaunch() throws Exception {
		ZestScript script = new ZestScript();
		script.add(new ZestClientLaunch("htmlunit", "HtmlUnit", "http://localhost:" + PORT + "/test"));
		script.add(new ZestClientWindowClose("htmlunit", 0));
		script.add(new ZestActionSleep(1));
	
		ZestBasicRunner runner = new ZestBasicRunner();
		// Uncomment this to proxy via ZAP
		//runner.setProxy("localhost", 8090);
		runner.run(script, null);
	}

	@Test
	public void testHtmlUnitByClassLaunch() throws Exception {
		ZestScript script = new ZestScript();
		script.add(new ZestClientLaunch("htmlunit", "org.openqa.selenium.htmlunit.HtmlUnitDriver", 
				"http://localhost:" + PORT + "/test"));
		script.add(new ZestClientWindowClose("htmlunit", 0));
	
		ZestBasicRunner runner = new ZestBasicRunner();
		// Uncomment this to proxy via ZAP
		//runner.setProxy("localhost", 8090);
		runner.run(script, null);
	}

	@Test(expected=ZestClientFailException.class)
	public void testInvalidName() throws Exception {
		ZestScript script = new ZestScript();
		script.add(new ZestClientLaunch("bad", "baddriver", "http://localhost:" + PORT + "/test"));
		script.add(new ZestClientWindowClose("bad", 0));
	
		ZestBasicRunner runner = new ZestBasicRunner();
		runner.run(script, null);
	}

	@Test
	public void testSerialization() {
		ZestClientLaunch zcl1 = new ZestClientLaunch("htmlunit", "HtmlUnit", "http://localhost:" + PORT + "/test");
		String str = ZestJSON.toString(zcl1);
		ZestClientLaunch zcl2 = (ZestClientLaunch) ZestJSON.fromString(str);
		
		assertEquals(zcl1.getElementType(), zcl2.getElementType());
		assertEquals(zcl1.getBrowserType(), zcl2.getBrowserType());
		assertEquals(zcl1.getWindowHandle(), zcl2.getWindowHandle());
		assertEquals(zcl1.getUrl(), zcl2.getUrl());
	}
}
