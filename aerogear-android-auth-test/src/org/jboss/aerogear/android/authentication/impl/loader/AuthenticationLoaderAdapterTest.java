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

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import org.jboss.aerogear.android.authentication.AbstractAuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.CALLBACK;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.METHOD;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.PARAMS;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.PASSWORD;
import static org.jboss.aerogear.android.authentication.impl.loader.LoaderAuthenticationModule.USERNAME;
import org.jboss.aerogear.android.impl.util.VoidCallback;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthenticationLoaderAdapterTest extends AndroidTestCase {

    private static String USERNAME_VALUE = "testUsername";
    private static String PASSWORD_VALUE = "testPassword";
    private Activity activity;
    private Fragment fragment;
    private LoaderManager manager;
    private ArgumentCaptor<Integer> idMatcher;
    private ArgumentCaptor<Bundle> bundleMatcher;
    private VoidCallback callback;
    private AuthenticationModule module = mock(AuthenticationModule.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = mock(Activity.class);
        fragment = mock(Fragment.class);
        manager = mock(LoaderManager.class);

        callback = new VoidCallback();
        idMatcher = ArgumentCaptor.forClass(Integer.class);
        bundleMatcher = ArgumentCaptor.forClass(Bundle.class);

        when(activity.getLoaderManager()).thenReturn(manager);
        when(fragment.getLoaderManager()).thenReturn(manager);
    }

    public void testActivityLogin() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(activity, module, "name");
        authModule.login(USERNAME_VALUE, PASSWORD_VALUE, callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();
        assertNotNull(bundle);
        assertEquals("LOGIN", ((Enum) bundle.get(METHOD)).name());
        assertEquals(USERNAME_VALUE, bundle.getBundle(PARAMS).get(AbstractAuthenticationModule.USERNAME_PARAMETER_NAME));
        assertEquals(PASSWORD_VALUE, bundle.getBundle(PARAMS).get(AbstractAuthenticationModule.PASSWORD_PARAMETER_NAME));

    }

    public void testActivityEnroll() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(activity, null, "name");
        Map<String, String> userData = makeUserData();
        authModule.enroll(userData, callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();
        assertNotNull(bundle);
        assertEquals("ENROLL", ((Enum) bundle.get(METHOD)).name());
        assertEquals(userData, bundle.get(PARAMS));

    }

    public void testActivityLogout() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(activity, module, "name");
        authModule.logout(callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();

        assertNotNull(bundle);
        assertEquals("LOGOUT", ((Enum) bundle.get(METHOD)).name());
        assertEquals(callback, bundle.get(CALLBACK));
    }

    public void testFragmentLogin() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(fragment, this.mContext, null, "name");
        authModule.login(USERNAME_VALUE, PASSWORD_VALUE, callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();
        assertNotNull(bundle);
        assertEquals("LOGIN", ((Enum) bundle.get(METHOD)).name());
        assertEquals(USERNAME_VALUE, bundle.getBundle(PARAMS).get(AbstractAuthenticationModule.USERNAME_PARAMETER_NAME));
        assertEquals(PASSWORD_VALUE, bundle.getBundle(PARAMS).get(AbstractAuthenticationModule.PASSWORD_PARAMETER_NAME));

    }

    public void testFragmentEnroll() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(fragment, this.mContext, null, "name");
        Map<String, String> userData = makeUserData();
        authModule.enroll(userData, callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();
        assertNotNull(bundle);
        assertEquals("ENROLL", ((Enum) bundle.get(METHOD)).name());
        assertEquals(userData, bundle.get(PARAMS));

    }

    public void testFragmentLogout() {
        AuthenticationModuleAdapter authModule = new AuthenticationModuleAdapter(fragment, this.mContext, null, "name");
        authModule.logout(callback);
        verify(manager).initLoader(idMatcher.capture(), bundleMatcher.capture(), (LoaderManager.LoaderCallbacks) any());
        Bundle bundle = bundleMatcher.getValue();

        assertNotNull(bundle);
        assertEquals("LOGOUT", ((Enum) bundle.get(METHOD)).name());
        assertEquals(callback, bundle.get(CALLBACK));
    }

    private Map<String, String> makeUserData() {
        Map<String, String> toReturn = new HashMap<String, String>();
        toReturn.put(USERNAME, USERNAME_VALUE);
        toReturn.put(PASSWORD, PASSWORD_VALUE);
        return toReturn;
    }
}
