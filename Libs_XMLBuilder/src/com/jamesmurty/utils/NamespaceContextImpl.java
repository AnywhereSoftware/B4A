
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
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
 
 package com.jamesmurty.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Element;

import anywheresoftware.b4a.BA.Hide;

/**
 * Mappings between prefix strings and namespace URI strings, as required to
 * perform XPath queries on namespaced XML documents.
 *
 * @author jmurty
 */
@Hide
public class NamespaceContextImpl implements NamespaceContext {
    protected Element element = null;
    protected Map<String, String> prefixToNsUriMap = new HashMap<String, String>();
    protected Map<String, Set<String>> nsUriToPrefixesMap = new HashMap<String, Set<String>>();

    /**
     * Create an empty namespace context.
     */
    public NamespaceContextImpl() {
    }

    /**
     * Create a namespace context that will lookup namespace
     * information in the given element.
     */
    public NamespaceContextImpl(Element element) {
        this.element = element;
    }

    /**
     * Add a custom mapping from prefix to a namespace. This mapping will
     * override any mappings present in this class's XML Element (if provided).
     *
     * @param prefix
     * the namespace's prefix. Use an empty string for the
     * default prefix.
     * @param namespaceURI
     * the namespace URI to map.
     */
    public void addNamespace(String prefix, String namespaceURI) {
        this.prefixToNsUriMap.put(prefix, namespaceURI);
        if (this.nsUriToPrefixesMap.get(namespaceURI) == null) {
            this.nsUriToPrefixesMap.put(namespaceURI, new HashSet<String>());
        }
        this.nsUriToPrefixesMap.get(namespaceURI).add(prefix);
    }

    public String getNamespaceURI(String prefix) {
        String namespaceURI = this.prefixToNsUriMap.get(prefix);
        if (namespaceURI == null && this.element != null) {
            // Need null to find default namespace, not an empty string
            if (prefix != null && prefix.length() == 0) {
                prefix = null;
            }
            namespaceURI = this.element.lookupNamespaceURI(prefix);
        }
        return namespaceURI;
    }

    public String getPrefix(String namespaceURI) {
        Set<String> prefixes = this.nsUriToPrefixesMap.get(namespaceURI);
        if (prefixes != null && prefixes.size() > 0) {
            return prefixes.iterator().next();
        }
        if (this.element != null) {
            return this.element.lookupPrefix(namespaceURI);
        }
        return null;
    }

    // No implemented
    @SuppressWarnings("unchecked")
    public Iterator getPrefixes(String namespaceURI) {
        return Collections.EMPTY_LIST.iterator();
    }

}
