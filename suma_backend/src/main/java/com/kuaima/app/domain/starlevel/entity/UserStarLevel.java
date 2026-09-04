package com.kuaima.app.domain.starlevel.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户星级等级：level 当前等级，progress 当前等级进度百分比(0~100)。
 * 首次查询时自动创建 level=1, progress=0 的初始记录。
 */
@Entity
@Table(name = "user_star_level")
@Getter
@Setter
public class UserStarLevel extends BaseEntity {

    @Column(unique = true, comment = "用户ID")
    private Long userId;

    @Column(comment = "当前星级(1~5)")
    private Integer level;

    @Column(comment = "当前等级进度(0~100)")
    private Integer progress;
}
