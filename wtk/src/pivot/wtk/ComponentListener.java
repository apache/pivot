/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.wtk;

public interface ComponentListener {
    public void parentChanged(Component component, Container previousParent);
    public void sizeChanged(Component component, int previousWidth, int previousHeight);
    public void locationChanged(Component component, int previousX, int previousY);
    public void visibleChanged(Component component);
    public void decoratorChanged(Component component, Decorator previousDecorator);
    public void cursorChanged(Component component, Cursor previousCursor);
    public void tooltipTextChanged(Component component, String previousTooltipText);
    public void dragHandlerChanged(Component component, DragHandler previousDragHandler);
    public void dropHandlerChanged(Component component, DropHandler previousDropHandler);
}
