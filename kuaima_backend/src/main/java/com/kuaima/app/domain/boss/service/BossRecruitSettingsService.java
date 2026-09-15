package com.kuaima.app.domain.boss.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.kuaima.app.domain.boss.constant.BossType;
import com.kuaima.app.domain.boss.entity.BossRecruitSettings;
import com.kuaima.app.domain.boss.model.BossRecruitSettingsModels.Settings;
import com.kuaima.app.domain.boss.repository.BossRecruitSettingsRepository;
import com.kuaima.app.domain.user.repository.UserRepository;

@Service
public class BossRecruitSettingsService {
    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");
    private final BossRecruitSettingsRepository repository;
    private final UserRepository userRepository;
    public BossRecruitSettingsService(BossRecruitSettingsRepository repository) { this(repository, null); }
    @org.springframework.beans.factory.annotation.Autowired
    public BossRecruitSettingsService(BossRecruitSettingsRepository repository, UserRepository userRepository) { this.repository = repository; this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public Settings get(Long bossId) {
        return repository.findByBossId(bossId).map(this::toSettings)
                .orElseGet(() -> defaults(userRepository == null ? null : userRepository.findById(bossId).map(u -> u.getPhone()).orElse(null)));
    }

    @Transactional
    public Settings save(Long bossId, Settings input) {
        if (input == null) throw new IllegalArgumentException("招工设置不能为空");
        validate(input);
        BossRecruitSettings entity = repository.findByBossId(bossId).orElseGet(BossRecruitSettings::new);
        entity.setBossId(bossId); copy(entity, input);
        return toSettings(repository.save(entity));
    }

    public Settings defaults() { return defaults(null); }
    public Settings defaults(String backupPhone) { return new Settings("零工需打电话", StringUtils.hasText(backupPhone) ? backupPhone : "", "开工后3小时", false, false, false,
            "按工作设定时间", true, "日结", BossType.DAILY, "auto", true, true, true, true); }
    private void validate(Settings s) {
        if (!StringUtils.hasText(s.phoneMode()) || !(s.phoneMode().equals("零工需打电话") || s.phoneMode().equals("零工无需打电话"))) throw new IllegalArgumentException("phoneMode 枚举值无效");
        if (StringUtils.hasText(s.backupPhone()) && !PHONE.matcher(s.backupPhone()).matches()) throw new IllegalArgumentException("backupPhone 必须是有效的11位手机号");
        if (!StringUtils.hasText(s.stopTime()) || !java.util.Set.of("开工时", "开工后1小时", "开工后3小时", "手动停招").contains(s.stopTime())) throw new IllegalArgumentException("stopTime 枚举值无效");
        if (!StringUtils.hasText(s.attendanceMode()) || !java.util.Set.of("按工作设定时间", "按实际打卡时间").contains(s.attendanceMode())) throw new IllegalArgumentException("attendanceMode 枚举值无效");
        if (!StringUtils.hasText(s.settleMode()) || !java.util.Set.of("日结", "压薪日结", "月结").contains(s.settleMode())) throw new IllegalArgumentException("settleMode 枚举值无效");
        if (!BossType.isValid(s.type())) throw new IllegalArgumentException("type 枚举值无效");
        if (!"auto".equals(s.signMode()) && !"manual".equals(s.signMode())) throw new IllegalArgumentException("signMode 只能是 auto 或 manual");
        if (s.dndEnabled() == null || s.startCodeEnabled() == null || s.earlyCodeEnabled() == null || s.faceClockEnabled() == null || s.phoneNotify() == null || s.signNotify() == null || s.startRemind() == null || s.settleNotify() == null) throw new IllegalArgumentException("开关字段不能为空");
    }
    private void copy(BossRecruitSettings e, Settings s) { e.setPhoneMode(s.phoneMode()); e.setBackupPhone(s.backupPhone()); e.setStopTime(s.stopTime()); e.setDndEnabled(s.dndEnabled()); e.setStartCodeEnabled(s.startCodeEnabled()); e.setEarlyCodeEnabled(s.earlyCodeEnabled()); e.setAttendanceMode(s.attendanceMode()); e.setFaceClockEnabled(s.faceClockEnabled()); e.setSettleMode(s.settleMode()); e.setType(s.type()); e.setSignMode(s.signMode()); e.setPhoneNotify(s.phoneNotify()); e.setSignNotify(s.signNotify()); e.setStartRemind(s.startRemind()); e.setSettleNotify(s.settleNotify()); }
    private Settings toSettings(BossRecruitSettings e) { return new Settings(e.getPhoneMode(), e.getBackupPhone(), e.getStopTime(), e.getDndEnabled(), e.getStartCodeEnabled(), e.getEarlyCodeEnabled(), e.getAttendanceMode(), e.getFaceClockEnabled(), e.getSettleMode(), e.getType(), e.getSignMode(), e.getPhoneNotify(), e.getSignNotify(), e.getStartRemind(), e.getSettleNotify()); }
}
