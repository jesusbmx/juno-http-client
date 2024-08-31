package juno.http.convert;

/**
 * ConvertFactory es una clase abstracta que gestiona la creación y almacenamiento
 * en caché de convertidores para cuerpos de solicitudes y respuestas HTTP.
 * Proporciona métodos para convertir objetos a diferentes formatos de cuerpo.
 */
public interface ConverterFactory {

    /**
     * Método abstracto para crear un convertidor de cuerpo de solicitud cuando no está en caché.
     * Debe ser implementado por las subclases.
     * 
     * @param <V> El tipo del objeto.
     * @param type La clase del objeto.
     * @return El convertidor creado.
     */
    <V> RequestBodyConverter<V> requestBodyConverter(Class<V> type);

    /**
     * Método abstracto para crear un convertidor de cuerpo de respuesta cuando no está en caché.
     * Debe ser implementado por las subclases.
     * 
     * @param <V> El tipo del objeto.
     * @param type La clase del objeto.
     * @return El convertidor creado.
     */
    <V> ResponseBodyConverter<V> responseBodyConverter(Class<V> type);

}
