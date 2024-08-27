package com.example.login.web;

import com.example.login.model.User;
import com.example.login.model.UserDTO;
import com.example.login.service.UserService;
import com.example.login.util.MaskingUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

/**
 * AdminController hanterar webbförfrågningar relaterade till administration, såsom registrering och borttagning av användare.
 */
@Controller
public class AdminController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/register`.
     * När en användare navigerar till denna URL,
     * kommer metoden att köras och den kommer att förbereda data för registreringsformuläret.
     *
     * Inuti metoden skapas en ny `UserDTO`-instans och läggs till i modellen under namnet "user".
     * Detta gör att ett tomt användarobjekt är tillgängligt för vyn som ska renderas.
     * Metoden loggar också ett debug-meddelande med texten "Register user" för att indikera att registreringssidan har begärts.
     *
     * Slutligen returnerar metoden namnet på vyn som ska användas, vilket är "register_form".
     * Detta innebär att applikationen kommer att använda en vyfil vid namn `register_form.html` eller motsvarande för att visa registreringsformuläret för användaren.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserDTO());
        logger.debug("Register user");
        return "register_form";
    }


    /**
     * Denna metod hanterar POST-förfrågningar till URL:en `/register`,
     * vilket innebär att den processar användardata som skickas vid registrering.
     * När registreringsformuläret skickas, kontrollerar metoden först om det finns några valideringsfel i den inskickade användardatan (`UserDTO`).
     * Om det finns fel, loggar den ett varningsmeddelande och returnerar formuläret igen så att användaren kan rätta till felen.
     *
     * Om inga valideringsfel upptäcks, försöker metoden registrera användaren via en tjänst (`userService`).
     * Vid en lyckad registrering loggas en framgångsrik registrering och användaren dirigeras till en bekräftelsesida.
     * Om det uppstår ett dataintegritetsproblem, som att e-postadressen redan är registrerad,
     * loggas detta som en varning och ett felmeddelande läggs till modellen för att visa ett fel på registreringssidan.
     *Vid andra oväntade fel loggas ett felmeddelande och användaren får ett generellt felmeddelande på registreringssidan.
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO, BindingResult bindingResult, Model model) {
        logger.debug("Processing registration for email: {}", MaskingUtils.anonymize(userDTO.getEmail()));
        if (bindingResult.hasErrors()) {
            logger.warn("Registration failed due to validation errors for email: {}", MaskingUtils.anonymize(userDTO.getEmail()));
            return "register_form";
        }

        try {
            userService.registerUser(userDTO);
            logger.debug("User successfully registered with email: {}", MaskingUtils.anonymize(userDTO.getEmail()));
            return "register_success";
        } catch (DataIntegrityViolationException e) {
            logger.warn("Registration failed due to email already being registered: {}", MaskingUtils.anonymize(userDTO.getEmail()));
            model.addAttribute("error", "Email already registered");
            return "register_error";
        } catch (Exception e) {
            logger.error("An unexpected error occurred during registration for email: {}", MaskingUtils.anonymize(userDTO.getEmail()), e);
            model.addAttribute("error", "An unexpected error occurred");
            return "register_error";
        }
    }


    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/homepage`.
     * När en användare besöker denna URL loggas ett meddelande med texten "Homepage" och metoden returnerar namnet på vyn som ska användas,
     * vilket är "homepage". '
     * Detta innebär att användaren kommer att se sidan `homepage.html` eller motsvarande vy när de navigerar till `/homepage`.
     */
    @GetMapping("/homepage")
    public String loggedIn() {
        logger.debug("Homepage");
        return "homepage";
    }


    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/login`.
     * När en användare besöker denna URL läggs en ny, tom instans av `UserDTO` till i modellen med namnet "user".
     * Detta gör att inloggningssidan kan använda denna instans, vanligtvis för att hantera användarens inloggningsuppgifter.
     * Metoden loggar också ett meddelande som säger "Showing login page" för att indikera att inloggningssidan visas.
     * Slutligen returnerar metoden namnet på vyn som ska renderas, vilket är "login".
     * Detta betyder att användaren kommer att se sidan `login.html` eller motsvarande vy när de navigerar till `/login`.
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new UserDTO());
        logger.debug("Showing login page.");
        return "login";
    }


    /**
     * Denna metod hanterar GET-förfrågningar till URL:en `/admin`.
     * När en användare besöker denna URL loggas ett meddelande som säger "Admin accessed admin page,"
     * vilket indikerar att administratörssidan har åtkommits.
     * Metoden returnerar namnet på vyn som ska användas, vilket är "admin_page".
     * Detta innebär att användaren kommer att se sidan `admin_page.html` eller motsvarande vy när de navigerar till `/admin`.
     */
    @GetMapping("/admin")
    public String adminPage() {
        logger.debug("Admin accessed admin page.");
        return "admin_page";
    }


    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/users`.
     * När en användare besöker denna URL hämtar metoden en lista över alla användare från tjänsten `userService` och lägger till denna lista i modellen under namnet "users".
     * Detta gör att listan över registrerade användare kan användas på vyn som renderas.
     * Metoden loggar också ett meddelande som säger "Showing usersPage with a list of registered users" för att indikera att användarsidan visas med en lista över användare.
     * Slutligen returnerar metoden namnet på vyn som ska användas, vilket är "users_list".
     * Detta innebär att användaren kommer att se sidan `users_list.html` eller motsvarande vy när de navigerar till `/users`.
     */
    @GetMapping("/users")
    public String userPage(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        logger.debug("Showing usersPage with a list of registered users.");
        return "users_list";
    }


    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/delete`.
     * När en användare besöker denna URL loggas ett meddelande som säger "Displaying user deletion form" för att indikera att formuläret för att ta bort en användare visas.
     * Metoden lägger till en ny, tom `UserDTO`-instans i modellen under namnet "user", vilket gör att formuläret kan använda detta objekt.
     * Den hämtar också en lista över alla användare från `userService` och lägger till den i modellen under namnet "users".
     * Detta gör att vyn kan visa en lista över användare som kan väljas för borttagning.
     * Metoden returnerar namnet på vyn som ska användas, vilket är "delete_form".
     * Detta innebär att användaren kommer att se sidan `delete_form.html` eller motsvarande vy när de navigerar till `/delete`.
     */
    @GetMapping("/delete")
    public String deleteUserForm(Model model) {
        logger.debug("Displaying user deletion form.");
        model.addAttribute("user", new UserDTO());
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "delete_form";
    }


    /**
     * Denna metod hanterar POST-förfrågningar till URL:en `/delete` och används för att ta bort en användare.
     * När formuläret skickas, försöker metoden att hitta en användare baserat på den e-postadress som anges i `UserDTO`.
     * Om användaren finns och inte har rollen "ADMIN", raderas användaren och en framgångssida visas.
     * Om användaren har rollen "ADMIN", visas ett felmeddelande som indikerar att administratörer inte kan raderas.
     * Ifall att användaren inte hittas, visas en sida som meddelar att användaren inte finns.
     * Vid eventuella fel under processen loggas ett felmeddelande och en fel-sida visas.
     */
    @PostMapping("/delete")
    public String deleteUser(@ModelAttribute("user") UserDTO user, Model model) {
        logger.debug("Processing user deletion.");

        try {

            Optional<User> userOptional = Optional.ofNullable(userService.findByEmail( HtmlUtils.htmlEscape(user.getEmail())));
            if (userOptional.isPresent()) {
                User user1 = userOptional.get();

                if (!user1.getRole().equals("ROLE_ADMIN")) {
                    logger.debug("User does not have the ADMIN role.");

                    userService.deleteUser(user1.getEmail());

                    logger.debug("User deleted: {}", MaskingUtils.anonymize(user1.getEmail()));
                    return "delete_success";
                } else {
                    logger.warn("ADMIN cannot be deleted.");
                    return "admin_error";
                }
            } else {
                logger.warn("User {} not found for deletion.", MaskingUtils.anonymize(user.getEmail()));
                model.addAttribute("id", HtmlUtils.htmlEscape(user.getEmail()));
                return "user_not_found";
            }
        } catch (Exception e) {
            logger.error("An error occurred while deleting the user: {}", MaskingUtils.anonymize(user.getEmail()), e);
            model.addAttribute("error", "An error occurred while deleting the user.");
            return "delete_error";
        }
    }


    /**
     *Denna metod hanterar GET-förfrågningar till URL:en `/delete_success`.
     * När användaren navigerar till denna URL loggas ett meddelande som säger "User deleted successfully,"
     * vilket indikerar att en användare har raderats framgångsrikt.
     * Metoden returnerar namnet på vyn som ska användas, vilket är "delete_success".
     * Detta betyder att användaren kommer att se sidan `delete_success.html` eller motsvarande vy,
     * som bekräftar att borttagningen av användaren har genomförts utan problem.
     */
    @GetMapping("/delete_success")
    public String deleteSuccess() {
        logger.debug("User deleted successfully.");
        return "delete_success";
    }


    /**
     * Denna metod hanterar GET-förfrågningar till URL:en `/delete-error`.
     * När användaren besöker denna URL loggas ett meddelande som säger "Error during user deletion,"
     * vilket indikerar att det har uppstått ett fel under användartagning.
     * Metoden returnerar namnet på vyn som ska användas, vilket är "delete_error".
     * Detta innebär att användaren kommer att se sidan `delete_error.html` eller motsvarande vy,
     * som informerar om att det inträffade ett problem vid försök att radera en användare.
     */
    @GetMapping("/delete-error")
    public String deleteError() {
        logger.debug("Error during user deletion.");
        return "delete_error";
    }
}
