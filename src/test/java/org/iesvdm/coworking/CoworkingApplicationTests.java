package org.iesvdm.coworking;

import lombok.RequiredArgsConstructor;
import org.iesvdm.coworking.modelo.Miembro;
import org.iesvdm.coworking.modelo.Reserva;
import org.iesvdm.coworking.modelo.Sala;
import org.iesvdm.coworking.repositorio.MiembroRepository;
import org.iesvdm.coworking.repositorio.ReservaRepository;
import org.iesvdm.coworking.repositorio.SalaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
class CoworkingApplicationTests {

    @Autowired
    MiembroRepository miembroRepository;

    @Autowired
    ReservaRepository reservaRepository;

    @Autowired
    SalaRepository salaRepository;

    @Test
    void testMiembros() {

        miembroRepository.findAll().forEach(System.out::println);

    }

    @Test
    void testReservas() {

        reservaRepository.findAll().forEach(System.out::println);

    }

    @Test
    void testSalas() {

        salaRepository.findAll().forEach(System.out::println);

    }

    //1. Devuelve un listado de todas las reservas realizadas durante el año 2025, cuya sala tenga un precio_hora superior a 25€.
    @Test
    void consulta1_reservas2025ConPrecioMayor25() {

        List<Reserva> resultado = salaRepository.findAll().stream()

                // Ponemos las salas con precio mayor a 25€
                .filter(s -> s.getPrecioHora().compareTo(new BigDecimal("25")) > 0)

                // Extraemos las reservas de cada sala
                .flatMap(sala -> sala.getReservas().stream())

                // RFiltramos por fecha y año las reservas de 2025
                .filter(r -> r.getFecha().getYear() == 2025)

                // Ordenar por fecha y hora
                .sorted(
                        Comparator.comparing(Reserva::getFecha)
                                .thenComparing(Reserva::getHoraInicio)
                )

                .toList();

        // Mostramos el resultado del stream
        resultado.forEach(System.out::println);
    }
    // 2. Devuelve un listado de todos los miembros que NO han realizado ninguna reserva.
    @Test
    void consulta2_miembrosSinReservas() {

        List<Miembro> miembrosSinReservas = miembroRepository.findAll().stream()

                // Miembros cuyo set de reservas está vacío
                .filter(m -> m.getReservas().isEmpty())

                .toList();

        miembrosSinReservas.forEach(System.out::println);
    }
    // 3. Devuelve una lista de los id's, nombres y emails de los miembros que no tienen el teléfono registrado.
    // El listado tiene que estar ordenado inverso alfabéticamente por nombre (z..a).

    @Test
    void miembrosSinTelefono() {
        var lista = miembroRepository.findAll().stream()
                // Filtramos los miembros sin número de teléfono
                .filter(m -> m.getTelefono() == null || m.getTelefono().isBlank())
                // Mapeamos una representación de id, nombre y email
                .map(m -> String.format("ID: %d, Nombre: %s, Email: %s",
                        m.getId(), m.getNombre(), m.getEmail()))
                // Ordenar alfabéticamente inverso los nombres de los miembros (Z..A)
                .sorted((a, b) -> b.compareTo(a))
                .toList();

        // Mostramos el resultado
        lista.forEach(System.out::println);
    }
    // 4. Devuelve un listado con los id's y emails de los miembros que se hayan registrado con una cuenta de yahoo.es
    // en el año 2024.
    @Test
    void miembrosYahoo2024() {
        var lista = miembroRepository.findAll().stream()
                // Filtramos los emails que terminen en "@yahoo.es"
                .filter(m -> m.getEmail().toLowerCase().endsWith("@yahoo.es"))
                // Filtramos los miembros que se han dado de alta en 2024
                .filter(m -> m.getFechaAlta().getYear() == 2024)
                // Mapeamos una representación de id y email
                .map(m -> String.format("ID: %d, Email: %s", m.getId(), m.getEmail()))
                .toList();

        // Mostramos los resultados
        lista.forEach(System.out::println);
    }

