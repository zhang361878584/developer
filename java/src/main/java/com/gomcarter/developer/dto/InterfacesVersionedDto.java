package com.gomcarter.developer.dto;

import com.gomcarter.frameworks.interfaces.annotation.Notes;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author gomcarter on 2019-12-13 11:24:57
 */
@Data
@Accessors(chain = true)
public class InterfacesVersionedDto {

    /**
     * 主键
     */
    @Notes("主键")
    private Long id;

    /**
     * 接口 id
     */
    @Notes("接口 id")
    private Long fkInterfacesId;
    /**
     * 接口名称
     */
    @Notes("接口名称")
    private String name;
    /**
     * 控制器
     */
    @Notes("控制器")
    private String controller;
    /**
     * 接口地址，域名后面的一截如：http://g.cpsdb.com/platform/category中的platform/category
     */
    @Notes("接口地址，域名后面的一截如：http://g.cpsdb.com/platform/category中的platform/category")
    private String url;
    /**
     * GET, POST, PUT, PATCH, DELETE
     */
    @Notes("GET, POST, PUT, PATCH, DELETE")
    private String method;
    /**
     * 是否已经废弃
     */
    @Notes("是否已经废弃")
    private Boolean deprecated;
    /**
     * 详细描述：如返回值说明，接口的一些说明等
     */
    @Notes("详细描述：如返回值说明，接口的一些说明等")
    private String mark;
    /**
     * 返回值数据结构
     */
    @Notes("返回值数据结构")
    private String returns;
    /**
     * 参数数据结构
     */
    @Notes("参数数据结构")
    private String parameters;
    /**
     * 属于哪个java项目
     */
    @Notes("属于哪个java项目")
    private Long fkJavaId;
    /**
     * 属于哪个前端项目
     */
    @Notes("属于哪个前端项目")
    private Long fkEndId;
    /**
     *
     */
    @Notes("")
    private Date createTime;
    /**
     *
     */
    @Notes("")
    private Date modifyTime;
}
