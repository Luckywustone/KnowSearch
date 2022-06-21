package com.didichuxing.datachannel.arius.admin.common.constant;

public enum PageSearchHandleTypeEnum {
        UNKNOWN("unknown"),
        TEMPLATE_LOGIC("templateLogicPageSearch"),
        CLUSTER_LOGIC("clusterLogicPageSearch"),
        DSL_TEMPLATE("dslTemplatePageSearch"),
        CLUSTER_PHY("clusterPhyPageSearch"),
        INDEX("indexPageSearch"),
        TEMPLATE_SRV("templateSrvPageSearch");

       private String pageSearchType;

        public String getPageSearchType(){
            return pageSearchType;
        }

        PageSearchHandleTypeEnum(String pageSearchType) {
            this.pageSearchType   = pageSearchType;
        }
    }