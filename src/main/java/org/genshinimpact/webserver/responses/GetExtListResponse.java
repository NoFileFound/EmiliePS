package org.genshinimpact.webserver.responses;

// Imports
import java.util.List;

public class GetExtListResponse {
    public Integer code;
    public String msg;
    public List<String> ext_list;
    public List<String> pkg_list;
    public String pkg_str;

    public GetExtListResponse(Integer code, String msg, List<String> ext_list, List<String> pkg_list, String pkg_str) {
        this.code = code;
        this.msg = msg;
        this.ext_list = ext_list;
        this.pkg_list = pkg_list;
        this.pkg_str = pkg_str;
    }

    public GetExtListResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.ext_list = List.of();
        this.pkg_list = List.of();
        this.pkg_str = "";
    }
}