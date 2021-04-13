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

package com.haulmont.cuba;

import com.haulmont.cuba.core.global.CubaMessageTools;
import com.haulmont.cuba.core.global.CubaMetadataTools;
import com.haulmont.cuba.core.global.EntityStates;
import com.haulmont.cuba.core.global.impl.CubaFetchPlanRepository;
import com.haulmont.cuba.core.global.impl.CubaInstanceNameProviderImpl;
import com.haulmont.cuba.core.global.impl.CubaMetadata;
import com.haulmont.cuba.core.global.impl.MessagesImpl;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.CubaMetaModelLoader;
import com.haulmont.cuba.core.sys.CubaNumberIdCache;
import com.haulmont.cuba.gui.components.CubaUiTestIdsSupport;
import com.haulmont.cuba.gui.components.presentation.CubaPresentationActionsBuilder;
import com.haulmont.cuba.gui.model.impl.CubaScreenDataImpl;
import com.haulmont.cuba.gui.model.impl.CubaScreenDataXmlLoader;
import com.haulmont.cuba.gui.presentation.Presentations;
import com.haulmont.cuba.gui.presentation.PresentationsImpl;
import com.haulmont.cuba.gui.xml.CubaPropertyShortcutLoader;
import com.haulmont.cuba.security.app.UserSettingServiceBean;
import com.haulmont.cuba.web.app.settings.UserSettingsToolsImpl;
import com.haulmont.cuba.web.gui.CubaUiControllerReflectionInspector;
import com.haulmont.cuba.web.sys.*;
import com.haulmont.cuba.web.sys.navigation.CubaUrlChangeHandler;
import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.MetaModelLoader;
import io.jmix.core.impl.MetadataLoader;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.NumberIdCache;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.datatoolsui.DatatoolsUiConfiguration;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattrui.DynAttrUiConfiguration;
import io.jmix.localfs.LocalFileStorageConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.securityui.SecurityUiConfiguration;
import io.jmix.ui.*;
import io.jmix.ui.builder.EditorBuilderProcessor;
import io.jmix.ui.bulk.BulkEditors;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.impl.UiTestIdsSupport;
import io.jmix.ui.component.presentation.action.PresentationActionsBuilder;
import io.jmix.ui.menu.MenuItemCommands;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.jmix.ui.navigation.UrlChangeHandler;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.settings.UserSettingService;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllerDependencyInjector;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.uidata.UiDataConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class, UiConfiguration.class, UiDataConfiguration.class,
        DynAttrConfiguration.class, DynAttrUiConfiguration.class, LocalFileStorageConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class, SecurityUiConfiguration.class,
        DatatoolsConfiguration.class, DatatoolsUiConfiguration.class
})
@PropertySource(name = "com.haulmont.cuba", value = "classpath:/com/haulmont/cuba/module.properties")
@EnableScheduling
public class CubaConfiguration {

    protected ApplicationContext applicationContext;
    protected UiControllerReflectionInspector uiControllerReflectionInspector;

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    protected void setUiControllerReflectionInspector(UiControllerReflectionInspector uiControllerReflectionInspector) {
        this.uiControllerReflectionInspector = uiControllerReflectionInspector;
    }

    @Bean("cuba_Screens")
    @Primary
    @UIScope
    protected Screens getScreens() {
        return new CubaScreens();
    }

    @Bean("cuba_Fragments")
    @Primary
    @UIScope
    protected Fragments getFragments() {
        return new CubaFragments();
    }

    @Bean("cuba_Dialogs")
    @Primary
    @UIScope
    protected Dialogs getDialogs() {
        return new CubaDialogs();
    }

    @Bean("cuba_UrlChangeHandler")
    @Primary
    @UIScope
    protected UrlChangeHandler getUrlChangeHandler() {
        return new CubaUrlChangeHandler();
    }

    @Bean("cuba_Messages")
    @Primary
    protected Messages messages() {
        return new MessagesImpl();
    }

    @Bean("cuba_MessageTools")
    @Primary
    protected MessageTools messageTools() {
        return new CubaMessageTools();
    }

    @Bean("cuba_Metadata")
    @Primary
    protected Metadata metadata(MetadataLoader metadataLoader) {
        return new CubaMetadata(metadataLoader);
    }

    @Bean("cuba_InstanceNameProvider")
    @Primary
    protected InstanceNameProvider instanceNameProvider() {
        return new CubaInstanceNameProviderImpl();
    }

    @Bean("cuba_FetchPlanRepository")
    @Primary
    protected FetchPlanRepository fetchPlanRepository() {
        return new CubaFetchPlanRepository();
    }

