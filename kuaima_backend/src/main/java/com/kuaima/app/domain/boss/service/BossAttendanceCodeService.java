package com.kuaima.app.domain.boss.service;

import java.security.SecureRandom;
import java.time.*;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.kuaima.app.domain.boss.entity.BossAttendanceCode;
import com.kuaima.app.domain.boss.repository.BossAttendanceCodeRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BossAttendanceCodeService {
    private final BossAttendanceCodeRepository codes; private final BossRecruitSettingsService settings; private final UserRepository users; private final EnterpriseRepository enterprises; private final SecureRandom random = new SecureRandom();
    public BossAttendanceCodeService(BossAttendanceCodeRepository codes, BossRecruitSettingsService settings, UserRepository users) { this(codes, settings, users, null); }
    @org.springframework.beans.factory.annotation.Autowired public BossAttendanceCodeService(BossAttendanceCodeRepository codes, BossRecruitSettingsService settings, UserRepository users, EnterpriseRepository enterprises) { this.codes = codes; this.settings = settings; this.users = users; this.enterprises = enterprises; }
    /** 使用独立事务，避免外层事务快照先于并发插入建立后重复 insert 当日唯一记录。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String,Object> today(Long bossId) { return state(ensure(bossId)); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String,Object> refresh(Long bossId, boolean work) { BossAttendanceCode c=ensure(bossId); String value=distinctCode(work?c.getLeaveCode():c.getWorkCode()); if(work){c.setWorkCode(value);c.setWorkCodeEnabled(true);c.setWorkCodeExpiresAt(end());}else{c.setLeaveCode(value);c.setLeaveCodeEnabled(true);c.setLeaveCodeExpiresAt(end());} return state(codes.save(c)); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void verify(Long bossId, boolean work, String value) { if(value==null||value.isBlank()) throw new IllegalArgumentException("验证码不能为空"); BossAttendanceCode c=ensure(bossId); LocalDateTime locked=work?c.getWorkLockedUntil():c.getLeaveLockedUntil(); if(locked!=null&&LocalDateTime.now().isBefore(locked))throw new IllegalArgumentException("验证码尝试次数过多，请稍后再试"); String expected=work?c.getWorkCode():c.getLeaveCode(); Boolean enabled=work?c.getWorkCodeEnabled():c.getLeaveCodeEnabled(); LocalDateTime expires=work?c.getWorkCodeExpiresAt():c.getLeaveCodeExpiresAt(); if(!Boolean.TRUE.equals(enabled)||expected==null||expires==null||LocalDateTime.now().isAfter(expires)||!expected.equals(value.trim())){fail(c,work);throw new IllegalArgumentException("验证码错误或已失效");} if(work){c.setWorkFailedAttempts(0);c.setWorkLockedUntil(null);}else{c.setLeaveFailedAttempts(0);c.setLeaveLockedUntil(null);} codes.save(c); }
    @Transactional(propagation = Propagation.REQUIRES_NEW) public Map<String,Object> todayByEnterprise(Long enterpriseId, Long operatorId) { return state(ensureEnterprise(enterpriseId, operatorId)); }
    @Transactional(propagation = Propagation.REQUIRES_NEW) public Map<String,Object> refreshByEnterprise(Long enterpriseId, Long operatorId, boolean work) { BossAttendanceCode c=ensureEnterprise(enterpriseId, operatorId); String value=distinctCode(work?c.getLeaveCode():c.getWorkCode()); if(work){c.setWorkCode(value);c.setWorkCodeEnabled(true);c.setWorkCodeExpiresAt(end());}else{c.setLeaveCode(value);c.setLeaveCodeEnabled(true);c.setLeaveCodeExpiresAt(end());} return state(codes.save(c)); }
    @Transactional(propagation = Propagation.REQUIRES_NEW) public void verifyByEnterprise(Long enterpriseId, Long operatorId, boolean work, String value) { if(value==null||value.isBlank())throw new IllegalArgumentException("验证码不能为空"); BossAttendanceCode c=ensureEnterprise(enterpriseId,operatorId); LocalDateTime locked=work?c.getWorkLockedUntil():c.getLeaveLockedUntil(); if(locked!=null&&LocalDateTime.now().isBefore(locked))throw new IllegalArgumentException("验证码尝试次数过多，请稍后再试"); String expected=work?c.getWorkCode():c.getLeaveCode(); Boolean enabled=work?c.getWorkCodeEnabled():c.getLeaveCodeEnabled(); LocalDateTime expires=work?c.getWorkCodeExpiresAt():c.getLeaveCodeExpiresAt(); if(!Boolean.TRUE.equals(enabled)||expected==null||expires==null||LocalDateTime.now().isAfter(expires)||!expected.equals(value.trim())){fail(c,work);throw new IllegalArgumentException("验证码错误或已失效");} if(work){c.setWorkFailedAttempts(0);c.setWorkLockedUntil(null);}else{c.setLeaveFailedAttempts(0);c.setLeaveLockedUntil(null);}codes.save(c); }
    private BossAttendanceCode ensure(Long bossId){ if(bossId==null)throw new IllegalArgumentException("老板用户ID不能为空");users.findByIdForUpdate(bossId).orElseThrow(()->new EntityNotFoundException("老板账号不存在: "+bossId));LocalDate d=LocalDate.now(); BossAttendanceCode c=codes.findByBossIdAndCodeDate(bossId,d).orElseGet(()->{BossAttendanceCode n=new BossAttendanceCode();n.setBossId(bossId);n.setCodeDate(d);return n;}); var s=settings.get(bossId); c.setWorkCodeEnabled(s.startCodeEnabled());c.setLeaveCodeEnabled(s.earlyCodeEnabled());if(Boolean.TRUE.equals(c.getWorkCodeEnabled())&&c.getWorkCode()==null){c.setWorkCode(code());c.setWorkCodeExpiresAt(end());}if(Boolean.TRUE.equals(c.getLeaveCodeEnabled())&&c.getLeaveCode()==null){c.setLeaveCode(distinctCode(c.getWorkCode()));c.setLeaveCodeExpiresAt(end());}return codes.save(c); }
    private BossAttendanceCode ensureEnterprise(Long enterpriseId, Long operatorId){
        if(enterpriseId==null)throw new IllegalArgumentException("企业ID不能为空");
        if(enterprises==null)return ensure(operatorId);
        enterprises.findByIdForUpdate(enterpriseId).orElseThrow(()->new EntityNotFoundException("企业不存在: "+enterpriseId));
        LocalDate d=LocalDate.now();
        // 兼容企业成员改造前按 boss_id+日期 保存的历史记录，避免旧唯一索引导致重复插入。
        BossAttendanceCode c=codes.findByEnterpriseIdAndCodeDate(enterpriseId,d)
                .orElseGet(() -> codes.findByBossIdAndCodeDate(operatorId,d).orElseGet(() -> {
                    BossAttendanceCode n=new BossAttendanceCode();
                    n.setEnterpriseId(enterpriseId); n.setBossId(operatorId); n.setCodeDate(d);
                    return n;
                }));
        c.setEnterpriseId(enterpriseId);
        c.setBossId(operatorId);
        c.setCodeDate(d);
        var s=settings.getByEnterprise(enterpriseId,users.findById(operatorId).map(u->u.getPhone()).orElse(null));
        c.setWorkCodeEnabled(s.startCodeEnabled());c.setLeaveCodeEnabled(s.earlyCodeEnabled());
        if(Boolean.TRUE.equals(c.getWorkCodeEnabled())&&c.getWorkCode()==null){c.setWorkCode(code());c.setWorkCodeExpiresAt(end());}
        if(Boolean.TRUE.equals(c.getLeaveCodeEnabled())&&c.getLeaveCode()==null){c.setLeaveCode(distinctCode(c.getWorkCode()));c.setLeaveCodeExpiresAt(end());}
        return codes.save(c);
    }
    private void fail(BossAttendanceCode c,boolean work){int attempts=(work?c.getWorkFailedAttempts():c.getLeaveFailedAttempts())==null?1:(work?c.getWorkFailedAttempts():c.getLeaveFailedAttempts())+1;if(work)c.setWorkFailedAttempts(attempts);else c.setLeaveFailedAttempts(attempts);if(attempts>=5){if(work)c.setWorkLockedUntil(LocalDateTime.now().plusMinutes(10));else c.setLeaveLockedUntil(LocalDateTime.now().plusMinutes(10));}codes.save(c);}
    private Map<String,Object> state(BossAttendanceCode c){ return Map.of("workCodeEnabled",Boolean.TRUE.equals(c.getWorkCodeEnabled()),"workCode",c.getWorkCode()==null?"":c.getWorkCode(),"workCodeExpiresAt",c.getWorkCodeExpiresAt()==null?"":c.getWorkCodeExpiresAt(),"leaveCodeEnabled",Boolean.TRUE.equals(c.getLeaveCodeEnabled()),"leaveCode",c.getLeaveCode()==null?"":c.getLeaveCode(),"leaveCodeExpiresAt",c.getLeaveCodeExpiresAt()==null?"":c.getLeaveCodeExpiresAt()); }
    private String code(){ return String.format("%04d",random.nextInt(10000)); }
    private String distinctCode(String other){ String value; do { value=code(); } while(value.equals(other)); return value; }
    private LocalDateTime end(){ return LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1); }
}
