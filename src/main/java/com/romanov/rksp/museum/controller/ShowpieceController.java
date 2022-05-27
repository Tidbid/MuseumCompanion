package com.romanov.rksp.museum.controller;

import com.romanov.rksp.museum.model.Exhibit;
import com.romanov.rksp.museum.model.Hall;
import com.romanov.rksp.museum.model.Showpiece;
import com.romanov.rksp.museum.service.ImageService;
import com.romanov.rksp.museum.service.ShowpieceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/museum")
public class ShowpieceController {
    private final ShowpieceService showpieceService;
    private final ImageService imageService;

    @GetMapping("/browse/showpieces/more")
    public String viewShowpieceMore(@RequestParam Long shwp_id, Model model) {
        model.addAttribute("showpiece", showpieceService.findShowpieceById(shwp_id));
        return "showpiece_more";
    }

    @GetMapping("/browse/showpieces/orphans")
    public String viewOrphanShowpieces(Model model) {
        //Using the same view that
        //displays showpieces of a hall
        //but here we display showpieces with
        //no hall assigned
        Hall stub = new Hall("Свободные Экспонаты:", showpieceService.findVacantShowpieces());
        model.addAttribute("hall", stub);
        return "showpieces";
    }

    //TODO allow to specify hall from this view
    @GetMapping("/edit/showpieces/create")
    public String showNewShowpieceForm(Model model) {
        model.addAttribute("showpiece", new Showpiece());
        model.addAttribute("head", "Создать");
        return "modify_form_shwp";
    }

    @GetMapping("/edit/showpieces/update")
    public String updateShowpiece(@RequestParam Long shwp_id, Model model) {
        Showpiece showpiece = showpieceService.findShowpieceById(shwp_id);
        model.addAttribute("showpiece", showpiece);
        model.addAttribute("head", "Изменить");
        return "modify_form_shwp";
    }

    @PostMapping("/edit/showpieces/save")
    public String saveShowpiece(
            @ModelAttribute("showpiece") Showpiece showpiece,
            @RequestParam("image") MultipartFile multipartFile
    ) {
        showpieceService.saveShowpiece(showpiece);
        String imgUrl = imageService.saveShowpieceImage(showpiece, multipartFile);
        showpiece.setImageUrl(imgUrl);
        //TODO look into JPA more closely
        // this entity should be persisted
        // but the session does not flush and
        // changes are not committed without this save
        // (should remove it but don't care to rn)
        showpieceService.saveShowpiece(showpiece);
        return "redirect:/museum/browse/showpieces/more?shwp_id=" + showpiece.getId().toString();
    }

    @GetMapping("/edit/showpieces/delete")
    public String deleteShowpiece(@RequestParam Long shwp_id, Model model){
        Showpiece showpiece = showpieceService.findShowpieceById(shwp_id);
        String ret = "redirect:/museum/browse/";
        ret += (showpiece.getHall() == null) ?
                "showpieces/orphans" : "/halls/showpieces?hall_id=" + showpiece.getHall().getId();
        //TODO delete images, since they will clog
        showpieceService.deleteShowpieceById(shwp_id);
        return ret;
    }
}
