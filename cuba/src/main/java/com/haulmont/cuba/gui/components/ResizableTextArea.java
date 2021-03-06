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

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.common.event.Subscription;

import java.util.function.Consumer;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.ResizableTextArea} instead
 */
@Deprecated
public interface ResizableTextArea<V> extends TextArea<V>, io.jmix.ui.component.ResizableTextArea<V>, HasSettings {

    String NAME = io.jmix.ui.component.ResizableTextArea.NAME;

    /**
     * @deprecated Use {@link io.jmix.ui.component.ResizableTextArea#setResizableDirection(ResizeDirection)} instead.
     */
    @Deprecated
    void setResizable(boolean resizable);

    /**
     * @deprecated Use {@link io.jmix.ui.component.ResizableTextArea#getResizableDirection()} instead.
     */
    @Deprecated
    boolean isResizable();

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeResizeListener(Consumer<ResizeEvent> listener);
}
