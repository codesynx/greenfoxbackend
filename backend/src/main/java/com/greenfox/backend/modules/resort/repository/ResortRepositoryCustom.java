package com.greenfox.backend.modules.resort.repository;

import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.entity.Resort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository for complex resort queries.
 */
public interface ResortRepositoryCustom {
    Page<Resort> findAllWithDistance(ResortFilterRequest filter, Pageable pageable);
}

