package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayExpandTaskHandler")
public class GatewayExpandTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayExpandContent content     = convertString2Content(param.getExpandData());
				final Integer              componentId = content.getComponentId();
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentById(
						componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				
				return expand(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				final String name = componentService.queryComponentById(
						gatewayExpandContent.getComponentId()).getData();
				return String.format("%s【%s】",  operationType().getMessage(),name);
		}
		
		@Override
		protected GatewayExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayExpandContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		protected String getName(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				return componentService.queryComponentById(gatewayExpandContent.getComponentId()).getData();
		}
}