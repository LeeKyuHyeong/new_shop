package com.kh.shop.repository;

import com.kh.shop.entity.BatchConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchConfigRepository extends JpaRepository<BatchConfig, String> {
}
