package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderReview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private Long orderDetailId;

    private Long userId;

    private Long dishId;

    private Long setmealId;

    private String itemName;

    private Integer rating;

    private String content;

    private String image;

    private Integer isAnonymous;

    private Integer status;

    private String reply;

    private LocalDateTime replyTime;

    private Long replyUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
