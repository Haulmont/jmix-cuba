/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class UserIdUtils {

    public static UUID getUserId(UserDetails user) {
        Field idField = ReflectionUtils.findField(user.getClass(), "id");
        if (idField != null) {
            try {
                if (!Modifier.isPublic(idField.getModifiers())) {
                    idField.setAccessible(true);
                }
                Object idValue = idField.get(user);
                if (!(idValue instanceof UUID)) {
                    throw new RuntimeException("User id must be a UUID");
                }
                return (UUID) idValue;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot get user id", e);
            }
        } else {
            throw new RuntimeException("Cannot find the id attribute in the User entity");
        }
    }

    public static boolean hasUserId(UserDetails user) {
        return ReflectionUtils.findField(user.getClass(), "id") != null;
    }
}
