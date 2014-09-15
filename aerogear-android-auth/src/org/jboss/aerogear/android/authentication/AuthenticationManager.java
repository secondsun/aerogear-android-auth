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

import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.ConfigurationProvider;
import org.jboss.aerogear.android.authentication.impl.HttpBasicAuthenticationConfiguration;
import org.jboss.aerogear.android.authentication.impl.HttpDigestAuthenticationConfiguration;


/**
 *
 * @author Summers
 */
public class AuthenticationManager {
    private static Map<String, AuthenticationModule> modules = new HashMap<String, AuthenticationModule>();

    private static Map<Class<? extends AuthenticationConfiguration<?>>, ConfigurationProvider<?>>
            configurationProviderMap = new HashMap<Class<? extends AuthenticationConfiguration<?>>, ConfigurationProvider<?>>();

    private static OnAuthenticationCreatedListener onAuthenticationCreatedListener = new OnAuthenticationCreatedListener() {
        @Override
        public void onAuthenticationCreated(AuthenticationConfiguration<?> configuration, AuthenticationModule module) {
            modules.put(configuration.getName(), module);
        }
    };

    static {
        HttpBasicAuthenticationConfigurationProvider basicConfigurationProvider = new HttpBasicAuthenticationConfigurationProvider();
        AuthenticationManager.registerConfigurationProvider(HttpBasicAuthenticationConfiguration.class, basicConfigurationProvider);
        HttpDigestAuthenticationConfigurationProvider digestConfigurationProvider = new HttpDigestAuthenticationConfigurationProvider();
        AuthenticationManager.registerConfigurationProvider(HttpDigestAuthenticationConfiguration.class, digestConfigurationProvider);
    }

    private AuthenticationManager() {
    }

    /**
     * 
     * This will add a new Configuration that this Manager can build 
     * Configurations for.
     * 
     * @param <CFG> the actual Configuration type
     * @param configurationClass the class of configuration to be registered
     * @param provider the instance which will provide the configuration.
     */
    public static <CFG extends AuthenticationConfiguration<CFG>> void registerConfigurationProvider
            (Class<CFG> configurationClass, ConfigurationProvider<CFG> provider) {
        configurationProviderMap.put(configurationClass, provider);
    }

    /**
     * Begins a new fluent configuration stanza.
     * 
     * @param <CFG> the Configuration type.
     * @param name an identifier which will be used to fetch the AuthenticationModule after 
     * configuration is finished. 
     * @param authenticationConfigurationClass the class of the configuration type.
     *
     * @return a AuthenticationConfiguration which can be used to build a AuthenticationModule object.
     */            
    public static <CFG extends AuthenticationConfiguration<CFG>> CFG config(String name, Class<CFG> authenticationConfigurationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends AuthenticationConfiguration<CFG>> provider =
                (ConfigurationProvider<? extends AuthenticationConfiguration<CFG>>)
                        configurationProviderMap.get(authenticationConfigurationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name)
                .addOnAuthenticationCreatedListener(onAuthenticationCreatedListener);

    }

    /**
     * Fetches a named module
     * @param name the name of the AuthenticationModule given in {@link AuthenticationManager#config(java.lang.String, java.lang.Class) }
     *
     * @return the named AuthenticationModule or null
     */
    public static AuthenticationModule getModule(String name) {
        return modules.get(name);
    }

    
}
