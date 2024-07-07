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

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import de.hasait.common.util.Unit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;

@Service
public class BytesPuiFactory extends AbstractTextFieldForNumberPuiFactory<Long, Void> {

    public BytesPuiFactory() {
        super(Long.class, 1000, Long.TYPE, () -> null, new Converter<>() {
            @Override
            public String convertToPresentation(Long longValue, ValueContext context) {
                if (longValue == null) {
                    return null;
                }
                return Unit.B1024.toHuman(longValue);
            }

            @Override
            public Result<Long> convertToModel(String stringValue, ValueContext context) {
                if (StringUtils.isBlank(stringValue)) {
                    return Result.ok(null);
                }

                long longValue;
                try {
                    longValue = Unit.B1024.fromHuman(stringValue);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
                return Result.ok(longValue);
            }
        });
    }

    @Override
    protected boolean canHandle(Class<?> beanClass, PropertyDescriptor propertyDescriptor, Void context) {
        return super.canHandle(beanClass, propertyDescriptor, context) && propertyDescriptor.getName().endsWith("Bytes");
    }

}
