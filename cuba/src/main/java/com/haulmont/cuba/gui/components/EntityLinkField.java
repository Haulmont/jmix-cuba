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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.WindowManager.OpenType;
import io.jmix.ui.screen.OpenMode;

@Deprecated
public interface EntityLinkField<V> extends io.jmix.ui.component.EntityLinkField<V> {

    /**
     * @return open type
     * @deprecated Use {@link #getOpenMode()} instead.
     */
    @Deprecated
    OpenType getScreenOpenType();

    /**
     * @param openType open type
     * @deprecated Use {@link #setOpenMode(OpenMode)} instead.
     */
    @Deprecated
    void setScreenOpenType(OpenType openType);
}