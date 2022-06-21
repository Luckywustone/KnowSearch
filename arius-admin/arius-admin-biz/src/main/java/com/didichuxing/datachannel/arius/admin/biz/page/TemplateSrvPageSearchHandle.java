package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.UnavailableTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Component
public class TemplateSrvPageSearchHandle extends AbstractPageSearchHandle<TemplateQueryDTO, TemplateWithSrvVO> {
    private static final FutureUtil<Void> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL = FutureUtil.init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL", 10, 10, 100);
    private static final FutureUtil<Void> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL = FutureUtil.init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL", 10, 10, 100);

    @Autowired
    private IndexTemplateService          indexTemplateService;

    @Autowired
    private IndexTemplatePhyService       indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;

    @Override
    protected Result<Boolean> checkCondition(TemplateQueryDTO condition, Integer appId) {

        String templateName = condition.getName();
        if (!AriusObjUtils.isBlack(templateName) && (templateName.startsWith("*") || templateName.startsWith("?"))) {
            return Result.buildParamIllegal("模板名称不能以*或者?开头");
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    protected void initCondition(TemplateQueryDTO condition, Integer appId) {
        // nothing to do
    }

    @Override
    protected PaginationResult<TemplateWithSrvVO> buildPageData(TemplateQueryDTO condition, Integer appId) {
        // 注意这里的condition是物理集群
        Integer totalHit;
        List<IndexTemplate> matchIndexTemplateList;
        if (AriusObjUtils.isBlank(condition.getCluster())) {
            matchIndexTemplateList = indexTemplateService.pagingGetTemplateSrvByCondition(condition);
            totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();
        } else {
            List<IndexTemplate> meetConditionTemplateList = getMatchConditionTemplateListByClusterName(condition);
            totalHit = meetConditionTemplateList.size();
            matchIndexTemplateList = filterFullDataByPage(meetConditionTemplateList, condition);
        }

        List<TemplateWithSrvVO> templateWithSrvVOList = buildExtraAttribute(matchIndexTemplateList);
        return PaginationResult.buildSucc(templateWithSrvVOList, totalHit, condition.getPage(), condition.getSize());
    }
    /******************************************private***********************************************/
    /**
     * 根据模板Id、名称、归属AppId、归属物理集群等进行组合查询
     *
     * @param condition
     * @return
     */
    private List<IndexTemplate> getMatchConditionTemplateListByClusterName(TemplateQueryDTO condition) {
        List<IndexTemplate> meetConditionTemplateList = Lists.newArrayList();
        List<IndexTemplatePhy> indexTemplatePhyList = indexTemplatePhyService.getNormalTemplateByCluster(condition.getCluster());
        if (CollectionUtils.isEmpty(indexTemplatePhyList)) { return meetConditionTemplateList;}

        List<Integer> matchTemplateLogicIdList = indexTemplatePhyList.stream().map(IndexTemplatePhy::getLogicId).distinct().collect(Collectors.toList());

        List<IndexTemplate> matchIndexTemplates = indexTemplateService.listLogicTemplatesByIds(matchTemplateLogicIdList);
        if (null != condition.getId()) {
            matchIndexTemplates = matchIndexTemplates.stream().filter(r -> r.getId().equals(condition.getId())).collect(Collectors.toList());
        }

        if (!AriusObjUtils.isBlack(condition.getName())) {
            matchIndexTemplates = matchIndexTemplates.stream().filter(r -> r.getName().contains(condition.getName())).collect(Collectors.toList());
        }

        if (null != condition.getAppId()) {
            matchIndexTemplates = matchIndexTemplates.stream().filter(r -> r.getAppId().equals(condition.getAppId())).collect(Collectors.toList());
        }
        return matchIndexTemplates;
    }

    private List<TemplateWithSrvVO> buildExtraAttribute(List<IndexTemplate> templateList) {
        if (CollectionUtils.isEmpty(templateList)) { return Lists.newArrayList();}
        List<TemplateWithSrvVO> templateWithSrvVOList = new ArrayList<>();
        // 构建基础信息
        for (IndexTemplate template : templateList) {
            TemplateWithSrvVO templateWithSrvVO = ConvertUtil.obj2Obj(template, TemplateWithSrvVO.class);
            templateWithSrvVO.setOpenSrv(ConvertUtil.list2List(TemplateSrv.codeStr2SrvList(template.getOpenSrv()), TemplateSrvVO.class));
            templateWithSrvVOList.add(templateWithSrvVO);
        }

        buildTemplateCluster(templateWithSrvVOList);
        buildTemplateUnavailableSrv(templateWithSrvVOList);
        return templateWithSrvVOList;
    }

    /**
     * 获取额外信息：
     * 1. 模板归属集群名称
     * @param templateWithSrvVOList    templateWithSrvVOList
     */
    private void buildTemplateCluster(List<TemplateWithSrvVO> templateWithSrvVOList) {
        for (TemplateWithSrvVO templateSrvVO : templateWithSrvVOList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                Set<String> clusterNameList = indexTemplatePhyService.getTemplateByLogicId(templateSrvVO.getId())
                        .stream()
                        .map(IndexTemplatePhy::getCluster)
                        .collect(Collectors.toSet());

                templateSrvVO.setCluster(Lists.newArrayList(clusterNameList));
            });
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.waitExecute();
    }

    /**
     * 构建不支持的模板服务列表（模板服务最低版本与模板归属物理集群版本比对）
     * @param templateWithSrvVOList     templateWithSrvVOList
     */
    private void buildTemplateUnavailableSrv(List<TemplateWithSrvVO> templateWithSrvVOList) {
        for (TemplateWithSrvVO templateSrvVO : templateWithSrvVOList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL.runnableTask(() -> {
                templateSrvVO.setUnavailableSrv(ConvertUtil.list2List(templateSrvManager.getUnavailableSrv(templateSrvVO.getId()), UnavailableTemplateSrvVO.class));
            });
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL.waitExecute();
    }

    /**
     * 对全量查询结果根据分页条件进行过滤
     *
     * @param condition 分页条件
     * @param source    全量查询结果
     * @return
     */
    <T> List<T> filterFullDataByPage(List<T> source, PageDTO condition) {
        //这里页码和前端对应起来，第一页页码是1 而不是0
        long fromIndex = condition.getSize() * (condition.getPage() - 1);
        long toIndex = getLastPageSize(condition, source.size());
        return source.subList((int) fromIndex, (int) toIndex);
    }

    /**
     * 获取最后一条数据的index，以防止数组溢出
     *
     * @param condition      分页条件
     * @param pageSizeFromDb 查询结果
     * @return
     */
    long getLastPageSize(PageDTO condition, Integer pageSizeFromDb) {
        //分页最后一条数据的index
        long size = condition.getPage() * condition.getSize();
        if (pageSizeFromDb < size) {
            size = pageSizeFromDb;
        }
        return size;
    }
}