    // 5. Devuelve un listado de los miembros cuyo primer apellido es Martín. El listado tiene que estar ordenado
    // por fecha de alta en el coworking de más reciente a menos reciente y nombre y apellidos en orden alfabético.
    @Test
    void consulta5_miembrosApellidoMartinOrdenados() {

        List<Miembro> miembrosMartín = miembroRepository.findAll().stream()
                // Filtramos para buscar a las personas con el apellido Martín
                .filter(m -> m.getNombre().contains("Martín"))
                // Ordenamos por fechaAlta descendente y luego por nombre ascendente
                .sorted((m1, m2) -> {
                    int cmpFecha = m2.getFechaAlta().compareTo(m1.getFechaAlta()); // Descendente
                    if (cmpFecha != 0) return cmpFecha;
                    return m1.getNombre().compareTo(m2.getNombre()); // Ascendente
                })
                .toList();

        miembrosMartín.forEach(System.out::println);
    }

    // 6. Devuelve el gasto total (estimado) que ha realizado la miembro Ana Beltrán en reservas del coworking.
    @Test
    void consulta6_gastoTotalAnaBeltran() {

        // Obtenemos el gasto total recorriendo todos los miembros
        BigDecimal gastoTotal = miembroRepository.findAll().stream()  // Creamos un Stream con todos los miembros
                // Filtramos solo a la miembro llamada Ana Beltrán
                .filter(m -> "Ana".equals(m.getNombre()) || m.getNombre().contains("Ana Beltrán"))

                // Transformamos cada miembro en sus reservas
                .flatMap(m -> m.getReservas().stream())

                // Calculamos el coste de cada reserva
                .map(r -> {
                    BigDecimal precioHora = r.getSala().getPrecioHora();  // Obtenemos el precio por hora de la sala
                    BigDecimal horas = r.getHoras() != null ? r.getHoras() : BigDecimal.ZERO; // Si horas es null, usamos 0
                    return precioHora.multiply(horas);  // Calculamos el coste de esa reserva
                })

                // Sumamos todos los costes de reservas para obtener el gasto total
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mostramos el resultado
        System.out.println("Gasto total de Ana Beltrán: " + gastoTotal);
    }
    // 7. Devuelve el listado de las 3 salas de menor precio_hora.
    @Test
    void consulta7_tresSalasMenorPrecioHora() {

        List<Sala> tresMasBaratas = salaRepository.findAll().stream()  // Obtenemos todas las salas
                .sorted((s1, s2) -> s1.getPrecioHora().compareTo(s2.getPrecioHora()))  // Ordenamos por precioHora ascendente
                .limit(3)  // Tomamos solo las 3 primeras (las más baratas)
                .toList();  // Convertimos a lista

        tresMasBaratas.forEach(System.out::println);  // Mostramos el resultado
    }
    // 8. Devuelve la reserva a la que se le ha aplicado la mayor cuantía de descuento sobre el precio sin descuento
    // (precio_hora × horas).
    @Test
    void consulta8_reservaMayorDescuento() {

        Reserva reservaMayorDescuento = reservaRepository.findAll().stream()
                // Calculamos el importe original (precioHora * horas) y lo reducimos aplicando el descuento
                .max((r1, r2) -> {
                    BigDecimal descuento1 = r1.getDescuentoPct() != null ? r1.getDescuentoPct() : BigDecimal.ZERO;
                    BigDecimal total1 = r1.getSala().getPrecioHora().multiply(r1.getHoras() != null ? r1.getHoras() : BigDecimal.ZERO);
                    BigDecimal importeDescuento1 = total1.multiply(descuento1).divide(BigDecimal.valueOf(100));

                    BigDecimal descuento2 = r2.getDescuentoPct() != null ? r2.getDescuentoPct() : BigDecimal.ZERO;
                    BigDecimal total2 = r2.getSala().getPrecioHora().multiply(r2.getHoras() != null ? r2.getHoras() : BigDecimal.ZERO);
                    BigDecimal importeDescuento2 = total2.multiply(descuento2).divide(BigDecimal.valueOf(100));

                    return importeDescuento1.compareTo(importeDescuento2);
                })
                .orElse(null);  // PLo ponemos por si no hay reservas

        System.out.println("Reserva con mayor descuento: " + reservaMayorDescuento);
    }
    // 9. Devuelve los miembros que hayan tenido alguna reserva con estado 'ASISTIDA' y exactamente 10 asistentes.
    @Test
    void consulta9_miembrosConReservaAsistida10Asistentes() {

        List<Miembro> miembrosFiltrados = miembroRepository.findAll().stream()
                // Filtramos los miembros que tengan al menos una reserva con estado 'ASISTIDA' y 10 asistentes
                .filter(m -> m.getReservas().stream()
                        .anyMatch(r -> "ASISTIDA".equals(r.getEstado()) && r.getAsistentes() != null && r.getAsistentes() == 10))
                .toList();

        miembrosFiltrados.forEach(System.out::println);
    }

