package com.shuxiang.buddymatch.model.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 标签
 * @TableName tag
 */
@Data
public class Tag implements Serializable {
    /**
     * id
     */
    @TableId("id")
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 父标签 id
     */
    private Long parentId;

    /**
     * 0 - 不是, 1 - 父标签
     */
    private Integer isParent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}