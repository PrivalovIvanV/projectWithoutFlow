package com.example.final1.controllers;

import com.example.final1.models.Book;
import com.example.final1.models.Person;
import com.example.final1.servises.BookService;
import com.example.final1.servises.PersonService;
import com.example.final1.servises.SettingsService;
import com.example.final1.util.personalSettings.settings.BookFilter;
import com.example.final1.util.personalSettings.settings.PageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final BookService bookService;
    private final PersonService personSer;
    private final SettingsService settingsService;



    @GetMapping("/{id}")
    public String bookPage(@PathVariable("id") int id,
                           @RequestParam( name = "isCatalog", required = false) String isCatalog,
                           Model model){

        boolean isLibrary;
        boolean response;
        Book bookResp =  bookService.findById(id).get();

        if (isPersonAuth()) {
            response = personSer.getCurrentUser().isOwnerThisBook(id);
        }else response = false;
        if (isCatalog == null) {
            isLibrary = false;
        } else isLibrary = true;

        model.addAttribute("isLibrary", isLibrary);
        model.addAttribute("isBookOwnedByCurrentUser", response);
        model.addAttribute("lastSearch", lastSearch());
        model.addAttribute("book", bookResp);
        return "book/BookPage";
    }



    @GetMapping
    public String catalog(@RequestParam(name = "q", required = false) String query,
                          @RequestParam(name = "page", required = false) Integer page,
                          @RequestParam(name = "CS", required = false) boolean CS,
                          @RequestParam(name = "fan", required = false) boolean FICTION,
                          @RequestParam(name = "hist", required = false) boolean HISTORY,
                          @RequestParam(name = "comics", required = false) boolean COMICS,
                          @RequestParam(name = "isAll", required = false) String isAll,
                          Model model){

        List<Integer> pageIterator;
        BookFilter bookFilter;
        List<Book> listBook;

        bookFilter = settingsService.addCatalogFilter(page, query, isAll, CS, FICTION, HISTORY, COMICS);
        listBook = bookService.findAll(lastSearch(), lastPage());
        pageIterator = PageIterator(bookFilter);

        model.addAttribute("bookFilter", bookFilter);
        model.addAttribute("currentPage", lastPage());
        model.addAttribute("searchVal", lastSearch());
        model.addAttribute("bookList", listBook);
        model.addAttribute("PageIterator", pageIterator);
        return "book/BookCatalog";
    }







    @PostMapping("/{id}")
    public String addBookOwner(@PathVariable("id") int id, Model model){
        if (personSer.isAuth()) {
            log.warn("Попытка добавить книгу");
            bookService.addBookOwner(id, personSer.getCurrentUser().getId());
        }


        model.addAttribute("searchVal", lastSearch());
        model.addAttribute("book", bookService.findById(id).get());
        return "redirect:/catalog/" + id;
    }








    public List<Integer> PageIterator(BookFilter bookFilter){
        List<Integer> list = new ArrayList<>();
        int numOfPage;

        if (bookFilter.isHaveAFilter()){
            int listSize = bookService.findAllWithFilter(lastSearch()).size();
            numOfPage = listSize/15;
            if (listSize%15 != 0) numOfPage++;
        }else{
            int listSize = bookService.findAll(lastSearch()).size();
            numOfPage = listSize/15;
            if (listSize%15 != 0) numOfPage++;
        }

        for (int i = 0; i < numOfPage; i++){
            list.add(i);
        }
        return list;
    }//нужно, чтобы можно было по страничкам ходить



    @ModelAttribute(name = "isAuth")
    public boolean isPersonAuth(){ return personSer.isAuth();}

    @ModelAttribute(name = "AuthPerson")
    public Person getAuthPerson(){ return personSer.getCurrentUser();}

    private int lastPage(){
        try {
            return ((PageStatus) settingsService.get("PageStatus")).getLastPage();
        } catch (Exception e) {
            return 0;
        }
    }
    private String lastSearch(){
        try {
            return ((PageStatus) settingsService.get("PageStatus")).getLastSearch();
        } catch (Exception e) {
            return "";
        }
    }


}
