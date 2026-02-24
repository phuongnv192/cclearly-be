package com.swp391.cclearly.repository;

import com.swp391.cclearly.entity.ContentBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentBannerRepository extends JpaRepository<ContentBanner, UUID> {
    Optional<ContentBanner> findByPosition(String position);
}
