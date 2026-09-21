package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossAddress;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossAddressRepository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossOrderLocationTests {

    private BossOrderRespository orders;
    private BossAddressRepository addresses;
    private BossOrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(BossOrderRespository.class);
        addresses = mock(BossAddressRepository.class);
        service = new BossOrderService(orders, mock(BaseOrderItemRespository.class),
                mock(UserRepository.class), mock(MessageService.class), mock(SettlementRespository.class),
                null, null, null, null, addresses);
        when(orders.save(any(BossOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_shouldRequireCoordinates() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(validOrder()));

        assertEquals("工作地址必须提供经度和纬度，请重新选择地址", error.getMessage());
    }

    @Test
    void create_shouldRejectSingleCoordinate() {
        BossOrder order = validOrder();
        order.setLongitude(new BigDecimal("121.4737010"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(order));

        assertEquals("经度和纬度必须同时填写", error.getMessage());
    }

    @Test
    void create_shouldRejectOutOfRangeCoordinate() {
        BossOrder order = validOrder();
        order.setLongitude(new BigDecimal("181"));
        order.setLatitude(new BigDecimal("31"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(order));

        assertEquals("经度必须在 -180 到 180 之间", error.getMessage());
    }

    @Test
    void create_shouldCopyAddressAndCoordinatesFromEnterpriseAddress() {
        BossOrder order = validOrder();
        order.setEnterpriseId(7L);
        order.setAddressId(11L);
        BossAddress address = savedAddress();
        when(addresses.findByIdAndEnterpriseId(11L, 7L)).thenReturn(Optional.of(address));

        BossOrder saved = service.createOrder(order);

        assertEquals("上海市浦东新区世纪大道100号", saved.getAddress());
        assertEquals(0, saved.getLongitude().compareTo(BigDecimal.valueOf(121.473701)));
        assertEquals(0, saved.getLatitude().compareTo(BigDecimal.valueOf(31.230416)));
    }

    @Test
    void create_shouldRejectAddressOwnedByAnotherEnterprise() {
        BossOrder order = validOrder();
        order.setEnterpriseId(7L);
        order.setAddressId(11L);
        when(addresses.findByIdAndEnterpriseId(11L, 7L)).thenReturn(Optional.empty());

        ForbiddenBusinessException error = assertThrows(ForbiddenBusinessException.class,
                () -> service.createOrder(order));

        assertEquals("地址不存在或不属于当前企业", error.getMessage());
    }

    @Test
    void update_shouldRejectChangedAddressWithoutNewCoordinates() {
        BossOrder existing = existingOrder();
        BossOrder update = new BossOrder();
        update.setAddress("新的工作地址");
        when(orders.findById(5L)).thenReturn(Optional.of(existing));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.updateOrder(5L, update));

        assertEquals("修改工作地址时必须同时提供经度和纬度", error.getMessage());
    }

    @Test
    void update_shouldAcceptChangedAddressWithNewCoordinates() {
        BossOrder existing = existingOrder();
        BossOrder update = new BossOrder();
        update.setAddress("新的工作地址");
        update.setLongitude(new BigDecimal("120.1000000"));
        update.setLatitude(new BigDecimal("30.2000000"));
        when(orders.findById(5L)).thenReturn(Optional.of(existing));

        BossOrder saved = service.updateOrder(5L, update);

        assertEquals("新的工作地址", saved.getAddress());
        assertEquals(new BigDecimal("120.1000000"), saved.getLongitude());
        assertEquals(new BigDecimal("30.2000000"), saved.getLatitude());
    }

    @Test
    void update_shouldKeepCoordinatesWhenOnlyNonLocationFieldChanges() {
        BossOrder existing = existingOrder();
        BossOrder update = new BossOrder();
        update.setOrderTitle("新标题");
        when(orders.findById(5L)).thenReturn(Optional.of(existing));

        BossOrder saved = service.updateOrder(5L, update);

        assertEquals("新标题", saved.getOrderTitle());
        assertEquals(new BigDecimal("121.4737010"), saved.getLongitude());
        assertEquals(new BigDecimal("31.2304160"), saved.getLatitude());
    }

    @Test
    void update_shouldPreferCoordinatesFromSavedAddress() {
        BossOrder existing = existingOrder();
        BossOrder update = new BossOrder();
        update.setAddressId(11L);
        update.setLongitude(BigDecimal.ZERO);
        update.setLatitude(BigDecimal.ZERO);
        when(orders.findById(5L)).thenReturn(Optional.of(existing));
        when(addresses.findByIdAndEnterpriseId(11L, 7L)).thenReturn(Optional.of(savedAddress()));

        BossOrder saved = service.updateOrder(5L, update);

        assertEquals("上海市浦东新区世纪大道100号", saved.getAddress());
        assertEquals(0, saved.getLongitude().compareTo(BigDecimal.valueOf(121.473701)));
        assertEquals(0, saved.getLatitude().compareTo(BigDecimal.valueOf(31.230416)));
    }

    private BossOrder validOrder() {
        BossOrder order = new BossOrder();
        order.setOrderTitle("仓库分拣");
        order.setType("daily");
        order.setPostion("分拣员");
        order.setOrderNum(2);
        order.setDuration(8);
        order.setSalary(200);
        return order;
    }

    private BossOrder existingOrder() {
        BossOrder order = validOrder();
        order.setId(5L);
        order.setEnterpriseId(7L);
        order.setOrderStatus(BossStatus.ORDER_RECRUITING);
        order.setAddress("原工作地址");
        order.setLongitude(new BigDecimal("121.4737010"));
        order.setLatitude(new BigDecimal("31.2304160"));
        return order;
    }

    private BossAddress savedAddress() {
        BossAddress address = new BossAddress();
        address.setEnterpriseId(7L);
        address.setCity("上海市");
        address.setDistrict("浦东新区");
        address.setDetail("世纪大道100号");
        address.setLng(121.473701);
        address.setLat(31.230416);
        return address;
    }
}
