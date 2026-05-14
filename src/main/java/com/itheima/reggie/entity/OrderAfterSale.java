package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderAfterSale implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private Long userId;

    private Integer type;

    private Integer status;

    private String reason;

    private String description;

    private String evidence;

    private BigDecimal applyAmount;

    private BigDecimal handleAmount;

    private String handleRemark;

    private LocalDateTime applyTime;

    private LocalDateTime handleTime;

    private Long handleUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
