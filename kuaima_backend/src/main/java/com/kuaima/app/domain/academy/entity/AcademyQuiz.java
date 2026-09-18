package com.kuaima.app.domain.academy.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 接单课堂新手答题题库。 */
@Entity
@Table(name = "academy_quiz")
@Getter
@Setter
public class AcademyQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** single / multi / judge。 */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String stem;

    /** JSON 数组。 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String options;

    /** JSON 数组，元素为 options 下标。 */
    @Column(nullable = false, length = 100)
    private String answer;

    @Column(nullable = false)
    private Integer sort = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
