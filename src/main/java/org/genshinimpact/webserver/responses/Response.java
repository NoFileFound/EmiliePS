package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.enums.Retcode;

public class Response<T> {
    public Retcode retcode;
    public String message;
    public T data;

    public Response(Retcode retcode, String message) {
        this.retcode = retcode;
        this.message = message;
    }

    public Response(Retcode retcode, String message, T data) {
        this.retcode = retcode;
        this.message = message;
        this.data = data;
    }
}