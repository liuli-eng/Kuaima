package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossRecruitSettingsModels.Settings;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossHomeServiceTests {
    private BossOrderRespository orders;
    private BaseOrderItemRespository items;
    private SettlementRespository settlements;
    private BossRecruitAccountRepository accounts;
    private UserRepository users;
    private BossAttendanceCodeService attendanceCodes;
    private BossRecruitSettingsService recruitSettings;
    private BossHomeService service;

    private Settings view(boolean startCode, boolean earlyCode) {
        return new Settings("零工需打电话", null, "开工后3小时", false, startCode, earlyCode,
                "按工作设定时间", true, "日结", "daily", "auto", true, true, true, true);
    }

    @BeforeEach
    void setUp() {
        orders = mock(BossOrderRespository.class);
        items = mock(BaseOrderItemRespository.class);
        settlements = mock(SettlementRespository.class);
        accounts = mock(BossRecruitAccountRepository.class);
        users = mock(UserRepository.class);
        attendanceCodes = mock(BossAttendanceCodeService.class);
        recruitSettings = mock(BossRecruitSettingsService.class);
        service = new BossHomeService(orders, items, settlements, accounts, users,
                attendanceCodes, recruitSettings);
    }

    @Test
    void overviewWithoutOrders_shouldReturnCompleteZeroStructureAndRealAccountFields() {
        User boss = new User(); boss.setId(1L); boss.setCity("松江"); boss.setRealName("王娜");
        BossRecruitAccount account = account(10L, 1L, true); account.setName("王娜"); account.setWorkCode("1633");
        when(users.findById(1L)).thenReturn(Optional.of(boss));
        when(users.countByRoleAndCity("USER", "松江")).thenReturn(23L);
        when(accounts.findByOwnerUserIdOrderByIdAsc(1L)).thenReturn(List.of(account));
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any()))
                .thenReturn(List.of());
        when(recruitSettings.get(1L)).thenReturn(view(true, true));
        when(attendanceCodes.today(1L)).thenReturn(Map.of(
                "workCode", "1633", "leaveCode", "7788"));

        var result = service.overview(1L, null, null);

        assertEquals("松江", result.city());
        assertEquals(23L, result.nearbyWorkers());
        assertEquals(0, result.fastestMinutes());
        assertEquals(10L, result.currentAccountId());
        assertEquals("1633", result.account().workCode());
        assertEquals(4, result.scheduleDays().size());
        result.scheduleDays().forEach(day -> assertEquals(0, day.demand()));
    }

    @Test
    void overview_shouldNotLoadOrCreateAttendanceCodesWhenBothConfigurationsDisabled() {
        User boss = new User(); boss.setId(1L); boss.setCity("松江");
        BossRecruitAccount account = account(10L, 1L, true);
        account.setWorkCode("1633"); account.setLeaveCode("7788");
        when(users.findById(1L)).thenReturn(Optional.of(boss));
        when(users.countByRole("USER")).thenReturn(0L);
        when(accounts.findByOwnerUserIdOrderByIdAsc(1L)).thenReturn(List.of(account));
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any())).thenReturn(List.of());
        when(recruitSettings.get(1L)).thenReturn(view(false, false));

        var result = service.overview(1L, null, null);

        assertEquals("", result.account().workCode());
        assertEquals("", result.account().leaveCode());
        verify(recruitSettings).get(1L);
        verifyNoInteractions(attendanceCodes);
    }

    @Test
    void overview_shouldOnlyReturnEnabledAttendanceCode() {
        User boss = new User(); boss.setId(1L); boss.setCity("松江");
        BossRecruitAccount account = account(10L, 1L, true);
        when(users.findById(1L)).thenReturn(Optional.of(boss));
        when(users.countByRole("USER")).thenReturn(0L);
        when(accounts.findByOwnerUserIdOrderByIdAsc(1L)).thenReturn(List.of(account));
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any())).thenReturn(List.of());
        when(recruitSettings.get(1L)).thenReturn(view(true, false));
        when(attendanceCodes.today(1L)).thenReturn(Map.of(
                "workCode", "1633", "leaveCode", "7788"));

        var result = service.overview(1L, null, null);

        assertEquals("1633", result.account().workCode());
        assertEquals("", result.account().leaveCode());
        verify(attendanceCodes).today(1L);
    }

    @Test
    void quickLoginOverview_shouldReadAccountsFromOriginalGroupButUseTargetForCityAndOrders() {
        User target = new User(); target.setId(31L); target.setCity("上海");
        BossRecruitAccount account = account(10L, 7L, true);
        account.setTargetUserId(31L);
        when(users.findById(31L)).thenReturn(Optional.of(target));
        when(users.countByRoleAndCity("USER", "上海")).thenReturn(9L);
        when(accounts.findByOwnerUserIdOrderByIdAsc(7L)).thenReturn(List.of(account));
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any())).thenReturn(List.of());

        var result = service.overview(31L, 7L, null, null);

        assertEquals(9L, result.nearbyWorkers());
        assertEquals(10L, result.currentAccountId());
        verify(accounts, times(2)).findByOwnerUserIdOrderByIdAsc(7L);
        verify(orders, times(4)).findByCreateByInAndOverlappingTime(eq(List.of(31L)), any(), any());
    }

    @Test
    void schedule_shouldAggregateStatusesAndSettlementsWithoutCanceledItems() {
        LocalDate date = LocalDate.of(2026, 9, 14);
        BossOrder first = order(1001L, 20, BossStatus.ORDER_RECRUITING);
        BossOrder second = order(1002L, 10, BossStatus.ORDER_PENDING_SETTLE);
        when(accounts.findByIdAndOwnerUserId(10L, 1L)).thenReturn(Optional.of(account(10L, 1L, true)));
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any()))
                .thenReturn(List.of(first, second));
        BaseOrderItem applied = item(1L, 1001L, BossStatus.ITEM_APPLIED);
        BaseOrderItem hired = item(2L, 1001L, BossStatus.ITEM_HIRED);
        BaseOrderItem arrived = item(3L, 1001L, BossStatus.ITEM_ON_WORK);
        BaseOrderItem finished = item(4L, 1002L, BossStatus.ITEM_FINISHED);
        BaseOrderItem canceled = item(5L, 1002L, BossStatus.ITEM_CANCELED);
        when(items.findByOrderIdIn(List.of(1001L, 1002L)))
                .thenReturn(List.of(applied, hired, arrived, finished, canceled));
        Settlement pending = settlement(3L, SettlementStatus.PENDING);
        Settlement paid = settlement(4L, SettlementStatus.PAID);
        Settlement canceledSettlement = settlement(5L, SettlementStatus.CANCELED);
        when(settlements.findByItemIdInOrderByIdDesc(List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(List.of(pending, paid, canceledSettlement));

        var result = service.schedule(1L, date, 10L);

        assertEquals(30, result.demand());
        assertEquals(3, result.stats().accepted());
        assertEquals(2, result.stats().arrived());
        assertEquals(1, result.stats().working());
        assertEquals(1, result.stats().finished());
        assertEquals(2, result.stats().settled());
        assertEquals(2, result.records().size());
        assertEquals("进行中", result.records().get(0).status());
    }

    @Test
    void overview_shouldReturnDemandForFourDifferentDates() {
        BossRecruitAccount account = account(10L, 1L, true);
        when(accounts.findByOwnerUserIdOrderByIdAsc(1L)).thenReturn(List.of(account));
        when(users.findById(1L)).thenReturn(Optional.empty());
        when(users.countByRole("USER")).thenReturn(0L);
        when(orders.findByCreateByInAndOverlappingTime(any(), any(), any()))
                .thenReturn(List.of(order(1L, 12, BossStatus.ORDER_RECRUITING)),
                        List.of(order(2L, 30, BossStatus.ORDER_RECRUITING)),
                        List.of(order(3L, 8, BossStatus.ORDER_RECRUITING)),
                        List.of(order(4L, 2, BossStatus.ORDER_RECRUITING)));

        var result = service.overview(1L, null, null);

        assertEquals(List.of(12, 30, 8, 2), result.scheduleDays().stream().map(d -> d.demand()).toList());
    }

    @Test
    void schedule_shouldRejectAnotherBossAccount() {
        when(accounts.findByIdAndOwnerUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(ForbiddenBusinessException.class,
                () -> service.schedule(1L, LocalDate.of(2026, 9, 14), 99L));
    }

    private BossRecruitAccount account(Long id, Long ownerId, boolean current) {
        BossRecruitAccount account = new BossRecruitAccount(); account.setId(id); account.setOwnerUserId(ownerId);
        account.setName("账号" + id); account.setAuthorizationType("PERSONAL"); account.setCurrent(current); return account;
    }

    private BossOrder order(Long id, int demand, String status) {
        BossOrder order = new BossOrder(); order.setId(id); order.setOrderNum(demand); order.setOrderStatus(status);
        order.setOrderTitle("电子厂普工"); order.setType("daily"); return order;
    }

    private BaseOrderItem item(Long id, Long orderId, String status) {
        BaseOrderItem item = new BaseOrderItem(); item.setId(id); item.setOrderId(orderId); item.setStatus(status); return item;
    }

    private Settlement settlement(Long itemId, String status) {
        Settlement settlement = new Settlement(); settlement.setItemId(itemId); settlement.setStatus(status); return settlement;
    }
}
