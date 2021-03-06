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

package com.haulmont.cuba.web.gui.components.datagrid;

import com.haulmont.cuba.gui.components.HasRowsCount;
import com.haulmont.cuba.gui.components.RowsCount;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Layout;
import io.jmix.ui.component.VisibilityChangeNotifier;
import io.jmix.ui.component.impl.AbstractDataGrid;
import io.jmix.ui.widget.JmixCssActionsLayout;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Component(DataGridDelegate.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataGridDelegate implements HasRowsCount {
    public static final String NAME = "cuba_DataGridDelegate";

    protected RowsCount rowsCount;

    @Nullable
    @Override
    public RowsCount getRowsCount() {
        return rowsCount;
    }

    @Override
    public void setRowsCount(@Nullable RowsCount rowsCount) {
        this.rowsCount = rowsCount;
    }

    public void setRowsCount(@Nullable RowsCount rowsCount,
                             Layout topPanel,
                             Supplier<Layout> topPanelCreator,
                             JmixCssActionsLayout componentComposition,
                             Runnable visibilityChangeHandler,
                             AbstractDataGrid dataGrid) {
        if (this.rowsCount != null && topPanel != null) {
            topPanel.removeComponent(this.rowsCount.unwrap(com.vaadin.ui.Component.class));
            this.rowsCount.setParent(null);
        }
        setRowsCount(rowsCount);
        if (rowsCount != null) {
            if (rowsCount.getParent() != null && rowsCount.getParent() != dataGrid) {
                throw new IllegalStateException("Component already has parent");
            }

            if (topPanel == null) {
                topPanel = topPanelCreator.get();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel); // creates new top panel
            }
            rowsCount.setWidthAuto();
            com.vaadin.ui.Component rc = rowsCount.unwrap(com.vaadin.ui.Component.class);
            topPanel.addComponent(rc);

            if (rowsCount instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) rowsCount).addVisibilityChangeListener(event ->
                        visibilityChangeHandler.run());
            }
        }

        visibilityChangeHandler.run();
    }
}
