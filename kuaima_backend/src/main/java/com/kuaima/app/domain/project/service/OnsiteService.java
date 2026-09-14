package com.kuaima.app.domain.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.project.entity.ProjectOnsiteStaff;
import com.kuaima.app.domain.project.repository.ProjectOnsiteStaffRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OnsiteService {

    private final ProjectOnsiteStaffRepository onsiteRepository;

    public OnsiteService(ProjectOnsiteStaffRepository onsiteRepository) {
        this.onsiteRepository = onsiteRepository;
    }

    public List<ProjectOnsiteStaff> list(Long projectId) {
        return onsiteRepository.findByProjectId(projectId);
    }

    public ProjectOnsiteStaff getOrThrow(Long id) {
        return onsiteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("驻场人员不存在: " + id));
    }

    @Transactional
    public ProjectOnsiteStaff add(ProjectOnsiteStaff staff, Long operatorId) {
        if (staff.getOnsiteRole() == null) {
            staff.setOnsiteRole(com.kuaima.app.domain.project.constant.ProjectConstants.ONSITE_ASSISTANT);
        }
        if (staff.getOnsiteDays() == null) {
            staff.setOnsiteDays(0);
        }
        if (operatorId != null) {
            staff.setCreateBy(operatorId);
        }
        return onsiteRepository.save(staff);
    }

    @Transactional
    public void remove(Long id) {
        if (!onsiteRepository.existsById(id)) {
            throw new EntityNotFoundException("驻场人员不存在: " + id);
        }
        onsiteRepository.deleteById(id);
    }
}
