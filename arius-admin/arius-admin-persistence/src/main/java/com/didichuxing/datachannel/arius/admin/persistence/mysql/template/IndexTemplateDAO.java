package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;

/**
 * @author chengxiang
 * @date 2022/5/10
 */
@Repository
public interface IndexTemplateDAO {

    List<IndexTemplatePO> listByCondition(IndexTemplatePO param);

    int insert(IndexTemplatePO param);

    int update(IndexTemplatePO param);

    int delete(Integer logicId);

    IndexTemplatePO getById(Integer logicId);

    List<IndexTemplatePO> listByAppId(Integer appId);

    List<IndexTemplatePO> listAll();

    List<IndexTemplatePO> listByIds(@Param("logicIds") List<Integer> logicIds);

    List<IndexTemplatePO> listByResourceIds(@Param("resourceIds") List<Long> resourceIds);

    List<IndexTemplatePO> listByDataCenter(String dataCenter);

    List<IndexTemplatePO> listByName(String name);

    List<IndexTemplatePO> likeByCondition(IndexTemplatePO param);

    List<IndexTemplatePO> pagingByCondition(@Param("param") IndexTemplatePO param,
                                            @Param("from") Long from, @Param("size") Long size,
                                            @Param("sortTerm") String sortTerm, @Param("sortType") String sortType);

    long getTotalHitByCondition(IndexTemplatePO param);

    List<IndexTemplatePO> likeByResponsible(String responsible);

    int batchChangeHotDay(Integer days);

    int updateBlockReadState(@Param("logicId") Integer logicId, @Param("blockRead") Boolean blockRead);

    int updateBlockWriteState(@Param("logicId") Integer logicId, @Param("blockWrite") Boolean blockWrite);

    List<String> listAllNames();

}
