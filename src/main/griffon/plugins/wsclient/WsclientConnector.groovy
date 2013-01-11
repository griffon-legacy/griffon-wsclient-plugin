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
import groovyx.net.ws.cxf.SoapVersion

/**
 * @author Andres Almiray
 */
@Singleton
class WsclientConnector {
    public WSClient createClient(Map params) {
        def wsdl = params.remove('wsdl')
        def classLoader = params.remove('classLoader') ?: getClass().classLoader
        if (!wsdl) {
            throw new RuntimeException("Failed to create ws client, wsdl: parameter is null or invalid.")
        }
        try {
            def soapVersion = params.remove('soapVersion') ?: '1.1'
            switch (soapVersion) {
                case '1.1': soapVersion = SoapVersion.SOAP_1_1; break
                case '1.2': soapVersion = SoapVersion.SOAP_1_2; break
                default: throw new IllegalArgumentException("Invalid soapVersion: value. Must be either '1.1' or '1.2'")
            }
            def wsclient = new WSClient(wsdl, classLoader, soapVersion)
            wsclient.initialize()

            if (params.containsKey('proxy')) wsclient.setProxyProperties(params.remove('proxy'))
            if (params.containsKey('ssl')) wsclient.setSSLProperties(params.remove('ssl'))
            if (params.containsKey('timeout')) wsclient.setConnectionTimeout(params.remove('timeout'))
            if (params.containsKey('mtom')) wsclient.setMtom(params.remove('mtom'))
            if (params.containsKey('basicAuth')) {
                Map basicAuth = params.remove('basicAuth')
                wsclient.setBasicAuthentication(basicAuth.username ?: '', basicAuth.password ?: '')
            }

            return wsclient
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ws client, reason: $e", e)
        }
    }
}
