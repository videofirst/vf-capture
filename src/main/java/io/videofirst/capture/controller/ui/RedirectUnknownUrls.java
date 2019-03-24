package io.videofirst.capture.controller.ui;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirect ULRs to homepage - see https://stackoverflow.com/a/41989976/1692179
 *
 * @author Bob Marks
 */
//@Controller
public class RedirectUnknownUrls implements ErrorController {

    // Issues where React loses it's routes - see https://stackoverflow.com/a/41249464/1692179
    @GetMapping("/error")
    public void redirectNonExistentUrlsToHome(HttpServletResponse response) throws IOException {
        response.sendRedirect("/"); // not sure if this is right
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}