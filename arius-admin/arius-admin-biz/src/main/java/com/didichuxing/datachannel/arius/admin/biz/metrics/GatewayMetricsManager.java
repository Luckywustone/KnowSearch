package com.didichuxing.datachannel.arius.admin.biz.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.gateway.GatewayOverviewMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;

/**
 * Created by fitz on 2021-08-11
 *
 *  网关看板业务类
 */
public interface GatewayMetricsManager {

    /**
     * 获取gateway不同组的指标项
     */
    Result<List<String>> getGatewayMetricsEnums(String group);

    /**
     * 获取某个projectId下的dslMd5列表
     */
    Result<List<String>> getDslMd5List(Integer projectId, Long startTime, Long endTime);

    /**
     * 获取gateway全局维度指标信息
     */
    Result<List<GatewayOverviewMetricsVO>> getGatewayOverviewMetrics(GatewayOverviewDTO dto);

    /**
     * 获取gateway节点维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getGatewayNodeMetrics(GatewayNodeDTO dto, Integer projectId);

    /**
     * 获取多节点gateway节点维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getMultiGatewayNodesMetrics(MultiGatewayNodesDTO dto, Integer projectId);

    /**
     * 获取client节点维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getClientNodeMetrics(ClientNodeDTO dto, Integer projectId);

    /**
     * 获取gateway索引维度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getGatewayIndexMetrics(GatewayIndexDTO dto, Integer projectId);

    /**
     * 获取gateway项目纬度指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getGatewayAppMetrics(GatewayProjectDTO dto);

    /**
     * 获取gatewayDSL模版查询指标信息
     */
    Result<List<VariousLineChartMetricsVO>> getGatewayDslMetrics(GatewayDslDTO dto, Integer projectId);

    /**
     * 获取clientNode ip信息
     */
    Result<List<Tuple<String, String>>> getClientNodeIdList(String gatewayNode, Long startTime, Long endTime, Integer projectId);
}