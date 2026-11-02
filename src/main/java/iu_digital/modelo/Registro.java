package iu_digital.modelo;

import java.time.LocalDateTime;
import iu_digital.modelo.Accion;

public record Registro(String idUsuario,
                       String ruta,
                       String estacion,
                       Accion accion,
                       LocalDateTime timestamp) {


    public boolean esEntrada() {
        return Accion = Accion.ENTRADA;

    }

    public boolean esSalida() {
        return Accion = Accion.SALIDA;
    }
    public int Hora(){
        return timestamp.getHour();
    }
}

