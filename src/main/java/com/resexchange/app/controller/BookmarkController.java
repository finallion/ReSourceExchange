package com.resexchange.app.controller;

import com.resexchange.app.model.Bookmark;
import com.resexchange.app.model.Listing;
import com.resexchange.app.services.BookmarkService;
import com.resexchange.app.services.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookmark")
public class BookmarkController {
    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private ListingService listingService;

    @GetMapping("/{id}")
    public String getBookmarkDetails(@PathVariable Long id, Model model) {
        // Das Bookmark anhand der ID finden
        Bookmark bookmark = bookmarkService.findById(id);

        // Das Listing, das mit dem Bookmark verknüpft ist, finden
        Listing listing = listingService.getListingById(bookmark.getListing().getId());

        // Hinzufügen der Details zum Model, um sie in der View anzuzeigen
        model.addAttribute("bookmark", bookmark);
        model.addAttribute("listing", listing);


        return "listing-detail";
    }

    @GetMapping ("/delete/{id}")
    public String deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);

        return "redirect:/main";
    }

}

