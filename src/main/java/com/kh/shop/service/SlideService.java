package com.kh.shop.service;

import com.kh.shop.entity.Slide;
import com.kh.shop.repository.SlideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlideService {

    @Autowired
    private SlideRepository slideRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 전체 슬라이드 조회 (Admin용)
    public List<Slide> getAllSlides() {
        return slideRepository.findByUseYnOrderBySlideOrderAsc("Y");
    }

    // 활성화된 슬라이드 조회 (Client용)
    public List<Slide> getActiveSlides() {
        return slideRepository.findActiveSlides(LocalDateTime.now());
    }

    // 슬라이드 상세 조회
    public Optional<Slide> getSlideById(Long slideId) {
        return slideRepository.findById(slideId);
    }

    // 슬라이드 등록
    @Transactional
    public Slide createSlide(String slideTitle, String slideDescription, String linkUrl,
                             Integer duration, Integer slideOrder,
                             LocalDateTime startDate, LocalDateTime endDate,
                             MultipartFile image) throws IOException {

        String imageUrl = saveFile(image);

        Slide slide = Slide.builder()
                .slideTitle(slideTitle)
                .slideDescription(slideDescription)
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .duration(duration != null ? duration : 5)
                .slideOrder(slideOrder != null ? slideOrder : 0)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return slideRepository.save(slide);
    }

    // 슬라이드 수정
    @Transactional
    public Slide updateSlide(Long slideId, String slideTitle, String slideDescription, String linkUrl,
                             Integer duration, Integer slideOrder,
                             LocalDateTime startDate, LocalDateTime endDate,
                             MultipartFile image) throws IOException {

        Optional<Slide> slideOpt = slideRepository.findById(slideId);
        if (!slideOpt.isPresent()) {
            return null;
        }

        Slide slide = slideOpt.get();
        slide.setSlideTitle(slideTitle);
        slide.setSlideDescription(slideDescription);
        slide.setLinkUrl(linkUrl);
        slide.setDuration(duration != null ? duration : 5);
        slide.setSlideOrder(slideOrder != null ? slideOrder : 0);
        slide.setStartDate(startDate);
        slide.setEndDate(endDate);

        // 이미지 변경
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            deleteFile(slide.getImageUrl());
            String imageUrl = saveFile(image);
            slide.setImageUrl(imageUrl);
        }

        return slideRepository.save(slide);
    }

    // 슬라이드 삭제 (soft delete)
    @Transactional
    public void deleteSlide(Long slideId) {
        Optional<Slide> slideOpt = slideRepository.findById(slideId);
        if (slideOpt.isPresent()) {
            Slide slide = slideOpt.get();
            slide.setUseYn("N");
            slideRepository.save(slide);
        }
    }

    // 파일 저장
    private String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        String dirPath = uploadDir + "/slide";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File savedFile = new File(dirPath + "/" + savedFilename);
        file.transferTo(savedFile);

        return "/uploads/slide/" + savedFilename;
    }

    // 파일 삭제
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