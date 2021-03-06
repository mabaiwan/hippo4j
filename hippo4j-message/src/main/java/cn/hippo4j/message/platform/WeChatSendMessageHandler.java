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

package cn.hippo4j.message.platform;

import cn.hippo4j.common.toolkit.JSONUtil;
import cn.hippo4j.common.toolkit.StringUtil;
import cn.hippo4j.message.dto.NotifyConfigDTO;
import cn.hippo4j.message.enums.NotifyPlatformEnum;
import cn.hippo4j.message.enums.NotifyTypeEnum;
import cn.hippo4j.message.service.SendMessageHandler;
import cn.hippo4j.message.request.AlarmNotifyRequest;
import cn.hippo4j.message.request.ChangeParameterNotifyRequest;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.google.common.base.Joiner;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static cn.hippo4j.message.platform.WeChatAlarmConstants.*;

/**
 * WeChat send message handler.
 */
@Slf4j
public class WeChatSendMessageHandler implements SendMessageHandler<AlarmNotifyRequest, ChangeParameterNotifyRequest> {

    @Override
    public String getType() {
        return NotifyPlatformEnum.WECHAT.name();
    }

    @Override
    public void sendAlarmMessage(NotifyConfigDTO notifyConfig, AlarmNotifyRequest alarmNotifyRequest) {
        String[] receives = notifyConfig.getReceives().split(",");
        String afterReceives = Joiner.on("><@").join(receives);
        String weChatAlarmTxt;
        String weChatAlarmTimoutReplaceTxt;
        if (Objects.equals(alarmNotifyRequest.getNotifyTypeEnum(), NotifyTypeEnum.TIMEOUT)) {
            String executeTimeoutTrace = alarmNotifyRequest.getExecuteTimeoutTrace();
            if (StringUtil.isNotBlank(executeTimeoutTrace)) {
                String weChatAlarmTimoutTraceReplaceTxt = String.format(WE_CHAT_ALARM_TIMOUT_TRACE_REPLACE_TXT, executeTimeoutTrace);
                weChatAlarmTimoutReplaceTxt = StrUtil.replace(WE_CHAT_ALARM_TIMOUT_REPLACE_TXT, WE_CHAT_ALARM_TIMOUT_TRACE_REPLACE_TXT, weChatAlarmTimoutTraceReplaceTxt);
            } else {
                weChatAlarmTimoutReplaceTxt = StrUtil.replace(WE_CHAT_ALARM_TIMOUT_REPLACE_TXT, WE_CHAT_ALARM_TIMOUT_TRACE_REPLACE_TXT, "");
            }
            weChatAlarmTimoutReplaceTxt = String.format(weChatAlarmTimoutReplaceTxt, alarmNotifyRequest.getExecuteTime(), alarmNotifyRequest.getExecuteTimeOut());
            weChatAlarmTxt = StrUtil.replace(WE_CHAT_ALARM_TXT, WE_CHAT_ALARM_TIMOUT_REPLACE_TXT, weChatAlarmTimoutReplaceTxt);
        } else {
            weChatAlarmTxt = StrUtil.replace(WE_CHAT_ALARM_TXT, WE_CHAT_ALARM_TIMOUT_REPLACE_TXT, "");
        }

        String text = String.format(
                weChatAlarmTxt,
                // ??????
                alarmNotifyRequest.getActive(),
                // ????????????
                alarmNotifyRequest.getNotifyTypeEnum(),
                // ?????????ID
                alarmNotifyRequest.getThreadPoolId(),
                // ????????????
                alarmNotifyRequest.getAppName(),
                // ????????????
                alarmNotifyRequest.getIdentify(),
                // ???????????????
                alarmNotifyRequest.getCorePoolSize(),
                // ???????????????
                alarmNotifyRequest.getMaximumPoolSize(),
                // ???????????????
                alarmNotifyRequest.getPoolSize(),
                // ???????????????
                alarmNotifyRequest.getActiveCount(),
                // ???????????????
                alarmNotifyRequest.getLargestPoolSize(),
                // ?????????????????????
                alarmNotifyRequest.getCompletedTaskCount(),
                // ??????????????????
                alarmNotifyRequest.getQueueName(),
                // ????????????
                alarmNotifyRequest.getCapacity(),
                // ??????????????????
                alarmNotifyRequest.getQueueSize(),
                // ??????????????????
                alarmNotifyRequest.getRemainingCapacity(),
                // ??????????????????
                alarmNotifyRequest.getRejectedExecutionHandlerName(),
                // ??????????????????
                alarmNotifyRequest.getRejectCountNum(),
                // ???????????????
                afterReceives,
                // ????????????
                notifyConfig.getInterval(),
                // ????????????
                DateUtil.now());
        execute(notifyConfig.getSecretKey(), text);
    }

