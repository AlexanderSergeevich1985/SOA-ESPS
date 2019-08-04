/**The MIT License (MIT)
 Copyright (c) 2019 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.core.Utils.DataStructure;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

public class LRUInMemoryCache<ID extends Serializable, T> extends AbstractInMemoryCache<ID, T> {
    @Override
    protected void updateStats(final CacheKey<ID> key) {
        LocalDateTime time = LocalDateTime.now();
        key.setLastUpdate(time);
    }

    @Override
    public Comparator<CacheKey<ID>> createComparator() {
        return (o1, o2) -> {
            if (o1.getKey().equals(o2.getKey())) {
                return 0;
            }
            if (o1.getLastUpdate() == null) {
                return 1;
            }
            int value = o1.getLastUpdate().compareTo(o2.getLastUpdate());
            return value != 0 ? value : 1;
        };
    }
}