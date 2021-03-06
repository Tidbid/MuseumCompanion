package com.romanov.rksp.museum.service;

import com.romanov.rksp.museum.MuseumApplication;
import com.romanov.rksp.museum.model.Exhibit;
import com.romanov.rksp.museum.model.Showpiece;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {
    private static final String exhDir = "exh/";
    private static final String shwpDir = "shwp/";
    private static final String contentUrl = "/img/";

    @Override
    public String getRandomExhibitionImage() {
        return null;
    }

    @Override
    public String getDefaultShowpieceImage() {
        return null;
    }

    @Override
    //TODO looks ugly, change it
    public String saveExhibitionImage(Exhibit exhibit, MultipartFile exhImg) {
        String retUrl;
        if (exhImg.isEmpty() && (exhibit.getImageUrl() == null || exhibit.getImageUrl().isEmpty())) {
            log.info("Assigning random image to exhibit: {}", exhibit.getName());
            retUrl = getRandomExhibitionImage();
        } else if (exhImg.isEmpty()) {
            //user did not change the image
            return exhibit.getImageUrl();
        } else {
            try {
                String fileName =
                        StringUtils.cleanPath(Objects.requireNonNull(exhImg.getOriginalFilename()));
                String[] parts = fileName.split("[.]");
                fileName = exhibit.getId().toString() + "." + parts[parts.length - 1];
                byte[] bytes = exhImg.getBytes();
                Path path =
                        Paths.get(MuseumApplication.IMAGE_EXH_DIR + fileName);
                Files.write(path, bytes);
                retUrl = contentUrl + exhDir + fileName;
            } catch (Exception e) {
                log.error("Failed to assign new image to exhibition: {}.  With error message: {}",
                        exhibit.getName(),
                        e.getMessage());
                //if no image then random, else keep
                retUrl =
                        (exhibit.getImageUrl() == null || exhibit.getImageUrl().isEmpty()) ?
                                getRandomExhibitionImage() : exhibit.getImageUrl();
            }
        }
        return retUrl;
    }

    @Override
    //TODO looks ugly, change it
    public String saveShowpieceImage(Showpiece showpiece, MultipartFile showpieceImg) {
        String retUrl;
        if (showpieceImg.isEmpty() && (showpiece.getImageUrl() == null || showpiece.getImageUrl().isEmpty())) {
            //user decided not to choose an image
            log.info("Assigning default image to showpiece: {}", showpiece.getName());
            return getDefaultShowpieceImage();
        } else if (showpieceImg.isEmpty()) {
            //user did not change the image
            return showpiece.getImageUrl();
        } else {
            try {
                String fileName =
                        StringUtils.cleanPath(Objects.requireNonNull(showpieceImg.getOriginalFilename()));
                String[] parts = fileName.split("[.]");
                fileName = showpiece.getId().toString() + "." + parts[parts.length - 1];
                byte[] bytes = showpieceImg.getBytes();
                Path path =
                        Paths.get(MuseumApplication.IMAGE_SHWP_DIR + fileName);
                Files.write(path, bytes);
                retUrl = contentUrl + shwpDir + fileName;
            } catch (Exception e) {
                log.error("Failed to assign new image to showpiece: {}.  With error message: {}",
                        showpiece.getName(),
                        e.getMessage());
                //if no image then default, else keep
                retUrl =
                        (showpiece.getImageUrl() == null || showpiece.getImageUrl().isEmpty()) ?
                                getDefaultShowpieceImage() : showpiece.getImageUrl();
            }
        }
        return retUrl;
    }

    @Override
    public List<String> showAllExhibitionImages() {
        return null;
    }

    @Override
    public List<String> showAllShowpieceImages() {
        return null;
    }
}
