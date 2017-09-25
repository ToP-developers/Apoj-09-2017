package application.controllers;

import application.models.User;
import application.services.AccountService;
import application.utils.Validator;
import application.utils.requests.SettingsRequest;
import application.utils.requests.SigninRequest;
import application.utils.requests.SignupRequest;
import application.utils.responses.MessageResponse;
import application.utils.responses.UserResponseWP;
import application.utils.Messages;
import application.utils.responses.ValidatorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@RestController
@CrossOrigin //(origins = {"https://gametes.herokuapp.com", "localhost:8080"})
public class SessionController {
    private AccountService service;

    public SessionController(AccountService service) {
        this.service = service;
    }

    @PostMapping(path = "/signup", consumes = Messages.JSON, produces = Messages.JSON)
    public ResponseEntity signup(@RequestBody SignupRequest body, HttpSession httpSession) {
        final ArrayList<String> error = Validator.checkSignup(body);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidatorResponse(error));
        }
        if (httpSession.getAttribute(Messages.USER_ID) != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(Messages.AUTHORIZED));
        }
        if (!service.checkSignup(body.getLogin(), body.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.EXSISTS));
        }
        final Long id = service.addUser(body);
        httpSession.setAttribute(Messages.USER_ID, id);
        final User newUser = new User(id, body);
        return ResponseEntity.ok(new UserResponseWP(newUser));
    }

    @PostMapping(path = "/signin", consumes = Messages.JSON, produces = Messages.JSON)
    public ResponseEntity greetingSubmit(@RequestBody SigninRequest body, HttpSession httpSession) {
        final ArrayList<String> error = Validator.checkSignin(body);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidatorResponse(error));
        }
        if (httpSession.getAttribute(Messages.USER_ID) != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(Messages.AUTHORIZED));
        }
        final Long id = service.getId(body.getLogin());
        if (id == null || !service.checkSignin(id, body.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.WRONG_LOGIN_PASSWORD));
        }
        httpSession.setAttribute(Messages.USER_ID, id);
        return ResponseEntity.ok(new UserResponseWP(service.getUser(id)));
    }

    @PostMapping(path = "/newpswrd", consumes = Messages.JSON, produces = Messages.JSON)
    public ResponseEntity setPassword(@RequestBody SettingsRequest body, HttpSession httpSession) {
        if (httpSession.getAttribute(Messages.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Messages.NOT_AUTHORIZE));
        }
        final Long id = (Long) httpSession.getAttribute(Messages.USER_ID);
        if (!service.checkSignin(id, body.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.WRONG_PASSWORD));
        }
        final ArrayList<String> error = Validator.checkPassword(body.getPassword());
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidatorResponse(error));
        }
        service.changePassword(id, body.getFieldToChange());

        return ResponseEntity.ok(new UserResponseWP(service.getUser(id)));
    }

    @PostMapping(path = "/newlogin", consumes = Messages.JSON, produces = Messages.JSON)
    public ResponseEntity setLogin(@RequestBody SettingsRequest body, HttpSession httpSession) {
        if (httpSession.getAttribute(Messages.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Messages.NOT_AUTHORIZE));
        }
        final Long id = (Long) httpSession.getAttribute(Messages.USER_ID);
        if (!service.checkSignin(id, body.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.WRONG_PASSWORD));
        }
        if (!service.checkLogin(body.getFieldToChange())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.LOGIN_EXSISTS));
        }
        service.changeLogin(id, body.getFieldToChange());

        return ResponseEntity.ok(new UserResponseWP(service.getUser(id)));
    }

    @PostMapping(path = "/newemail", consumes = Messages.JSON, produces = Messages.JSON)
    public ResponseEntity setEmail(@RequestBody SettingsRequest body, HttpSession httpSession) {
        if (httpSession.getAttribute(Messages.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Messages.NOT_AUTHORIZE));
        }
        final Long id = (Long) httpSession.getAttribute(Messages.USER_ID);
        if (!service.checkSignin(id, body.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.WRONG_PASSWORD));
        }
        if (!service.checkEmail(body.getFieldToChange())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Messages.EMAIL_EXSISTS));
        }
        service.changeEmail(id, body.getFieldToChange());

        return ResponseEntity.ok(new UserResponseWP(service.getUser(id)));
    }

    @GetMapping(path = "/logout", produces = Messages.JSON)
    public ResponseEntity logout(HttpSession httpSession) {
        if (httpSession.getAttribute(Messages.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Messages.NOT_AUTHORIZE));
        }
        httpSession.removeAttribute(Messages.USER_ID);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Messages.SUCCESS));
    }

    @GetMapping(path = "/user", produces = Messages.JSON)
    public ResponseEntity user(HttpSession httpSession) {
        if (httpSession.getAttribute(Messages.USER_ID) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(Messages.NOT_AUTHORIZE));
        }
        final Long id = (Long) httpSession.getAttribute(Messages.USER_ID);
        return ResponseEntity.ok(new UserResponseWP(service.getUser(id)));
    }
}



