/**
 * Copyright 2012 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package whitespell.net.websockets.socketio.annotation;

import whitespell.net.websockets.socketio.SocketIOClient;
import whitespell.net.websockets.socketio.handler.SocketIOException;
import whitespell.net.websockets.socketio.listener.DisconnectListener;
import whitespell.net.websockets.socketio.namespace.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class OnDisconnectScanner implements AnnotationScanner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Class<? extends Annotation> getScanAnnotation() {
        return OnDisconnect.class;
    }

    @Override
    public void addListener(Namespace namespace, final Object object, final Class clazz, final Method method) {
        namespace.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                try {
                    method.invoke(object, client);
                } catch (Exception e) {
                    throw new SocketIOException(e);
                }
            }
        });
    }

    @Override
    public void validate(Method method, Class clazz) {
        if (method.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Wrong OnDisconnect listener signature: " + clazz + "." + method.getName());
        }
        boolean valid = false;
        for (Class<?> eventType : method.getParameterTypes()) {
            if (eventType.equals(SocketIOClient.class)) {
                valid = true;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("Wrong OnDisconnect listener signature: " + clazz + "." + method.getName());
        }
    }

}
