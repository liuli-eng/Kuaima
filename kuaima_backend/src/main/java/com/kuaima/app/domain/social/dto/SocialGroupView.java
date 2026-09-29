package com.kuaima.app.domain.social.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SocialGroupView(Long id,
                              String name,
                              String category,
                              String description,
                              String qrcodeUrl,
                              int memberCount,
                              int memberLimit,
                              LocalDateTime createdAt,
                              String status,
                              boolean joined,
                              boolean full,
                              List<String> avatarUrls,
                              int sort) {
}
