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

package de.hasait.common.ui.puif;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import de.hasait.common.util.ReflectionUtil;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class TextAreaForStringPuiFactory extends AbstractPuiFactory<String, String, TextArea, TextAreaForStringPuiConfig> {

    public TextAreaForStringPuiFactory() {
        super(String.class, 0, TextAreaForStringPuiConfig::new);
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, TextAreaForStringPuiConfig context) {
        if (super.canHandle(beanClass, propertyDescriptor, context)) {
            TextAreaForStringPui annotation = ReflectionUtil.findAnnotation(beanClass, propertyDescriptor, TextAreaForStringPui.class);
            if (annotation != null) {
                context.height = annotation.height();
                return true;
            }
        }
        return false;
    }

    @Override
    protected TextArea createAndAddField(FormLayout formLayout, String caption, TextAreaForStringPuiConfig context) {
        TextArea textArea = new TextArea(caption);
        textArea.setHeight(context.height);
        formLayout.add(textArea, 1);
        return textArea;
    }

    @Override
    protected <T> Binder.BindingBuilder<T, String> customizeBinding(Binder.BindingBuilder<T, String> bindingBuilder, TextAreaForStringPuiConfig context) {
        return bindingBuilder;
    }

}
