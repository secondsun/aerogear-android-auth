/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authentication.digest;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.jboss.aerogear.android.authentication.AuthenticationModuleTest;
import org.jboss.aerogear.android.authentication.util.VoidCallback;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeManager;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.rest.RestfulPipeConfiguration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Ignore
@RunWith(AndroidJUnit4.class)
public class HttpDigestIntegrationTest implements AuthenticationModuleTest {

    private static final URL CONTROLLER_URL;
    private static final RestfulPipeConfiguration AUTO_BOT_CONFIG;

    private static final String TAG = HttpDigestIntegrationTest.class.getSimpleName();

    static {
        try {
            CONTROLLER_URL = new URL("http://controller-aerogear.rhcloud.com/aerogear-controller-demo/autobots");
            AUTO_BOT_CONFIG = PipeManager.config("autobots", RestfulPipeConfiguration.class);
            AUTO_BOT_CONFIG.withUrl(CONTROLLER_URL);

        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testBadLogin() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTO_BOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("baduser", "badpass", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTO_BOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        Assert.assertFalse(success.get());

    }

    @Test
    public void testLogin() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTO_BOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("agnes", "123", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTO_BOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                latch.countDown();
            }
        });

        latch.await(1000, TimeUnit.SECONDS);
        Assert.assertTrue(success.get());
    }

    @Test
    public void testLogout() throws InterruptedException {
        HttpDigestAuthenticationModule basicAuthModule = new HttpDigestAuthenticationModule(CONTROLLER_URL, "/autobots", "", 60000);
        final AtomicBoolean success = new AtomicBoolean(false);
        AUTO_BOT_CONFIG.module(basicAuthModule);
        final CountDownLatch authLatch = new CountDownLatch(1);
        basicAuthModule.login("agnes", "123", new Callback<HeaderAndBody>() {

            @Override
            public void onFailure(Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                authLatch.countDown();
            }

            @Override
            public void onSuccess(HeaderAndBody arg0) {
                authLatch.countDown();
            }
        });
        authLatch.await(10, TimeUnit.SECONDS);
        Pipe<String> autobots = AUTO_BOT_CONFIG.forClass(String.class);
        final CountDownLatch latch = new CountDownLatch(1);

        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue(success.get());

        final CountDownLatch latch2 = new CountDownLatch(1);

        final CountDownLatch logoutLatch = new CountDownLatch(1);
        basicAuthModule.logout(new VoidCallback(logoutLatch));
        logoutLatch.await(2, TimeUnit.SECONDS);
        autobots.read(new Callback<List<String>>() {

            @Override
            public void onSuccess(List<String> data) {
                success.set(true);
                latch2.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                success.set(false);
                latch2.countDown();
            }
        });

        latch2.await(10, TimeUnit.SECONDS);
        Assert.assertFalse(success.get());

    }

}
