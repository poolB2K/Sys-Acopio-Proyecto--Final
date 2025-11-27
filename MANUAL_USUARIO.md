# Manual de Usuario - SysAcopio

Bienvenido al manual de usuario de **SysAcopio**, el sistema integral para la gestión de acopio de minerales. Este documento le guiará a través de las funcionalidades principales del sistema.

## Tabla de Contenidos
1. [Acceso al Sistema](#1-acceso-al-sistema)
2. [Pantalla Principal](#2-pantalla-principal)
3. [Gestión de Proveedores](#3-gestión-de-proveedores)
4. [Registro de Acopio](#4-registro-de-acopio)
5. [Historial de Movimientos](#5-historial-de-movimientos)

---

## 1. Acceso al Sistema

Para ingresar al sistema, debe autenticarse con sus credenciales.

1.  Ejecute la aplicación.
2.  En la pantalla de inicio de sesión, ingrese su **Usuario** y **Contraseña**.
3.  Haga clic en el botón **Ingresar** o presione la tecla `Enter`.
4.  Si los datos son correctos, accederá a la pantalla principal.

> **Nota:** Si olvida su contraseña, contacte al administrador del sistema.

---

## 2. Pantalla Principal

Una vez dentro, verá la interfaz principal del sistema, que consta de:

*   **Barra Superior:** Muestra el nombre del usuario conectado y la fecha/hora actual.
*   **Menú de Navegación:** Botones para acceder a las diferentes secciones:
    *   **Nuevo Acopio:** Para registrar una nueva transacción.
    *   **Proveedores:** Para gestionar la base de datos de proveedores.
    *   **Historial:** Para ver los registros de movimientos y auditoría.
    *   **Cerrar Sesión:** Para salir de su cuenta.
    *   **Salir:** Para cerrar la aplicación completamente.

---

## 3. Gestión de Proveedores

En esta sección puede administrar la información de los proveedores (mineros o empresas).

### Registrar Nuevo Proveedor
1.  Haga clic en el botón **Nuevo** para limpiar el formulario.
2.  Seleccione el **Tipo de Documento** (DNI, RUC, etc.).
3.  Ingrese el **Número de Documento**.
    *   *Tip:* Si es DNI o RUC, puede usar el botón **Consultar** (lupa) para obtener los datos automáticamente desde RENIEC/SUNAT.
4.  Complete o verifique los campos: Nombres, Apellidos, Dirección, Teléfono, Email, Ciudad.
5.  Agregue observaciones si es necesario.
6.  Haga clic en **Guardar**.

### Buscar y Editar Proveedor
1.  Use la barra de búsqueda en la parte superior para encontrar un proveedor por nombre.
2.  Seleccione al proveedor en la lista (tabla).
3.  Sus datos aparecerán en el formulario de la izquierda.
4.  Modifique la información necesaria y haga clic en **Guardar**.

### Desactivar Proveedor
1.  Seleccione al proveedor en la lista.
2.  Haga clic en el botón **Eliminar** (esto desactivará al proveedor, no borrará sus datos históricos).

---

## 4. Registro de Acopio

Esta es la función principal para registrar la compra/acopio de material.

### Pasos para registrar un acopio:
1.  **Seleccionar Proveedor:** Elija al proveedor de la lista desplegable.
2.  **Fecha:** Verifique la fecha (por defecto es hoy).
3.  **Agregar Detalles (Materiales):**
    *   Seleccione el **Material** (ej. Mineral Aurífero).
    *   Ingrese el **Peso**.
    *   Ingrese la **Ley**.
    *   Verifique/Ajuste la **Deducción** (Defecto: 0.10).
    *   Ingrese el **Precio Internacional (Onza)**.
    *   Verifique el **Tipo de Cambio**.
    *   *El sistema calculará automáticamente el precio por gramo y el total.*
    *   Haga clic en **Agregar Detalle**.
    *   Repita si hay más materiales en el mismo acopio.
4.  **Revisar Total:** Verifique el monto total en la parte inferior.
5.  **Observaciones:** Agregue notas si es necesario.
6.  **Guardar:** Haga clic en **Guardar Acopio**.
7.  **Imprimir:** El sistema le preguntará si desea imprimir el voucher.
    *   Si acepta, se enviará a la impresora predeterminada.
    *   Si no hay impresora, se mostrará en pantalla.

---

## 5. Historial de Movimientos

Permite auditar las acciones realizadas en el sistema.

*   **Filtros:**
    *   **Fecha Inicio / Fin:** Defina un rango de fechas.
    *   **Módulo:** Filtre por área (Acopio, Proveedor, Sistema, etc.).
    *   **Buscar:** Busque por palabras clave en la descripción.
*   **Botones Rápidos:**
    *   **Ver Hoy:** Muestra movimientos del día actual.
    *   **Ver Recientes:** Muestra los últimos movimientos.
*   **Detalles:** Al seleccionar una fila en la tabla, verá la información completa en el panel inferior.

---

**Soporte Técnico**
Para reportar errores o solicitar ayuda, contacte al área de sistemas.
