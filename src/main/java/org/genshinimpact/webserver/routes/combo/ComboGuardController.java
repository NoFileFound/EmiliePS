package org.genshinimpact.webserver.routes.combo;

// Imports
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"hk4e_global/combo/guard", "hk4e_cn/combo/guard", "combo/guard"}, produces = "application/json")
public class ComboGuardController {

}