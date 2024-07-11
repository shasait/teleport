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

package de.hasait.common.util.xml;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Optional;

public class XmlElement {

    private final Element element;

    XmlElement(Element element) {
        this.element = element;
    }

    public String getTagName() {
        return element.getTagName();
    }

    public String getNamespaceURI() {
        return element.getNamespaceURI();
    }

    public boolean hasAttribute(String name) {
        return element.hasAttribute(name);
    }

    public String getAttribute(String name) {
        return element.getAttribute(name);
    }

    public int getAttributeAsInt(String name) {
        return Integer.parseInt(getAttribute(name));
    }

    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        return element.getAttributeNS(namespaceURI, localName);
    }

    public String getTextContent() {
        return element.getTextContent();
    }

    public int getTextContentAsInt() {
        return Integer.parseInt(getTextContent());
    }

    public XmlElements getElementsByTagName(String tagName) {
        return new XmlElements(element.getElementsByTagName(tagName));
    }

    public Optional<XmlElement> getFirstElement(String... tagNames) {
        if (tagNames == null || tagNames.length == 0) {
            return Optional.empty();
        }
        Optional<XmlElement> optional = Optional.of(this);
        int i = 0;
        while (i < tagNames.length && optional.isPresent()) {
            optional = optional.orElseThrow().getSingleFirstElement(tagNames[i++]);
        }
        return optional;
    }

    private Optional<XmlElement> getSingleFirstElement(String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        return nodeList.getLength() == 0 ? Optional.empty() : Optional.of(new XmlElement((Element) nodeList.item(0)));
    }

}