    // 10. Devuelve el valor mínimo de horas reservadas (campo calculado 'horas') en una reserva.
    @Test
    void consulta10_horasMinimasReserva() {

        // Obtenemos todas las reservas de todos los miembros
        BigDecimal horasMinimas = miembroRepository.findAll().stream()
                .flatMap(m -> m.getReservas().stream())           // todas las reservas
                .map(r -> r.getHoras() != null ? r.getHoras() : BigDecimal.ZERO) // evitamos null
                .min(BigDecimal::compareTo)                      // buscamos el mínimo
                .orElse(BigDecimal.ZERO);                        // por si no hay reservas

        System.out.println("Horas mínimas reservadas: " + horasMinimas);
    }
    // 11. Devuelve un listado de las salas que empiecen por 'Sala' y terminen por 'o',
    // y también las salas que terminen por 'x'.
    @Test
    void consulta11_salasSalaX() {
        List<Sala> salasSalaX = salaRepository.findAll().stream()
                // Filtramos las salas que empiecen por 'Sala' y terminen por 'o' o 'x'
                .filter(s -> (s.getNombre().startsWith("Sala") && s.getNombre().endsWith("o"))
                        || s.getNombre().endsWith("x"))
                .toList();

        salasSalaX.forEach(System.out::println);
    }

    // 12. Devuelve un listado que muestre todas las reservas y salas en las que se ha registrado cada miembro.
    // El resultado debe mostrar todos los datos del miembro primero junto con un sublistado de sus reservas y salas.
    // El listado debe mostrar los datos de los miembros ordenados alfabéticamente por nombre.
    @Test
    void consulta12_reservasYSalas() {
        // Obtenemos todos los miembros y los ordenamos por nombre alfabeticamente
        List<Miembro> miembrosOrdenados = miembroRepository.findAll().stream()
                .sorted((m1, m2) -> m1.getNombre().compareToIgnoreCase(m2.getNombre()))
                .toList();

        miembrosOrdenados.forEach(miembro -> {
            System.out.println("Miembro: " + miembro.getNombre() + ", Email: " + miembro.getEmail() +
                    ", Teléfono: " + miembro.getTelefono() + ", Empresa: " + miembro.getEmpresa() +
                    ", Plan: " + miembro.getPlan() + ", Fecha Alta: " + miembro.getFechaAlta());

            // Otro listado de reservas y sus salas con sus datos
            miembro.getReservas().forEach(reserva -> {
                System.out.println("  Reserva ID: " + reserva.getId() +
                        ", Fecha: " + reserva.getFecha() +
                        ", Sala: " + reserva.getSala().getNombre() +
                        ", Precio/Hora: " + reserva.getSala().getPrecioHora() +
                        ", Horas: " + reserva.getHoras());
            });
        });
    }

