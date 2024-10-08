package itstep.learning.rest;

public class RestResponse {
    private RestResponseStatus status;
    private Object data;
    private RestMetaData meta;

    public RestResponseStatus getStatus() {
        return status;
    }

    public RestResponse setStatus( RestResponseStatus status ) {
        this.status = status;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object data) {
        this.data = data;
        return this;
    }

    public RestMetaData getMeta() {
        return meta;
    }

    public void setMeta(RestMetaData meta) {
        this.meta = meta;
    }
}

