/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authentication;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import org.jboss.aerogear.android.Config;

/**
 * Authentication Registration config.
 *
 * @param <CONFIGURATION> The concrete implementation of this configuration.
 */
public abstract class AuthenticationConfiguration<CONFIGURATION extends AuthenticationConfiguration<CONFIGURATION>> implements Config<CONFIGURATION> {

    private String name;
    private URL baseURL;

    private Collection<OnAuthenticationCreatedListener> listeners = new HashSet<OnAuthenticationCreatedListener>();

    public AuthenticationConfiguration() {
    }

    /**
     *
     * The name is the lookup parameter which will be used by {@link AuthenticationManager#getModule(java.lang.String)
     * }. It is automatically registered when the module is built.
     *
     * @return the current name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * The name is the lookup parameter which will be used by {@link AuthenticationManager#getModule(java.lang.String)
     * }. It is automatically registered when the module is built.
     *
     * @param name a new name.
     * 
     * @return the current configuration
     */
    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     *
     * @return the current collection.
     */
    public Collection<OnAuthenticationCreatedListener> getOnAuthenticationCreatedListeners() {
        return listeners;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     *
     * @param listener new listener to add to the collection
     * @return this configuration
     */
    public CONFIGURATION addOnAuthenticationCreatedListener(OnAuthenticationCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     *
     * @param listeners new collection to replace the current one
     * @return this configuration
     */
    public CONFIGURATION setOnAuthenticationCreatedListeners(Collection<OnAuthenticationCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }

    /**
     *
     * Creates a authenticationModule based on the current configuration and
     * notifies all listeners
     *
     * @return An AuthenticationModule based on this configuration
     *
     * @throws IllegalStateException if the AuthenticationModule can not be
     * constructed.
     *
     */
    public final AuthenticationModule asModule() {
        if (baseURL == null) {
            throw new IllegalStateException("baseURL may not be null");
        }
        AuthenticationModule newModule = buildModule();
        for (OnAuthenticationCreatedListener listener : getOnAuthenticationCreatedListeners()) {
            listener.onAuthenticationCreated(this, newModule);
        }
        return newModule;
    }

    /**
     *
     * Validates configuration parameters and returns a AuthenticationModule
     * instance.
     *
     * @return An AuthenticationModule based on this configuration
     *
     * @throws IllegalStateException if the Pipe can not be constructed.
     */
    protected abstract AuthenticationModule buildModule();

    /**
     * The baseURL is the URL that any endpoints (for example, login, logout,
     * enroll etc) will be build on.
     *
     * @return the baseURL
     */
    public URL getBaseUrl() {
        return baseURL;
    }

    /**
     *
     * The baseURL is the URL that any endpoints (for example, login, logout,
     * enroll etc) will be build on.
     *
     * @param baseURL a new baseURL
     * @return the configuration objects
     */
    public CONFIGURATION baseURL(URL baseURL) {
        this.baseURL = baseURL;
        return (CONFIGURATION) this;
    }

}
