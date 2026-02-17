package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.enums.Retcode;

public class ABTestExperimentsListResponse<T> {
    public Retcode retcode;
    public Boolean success;
    public String message;
    public T data;

    public ABTestExperimentsListResponse(Retcode retcode, String message) {
        this.retcode = retcode;
        this.success = false;
        this.message = message;
        this.data = null;
    }

    public ABTestExperimentsListResponse(Retcode retcode, String message, T data) {
        this.retcode = retcode;
        this.success = true;
        this.message = message;
        this.data = data;
    }
}