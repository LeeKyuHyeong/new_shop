package com.kh.shop.service;

import com.kh.shop.entity.Popup;
import com.kh.shop.repository.PopupRepository;
import com.kh.shop.security.FileUploadValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PopupService {

    @Autowired
    private PopupRepository popupRepository;

    @Autowired
    private FileUploadValidator fileUploadValidator;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<Popup> getAllPopups() {
        return popupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Popup> getActivePopups() {
        return popupRepository.findByUseYnOrderByPopupOrderAsc("Y");
    }

    @Transactional(readOnly = true)
    public Optional<Popup> getPopupById(Long popupId) {
        return popupRepository.findById(popupId);
    }

    @Transactional
    public Popup createPopup(String popupTitle, String popupContent, String popupLink,
                             Integer popupWidth, Integer popupHeight, Integer popupTop,
                             Integer popupLeft, Integer popupOrder, MultipartFile popupImage) throws IOException {

        String imageUrl = null;
        if (popupImage != null && !popupImage.isEmpty()) {
            imageUrl = saveFile(popupImage);
        }

        Popup popup = Popup.builder()
                .popupTitle(popupTitle)
                .popupContent(popupContent)
                .popupImageUrl(imageUrl)
                .popupLink(popupLink)
                .popupWidth(popupWidth)
                .popupHeight(popupHeight)
                .popupTop(popupTop)
                .popupLeft(popupLeft)
                .popupOrder(popupOrder)
                .build();

        return popupRepository.save(popup);
    }

    @Transactional
    public Popup updatePopup(Long popupId, String popupTitle, String popupContent, String popupLink,
                             Integer popupWidth, Integer popupHeight, Integer popupTop,
                             Integer popupLeft, Integer popupOrder, MultipartFile popupImage) throws IOException {

        Optional<Popup> popupOpt = popupRepository.findById(popupId);
        if (!popupOpt.isPresent()) {
            return null;
        }

        Popup popup = popupOpt.get();
        popup.setPopupTitle(popupTitle);
        popup.setPopupContent(popupContent);
        popup.setPopupLink(popupLink);
        popup.setPopupWidth(popupWidth);
        popup.setPopupHeight(popupHeight);
        popup.setPopupTop(popupTop);
        popup.setPopupLeft(popupLeft);
        popup.setPopupOrder(popupOrder);

        if (popupImage != null && !popupImage.isEmpty()) {
            if (popup.getPopupImageUrl() != null) {
                deleteFile(popup.getPopupImageUrl());
            }
            String imageUrl = saveFile(popupImage);
            popup.setPopupImageUrl(imageUrl);
        }

        return popupRepository.save(popup);
    }

    @Transactional
    public void deletePopup(Long popupId) {
        Optional<Popup> popupOpt = popupRepository.findById(popupId);
        if (popupOpt.isPresent()) {
            Popup popup = popupOpt.get();
            popup.setUseYn("N");
            popupRepository.save(popup);
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        // 보안 검증 (확장자 화이트리스트 + MIME + 매직 바이트 + 경로 조작 차단)
        FileUploadValidator.ValidationResult result = fileUploadValidator.validateImage(file);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }

        // 원본 파일명 무시, 검증된 안전한 확장자만 사용
        String safeExtension = fileUploadValidator.getSafeExtension(file.getOriginalFilename());
        if (safeExtension.isEmpty()) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        String savedFilename = UUID.randomUUID().toString() + safeExtension;

        String dirPath = uploadDir + "/popup";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성 실패: " + dirPath);
            }
        }

        File savedFile = new File(dirPath + "/" + savedFilename);
        file.transferTo(savedFile);

        return "/uploads/popup/" + savedFilename;
    }

    private void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String filePath = uploadDir + fileUrl.substring("/uploads".length());
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}