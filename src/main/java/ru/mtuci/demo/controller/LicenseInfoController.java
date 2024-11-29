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
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.model.LicenseInfoRequest;
import ru.mtuci.demo.service.impl.DeviceServiceImpl;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailsServiceImpl;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseInfoController {

    private final DeviceServiceImpl deviceService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final LicenseServiceImpl licenseService;

    @PostMapping("/info")
    public ResponseEntity<?> infoLicense(@RequestBody LicenseInfoRequest request, HttpServletRequest req) {
        try {
            String mac = request.getMac_address();
            String name = request.getName();
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();
            Optional<ApplicationDevice> device = deviceService.findDeviceByInfo(user, mac, name);
            if (!device.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The device was not found.");
            }

            List<ApplicationTicket> tickets = licenseService.getActiveLicensesForDevice(device.get());

            return ResponseEntity.status(HttpStatus.CREATED).body(tickets);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
