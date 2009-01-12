/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text;

/**
 * Element representing an inline run of text.
 * <p>
 * TODO Text alignment style/enum.
 *
 * @author gbrown
 */
public class Span extends Element {
    public Span() {
        super();
    }

    public Span(Span span, boolean recursive) {
        super(span, recursive);
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new Span(this, recursive);
    }
}