    // 13. Devuelve el total de personas que podrían alojarse simultáneamente en el centro en base al aforo de todas las salas.
    @Test
    void consulta13_aforoTotalCentro() {
        // Sumamos el aforo de todas las salas para ver el total
        int aforoTotal = salaRepository.findAll().stream()
                .map(Sala::getAforo)        // obtenemos el aforo de cada sala
                .filter(aforo -> aforo != null) // evitamos que haya algun null
                .mapToInt(Integer::intValue)    // convertimos a int para sumar
                .sum();

        System.out.println("Aforo total del centro: " + aforoTotal + " personas");
    }

    // 14. Calcula el número total de miembros (diferentes) que tienen alguna reserva.
    @Test
    void consulta14_miembrosConReserva() {
        // Filtramos los miembros que tengan al menos una reserva y las contamos
        long totalMiembrosConReserva = miembroRepository.findAll().stream()
                .filter(m -> !m.getReservas().isEmpty()) // miembros con reservas
                .count(); // las contamos para obtener el total

        System.out.println("Número total de miembros con al menos una reserva: " + totalMiembrosConReserva);
    }
    // 15. Devuelve el listado de las salas para las que se aplica un descuento porcentual (descuento_pct) superior al 10%
    // en alguna de sus reservas.
    @Test
    void consulta15_salasCondescuento() {
        List<Sala> salasConDescuento = salaRepository.findAll().stream()
                // Filtramos las salas que tengan alguna reserva con un descuento mayor al 10%
                .filter(sala -> sala.getReservas().stream()
                        .anyMatch(r -> r.getDescuentoPct() != null && r.getDescuentoPct().compareTo(new BigDecimal("10")) > 0))
                .toList();

        salasConDescuento.forEach(System.out::println);
    }

    // 16. Devuelve el nombre del miembro que pagó la reserva de mayor cuantía (precio_hora × horas aplicando el descuento).
    @Test
    void consulta16_miembroReservaMayorCuantia(){
        Optional<Reserva> reservaMayor = reservaRepository.findAll().stream()
                // Comparamos por el importe de cada reserva: horas * precioHora * (1 - descuentoPct/100)
                .max(Comparator.comparing(r -> {
                    BigDecimal descuento = r.getDescuentoPct() != null ? r.getDescuentoPct().divide(new BigDecimal("100")) : BigDecimal.ZERO;
                    return r.getHoras().multiply(r.getSala().getPrecioHora()).multiply(BigDecimal.ONE.subtract(descuento));
                }));

        reservaMayor.ifPresent(r -> System.out.println(
                "Miembro: " + r.getMiembro().getNombre() +
                        " | Reserva ID: " + r.getId() +
                        " | Importe: " + r.getHoras().multiply(r.getSala().getPrecioHora())
                        .multiply(BigDecimal.ONE.subtract(
                                r.getDescuentoPct() != null ? r.getDescuentoPct().divide(new BigDecimal("100")) : BigDecimal.ZERO
                        ))
        ));
    }
    // 17. Devuelve los nombres de los miembros que hayan coincidido en alguna reserva con la miembro Ana Beltrán
    // (misma sala y fecha con solape horario).
    @Test
    void consulta17_miembrosCoincidentesConAnaBeltran() {

        // Obtenemos todos los miembros de la base de datos
        List<Miembro> todos = miembroRepository.findAll();

        // Buscamos a Ana Beltrán por su nombre (ignora mayúsculas/minúsculas)
        Optional<Miembro> ana = todos.stream()
                .filter(m -> m.getNombre().equalsIgnoreCase("Ana Beltrán"))
                .findFirst();

        // Si no se encuentra a Ana, salimos del test
        if (ana.isEmpty()) {
            System.out.println("No se encontró a Ana Beltrán");
            return;
        }

        // Convertimos el set de reservas de Ana a una lista para poder iterar con streams
        List<Reserva> reservasAna = new ArrayList<>(ana.get().getReservas());

        // Filtramos los miembros que hayan coincidido con Ana en alguna reserva
        List<String> miembrosCoincidentes = todos.stream()
                // Excluimos a Ana
                .filter(m -> !m.equals(ana.get()))
                // Comprobamos si alguna de sus reservas coincide con alguna reserva de Ana
                .filter(m -> m.getReservas().stream()
                        .anyMatch(r -> reservasAna.stream()
                                .anyMatch(ra -> ra.getSala().equals(r.getSala())
                                        && ra.getFecha().equals(r.getFecha())
                                        // Comprobamos si hay solape en el horario
                                        && ra.getHoraInicio().isBefore(r.getHoraFin())
                                        && ra.getHoraFin().isAfter(r.getHoraInicio())
                                )
                        )
                )
                // Obtenemos solo los nombres de los miembros coincidentes
                .map(Miembro::getNombre)
                // Evitamos nombres repetidos si coinciden varias veces
                .distinct()
                .toList();

        // Mostramos los resultados por consola
        miembrosCoincidentes.forEach(System.out::println);
    }

