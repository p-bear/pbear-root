package com.pbear.oauth.core;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
public class LoginController {
  private final RedirectService redirectService;

  @GetMapping("/login")
  public RedirectView getLogin(final HttpServletRequest request) {
    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(this.redirectService.createLoginPageRedirectUrl(request.getRequestURL().toString()));
    return redirectView;
  }
}