    @Bean("cuba_MetaModelLoader")
    @Primary
    protected MetaModelLoader metaModelLoader(DatatypeRegistry datatypes, Stores stores, FormatStringsRegistry formatStringsRegistry) {
        return new CubaMetaModelLoader(datatypes, stores, formatStringsRegistry);
    }

    @Bean("cuba_NumberIdCache")
    @Primary
    protected NumberIdCache numberIdCache() {
        return new CubaNumberIdCache();
    }

    @Bean("cuba_UiControllers")
    public UiControllersConfiguration screens(org.springframework.context.ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("com.haulmont.cuba.web.app"));
        return uiControllers;
    }

    @Bean("cuba_UiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("com.haulmont.cuba.gui.actions"));
        return actionsConfiguration;
    }

    @Bean("cuba_UiControllerDependencyInjector")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected UiControllerDependencyInjector uiControllerDependencyInjector(FrameOwner frameOwner, ScreenOptions options) {
        UiControllerDependencyInjector injector = new CubaUiControllerReflectionInspector(frameOwner, options);
        injector.setApplicationContext(applicationContext);
        injector.setReflectionInspector(uiControllerReflectionInspector);
        return injector;
    }

    @Bean("cuba_MenuItemCommands")
    @Primary
    protected MenuItemCommands menuItemCommands() {
        return new CubaMenuItemCommands();
    }

    @Bean("cuba_MetadataTools")
    @Primary
    protected MetadataTools metadataTools() {
        return new CubaMetadataTools();
    }

    @Bean("cuba_ScreenData")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected ScreenData screenData() {
        return new CubaScreenDataImpl();
    }

    @Bean("cuba_ScreenDataXmlLoader")
    @Primary
    protected ScreenDataXmlLoader screenDataXmlLoader() {
        return new CubaScreenDataXmlLoader();
    }

    @Bean("cuba_EntityStates")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected EntityStates entityStates() {
        return new EntityStates();
    }

    @Bean("cuba_FluentLoader")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected com.haulmont.cuba.core.global.FluentLoader fluentLoader(Class entityClass) {
        return new com.haulmont.cuba.core.global.FluentLoader(entityClass);
    }

    @Bean("cuba_UserSettingsTools")
    @Primary
    protected UserSettingsTools userSettingsTools() {
        return new UserSettingsToolsImpl();
    }

    @Bean("cuba_UserSettingService")
    @Primary
    protected UserSettingService userSettingService() {
        return new UserSettingServiceBean();
    }

    @Bean("cuba_Presentations")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected Presentations presentations(Component component) {
        return new PresentationsImpl(component);
    }

    @Bean("cuba_CubaPresentationActionsBuilder")
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected PresentationActionsBuilder presentationActionsBuilder(Table component, ComponentSettingsBinder settingsBinder) {
        return new CubaPresentationActionsBuilder(component, settingsBinder);
    }

    @Bean("cuba_WindowConfig")
    @Primary
    WindowConfig windowConfig() {
        return new com.haulmont.cuba.gui.config.WindowConfig();
    }

    @Bean("cuba_UiTestIdsSupport")
    @Primary
    protected UiTestIdsSupport uiTestIdsSupport() {
        return new CubaUiTestIdsSupport();
    }

    @Bean("cuba_ScreenBuilders")
    @Primary
    protected ScreenBuilders screenBuilders() {
        return new com.haulmont.cuba.gui.ScreenBuilders();
    }

    @Bean("cuba_BulkEditors")
    @Primary
    protected BulkEditors bulkEditors() {
        return new com.haulmont.cuba.gui.BulkEditors();
    }

    @Bean("cuba_ScreenTools")
    @Primary
    protected ScreenTools screenTools() {
        return new WebScreenTools();
    }

    @Bean("cuba_EditorBuilderProcessor")
    @Primary
    protected EditorBuilderProcessor editorBuilderProcessor() {
        return new com.haulmont.cuba.gui.builders.EditorBuilderProcessor();
    }

    @Bean("cuba_PropertyShortcutLoader")
    @Primary
    protected CubaPropertyShortcutLoader propertyShortcutLoader(UiComponentProperties componentProperties,
                                                                UiScreenProperties screenProperties,
                                                                CubaProperties cubaProperties) {
        return new CubaPropertyShortcutLoader(componentProperties, screenProperties, cubaProperties);
    }

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
    public void onApplicationContextRefreshFirst(ContextRefreshedEvent event) {
        AppContext.Internals.setApplicationContext(event.getApplicationContext());
    }

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 10)
    public void onApplicationContextRefreshLast(ContextRefreshedEvent event) {
        AppContext.Internals.startContext();
    }

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
    public void onApplicationContextClosedEvent(ContextClosedEvent event) {
        AppContext.Internals.onContextClosed(event.getApplicationContext());
    }
}
