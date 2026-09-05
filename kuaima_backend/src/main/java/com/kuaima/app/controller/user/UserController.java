package com.kuaima.app.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Certification;
import com.kuaima.app.admin.repository.CertificationRepository;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.entity.CreditFlow;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.CreditFlowRepository;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final CreditFlowRepository creditFlowRepository;
    private final CertificationRepository certificationRepository;

    public UserController(UserRepository userRepository,
                          CreditFlowRepository creditFlowRepository,
                          CertificationRepository certificationRepository) {
        this.userRepository = userRepository;
        this.creditFlowRepository = creditFlowRepository;
        this.certificationRepository = certificationRepository;
    }

    /** 获取用户完整资料：GET /user/{id} */
    @GetMapping("/{id}")
    public Result<User> getProfile(@PathVariable Long id) {
        return Result.success(getUserOrThrow(id));
    }

    /**
     * 修改个人资料：PUT /user/{id}
     * 仅更新传入的非空字段（nickname, avatar, phone, email, age, gender, city, skills, remark）
     */
    @PutMapping("/{id}")
    @Transactional
    public Result<User> updateProfile(@PathVariable Long id, @RequestBody User body) {
        User user = getUserOrThrow(id);
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (StringUtils.hasText(body.getNickname())) {
            user.setNickname(body.getNickname());
        }
        if (StringUtils.hasText(body.getAvatar())) {
            user.setAvatar(body.getAvatar());
        }
        if (StringUtils.hasText(body.getPhone())) {
            user.setPhone(body.getPhone());
        }
        if (StringUtils.hasText(body.getEmail())) {
            user.setEmail(body.getEmail());
        }
        if (body.getAge() != null) {
            user.setAge(body.getAge());
        }
        if (StringUtils.hasText(body.getGender())) {
            user.setGender(body.getGender());
        }
        if (StringUtils.hasText(body.getCity())) {
            user.setCity(body.getCity());
        }
        if (StringUtils.hasText(body.getSkills())) {
            user.setSkills(body.getSkills());
        }
        if (StringUtils.hasText(body.getRemark())) {
            user.setRemark(body.getRemark());
        }
        return Result.success(userRepository.save(user));
    }

    /**
     * 提交实名认证：POST /user/{id}/realname
     * body: { "realName": "张三", "idCard": "110101199001011234" }
     * 设置 certStatus=待审核, certType=REALNAME, 并落库 realName / idCard
     */
    @PostMapping("/{id}/realname")
    @Transactional
    public Result<User> submitRealname(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String realName = body.get("realName");
        String idCard = body.get("idCard");
        if (!StringUtils.hasText(realName)) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }
        if (!StringUtils.hasText(idCard)) {
            throw new IllegalArgumentException("身份证号不能为空");
        }
        User user = getUserOrThrow(id);
        user.setCertType("REALNAME");
        user.setCertStatus("待审核");
        user.setRealName(realName);
        user.setIdCard(idCard);
        return Result.success(userRepository.save(user));
    }

    /**
     * 认证审核记录：GET /user/{id}/certifications
     * 查询 CertificationRepository，返回该用户的认证审核轨迹
     */
    @GetMapping("/{id}/certifications")
    public Result<List<Certification>> listCertifications(@PathVariable Long id) {
        // 校验用户存在
        getUserOrThrow(id);
        return Result.success(certificationRepository.findByUserIdOrderByIdDesc(id));
    }

    /**
     * 信用分概览：GET /user/{id}/credit
     * 返回 creditScore + 最近 10 条信用流水
     */
    @GetMapping("/{id}/credit")
    public Result<Map<String, Object>> getCredit(@PathVariable Long id) {
        User user = getUserOrThrow(id);
        List<CreditFlow> recentFlows = creditFlowRepository.findTop10ByUserIdOrderByTimestampDesc(id);
        Map<String, Object> data = new HashMap<>();
        data.put("creditScore", user.getCreditScore());
        data.put("recentFlows", recentFlows);
        return Result.success(data);
    }

    /**
     * 信用流水分页查询：GET /user/{id}/credit/flows?page=0&size=10
     */
    @GetMapping("/{id}/credit/flows")
    public Result<List<CreditFlow>> listCreditFlows(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        // 校验用户存在
        getUserOrThrow(id);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<CreditFlow> result = creditFlowRepository.findByUserIdOrderByTimestampDesc(id, pageable);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
    }
}
