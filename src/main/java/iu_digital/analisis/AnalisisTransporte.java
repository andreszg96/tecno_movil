package iu_digital.analisis;

import iu_digital.modelo.FranjaHoraria;
import iu_digital.modelo.Registro;
import iu_digital.modelo.RutaCritica;
import iu_digital.modelo.Tramo;
import iu_digital.modelo.UsoRuta;


import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;



public class AnalisisTransporte {

    public static record TramoRecorrido(Tramo tramo, long minutos) {
    }

    public static Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::estacion,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public static Map<Integer, Long> flujoPorHora(List<Registro> registros) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::hora,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public static List<FranjaHoraria> horasPico(List<Registro> registros, int cuantas) {
        return flujoPorHora(registros).entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(cuantas)
                .map(e -> new FranjaHoraria(e.getKey(), e.getValue()))
                .toList();
    }

    public static List<UsoRuta> rutasMasUtilizadas(List<Registro> registros, int cuantas) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::ruta,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(cuantas)
                .map(e -> new UsoRuta(e.getKey(), e.getValue()))
                .toList();
    }

    public static Map<String, List<String>> patronesDeViaje(List<Registro> registros) {
        return registros.stream()
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(
                        Registro::idUsuario,
                        Collectors.mapping(Registro::estacion, Collectors.toList())
                ));
    }

    public static Map<Tramo, Duration> tiempoPromedioEntreEstaciones(List<Registro> registros) {
        Map<Tramo, Double> promediosEnMinutos = registros.stream()
                .collect(Collectors.groupingBy(Registro::idUsuario))
                .values().stream()
                .flatMap(AnalisisTransporte::tramosDeUnUsuario)
                .collect(Collectors.groupingBy(
                        TramoRecorrido::tramo,
                        Collectors.averagingLong(TramoRecorrido::minutos)
                ));

        return aDuraciones(promediosEnMinutos);
    }

    public static List<RutaCritica> rutasCriticas(List<Registro> registros, long umbral) {
        Map<Tramo, Long> conteos = registros.stream()
                .collect(Collectors.groupingBy(Registro::idUsuario))
                .values().stream()
                .flatMap(AnalisisTransporte::tramosDeUnUsuario)
                .map(TramoRecorrido::tramo)
                .collect(Collectors.groupingBy(
                        t -> t,
                        Collectors.counting()
                ));

        return conteos.entrySet().stream()
                .filter(e -> e.getValue() > umbral)
                .map(e -> new RutaCritica(e.getKey().origen(), e.getValue(), umbral))
                .toList();
    }

    private static Stream<TramoRecorrido> tramosDeUnUsuario(List<Registro> registrosDelUsuario) {
        List<Registro> ordenados = registrosDelUsuario.stream()
                .sorted(Comparator.comparing(Registro::timestamp))
                .toList();

        return IntStream.range(0, Math.max(0, ordenados.size() - 1))
                .filter(i -> ordenados.get(i).esEntrada()
                        && ordenados.get(i + 1).esSalida()
                        && ordenados.get(i).ruta().equals(ordenados.get(i + 1).ruta()))
                .mapToObj(i -> {
                    Registro origen = ordenados.get(i);
                    Registro destino = ordenados.get(i + 1);
                    Tramo tramo = new Tramo(origen.estacion(), destino.estacion());
                    long minutos = Duration.between(origen.timestamp(), destino.timestamp()).toMinutes();
                    return new TramoRecorrido(tramo, minutos);
                });
    }

    private static Map<Tramo, Duration> aDuraciones(Map<Tramo, Double> promediosEnMinutos) {
        return promediosEnMinutos.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Duration.ofSeconds(Math.round(e.getValue() * 60))
                ));
    }
}