    @Override
    public void sendChangeMessage(NotifyConfigDTO notifyConfig, ChangeParameterNotifyRequest changeParameterNotifyRequest) {
        String threadPoolId = changeParameterNotifyRequest.getThreadPoolId();
        String[] receives = notifyConfig.getReceives().split(",");
        String afterReceives = Joiner.on("><@").join(receives);
        String text = String.format(
                WE_CHAT_NOTICE_TXT,
                // ??????
                changeParameterNotifyRequest.getActive(),
                // ???????????????
                threadPoolId,
                // ????????????
                changeParameterNotifyRequest.getAppName(),
                // ????????????
                changeParameterNotifyRequest.getIdentify(),
                // ???????????????
                changeParameterNotifyRequest.getBeforeCorePoolSize() + "  ???  " + changeParameterNotifyRequest.getNowCorePoolSize(),
                // ???????????????
                changeParameterNotifyRequest.getBeforeMaximumPoolSize() + "  ???  " + changeParameterNotifyRequest.getNowMaximumPoolSize(),
                // ??????????????????
                changeParameterNotifyRequest.getBeforeAllowsCoreThreadTimeOut() + "  ???  " + changeParameterNotifyRequest.getNowAllowsCoreThreadTimeOut(),
                // ??????????????????
                changeParameterNotifyRequest.getBeforeKeepAliveTime() + "  ???  " + changeParameterNotifyRequest.getNowKeepAliveTime(),
                // ??????????????????
                changeParameterNotifyRequest.getBeforeExecuteTimeOut() + "  ???  " + changeParameterNotifyRequest.getNowExecuteTimeOut(),
                // ????????????
                changeParameterNotifyRequest.getBlockingQueueName(),
                // ??????????????????
                changeParameterNotifyRequest.getBeforeQueueCapacity() + "  ???  " + changeParameterNotifyRequest.getNowQueueCapacity(),
                // ????????????
                changeParameterNotifyRequest.getBeforeRejectedName(),
                changeParameterNotifyRequest.getNowRejectedName(),
                // ???????????????
                afterReceives,
                // ????????????
                DateUtil.now());
        execute(notifyConfig.getSecretKey(), text);
    }

    /**
     * Execute.
     *
     * @param secretKey
     * @param text
     */
    private void execute(String secretKey, String text) {
        String serverUrl = WE_CHAT_SERVER_URL + secretKey;
        try {
            WeChatReqDTO weChatReq = new WeChatReqDTO();
            weChatReq.setMsgtype("markdown");
            Markdown markdown = new Markdown();
            markdown.setContent(text);
            weChatReq.setMarkdown(markdown);
            HttpRequest.post(serverUrl).body(JSONUtil.toJSONString(weChatReq)).execute();
        } catch (Exception ex) {
            log.error("WeChat failed to send message", ex);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class WeChatReqDTO {

        private String msgtype;

        private Markdown markdown;
    }

    @Data
    public static class Markdown {

        private String content;
    }
}
