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

package de.hasait.common.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Optional;

public class XmlUtil {

    private XmlUtil() {
        super();
    }

    public static Optional<Element> getFirstElement(Element element, String... tagNames) {
        Element current = element;
        for (String tagName : tagNames) {
            NodeList nodeList = element.getElementsByTagName(tagName);
            if (nodeList.getLength() == 0) {
                return Optional.empty();
            }
            current = (Element) nodeList.item(0);
        }
        return Optional.of(current);
    }

    public static int getIntAttribute(Element element, String attributeName) {
        return Integer.parseInt(element.getAttribute(attributeName));
    }

}
