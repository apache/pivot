/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
importPackage(org.apache.pivot.wtk);

var dragSoure = new DragSource() {
    beginDrag: function(component, x, y) {
        return true;
    },

    endDrag: function(component, dropAction) {
        // No-op
    },

    getContent: function() {
        var content = new LocalManifest();
        content.putImage(imageView.getImage());

        return content;
    },

    getOffset: function() {
        // No-op; not used for native drags
        return null;
    },

    getRepresentation: function() {
        // No-op; not used for native drags
        return null;
    },

    getSupportedDropActions: function() {
        return DropAction.COPY.getMask();
    },

    isNative: function() {
        return true;
    }
};

var dropTarget = new DropTarget() {
    dragEnter: function(component, dragContent, supportedDropActions, userDropAction) {
        return (dragContent.containsImage()) ? DropAction.COPY : null;
    },

    dragExit: function(component) {
        // No-op
    },

    dragMove: function(component, dragContent, supportedDropActions, x, y, userDropAction) {
        return (dragContent.containsImage()) ? DropAction.COPY : null;
    },

    userDropActionChange: function(component, dragContent, supportedDropActions,
        x, y, userDropAction) {
        return (dragContent.containsImage()) ? DropAction.COPY : null;
    },

    drop: function(component, dragContent, supportedDropActions, x, y, userDropAction) {
        var dropAction = null;

        if (dragContent.containsImage()) {
            imageView.setImage(dragContent.getImage());
            dropAction = DropAction.COPY;
        }

        return dropAction;
    }
};
