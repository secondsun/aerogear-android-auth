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
package org.jboss.aerogear.android.authentication.impl.loader;

import android.content.Loader;
import android.os.Bundle;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.jboss.aerogear.android.authentication.MainActivity;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.impl.HttpBasicAuthenticationModule;
import static org.jboss.aerogear.android.authentication.impl.loader.AuthenticationModuleAdapter.Methods.LOGIN;
import static org.jboss.aerogear.android.authentication.impl.loader.AuthenticationModuleAdapter.Methods.LOGOUT;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.METHOD;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.impl.util.VoidCallback;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LoadersTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public LoadersTest() {
        super(MainActivity.class);
    }

    final HeaderAndBody response = new HeaderAndBody(new byte[] { 1, 2, 3, 4 }, new HashMap<String, Object>());
    AuthenticationModule module;
    VoidCallback callback;

    @Override
    public void setUp() {
        module = mock(AuthenticationModule.class);
        callback = new VoidCallback();
        doAnswer(new ResponseAnswer<HeaderAndBody>(response)).when(module).enroll(any(Map.class), any(Callback.class));
        doAnswer(new ResponseAnswer<HeaderAndBody>(response)).when(module).login(anyString(), anyString(), any(Callback.class));
        doAnswer(new ResponseAnswer<HeaderAndBody>(response)).when(module).login(anyMapOf(String.class, String.class), any(Callback.class));
        doAnswer(new ResponseAnswer<Void>(null)).when(module).logout(any(Callback.class));
        when(module.isLoggedIn()).thenReturn(Boolean.FALSE);

    }

    public void testEnrollLoader() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("test", "test");
        EnrollLoader loader = new EnrollLoader(getActivity(), callback, module, params);
        loader.loadInBackground();
        verify(module).enroll(eq(params), any(Callback.class));
    }

    public void testLogoutLoader() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("test", "test");
        LogoutLoader loader = new LogoutLoader(getActivity(), callback, module);
        loader.loadInBackground();
        verify(module).logout(any(Callback.class));
    }

    public void testLoginLoader() {
        Map<String, String> loginData = new HashMap<String, String>();
        LoginLoader loader = new LoginLoader(getActivity(), callback, module, loginData);
        loader.loadInBackground();
        verify(module).login(anyMapOf(String.class, String.class), any(Callback.class));
    }

    public void testLoaderDoesNotCache() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InterruptedException, MalformedURLException {

        AuthenticationModule module = spy(new HttpBasicAuthenticationModule(new URL("http://test.com")));
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean loggedIn = new AtomicBoolean(false);
        final AtomicInteger loginCount = new AtomicInteger(0);
        final AtomicInteger logoutCount = new AtomicInteger(0);
        AuthenticationModuleAdapter adapter = new AuthenticationModuleAdapter(getActivity(), module, "ignore");

        adapter = spy(adapter);
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Bundle bundle = (Bundle) invocation.getArguments()[1];
                AuthenticationModuleAdapter.Methods method = (AuthenticationModuleAdapter.Methods) bundle.get(METHOD);
                switch (method) {
                case LOGIN:
                    loginCount.incrementAndGet();
                    loggedIn.set(true);
                    break;
                case LOGOUT:
                    logoutCount.incrementAndGet();
                    loggedIn.set(false);
                }
                ((Callback) bundle.getSerializable(AuthenticationModuleAdapter.CALLBACK)).onSuccess(null);
                return mock(Loader.class);
            }
        }).when(adapter).onCreateLoader(anyInt(), any(Bundle.class));

        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return loggedIn.get();
            }
        }).when(module).isLoggedIn();

        adapter.login("evilname", "password", new VoidCallback(latch));
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        latch = new CountDownLatch(1);
        adapter.logout(new VoidCallback());
        adapter.login("evilname", "password", new VoidCallback(latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        adapter.logout(new VoidCallback());

        assertEquals(2, loginCount.get());
        assertEquals(2, logoutCount.get());

    }

    public void testLoginLoaderDoesNotCache() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InterruptedException, MalformedURLException {

        AuthenticationModule module = new HttpBasicAuthenticationModule(new URL("http://test.com"));
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger loginCount = new AtomicInteger(0);
        final AtomicInteger logoutCount = new AtomicInteger(0);
        AuthenticationModuleAdapter adapter = new AuthenticationModuleAdapter(getActivity(), module, "ignore");

        adapter = spy(adapter);
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Bundle bundle = (Bundle) invocation.getArguments()[1];
                AuthenticationModuleAdapter.Methods method = (AuthenticationModuleAdapter.Methods) bundle.get(METHOD);
                switch (method) {
                case LOGIN:
                    loginCount.incrementAndGet();
                    break;
                case LOGOUT:
                    logoutCount.incrementAndGet();
                    break;
                }
                ((Callback) bundle.getSerializable(AuthenticationModuleAdapter.CALLBACK)).onSuccess(null);
                return mock(Loader.class);
            }
        }).when(adapter).onCreateLoader(anyInt(), any(Bundle.class));

        adapter.login("evilname", "password", new VoidCallback(latch));
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        latch = new CountDownLatch(1);
        adapter.logout(new VoidCallback());
        adapter.login("evilname", "password", new VoidCallback(latch));
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(2, loginCount.get());
        assertEquals(1, logoutCount.get());

    }

    private final class ResponseAnswer<T> implements Answer<Void> {

        T response;

        public ResponseAnswer(T response) {
            this.response = response;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Callback callback = null;
            for (Object argument : invocation.getArguments()) {
                if (argument instanceof Callback) {
                    callback = (Callback) argument;
                }
            }

            if (callback == null) {
                throw new IllegalArgumentException("A callback was not passed");
            }

            callback.onSuccess(response);

            return null;
        }
    }

}
