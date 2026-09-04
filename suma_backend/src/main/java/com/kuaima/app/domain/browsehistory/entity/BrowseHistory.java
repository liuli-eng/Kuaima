package com.kuaima.app.domain.browsehistory.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 零工岗位浏览记录：每次浏览写入一条，viewedAt 记录浏览时间。
 */
@Entity
@Table(name = "browse_history")
@Getter
@Setter
public class BrowseHistory extends BaseEntity {

    @Column(comment = "零工用户ID")
    private Long userId;

    @Column(comment = "岗位订单ID")
    private Long orderId;

    @Column(comment = "浏览时间")
    private Timestamp viewedAt;
}
