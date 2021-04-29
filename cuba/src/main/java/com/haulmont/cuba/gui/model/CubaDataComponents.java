/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.model;

import com.haulmont.cuba.gui.model.impl.CubaCollectionLoaderImpl;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;

public class CubaDataComponents extends DataComponents {

    /**
     * Creates CUBA {@code CollectionLoader}.
     */
    public <E> CollectionLoader<E> createCubaCollectionLoader() {
        CubaCollectionLoaderImpl<E> loader = new CubaCollectionLoaderImpl<>();
        autowire(loader);
        return loader;
    }
}
