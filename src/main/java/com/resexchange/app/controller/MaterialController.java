package com.resexchange.app.controller;

import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MaterialRepository;
import com.resexchange.app.services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    private final MaterialService materialService;
    @Autowired
    private ListingRepository listingRepository;

    public MaterialController(MaterialService materialService) {this.materialService = materialService;}

    // GET-Request, um das Material-Erstellungsformular zu zeigen
    @GetMapping("/create")
    public String showMaterialForm(Model model) {
        model.addAttribute("material", new Material());
        return "admin/addMaterial";  // Thymeleaf-Template für das Formular
    }

    // POST-Request, um das Material zu speichern
    @PostMapping("/create")
    public String saveMaterial(Material material) {
        materialService.addMaterial(material);
        return "redirect:/material/overview";
    }

    // Übersicht anzeigen
    @GetMapping("/overview")
    public String showMaterialOverview(Model model) {
        model.addAttribute("materials", materialRepository.findAll());
        return "admin/materialOverview"; // Thymeleaf-Template
    }

    // Material löschen
    @GetMapping("/delete/{id}")
    public String deleteMaterial(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        boolean isMaterialUsed = listingRepository.existsByMaterial(materialRepository.getReferenceById(id));

        if (isMaterialUsed) {
            // Fehlermeldung hinzufügen
            redirectAttributes.addFlashAttribute("error", "Material cannot be deleted because it is used in one or more listings.");
            return "redirect:/material/overview";
        }

        materialRepository.deleteById(id);
        return "redirect:/material/overview"; // Nach dem Löschen zur Übersicht umleiten
    }

    // Material bearbeiten
    @GetMapping("/edit/{id}")
    public String editMaterial(@PathVariable Long id, Model model) {
        Optional<Material> material = materialRepository.findById(id);
        if (material.isPresent()) {
            model.addAttribute("material", material.get());
            return "admin/addMaterial"; // Reuse des Material-Formulars
        }
        return "redirect:/material/overview"; // Wenn das Material nicht gefunden wird, zur Übersicht zurückkehren
    }

    // Aktualisierte Daten speichern
    @PostMapping("/edit")
    public String updateMaterial(Material material) {
        materialRepository.save(material);
        return "redirect:/material/overview"; // Nach dem Speichern zur Übersicht umleiten
    }
}
