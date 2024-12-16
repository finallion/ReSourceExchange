package com.resexchange.app.controller;

import com.resexchange.app.model.Material;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MaterialRepository;
import com.resexchange.app.services.MaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);

    @Autowired
    private MaterialRepository materialRepository;

    private final MaterialService materialService;
    @Autowired
    private ListingRepository listingRepository;

    public MaterialController(MaterialService materialService) {this.materialService = materialService;}

    // GET-Request, um das Material-Erstellungsformular zu zeigen
    @GetMapping("/create")
    public String showMaterialForm(Model model) {
        LOGGER.info("Displaying material creation form");
        model.addAttribute("material", new Material());
        return "admin/addMaterial";  // Thymeleaf-Template für das Formular
    }

    // POST-Request, um das Material zu speichern
    @PostMapping("/create")
    public String saveMaterial(Material material) {
        LOGGER.info("Saving material: {}", material);

        materialService.addMaterial(material);

        LOGGER.info("Material saved successfully: {}", material);

        return "redirect:/material/overview";
    }

    // Übersicht anzeigen
    @GetMapping("/overview")
    public String showMaterialOverview(Model model) {
        LOGGER.info("Fetching all materials for overview");

        model.addAttribute("materials", materialRepository.findAll());

        LOGGER.info("Materials fetched successfully");

        return "admin/materialOverview"; // Thymeleaf-Template
    }

    // Material löschen
    @GetMapping("/delete/{id}")
    public String deleteMaterial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        LOGGER.info("Attempting to delete material with ID: {}", id);

        boolean isMaterialUsed = listingRepository.existsByMaterial(materialRepository.getReferenceById(id));

        if (isMaterialUsed) {
            LOGGER.warn("Material with ID: {} cannot be deleted because it is in use in one or more listings.", id);
            redirectAttributes.addFlashAttribute("error", "Material cannot be deleted because it is used in one or more listings.");
            return "redirect:/material/overview";
        }

        materialRepository.deleteById(id);
        LOGGER.info("Material with ID: {} deleted successfully", id);
        return "redirect:/material/overview"; // Nach dem Löschen zur Übersicht umleiten
    }

    // Material bearbeiten
    @GetMapping("/edit/{id}")
    public String editMaterial(@PathVariable Long id, Model model) {
        LOGGER.info("Attempting to edit material with ID: {}", id);

        Optional<Material> material = materialRepository.findById(id);
        if (material.isPresent()) {
            LOGGER.info("Material with ID: {} found, loading the edit form.", id);
            model.addAttribute("material", material.get());
            return "admin/addMaterial";
        }

        LOGGER.warn("Material with ID: {} not found, redirecting to material overview.", id);
        return "redirect:/material/overview";
    }

    // Aktualisierte Daten speichern
    @PostMapping("/edit")
    public String updateMaterial(Material material) {
        LOGGER.info("Attempting to update material with ID: {}", material.getId());

        materialRepository.save(material);

        LOGGER.info("Material with ID: {} successfully updated.", material.getId());
        return "redirect:/material/overview"; // Nach dem Speichern zur Übersicht umleiten
    }
}
