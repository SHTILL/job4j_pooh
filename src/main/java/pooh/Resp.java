package pooh;

import net.jcip.annotations.Immutable;

@Immutable
public class Resp {
    private final String text;
    private final int status;

    public Resp(String text, int status) {
        this.text = text;
        this.status = status;
    }

    public String text() {
        return text;
    }

    public int status() {
        return status;
    }

    static public Resp responseBadRequest() {
        return new Resp("", 400);
    }

    static public Resp queuedResponse() {
        return new Resp("Queued" + System.lineSeparator(), 200);
    }

    static public Resp emptyResponse() {
        return new Resp("", 200);
    }
}
