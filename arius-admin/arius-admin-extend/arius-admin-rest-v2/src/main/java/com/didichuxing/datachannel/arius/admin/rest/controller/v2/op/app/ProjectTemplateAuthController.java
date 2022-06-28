package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.project.ProjectLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.project.ProjectTemplateAuthVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ V2_OP + "/app/auth",V3_OP + "/app/auth/template" })
@Api(tags = "OP-运维侧App模板权限接口(REST)")
@Deprecated
public class ProjectTemplateAuthController {

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ProjectLogicTemplateAuthManager projectLogicTemplateAuthManager;

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    public Result<List<ProjectTemplateAuthVO>> getAppTemplateAuths(@RequestParam("projectId") Integer projectId) {
        List<ProjectTemplateAuthVO> templateAuths = ConvertUtil
            .list2List(projectLogicTemplateAuthService.getProjectActiveTemplateRWAndRAuths(projectId), ProjectTemplateAuthVO.class);

        fillTemplateAuthVO(templateAuths);

        return Result.buildSucc(templateAuths);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "增加APP权限接口" )
    public Result<Void> addTemplateAuth(HttpServletRequest request, @RequestBody ProjectTemplateAuthDTO authDTO) {
        return projectLogicTemplateAuthService.addTemplateAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "更新APP权限接口" )
    public Result<Void> updateTemplateAuth(HttpServletRequest request, @RequestBody ProjectTemplateAuthDTO authDTO) {
        return projectLogicTemplateAuthManager.updateTemplateAuth(authDTO, HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除APP权限接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "authId", value = "权限ID", required = true) })
    public Result<Void> deleteTemplateAuth(HttpServletRequest request, @RequestParam("authId") Long authId) {
        return projectLogicTemplateAuthService.deleteTemplateAuth(authId, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/checkMeta")
    @ResponseBody
    @ApiOperation(value = "权限元数据校验接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "boolean", name = "delete", value = "是否删除脏数据", required = true) })
    public Result<Void> deleteExcessTemplateAuthsIfNeed(@RequestParam("delete") boolean delete) {
        return Result.build(projectLogicTemplateAuthService.deleteRedundancyTemplateAuths(delete));
    }

    /********************************************private********************************************/
    /**
     * 给AppTemplateAuthVO设置所属逻辑集群ID、name，逻辑模板name
     * @param templateAuths 模板权限列表
     */
    private void fillTemplateAuthVO(List<ProjectTemplateAuthVO> templateAuths) {
        if (CollectionUtils.isEmpty(templateAuths)) {
            return;
        }

        // 涉及的逻辑模板id
        List<Integer> templateIds = templateAuths.stream().map(ProjectTemplateAuthVO::getTemplateId)
            .collect(Collectors.toList());

        Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> logicTemplateMap = indexTemplateService
            .getLogicTemplatesWithClusterAndMasterTemplateMap(new HashSet<>(templateIds));

        for (ProjectTemplateAuthVO authVO : templateAuths) {
            Integer templateId = authVO.getTemplateId();
            IndexTemplateLogicWithClusterAndMasterTemplate logicTemplate = logicTemplateMap.get(templateId);
            if (logicTemplate != null) {
                // 逻辑模板信息
                authVO.setTemplateName(logicTemplate.getName());
                // 逻辑集群信息
                ClusterLogic logicCluster = logicTemplate.getLogicCluster();
                // 物理模板被删除后有可能没有集群信息
                if (logicCluster != null) {
                    authVO.setLogicClusterId(logicCluster.getId());
                    authVO.setLogicClusterName(logicCluster.getName());
                } else {
                    authVO.setLogicClusterName("");
                }
            } else {
                authVO.setTemplateName("");
            }
        }
    }
}