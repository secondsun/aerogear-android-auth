/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authentication.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.authentication.AuthenticationConfig;
import org.jboss.aerogear.android.authentication.MainActivity;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.impl.util.VoidCallback;

public class AGSecurityAuthenticationModuleTest extends PatchedActivityInstrumentationTestCase implements AuthenticationModuleTest {

    private static final URL SIMPLE_URL;
    private static final URL LIVE_URL;

    public AGSecurityAuthenticationModuleTest() {
        super(MainActivity.class);
    }

    static {
        try {
            SIMPLE_URL = new URL("http://localhost:8080/todo-server");
            LIVE_URL = new URL("https://controller-aerogear.rhcloud.com/aerogear-controller-demo/");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void testDefaultConstructor() throws Exception {
        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                SIMPLE_URL, new AuthenticationConfig());
        Object runner = UnitTestUtils.getPrivateField(module, "runner");
        HttpProvider provider = (HttpProvider) ((Provider) UnitTestUtils.getSuperPrivateField(
                runner, "httpProviderFactory")).get(SIMPLE_URL);
        Assert.assertEquals(SIMPLE_URL, provider.getUrl());

        Assert.assertEquals(SIMPLE_URL, module.getBaseURL());

    }

    public void testLoginFails() throws Exception {
        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                SIMPLE_URL, new AuthenticationConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        Object runner = UnitTestUtils.getPrivateField(module, "runner");
        UnitTestUtils.setPrivateField(runner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return new HttpStubProvider(SIMPLE_URL) {
                            @Override
                            public HeaderAndBody post(String ignore)
                                    throws RuntimeException {
                                try {
                                    throw new HttpException(new byte[1], 403);
                                } finally {
                                }
                            }
                        };
                    }
                });

        SimpleCallback callback = new SimpleCallback(latch);
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertNotNull(callback.exception);
        Assert.assertFalse(module.isLoggedIn());
    }

    public void testLoginSucceeds() throws IOException, NoSuchFieldException,
            InterruptedException, IllegalArgumentException,
            IllegalAccessException {
        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                SIMPLE_URL, new AuthenticationConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        Object runner = UnitTestUtils.getPrivateField(module, "runner");
        UnitTestUtils.setPrivateField(runner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return new HttpStubProvider(SIMPLE_URL) {
                            @Override
                            public HeaderAndBody post(String ignore)
                                    throws RuntimeException {
                                HashMap<String, Object> headers = new HashMap<String, Object>();
                                return new HeaderAndBody(new byte[1], headers);

                            }
                        };
                    }
                });

        SimpleCallback callback = new SimpleCallback(latch);
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);
        latch.await(2, TimeUnit.SECONDS);

        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);
        Assert.assertTrue(module.isLoggedIn());
    }

    public void testEnrollSucceeds() throws Exception {
        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                SIMPLE_URL, new AuthenticationConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        Object runner = UnitTestUtils.getPrivateField(module, "runner");
        UnitTestUtils.setPrivateField(runner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return new HttpStubProvider(SIMPLE_URL) {
                            @Override
                            public HeaderAndBody post(String enrollData)
                                    throws RuntimeException {
                                HashMap<String, Object> headers = new HashMap<String, Object>();
                                return new HeaderAndBody(new byte[1], headers);

                            }
                        };
                    }
                });
        SimpleCallback callback = new SimpleCallback(latch);

        Map<String, String> userData = new HashMap<String, String>();
        userData.put("username", PASSING_USERNAME);
        userData.put("password", ENROLL_PASSWORD);
        userData.put("firstname", "Summers");
        userData.put("lastname", "Pittman");
        userData.put("role", "admin");

        module.enroll(userData, callback);
        latch.await(2, TimeUnit.SECONDS);
        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);

        Assert.assertTrue(module.isLoggedIn());
    }

    public void testLogoutSucceeds() throws Exception {
        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                SIMPLE_URL, new AuthenticationConfig());
        SimpleCallback callback = new SimpleCallback();

        final CountDownLatch latch = new CountDownLatch(1);
        Object runner = UnitTestUtils.getPrivateField(module, "runner");
        UnitTestUtils.setPrivateField(runner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return new HttpStubProvider(SIMPLE_URL) {
                            @Override
                            public HeaderAndBody post(String ignore)
                                    throws RuntimeException {
                                try {
                                    HashMap<String, Object> headers = new HashMap<String, Object>();
                                    return new HeaderAndBody(new byte[1],
                                            headers);
                                } finally {
                                    latch.countDown();
                                }
                            }
                        };
                    }
                });

        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);

        latch.await(2, TimeUnit.SECONDS);

        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);
        Assert.assertTrue(module.isLoggedIn());

        VoidCallback voidCallback = new VoidCallback();

        final CountDownLatch latch2 = new CountDownLatch(1);
        UnitTestUtils.setPrivateField(runner, "httpProviderFactory",
                new Provider<HttpProvider>() {
                    @Override
                    public HttpProvider get(Object... in) {
                        return new HttpStubProvider(SIMPLE_URL) {
                            @Override
                            public HeaderAndBody post(String ignore)
                                    throws RuntimeException {
                                try {
                                    HashMap<String, Object> headers = new HashMap<String, Object>();

                                    return new HeaderAndBody(new byte[1],
                                            headers);
                                } finally {
                                    latch2.countDown();
                                }
                            }
                        };
                    }
                });

        module.logout(voidCallback);

        latch2.await(2, TimeUnit.SECONDS);

        Assert.assertNull(voidCallback.exception);

        Assert.assertFalse(module.isLoggedIn());
    }

    public void testLoginTimeout() throws IOException, NoSuchFieldException,
            InterruptedException, IllegalArgumentException,
            IllegalAccessException {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTimeout(1);

        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                LIVE_URL, config);

        final CountDownLatch latch = new CountDownLatch(1);

        SimpleCallback callback = new SimpleCallback(latch);
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);

        latch.await(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(callback.exception);
        Assert.assertEquals(SocketTimeoutException.class, callback.exception.getCause().getClass());
    }

    public void testEnrollTimeout() throws Exception {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTimeout(1);

        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                LIVE_URL, config);

        final CountDownLatch latch = new CountDownLatch(1);

        SimpleCallback callback = new SimpleCallback(latch);

        Map<String, String> userData = new HashMap<String, String>();
        userData.put("username", PASSING_USERNAME);
        userData.put("password", ENROLL_PASSWORD);
        userData.put("firstname", "Summers");
        userData.put("lastname", "Pittman");
        userData.put("role", "admin");

        module.enroll(userData, callback);

        latch.await(50000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(callback.exception);
        Assert.assertEquals(SocketTimeoutException.class, callback.exception.getCause().getClass());

    }

    public void testLogoutTimesout() throws Exception {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTimeout(1);

        AGSecurityAuthenticationModule module = new AGSecurityAuthenticationModule(
                LIVE_URL, config);

        final CountDownLatch latch = new CountDownLatch(1);

        VoidCallback callback = new VoidCallback();
        module.logout(callback);

        latch.await(5000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(callback.exception);
        Throwable cause = callback.exception;
        if (callback.exception.getCause() != null) {
            cause = callback.exception.getCause();
        }
        Assert.assertEquals(SocketTimeoutException.class, cause.getClass());

    }
}
