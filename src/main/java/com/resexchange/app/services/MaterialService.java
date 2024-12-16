package com.resexchange.app.services;

import com.resexchange.app.model.Company;
import com.resexchange.app.model.Material;
import com.resexchange.app.model.PrivateUser;
import com.resexchange.app.model.Role;
import com.resexchange.app.repositories.MaterialRepository;
import com.resexchange.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service-Klasse, die verschiedene Operationen für Materialien durchführt.
 * Diese Klasse verwaltet das Hinzufügen, Abrufen, Aktualisieren und Löschen von Materialien,
 * welche mit Listings verknüpft werden können
 *
 * @author Stefan
 */
@Service
public class MaterialService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialService.class);

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    /**
     * Add Material to the Database
     */
    public void addMaterial(Material material) {
        LOGGER.info("Attempting to add material: {}", material.getName());

        try {
            if (material.getName() == null) {
                LOGGER.warn("Attempted to add null material");
                throw new IllegalArgumentException("Material cannot be null");
            }

            materialRepository.save(material);

            LOGGER.info("Material successfully added: {}", material.getName());
        } catch (Exception e) {
            LOGGER.error("Error occurred while adding material: {}", material.getName() != null ? material.getName() : "null", e);
            throw new RuntimeException("Error occurred while adding material", e); // Optional: Exception weiterwerfen
        }
    }

    /**
     * Get every Material from the Database
     */
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }
}
