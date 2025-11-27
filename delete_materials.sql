-- Script para eliminar los materiales Plata y Cobre
-- Dejando solo Oro como material disponible

DELETE FROM materiales WHERE nombre = 'Plata';
DELETE FROM materiales WHERE nombre = 'Cobre';

-- Verificar materiales restantes
SELECT * FROM materiales;
