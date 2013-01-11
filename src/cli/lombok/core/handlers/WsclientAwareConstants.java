/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.core.BaseConstants;
import lombok.core.util.MethodDescriptor;

import static lombok.core.util.MethodDescriptor.args;
import static lombok.core.util.MethodDescriptor.type;
import static lombok.core.util.MethodDescriptor.typeParams;

/**
 * @author Andres Almiray
 */
public interface WsclientAwareConstants extends BaseConstants {
    String WSCLIENT_PROVIDER_TYPE = "griffon.plugins.wsclient.WsclientProvider";
    String DEFAULT_WSCLIENT_PROVIDER_TYPE = "griffon.plugins.wsclient.DefaultWsclientProvider";
    String WSCLIENT_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.wsclient.WsclientContributionHandler";
    String WSCLIENT_PROVIDER_FIELD_NAME = "this$wsclientProvider";
    String METHOD_GET_WSCLIENT_PROVIDER = "getWsclientProvider";
    String METHOD_SET_WSCLIENT_PROVIDER = "setWsclientProvider";
    String METHOD_WITH_WS = "withWs";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_WS,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_WS,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        )
    };
}
