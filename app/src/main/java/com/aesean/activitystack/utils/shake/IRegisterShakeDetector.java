/*
 *    Copyright (C) 2017.  Aesean
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.aesean.activitystack.utils.shake;

import java.util.Map;

/**
 * IRegisterShakeDetector
 *
 * @author xl
 * @version V1.0
 * @since 23/12/2016
 */
@SuppressWarnings("WeakerAccess")
public interface IRegisterShakeDetector {
    void registerShakeDetector(final Map<String, Object> map);
}
