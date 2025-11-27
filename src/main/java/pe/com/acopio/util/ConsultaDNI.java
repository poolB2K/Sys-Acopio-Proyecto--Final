package pe.com.acopio.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para consultar DNI/RUC usando APIs públicas
 * Nota: Para producción, usar una API de pago más confiable
 */
public class ConsultaDNI {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaDNI.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Consulta datos de una persona por DNI
     * @param dni El número de DNI a consultar
     * @return Map con los datos (nombres, apellidoPaterno, apellidoMaterno) o null si falla
     */
    public static Map<String, String> consultarDNI(String dni) {
        if (dni == null || dni.length() != 8) {
            return null;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Usar API pública (cambiar por una API de producción)
            String url = "https://dniruc.apisperu.com/api/v1/dni/" + dni + "?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";

            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode root = objectMapper.readTree(jsonResponse);

                if (root.has("nombres")) {
                    Map<String, String> datos = new HashMap<>();
                    datos.put("nombres", root.get("nombres").asText());
                    datos.put("apellidoPaterno", root.get("apellidoPaterno").asText());
                    datos.put("apellidoMaterno", root.get("apellidoMaterno").asText());

                    return datos;
                }
            }
        } catch (Exception e) {
            logger.error("Error al consultar DNI: " + dni, e);
        }

        return null;
    }

    /**
     * Consulta datos de una empresa por RUC
     * @param ruc El número de RUC a consultar
     * @return Map con los datos (razonSocial, direccion, etc.) o null si falla
     */
    public static Map<String, String> consultarRUC(String ruc) {
        if (ruc == null || ruc.length() != 11) {
            return null;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = "https://dniruc.apisperu.com/api/v1/ruc/" + ruc + "?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9";

            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonNode root = objectMapper.readTree(jsonResponse);

                if (root.has("razonSocial")) {
                    Map<String, String> datos = new HashMap<>();
                    datos.put("razonSocial", root.get("razonSocial").asText());
                    datos.put("direccion", root.has("direccion") ? root.get("direccion").asText() : "");
                    datos.put("estado", root.has("estado") ? root.get("estado").asText() : "");

                    return datos;
                }
            }
        } catch (Exception e) {
            logger.error("Error al consultar RUC: " + ruc, e);
        }

        return null;
    }
}