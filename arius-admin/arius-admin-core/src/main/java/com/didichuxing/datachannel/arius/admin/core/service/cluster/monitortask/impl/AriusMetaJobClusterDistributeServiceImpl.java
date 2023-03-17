package com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.AriusMetaJobClusterDistribute;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.AriusMetaJobClusterDistributeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.AriusMetaJobClusterDistributeDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * @date    3/21/22
 * @author linyunan
 */
@Service
public class AriusMetaJobClusterDistributeServiceImpl implements AriusMetaJobClusterDistributeService {
    private static final ILog                LOGGER = LogFactory.getLog(AriusMetaJobClusterDistributeServiceImpl.class);

    /**
     * maxPoolSize，当前monitor-job能支持的最大集群采集个数，
     * 超过maxPoolSize的集群不会被采集，保证maxPoolSize个集群采集的稳定性
     */
    @Value("${monitorJob.threadPool.maxsize:30}")
    private int                              maxPoolSize;

    @Autowired
    private ClusterPhyService                clusterPhyService;

    @Autowired
    private AriusMetaJobClusterDistributeDAO ariusMetaJobClusterDistributeDAO;

     /**
     * 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
     * @param monitorHost            采集机器名称
     * @return List<ClusterPhy>      采集集群列表
     */
    @Override
    public List<ClusterPhy> getSingleMachineMonitorCluster(String monitorHost) {
        List<ClusterPhy> monitorCluster = Lists.newArrayList();
        List<ClusterPhy> clusterPhyList = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            LOGGER.warn("class=AriusMetaJobClusterDistributeServiceImpl||method=getSingleMachineMonitorCluster||"
                        + "msg=clusterPhyList is empty");
            return monitorCluster;
        }

        List<AriusMetaJobClusterDistribute> ariusMetaJobClusterDistributes = getTaskByHost(monitorHost, maxPoolSize);
        if (CollectionUtils.isEmpty(ariusMetaJobClusterDistributes)) {
            LOGGER.warn("class=AriusMetaJobClusterDistributeServiceImpl||method=getSingleMachineMonitorCluster||"
                        + "msg=clusterMonitorTaskPOS is empty");
        } else {
            Map<String, AriusMetaJobClusterDistribute> taskMap = ariusMetaJobClusterDistributes.stream()
                .collect(Collectors.toMap(AriusMetaJobClusterDistribute::getCluster, c -> c));

            for (ClusterPhy clusterPhy : clusterPhyList) {
                if (null != taskMap.get(clusterPhy.getCluster())) {
                    monitorCluster.add(clusterPhy);
                }
            }
        }

        LOGGER.info(
            "class=AriusMetaJobClusterDistributeServiceImpl||method=getSingleMachineMonitorCluster||monitorCluster={}",
            JSON.toJSONString(monitorCluster));
        return monitorCluster;
    }

     /**
     * 根据host获取监控采集的集群名称列表
     * @param monitorHost 采集机器名称
     * @param size 采集个数
     * @return 监控的集群信息
     */
    @Override
    public List<AriusMetaJobClusterDistribute> getTaskByHost(String monitorHost, int size) {
        return ConvertUtil.list2List(ariusMetaJobClusterDistributeDAO.getTaskByHost(monitorHost, size),
            AriusMetaJobClusterDistribute.class);
    }
}
