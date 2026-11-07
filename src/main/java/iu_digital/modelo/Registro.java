package iu_digital.modelo;

import java.time.LocalDateTime;

public record Registro(String idUsuario,
                       String ruta,
                       String estacion,
                       Accion accion,
                       LocalDateTime timestamp) {

    public boolean esEntrada() {
        return this.accion == Accion.ENTRADA;
    }

    public boolean esSalida() {
        return this.accion == Accion.SALIDA;
    }

    public int hora() {
        return this.timestamp.getHour();
    }
}