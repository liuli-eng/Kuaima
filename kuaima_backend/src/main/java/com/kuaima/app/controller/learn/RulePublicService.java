package com.kuaima.app.controller.learn;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import jakarta.persistence.EntityNotFoundException;
@Service
public class RulePublicService {
    private static final List<String> PUBLISHED_STATUSES = List.of("已发布", "published");
    private static final Map<String, Set<String>> CATEGORY_MAP = Map.of(
            "NOTICE", Set.of("规则公示", "通知公告", "NOTICE"),
            "CREDIT", Set.of("信用分规则", "信用评定", "CREDIT"),
            "FEE", Set.of("收费规则", "收费标准", "FEE"),
            "TRADE", Set.of("交易规则", "TRADE"),
            "FLY", Set.of("飞单认定与处理规则", "飞单", "FLY"));
    private final RulesRepository repository;
    public RulePublicService(RulesRepository repository) { this.repository = repository; }
    public List<Rules> list(String category) {
        if (category == null || category.isBlank()) return repository.findByStatusIn(PUBLISHED_STATUSES);
        String normalized = category.trim().toUpperCase(Locale.ROOT);
        Set<String> databaseCategories = CATEGORY_MAP.get(normalized);
        if (databaseCategories != null) {
            return repository.findByStatusIn(PUBLISHED_STATUSES).stream()
                    .filter(r -> databaseCategories.contains(r.getCategory()))
                    .toList();
        }
        boolean legacyCategory = CATEGORY_MAP.values().stream().anyMatch(values -> values.contains(category.trim()));
        if (legacyCategory) return repository.findByStatusInAndCategory(PUBLISHED_STATUSES, category.trim());
        throw new IllegalArgumentException("规则分类不合法，仅支持 NOTICE、CREDIT、FEE、TRADE、FLY");
    }
    public Rules get(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("规则 ID 必须为正整数");
        return repository.findById(id).filter(r -> PUBLISHED_STATUSES.contains(r.getStatus()))
                .orElseThrow(() -> new EntityNotFoundException("规则不存在或未发布: " + id));
    }
}
