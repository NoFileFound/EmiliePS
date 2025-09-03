package org.emilieps.webroutes.common;

// Imports
import org.emilieps.data.webserver.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"common/hk4e_global/announcement/api", "common/hk4e_cn/announcement/api", "common/announcement/api"}, produces = "application/json")
public final class Announcement implements Response {
    /// TODO: Implement
}