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

package griffon.plugins.wsclient

import groovyx.net.ws.WSClient

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
class WsclientHolder {
    private static final WsclientHolder INSTANCE

    static {
        INSTANCE = new WsclientHolder()
    }

    static WsclientHolder getInstance() {
        INSTANCE
    }

    private WsclientHolder() {}

    private final Map<String, WSClient> CLIENTS = new ConcurrentHashMap<String, WSClient>()

    String[] getWsclientIds() {
        List<String> ids = []
        ids.addAll(CLIENTS.keySet())
        ids.toArray(new String[ids.size()])
    }

    WSClient getWsclient(String id) {
        CLIENTS[id]
    }

    void setWsclient(String id, WSClient client) {
        CLIENTS[id] = client
    }

    // ======================================================

    WSClient fetchWsclient(Map<String, Object> params) {
        WSClient client = CLIENTS[(params.id).toString()]
        if (client == null) {
            String id = params.id ? params.remove('id').toString() : '<EMPTY>'
            client = WsclientConnector.instance.createClient(params)
            if (id != '<EMPTY>') CLIENTS[id] = client
        }
        client
    }
}
