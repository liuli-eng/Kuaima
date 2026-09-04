package com.kuaima.app.domain.boss.entity;

import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_order")
@Setter
@Getter
public class BossOrder extends BaseEntity {

    @Column(comment = "订单标题")
    private String orderTitle;

    @Column(comment = "订单状态:招工中,招工结束,待结算，已完成，取消招工")
    private String orderStatus;

    @Column(comment = "招工类型:每天日结(daily)/压薪日结(heldBack)/月结(month)")
    private String type;

    @Column(comment = "招工内容", length = 500)
    private String orderContent;

    @Column(comment = "订单备注")
    private String orderRemark;

    @Column(comment = "招聘岗位")
    private String postion;

    @Column(comment = "招工人数")
    private Integer orderNum;

    @Column(comment = "工作时长：日结/小时，压薪日结/天")
    private Integer duration;

    @Column(comment = "工作地点")
    private String address;

    @Column(comment = "标签")
    private String tags;

    @Column(comment = "开始时间")
    private Date startTime;

    @Column(comment = "结束时间")
    private Date endTime;

    @Column(comment = "工资:元/天")
    private Integer salary;

    @Column(comment = "月结才有试工时间")
    private String trialDuration;

}
