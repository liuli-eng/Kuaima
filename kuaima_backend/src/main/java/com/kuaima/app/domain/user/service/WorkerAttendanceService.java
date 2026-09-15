package com.kuaima.app.domain.user.service;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.service.BossAttendanceCodeService;
import jakarta.persistence.EntityNotFoundException;
@Service
public class WorkerAttendanceService {
 private final BossOrderRespository orders; private final BaseOrderItemRespository items; private final BossAttendanceCodeService codes;
 public WorkerAttendanceService(BossOrderRespository orders,BaseOrderItemRespository items,BossAttendanceCodeService codes){this.orders=orders;this.items=items;this.codes=codes;}
 @Transactional public Map<String,Object> checkIn(Long workerId,Long orderId,String code){BossOrder o=orders.findById(orderId).orElseThrow(()->new EntityNotFoundException("订单不存在: "+orderId));var i=items.findByOrderIdAndUserId(orderId,workerId).orElseThrow(()->new EntityNotFoundException("未找到当前零工的报名记录"));if(!BossStatus.ITEM_HIRED.equals(i.getStatus()))throw new IllegalArgumentException("仅已录用零工可以开工");codes.verify(o.getCreateBy(),true,code);i.setStatus(BossStatus.ITEM_ON_WORK);i.setWorkDate(Date.valueOf(LocalDate.now()));items.save(i);return Map.of("checkedIn",true,"orderId",orderId,"status",i.getStatus());}
 @Transactional public Map<String,Object> earlyLeave(Long workerId,Long orderId,String code){BossOrder o=orders.findById(orderId).orElseThrow(()->new EntityNotFoundException("订单不存在: "+orderId));var i=items.findByOrderIdAndUserId(orderId,workerId).orElseThrow(()->new EntityNotFoundException("未找到当前零工的报名记录"));if(!BossStatus.ITEM_ON_WORK.equals(i.getStatus()))throw new IllegalArgumentException("仅开工中的零工可以早退");codes.verify(o.getCreateBy(),false,code);i.setStatus(BossStatus.ITEM_FINISHED);i.setFinishDate(Date.valueOf(LocalDate.now()));items.save(i);return Map.of("earlyLeft",true,"orderId",orderId,"status",i.getStatus());}
}
