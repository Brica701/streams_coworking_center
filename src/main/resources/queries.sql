-- 1. Devuelve un listado de todas las reservas realizadas durante el año 2025, cuya sala tenga un precio_hora superior a 25€.
SELECT r.reserva_id, r.fecha, m.nombre AS miembro, s.nombre AS sala
FROM reserva r
         JOIN miembro m USING (miembro_id)
         JOIN sala s USING (sala_id)
WHERE YEAR(r.fecha) = 2025
  AND s.precio_hora > 25
ORDER BY r.fecha, r.hora_inicio;

-- 2. Devuelve un listado de todos los miembros que NO han realizado ninguna reserva.
SELECT m.miembro_id, m.nombre, m.email FROM miembro m WHERE m.miembro_id NOT IN (SELECT DISTINCT miembro_id FROM reserva);

-- 3. Devuelve una lista de los id's, nombres y emails de los miembros que no tienen el teléfono registrado.
-- El listado tiene que estar ordenado inverso alfabéticamente por nombre (z..a).
SELECT miembro_id, nombre, email FROM miembro WHERE telefono IS NULL ORDER BY nombre DESC;

-- 4. Devuelve un listado con los id's y emails de los miembros que se hayan registrado con una cuenta de yahoo.es
-- en el año 2024.
SELECT miembro_id, email FROM miembro WHERE email LIKE '%@yahoo.es' AND YEAR(fecha_alta) = 2024;

-- 5. Devuelve un listado de los miembros cuyo primer apellido es Martín. El listado tiene que estar ordenado
-- por fecha de alta en el coworking de más reciente a menos reciente y nombre y apellidos en orden alfabético.SELECT miembro_id, nombre, email, fecha_alta
SELECT miembro_id, nombre, email, fecha_alta FROM miembro WHERE nombre LIKE '%Martín%' ORDER BY fecha_alta DESC, nombre ASC;

-- 6. Devuelve el gasto total (estimado) que ha realizado la miembro Ana Beltrán en reservas del coworking.
SELECT SUM(r.horas * s.precio_hora * COALESCE(1 - r.descuento_pct/100, 1)) AS gasto_total
FROM reserva r
         JOIN miembro m ON r.miembro_id = m.miembro_id
         JOIN sala s ON r.sala_id = s.sala_id
WHERE m.nombre = 'Ana Beltrán';

-- 7. Devuelve el listado de las 3 salas de menor precio_hora.
SELECT sala_id, nombre, precio_hora FROM sala ORDER BY precio_hora ASC LIMIT 3;

-- 8. Devuelve la reserva a la que se le ha aplicado la mayor cuantía de descuento sobre el precio sin descuento
-- (precio_hora × horas).
SELECT r.reserva_id, m.nombre AS miembro, s.nombre AS sala,
       r.horas * s.precio_hora * COALESCE(r.descuento_pct/100, 0) AS descuento_valor
FROM reserva r
         JOIN miembro m ON r.miembro_id = m.miembro_id
         JOIN sala s ON r.sala_id = s.sala_id
ORDER BY descuento_valor DESC
LIMIT 1;

-- 9. Devuelve los miembros que hayan tenido alguna reserva con estado 'ASISTIDA' y exactamente 10 asistentes.
SELECT DISTINCT m.miembro_id, m.nombre, m.email
FROM miembro m
         JOIN reserva r ON m.miembro_id = r.miembro_id
WHERE r.estado = 'ASISTIDA' AND r.asistentes = 10;

-- 10. Devuelve el valor mínimo de horas reservadas (campo calculado 'horas') en una reserva.
 SELECT MIN(horas) AS horas_min FROM reserva;

-- 11. Devuelve un listado de las salas que empiecen por 'Sala' y terminen por 'o',
-- y también las salas que terminen por 'x'.
SELECT * FROM sala WHERE (nombre LIKE 'Sala%o') OR (nombre LIKE '%x');

-- 12. Devuelve un listado que muestre todas las reservas y salas en las que se ha registrado cada miembro.
-- El resultado debe mostrar todos los datos del miembro primero junto con un sublistado de sus reservas y salas.
-- El listado debe mostrar los datos de los miembros ordenados alfabéticamente por nombre.
SELECT m.miembro_id, m.nombre, m.email, r.reserva_id, r.fecha, r.hora_inicio, r.hora_fin, s.nombre AS sala
FROM miembro m
         LEFT JOIN reserva r ON m.miembro_id = r.miembro_id
         LEFT JOIN sala s ON r.sala_id = s.sala_id
ORDER BY m.nombre ASC;

-- 13. Devuelve el total de personas que podrían alojarse simultáneamente en el centro en base al aforo de todas las salas.
    SELECT SUM(aforo)FROM sala;
-- 14. Calcula el número total de miembros (diferentes) que tienen alguna reserva.
    SELECT COUNT(DISTINCT miembro_id) FROM reserva;
-- 15. Devuelve el listado de las salas para las que se aplica un descuento porcentual (descuento_pct) superior al 10%
-- en alguna de sus reservas.
SELECT DISTINCT s.sala_id, s.nombre FROM sala s JOIN reserva r ON s.sala_id = r.sala_id WHERE r.descuento_pct > 10;

-- 16. Devuelve el nombre del miembro que pagó la reserva de mayor cuantía (precio_hora × horas aplicando el descuento).
SELECT m.nombre, r.reserva_id, s.nombre AS sala,
       r.horas * s.precio_hora * COALESCE(1 - r.descuento_pct/100, 1) AS total_pagado
FROM reserva r
         JOIN miembro m ON r.miembro_id = m.miembro_id
         JOIN sala s ON r.sala_id = s.sala_id
ORDER BY total_pagado DESC
LIMIT 1;

-- 17. Devuelve los nombres de los miembros que hayan coincidido en alguna reserva con la miembro Ana Beltrán
-- (misma sala y fecha con solape horario).
SELECT DISTINCT m2.nombre
FROM reserva r1
         JOIN miembro m1 ON r1.miembro_id = m1.miembro_id
         JOIN reserva r2 ON r1.sala_id = r2.sala_id AND r1.fecha = r2.fecha
         JOIN miembro m2 ON r2.miembro_id = m2.miembro_id
WHERE m1.nombre = 'Ana Beltrán'
  AND m2.miembro_id <> m1.miembro_id
  AND r1.hora_inicio < r2.hora_fin
  AND r1.hora_fin > r2.hora_inicio;

-- 18. Devuelve el total de lo ingresado por el coworking en reservas para el mes de enero de 2025.
SELECT SUM(r.horas * s.precio_hora * COALESCE(1 - r.descuento_pct/100, 1)) AS total_enero_2025
FROM reserva r
         JOIN sala s ON r.sala_id = s.sala_id
WHERE r.fecha BETWEEN '2025-01-01' AND '2025-01-31';

-- 19. Devuelve el conteo de cuántos miembros tienen la observación 'Requiere equipamiento especial' en alguna de sus reservas.
SELECT COUNT(DISTINCT m.miembro_id) AS total_miembros FROM miembro m JOIN reserva r ON m.miembro_id = r.miembro_id WHERE r.observaciones = 'Requiere equipamiento especial';

-- 20. Devuelve cuánto se ingresaría por la sala 'Auditorio Sol' si estuviera reservada durante todo su horario de apertura
-- en un día completo (sin descuentos).
SELECT (TIME_TO_SEC(cierre) - TIME_TO_SEC(apertura)) / 3600 * precio_hora AS ingreso_teorico FROM sala WHERE nombre = 'Auditorio Sol';
