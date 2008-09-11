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
package pivot.wtk.effects.easing;

/**
 *
 *
 * @author gbrown
 */
public class Back implements Easing {
    private float overshoot;

    public Back() {
        this(1.70158f);
    }

    public Back(float overshoot) {
        this.overshoot = overshoot;
    }

    public float easeIn(float time, float begin, float change, float duration) {
        return change * (time /= duration) * time * ((overshoot + 1) * time - overshoot) + begin;
    }

    public float easeInOut(float time, float begin, float change, float duration) {
        return change * ((time = time / duration - 1) * time * ((overshoot + 1) * time + overshoot) + 1) + begin;
    }

    public float easeOut(float time, float begin, float change, float duration) {
        if ((time /= duration / 2) < 1) {
            return change / 2 * (time * time * (((overshoot *= (1.525)) + 1) * time - overshoot)) + begin;
        } else {
            return change / 2 * ((time -= 2) * time * (((overshoot *= (1.525)) + 1) * time + overshoot) + 2) + begin;
        }
    }

}
