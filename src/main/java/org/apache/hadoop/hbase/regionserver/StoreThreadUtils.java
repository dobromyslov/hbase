/*
 * Copyright 2010 The Apache Software Foundation
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.regionserver;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.util.Threads;

/**
 * Thread utilities which are used while creating a thread pool to
 * load store files
 */
public class StoreThreadUtils {
  public static ThreadPoolExecutor getStoreOpenAndCloseThreadPool(
      final String threadNamePrefix, HRegionInfo regionInfo, Configuration conf) {
    int numStores = Math.max(1, regionInfo.getTableDesc().families.size());
    int maxThreads = Math.min(numStores,
        conf.getInt(HConstants.HSTORE_OPEN_AND_CLOSE_THREADS_MAX,
            HConstants.DEFAULT_HSTORE_OPEN_AND_CLOSE_THREADS_MAX));
    return getOpenAndCloseThreadPool(maxThreads, threadNamePrefix);
  }

  public static ThreadPoolExecutor getStoreFileOpenAndCloseThreadPool(
      final String threadNamePrefix, HRegionInfo regionInfo, Configuration conf) {
    int numStores = Math.max(1, regionInfo.getTableDesc().families.size());
    int maxThreads = Math.max(1,
        conf.getInt(HConstants.HSTORE_OPEN_AND_CLOSE_THREADS_MAX,
            HConstants.DEFAULT_HSTORE_OPEN_AND_CLOSE_THREADS_MAX)
            / numStores);
    return getOpenAndCloseThreadPool(maxThreads, threadNamePrefix);
  }

  private static ThreadPoolExecutor getOpenAndCloseThreadPool(int maxThreads,
      final String threadNamePrefix) {
    ThreadPoolExecutor openAndCloseThreadPool = Threads
        .getBoundedCachedThreadPool(maxThreads, 30L, TimeUnit.SECONDS,
            new ThreadFactory() {
              private int count = 1;

              public Thread newThread(Runnable r) {
                Thread t = new Thread(r, threadNamePrefix + "-" + count++);
                t.setDaemon(true);
                return t;
              }
            });
    return openAndCloseThreadPool;
  }
}