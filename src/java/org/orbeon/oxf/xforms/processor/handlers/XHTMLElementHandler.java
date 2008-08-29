/**
 *  Copyright (C) 2008 Orbeon, Inc.
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version
 *  2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms.processor.handlers;

import org.orbeon.oxf.xforms.ControlTree;
import org.orbeon.oxf.xforms.control.controls.XXFormsAttributeControl;
import org.orbeon.oxf.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Handle xhtml:* when AVTs are turned on.
 */
public class XHTMLElementHandler extends XFormsBaseHandler {
    public XHTMLElementHandler() {
        super(false, true);
    }

    public void start(String uri, String localname, String qName, Attributes attributes) throws SAXException {

        // Start xhtml:* element
        final ContentHandler contentHandler = handlerContext.getController().getOutput();

        final String id = attributes.getValue("id");
        if (id != null) {
            final String effectiveId = handlerContext.getEffectiveId(attributes);

            final ControlTree controlState = containingDocument.getControls().getCurrentControlTree();
            final boolean hasAVT = controlState.hasAttributeControl(effectiveId);
            if (hasAVT) {
                // This XHTML element has at least one AVT so process its attributes

                final int attributesCount = attributes.getLength();
                boolean found = false;
                for (int i = 0; i < attributesCount; i++) {
                    final String attributeValue = attributes.getValue(i);
                    if (attributeValue.indexOf('{') != -1) {
                        // This is an AVT
                        found = true;

                        final String attributeLocalName = attributes.getLocalName(i);
                        final String attributeQName = attributes.getQName(i);// use qualified name so we match on "xml:lang"
                        final XXFormsAttributeControl attributeControl = controlState.getAttributeControl(effectiveId, attributeQName);

                        // Find effective attribute value
                        final String effectiveAttributeValue;
                        if (attributeControl != null) {
                            effectiveAttributeValue = attributeControl.getExternalValue(pipelineContext);
                        } else {
                            // Use blank value
                            effectiveAttributeValue = "";
                        }

                        // Set the value of the attribute
                        attributes = XMLUtils.addOrReplaceAttribute(attributes, attributes.getURI(i),
                                XMLUtils.prefixFromQName(attributeQName), attributeLocalName, effectiveAttributeValue);
                    }
                }

                if (found) {
                    // Update the value of the id attribute
                    attributes = XMLUtils.addOrReplaceAttribute(attributes, "", "", "id", effectiveId);
                }
            }
        }

        contentHandler.startElement(uri, localname, qName, attributes);
    }

    public void end(String uri, String localname, String qName) throws SAXException {
        // Close xhtml:*
        final ContentHandler contentHandler = handlerContext.getController().getOutput();
        contentHandler.endElement(uri, localname, qName);
    }
}
