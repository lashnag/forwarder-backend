package ru.lashnev.forwarderbackend.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.lashnev.forwarderbackend.dto.Mail
import ru.lashnev.forwarderbackend.services.MailingService
import ru.lashnev.forwarderbackend.utils.withMDC

@RestController
@RequestMapping("/mail")
class MailingController(
    private val mailingService: MailingService,
) {
    @PostMapping
    fun sendAll(
        @RequestBody mail: Mail,
    ) {
        withMDC { mailingService.send(mail) }
    }
}
