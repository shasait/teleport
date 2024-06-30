/*
 * Copyright (C) 2024 by Sebastian Hasait (sebastian at hasait dot de)
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

package de.hasait.common.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.internal.LocaleUtil;
import com.vaadin.flow.router.RouterLink;

import java.util.Optional;

public class VaadinUtil {

    public static String getTranslation(String key, Object... params) {
        final Optional<I18NProvider> i18NProvider = LocaleUtil
                .getI18NProvider();
        return i18NProvider
                .map(i18n -> i18n.getTranslation(key,
                        LocaleUtil.getLocale(() -> i18NProvider), params))
                .orElseGet(() -> "!{" + key + "}!");
    }

    public static String getApplicationTitle() {
        return getTranslation("application.title");
    }

    public static String getPageTitle(String pageKey, String pageType) {
        return getTranslation(pageKey + "." + pageType + ".title");
    }

    public static String getPageTitle(Class<?> dataClass, String pageType) {
        return getPageTitle(dataClass.getSimpleName(), pageType);
    }

    public static String getApplicationAndPageTitle(String pageKey, String pageType) {
        return getApplicationTitle() + " | " + getPageTitle(pageKey, pageType);
    }

    public static String getApplicationAndPageTitle(Class<?> clazz, String pageType) {
        return getApplicationAndPageTitle(clazz.getSimpleName(), pageType);
    }

    public static void addDataViewRouterLink(HasComponents hasComponents, Class<?> dataClass, String pageType, Class<? extends Component> viewClass) {
        RouterLink routerLink = new RouterLink(VaadinUtil.getPageTitle(dataClass, pageType), viewClass);
        hasComponents.add(routerLink);
    }

}
