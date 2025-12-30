package com.kh.shop.repository;

import com.kh.shop.entity.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {
    List<Popup> findByUseYnOrderByPopupOrderAsc(String useYn);
}