    // 18. Devuelve el total de lo ingresado por el coworking en reservas para el mes de enero de 2025.
    @Test
    void consulta18_totalIngresosEnero2025() {

        BigDecimal total = reservaRepository.findAll().stream()
                // Filtramos reservas de enero de 2025
                .filter(r -> r.getFecha() != null &&
                        r.getFecha().getYear() == 2025 &&
                        r.getFecha().getMonthValue() == 1)
                // Calculamos el importe de cada reserva
                .map(r -> {
                    BigDecimal precioHora = r.getSala().getPrecioHora();
                    BigDecimal horas = r.getHoras() != null ? r.getHoras() : BigDecimal.ZERO;
                    BigDecimal descuento = r.getDescuentoPct() != null ? r.getDescuentoPct() : BigDecimal.ZERO;
                    BigDecimal factor = BigDecimal.ONE.subtract(descuento.divide(new BigDecimal("100")));
                    return precioHora.multiply(horas).multiply(factor);
                })
                // Sumamos todos los importes
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("Total ingresado en enero 2025: " + total + " €");
    }

    // 19. Devuelve el conteo de cuántos miembros tienen la observación 'Requiere equipamiento especial' en alguna de sus reservas.
    @Test
    void consulta19_miembrosConObservacion() {

        long count = miembroRepository.findAll().stream()
                // Filtramos miembros que tengan al menos una reserva con la observación deseada
                .filter(m -> m.getReservas().stream()
                        .anyMatch(r -> r.getObservaciones() != null &&
                                r.getObservaciones().equalsIgnoreCase("Requiere equipamiento especial"))
                )
                .count();

        System.out.println("Número de miembros con observación 'Requiere equipamiento especial': " + count);
    }

    // 20. Devuelve cuánto se ingresaría por la sala 'Auditorio Sol' si estuviera reservada durante todo su horario de apertura
    // en un día completo (sin descuentos).
    @Test
    void consulta20_ingresoAuditorioSolDiaCompleto() {

        Optional<Sala> auditorio = salaRepository.findAll().stream()
                .filter(s -> s.getNombre().equalsIgnoreCase("Auditorio Sol"))
                .findFirst();

        if (auditorio.isPresent()) {
            Sala sala = auditorio.get();

            // Calculamos horas totales del horario de apertura
            long segundos = java.time.Duration.between(sala.getApertura(), sala.getCierre()).getSeconds();
            BigDecimal horas = new BigDecimal(segundos).divide(new BigDecimal("3600"), 2, BigDecimal.ROUND_HALF_UP);

            // Multiplicamos por precio por hora, sin descuentos
            BigDecimal ingreso = sala.getPrecioHora().multiply(horas);

            System.out.println("Ingreso teórico de 'Auditorio Sol' en un día completo: " + ingreso + " €");
        } else {
            System.out.println("No se encontró la sala 'Auditorio Sol'");
        }
    }


}
