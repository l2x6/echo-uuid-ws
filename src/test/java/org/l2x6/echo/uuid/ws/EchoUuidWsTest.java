package org.l2x6.echo.uuid.ws;


import java.io.IOException;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class EchoUuidWsTest {

    @Test
    void getUuid() {
        Assertions.assertThat(EchoUuidWsRoutes.getUuid("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:echoUuid xmlns:ns2=\"http://l2x6.org/echo-uuid-ws/\"><uuid>85f02a6a-063c-4d09-97ea-ff2d514fd7f7</uuid></ns2:echoUuid></soap:Body></soap:Envelope>"))
        .isEqualTo("85f02a6a-063c-4d09-97ea-ff2d514fd7f7");
    }

    @Test
    void echoSoap11Vtx() throws IOException {

        /* Ensure the WSDL is served */
        RestAssured.get("http://localhost:8081/echo-uuid-ws/soap-1.1?wsdl")
                .then()
                .statusCode(200)
                .body(
                        Matchers.containsString("""
                                <xs:element minOccurs="0" name="uuid" type="xs:string"/>
                                """),
                        Matchers.containsString("""
                                <wsdl:operation name="echoUuid">
                                """));

        RestAssured.get("http://localhost:8081/echo-uuid-ws/soap-1.1")
                .then()
                .statusCode(404);

        final String uuid = UUID.randomUUID().toString();
        /* Ensure the service works */
        RestAssured.given()
                .contentType("text/xml")
                .accept("*/*")
                .header("Connection", "Keep-Alive")
                .body(
                        String.format(
                                """
                                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><ns2:echoUuid xmlns:ns2="http://l2x6.org/echo-uuid-ws/"><uuid>%s</uuid></ns2:echoUuid></soap:Body></soap:Envelope>
                                        """,
                                uuid))
                .post("http://localhost:8081/echo-uuid-ws/soap-1.1")
                .then()
                .statusCode(200)
                .contentType("text/xml")
                .body(
                        Matchers.is(String.format(
                                """
                                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><ns2:echoUuidResponse xmlns:ns2="http://l2x6.org/echo-uuid-ws/"><return>%s</return></ns2:echoUuidResponse></soap:Body></soap:Envelope>
                                        """,
                                uuid).trim()));

    }

}
