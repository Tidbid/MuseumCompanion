package com.romanov.rksp.museum.controller;

import com.romanov.rksp.museum.dto.ExhibitHallsDto;
import com.romanov.rksp.museum.model.Exhibit;
import com.romanov.rksp.museum.service.ExhibitService;
import com.romanov.rksp.museum.service.HallService;
import com.romanov.rksp.museum.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/museum")
public class ExhibitionController {
    private final ExhibitService exhibitService;
    private final HallService hallService;
    private final ImageService imageService;

    @GetMapping("/browse/exhibitions")
    public String viewExhibitions(Model model) {
        //TODO display only ongoing exhibitions
        // or separate them into two parts:
        // ongoing and closed
        List<Exhibit> exhibitionsList = this.exhibitService.findAllExhibits();
        model.addAttribute("exhibitions", exhibitionsList);
        return "all_exh";
    }

    @GetMapping("/browse/exhibitions/more")
    public String viewExhibitionMore(@RequestParam(value="exh_id") Long exhId, Model model) {
        Exhibit exhibit = exhibitService.findExhibitById(exhId);
        model.addAttribute("exhibit", exhibit);
        model.addAttribute("start",
                exhibit.getDateStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        model.addAttribute("finish",
                (exhibit.getDateFinish() == null) ?
                        "конца времен" : exhibit.getDateStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        return "exhibition_more";
    }

    @GetMapping("/browse/exhibitions/halls")
    public String viewHalls(@RequestParam Long exh_id, Model model) {
        model.addAttribute("exhibit", exhibitService.findExhibitById(exh_id));
        return "halls";
    }

    @GetMapping("/edit/exhibitions/create")
    public String showNewExhibitForm(Model model) {
        Exhibit exhibit = new Exhibit();
        exhibit.setHalls(new ArrayList<>());
        model.addAttribute("exhibitHallsDto", new ExhibitHallsDto(exhibit, new ArrayList<>()));
        model.addAttribute("vacantHalls", hallService.findVacantHalls());
        model.addAttribute("head", "Создать");
            return "modify_form_exh";
    }

    @GetMapping("/edit/exhibitions/update")
    public String updateExhibit(@RequestParam Long exh_id, Model model) {
        Exhibit exhibit = exhibitService.findExhibitById(exh_id);
        model.addAttribute("exhibitHallsDto",
                new ExhibitHallsDto(exhibit, exhibit.getHalls()));
        model.addAttribute("vacantHalls", hallService.findVacantHalls());
        model.addAttribute("head", "Изменить");
        return "modify_form_exh";
    }

    @PostMapping("/edit/exhibitions/save")
    public String saveExhibit(
            @ModelAttribute("exhibitHallsDto") ExhibitHallsDto exhibitHallsDto,
            @RequestParam("image") MultipartFile multipartFile
    ) {
        Exhibit exhibit = exhibitService.saveExhibitAndProcessHalls(
                exhibitHallsDto.getExhibit(),
                exhibitHallsDto.getHallsToAdd()
        );
        String updatedUrl = imageService.saveExhibitionImage(exhibit, multipartFile);
        if (!exhibit.getImageUrl().equals(updatedUrl))
            exhibitService.updateImageById(exhibit.getId(), updatedUrl);
        return "redirect:/museum/browse/exhibitions/halls?exh_id=" + exhibit.getId().toString();
    }

    @GetMapping("/edit/exhibitions/delete")
    public String deleteExhibit(@RequestParam Long exh_id, @RequestParam Boolean erase){
        exhibitService.deleteExhibitAndProcessHalls(exh_id, erase);
        //TODO add image deletion
        return "redirect:/museum/browse/exhibitions";
    }
}
