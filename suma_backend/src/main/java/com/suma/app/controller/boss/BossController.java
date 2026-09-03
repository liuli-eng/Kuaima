package com.suma.app.controller.boss;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.common.Result;
import com.suma.app.domain.boss.entity.BaseOrderItem;
import com.suma.app.domain.boss.entity.BossOrder;
import com.suma.app.domain.boss.service.BossOrderService;

@RestController
@RequestMapping("/boss")
public class BossController {

    private final BossOrderService bossOrderService;

    public BossController(BossOrderService bossOrderService) {
        this.bossOrderService = bossOrderService;
    }

    // ==================== 招工订单 ====================

    /** 发布订单 */
    @PostMapping("/order")
    public Result<BossOrder> createOrder(@RequestBody BossOrder order) {
        return Result.success(bossOrderService.createOrder(order));
    }

    /** 修改订单（仅招工中） */
    @PutMapping("/order/{id}")
    public Result<BossOrder> updateOrder(@PathVariable Long id, @RequestBody BossOrder order) {
        return Result.success(bossOrderService.updateOrder(id, order));
    }

    /** 订单详情 */
    @GetMapping("/order/{id}")
    public Result<BossOrder> getOrder(@PathVariable Long id) {
        return Result.success(bossOrderService.getOrder(id));
    }

    /**
     * 订单列表分页查询：/boss/order?type=daily&status=招工中&title=xxx&page=0&size=10
     * type 取值：daily(每天日结) / heldBack(压薪日结) / month(月结)
     * page 从 0 开始，size 默认 10
     */
    @GetMapping("/order")
    public Result<List<BossOrder>> listOrders(@RequestParam(required = false) String type,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Page<BossOrder> result = bossOrderService.listOrders(type, status, title, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 删除订单（仅招工中/已取消） */
    @DeleteMapping("/order/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        bossOrderService.deleteOrder(id);
        return Result.success();
    }

    /** 订单状态流转：/boss/order/{id}/status?target=招工结束|待结算|已完成|取消招工 */
    @PutMapping("/order/{id}/status")
    public Result<BossOrder> changeOrderStatus(@PathVariable Long id, @RequestParam String target) {
        return Result.success(bossOrderService.changeOrderStatus(id, target));
    }

    /**
     * 【预留】岗位快开始提醒：手动触发，通知该订单 已报名/已录用/已到岗 的用户
     * （当前不做定时任务，由前端/人工调用；后续如需自动提醒可加定时扫描）
     */
    @PostMapping("/order/{id}/remind-start")
    public Result<Void> notifyOrderStart(@PathVariable Long id) {
        bossOrderService.notifyOrderStart(id);
        return Result.success();
    }

    // ==================== 报名记录 ====================

    /** 用户报名：/boss/order/{orderId}/apply?userId=1&remark=xxx&trial=true（trial 仅月结订单有效） */
    @PostMapping("/order/{orderId}/apply")
    public Result<BaseOrderItem> applyOrder(@PathVariable Long orderId,
                                            @RequestParam Long userId,
                                            @RequestParam(required = false) String remark,
                                            @RequestParam(required = false) Boolean trial) {
        return Result.success(bossOrderService.applyOrder(orderId, userId, remark, trial));
    }

    /** 老板录用 */
    @PutMapping("/item/{id}/hire")
    public Result<BaseOrderItem> hireItem(@PathVariable Long id) {
        return Result.success(bossOrderService.hireItem(id));
    }

    /** 用户确认到岗 */
    @PutMapping("/item/{id}/work")
    public Result<BaseOrderItem> confirmWork(@PathVariable Long id) {
        return Result.success(bossOrderService.confirmWork(id));
    }

    /** 完成 */
    @PutMapping("/item/{id}/finish")
    public Result<BaseOrderItem> finishItem(@PathVariable Long id) {
        return Result.success(bossOrderService.finishItem(id));
    }

    /** 取消报名：/boss/item/{id}/cancel?reason=xxx */
    @PutMapping("/item/{id}/cancel")
    public Result<BaseOrderItem> cancelItem(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return Result.success(bossOrderService.cancelItem(id, reason));
    }

    /** 某订单的报名列表 */
    @GetMapping("/order/{orderId}/items")
    public Result<List<BaseOrderItem>> listItemsByOrder(@PathVariable Long orderId) {
        return Result.success(bossOrderService.listItemsByOrder(orderId));
    }

    /** 某用户的报名记录：/boss/user/items?userId=1 */
    @GetMapping("/user/items")
    public Result<List<BaseOrderItem>> listItemsByUser(@RequestParam Long userId) {
        return Result.success(bossOrderService.listItemsByUser(userId));
    }
}
