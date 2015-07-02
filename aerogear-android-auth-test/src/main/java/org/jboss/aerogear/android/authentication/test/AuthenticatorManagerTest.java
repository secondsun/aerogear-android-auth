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
package org.jboss.aerogear.android.authentication.test;

import android.support.test.runner.AndroidJUnit4;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.android.authentication.AuthenticationConfiguration;
import org.jboss.aerogear.android.authentication.AuthenticationManager;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.basic.HttpBasicAuthenticationConfiguration;
import org.jboss.aerogear.android.authentication.digest.HttpDigestAuthenticationConfiguration;
import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.authentication.test.util.PatchedActivityInstrumentationTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AuthenticatorManagerTest extends PatchedActivityInstrumentationTestCase {

    public AuthenticatorManagerTest() {
        super(MainActivity.class);
    }

    private static final URL SIMPLE_URL;
    private static final String SIMPLE_MODULE_NAME = "simple";

    static {
        try {
            SIMPLE_URL = new URL("http", "localhost", 80, "/");
        } catch (MalformedURLException ex) {
            Logger.getLogger(AuthenticatorManagerTest.class.getName()).log(
                    Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testAddAuthenticatorFailsWithUnsupportedType() {
        try {

            AuthenticationManager.config(SIMPLE_MODULE_NAME, new AuthenticationConfiguration() {

                @Override
                protected AuthenticationModule buildModule() {
                    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
                }
            }.getClass());

            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException ex) {
            // ignore
        }

    }

    @Test
    public void testAddSimpleAuthenticator() {

        AuthenticationManager.registerConfigurationProvider(DummAuthenticationConfiguration.class, new DummyAuthenticationConfigProvider());

        AuthenticationConfiguration config = AuthenticationManager.config("test", DummAuthenticationConfiguration.class);

        Assert.assertTrue(config instanceof DummAuthenticationConfiguration);

    }

    @Test
    public void testAddAndGetSimpleAuthenticator() {
        AuthenticationConfiguration config = AuthenticationManager.config(SIMPLE_MODULE_NAME, HttpBasicAuthenticationConfiguration.class);

        AuthenticationModule simpleAuthModule = config.baseURL(SIMPLE_URL).asModule();
        Assert.assertEquals(simpleAuthModule, AuthenticationManager.getModule(SIMPLE_MODULE_NAME));

    }

    @Test
    public void testNullBaseURLFails() {

        HttpDigestAuthenticationConfiguration config = AuthenticationManager.config(SIMPLE_MODULE_NAME, HttpDigestAuthenticationConfiguration.class);
        try {
            config.asModule();
            Assert.fail("Should not pass");
        } catch (IllegalStateException e) {
            // ignore;
        }

    }

    @Test
    public void testAddAuthenticator() {

        HttpDigestAuthenticationConfiguration config = AuthenticationManager.config(SIMPLE_MODULE_NAME, HttpDigestAuthenticationConfiguration.class);

        AuthenticationModule simpleAuthModule = config.loginEndpoint("testLogin").logoutEndpoint("testLogout").baseURL(SIMPLE_URL).asModule();

        Assert.assertEquals(simpleAuthModule, AuthenticationManager.getModule(SIMPLE_MODULE_NAME));

        Assert.assertEquals("testLogin", simpleAuthModule.getLoginEndpoint());
        Assert.assertEquals("testLogout", simpleAuthModule.getLogoutEndpoint());
    }

    @Test
    public void testGetNullAuthModule() {

        Assert.assertNull(AuthenticationManager.getModule("nullModule"));
    }

    private static final class DummyAuthenticationConfigProvider implements ConfigurationProvider<DummAuthenticationConfiguration> {

        @Override
        public DummAuthenticationConfiguration newConfiguration() {
            return new DummAuthenticationConfiguration();
        }
    }

    private static class DummAuthenticationConfiguration extends AuthenticationConfiguration<DummAuthenticationConfiguration> {

        public DummAuthenticationConfiguration() {
        }

        @Override
        protected AuthenticationModule buildModule() {
            return null;
        }
    }

}
