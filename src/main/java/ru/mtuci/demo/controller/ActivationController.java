package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ActivationRequest;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.service.impl.DeviceServiceImpl;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailsServiceImpl;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class ActivationController {

    private final DeviceServiceImpl deviceService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final LicenseServiceImpl licenseService;

    @PostMapping("/activate")
    public ResponseEntity<?> createLicense(@RequestBody ActivationRequest request, HttpServletRequest req) {
        try {
            String mac = request.getMac_address();
            String name = request.getName();
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();
            ApplicationDevice device = deviceService.registerOrUpdateDevice(mac, name, user);

            ApplicationTicket applicationTicket = licenseService.activateLicense(request.getActivationCode(), device, user);

            if (!applicationTicket.getInfo().equals("The license has been successfully activated")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(applicationTicket.getInfo());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(applicationTicket);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
