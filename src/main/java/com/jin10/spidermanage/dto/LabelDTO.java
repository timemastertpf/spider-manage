package com.jin10.spidermanage.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jin10.spidermanage.entity.Link;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelDTO implements Serializable {

    private Integer id;

    @JsonProperty("label_name")
    private String labelName;

    /**
     * 抓取时间描述
     */
    @JsonProperty("time_desc")
    private String timeDesc;

    /**
     * 需求描述
     */
    @JsonProperty("demand_desc")
    private String demandDesc;

    /**
     * 爬虫链接
     */
    @JsonProperty("spider_link")
    private List<String> spiderLink;

    /**
     * 实例图片路径
     */
    private List<ImlUrlDTO> img;

    /**
     * 爬虫ID
     */
    @JsonProperty("spider_id")
    private Integer spiderId;

    /**
     * 参数
     */
    private Integer param;

    /**
     * 时间表达式
     */
    private String cron;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:MM:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:MM:ss",timezone = "GMT+8")
    private Date updateTime;


}
