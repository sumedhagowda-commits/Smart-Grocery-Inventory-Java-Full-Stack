package com.grocery.controller;

import com.grocery.model.AppUser;
import com.grocery.model.GroceryItem;
import com.grocery.repository.GroceryItemRepository;
import com.grocery.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GroceryController {
    private final GroceryItemRepository items;
    private final UserRepository users;

    public GroceryController(GroceryItemRepository items, UserRepository users) {
        this.items = items;
        this.users = users;
    }

    private AppUser currentUser(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (!(id instanceof Long)) return null;
        return users.findById((Long) id).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        List<GroceryItem> all = items.findByOwnerOrderByItemNameAsc(user);
        LocalDate today = LocalDate.now();
        List<GroceryItem> low = all.stream().filter(i -> i.getQuantity() <= i.getMinStock()).toList();
        List<GroceryItem> expiring = all.stream()
                .filter(i -> i.getExpiryDate() != null && !i.getExpiryDate().isBefore(today)
                        && !i.getExpiryDate().isAfter(today.plusDays(7))).toList();
        List<GroceryItem> expired = all.stream()
                .filter(i -> i.getExpiryDate() != null && i.getExpiryDate().isBefore(today)).toList();
        model.addAttribute("items", all);
        model.addAttribute("totalItems", all.size());
        model.addAttribute("totalQuantity", all.stream().mapToDouble(GroceryItem::getQuantity).sum());
        model.addAttribute("lowStock", low);
        model.addAttribute("expiring", expiring);
        model.addAttribute("expired", expired);
        model.addAttribute("shoppingList", low);
        model.addAttribute("userName", user.getName());
        return "dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String category,
                            HttpSession session, Model model) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        List<GroceryItem> all = items.findByOwnerOrderByItemNameAsc(user);
        String q = search == null ? "" : search.trim().toLowerCase();
        String c = category == null ? "" : category.trim();
        List<GroceryItem> filtered = all.stream()
                .filter(i -> i.getItemName().toLowerCase().contains(q))
                .filter(i -> c.isBlank() || i.getCategory().equalsIgnoreCase(c))
                .toList();
        List<String> categories = all.stream().map(GroceryItem::getCategory).distinct().sorted().toList();
        model.addAttribute("items", filtered);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedCategory", c);
        model.addAttribute("userName", user.getName());
        return "inventory";
    }

    @GetMapping("/items/new")
    public String newItem(HttpSession session, Model model) {
        if (currentUser(session) == null) return "redirect:/login";
        model.addAttribute("item", new GroceryItem());
        model.addAttribute("pageTitle", "Add Grocery Item");
        return "item-form";
    }

    @GetMapping("/items/{id}/edit")
    public String editItem(@PathVariable Long id, HttpSession session, Model model,
                           RedirectAttributes redirect) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        GroceryItem item = items.findByIdAndOwner(id, user).orElse(null);
        if (item == null) {
            redirect.addFlashAttribute("error", "Item not found.");
            return "redirect:/inventory";
        }
        model.addAttribute("item", item);
        model.addAttribute("pageTitle", "Edit Grocery Item");
        return "item-form";
    }

    @PostMapping("/items/save")
    public String saveItem(@RequestParam(required = false) Long id,
                           @RequestParam String itemName,
                           @RequestParam String category,
                           @RequestParam double quantity,
                           @RequestParam String unit,
                           @RequestParam double minStock,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
                           HttpSession session, RedirectAttributes redirect) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        if (itemName.isBlank() || category.isBlank() || unit.isBlank() || quantity < 0 || minStock < 0) {
            redirect.addFlashAttribute("error", "Enter valid item details. Quantity and minimum stock cannot be negative.");
            return id == null ? "redirect:/items/new" : "redirect:/items/" + id + "/edit";
        }
        GroceryItem item;
        if (id != null) {
            item = items.findByIdAndOwner(id, user).orElse(null);
            if (item == null) return "redirect:/inventory";
        } else {
            item = new GroceryItem();
            item.setOwner(user);
        }
        item.setItemName(itemName.trim());
        item.setCategory(category.trim());
        item.setQuantity(quantity);
        item.setUnit(unit.trim());
        item.setMinStock(minStock);
        item.setExpiryDate(expiryDate);
        items.save(item);
        redirect.addFlashAttribute("success", "Grocery item saved.");
        return "redirect:/inventory";
    }

    @PostMapping("/items/{id}/quantity")
    public String changeQuantity(@PathVariable Long id, @RequestParam String action,
                                 HttpSession session, RedirectAttributes redirect) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        GroceryItem item = items.findByIdAndOwner(id, user).orElse(null);
        if (item == null) return "redirect:/inventory";
        if ("increase".equals(action)) item.setQuantity(item.getQuantity() + 1);
        else if ("decrease".equals(action)) item.setQuantity(Math.max(0, item.getQuantity() - 1));
        items.save(item);
        redirect.addFlashAttribute("success", "Stock quantity updated.");
        return "redirect:/inventory";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        AppUser user = currentUser(session);
        if (user == null) return "redirect:/login";
        items.findByIdAndOwner(id, user).ifPresent(items::delete);
        redirect.addFlashAttribute("success", "Item deleted.");
        return "redirect:/inventory";
    }

    @GetMapping("/shopping-list.csv")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(HttpSession session) {
        AppUser user = currentUser(session);
        if (user == null) return org.springframework.http.ResponseEntity.status(401).build();
        List<GroceryItem> all = items.findByOwnerOrderByItemNameAsc(user);
        StringBuilder csv = new StringBuilder("Item,Category,Quantity,Unit,Minimum Stock,Expiry Date\\n");
        for (GroceryItem i : all) {
            csv.append(csvCell(i.getItemName())).append(',')
               .append(csvCell(i.getCategory())).append(',')
               .append(i.getQuantity()).append(',')
               .append(csvCell(i.getUnit())).append(',')
               .append(i.getMinStock()).append(',')
               .append(i.getExpiryDate() == null ? "" : i.getExpiryDate()).append("\\n");
        }
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=inventory.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String csvCell(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
