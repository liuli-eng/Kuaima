package com.kuaima.app.domain.jobfavorite.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 零工收藏的岗位：userId + orderId 唯一。
 */
@Entity
@Table(name = "job_favorite")
@Getter
@Setter
public class JobFavorite extends BaseEntity {

    @Column(comment = "零工用户ID")
    private Long userId;

    @Column(comment = "岗位订单ID")
    private Long orderId;
}
