package juno.http.convert;

import juno.http.HttpResponse;

public interface ResponseBodyConverter<T> {

    /**
     * Convierte el body de {@code response} a {@code T}. La implementación NO
     * es responsable de cerrar {@code response} — quien invoca {@code convert}
     * (p.ej. {@code HttpClient}/{@code HttpTask}) lo cierra siempre en un
     * {@code finally}, ocurra éxito o excepción.
     */
    public T convert(HttpResponse response) throws Exception;
}
