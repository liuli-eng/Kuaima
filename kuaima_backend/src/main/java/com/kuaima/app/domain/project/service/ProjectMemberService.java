package com.kuaima.app.domain.project.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.project.constant.ProjectConstants;
import com.kuaima.app.domain.project.entity.ProjectMember;
import com.kuaima.app.domain.project.repository.ProjectMemberRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository memberRepository;

    public ProjectMemberService(ProjectMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<ProjectMember> listMembers(Long projectId, String status) {
        if (ProjectConstants.MEMBER_ACTIVE.equals(status)
                || ProjectConstants.MEMBER_TEMP.equals(status)
                || ProjectConstants.MEMBER_LEFT.equals(status)) {
            return memberRepository.findByProjectIdAndStatus(projectId, status);
        }
        return memberRepository.findByProjectId(projectId);
    }

    public ProjectMember getOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("项目成员不存在: " + id));
    }

    @Transactional
    public ProjectMember add(ProjectMember member, Long operatorId) {
        member.setProjectId(member.getProjectId());
        if (member.getStatus() == null) {
            member.setStatus(ProjectConstants.MEMBER_TEMP);
        }
        if (operatorId != null) {
            member.setCreateBy(operatorId);
        }
        return memberRepository.save(member);
    }

    @Transactional
    public ProjectMember updateStatus(Long id, String status) {
        ProjectMember member = getOrThrow(id);
        member.setStatus(status);
        return memberRepository.save(member);
    }

    @Transactional
    public void remove(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException("项目成员不存在: " + id);
        }
        memberRepository.deleteById(id);
    }

    /** 成员统计：全部 / 在职 / 离职 / 临时。 */
    public Map<String, Long> stats(Long projectId) {
        Map<String, Long> result = new LinkedHashMap<>();
        List<ProjectMember> all = memberRepository.findByProjectId(projectId);
        long active = all.stream().filter(m -> ProjectConstants.MEMBER_ACTIVE.equals(m.getStatus())).count();
        long left = all.stream().filter(m -> ProjectConstants.MEMBER_LEFT.equals(m.getStatus())).count();
        long temp = all.stream().filter(m -> ProjectConstants.MEMBER_TEMP.equals(m.getStatus())).count();
        result.put("all", (long) all.size());
        result.put("active", active);
        result.put("left", left);
        result.put("temp", temp);
        return result;
    }
}
