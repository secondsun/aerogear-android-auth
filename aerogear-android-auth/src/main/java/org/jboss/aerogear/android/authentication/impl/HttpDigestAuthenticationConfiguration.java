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

import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.authentication.AuthenticationConfiguration;

public class HttpDigestAuthenticationConfiguration extends AuthenticationConfiguration<HttpDigestAuthenticationConfiguration> implements
        Config<HttpDigestAuthenticationConfiguration> {

    private String loginEndpoint = "/auth/login";
    private String logoutEndpoint = "/auth/logout";
    private Integer timeout = 60000;

    public HttpDigestAuthenticationConfiguration() {
    }

    @Override
    protected HttpDigestAuthenticationModule buildModule() {
        return new HttpDigestAuthenticationModule(super.getBaseUrl(), loginEndpoint, logoutEndpoint, timeout);
    }

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public HttpDigestAuthenticationConfiguration loginEndpoint(String loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
        return this;

    }

    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }

    public HttpDigestAuthenticationConfiguration logoutEndpoint(String logoutEndpoint) {
        this.logoutEndpoint = logoutEndpoint;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public HttpDigestAuthenticationConfiguration timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

}
