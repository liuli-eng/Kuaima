package com.kuaima.app.domain.boss.service;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BossAddress;
import com.kuaima.app.domain.boss.model.BossRecruitAddressModels.AddressRequest;
import com.kuaima.app.domain.boss.model.BossRecruitAddressModels.AddressView;
import com.kuaima.app.domain.boss.repository.BossAddressRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BossRecruitAddressService {
    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");
    private final BossAddressRepository repository;
    public BossRecruitAddressService(BossAddressRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<AddressView> list(Long bossId) {
        return repository.findByUserIdOrderByIdDesc(bossId).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<AddressView> listByEnterprise(Long enterpriseId) {
        return repository.findByEnterpriseIdOrderByIdDesc(enterpriseId).stream().map(this::view).toList();
    }

    @Transactional
    public AddressView createByEnterprise(Long enterpriseId, Long operatorId, AddressRequest request) {
        validate(request); BossAddress address = new BossAddress(); address.setEnterpriseId(enterpriseId); address.setUserId(operatorId); apply(address, request);
        if (Boolean.TRUE.equals(address.getIsDefault())) repository.clearDefaultByEnterpriseId(enterpriseId);
        return view(repository.save(address));
    }

    @Transactional
    public AddressView updateByEnterprise(Long enterpriseId, Long id, AddressRequest request) {
        validate(request); BossAddress address = repository.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id)); apply(address, request);
        if (Boolean.TRUE.equals(address.getIsDefault())) repository.clearDefaultByEnterpriseId(enterpriseId);
        return view(repository.save(address));
    }

    @Transactional
    public void deleteByEnterprise(Long enterpriseId, Long id) { BossAddress address = repository.findByIdAndEnterpriseId(id, enterpriseId)
            .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id)); if (Boolean.TRUE.equals(address.getIsDefault())) throw new IllegalArgumentException("默认地址不可直接删除，请先设置其他默认地址"); repository.delete(address); }

    @Transactional
    public AddressView setDefaultByEnterprise(Long enterpriseId, Long id) { BossAddress address = repository.findByIdAndEnterpriseId(id, enterpriseId)
            .orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id)); repository.clearDefaultByEnterpriseId(enterpriseId); address.setIsDefault(true); return view(repository.save(address)); }

    @Transactional
    public AddressView create(Long bossId, AddressRequest request) {
        validate(request);
        BossAddress address = new BossAddress();
        address.setUserId(bossId);
        apply(address, request);
        if (Boolean.TRUE.equals(address.getIsDefault())) repository.clearDefaultByUserId(bossId);
        return view(repository.save(address));
    }

    @Transactional
    public AddressView update(Long bossId, Long id, AddressRequest request) {
        validate(request);
        BossAddress address = owned(bossId, id);
        apply(address, request);
        if (Boolean.TRUE.equals(address.getIsDefault())) repository.clearDefaultByUserId(bossId);
        return view(repository.save(address));
    }

    @Transactional
    public void delete(Long bossId, Long id) {
        BossAddress address = owned(bossId, id);
        if (Boolean.TRUE.equals(address.getIsDefault())) throw new IllegalArgumentException("默认地址不可直接删除，请先设置其他默认地址");
        repository.delete(address);
    }

    @Transactional
    public AddressView setDefault(Long bossId, Long id) {
        BossAddress address = owned(bossId, id);
        repository.clearDefaultByUserId(bossId);
        address.setIsDefault(true);
        return view(repository.save(address));
    }

    private BossAddress owned(Long bossId, Long id) {
        BossAddress address = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("地址不存在: " + id));
        if (!bossId.equals(address.getUserId())) throw new ForbiddenBusinessException("无权操作其他老板的地址");
        return address;
    }

    private void validate(AddressRequest r) {
        if (r == null) throw new IllegalArgumentException("地址信息不能为空");
        required(r.name(), "地址名称"); required(r.contactName(), "联系人"); required(r.contactPhone(), "联系电话");
        required(r.city(), "城市"); required(r.district(), "区县"); required(r.detail(), "详细地址");
        if (!PHONE.matcher(r.contactPhone().trim()).matches()) throw new IllegalArgumentException("contactPhone 必须是有效的11位手机号");
        if (r.latitude() == null || r.longitude() == null) throw new IllegalArgumentException("经纬度不能为空");
        if (!Double.isFinite(r.latitude()) || r.latitude() < -90 || r.latitude() > 90) throw new IllegalArgumentException("latitude 必须在 -90 到 90 之间");
        if (!Double.isFinite(r.longitude()) || r.longitude() < -180 || r.longitude() > 180) throw new IllegalArgumentException("longitude 必须在 -180 到 180 之间");
    }
    private void required(String value, String name) { if (!StringUtils.hasText(value)) throw new IllegalArgumentException(name + "不能为空"); }
    private void apply(BossAddress a, AddressRequest r) { a.setName(r.name().trim()); a.setContactName(r.contactName().trim()); a.setContactPhone(r.contactPhone().trim()); a.setCity(r.city().trim()); a.setDistrict(r.district().trim()); a.setDetail(r.detail().trim()); a.setLat(r.latitude()); a.setLng(r.longitude()); a.setIsDefault(Boolean.TRUE.equals(r.isDefault())); }
    private AddressView view(BossAddress a) { return new AddressView(a.getId(), a.getName(), a.getContactName(), a.getContactPhone(), a.getCity(), a.getDistrict(), a.getDetail(), a.getLat(), a.getLng(), Boolean.TRUE.equals(a.getIsDefault())); }
}
