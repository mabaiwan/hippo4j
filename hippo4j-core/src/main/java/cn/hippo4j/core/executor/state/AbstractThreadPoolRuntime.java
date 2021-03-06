/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hippo4j.core.executor.state;

import cn.hippo4j.common.model.ThreadPoolRunStateInfo;
import cn.hippo4j.core.executor.DynamicThreadPoolExecutor;
import cn.hippo4j.core.executor.DynamicThreadPoolWrapper;
import cn.hippo4j.core.executor.manage.GlobalThreadPoolManage;
import cn.hippo4j.common.toolkit.CalculateUtil;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract threadPool runtime info.
 *
 * @author chen.ma
 * @date 2021/12/7 19:44
 */
public abstract class AbstractThreadPoolRuntime {

    /**
     * Supplement.
     *
     * @param threadPoolRunStateInfo
     * @return
     */
    public abstract ThreadPoolRunStateInfo supplement(ThreadPoolRunStateInfo threadPoolRunStateInfo);

    /**
     * Get pool run state.
     *
     * @param threadPoolId
     * @return
     */
    public ThreadPoolRunStateInfo getPoolRunState(String threadPoolId) {
        DynamicThreadPoolWrapper executorService = GlobalThreadPoolManage.getExecutorService(threadPoolId);
        ThreadPoolExecutor pool = executorService.getExecutor();
        return getPoolRunState(threadPoolId, pool);
    }

    /**
     * Get pool run state.
     *
     * @param threadPoolId
     * @param executor
     * @return
     */
    public ThreadPoolRunStateInfo getPoolRunState(String threadPoolId, Executor executor) {
        ThreadPoolRunStateInfo stateInfo = new ThreadPoolRunStateInfo();
        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        // ???????????????
        int corePoolSize = pool.getCorePoolSize();
        // ???????????????
        int maximumPoolSize = pool.getMaximumPoolSize();
        // ???????????????????????? (??????)
        int poolSize = pool.getPoolSize();
        // ??????????????? (??????)
        int activeCount = pool.getActiveCount();
        // ???????????????????????????????????? (??????)
        int largestPoolSize = pool.getLargestPoolSize();
        // ????????????????????????????????? (??????)
        long completedTaskCount = pool.getCompletedTaskCount();
        // ????????????
        String currentLoad = CalculateUtil.divide(activeCount, maximumPoolSize) + "";
        // ????????????
        String peakLoad = CalculateUtil.divide(largestPoolSize, maximumPoolSize) + "";
        BlockingQueue<Runnable> queue = pool.getQueue();
        // ??????????????????
        int queueSize = queue.size();
        // ????????????
        String queueType = queue.getClass().getSimpleName();
        // ??????????????????
        int remainingCapacity = queue.remainingCapacity();
        // ????????????
        int queueCapacity = queueSize + remainingCapacity;
        stateInfo.setCoreSize(corePoolSize);
        stateInfo.setTpId(threadPoolId);
        stateInfo.setPoolSize(poolSize);
        stateInfo.setMaximumSize(maximumPoolSize);
        stateInfo.setActiveSize(activeCount);
        stateInfo.setCurrentLoad(currentLoad);
        stateInfo.setPeakLoad(peakLoad);
        stateInfo.setQueueType(queueType);
        stateInfo.setQueueSize(queueSize);
        stateInfo.setQueueCapacity(queueCapacity);
        stateInfo.setQueueRemainingCapacity(remainingCapacity);
        stateInfo.setLargestPoolSize(largestPoolSize);
        stateInfo.setCompletedTaskCount(completedTaskCount);
        long rejectCount =
                pool instanceof DynamicThreadPoolExecutor ? ((DynamicThreadPoolExecutor) pool).getRejectCountNum() : -1L;
        stateInfo.setRejectCount(rejectCount);
        stateInfo.setClientLastRefreshTime(DateUtil.formatDateTime(new Date()));
        stateInfo.setTimestamp(System.currentTimeMillis());
        return supplement(stateInfo);
    }
}
