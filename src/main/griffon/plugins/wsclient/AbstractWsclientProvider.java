/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.wsclient;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import groovyx.net.ws.WSClient;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public abstract class AbstractWsclientProvider implements WsclientProvider {
    public <R> R withWs(Map<String, Object> params, Closure<R> closure) {
        if (closure != null) {
            closure.setDelegate(getWsclient(params));
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            return closure.call();
        }
        return null;
    }

    public <R> R withWs(Map<String, Object> params, CallableWithArgs<R> callable) {
        if (callable != null) {
            callable.setArgs(new Object[]{getWsclient(params)});
            return callable.call();
        }
        return null;
    }

    protected abstract WSClient getWsclient(Map<String, Object> params);
}
