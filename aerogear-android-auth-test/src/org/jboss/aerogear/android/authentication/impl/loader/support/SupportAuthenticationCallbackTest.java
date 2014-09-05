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
package org.jboss.aerogear.android.authentication.impl.loader.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.test.AndroidTestCase;
import java.util.HashMap;
import org.jboss.aerogear.android.authentication.impl.loader.support.SupportAuthenticationModuleAdapter.CallbackHandler;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.pipeline.support.AbstractFragmentActivityCallback;
import org.jboss.aerogear.android.pipeline.support.AbstractSupportFragmentCallback;
import org.mockito.Mockito;

@SuppressWarnings( { "unchecked", "rawtypes" })
public class SupportAuthenticationCallbackTest extends AndroidTestCase {

    public void testPassSupportFragmentCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Fragment fragment = Mockito.mock(Fragment.class);

        SupportAuthenticationModuleAdapter adapter = new SupportAuthenticationModuleAdapter(fragment, getContext(), null, "ignore");
        VoidFragmentCallback fragmentCallback = new VoidFragmentCallback();
        SupportLoginLoader loader = Mockito.mock(SupportLoginLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(fragmentCallback);

        HeaderAndBody data = new HeaderAndBody(new byte[] { 1, 2, 3, 4 }, new HashMap<String, Object>());
        SupportAuthenticationModuleAdapter.CallbackHandler handler = new SupportAuthenticationModuleAdapter.CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(fragmentCallback.successCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(fragmentCallback, "fragment"));

    }

    public void testFailSupportFragmentCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Fragment fragment = Mockito.mock(Fragment.class);

        SupportAuthenticationModuleAdapter adapter = new SupportAuthenticationModuleAdapter(fragment, getContext(), null, "ignore");
        VoidFragmentCallback fragmentCallback = new VoidFragmentCallback();
        SupportLoginLoader loader = Mockito.mock(SupportLoginLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(fragmentCallback);
        Mockito.when(loader.hasException()).thenReturn(true);
        Mockito.when(loader.getException()).thenReturn(new RuntimeException("This is only a test exception."));

        HeaderAndBody data = new HeaderAndBody(new byte[] { 1, 2, 3, 4 }, new HashMap<String, Object>());
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(fragmentCallback.failCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(fragmentCallback, "fragment"));

    }

    public void testPassSupportActivityCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        FragmentActivity activity = Mockito.mock(FragmentActivity.class);

        SupportAuthenticationModuleAdapter adapter = new SupportAuthenticationModuleAdapter(activity, null, "ignore");
        VoidFragmentActivityCallback activityCallback = new VoidFragmentActivityCallback();
        SupportLoginLoader loader = Mockito.mock(SupportLoginLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(activityCallback);

        HeaderAndBody data = new HeaderAndBody(new byte[] { 1, 2, 3, 4 }, new HashMap<String, Object>());
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(activityCallback.successCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(activityCallback, "activity"));

    }

    public void testFailFragmentActivityCallbacks() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        FragmentActivity activity = Mockito.mock(FragmentActivity.class);

        SupportAuthenticationModuleAdapter adapter = new SupportAuthenticationModuleAdapter(activity, null, "ignore");
        VoidFragmentActivityCallback activityCallback = new VoidFragmentActivityCallback();
        SupportLoginLoader loader = Mockito.mock(SupportLoginLoader.class);
        Mockito.when(loader.getCallback()).thenReturn(activityCallback);
        Mockito.when(loader.hasException()).thenReturn(true);
        Mockito.when(loader.getException()).thenReturn(new RuntimeException("This is only a test exception."));

        HeaderAndBody data = new HeaderAndBody(new byte[] { 1, 2, 3, 4 }, new HashMap<String, Object>());
        CallbackHandler handler = new CallbackHandler(adapter, loader, data);
        handler.run();
        assertTrue(activityCallback.failCalled);
        assertNull(UnitTestUtils.getSuperPrivateField(activityCallback, "activity"));

    }

    private static class VoidFragmentCallback extends AbstractSupportFragmentCallback<HeaderAndBody> {

        boolean successCalled = false;
        boolean failCalled = false;

        public VoidFragmentCallback() {
            super("HashCode");
        }

        @Override
        public void onSuccess(HeaderAndBody data) {
            assertNotNull(getFragment());
            successCalled = true;
        }

        @Override
        public void onFailure(Exception e) {
            assertNotNull(getFragment());
            failCalled = true;
        }
    }

    private static class VoidFragmentActivityCallback extends AbstractFragmentActivityCallback<HeaderAndBody> {

        boolean successCalled = false;
        boolean failCalled = false;

        public VoidFragmentActivityCallback() {
            super("HashCode");
        }

        @Override
        public void onSuccess(HeaderAndBody data) {
            assertNotNull(getFragmentActivity());
            successCalled = true;
        }

        @Override
        public void onFailure(Exception e) {
            assertNotNull(getFragmentActivity());
            failCalled = true;
        }
    }

}
