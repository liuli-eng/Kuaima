package com.kuaima.app.domain.talent.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 老板收藏的零工（人才库收藏）。
 */
@Entity
@Table(name = "talent_favorite")
@Getter
@Setter
public class TalentFavorite extends BaseEntity {

    @Column(comment = "老板用户 id")
    private Long bossId;

    @Column(comment = "被收藏的零工用户 id")
    private Long workerId;
}
