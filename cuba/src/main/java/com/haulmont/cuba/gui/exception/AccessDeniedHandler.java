/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.exception;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Frame;
import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("cuba_AccessDeniedHandler")
public class AccessDeniedHandler extends AbstractGenericExceptionHandler implements Ordered {

    @Autowired
    protected Messages messages;

    public AccessDeniedHandler() {
        super(AccessDeniedException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable,
                            WindowManager windowManager) {
        String msg = messages.getMessage("accessDenied.message");
        windowManager.showNotification(msg, Frame.NotificationType.ERROR);
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 20;
    }
}
