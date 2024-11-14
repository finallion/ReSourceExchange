package com.resexchange.app.controller;

import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.MaterialRepository;
import com.resexchange.app.security.REUserDetails;
import com.resexchange.app.services.MaterialService;
import com.resexchange.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {this.materialService = materialService;}

    // GET-Request, um das Material-Erstellungsformular zu zeigen
    @GetMapping("/create")
    public String showMaterialForm(Model model) {
        model.addAttribute("material", new Material());
        return "addMaterial";  // Thymeleaf-Template für das Formular
    }

    // POST-Request, um das Material zu speichern
    @PostMapping("/create")
    public String saveMaterial(Material material) {
        materialService.addMaterial(material);
        return "redirect:/main";
    }
}
